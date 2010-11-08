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

import java.util.Iterator;
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
 * A simple bean for backend references in the MDM model object.
 *
 * @author mpoyhone
 */
public class ModelBackendRef
{
    /**
     * Contains list of ModelBackendEntityRef objects.
     */
    private List<ModelBackendEntityRef> lEntityRefs;
    /**
     * Contains handle of the real Backend object.
     */
    private CobocContentHandle cchBackendHandle;
    
    private String sTop;
    private String sLeft;
    private String sHeight;
    private String sWidth;
    private String sComplexRelationsBackend;
    
    /**
     * Constructor for ModelBackendRef
     */
    public ModelBackendRef() {
        cchBackendHandle = new CobocContentHandle(EContentType.MDM_BACKEND);
    }
    
    public void getReferencedContent(List<IContentHandle> lResult)
    {
        if (cchBackendHandle != null && cchBackendHandle.isSet()) {
            lResult.add(cchBackendHandle);
        }
        
        for (Iterator<ModelBackendEntityRef> iIter = lEntityRefs.iterator(); iIter.hasNext();)
        {
            ModelBackendEntityRef mbefEntity = iIter.next();
            
            mbefEntity.getReferencedContent(lResult);
        } 
    }

    public void updateReferences(BFContext bcContext, IContentSource csSrc) throws BFException
    {
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType()  == EContentSourceType.ISV) {
            CobocUtils.updateCobocHandle(bcContext, cchBackendHandle);
        }
        
        for (Iterator<ModelBackendEntityRef> iIter = lEntityRefs.iterator(); iIter.hasNext();)
        {
            ModelBackendEntityRef mbefEntity = iIter.next();
            
            mbefEntity.updateReferences(bcContext, csSrc);
        } 
    }   

    /**
     * Returns the entityRefs.
     *
     * @return Returns the entityRefs.
     */
    public List<ModelBackendEntityRef> getEntityRefs()
    {
        return lEntityRefs;
    }

    /**
     * The entityRefs to set.
     *
     * @param aEntityRefs The entityRefs to set.
     */
    public void setEntityRefs(List<ModelBackendEntityRef> aEntityRefs)
    {
        lEntityRefs = aEntityRefs;
    }
    
    /**
     * Returns the key.
     *
     * @return Returns the key.
     */
    public String getKey()
    {
        return cchBackendHandle.getKey();
    }
    /**
     * The key to set.
     *
     * @param aKey The key to set.
     */
    public void setKey(String aKey)
    {
        cchBackendHandle.setKey(aKey);
    }
    /**
     * Returns the objectId.
     *
     * @return Returns the objectId.
     */
    public String getObjectId()
    {
        return cchBackendHandle.getObjectId();
    }
    /**
     * The objectId to set.
     *
     * @param aObjectId The objectId to set.
     */
    public void setObjectId(String aObjectId)
    {
        cchBackendHandle.setObjectId(aObjectId);
    }    
    
    /**
     * Returns the height.
     *
     * @return Returns the height.
     */
    public String getHeight()
    {
        return sHeight;
    }


    /**
     * The height to set.
     *
     * @param aHeight The height to set.
     */
    public void setHeight(String aHeight)
    {
        sHeight = aHeight;
    }


    /**
     * Returns the left.
     *
     * @return Returns the left.
     */
    public String getLeft()
    {
        return sLeft;
    }


    /**
     * The left to set.
     *
     * @param aLeft The left to set.
     */
    public void setLeft(String aLeft)
    {
        sLeft = aLeft;
    }


    /**
     * Returns the top.
     *
     * @return Returns the top.
     */
    public String getTop()
    {
        return sTop;
    }


    /**
     * The top to set.
     *
     * @param aTop The top to set.
     */
    public void setTop(String aTop)
    {
        sTop = aTop;
    }


    /**
     * Returns the width.
     *
     * @return Returns the width.
     */
    public String getWidth()
    {
        return sWidth;
    }


    /**
     * The width to set.
     *
     * @param aWidth The width to set.
     */
    public void setWidth(String aWidth)
    {
        sWidth = aWidth;
    }  
    

    /**
     * Returns the complexRelationsBackend.
     *
     * @return Returns the complexRelationsBackend.
     */
    public String getComplexRelationsBackend()
    {
        return sComplexRelationsBackend;
    }

    /**
     * The complexRelationsBackend to set.
     *
     * @param aComplexRelationsBackend The complexRelationsBackend to set.
     */
    public void setComplexRelationsBackend(String aComplexRelationsBackend)
    {
        sComplexRelationsBackend = aComplexRelationsBackend;
    }     

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return CobocUtils.beanToString(this, "ModelBackendRef", true);
   }        
}
