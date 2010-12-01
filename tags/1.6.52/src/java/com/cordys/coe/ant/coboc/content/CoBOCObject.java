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

import java.util.Iterator;

import com.eibus.xml.nom.Document;

/**
 * An interface for all CoBOC object types that defines the common interface
 * for importing and exporting between files and CoBOC.
 *
 * @author mpoyhone
 */
public interface CoBOCObject
{
    /**
     * Returns the iterator that can be used to access the other CoBOCObjects
     * that this object is dependent of.
     *
     * @return An iterator that returns CoBOCObject objects.
     */
    public Iterator<?> getDependencies();

    /**
     * Sets the entity ID for this element.
     *
     * @param sId The entity ID for this element.
     */
    public void setEntityID(String sId);

    /**
     * Returns the entity ID for this element.
     *
     * @return The entity ID for this element.
     */
    public String getEntityID();

    /**
     * Sets the path key for this element. Note that all object do not have
     * path keys.
     *
     * @param sPathKey The CoBOC path key.
     */
    public void setEntityPathKey(String sPathKey);

    /**
     * Returns the path key for this element.
     *
     * @return The CoBOC path key or null if this object does not support
     *         paths.
     */
    public String getEntityPathKey();

    /**
     * Sets the parent object. For folders and folder entities this is  the
     * parent folder.
     *
     * @param coParent The parent object.
     */
    public void setParentObject(CoBOCObject coParent);

    /**
     * Returns the parent object. For folders and folder entities this is  the
     * parent folder.
     *
     * @return The parent object.
     */
    public CoBOCObject getParentObject();

    /**
     * Adds a child object to this object. This is supported only for folders.
     *
     * @param coObject
     */
    public void addChildObject(CoBOCObject coObject);

    /**
     * Converts the ECX message structure to the internal representation.
     *
     * @param iNode The ECX message structure that needs to be parsed.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public void convertFromECX(int iNode)
                        throws ContentException;

    /**
     * Converts the file structure to the internal representation.
     *
     * @param iNode The file XML structure that needs to be parsed.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public void convertFromFile(int iNode)
                         throws ContentException;

    /**
     * Converts the content element to an XML structure than can be send to
     * ECX. The structure must contain tuple and new-tags.
     *
     * @param dDoc The XML document object used to create the resulting XML
     *        structure.
     *
     * @return The converted ECX message XML structure.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public int convertToECX(Document dDoc)
                     throws ContentException;

    /**
     * Converts the content element XML format that is written in the content
     * file.
     *
     * @param dDoc The XML document object used to create the resulting XML
     *        structure.
     *
     * @return The converted file XML structure.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public int convertToFile(Document dDoc)
                      throws ContentException;

    /**
     * Returns true if this object has been read succesfully either from CoBOC
     * or a file.
     *
     * @return True, if this object has been read succesfully either from CoBOC
     *         or a file, otherwise false.
     */
    public boolean hasContent();
}
