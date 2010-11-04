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

import javax.xml.namespace.QName;

import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;

/**
 * Interface for keeping the context in binding operations.
 * 
 * @author mpoyhone
 */
public interface IBindingContext
{
    /**
     * Returns the binding template that this contet is based on.
     * @return Binding template or <code>null</code> if no binding template is set for this context.
     */
    public IBindingTemplate getBindingTemplate();
    
    /**
     * Sets template specific properties.
     * @param mProperties To be set. 
     */
    public void setParameters(BindingParameters bpParams);
    
    /**
     * Returns template specific properties.
     * @return Propeties as a map.
     */
    public BindingParameters getParameters();
    
    /**
     * Returns the root handler for defined for this template.
     * 
     * @return Root handler.
     */
    public IBindingHandler getRootHandler();
    
    /**
     * Returns the current XML source when unmarshalling data from XML.
     * 
     * @return Current XML source.
     * @throws BindingException Thrown if the context is not specified for unmarshalling.
     */
    public IXmlSource getUnmarshallingSource() throws BindingException;
    
    /**
     * Returns if this context is set for unmarshalling or marshalling.
     * @return <code>true</code> if this is an unmarshalling context or <code>false</code> if this is a marshalling context.
     */
    public boolean isUnmarshallingContext();
    
    /**
     * Sets the current JavaBean object.
     * @param sBeanId ID of the bean.
     * @param oObject Object to be set.
     */
    public void setJavaBean(String sBeanId, Object oObject);
    
    /**
     * Sets the current JavaBean object with the given unmarshaller. This
     * method is inteded for set objects that will be handled by a specific handler (e.g. the TupleOldNewHandler).
     * The context must be in unmarshalling mode.
     * @param sBeanId ID of the bean.
     * @param cuUnmarshaller Unmarshaller to be set or <code>null</code> if no unmarshalling is required.
     * @param oObject Object to be set.
     */
    public void setJavaBean(String sBeanId, Object oObject, IContentUnmarshaller cuUnmarshaller);    

    /**
     * Sets the current JavaBean object with the given marshaller. This
     * method is inteded for set objects that will be handled by a specific handler (e.g. the TupleOldNewHandler).
     * The context must be in marshalling mode.
     * @param sBeanId ID of the bean.
     * @param cmMarshaller Marshaller to be set or <code>null</code> if no marshalling is required.
     * @param oObject Object to be set.
     */
    public void setJavaBean(String sBeanId, Object oObject, IContentMarshaller cmMarshaller);    
    
    /**
     * Return the current JavaBean object.
     * @param sBeanId ID of the bean.
     * @param oObject Object to be set.
     */
    public Object getJavaBean(String sBeanId);

    /**
     * Returns the unmarshaller set for this bean.
     * @param sBeanId Bean ID.
     * @return Unmarshaller or <code>null</code> if none was set.
     */
    public IContentUnmarshaller getJavaBeanUnmarshaller(String sBeanId);
    
    /**
     * Returns the marshaller set for this bean.
     * @param sBeanId Bean ID.
     * @return Marshaller or <code>null</code> if none was set.
     */
    public IContentMarshaller getJavaBeanMarshaller(String sBeanId);
    
    /**
     * Returns the XML destination when marshalling data to XML.
     * @return The current StAX writer.
     * @throws BindingException Thrown if the context is not specified for unmarshalling.
     */
    public IXmlDestination getMarshallingDestination() throws BindingException;
    
    /**
     * Sets the current namespace URI and returns the old one.
     * @param qnNew New namespace as a qname object.
     * @return Old namespace or <code>null</code> if no was set.
     */
    public QName setCurrentNamespace(QName qnNew);
    
    /**
     * Returns the current namespace URI or <code>null</code> if no was set
     * @return Current namespace or <code>null</code> if no was set.
     */
    public QName getCurrentNamespace();    
}
