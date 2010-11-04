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

import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentWriteMethod;
import com.cordys.coe.bf.exception.BFException;

/**
 * A base class for all write methods.
 *
 * @author mpoyhone
 */
public abstract class WriteMethodBase implements IContentWriteMethod
{
    /**
     * Contains the method version ID.
     */
    protected String sMethodVersion;
    /**
     * Determines if content filtering is used by this method.
     */
    protected boolean bUseContentFiltering = true;
    /**
     * Version parameter for content sources that support
     * more versions (e.g. XMLStore).
     */
    protected String sContentVersion;
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#setContentVersion(java.lang.String)
     */
    public void setContentVersion(String sVersion)
    {
        sContentVersion = sVersion;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#deleteObject(com.cordys.coe.bf.content.base.IContent)
     */
    public IContent deleteObject(IContent cObject) throws BFException
    {
        return updateObject(new ContentTuple(cObject, null));
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#insertObject(com.cordys.coe.bf.content.base.IContent, boolean)
     */
    public IContent insertObject(IContent cObject, boolean bCheckForOld) throws BFException
    {
        return updateObject(new ContentTuple(null, cObject));
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandlingMethod#getMethodVersion()
     */
    public String getMethodVersion()
    {
        return sMethodVersion;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#updateObject(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IContent)
     */
    public IContent updateObject(IContent cOldObject, IContent cNewObject) throws BFException
    {
        return updateObject(new ContentTuple(cOldObject, cNewObject));
    }      

    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#updateObjects(java.util.List)
     */
    public List<IContent> updateObjects(List<ContentTuple> lTupleList) throws BFException
    {
        List<IContent> lResList = null;
        
        for (ContentTuple ctTuple : lTupleList)
        {
            IContent cRes = updateObject(ctTuple);
            
            if (cRes != null) {
                if (lResList == null) {
                    lResList = new ArrayList<IContent>(lTupleList.size());
                }
                
                lResList.add(cRes);
            } else {
                if (lResList != null) {
                    throw new BFException("Update method is retuning a null while it has returned a non-null previously.");
                }
            }
        }
        return lResList;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandlingMethod#getContentFilterStatus()
     */
    public boolean getContentFilterStatus()
    {
        return bUseContentFiltering;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandlingMethod#setContentFilterStatus(boolean)
     */
    public void setContentFilterStatus(boolean bOn)
    {
        bUseContentFiltering = bOn;
    }
}
