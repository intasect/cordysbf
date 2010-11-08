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

import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.databind.ObjectConverter;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;


/**
 * A class for handling all content types for a specific content source (ECX or file system). 
 *
 * @author mpoyhone
 */
public interface IContentSource {
    /**
     * Return the type of this source.
     * @return Content source type enumeration.
     */
    public EContentSourceType getType();
    
	/**
	 * Returns the content read method for the given content type.
	 * 
	 * @param ctType Content type in question.
	 * @return Read method object configured for this source or null if no was set.
	 * @throws BFException Thrown if the object creation failed or the content type is not supported by this source.
	 */
	public IContentReadMethod getReadMethod(EContentType ctType) throws BFException;
    
    /**
     * Returns the content write method for the given content type.
     * 
     * @param ctType Content type in question.
     * @return Write method object configured for this source or null if no was set.
     * @throws BFException Thrown if the object creation failed or the content type is not supported by this source.
     */
    public IContentWriteMethod getWriteMethod(EContentType ctType) throws BFException;    
	
    /**
     * Returns a configured content unmarshaller for the given read method.
     * @param ctType Content type that the unmarshaller must support.
     * @param sSubtype Content specific subtype ID or <code>null</code> if not applicable.
     * @param sVersion Content version ID or <code>null</code> if not applicable.
     * @return Configured content unmarshaller or <code>null</code> if there is no unmarshaller configured for the given parameters.
     * @throws BFException Thrown if the arguments are illegal.
     */
	public IContentUnmarshaller getContentUnmarshaller(EContentType ctType, String sSubtype, String sVersion) throws BFException;
    
    /**
     * Returns a configured content marshaller for the given write method.
     * @param ctType Content type that the marshaller must support.
     * @param sSubtype Content specific subtype ID or <code>null</code> if not applicable.
     * @param sVersion Content version ID or <code>null</code> if not applicable.
     * @return Configured content marshaller or <code>null</code> if there is no marshaller configured for the given parameters.
     * @throws BFException Thrown if the arguments are illegal.
     */
    public IContentMarshaller getContentMarshaller(EContentType ctType, String sSubtype, String sVersion) throws BFException;    
    
    /**
     * Returns a content filter for the given type.
     * @param ctType Content type.
     * @return Content filter or <code>null</code> if none was set.
     */
    public IContentFilter getContentFilter(EContentType ctType);
    
    /**
     * A helper method for checking content filtering.
     * @param ctType Content type.
     * @param sKey Content key.
     * @param bIsFolder <code>true</code> if the content is a folder.
     * @return <code>true</code> if the content is not filtered.
     */
    public boolean checkForAccess(EContentType ctType, String sKey, boolean bIsFolder);

    /**
     * Registers a read  method for the given content types for the given version.
     * @param version Version string.
     * @param types A list of content type to register this method to.
     * @param methodClass Implementation class.
     */
    public void registerContentReadMethod(String version, EContentType[] types, Class<IContentReadMethod> methodClass);
    
    /**
     * Registers a write  method for the given content types for the given version.
     * @param version Version string.
     * @param types A list of content type to register this method to.
     * @param methodClass Implementation class.
     */
    public void registerContentWriteMethod(String version, EContentType[] types, Class<IContentWriteMethod> methodClass);
    
    /**
     * Registers a binding template for this content source. 
     * @param ctType Content type for this template. 
     * @param sSubType A template subtype.
     * @param ocConverter ObjectConverted object that contains the marshalling and unmarshalling operations.
     */
    public void registerConversionTemplate(EContentType aCtType, String aSubType, String version, ObjectConverter aOcConverter);

    /**
     * Registers a SOAP request template for this content source. 
     * @param sTemplateId Template ID
     * @param srtTemplate Template
     */
    public void registerSoapMethodTemplate(String aTemplateId, String version, ISoapRequestTemplate aSrtTemplate)  throws BFException;

    /**
     * Determines the content type based on the given XML.
     * @param xsXml XML source containing the XML.
     * @return Determined content type or <code>null</code> if unknown.
     */
    public EContentType getContentTypeFromXml(IXmlSource xsXml) throws BFException;

    /**
     * Set a content filter for the given content type.
     * @param ctType Content type.
     * @param cfFilter Content filter object.
     */
    public void addContentFilter(EContentType ctType, IContentFilter cfFilter);
}
