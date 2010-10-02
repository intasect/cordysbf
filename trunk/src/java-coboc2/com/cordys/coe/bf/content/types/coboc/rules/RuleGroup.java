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
package com.cordys.coe.bf.content.types.coboc.rules;

import java.util.ArrayList;

import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;


/**
 * Class containing CoBOC rule group information.
 *
 * @author mpoyhone
 */
public class RuleGroup extends CobocContentBase
{   
    private String sPriority;
    
    /**
     * Constructor for CobocFolderContentBase
     */
    public RuleGroup()
    {
        super(EContentType.COBOC_RULES_RULEGROUP);
        lSubContentList = new ArrayList<IContentHandle>(5);
    }

    /**
     * Returns the priority.
     *
     * @return Returns the priority.
     */
    public String getPriority()
    {
        return sPriority;
    }

    /**
     * The priority to set.
     *
     * @param aPriority The priority to set.
     */
    public void setPriority(String aPriority)
    {
        sPriority = aPriority;
    }

    /**
     * This also updates the name as this objects key.
     * @see com.cordys.coe.bf.content.types.coboc.CobocContentBase#setName(java.lang.String)
     */
    public void setName(String aName)
    {
        super.setName(aName);
        chHandle.setKey(aName);
    }
}
