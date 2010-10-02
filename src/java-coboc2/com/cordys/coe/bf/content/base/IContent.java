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

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;

/**
 * Interface for all build frame work content types. 
 *
 * @author mpoyhone
 */
public interface IContent {
    /**
     * Sets the context to which this object belongs to.
     * @param bcContext Context to be set.
     */
    public void setContext(BFContext bcContext);
    /**
     * Returns the context to which this object belongs to.
     * @return Current context.
     */
    public BFContext getContext();
    
	/**
	 * Sets the unique ID handle for this object.
	 * @param hHandle Handle to be set.
	 */
	public void setHandle(IContentHandle hHandle);
	
	/**
	 * Returns the unique ID handle which is set for this object.
	 * @return Handle set for this object.
	 */
	public IContentHandle getHandle();
    
    /**
     * Returns the unique ID handle which is set for this object that contains
     * the ID that is applicaple for this content source.
     * @param csSource Content source in question. 
     * @return Handle set for this object.
     */
    public IContentHandle getHandleForContentSource(IContentSource csSource);    
	
	/**
	 * Returns the content type object.
	 * @return Content type object.
	 */
	public EContentType getType();
    
    /**
     * Returns the name of this object that will be used for logging and exceptions.
     * @return Log name of this object. Cannot be <code>null</code>.
     */
    public String getLogName();
    
    /**
     * Sets the new parent for this object.
     * @param cParent New parent object.
     */
    public void setParent(IContentHandle chParent);
    
    /**
     * Returns the parent object for this object.
     * @return Parent object.
     */
    public IContentHandle getParent();
    
    /**
     * Adds a child object handle to this object.
     * @param chChildHandle Handle of the child object.
     * @throws BFException Thrown if this content type does not support child objects.
     */
    public void addChild(IContentHandle chChildHandle) throws BFException;
    
    /**
     * Removes a child object handle from this object.
     * @param chChildHandle Handle of the child object.
     * @throws BFException Thrown if this content type does not support child objects.
     */
    public void removeChild(IContentHandle chChildHandle) throws BFException;    
	
	/**
	 * Returns all content objects that belong to this one, e.g. subobjects for a folder.
	 * @return
	 */
	public Collection<IContentHandle> getChildren();

    /**
     * Returns all content objects that this object has references to. This does
     * not include sub-content.
     * @return
     */
    public Collection<IContentHandle> getReferencedContent();
    
    /**
     * Called when this object should update references after the referred objects
     * have been updated from the given content source.
     * @param csSrc Update content source.
     * @throws BFException Thrown if the references could not be updated.
     */
    public void updateReferences(IContentSource csSrc) throws BFException;
    
    /**
     * Called when the object has been created and the data loaded from the given content source. 
     * @param csSrc Content source where the object was loaded from.
     * @throws BFException Thrown if the operation failed.
     */
    public void onLoad(IContentSource csSrc) throws BFException;

    /**
     * Called when an old version of the object has been loaded from the given content source.
     * This object should copy all the needed information from the old version so that 
     * the update with the old and this object will succeed.  
     * @param cOldObject The old version of this object.
     * @param csSrc Content source where the old object was loaded from.
     * @throws BFException Thrown if the operation failed.
     */
    public void updateFromOldVersion(IContent cOldObject, IContentSource csSrc) throws BFException;
    
    /**
     * Called after the object is inserted to the content source and this object needs to copy information
     * from the insertion result object.
     * @param cNewObject The new version of this object that was returned by the insert method..
     * @param csSrc Content source where the object was inserted to.
     * @throws BFException Thrown if the operation failed.
     */
    public void updateFromNewVersion(IContent cNewObject, IContentSource csSrc) throws BFException;    
    
    /**
     * Creates a version that can be written to the given content source. This method
     * can return the object itself if it the format does not change for this content source. 
     * @param csDest Content source where the returned object will be written to.
     * @return A version of this object that is compatible with the given content source.
     * @throws BFException Thrown if the object could not be created.
     */
    public IContent createWriteVersion(IContentSource csDest) throws BFException;
}
