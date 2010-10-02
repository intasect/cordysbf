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
package com.cordys.coe.bf.exception;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFault;
import org.w3c.dom.Node;

import com.cordys.coe.bf.soap.ISoapConstants;
import com.cordys.coe.bf.utils.DOMUtils;

/**
 * Exception class for SOAP faults. 
 *
 * @author mpoyhone
 */
public class SoapFaultException extends Exception {
    private String sFaultCode;
    private String sFaultString;
    private String sDetail;
    private String sMessage;
    
	/**
	 * Constructs a new SOAPException object.
	 */
	public SoapFaultException() {
		super();
	}
	
	/**
	 * Constructs a new SOAPException object.
	 * @param nNode XML node containing the SOAP fault. 
	 */
	public SoapFaultException(Node nFaultNode) {
        initialize(nFaultNode);
	}
    
    /**
     * Constructs a new SOAPException object.
     * @param nNode Axiom node containing the SOAP fault. 
     */
    public SoapFaultException(SOAPFault oeFaultNode) {
        initialize(oeFaultNode);
    }    
    
    /**
     * Constructs a new SOAPException object.
     * @param nNode Axiom node containing the SOAP fault. 
     */
    public SoapFaultException(OMElement oeFaultNode) {
        initialize(oeFaultNode);
    }        
	
	/**
	 * Constructs a new SOAPException object.
	 * 
	 * @param sMessage Exception message.
	 */
	public SoapFaultException(String sMessage) {
		this.sMessage = sMessage;
	}	
	
	/**
	 * Constructs a new SOAPException object.
	 * 
	 * @param sMessage Exception message.
	 * @param tCause Causing exception.
	 */
	public SoapFaultException(String sMessage, Throwable tCause) {
		super(tCause);
        this.sMessage = sMessage;
	}		
	
	/**
	 * Constructs a new SOAPException object.
	 * 
	 * @param sMessage Exception message.
	 * @param tCause Causing exception.
	 */
	public SoapFaultException(Throwable tCause) {
		super(tCause);
	}		
    
	/**
     * @see java.lang.Throwable#getMessage()
     */
    public String getMessage()
    {
        return sMessage;
    }

    /**
	 * Fetches the exception message details from the SOAP fault XML.
	 * 
	 * @param nFaultNode SOAP fault node.
	 * @return Exception message.
	 */
	
	private void initialize(Node nFaultNode) {
        sFaultCode = DOMUtils.getNodeText(DOMUtils.findChildNode(nFaultNode, "faultcode"), "");
        sFaultString = DOMUtils.getNodeText(DOMUtils.findChildNode(nFaultNode, "faultstring"), "");
        sDetail = DOMUtils.getNodeText(DOMUtils.findChildNode(nFaultNode, "detail"), "");

        formatMessage();
	}
    
    private void initialize(OMElement oeFaultNode) {
        OMElement oeElem;
        
        if ((oeElem = oeFaultNode.getFirstChildWithName(ISoapConstants.SOAP_FAULT_FAULTCODE_ELEMENT)) != null) {
            sFaultCode = oeElem.getText();
        }
            
        if ((oeElem = oeFaultNode.getFirstChildWithName(ISoapConstants.SOAP_FAULT_FAULTSTRING_ELEMENT)) != null) {
            sFaultString = oeElem.getText();
        }
        
        if ((oeElem = oeFaultNode.getFirstChildWithName(ISoapConstants.SOAP_FAULT_DETAIL_ELEMENT)) != null) {
            sDetail = oeElem.getText();
        }      

        formatMessage();
    }

    private void formatMessage() {
        StringBuffer sMsg = new StringBuffer(256);
        
        sMsg.append("SOAP:Fault[faultcode='");
        if (sFaultCode != null) {
            sMsg.append(sFaultCode);
        }
        sMsg.append("',faultstring='");
        if (sFaultString != null) {
            sMsg.append(sFaultString);
        }
        sMsg.append("',detail='");
        if (sDetail != null) {
            sMsg.append(sDetail);
        }        
        sMsg.append("']");
        
        sMessage = sMsg.toString();
    }    

    /**
     * Returns the detail.
     *
     * @return Returns the detail.
     */
    public String getDetail()
    {
        return sDetail;
    }

    /**
     * The detail to set.
     *
     * @param aDetail The detail to set.
     */
    public void setDetail(String aDetail)
    {
        sDetail = aDetail;
    }

    /**
     * Returns the faultCode.
     *
     * @return Returns the faultCode.
     */
    public String getFaultCode()
    {
        return sFaultCode;
    }

    /**
     * The faultCode to set.
     *
     * @param aFaultCode The faultCode to set.
     */
    public void setFaultCode(String aFaultCode)
    {
        sFaultCode = aFaultCode;
    }

    /**
     * Returns the faultString.
     *
     * @return Returns the faultString.
     */
    public String getFaultString()
    {
        return sFaultString;
    }

    /**
     * The faultString to set.
     *
     * @param aFaultString The faultString to set.
     */
    public void setFaultString(String aFaultString)
    {
        sFaultString = aFaultString;
    }       
}
