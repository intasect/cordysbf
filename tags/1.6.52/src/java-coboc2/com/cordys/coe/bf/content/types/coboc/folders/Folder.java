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
package com.cordys.coe.bf.content.types.coboc.folders;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.coboc.impl.bcp42_c1.CobocConstants;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Class containing CoBOC folder information.
 *
 * @author mpoyhone
 */
public class Folder extends CobocContentBase
{
    /**
     * Constructor for CobocFolderContentBase
     */
    public Folder()
    {
        this(EContentType.COBOC_FOLDERS_FOLDER);
    }

    /**
     * Constructor for CobocFolderContentBase
     */
    public Folder(EContentType ctType)
    {
        super(ctType);
        cchParentHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_FOLDER);
        lSubContentList = new ArrayList<IContentHandle>(5);
    }
    
    /**
     * Creates a Folder object from the given key. Sets the parent and name key as well.
     * @param sFolderKey Folder's key.
     * @return Create folder object.
     * @throws BFException Thrown if the key was invalid.
     */
    public static Folder createFromKey(String sFolderKey) throws BFException {
        if (sFolderKey == null || sFolderKey.length() == 0)
        {
            throw new BFException("Key is empty.");
        }
        
        Folder fFolder = new Folder();
        int iSep = sFolderKey.lastIndexOf('/');
        
        if (iSep < 0 || iSep >= sFolderKey.length()) {
            throw new BFException("Invalid folder key: " + sFolderKey);
        }
        
        String sParentKey = iSep > 0 ? sFolderKey.substring(0, iSep) : null;
        String sName = sFolderKey.substring(iSep + 1);
        
        fFolder.setName(sName);
        fFolder.setDescription(sName);
        fFolder.setKey(sFolderKey);
        fFolder.setParentKey(sParentKey);    
        
        return fFolder;
    }

    /**
     * 
     * @see com.cordys.coe.bf.content.types.ContentBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        List<IContentHandle> lResList = new LinkedList<IContentHandle>();

        if ((cchParentHandle != null) && cchParentHandle.isSet())
        {
            lResList.add(cchParentHandle);
        }

        return lResList;
    }

    /**
     * Returns <code>true</code> if the parent folder is the root
     * folder.
     *
     * @return <code>true</code> if the parent folder is the root folder.
     */
    public boolean isParentRootFolder()
    {
        if ((getParentId() == null) || (getParentId().length() == 0))
        {
            return true;
        }

        if ((getParentKey() == null) || (getParentKey().length() == 0))
        {
            return true;
        }

        return CobocConstants.ROOT_FOLDER_KEY.equals(getParentKey());
    }

    /**
     * Returns <code>true</code> if this is the root folder.
     *
     * @return <code>true</code> if this is the root folder.
     */
    public boolean isRootFolder()
    {
        if ((getObjectId() == null) || (getObjectId().length() == 0))
        {
            return true;
        }

        if ((getKey() == null) || (getKey().length() == 0))
        {
            return true;
        }

        return CobocConstants.ROOT_FOLDER_KEY.equals(getKey());
    }

    /**
     * 
     * @see com.cordys.coe.bf.content.types.ContentBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc)
                          throws BFException
    {
        if ((cchParentHandle != null) && cchParentHandle.isSet())
        {
            Folder fParentFolder = (Folder) bcContext.findContent(cchParentHandle);
            
            if (! CobocConstants.ROOT_FOLDER_KEY.equals(getParentKey()) &&
                fParentFolder == null && 
                csSrc.getType() == EContentSourceType.FILESYSTEM) {
                // Folder has not been loaded (e.g. from file system), so create it now.
                fParentFolder = createFromKey(getParentKey());
                bcContext.registerContent(fParentFolder);
                fParentFolder.onLoad(csSrc);
                fParentFolder.updateReferences(csSrc);
            }            

            if (fParentFolder != null)
            {
                if (!fParentFolder.getChildren().contains(this))
                {
                    fParentFolder.addChild(chHandle);
                }

                if ((csSrc.getType() == EContentSourceType.BCP) ||
                        (csSrc.getType() == EContentSourceType.ISV))
                {
                    String sParentKey = fParentFolder.getKey();
                    String sParentObjectId = fParentFolder.getObjectId();

                    if (sParentKey != null)
                    {
                        cchParentHandle.setKey(sParentKey);
                    }

                    if (sParentObjectId != null)
                    {
                        cchParentHandle.setObjectId(sParentObjectId);
                    }
                }
            }
        }
    }

    /**
     * @see com.cordys.coe.bf.content.types.coboc.CobocContentBase#createWriteVersion(com.cordys.coe.bf.content.base.IContentSource)
     */
    @Override
    public IContent createWriteVersion(IContentSource csDest)
            throws BFException
    {
        Folder folder = (Folder) super.createWriteVersion(csDest);
        String desc = folder.getDescription();
        
        if (desc == null || desc.length() == 0)
        {
            folder.setDescription(getName());
        }
        
        return folder;
    }
}
