/**
 * Copyright 2004 Cordys R&D B.V. 
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
 /*
 *         Project         :        BuildFramework
 *         File                :        NodeUtil.java
 *         Author                :        manesh@cordys.com
 *         Created on         :        Oct 4, 2004
 *
 */
package com.cordys.tools.ant.util;

import com.eibus.xml.nom.Node;

/**
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class NodeUtil
{
    /**
     * DOCUMENTME
     *
     * @param node DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public static int[] getChildrenNodes(int node)
    {
        int childLen = Node.getNumChildren(node);

        if (childLen == 0)
        {
            return new int[0];
        }

        int[] nodes = new int[childLen];
        nodes[0] = Node.getFirstChild(node);

        for (int i = 1; i < childLen; i++)
        {
            nodes[i] = Node.getNextSibling(nodes[i - 1]);
        }

        return nodes;
    }
}
