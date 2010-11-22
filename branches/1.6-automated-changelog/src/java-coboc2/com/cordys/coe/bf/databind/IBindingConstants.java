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
 * Defines standard bean names for binding handlers.
 *
 * @author mpoyhone
 */
public interface IBindingConstants
{
    /**
     * Default bean name for templates that have only one bean.
     */
    public static final String BEANNAME_DEFAULT = "default";
    /**
     * Tuple/old/new protocol old bean name.
     */
    public static final String BEANNAME_OLD = "old";
    /**
     * Tuple/old/new protocol new bean name.
     */
    public static final String BEANNAME_NEW = "new";
    /**
     * Content type subtype for BCP update templates.
     */
    public static final String SUBTYPE_BCP_UPDATE = "BCP-Update";
    /**
     * Content type subtype for BCP deletion templates.
     */
    public static final String SUBTYPE_BCP_DELETE = "BCP-Delete";    
    /**
     * Update modes. 
     */
    public enum EUpdateMode {
        NONE, INSERT, UPDATE, DELETE;
    }
}
