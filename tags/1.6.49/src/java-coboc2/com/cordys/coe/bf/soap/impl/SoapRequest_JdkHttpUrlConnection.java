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
package com.cordys.coe.bf.soap.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.config.SoapConfig;
import com.cordys.coe.bf.exception.SoapFaultException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.io.SplitInputStream;
import com.cordys.coe.util.io.SplitOutputStream;
import com.cordys.coe.util.log.LogInterface;

/**
 * Handles sending of SOAP request through the JDK HttpURLConnection class.
 *
 * @author mpoyhone
 */
public class SoapRequest_JdkHttpUrlConnection implements ISoapRequest
{
    /**
     * Request timeout value;
     */
    private long lTimeout = 0;
    /**
     * Flag indicating if SOAP:Faults should be checked.
     */
    private boolean bCheckSoapFault = true;
    /**
     * Soap action header for this request.
     */
    private String sSoapAction = "";
    /**
     * Server URL to which the SOAP request will be sent to.
     */
    private URL uServerUrl;
    /**
     * Logger object for this connection.
     */
    private LogInterface liLogger;
    /** 
     * Contains valid SOAP content types.
     */
    private static final Set<String> mSoapContentTypes = new HashSet<String>(Arrays.asList(new String[] { "text/xml" }));
    
    /**
     * Temporary variable used during the SOAP request.
     */
    private HttpURLConnection hucConnection = null;
    /**
     * Temporary variable used during the SOAP request.
     */
    private OutputStream osHttpRequest = null;
    /**
     * Temporary variable used during the SOAP request.
     */
    private InputStream isHttpResponse = null;
    /**
     * Temporary variable used during the SOAP request.
     */
    private ByteArrayOutputStream basDebugRequestOutput = null;
    /**
     * Temporary variable used during the SOAP request.
     */
    private ByteArrayOutputStream basDebugResponseOutput = null;
    /**
     * Current context.
     */
    private BFContext bcContext;    
    /**
     * If <code>true</code> SOAP messages are recorded for debug messages. The value
     * of this flag is determined from the logger and SOAP config object.
     */
    private boolean bGatherDebug;
    
    /**
     * Constructor for SoapRequest_JdkHttpUrlConnection
     * @param uUrl Server URL.
     */
    public SoapRequest_JdkHttpUrlConnection() {
    }
    
    /**
     * @see com.cordys.coe.bf.soap.ISoapRequest#setContext(com.cordys.coe.bf.BFContext)
     */
    public void setContext(BFContext bfCtx)
    {
        this.bcContext = bfCtx;
    }
    
    /**
     * @see com.cordys.coe.bf.soap.ISoapRequest#setTimeout(long)
     */
    public void setTimeout(long lTimeout)
    {
        this.lTimeout = lTimeout;
    }
    
    /**
     * This method gets the timout.
     * 
     * @return The timout.
     */
    public long getTimeout()
    {
        return lTimeout;
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequest#setCheckSoapFault(boolean)
     */
    public void setCheckSoapFault(boolean bValue)
    {
        this.bCheckSoapFault = bValue;
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequest#sendSoapRequest(org.apache.axis2.om.OMElement)
     */
    public OMElement sendSoapRequest(final OMElement oeMethod)
            throws IOException, SoapFaultException
    {
        try {
            OutputStream osRequest = openSoapRequest();
            
            try
            {
                oeMethod.serialize(osRequest);
                osRequest.flush();
            }
            catch (XMLStreamException e)
            {
                throw new SoapFaultException("Unable to write the SOAP request to the output stream", e);
            }
            
            OMElement oeResponse = sendSoapRequest();
            
            oeResponse.build();
            
            return oeResponse;
        }
        finally {
            closeSoapRequest();
        }
    }

    public OutputStream openSoapRequest()
            throws IOException, SoapFaultException {
        if (bcContext == null) {
            throw new SoapFaultException("Context is not set for the SOAP request object.");
        }
        
        SoapConfig scConfig = bcContext.getConfig().getSoapConfig();
        
        liLogger = bcContext.getLogger();
        uServerUrl = scConfig.getWebGatewayUrl();
        bGatherDebug = liLogger.isDebugEnabled() && scConfig.isSoapDebugEnabled();
        
        hucConnection = null;
        osHttpRequest = null;
        isHttpResponse = null;
        basDebugRequestOutput = null;
        basDebugResponseOutput = null;        
        
        hucConnection = (HttpURLConnection) uServerUrl.openConnection();
        hucConnection.setRequestMethod("POST");
        hucConnection.addRequestProperty("SOAPAction", sSoapAction != null ? sSoapAction : ""); 
        hucConnection.addRequestProperty("Content-Type", "text/xml; charset=utf-8");
        hucConnection.setDoInput(true);
        hucConnection.setDoOutput(true);
        
        hucConnection.connect();
        
        osHttpRequest = hucConnection.getOutputStream();
        
        if (bGatherDebug) {
            basDebugRequestOutput = new ByteArrayOutputStream(1024);
            osHttpRequest = new SplitOutputStream(new OutputStream[] { osHttpRequest, basDebugRequestOutput });
        }   
        
        return osHttpRequest;
    }
    
    public OMElement sendSoapRequest()  throws IOException, SoapFaultException {
        try {
            osHttpRequest.flush();
            
            if (bGatherDebug) {
                basDebugRequestOutput.close();
                liLogger.debug("Sent SOAP request to URL " + uServerUrl);
                liLogger.debug(AxiomUtils.prettyPrintXml(new String(basDebugRequestOutput.toByteArray(), "UTF-8")));
                basDebugRequestOutput = null;
            }            
            
            isHttpResponse = hucConnection.getInputStream();
            
            if (bGatherDebug) {
                basDebugResponseOutput = new ByteArrayOutputStream(1024);
                isHttpResponse = new SplitInputStream(isHttpResponse, basDebugResponseOutput);
            }
            
            int iStatus = hucConnection.getResponseCode();

            if (iStatus != HttpURLConnection.HTTP_OK) {
                throw new SoapFaultException("Server returned an error '" + iStatus + 
                        "'. Status: " + hucConnection.getResponseMessage().toString());
            }
            
            String sContentType = hucConnection.getHeaderField("Content-Type");
            
            if (! mSoapContentTypes.contains(sContentType)) {
                throw new SoapFaultException("Server returned an invalid content type '" + sContentType);
            }
            
            XMLStreamReader xsrParser = AxiomUtils.xifXmlInputFactory.createXMLStreamReader(isHttpResponse);
            OMXMLParserWrapper omBuilder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMAbstractFactory.getSOAP11Factory(), xsrParser); 
            SOAPEnvelope oeEnvelope = (SOAPEnvelope) omBuilder.getDocumentElement();
            
            if (bGatherDebug) {
                oeEnvelope.build();
                basDebugResponseOutput.close();
                liLogger.debug("Received SOAP response:");
                liLogger.debug(AxiomUtils.prettyPrintXml(new String(basDebugResponseOutput.toByteArray(), "UTF-8")));
                basDebugResponseOutput = null;
            } 
            
            SOAPBody oeBody = oeEnvelope.getBody();
            
            if (bCheckSoapFault) {
                SOAPFault oeFault = oeBody.getFault();
                
                if (oeFault != null) {
                    throw new SoapFaultException(oeFault);
                }
            }
            
            return oeBody.getFirstElement();
        }
        catch (XMLStreamException e) {
            throw new SoapFaultException("Unable to parse the response XML.", e);
        }
    }
    
    public void closeSoapRequest() {
        if (isHttpResponse != null) {
            try
            {
                isHttpResponse.close();
            }
            catch (IOException ignored)
            {
            }
            isHttpResponse = null;
        }
        
        if (osHttpRequest != null) {
            try
            {
                osHttpRequest.close();
            }
            catch (IOException ignored)
            {
            }
            osHttpRequest = null;
        }
        
        if (hucConnection != null) {
            hucConnection.disconnect();
            hucConnection = null;
        }
    }    
}
