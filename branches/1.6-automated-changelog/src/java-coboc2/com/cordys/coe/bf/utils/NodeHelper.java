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

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * Utility class to use W3C DOM like NOM.
 *
 * @author mpoyhone
 */
public class NodeHelper {
    private static final int DEFAULT_TAB_SIZE = 4;
    private static final int DEFAULT_ATTRIB_LINE_SPLIT_WIDTH = 80;

    /**
     * @param aBaseNode
     * @return
     */
    public static String getLocalName(Element eBaseNode) {
        String sRes = eBaseNode.getLocalName();

        if (sRes == null) {
            sRes = eBaseNode.getNodeName();
        }

        return sRes;
    }

    /**
     * @param aFinal
     * @param aPretty
     * @return
     */
    public static String writeToString(Element nNode, boolean bPretty) {
        StringWriter swWriter = new StringWriter(2048);

        try {
            writeToWriter(nNode, swWriter, 0, bPretty ? DEFAULT_TAB_SIZE : (-1));
        } catch (IOException ignored) {
        }

        return swWriter.toString();
    }

    public static void writeToWriter(Node nRootNode, Writer wWriter,
        int iLevel, int iIndentSize) throws IOException {
        StringBuffer sb = new StringBuffer(128);
        boolean bWriteEndTag = false;
        boolean bIndentEndTag = false;

        switch (NodeHelper.getType(nRootNode)) {
        case Node.ELEMENT_NODE:

            if (iIndentSize > 0) {
                for (int i = 0; i < (iLevel * iIndentSize); i++) {
                    sb.append(' ');
                }
            }

            sb.append('<');
            sb.append(nRootNode.getNodeName());

            int iNumAttribs = NodeHelper.getNumAttributes(nRootNode);
            int iLineStartPos = 0;
            int iAttribIndentColumn = sb.length();

            for (int i = 0; i < iNumAttribs; i++) {
                String sName = NodeHelper.getAttributeName(nRootNode, i + 1);
                String sValue = NodeHelper.getAttribute(nRootNode, sName, "");

                sValue = escapeAttribute(sValue);

                sb.append(' ');
                sb.append(sName);
                sb.append("=\"");
                sb.append(sValue);
                sb.append("\"");

                if (iIndentSize > 0) {
                    int iAttribEndPos = sb.length();

                    // Check that if the attribute when over.
                    if (((iAttribEndPos - iLineStartPos) > DEFAULT_ATTRIB_LINE_SPLIT_WIDTH) &&
                            (i < (iNumAttribs - 1))) {
                        // Add a new line and indent to the attribute indent pos.
                        sb.append('\n');
                        iLineStartPos = sb.length();

                        for (int j = 0; j < iAttribIndentColumn; j++) {
                            sb.append(' ');
                        }
                    }
                }
            }

            if ((NodeHelper.getFirstChild(nRootNode) != null) || (iLevel == 0)) {
                sb.append('>');
                bWriteEndTag = true;
            } else {
                sb.append("/>");

                if (iIndentSize >= 0) {
                    sb.append("\n");
                }
            }

            break;

        case Node.TEXT_NODE: {
            String sValue = nRootNode.getNodeValue();

            if (sValue != null) {
                sValue = escapeData(sValue).trim();
                sb.append(sValue);
            }
        }

        break;

        case Node.CDATA_SECTION_NODE: {
            if (iIndentSize > 0) {
                for (int i = 0; i < (iLevel * iIndentSize); i++) {
                    sb.append(' ');
                }
            }

            String sValue = nRootNode.getNodeValue();

            if (sValue != null) {
                sValue = escapeCData(sValue);
            }

            sb.append("<![CDATA[");

            if (sValue != null) {
                sb.append(sValue);
            }

            sb.append("]]>");
        }

        break;

        case Node.COMMENT_NODE: {
            if (iIndentSize > 0) {
                for (int i = 0; i < (iLevel * iIndentSize); i++) {
                    sb.append(' ');
                }
            }

            String sValue = nRootNode.getNodeValue();

            if (sValue != null) {
                sValue = escapeComment(sValue);
            }

            sb.append("<!--");

            if (sValue != null) {
                sb.append(sValue);
            }

            sb.append("-->");
        }

        break;

        default:
            throw new IllegalStateException("Invalid node type encountered: " +
                NodeHelper.getType(nRootNode));
        }

        wWriter.write(sb.toString());
        sb = null;

        Node nChild = NodeHelper.getFirstChild(nRootNode);
        boolean bHasChildElements = false;

        if (iIndentSize >= 0) {
            nChild = NodeHelper.getFirstChild(nRootNode);

            while (nChild != null) {
                if (nChild.getNodeType() != Node.TEXT_NODE) {
                    bIndentEndTag = true;
                    bHasChildElements = true;
                }

                nChild = NodeHelper.getNextSibling(nChild);
            }

            if (bHasChildElements) {
                wWriter.write("\n");
            }
        }

        nChild = NodeHelper.getFirstChild(nRootNode);

        while (nChild != null) {
            writeToWriter(nChild, wWriter, iLevel + 1, iIndentSize);

            nChild = NodeHelper.getNextSibling(nChild);
        }

        if (bWriteEndTag) {
            sb = new StringBuffer(128);

            if (bIndentEndTag) {
                for (int i = 0; i < (iLevel * iIndentSize); i++) {
                    sb.append(' ');
                }
            }

            sb.append(("</" + nRootNode.getNodeName() + ">"));

            if (iIndentSize >= 0) {
                sb.append("\n");
            }

            wWriter.write(sb.toString());
            sb = null;
        }
    }

    /**
     * @param aChild
     * @return
     */
    public static String getData(Node nNode) {
        return NodeHelper.getDataWithDefault(nNode, null);
    }

    /**
     * @param aChild
     * @return
     */
    public static String getDataWithDefault(Node nNode, String sDefault) {
        if (nNode == null) {
            return null;
        }

        StringBuffer sbRes = null;

        for (Node nData = nNode.getFirstChild(); nData != null;
                nData = nData.getNextSibling()) {
            if ((nData.getNodeType() == Node.CDATA_SECTION_NODE) ||
                    (nData.getNodeType() == Node.TEXT_NODE)) {
                String sValue = nData.getNodeValue();

                if (sValue != null) {
                    if (sbRes == null) {
                        sbRes = new StringBuffer(256);
                    }

                    sbRes.append(sValue);
                }
            }
        }

        return (sbRes != null) ? sbRes.toString() : null;
    }

    /**
     * @param aChild
     * @return
     */
    public static int getType(Node nNode) {
        return (nNode != null) ? nNode.getNodeType() : 0;
    }

    /**
     * @param aChild
     * @return
     */
    public static Node getNextSibling(Node nNode) {
        return (nNode != null) ? nNode.getNextSibling() : null;
    }

    /**
     * @param aRootNode
     * @return
     */
    public static Node getFirstChild(Node nNode) {
        return (nNode != null) ? nNode.getFirstChild() : null;
    }

    /**
     * @param aRootNode
     * @param aName
     * @return
     */
    public static String getAttribute(Node nNode, String nName) {
        return getAttribute(nNode, nName, null);
    }

    /**
     * @param aRootNode
     * @param aName
     * @param nDefault
     * @return
     */
    public static String getAttribute(Node nNode, String nName, String nDefault) {
        NamedNodeMap nnmAttribs = nNode.getAttributes();

        if (nnmAttribs == null) {
            return null;
        }

        Node nAttrib = nnmAttribs.getNamedItem(nName);

        return (nAttrib != null) ? nAttrib.getNodeValue() : nDefault;
    }

    /**
     * @param aRootNode
     * @param aI
     * @return
     */
    public static String getAttributeName(Node nNode, int iIndex) {
        NamedNodeMap nnmAttribs = nNode.getAttributes();

        if (nnmAttribs == null) {
            return null;
        }

        Node nAttrib = nnmAttribs.item(iIndex - 1);

        return (nAttrib != null) ? nAttrib.getNodeName() : null;
    }

    /**
     * @param aRootNode
     * @return
     */
    public static int getNumAttributes(Node nNode) {
        NamedNodeMap nnmAttribs = nNode.getAttributes();

        return (nnmAttribs != null) ? nnmAttribs.getLength() : 0;
    }

    public static String escapeAttribute(String sValue) {
        if (sValue == null) {
            return null;
        }

        return sValue.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;");
    }

    public static String escapeData(String sValue) {
        if (sValue == null) {
            return null;
        }

        return sValue.replaceAll("&", "&amp;").replaceAll("<", "&lt;");
    }

    public static String escapeCData(String sValue) {
        if (sValue == null) {
            return null;
        }

        return sValue.replaceAll("]]>", "]]&gt;");
    }

    public static String escapeComment(String sValue) {
        if (sValue == null) {
            return null;
        }

        return sValue.replaceAll("-->", "--&gt;");
    }

    /**
     * @param aNode
     * @param aString
     * @param aFilename
     */
    public static void setAttribute(Element eNode, String sName, String sValue) {
        eNode.setAttribute(sName, sValue);
    }

    /**
     * @param aElement
     * @param aString
     */
    public static void removeAttribute(Element eNode, String sName) {
        eNode.removeAttribute(sName);
    }

    /**
     * @param eXmlNode
     * @param bPretty
     * @return
     */
    public static byte[] write(Element eXmlNode, boolean bPretty) {
        String sTmp = writeToString(eXmlNode, bPretty);
        byte[] baRes;

        try {
            baRes = sTmp.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            baRes = new byte[0];
        }

        return baRes;
    }

    /**
     * @param aCurrent
     * @return
     */
    public static String getName(Node nNode) {
        return (nNode != null) ? nNode.getNodeName() : null;
    }

    /**
     * @param aRootNode
     * @param sFilePath
     * @param aB
     */
    public static void writeToFile(Element aRootNode, String sFilePath,
        boolean bWritePretty) throws IOException {
        FileWriter fwWriter = new FileWriter(sFilePath);

        writeToWriter(aRootNode, fwWriter, 0,
            bWritePretty ? DEFAULT_TAB_SIZE : (-1));
    }
}
