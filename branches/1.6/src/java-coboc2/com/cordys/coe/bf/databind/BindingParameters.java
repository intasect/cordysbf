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
package com.cordys.coe.bf.databind;

import java.util.HashMap;
import java.util.Map;

import com.cordys.coe.bf.content.base.ContentTuple;

/**
 * Contains all different state parameters that can be passed to the binding context.
 *
 * @author mpoyhone
 */
public class BindingParameters
{
    /**
     * Mode of update when binding BCP tuple-old formats. 
     */
    private IBindingConstants.EUpdateMode umUpdateMode = IBindingConstants.EUpdateMode.NONE;
    /**
     * A map of generic parameters.
     */
    private Map<String, Object> mParameters = new HashMap<String, Object>();
    /**
     * Sets the BCP update mode from the tuple.
     * @param ctTuple Tuple.
     */
    public void setBcpUpdateModeFromTuple(ContentTuple ctTuple) {
        if (ctTuple.isInsert()) {
            umUpdateMode = IBindingConstants.EUpdateMode.INSERT;
        } else if (ctTuple.isUpdate()) {
            umUpdateMode = IBindingConstants.EUpdateMode.UPDATE;
        } else if (ctTuple.isDelete()) {
            umUpdateMode = IBindingConstants.EUpdateMode.DELETE;
        } else {
            umUpdateMode = IBindingConstants.EUpdateMode.NONE;
        }
    }
    
    /**
     * Returns the update mode for BCP update operations.
     * @return BCP update mode.
     */
    public IBindingConstants.EUpdateMode getBcpUpdateMode() {
        return umUpdateMode;
    }
    
    /**
     * Sets a generic parameter.
     * @param sName Parameter name.
     * @param oValue Parameter value.
     */
    public void setParameter(String sName, Object oValue) {
        mParameters.put(sName, oValue);
    }
    
    /**
     * Returns a generic parameter.
     * @param sName Parameter name.
     * @return Parameter value or <code>null</code> if the parameter is not set.
     */
    public Object getParameter(String sName) {
        return mParameters.get(sName);
    }    
}
