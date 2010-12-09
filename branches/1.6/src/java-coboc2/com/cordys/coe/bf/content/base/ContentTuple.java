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
package com.cordys.coe.bf.content.base;

/**
 * Represents the BCP tuple for content updates. Contains the old and new
 * versions.
 *
 * @author mpoyhone
 */
public class ContentTuple
{
    /**
     * New content object.
     */
    private IContent cNew;
    /**
     * Old content object.
     */
    private IContent cOld;

    /**
     * Constructor for ContentTuple
     */
    public ContentTuple()
    {
    }

    /**
     * Constructor for ContentTuple
     *
     * @param cOld Old content object.
     * @param cNew New content object.
     */
    public ContentTuple(IContent cOld, IContent cNew)
    {
        if ((cOld == null) && (cNew == null))
        {
            throw new IllegalArgumentException("Both old and new cannot be null in a tuple.");
        }

        this.cOld = cOld;
        this.cNew = cNew;
    }

    /**
     * DOCUMENT ME!
     *
     * @return <code>true</code> if this is an delete operation.
     */
    public boolean isDelete()
    {
        if ((cOld == null) && (cNew == null))
        {
            throw new IllegalStateException("Old and new are not set.");
        }

        return cNew == null;
    }

    /**
     * @return <code>true</code> if this is an insert operation.
     */
    public boolean isInsert()
    {
        if ((cOld == null) && (cNew == null))
        {
            throw new IllegalStateException("Old and new are not set.");
        }

        return cOld == null;
    }

    /**
     * The new to set.
     *
     * @param aNew The new to set.
     */
    public void setNew(IContent aNew)
    {
        cNew = aNew;
    }

    /**
     * Returns the new.
     *
     * @return Returns the new.
     */
    public IContent getNew()
    {
        return cNew;
    }

    /**
     * The old to set.
     *
     * @param aOld The old to set.
     */
    public void setOld(IContent aOld)
    {
        cOld = aOld;
    }

    /**
     * Returns the old.
     *
     * @return Returns the old.
     */
    public IContent getOld()
    {
        return cOld;
    }

    /**
     * @return <code>true</code> if this is an update operation.
     */
    public boolean isUpdate()
    {
        if ((cOld == null) && (cNew == null))
        {
            throw new IllegalStateException("Old and new are not set.");
        }

        return (cOld != null) && (cNew != null);
    }
    
    /**
     * Returns the object that will be written, e.g. new for updates and inserts
     * and old for deletes.
     * @return Write object.
     */
    public IContent getWriteObject() {
        if (isDelete()) {
            return cOld;
        }
        
        return cNew;
    }
}
