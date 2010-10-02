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
package com.cordys.coe.bf.databind.impl;

import javax.xml.namespace.QName;

/**
 * Context property to XML attribute mapper.
 *
 * @author mpoyhone
 */
public class PropertyAttributeHandler extends PropertyHandlerBase
{
    /**
     * Constructor for PropertyAttributeHandler
     * @param sPropertyName
     */
    public PropertyAttributeHandler(String sPropertyName)
    {
        super(sPropertyName, true);
    }
    /**
     * Constructor for PropertyAttributeHandler
     * @param qnName
     * @param sPropertyName
     */
    public PropertyAttributeHandler(QName qnName, String sPropertyName)
    {
        super(qnName, sPropertyName, true);
    }

}
