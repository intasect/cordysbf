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
package com.cordys.tools.ant.soap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.cordys.coe.util.xml.Message;
import com.cordys.coe.util.xml.MessageContext;
import com.eibus.xml.nom.Node;

/**
 * Collects NOM nodes so they can be deleted at a safe point.
 * 
 * @author mpoyhone
 */
public class NomCollector
{
    /**
     * Set containing the added nodes as <code>Integer</code> objects. This
     * prevents the same node to be delete twice.
     */
    private Set<Integer> lNodes = new HashSet<Integer>(128);

    /**
     * Message content for gathering <code>Message</code> wrappers.
     */
    private MessageContext mcMessageContext = new MessageContext();

    /**
     * Adds a new node.
     * 
     * @param iNode
     *            Node to be added.
     */
    public void addNode(int iNode)
    {
        lNodes.add(iNode);
    }

    /**
     * Adds a new <code>Message</code> wrappers.
     * 
     * @param mMsg
     *            <code>Message</code> wrappers.
     */
    public void addMessage(Message mMsg)
    {
        mcMessageContext.add(mMsg);
    }

    /**
     * Deletes all nodes that are added to this collector.
     */
    public void deleteNodes()
    {
        for (Iterator<Integer> iIter = lNodes.iterator(); iIter.hasNext();)
        {
            Integer iNodeObject = iIter.next();
            int iNode = iNodeObject.intValue();

            if (iNode != 0)
            {
                Node.delete(iNode);
            }
        }

        lNodes.clear();
        mcMessageContext.clear();
    }

    /**
     * @return Message context.
     */
    public MessageContext getMessageContext()
    {
        return mcMessageContext;
    }
}
