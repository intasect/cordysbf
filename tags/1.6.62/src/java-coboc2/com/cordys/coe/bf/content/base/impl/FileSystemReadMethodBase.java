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
package com.cordys.coe.bf.content.base.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
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
import com.cordys.coe.bf.content.base.IContentReadMethod;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.LogInterface;

/**
 * Base class for file system read methods.
 *
 * @author mpoyhone
 */
public abstract class FileSystemReadMethodBase implements IContentReadMethod {
    /**
     * Contains the method version ID.
     */
    protected String sMethodVersion;
    /**
     * Determines if content filtering is used by this method.
     */
    protected boolean bUseContentFiltering = true;
    
    protected BFContext bcContext;
    protected ContentSourceFileSystem csSource;
    
    protected static Set<String> sExcludedDirs = new HashSet<String>();
    
    static {
        // Folder names to be excluded.
        sExcludedDirs.add(".svn");
        sExcludedDirs.add("_svn");
    }
    
    public FileSystemReadMethodBase(BFContext bcContext, ContentSourceFileSystem csSource) {
        this.bcContext = bcContext;
        this.csSource = csSource; 
        this.sMethodVersion = bcContext.getConfig().getVersionInfo().getMajorVersion().getCobocString();
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#setContentVersion(java.lang.String)
     */
    public void setContentVersion(String sVersion)
    {
    }
    
    protected List<File> findAllFiles(File fRootDir, boolean bRecursive, boolean bReturnFolders) throws BFException {
        if (! fRootDir.exists()) {
            bcContext.getLogger().debug("Content root directory does not exist: " + fRootDir);
            return Collections.emptyList();
        }
        
        List<File> lResList = new LinkedList<File>();
        
        recurseAllFiles(fRootDir, null, lResList, bReturnFolders);
        
        return lResList;
    }
    
    protected List<File> findAllFiles(File fRootDir, String sRootElementName, boolean bRecursive, boolean bReturnFolders) throws BFException {
        List<File> lResList = new LinkedList<File>();
        
        if (! fRootDir.exists()) {
            // Skip as the directory does not exist.
            return lResList;
        }
        
        recurseAllFiles(fRootDir, new String[] { sRootElementName }, lResList, bReturnFolders);
        
        return lResList;
    }    
    
    protected List<File> findAllFiles(File fRootDir, String[] saRootElementNames, boolean bRecursive, boolean bReturnFolders) throws BFException {
        if (! fRootDir.exists()) {
            bcContext.getLogger().debug("Content root directory does not exist: " + fRootDir);
            return Collections.emptyList();
        }
        
        List<File> lResList = new LinkedList<File>();
        
        recurseAllFiles(fRootDir, saRootElementNames, lResList, bReturnFolders);
        
        return lResList;
    }      

    protected void recurseAllFiles(File fRootDir, String[] saRootElementNames, List<File> lResList, boolean bReturnFolders) throws BFException {
        if (! fRootDir.exists()) {
            throw new BFException("Content root directory does not exist: " + fRootDir);
        }
        
        File[] faContents = fRootDir.listFiles();
        
        for (File fFile : faContents)
        {
            boolean bAddFile = false;
            
            if (fFile.isFile() && fFile.canRead()) {
                if (saRootElementNames != null) {
                    FileReader frReader = null;
                    XMLStreamReader xsrReader = null;
                    
                    try
                    {
                        frReader = new FileReader(fFile.getAbsolutePath());
                        xsrReader = AxiomUtils.xifXmlInputFactory.createXMLStreamReader(frReader);
                        AxiomUtils.skipToStreamEvent(xsrReader, XMLStreamReader.START_ELEMENT);
                        
                        for (int i = 0; i < saRootElementNames.length; i++)
                        {
                            String sName = saRootElementNames[i];
                            
                            if (sName.equals(xsrReader.getLocalName())) {
                                bAddFile = true;
                                break;
                            }                            
                        }

                    }
                    catch (Exception e)
                    {
                        System.out.println("Skipped file " + fFile + ": " + e);
                        continue;
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
                } else {
                    bAddFile = true;
                }
            } else if (fFile.isDirectory() && bReturnFolders) {
                bAddFile = true;
            }
            
            if (fFile.isDirectory()) {
                String sDirName = fFile.getName();
                
                if (! sExcludedDirs.contains(sDirName)) {
                    recurseAllFiles(fFile, saRootElementNames, lResList, bReturnFolders);
                } else {
                    bAddFile = false;
                }
            }
            
            if (bAddFile) {
                lResList.add(fFile);
            }
        }
    }    
    
    public String readContentVersion(OMElement root) 
    {
        String version = root.getAttributeValue(new QName("bf-version"));
        
        if (version == null) {
            return getMethodVersion();
        } else {
            return version;
        }
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandlingMethod#getMethodVersion()
     */
    public String getMethodVersion()
    {
        return sMethodVersion;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandlingMethod#getContentFilterStatus()
     */
    public boolean getContentFilterStatus()
    {
        return bUseContentFiltering;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandlingMethod#setContentFilterStatus(boolean)
     */
    public void setContentFilterStatus(boolean bOn)
    {
        bUseContentFiltering = bOn;
    }

    protected List<IContent> readContentFromXml(EContentType ctType, OMElement oeRoot, String sSourceName)
            throws BFException
    {
        if (bUseContentFiltering &&
            ! bcContext.getConfig().getCobocConfig().isContentEnabled(ctType)) {
            // This type has been disabled in the configuration. 
            return Collections.emptyList();
        }          
        
        String fileVersion = readContentVersion(oeRoot);
        IXmlSource xsUnmarshallSource = IXmlSource.Factory.newInstance();
        IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(ctType, null, fileVersion);
        
        if (cuUnmarshaller == null) {
            throw new BFException("Unable to get " + ctType.getLogName() + 
                    " unmarshaller for version: " + fileVersion);
        }
    
        IContent cResult = null;        
    
        // Read the content.
        xsUnmarshallSource.set(oeRoot);
        cResult = cuUnmarshaller.unmarshalObject(xsUnmarshallSource);
        
        if (cResult == null) {
            throw new BFException("No " + ctType.getLogName() + 
                    " defined in " + sSourceName);
        } 
        
        return Collections.singletonList(cResult);
    }

    protected List<IContent> readObjects(EContentType ctType, boolean bReadFully, String ... saRootElementNames) throws BFException
    {
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        File fRootFolder = csSource.getContentRootDirectory(ctType);
        
        if (fRootFolder == null) {
            throw new BFException("Content root directory not configured for content type " + ctType.getLogName());
        }          
        
        List<File> lFiles = findAllFiles(fRootFolder, saRootElementNames, true, false);
        List<IContent> lResList = new ArrayList<IContent>(lFiles.size());
        
        for (File fFile : lFiles)
        {
            IContent cTpl = readContentFile(ctType, fFile);
            
            if (cTpl != null) {
                lResList.add(cTpl);
            }
        }
        
        return lResList;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle, boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive)
            throws BFException
    {
        return null;
    }

    protected IContent readContentFile(EContentType ctType, File fFile)
            throws BFException
    {
        FileReader frReader = null;
        XMLStreamReader xsrReader = null;
        IContent cResult = null;        
        LogInterface liLogger = bcContext.getLogger();
        
        try
        {
            if (liLogger.isDebugEnabled()) 
            {
                liLogger.debug("Reading File " + fFile);
            }
            
            frReader = new FileReader(fFile.getAbsolutePath());
            xsrReader = AxiomUtils.xifXmlInputFactory.createXMLStreamReader(frReader);
            
            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            StAXOMBuilder builder = new StAXOMBuilder(omFactory, xsrReader);
            OMElement documentElement = builder.getDocumentElement();     
                                    
            List<IContent> lContentList = readContentFromXml(ctType, documentElement, "file " + fFile);
            
            cResult = (lContentList.size() > 0 ? lContentList.get(0) : null);
        }
        catch (XMLStreamException e)
        {
            throw new BFException(ctType.getLogName() + " file read failed: " + fFile, e);
        }
        catch (FileNotFoundException e)
        {
            throw new BFException(ctType.getLogName() + " file was not found: " + fFile, e);
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
        
        return cResult;
    }
}
