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
 *         File                :        NOMHandlesCollection.java
 *         Author                :        manesh@cordys.com
 *         Created on         :        Oct 27, 2004
 *
 */
package com.cordys.tools.ant.util;

import java.util.HashSet;
import java.util.Set;

import com.eibus.xml.nom.Node;

/*
 * This code is copied from CSRPT (Written by Rahul Jain)
 * Modifications are done to the original code.
 */
public class NOMHandlesCollection
{
    private Set<Integer> nodes;

    /**
     * Creates a new NOMHandlesCollection object.
     *
     * @param initialSize DOCUMENTME
     */
    public NOMHandlesCollection(int initialSize)
    {
        nodes = new HashSet<Integer>();
    }

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public int getCurrentCounter()
    {
        return nodes.size() - 1;
    }

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public int getCurrentSize()
    {
        return nodes.size();
    }

    /**
     * DOCUMENTME
     *
     * @param handle DOCUMENTME
     */
    public void addHandle(int handle)
    {
        nodes.add(handle);
    }

    /**
     * DOCUMENTME
     */
    public void cleanup()
    {
        int[] tmp = new int[nodes.size()];
        int i = 0;
        
        // First unlink all nodes.
        for (int node : nodes)
        {
            tmp[i++] = Node.unlink(node);
        }
        
        // Then delete all the nodes.
        for (int node : tmp)
        {
            Node.delete(node);
        }
        
        nodes.clear();
    }
}
