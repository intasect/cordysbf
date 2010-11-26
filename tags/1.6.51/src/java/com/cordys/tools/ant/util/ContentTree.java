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
package com.cordys.tools.ant.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A class that holds content elements base on a tree structure.
 *
 * @author mpoyhone
 */
public class ContentTree
{
    /**
     * A map that for inserted nodes. This is used to speed up the insertion
     * process.
     */
    protected Map<String, TreeNode> mNodeMap = new HashMap<String, TreeNode>();
    /**
     * Tree's root node
     */
    protected TreeNode tnRootNode;
    /**
     * Tree path separator character.
     */
    protected char cNodeSeparator;

    /**
     * Creates a new ContentTree object.
     *
     * @param cSeparator Tree path separator character.
     */
    public ContentTree(String sRootPath, char cSeparator)
    {
        cNodeSeparator = cSeparator;
        
        tnRootNode = new TreeNode();
        tnRootNode.sName = "";
        tnRootNode.sKey = sRootPath;
        tnRootNode.oData = null;
        mNodeMap.put(sRootPath, tnRootNode);        
    }

    /**
     * Adds a new element
     *
     * @param sKey Element key
     * @param oData Element data
     */
    public void addElement(String sKey, Object oData)
    {
        int iPos = sKey.lastIndexOf(cNodeSeparator);
        String sParentKey = "";
        String sElemName = sKey;

        if ((iPos >= 0) && (iPos < (sKey.length() - 1)))
        {
            sParentKey = sKey.substring(0, iPos);
            sElemName = sKey.substring(iPos + 1);
        }

        TreeNode tnParent = null;
        TreeNode tnNode = new TreeNode();

        if (sParentKey.length() > 0)
        {
            tnParent = mNodeMap.get(sParentKey);
        }
        else
        {
            tnParent = tnRootNode;
        }

        tnNode.sName = sElemName;
        tnNode.sKey = sKey;
        tnNode.oData = oData;

        tnParent.lChildren.add(tnNode);

        mNodeMap.put(sKey, tnNode);
    }

    /**
     * Recurses the tree in depth-first fashion and calls the callback object
     * for every node.
     *
     * @param icCallback Callback object
     *
     * @return If false the recursion was interrupted by the callback
     *
     * @throws Exception Thrown if the operation failed.
     */
    public boolean recurseDepthFirst(IterationCallback icCallback)
                              throws Exception
    {
        if ((tnRootNode == null) || (icCallback == null))
        {
            return false;
        }

        return recurseDepthFirst(icCallback, tnRootNode);
    }

    /**
     * Recurses the tree in depth-first fashion and calls the callback object
     * for every node.
     *
     * @param icCallback Callback object
     * @param tnNode Current tree node.
     *
     * @return If false the recursion was interrupted by the callback
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected boolean recurseDepthFirst(IterationCallback icCallback,
                                        TreeNode tnNode)
                                 throws Exception
    {
        for (Iterator<TreeNode> iIter = tnNode.lChildren.iterator(); iIter.hasNext();)
        {
            TreeNode tnChildNode = iIter.next();

            if (!recurseDepthFirst(icCallback, tnChildNode))
            {
                return false;
            }
        }

        if (tnNode == tnRootNode)
        {
            return true;
        }

        return icCallback.handleElement(tnNode);
    }

    /**
     * Callback object for tree iteration.
     *
     * @author mpoyhone
     */
    public interface IterationCallback
    {
        /**
         * Called for every tree node.
         *
         * @param tnNode Current node.
         *
         * @return If false, the recursion is aborted.
         *
         * @throws Exception Thrown if the operation failed.
         */
        public boolean handleElement(TreeNode tnNode)
                              throws Exception;
    }

    /**
     * Tree node object.
     *
     * @author mpoyhone
     */
    public static class TreeNode
    {
        /**
         * List of child nodes.
         */
        protected List<TreeNode> lChildren;
        /**
         * Node's data
         */
        protected Object oData;
        /**
         * Node's key
         */
        protected String sKey;
        /**
         * Node's name (last part of the name).
         */
        protected String sName;

        /**
         * Creates a new TreeNode object.
         */
        public TreeNode()
        {
            sName = "";
            sKey = "";
            lChildren = new LinkedList<TreeNode>();
        }

        /**
         * Retuns the child node list.
         *
         * @return The child node list.
         */
        public List<TreeNode> getChildren()
        {
            return lChildren;
        }

        /**
         * Sets node's data.
         *
         * @param oData Node's data.
         */
        public void setData(Object oData)
        {
            this.oData = oData;
        }

        /**
         * Returns node's data
         *
         * @return Node's data
         */
        public Object getData()
        {
            return oData;
        }

        /**
         * Sets node's key.
         *
         * @param sKey Node's key.
         */
        public void setKey(String sKey)
        {
            this.sKey = sKey;
        }

        /**
         * Returns node's key
         *
         * @return Node's key
         */
        public String getKey()
        {
            return sKey;
        }

        /**
         * Sets name's name
         *
         * @param sName Node's name
         */
        public void setName(String sName)
        {
            this.sName = sName;
        }

        /**
         * Returns node's name (last part of the name).
         *
         * @return Node's name (last part of the name).
         */
        public String getName()
        {
            return sName;
        }
    }
}
