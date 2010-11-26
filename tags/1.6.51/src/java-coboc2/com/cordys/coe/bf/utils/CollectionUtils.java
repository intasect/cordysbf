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
package com.cordys.coe.bf.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Collections related utils.
 *
 * @author mpoyhone
 */
public class CollectionUtils
{
    @SuppressWarnings("unchecked")
    public static Map arrayToHashMap(Object[] ... oaaArray) {
        Map mResMap = new HashMap();
        
        for (Object[] oaMapping : oaaArray)
        {
            mResMap.put(oaMapping[0], oaMapping[1]);
        }
        
        return mResMap;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Set<T> arrayToHashSet(T ... tElems) {
        Set<T> sResSet = new HashSet<T>();
        
        for (T tElem : tElems)
        {
            sResSet.add(tElem);
        }
        
        return sResSet;
    }
}
