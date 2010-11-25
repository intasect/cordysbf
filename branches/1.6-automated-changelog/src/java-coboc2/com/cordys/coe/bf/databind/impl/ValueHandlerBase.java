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

import org.apache.commons.beanutils.ConvertUtils;

import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.databind.IBindingTemplate;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * Base class for all handlers that handle values in the XML, e.g. Bean properties
 * or the context properties.
 *
 * @author mpoyhone
 */
public abstract class ValueHandlerBase extends BindingHandlerBase
{
    /**
     * If set, the property will be a sub-bean. The bean must exist in the bean definition section.
     */
    protected String sFieldBeanId;
    /**
     * If <code>true</code> this is an array (indexed) property. The actual property must be of type 
     * Collection (e.g. a LinkedList).
     */
    protected boolean bIsArrayProperty;
    /**
     * If <code>true</code> the property is of type XmlStructure.
     */
    protected boolean bXmlStructureProperty;
    /**
     * If <code>true</code> the property is of type XmlStructure and the value will appear
     * as a sub-XML tree in the final XML, otherwise the XML will be encoded as a string. 
     */
    protected boolean bXmlStructureElement;    
    
    /**
     * Constructor for ValueHandlerBase
     */
    public ValueHandlerBase() {
        
    }

    /**
     * Constructor for ValueHandlerBase
     * @param qnName
     */
    public ValueHandlerBase(QName qnName) {
        super(qnName, false);
    }

    /**
     * Constructor for ValueHandlerBase
     * @param bIsAttribute
     */
    public ValueHandlerBase(boolean bIsAttribute) {
        super(bIsAttribute);
    }    
    
    /**
     * Constructor for ValueHandlerBase
     * @param qnName
     * @param bIsAttribute
     */
    public ValueHandlerBase(QName qnName, boolean bIsAttribute) {
        super(qnName, bIsAttribute);
    }   
    
    /**
     * Converts the given property to string based on the handler configuration.
     * @param oValueObject Property value as an Object/
     * @param sFieldName Property name for logging purposes.
     * @return Converted value as a string or <code>null</code> if the value could not be converted (or is <code>null</code>).
     * @throws BindingException
     */
    protected String convertStringProperty(Object oValueObject, String sFieldName) throws BindingException {
        try
        {
             if (oValueObject != null) {
                if (! bXmlStructureProperty) {
                    if (! (oValueObject instanceof String)) {
                        return ConvertUtils.convert(oValueObject);
                    } else {
                        return oValueObject.toString();
                    }
                } else {
                    XmlStructure xsValue = (XmlStructure) oValueObject;
                    
                    return  xsValue.getString();
                }
            }
        }
        catch (Exception e)
        {
            throw new BindingException("Unable to convert the property '" + sFieldName +
                    "'.", e);
        }
        
        return null;
    }  
    
    /**
     * Creates a bean that is specified in the bean section of the configuration.
     * @param bcContext Binding context that contains the bean information.
     * @param sBeanId Bean ID of the bean to be created.
     * @throws BindingException Thrown if the binding template is not set or the bean creation failed.
     */
    protected Object createBean(IBindingContext bcContext, String sBeanId) throws BindingException {
        IBindingTemplate btTemplate = bcContext.getBindingTemplate();
        
        if (btTemplate == null) {
            throw new BindingException("Binding template is not set in the context.");
        }
        
        Class<?> cBeanClass = btTemplate.getBeanClass(sBeanId);
        
        if (cBeanClass == null) {
            throw new BindingException("Bean class not found for bean ID " + sBeanId);
        }
        
        try
        {
            return cBeanClass.newInstance();
        }
        catch (Exception e)
        {
            throw new BindingException("Unable to instantiate the bean class " + cBeanClass, e);
        }
    }
    
    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#getDefaultValue()
     */
    public String getDefaultValue()
    {
        // We don't use default values.
        return null;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(String sValue)
    {
        // We don't use default values.
    }

    /**
     * Returns the fieldBeanId.
     *
     * @return Returns the fieldBeanId.
     */
    public String getFieldBeanId()
    {
        return sFieldBeanId;
    }

    /**
     * The fieldBeanId to set.
     *
     * @param aFieldBeanId The fieldBeanId to set.
     */
    public void setFieldBeanId(String aFieldBeanId)
    {
        sFieldBeanId = aFieldBeanId;
    }    
}
