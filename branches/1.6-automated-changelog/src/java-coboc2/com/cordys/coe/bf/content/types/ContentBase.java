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
package com.cordys.coe.bf.content.types;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.CobocUtils;

/**
 * Base class for content classes.
 *
 * @author mpoyhone
 */
public abstract class ContentBase implements IContent
{
    /**
     * If <code>true</code> the toString() method returns a pretty printed version.
     */
    private static final boolean bPrettyToString = true;
    
    /**
     * Holds sub-content objects for this content object. If the content type
     * does not support sub-content objects this is set to <code>null</code>. 
     */
    protected List<IContentHandle> lSubContentList = null;
    /**
     * Holds content objects referenced by this content object. If the content type
     * does not support referenced objects this is set to <code>null</code>. 
     */
    protected List<IContentHandle> lReferencedContentList = null;
    /**
     * Context where this object belongs to.
     */
    protected BFContext bcContext;
    
    /**
     * Constructor for ContentBase
     */
    public ContentBase()
    {
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#addChild(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public void addChild(IContentHandle chChildHandle) throws BFException
    {
        if (lSubContentList == null) {
            throw new BFException("Content type " + getType() + " does not support child objects.");
        }
        
        lSubContentList.add(chChildHandle);
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#removeChild(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public void removeChild(IContentHandle chChildHandle) throws BFException
    {
        if (lSubContentList == null) {
            throw new BFException("Content type " + getType() + " does not support child objects.");
        }
        
        lSubContentList.remove(chChildHandle);
    }      

    /**
     * @see com.cordys.coe.bf.content.base.IContent#getChildren()
     */
    public Collection<IContentHandle> getChildren()
    {
        return lSubContentList != null ? lSubContentList : new LinkedList<IContentHandle>();
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        return lReferencedContentList != null ? lReferencedContentList : new LinkedList<IContentHandle>();
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#getContext()
     */
    public BFContext getContext()
    {
        return bcContext;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#setContext(com.cordys.coe.bf.BFContext)
     */
    public void setContext(BFContext bcContext)
    {
        this.bcContext = bcContext;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#resolveDependencies()
     */
    public void resolveDependencies() throws BFException
    {
        if (lSubContentList != null) {
            for (IContentHandle chChildHandle : lSubContentList)
            {
                IContent cObject = bcContext.findContent(chChildHandle);
                
                if (cObject == null) {
                    throw new BFException("No object found for child handle " + chChildHandle);
                }
                
                // Set this object as the child's parent.
                cObject.setParent(getHandle());
            }
        }
    }  

    /**
     * @see com.cordys.coe.bf.content.base.IContent#getLogName()
     */
    public String getLogName()
    {
        IContentHandle chHandle = getHandle();
        
        if (chHandle == null) {
            return "*Unknown object*";
        }
        
        EContentType ctType = chHandle.getContentType();
        StringBuffer sbRes = new StringBuffer(50);
        
        if (ctType == null) {
            sbRes.append("*Unknown type*");
        } else {
            sbRes.append(ctType.getLogName());
        }
        
        sbRes.append(" ");
        sbRes.append(chHandle.getLogName());
        
        return sbRes.toString();
    }

    /**
     * Returns the the string representation of this object based on JavaBean properties.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return CobocUtils.beanToString(this, getType() != null ? getType().toString() : "*UNKNOWN*", bPrettyToString);
   }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#createWriteVersion(com.cordys.coe.bf.content.base.IContentSource)
     */
    public IContent createWriteVersion(IContentSource csDest) throws BFException
    {
        return this;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#updateFromNewVersion(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateFromNewVersion(IContent cNewObject, IContentSource csSrc) throws BFException
    {
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#updateFromOldVersion(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateFromOldVersion(IContent cOldObject, IContentSource csSrc) throws BFException
    {
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#onLoad(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void onLoad(IContentSource csSrc) throws BFException
    {
        
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#getHandleForContentSource(com.cordys.coe.bf.content.base.IContentSource)
     */
    public IContentHandle getHandleForContentSource(IContentSource csSource)
    {
        return getHandle();
    }
    
    
}
