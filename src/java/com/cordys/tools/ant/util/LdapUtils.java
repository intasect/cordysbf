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
package com.cordys.tools.ant.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Some utility methods for LDAP DN manipulation.
 *
 * @author mpoyhone
 */
public class LdapUtils {
    /**
     * Regexp pattern to extract the LDAP root from any DN.
     */
    private static Pattern pLdapRootPattern = Pattern.compile(
            ".*,(cn=[^,]+,o=.*)$");

    /**
     * Replaces the organization in the DN with the given organization DN.
     * @param sDn LDAP DN
     * @param sDestOrgDn Destination organization DN.
     * @return Replaced version.
     */
    public static String replaceOrganization(String sDn, String sDestOrgDn) {
        if ((sDn == null) || (sDestOrgDn == null)) {
            return null;
        }

        int iPos = sDn.indexOf(",o=");

        if (iPos >= 0) {
            sDn = sDn.substring(0, iPos) + "," + sDestOrgDn;
        }

        return sDn;
    }
    
    /**
     * Replaces the organization in the DN with the given organization name.
     * @param sOrgDn LDAP DN
     * @param sNewOrgName Destination organization name.
     * @return Replaced version.
     */
    public static String replaceOrganizationName(String sOrgDn, String sNewOrgName) {
        if ((sNewOrgName == null) || (sOrgDn == null)) {
            return null;
        }

        int iStartPos = sOrgDn.indexOf(",o=");

        if (iStartPos >= 0) {
            int iEndPos = sOrgDn.indexOf(',', iStartPos);
            
            if (iEndPos > iStartPos) {
                return sOrgDn.substring(0, iStartPos + 3) + sOrgDn.substring(iEndPos);
            }
        }

        return sOrgDn;
    }

    /**
     * Returns the LDAP root for the given DN.
     * @param sDn LDAP DN.
     * @return LDAP root or <code>null</code> if the root could not be found.
     */
    public static String getLdapRoot(String sDn) {
        Matcher mMatcher = pLdapRootPattern.matcher(sDn);

        if (!mMatcher.matches()) {
            return null;
        }

        return mMatcher.group(1);
    }

    /**
     * Replaces the root DN in the given DN.
     * @param sDn DN
     * @param sDestRootDn Destination root DN.
     * @return Replaced version.
     */
    public static String replaceLdapRoot(String sDn, String sDestRootDn) {
        if ((sDn == null) || (sDestRootDn == null)) {
            return null;
        }

        String sRoot = getLdapRoot(sDn);

        if (sRoot != null) {
            sDn = sDn.substring(0, sDn.length() - sRoot.length()) +
                sDestRootDn;
        }

        return sDn;
    }

    /**
     * Replaces the organization/root DN in a role DN.
     * @param sRoleDn Role DN
     * @param sDestOrgDn Destination organization DN.
     * @return Replaced version.
     */
    public static String replaceRoleDn(String sRoleDn, String sDestOrgDn) {
        if ((sRoleDn == null) || (sDestOrgDn == null)) {
            return null;
        }

        if (sRoleDn.contains(",cn=organizational roles,")) {
            // This is an organizational role.
            return replaceOrganization(sRoleDn, sDestOrgDn);
        } else {
            // This is an ISV role.
            String sDestRoot = getLdapRoot(sDestOrgDn);

            return replaceLdapRoot(sRoleDn, sDestRoot);
        }
    }
    
    /**
     * Replaces the organization/root DN in a role DN.
     * @param sUserDn User DN
     * @param sDestOrgDn Destination organization DN.
     * @return Replaced version.
     */
    public static String replaceOrgUserDn(String sUserDn, String sDestOrgDn) {
        if ((sUserDn == null) || (sDestOrgDn == null)) {
            return null;
        }

        if (sUserDn.contains(",cn=organizational users,")) {
            // This is an organizational role.
            return replaceOrganization(sUserDn, sDestOrgDn);
        }
        
        return sUserDn;
    }    
}
