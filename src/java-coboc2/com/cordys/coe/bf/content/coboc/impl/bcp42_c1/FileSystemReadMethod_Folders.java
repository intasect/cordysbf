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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
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
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem;
import com.cordys.coe.bf.content.base.impl.FileSystemReadMethodBase;
import com.cordys.coe.bf.content.types.EContentCategory;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.coboc.folders.Folder;
import com.cordys.coe.bf.content.types.coboc.folders.SpecialAttribute;
import com.cordys.coe.bf.content.types.coboc.folders.Template;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.LogInterface;
import com.cordys.tools.ant.util.FileUtil;

/**
 * Implements a read method for CoBOC folders content.
 *
 * @author mpoyhone
 */
public class FileSystemReadMethod_Folders extends FileSystemReadMethodBase
{
    public FileSystemReadMethod_Folders(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceFileSystem) csSource);
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(EContentType, boolean)
     */
    public List<IContent> readObjects(EContentType ctType, boolean bReadFully) throws BFException
    {
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle, boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive) throws BFException
    {
        LogInterface liLogger = bcContext.getLogger();
        EContentType ctType = chParentHandle.getContentType();
        
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        if (! (chParentHandle instanceof CobocContentHandle)) {
            throw new IllegalArgumentException("Invalid content handle for CoBOC folders opeation.");
        }
        
        CobocContentHandle cchCobocHandle = (CobocContentHandle) chParentHandle;        
        
        File fRootFolder = csSource.getContentRootDirectory(ctType);
        
        if (fRootFolder == null) {
            throw new BFException("Content root directory not configured for content type " + ctType.getLogName());
        }
        
        if (! CobocConstants.ROOT_FOLDER_KEY.equals(cchCobocHandle.getKey())) {
            fRootFolder = new File(fRootFolder, cchCobocHandle.getKey());
        }
        
        String sRootDirAbsName = fRootFolder.getAbsolutePath().replace('\\', '/');
        List<File> lFiles = findAllFiles(fRootFolder, new String[] { "template_content", "tuple" }, bRecursive, true);
        Set<File> sSeenFolders = new HashSet<File>();
        Set<String> sReadFolders = new HashSet<String>();
        List<IContent> lResList = new ArrayList<IContent>(lFiles.size());
        
        for (File fFile : lFiles)
        {
            if (fFile.isDirectory()) {
                String sName = fFile.getName();
                
                if (sExcludedDirs.contains(sName)) {
                    sSeenFolders.add(fFile);
                }
                
                continue;
            }
            
            String relFilePath;
            
            try
            {
                relFilePath = FileUtil.getRelativePath(fRootFolder, fFile).replace("\\", "/");
                
                if (! relFilePath.startsWith("/")) {
                    relFilePath = "/" + relFilePath;
                }
                
                if (relFilePath.toLowerCase().endsWith(".xml")) {
                    relFilePath = relFilePath.substring(0, relFilePath.length() - 4);
                }
            }
            catch (IOException e)
            {
                continue;
            }
            
            // Apply filtering only to BPM's. Otherwise references will be broken.
            if (relFilePath.startsWith("/Business Processes/")) {
                if (bUseContentFiltering && 
                    ! csSource.checkForAccess(ctType, relFilePath, fFile.isDirectory())) {
                    if (liLogger.isDebugEnabled()) {
                        liLogger.debug("  Skipping " + fFile);
                    }
                    
                    continue;
                }
            }
            
            if (liLogger.isDebugEnabled()) 
            {
                liLogger.debug("Reading File " + fFile);
            }
            List<IContent> lFileObjects = readFile(fFile);
            
            if (lFileObjects != null) {
                for (IContent cObject : lFileObjects)
                {
                    EContentType ctObjectType = cObject.getType();
                    IContentHandle chHandle = cObject.getHandle();
                    
                    if (liLogger.isDebugEnabled()) {
                        liLogger.debug("Read CoBOC " + ctObjectType.getLogName() + " " + chHandle.getLogName());
                    }
                    
                    if (ctObjectType == EContentType.COBOC_FOLDERS_FOLDER) {
                        // Add folder to the read folders set.
                        sReadFolders.add(((Folder) cObject).getKey());
                    }
                }
                
                lResList.addAll(lFileObjects);
            }
        }
        
        // Create any missing folders.
        Set<String> sMissingFolders = new HashSet<String>();
        
        for (IContent cObject : lResList)
        {
            if (! (cObject instanceof CobocContentBase) || cObject.getType().getCategory() != EContentCategory.COBOC_FOLDERS) {
                continue;
            }
            
            CobocContentHandle cchParentHandle = (CobocContentHandle) ((CobocContentBase) cObject).getParent();
            
            if (cchParentHandle == null) {
                throw new BFException("Parent handle is not set for object " + cObject.getLogName());
            }
            
            if (cchParentHandle.getContentType() != EContentType.COBOC_FOLDERS_FOLDER) {
                // Parent is not a folder.
                continue;
            }
            
            String sParentKey = cchParentHandle.getKey();
            
            if (! sReadFolders.contains(sParentKey) && ! sMissingFolders.contains(sParentKey)) {
                sMissingFolders.add(sParentKey);
            }
        }
        
        // Add folders that were seen but not were created yet. 
        for (File fFolder : sSeenFolders)
        {
            String sPath = fFolder.getAbsolutePath().replace('\\', '/');
            
            sPath = "/" + sPath.substring(sRootDirAbsName.length() + 1);
                
            if (! sReadFolders.contains(sPath) && ! sMissingFolders.contains(sPath)) {
                sMissingFolders.add(sPath);
            }
        }
        
        // Create missing folder objects.
        for (String sFolderKey : sMissingFolders)
        {
            if (sFolderKey == null || sFolderKey.length() == 0 || sFolderKey.equals(CobocConstants.ROOT_FOLDER_KEY)) {
                // Do not add the root folder.
                continue;
            }
            
            Folder fFolder = Folder.createFromKey(sFolderKey);

            lResList.add(fFolder);
        }
        
        return lResList;    
    }  
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObject(com.cordys.coe.bf.content.base.IContentSource, com.cordys.coe.bf.content.base.IContentUnmarshaller, com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent readObject(IContentHandle hHandle) throws BFException
    {
        LogInterface liLogger = bcContext.getLogger();
        EContentType ctType = hHandle.getContentType();
        
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        if (! (hHandle instanceof CobocContentHandle)) {
            throw new IllegalArgumentException("Invalid content handle for CoBOC folders opeation.");
        }
        
        CobocContentHandle cchCobocHandle = (CobocContentHandle) hHandle;        
        
        File fRootFolder = csSource.getContentRootDirectory(EContentType.COBOC_FOLDERS_FOLDER);
        
        if (fRootFolder == null) {
            throw new BFException("Content root directory not configured for content type " + ctType.getLogName());
        }
        
        File fFile = new File(fRootFolder, cchCobocHandle.getKey() + ".xml");
        
        if (! fFile.exists()) {
            return null;
        }
        
        List<IContent> lResList = new ArrayList<IContent>(10);
        
        if (liLogger.isDebugEnabled()) 
        {
            liLogger.debug("Reading File " + fFile);
        }
        
        List<IContent> lFileObjects = readFile(fFile);
            
        if (lFileObjects != null) {
            for (IContent cObject : lFileObjects)
            {
                EContentType ctObjectType = cObject.getType();
                IContentHandle chHandle = cObject.getHandle();
                
                if (liLogger.isDebugEnabled()) {
                    liLogger.debug("Read CoBOC " + ctObjectType.getLogName() + " " + chHandle.getLogName());
                }
            }
            
            lResList.addAll(lFileObjects);
        }
        
        return lFileObjects.size() > 0 ? lFileObjects.get(0) : null;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjectsFromXml(com.cordys.coe.bf.content.base.IXmlSource)
     */
    public List<IContent> readObjectsFromXml(IXmlSource xsSource) throws BFException
    {
        OMElement oeRoot = xsSource.getOMElement();
        
        if (oeRoot == null) {
            throw new BFException("readObjectFromXML: Only OMElement supported for XML.");
        }
        
        return readContentFromXml(oeRoot, "XML");
    }
    
    protected List<IContent> readFile(File fFile) throws BFException {
        
        FileReader frReader = null;
        XMLStreamReader xsrReader = null;
        LogInterface liLogger = bcContext.getLogger();
        
        if (liLogger.isDebugEnabled()) 
        {
            liLogger.debug("Reading File " + fFile);
        }
        
        try
        {
            frReader = new FileReader(fFile.getAbsolutePath());
            xsrReader = AxiomUtils.xifXmlInputFactory.createXMLStreamReader(frReader);
            
            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            StAXOMBuilder builder = new StAXOMBuilder(omFactory, xsrReader);
            OMElement documentElement = builder.getDocumentElement();
            
            List<IContent> lContentList = readContentFromXml(documentElement, "file " + fFile);
            
            return lContentList;
        }
        catch (XMLStreamException e)
        {
            throw new BFException("CoBOC file read failed: " + fFile, e);
        }
        catch (FileNotFoundException e)
        {
            throw new BFException("CoBOC file was not found: " + fFile, e);
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
    
    protected List<IContent> readContentFromXml(OMElement oeRoot, String sSourceName) throws BFException {
        
        IXmlSource xsUnmarshallSource = IXmlSource.Factory.newInstance();

        IContent cResult = null;        
        OMElement oeContentElement = oeRoot;
            
        if (oeContentElement == null) {
            throw new BFException("CoBOC content node missing from " + sSourceName);
        }
        
        // Detemine the type
        String sContentName = oeContentElement.getLocalName();
        
        if (sContentName.equals("template_content")) {
            // This is a folders object. Skip to the tuple element.
            oeContentElement = oeContentElement.getFirstElement();
            
            if (oeContentElement == null) {
                throw new BFException("template_content element has no children.");
            }
            
            sContentName = oeContentElement.getLocalName();
        }
        
        String fileVersion = readContentVersion(oeContentElement);
        String sFileType = oeContentElement.getAttributeValue(new QName("FILE_TYPE"));
        
        if (sFileType == null) {
            throw new BFException("CoBOC folders FILE_TYPE attribute missing from " + sSourceName);
        }
        
        EContentType ctType;
        IContentUnmarshaller cuUnmarshaller;

        xsUnmarshallSource.set(oeContentElement);
        ctType = csSource.getContentTypeFromXml(xsUnmarshallSource);
        
        if (ctType == null) {
            throw new BFException("Unknown CoBOC content file type '" + sFileType + "' in " + sSourceName);
        }
        
        if (bUseContentFiltering && ! bcContext.getConfig().getCobocConfig().isContentEnabled(ctType)) {
            // This type has been disabled in the configuration. 
            return Collections.emptyList();
        }        
        
        cuUnmarshaller = csSource.getContentUnmarshaller(ctType, null, fileVersion);
        if (cuUnmarshaller == null) {
            throw new BFException("Unable to get " + ctType.getLogName() + 
                    " unmarshaller for version: " + fileVersion);
        }
        
        // Read the content.
        xsUnmarshallSource.set(oeContentElement);
                    
        cResult = cuUnmarshaller.unmarshalObject(xsUnmarshallSource);
        
        if (cResult == null) {
            throw new BFException("No CoBOC content defined in " + sSourceName);
        } 
        
        List<IContent> lResultList = new ArrayList<IContent>(10);
        
        lResultList.add(cResult);
        
        if (cResult.getType() == EContentType.COBOC_FOLDERS_TEMPLATE) {
            readSpecialAttributes(oeRoot, (Template) cResult, lResultList, fileVersion);
        }
        
        return lResultList;
    }

    private void readSpecialAttributes(OMElement oeRoot, Template tTemplate, List<IContent> lResultList, String fileVersion) throws BFException
    {
        OMElement oeAttribs = oeRoot.getFirstChildWithName(new QName("attributes"));
        
        if (oeAttribs == null) {
            return;
        }

        EContentType ctType =  EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE;
        IContentUnmarshaller cuUnmarshaller;
        
        if (! bcContext.getConfig().getCobocConfig().isContentEnabled(ctType)) {
            // This type has been disabled in the configuration. 
            return;
        }  
        
        cuUnmarshaller = csSource.getContentUnmarshaller(ctType, null, fileVersion);
        
        if (cuUnmarshaller == null) {
            throw new BFException("Unable to get " + ctType.getLogName() + 
                    " unmarshaller for version: " + fileVersion);
        }   
        
        for (OMElement oeChild = oeAttribs.getFirstElement(); oeChild != null; oeChild = AxiomUtils.getNextSiblingElement(oeChild)) {
            IXmlSource xsUnmarshallSource = IXmlSource.Factory.newInstance(oeChild);
            IContent cAttrib;
            
            cAttrib = cuUnmarshaller.unmarshalObject(xsUnmarshallSource);
            
            if (! (cAttrib instanceof SpecialAttribute)) {
                throw new BFException("Invalid object class for special attribute: " + cAttrib.getClass());
            }
            
            cAttrib.getParent().copyFrom(tTemplate.getHandle());
            ((SpecialAttribute) cAttrib).generateKey(tTemplate);
            lResultList.add(cAttrib);
        }
    }
}
