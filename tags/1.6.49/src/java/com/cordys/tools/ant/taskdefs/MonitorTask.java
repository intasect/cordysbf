package com.cordys.tools.ant.taskdefs;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.cordys.coe.util.log.AntTaskLogger;
import com.cordys.coe.util.xml.Message;
import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.soap.BusSoapRequestManager;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.NomCollector;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.GeneralUtils;


/**
 * DOCUMENTME
 *
 * @author $author$
 */
public class MonitorTask extends Task {
    /**
     * Holds the wrapper around the ant logger.
     */
    private AntTaskLogger atlLog;

    /**
     * Holds the list of soap processors.
     */
    private ArrayList<Processor> alProcessors = new ArrayList<Processor>();

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
    private String ldapSSL = null;

    /**
     * Attribute which stores the ldapUser used to connect to the LDAP.
     */
    private String ldapUser = null;

    /**
     * Indicated the 'operation' parameter value.
     */
    private String operation;

    /**
     * Holds the organization for which the processors need to be
     * started/stopped.
     */
    private String organization;

    /**
     * Attribute which stores the distnguished name(dn) of the user in whose
     * name the  Soap Requests have to be sent to the ECX. Optional - If not
     * specified it defaults to user in whose context the ANT file is run.
     */
    private String userdn = "cn=SYSTEM,cn=organizational users,o=system,cn=cordys,o=vanenburg.com";

    /**
     * Attribute which stores the the ldapPort number of the LDAP.
     */
    private int ldapPort = -1;

    /**
     * Default constructor.
     */
    public MonitorTask() {
        super();

        atlLog = new AntTaskLogger(this);
    }

    /**
     * Sets the remote ldapHost machine name to connect.
     *
     * @param host The remote ldapHost machine name.
     */
    public void setLdapHost(String host) {
        this.ldapHost = host;
    }

    /**
     * Returns the remote ldapHost in which the content needs to be handled.
     *
     * @return The LDAP host.
     */
    public String getLdapHost() {
        return ldapHost;
    }

    /**
     * Sets the ldapPassword to be used for connecting to the remote ldapHost.
     *
     * @param pwd The ldapPassword to be used.
     */
    public void setLdapPassword(String pwd) {
        this.ldapPassword = pwd;
    }

    /**
     * Returns the ldapPassword to used for connecting to the remote ldapHost.
     *
     * @return The ldapPassword used for connecting to the remote ldapHost.
     */
    public String getLdapPassword() {
        return ldapPassword;
    }

    /**
     * Sets the ldapPort number used to connect to remote ldapHost machine.
     *
     * @param i The ldapPort number.
     */
    public void setLdapPort(int i) {
        this.ldapPort = i;
    }

    /**
     * Returns the ldapPort number used to connect to remote ldapHost machine.
     *
     * @return The ldapPort number.
     */
    public int getLdapPort() {
        return ldapPort;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ldapSSL The ldapSSL to set.
     */
    public void setLdapSSL(String ldapSSL) {
        this.ldapSSL = ldapSSL;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the ldapSSL.
     */
    public String getLdapSSL() {
        return ldapSSL;
    }

    /**
     * Sets the ldapUser of the LDAP to connect.
     *
     * @param user The ldapUser used for connecting to LDAP.
     */
    public void setLdapUser(String user) {
        this.ldapUser = user;
    }

    /**
     * Returns the name of the User to be used for connecting to ECX machine.
     *
     * @return The ldapUser used for connecting to LDAP.
     */
    public String getLdapUser() {
        return ldapUser;
    }

    /**
     * Sets the operation type to load or unload.
     *
     * @param sType Possible values are 'start', 'stop' and 'restart'.
     */
    public void setOperation(String sType) {
        if (!(sType.equalsIgnoreCase("start") ||
                sType.equalsIgnoreCase("stop") ||
                sType.equalsIgnoreCase("restart"))) {
            atlLog.error("Invalid operation type : " + sType);

            return;
        }

        operation = sType.toLowerCase();
    }

    /**
     * This method gets the current operation.
     *
     * @return The current operation.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * This method sets the organization.
     *
     * @param organization The organization.
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * This method gets the organization.
     *
     * @return The organization.
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * Sets the user context from which requests will be fired If not set, then
     * it will take the default user context
     *
     * @param dn DN of the user
     */
    public void setUserDN(String dn) {
        this.userdn = dn;
    }

    /**
     * Returns the user context from which requests will be fired
     *
     * @return The DN of the user.
     */
    public String getUserDN() {
        return userdn;
    }

    /**
     * This method adds the processor to the list.
     *
     * @param pProcessor The processor to add.
     */
    public void addConfiguredProcessor(Processor pProcessor) {
        alProcessors.add(pProcessor);
    }

    /**
     * Implementing the abstract method of class Task
     */
    public void execute() {
        validateAttributes();

        //Create SoapRequestManager object
        ISoapRequestManager srmSoap = null;

        try {
            if (ldapHost != null) {
                srmSoap = ContentManagerTask.createRequestManager(this,
                        ldapHost, ldapUser, ldapPassword, ldapPort, userdn,
                        organization);
            } else {
                srmSoap = new BusSoapRequestManager(this);
            }
        } catch (SoapRequestException sre) {
            GeneralUtils.handleException("Error occured while sending request " +
                "to ECX machine.\n" + sre.getMessage(), sre, this);
        }

        try {
            srmSoap.addNomCollector(new NomCollector());
            srmSoap.getCurrentNomCollector().getMessageContext().setDocument(srmSoap.getDocument());
            sendMonitorRequest(srmSoap);
        } catch (Exception sre) {
            GeneralUtils.handleException("Error occured while sending request " +
                "to ECX machine.\n" + sre.getMessage(), sre, this);
        }
        finally {
            srmSoap.removeNomCollector().deleteNodes();
        }   
    }

    /**
     * This method send the XML-message that will Start, stop or restart the
     * SOAP-processor identified by sDN
     *
     * @param scSoap The connector to use.
     */
    protected void sendMonitorRequest(ISoapRequestManager scSoap)
        throws Exception {
        String action = "";

        if (getOperation().equalsIgnoreCase("stop")) {
            action = "Stop";
        } else if (this.operation.equalsIgnoreCase("start")) {
            action = "Start";
        } else if (this.operation.equalsIgnoreCase("restart")) {
            action = "Restart";
        }

        for (Iterator<Processor> iProcessor = alProcessors.iterator();
                iProcessor.hasNext();) {
            Processor pProcessor = iProcessor.next();
            String sFullDN = getFullDN(pProcessor);
            atlLog.info(operation.toLowerCase() + " processor " + sFullDN);

            String sXml = "<" + action +
                " xmlns=\"http://schemas.cordys.com/1.0/monitor\">" +
                "	<name>" + sFullDN + "</name>" + "</" + action + ">";

            atlLog.debug(sXml);

            Message mRequest = scSoap.getCurrentNomCollector().getMessageContext().createMessage(sXml);
            Message mResponse = scSoap.makeSoapRequest(mRequest);

            atlLog.debug("Response : " + mResponse.toString());
        }
    }

    /**
     * Ensure we have a consistent and legal set of attributes, and set any
     * internal flags necessary based on different combinations of attributes.
     */
    protected void validateAttributes() throws BuildException {
        if ((ldapHost != null) || ((ldapPort != -1) && (ldapUser != null)) ||
                (ldapPassword != null)) {
            //If any of the proerty is set, then all must be set 
            //Else throw Exception
            if ((ldapHost == null) || ((ldapPort == -1) && (ldapUser == null)) ||
                    (ldapPassword == null)) {
                throw new BuildException(
                    "If LDAP properties are set, then all " +
                    "(ldaphost, ldapprot, ldapuser, ldappassword) " +
                    "must be set together");
            }
        }

        if ((operation == null) || (operation.length() == 0)) {
            throw new BuildException("operation attribute needs to be set.");
        }
    }

    /**
     * This method returns the actual full dn of the soap processor. The full
     * DN is determined based on either the organization that is set on the
     * MonitorTask or the organization that is set on the ProcessorTask.
     *
     * @param pProcessor The processor.
     *
     * @return The full DN for the processor.
     */
    private String getFullDN(Processor pProcessor) {
        String sReturn = pProcessor.getDn() + ",cn=soap nodes,";
        String sTemp = pProcessor.getOrganization();

        if (!((sTemp != null) && (sTemp.length() > 0))) {
            sTemp = getOrganization();
        }

        if ((sTemp == null) || (sTemp.length() == 0)) {
            throw new BuildException("No organizational context is specified.");
        }

        sReturn += sTemp;

        return sReturn;
    }
}
