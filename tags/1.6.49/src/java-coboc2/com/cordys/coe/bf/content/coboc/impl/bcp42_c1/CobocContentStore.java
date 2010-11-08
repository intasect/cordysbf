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
package com.cordys.coe.bf.content.coboc.impl.bcp42_c1;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentStore;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;

/**
 * Implements a content store that stores objects by the
 * content ID that is returned by the content handle. This
 * store should be enough for content types that have only a single key.
 *
 * @author mpoyhone
 */
public class CobocContentStore implements IContentStore
{
    private Map<String, IContent> mKeyMap = new HashMap<String, IContent>();
    private Map<String, IContent> mObjectIdMap = new HashMap<String, IContent>();
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentStore#insertObject(com.cordys.coe.bf.content.base.IContent)
     */
    public void insertObject(IContent cContent)
    {
        IContentHandle chHandle = cContent.getHandle();
        
        if (chHandle == null) {
            throw new IllegalArgumentException("Content handle is null.");
        }
        
        if (! (chHandle instanceof CobocContentHandle)) {
            throw new IllegalArgumentException("Content handle is not a CoBOC content handle.");
        }
        
        CobocContentHandle cchCobocHandle = (CobocContentHandle) chHandle;
        String sKey = cchCobocHandle.getKey();
        String sObjectId = cchCobocHandle.getObjectId();
        
        if (sKey != null) {
            mKeyMap.put(sKey, cContent);
        }
        
        if (sObjectId != null) {
            mObjectIdMap.put(sObjectId, cContent);
        }
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentStore#removeObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent removeObject(IContentHandle chHandle)
    {
        if (chHandle == null) {
            throw new IllegalArgumentException("Content handle is null.");
        }
        
        if (! (chHandle instanceof CobocContentHandle)) {
            throw new IllegalArgumentException("Content handle is not a CoBOC content handle.");
        }
        
        CobocContentHandle cchCobocHandle = (CobocContentHandle) chHandle;
        String sKey = cchCobocHandle.getKey();
        String sObjectId = cchCobocHandle.getObjectId();
        IContent cKeyRes = null;
        IContent cObjectIdRes = null;
        
        if (sKey != null) {
            cKeyRes = mKeyMap.remove(sKey);
        }
        
        if (sObjectId != null) {
            cObjectIdRes = mObjectIdMap.remove(sObjectId);
        }
        
        return cKeyRes != null ? cKeyRes : cObjectIdRes;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentStore#findObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent findObject(IContentHandle chHandle)
    {
        if (chHandle == null) {
            throw new IllegalArgumentException("Content handle is null.");
        }
        
        if (! (chHandle instanceof CobocContentHandle)) {
            throw new IllegalArgumentException("Content handle is not a CoBOC content handle.");
        }
        
        CobocContentHandle cchCobocHandle = (CobocContentHandle) chHandle;
        String sKey = cchCobocHandle.getKey();
        String sObjectId = cchCobocHandle.getObjectId();
        IContent cKeyRes = null;
        IContent cObjectIdRes = null;
        
        if (sKey != null) {
            cKeyRes = mKeyMap.get(sKey);
        }
        
        if (sObjectId != null) {
            cObjectIdRes = mObjectIdMap.get(sObjectId);
        }
        
        return cKeyRes != null ? cKeyRes : cObjectIdRes;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentStore#clear()
     */
    public void clear()
    {
        mKeyMap.clear();
        mObjectIdMap.clear();
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentStore#getObjects()
     */
    public Collection<IContent> getObjects()
    {
        Set<IContent> sResSet = new HashSet<IContent>();
        
        // Add objects from both maps to the result set. If an object exists
        // in both maps, the set will take care that it will be contained in it only once.
        sResSet.addAll(mKeyMap.values());
        sResSet.addAll(mObjectIdMap.values());

        return sResSet;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentStore#renameObjectReferences(com.cordys.coe.bf.content.base.IContentHandle, com.cordys.coe.bf.content.base.IContentHandle)
     */
    public void renameObjectReferences(IContentHandle chFromHandle, IContentHandle chToHandle) throws BFException
    {
        Collection<IContent> cObjects = getObjects();
        
        for (IContent cTmp : cObjects)
        {
            Collection<IContentHandle> cRefs = cTmp.getReferencedContent();
            
            for (IContentHandle chRefHandle : cRefs)
            {
                if (chFromHandle instanceof CobocContentHandle &&
                    chRefHandle instanceof CobocContentHandle) {
                    if (((CobocContentHandle) chRefHandle).keyOrIdEquals((CobocContentHandle) chFromHandle)) {
                        chRefHandle.copyFrom(chToHandle);
                    }
                } else {
                    if (chRefHandle.equals(chFromHandle)) {
                        chRefHandle.copyFrom(chToHandle);
                    }
                }
            }
        }
    }

}
