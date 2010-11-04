/**
 * Copyright 2005 Cordys R&D B.V. 
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

import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Helper class to perform any validation/checks
 *
 * @author asuresh, knayak
 */
public class ValidationUtil
{
    /**
     * Constant which stores the invalid isv/organization format message.
     */
    private static final String INVALID_ORG_SYNTAX_ERROR = "Invalid Syntax for ISV/Organization";
    /**
     * Constant which stores the non-existent isv/organization message.
     */
    private static final String INVALID_ORG_ERROR = "Specified ISV/Organization does not exist";
    /**
     * Constant which stores the invalid User format message.
     */
    private static final String INVALID_USER_SYNTAX_ERROR = "Invalid Syntax for User DN";
    /**
     * Constant which stores the non-existent user message.
     */
    private static final String INVALID_USER_ERROR = "Specified User is not a valid authenticated/organizational user";
    /**
     * The array of names of parameters
     */
    private static String[] paramNames = { "dn" };

    /**
     * Internal function to check if the ISV/ Organization specified exists
     *
     * @param sDN DN of org/ isv specified for export/import
     * @param srmSoap Instance of SoapRequestManager
     *
     * @throws IllegalArgumentException
     * @throws SoapRequestException
     */
    public static void checkOrganization(String sDN, ISoapRequestManager srmSoap)
                                  throws IllegalArgumentException, 
                                         SoapRequestException
    {
        boolean isOrganizationPresent = false;

        String sActualSearchRoot = srmSoap.getLdapRoot();

        String[] paramValues = { sActualSearchRoot };
        String methodname = "";

        if (sDN.startsWith("o") || sDN.startsWith("ou"))
        {
            methodname = "GetOrganizations";
        }
        else if (sDN.startsWith("cn"))
        {
            methodname = "GetSoftwarePackages";
        }
        else
        {
            throw new IllegalArgumentException(INVALID_ORG_SYNTAX_ERROR);
        }

        int organizationResponse = srmSoap.makeSoapRequest("cn=SYSTEM,cn=organizational users,o=system," +
                                                           sActualSearchRoot,
                                                           sDN,
                                                           "http://schemas.cordys.com/1.0/ldap",
                                                           methodname,
                                                           paramNames,
                                                           paramValues);

        //check the response for Soap Fault.
        GeneralUtils.handleException(organizationResponse);

        int[] entryNodes = Find.match(organizationResponse,
                                      "?<tuple><old><entry>");

        for (int i = 0; i < entryNodes.length; i++)
        {
            if (Node.getAttribute(entryNodes[i], "dn").equals(sDN))
            {
                isOrganizationPresent = true;

                break;
            }
        }

        if (!isOrganizationPresent)
        {
            throw new IllegalArgumentException(INVALID_ORG_ERROR);
        }
    }

    /**
     * Internal function to check if the user specified is a valid
     * authenticated or organizational user
     *
     * @param sUserDN The dn of user specified
     * @param srmSoap of SoapRequestManager
     *
     * @throws IllegalArgumentException
     * @throws SoapRequestException
     */
    public static void checkUser(String sUserDN, ISoapRequestManager srmSoap)
                          throws IllegalArgumentException, SoapRequestException
    {
        String sActualSearchRoot = srmSoap.getLdapRoot();
        String[] paramValues = { sActualSearchRoot };
        boolean isUserPresent = false;
        String methodname = "";
        String orgdn = "";

        if (sUserDN.regionMatches(sUserDN.indexOf(',') + 1,
                                      "cn=authenticated users", 0, 22))
        {
            methodname = "GetAuthenticatedUsers";
        }
        else if (sUserDN.regionMatches(sUserDN.indexOf(',') + 1,
                                           "cn=organizational users", 0, 23))
        {
            orgdn = sUserDN.split("cn=organizational users,")[1];
            methodname = "GetOrganizationalUsers";
            paramValues[0] = orgdn;
        }
        else
        {
            throw new IllegalArgumentException(INVALID_USER_SYNTAX_ERROR);
        }

        int usersResponse = srmSoap.makeSoapRequest("cn=SYSTEM,cn=organizational users,o=system," +
                                                    sActualSearchRoot, orgdn,
                                                    "http://schemas.cordys.com/1.0/ldap",
                                                    methodname, paramNames,
                                                    paramValues);

        //check the response for Soap Fault.
        GeneralUtils.handleException(usersResponse);

        int[] entryNodes = Find.match(usersResponse, "?<tuple><old><entry>");

        for (int i = 0; i < entryNodes.length; i++)
        {
            if (Node.getAttribute(entryNodes[i], "dn").equals(sUserDN))
            {
                isUserPresent = true;

                break;
            }
        }

        if (!isUserPresent)
        {
            throw new IllegalArgumentException(INVALID_USER_ERROR);
        }
    }

//    /**
//     * This method returns the actual search root for the current Cordys
//     * server. If there is a difference between the search root of the local
//     * installation we need to use the one of the LDAP connection. I found
//     * this at Ewals. My local  installation is pointing to vanenburg.com,
//     * while the LDAP server is pointing to ewals.com. We'll assume the LDAP
//     * connection's  root to be the correct one.
//     *
//     * @param ldDirectory The LDAP directory.
//     *
//     * @return The correct search root.
//     */
//    private static String getActualSearchRoot(LDAPDirectory ldDirectory)
//    {
//        String sReturn = ldDirectory.getDirectorySearchRoot();
//
//        try
//        {
//            String sLDAPConnectionSearchRoot = GeneralUtils.getRootDN(ldDirectory.getConnection());
//            if (!sLDAPConnectionSearchRoot.startsWith("cn=cordys"))
//            {
//                sLDAPConnectionSearchRoot = "cn=cordys," + sLDAPConnectionSearchRoot;
//            }
//
//            if (!sLDAPConnectionSearchRoot.equals(sReturn))
//            {
//                //This is a case where it happens. We'll assume the LDAP connection's 
//                //root to be the correct one.
//                sReturn = sLDAPConnectionSearchRoot;
//            }
//        }
//        catch (LDAPException e)
//        {
//            //Ignore the exception
//        }
//        return sReturn;
//    }
}
