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

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.commons.beanutils.PropertyUtils;

import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.EBindingHandlerParameter;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * Base class for all Java bean field handlers.
 *
 * @author mpoyhone
 */
public class BeanHandlerBase extends ValueHandlerBase
{
    private PropertyDescriptor pdBeanProperty;
    private String sFieldName;
    private Class<?> cPropertyType;
    private String sBeanId; 
    
    public BeanHandlerBase(String sBeanId, String sFieldName, Class<?> cBeanClass, boolean bIsAttribute) throws BindingException {
        super(bIsAttribute);
        this.sBeanId = sBeanId;
        this.sFieldName = sFieldName;
        
        Object oTmpInstance;
        
        try
        {
            // BeanUtils methods need an instance, so create a temporary one. 
            oTmpInstance = cBeanClass.newInstance();
            pdBeanProperty = PropertyUtils.getPropertyDescriptor(oTmpInstance, sFieldName);
        }
        catch (Exception e)
        {
            throw new BindingException(getXmlName() + ": Unable to access the bean property '" + sFieldName + "' for class " + cBeanClass, e);
        }
        
        if (pdBeanProperty == null) {
            throw new BindingException("Bean property '" + sFieldName + "' not found for class " + cBeanClass);
        }
       
        cPropertyType = pdBeanProperty.getPropertyType();
        
        if (cPropertyType == null) {
            if (pdBeanProperty instanceof MappedPropertyDescriptor) {
                cPropertyType = ((MappedPropertyDescriptor) pdBeanProperty).getMappedPropertyType();
            }
            
            if (cPropertyType == null) {
                throw new BindingException(getXmlName() + ": Unable get the property type for bean '" + sFieldName + "' for class " + cBeanClass);
            }
        }
        
        this.bXmlStructureProperty = XmlStructure.class.isAssignableFrom(this.cPropertyType);
        this.bIsArrayProperty = Collection.class.isAssignableFrom(this.cPropertyType);
    }   
    
    public BeanHandlerBase(String sBeanId, String sFieldName, Class<?> cBeanClass, boolean bIsAttribute, boolean bIsXmlStructure) throws BindingException{
        this(sBeanId, sFieldName, cBeanClass, bIsAttribute);
        this.bXmlStructureElement = bIsXmlStructure;
    }       
    
    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#marshallField(com.cordys.coe.bf.databind.IBindingContext)
     */
    public void marshallField(IBindingContext bcContext) throws BindingException, XMLStreamException
    {
        if (! shouldExecute(bcContext)) {
            return;
        }
        
        if (pdBeanProperty == null) {
            return;
        }
        
        Object oCurrentObject = bcContext.getJavaBean(sBeanId);
        
        if (oCurrentObject == null) {
            throw new BindingException("Java object is not set in the context. Expected bean " + sBeanId);
        }
        
        XMLStreamWriter xswWriter = bcContext.getMarshallingDestination().getStreamWriter();
        Object oPropertyObject;
        
        try
        {
            oPropertyObject = PropertyUtils.getProperty(oCurrentObject, sFieldName);
        }
        catch (Exception e)
        {
            throw new BindingException("Unable to read the property '" + sFieldName +
                    "'.", e);
        }
        
        // Check if this is an array property and if so get the iterator for the elements. 
        Iterator<?> iValueIter = null;
        
        if (bIsArrayProperty) {
            if (oPropertyObject == null) {
                marshallChildren(bcContext);
                return;
            }
            
            if (! (oPropertyObject instanceof Collection)) {
                throw new BindingException("An array property must be of type collection.");
            }
            
            iValueIter = ((Collection<?>) oPropertyObject).iterator();
        }
        
        // A loop for both simple properties and array properties
        while (! bIsArrayProperty || iValueIter.hasNext()) {
            if (bIsArrayProperty) {
                oPropertyObject = iValueIter.next();
            }
        
            if (bXmlStructureElement) {
                // This is an XML structure that should appear as XML tree. 
                XmlStructure xsValue = (XmlStructure) oPropertyObject;
                
                if (xsValue != null) {
                    writeElementWithValue(xswWriter, xsValue, bcContext);
                } else {
                    writeEmptyElement(xswWriter, bcContext);
                }
            } else if (sFieldBeanId != null) {
                // This is a field that contains another bean. We set the bean in the context
                // with the given ID and other handlers do the marshalling.
                Object oOldBean = bcContext.getJavaBean(sFieldBeanId);
                
                bcContext.setJavaBean(sFieldBeanId, oPropertyObject);
                writeElementWithValue(xswWriter, (String) null, bcContext);
                bcContext.setJavaBean(sFieldBeanId, oOldBean);
            } else {
                // This is a simple value field or an XmlStructure that will appear as encoded string.
                String sValue = convertStringProperty(oPropertyObject, sFieldName);
                
                if (sValue != null) {
                    writeElementWithValue(xswWriter, sValue, bcContext);
                } else {
                    writeEmptyElement(xswWriter, bcContext);
                }
            }
            
            if (! bIsArrayProperty) {
                break;
            }
        }
    }
    
    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#unmarshallField(com.cordys.coe.bf.databind.IBindingContext)
     */
    @SuppressWarnings("unchecked")
    public void unmarshallField(IBindingContext bcContext) throws BindingException
    {
        if (! shouldExecute(bcContext)) {
            return;
        }        
        
        if (pdBeanProperty == null) {
            return;
        }
        
        Object oCurrentObject = bcContext.getJavaBean(sBeanId);
        
        if (oCurrentObject == null) {
            throw new BindingException("Java object is not set in the context. Need bean with ID " + sBeanId);
        }

        try
        {
            Object oConvertedValue;
            OMElement oeElem = bcContext.getUnmarshallingSource().getOMElement();
            
            if (bXmlStructureElement) {
                // This is an XML structure that should appear as XML tree. 
                // Create a new XmlStructure instance. The property type must be a subclass
                // of XmlStructure (or the class it self).
                XmlStructure xsStruct = (XmlStructure) cPropertyType.newInstance();
                
                xsStruct.readFromXml(oeElem, getBooleanParameter(EBindingHandlerParameter.PRESERVE_NAMESPACE));
                oConvertedValue = xsStruct;
            } else if (sFieldBeanId != null) {
                // This is a field that contains another bean. We create  the bean 
                // and set it in the context with the given ID and other handlers do 
                // the unmarshalling.  
                oConvertedValue = createBean(bcContext, sFieldBeanId);
                bcContext.setJavaBean(sFieldBeanId, oConvertedValue);
            } else {                
                String sValue = null;
                
                if (! bIsAttribute) {
                    sValue = oeElem.getText().trim();
                } else {
                    sValue = oeElem.getAttributeValue(qnName);
                }
                
                if (! bXmlStructureProperty) {
                    oConvertedValue = ConvertUtils.convert(sValue, cPropertyType);
                } else {
                    // Create a new XmlStructure instance. The property type must be a subclass
                    // of XmlStructure (or the class it self).
                    XmlStructure xsStruct = (XmlStructure) cPropertyType.newInstance();
                    
                    xsStruct.setString(sValue);
                    oConvertedValue = xsStruct;
                }
            }
            
            if (bIsArrayProperty) {
                Object oPropertyObject = PropertyUtils.getProperty(oCurrentObject, sFieldName);
                
                if (oPropertyObject == null) {
                    oPropertyObject = new LinkedList();
                    PropertyUtils.setProperty(oCurrentObject, sFieldName, oPropertyObject);
                }
                
                if (! (oPropertyObject instanceof Collection)) {
                    throw new BindingException("An array property must be of type collection.");
                }
                
                ((Collection) oPropertyObject).add(oConvertedValue);
            } else {
                PropertyUtils.setProperty(oCurrentObject, sFieldName, oConvertedValue);
            }
        }
        catch (Exception e)
        {
            throw new BindingException("Unable to execute the write method for bean '" + sBeanId + "' field '" + sFieldName + "'.", e);
        }
        
        unmarshallChildren(bcContext);
    }
    
    /**
     * Checks the parameters if this handler should execute. 
     * @return <code>true</code> if this handler should execute.
     */
    protected boolean shouldExecute(IBindingContext bcContext) {
        if (! super.shouldExecute(bcContext)) {
            return false;
        }

        if (bcContext.isUnmarshallingContext() && sFieldBeanId == null) {
            boolean bValueExists = false;
            Object oCurrentObject = bcContext.getJavaBean(sBeanId);
            Object oPropertyObject = null;
            
            try
            {
                oPropertyObject = oCurrentObject != null ? PropertyUtils.getProperty(oCurrentObject, sFieldName) : null;
            }
            catch (Exception ignored)
            {
            }
            
            if (oPropertyObject != null) {
                if (bIsArrayProperty) {
                    // Only execute if the collection is empty.
                    bValueExists = ((Collection<?>) oPropertyObject).size() == 0;
                } else {
                    // Simple property is already set.
                    bValueExists = true;
                }
            }
            
            if (bValueExists) {
                return getParameter(EBindingHandlerParameter.REPLACE) != null;
            }
        }
        
        return true;
    }
}
