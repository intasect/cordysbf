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

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;

/**
 * Interface for a class that holds binding operation configuration.
 *
 * @author mpoyhone
 */
public interface IBindingTemplate
{
    /**
     * Gets an ID for this template. This is usually read from the configuration file.
     * @return Template ID or <code>null</code> if none was set.
     */
    public String getTemplateId();
    
    /**
     * Sets the ID for this template. 
     * @param sId Template ID.
     */
    public void setTemplateId(String sId);
    
    /**
     * Creates a new binding context based on this template. The context is created
     * in marhsalling mode.
     * 
     * @return New marshalling binding context.
     */
    public IBindingContext createMarshallingContext(IXmlDestination xdDestination);
    
    /**
     * Creates a new binding context based on this template. The context is created
     * in unmarshalling mode.
     *  
     * @return New unmarshalling binding context.
     */
    public IBindingContext createUnmarshallingContext(IXmlSource xsSource);    
    
    /**
     * Returns the root handler for defined for this template.
     * 
     * @return Root handler.
     */
    public IBindingHandler getRootHandler();
    
    /**
     * Returns the configured class for the given bean. These classes are only read from the
     * configuration file beans configuration section. If this template was not loaded from a file
     * the classes will not be set.
     * @param sBeanId Bean ID.
     * @return Configured class or <code>null</code> if none was set.
     */
    public Class<?> getBeanClass(String sBeanId);
    
    /**
     * Replaces an existing handler with the new one.
     * @param sHandlerPath Path to the handler.
     * @param bhNewHandler Replacing handler. 
     * @throws BindingException Thrown if the operation failed.
     */
    public void replaceHandler(String sHandlerPath, IBindingHandler bhNewHandler) throws BindingException;
    
    /**
     * Parses an XML instance as a this template. This instance is used as a skeleton for the binding
     * operations.
     * 
     * @param oeRootElement Template XML root element.
     * @throws BindingException Thrown if the operation failed.
     */
    public void parseHandlersFromXml(OMElement oeRootElement) throws BindingException;
    
    /**
     * Parses a configuration XML that contains the template XML structure as well as the handler definitions.
     * 
     * @param oeRootElement Configuration XML root element.
     * @param xlLoader Loader used to load imported templates.
     * @throws BindingException Thrown if the operation failed.
     */
    public void parseConfigurationFromXml(OMElement oeRootElement, XmlLoader xlLoader) throws BindingException;    
    
    /**
     * Parses an XML configuration file that contains the XML structure as well as the handler definitions.
     * 
     * @param sFilePath File path.
     * @param xlLoader XML loader that contains the root location information. 
     * @throws BindingException Thrown if the operation failed.
     */
    public void parseConfiguration(String sFilePath, XmlLoader xlLoader) throws BindingException;      
}
