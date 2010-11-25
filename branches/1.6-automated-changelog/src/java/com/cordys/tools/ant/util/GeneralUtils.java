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

import com.cordys.coe.util.general.ExceptionUtil;
import com.cordys.coe.util.soap.SoapFaultInfo;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.Enumeration;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * A Utility class for helper functions.
 *
 * @author msreejit
 */
public class GeneralUtils
{
    /**
     * Method to check if the directory exists and throw and build
     * exception.
     *
     * @param filepath The directory that needs to be checked.
     *
     * @throws BuildException If the path specified doesn't exist.
     */
    public static void checkDirectoryExists(java.io.File filepath)
    {
        if (!filepath.exists())
        {
            throw new BuildException("The path specified cannot be found - " +
                                     filepath.getAbsolutePath());
        }
    }

    /**
     * Tries to find a message from the exception hierarchy with the
     * matching pattern and if one was found returns the matched result.
     *
     * @param matchPattern Match regex pattern to use.
     * @param replacement Return value is extracted from the regex with this.
     * @param root Root exception.
     *
     * @return Extracted result or null if no exception matched this pattern.
     */
    public static String findMessageFromException(String matchPattern,
                                                  String replacement,
                                                  Throwable root)
    {
        Throwable t = root;

        while (t != null)
        {
            String msg = t.getMessage();

            if (msg != null)
            {
                String res = msg.replaceFirst(matchPattern, replacement);

                if (!res.equals(msg))
                {
                    // Regex matched.
                    return res;
                }
            }

            t = t.getCause();
        }

        return null;
    }

    /**
     * Helper Method to get the cn from the dn of LDAP string.
     *
     * @param DN The distinguished name of the LDAP entry.
     *
     * @return CN The common name of the LDAP entry.
     */
    public static String getCnFromDn(String DN)
    {
        String roleCN = DN.substring(3, DN.indexOf(","));

        return roleCN;
    }

    /**
     * Returns LDAP root from the organization DN.
     *
     * @param sOrganization Organization DN.
     *
     * @return LDAP root.
     */
    public static String getLdapRootFromOrganization(String sOrganization)
    {
        int iPos;
        final String sMatchStr = ",cn=";

        iPos = sOrganization.indexOf(sMatchStr);

        if (iPos == -1)
        {
            return null;
        }

        iPos++;

        if (iPos >= sOrganization.length())
        {
            return null;
        }

        return sOrganization.substring(iPos);
    }

    /**
     * This returns the dn of the root of the LDAP
     *
     * @param lConn The LDAPConnection to use for manipulating the entries
     *
     * @return the root of the ldap as a string
     *
     * @throws LDAPException if an error occured working with LDAP.
     */
    public static String getRootDN(LDAPConnection lConn)
                            throws LDAPException
    {
        String sReturn = null;

        LDAPSearchResults res = lConn.search("", LDAPConnection.SCOPE_BASE,
                                             "(namingContexts=*)",
                                             new String[] { "namingContexts" },
                                             false);

        /* There should be only one entry in the results (the root DSE). */
        while (res.hasMore())
        {
            LDAPEntry findEntry = res.next();
            /* Get the attributes of the root DSE. */
            LDAPAttributeSet findAttrs = findEntry.getAttributeSet();
            Iterator<?> iAttributes = findAttrs.iterator();

            //Iterate through each attribute.
            if (iAttributes.hasNext())
            {
                LDAPAttribute anAttr = (LDAPAttribute) iAttributes.next();
                Enumeration<?> enumVals = anAttr.getStringValues();

                if (enumVals != null)
                {
                    sReturn = (String) enumVals.nextElement();
                }
            }
        }
        return sReturn;
    }

    /**
     * This method returns the string-representation of the stacktrace
     * of the passed on exception.
     *
     * @param tException The exception to get the stacktrace of.
     *
     * @return The string-representation of the stacktrace.
     */
    public static String getStackTrace(Throwable tException)
    {
        //Get the stack-trace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tException.printStackTrace(pw);
        pw.flush();

        return sw.getBuffer().toString();
    }

    /**
     * This helper method is used for throwing BuildException with a
     * message.
     *
     * @param mess The message to be put in the exception.
     * @param e The exception which is thrown and need to be displayed.
     * @param task The instance of the ANT task which is used to log message.
     *
     * @throws BuildException
     */
    public static void handleException(String mess, Throwable e, Task task)
                                throws BuildException
    {
        if (e == null)
        {
            throw new BuildException("\n" + mess);
        }
        else
        {
            if (task != null)
            {
                task.log("\nStacktrace of the error:\n" + getStackTrace(e),
                         Project.MSG_DEBUG);
            }

            throw new BuildException("\n" + mess + "\n" +
                                     ExceptionUtil.getSimpleErrorTrace(e, true), e);
        }
    }

    /**
     * This helper method is used for throwing BuildException with a
     * message which is extracted from the response of the soap faults.
     *
     * @param responseNode The response node received after execution of the
     *        soap request.
     *
     * @throws BuildException
     */
    public static void handleException(int responseNode)
                                throws BuildException
    {
        String errorMess = "";

        /*
         * Check the response for Soap Fault.
         * If Soap Fault occurs, display appropriate message and halt the build.
         */
        SoapFaultInfo info = SoapFaultInfo.findSoapFault(responseNode);
        
        if (info != null) {
            String faultString = info.getFaultstring();
            
            if (faultString != null) {
                errorMess = "\nMessage: " + faultString;
            }

            int detailNode = info.getDetail();
            
            if (detailNode != 0) {
                int descNode = Find.firstMatch(detailNode, "<><errordescription>");
                
                errorMess = errorMess + "\nDetails: " + Node.getData(descNode);
            }
        }

        if (!"".equals(errorMess))
        {
            handleException(errorMess);
        }
    }

    /**
     * This helper method is used for throwing BuildException with a
     * message.
     *
     * @param mess The message to be put in the exception.
     *
     * @throws BuildException
     */
    public static void handleException(String mess)
                                throws BuildException
    {
        handleException(mess, null, null);
    }
    
    /**
     * Trims a string. Handles a null argument correctly.
     * 
     * @param s String to be trimmed.
     * @return Trimmed string.
     */
    public static String safeTrim(String s) 
    {
        return s != null ? s.trim() : null;
    }
    
    /**
     * Returns a trimmed project property.
     * 
     * @param s String to be trimmed.
     * @return Trimmed string.
     */
    public static String getTrimmedProperty(Project p, String propname) 
    {
        String value = p.getProperty(propname);
        
        return safeTrim(value);
    }    
}
