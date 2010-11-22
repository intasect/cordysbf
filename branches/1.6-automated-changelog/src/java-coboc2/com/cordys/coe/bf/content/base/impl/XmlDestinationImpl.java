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

import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.content.base.IXmlDestination;

public class XmlDestinationImpl implements IXmlDestination {
    private OutputStream osOutputStream;
    private XMLStreamWriter xswStreamWriter;
    private OMElement oeAxiomElement;
    private int iNomElement;
    
    public XmlDestinationImpl() {
    }    
    
    public XmlDestinationImpl(OutputStream osOutput) {
        this.osOutputStream = osOutput;
    }
    
    public XmlDestinationImpl(XMLStreamWriter xswWriter) {
        this.xswStreamWriter = xswWriter;
    }   
    
    public XmlDestinationImpl(OMElement oeAxiomElement) {
        this.oeAxiomElement = oeAxiomElement;
    }     
    
    public XmlDestinationImpl(int iNomElem) {
        this.iNomElement = iNomElem;
    }      
    
    /**
     * @see com.cordys.coe.bf.content.base.IXmlDestination#getOutputStream()
     */
    public OutputStream getOutputStream() { 
        return osOutputStream;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IXmlDestination#getStreamWriter()
     */
    public XMLStreamWriter getStreamWriter() {
        return xswStreamWriter;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IXmlDestination#getAxiomElement()
     */
    public OMElement getAxiomElement() {
        return oeAxiomElement;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IXmlDestination#set(java.io.OutputStream)
     */
    public void set(OutputStream osOutput)
    {
        this.osOutputStream = osOutput;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IXmlDestination#set(javax.xml.stream.XMLStreamWriter)
     */
    public void set(XMLStreamWriter xswWriter)
    {
        this.xswStreamWriter = xswWriter;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IXmlDestination#set(org.apache.axis2.om.OMElement)
     */
    public void set(OMElement oeElem)
    {
        this.oeAxiomElement = oeElem;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IXmlDestination#set(int)
     */
    public void set(int iNomElem)
    {
        this.iNomElement = iNomElem;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IXmlDestination#getNomElement()
     */
    public int getNomElement()
    {
        return iNomElement;
    }    
}