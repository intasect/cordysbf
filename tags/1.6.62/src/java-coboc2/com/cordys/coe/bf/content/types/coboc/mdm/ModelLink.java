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

import java.util.List;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.CobocUtils;
import com.cordys.coe.bf.utils.CobocXmlStructure;

/**
 * A simple bean for links in the MDM model object.
 *
 * @author mpoyhone
 */
public class ModelLink
{
    private CobocContentHandle cchSodHandle;
    private String sSodType;
    private String sSodName;
    
    private CobocContentHandle cchSodEntityHandle;
    private String sSodEntityTemplate;
    private String sSodEntityName;

    private CobocContentHandle cchCodEntityHandle;
    private String sCodEntityTemplate;
    private String sCodEntityName;
    
    private CobocXmlStructure xSubscription;
    
    /**
     * Constructor for ModelBackendRef
     */
    public ModelLink() {
        cchSodHandle = new CobocContentHandle(EContentType.MDM_BACKEND);
        cchSodEntityHandle = new CobocContentHandle(EContentType.MDM_ENTITY);
        cchCodEntityHandle = new CobocContentHandle(EContentType.MDM_ENTITY);
    }
    
    public void getReferencedContent(List<IContentHandle> lResult)
    {
        if (cchSodHandle != null && cchSodHandle.isSet()) {
            lResult.add(cchSodHandle);
        }
        
        if (cchSodEntityHandle != null && cchSodEntityHandle.isSet()) {
            lResult.add(cchSodEntityHandle);
        }
        
        if (cchCodEntityHandle != null && cchCodEntityHandle.isSet()) {
            lResult.add(cchCodEntityHandle);
        }
    }

    public void updateReferences(BFContext bcContext, IContentSource csSrc) throws BFException
    {
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType()  == EContentSourceType.ISV) {
            CobocUtils.updateCobocHandle(bcContext, cchSodHandle);
            CobocUtils.updateCobocHandle(bcContext, cchSodEntityHandle);
            CobocUtils.updateCobocHandle(bcContext, cchCodEntityHandle);
        }
    }   
    
    /**
     * Returns the cchCodEntityHandle.
     *
     * @return Returns the cchCodEntityHandle.
     */
    public CobocContentHandle getCodEntityHandle()
    {
        return cchCodEntityHandle;
    }

    /**
     * The cchCodEntityHandle to set.
     *
     * @param aCchCodEntityHandle The cchCodEntityHandle to set.
     */
    public void setCodEntityHandle(CobocContentHandle aCchCodEntityHandle)
    {
        cchCodEntityHandle = aCchCodEntityHandle;
    }

    /**
     * Returns the cchSodEntityHandle.
     *
     * @return Returns the cchSodEntityHandle.
     */
    public CobocContentHandle getSodEntityHandle()
    {
        return cchSodEntityHandle;
    }

    /**
     * The cchSodEntityHandle to set.
     *
     * @param aCchSodEntityHandle The cchSodEntityHandle to set.
     */
    public void setSodEntityHandle(CobocContentHandle aCchSodEntityHandle)
    {
        cchSodEntityHandle = aCchSodEntityHandle;
    }

    /**
     * Returns the cchSodHandle.
     *
     * @return Returns the cchSodHandle.
     */
    public CobocContentHandle getSodHandle()
    {
        return cchSodHandle;
    }

    /**
     * The cchSodHandle to set.
     *
     * @param aCchSodHandle The cchSodHandle to set.
     */
    public void setSodHandle(CobocContentHandle aCchSodHandle)
    {
        cchSodHandle = aCchSodHandle;
    }

    /**
     * Returns the codEntityName.
     *
     * @return Returns the codEntityName.
     */
    public String getCodEntityName()
    {
        return sCodEntityName;
    }

    /**
     * The codEntityName to set.
     *
     * @param aCodEntityName The codEntityName to set.
     */
    public void setCodEntityName(String aCodEntityName)
    {
        sCodEntityName = aCodEntityName;
    }

    /**
     * Returns the codEntityTemplate.
     *
     * @return Returns the codEntityTemplate.
     */
    public String getCodEntityTemplate()
    {
        return sCodEntityTemplate;
    }

    /**
     * The codEntityTemplate to set.
     *
     * @param aCodEntityTemplate The codEntityTemplate to set.
     */
    public void setCodEntityTemplate(String aCodEntityTemplate)
    {
        sCodEntityTemplate = aCodEntityTemplate;
    }

    /**
     * Returns the sodEntityName.
     *
     * @return Returns the sodEntityName.
     */
    public String getSodEntityName()
    {
        return sSodEntityName;
    }

    /**
     * The sodEntityName to set.
     *
     * @param aSodEntityName The sodEntityName to set.
     */
    public void setSodEntityName(String aSodEntityName)
    {
        sSodEntityName = aSodEntityName;
    }

    /**
     * Returns the sodEntityTemplate.
     *
     * @return Returns the sodEntityTemplate.
     */
    public String getSodEntityTemplate()
    {
        return sSodEntityTemplate;
    }

    /**
     * The sodEntityTemplate to set.
     *
     * @param aSodEntityTemplate The sodEntityTemplate to set.
     */
    public void setSodEntityTemplate(String aSodEntityTemplate)
    {
        sSodEntityTemplate = aSodEntityTemplate;
    }

    /**
     * Returns the sodName.
     *
     * @return Returns the sodName.
     */
    public String getSodName()
    {
        return sSodName;
    }

    /**
     * The sodName to set.
     *
     * @param aSodName The sodName to set.
     */
    public void setSodName(String aSodName)
    {
        sSodName = aSodName;
    }

    /**
     * Returns the sodType.
     *
     * @return Returns the sodType.
     */
    public String getSodType()
    {
        return sSodType;
    }

    /**
     * The sodType to set.
     *
     * @param aSodType The sodType to set.
     */
    public void setSodType(String aSodType)
    {
        sSodType = aSodType;
    }

    /**
     * Returns the subscription.
     *
     * @return Returns the subscription.
     */
    public CobocXmlStructure getSubscription()
    {
        return xSubscription;
    }

    /**
     * The subscription to set.
     *
     * @param aSubscription The subscription to set.
     */
    public void setSubscription(CobocXmlStructure aSubscription)
    {
        xSubscription = aSubscription;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return CobocUtils.beanToString(this, "ModelLink", true);
   }
    
}
