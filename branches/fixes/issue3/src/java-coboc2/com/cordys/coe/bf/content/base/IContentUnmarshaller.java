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



import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;

/**
 * Interface for reading a content object from an input readed. At this
 * moment only unmarshaling from textual format is supported.
 *
 * @author mpoyhone
 */
public interface IContentUnmarshaller {
    /**
	 * Reads the content from the input stream and returns a content object based on this content. 
	 * @param sSrc Contains the source from where the object should be unmarshalled. 
	 * @return Created content object.
	 * @throws BFException Thrown if the object could not be created.
	 */
	public IContent unmarshalObject(IXmlSource sSrc) throws BFException;
    
    /**
     * Reads the content from the input stream and returns a content object based on this content. 
     * @param sSrc Contains the source from where the object should be unmarshalled.
     * @param bpParameters Unmarshaller specific parameters or <code>null</code> if none is needed. 
     * @return Created content object.
     * @throws BFException Thrown if the object could not be created.
     */
    public IContent unmarshalObject(IXmlSource sSrc, BindingParameters bpParameters) throws BFException;    
}
