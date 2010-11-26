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

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.ContentBase;
import com.cordys.coe.bf.content.types.EContentCategory;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;

/**
 * Base class for all CoBOC folder content objects.
 *
 * @author mpoyhone
 */
public abstract class CobocContentBase extends ContentBase
{
    /**
     * Content type.
     */
    protected EContentType ctContentType;
    /**
     * Handle object for this content.
     */
    protected CobocContentHandle chHandle;
    /**
     * Handle object for parent object.
     */
    protected CobocContentHandle cchParentHandle;
    /**
     * Contains the original object ID that can be
     * stored on the file system. Usually this is not
     * used, but sometime it might be required to use
     * the original ID.
     */
    protected String originalObjectId;
    /**
     * Object name.
     */
    protected String sName;
    /**
     * Object description.
     */
    protected String sDescription;
    /**
     * Last modification timestamp.
     */
    protected String sLastModified; 
    /**
     * Object organization DN.
     */
    protected String sOrganization;
    /**
     * Owner DN.
     */
    protected String sOwner;
    
    /**
     * Constructor for CobocFolderContentBase
     */
    public CobocContentBase(EContentType ctContentType)
    {
        this.ctContentType = ctContentType;
        this.chHandle = new CobocContentHandle(this.ctContentType);
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#getHandle()
     */
    public CobocContentHandle getHandle()
    {
        return chHandle;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#setHandle(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public void setHandle(IContentHandle hHandle)
    {
        if (! (hHandle instanceof CobocContentHandle)) {
            throw new IllegalArgumentException("Content handle is not of the CobocFolderContentHandle");
        }
        
        this.chHandle = (CobocContentHandle) hHandle;
    }  
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#getType()
     */
    public EContentType getType()
    {
        return ctContentType;
    }
    
    /**
     * Returns the CoBOC key.
     *
     * @return Returns the CoBOC key.
     */
    public String getKey()
    {
        return chHandle.getKey();
    }

    /**
     * The CoBOC key to set.
     *
     * @param sKey The CoBOC key to set.
     */
    public void setKey(String sKey)
    {
        chHandle.setKey(sKey);
    }
    
    /**
     * Returns the CoBOC object ID.
     *
     * @return Returns the CoBOC object ID.
     */
    public String getObjectId()
    {
        return chHandle.getObjectId();
    }

    /**
     * The CoBOC object ID to set.
     *
     * @param aLdapDn The CoBOC object ID to set.
     */
    public void setObjectId(String sObjectId)
    {
        chHandle.setObjectId(sObjectId);
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
     * Returns the description.
     *
     * @return Returns the description.
     */
    public String getDescription()
    {
        return sDescription;
    }


    /**
     * The description to set.
     *
     * @param aDescription The description to set.
     */
    public void setDescription(String aDescription)
    {
        sDescription = aDescription;
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
        if (! (chParent instanceof CobocContentHandle)) {
            throw new IllegalArgumentException("Handle is not of type CobocContentHandle.");
        }
        
        this.cchParentHandle = (CobocContentHandle) chParent;
    }
    
    public void setParentId(String sId) {
        if (cchParentHandle == null) {
            throw new IllegalStateException("Parent handle is not set.");
        }

        cchParentHandle.setObjectId(sId);
    }    
    
    public String getParentId() {
        if (cchParentHandle == null) {
            throw new IllegalStateException("Parent handle is not set.");
        }
        
        return cchParentHandle.getObjectId();
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
     * Returns the organization.
     *
     * @return Returns the organization.
     */
    public String getOrganization()
    {
        return sOrganization;
    }

    /**
     * The organization to set.
     *
     * @param aOrganization The organization to set.
     */
    public void setOrganization(String aOrganization)
    {
        sOrganization = aOrganization;
    }

    /**
     * Returns the owner.
     *
     * @return Returns the owner.
     */
    public String getOwner()
    {
        return sOwner;
    }

    /**
     * The owner to set.
     *
     * @param aOwner The owner to set.
     */
    public void setOwner(String aOwner)
    {
        sOwner = aOwner;
    }   
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#updateFromNewVersion(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateFromNewVersion(IContent cNewObject, IContentSource csSrc) throws BFException
    {
        if (! (cNewObject instanceof CobocContentBase)) {
            throw new BFException("New object is not type CobocContentBase.");
        }
        
        CobocContentBase ccbNew = (CobocContentBase) cNewObject;
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType() == EContentSourceType.ISV) {
            String sNewObjectId = ccbNew.getObjectId();
            String sNewLastModified = ccbNew.getLastModified();
            
            if (sNewObjectId == null) {
                throw new BFException(getLogName() + ": Object ID is null in the new object.");
            }      
            
            setObjectId(sNewObjectId);

            // Rules (only rule groups?) do not need last modified. 
            if (getType().getCategory() != EContentCategory.COBOC_RULES) {
                if (sNewLastModified == null) {
                    throw new BFException(getLogName() + ": lastModified is null in the new object.");
                }                  
                
                setLastModified(sNewLastModified);
            }
        }
    }    
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#updateFromOldVersion(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateFromOldVersion(IContent cOldObject, IContentSource csSrc) throws BFException
    {
        if (! (cOldObject instanceof CobocContentBase)) {
            throw new BFException("Old object is not type CobocContentBase.");
        }
        
        CobocContentBase ccbOld = (CobocContentBase) cOldObject;
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType() == EContentSourceType.ISV) {
            String sOldObjectId = ccbOld.getObjectId();
            String sOldLastModified = ccbOld.getLastModified();
            
            if (sOldObjectId == null) {
                throw new BFException(getLogName() + ": Object ID is null in the old object.");
            }      

            setObjectId(sOldObjectId);
            
            // Rules (only rule groups?) do not need last modified. 
            if (getType().getCategory() != EContentCategory.COBOC_RULES) {
                if (sOldLastModified == null) {
                    throw new BFException(getLogName() + ": lastModified is null in the old object.");
                }                  

                setLastModified(sOldLastModified);
            }
        }
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#createWriteVersion(com.cordys.coe.bf.content.base.IContentSource)
     */
    public IContent createWriteVersion(IContentSource csDest) throws BFException
    {
        CobocContentBase ccbWriteVersion = (CobocContentBase) super.createWriteVersion(csDest);
        
        if (csDest.getType() == EContentSourceType.BCP) {
            // Update the organization and user information.
            // Modify to the organization and information.
            String sUserDn  = bcContext.getConfig().getUserDn();
            String sOrgDn = bcContext.getConfig().getOrganizationDn();
            
            if (sUserDn == null) {
                throw new BFException("User DN not set in the config.");
            }
            
            if (sOrgDn == null) {
                throw new BFException("Organization DN not set in the config.");
            }
            
            if (! sOrgDn.equals(ccbWriteVersion.getOrganization())) {
                // Organization has changed.
                ccbWriteVersion.setOwner(sUserDn);
                ccbWriteVersion.setOrganization(sOrgDn);
            }                
        } else if (csDest.getType() == EContentSourceType.ISV) {
            if (sLastModified == null || sLastModified.length() == 0)
            {
                // Set the last modified to current time.
                sLastModified = Long.toString(System.currentTimeMillis());
            }
        }
        
        return ccbWriteVersion;
    }

    /**
     * Returns the originalObjectId.
     *
     * @return Returns the originalObjectId.
     */
    public String getOriginalObjectId()
    {
        return originalObjectId;
    }

    /**
     * Sets the originalObjectId.
     *
     * @param originalObjectId The originalObjectId to be set.
     */
    public void setOriginalObjectId(String originalObjectId)
    {
        this.originalObjectId = originalObjectId;
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#onLoad(com.cordys.coe.bf.content.base.IContentSource)
     */
    @Override
    public void onLoad(IContentSource csSrc) throws BFException
    {
        super.onLoad(csSrc);
        
        if (csSrc.getType() == EContentSourceType.BCP) {    
            // Set the original object ID if not yet set.
            if (originalObjectId == null) {
                originalObjectId = getObjectId();
            }
        }
    }       
}
