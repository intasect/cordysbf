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

import java.util.ArrayList;
import java.util.List;

import com.cordys.coe.bf.content.base.IContentFilter;
import com.cordys.coe.bf.content.types.EContentType;

/**
 * A simple implementation of the content filter interface.
 *
 * @author mpoyhone
 */
public class SimpleContentFilter implements IContentFilter
{
    protected List<String> lFilterList = new ArrayList<String>();
    protected List<Boolean> bExcludeList = new ArrayList<Boolean>();
    protected boolean bIncludeByDefault = false; 
    
    public void add(String sFilter, boolean bExclude) {
        lFilterList.add(sFilter);
        bExcludeList.add(bExclude);
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentFilter#checkFolderAccess(java.lang.String)
     */
    public boolean checkFolderAccess(String sKey)
    {
        int i = 0;
        
        for (String sItem : lFilterList)
        {
            boolean bExcludeFilter = bExcludeList.get(i++);
            
            if (sItem.equals(sKey)) {
                return ! bExcludeFilter;
            }
        }
        
        return bIncludeByDefault;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentFilter#checkItemAccess(java.lang.String, EContentType)
     */
    public boolean checkItemAccess(String sKey, EContentType contentType)
    {
        return checkFolderAccess(sKey);
    }

    /**
     * Returns the bIncludeByDefault.
     *
     * @return Returns the bIncludeByDefault.
     */
    public boolean isIncludeByDefault()
    {
        return bIncludeByDefault;
    }

    /**
     * Sets the bIncludeByDefault.
     *
     * @param includeByDefault The bIncludeByDefault to be set.
     */
    public void setIncludeByDefault(boolean includeByDefault)
    {
        bIncludeByDefault = includeByDefault;
    }
}
