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
package com.cordys.coe.bf.content.types.coboc;

import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;

/**
 * Content handle implementation CoBOC folder content types.
 *
 * @author mpoyhone
 */
public class CobocContentHandle implements IContentHandle
{
    /**
     * CoBOC object id for this rule or rule group.
     */
    private String sObjectId;
    /**
     * CoBOC key for this object.
     */
    private String sKey;
    /**
     * Content type that this handle can point to.
     */
    private EContentType ctContentType;
    
    /**
     * Constructor for this handle. 
     * @param ctType Handle content type.
     */
    public CobocContentHandle(EContentType ctType) {
        this.ctContentType = ctType;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#getContentType()
     */
    public EContentType getContentType()
    {
        return ctContentType;
    }    
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#getContentId()
     */
    public String getContentId()
    {
        return sKey;
    }

    /**
     * Returns the objectId.
     *
     * @return Returns the objectId.
     */
    public String getObjectId()
    {
        return sObjectId;
    }

    /**
     * The objectId to set.
     *
     * @param aObjectId The objectId to set.
     */
    public void setObjectId(String aObjectId)
    {
        sObjectId = aObjectId;
    }

    /**
     * Returns the key.
     *
     * @return Returns the key.
     */
    public String getKey()
    {
        return sKey;
    }

    /**
     * The key to set.
     *
     * @param aKey The key to set.
     */
    public void setKey(String aKey)
    {
        sKey = aKey;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#copyFrom(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public void copyFrom(IContentHandle chSource) throws BFException
    {
        if (! (chSource instanceof CobocContentHandle)) {
            throw new BFException("Content handle is not a CoBOC handle");
        }
        
        this.sKey = ((CobocContentHandle) chSource).sKey;
        this.sObjectId = ((CobocContentHandle) chSource).sObjectId;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "{" + sKey + ", " + sObjectId + "}";
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#getLogName()
     */
    public String getLogName()
    {
        return sKey != null ? sKey : sObjectId != null ? "(ID=" + sObjectId + ")" : "*Unknown*";
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#isSet()
     */
    public boolean isSet()
    {
        return (sObjectId != null &&
                sObjectId.length() > 0) ||
               (sKey != null &&
                sKey.length() > 0);
    } 
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#equals(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public boolean keyOrIdEquals(CobocContentHandle cchOther)
    {
        if (sKey != null) {
            if (sKey.equals(cchOther.sKey)) {
                return true;
            }
        }
        
        if (sObjectId != null) {
            if (sObjectId.equals(cchOther.sObjectId)) {
                return true;
            }
        }     
        
        return false;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#equals(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public boolean equals(IContentHandle chOther)
    {
        if (! (chOther instanceof CobocContentHandle)) {
            return false;
        }
        
        CobocContentHandle cchOther = (CobocContentHandle) chOther;
        
        if (sKey != null) {
            if (! sKey.equals(cchOther.sKey)) {
                return false;
            }
        } else {
            if (cchOther.sKey != null) {
                return false;
            }
        }
        
        if (sObjectId != null) {
            if (! sObjectId.equals(cchOther.sObjectId)) {
                return false;
            }
        } else {
            if (cchOther.sObjectId != null) {
                return false;
            }
        }     
        
        if (ctContentType != null) {
            if (ctContentType != cchOther.ctContentType) {
                return false;
            }
        } else {
            if (cchOther.ctContentType != null) {
                return false;
            }
        }    
        
        return true;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (! (obj instanceof CobocContentHandle)) {
            return false;
        }
        
        return equals((CobocContentHandle) obj);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        int iRes = 0;
        
        if (sObjectId != null) {
            iRes ^= sObjectId.hashCode(); 
        }
        
        if (sKey != null) {
            iRes ^= sKey.hashCode(); 
        }
        
        if (ctContentType != null) {
            iRes ^= ctContentType.hashCode();
        }
        
        return iRes;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#cloneHandle()
     */
    public IContentHandle cloneHandle()
    {
        CobocContentHandle cchRes = new CobocContentHandle(ctContentType);
        
        cchRes.sObjectId = sObjectId;
        cchRes.sKey = sKey;
        
        return cchRes;
    }
}
