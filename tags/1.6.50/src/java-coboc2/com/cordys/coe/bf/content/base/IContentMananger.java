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

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.exception.BFException;

/**
 * Defines a main class for handling of a specific type of content. 
 * This class is also reponsible of registering itself to the content
 * sources as well as loading all the necessary SOAP end object conversion templates.
 * @author mpoyhone
 */
public interface IContentMananger
{
    /**
     * Called when the content manager is created and needs to be initialized. 
     * @param bcContext BFContext that this manager is to be initislized to.
     *  @throws BFException Thrown if the operation failed.
     */
    public void initialize(BFContext bcContext) throws BFException;
    
    /**
     * Called when the content manager is being removed from the context and needs to be uninitialized. 
     * @param bcContext BFContext that this manager is being removed from.
     * @throws BFException Thrown if the operation failed.
     */    
    public void uninitialize(BFContext bcContext)  throws BFException;
    
    /**
     * Reads all content from the given source into the BFContext.
     * @param csSource Content source from where the content is read.
     * @throws BFException Thrown if the operation failed.
     */
    public void readContentFromSource(IContentSource csSource) throws BFException;
    
    /**
     * Reads all content from the given XML source into the BFContext. The manager must be able
     * figure out the content type from the XML itself.
     * 
     * @param csContentSource Content source used to get the right read methods.
     * @param xsXmlSource XML source from where the content is read.
     * @throws BFException Thrown if the operation failed.
     */
    public void readContentFromXml(IContentSource csContentSource, IXmlSource xsXmlSource) throws BFException;    
    
    /**
     * Writes all content to the given source from the BFContext.
     * @param csDestination Content source where content is written to.
     * @throws BFException Thrown if the operation failed.
     */    
    public void writeContentToSource(IContentSource csDestination) throws BFException;
    
    /**
     * Deletes all content from the given source.
     * @param csSource Content source where content is deleted from.
     * @throws BFException Thrown if the operation failed.
     */    
    public void deleteContentFromSource(IContentSource csSource) throws BFException;
    
    /**
     * Transfrers all content from the given source to the given destination.
     * @param csSource Content source from where the content is read.
     * @param csDestination Content source where content is written to.
     * @param bLeaveContentInContext If <code>true</code> the content is left in the BFContext.
     * @throws BFException
     */
    public void transfrerContent(IContentSource csSource, IContentSource csDestination, boolean bLeaveContentInContext) throws BFException;
}
