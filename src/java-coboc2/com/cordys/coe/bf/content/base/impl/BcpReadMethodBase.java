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
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentReadMethod;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.folders.Folder;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.databind.IBindingConstants;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.databind.IBindingHandler;
import com.cordys.coe.bf.databind.IBindingTemplate;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.sun.org.apache.bcel.internal.generic.NEW;

/**
 * Base class for all BCP read methods.
 *
 * @author mpoyhone
 */
public abstract class BcpReadMethodBase implements IContentReadMethod {
    /**
     * Contains the method version ID.
     */
    String sMethodVersion;    
    /**
     * Context to that this method belongs to 
     */
    protected BFContext bcContext;
    /**
     * BCP content source object.
     */
    protected ContentSourceBcp csSource;
    /**
     * Determines if content filtering is used by this method.
     */
    protected boolean bUseContentFiltering = true;
    /**
     * Version parameter for content sources that support
     * more versions (e.g. XMLStore).
     */
    protected String sContentVersion;
    /**
     * Constant for an object which is filtered out.
     */
    protected static final IContent FILTERED_CONTENT = new Folder();
    /**
     * Pattern: service\s+is\s+unavailable
     * Used to detect the SOAP:Fault when the SOAP processor is not running.
     */
    private static final Pattern pSoapProcessorNotRunningPattern =
        Pattern.compile("service\\s+is\\s+unavailable|could\\s+not\\s+find\\s+a\\s+soap\\s+node\\s+implementing", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    /**
     * Constructor for BcpReadMethodBase
     * @param bcContext Context.
     * @param csSource BCP content source.
     */
    public BcpReadMethodBase(BFContext bcContext, ContentSourceBcp csSource) {
        this.bcContext = bcContext;
        this.csSource = csSource; 
        this.sMethodVersion = bcContext.getConfig().getVersionInfo().getMajorVersion().getCobocString();
    }
    
    /**
     * Sends a SOAP request that is defined by the given template and parameters and returns
     * the objects unmarshalled from the response. The unmarshalling is done by calling the
     * handleSoapResponse method.
     * 
     * @param srRequest SOAP request object.
     * @param btRequestTemplate SOAP request template.
     * @param responseType TODO
     * @param mRequestParameters SOAP request template parameters.
     * @return A list of unmarshalled objects.
     * @throws BFException Thrown if the operation failed.
     */
    protected List<IContent> sendSoapRequest(ISoapRequest srRequest, IBindingTemplate btRequestTemplate, BindingParameters bpRequestParameters, EContentType responseType) throws BFException
    {
        try {
            OutputStream osSoapRequest;
            XMLStreamWriter xswWriter;
            IXmlDestination xdDestination = IXmlDestination.Factory.newInstance();
            
            osSoapRequest = srRequest.openSoapRequest();
            xswWriter = AxiomUtils.xofXmlOuputFactory.createXMLStreamWriter(osSoapRequest);
            xdDestination.set(xswWriter);
            
            IBindingContext bcMarshallContext = btRequestTemplate.createMarshallingContext(xdDestination);
            IBindingHandler bhMarshallHandler = bcMarshallContext.getRootHandler();
            
            if (bpRequestParameters != null) {
                bcMarshallContext.setParameters(bpRequestParameters);
            }
            
            bhMarshallHandler.marshallField(bcMarshallContext);
            xswWriter.flush();
            
            OMElement oeResponse = srRequest.sendSoapRequest();
            
            return handleSoapResponse(oeResponse, responseType);
        }
        catch (Exception e) {
            throw new BFException("SOAP request failed.", e);
        }
        finally {
            srRequest.closeSoapRequest();
        }
    }

    /**
     * Called by the sendSoapRequest method to parse the SOAP response.
     * 
     * @param oeResponse First response method element.
     * @param objectType Response object type.
     * @return A list of unmarshalled content objects or <code>null</code> if none is provided. 
     */
    @SuppressWarnings("unchecked")
    protected List<IContent> handleSoapResponse(OMElement oeResponse, EContentType objectType) throws BFException {
        QName qnTmp = oeResponse.getQName();
        OMElement oeTuple = oeResponse.getFirstChildWithName(AxiomUtils.createQName("tuple", qnTmp.getPrefix(), qnTmp.getNamespaceURI()));
        
        if (oeTuple == null) {
            return Collections.emptyList();
        }
        
        IXmlSource xsUnmarshallSource = IXmlSource.Factory.newInstance();
        List<IContent> lRes = new ArrayList<IContent>(20);
        IContentUnmarshaller cuTupleUnmarshaller = csSource.getContentUnmarshaller(objectType, null, getMethodVersion());
        
        if (cuTupleUnmarshaller == null) {
            if (objectType != null) {
                throw new BFException("No content unmarshaller configured for " + objectType.getLogName(true));
            } else {
                throw new BFException("No content unmarshaller configured.");
            }
        }
        
        try
        {
            for (; oeTuple != null; oeTuple = AxiomUtils.getNextSiblingElement(oeTuple)) {
                BindingParameters bpParams = new BindingParameters();
                IContent cResObject;
                
                xsUnmarshallSource.set(oeTuple);
                cResObject = cuTupleUnmarshaller.unmarshalObject(xsUnmarshallSource, bpParams);
                
                if (cResObject != null) {
                    lRes.add(cResObject);
                }
            }
        }
        catch (Exception e)
        {
            throw new BFException("SOAP response parsing failed.", e);
        }
        
        return lRes;
    }  
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandlingMethod#getMethodVersion()
     */
    public String getMethodVersion()
    {
        return sMethodVersion;
    }      

    /**
     * Unmarshalls the response XML into a content object using the given template.
     * 
     * @param sSrc XML source.
     * @param cContentObject Content object that will receive the data.
     * @param btTemplate Unmarshalling template.
     * @throws BFException Thrown if the operation failed.
     */
    protected void processResponseTemplate(IXmlSource sSrc, IContent cContentObject, IBindingTemplate btTemplate) throws BFException
    {
        IBindingContext bcContext = btTemplate.createUnmarshallingContext(sSrc);
        
        try
        {
            bcContext.setJavaBean(IBindingConstants.BEANNAME_DEFAULT, cContentObject);
            bcContext.getRootHandler().unmarshallField(bcContext);
        }
        catch (Exception e)
        {
            throw new BFException("Exception occurred during data marshalling.", e);
        }
    }
    

    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#setContentVersion(java.lang.String)
     */
    public void setContentVersion(String sVersion)
    {
        sContentVersion = sVersion;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandlingMethod#getContentFilterStatus()
     */
    public boolean getContentFilterStatus()
    {
        return bUseContentFiltering;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandlingMethod#setContentFilterStatus(boolean)
     */
    public void setContentFilterStatus(boolean bOn)
    {
        bUseContentFiltering = bOn;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjectsFromXml(com.cordys.coe.bf.content.base.IXmlSource)
     */
    public List<IContent> readObjectsFromXml(IXmlSource xsSource) throws BFException
    {
        throw new BFException("readObjectFromXML is not supported for BCP methods.");
    }
    
    /**
     * Tries to find the SOAP fault exception for non available SOAP processor.
     * @param e Exception received when sending a SOAP message.
     * @return SOAP:Fault exception or <code>null</code> if none was found.
     */
    protected Throwable findSoapProcessorNotRunningException(Throwable e)
    {
        // Try to check the SOAP:Fault
        Throwable tCause = e;
        
        while (tCause != null) {
            String sMessage = tCause.getMessage();
            
            if (sMessage != null) {
                if (pSoapProcessorNotRunningPattern.matcher(sMessage).find()) {
                    return tCause;
                }
            }
            
            tCause = tCause.getCause();
        }    
        
        return null;
    }
}
