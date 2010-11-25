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
package com.cordys.coe.bf.ant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.exception.SoapFaultException;
import com.cordys.coe.bf.objectfactory.SoapRequestFactory;
import com.cordys.coe.bf.soap.ISoapConstants;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * A bridge class for sending SOAP requests throught the ISoapRequestManager
 * interface.
 * 
 * @author mpoyhone
 */
public class SoapRequest_ISoapRequestManager implements ISoapRequest
{
    static Document dSharedDoc = new Document();

    /**
     * Temporary variable used during the SOAP request.
     */
    private ByteArrayOutputStream osHttpRequest = null;

    /**
     * Temporary variable used during the SOAP request.
     */
    private ByteArrayInputStream isHttpResponse = null;

    /**
     * Current SOAP manager.
     */
    private ISoapRequestManager srmSoap;

    /**
     * Temporary variable used during the SOAP request.
     */
    private int iRequestNode = 0;

    /**
     * Temporary variable used during the SOAP request.
     */
    private int iResponseNode = 0;

    /**
     * Flag indicating if SOAP:Faults should be checked.
     */
    private boolean bCheckSoapFault = true;

    /**
     * Constructor for SoapRequest_ISoapRequestManager
     * 
     * @param srmSoap
     *            SOAP manager instance.
     */
    public SoapRequest_ISoapRequestManager(ISoapRequestManager srmSoap)
    {
        this.srmSoap = srmSoap;
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequest#setTimeout(long)
     */
    public void setTimeout(long lTimeout)
    {

    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequest#setCheckSoapFault(boolean)
     */
    public void setCheckSoapFault(boolean bValue)
    {
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequest#sendSoapRequest(org.apache.axiom.om.OMElement)
     */
    public OMElement sendSoapRequest(OMElement oeMethod)
            throws SoapFaultException, IOException
    {
        return null;
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequest#openSoapRequest()
     */
    public OutputStream openSoapRequest() throws IOException,
            SoapFaultException
    {
        osHttpRequest = new ByteArrayOutputStream(4096);
        return osHttpRequest;
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequest#sendSoapRequest()
     */
    public OMElement sendSoapRequest() throws IOException, SoapFaultException
    {
        try
        {
            osHttpRequest.flush();
            iRequestNode = dSharedDoc.load(osHttpRequest.toByteArray());
        }
        catch (XMLException e)
        {
            throw new SoapFaultException("Unable to parse the request XML.", e);
        }
        
        try
        {
            com.cordys.tools.ant.soap.ISoapRequest srRequest = srmSoap.createSoapRequest();
            int[] iaMethodNodes = Find.match(iRequestNode, "<><><>");
            
            if (iaMethodNodes == null || iaMethodNodes.length == 0) {
                throw new SoapFaultException("SOAP request method node not found.");
            }
            
            for (int i = 0; i < iaMethodNodes.length; i++)
            {
                int iNode = iaMethodNodes[i];
            
                srRequest.addMethod(null, iNode);
            }
            
            iResponseNode = srRequest.execute();
            
            Node.delete(iRequestNode);
            iRequestNode = 0;
            
            String sResponse = Node.writeToString(iResponseNode, false);

            Node.delete(iResponseNode);
            iResponseNode = 0;
            
            XMLStreamReader xsrParser = AxiomUtils.xifXmlInputFactory.createXMLStreamReader(new StringReader(sResponse));
            OMXMLParserWrapper omBuilder = OMXMLBuilderFactory.createStAXOMBuilder(OMAbstractFactory.getSOAP11Factory(), xsrParser); 
            OMElement oeEnvelope = omBuilder.getDocumentElement();
            OMElement oeBody = oeEnvelope.getFirstChildWithName(ISoapConstants.SOAP_BODY_ELEMENT);
            
            if (oeBody == null) {
                throw new SoapFaultException("SOAP:Body element is missing.");
            }
            
            oeBody.build();
            xsrParser.close();
            
            if (bCheckSoapFault) {
                OMElement oeFault = oeBody.getFirstChildWithName(ISoapConstants.SOAP_FAULT_ELEMENT);
                
                if (oeFault != null) {
                    throw new SoapFaultException(oeFault);
                }
            }            
            
            return oeBody.getFirstElement();            
        }
        catch (SoapRequestException e)
        {
            throw new SoapFaultException("SOAP request failed.", e);
        }
        catch (XMLStreamException e)
        {
            throw new SoapFaultException("SOAP request failed.", e);
        }
        catch (FactoryConfigurationError e)
        {
            throw new SoapFaultException("SOAP request failed.", e);
        }
        
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequest#closeSoapRequest()
     */
    public void closeSoapRequest()
    {
        if (isHttpResponse != null)
        {
            try
            {
                isHttpResponse.close();
            }
            catch (IOException ignored)
            {
            }
            isHttpResponse = null;
        }

        if (osHttpRequest != null)
        {
            try
            {
                osHttpRequest.close();
            }
            catch (IOException ignored)
            {
            }
            osHttpRequest = null;
        }

        if (iRequestNode != 0)
        {
            Node.delete(Node.getRoot(iRequestNode));
            iRequestNode = 0;
        }
        if (iResponseNode != 0)
        {
            Node.delete(Node.getRoot(iResponseNode));
            iResponseNode = 0;
        }
    }

    /**
     * Factory class.
     *
     * @author mpoyhone
     */
    public static class Factory extends SoapRequestFactory
    {
        private ISoapRequestManager srmSoap;

        /**
         * Constructor for Factory
         * @param srmSoap
         */
        public Factory(ISoapRequestManager srmSoap)
        {
            this.srmSoap = srmSoap;
        }

        /**
         * @see com.cordys.coe.bf.objectfactory.SoapRequestFactory#createInstance()
         */
        @Override
        public ISoapRequest createInstance() throws BFException
        {
            return new SoapRequest_ISoapRequestManager(srmSoap);
        }
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequest#setContext(com.cordys.coe.bf.BFContext)
     */
    public void setContext(BFContext bfCtx)
    {
        // this.bcContext = bfCtx;
    }
}
