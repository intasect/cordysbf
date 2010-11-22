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

/**
 * A category enumeration that can be used to refer to all content in that category (e.g. all CoBOC folder objects
 * or LDAP content). A category can also have a subcategories. All content of one category is
 * usually handles in a similar way and they are kept under the same folder 
 *
 * @author mpoyhone
 */
public enum EContentCategory
{
    COBOC,
    COBOC_FOLDERS,
    COBOC_RULES,
    COBOC_SCHEDULES,
    COBOC_MESSAGE_MODELS,
    MDM,
    XMLSTORE;
    
    /**
     * Returns the content types in this category.
     * @return An array of all <code>EContentType</code> objects that belong to this category.
     */
    public EContentType[] getContentTypes() {
        switch (this) {
        case COBOC :
            return new EContentType[] {
                EContentType.COBOC_FOLDERS_FOLDER,
                EContentType.COBOC_FOLDERS_TEMPLATE,
                EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE,
                EContentType.COBOC_FOLDERS_GENERIC_INSTANCE,
                EContentType.COBOC_FOLDERS_MAPPING,
                EContentType.COBOC_FOLDERS_PROCESSTEMPLATE,
                EContentType.COBOC_FOLDERS_PROCESSINSTANCE,
                EContentType.COBOC_FOLDERS_PROCESSBPMN,
                EContentType.COBOC_FOLDERS_PROCESSBPML,
                EContentType.COBOC_FOLDERS_CONTENTMAP,
                EContentType.COBOC_FOLDERS_DECISIONCASE,
                EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE,
                EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM,
                EContentType.COBOC_FOLDERS_ACTIONTEMPLATE,
                EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM,
                EContentType.COBOC_RULES_RULE,
                EContentType.COBOC_RULES_RULEGROUP,
                EContentType.COBOC_SCHEDULE_TEMPLATE,     
                EContentType.COBOC_INBOX_MODEL_C1,
                EContentType.COBOC_MESSAGE_TEMPLATE,
                EContentType.COBOC_EMAIL_MODEL,
                EContentType.COBOC_INBOX_MODEL_C3,
                EContentType.MDM_BACKEND,
                EContentType.MDM_ENTITY,
                EContentType.MDM_ENTITY_FOLDER, 
                EContentType.MDM_MODEL,
            };
            
        case COBOC_FOLDERS :
            return new EContentType[] {
                EContentType.COBOC_FOLDERS_FOLDER,
                EContentType.COBOC_FOLDERS_TEMPLATE,
                EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE,
                EContentType.COBOC_FOLDERS_GENERIC_INSTANCE,
                EContentType.COBOC_FOLDERS_MAPPING,
                EContentType.COBOC_FOLDERS_PROCESSTEMPLATE,
                EContentType.COBOC_FOLDERS_PROCESSINSTANCE,
                EContentType.COBOC_FOLDERS_PROCESSBPMN,
                EContentType.COBOC_FOLDERS_PROCESSBPML,
                EContentType.COBOC_FOLDERS_CONTENTMAP,
                EContentType.COBOC_FOLDERS_DECISIONCASE,
                EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE,
                EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM,
                EContentType.COBOC_FOLDERS_ACTIONTEMPLATE,
                EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM,
            };
            
        case COBOC_RULES :
            return new EContentType[] {
                EContentType.COBOC_RULES_RULE,                 
                EContentType.COBOC_RULES_RULEGROUP,
            };            

        case COBOC_SCHEDULES :
            return new EContentType[] {
                EContentType.COBOC_SCHEDULE_TEMPLATE,                 
            };      
            
        case COBOC_MESSAGE_MODELS :
            return new EContentType[] {
                EContentType.COBOC_INBOX_MODEL_C1,
                EContentType.COBOC_MESSAGE_TEMPLATE,
                EContentType.COBOC_EMAIL_MODEL,                    
                EContentType.COBOC_INBOX_MODEL_C3,                 
            }; 
            
        case MDM :
            return new EContentType[] {
                EContentType.MDM_BACKEND,
                EContentType.MDM_ENTITY, 
                EContentType.MDM_ENTITY_FOLDER,
                EContentType.MDM_MODEL,
            };
            
        case XMLSTORE :
            return new EContentType[] {
                EContentType.XMLSTORE_FOLDER,
                EContentType.RUNTIME_XFORM,
            };
        }
        
        throw new IllegalStateException("getContentTypes() not implemented for category " + this);
    }
    
    /**
     * Returns all subcategories for this category or <code>null</code> if this contains none.
     * @return All subcategories for this category or <code>null</code> if this contains none.
     */
    public EContentCategory[] getSubCategories() {
        switch (this) {
        case COBOC :
            return new EContentCategory[] {
                COBOC_FOLDERS, COBOC_RULES, MDM, COBOC_SCHEDULES, COBOC_MESSAGE_MODELS
            };
            
        case COBOC_FOLDERS :
        case COBOC_RULES :
        case COBOC_SCHEDULES :
        case MDM :
        case XMLSTORE :
            return null;
        }     
        
        throw new IllegalStateException("getSubCategories() not implemented for category " + this);
    }
}
