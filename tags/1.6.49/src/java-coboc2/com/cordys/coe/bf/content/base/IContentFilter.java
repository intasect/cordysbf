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

/**
 * Interface for all filtering out content.
 *
 * @author mpoyhone
 */
public interface IContentFilter
{
    /**
     * Check if the given key is valid for a folder.
     * @param sKey Key to be checked.
     * @return <code>true</code> if the folder can be accepted.
     */
    public boolean checkFolderAccess(String sKey);
    
    /**
     * Check if the given key is valid for a folder item.
     * @param sKey Key to be checked.
     * @param contentType TODO
     * @return <code>true</code> if the item can be accepted.
     */
    public boolean checkItemAccess(String sKey, EContentType contentType);
}
