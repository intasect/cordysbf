/*
 * Created on Sep 8, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
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
package com.cordys.coe.ant.coboc.util;

import java.io.UnsupportedEncodingException;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * @author mpoyhone
 */
public class Serializer {
	protected boolean bIsLoading;
	protected Document dDoc;
	
	public Serializer() {
		bIsLoading = true;
	}
	
	public Serializer(Document dDoc) {
		this.dDoc = dDoc;
		bIsLoading = false;
	}
	
	public String readString(int iParentNode, String sName) throws XMLException {
		return readString(iParentNode, sName, null);
	}
	
	public int readInt(int iParentNode, String sName, int iDefault) throws XMLException {
		String sValue = readString(iParentNode, sName, null);
		
		if (sValue == null || sValue.equals("")) {
			return iDefault;
		}
	
		try {
			return Integer.parseInt(sValue);
		}
		catch (Exception e) {
			throw new XMLException("Invalid number '" + sValue + "' : " + e);
		}
	}

	public int readInt(int iParentNode, String sName) throws XMLException {
		return readInt(iParentNode, sName, 0);
	}

	public boolean readBoolean(int iParentNode, String sName, boolean bDefault) throws XMLException {
		String sValue = readString(iParentNode, sName, null);
		
		if (sValue == null || sValue.equals("")) {
			return bDefault;
		}
		
		sValue = sValue.toLowerCase();
		
		if (sValue.equals("true") || sValue.equals("on") || sValue.equals("1")) {
			return true;
		} 
		else if (sValue.equals("false") || sValue.equals("off") || sValue.equals("0")) {
			return false;
		}
		
		return bDefault;
	}

	public boolean readBoolean(int iParentNode, String sName) throws XMLException {
		return readBoolean(iParentNode, sName, false);
	}
	
	public int readNode(int iParentNode, String sName, int iDefaultNode) throws XMLException {
		int iElemNode = findNode(iParentNode, sName, false);
		
		return iElemNode != 0 ? iElemNode : iDefaultNode;
	}
	
	public int readNode(int iParentNode, String sName) throws XMLException {
		return readNode(iParentNode, sName, 0);
	}
	
	protected String getValue(int iParentNode, String sName) throws XMLException {
		String sValue;
		int iElemNode = findNode(iParentNode, sName, false);
		int nPos;
		
		// If this is a path expression, get the last node name.
		if ((nPos = sName.indexOf('/')) > 0) {
			// Yes it is, get the next subtree name and adjust the current element name.
			sName = sName.substring(nPos + 1);
		}
		
		if (sName.startsWith("@")) {
			// Return the attirbute value.
			sName = sName.substring(1);
			sValue = Node.getAttribute(iElemNode, sName);
		} else {
			// This the node contents.
			sValue = Node.getData(iElemNode);
		}
		
		if (sValue != null && sValue.equals("")) {
			return null;
		}
		
		return sValue;
	}
	
	protected int setValue(int iParentNode, String sName, String sValue) throws XMLException  {
		if (sValue == null) {
			sValue = "";
		}
		
		int iElemNode = findNode(iParentNode, sName, true);
		int nPos;
		
		// If this is a path expression, get the last node name.
		if ((nPos = sName.indexOf('/')) > 0) {
			// Yes it is, get the next subtree name and adjust the current element name.
			sName = sName.substring(nPos + 1);
		}
				
		if (sName.indexOf('@') != -1) {
			// Get the attribute name and set the attribute in the node.
			sName = sName.substring(1);
			Node.setAttribute(iElemNode, sName, sValue);
		} else {
			// Try to get the element text node.
			int iTextNode = Node.getFirstDataNode(iElemNode);
			
			if (iTextNode == 0) {
				// Text node was not set, so create a new one.
				iTextNode = dDoc.createText(sValue, iElemNode);
			} else {
				// Set the text node contents
				Node.setData(iTextNode, sValue);
			}
		}		
		
		return iElemNode;
	}
	
	protected int findNode(int iParentNode, String sElementName, boolean bCreate) throws XMLException {
		// Check the element name type.
		if (sElementName.startsWith("@")) {
			// This is an attribute, so return the node.
			return iParentNode;
		} 
		
		// Element is a node, so we must find it under the current node. 
		
		int nPos;
		String sSubName = null;
		
		// Check if this is a path expression.
		if ((nPos = sElementName.indexOf('/')) > 0) {
			// Yes it is, get the next subtree name and adjust the current element name.
			sSubName = sElementName.substring(nPos + 1);
			sElementName = sElementName.substring(0, nPos);
		}
		
		// Check for wildcards
		if (sElementName.equals("*")) {
			return Node.getNumChildren(iParentNode) > 0 ? Node.getFirstChild(iParentNode) : 0;
		}
		
		int iSubNode;
		
		if (! sElementName.equals(".")) {
			// Find the node corresponding to element name.
			iSubNode = Find.firstMatch(iParentNode, "<" + Node.getName(iParentNode) + "><" + sElementName + ">");
		} else {
			// The node is the current node.
			iSubNode = iParentNode;
		}
		
		// Check if it was already there.
		if (iSubNode == 0) {
			// The sub-node was not found.
			if (! bCreate) {
				// We are loading, and cannot create the new one.
				throw new XMLException("Node " + sElementName + " was not found.");
			}
			
			// Create a new node.
			iSubNode = dDoc.createElement(sElementName, iParentNode);
		}
		
		// If we have subnodes to search, do it recursively
		if (sSubName != null) {
			return findNode(iSubNode, sSubName, bCreate);
		}
		
		// This is the last node to be found, so return it.
		return iSubNode;
	}
	
	public int createNode(int iParentNode, String sElementName) throws XMLException  {
		return findNode(iParentNode, sElementName, true);
	}
	
	public int findNode(int iParentNode, String sElementName) throws XMLException  {
		return findNode(iParentNode, sElementName, false);
	}
	
	public String readString(int iParentNode, String sName, String sDefaultValue)  throws XMLException {
		String sValue = getValue(iParentNode, sName);
		
		return sValue != null ? sValue : sDefaultValue;
	}
	
	public int writeString(int iParentNode, String sName, String sValue) throws XMLException {
		if (bIsLoading) {
			throw new IllegalStateException("Serializer is in load mode.");
		}
		
		if (dDoc == null) {
			throw new IllegalStateException("Document not set.");
		}
		
		return setValue(iParentNode, sName, sValue);
	}
	
	public int writeInt(int iParentNode, String sName, int iValue) throws XMLException {
		return writeString(iParentNode, sName, "" + iValue);
	}

	public int writeBoolean(int iParentNode, String sName, boolean bValue) throws XMLException {
		return writeString(iParentNode, sName, bValue ? "1" : "0");
	}
	
	public int writeNode(int iParentNode, String sName, int iNode, boolean bCloneNode) throws XMLException {
		// Find the right node.
		int iElemNode = findNode(iParentNode, sName, true);
		
		// Clone the node to be added if necessary.
		if (bCloneNode) {
			iNode = Node.clone(iNode, true);
		}
		
		Node.appendToChildren(iNode, iElemNode);
		
		return iElemNode;
	}
	
	public int writeNode(int iParentNode, String sName, String sNodeXml) throws XMLException {
		// Find the right node.
		int iElemNode = findNode(iParentNode, sName, true);
		
		if (sNodeXml != null && sNodeXml.length() > 0) {
		    int iNode;
		    
		    try {
                iNode = dDoc.parseString(sNodeXml);
            } catch (UnsupportedEncodingException e) {
                throw new XMLException("XML parsing failed : " + e);
            }
		
		    Node.appendToChildren(iNode, iElemNode);
		}

		return iElemNode;
	}	
	
	public String serialize(int iParentNode, String sName, String sValue) throws XMLException {
		if (bIsLoading) {
			return readString(iParentNode, sName);
		} else {
			writeString(iParentNode, sName, sValue);
			return sValue;
		}
	}
	
	public boolean isLoading() {
		return bIsLoading;
	}
	
	public boolean isStoring() {
		return ! bIsLoading;
	}
}
