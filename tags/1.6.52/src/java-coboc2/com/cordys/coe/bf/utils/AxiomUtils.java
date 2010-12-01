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
package com.cordys.coe.bf.utils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.w3c.dom.Document;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;

/**
 * TODO Describe the class.
 *
 * @author mpoyhone
 */
public class AxiomUtils
{
    public static OMFactory omFactory = OMAbstractFactory.getOMFactory();
    // Set the Woodstox implementation as default.
    public static XMLInputFactory xifXmlInputFactory = new WstxInputFactory();
    public static XMLOutputFactory xofXmlOuputFactory = new WstxOutputFactory();
    
    public static OMElement loadFile(String sFile) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        FileReader frReader = null;
        XMLStreamReader xsrXmlReader = null;
        
        try {
            frReader = new FileReader(sFile);
            xsrXmlReader = xifXmlInputFactory.createXMLStreamReader(frReader);
        
            StAXOMBuilder builder = new StAXOMBuilder(omFactory, xsrXmlReader);
            OMElement oeDocumentElement = builder.getDocumentElement();
            
            oeDocumentElement.build();
            
            return oeDocumentElement;
        }
        finally {
            if (frReader != null) {
                try
                {
                    frReader.close();
                }
                catch (IOException ignored)
                {
                }
            }
            
            if (xsrXmlReader != null) {
                xsrXmlReader.close();
            }
        }
    }
    
    public static OMElement loadStream(InputStream isInput) throws XMLStreamException, FactoryConfigurationError {
        XMLStreamReader xsrXmlReader = null;
        
        try {
            xsrXmlReader = xifXmlInputFactory.createXMLStreamReader(isInput);
        
            StAXOMBuilder builder = new StAXOMBuilder(omFactory, xsrXmlReader);
            OMElement oeDocumentElement = builder.getDocumentElement();
            
            oeDocumentElement.build();
            
            return oeDocumentElement;
        }
        finally {
            if (xsrXmlReader != null) {
                xsrXmlReader.close();
            }
        }
    }
    
    public static OMElement parseString(String sXml) throws XMLStreamException, FactoryConfigurationError {
        StringReader srReader = new StringReader(sXml);
        XMLStreamReader xsrXmlReader = null;
        
        try {
            xsrXmlReader = xifXmlInputFactory.createXMLStreamReader(srReader);
        
            StAXOMBuilder builder = new StAXOMBuilder(omFactory, xsrXmlReader);
            OMElement oeDocumentElement = builder.getDocumentElement();
            
            oeDocumentElement.build();
            
            return oeDocumentElement;
        }
        finally {
            if (srReader != null) {
                srReader.close();
            }
            
            if (xsrXmlReader != null) {
                xsrXmlReader.close();
            }
        }
    }    
    
    public static String writeToString(OMElement oeRoot) throws XMLStreamException
    {
        StringWriter swWriter = new StringWriter(4096);
        XMLStreamWriter writer = xofXmlOuputFactory.createXMLStreamWriter(swWriter);
        oeRoot.serialize(writer);
        writer.flush();
        writer.close();
                
        return swWriter.toString();        
    }
    
    public static void skipToStreamEvent(XMLStreamReader xsrReader, int iToEvent) throws XMLStreamException {
        while (xsrReader.hasNext()) {
            int iEvent = xsrReader.next();
            
            if (iEvent == iToEvent) {
                return;
            }
        }
    }
    
    public static OMElement getNextSiblingElement(OMElement oeElem) {
        OMNode onNext = oeElem.getNextOMSibling();
        
        while (onNext != null && onNext.getType() != OMNode.ELEMENT_NODE) {
            onNext = onNext.getNextOMSibling();
        }
        
        return (OMElement) onNext;
    }
    
    public static String prettyPrintXml(String sXml) {
        try
        {
            Document dResponseDoc;
            
            DocumentBuilderFactory dbfFactory = DocumentBuilderFactory.newInstance();
            
            dbfFactory.setNamespaceAware(true);
            dResponseDoc = dbfFactory.newDocumentBuilder().parse(new ByteArrayInputStream(sXml.getBytes("UTF-8")));
            
            return NodeHelper.writeToString(dResponseDoc.getDocumentElement(), true);
            
        }
        catch (Exception e)
        {
            return sXml;
        }        
    }
    
    public static void skipToStreamEvent(XMLStreamReader xsrReader, int[] iaToEvents) throws XMLStreamException {
        while (xsrReader.hasNext()) {
            int iEvent = xsrReader.next();
            
            for (int iToEvent : iaToEvents)
            {
                if (iEvent == iToEvent) {
                    return;
                }
            }
        }
    }    
    
    public static boolean namespaceEquals(QName qn1, QName qn2)
    {
        if (qn1 == null || qn2 == null) {
            return qn1 == qn2;
        }
        
        // We ignore the prefixes for now.
        String u1 = qn1.getNamespaceURI();
        String u2 = qn2.getNamespaceURI();
        
        if (u1 == null) {
            return u1 == u2;
        } else {
            return u1.equals(u2);
        }
    }
    
    public static QName createQName(String sLocalPart, String sPrefix, String sNamespaceUri) {
        if (sPrefix != null && sNamespaceUri != null) {
            return new QName(sNamespaceUri, sLocalPart, sPrefix);
        }        
        
        if (sNamespaceUri != null) {
            return new QName(sNamespaceUri, sLocalPart);
        }
        
        return new QName(sLocalPart);
    }
    
    public static void changeNamespace(OMNode oeRoot, OMNamespace onFromNamespace,  
                                        OMNamespace onToNamespace) {
        String sFromUri = onFromNamespace != null ? onFromNamespace.getName() : null;
        String sToUri = onToNamespace != null ? onToNamespace.getName() : null;
        
        if (sFromUri == null) {
            sFromUri = "";
        }
        
        if (sToUri == null) {
            sToUri = "";
        }
        
        if (oeRoot.getType() == OMNode.ELEMENT_NODE) {
            OMElement oeElem = (OMElement) oeRoot;
            String sElemUri = oeElem.getNamespace() != null ? oeElem.getNamespace().getName() : null;
            
            if (sElemUri == null) {
                sElemUri = "";
            }            
            
            // If the current namespace is not set or empty or equals to the from namespace
            // replace it with the to namespace.
            if (sElemUri.length() == 0 || sElemUri.equals(sFromUri)) {
                // Remove the namespace declaration.
                for (Iterator<?> iIter = oeElem.getAllDeclaredNamespaces(); iIter.hasNext();)
                {
                    OMNamespace onNs = (OMNamespace) iIter.next();
                    
                    if (sElemUri.equals(onNs.getName())) {
                        iIter.remove();
                    }
                }
                
                oeElem.setNamespace(onToNamespace);
            }
            
            for (Iterator<?> iIter = ((OMElement) oeRoot).getChildren(); iIter.hasNext(); ) {
                OMNode onChild = (OMNode) iIter.next();
            
                changeNamespace(onChild, onFromNamespace, onToNamespace);
            }  
        }
    }
    
    /**
     * Returns node's text. This returns all text contained by this element or
     * all text contained by text node's parent.
     * @param onNode Node.
     * @return Node's text
     */
    public static String getNodeText(Object onNode) {
        if (onNode instanceof OMNode) {
            OMElement oeElement;
            
            switch (((OMNode) onNode).getType()) {
            case OMNode.ELEMENT_NODE :
                oeElement = (OMElement) onNode;
                break;
                
            default:
                oeElement = (OMElement) ((OMNode) onNode).getParent();
                break;
            }   
        
            return oeElement.getText();
        } else if (onNode instanceof OMAttribute) {
            return ((OMAttribute) onNode).getAttributeValue();
        }
        
        return null;
    }    
    
    /**
     * Sets node's text. 
     * @param onNode Node.
     * @param sValue Text to be set.
     * @return <code>true</code> if the text was set.
     */
    public static boolean setNodeText(Object onNode, String sValue) {
        if (onNode instanceof OMNode) {
            OMElement oeElement;
            
            switch (((OMNode) onNode).getType()) {
            case OMNode.ELEMENT_NODE :
                oeElement = (OMElement) onNode;
                break;
                
            default:
                oeElement = (OMElement) ((OMNode) onNode).getParent();
                break;
            }   
            
            oeElement.setText(sValue);
            
            return true;
        } else if (onNode instanceof OMAttribute) {
            ((OMAttribute) onNode).setAttributeValue(sValue);
            
            return true;
        }
        
        return false;
    }
    
    public static void setAttribute(OMElement elem, String attribName, String value)
    {
        QName qn = createQName(attribName, null, null);
        OMAttribute attrib = elem.getAttribute(qn);
        
        if (attrib != null) {
            if (value != null) {
                attrib.setAttributeValue(value);
            } else {
                elem.removeAttribute(attrib);
            }
        } else {
            if (value != null) {
                elem.addAttribute(attribName, value, elem.getNamespace());
            } 
        }
    }
    
    public static void removeAttribute(OMElement elem, String attribName)
    {
        setAttribute(elem, attribName, null);
    }
    
    /**
     * Returns the first child element with the given local name.
     * @param oeNode Node.
     * @param name Local name.
     * @return Found child or <code>null</code>.
     */
    public static OMElement getChildElementWithLocalName(OMElement node, String name)
    {
        if (node == null)
        {
            throw new NullPointerException("'node' cannot be null.");
        }

        if (name == null)
        {
            throw new NullPointerException("'name' cannot be null.");
        }

        OMNode child = node.getFirstElement();

        while (child != null) {
            switch (child.getType()) {
            case OMNode.ELEMENT_NODE :
                OMElement elem = (OMElement) child;

                if (name.equals(elem.getLocalName())) {
                    return elem;
                }
                break;
            }

            child = child.getNextOMSibling();
        }

        return null;
    }    
}
