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
package com.cordys.coe.bf.content.base;

import java.io.OutputStream;

import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.content.base.impl.XmlDestinationImpl;

/**
 * TODO Describe the class.
 *
 * @author mpoyhone
 */
public interface IXmlDestination
{
    public static class Factory {
        public static IXmlDestination newInstance() {
            return new XmlDestinationImpl();
        }    
        
        public static IXmlDestination newInstance(OutputStream osOutput) {
            return new XmlDestinationImpl(osOutput);
        }
        
        public static IXmlDestination newInstance(XMLStreamWriter xswWriter) {
            return new XmlDestinationImpl(xswWriter);
        }   
        
        public static IXmlDestination newInstance(OMElement oeAxiomElement) {
            return new XmlDestinationImpl(oeAxiomElement);
        }
        
        public static IXmlDestination newInstance(int iNomElement) {
            return new XmlDestinationImpl(iNomElement);
        }        
    }
    
    public void set(OutputStream osOutput);
    public void set(XMLStreamWriter xswWriter);
    public void set(OMElement oeElem);
    public void set(int iNomElem);
    
    public OutputStream getOutputStream();

    public XMLStreamWriter getStreamWriter();

    public OMElement getAxiomElement();
    
    public int getNomElement();

}
