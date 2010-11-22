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
package com.cordys.coe.ant.coboc.content.folders;

import com.cordys.coe.ant.coboc.content.CoBOCContext;
import com.cordys.coe.ant.coboc.content.CoBOCObject;
import com.cordys.coe.ant.coboc.content.ContentException;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Class for reading and writing CoBOC and file mapping objects. The Mapping
 * class is the container for individual mappings and this class contains the
 * implementations.
 *
 * @author mpoyhone
 */
public class MappingHandler extends FolderObjectHandler
{
    /**
     * Creates a new MappingHandler instance.
     *
     * @param ccContext The CoBOCContext that is used when storing and accesing
     *        CoBOC objects.
     */
    public MappingHandler(CoBOCContext ccContext)
    {
        super(ccContext);
    }

    /**
     * Returns the filter attribute value for GetCollection and GetXMLObject.
     *
     * @return The CoBOC entity filter.
     */
    public String getCoBOCEntityFilter()
    {
        return "/MAP";
    }

    /**
     * Returns the entity attribute value for GetCollection and GetXMLObject.
     * Valid values are "entity" or "instance".
     *
     * @return The CoBOC entity filter.
     */
    public String getCoBOCEntityType()
    {
        return "instance";
    }

    /**
     * Checks if the read file was of correct type and returns the tuple XML
     * structure.
     *
     * @param iFileNode The XML structure that was read from the file.
     *
     * @return None-zero, if this class can handle objects of this type,
     *         otherwise zero.
     */
    public int checkXmlFileType(int iFileNode)
    {
        return Mapping.isMapping(iFileNode) ? iFileNode : 0;
    }

    /**
     * Finds an object from the context by the entity ID or creates a new one,
     * if bCreate parameter is true.
     *
     * @param sEntityId The entity ID that is used in the search.
     * @param bCreate If true, a new object is created.
     *
     * @return The found or created object, or null if no object was found.
     */
    public CoBOCObject findObjectById(String sEntityId, boolean bCreate)
    {
        return Mapping.findMappingById(ccContext, sEntityId, bCreate);
    }

    /**
     * Finds an object from the context by the entity key or creates a new one,
     * if bCreate parameter is true.
     *
     * @param sEntityPathKey The entity ID that is used in the search.
     * @param bCreate If true, a new object is created.
     *
     * @return The found or created object, or null if no object was found.
     */
    public CoBOCObject findObjectByPathKey(String sEntityPathKey,
                                           boolean bCreate)
    {
        return Mapping.findMappingByPathKey(ccContext, sEntityPathKey, bCreate);
    }

    /**
     * Returns the bit mask that identifies the types of objects that are
     * fetched from folders. The mask values are defined in  FolderCallback
     * class.
     *
     * @return The bit mask indicating the types of objects this handler
     *         fetched from folders.
     */
    protected int getFolderObjectMask()
    {
        return FolderCallback.MASK_MAPPING;
    }

    /**
     * @see com.cordys.coe.ant.coboc.content.folders.FolderObjectHandler#modifyUpdateRequest(int,
     *      int)
     */
    protected int modifyUpdateRequest(int xUpdateRequestNode, int xOldObjectNode)
                               throws ContentException
    {
        //Set the template parameter to a mapping type.
        Node.setAttribute(xUpdateRequestNode, "template", "/MAP");        
    	
        if (xOldObjectNode == 0)
        {
            return xUpdateRequestNode;
        }

		// Copy the last modified field.        
        String sLastModified = Node.getAttribute(xOldObjectNode,
                                                 "lastModified");

        if (sLastModified == null)
        {
            throw new ContentException("Attibute lastModified not found from old node.");
        }

        Node.setAttribute(xUpdateRequestNode, "lastModified", sLastModified);

        // Copy the ENTITY_ID element.
        int xEntityId = Find.firstMatch(xOldObjectNode, "?<ENTITY><ENTITY_ID>");

        if (xEntityId == 0)
        {
            throw new ContentException("ENTITY_ID not found from the old node.");
        }

        int xUpdateOld = Node.createElement("old", xUpdateRequestNode);
        int xUpdateEntity = Node.createElement("ENTITY", xUpdateOld);

        Node.appendToChildren(Node.clone(xEntityId, true), xUpdateEntity);
        
        return xUpdateRequestNode;
    }
}
