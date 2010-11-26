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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.coboc.folders.Folder;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.CobocUtils;

/**
 * MDM folder that contains entities for a backend. This is needed
 * because the folder name in CoBOC is the backend object ID, but
 * we want to store it as the backend name on the file system. Also
 * when inserting this folder to CoBOC the folder name will change 
 * because the backend will have a new ID anyway.
 *
 * @author mpoyhone
 */
public class EntityFolder extends Folder
{
    /**
     * A handle to the backend object.
     */
    protected CobocContentHandle cchBackendHandle;
    
    /**
     * Constructor for Entity
     */
    public EntityFolder()
    {
        super(EContentType.MDM_ENTITY_FOLDER);
        cchBackendHandle = new CobocContentHandle(EContentType.MDM_BACKEND);
    }
    
    /**
     * Returns the backend handle.
     * @return The backend handle.
     */
    public IContentHandle getBackend() {
        return cchBackendHandle;
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        Collection<IContentHandle> cSuperRes = super.getReferencedContent();
        List<IContentHandle> lRes = new LinkedList<IContentHandle>();
        
        if (cSuperRes != null) {
            lRes.addAll(cSuperRes);
        }
        
        if (cchBackendHandle != null && cchBackendHandle.isSet()) {
            lRes.add(cchBackendHandle);
        }
        
        return lRes;
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        super.updateReferences(csSrc);
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType()  == EContentSourceType.ISV) {
            CobocUtils.updateCobocHandle(bcContext, cchBackendHandle);
        }
    }   
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#onLoad(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void onLoad(IContentSource csSrc) throws BFException
    {
        super.onLoad(csSrc);
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType()  == EContentSourceType.ISV) {
            String sBackendId = getName();
            
            if (sBackendId == null) {
                throw new BFException(getLogName() + ": Backend ID not set.");
            }
            
            cchBackendHandle.setObjectId(sBackendId);
            CobocUtils.updateCobocHandle(bcContext, cchBackendHandle);
        } else if (csSrc.getType() == EContentSourceType.FILESYSTEM) {
            String sBackendName = getName();
            
            if (sBackendName == null) {
                throw new BFException(getLogName() + ": Backend name not set.");
            }
            
            cchBackendHandle.setKey("/cordys/mdm/modeler/components/backends/" + sBackendName);
            CobocUtils.updateCobocHandle(bcContext, cchBackendHandle);
        }
    }
    
    /**
     * Returns a new handle for the given content source.
     * @param cstType Content source type.
     * @return Entity key.
     * @throws BFException 
     */
    public IContentHandle getHandleForSource(EContentSourceType cstType) throws BFException {
        CobocContentHandle cchRes = (CobocContentHandle) getHandle().cloneHandle();
        
        switch (cstType) {
        case BCP :
        case ISV :
            cchRes.setKey(recreateKey(getKey(), getBcpName()));
            break;
            
        case FILESYSTEM :
            cchRes.setKey(recreateKey(getKey(), getFilesystemName()));
            break;
        }
        
        return cchRes;
    }
    
    /**
     * Changes the key and name to match the destination format.
     * @param csDest Destination content source.
     * @throws BFException 
     */
    public void changeKeyFor(EContentSourceType cstType) throws BFException {
        switch (cstType) {
        case BCP :
        case ISV :
            setName(getBcpName());
            setKey(recreateKey(getKey(), getName()));
            break;
            
        case FILESYSTEM :
            setName(getFilesystemName());
            setKey(recreateKey(getKey(), getName()));
            break;
        }
    }
    
    /**
     * Recreates the key from the passed backend name or ID.
     * @param sOrigKey Original entity folder name.
     * @param sBackendName Backend name or object ID.
     * @return New key.
     */
    public static String recreateKey(String sOrigKey, String sBackendName) {
        return sOrigKey.replaceFirst("^(.+)/[^/]+$", "$1/" + sBackendName);
    }

    /**
     * Returns the bcpKey.
     *
     * @return Returns the bcpKey.
     */
    public String getBcpKey() throws BFException
    {
        String sKey = getKey();
        String sName = getBcpName();
        
        if (sKey == null) {
            throw new BFException(getLogName() + ": Key is not set.");
        }

        int iPos = sKey.lastIndexOf('/');
        
        if (iPos > 0 && iPos < sKey.length() - 1) {
            sKey = sKey.substring(0, iPos + 1) + sName;
        }
        
        return sKey;
    }

    /**
     * Returns the bcpName.
     *
     * @return Returns the bcpName.
     */
    public String getBcpName() throws BFException
    {
        String sBackendId = cchBackendHandle.getObjectId();
        
        if (sBackendId == null) {
            throw new BFException(getLogName() + ": Backend object ID is not set.");
        }

        return sBackendId;
    }

    /**
     * Returns the filesystemKey.
     *
     * @return Returns the filesystemKey.
     */
    public String getFilesystemKey() throws BFException
    {
        String sKey = getKey();
        String sName = getFilesystemName();
        
        if (sKey == null) {
            throw new BFException(getLogName() + ": Key is not set.");
        }

        int iPos = sKey.lastIndexOf('/');
        
        if (iPos > 0 && iPos < sKey.length() - 1) {
            sKey = sKey.substring(0, iPos + 1) + sName;
        }
        
        return sKey;
    }

    /**
     * Returns the filesystemName.
     *
     * @return Returns the filesystemName.
     */
    public String getFilesystemName() throws BFException
    {
        Backend bBackend = (Backend) bcContext.findContent(cchBackendHandle);
        
        if (bBackend == null) {
            throw new BFException(getLogName() + ": Backend not found: " + cchBackendHandle);
        }
        
        String sBackendName = bBackend.getName();

        if (sBackendName == null) {
            throw new BFException(getLogName() + ": Backend name not set. ");
        }

        return sBackendName;
    }
}
