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

import java.util.ArrayList;
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
import com.cordys.coe.bf.utils.XmlStructure;


/**
 * Class containing CoBOC template information.
 *
 * @author mpoyhone
 */
public class Template extends CobocContentBase
{   
    private XmlStructure xsSchema;
    private String sPermanentlyInMemory;
    private String sValidateSchema;
    private String sIconUrl;
    private String sReference;
    private String sValidateRule;
    private String sHistory;
    private String sPluginClass;
    private String sPersistsInDb;
    
    /**
     * Constructor for Template
     */
    public Template()
    {
        super(EContentType.COBOC_FOLDERS_TEMPLATE);
        cchParentHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_FOLDER);
        lSubContentList = new ArrayList<IContentHandle>(5); // For special attributes.
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
        Folder fFolder = (Folder) bcContext.findContent(cchParentHandle);
        
        if (fFolder == null && csSrc.getType() == EContentSourceType.FILESYSTEM) {
            // Folder has not been loaded (e.g. from file system), so create it now.
            fFolder = Folder.createFromKey(getParentKey());
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
     * Returns the iconUrl.
     *
     * @return Returns the iconUrl.
     */
    public String getIconUrl()
    {
        return sIconUrl;
    }

    /**
     * The iconUrl to set.
     *
     * @param aIconUrl The iconUrl to set.
     */
    public void setIconUrl(String aIconUrl)
    {
        sIconUrl = aIconUrl;
    }

    /**
     * Returns the permanentlyInMemory.
     *
     * @return Returns the permanentlyInMemory.
     */
    public String getPermanentlyInMemory()
    {
        return sPermanentlyInMemory;
    }

    /**
     * The permanentlyInMemory to set.
     *
     * @param aPermanentlyInMemory The permanentlyInMemory to set.
     */
    public void setPermanentlyInMemory(String aPermanentlyInMemory)
    {
        sPermanentlyInMemory = aPermanentlyInMemory;
    }

    /**
     * Returns the pluginClass.
     *
     * @return Returns the pluginClass.
     */
    public String getPluginClass()
    {
        return sPluginClass;
    }

    /**
     * The pluginClass to set.
     *
     * @param aPluginClass The pluginClass to set.
     */
    public void setPluginClass(String aPluginClass)
    {
        sPluginClass = aPluginClass;
    }

    /**
     * Returns the reference.
     *
     * @return Returns the reference.
     */
    public String getReference()
    {
        return sReference;
    }

    /**
     * The reference to set.
     *
     * @param aReference The reference to set.
     */
    public void setReference(String aReference)
    {
        sReference = aReference;
    }

    /**
     * Returns the schema.
     *
     * @return Returns the schema.
     */
    public XmlStructure getSchema()
    {
        return xsSchema;
    }

    /**
     * The schema to set.
     *
     * @param aSchema The schema to set.
     */
    public void setSchema(XmlStructure aSchema)
    {
        xsSchema = aSchema;
    }

    /**
     * Returns the validateRule.
     *
     * @return Returns the validateRule.
     */
    public String getValidateRule()
    {
        return sValidateRule;
    }

    /**
     * The validateRule to set.
     *
     * @param aValidateRule The validateRule to set.
     */
    public void setValidateRule(String aValidateRule)
    {
        sValidateRule = aValidateRule;
    }

    /**
     * Returns the validateSchema.
     *
     * @return Returns the validateSchema.
     */
    public String getValidateSchema()
    {
        return sValidateSchema;
    }

    /**
     * The validateSchema to set.
     *
     * @param aValidateSchema The validateSchema to set.
     */
    public void setValidateSchema(String aValidateSchema)
    {
        sValidateSchema = aValidateSchema;
    }

    /**
     * Returns the persistsInDb.
     *
     * @return Returns the persistsInDb.
     */
    public String getPersistsInDb()
    {
        return sPersistsInDb;
    }

    /**
     * The persistsInDb to set.
     *
     * @param aPersistsInDb The persistsInDb to set.
     */
    public void setPersistsInDb(String aPersistsInDb)
    {
        sPersistsInDb = aPersistsInDb;
    }
    
    
}
