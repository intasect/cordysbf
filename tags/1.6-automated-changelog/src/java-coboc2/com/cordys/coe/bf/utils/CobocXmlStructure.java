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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;

/**
 * A specific XmlStructure that is used to keep track CoBOC objects ID's and keys
 * in the XML. This class creates CoBOC handles from the elements that are pointed
 * by given XPath expressions and the handles can be queried and updated in a standard
 * way.
 *
 * @author mpoyhone
 */
public class CobocXmlStructure extends XmlStructure
{
    /**
     * Contains all created handles and mappings from handles to the correct nodes.
     * This should possibly handle attributes in the future.
     */
    private List<ContentHandleStruct> lHandleList = new LinkedList<ContentHandleStruct>();
    
    /**
     * @see java.lang.Object#clone()
     */
    public CobocXmlStructure clone() {
        CobocXmlStructure xdRes = new CobocXmlStructure();
        
        xdRes.copyFrom(this);
        
        // TODO: The nodes still point to the old XML!!
        if (lHandleList != null) {
            xdRes.lHandleList = new ArrayList<ContentHandleStruct>(lHandleList.size());
            
            for (ContentHandleStruct cshStruct : lHandleList)
            {
                xdRes.lHandleList.add(new ContentHandleStruct(cshStruct.cchHandle, cshStruct.oNode));
            }        
        }
        
        return xdRes;
    }
    
    /**
     * Creates handles for the nodes pointed by the XPath.  
     * @param axpPath XPath expression.
     * @param ctHandleContentType Content type for the created content handles.
     * @param bXmlHasObjectIds If <code>true</code> the node values are treated as object ID's, otherwise as keys.
     * @param bInitialize If <code>true</code> this initialized the handle list.
     */
    public void createHandles(AXIOMXPath axpPath, EContentType ctHandleContentType, boolean bXmlHasObjectIds, boolean bInitialize) throws BFException {
        if (bInitialize) {
            lHandleList.clear();
        }
        
        List<Object> lNodes = selectNodesOrAttributes(axpPath);
        
        for (Object oObj : lNodes) {
            CobocContentHandle cchHandle = new CobocContentHandle(ctHandleContentType);
            String sNodeValue = null;
            
            if (oObj instanceof OMNode) {
                OMNode onNode = (OMNode) oObj;
                
                sNodeValue = AxiomUtils.getNodeText(onNode);
            } else if (oObj instanceof OMAttribute) { 
                OMAttribute oaAttrib = (OMAttribute) oObj;
                
                sNodeValue = oaAttrib.getAttributeValue();
            }
            
            if (sNodeValue == null) {
                throw new BFException("CobocXmlStructure: No value found for node/attribute " + oObj);
            }
            
            if (bXmlHasObjectIds) {
                cchHandle.setObjectId(sNodeValue);
            } else {
                cchHandle.setKey(sNodeValue);
            }
            
            lHandleList.add(new ContentHandleStruct(cchHandle, oObj));
        }
    }
    
    /**
     * Returns the handles set for this structure.
     * @return Handles.
     */
    public Collection<CobocContentHandle> getHandles(boolean bIncludeEmpty) {
        if (lHandleList == null) {
            return Collections.emptyList();
        }
        
        List<CobocContentHandle> lResList = new ArrayList<CobocContentHandle>(lHandleList.size());
        
        for (ContentHandleStruct cshStruct : lHandleList)
        {
            if (bIncludeEmpty || cshStruct.cchHandle.isSet()) {
                lResList.add(cshStruct.cchHandle);
            }
        }
        
        return lResList;
    }
    
    /**
     * Updates the objects ID's or keys in the handles.
     * @param bcContext Context where the object ID's or keys are read from.
     * @param bUpdateIds If <code>true</code> object ID's are updated, otherwise keys.
     * @throws BFException Thrown if some of the content objects were not CoBOC objects.
     */
    public void updateHandles(BFContext bcContext, boolean bUpdateIds) throws BFException {
        if (lHandleList == null) {
            return;
        }
        
        for (ContentHandleStruct cshStruct : lHandleList)
        {
            IContent cTmp = bcContext.findContent(cshStruct.cchHandle);
            
            if (cTmp == null) {
                continue;
            }
            
            if (! (cTmp instanceof CobocContentBase)) {
                throw new BFException("Content object '" + cTmp.getHandle() + "' is not of type CobocContentBase");
            }
            
            String sValue = bUpdateIds ? ((CobocContentBase) cTmp).getObjectId() : ((CobocContentBase) cTmp).getKey();
            
            if (sValue == null) {
                continue;
            }
            
            if (bUpdateIds) {
                cshStruct.cchHandle.setObjectId(sValue);
            } else {
                cshStruct.cchHandle.setKey(sValue);
            }
        }
    }
    
    /**
     * Updates the XML element values from the handles either as object ID's or keys. 
     * @param bSetAsIds If <code>true</code> the values are sets as object ID's otherwise as keys.
     */
    public void updateHandleXmlValues(boolean bSetAsIds) {
        if (lHandleList == null) {
            return;
        }
        
        for (ContentHandleStruct cshStruct : lHandleList)
        {
            String sValue = bSetAsIds ? cshStruct.cchHandle.getObjectId() : cshStruct.cchHandle.getKey();
            
            if (sValue == null) {
                sValue = "";
            }
            
            AxiomUtils.setNodeText(cshStruct.oNode, sValue);
        }
    }
    

    /**
     * Clears the handle array.
     */
    public void clearHandles()
    {
        if (lHandleList != null) {
            lHandleList.clear();
        }
    }   
    
    /**
     * A struct class to keep the content handle and the node together.
     *
     * @author mpoyhone
     */
    private class ContentHandleStruct {
        /**
         * Handle
         */
        CobocContentHandle cchHandle;
        /**
         * Node/attribute
         */
        Object oNode;

        /**
         * Constructor for ContentHandleStruct
         * @param cchHandle Handle.
         * @param onNode Node.
         */
        ContentHandleStruct(CobocContentHandle cchHandle, Object oNode) {
            this.cchHandle = cchHandle;
            this.oNode = oNode;
        }
    }
}
