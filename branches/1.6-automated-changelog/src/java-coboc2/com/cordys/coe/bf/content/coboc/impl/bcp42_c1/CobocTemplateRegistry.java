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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.types.EContentType;

/**
 * A class that is used to resolve the object instance template key from
 * the template ID. For standard templates (e.g. mappings) this returns
 * the hard-coded value otherwise this searches the template from BCP.
 *
 * @author mpoyhone
 */
public class CobocTemplateRegistry
{
    /**
     * Contains the template ID's of standard instances (e.g. mappings) to keys. 
     */
    private static Map<String, String> mStandardIdToKeyMap = new HashMap<String, String>();
    /**
     * Contains the template keys of standard instances (e.g. mappings) to ID's. 
     */
    private static Map<String, String> mStandardKeyToIdMap = new HashMap<String, String>();    
    /**
     * Contains mappings from template key to the content type enumeration value.
     */
    private static Map<String, EContentType> mStandardKeyToContentTypeMap = new HashMap<String, EContentType>();
    /**
     * Contains mappings from the content type enumeration value to template key.
     */
    private static Map<EContentType, String> mStandardContentTypeToKeyMap = new HashMap<EContentType, String>();    /**
     * Contains mappings from file system type to the content type enumeration value.
     */
    private static Map<String, EContentType> mFileSystemTypeToContentTypeMap = new HashMap<String, EContentType>();    
    /**
     * Contains mappings from the content type enumeration value to file system type.
     */
    private static Map<EContentType, String> mContentTypeMapToFileSystemType = new HashMap<EContentType, String>();    
    /**
     * Contains keys of all supported instance templates.
     */
    private static List<String> lSupportedTemplateKeys = new LinkedList<String>();
    
    public CobocTemplateRegistry(BFContext bcContext) {
        // Add standard types.
        addMapping("1000", "/DEFAULTFOLDER", null, null);
        addMapping("1001", "/SUPERTEMPLATE", null, null);
        addMapping("1002", "/COD", null, null);
        addMapping("1003", "/BCHANNELTEMPLATE", null, null);
        addMapping("1004", "/PROCESS", null, null);
        addMapping("1005", "/MAP", "mapping", EContentType.COBOC_FOLDERS_MAPPING);
        addMapping("1006", "/MDHM", null, null);
        addMapping("1007", "/PROCESSTEMPLATE", "process-template", EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
        addMapping("1008", "/PROCESSINSTANCE", "process-instance", EContentType.COBOC_FOLDERS_PROCESSINSTANCE);
        addMapping("1009", "/BR_CUSTOM_LIBRARY", null, null);
        addMapping("1010", "/CONTENTMAP", "content-mapping", EContentType.COBOC_FOLDERS_CONTENTMAP);
        addMapping("1011", "/DECISIONCASE", "decision-case", EContentType.COBOC_FOLDERS_DECISIONCASE);
        addMapping("1012", "/CONDITION", "condition-template", EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE);
        addMapping("1013", "/ACTION", "action-template", EContentType.COBOC_FOLDERS_ACTIONTEMPLATE);
        addMapping("1020", "/BSF_DEFAULT_TEMPLATE", null, null);
        addMapping("1106", "/MDHM TREE", null, null);
        
        // Action template XForm
        addMapping(null, null, "action-template-xform", EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM);
        addMapping(null, null, "condition-template-xform", EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM);
        
        // BPMN and BPML objects use the generic /SUPERTEMPLATE, so we cannot map them normally.
        addMapping(null, null, "process-bpmn", EContentType.COBOC_FOLDERS_PROCESSBPMN);
        addMapping(null, null, "process-bpml", EContentType.COBOC_FOLDERS_PROCESSBPML);
        
        // MDM types
        /*addMapping(null, "/cordys/mdm/modeler/templates/entity", "mdm-entity", EContentType.MDM_ENTITY);
        addMapping(null, "/cordys/mdm/modeler/templates/backend", "mdm-backend", EContentType.MDM_BACKEND);
        addMapping(null, "/cordys/mdm/modeler/templates/model", "mdm-model", EContentType.MDM_MODEL);*/
    }
    
    private void addMapping(String sId, String sKey, String sFileSystemType, EContentType ctType) {
        if (sId != null && sKey != null) {
            mStandardIdToKeyMap.put(sId, sKey);
            mStandardKeyToIdMap.put(sKey, sId);
        }
        
        if (sKey != null) {
            mStandardKeyToContentTypeMap.put(sKey, ctType);
        }
        
        if (ctType != null) {
            mStandardContentTypeToKeyMap.put(ctType, sKey);
            mFileSystemTypeToContentTypeMap.put(sFileSystemType, ctType);
            mContentTypeMapToFileSystemType.put(ctType, sFileSystemType);
            
            if (sKey != null) {
                lSupportedTemplateKeys.add(sKey);
            }
        }
    }
    
    /**
     * Returns a list of all supported templates keys.
     * @return Template key list as a collection.
     */
    public Collection<String> getSupportedTemplateKeys() {
        return lSupportedTemplateKeys;
    }
    
    /**
     * Returns the template key from the instance template ID.
     * @param sTemplateId Instance template ID.
     * @return Matching template key or <code>null</code> if the key could not be resolved..
     */
    public String getInstanceTemplateKey(String sTemplateId) {
        // First try to find a standard or cached custom key.
        String sKey = mStandardIdToKeyMap.get(sTemplateId);
        
        if (sKey != null) {
            return sKey;
        }
        
        // TODO Here we would go to CoBOC to try to find out the template key.. 
        
        return null;
    }
    
    /**
     * Returns the content type enumeration value from the instance template key.
     * @param sTemplateKey Instance template key.
     * @return Content type or <code>null</code> if this the type could not be resolved.
     */
    public EContentType getContentTypeFromTemplateKey(String sTemplateKey) {
        return mStandardKeyToContentTypeMap.get(sTemplateKey);
    }
    
    /**
     * Returns the instance template key from content type enumeration.
     * @param ctType content type
     * @return Content type or <code>null</code> if this the type could not be resolved.
     */
    public String getTemplateKeyFromContentType(EContentType ctType) {
        String sRes = mStandardContentTypeToKeyMap.get(ctType);
        
        if (sRes == null) {
            // Check for generic template key types.
            switch (ctType) {
            case COBOC_FOLDERS_PROCESSBPML :
            case COBOC_FOLDERS_PROCESSBPMN : return "/SUPERTEMPLATE";
            }
        }
        
        return sRes;
    }  
    
    /**
     * Returns the content type enumeration value from the file type.
     * @param sFileType FILE_TYPE attribute value from the file.
     * @return Content type or <code>null</code> if this the type could not be resolved.
     */
    public EContentType getContentTypeFromFileType(String sFileType) {
        return mFileSystemTypeToContentTypeMap.get(sFileType);
    }    
}
