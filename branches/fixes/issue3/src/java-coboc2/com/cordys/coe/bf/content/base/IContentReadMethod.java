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

import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;

/**
 * Interface for reading content either from ECX or filesystem. This method is
 * always tied to a specific source, e.g. to a certain SOAP method.
 *  
 * @author mpoyhone
 */
public interface IContentReadMethod extends IContentHandlingMethod {
    /**
     * Sets the version parameter, e.g. isv or organization for XMLStore.
     * This is content source dependent.
     * @param sVersion Version parameters.
     */
    public void setContentVersion(String sVersion);
    
	/**
	 * Fetches all found top-level objects of the given type. Some content types might not
     * be accessible without a parent object, so for those types a <code>BFException</code> will 
     * be thrown.
     * 
     * Some list methods return only the object ID and name and
	 * if all object data should be returned by this method, then parameter 'bReadFully'
	 * must be set.  
	 * 
	 * Note! If an object has references to other objects, those references are
	 * be resolved at this point (e.g. refence content handle only has the ID set).
	 * 
	 * @param ctType Type of the content objects that will be read.
	 * @param bReadFully If <code>true</code> then objects are guaranteed to contain all data available, otherwise only the basic infomation can be set.  
	 * @return A list of all read objects. 
	 * @throws BFException Thrown if the operation failed. 
	 */
	public List<IContent> readObjects(EContentType ctType, boolean bReadFully) throws BFException;
	
    /**
     * Fetches all found sub-objects for the given parent object handle. 
     * 
     * Some list methods return only the object ID and name and
     * if all object data should be returned by this method, then parameter 'bReadFully'
     * must be set.  
     * 
     * Note! If an object has references to other objects, those references are
     * be resolved at this point (e.g. refence content handle only has the ID set).
     * 
     * @param chParentHandle Content handle of the parent object. 
     * @param bReadFully If <code>true</code> then objects are guaranteed to contain all data available, otherwise only the basic infomation can be set.  
     * @param bRecursive If <code>true</code> all objects under the given handle are returned. Not all content types have hierarchial structure so for those this is ignored.
     * @return A list of all read objects. 
     * @throws BFException Thrown if the operation failed. 
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive) throws BFException;    

    /**
     * Reads a single object with the given handle. The handle must be of right type for this
     * operation to succeed.
     * @param hHandle Handle of the object that will be returned.
     * @return Read object.
     * @throws BFException Thrown if the operation failed.
     */
	public IContent readObject(IContentHandle hHandle) throws BFException;
    
    /**
     * Reads a single object from the given XML. The XML must be in the format that this method supports.
     * 
     * @param xsSource XML source that contains the object XML.
     * @return Read object.
     * @throws BFException Thrown if the operation failed.
     */
    public List<IContent> readObjectsFromXml(IXmlSource xsSource) throws BFException;
}
