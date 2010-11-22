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
package com.cordys.coe.util;

import java.util.Comparator;

/**
 * A simple comparator for comparing version strings properly. For example a string comparator would
 * put 1.0.10 before 1.0.9. Following version formats are supported:
 *
 * <ul>
 *   <li>1.2.3</li>
 *   <li>1-2-3</li>
 *   <li>1_2_3</li>
 * </ul>
 *
 * @author  mpoyhone
 */
public class VersionNumberComparator
    implements Comparator<String>
{
    /**
     * Sort order.
     */
    private boolean bAscending = true;

    /**
     * Constructor for VersionNumberComparator. Sorts in ascending order.
     */
    public VersionNumberComparator()
    {
    }

    /**
     * Constructor for VersionNumberComparator.
     *
     * @param  bAscending  Sort order.
     */
    public VersionNumberComparator(boolean bAscending)
    {
        this.bAscending = bAscending;
    }

    /**
     * @see  java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(String o1, String o2)
    {
        return (bAscending ? 1 : (-1)) * internalCompare(o1, o2);
    }

    /**
     * @see  java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    private int internalCompare(String o1, String o2)
    {
        if (o1 == null)
        {
            return (o2 != null) ? (-1) : 0;
        }

        if (o2 == null)
        {
            return 1;
        }

        String[] a1 = o1.split("[-_.]");
        String[] a2 = o2.split("[-_.]");
        int iElems = Math.min(a1.length, a2.length);

        for (int i = 0; i < iElems; i++)
        {
            int i1;
            int i2;

            try
            {
                i1 = Integer.parseInt(a1[i]);
            }
            catch (NumberFormatException e)
            {
                // Not a number, so use a string comparison.
                int iRes = a1[i].compareToIgnoreCase(a2[i]);

                if (iRes != 0)
                {
                    return iRes;
                }

                continue;
            }

            try
            {
                i2 = Integer.parseInt(a2[i]);
            }
            catch (NumberFormatException e)
            {
                // Not a number, so use a string comparison.
                int iRes = a1[i].compareToIgnoreCase(a2[i]);

                if (iRes != 0)
                {
                    return iRes;
                }

                continue;
            }

            if (i1 != i2)
            {
                return i1 - i2;
            }
        }

        // All the digits are equal, so compare the array lengths.
        return a1.length - a2.length;
    }
}
