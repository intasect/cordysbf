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
package com.cordys.coe.ant.coboc.content;

import com.cordys.tools.ant.cm.FilterSet;

/**
 * An interface for all CoBOC objects
 *
 * @author mpoyhone
 */
public interface CoBOCObjectHandler
{
    /**
     * Returns the filter attribute value for GetCollection and GetXMLObject.
     *
     * @return The CoBOC entity filter.
     */
    public String getCoBOCEntityFilter();

    /**
     * Returns the entity attribute value for GetCollection and GetXMLObject.
     * Valid values are "entity" or "instance".
     *
     * @return The CoBOC entity filter.
     */
    public String getCoBOCEntityType();

    /**
     * Sets the filter set.
     *
     * @param fsFilterSet The filter set to be set for this handler.
     */
    public void setFilterSet(FilterSet fsFilterSet);

    /**
     * Returns the filter set.
     *
     * @return The filter set used by this handler.
     */
    public FilterSet getFilterSet();

    /**
     * Checks if the read file was of correct type and returns the tuple XML
     * structure.
     *
     * @param iFileNode The XML structure that was read from the file.
     *
     * @return None-zero, if this class can handle objects of this type,
     *         otherwise zero.
     */
    public int checkXmlFileType(int iFileNode);

    /**
     * Called when the this handler's objects must be read from the ECX.
     *
     * @throws ContentException
     */
    public void convertObjectsFromEcx()
                               throws ContentException;

    /**
     * Called when the this handler's objects must be read from the file
     * system.
     *
     * @throws ContentException Thrown if the operation failed.
     */
    public void convertObjectsFromFiles()
                                 throws ContentException;

    /**
     * Called when the this handler's objects must be written to the ECX.
     *
     * @throws ContentException Thrown if the operation failed.
     */
    public void convertObjectsToEcx()
                             throws ContentException;

    /**
     * Called when the this handler's objects must be written to the file
     * system.
     *
     * @throws ContentException Thrown if the operation failed.
     */
    public void convertObjectsToFiles()
                               throws ContentException;

    /**
     * Called when objects need to be deleted from the ECX.
     *
     * @throws ContentException Thrown if the operation failed.
     */
    public void deleteObjectsFromEcx()
                              throws ContentException;

    /**
     * Finds an object from the context by the entity ID or creates a new one,
     * if bCreate parameter is true.
     *
     * @param sEntityId The entity ID that is used in the search.
     * @param bCreate If true, a new object is created.
     *
     * @return The found or created object, or null if no object was found.
     */
    public CoBOCObject findObjectById(String sEntityId, boolean bCreate);

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
                                           boolean bCreate);
}
