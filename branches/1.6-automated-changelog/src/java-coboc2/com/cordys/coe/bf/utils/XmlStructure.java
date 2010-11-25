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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import com.cordys.coe.bf.exception.BFException;

/**
 * A class for holding XML structures. The XML is stored internally as a string.
 *
 * @author mpoyhone
 */
public class XmlStructure
{
    protected OMDocument odDoc;
    
    /**
     * @see java.lang.Object#clone()
     */
    public XmlStructure clone() {
        XmlStructure xdRes = new XmlStructure();
        
        xdRes.copyFrom(xdRes);
        
        return xdRes;
    }
    
    
    /**
     * Copies the XML from the given structure.
     * @param xsSrc Source where the XML is copied from.
     */
    public void copyFrom(XmlStructure xsSrc) {
        odDoc = null;
        
        if (xsSrc.odDoc != null) {
            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            
            odDoc = omFactory.createOMDocument();
            
            if (xsSrc.odDoc.getOMDocumentElement() != null) {
                try
                {
                    readFromXml(xsSrc.odDoc.getOMDocumentElement(), false);
                }
                catch (XMLStreamException e)
                {
                    // Just dump it.
                    e.printStackTrace();
                }
            }
        }
    }    
    public void setString(String sValue) throws BFException {
        try
        {
            XMLStreamReader parser = AxiomUtils.xifXmlInputFactory
                                                    .createXMLStreamReader(new StringReader(sValue));

            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            StAXOMBuilder builder = new StAXOMBuilder(omFactory, parser);
            odDoc = builder.getDocument();
        }
        catch (Exception e)
        {
            throw new BFException("Invalid XML string.", e);
        }
    }
    
    public String getString() {
        try
        {
            StringWriter swStringWriter = new StringWriter(1024);
            XMLStreamWriter xswWriter = AxiomUtils.xofXmlOuputFactory.createXMLStreamWriter(swStringWriter);
            OMElement oeRoot = odDoc.getOMDocumentElement();
            
            if (oeRoot.getNamespace() != null) {
                xswWriter.setDefaultNamespace(oeRoot.getNamespace().getName());
            }
            
            oeRoot.serialize(xswWriter);
            
            return swStringWriter.toString();
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    public OMElement getRootElement() {
        return odDoc.getOMDocumentElement();
    }
    
    public void readFromXml(OMElement oeElem, boolean preserveNamespace) throws XMLStreamException, FactoryConfigurationError {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement oeRoot;
        
        odDoc = omFactory.createOMDocument();
        oeRoot = omFactory.createOMElement(new QName("xmlstruct"), odDoc);
        
        for (Iterator<?> iIter = oeElem.getChildren(); iIter.hasNext(); ) {
            OMNode onChild = (OMNode) iIter.next();
            
            if (onChild.getType() == OMNode.ELEMENT_NODE) {
                oeRoot.addChild(((OMElement) onChild).cloneOMElement());
            }
        }
        
        for (Iterator<?> iIter = oeElem.getAllAttributes(); iIter.hasNext(); ) {
            oeRoot.addAttribute((OMAttribute) iIter.next());
        }

        String elemNs = oeElem.getNamespace() != null ? oeElem.getNamespace().getName() : null;
        
        for (Iterator<?> iIter = oeElem.getAllDeclaredNamespaces(); iIter.hasNext(); ) {
            OMNamespace ns = (OMNamespace) iIter.next();
            
            if (elemNs == null || preserveNamespace || ! elemNs.equals(ns.getName())) {
                oeRoot.declareNamespace(ns);
            }
        }
        
        // Change the namespace of the whole tree to empty. This will avoid creation
        // of empty namespace declarations in the children.
        if (! preserveNamespace) {
            OMNamespace onNewNamespace = omFactory.createOMNamespace("", "");
            AxiomUtils.changeNamespace(oeRoot, oeElem.getNamespace(), onNewNamespace);
        }
    }    
    
    public void writeToXml(XMLStreamWriter xswWriter) throws XMLStreamException, FactoryConfigurationError {
        if (odDoc == null || odDoc.getOMDocumentElement() == null) {
            return;
        }
        
        // Fake the default namespace.
        String sOldDefaultNamespace = xswWriter.getNamespaceContext().getNamespaceURI("");
        xswWriter.setDefaultNamespace("");
        
        OMElement oeRoot = odDoc.getOMDocumentElement();
        
        for (Iterator<?> iIter = oeRoot.getAllAttributes(); iIter.hasNext(); ) {
            OMAttribute attrib = (OMAttribute) iIter.next();
            
            xswWriter.writeAttribute(attrib.getLocalName(), attrib.getAttributeValue());
        }
        
        for (Iterator<?> iIter = oeRoot.getAllDeclaredNamespaces(); iIter.hasNext(); ) {
            OMNamespace ns = (OMNamespace) iIter.next();
            
            if (! "".equals(ns.getName())) {
                xswWriter.writeNamespace(ns.getPrefix(), ns.getName());
            }
        }
        
        for (Iterator<?> iIter = oeRoot.getChildren(); iIter.hasNext(); ) {
            OMNode onChild = (OMNode) iIter.next();
            
            onChild.serialize(xswWriter);
        }
        
        xswWriter.setDefaultNamespace(sOldDefaultNamespace);
    }
    
    public List<OMNode> selectNodes(XPath xpPath) throws BFException {
        if (odDoc == null || odDoc.getOMDocumentElement() == null) {
            return null;
        }
        
        List<?> lNodeList;
        
        try
        {
            lNodeList = xpPath.selectNodes(odDoc.getOMDocumentElement());
        }
        catch (Exception e)
        {
            throw new BFException("XPath operation failed for XmlStructure. XPath=" + xpPath, e);
        }
        
        List<OMNode> lResList = new ArrayList<OMNode>(lNodeList.size());
        
        for (Object oTmp : lNodeList)
        {
            lResList.add((OMNode) oTmp);
        }
        
        return lResList;
    }
    
    public List<Object> selectNodesOrAttributes(XPath xpPath) throws BFException {
        if (odDoc == null || odDoc.getOMDocumentElement() == null) {
            return null;
        }
        
        List<?> lNodeList;
        
        try
        {
            lNodeList = xpPath.selectNodes(odDoc.getOMDocumentElement());
        }
        catch (Exception e)
        {
            throw new BFException("XPath operation failed for XmlStructure. XPath=" + xpPath, e);
        }
        
        List<Object> lResList = new ArrayList<Object>(lNodeList.size());
        
        for (Object oTmp : lNodeList)
        {
            lResList.add(oTmp);
        }
        
        return lResList;
    }
    
    /**
     * Returns the node pointed by the XPath expression.
     * @param xpPath XPath.
     * @return OMNode found or <code>null</code> if the node did not exist in the XML.
     */
    public Object selectSingleNode(XPath xpPath, boolean bMustExist) throws BFException {
        if (odDoc == null || odDoc.getOMDocumentElement() == null) {
            return null;
        }
        
        Object onNode;
        
        try
        {
            onNode = xpPath.selectSingleNode(odDoc.getOMDocumentElement());
        }
        catch (JaxenException e)
        {
            throw new BFException("Unable to set the XML element with XPath " + xpPath.toString(), e);
        }
        
        if (onNode == null) {
            if (bMustExist) {
                throw new BFException("No node found with XPath " + xpPath.toString());
            }
        }
        
        return onNode;
    }    
    
    /**
     * Returns the value pointed by the XPath expression.
     * @param xpPath XPath.
     * @param bMustExist If <code>true</code> and the value does not exist, an exception is thrown.
     * @return Value.
     */
    public String getValue(XPath xpPath, boolean bMustExist) throws BFException {
        String sValue = null;
        
        if (odDoc != null && odDoc.getOMDocumentElement() != null) {
            Object onNode = selectSingleNode(xpPath, false);
        
            if (onNode != null) {
                sValue = AxiomUtils.getNodeText(onNode); 
            }
        }
        
        if (sValue == null) {
            if (bMustExist) {
                throw new BFException("No value found with XPath " + xpPath.toString());
            }
        }
        
        return sValue;
    } 
    
    /**
     * Returns the value pointed by the XPath expression.
     * @param xpPath XPath.
     * @param sDefaultValue Default value to be returned when the value could not be found from the XML.
     * @return Value.
     */
    public String getValue(XPath xpPath, String sDefaultValue) throws BFException {
        if (odDoc == null || odDoc.getOMDocumentElement() == null) {
            return sDefaultValue;
        }
        
        Object onNode = selectSingleNode(xpPath, false);
        
        if (onNode == null) {
            return sDefaultValue;
        }
        
        String sValue = AxiomUtils.getNodeText(onNode);   
        
        return sValue != null ? sValue : sDefaultValue;
    }    
    
    /**
     * Sets the value pointed by the XPath expression.
     * @param xpPath XPath.
     * @param sValue Value to be set.
     * @return <code>true</code> if the value was set.
     */
    public boolean setValue(XPath xpPath, String sValue) throws BFException {
        if (odDoc == null || odDoc.getOMDocumentElement() == null) {
            return false;
        }
        
        Object onNode = selectSingleNode(xpPath, false);
        
        if (onNode == null) {
            return false;
        }
        
        AxiomUtils.setNodeText(onNode, sValue);
        
        return true;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return odDoc != null ? getString() : "null";
    }
    

}
