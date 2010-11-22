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

import org.w3c.dom.Node;

/**
 * TODO Describe the class.
 *
 * @author mpoyhone
 */
public class DOMUtils {
	public static Node findChildNode(Node nParent, String sLocalName) {
		if (nParent == null || sLocalName == null) {
			return null;
		}
		
		Node nChild = nParent.getFirstChild();
		
		while (nChild != null) {
			if (nChild.getNodeType() == Node.ELEMENT_NODE &&
				sLocalName.equals(nChild.getLocalName())) {
				return nChild;
			}
			
			nChild = nChild.getNextSibling();
		}
		
		return null;
	}
	
	public static Node findChildFirstElement(Node nParent) {
		if (nParent == null) {
			return null;
		}
		
		Node nChild = nParent.getFirstChild();
		
		while (nChild != null) {
			if (nChild.getNodeType() == Node.ELEMENT_NODE) {
				return nChild;
			}
			
			nChild = nChild.getNextSibling();
		}
		
		return null;
	}	
	
	public static String getNodeText(Node nNode, String sDefault) {
		if (nNode == null) {
			return sDefault;
		}
		
		Node nChild = nNode.getFirstChild();
		String sRes = null;
		
		while (nChild != null) {
			switch (nChild.getNodeType()) {
			case Node.TEXT_NODE :
			case Node.CDATA_SECTION_NODE :
				sRes += nNode.getNodeValue();
				break;
			}
			
			nChild = nChild.getNextSibling();
		}
		
		return sRes != null ? sRes : sDefault;
	}	
}
