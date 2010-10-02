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
package com.cordys.coe.ant.isvloadtask;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.cordys.coe.util.log.AntTaskLogger;
import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.NomCollector;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.GeneralUtils;

/**
 * This class enables database content to be scripted via ant. The configuration
 * in the antfile should look something like this:
 * 
 * <pre>
 *  &lt;isvloader operation=&quot;load|unload&quot;&gt;
 *    &lt;isvfile name=&quot;CoE_Project*.isvp&quot; /&gt;
 *  &lt;/isvloader&gt;
 * </pre>
 * 
 * @author mpoyhone
 */
public class IsvLoadTask extends Task
{
    /**
     * Holds the wrapper around the ant logger.
     */
    private AntTaskLogger atlLog;

    /**
     * Holds all the nested isvfile-elements.
     */
    private List<IsvFile> lFiles;

    /**
     * Attribute which stores the ldapHost name of the LDAP.
     */
    private String ldapHost = null;

    /**
     * Attribute which stores the ldapPassword used to connect to the LDAP.
     */
    private String ldapPassword = null;

    /**
     * Attribute which stores the ldapUser used to connect to the LDAP.
     */
    private String ldapUser = null;

    /**
     * Name of the connector instance that needs to be created
     */
    private String name = "ISVLoadTask";

    /**
     * Attribute which stores the distiguished name(dn) of the organization A
     * soap processor will be searched in this organization first
     */
    private String orgdn = "o=system,cn=cordys,o=vanenburg.com";

    /**
     * Name of the soap processor to which you need to send request
     */
    private String soapprocessordn = null;

    /**
     * Attribute which stores the distnguished name(dn) of the user in whose
     * name the Soap Requests have to be sent to the ECX. Optional - If not
     * specified it defaults to user in whose context the ANT file is run.
     */
    private String userdn = "cn=SYSTEM,cn=organizational users,o=system,cn=cordys,o=vanenburg.com";

    /**
     * Indicated the 'operation' parameter value.
     */
    private boolean bLoadOperation = true;

    /**
     * Attribute which stores the the ldapPort number of the LDAP.
     */
    private int ldapPort = -1;

    /**
     * Default constructor.
     */
    public IsvLoadTask()
    {
        super();

        lFiles = new LinkedList<IsvFile>();
        atlLog = new AntTaskLogger(this);
    }

    /**
     * Sets the remote ldapHost machine name to connect.
     * 
     * @param host
     *            The remote ldapHost machine name.
     */
    public void setLdapHost(String host)
    {
        this.ldapHost = host;
    }

    /**
     * Returns the remote ldapHost in which the content needs to be handled.
     * 
     * @return The LDAP host.
     */
    public String getLdapHost()
    {
        return ldapHost;
    }

    /**
     * Sets the ldapPassword to be used for connecting to the remote ldapHost.
     * 
     * @param pwd
     *            The ldapPassword to be used.
     */
    public void setLdapPassword(String pwd)
    {
        this.ldapPassword = pwd;
    }

    /**
     * Returns the ldapPassword to used for connecting to the remote ldapHost.
     * 
     * @return The ldapPassword used for connecting to the remote ldapHost.
     */
    public String getLdapPassword()
    {
        return ldapPassword;
    }

    /**
     * Sets the ldapPort number used to connect to remote ldapHost machine.
     * 
     * @param i
     *            The ldapPort number.
     */
    public void setLdapPort(int i)
    {
        this.ldapPort = i;
    }

    /**
     * Returns the ldapPort number used to connect to remote ldapHost machine.
     * 
     * @return The ldapPort number.
     */
    public int getLdapPort()
    {
        return ldapPort;
    }

    /**
     * Sets the ldapUser of the LDAP to connect.
     * 
     * @param user
     *            The ldapUser used for connecting to LDAP.
     */
    public void setLdapUser(String user)
    {
        this.ldapUser = user;
    }

    /**
     * Returns the name of the User to be used for connecting to ECX machine.
     * 
     * @return The ldapUser used for connecting to LDAP.
     */
    public String getLdapUser()
    {
        return ldapUser;
    }

    /**
     * Sets the connector instance name
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the name with which connector instace will be created
     * 
     * @return The name of the connector.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the operation type to load or unload.
     * 
     * @param sType
     *            Possible values are 'load' and 'unload'.
     */
    public void setOperation(String sType)
    {
        if (sType.equals("load"))
        {
            bLoadOperation = true;
        }
        else if (sType.equals("unload"))
        {
            bLoadOperation = false;
        }
        else
        {
            atlLog.error("Invalid operation type : " + sType);
            return;
        }
    }

    /**
     * Sets the organizational context from which soap node has to be found out
     * 
     * @param dn
     *            DN of the organization
     */
    public void setOrgDN(String dn)
    {
        this.orgdn = dn;
    }

    /**
     * Returns the organizational context from which soap node will be serached
     * for.
     * 
     * @return The DN of the organization.
     */
    public String getOrgDN()
    {
        return orgdn;
    }

    /**
     * Sets the user context from which requests will be fired If not set, then
     * it will take the default user context
     * 
     * @param dn
     *            DN of the user
     */
    public void setSoapProcessorDN(String dn)
    {
        this.soapprocessordn = dn;
    }

    /**
     * Returns the user context from which requests will be fired
     * 
     * @return The DN of the soap processor.
     */
    public String getSoapProcessorDN()
    {
        return soapprocessordn;
    }

    /**
     * Sets the user context from which requests will be fired If not set, then
     * it will take the default user context
     * 
     * @param dn
     *            DN of the user
     */
    public void setUserDN(String dn)
    {
        this.userdn = dn;
    }

    /**
     * Returns the user context from which requests will be fired
     * 
     * @return The DN of the user.
     */
    public String getUserDN()
    {
        return userdn;
    }

    /**
     * This method adds the configured ISV file to the list.
     * 
     * @param ifFile
     *            The database to add.
     */
    public void addConfiguredIsvFile(IsvFile ifFile)
    {
        lFiles.add(ifFile);
    }

    /**
     * Implementing the abstract method of class Task
     */
    public void execute()
    {
        validateAttributes();
        
        // Fix the organization DN to 'system'.
        orgdn = orgdn.replaceFirst("^o=[^,]+,", "o=system,");
        
        // Fix the user DN to SYSTEM under the system organization.
        userdn = "cn=SYSTEM,cn=organizational users," + orgdn;

        // Create SoapRequestManager object
        ISoapRequestManager soapRequestMGR = null;

        try
        {
            soapRequestMGR = ContentManagerTask.createRequestManager(this,
                    ldapHost, ldapUser, ldapPassword, ldapPort, userdn, orgdn);
        }
        catch (SoapRequestException sre)
        {
            GeneralUtils.handleException("Error occured while sending request "
                    + "to ECX machine.\n" + sre.getMessage(), sre, this);
        }

        // Set required properties
        soapRequestMGR.setTimeout(0);

        try
        {
            NomCollector nmCollector = new NomCollector();
            
            nmCollector.getMessageContext().setDocument(soapRequestMGR.getDocument());
            soapRequestMGR.addNomCollector(nmCollector);

            for (Iterator<IsvFile> iIter = lFiles.iterator(); iIter.hasNext();)
            {
                IsvFile ifFile = iIter.next();

                if (bLoadOperation)
                {
                    ifFile.executeLoad(soapRequestMGR);
                }
                else
                {
                    ifFile.executeUnload(soapRequestMGR, false);
                }
            }
        }
        catch (Exception sre)
        {
            GeneralUtils.handleException("Error occured while sending request "
                    + "to ECX machine.\n" + sre.getMessage(), sre, this);
        }
        finally
        {
            soapRequestMGR.removeNomCollector().deleteNodes();
        }

    }

    /**
     * Ensure we have a consistent and legal set of attributes, and set any
     * internal flags necessary based on different combinations of attributes.
     */
    protected void validateAttributes() throws BuildException
    {
        if ((ldapHost != null) || ((ldapPort != -1) && (ldapUser != null))
                || (ldapPassword != null))
        {
            // If any of the proerty is set, then all must be set
            // Else throw Exception
            if ((ldapHost == null) || ((ldapPort == -1) && (ldapUser == null))
                    || (ldapPassword == null))
            {
                throw new BuildException(
                        "If LDAP properties are set, then all "
                                + "(ldaphost, ldapprot, ldapuser, ldappassword) "
                                + "must be set together");
            }
        }

        if (orgdn == null) {
            throw new BuildException("Organization DN is not set.");
        }
    }
}
