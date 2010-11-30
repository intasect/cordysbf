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

import javax.xml.namespace.QName;


/**
 * SOAP specific constants.
 *
 * @author mpoyhone
 */
public interface ISoapConstants
{
    /**
     * Defines the SOAP namespace.
     */
    String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
    
    /**
     * Defines the name of the SOAP:Envelope.
     */
    QName SOAP_ENVELOPE_ELEMENT = new QName(SOAP_NAMESPACE, "Envelope", "SOAP");

    /**
     * Defines the name of the SOAP:Body.
     */
    QName SOAP_BODY_ELEMENT = new QName(SOAP_NAMESPACE, "Body", "SOAP");
    
    /**
     * Defines the name of the SOAP:Fault.
     */
    QName SOAP_FAULT_ELEMENT = new QName(SOAP_NAMESPACE, "Fault", "SOAP");    
    
    /**
     * Defines the name of the SOAP faultcode.
     */
    QName SOAP_FAULT_FAULTCODE_ELEMENT = new QName("faultcode");    
    /**
     * Defines the name of the SOAP faultstring.
     */
    QName SOAP_FAULT_FAULTSTRING_ELEMENT = new QName("faultstring");  
    /**
     * Defines the name of the SOAP detail.
     */
    QName SOAP_FAULT_DETAIL_ELEMENT = new QName("detail");    

}
