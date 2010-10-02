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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentStore;

/**
 * Implements a content store that stores objects by the
 * content ID that is returned by the content handle. This
 * store should be enough for content types that have only a single key.
 *
 * @author mpoyhone
 */
public class SimpleContentStore implements IContentStore
{
    private Map<String, IContent> mContentMap = new HashMap<String, IContent>();
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentStore#insertObject(com.cordys.coe.bf.content.base.IContent)
     */
    public void insertObject(IContent cContent)
    {
        IContentHandle chHandle = cContent.getHandle();
        
        if (chHandle == null) {
            throw new IllegalArgumentException("Content handle is null.");
        }
        
        mContentMap.put(chHandle.getContentId(), cContent);
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentStore#removeObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent removeObject(IContentHandle chHandle)
    {
        if (chHandle == null) {
            throw new IllegalArgumentException("Content handle is null.");
        }
        
        return mContentMap.remove(chHandle.getContentId());
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentStore#findObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent findObject(IContentHandle chHandle)
    {
        if (chHandle == null) {
            throw new IllegalArgumentException("Content handle is null.");
        }
        
        return mContentMap.get(chHandle.getContentId());
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentStore#clear()
     */
    public void clear()
    {
        mContentMap.clear();
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentStore#getObjects()
     */
    public Collection<IContent> getObjects()
    {
        return mContentMap.values();
    }

    /**
     * Note: This is not implemented.
     * @see com.cordys.coe.bf.content.base.IContentStore#renameObjectReferences(com.cordys.coe.bf.content.base.IContentHandle, com.cordys.coe.bf.content.base.IContentHandle)
     */
    public void renameObjectReferences(IContentHandle chFromHandle, IContentHandle chToHandle)
    {
        
    }
}
