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
package com.cordys.coe.bf.soap;

import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.exception.SoapFaultException;
import com.cordys.coe.bf.utils.AxiomUtils;

/**
 * SOAP helper methods. These probably should be in the ISoapRequest interface.
 *
 * @author mpoyhone
 */
public class SoapUtils
{
    /**
     * Adds a SOAP method to the started SOAP request. The template must contain the method element.
     * @param bcMarshallingContext Context that contains the method template.
     * @param xdDest Contains the destination where the XML will be written to.
     * @throws SoapFaultException Thrown if the operation failed.
     */
    public static void addSoapRequestMethod(IBindingContext bcMarshallingContext, IXmlDestination xdDest) throws SoapFaultException {
        addSoapRequestMethod(null, bcMarshallingContext, xdDest);
    }
    
    /**
     * Adds a SOAP method to the started SOAP request. This version writes the method element.
     * @param bcMarshallingContext Context that contains the method template.
     * @param xdDest Contains the destination where the XML will be written to.
     * @param qnMethodName Method name to be used.
     * @throws SoapFaultException Thrown if the operation failed.
     */
    public static void addSoapRequestMethod(QName qnMethodName, IBindingContext bcMarshallingContext, IXmlDestination xdDest) throws SoapFaultException {
        XMLStreamWriter xswWriter = xdDest.getStreamWriter();
        QName oldNamespace = null;
        
        if (xswWriter == null) {
            throw new SoapFaultException("XMLStreamWriter is not set.");
        }
        
        try
        {
            if (qnMethodName != null) {
                xswWriter.setDefaultNamespace(qnMethodName.getNamespaceURI());
                xswWriter.writeStartElement(qnMethodName.getNamespaceURI(), qnMethodName.getLocalPart());
                xswWriter.writeNamespace("", qnMethodName.getNamespaceURI());
                oldNamespace = bcMarshallingContext.setCurrentNamespace(qnMethodName);
            }
            
            bcMarshallingContext.getRootHandler().marshallField(bcMarshallingContext);
            
            if (qnMethodName != null) {
                xswWriter.writeEndElement(); // Method
            }            
        }
        catch (BindingException e)
        {
            throw new SoapFaultException("SOAP request data binding failed.", e);
        }
        catch (XMLStreamException e)
        {
            throw new SoapFaultException("SOAP request writing failed.", e);
        }
        finally {
            if (oldNamespace != null) {
                bcMarshallingContext.setCurrentNamespace(oldNamespace);
            }
        }
    }
    
    /**
     * Start creates the SOAP envelope, body and optionally the method element and returns
     * the XML destination object where the rest of the request contents can be written to.
     * @param osSoapRequest SOAP request ouput stream. 
     * @param qnMethodName Optional method element name. If <code>null</code> the method element is not written.
     * @return XML destination object.
     * @throws SoapFaultException Thrown if the 
     */
    public static IXmlDestination startSoapRequestBody(OutputStream osSoapRequest, QName qnMethodName) throws SoapFaultException {
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
            
            if (qnMethodName != null) {
                xswWriter.setDefaultNamespace(qnMethodName.getNamespaceURI());
            }
            
            xswWriter.writeStartDocument();
            xswWriter.writeStartElement(sSoapPrefix, ISoapConstants.SOAP_ENVELOPE_ELEMENT.getLocalPart(), sSoapNamespace);
            xswWriter.writeNamespace(sSoapPrefix, sSoapNamespace);
            xswWriter.writeStartElement(sSoapPrefix, ISoapConstants.SOAP_BODY_ELEMENT.getLocalPart(), sSoapNamespace);
            
            if (qnMethodName != null) {
                xswWriter.writeStartElement(qnMethodName.getNamespaceURI(), qnMethodName.getLocalPart());
                xswWriter.writeNamespace("", qnMethodName.getNamespaceURI());
            }
        }
        catch (Exception e)
        {
            throw new SoapFaultException("Unable to send the update SOAP request.", e);
        }
        
        return IXmlDestination.Factory.newInstance(xswWriter);
    }

    /**
     * Closes creates the SOAP elements created by the startSoapRequest mehtod. 
     * @param xdDest XML destination object returned by the startSoapRequest mehtod.
     * @param bEndMethodElement If <code>true</code> the request method element is also close.
     * @throws SoapFaultException Thrown if the operation failed.
     */
    public static void endSoapRequest(IXmlDestination xdDest, boolean bEndMethodElement) throws SoapFaultException {
        XMLStreamWriter xswWriter = xdDest.getStreamWriter();
        
        if (xswWriter == null) {
            throw new SoapFaultException("XMLStreamWriter is not set.");
        }
        
        try
        {  
            if (bEndMethodElement) {
                xswWriter.writeEndElement(); // Method
            }
            
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
}
