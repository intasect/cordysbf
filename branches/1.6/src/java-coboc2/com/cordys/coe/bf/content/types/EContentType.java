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
package com.cordys.coe.bf.content.types;

import com.cordys.tools.ant.cm.EBcpVersion;


/**
 * Contains all supported content types. 
 * 
 * When adding a new type modify the following places:
 * - com.cordys.coe.bf.content.types.EContentType.getLogName()
 * - com.cordys.coe.bf.content.types.EContentType.getCategory()
 * - com.cordys.coe.bf.content.types.EContentType.getMinimumVersion
 * - com.cordys.coe.bf.content.types.EContentCategory.getContentTypes()
 * - com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem.getContentTypeFromXml()
 * - com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem.getContentFileName()
 * - com.cordys.coe.bf.config.CobocConfig.setEnabledContentTypes()
 * @author mpoyhone
 */
public enum EContentType
{
    // LDAP content.
    METHOD_SET,
    METHOD,
    METHOD_XSD,
    ROLE,
    
    // XMLStore content
    XMLSTORE_FOLDER,
    RUNTIME_XFORM,
    MENU,
    TOOLBAR,
    
    // CoBOC content.
    COBOC_FOLDERS_FOLDER,
    COBOC_FOLDERS_TEMPLATE,
    COBOC_FOLDERS_SPECIAL_ATTRIBUTE,
    COBOC_FOLDERS_GENERIC_INSTANCE,
    COBOC_FOLDERS_MAPPING,
    COBOC_FOLDERS_PROCESSTEMPLATE,
    COBOC_FOLDERS_PROCESSINSTANCE,
    COBOC_FOLDERS_PROCESSBPMN,
    COBOC_FOLDERS_PROCESSBPML,
    COBOC_FOLDERS_CONTENTMAP,
    COBOC_FOLDERS_CONDITIONTEMPLATE,
    COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM,
    COBOC_FOLDERS_ACTIONTEMPLATE,
    COBOC_FOLDERS_ACTIONTEMPLATE_XFORM,
    COBOC_FOLDERS_DECISIONCASE,
    COBOC_RULES_RULE,
    COBOC_RULES_RULEGROUP,
    COBOC_SCHEDULE_TEMPLATE,
    COBOC_MESSAGE_TEMPLATE,
    COBOC_INBOX_MODEL_C1,
    COBOC_INBOX_MODEL_C3,
    COBOC_EMAIL_MODEL,
    
    // MDM content
    MDM_ENTITY_FOLDER,
    MDM_ENTITY,
    MDM_BACKEND,
    MDM_MODEL;
    
    /**
     * Default Constructor for EContentType. 
     */
    private EContentType() {
    }    
    
    /**
     * Returns the log name for this content type. The name is always singular.
     * @return Log name.
     */
    public String getLogName() {    
        return getLogName(false);
    }
    
    /**
     * Returns the log name for this content type with optional plurality argument.
     * @param bPlural If <code>true</code> the name will be returned as plural otherwise singular.
     * @return Log name.
     */
    public String getLogName(boolean bPlural) {
        String sRes;
    
        switch (this) {
        case METHOD_SET : sRes = "method set"; break;
        case METHOD : sRes = "method"; break;
        case METHOD_XSD : sRes = "XSD"; break;
        case ROLE : sRes = "role"; break;
        case MENU : sRes = "menu"; break;
        case TOOLBAR : sRes = "toolbar"; break;
        case XMLSTORE_FOLDER : sRes = "XMLStore folder"; break;
        case RUNTIME_XFORM : sRes = "xform"; break;
        case COBOC_FOLDERS_FOLDER : sRes = "folder"; break;
        case COBOC_FOLDERS_TEMPLATE : sRes = "template"; break;
        case COBOC_FOLDERS_SPECIAL_ATTRIBUTE : sRes = "special attribute"; break;
        case COBOC_FOLDERS_GENERIC_INSTANCE : sRes = "object instance"; break;
        case COBOC_FOLDERS_MAPPING : sRes = "mapping"; break;
        case COBOC_FOLDERS_PROCESSINSTANCE : sRes = "process instance"; break;
        case COBOC_FOLDERS_PROCESSTEMPLATE : sRes = "process template"; break;
        case COBOC_FOLDERS_PROCESSBPMN : sRes = "process BPMN"; break;
        case COBOC_FOLDERS_PROCESSBPML : sRes = "process BPML"; break;
        case COBOC_FOLDERS_CONTENTMAP : sRes = "content map"; break;
        case COBOC_FOLDERS_DECISIONCASE : sRes = "decision case"; break;
        case COBOC_FOLDERS_CONDITIONTEMPLATE : sRes = "condition template"; break;
        case COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM : sRes = "condition template XForm"; break;
        case COBOC_FOLDERS_ACTIONTEMPLATE : sRes = "action template"; break;
        case COBOC_FOLDERS_ACTIONTEMPLATE_XFORM : sRes = "action template XForm"; break;
        case COBOC_RULES_RULE : sRes = "rule"; break;
        case COBOC_RULES_RULEGROUP : sRes = "rule group"; break;
        case COBOC_SCHEDULE_TEMPLATE : sRes = "schedule template"; break;
        case COBOC_MESSAGE_TEMPLATE : sRes = "message template"; break;
        case COBOC_INBOX_MODEL_C1 : sRes = "legacy inbox model"; break;
        case COBOC_INBOX_MODEL_C3 : sRes = "inbox model"; break;
        case COBOC_EMAIL_MODEL : sRes = "email model"; break;
        case MDM_ENTITY : sRes = "MDM entity"; break;
        case MDM_ENTITY_FOLDER : sRes = "MDM entity folder"; break;
        case MDM_BACKEND : sRes = "MDM backend"; break;
        case MDM_MODEL : sRes = "MDM model"; break;
        default : sRes = toString();
        }
        
        if (bPlural) {
            switch (this) {
            case MDM_ENTITY : 
                sRes = sRes.substring(0, sRes.length() - 1) + "ies"; 
                break;
            default :
                sRes += "s";
                break;
            }
        }
        
        return sRes;
    }
    
    /**
     * Returns the name for this content type that is used in the configuration. This is
     * used for the content enabling parameter.
     * @return Configuration name or <code>null</code> if this type does not use one.
     */
    public String getConfigName() {
        String sRes = null;
    
        switch (this) {
        case COBOC_FOLDERS_FOLDER : sRes = "folders"; break;
        case COBOC_FOLDERS_TEMPLATE : sRes = "templates"; break;
        case COBOC_FOLDERS_SPECIAL_ATTRIBUTE : sRes = "special-attributes"; break;
        case COBOC_FOLDERS_GENERIC_INSTANCE : sRes = "object-instances"; break;
        case COBOC_FOLDERS_MAPPING : sRes = "mappings"; break;
        case COBOC_FOLDERS_PROCESSTEMPLATE : sRes = "bpms"; break;
        case COBOC_FOLDERS_PROCESSBPMN : sRes = "bpmns"; break;
        case COBOC_FOLDERS_PROCESSBPML : sRes = "bpmls"; break;
        case COBOC_FOLDERS_CONTENTMAP : sRes = "content-maps"; break;
        case COBOC_FOLDERS_DECISIONCASE : sRes = "decision-cases"; break;
        case COBOC_FOLDERS_CONDITIONTEMPLATE : sRes = "condition-templates"; break;
        case COBOC_FOLDERS_ACTIONTEMPLATE : sRes = "action-templates"; break;
        case COBOC_RULES_RULE : sRes = "rules"; break;
        case COBOC_RULES_RULEGROUP : sRes = "rule-groups"; break;
        case COBOC_SCHEDULE_TEMPLATE : sRes = "schedules"; break;
        case COBOC_INBOX_MODEL_C1 : sRes = "c2-inbox-models"; break;
        case COBOC_MESSAGE_TEMPLATE : sRes = "message-templates"; break;
        case COBOC_EMAIL_MODEL : sRes = "email-models"; break;
        case COBOC_INBOX_MODEL_C3 : sRes = "inbox-models"; break;
        case MDM_BACKEND : sRes = "mdm"; break; // MDM content cannot be disabled individually.
        }
        
        return sRes;
    }
    
    /**
     * Returns the content category for this content type. 
     * @return Content category enumeration.
     */
    public EContentCategory getCategory() {
        switch (this) {
        case METHOD_SET :
        case METHOD :
        case METHOD_XSD :
        case ROLE :
            // Not impl yet.
            break;
        case MENU :
        case TOOLBAR :
            // Not impl yet.
            break;
            
        case XMLSTORE_FOLDER :
        case RUNTIME_XFORM :
            return EContentCategory.XMLSTORE;
            
        case COBOC_FOLDERS_FOLDER :
        case COBOC_FOLDERS_TEMPLATE :
        case COBOC_FOLDERS_SPECIAL_ATTRIBUTE :
        case COBOC_FOLDERS_GENERIC_INSTANCE :
        case COBOC_FOLDERS_MAPPING :
        case COBOC_FOLDERS_PROCESSINSTANCE :
        case COBOC_FOLDERS_PROCESSTEMPLATE :
        case COBOC_FOLDERS_PROCESSBPMN :
        case COBOC_FOLDERS_PROCESSBPML :
        case COBOC_FOLDERS_CONTENTMAP :
        case COBOC_FOLDERS_DECISIONCASE :
        case COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM :
        case COBOC_FOLDERS_CONDITIONTEMPLATE :
        case COBOC_FOLDERS_ACTIONTEMPLATE_XFORM :
        case COBOC_FOLDERS_ACTIONTEMPLATE :
            return EContentCategory.COBOC_FOLDERS;
            
        case COBOC_RULES_RULE :
        case COBOC_RULES_RULEGROUP :
            return EContentCategory.COBOC_RULES;
            
        case COBOC_SCHEDULE_TEMPLATE :
            return EContentCategory.COBOC_SCHEDULES;
        
        case COBOC_INBOX_MODEL_C1 :
        case COBOC_MESSAGE_TEMPLATE :
        case COBOC_INBOX_MODEL_C3 :
        case COBOC_EMAIL_MODEL :
            return EContentCategory.COBOC_MESSAGE_MODELS;
            
        case MDM_ENTITY :
        case MDM_ENTITY_FOLDER :
        case MDM_BACKEND :
        case MDM_MODEL :
            return EContentCategory.MDM;
        }
        
        throw new IllegalStateException("getCategory() not implemented for content type "  + this);
    }
    
    /**
     * Returns the minimum platform version for this content type.
     * @return Minimum version value or <code>null</code> if this is supported for all versions.
     */
    public EBcpVersion getMinimumVersion()
    {
        switch (this) {
        case COBOC_FOLDERS_PROCESSBPML :
        case COBOC_MESSAGE_TEMPLATE :
        case COBOC_EMAIL_MODEL :
        case COBOC_INBOX_MODEL_C3 :
            return EBcpVersion.BCP42_C3;
        }
        
        return null;
    }
}

