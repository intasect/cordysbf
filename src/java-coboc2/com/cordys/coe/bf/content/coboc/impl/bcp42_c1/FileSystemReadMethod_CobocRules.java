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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentStore;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem;
import com.cordys.coe.bf.content.base.impl.FileSystemReadMethodBase;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.rules.Rule;
import com.cordys.coe.bf.content.types.coboc.rules.RuleGroup;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.LogInterface;

/**
 * Implements a read method for CoBOC rule content.
 *
 * @author mpoyhone
 */
public class FileSystemReadMethod_CobocRules extends FileSystemReadMethodBase
{
    private static IContentStore csRuleCache = null;
    private static IContentStore csRuleGroupCache = null;
    
    public FileSystemReadMethod_CobocRules(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceFileSystem) csSource);
    }
    
    public static void clearCaches() {
        if (csRuleCache != null) {
            csRuleCache.clear();
            csRuleCache = null;
        }
        
        if (csRuleGroupCache != null) {
            csRuleGroupCache.clear();
            csRuleGroupCache = null;
        }        
    }
    
    /**
     * Reads all CoBOC rules or rule groups.
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(EContentType, boolean)
     */
    public List<IContent> readObjects(EContentType ctType, boolean bReadFully) throws BFException
    {
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        if (csRuleCache == null || csRuleGroupCache == null) {
            readAllIntoCaches();
        }
        
        Collection<IContent> cContentList;
        
        switch (ctType) {
        case COBOC_RULES_RULEGROUP :
            cContentList = csRuleGroupCache.getObjects();
            break;
            
        case COBOC_RULES_RULE :
            cContentList = csRuleCache.getObjects();
            break;
            
        default:
            throw new BFException("CoBOC rule method content type must be rule or rule group. Got: " + ctType.getLogName());
        }
        
        return new ArrayList<IContent>(cContentList);
    }
    
    /**
     * This method does nothing as all rules and rule groups are read by the other list method. 
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle, boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive) throws BFException
    {
        EContentType ctType = chParentHandle.getContentType();
        
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        if (ctType != EContentType.COBOC_RULES_RULEGROUP) {
            return new ArrayList<IContent>();
        }
        
        if (csRuleCache == null || csRuleGroupCache == null) {
            readAllIntoCaches();
        }
        
        IContent cRuleGroup = csRuleGroupCache.findObject(chParentHandle);
        
        if (cRuleGroup == null) {
            throw new BFException("Rule group not found with handle " + chParentHandle.getLogName());
        }
        
        Collection<IContentHandle> cChildHandleList = cRuleGroup.getChildren();
        List<IContent> lResList = new ArrayList<IContent>(cChildHandleList.size());
        
        for (IContentHandle cChildHandle : cChildHandleList)
        {
            IContent cRule = csRuleCache.findObject(cChildHandle);
            
            if (cRule == null) {
                throw new BFException("Rule not found with handle " + cChildHandle.getLogName());
            }
            
            lResList.add(cRule);
        }
        
        return lResList;
    }  
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObject(com.cordys.coe.bf.content.base.IContentSource, com.cordys.coe.bf.content.base.IContentUnmarshaller, com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent readObject(IContentHandle hHandle) throws BFException
    {
        EContentType ctType = hHandle.getContentType();
        
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        if (csRuleCache == null || csRuleGroupCache == null) {
            readAllIntoCaches();
        }
        
        IContent cResult;
        
        switch (ctType) {
        case COBOC_RULES_RULEGROUP :
            cResult = csRuleGroupCache.findObject(hHandle);
            break;
            
        case COBOC_RULES_RULE :
            cResult = csRuleCache.findObject(hHandle);
            break;
            
        default:
            throw new BFException("CoBOC rule method content type must be rule or rule group. Got: " + ctType.getLogName());
        }
        
        if (cResult == null) {
            throw new BFException("No " + ctType.getLogName() + " found with handle " + hHandle.getLogName());
        }
        
        return cResult;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjectsFromXml(com.cordys.coe.bf.content.base.IXmlSource)
     */
    public List<IContent> readObjectsFromXml(IXmlSource xsSource) throws BFException
    {
        if (csRuleCache == null) {
            csRuleCache = new CobocContentStore();
        }
        
        if (csRuleGroupCache == null) {
            csRuleGroupCache = new CobocContentStore();
        }
        
        OMElement oeRoot = xsSource.getOMElement();
        
        if (oeRoot == null) {
            throw new BFException("readObjectFromXML: Only OMElement supported for XML.");
        }
        
        try
        {
            return readRuleGroupFromXml(oeRoot.getXMLStreamReader(), "XML");
        }
        catch (XMLStreamException e)
        {
            throw new BFException("Error while reading the XML.", e);
        }
    }
    
    protected void readAllIntoCaches() throws BFException {
        csRuleCache = new CobocContentStore();
        csRuleGroupCache = new CobocContentStore();
        
        File fRootFolder = csSource.getContentRootDirectory(EContentType.COBOC_RULES_RULEGROUP);
        
        if (fRootFolder == null) {
            throw new BFException("Content root directory not configured for content type " + fRootFolder);
        }           

        List<File> lRuleGroupFiles = findAllFiles(fRootFolder, "rulegroup", true, false);
        
        for (File fRuleGroup : lRuleGroupFiles)
        {
            readRuleGroupFileIntoCaches(fRuleGroup);
        }        
    }
    
    protected void readRuleGroupFileIntoCaches(File fFile) throws BFException {
        FileReader frReader = null;
        XMLStreamReader xsrReader = null;
        LogInterface liLogger = bcContext.getLogger();
        
        try
        {
            if (liLogger.isDebugEnabled()) 
            {
                liLogger.debug("Reading File " + fFile);
            }
            
            frReader = new FileReader(fFile.getAbsolutePath());
            xsrReader = AxiomUtils.xifXmlInputFactory.createXMLStreamReader(frReader);
            
            readRuleGroupFromXml(xsrReader, "file " + fFile);
        }
        catch (XMLStreamException e)
        {
            throw new BFException("Rule group file read failed: " + fFile, e);
        }
        catch (FileNotFoundException e)
        {
            throw new BFException("Rule group file was not found: " + fFile, e);
        }
        finally {
            if (xsrReader != null) {
                try
                {
                    xsrReader.close();
                }
                catch (XMLStreamException ignored)
                {
                }
                xsrReader = null;
            }
            if (frReader != null) {
                try
                {
                    frReader.close();
                }
                catch (IOException ignored)
                {
                }
                frReader = null;
            }
        }        
    }
    
    protected List<IContent> readRuleGroupFromXml(XMLStreamReader xsrReader, String sSourceName) throws XMLStreamException, BFException {
        IXmlSource xsUnmarshallSource = IXmlSource.Factory.newInstance();
        IContent cRuleGroup = null;
        List<IContent> cRuleList = new ArrayList<IContent>(20);        
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        StAXOMBuilder builder = new StAXOMBuilder(omFactory, xsrReader);
        OMElement documentElement = builder.getDocumentElement();     
        String fileVersion = readContentVersion(documentElement);
        IContentUnmarshaller cuRuleGroupUnmarshaller = csSource.getContentUnmarshaller(EContentType.COBOC_RULES_RULEGROUP, null, fileVersion);
        IContentUnmarshaller cuRuleUnmarshaller = csSource.getContentUnmarshaller(EContentType.COBOC_RULES_RULE, null, fileVersion);

        if (cuRuleGroupUnmarshaller == null) {
            throw new BFException("Unable to get rule group unmarshaller for version: " + fileVersion);
        }

        if (cuRuleUnmarshaller == null) {
            throw new BFException("Unable to get rule unmarshaller for version: " + fileVersion);
        }
        
        OMElement oeChild = documentElement.getFirstElement();
        
        if (oeChild == null) {
            throw new BFException("Rule group root node missing from " + sSourceName);
        }
        
        for (; oeChild != null; oeChild = AxiomUtils.getNextSiblingElement(oeChild)) {
            String sElemName = oeChild.getLocalName();
            
            if ("rulegroup-content".equals(sElemName)) {
                if (bUseContentFiltering &&
                    ! bcContext.getConfig().getCobocConfig().isContentEnabled(EContentType.COBOC_RULES_RULEGROUP)) {
                    // This type has been disabled in the configuration. 
                    return Collections.emptyList();
                }

                OMElement oeRuleGroupChild = oeChild.getFirstElement();
            
                if (oeRuleGroupChild != null) {
                    // Read the rule group content.
                    xsUnmarshallSource.set(oeRuleGroupChild);
                    
                    if (cRuleGroup != null) {
                        throw new BFException("More than one rule group defined in the " + sSourceName);
                    }
                    
                    cRuleGroup = cuRuleGroupUnmarshaller.unmarshalObject(xsUnmarshallSource);
                    
                    if (csRuleGroupCache.findObject(cRuleGroup.getHandle()) != null) {
                        throw new BFException("Rule group already loaded from " + csSource.getType().getLogName() +
                                " with key " + cRuleGroup.getHandle().getLogName());
                    }
                    
                    csRuleGroupCache.insertObject(cRuleGroup);
                }                        
            } else if ("rules".equals(sElemName)) {
                if (bUseContentFiltering &&
                    ! bcContext.getConfig().getCobocConfig().isContentEnabled(EContentType.COBOC_RULES_RULE)) {
                    // This type has been disabled in the configuration. 
                    continue;
                }
                
                OMElement oeRuleChild = oeChild.getFirstElement(); 
            
                for (; oeRuleChild != null; oeRuleChild = AxiomUtils.getNextSiblingElement(oeRuleChild)) {
                    // Read the rule content.
                    xsUnmarshallSource.set(oeRuleChild);
                    
                    IContent cRule = cuRuleUnmarshaller.unmarshalObject(xsUnmarshallSource);
                    
                    if (cRule != null) {
                        if (csRuleCache.findObject(cRule.getHandle()) != null) {
                            throw new BFException("Rule already loaded from " + csSource.getType().getLogName() +
                                    " with key " + cRule.getHandle().getLogName());
                        }
                        
                        cRuleList.add(cRule);
                        csRuleCache.insertObject(cRule);
                    }
                }
            } else {
                throw new BFException("Invalid element found in rule group file: " + sElemName);
            }
        }   
        
        
        if (cRuleGroup == null) {
            return null;
        } 
        
        for (IContent cTmp : cRuleList)
        {
            Rule rRule = (Rule) cTmp;
            
            rRule.setRuleGroup((RuleGroup) cRuleGroup);
        } 
        
        List<IContent> lResList = new ArrayList<IContent>(cRuleList.size() + 1);
        
        lResList.add(cRuleGroup);
        lResList.addAll(cRuleList);
        
        return lResList;
    }
}
