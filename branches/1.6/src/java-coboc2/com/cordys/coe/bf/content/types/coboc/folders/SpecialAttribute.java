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
import com.cordys.coe.bf.utils.CobocUtils;


/**
 * Class containing CoBOC template special attribute information.
 *
 * @author mpoyhone
 */
public class SpecialAttribute extends CobocContentBase
{   
    private String sAttributeType;
    private String sPrimaryKey;

    /**
     * Constructor for SpecialAttribute
     */
    public SpecialAttribute()
    {
        this(EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE);
    }
    
    /**
     * Constructor for SpecialAttribute
     */
    public SpecialAttribute(EContentType ctType)
    {
        super(ctType);
        cchParentHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_TEMPLATE);
        sLastModified = "Not needed."; // These don't have this field. Just fill it to skip some checks.
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
        if (cchParentHandle != null && cchParentHandle.isSet()) {
            Template tTemplate = (Template) bcContext.findContent(cchParentHandle);
            
            if (tTemplate != null) {
                if (! tTemplate.getChildren().contains(this)) {
                    tTemplate.addChild(chHandle);
                }
                
                generateKey(tTemplate); 
            }
            
            if (csSrc.getType()  == EContentSourceType.BCP ||
                csSrc.getType()  == EContentSourceType.ISV) {
                CobocUtils.updateCobocHandle(bcContext, cchParentHandle);
            }
        }
    }
    
    /**
     * Generates the key based on the template key. Format is "[template key]/[attribute name]"
     * @return <code>true</code> if the key was generated succesfully.
     */
    public boolean generateKey(Template tTemplate) {
        if (tTemplate != null) {
            // Generate our key based on the template key.
            String sParentKey = tTemplate.getKey();
            
            if (sParentKey != null) {
                chHandle.setKey(sParentKey + "/" + getName());
                return true;
            }                
        }        
        
        return false;
    }

    /**
     * Returns the attributeType.
     *
     * @return Returns the attributeType.
     */
    public String getAttributeType()
    {
        return sAttributeType;
    }

    /**
     * The attributeType to set.
     *
     * @param aAttributeType The attributeType to set.
     */
    public void setAttributeType(String aAttributeType)
    {
        sAttributeType = aAttributeType;
    }

    /**
     * Returns the primaryKey.
     *
     * @return Returns the primaryKey.
     */
    public String getPrimaryKey()
    {
        return sPrimaryKey;
    }

    /**
     * The primaryKey to set.
     *
     * @param aPrimaryKey The primaryKey to set.
     */
    public void setPrimaryKey(String aPrimaryKey)
    {
        sPrimaryKey = aPrimaryKey;
    }
}
