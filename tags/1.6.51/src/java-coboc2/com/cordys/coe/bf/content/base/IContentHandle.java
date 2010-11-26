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
import com.cordys.coe.bf.exception.BFException;

/**
 * Interface for uniquely identifying a specific content.
 * Implementing classes will contains the actual ID's, e.g.
 * Coboc object ID and path key or method set LDAP DN. 
 *
 * @author mpoyhone
 */
public interface IContentHandle {
    /**
     * Returns the content type for this handle.
     * @return Content type enumeration value.
     */
	public EContentType getContentType();
    
    /**
     * Returns a unique content identifier within the content type.
     * The ID should also be unique over the similar content types, e.g.
     * CoBOC objects where the ID is the CoBOC object ID.
     * @return Unique ID for this content object.
     */
    public String getContentId();
    
    /**
     * Clones this content handle.
     * @return Cloned handle.
     */
    public IContentHandle cloneHandle();
    
    /**
     * Copies the information from the source handle to this handle.
     * The handle types must be compatible.
     * @param chSource Source handle.
     * @throws BFException Thrown if the handles are not compatible.
     */
    public void copyFrom(IContentHandle chSource) throws BFException;
    
    /**
     * Returns <code>true</code> if the two content handles refer to the same content.
     * @param chOther Other handle
     * @return <code>true</code> if the two content handles refer to the same content.
     */
    public boolean equals(IContentHandle chOther);
    
    /**
     * Returns a name of this handle that can be used for log messages.
     * @return Log name of this handle.
     */
    public String getLogName();
    
    /**
     * Returns <code>true</code> if this handle contains a valid value.
     * @return <code>true</code> if this handle contains a valid value.
     */
    public boolean isSet();
}
