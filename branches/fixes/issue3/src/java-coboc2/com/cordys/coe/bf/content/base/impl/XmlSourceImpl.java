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

import java.io.InputStream;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.content.base.IXmlSource;

public class XmlSourceImpl implements IXmlSource {
    private InputStream isInputStream;
    private XMLStreamReader xswStreamReader;
    private OMElement oeElement;
    
    public XmlSourceImpl() {
    }        
    
    public XmlSourceImpl(InputStream isInputStream) {
        this.isInputStream = isInputStream;
    }
    
    public XmlSourceImpl(XMLStreamReader xswReader) {
        this.xswStreamReader = xswReader;
    }    
    
    public XmlSourceImpl(OMElement oeElement) {
        this.oeElement = oeElement;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IXmlSource#set(java.io.InputStream)
     */
    public void set(InputStream isInput) {
        this.isInputStream = isInput;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IXmlSource#set(javax.xml.stream.XMLStreamReader)
     */
    public void set(XMLStreamReader xswStreamReader) {
        this.xswStreamReader = xswStreamReader;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IXmlSource#set(org.apache.axis2.om.OMElement)
     */
    public void set(OMElement oeElement) {
        this.oeElement = oeElement;
    }
    
    
    /**
     * @see com.cordys.coe.bf.content.base.IXmlSource#getInputStream()
     */
    public InputStream getInputStream() { 
        return isInputStream;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IXmlSource#getStreamReader()
     */
    public XMLStreamReader getStreamReader() {
        return xswStreamReader;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IXmlSource#getOMElement()
     */
    public OMElement getOMElement() {
        return oeElement;
    }
}