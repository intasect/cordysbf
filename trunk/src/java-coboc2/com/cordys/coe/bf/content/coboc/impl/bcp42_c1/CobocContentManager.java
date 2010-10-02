/**
 * Copyright 2006 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys Build Framework. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cordys.coe.bf.content.coboc.impl.bcp42_c1;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.config.CobocConfig;
import com.cordys.coe.bf.config.CobocObjectIdMap;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentMananger;
import com.cordys.coe.bf.content.base.IContentReadMethod;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentStore;
import com.cordys.coe.bf.content.base.IContentWriteMethod;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem;
import com.cordys.coe.bf.content.base.impl.SimpleContentStore;
import com.cordys.coe.bf.content.types.EContentCategory;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.coboc.mdm.Entity;
import com.cordys.coe.bf.content.types.coboc.mdm.EntityFolder;
import com.cordys.coe.bf.content.types.coboc.rules.Rule;
import com.cordys.coe.bf.content.types.xmlstore.XmlStoreContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.bf.utils.DependencyGraph;
import com.cordys.coe.util.log.LogInterface;
import com.cordys.tools.ant.cm.EBcpVersion;
import com.cordys.tools.ant.util.GeneralUtils;
import com.eibus.util.system.Native;


/**
 * Handles CoBOC content.
 *
 * @author mpoyhone
 */
public class CobocContentManager implements IContentMananger
{
    private BFContext bcContext;
    
    /**
     * Contains a handle for the root folder.
     */
    CobocContentHandle chRootFolderHandle;
    
    /**
     * Constructor for CobocContentManager
     */
    public CobocContentManager() {
        chRootFolderHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_FOLDER);
        chRootFolderHandle.setKey(CobocConstants.ROOT_FOLDER_KEY);
        chRootFolderHandle.setObjectId(CobocConstants.ROOT_FOLDER_ID);
    }
    
    /**
     * Sets the CoBOC content root directory.
     * @param fRootDir Directory.
     * @throws BFException Thrown if the operation failed.
     */
    public void setContentRootDirectory(File fRootDir) throws BFException {
        ContentSourceFileSystem csSource = (ContentSourceFileSystem) bcContext.getContentSource(EContentSourceType.FILESYSTEM);
        
        if (csSource == null) {
            throw new BFException("File system content source is not set.");
        }
        
        File fFoldersDir = new File(fRootDir, "folders");
        File fRulesDir = new File(fRootDir, "rules");
        File fSchedulesDir = new File(fRootDir, "schedules");
        File fInboxModelsDir = new File(fRootDir, "inbox-models");
        File fMessageTemplatesDir = new File(fRootDir, "message-templates");
        File fMdmDir = fFoldersDir;
        
        for (EContentType ctType : EContentCategory.COBOC_FOLDERS.getContentTypes())
        {
            csSource.setContentRootDirectory(ctType, fFoldersDir);
        }
        
        for (EContentType ctType : EContentCategory.COBOC_RULES.getContentTypes())
        {
            csSource.setContentRootDirectory(ctType, fRulesDir);
        }

        for (EContentType ctType : EContentCategory.COBOC_SCHEDULES.getContentTypes())
        {
            csSource.setContentRootDirectory(ctType, fSchedulesDir);
        }
        
        if (bcContext.isVersionLaterThan(EBcpVersion.BCP42_C3)) {
            for (EContentType ctType : EContentCategory.COBOC_MESSAGE_MODELS.getContentTypes())
            {
                csSource.setContentRootDirectory(ctType, fMessageTemplatesDir);
            }
        }
         
        csSource.setContentRootDirectory(EContentType.COBOC_INBOX_MODEL_C1, fInboxModelsDir);

        for (EContentType ctType : EContentCategory.MDM.getContentTypes())
        {
            csSource.setContentRootDirectory(ctType, fMdmDir);
        }
        
        bcContext.getConfig().getCobocConfig().setObjectIdMappingFile(new File(fRootDir, CobocConstants.COBOC_OBJECT_ID_MAP_FILE));
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentMananger#initialize(com.cordys.coe.bf.BFContext)
     */
    public void initialize(BFContext bcContext) throws BFException
    {
        this.bcContext = bcContext;
        
        // Create content stores and register them into the context.
        for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
        {
            IContentStore csContentStore;
            
            switch (ctType) {
            case COBOC_FOLDERS_ACTIONTEMPLATE_XFORM :
            case COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM :
                csContentStore = new SimpleContentStore();
                break;
            
            default :
                csContentStore = new CobocContentStore();
                break;
            }
            
            bcContext.setContentStore(ctType, csContentStore);
        }
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentMananger#uninitialize(com.cordys.coe.bf.BFContext)
     */
    public void uninitialize(BFContext bcContext) throws BFException
    {
        this.bcContext = null;
        
        // Remove content stores from the context.
        for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
        {
            IContentStore csContentStore;
            
            csContentStore = bcContext.getContentStore(ctType);
            if (csContentStore != null) {
                bcContext.setContentStore(ctType, null);
                csContentStore.clear();
            }
        }     
    }    
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentMananger#readContentFromSource(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void readContentFromSource(IContentSource csSource)
            throws BFException
    {
        LogInterface liLogger = bcContext.getLogger();
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Starting to read all CoBOC content from " + csSource.getType().getLogName());
        }
        
        CobocConfig cobocConfig = bcContext.getConfig().getCobocConfig();
        
        if (cobocConfig.isContentCategoryEnabled(EContentCategory.COBOC_FOLDERS)) {
            // Read folder objects
            readFolderObjects(csSource);
        }
        
        if (cobocConfig.isContentCategoryEnabled(EContentCategory.COBOC_RULES)) {
            // Read rules.
            readRules(csSource);
        }
        
        if (cobocConfig.isContentCategoryEnabled(EContentCategory.COBOC_SCHEDULES)) {
            // Read schedule templates.
            readScheduleTemplates(csSource);
        }
        
        if (cobocConfig.isContentCategoryEnabled(EContentCategory.COBOC_MESSAGE_MODELS)) {
            // Read inbox message models.
            readMessageModels(csSource);
        }
        
        clearReadCaches();
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentMananger#readContentFromXml(com.cordys.coe.bf.content.base.IContentSource, com.cordys.coe.bf.content.base.IXmlSource)
     */
    public void readContentFromXml(IContentSource csContentSource, IXmlSource xsSource) throws BFException
    {
        OMElement oeRoot = xsSource.getOMElement();
        LogInterface liLogger = bcContext.getLogger();
        
        if (oeRoot == null) {
            throw new BFException("readObjectFromXML: Only OMElement supported for XML.");
        }
        
        List<IContent> lContentList = new ArrayList<IContent>(100); 
        
        for (OMElement oeElem = oeRoot.getFirstElement(); oeElem != null; oeElem = AxiomUtils.getNextSiblingElement(oeElem)) {
            IXmlSource xsElemSource = IXmlSource.Factory.newInstance(oeElem);
            EContentType ctType = csContentSource.getContentTypeFromXml(xsElemSource);
            
            if (ctType == null) {
                throw new BFException("Unknown CoBOC content XML received.");
            }
            
            IContentReadMethod crmReadMethod = csContentSource.getReadMethod(ctType);
            
            if (crmReadMethod == null) {
                throw new BFException("Unable to get " + ctType.getLogName() + 
                        " read method.");
            }
            
            List<IContent> lRes = crmReadMethod.readObjectsFromXml(xsElemSource);
            
            if (lRes == null || lRes.size() == 0) {
                throw new BFException("No CoBOC content defined in given XML.");
            } 
            
            for (IContent cContent : lRes)
            {
                bcContext.registerContent(cContent);
                cContent.onLoad(csContentSource);
                lContentList.add(cContent);
                
                if (liLogger.isDebugEnabled()) {
                    liLogger.debug("Read " + cContent.getLogName() + " from " + csContentSource.getType().getLogName());
                }       
            }
        }    
        
        // Update all refeneces.
        for (IContent cTmp : lContentList) {
            cTmp.updateReferences(csContentSource);
        }    
    }

    private void readFolderObjects(IContentSource csSource)  throws BFException {
        LogInterface liLogger = bcContext.getLogger();
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Reading CoBOC folder objects from " + csSource.getType().getLogName());
        }        
        
        // Fetch the content recursively.
        IContentReadMethod crmMethod = csSource.getReadMethod(EContentType.COBOC_FOLDERS_FOLDER);
        List<IContent> lContent = crmMethod.readObjects(chRootFolderHandle, true, true);
        
        for (IContent cTmp : lContent)
        {
            if (liLogger.isDebugEnabled()) 
            {
                liLogger.debug("Read CoBOC " + cTmp.getLogName());
            }       
            
            bcContext.registerContent(cTmp);
            cTmp.onLoad(csSource);
        }
        
        // Update all refeneces in objects.
        for (IContent cTmp : lContent) {
            cTmp.updateReferences(csSource);
        }        
    }
    
    private void readRules(IContentSource csSource)  throws BFException {
        LogInterface liLogger = bcContext.getLogger();
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Reading CoBOC rules from " + csSource.getType().getLogName());
        }             
        
        // Fetch the rule groups and rules when reading from file system.
        IContentReadMethod crmRuleGroupMethod = csSource.getReadMethod(EContentType.COBOC_RULES_RULEGROUP);
        List<IContent> lRuleGroups = crmRuleGroupMethod.readObjects(EContentType.COBOC_RULES_RULEGROUP, true);
        
        for (IContent cTmp : lRuleGroups)
        {
            EContentType ctTmpType = cTmp.getType();
            
            if (liLogger.isDebugEnabled()) 
            {
                liLogger.debug("Read CoBOC " + ctTmpType.getLogName() + " " + cTmp.getHandle().getLogName());
            }            
            
            // Insert the rule group (or rule in case of the file system).
            bcContext.registerContent(cTmp);
            cTmp.onLoad(csSource);
        }
         
        // Fetch the rule group rules.
        IContentReadMethod crmRuleMethod = csSource.getReadMethod(EContentType.COBOC_RULES_RULE);
        List<IContent> lRules = crmRuleMethod.readObjects(EContentType.COBOC_RULES_RULE, true);
        
        for (IContent cTmp : lRules)
        {
            Rule rRule = (Rule) cTmp;
            EContentType ctRuleType = rRule.getType();
            
            if (liLogger.isDebugEnabled()) 
            {
                liLogger.debug("Read CoBOC " + ctRuleType.getLogName() + " " + rRule.getHandle().getLogName());
            }    
            
            rRule.findRuleGroup(bcContext.getContentStore(EContentType.COBOC_RULES_RULEGROUP));
            rRule.generateKey();
            bcContext.registerContent(rRule);
            cTmp.onLoad(csSource);
        }
        
        // Update all refeneces.
        for (IContent cTmp : lRuleGroups) {
            cTmp.updateReferences(csSource);
        }          
        
        // Update all refeneces.
        for (IContent cTmp : lRules) {
            cTmp.updateReferences(csSource);
        }         
    }
    
    private void readScheduleTemplates(IContentSource csSource)  throws BFException {
        LogInterface liLogger = bcContext.getLogger();
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Reading CoBOC schedule templates from " + csSource.getType().getLogName());
        }             
        
        // Fetch the schedule templates.
        IContentReadMethod crmScheduleTemplateMethod = csSource.getReadMethod(EContentType.COBOC_SCHEDULE_TEMPLATE);
        List<IContent> lScheduleTemplates = crmScheduleTemplateMethod.readObjects(EContentType.COBOC_SCHEDULE_TEMPLATE, true);
        
        for (IContent cTmp : lScheduleTemplates)
        {
            EContentType ctTmpType = cTmp.getType();
            
            if (liLogger.isDebugEnabled()) 
            {
                liLogger.debug("Read CoBOC " + ctTmpType.getLogName() + " " + cTmp.getHandle().getLogName());
            }            
            
            // Insert the schedule template
            bcContext.registerContent(cTmp);
            cTmp.onLoad(csSource);
        }
    }    
    

    private void readMessageModels(IContentSource csSource)  throws BFException {
        readMessageModels_C1(csSource);
        
        if (bcContext.isVersionLaterThan(EBcpVersion.BCP42_C3)) {
            readMessageModels_C3(csSource);
        }
    }    
    
    
    /**
     * C3 message template, inbox model and email model method.
     */
    private void readMessageModels_C3(IContentSource csSource)  throws BFException {
        LogInterface liLogger = bcContext.getLogger();
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Reading CoBOC message models from " + csSource.getType().getLogName());
        }             
        
        // Fetch the message models
        IContentReadMethod crmMessageModelMethod = csSource.getReadMethod(EContentType.COBOC_MESSAGE_TEMPLATE);
        List<IContent> lMessageModels = crmMessageModelMethod.readObjects(EContentType.COBOC_MESSAGE_TEMPLATE, true);
        
        for (IContent cTmp : lMessageModels)
        {
            EContentType ctTmpType = cTmp.getType();
            
            if (liLogger.isDebugEnabled()) 
            {
                liLogger.debug("Read CoBOC " + ctTmpType.getLogName() + " " + cTmp.getHandle().getLogName());
            }            
            
            // Insert the message mode.
            bcContext.registerContent(cTmp);
            cTmp.onLoad(csSource);
        }
        
        // Update all refeneces.
        for (IContent cTmp : lMessageModels) {
            cTmp.updateReferences(csSource);
        }    
        
        // Fetch the inbox and email  models.
        for (IContent cTmp : lMessageModels)
        {
            IContentReadMethod crmMethod = csSource.getReadMethod(cTmp.getType());
            List<IContent> children = crmMethod.readObjects(cTmp.getHandle(), true, true);
            
            for (IContent child : children)
            {
                // Insert the message mode.
                child.onLoad(csSource);
                
                if (liLogger.isDebugEnabled()) 
                {
                    liLogger.debug("Read CoBOC " + child.getType().getLogName() + " " + child.getHandle().getLogName());
                }                        
            }
            
            // Update all references.
            for (IContent child : children) {
                child.updateReferences(csSource);
                bcContext.registerContent(child);
            }                
        }        
    }    
    
    /**
     * C1 and C2 inbox model method.
     */
    private void readMessageModels_C1(IContentSource csSource)  throws BFException {
        LogInterface liLogger = bcContext.getLogger();
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Reading CoBOC message models from " + csSource.getType().getLogName());
        }             
        
        // Fetch the message models
        IContentReadMethod crmMessageModelMethod = csSource.getReadMethod(EContentType.COBOC_INBOX_MODEL_C1);
        List<IContent> lMessageModels = crmMessageModelMethod.readObjects(EContentType.COBOC_INBOX_MODEL_C1, true);
        
        for (IContent cTmp : lMessageModels)
        {
            EContentType ctTmpType = cTmp.getType();
            
            if (liLogger.isDebugEnabled()) 
            {
                liLogger.debug("Read CoBOC " + ctTmpType.getLogName() + " " + cTmp.getHandle().getLogName());
            }            
            
            // Insert the message mode.
            bcContext.registerContent(cTmp);
            cTmp.onLoad(csSource);
        }
    }   

    /**
     * @see com.cordys.coe.bf.content.base.IContentMananger#writeContentToSource(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void writeContentToSource(IContentSource csDestination)
            throws BFException
    {
        LogInterface liLogger = bcContext.getLogger();
        
        // BCP needs special care to have the objects ID's resolved right.
        if (csDestination.getType() == EContentSourceType.BCP) {
            writeContentToBcp(csDestination);
            return;
        } else if (csDestination.getType() == EContentSourceType.ISV) {
            // Also ISV package needs the objects ID's to be calculated.
            writeContentToIsv(csDestination);
            return;
        }
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Starting to write all CoBOC content to " + csDestination.getType().getLogName());
        }              
        
        // MDM entity folder names need to be renamed before they are written to file system.
        renameMdmEntityReferences(csDestination);
        
        // Write all the objects. For all the other types no old object fetching is needed.
        for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
        {        
            if (! bcContext.getConfig().getCobocConfig().isContentEnabled(ctType)) {
                // This type has been disabled in the configuration. 
                continue;
            }  
            
            if (liLogger.isDebugEnabled()) {
                liLogger.debug("Writing objects of type " + ctType.getLogName());
            }               
            
            Collection<IContent> cCurrentContentList = bcContext.getContentStore(ctType).getObjects();
            IContentWriteMethod cwmWriteMethod = csDestination.getWriteMethod(ctType);
        
            for (IContent cCurrent : cCurrentContentList)
            {       
                // Try to find the old version and create a write version of the current.
                IContent cCurrentWrite = cCurrent.createWriteVersion(csDestination);
                
                if (liLogger.isDebugEnabled()) {
                    liLogger.debug("Writing " + cCurrent.getHandle().getLogName());
                }                    
                
                // Write the current and old (if exists) objects to the destination.
                cwmWriteMethod.insertObject(cCurrentWrite, false);
            }
        }
        
        // Check if we have to write the object ID mapping file.
        if (csDestination.getType() == EContentSourceType.FILESYSTEM) {
            CobocConfig cobocConfig = bcContext.getConfig().getCobocConfig();
            CobocObjectIdMap objectIdMap = cobocConfig.getObjectIdMap();
            
            if (objectIdMap != null) {
                File contentFile = cobocConfig.getObjectIdMappingFile();
                
                liLogger.info("Writing object ID mappings to file: " + contentFile);
                
                // First try to load the old ones.
                if (contentFile.exists()) {
                    try {
                        objectIdMap.loadFromFile(contentFile);
                    }
                    catch (Exception e) {
                        liLogger.error("Unable to load object ID mappings: ", e);
                    }
                }
                
                // Then find out the new values.
                for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
                {        
                    Collection<IContent> cCurrentContentList = bcContext.getContentStore(ctType).getObjects();
                
                    for (IContent c : cCurrentContentList)
                    {       
                        if (c instanceof CobocContentBase) {
                            CobocContentBase ccb = (CobocContentBase) c;
                            String key = ccb.getHandle().getKey();
                            
                            // Do not replace existing object ID's.
                            if (objectIdMap.getObjectId(ctType, key) == null) {
                                objectIdMap.addEntry(ctType, key, ccb.getHandle().getObjectId());
                            }
                        }
                    }
                }
                     
                // Finally, write the file.
                try {
                    File parent = contentFile.getParentFile();
                    
                    if (! parent.exists()) {
                        parent.mkdirs();
                    }
                    
                    objectIdMap.writeToFile(contentFile);
                }
                catch (Exception e) {
                    liLogger.error("Unable to write object ID mappings.", e);
                }
            }
        }
    }
    
    protected void writeContentToIsv(IContentSource csDestination) throws BFException {    
        LogInterface liLogger = bcContext.getLogger();
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Preparing to write all CoBOC content to " + csDestination.getType().getLogName());
        }
        
        // Generated new object ID's for all content objects.
        Set<String> sGeneratedObjectIds = new HashSet<String>();
        
        for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
        {
            if (ctType == EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM ||
                ctType == EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM) {
                continue;
            }
            
            Collection<IContent> cCurrentContentList = bcContext.getContentStore(ctType).getObjects();

            for (IContent cContent : cCurrentContentList)
            {
                String sNewId = generateCobocObjectId(cContent, sGeneratedObjectIds);
                
                ((CobocContentBase) cContent).setObjectId(sNewId);
            }
        }
        sGeneratedObjectIds.clear();
        
        // Update all references in the objects to the generated object ID's.
        for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
        {        
            Collection<IContent> cCurrentContentList = bcContext.getContentStore(ctType).getObjects();
        
            for (IContent cCurrent : cCurrentContentList)
            {       
                cCurrent.updateReferences(csDestination);
            }
        }    
        
        // Find all the dependencies just to have the right ISV order. This might not be needed
        // as objects are separated in the XML but at least the gives the right order inside the sections.
        DependencyGraph<IContentHandle> dgDependencies = new DependencyGraph<IContentHandle>();
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Resolving CoBOC content to dependencies");
        }        
        
        for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
        {        
            Collection<IContent> cCurrentContentList = bcContext.getContentStore(ctType).getObjects();
        
            for (IContent cObj : cCurrentContentList)
            {  
                Collection<IContentHandle> cRefecences = cObj.getReferencedContent();
                
                // Check that the handles are valid.
                for (IContentHandle chHandle : cRefecences)
                {
                    if (chHandle == null || ! chHandle.isSet()) {
                        throw new BFException(ctType.getLogName() + " " + cObj.getHandle().getLogName() + " returned an empty handle.");
                    }
                    
                    if (chHandle instanceof CobocContentHandle) {
                        CobocContentHandle cchCobocHandle = (CobocContentHandle) chHandle;
                        
                        if (cchCobocHandle.getContentId() == null) {
                            throw new BFException(ctType.getLogName() + " " + cObj.getHandle().getLogName() + " returned a handle with no object ID.");
                        }

                        if (cchCobocHandle.getKey() == null) {
                            throw new BFException(ctType.getLogName() + " " + cObj.getHandle().getLogName() + " returned a handle with no key.");
                        }
                    }
                }
                
                dgDependencies.add(cObj.getHandle(), cRefecences);
            }
        }
        
        Collection<IContentHandle> lDependencyList = dgDependencies.getDepencencies(); 

        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Starting to write all CoBOC content to " + csDestination.getType().getLogName());
            liLogger.debug("ISV Content order is " + lDependencyList);
        }        
        
        // Get write method. This method should be able to write all every of CoBOC content.
        IContentWriteMethod cwmWriteMethod = csDestination.getWriteMethod(EContentType.COBOC_FOLDERS_FOLDER);
        
        // Create the ISV package list.
        List<ContentTuple> lIsvpList = new ArrayList<ContentTuple>(lDependencyList.size());
        
        for (IContentHandle chHandle : lDependencyList)
        {       
            if (chHandle.getContentType() == EContentType.COBOC_FOLDERS_FOLDER) {
                CobocContentHandle cchFolderHandle = (CobocContentHandle) chHandle;
                
                if ("/".equals(cchFolderHandle.getKey())) {
                    // Skip the root folder.
                    continue;
                }
            }
            
            IContent cContent = bcContext.findContent(chHandle);
            
            if (cContent == null) {
                throw new BFException(csDestination.getType().getLogName() + ": Content not found with handle " + chHandle);
            }
            
            cContent = cContent.createWriteVersion(csDestination);
            
            lIsvpList.add(new ContentTuple(null, cContent));
        }
        
        // Write the content to the ISV package.
        cwmWriteMethod.updateObjects(lIsvpList);
    }
    
    protected String generateCobocObjectId(IContent content, Set<String> sGeneratedObjectIds) throws BFException {
        LogInterface liLogger = bcContext.getLogger();
        String sId;

        if (content != null && bcContext.getConfig().getCobocConfig().usesOriginalId(content)) {
            String origId = ((CobocContentBase) content).getOriginalObjectId();
            
            if (origId == null || origId.length() == 0)
            {
                throw new BFException("Original object ID not set for " + content.getLogName());
            }
            
            sId = origId;
            
            if (sGeneratedObjectIds.contains(origId)) {
                throw new BFException("Object ID is already defined: " + origId);
            }
            
            liLogger.debug("Using original ID '" + origId + " for " + content.getLogName());
        } else {
            if (content instanceof CobocContentBase) {
                // Check if we are using object ID mapping file.
                CobocObjectIdMap objectIdMap = bcContext.getConfig().getCobocConfig().getObjectIdMap();
                String key = ((CobocContentBase) content).getKey();
                
                if (objectIdMap != null) {
                    String id = objectIdMap.getObjectId(content.getType(), key);
                    
                    if (id != null) {
                        return id;
                    }
                     
                    liLogger.error("Unable to find a mapped object ID for " + content.getLogName());
                }
            }
            
            boolean useGuid = bcContext.getConfig().getVersionInfo().isLaterThan(EBcpVersion.BCP42_C3);
    
            do
            {
                if (! useGuid) {
                    sId = System.currentTimeMillis() + "" +
                            ((int) (Math.random() * 100));
                } else {
                    sId = Native.createGuid();
                }
            }
            while (sGeneratedObjectIds.contains(sId));
        }    

        sGeneratedObjectIds.add(sId);
        
        return sId;        
    }
    
    /**
     * Renames MDM entity and entity folder references for the given destination.
     * Entities are store in CoBOC under a folder that has the backend ID, but on
     * the file system the backend name is used.
     * @param csDestination Destination that determines the entity folder name.
     * @throws BFException 
     */
    protected void renameMdmEntityReferences(IContentSource csDestination) throws BFException {
        // Rename entity folders
        for (IContent efEntityFolder : bcContext.getContentStore(EContentType.MDM_ENTITY_FOLDER).getObjects())
        {
            renameMdmEntityReferences((EntityFolder) efEntityFolder, csDestination);
        }
        
        // Rename entities.
        for (IContent eEntity : bcContext.getContentStore(EContentType.MDM_ENTITY).getObjects())
        {
            renameMdmEntityReferences((Entity)eEntity, csDestination);
        }
    }
    
    /**
     * Renames MDM entity and entity folder references for the given destination.
     * Entities are store in CoBOC under a folder that has the backend ID, but on
     * the file system the backend name is used.
     * @param csDestination Destination that determines the entity folder name.
     * @throws BFException 
     */
    protected void renameMdmEntityReferences(Entity eEntity, IContentSource csDestination) throws BFException {
        IContentHandle chOldHandle = eEntity.getHandle().cloneHandle();
        
        // Change the key to match the destination.
        try
        {
            eEntity.changeKeyFor(csDestination.getType());
        }
        catch (BFException e)
        {
            return;
        }
        
        // Update all the references to this object to match the new key
        bcContext.renameObjectReferences(chOldHandle, eEntity.getHandle());
    }
    
    /**
     * Renames MDM entity and entity folder references for the given destination.
     * Entities are store in CoBOC under a folder that has the backend ID, but on
     * the file system the backend name is used.
     * @param csDestination Destination that determines the entity folder name.
     * @throws BFException 
     */
    protected void renameMdmEntityReferences(EntityFolder efEntityFolder, IContentSource csDestination) throws BFException {
        IContentHandle chOldHandle = efEntityFolder.getHandle().cloneHandle();
        
        // Change the key to match the destination.
        try
        {
            efEntityFolder.changeKeyFor(csDestination.getType());
        }
        catch (BFException e)
        {
            return;
        }
        
        // Update all the references to this object to match the new key
        bcContext.renameObjectReferences(chOldHandle, efEntityFolder.getHandle());
    }
    
    protected void writeContentToBcp(IContentSource csDestination)
        throws BFException {    
        LogInterface liLogger = bcContext.getLogger();
        boolean bMdmInstalled;
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Preparing to write all CoBOC content to " + csDestination.getType().getLogName());
        }

        // MDM is not supported at the moment.
        bMdmInstalled = false;
        /*
        // Check if MDM is installed in the current organization.
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Checking if MDM is installed in the organization.");
        }
        
        bMdmInstalled = checkMdmInstallation(csDestination);
        
        if (bMdmInstalled) {
            if (liLogger.isDebugEnabled()) {
                liLogger.debug("MDM is installed.");
            }
        } else {
            if (liLogger.isInfoEnabled()) {
                liLogger.info("MDM is not installed in this organization. Skipping all MDM content.");
            }
        }*/
        
        // This holds all the old versions of current objects.
        Map<EContentType, IContentStore> mOldContentStoreMap = new HashMap<EContentType, IContentStore>();
        
        // First get all the old versions for the objects that will be inserted. This
        // also allows us to get the object ID and update it in the current objects.
        for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
        {
            if (! bMdmInstalled && ctType.getCategory() == EContentCategory.MDM) {
                continue;
            }
            
            if (! bcContext.getConfig().getCobocConfig().isContentEnabled(ctType)) {
                // This type has been disabled in the configuration. 
                continue;
            }            
            
            if (liLogger.isDebugEnabled()) {
                liLogger.debug("Reading old versions of type " + ctType.getLogName());
            }                
            
            switch (ctType) {
            case COBOC_FOLDERS_ACTIONTEMPLATE_XFORM :
            case COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM :
                mOldContentStoreMap.put(ctType, new SimpleContentStore());
                break;
            
            default :
                mOldContentStoreMap.put(ctType, new CobocContentStore());
                break;
            }

            // Get all the current content of this type. 
            Collection<IContent> cCurrentContentList = bcContext.getContentStore(ctType).getObjects();
            IContentReadMethod crmReadMethod = csDestination.getReadMethod(ctType);
            
            // Disable filtering from the read method. This is needed for getting right object ID's
            // for references event thought the referred content might be filtered out.
            crmReadMethod.setContentFilterStatus(false);

            // For each version try read the old version.
            for (IContent cCurrent : cCurrentContentList)
            {
                IContentHandle cCurrentHandle = cCurrent.getHandleForContentSource(csDestination);
                
                // MDM entity folder handles need to use the 'object ID' key.
                if (bMdmInstalled &&
                    ctType.getCategory() == EContentCategory.MDM) {
                    try
                    {
                        switch (cCurrent.getType()) {
                        case MDM_ENTITY :
                            ((Entity) cCurrent).updateReferences(csDestination);
                            cCurrentHandle = ((Entity) cCurrent).getHandleForSource(csDestination.getType());
                            break;

                        case MDM_ENTITY_FOLDER :
                            ((EntityFolder) cCurrent).updateReferences(csDestination);
                            cCurrentHandle = ((EntityFolder) cCurrent).getHandleForSource(csDestination.getType());
                            break;
                        }
                    }
                    catch (BFException e)
                    {
                        // The backend ID was not set, so just don't get the old version.
                        continue;
                    }
                }
                
                if (ctType == EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM ||
                    ctType == EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM) {
                    // We need to read the content from isv level.
                    crmReadMethod.setContentVersion("isv");
                }
                
                IContent cOld = crmReadMethod.readObject(cCurrentHandle);
                
                if (cOld != null) {
                    if (liLogger.isDebugEnabled()) {
                        if (cOld instanceof CobocContentBase) {
                            liLogger.debug("Found " + cOld.getHandle().getLogName() + " with ID " + ((CobocContentBase) cOld).getObjectId());
                        } else {
                            liLogger.debug("Found " + cOld.getHandle().getLogName() + " with ID " + cOld.getHandle());
                        }
                    }                    
                    
                    mOldContentStoreMap.get(ctType).insertObject(cOld);
                    
                    // Update needed fields from the old to the current for the update.
                    cCurrent.updateFromOldVersion(cOld, csDestination);
                }
            }
        }
        
        // Update all references in the current objects to content the object ID read from the old.
        // This is needed to update e.g. the parent handlers.
        for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
        {        
            if (! bMdmInstalled && ctType.getCategory() == EContentCategory.MDM) {
                continue;
            }            
            
            Collection<IContent> cCurrentContentList = bcContext.getContentStore(ctType).getObjects();
        
            for (IContent cCurrent : cCurrentContentList)
            {      
                cCurrent.updateReferences(csDestination);
            }
        }
        
        // MDM entity folder names need to be renamed before they are written to BCP.
        renameMdmEntityReferences(csDestination);
        
        // Clear the read method caches.
        clearReadCaches();
        
        // Find the right update/insert order by calculating the content dependencies. 
        DependencyGraph<IContentHandle> dgDependencies = new DependencyGraph<IContentHandle>();
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Resolving CoBOC content to dependencies");
        }        
        
        for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
        {        
            if (! bMdmInstalled && ctType.getCategory() == EContentCategory.MDM) {
                continue;
            }           
            
            if (! bcContext.getConfig().getCobocConfig().isContentEnabled(ctType)) {
                // This type has been disabled in the configuration. 
                continue;
            }                       
            
            Collection<IContent> cCurrentContentList = bcContext.getContentStore(ctType).getObjects();
        
            for (IContent cObj : cCurrentContentList)
            {
                // Check if this object is filtered out.
                String sUpdateKey;
                boolean bIsFolder = false;
                
                switch (ctType) {
                case COBOC_FOLDERS_FOLDER :
                case COBOC_RULES_RULEGROUP :
                    bIsFolder = true;
                    sUpdateKey = ((CobocContentHandle) cObj.getHandle()).getKey();
                    break;
                    
                case COBOC_FOLDERS_ACTIONTEMPLATE_XFORM :
                case COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM :
                    sUpdateKey = ((XmlStoreContentHandle) cObj.getHandle()).getKey();
                    break;

                default :
                    sUpdateKey = ((CobocContentHandle) cObj.getHandle()).getKey();
                    break;
                }
                
                if (! csDestination.checkForAccess(ctType, sUpdateKey, bIsFolder)) {
                    // This item is filtered out.
                    continue;
                }                     
                
                Collection<IContentHandle> cRefecences = cObj.getReferencedContent();
                
                // Check that the handles are valid.
                for (IContentHandle chHandle : cRefecences)
                {
                    if (chHandle == null || ! chHandle.isSet()) {
                        throw new BFException(cObj.getLogName() + " returned an empty handle.");
                    }
                }
                
                dgDependencies.add(cObj.getHandle(), cRefecences);
            }
        }
        
        Collection<IContentHandle> lUpdateList = dgDependencies.getDepencencies();

        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Starting to write all CoBOC content to " + csDestination.getType().getLogName());
            
            StringBuffer sbBuffer = new StringBuffer(1024);
            
            for (IContentHandle cchHandle : lUpdateList)
            {
                sbBuffer.append("\t").append(cchHandle.getLogName()).append("\n");
            }
            
            liLogger.debug("Content write order is:\n" + sbBuffer);
        }        
        
        // Update or insert all the objects. The dependency graph specifies the right update order.
        for (IContentHandle chCurrentHandle : lUpdateList)
        {        
            IContent cCurrent = bcContext.findContent(chCurrentHandle);
            
            if (cCurrent == null) {
                throw new BFException("Content not found with handle " + chCurrentHandle);
            }
            
            IContentHandle cCurrentDestHandle = cCurrent.getHandleForContentSource(csDestination);
            IContentWriteMethod cwmWriteMethod = csDestination.getWriteMethod(cCurrentDestHandle.getContentType());
            
            if (liLogger.isDebugEnabled()) {
                liLogger.debug("Writing " + cCurrentDestHandle.getLogName());
            }                  
            
            // Try to find the old version and create a write version of the current.
            IContent cOld = mOldContentStoreMap.get(cCurrentDestHandle.getContentType()).findObject(cCurrentDestHandle);
            
            // For inserts we need to resolve the references before writing the object.
            // All the referred objects must have been written prior to this one so 
            // they have the right object ID set at this point.
            if (cOld == null) {
                cCurrent.updateReferences(csDestination);
            }
            
            IContent cCurrentWrite = cCurrent.createWriteVersion(csDestination);
            IContent cWriteResult;
            
            // Write the current and old (if exists) objects to the destination.
            try
            {
                cWriteResult = cwmWriteMethod.updateObject(cOld, cCurrentWrite);
            }
            catch (BFException e)
            {
                handleBcpUpdateException(cCurrentWrite, e);
                return; // Never reached.
            }
            
            // For inserts we need to copy the returned object ID back to the current version.
            if (cOld == null) {
                if (cWriteResult == null) {
                    // Unsupported content was written.
                    continue;
                }
                
                if (cWriteResult instanceof CobocContentBase) {
                    String sObjectId = ((CobocContentBase) cWriteResult).getObjectId();
                    
                    if (sObjectId == null) {
                        throw new BFException("Insert result object ID is null.");
                    }     
                }
                
                cCurrent.updateFromNewVersion(cWriteResult, csDestination);
                
                // MDM entity folder names need to be renamed before they are written to BCP.
                if (bMdmInstalled &&
                    cCurrent.getType().getCategory() == EContentCategory.MDM) {
                    switch (cCurrent.getType()) {
                    case MDM_ENTITY :
                        renameMdmEntityReferences((Entity) cCurrent, csDestination);
                        break;

                    case MDM_ENTITY_FOLDER :
                        renameMdmEntityReferences((EntityFolder) cCurrent, csDestination);
                        break;
                        
                    case MDM_BACKEND :
                        // MDM entity folder names need to be renamed because this backend was inserted
                        // and all the entities and entity folders use the backend ID in the key.
                        renameMdmEntityReferences(csDestination);   
                        break;                    
                    }
                }
            }
        }        

        // Update all refeneces once more for the current objects because they were not set for inserted
        // objects.
        for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
        {        
            Collection<IContent> cCurrentContentList = bcContext.getContentStore(ctType).getObjects();
        
            for (IContent cCurrent : cCurrentContentList)
            {       
                cCurrent.updateReferences(csDestination);
            }
        }
    }
    
    /**
     * Handles a BCP update exception. This method tries to create a proper message based
     * on the SOAP fault.
     * @param e Thrown exception.
     */
    private void handleBcpUpdateException(IContent cContent, BFException e) throws BFException
    {
        String contentName = cContent.getType().getLogName() + " " + cContent.getHandle().getLogName();
        
        // First check missing inbox model participant field. 
        if (cContent.getType() == EContentType.COBOC_INBOX_MODEL_C1) {
            if (GeneralUtils.findMessageFromException("value\\s+for\\s+the\\s+.?PARTICIPANT.?\\s+element", "dummy", e) != null) {
                throw new BFException("Unable to insert/update the " +  contentName +
                                      ". The PARTICIPANT is probably invalid. Check that the role/user exists in the system.", e);
            }
        }
        
        // For these types, hint that the object might already exist in another organization.
        switch (cContent.getType()) {
        case COBOC_SCHEDULE_TEMPLATE :
        case COBOC_INBOX_MODEL_C1 :
            throw new BFException("Unable to insert/update the object " + contentName +
                                  ". Check that this does not already exist in another organization.", e);
        }

        // For others, just rethrow.
        throw e;
    }

    /**
     * Checks if MDM is installed in the current organization. This tries to read
     * MDM template '/cordys/mdm/modeler/templates/entity' and returns <code>true</code> if it was found. 
     * 
     * @param csSrc BCP content source.
     * @return <code>true</code> if MDM is installed.
     * 
     * @deprecated Not used anywhere
     */
    @Deprecated
    protected boolean checkMdmInstallation(IContentSource csSrc) throws BFException
    {
        CobocContentHandle cchHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_TEMPLATE);
        
        cchHandle.setKey("/cordys/mdm/modeler/templates/entity");
        
        IContentReadMethod crmMethod = csSrc.getReadMethod(cchHandle.getContentType());
        IContent cTemplate;

        // Disable the filters.
        crmMethod.setContentFilterStatus(false);
        
        // Try to read the template.
        cTemplate = crmMethod.readObject(cchHandle);
        
        return cTemplate != null;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentMananger#deleteContentFromSource(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void deleteContentFromSource(IContentSource csSource) throws BFException
    {
        LogInterface liLogger = bcContext.getLogger();
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Starting to delete all CoBOC content from " + csSource.getType().getLogName());
        }                

        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Deleting all folder objects.");
        }     
        
        boolean isC3 = bcContext.isVersionLaterThan(EBcpVersion.BCP42_C3);
        
        // Read all folder objects and delete them in the right order. We don't
        // calculate the dependency graph as it is not needed here.
        List<IContent> lFolderContentList = new LinkedList<IContent>();
        EContentType[] ctaDeleteOrder = new EContentType[] {
                EContentType.MDM_MODEL,
                EContentType.MDM_BACKEND,
                EContentType.MDM_ENTITY,
                EContentType.COBOC_FOLDERS_MAPPING,
                EContentType.COBOC_FOLDERS_PROCESSBPMN,
                isC3 ? EContentType.COBOC_FOLDERS_PROCESSBPML : null,
                EContentType.COBOC_FOLDERS_PROCESSTEMPLATE,
                EContentType.COBOC_FOLDERS_PROCESSINSTANCE,
                EContentType.COBOC_FOLDERS_CONTENTMAP,
                ! isC3 ? EContentType.COBOC_FOLDERS_DECISIONCASE : null,
                EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM,
                EContentType.COBOC_FOLDERS_ACTIONTEMPLATE,
                EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM,
                EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE,
                EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE,
                EContentType.COBOC_FOLDERS_TEMPLATE,
                EContentType.COBOC_FOLDERS_FOLDER,
                EContentType.MDM_ENTITY_FOLDER, 
        };
        
        readContentRecursively(csSource, chRootFolderHandle, lFolderContentList);
        
        // The content objects are now in reverse order in terms of the folder tree,
        // so we can just delete them in the right order according to the type delete order.
        for (EContentType ctType : ctaDeleteOrder)
        {
            if (ctType == null) {
                continue;
            }
            
            if (ctType == EContentType.COBOC_FOLDERS_FOLDER) {
                // Don't delete folders for now.
                continue;
            }
            
            for (IContent cContent : lFolderContentList)
            {
                if (cContent.getType() != ctType) {
                    continue;
                }
                
                IContentWriteMethod cwmWriteMethod = csSource.getWriteMethod(cContent.getType());
                
                if (cContent.getType() == EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM ||
                    cContent.getType() == EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM) {
                    // We need to delete the content from isv level.
                    cwmWriteMethod.setContentVersion("isv");
                }                
                
                cwmWriteMethod.deleteObject(cContent);
            }            
        }
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Deleting all rules.");
        }   

        deleteContentRecursively(csSource, EContentType.COBOC_RULES_RULEGROUP);
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Deleting all schedule templates.");
        }   

        deleteContentRecursively(csSource, EContentType.COBOC_SCHEDULE_TEMPLATE);        
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Deleting all message models.");
        }   

        deleteContentRecursively(csSource, EContentType.COBOC_INBOX_MODEL_C1);
        
        if (isC3) {
            deleteContentRecursively(csSource, EContentType.COBOC_MESSAGE_TEMPLATE);
        }
    }
    
    private boolean readContentRecursively(IContentSource csSource, IContentHandle chParentHandle, Collection<IContent> cRes) throws BFException {
        IContentReadMethod crmReadMethod = csSource.getReadMethod(chParentHandle.getContentType());
        List<IContent> lContent = crmReadMethod.readObjects(chParentHandle, false, false);
        boolean bHasFilteredContent = false;
    
        for (IContent cContent : lContent)
        {   
            bcContext.registerContent(cContent);
            cContent.onLoad(csSource);
        }
        
        for (IContent cContent : lContent)
        {   
            cContent.updateReferences(csSource);
        }
        
        for (IContent cContent : lContent)
        {   
            cContent.onLoad(csSource);
            cContent.updateReferences(csSource);
            
            EContentType ctType = cContent.getType();
            String sKey = cContent.getHandle().getContentId();
            boolean bIsFolder = ctType == EContentType.COBOC_FOLDERS_FOLDER;
            
            if (! csSource.checkForAccess(ctType, sKey, bIsFolder)) {
                // This content object is filtered out.
                bHasFilteredContent = true;
                continue;
            }            
            
            if (! readContentRecursively(csSource, cContent.getHandle(), cRes)) {
                cRes.add(cContent);
            } else {
                bHasFilteredContent = true;
            }
        }        
        
        return bHasFilteredContent;
    }
    
    private void deleteContentRecursively(IContentSource csSource, IContentHandle chParentHandle) throws BFException {
        IContentReadMethod crmReadMethod = csSource.getReadMethod(chParentHandle.getContentType());
        IContentWriteMethod cwmWriteMethod = csSource.getWriteMethod(chParentHandle.getContentType());
        List<IContent> lContent = crmReadMethod.readObjects(chParentHandle, false, false);
        
        for (IContent cContent : lContent)
        {   
            deleteContentRecursively(csSource, cContent.getHandle());
            cwmWriteMethod.deleteObject(cContent);
        }        
    }    
    
    private void deleteContentRecursively(IContentSource csSource, EContentType ctParentType) throws BFException {
        IContentReadMethod crmReadMethod = csSource.getReadMethod(ctParentType);
        IContentWriteMethod cwmWriteMethod = csSource.getWriteMethod(ctParentType);
        List<IContent> lContent = crmReadMethod.readObjects(ctParentType, false);
    
        for (IContent cContent : lContent)
        {   
            deleteContentRecursively(csSource, cContent.getHandle());
            cwmWriteMethod.deleteObject(cContent);
        }        
    }
    
    private void clearReadCaches() {
        BcpReadMethod_CobocRules.clearCaches();
        FileSystemReadMethod_CobocRules.clearCaches();
        BcpReadMethod_CoboScheduleTemplates.clearCaches();
        BcpReadMethod_MessageModels.clearCaches();
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentMananger#transfrerContent(com.cordys.coe.bf.content.base.IContentSource, com.cordys.coe.bf.content.base.IContentSource, boolean)
     */
    public void transfrerContent(IContentSource csSource,
            IContentSource csDestination, boolean bLeaveContentInContext)
            throws BFException
    {
        readContentFromSource(csSource);
        writeContentToSource(csDestination);
        
        if (! bLeaveContentInContext) {
            for (EContentType ctType : EContentCategory.COBOC_FOLDERS.getContentTypes())
            {
                IContentStore csContentStore;
                
                csContentStore = bcContext.getContentStore(ctType);
                if (csContentStore != null) {
                    csContentStore.clear();
                }
            }   
        }
    }
}
