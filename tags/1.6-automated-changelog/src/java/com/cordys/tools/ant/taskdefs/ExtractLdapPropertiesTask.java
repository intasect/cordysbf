/**
 * Copyright 2007 Cordys R&D B.V. 
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
package com.cordys.tools.ant.taskdefs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Extracts some LDAP properties from the passed user DN.
 *
 * @author mpoyhone
 */
public class ExtractLdapPropertiesTask extends Task
{
    /**
     * User DN parameter.
     */
    private String userDn;
    /**
     * Name of the property that will be filled with user name.
     */
    private String userNameProperty;
    /**
     * Name of the property that will be filled with organization name.
     */
    private String orgNameProperty;
    
    /**
     * Name of the property that will be filled with organization DN.
     */
    private String orgDnProperty;
    
    /**
     * Name of the property that will be filled with LDAP root.
     */
    private String ldapRootProperty;
    /**
     * Regexp pattern for extracting the information.
     */
    private static final Pattern pExtractPattern = Pattern.compile("^cn=([^,]+),cn=organizational users,o=([^,]+),(.*)$");
    
    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException
    {
        super.execute();
        
        if (userDn == null || userDn.length() == 0)
        {
            throw new BuildException("UserDN parameter is not set.");
        }
        
        Matcher mMatcher = pExtractPattern.matcher(userDn);
        
        if (! mMatcher.matches()) {
            throw new BuildException("Invalid user DN: " + userDn);
        }
        
        if (userNameProperty != null) {
            getProject().setProperty(userNameProperty, mMatcher.group(1));
        }
        
        if (orgNameProperty != null) {
            getProject().setProperty(orgNameProperty, mMatcher.group(2));
        }
        
        if (orgDnProperty != null) {
            getProject().setProperty(ldapRootProperty, "o=" + mMatcher.group(2) + "," + mMatcher.group(3));
        }
        
        if (ldapRootProperty != null) {
            getProject().setProperty(ldapRootProperty, mMatcher.group(3));
        }
    }

    /**
     * Returns the userDn.
     *
     * @return Returns the userDn.
     */
    public String getUserDn()
    {
        return userDn;
    }

    /**
     * Sets the userDn.
     *
     * @param userDn The userDn to be set.
     */
    public void setUserDn(String userDn)
    {
        this.userDn = userDn;
    }

    /**
     * Returns the ldapRootProperty.
     *
     * @return Returns the ldapRootProperty.
     */
    public String getLdapRootProperty()
    {
        return ldapRootProperty;
    }

    /**
     * Returns the orgDnProperty.
     *
     * @return Returns the orgDnProperty.
     */
    public String getOrgDnProperty()
    {
        return orgDnProperty;
    }

    /**
     * Returns the orgNameProperty.
     *
     * @return Returns the orgNameProperty.
     */
    public String getOrgNameProperty()
    {
        return orgNameProperty;
    }

    /**
     * Returns the userNameProperty.
     *
     * @return Returns the userNameProperty.
     */
    public String getUserNameProperty()
    {
        return userNameProperty;
    }

    /**
     * Sets the ldapRootProperty.
     *
     * @param ldapRootProperty The ldapRootProperty to be set.
     */
    public void setLdapRootProperty(String ldapRootProperty)
    {
        this.ldapRootProperty = ldapRootProperty;
    }

    /**
     * Sets the orgDnProperty.
     *
     * @param orgDnProperty The orgDnProperty to be set.
     */
    public void setOrgDnProperty(String orgDnProperty)
    {
        this.orgDnProperty = orgDnProperty;
    }

    /**
     * Sets the orgNameProperty.
     *
     * @param orgNameProperty The orgNameProperty to be set.
     */
    public void setOrgNameProperty(String orgNameProperty)
    {
        this.orgNameProperty = orgNameProperty;
    }

    /**
     * Sets the userNameProperty.
     *
     * @param userNameProperty The userNameProperty to be set.
     */
    public void setUserNameProperty(String userNameProperty)
    {
        this.userNameProperty = userNameProperty;
    }
    
}
