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
package com.cordys.coe.bf.content.types.xmlstore;

import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;

/**
 * Content handle implementation XMLStore content types.
 *
 * @author mpoyhone
 */
public class XmlStoreContentHandle implements IContentHandle
{
    /**
     * XMLStore key for this object.
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
    public XmlStoreContentHandle(EContentType ctType) {
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
        if (! (chSource instanceof XmlStoreContentHandle)) {
            throw new BFException("Content handle is not an XMLStore handle");
        }
        
        this.sKey = ((XmlStoreContentHandle) chSource).sKey;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "{" + sKey + "}";
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#getLogName()
     */
    public String getLogName()
    {
        return sKey != null ? sKey : "*Unknown*";
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#isSet()
     */
    public boolean isSet()
    {
        return (sKey != null &&
                sKey.length() > 0);
    } 
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#equals(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public boolean keyOrIdEquals(XmlStoreContentHandle cchOther)
    {
        if (sKey != null) {
            if (sKey.equals(cchOther.sKey)) {
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
        if (! (chOther instanceof XmlStoreContentHandle)) {
            return false;
        }
        
        XmlStoreContentHandle cchOther = (XmlStoreContentHandle) chOther;
        
        if (sKey != null) {
            if (! sKey.equals(cchOther.sKey)) {
                return false;
            }
        } else {
            if (cchOther.sKey != null) {
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
        if (! (obj instanceof XmlStoreContentHandle)) {
            return false;
        }
        
        return equals((XmlStoreContentHandle) obj);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return sKey.hashCode();
        
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#cloneHandle()
     */
    public IContentHandle cloneHandle()
    {
        XmlStoreContentHandle cchRes = new XmlStoreContentHandle(ctContentType);
        
        cchRes.sKey = sKey;
        
        return cchRes;
    }
}
