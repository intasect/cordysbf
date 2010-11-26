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
package com.cordys.coe.bf.content.types.ldap;

import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.types.ContentBase;
import com.cordys.coe.bf.content.types.EContentType;

/**
 * Base class for all LDAP content objects.
 *
 * @author mpoyhone
 */
public abstract class LdapContentBase extends ContentBase
{
    /**
     * Content type.
     */
    protected EContentType ctContentType;
    /**
     * Handle object for this content.
     */
    protected LdapContentHandle lchHandle;
    /**
     *  Handle object for parent object.
     */
    protected LdapContentHandle lchParentHandle;    
    
    /**
     * Constructor for LdapContentBase
     */
    public LdapContentBase(EContentType ctContentType)
    {
        this.ctContentType = ctContentType;
        this.lchHandle = new LdapContentHandle(this.ctContentType);
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#getHandle()
     */
    public LdapContentHandle getHandle()
    {
        return lchHandle;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#setHandle(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public void setHandle(IContentHandle hHandle)
    {
        if (! (hHandle instanceof LdapContentHandle)) {
            throw new IllegalArgumentException("Content handle is not of the LdapContentHandle");
        }
        
        this.lchHandle = (LdapContentHandle) hHandle;
    }  
    

    /**
     * @see com.cordys.coe.bf.content.base.IContent#setParent(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public void setParent(IContentHandle chParent)
    {
        if (! (chParent instanceof LdapContentHandle)) {
            throw new IllegalArgumentException("Content handle is not of the LdapContentHandle");
        }     
        
        this.lchParentHandle = (LdapContentHandle) chParent;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContent#getParent()
     */
    public IContentHandle getParent()
    {
        return lchHandle;
    }      
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#getType()
     */
    public EContentType getType()
    {
        return ctContentType;
    }
    
    /**
     * Returns the LDAP DN.
     *
     * @return Returns the LDAP DN.
     */
    public String getLdapDn()
    {
        return lchHandle.getLdapDn();
    }

    /**
     * The LDAP DN to set.
     *
     * @param aLdapDn The LDAP DN to set.
     */
    public void setLdapDn(String aLdapDn)
    {
        lchHandle.setLdapDn(aLdapDn);
    }
}
