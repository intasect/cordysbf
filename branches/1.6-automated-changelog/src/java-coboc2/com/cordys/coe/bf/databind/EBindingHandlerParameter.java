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
package com.cordys.coe.bf.databind;


/**
 * Contains all binding handler parameters that can be specified in the binding template XML.
 *
 * @author mpoyhone
 */
public enum EBindingHandlerParameter
{
    /**
     * Handler is skipped if the context is marked as insert operation.
     */
    SKIP_ON_INSERT("skip-insert"),
    /**
     * Handler is skipped if the context is marked as update operation.
     */
    SKIP_ON_UPDATE("skip-update"),    
    /**
     * Handler is skipped if the context is marked as delete operation.
     */
    SKIP_ON_DELETE("skip-delete"),    
    /**
     * Skip this handler completely if the value is an empty string.
     */
    SKIP_IF_EMPTY("skip-empty"),
    /**
     * Skip this handler completely if the value is null.
     */
    SKIP_IF_NULL("skip-null"),
    /**
     * Namespace of an XML field needs to be is preserved.
     */
    PRESERVE_NAMESPACE("keep-ns"),
    /**
     * If the value exists in the bean/properties when unmarshalling content, the old value
     * is overwritten with the current value.
     */
    REPLACE("replace");
    
    /**
     * Contains the name in the configuration.
     */
    private String sConfigName;
    
    /**
     * Constructor for EBindingParameter
     * @param sConfigName Parameter configuration name. 
     */
    private EBindingHandlerParameter(String sConfigName) {
        this.sConfigName = sConfigName;
    }
    
    /**
     * Returns the paramater enum object by the configuration name.
     * @param sConfigName Parameter configuration name. 
     * @return Binding parameter enumeration object or <code>null</code> if none was found.
     */
    public static EBindingHandlerParameter findByConfigurationName(String sConfigName) {
        for (EBindingHandlerParameter bpParam : values())
        {
            if (bpParam.sConfigName.equals(sConfigName)) {
                return bpParam;
            }
        }
        
        return null;
    }
}
