/**
 * Copyright 2005 Cordys R&D B.V. 
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

package com.cordys.tools.ant.util;

import java.io.UnsupportedEncodingException;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.NodeType;
import com.eibus.xml.nom.XMLException;

/**
 * Helper class for handling xml related functions.
 *
 * @author Keerti Nayak
 */
public class XMLUtils
{
    /**
     * XML Prefix used to prefix in the XML output created.
     */
    public static final String XML_FILE_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    /**
     * document used to parse the string to Node reference.
     */
    private static Document document = new Document();

    /**
     * Replaces XML CDATA like &amp;apos;, &amp;quot;, &amp;lt;, &amp;gt;,
     * &amp;amp;  with XML characters like ', ", &lt;, &gt;, &amp;
     * This will also remove any XML definitions (&lt;?xml ... ?&gt;).
     * 
     * @param xmlString The input string that needs to be replaced for xml
     *        special.
     *
     * @return The xml special character replaced string.
     */
    public static String CDATAToXML(String xmlString)
    {
        xmlString = xmlString.replaceAll("&apos;", "'");
        xmlString = xmlString.replaceAll("&quot;", "\"");
        xmlString = xmlString.replaceAll("&lt;", "<");
        xmlString = xmlString.replaceAll("&gt;", ">");
        xmlString = xmlString.replaceAll("&amp;", "&");
        xmlString = xmlString.replaceAll("<\\?xml[^?]*\\?>", "");
        
        return xmlString;
    }
    
    /**
     * Replaces XML CDATA like &amp;apos;, &amp;quot;, &amp;lt;, &amp;gt;,
     * &amp;amp;  with XML characters like ', ", &lt;, &gt;, &amp;
     * 
     * This version actually iterates over the nodes instead of just
     * blindly replacing the strings. The other version breaks XML 
     * put in an attribute.
     *
     * @param xmlString The input string that needs to be replaced for xml
     *        special.
     *
     * @return The xml special character replaced string.
     */
    public static void iterateAllCDATAToXML(int xRootNode)
    {
        int iCount = Node.getNumAttributes(xRootNode);
        
        for (int i = 1; i <= iCount; i++) {
            String sName = Node.getAttributeName(xRootNode, i);
            String sValue = Node.getAttribute(xRootNode, sName);
            
            if (sValue == null) {
                continue;
            }
            
            Node.setAttribute(xRootNode, sName, CDATAToXML(sValue));
        }
        
        // Check if the element has child elements. If this is a simple text
        // element, we combine all data elements and try to parse them as XML.
        boolean bHasChildElems = Node.getFirstElement(xRootNode) != 0;
        
        if (bHasChildElems) {
            // This is a complex element, so recurse to the children.
            int xChild = Node.getFirstChild(xRootNode);
            
            while (xChild != 0) {
                int xNextChild = Node.getNextSibling(xChild);
                
                if (Node.getType(xChild) == NodeType.ELEMENT) {
                    iterateAllCDATAToXML(xChild);
                }
                
                xChild = xNextChild;
            }
                
            return;
        }
        
        // This is a text element, so combine all data and try to parse
        // it as XML.
        String sValue = Node.getData(xRootNode);
                
        if (sValue != null) {
            // If it looks like XML, try to parse it
            // and add it to the parent node.
            int iStartPos = sValue.indexOf('<');
            
            if (iStartPos >= 0) {
                // Check that is it whitespace.
                String sTmp = sValue.substring(0, iStartPos);
                
                if (sTmp.matches("^\\s*$")) {
                    int xParsedNode = 0;
                    
                    try {
                        xParsedNode = Node.getDocument(xRootNode).parseString(sValue);
                    }
                    catch (Exception e) {
                    }
                    
                    if (xParsedNode != 0) {
                        int xChild;
                        
                        while ((xChild = Node.getFirstChild(xRootNode)) != 0) {
                            Node.delete(xChild);
                            xChild = 0;
                        }
                        
                        Node.appendToChildren(xParsedNode, xRootNode);
                        xParsedNode = 0;
                    }
                }
            }
        }
    }

    /**
     * Replaces XML characters like ', ", &lt;, &gt;, &amp; with XML CDATA like
     * &amp;apos;, &amp;quot;, &amp;lt;, &amp;gt;, &amp;amp;
     *
     * @param xmlString The input string that needs to be replaced for xml
     *        special.
     *
     * @return The xml special character replaced string.
     */
    public static String XMLToCDATA(String xmlString)
    {
        xmlString = xmlString.replaceAll("&", "&amp;");
        xmlString = xmlString.replaceAll("'", "&apos;");
        xmlString = xmlString.replaceAll("\"", "&quot;");
        xmlString = xmlString.replaceAll("<", "&lt;");
        xmlString = xmlString.replaceAll(">", "&gt;");

        return xmlString;
    }

    /**
     * Converts all the child tags matched by the pattern as parameter in the
     * node passed as parameter to CDATA string.
     *
     * @param node The xml node reference in which the pattern has to be looked
     *        for.
     * @param pattern The pattern of the node to to be replaced with string
     */
    public static void convertNodeToString(int node, String pattern)
    {
        //match for the pattern in the xml passed.
        int[] matchedNodes = Find.match(node, pattern);

        if (matchedNodes.length > 0)
        {
            for (int i = 0; i < matchedNodes.length; i++)
            {
                //get the first child of the parent node.
                int childNode = Node.getFirstChild(matchedNodes[i]);

                //get the contents of the matched node.
                String formatedStr = Node.writeToString(childNode, true);

                //get the name of the parent node.
                String parentNodeName = Node.getName(matchedNodes[i]);

                //get the parent node.
                int parentNode = Node.getParent(matchedNodes[i]);

                //delete the matched node.
                Node.delete(matchedNodes[i]);

                //recreate the matched node with its nested tags in xmlstring.
                Node.setDataElement(parentNode, parentNodeName, formatedStr);
            }
        }
    }

    /**
     * Formats the xml to nice xml by parsing it.
     *
     * @param xmlString The xml string to be formatted.
     *
     * @return The formatted nice xml string.
     *
     * @throws UnsupportedEncodingException
     * @throws XMLException
     */
    public static String formatToNiceXML(String xmlString)
                                  throws UnsupportedEncodingException, 
                                         XMLException
    {
        int niceXmlNode = document.parseString(xmlString);

        return Node.writeToString(niceXmlNode, true);
    }
}
