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

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Interface for binding XML element to Java field.
 * 
 * @author mpoyhone
 */
public interface IBindingHandler
{
    /**
     * Called when a field is to be written to XML stream. The handler should
     * call marshalling for all subfields itself.
     * 
     * @param bcContext
     *            Current binding context.
     */
    public void marshallField(IBindingContext bcContext) throws BindingException, XMLStreamException;

    /**
     * Called when a field is to be read from XML stream. The handler should
     * call unmarshalling for all subfields itself.
     * 
     * @param bcContext
     *            Current binding context.
     */
    public void unmarshallField(IBindingContext bcContext) throws BindingException;
    
    /**
     * Sets a parameter for this handler.
     * @param bpParam Parameter type.
     * @param sValue Parameter value.
     */
    public void setParameter(EBindingHandlerParameter bpParam, String sValue);
    
    /**
     * Returns a parameter value for this handler.
     * @param bpParam Parameter type.
     * @return Parameter value or <code>null</code> if the parameter was not set.
     */
    public String getParameter(EBindingHandlerParameter bpParam);    

    /**
     * Returns a list of all handlers that are specified under this handler.
     * 
     * @return A list of handlers under this handler.
     */
    public List<IBindingHandler> getChildren();

    /**
     * Returns the name of this handler. This is used for handler find
     * operations. For all handlers this should the the matching XML element
     * name or &at;element for attributes.
     * 
     * @return Handler name.
     */
    public String getHandlerName();
    
    /**
     * Returns the fully qualified XML name.
     * @return XML name of this elemement/attribute.
     */
    public QName getXmlName();
    /**
     * Sets the fully qualified XML name.
     * @param qnName XML name of this elemement/attribute.
     */
    public void setXmlName(QName qnName);
    
    /**
     * Returns the parent handler.
     * @return This handlers parent handler or <code>null</code> if this is the root handler.
     */
    public IBindingHandler getParent();
    
    /**
     * Sets this handlers parent handler 
     * @param bhHandler Parent handler.
     */
    public void setParent(IBindingHandler bhHandler);
    
    /**
     * Add a new child handler under this one.
     * @param bhHandler Child handler to be added.
     */
    public boolean addChildHandler(IBindingHandler bhHandler) throws BindingException;
    
    /**
     * Removes a child handler.
     * @param bhHandler Child handler to be removed.
     */
    public boolean removeChildHandler(IBindingHandler bhHandler) throws BindingException;
    
    /**
     * Removes a child handler by the given name.
     * @param bhHandler Name of the child handler to be removed.
     */
    public boolean removeChildHandler(String sHandlerName) throws BindingException;    
    
    /**
     * Replaces a child handler.
     * @param bhOriginalHandler Child handler to be replaced.
     * @param bhNewHandler New child handler.
     */
    public boolean replaceChildHandler(IBindingHandler bhOriginalHandler, IBindingHandler bhNewHandler) throws BindingException;
    
    /**
     * Removes a child handler by the given name.
     * @param bhHandler Name of the child handler to be replaced.
     */
    public boolean replaceChildHandler(String sOriginalHandlerName, IBindingHandler bhNewHandler) throws BindingException;
    
    /**
     * Finds a child handler by the given name.
     * @param bhHandler Name of the child handler to be returned.
     */
    public IBindingHandler findChildHandler(String sHandlerName) throws BindingException;
    
    /**
     * Returns the isAttribute.
     *
     * @return Returns the isAttribute.
     */
    public boolean isAttribute();
    /**
     * The isAttribute to set.
     *
     * @param aIsAttribute The isAttribute to set.
     */
    public void setAttribute(boolean aIsAttribute);    
    
    /**
     * Returns the default value for this handler, if any is set.
     * @return Default value or <code>null</code> if none is set.
     */
    public String getDefaultValue();
    
    /**
     * Sets the default value for this handler, if any is set.
     * @param sValue Default value.
     */
    public void setDefaultValue(String sValue);    
}
