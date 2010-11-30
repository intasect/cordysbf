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
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.xmlstore.XmlStoreContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;

/**
 * TODO Describe the class.
 *
 * @author mpoyhone
 */
public class ContentSourceFileSystem extends ContentSourceBase
{
    private Map<EContentType, File> mContentRootDirMap = new HashMap<EContentType, File>();
    
    /**
     * Constructor for ContentSourceBcp
     * @param aBcContext
     */
    public ContentSourceFileSystem(BFContext bcContext)
    {
        super(bcContext);       
    }

    public void registerSoapMethodTemplate(String sTemplateId, String version, ISoapRequestTemplate srtTemplate) throws BFException {
        throw new BFException("SOAP request templates are not supported by " + getType().getLogName() + " content source.");
    }
    
    public void setContentRootDirectory(EContentType ctType, File fRootDir) {
        mContentRootDirMap.put(ctType, fRootDir);
    }
    
    public File getContentRootDirectory(EContentType ctType) {
        return mContentRootDirMap.get(ctType);
    }
    
    public File getContentFileName(IContentHandle chHandle, String sContentName) throws BFException{
        EContentType ctType = chHandle.getContentType();
        File fRootFolder = getContentRootDirectory(ctType);
        
        if (fRootFolder == null) {
            throw new BFException("Root folder not configured for " + ctType.getLogName(true));
        }
        
        String sFileName = "";
        String sContentTypeId = "";
        String sExtension = ".xml";
        
        switch (ctType) {
        case METHOD_SET : 
        case ROLE :
        case MENU :
        case TOOLBAR :
            sFileName = sContentName;
            break;
        
        case MDM_ENTITY_FOLDER :
        case COBOC_FOLDERS_FOLDER :
            sFileName = ((CobocContentHandle) chHandle).getKey() + "/00-FOLDER_INFO"; 
            break;
        case COBOC_FOLDERS_TEMPLATE :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_TPL";
            break;
        case COBOC_FOLDERS_GENERIC_INSTANCE :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_INST";
            break;
        case COBOC_FOLDERS_MAPPING :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_MAP";
            break;
        case COBOC_FOLDERS_PROCESSTEMPLATE :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_PTPL";
            break;
        case COBOC_FOLDERS_PROCESSBPMN :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_BPMN";
            break;            
        case COBOC_FOLDERS_PROCESSBPML :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_BPML";
            break;            
        case COBOC_FOLDERS_CONTENTMAP :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_CMAP";
            break;
        case COBOC_FOLDERS_DECISIONCASE :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_DC";
            break;
        case COBOC_FOLDERS_CONDITIONTEMPLATE :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_CT";
            break;        
        case COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM :
            sFileName = ((XmlStoreContentHandle) chHandle).getKey();
            sContentTypeId = "_CTF";
            break;                
        case COBOC_FOLDERS_ACTIONTEMPLATE :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_AT";
            break;
        case COBOC_FOLDERS_ACTIONTEMPLATE_XFORM :
            sFileName = ((XmlStoreContentHandle) chHandle).getKey();
            sContentTypeId = "_ATF";
            break;
        case COBOC_RULES_RULEGROUP :
            sFileName = escapeFileName(sContentName); 
            break;
        case COBOC_SCHEDULE_TEMPLATE :
            sFileName = escapeFileName(sContentName);
            break;
        case COBOC_INBOX_MODEL_C1 :
            sFileName = escapeFileName(sContentName);
            break;
        case COBOC_EMAIL_MODEL :
        case COBOC_INBOX_MODEL_C3 :
            {
                String key = ((CobocContentHandle) chHandle).getKey();
                
                if (key == null) {
                    throw new BFException("Content key is not set.");
                }
                
                String folderName = ctType == EContentType.COBOC_EMAIL_MODEL ? "email-models" : "inbox-models";
                
                sFileName = String.format("%s/%s/%s", 
                                          key.replaceFirst("^(.*)/[^/]+$", "$1"),
                                          escapeFileName(folderName),
                                          escapeFileName(sContentName));
            }
            break;
        case COBOC_MESSAGE_TEMPLATE :
            sFileName = String.format("%s/TemplateData", escapeFileName(sContentName));
            break;
            
        case MDM_BACKEND :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_MDM_BACKEND";
            break;
        case MDM_ENTITY :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_MDM_ENTITY";
            break;
        case MDM_MODEL :
            sFileName = ((CobocContentHandle) chHandle).getKey();
            sContentTypeId = "_MDM_MODEL";
            break;
            
        case COBOC_RULES_RULE :
        case METHOD :
        case METHOD_XSD :            
            throw new BFException("Cannot query file name for a " + ctType.getLogName());
            
        default :
            throw new BFException("Unsupported file system content type: " + ctType.getLogName());
        }
        
        if (bcContext.getConfig().getCobocConfig().isUseFileContentTypes()) {
            sFileName += sContentTypeId;
        }
        
        sFileName = escapeFilePath(sFileName);
        sFileName += sExtension;
        
        return new File(fRootFolder, sFileName);
    }
    
    public static String escapeFilePath(String path)
    {
        return path.replaceAll("[%$\\:]", "_");
    }
    
    public static String escapeFileName(String path)
    {
        return path.replaceAll("[%$/\\:]", "_");
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#getType()
     */
    public EContentSourceType getType()
    {
        return EContentSourceType.FILESYSTEM;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#getContentTypeFromXml(com.cordys.coe.bf.content.base.IXmlSource)
     */
    public EContentType getContentTypeFromXml(IXmlSource xsXml) throws BFException
    {
        OMElement oeRoot = xsXml.getOMElement();
        
        if (oeRoot == null) {
            throw new BFException("getContentTypeFromXml: Only OMElement supported for XML.");
        }
        
        String sRootName = oeRoot.getLocalName();
        
        if (sRootName.equals("template_content")) {
            // This is a folders object. Skip to the tuple element.
            oeRoot = oeRoot.getFirstElement();
            
            if (oeRoot == null) {
                throw new BFException("template_content element has no children.");
            }
            
            sRootName = oeRoot.getLocalName();
        }
        
        EContentType ctType = null;
        
        if (sRootName.equals("tuple")) {
            // This is folder content.
            String sFileType = oeRoot.getAttributeValue(new QName("FILE_TYPE"));
            
            if (sFileType == null) {
                throw new BFException("CoBOC folder content FILE_TYPE attribute missing from XML.");
            }
            
            if (sFileType.equals("folder")) {
                ctType = EContentType.COBOC_FOLDERS_FOLDER;
            } else if (sFileType.equals("mdm-entity-folder")) {
                ctType = EContentType.MDM_ENTITY_FOLDER;
            } else if (sFileType.equals("template")) {
                ctType = EContentType.COBOC_FOLDERS_TEMPLATE;
            } else {
                ctType = bcContext.getCobocTemplateRegistry().getContentTypeFromFileType(sFileType);
            }
        } else if (sRootName.equals("rulegroup")) {
            ctType = EContentType.COBOC_RULES_RULEGROUP;
        } else if (sRootName.equals("schedule-template")) {
            ctType = EContentType.COBOC_SCHEDULE_TEMPLATE;
        } else if (sRootName.equals("message-model")) {
            // C1/C2 inbox model.
            ctType = EContentType.COBOC_INBOX_MODEL_C1;
        } else if (sRootName.equals("inbox-model")) {
            // C3 inbox model.
            ctType = EContentType.COBOC_INBOX_MODEL_C3;
        } else if (sRootName.equals("email-model")) {
            ctType = EContentType.COBOC_EMAIL_MODEL;
        } else if (sRootName.equals("message-template")) {
            ctType = EContentType.COBOC_MESSAGE_TEMPLATE;
        }
        
        return ctType;  
    }
}
