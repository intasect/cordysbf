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

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.CobocUtils;

/**
 * Contains rule information defined in the decision case.
 *
 * @author mpoyhone
 */
public class DecisionCaseRule
{
    private String sRuleType;
    private String sRuleNumber;
    private List<ActionTemplate> lActions = new LinkedList<ActionTemplate>();
    private List<ConditionTemplate> lConditions = new LinkedList<ConditionTemplate>();
    
    public void getReferencedContent(List<IContentHandle> lResult)
    {
        List<IContentHandle> lRes = new LinkedList<IContentHandle>();
        
        for (ActionTemplate atTemplate : lActions)
        {
            Collection<IContentHandle> cTmpRes = atTemplate.getReferencedContent();

            if (cTmpRes != null) {
                lRes.addAll(cTmpRes);
            }
        }

       for (ConditionTemplate ctTemplate : lConditions)
       {
           Collection<IContentHandle> cTmpRes = ctTemplate.getReferencedContent();

           if (cTmpRes != null) {
               lRes.addAll(cTmpRes);
           }
       }
    }

    public void updateReferences(BFContext bcContext, IContentSource csSrc) throws BFException
    {
        for (ActionTemplate atTemplate : lActions)
        {
            CobocUtils.updateCobocHandle(bcContext, atTemplate.getHandle());
            atTemplate.updateReferences(csSrc);
        }

        for (ConditionTemplate ctTemplate : lConditions)
        {
            CobocUtils.updateCobocHandle(bcContext, ctTemplate.getHandle());
            ctTemplate.updateReferences(csSrc);
        }
    }  

    /**
     * Sets the context for this rule. This is needed to be able to set the context
     * for the action and condition templates
     * @param bcContext Current context.
     */
    public void setContext(BFContext aBcContext)
    {
        for (ActionTemplate atTemplate : lActions)
        {
            atTemplate.setContext(aBcContext);
        }

        for (ConditionTemplate ctTemplate : lConditions)
        {
            ctTemplate.setContext(aBcContext);
        }
    }
    
    /**
     * Returns the actions.
     *
     * @return Returns the actions.
     */
    public List<ActionTemplate> getActions()
    {
        return lActions;
    }
    /**
     * The actions to set.
     *
     * @param aActions The actions to set.
     */
    public void setActions(List<ActionTemplate> aActions)
    {
        lActions = aActions;
    }
    /**
     * Returns the conditions.
     *
     * @return Returns the conditions.
     */
    public List<ConditionTemplate> getConditions()
    {
        return lConditions;
    }
    /**
     * The conditions to set.
     *
     * @param aConditions The conditions to set.
     */
    public void setConditions(List<ConditionTemplate> aConditions)
    {
        lConditions = aConditions;
    }
    /**
     * Returns the ruleNumber.
     *
     * @return Returns the ruleNumber.
     */
    public String getRuleNumber()
    {
        return sRuleNumber;
    }
    /**
     * The ruleNumber to set.
     *
     * @param aRuleNumber The ruleNumber to set.
     */
    public void setRuleNumber(String aRuleNumber)
    {
        sRuleNumber = aRuleNumber;
    }
    /**
     * Returns the ruleType.
     *
     * @return Returns the ruleType.
     */
    public String getRuleType()
    {
        return sRuleType;
    }
    /**
     * The ruleType to set.
     *
     * @param aRuleType The ruleType to set.
     */
    public void setRuleType(String aRuleType)
    {
        sRuleType = aRuleType;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return CobocUtils.beanToString(this, "DecisionCaseRule", true);
   }
}
