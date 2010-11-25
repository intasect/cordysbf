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
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;

/**
 * Content handle implementation LDAP content types.
 *
 * @author mpoyhone
 */
public class LdapContentHandle implements IContentHandle
{
    private EContentType ctContentType;
    private String sLdapDn; 
    
    /**
     * Constructor for this handle. 
     * @param ctType Handle content type.
     */
    public LdapContentHandle(EContentType ctType) {
        this.ctContentType = ctType;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#getContentType()
     */
    public EContentType getContentType()
    {
        return ctContentType;
    }

    /**
     * Returns the ldap DN.
     *
     * @return Returns the ldap DN.
     */
    public String getLdapDn()
    {
        return sLdapDn;
    }

    /**
     * The ldap DN to set.
     *
     * @param aLdapDn The ldap DN to set.
     */
    public void setLdapDn(String aLdapDn)
    {
        sLdapDn = aLdapDn;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#getContentId()
     */
    public String getContentId()
    {
        return sLdapDn;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#copyFrom(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public void copyFrom(IContentHandle chSource) throws BFException
    {
        if (! (chSource instanceof LdapContentHandle)) {
            throw new BFException("Content handle is not a LDAP handle");
        }
        
        this.sLdapDn = ((LdapContentHandle) chSource).sLdapDn;
    }   
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "{" + sLdapDn + "}";
    }   
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#getLogName()
     */
    public String getLogName()
    {
        return sLdapDn != null ? sLdapDn : "*Unknown*";
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#isSet()
     */
    public boolean isSet()
    {
        return sLdapDn != null && sLdapDn.length() > 0;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#equals(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public boolean equals(IContentHandle chOther)
    {
        if (! (chOther instanceof LdapContentHandle)) {
            return false;
        }
        
        if (sLdapDn == null) {
            return false;
        }
        
        return sLdapDn.equals(((LdapContentHandle) chOther).sLdapDn);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        int iRes = 0;
        
        if (sLdapDn != null) {
            iRes ^= sLdapDn.hashCode(); 
        }
        
        return iRes;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandle#cloneHandle()
     */
    public IContentHandle cloneHandle()
    {
        LdapContentHandle lchRes = new LdapContentHandle(ctContentType);
        
        lchRes.sLdapDn = sLdapDn;
        
        return lchRes;
    }
}
