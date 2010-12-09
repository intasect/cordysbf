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
package com.cordys.coe.bf.content.coboc.impl.bcp42_c3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem;
import com.cordys.coe.bf.content.base.impl.FileSystemReadMethodBase;
import com.cordys.coe.bf.content.types.EContentCategory;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.coboc.messagemodels.EmailModel;
import com.cordys.coe.bf.content.types.coboc.messagemodels.InboxModel_C3;
import com.cordys.coe.bf.exception.BFException;

/**
 * Implements a read method for C3 CoBOC message template content.
 *
 * @author mpoyhone
 */
public class FileSystemReadMethod_MessageModels extends FileSystemReadMethodBase
{
    public FileSystemReadMethod_MessageModels(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceFileSystem) csSource);
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
        
        return readContentFromXml(EContentType.COBOC_MESSAGE_TEMPLATE, oeRoot, "XML");
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(EContentType, boolean)
     */
    public List<IContent> readObjects(EContentType ctType, boolean bReadFully) throws BFException
    {
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        if (ctType.getCategory() != EContentCategory.COBOC_MESSAGE_MODELS) {
            throw new BFException("Invalid CoBOC message model content type: " + ctType.getLogName());
        }
        
        String rootElemName = null;
        
        switch (ctType) {
        case COBOC_MESSAGE_TEMPLATE : rootElemName =  "message-template"; break;
        case COBOC_INBOX_MODEL_C3 : rootElemName =  "inbox-model"; break;
        case COBOC_EMAIL_MODEL : rootElemName =  "email-model"; break;
        }
        
        return super.readObjects(ctType, bReadFully, rootElemName);
    }    
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle, boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive)
            throws BFException
    {
        File fRootFolder = csSource.getContentRootDirectory(chParentHandle.getContentType());
        
        if (fRootFolder == null) {
            throw new BFException("Content root directory not configured for content type " + chParentHandle.getContentType().getLogName());
        }          
        
        File fContentFolder = new File(fRootFolder, ((CobocContentHandle) chParentHandle).getKey());
        List<File> lFiles = findAllFiles(fContentFolder, new String[] { "inbox-model", "email-model" }, true, false);
        List<IContent> lResList = new ArrayList<IContent>(lFiles.size());
        
        for (File fFile : lFiles)
        {
            IContent cTpl = readContentFile(null, fFile);
            
            if (cTpl != null) {
                lResList.add(cTpl);
            }
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
        
        if (ctType.getCategory() != EContentCategory.COBOC_MESSAGE_MODELS) {
            throw new BFException("Invalid CoBOC message model content type: " + ctType.getLogName());
        }
        
        return readContentFile(ctType, csSource.getContentFileName(hHandle, ((CobocContentHandle) hHandle).getKey()));
    }

    /**
     * @see com.cordys.coe.bf.content.base.impl.FileSystemReadMethodBase#readContentFromXml(com.cordys.coe.bf.content.types.EContentType, org.apache.axiom.om.OMElement, java.lang.String)
     */
    @Override
    protected List<IContent> readContentFromXml(EContentType ctType,
            OMElement oeRoot, String sourceName) throws BFException
    {
        String nodeName = oeRoot.getLocalName();
        
        if ("message-template".equals(nodeName)) {
            ctType = EContentType.COBOC_MESSAGE_TEMPLATE;
        } else if ("inbox-model".equals(nodeName)) {
            ctType = EContentType.COBOC_INBOX_MODEL_C3;
        } else if ("email-model".equals(nodeName)) {
            ctType = EContentType.COBOC_EMAIL_MODEL;
        } else if ("message-model".equals(nodeName)) {
            // This is the C1/C2 inbox model
            ctType = EContentType.COBOC_INBOX_MODEL_C1;            
        }
        
        List<IContent> res = super.readContentFromXml(ctType, oeRoot, sourceName);
        
        if (res != null) {
            for (IContent content : res)
            {
                if (ctType == EContentType.COBOC_INBOX_MODEL_C3) {
                    ((InboxModel_C3) content).createKey();
                } else if (ctType == EContentType.COBOC_EMAIL_MODEL) {
                    ((EmailModel) content).createKey();
                }
            }
        }
        
        return res;
    }    
}
