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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;


/**
 * Base class for all CoBOC object instances (mappings, content maps, etc.)
 *
 * @author mpoyhone
 */
public class ObjectInstanceBase extends CobocContentBase
{   
    private String sEventFlag;
    private String sHistory;
    private CobocContentHandle cchTemplateHandle;
    
    /**
     * Constructor for TemplateInstance
     * @param ctType The actual type of this instance.
     */
    public ObjectInstanceBase(EContentType ctType)
    {
        super(ctType);
        cchParentHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_FOLDER);
        cchTemplateHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_TEMPLATE);
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        List<IContentHandle> lResList = new LinkedList<IContentHandle>();
        
        if (cchParentHandle != null && cchParentHandle.isSet()) {
            lResList.add(cchParentHandle);
        }
        
        return lResList;
    }

    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        if (bcContext == null) {
            // This has not been added to the context.
            return;
        }
        
        Folder fFolder = (Folder) bcContext.findContent(cchParentHandle);
        
        if (fFolder == null && csSrc.getType() == EContentSourceType.FILESYSTEM) {
            // Folder has not been loaded (e.g. from file system), so create it now.
            String parentKey = getParentKey();
            
            if (parentKey == null || parentKey.length() == 0)
            {
                throw new BFException("Parent folder is not set for " + getLogName());
            }
            
            fFolder = Folder.createFromKey(parentKey);
            bcContext.registerContent(fFolder);
            fFolder.onLoad(csSrc);
            fFolder.updateReferences(csSrc);
        }
        
        if (fFolder != null) {
            if (! fFolder.getChildren().contains(this)) {
                fFolder.addChild(chHandle);
            }
            
            if (csSrc.getType()  == EContentSourceType.BCP ||
                csSrc.getType()  == EContentSourceType.ISV) {
                String sParentKey = fFolder.getKey();
                String sParentObjectId = fFolder.getObjectId();
                
                if (sParentKey != null) {
                    cchParentHandle.setKey(sParentKey);
                }
                
                if (sParentObjectId != null) {
                    cchParentHandle.setObjectId(sParentObjectId);
                }                
            }      
        }
    }

    /**
     * Returns the eventFlag.
     *
     * @return Returns the eventFlag.
     */
    public String getEventFlag()
    {
        return sEventFlag;
    }

    /**
     * The eventFlag to set.
     *
     * @param aEventFlag The eventFlag to set.
     */
    public void setEventFlag(String aEventFlag)
    {
        sEventFlag = aEventFlag;
    }

    /**
     * Returns the history.
     *
     * @return Returns the history.
     */
    public String getHistory()
    {
        return sHistory;
    }

    /**
     * The history to set.
     *
     * @param aHistory The history to set.
     */
    public void setHistory(String aHistory)
    {
        sHistory = aHistory;
    }

    /**
     * Returns the templateId.
     *
     * @return Returns the templateId.
     */
    public String getTemplateId()
    {
        return cchTemplateHandle.getObjectId();
    }

    /**
     * The templateId to set.
     *
     * @param aTemplateId The templateId to set.
     */
    public void setTemplateId(String aTemplateId)
    {
        cchTemplateHandle.setObjectId(aTemplateId);
    }

    /**
     * Returns the templateKey.
     *
     * @return Returns the templateKey.
     */
    public String getTemplateKey()
    {
        return cchTemplateHandle.getKey();
    }

    /**
     * The templateKey to set.
     *
     * @param aTemplateKey The templateKey to set.
     */
    public void setTemplateKey(String aTemplateKey)
    {
        cchTemplateHandle.setKey(aTemplateKey);
    }    
    
}
