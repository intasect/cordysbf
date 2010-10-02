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

import java.io.IOException;

import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;

/**
 * Interface for writing a content object to an output writer. At this
 * moment only marshaling to textual format is supported.
 *
 * @author mpoyhone
 */
public interface IContentMarshaller {
    /**
	 * Writes the content from the object to the output stream.
	 * 
	 * @param cContent Content object to be written.
	 * @param dDest Object that contains the destination where this object will be written to.
	 * @throws IOException Thrown if writing of the output stream failed.
	 * @throws BFException Thrown if the object has an invalid state.
	 */
	public void marshalObject(IContent cContent, IXmlDestination dDest) throws IOException, BFException;
    
    /**
     * Writes the content from the object to the output stream.
     * 
     * @param cContent Content object to be written.
     * @param dDest Object that contains the destination where this object will be written to.
     * @param bpParameters Marshaller specific parameters or <code>null</code> if none are needed.
     * @throws IOException Thrown if writing of the output stream failed.
     * @throws BFException Thrown if the object has an invalid state.
     */
    public void marshalObject(IContent cContent, IXmlDestination dDest, BindingParameters bpParameters) throws IOException, BFException;    
}
