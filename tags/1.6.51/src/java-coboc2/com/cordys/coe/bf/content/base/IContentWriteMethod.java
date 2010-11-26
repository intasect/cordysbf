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

import java.util.List;

import com.cordys.coe.bf.exception.BFException;

/**
 * Interface for writing content either to ECX or filesystem. This method is
 * always tied to a specific content source (in this case it is the
 * destination),  e.g. to a certain SOAP method.
 *
 * @author mpoyhone
 */
public interface IContentWriteMethod extends IContentHandlingMethod
{
    /**
     * Sets the version parameter, e.g. isv or organization for XMLStore.
     * This is content source dependent.
     * @param sVersion Version parameters.
     */
    public void setContentVersion(String sVersion);
    
    /**
     * Deletes the given object from the destination.   This method may call
     * the update method to perform this operation.
     *
     * @param cObject Object to be deleted.
     * @return Old version of the object or <code>null</code> if this method
     *         does support returning it.
     * @throws BFException Thrown if the operation failed.
     */
    public IContent deleteObject(IContent cObject)
                      throws BFException;

    /**
     * Deletes the object pointed by the given handle  from the destination.
     * The real object might first have to be fetched from the destination.
     * This method may call the update method to perform this operation.
     *
     * @param chHandle Handle of the object to be deleted.
     * @return Old version of the object or <code>null</code> if this method
     *         does support returning it.
     * @throws BFException Thrown if the operation failed.
     */
    public IContent deleteObject(IContentHandle chHandle)
                      throws BFException;

    /**
     * Inserts the object into the destination. It is possible to query for the
     * old version of the object and turn this operation into an update if it
     * existed. This method is  the main method intended to be used for this
     * interface for object insertions because the caller usually does not
     * care about the insert/update functionality.  This method may call the
     * update method to perform this operation.
     *
     * @param cObject Object to be inserted.
     * @param bCheckForOld If true, the destination is first queried for an old
     *        version and if it existed this operation is turned into an
     *        update operation.
     *
     * @return New version of the object or <code>null</code> if this method
     *         does support returning it.
     *
     * @throws BFException Thrown if the operation failed
     */
    public IContent insertObject(IContent cObject, boolean bCheckForOld) throws BFException;

    /**
     * Implements the standard Cordys tuple/old/new protocol for insert, update
     * and delete methods. The underlying SOAP method may not support this
     * protocol, but this method must implement the functionality and possibly
     * call multiple methods in order to achieve this. For filesystem methods
     * this method behaves just as an insert.
     *
     * @param cOldObject Old object for update and delete.
     * @param cNewObject New object for update and insert.
     *
     * @return New version of the object or <code>null</code> if this method
     *         does support returning it.
     *
     * @throws BFException Thrown if the operation failed.
     */
    public IContent updateObject(IContent cOldObject, IContent cNewObject)
                          throws BFException;
    
    /**
     * Implements the standard Cordys tuple/old/new protocol for insert, update
     * and delete methods. The underlying SOAP method may not support this
     * protocol, but this method must implement the functionality and possibly
     * call multiple methods in order to achieve this. For filesystem methods
     * this method behaves just as an insert.
     *
     * @param ctTuple Tuple object containing the old and new versions.
     *
     * @return New version of the object or <code>null</code> if this method
     *         does support returning it.
     *
     * @throws BFException Thrown if the operation failed.
     */
    public IContent updateObject(ContentTuple ctTuple)
                          throws BFException;    
    
    /**
     * Same as <code>updateObject</code> but accepts a list of tuples to be 
     * updated as a single operation. 
     *
     * @param lTupleList A list of tuple objects.
     *
     * @return A list containig new versions of the objects or null if this method
     *         does support returning the new versions. The returned list will always have
     *         the same size as the tuple list.
     *
     * @throws BFException Thrown if the operation failed.
     */
    public List<IContent> updateObjects(List<ContentTuple> lTupleList)
                          throws BFException;      
}
