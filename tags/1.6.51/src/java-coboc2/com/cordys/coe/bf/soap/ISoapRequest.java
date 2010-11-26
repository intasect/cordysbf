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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.exception.SoapFaultException;

/**
 * Interface for a SOAP request.
 *
 * @author mpoyhone
 */
public interface ISoapRequest
{
    /**
     * Sets the context for this request. The request can look up needed parameters
     * from the configuration.
     * @param bfCtx Context object.
     */
    public void setContext(BFContext bfCtx);
    
    /**
     * Sets the timeout for this request. This overrides the default one.
     * @param lTimeout Timeout value.
     */
    public void setTimeout(long lTimeout);
    
    /**
     * Sets the SOAP fault check flag. This is by default <code>true</code>.
     * @param bValue Flag value.
     */
    public void setCheckSoapFault(boolean bValue);
       
    /**
     * Sends a SOAP request containing the given method and returns the SOAP response.
     * @param oeMethod Request method.
     * @return Response method.
     * @throws IOException 
     */
    public OMElement sendSoapRequest(OMElement oeMethod) throws SoapFaultException, IOException;
    
    /**
     * Opens a connection to the server and returns an output stream where the SOAP request can be sent.
     * @return SOAP request output stream.
     */
    public OutputStream openSoapRequest() throws IOException, SoapFaultException;
    
    /**
     * Finishes the SOAP request and reads the response.
     * @return Response as OMEelement. The stream might not yet be fully read.
     * @throws IOException Thrown if the connection failed.
     * @throws SoapFaultException Thrown if the server returned a SOAP fault.
     */
    public OMElement sendSoapRequest()  throws IOException, SoapFaultException;
    
    /**
     * Closes the current SOAP reqeust. This method must be always called when this object
     * is no longer needed.
     */
    public void closeSoapRequest();
}

