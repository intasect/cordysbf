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

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.xmlstore.XmlStoreContentHandle;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.databind.IBindingConstants;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.databind.IBindingHandler;
import com.cordys.coe.bf.databind.IBindingTemplate;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.exception.SoapFaultException;
import com.cordys.coe.bf.soap.ISoapConstants;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;
import com.cordys.coe.bf.utils.AxiomUtils;

/**
 * Base class for all BCP write methods.
 *
 * @author mpoyhone
 */
public abstract class BcpWriteMethodBase extends WriteMethodBase
{
    protected BFContext bcContext;
    protected ContentSourceBcp csSource;
    
    public BcpWriteMethodBase(BFContext bcContext, ContentSourceBcp csSource) {
        this.bcContext = bcContext;
        this.csSource = csSource; 
        this.sMethodVersion = bcContext.getConfig().getVersionInfo().getMajorVersion().getCobocString();
    }
    
    protected boolean checkFiltering(IContent cWriteObject) {
        IContentHandle chHandle = cWriteObject.getHandle();
        String sKey;
        
        if (chHandle instanceof CobocContentHandle) {
            sKey = ((CobocContentHandle) chHandle).getKey();
        } else if (chHandle instanceof XmlStoreContentHandle) {
            sKey = ((XmlStoreContentHandle) chHandle).getKey();
        } else {
            sKey = chHandle.getContentId();
        }
        
        boolean bIsFolder = chHandle.getContentType() == EContentType.COBOC_FOLDERS_FOLDER;

        if (! bUseContentFiltering) {
            return true;
        }
            
        return csSource.checkForAccess(cWriteObject.getType(), sKey, bIsFolder);
    }
    
    protected IContent sendUpdateRequest(ISoapRequest srRequest, ISoapRequestTemplate srtSoapTemplate, ContentTuple ctTuple, BindingParameters bpParams) throws BFException {
        try {
            OutputStream osSoapRequest;
            
            osSoapRequest = srRequest.openSoapRequest();
            marshallUpdateRequestContent(osSoapRequest, srtSoapTemplate, ctTuple, bpParams);
            
            OMElement oeResponse = srRequest.sendSoapRequest();
            
            return handleUpdateResponse(oeResponse, ctTuple.getWriteObject());
        }
        catch (Exception e) {
            throw new BFException("Update SOAP request failed.", e);
        }
        finally {
            srRequest.closeSoapRequest();
        }
    }
    
    protected void marshallUpdateRequestContent(OutputStream osSoapRequest, ISoapRequestTemplate srtSoapTemplate, ContentTuple ctTuple, BindingParameters bpParams) throws SoapFaultException {
        XMLStreamWriter xswWriter;
        
        try
        {
            xswWriter = AxiomUtils.xofXmlOuputFactory.createXMLStreamWriter(osSoapRequest);
        }
        catch (XMLStreamException e)
        {
            throw new SoapFaultException("Exception while creating the XML output writer.", e);
        }
        catch (FactoryConfigurationError e)
        {
            throw new SoapFaultException("Unable to create the XML output writer.", e);
        }
        
        try
        {
            IXmlDestination xdDest = IXmlDestination.Factory.newInstance(xswWriter);  
            IBindingTemplate btBindingTemplate = srtSoapTemplate.getBindingTemplate();
            IBindingContext bcMarshallContext = btBindingTemplate.createMarshallingContext(xdDest);
            IBindingHandler bhMarshallRootHandler = btBindingTemplate.getRootHandler();
            
            bpParams.setBcpUpdateModeFromTuple(ctTuple);
            bcMarshallContext.setParameters(bpParams);
            
            if (ctTuple.getOld() != null) {
                bcMarshallContext.setJavaBean(IBindingConstants.BEANNAME_OLD, ctTuple.getOld());
            }
            
            if (ctTuple.getNew() != null) {
                bcMarshallContext.setJavaBean(IBindingConstants.BEANNAME_NEW, ctTuple.getNew());
            }
            
            // Marshall the SOAP request and the bean(s) into XML.
            xswWriter.writeStartDocument();
            bhMarshallRootHandler.marshallField(bcMarshallContext);
            xswWriter.writeEndDocument();
            xswWriter.flush();
        }
        catch (Exception e)
        {
            throw new SoapFaultException("Unable to send the update SOAP request.", e);
        }
    }    
    
    protected IContent sendUpdateRequest(ISoapRequest srRequest, QName qnMethodName, ContentTuple ctTuple, IContentMarshaller cmMarshaller) throws BFException {
        try {
            OutputStream osSoapRequest;
            
            osSoapRequest = srRequest.openSoapRequest();
            marshallUpdateRequestContent(osSoapRequest, qnMethodName, ctTuple, cmMarshaller);
            
            OMElement oeResponse = srRequest.sendSoapRequest();
            
            return handleUpdateResponse(oeResponse, ctTuple.getWriteObject());
        }
        catch (Exception e) {
            throw new BFException("Update SOAP request failed.", e);
        }
        finally {
            srRequest.closeSoapRequest();
        }
    }
    
    protected void marshallUpdateRequestContent(OutputStream osSoapRequest, QName qnMethodName, ContentTuple ctTuple, IContentMarshaller cmMarshaller) throws SoapFaultException {
        XMLStreamWriter xswWriter;
        
        try
        {
            xswWriter = AxiomUtils.xofXmlOuputFactory.createXMLStreamWriter(osSoapRequest);
        }
        catch (XMLStreamException e)
        {
            throw new SoapFaultException("Exception while creating the XML output writer.", e);
        }
        catch (FactoryConfigurationError e)
        {
            throw new SoapFaultException("Unable to create the XML output writer.", e);
        }
        
        try
        {
            final String sSoapPrefix = ISoapConstants.SOAP_ENVELOPE_ELEMENT.getPrefix();
            final String sSoapNamespace = ISoapConstants.SOAP_ENVELOPE_ELEMENT.getNamespaceURI();
            
            xswWriter.setDefaultNamespace(qnMethodName.getNamespaceURI());
            xswWriter.writeStartDocument();
            xswWriter.writeStartElement(sSoapPrefix, ISoapConstants.SOAP_ENVELOPE_ELEMENT.getLocalPart(), sSoapNamespace);
            xswWriter.writeNamespace(sSoapPrefix, sSoapNamespace);
            xswWriter.writeStartElement(sSoapPrefix, ISoapConstants.SOAP_BODY_ELEMENT.getLocalPart(), sSoapNamespace);
            xswWriter.writeStartElement(qnMethodName.getNamespaceURI(), qnMethodName.getLocalPart());
            xswWriter.writeNamespace("", qnMethodName.getNamespaceURI());
            
            IXmlDestination xdDest = IXmlDestination.Factory.newInstance(xswWriter);  
            BindingParameters bpParams = new BindingParameters();
            
            bpParams.setBcpUpdateModeFromTuple(ctTuple);
            
            xswWriter.writeStartElement("tuple");
            
            if (ctTuple.getOld() != null) {
                xswWriter.writeStartElement("old");
                cmMarshaller.marshalObject(ctTuple.getOld(), xdDest, bpParams);
                xswWriter.writeEndElement(); // old
            }
            
            if (ctTuple.getNew() != null) {
                xswWriter.writeStartElement("new");
                cmMarshaller.marshalObject(ctTuple.getNew(), xdDest, bpParams);
                xswWriter.writeEndElement(); // new
            }
            
            xswWriter.writeEndElement(); // tuple
            
            xswWriter.writeEndElement(); // Update method
            xswWriter.writeEndElement(); // Body
            xswWriter.writeEndElement(); // Envelope
            xswWriter.writeEndDocument();
            xswWriter.flush();
        }
        catch (Exception e)
        {
            throw new SoapFaultException("Unable to send the update SOAP request.", e);
        }
    }
    
    protected IContent sendSimpleUpdateRequest(ISoapRequest srRequest, QName qnMethodName, IContent cObjectObject, IContentMarshaller cmMarshaller) throws BFException {
        try {
            OutputStream osSoapRequest;
            
            osSoapRequest = srRequest.openSoapRequest();
            marshallSimpleUpdateRequestContent(osSoapRequest, qnMethodName, cObjectObject, cmMarshaller);
            
            OMElement oeResponse = srRequest.sendSoapRequest();
            
            return handleUpdateResponse(oeResponse, cObjectObject);
        }
        catch (Exception e) {
            throw new BFException("Update SOAP request failed.", e);
        }
        finally {
            srRequest.closeSoapRequest();
        }
    }
    
    protected void marshallSimpleUpdateRequestContent(OutputStream osSoapRequest, QName qnMethodName, IContent cUbjectObject, IContentMarshaller cmMarshaller) throws SoapFaultException {
        XMLStreamWriter xswWriter;
        
        try
        {
            xswWriter = AxiomUtils.xofXmlOuputFactory.createXMLStreamWriter(osSoapRequest);
        }
        catch (XMLStreamException e)
        {
            throw new SoapFaultException("Exception while creating the XML output writer.", e);
        }
        catch (FactoryConfigurationError e)
        {
            throw new SoapFaultException("Unable to create the XML output writer.", e);
        }
        
        try
        {
            final String sSoapPrefix = ISoapConstants.SOAP_ENVELOPE_ELEMENT.getPrefix();
            final String sSoapNamespace = ISoapConstants.SOAP_ENVELOPE_ELEMENT.getNamespaceURI();
            
            xswWriter.setDefaultNamespace(qnMethodName.getNamespaceURI());
            xswWriter.writeStartDocument();
            xswWriter.writeStartElement(sSoapPrefix, ISoapConstants.SOAP_ENVELOPE_ELEMENT.getLocalPart(), sSoapNamespace);
            xswWriter.writeNamespace(sSoapPrefix, sSoapNamespace);
            xswWriter.writeStartElement(sSoapPrefix, ISoapConstants.SOAP_BODY_ELEMENT.getLocalPart(), sSoapNamespace);
            xswWriter.writeStartElement(qnMethodName.getNamespaceURI(), qnMethodName.getLocalPart());
            xswWriter.writeNamespace("", qnMethodName.getNamespaceURI());
            
            IXmlDestination xdDest = IXmlDestination.Factory.newInstance(xswWriter);  

            cmMarshaller.marshalObject(cUbjectObject, xdDest);
            
            xswWriter.writeEndElement(); // Update method
            xswWriter.writeEndElement(); // Body
            xswWriter.writeEndElement(); // Envelope
            xswWriter.writeEndDocument();
            xswWriter.flush();
        }
        catch (Exception e)
        {
            throw new SoapFaultException("Unable to send the update SOAP request.", e);
        }
    }
    
    protected IContent handleUpdateResponse(OMElement oeResponse, IContent cUpdateObject) throws BFException {
        // This does not check the response.
        return null;
    }
}
