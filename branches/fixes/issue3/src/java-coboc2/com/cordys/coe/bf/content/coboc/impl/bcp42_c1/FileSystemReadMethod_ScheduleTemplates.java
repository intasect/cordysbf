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
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem;
import com.cordys.coe.bf.content.base.impl.FileSystemReadMethodBase;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.LogInterface;

/**
 * Implements a read method for CoBOC schedule template content.
 *
 * @author mpoyhone
 */
public class FileSystemReadMethod_ScheduleTemplates extends FileSystemReadMethodBase
{
    public FileSystemReadMethod_ScheduleTemplates(BFContext bcContext, IContentSource csSource) throws BFException {
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
        
        switch (ctType) {
        case COBOC_SCHEDULE_TEMPLATE :
            break;
            
        default:
            throw new BFException("CoBOC schedule template content type must be schedule template. Got: " + ctType.getLogName());
        }
        
        File fRootFolder = csSource.getContentRootDirectory(ctType);
        
        if (fRootFolder == null) {
            throw new BFException("Content root directory not configured for content type " + ctType.getLogName());
        }          
        
        List<File> lFiles = findAllFiles(fRootFolder, "schedule-template", true, false);
        List<IContent> lResList = new ArrayList<IContent>(lFiles.size());
        
        for (File fFile : lFiles)
        {
            IContent cTpl = readScheduleTemplateFile(fFile);
            
            if (cTpl != null) {
                lResList.add(cTpl);
            }
        }
        
        return lResList;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle, boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive) throws BFException
    {
        // Schedule templates can't have any subcontent.
        return new ArrayList<IContent>();
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
        
        switch (ctType) {
        case COBOC_SCHEDULE_TEMPLATE :
            return readScheduleTemplateFile(csSource.getContentFileName(hHandle, ((CobocContentHandle) hHandle).getKey()));
            
        default:
            throw new BFException("CoBOC schedule template read method type cannot be " + ctType.getLogName());
        }
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
        
        return readContentFromXml(EContentType.COBOC_SCHEDULE_TEMPLATE, oeRoot, "XML");
    }
    
    protected IContent readScheduleTemplateFile(File fFile) throws BFException {
        EContentType ctType = EContentType.COBOC_SCHEDULE_TEMPLATE;
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
        
        return cResult;
    }
    
    protected List<IContent> readContentFromXml(EContentType ctType, OMElement oeRoot, String sSourceName) throws BFException {
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
        OMElement oeScheduleTemplate = oeRoot.getFirstElement();
        
        if (oeScheduleTemplate == null) {
            throw new BFException(ctType.getLogName() + " content node missing from " + sSourceName);
        }

        // Read the content.
        xsUnmarshallSource.set(oeScheduleTemplate);
                    
        cResult = cuUnmarshaller.unmarshalObject(xsUnmarshallSource);
        
        if (cResult == null) {
            throw new BFException("No " + ctType.getLogName() + 
                    " defined in " + sSourceName);
        } 
        
        return Collections.singletonList(cResult);
    }
}
