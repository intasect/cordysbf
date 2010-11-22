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
package com.cordys.coe.bf.content.base.impl;

import com.cordys.coe.bf.content.base.IContentFilter;
import com.cordys.coe.bf.content.types.EContentType;

/**
 * Content filter that blocks all content.
 *
 * @author mpoyhone
 */
public class BlockContentFilter implements IContentFilter
{
    /**
     * @see com.cordys.coe.bf.content.base.IContentFilter#checkFolderAccess(java.lang.String)
     */
    public boolean checkFolderAccess(String sKey)
    {
        return false;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentFilter#checkItemAccess(java.lang.String, EContentType)
     */
    public boolean checkItemAccess(String sKey, EContentType contentType)
    {
        return false;
    }
}
