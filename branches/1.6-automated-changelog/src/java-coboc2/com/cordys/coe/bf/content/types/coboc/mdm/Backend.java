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
package com.cordys.coe.bf.content.types.coboc.mdm;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.LDAPStringUtil;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * Class containing MDM backend information.
 *
 * @author mpoyhone
 */
public class Backend extends MDMBase
{
    private String sIdentity;
    private String sDisplayName;
    private String sPublisherDn;
    private String sBackendType;
    private String sIsMdm;
    private XmlStructure xSniffer;
    
    /**
     * Constructor for Backend
     */
    public Backend()
    {
        super(EContentType.MDM_BACKEND);
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#createWriteVersion(com.cordys.coe.bf.content.base.IContentSource)
     */
    public IContent createWriteVersion(IContentSource csDest) throws BFException
    {
        if (csDest.getType() == EContentSourceType.BCP) {
            sPublisherDn = LDAPStringUtil.replaceOrganization(sPublisherDn, bcContext.getConfig().getOrganizationDn());
        }
        
        return this;
    }

    /**
     * Returns the backendType.
     *
     * @return Returns the backendType.
     */
    public String getBackendType()
    {
        return sBackendType;
    }

    /**
     * The backendType to set.
     *
     * @param aBackendType The backendType to set.
     */
    public void setBackendType(String aBackendType)
    {
        sBackendType = aBackendType;
    }

    /**
     * Returns the displayName.
     *
     * @return Returns the displayName.
     */
    public String getDisplayName()
    {
        return sDisplayName;
    }

    /**
     * The displayName to set.
     *
     * @param aDisplayName The displayName to set.
     */
    public void setDisplayName(String aDisplayName)
    {
        sDisplayName = aDisplayName;
    }

    /**
     * Returns the identity.
     *
     * @return Returns the identity.
     */
    public String getIdentity()
    {
        return sIdentity;
    }

    /**
     * The identity to set.
     *
     * @param aIdentity The identity to set.
     */
    public void setIdentity(String aIdentity)
    {
        sIdentity = aIdentity;
    }

    /**
     * Returns the isMdm.
     *
     * @return Returns the isMdm.
     */
    public String getIsMdm()
    {
        return sIsMdm;
    }

    /**
     * The isMdm to set.
     *
     * @param aIsMdm The isMdm to set.
     */
    public void setIsMdm(String aIsMdm)
    {
        sIsMdm = aIsMdm;
    }

    /**
     * Returns the publisherDn.
     *
     * @return Returns the publisherDn.
     */
    public String getPublisherDn()
    {
        return sPublisherDn;
    }

    /**
     * The publisherDn to set.
     *
     * @param aPublisherDn The publisherDn to set.
     */
    public void setPublisherDn(String aPublisherDn)
    {
        sPublisherDn = aPublisherDn;
    }

    /**
     * Returns the sniffer.
     *
     * @return Returns the sniffer.
     */
    public XmlStructure getSniffer()
    {
        return xSniffer;
    }

    /**
     * The sniffer to set.
     *
     * @param aSniffer The sniffer to set.
     */
    public void setSniffer(XmlStructure aSniffer)
    {
        xSniffer = aSniffer;
    }
    
}
