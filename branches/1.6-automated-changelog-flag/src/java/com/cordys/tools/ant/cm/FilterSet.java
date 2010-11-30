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
package com.cordys.tools.ant.cm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A class that contains a set of Filter objects.
 *
 * @author mpoyhone
 */
public class FilterSet
{
    /**
     * Contains all configured filters.
     */
    private List<Filter> lFilterList = new LinkedList<Filter>();

    /**
     * Returns true if this filter set has no filters.
     *
     * @return true if this filter set has no filters.
     */
    public boolean isEmpty()
    {
        return lFilterList.isEmpty();
    }
    
    /**
     * This method returns the filterlist.
     * 
     * @return The filterlist.
     */
    public List<Filter> getFilterList()
    {
        return lFilterList;
    }
    
    /**
     * Returns true if the given path is accepted by configured content
     * filters.
     *
     * @param sPath The path to be tested.
     *
     * @return True if the given path is accepted by configured content
     *         filters.
     */
    public boolean isPathAccepted(String sPath) {
    	return isPathAccepted(sPath, false);
    }

    /**
     * Returns true if the given path is accepted by configured content
     * filters.
     *
     * @param sPath The path to be tested.
     * @param bIsParentDirectory If true the filter is tested for parent directory match. 
     *
     * @return True if the given path is accepted by configured content
     *         filters.
     */
    public boolean isPathAccepted(String sPath, boolean bIsParentDirectory)
    {
        boolean bHasInclusiveFilters = false;

        for (Iterator<Filter> iIter = lFilterList.iterator(); iIter.hasNext();)
        {
            Filter fFilter = iIter.next();
            boolean bMatches = fFilter.checkMatch(sPath, bIsParentDirectory);

            if (fFilter.getInclusive())
            {
                if (bMatches)
                {
                    return true;
                }

                bHasInclusiveFilters = true;
            }
            else
            {
                if (bMatches)
                {
                    return false;
                }
            }
        }

        return !bHasInclusiveFilters;
    }

    /**
     * Adds a new filters to this set.
     *
     * @param fFilter The filter to be added.
     */
    public void addFilter(Filter fFilter)
    {
        lFilterList.add(fFilter);
    }
}
