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
package com.cordys.coe.bf.content.base.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContentHandlingMethod;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.databind.IBindingHandler;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.objectfactory.SoapRequestFactory;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;
import com.cordys.coe.bf.utils.AxiomUtils;

/**
 * TODO Describe the class.
 *
 * @author mpoyhone
 */
public class ContentSourceBcp extends ContentSourceBase
{
    private Map<String, ISoapRequestTemplate> mMethodTemplateMap = new HashMap<String, ISoapRequestTemplate>();

    /**
     * Constructor for ContentSourceBcp
     * @param aBcContext
     */
    public ContentSourceBcp(BFContext bcContext)
    {
        super(bcContext);
    }
    
    public void registerSoapMethodTemplate(String sTemplateId, String version, ISoapRequestTemplate srtTemplate) throws BFException {
        if (sTemplateId == null) {
            sTemplateId = "";
        }
        
        if (version == null) {
            version = "";
        }
        
        if (mMethodTemplateMap.containsKey(sTemplateId)) {
            throw new BFException("SOAP request template with ID '" + sTemplateId + "' already defined.");
        }  
        
        mMethodTemplateMap.put(sTemplateId + "@" + version, srtTemplate);
    }

    public ISoapRequest createSoapRequest() throws BFException {
        try
        {
            ISoapRequest srRequest = SoapRequestFactory.getFactoryInstance().createInstance();
            
            srRequest.setContext(bcContext);
            
            return srRequest;
        }
        catch (Exception e)
        {
            throw new BFException("Unabel to create the SOAP request.", e);
        }
    }
    
    /**
     * Returns a request template for the given read method.
     * @param sTemplateId Method template ID.
     * @param chmMethod Read or write method that is compatible with this method.
     * @return Method template or <code>null</code> if none was found.
     */
    public ISoapRequestTemplate getSoapRequestTemplate(String sTemplateId, IContentHandlingMethod crmMethod) {
        return getSoapRequestTemplate(sTemplateId, crmMethod.getMethodVersion());
    }    
    
    /**
     * Returns a request template for the given read method.
     * @param sTemplateId Method template ID.
     * @param sMethodVersion Method version.
     * @return Method template or <code>null</code> if none was found.
     */
    public ISoapRequestTemplate getSoapRequestTemplate(String sTemplateId, String sMethodVersion) {
        if (sTemplateId == null) {
            sTemplateId = "";
        }
        
        if (sMethodVersion == null) {
            sMethodVersion = "";
        }    
        
        return mMethodTemplateMap.get(sTemplateId + "@" + sMethodVersion);
    }    

    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#getType()
     */
    public EContentSourceType getType()
    {
        return EContentSourceType.BCP;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#getContentTypeFromXml(com.cordys.coe.bf.content.base.IXmlSource)
     */
    public EContentType getContentTypeFromXml(IXmlSource xsXml) throws BFException
    {
        throw new BFException("getContentTypeFromXml is not implemented.");
    }
    
    public OMElement[] sendSoapRequest(String sRequestTemplateName, 
                                        String sMethodVersion, 
                                        BindingParameters bpParams, 
                                        AXIOMXPath axResXPath) throws BFException
    {
        ISoapRequest srRequest;
        ISoapRequestTemplate srtSoapTemplate;
        
        srtSoapTemplate = getSoapRequestTemplate(sRequestTemplateName, sMethodVersion);
        srRequest = createSoapRequest();
            
        try {
            OutputStream osSoapRequest;
            XMLStreamWriter xswWriter;
            IXmlDestination xdDestination = IXmlDestination.Factory.newInstance();
            
            osSoapRequest = srRequest.openSoapRequest();
            xswWriter = AxiomUtils.xofXmlOuputFactory.createXMLStreamWriter(osSoapRequest);
            xdDestination.set(xswWriter);
            
            IBindingContext bcMarshallContext = srtSoapTemplate.getBindingTemplate().createMarshallingContext(xdDestination);
            IBindingHandler bhMarshallHandler = bcMarshallContext.getRootHandler();
            
            if (bpParams != null) {
                bcMarshallContext.setParameters(bpParams);
            }
            
            bhMarshallHandler.marshallField(bcMarshallContext);
            xswWriter.flush();
            
            OMElement oeResponse = srRequest.sendSoapRequest();
            
            List<?> lNodeList;
            
            try
            {
                lNodeList = axResXPath.selectNodes(oeResponse);
            }
            catch (Exception e)
            {
                throw new BFException("XPath operation failed for SOAP reponse. XPath=" + axResXPath, e);
            }
            
            List<OMElement> lResList = new ArrayList<OMElement>(lNodeList.size());
            
            for (Object oTmp : lNodeList)
            {
                if (oTmp instanceof OMElement) {
                    lResList.add((OMElement) oTmp);
                }
            }
            
            return (OMElement[]) lResList.toArray(new OMElement[lResList.size()]);
        }
        catch (Exception e) {
            throw new BFException("SOAP request failed.", e);
        }
        finally {
            srRequest.closeSoapRequest();
        }     
    }
}
