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

import java.util.List;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.CobocUtils;

/**
 * Contains attribute information defined in the decision case.
 *
 * @author mpoyhone
 */
public class DecisionCaseAttribute
{
    private String sChosenName;
    private String sName;
    private CobocContentHandle cchTemplateHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_TEMPLATE);
    
    public void getReferencedContent(List<IContentHandle> lResult)
    {
        if (cchTemplateHandle.isSet()) {
            lResult.add(cchTemplateHandle);
        }
    }

    public void updateReferences(BFContext bcContext, IContentSource csSrc) throws BFException
    {
        CobocUtils.updateCobocHandle(bcContext, cchTemplateHandle);
    }  

    /**
     * Returns the chosenName.
     *
     * @return Returns the chosenName.
     */
    public String getChosenName()
    {
        return sChosenName;
    }

    /**
     * The chosenName to set.
     *
     * @param aChosenName The chosenName to set.
     */
    public void setChosenName(String aChosenName)
    {
        sChosenName = aChosenName;
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

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return CobocUtils.beanToString(this, "DecisionCaseAttribute", true);
   }
}
