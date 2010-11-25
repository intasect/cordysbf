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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains utility functions for string based DN operations.
 * These methods are moved here from the CobocUtils class.
 *
 * @author pgussow
 */
public class LDAPStringUtil
{
    /**
     * Regexp pattern to extract the LDAP root from any DN.
     */
    private static Pattern pLdapRootPattern = Pattern.compile(".*,(cn=[^,]+,o=.*)$");

    /**
     * Returns the LDAP root for the given DN.
     *
     * @param sDn LDAP DN.
     *
     * @return LDAP root or <code>null</code> if the root could not be found.
     */
    public static String getLdapRoot(String sDn)
    {
        Matcher mMatcher = pLdapRootPattern.matcher(sDn);

        if (!mMatcher.matches())
        {
            return null;
        }

        return mMatcher.group(1);
    }

    /**
     * DOCUMENTME
     *
     * @param sDn DOCUMENTME
     * @param sDestRootDn DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public static String replaceLdapRoot(String sDn, String sDestRootDn)
    {
        if ((sDn == null) || (sDestRootDn == null))
        {
            return null;
        }

        String sRoot = getLdapRoot(sDn);

        if (sRoot != null)
        {
            sDn = sDn.substring(0, sDn.length() - sRoot.length()) +
                  sDestRootDn;
        }

        return sDn;
    }

    /**
     * DOCUMENTME
     *
     * @param sDn DOCUMENTME
     * @param sDestOrgDn DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public static String replaceOrganization(String sDn, String sDestOrgDn)
    {
        if ((sDn == null) || (sDestOrgDn == null))
        {
            return null;
        }

        int iPos = sDn.indexOf(",o=");

        if (iPos >= 0)
        {
            sDn = sDn.substring(0, iPos) + "," + sDestOrgDn;
        }

        return sDn;
    }

    /**
     * This method replaces the passed on role dn with a DN that
     * matches the destination organization. It can also make a distinction
     * between org roles and isv roles.
     *
     * @param sRoleDn The source role DN.
     * @param sDestOrgDn The destination organization.
     *
     * @return The replaced DN.
     */
    public static String replaceRoleDn(String sRoleDn, String sDestOrgDn)
    {
        if ((sRoleDn == null) || (sDestOrgDn == null))
        {
            return null;
        }

        if (sRoleDn.contains(",cn=organizational roles,"))
        {
            // This is an organizational role.
            return replaceOrganization(sRoleDn, sDestOrgDn);
        }
        else
        {
            // This is an ISV role.
            String sDestRoot = getLdapRoot(sDestOrgDn);

            return replaceLdapRoot(sRoleDn, sDestRoot);
        }
    }
}
