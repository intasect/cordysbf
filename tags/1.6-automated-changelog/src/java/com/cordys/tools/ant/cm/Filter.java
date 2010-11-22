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

import com.cordys.tools.ant.util.GeneralUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * Content filter Ant element.
 *
 * @author  mpoyhone
 */
public class Filter extends MatchingTask
{
    /**
     * Indicated wheter this filter is inclusive or excelusive filter.
     */
    private boolean bIsInclusive = true;
    /**
     * Contains path configured by 'path' attribute.
     */
    private FilterObject foFilterObject;

    /**
     * Creates a new Filter object.
     */
    public Filter()
    {
    }

    /**
     * Creates a new Filter object.
     *
     * @param  sPath         Filter path
     * @param  bIsInclusive  Inclusive flag.
     */
    public Filter(String sPath, boolean bIsInclusive)
    {
        setPath(sPath);
        setInclusive(bIsInclusive);
    }

    /**
     * Returns true is this filter is inclusive.
     *
     * @return  true is this filter is inclusive.
     */
    public boolean getInclusive()
    {
        return bIsInclusive;
    }

    /**
     * Returns the path attribute.
     *
     * @return  Path value
     */
    public String getPath()
    {
        return foFilterObject.getFilterString();
    }

    /**
     * Returns the regexp attribute.
     *
     * @return  Path value
     */
    public String getRegexp()
    {
        return foFilterObject.getFilterString();
    }

    /**
     * Sets this filter as inclusive or exclusive.
     *
     * @param  bValue  If true, this filter is inclusive.
     */
    public void setInclusive(boolean bValue)
    {
        bIsInclusive = bValue;
    }

    /**
     * Sets the path attribute.
     *
     * @param  sPath  Path value.
     */
    public void setPath(String sPath)
    {
        foFilterObject = new FilterObject(GeneralUtils.safeTrim(sPath), true);
    }

    /**
     * Sets the regexp attribute.
     *
     * @param  sRegexp  Path value.
     */
    public void setRegexp(String sRegexp)
    {
        foFilterObject = new FilterObject(sRegexp, false);
    }

    /**
     * Returns true if the given path is accepted by this content filter.
     *
     * @param   sPath               The path to be tested.
     * @param   bIsParentDirectory  If true the filter is tested for parent directory match.
     *
     * @return  True if the given path is accepted by this content filter.
     */
    protected boolean checkMatch(String sPath, boolean bIsParentDirectory)
    {
        if (!bIsParentDirectory)
        {
            return foFilterObject.checkMatch(sPath);
        }

        return foFilterObject.checkDirectoryMatch(sPath);
    }

    /**
     * Holds and parses the filter regexp.
     *
     * @author  mpoyhone
     */
    protected class FilterObject
    {
        /**
         * Filter regexp.
         */
        protected Pattern pPattern;
        /**
         * Original filter string.
         */
        protected String sOrigFilterStr;

        /**
         * Creates a new FilterObject object.
         *
         * @param  sFilterStr        Filter string
         * @param  bTranslateFilter  If true, the string is translated into a regexp
         */
        public FilterObject(String sFilterStr, boolean bTranslateFilter)
        {
            sOrigFilterStr = sFilterStr;

            if (bTranslateFilter)
            {
                // Convert simple wild-card to a regexp. This uses unicode character \u0001 as a
                // marker for * character so it will not be replaced twice. Escape special
                // characters
                sFilterStr = sFilterStr.replaceAll("([\\[\\]\\(\\)\\{\\}$])", "\\\\$1");

                // Change /path/** notation to /path/.*
                sFilterStr = sFilterStr.replaceAll("/\\*\\*$", "/.\u0001");
                // Change /path/**/path notation to /path/.*/
                sFilterStr = sFilterStr.replaceAll("/\\*\\*/", "/.\u0001/");
                // Change /path/*/path notation /path/[^/]*/path
                sFilterStr = sFilterStr.replaceAll("\\*", "[^/]\u0001");
                // Replace the marker with *
                sFilterStr = sFilterStr.replace('\u0001', '*');
            }
            
            // Remove first slash
            if (sFilterStr.startsWith("/")) {
                sFilterStr = sFilterStr.substring(1);
            }

            try
            {
                pPattern = Pattern.compile(sFilterStr, Pattern.CASE_INSENSITIVE);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Invalid filter pattern '" + sOrigFilterStr +
                                                   "' : " + e);
            }
        }

        /**
         * Checks if the given directory path matches the filter. NOTE: This is a really simple
         * implementation.
         *
         * @param   sPath  The directorty path
         *
         * @return  True, if the path was accepted.
         */
        public boolean checkDirectoryMatch(String sPath)
        {
            // First check if this is a sub-directory in the filter.
            if (bIsInclusive && checkSubPathMatch(sOrigFilterStr, sPath))
            {
                return true;
            }

            if (!sPath.endsWith("/"))
            {
                sPath += "/";
            }

            sPath += "a"; // Add a test file name.

            return checkMatch(sPath);
        }

        /**
         * Check if the path is accepted by this filter.
         *
         * @param   sPath  The path to be tested
         *
         * @return  True, if the path was accepted.
         */
        public boolean checkMatch(String sPath)
        {
            // Remove first slash
            if (sPath.startsWith("/")) {
                sPath = sPath.substring(1);
            }
            
            Matcher mMatcher = pPattern.matcher(sPath);

            return mMatcher.matches();
        }

        /**
         * Returns the filter string.
         *
         * @return  Returns the filter string
         */
        public String getFilterString()
        {
            return sOrigFilterStr;
        }

        /**
         * Returns the filter regexp pattern.
         *
         * @return  Returns the filter regexp pattern
         */
        public Pattern getPattern()
        {
            return pPattern;
        }

        /**
         * Checks if the sub-path matches the beginning of the filter. If we have filter '/a/b/' we
         * can test if '/a' matches.
         *
         * @param   sFilter   The filter key string.
         * @param   sSubPath  The sub-path string.
         *
         * @return  True if the filter matches.
         */
        protected boolean checkSubPathMatch(String sFilter, String sSubPath)
        {
            // If the filter is shorter than the path, the path cannot match.
            if (sSubPath.length() > sFilter.length())
            {
                return false;
            }

            // If both have the same length, they can be checked for equality.
            if (sSubPath.length() == sFilter.length())
            {
                return sSubPath.equals(sFilter);
            }

            // Now the filter must start with the path if the path is supposed
            // to match.
            if (!sFilter.startsWith(sSubPath))
            {
                return false;
            }

            int ch;

            // Get the character in the path key that is right after the filter.
            ch = sFilter.charAt(sSubPath.length());

            if (ch != '/')
            {
                // The folder names do not match completely (filter is a substring
                // of the path key folder name).
                return false;
            }

            // Path key matches the filter.
            return true;
        }
    }
}
