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
package com.cordys.coe.bf.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.utils.LDAPStringUtil;
import com.cordys.tools.ant.cm.BcpVersionInfo;

/**
 * Contains all configuration information.
 *
 * @author mpoyhone
 */
public class BFConfig
{
    /**
     * Contains the SOAP configuration.
     */
    private SoapConfig scSoapConfig = new SoapConfig();
    /**
     * Contains the CoBOC specific configuration.
     */
    private CobocConfig ccCobocConfig = new CobocConfig();
    /**
     * Contains the root folder for each content type.
     */
    private Map<EContentType, File> mContentDirectoryMap = new HashMap<EContentType, File>();
    /**
     * Contains the BCP user DN to be used.
     */
    private String sUserDn;
    /**
     * Contains the BCP organization DN to be used.
     */
    private String sOrganizationDn;
    /**
     * Contains the LDAP root DN.
     */
    private String sLdapRootDn;
    private BcpVersionInfo versionInfo;
    
    /**
     * Returns the organizationDn.
     *
     * @return Returns the organizationDn.
     */
    public String getOrganizationDn()
    {
        return sOrganizationDn;
    }

    /**
     * The organizationDn to set.
     *
     * @param aOrganizationDn The organizationDn to set.
     */
    public void setOrganizationDn(String aOrganizationDn)
    {
        sOrganizationDn = aOrganizationDn;
        sLdapRootDn = LDAPStringUtil.getLdapRoot(sOrganizationDn);
    }

    /**
     * Returns the userDn.
     *
     * @return Returns the userDn.
     */
    public String getUserDn()
    {
        return sUserDn;
    }

    /**
     * The userDn to set.
     *
     * @param aUserDn The userDn to set.
     */
    public void setUserDn(String aUserDn)
    {
        sUserDn = aUserDn;
    }

    /**
     * Returns the SOAP configuration object.
     * @return The SOAP configuration object.
     */
    public SoapConfig getSoapConfig() {
        return scSoapConfig;
    }

    /**
     * Returns the CoBOC configuration object.
     * @return The CoBOC configuration object.
     */
    public CobocConfig getCobocConfig() {
        return ccCobocConfig;
    }
    
    /**
     * Return the root folder for the given content type.
     * @param ctType Content type.
     * @return Root folder.
     */
    public File getContentRootDirectory(EContentType ctType) {
        return mContentDirectoryMap.get(ctType);
    }
    
    /**
     * Returns <code>true</code> if the given content XML should be pretty printed for the given
     * content source.
     * @param ctContentType Content type.
     * @param cstSourceType Content source type.
     * @return <code>true</code> if the content should be pretty printed for this content source.
     */
    public boolean isContentPrettyPrint(EContentType ctContentType, EContentSourceType cstSourceType) {
        return true;
    }

    /**
     * Returns the ldapRootDn.
     *
     * @return Returns the ldapRootDn.
     */
    public String getLdapRootDn()
    {
        return sLdapRootDn;
    }

    /**
     * The ldapRootDn to set.
     *
     * @param aLdapRootDn The ldapRootDn to set.
     */
    public void setLdapRootDn(String aLdapRootDn)
    {
        sLdapRootDn = aLdapRootDn;
    }

    /**
     * Sets the BCP version info object.
     * @param versionInfo
     */
    public void setVersionInfo(BcpVersionInfo versionInfo)
    {
        this.versionInfo = versionInfo;
    }

    /**
     * Returns the versionInfo.
     *
     * @return Returns the versionInfo.
     */
    public BcpVersionInfo getVersionInfo()
    {
        return versionInfo;
    }

}
