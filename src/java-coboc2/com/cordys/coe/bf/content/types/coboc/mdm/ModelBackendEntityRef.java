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
package com.cordys.coe.bf.content.types.coboc.mdm;

import java.util.List;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.CobocUtils;

/**
 * A simple bean for entity references in the MDM model backend reference.
 *
 * @author mpoyhone
 */
public class ModelBackendEntityRef
{
    /**
     * Contains handle of the real Entity object.
     */
    private CobocContentHandle cchEntityHandle;

    
    /**
     * Constructor for ModelBackendRef
     */
    public ModelBackendEntityRef() {
        cchEntityHandle = new CobocContentHandle(EContentType.MDM_ENTITY);
    }

    public void getReferencedContent(List<IContentHandle> lResult)
    {
        if (cchEntityHandle != null && cchEntityHandle.isSet()) {
            lResult.add(cchEntityHandle);
        }
    }

    public void updateReferences(BFContext bcContext, IContentSource csSrc) throws BFException
    {
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType() == EContentSourceType.ISV) {
            CobocUtils.updateCobocHandle(bcContext, cchEntityHandle);
        }
    }   

    /**
     * Returns the key.
     *
     * @return Returns the key.
     */
    public String getKey()
    {
        return cchEntityHandle.getKey();
    }
    /**
     * The key to set.
     *
     * @param aKey The key to set.
     */
    public void setKey(String aKey)
    {
        cchEntityHandle.setKey(aKey);
    }
    /**
     * Returns the objectId.
     *
     * @return Returns the objectId.
     */
    public String getObjectId()
    {
        return cchEntityHandle.getObjectId();
    }
    /**
     * The objectId to set.
     *
     * @param aObjectId The objectId to set.
     */
    public void setObjectId(String aObjectId)
    {
        cchEntityHandle.setObjectId(aObjectId);
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return CobocUtils.beanToString(this, "ModelBackendEntityRef", true);
   }
}
