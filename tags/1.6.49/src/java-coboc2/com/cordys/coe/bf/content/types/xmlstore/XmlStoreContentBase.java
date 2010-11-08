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

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.ContentBase;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;

/**
 * Base class for all XMLStore folder content objects.
 *
 * @author mpoyhone
 */
public abstract class XmlStoreContentBase extends ContentBase
{
    /**
     * Content type.
     */
    protected EContentType ctContentType;
    /**
     * Handle object for this content.
     */
    protected XmlStoreContentHandle chHandle;
    
    /**
     * Handle object for parent object.
     */
    protected XmlStoreContentHandle cchParentHandle;
    /**
     * Object name.
     */
    protected String sName;
    /**
     * Object level/version  (user/isv/organization).
     */
    protected String sLevel;
    /**
     * Last modification timestamp.
     */
    protected String sLastModified; 
    /**
     * Owner DN.
     */
    protected String sReadOnly;
    
    /**
     * Constructor for CobocFolderContentBase
     */
    public XmlStoreContentBase(EContentType ctContentType)
    {
        this.ctContentType = ctContentType;
        this.chHandle = new XmlStoreContentHandle(this.ctContentType);
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#getHandle()
     */
    public XmlStoreContentHandle getHandle()
    {
        return chHandle;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#setHandle(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public void setHandle(IContentHandle hHandle)
    {
        if (! (hHandle instanceof XmlStoreContentHandle)) {
            throw new IllegalArgumentException("Content handle is not of type XmlStoreContentHandle");
        }
        
        this.chHandle = (XmlStoreContentHandle) hHandle;
    }  
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#getType()
     */
    public EContentType getType()
    {
        return ctContentType;
    }
    
    /**
     * Returns the XMLStore key.
     *
     * @return Returns the XMLStore key.
     */
    public String getKey()
    {
        return chHandle.getKey();
    }

    /**
     * The XMLStore key to set.
     *
     * @param sKey The XMLStore key to set.
     */
    public void setKey(String sKey)
    {
        chHandle.setKey(sKey);
    }
    
    /**
     * Returns the name.
     *
     * @return Returns the name.
     */
    public String getName()
    {
        return sName;
    }

    /**
     * The name to set.
     *
     * @param aName The name to set.
     */
    public void setName(String aName)
    {
        sName = aName;
    }


    /**
     * Returns the level.
     *
     * @return Returns the level.
     */
    public String getLevel()
    {
        return sLevel;
    }


    /**
     * The level to set.
     *
     * @param aDescription The level to set.
     */
    public void setLevel(String aDescription)
    {
        sLevel = aDescription;
    }

    /**
     * Returns the lastModified.
     *
     * @return Returns the lastModified.
     */
    public String getLastModified()
    {
        return sLastModified;
    }

    /**
     * The lastModified to set.
     *
     * @param aLastModified The lastModified to set.
     */
    public void setLastModified(String aLastModified)
    {
        sLastModified = aLastModified;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#getParent()
     */
    public IContentHandle getParent()
    {
        return cchParentHandle;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#setParent(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public void setParent(IContentHandle chParent)
    {
        if (! (chParent instanceof XmlStoreContentHandle)) {
            throw new IllegalArgumentException("Handle is not of type XmlStoreContentHandle.");
        }
        
        this.cchParentHandle = (XmlStoreContentHandle) chParent;
    }
    
    public void setParentKey(String sKey) {
        if (cchParentHandle == null) {
            throw new IllegalStateException("Parent handle is not set.");
        }

        cchParentHandle.setKey(sKey);
    }    
    
    public String getParentKey() {
        if (cchParentHandle == null) {
            throw new IllegalStateException("Parent handle is not set.");
        }
        
        return cchParentHandle.getKey();
    }    

    /**
     * Returns the owner.
     *
     * @return Returns the owner.
     */
    public String getReadOnly()
    {
        return sReadOnly;
    }

    /**
     * The owner to set.
     *
     * @param aOwner The owner to set.
     */
    public void setReadOnly(String aOwner)
    {
        sReadOnly = aOwner;
    }   
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#updateFromNewVersion(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateFromNewVersion(IContent cNewObject, IContentSource csSrc) throws BFException
    {
        if (! (cNewObject instanceof XmlStoreContentBase)) {
            throw new BFException("New object is not type XmlStoreContentBase.");
        }
        
        XmlStoreContentBase xscbNew = (XmlStoreContentBase) cNewObject;
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType() == EContentSourceType.ISV) {
            String sNewLastModified = xscbNew.getLastModified();

            if (sNewLastModified == null) {
                throw new BFException(getLogName() + ": lastModified is null in the new object.");
            }                  
            
            setLastModified(sNewLastModified);
        }
    }    
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#updateFromOldVersion(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateFromOldVersion(IContent cOldObject, IContentSource csSrc) throws BFException
    {
        if (! (cOldObject instanceof XmlStoreContentBase)) {
            throw new BFException("Old object is not type XmlStoreContentBase.");
        }
        
        XmlStoreContentBase xscbOld = (XmlStoreContentBase) cOldObject;
        
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType() == EContentSourceType.ISV) {
            String sOldLastModified = xscbOld.getLastModified();
            
            if (sOldLastModified == null) {
                throw new BFException(getLogName() + ": lastModified is null in the old object.");
            }                  

            setLastModified(sOldLastModified);
        }
    }    
}
