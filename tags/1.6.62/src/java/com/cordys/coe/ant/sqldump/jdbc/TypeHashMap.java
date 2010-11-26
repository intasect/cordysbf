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
package com.cordys.coe.ant.sqldump.jdbc;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is a standard hashmap but is extended with methods to get a value
 * as a certain type.
 *
 * @author pgussow
 */

public class TypeHashMap<K, V> extends LinkedHashMap<K, V>
{
    /**
     * Creates a new TypeHashMap object.
     */
    public TypeHashMap()
    {
        super();
    }

    /**
     * Creates a new TypeHashMap object.
     *
     * @param iInitialCapacity The initial capacipty.
     */
    public TypeHashMap(int iInitialCapacity)
    {
        super(iInitialCapacity);
    }

    /**
     * Creates a new TypeHashMap object.
     *
     * @param iInitialCapacity The initial capacipty.
     * @param fLoadFactor The load factor.
     */
    public TypeHashMap(int iInitialCapacity, float fLoadFactor)
    {
        super(iInitialCapacity, fLoadFactor);
    }

    /**
     * Creates a new TypeHashMap object.
     *
     * @param mMap The map whose mappings are to be placed in this map.
     */
    public TypeHashMap(Map<K, V> mMap)
    {
        super(mMap);
    }

    /**
     * This method returns the boolean value for the object.
     *
     * @param oKey The key in the hashmap.
     *
     * @return The boolean value for the object.
     */
    public boolean getBoolean(Object oKey)
    {
        boolean bReturn = false;

        if (containsKey(oKey))
        {
            Object oValue = get(oKey);

            if (oValue != null)
            {
                if (oValue instanceof String)
                {
                    if ("0".equals((String) oValue))
                    {
                        bReturn = true;
                    }
                    else if ("1".equals((String) oValue))
                    {
                        bReturn = false;
                    }
                    else
                    {
                        try
                        {
                            bReturn = Boolean.getBoolean((String) oValue);
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
                else if (oValue instanceof Boolean)
                {
                    bReturn = ((Boolean) oValue).booleanValue();
                }
                else
                {
                    try
                    {
                        bReturn = Boolean.getBoolean(oValue.toString());
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        return bReturn;
    }

    /**
     * This method returns the integer value for the object.
     *
     * @param oKey The key in the hashmap.
     *
     * @return The integer value for the object.
     */
    public int getInt(Object oKey)
    {
        int iReturn = -1;

        if (containsKey(oKey))
        {
            Object oValue = get(oKey);

            if (oValue != null)
            {
                if (oValue instanceof String)
                {
                    try
                    {
                        iReturn = Integer.parseInt((String) oValue);
                    }
                    catch (Exception e)
                    {
                    }
                }
                else if (oValue instanceof Integer)
                {
                    iReturn = ((Integer) oValue).intValue();
                }
                else
                {
                    try
                    {
                        iReturn = Integer.parseInt(oValue.toString());
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        return iReturn;
    }

    /**
     * This method returns the short value for the object.
     *
     * @param oKey The key in the hashmap.
     *
     * @return The short value for the object.
     */
    public short getShort(Object oKey)
    {
        short sReturn = -1;

        if (containsKey(oKey))
        {
            Object oValue = get(oKey);

            if (oValue != null)
            {
                if (oValue instanceof String)
                {
                    try
                    {
                        sReturn = Short.parseShort((String) oValue);
                    }
                    catch (Exception e)
                    {
                    }
                }
                else if (oValue instanceof Short)
                {
                    sReturn = ((Short) oValue).shortValue();
                }
                else
                {
                    try
                    {
                        sReturn = Short.parseShort(oValue.toString());
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        return sReturn;
    }

    /**
     * This method returns the value for this key as a string.
     *
     * @param oKey The key in the hashmap.
     *
     * @return The stringvalue for the key.
     */
    public String getString(Object oKey)
    {
        String sReturn = null;

        if (containsKey(oKey))
        {
            Object oValue = get(oKey);

            if (oValue != null)
            {
                if (oValue instanceof String)
                {
                    sReturn = (String) oValue;
                }
                else
                {
                    sReturn = oValue.toString();
                }
            }
        }
        return sReturn;
    }
}
