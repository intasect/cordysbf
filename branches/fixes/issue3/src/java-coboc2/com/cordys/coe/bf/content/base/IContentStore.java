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
package com.cordys.coe.bf.content.base;

import java.util.Collection;

import com.cordys.coe.bf.exception.BFException;

/**
 * Defines an interface for a store that is used to contain content 
 * the specific content objects. The idea of this interface is
 * that content objects can have different ways of indexing (e.g. CoBOC
 * folder objects need to be looked by either by the key or the object ID).
 *
 * @author mpoyhone
 */
public interface IContentStore
{
    /**
     * Insert the content object in the store.
     * @param cContent Object to be inserted.
     */
    public void insertObject(IContent cContent);
    
    /**
     * Removed the object pointed by the handle from the store.
     * @param chHandle Object handle.
     * @return The removed object or <code>null</code> if none was found.
     */
    public IContent removeObject(IContentHandle chHandle);

    /**
     * Finds an object from the content store.
     * @param chHandle Object handle that points to he object to be located.
     * @return Found object or <code>null</code> if no object was found.
     */
    public IContent findObject(IContentHandle chHandle);
    
    /**
     * Renames contents handles in all objects return the from handle as a referenced content
     * with the contents of the new handle.
     * @param chFromHandle From handle value.
     * @param chToHandle To handle value.
     * @throws BFException Throws if renaming failed.
     */
    public void renameObjectReferences(IContentHandle chFromHandle, IContentHandle chToHandle) throws BFException;
    
    /**
     * Returns all objects in this store.
     * @return A collection object containing all objects.
     */
    public Collection<IContent> getObjects();
    
    /**
     * Removes all objects from this store.
     */
    public void clear();
}
