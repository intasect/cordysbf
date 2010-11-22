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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.databind.IBindingHandler;
import com.cordys.coe.bf.databind.IBindingTemplate;

/**
 * Implements the binding context interface.
 *
 * @author mpoyhone
 */
public class BindingContextImpl implements IBindingContext
{
    private IXmlSource xsUnmarshallSource;
    private IXmlDestination xsMarshallDestination;
    private IBindingHandler bhBaseHandler;
    private Map<String, Object> mBeanMap = new HashMap<String, Object>();
    private BindingParameters bpParameters = new BindingParameters();
    private Map<String, IContentUnmarshaller> mBeanUnmarshallerMap = new HashMap<String, IContentUnmarshaller>();
    private Map<String, IContentMarshaller> mBeanMarshallerMap = new HashMap<String, IContentMarshaller>();
    private BindingParameters bsState;
    private IBindingTemplate btTemplate;
    private QName currentNamespace;
    
    public BindingContextImpl() {
        bsState = new BindingParameters();
    }
    
    public BindingContextImpl(IBindingTemplate btTemplate, IBindingHandler bhBaseHandler, IXmlSource xsUnmarshallSource) {
        bsState = new BindingParameters();
        
        this.btTemplate = btTemplate;
        this.xsUnmarshallSource = xsUnmarshallSource;
        this.bhBaseHandler = bhBaseHandler;
        
        if (this.xsUnmarshallSource.getOMElement() == null) {
            throw new IllegalArgumentException("Binding content only supports OMElement source.");
        }
    }

    public BindingContextImpl(IBindingTemplate btTemplate, IBindingHandler bhBaseHandler, IXmlDestination xsMarshallDestination) {
        bsState = new BindingParameters();
       
        this.btTemplate = btTemplate;
        this.xsMarshallDestination = xsMarshallDestination;
        this.bhBaseHandler = bhBaseHandler;
        
        if (this.xsMarshallDestination.getStreamWriter() == null) {
            throw new IllegalArgumentException("Binding content only supports XMLStreamWriter destination.");
        }        
    }
    
    public IBindingHandler getBaseHandler() {
        return bhBaseHandler;
    }
    
    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#getUnmarshallingSource()
     */
    public IXmlSource getUnmarshallingSource() throws BindingException
   {
        if (xsUnmarshallSource == null) {
            throw new BindingException("Binding content is not in unmarshalling mode.");
        }
        
        return xsUnmarshallSource;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#getMarshallingDestination()
     */
    public IXmlDestination getMarshallingDestination() throws BindingException
    {
        if (xsMarshallDestination == null) {
            throw new BindingException("Binding content is not in marshalling mode.");
        }
        
        return xsMarshallDestination;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#setJavaBean(java.lang.String, java.lang.Object)
     */
    public void setJavaBean(String sBeanId, Object oObject)
    {
        mBeanMap.put(sBeanId, oObject);
    }
    
    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#setJavaBean(java.lang.String, java.lang.Object, com.cordys.coe.bf.content.base.IContentUnmarshaller)
     */
    public void setJavaBean(String sBeanId, Object oObject, IContentUnmarshaller cuUnmarshaller)
    {
        setJavaBean(sBeanId, oObject);
        mBeanUnmarshallerMap.put(sBeanId, cuUnmarshaller);
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#setJavaBean(java.lang.String, java.lang.Object, com.cordys.coe.bf.content.base.IContentMarshaller)
     */
    public void setJavaBean(String sBeanId, Object oObject, IContentMarshaller cmMarshaller)
    {
        setJavaBean(sBeanId, oObject);
        mBeanMarshallerMap.put(sBeanId, cmMarshaller);
    }    

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#getJavaBean(java.lang.String)
     */
    public Object getJavaBean(String sBeanId)
    {
        return mBeanMap.get(sBeanId);
    }
    
    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#getJavaBeanUnmarshaller(java.lang.String)
     */
    public IContentUnmarshaller getJavaBeanUnmarshaller(String sBeanId)
    {
        return mBeanUnmarshallerMap.get(sBeanId);
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#getJavaBeanMarshaller(java.lang.String)
     */
    public IContentMarshaller getJavaBeanMarshaller(String sBeanId)
    {
        return mBeanMarshallerMap.get(sBeanId);
    }    

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#getRootHandler()
     */
    public IBindingHandler getRootHandler()
    {
        return bhBaseHandler;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#setParameters(BindingParameters)
     */
    public void setParameters(BindingParameters bpParams)
    {
        this.bpParameters = bpParams;
        
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#getParameters()
     */
    public BindingParameters getParameters()
    {
        return bpParameters;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#isUnmarshallingContext()
     */
    public boolean isUnmarshallingContext()
    {
        return xsMarshallDestination == null;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#getState()
     */
    public BindingParameters getState()
    {
        return bsState;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#getBindingTemplate()
     */
    public IBindingTemplate getBindingTemplate()
    {
        return btTemplate;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#setCurrentNamespace(javax.xml.namespace.QName)
     */
    public QName setCurrentNamespace(QName qnNew)
    {
        QName tmp = currentNamespace;
        
        currentNamespace = qnNew;
        
        return tmp;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingContext#getCurrentNamespace()
     */
    public QName getCurrentNamespace()
    {
        return currentNamespace;
    }
    
    
}
