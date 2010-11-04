/**
 * Copyright 2009 Cordys R&D B.V. 
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
package com.cordys.coe.bf.content.base.impl;

import com.cordys.coe.bf.content.base.IContentFilter;
import com.cordys.coe.bf.content.types.EContentType;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements a content filter for CoBOC content. This handles hierarchical structures with wild
 * cards and also subfolders (e.g. "3.Business Process Models/MyFlows/TestFlow1" will match
 * "/Business Processes/3.Business Process Models/MyFlows/TestFlow1", "/Business
 * Processes/BPML/3.Business Process Models/MyFlows/TestFlow1" and "/Business
 * Processes/BPMN/3.Business Process Models/MyFlows/TestFlow1").
 *
 * @author  mpoyhone
 */
public class CobocContentFilter
    implements IContentFilter
{
    /**
     * Contains all parsed filters.
     */
    private List<FilterRootEntry> entryList = new ArrayList<FilterRootEntry>(16);

    /**
     * Creates a regular expression from glob-pattern. Supported formats:
     *
     * <pre>
         a/**&nbsp;/b
         a/*.xml
         a/*
         a*&nbsp;/**&nbsp;/b.xml
     * </pre>
     *
     * @param   globPattern        Glob pattern to be converted.
     * @param   useForwardSlashes  If <code>true</code> backslashes are converted to forward slashes
     *                             in the regexp.
     * @param   regexFlags         Standard regex flags to be used in the returned pattern.
     *
     * @return  Converted regexp.
     */
    public static Pattern createGlobRegex(String globPattern, boolean useForwardSlashes,
                                          int regexFlags)
    {
        StringBuilder sb = new StringBuilder(512);

        sb.append('^');

        for (int j = 0; j < globPattern.length(); j++)
        {
            char ch = globPattern.charAt(j);

            switch (ch)
            {
                case '\\':
                    if (useForwardSlashes)
                    {
                        sb.append('/');
                    }
                    else
                    {
                        // Escape \
                        sb.append("\\\\");
                    }
                    break;

                case '?':
                    sb.append('.');
                    break;

                case '*':
                    if ((j < (globPattern.length() - 1)) && (globPattern.charAt(j + 1) == '*'))
                    {
                        // This is **, so match everything.
                        sb.append(".+");
                        j++;
                    }
                    else
                    {
                        // This is *, so match only file and folder names.
                        sb.append("[^/]+");
                    }
                    break;

                case '.':
                case '[':
                case ']':
                case '(':
                case ')':
                case '{':
                case '}':
                case '^':
                case '$':
                case '|':
                    // Escape these regexp characters.
                    sb.append("\\").append(ch);
                    break;

                default:
                    sb.append(ch);
                    break;
            }
        }

        sb.append('$');

        return Pattern.compile(sb.toString(), regexFlags);
    }

    /**
     * Adds a filter entry.
     *
     * @param  path       Path to add.
     * @param  isInclude  If <code>true</code>, the path is included. Otherwise excluded.
     */
    public void addFilter(String path, boolean isInclude)
    {
        String[] parts = parsePath(path);
        FilterRootEntry entry = new FilterRootEntry(parts[0], isInclude);

        if (parts.length > 1)
        {
            entry.pushFilter(parts, 1);
        }

        entryList.add(entry);
    }

    /**
     * Adds a filter entry.
     *
     * @param  path         Path to add.
     * @param  isInclude    If <code>true</code>, the path is included. Otherwise excluded.
     * @param  contentType  Content type of this path.
     */
    public void addFilter(String path, boolean isInclude, EContentType contentType)
    {
        switch (contentType)
        {
            case COBOC_FOLDERS_PROCESSTEMPLATE:
            case COBOC_FOLDERS_PROCESSBPML:
            case COBOC_FOLDERS_PROCESSBPMN:
                if (path.endsWith("**"))
                {
                    // This needs to become: /Business Processes/path/**/*_*.bpm
                    path += "/*";
                }

                addFilter("/Business Processes/" + path + "_*.bpm", isInclude);
                addFilter("/Business Processes/BPML/" + path + "_*.bpm/*", isInclude);
                addFilter("/Business Processes/BPMN/" + path + "_*.bpm/*", isInclude);
                break;

            default:
                addFilter(path, isInclude);
                break;
        }
    }

    /**
     * @see  com.cordys.coe.bf.content.base.IContentFilter#checkFolderAccess(java.lang.String)
     */
    @Override
    public boolean checkFolderAccess(String path)
    {
        if (path.equals("/"))
        {
            // Root folder always matches.
            return true;
        }

        String[] parts = parsePath(path);
        boolean hasIncludeFilters = false;

        for (FilterRootEntry entry : entryList)
        {
            if (entry.isInclude)
            {
                hasIncludeFilters = true;
            }

            switch (entry.match(parts, 0))
            {
                case FULL_MATCH:
                    return entry.isInclude;

                case PARTIAL_MATCH:
                    if (entry.isInclude)
                    {
                        return true;
                    }
                    break;
            }
        }

        return !hasIncludeFilters;
    }

    /**
     * @see  com.cordys.coe.bf.content.base.IContentFilter#checkItemAccess(java.lang.String, com.cordys.coe.bf.content.types.EContentType)
     */
    @Override
    public boolean checkItemAccess(String path, EContentType contentType)
    {
        if (path == null || path.length() == 0)
        {
            return false;
        }
        
        String[] parts = parsePath(path);
        boolean hasIncludeFilters = false;

        for (FilterRootEntry entry : entryList)
        {
            if (entry.isInclude)
            {
                hasIncludeFilters = true;
            }

            switch (entry.match(parts, 0))
            {
                case FULL_MATCH:
                    return entry.isInclude;
            }
        }

        return !hasIncludeFilters;
    }

    /**
     * @see  java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder(2048);

        buf.append("[ ");

        for (FilterRootEntry entry : entryList)
        {
            buf.append(entry.isInclude ? "I" : "E");
            buf.append(":\"");

            Entry tmp = entry;

            while (tmp != null)
            {
                buf.append("/").append(tmp.origPattern);
                tmp = tmp.childEntry;
            }

            buf.append("\", ");
        }

        buf.append(" ]");

        return buf.toString();
    }

    /**
     * Parses the path into parts.
     *
     * @param   path  Path to parse.
     *
     * @return  Parts.
     */
    private static String[] parsePath(String path)
    {
        if ((path == null) || (path.length() == 0))
        {
            return new String[0];
        }

        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }

        path = path.replace("//", "/");

        return path.split("/");
    }

    /**
     * Defines match type.
     *
     * @author  mpoyhone
     */
    public enum EMatchType
    {
        /**
         * No match.
         */
        NO_MATCH,
        /**
         * Beginning of the path match (parent folder match).
         */
        PARTIAL_MATCH,
        /**
         * Path matched completely.
         */
        FULL_MATCH;
    }

    /**
     * Filter entry. This contains the parsed filter expression.
     *
     * @author  mpoyhone
     */
    static class Entry
    {
        /**
         * Child entry.
         */
        Entry childEntry = null;
        /**
         * Original glob pattern.
         */
        String origPattern;
        /**
         * Regular expression for parsing the glob pattern.
         */
        Pattern regex;
        /**
         * If <code>true</code>, this is "**".
         */
        boolean subtreeMatch;

        /**
         * Constructor for Entry.
         *
         * @param  pattern  Filter pattern.
         */
        Entry(String pattern)
        {
            this.origPattern = pattern.trim();

            if (origPattern.equals("**"))
            {
                subtreeMatch = true;
            }
            else
            {
                this.regex = createGlobRegex(origPattern, true, Pattern.CASE_INSENSITIVE);
            }
        }

        /**
         * Tries to match the given part with this entry.
         *
         * @param   parts  Path parts. The part pointed by index is used.
         * @param   index  Current index in the part array.
         *
         * @return  Match type.
         */
        EMatchType match(String[] parts, int index)
        {
            if ((index < 0) || (index >= parts.length))
            {
                throw new IllegalArgumentException("Invalid part index: " + index +
                                                   ". Must be [0, " + parts.length + "]");
            }

            String part = parts[index];

            if (subtreeMatch)
            {
                // This is a ** wildcard
                if (childEntry == null)
                {
                    // This matches the whole subtree.
                    return EMatchType.FULL_MATCH;
                }
                
                // Try to find a part which matches our child.
                // This is pattern /a/b/**/x trying to match /a/b/c/x and /a/b/c/d/x
                for (int i = index; i < parts.length; i++)
                {
                    EMatchType result = childEntry.match(parts, i);

                    if (result != EMatchType.NO_MATCH)
                    {
                        return result;
                    }
                }
                
                // Lastly try to match the child directly (i.e. ** does not match anything).
                // This is needed for /a/b/**/x to match /a/b/x
                return childEntry.match(parts, index);
            }
            
            Matcher m = regex.matcher(part);

            if (!m.matches())
            {
                return EMatchType.NO_MATCH;
            }

            if (index == (parts.length - 1))
            {
                if (subtreeMatch)
                {
                    return EMatchType.FULL_MATCH;
                }

                return (childEntry != null) ? EMatchType.PARTIAL_MATCH : EMatchType.FULL_MATCH;
            }

            return (childEntry != null) ? childEntry.match(parts, index + 1) : EMatchType.NO_MATCH;
        }

        /**
         * Pushes the parts into this filter.
         *
         * @param  parts  Parts array.
         * @param  index  Starting index.
         */
        void pushFilter(String[] parts, int index)
        {
            // Parse the part into an entry.
            Entry entry = new Entry(parts[index]);

            childEntry = entry;

            if (index < (parts.length - 1))
            {
                // Push the rest of the parts.
                entry.pushFilter(parts, index + 1);
            }
        }
    }

    /**
     * Root entry for filters.
     *
     * @author  mpoyhone
     */
    static class FilterRootEntry extends Entry
    {
        /**
         * Whether this filter is includes or excludes paths.
         */
        private boolean isInclude;

        /**
         * Constructor for FilterRootEntry.
         *
         * @param  pattern    Path
         * @param  isInclude  Whether this filter is includes or excludes paths.
         */
        protected FilterRootEntry(String pattern, boolean isInclude)
        {
            super(pattern);

            this.isInclude = isInclude;
        }
    }
}
