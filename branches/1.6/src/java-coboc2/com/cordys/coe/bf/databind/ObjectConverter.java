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

import java.io.IOException;

import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.databind.impl.BindingTemplateImpl;
import com.cordys.coe.bf.exception.BFException;

/**
 * A wrapper class for holding the binding template. This implements the IContentUnmarshaller and
 * IContentMarshaller interfaces.
 *
 * @author mpoyhone
 */
public class ObjectConverter implements IContentUnmarshaller,
        IContentMarshaller
{
    private IBindingTemplate btTemplate;
    private String sBeanId;
    
    public ObjectConverter(String sBeanId, String sPath, XmlLoader xlLoader) throws BindingException {
        btTemplate = new BindingTemplateImpl();
        btTemplate.parseConfiguration(sPath, xlLoader);
        this.sBeanId = sBeanId;
    }   
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentUnmarshaller#unmarshalObject(com.cordys.coe.bf.content.base.IXmlSource)
     */
    public IContent unmarshalObject(IXmlSource sSrc) throws BFException
    {
        return unmarshalObject(sSrc, null);
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentUnmarshaller#unmarshalObject(com.cordys.coe.bf.content.base.IXmlSource, com.cordys.coe.bf.databind.BindingParameters)
     */
    public IContent unmarshalObject(IXmlSource sSrc, BindingParameters bpParameters) throws BFException
    {
        IBindingContext bcContext = btTemplate.createUnmarshallingContext(sSrc);
        Class<?> cContentClass = btTemplate.getBeanClass(sBeanId);

        try
        {
            IContent cContentObject = null;
            
            if (cContentClass != null) {
                cContentObject = (IContent) cContentClass.newInstance();
                bcContext.setJavaBean(sBeanId, cContentObject);
            }
            
            if (bpParameters != null) {
                bcContext.setParameters(bpParameters);
            }
            
            bcContext.getRootHandler().unmarshallField(bcContext);
            
            return cContentObject;
        }
        catch (Exception e)
        {
            throw new BFException("Exception occurred during data unmarshalling.", e);
        }
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentMarshaller#marshalObject(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IXmlDestination)
     */
    public void marshalObject(IContent cContent, IXmlDestination dDest)
            throws IOException, BFException
    {
        marshalObject(cContent, dDest, null);
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentMarshaller#marshalObject(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IXmlDestination, com.cordys.coe.bf.databind.BindingParameters)
     */
    public void marshalObject(IContent cContent, IXmlDestination dDest, BindingParameters bpParameters) throws IOException, BFException
    {
        IBindingContext bcContext = btTemplate.createMarshallingContext(dDest);
        
        try
        {
            if (bpParameters != null) {
                bcContext.setParameters(bpParameters);
            }
            
            bcContext.setJavaBean(sBeanId, cContent);
            bcContext.getRootHandler().marshallField(bcContext);
        }
        catch (Exception e)
        {
            throw new BFException("Exception occurred during data marshalling.", e);
        }
    }

    public IBindingTemplate getBindingTemplate() {
        return btTemplate;
    }

}
