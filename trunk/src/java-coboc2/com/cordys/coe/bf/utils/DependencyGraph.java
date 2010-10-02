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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Used for resolving content dependencies for inserting objects in
 * the right order. The objects that are inserted here must implement
// * the equals and hashCode methods properly.
 *
 * @author mpoyhone
 */
 public class DependencyGraph<T>
{
     private Map<T, Node<T>> mNodeMap = new HashMap<T, Node<T>>();
     
     public void add(T tObj, Collection<T> cDependencies) {
         // Find an exiting node ot create a one for the content key.
         Node<T> nNode = findNode(tObj);
         
         // Create links for all dependecies as well as nodes if needed.
         for (T tDep : cDependencies)
        {
             // Find an exiting dependency node ot create a one for the content key.
             Node<T> nDepNode = findNode(tDep);
             
             // Create the link between these two nodes.
             Link<T> lLink = new Link<T>(nNode, nDepNode);
             
             nNode.lOutgoing.add(lLink);
             nDepNode.lIncoming.add(lLink);
        }
     }
     
     /**
      * Returns the sorted list that is sorted by object depdendencies. 
      * Objects have dependencies only to objects earlier in the list.
      * @return A sorted list of objects.
     * @throws CircularDependencyException Thrown if a circular dependency was located in the graph.
      */
     public Collection<T> getDepencencies()  {
         if (mNodeMap.isEmpty()) {
             // This graph was empty, so return an empty list.
             return new LinkedList<T>();
         }
         
         Set<T> sAdded = new HashSet<T>();
         List<T> lResultList = new LinkedList<T>();

         // Iteratively add objects to the result list that have their dependencies
         // alreay added.
         while (lResultList.size() < mNodeMap.size()) {
             int iStartCount = sAdded.size();

             nodeloop:
             for (Node<T> nNode : mNodeMap.values())
             {
                 if (sAdded.contains(nNode.tObject)) {
                     // This node has already been added.
                     continue;
                 }
                 
                 for (Link<T> nDepLink : nNode.lOutgoing) {
                     if (! sAdded.contains(nDepLink.nTo.tObject)) {
                         // Not all dependencies have been added.
                         continue nodeloop;
                     }
                 }
                 
                 // We can add the node now.
                 lResultList.add(nNode.tObject);
                 sAdded.add(nNode.tObject);
             }
             
             if (sAdded.size() == iStartCount) {
                 throw new IllegalStateException("Circular content dependency found.");
             }
         }
         
         return lResultList;
     }

     private Node<T> findNode(T tObj) {
         Node<T> nNode = mNodeMap.get(tObj);
         
         if (nNode == null) {
             nNode = new Node<T>(tObj);
             mNodeMap.put(tObj, nNode);  
         }
         
         return nNode; 
     }
              
     private static class Node<T> {
         T tObject;
         List<Link<T>> lIncoming = new LinkedList<Link<T>>();
         List<Link<T>> lOutgoing = new LinkedList<Link<T>>();
        
         public Node(T tObj) {
             this.tObject = tObj;
         }
     }
    
     private static class Link<T> {
         Link(Node<T> nFrom, Node<T> nTo) {
             this.nFrom = nFrom;
             this.nTo = nTo;
         }
         Node<T> nFrom;
         Node<T> nTo;
     }
}

 