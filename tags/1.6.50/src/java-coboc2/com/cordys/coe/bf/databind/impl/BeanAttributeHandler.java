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

import java.beans.IntrospectionException;

import com.cordys.coe.bf.databind.BindingException;

/**
 * Java bean to XML attribute mapper.
 *
 * @author mpoyhone
 */
public class BeanAttributeHandler extends BeanHandlerBase
{
    /**
     * Constructor for BeanAttributeHandler
     * @param sFieldName
     * @param cBeanClass
     * @param bIsAttribute
     * @param bIsXmlStructure
     * @throws IntrospectionException
     */
    public BeanAttributeHandler(String sBeanId, String sFieldName, Class<?> cBeanClass, boolean bIsAttribute, boolean bIsXmlStructure) throws BindingException
    {
        super(sBeanId, sFieldName, cBeanClass, bIsAttribute, bIsXmlStructure);
    }
    /**
     * Constructor for BeanAttributeHandler
     * @param qnName
     * @param sFieldName
     * @param cBeanClass
     * @throws IntrospectionException
     */
    public BeanAttributeHandler(String sBeanId, String sFieldName, Class<?> cBeanClass) throws BindingException
    {
        super(sBeanId, sFieldName, cBeanClass, true);
    }


}
