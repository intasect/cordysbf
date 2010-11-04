/**
 * Copyright 2004 Cordys R&D B.V. 
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
package com.cordys.tools.ant.soap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.util.GeneralUtils;
import com.eibus.connector.nom.Connector;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.directory.soap.LDAPDirectory;
import com.eibus.exception.ExceptionGroup;
import com.eibus.util.Base64;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Document;
import com.novell.ldap.LDAPException;


/**
 * This Class creates Soap Messages and sends them to appropriate backend based
 * on the organization. It provides the Document object for handling tuples.
 *
 * @author msreejit
 * @author mviswam
 */
public class BusSoapRequestManager extends SoapRequestManagerBase {
    /**
     * The instance of the Class which implements the interface
     * <code>ISoapRequest</code>
     */
    ISoapRequest soapRequestInstance;

    /**
     * The LDAP Directory to which connection has to be made.
     */
    LDAPDirectory ldapDirectory;

    /**
     * A Connector holds an XML connection to Cordys WCP. Used to send and
     * receive SOAP messages.
     */
    private Connector connector;
    
    private static Document dSharedDoc = new Document();

    /**
     * Default Constructor.
     *
     * @throws SoapRequestException
     */
    public BusSoapRequestManager() throws SoapRequestException {
        // first get the connector instantiated.
        connector = getConnectorInstance();

        // then get the Default LDAP Directory from the connector and set it.
        setLDAPDirectory(connector.getMiddleware().getDirectory());
    }

    /**
     * Parameterized Constructor for usage with ANT tasks.
     *
     * @param task The ANT Task to be used with this manager. The logging
     *        mechanism of ANT would be used for logging the messages.
     *
     * @throws SoapRequestException
     */
    public BusSoapRequestManager(Task task) throws SoapRequestException {
        // set the task to be used.
        antTask = task;

        // first get the connector instantiated.
        connector = getConnectorInstance();

        // then get the Default LDAP Directory from the connector and set it.
        setLDAPDirectory(connector.getMiddleware().getDirectory());

        log("Connecting to host:" + ldapDirectory.getConnection().getHost() +
            " at port:" + ldapDirectory.getConnection().getPort());
    }

    /**
     * Parameterized Constructor for sending request to different ECX. Creates
     * a new instance of SoapRequestManager
     *
     * @param host The ECX machine to which the soap request has to be sent.
     * @param port The port number at which the LDAP Server can be connected.
     * @param username The user name for connecting to the LDAP Server.
     * @param password The password for connecting to the LDAP Server.
     * @param ssl SSL enabled
     * @param sTrustStorePath SSL truststore path.
     * @param sTrustStorePassword SSL truststore password.
     *
     * @throws SoapRequestException If any error happens while sending request
     *         to backend.
     */
    public BusSoapRequestManager(String host, int port, String username,
        String password, boolean ssl, String sTrustStorePath,
        String sTrustStorePassword) throws SoapRequestException {
        // first set the LDAP Directory using given credentials.
        try {
            // mpoyhone: SSL Properties must be set here because the LDAP connection
            // creation needs them to be set.
            EIBProperties.getProperties()
                         .setProperty("bus.ldap.processor.ssl",
                ssl ? "true" : "false");

            if (ssl) {
                if (sTrustStorePath == null) {
                    throw new SoapRequestException("Property '" +
                        ContentManagerTask.PROPERTY_BUS_TRUSTSTORE_PATH +
                        "' is must be set for SSL connections.");
                }

                if (sTrustStorePassword == null) {
                    throw new SoapRequestException("Property '" +
                        ContentManagerTask.PROPERTY_BUS_TRUSTSTORE_PASSWORD +
                        "' is must be set for SSL connections.");
                }

                EIBProperties.getProperties()
                             .setProperty("bus.ssl.truststore", sTrustStorePath);
                EIBProperties.getProperties()
                             .setProperty("bus.ssl.truststorepassword",
                    sTrustStorePassword);
            }
            
            // mpoyhone: First create one instance of LDAPDirectory to get the LDAP
            // root DN (first instance uses a wrong DN). Then we get the right root DN
            // and create a new instance.
            LDAPDirectory ldDir = new LDAPDirectory(host, port, username, password);
            String sRoot = GeneralUtils.getRootDN(ldDir.getConnection());
            
            EIBProperties.getProperties().setProperty("ldap.root", "cn=cordys," + sRoot);
            ldDir = new LDAPDirectory(host, port, username, password);

            // PGUSSOW: In order to handle differences in DNS suffix between the
            // developer machine and the cordys server we're going to modify the
            // EIB.properties
            // with the new LDAP values. NOTE: We will NOT save these values.
            String sBase64EncodePW = Base64.encode(password);
            EIBProperties.getProperties()
                         .setProperty("bus.ldap.processor.password",
                sBase64EncodePW);
            EIBProperties.getProperties()
                         .setProperty("bus.administrator.pwd", sBase64EncodePW);
            EIBProperties.getProperties()
                         .setProperty("bus.ldap.processor.host", host);
            EIBProperties.getProperties()
                         .setProperty("bus.administrator.user", username);
            EIBProperties.getProperties()
                         .setProperty("bus.ldap.processor.port",
                String.valueOf(port));
            EIBProperties.getProperties()
                         .setProperty("bus.ldap.processor.user", username);
            EIBProperties.getProperties()
                         .setProperty("ldap.server", host + ":" + port);
            EIBProperties.getProperties()
                         .setProperty("ldap.soap.processor.dn",
                "cn=LDAP Processor,cn=LDAP Service,cn=soap nodes,o=system,cn=cordys," +
                sRoot);

            // mpoyhone: This is needed to avoid a NullPointerException in wcp.jar.
            // mpoyhone: 2007-09-20: Commented out for C3.
            //ldDir.switchToDirectConnection(new LDAPWrapper(host,port,username,password));
            setLDAPDirectory(ldDir);
        } catch (LDAPException ex) {
            StringBuffer mess = new StringBuffer("Connect Error\n");
            mess.append(
                "Note: did you set property bus.ldap.processor.ssl correctly\n");
            throw new SoapRequestException(mess.toString(), ex);
        } catch (Exception de) {
            // handle exception
            throw new SoapRequestException(de.getMessage(), de);
        }

        // then get the connector instantiated.
        connector = getConnectorInstance();
    }

    /**
     * Parameterized Constructor for sending request to different ECX. Creates
     * a new instance of SoapRequestManager
     *
     * @param host The ECX machine to which the soap request has to be sent.
     * @param port The port number at which the LDAP Server can be connected.
     * @param username The user name for connecting to the LDAP Server.
     * @param password The password for connecting to the LDAP Server.
     * @param ssl true if SSL is enabled
     * @param sTrustStorePath SSL truststore path.
     * @param sTrustStorePassword SSL truststore password.
     * @param task The ANT Task to be used with this manager. The logging
     *        mechanism of ANT would be used for logging the messages. Can be
     *        null if logging is not required.
     *
     * @throws SoapRequestException If any error happens while sending request
     *         to backend.
     */
    public BusSoapRequestManager(String host, int port, String username,
        String password, boolean ssl, String sTrustStorePath,
        String sTrustStorePassword, Task task) throws SoapRequestException {
        this(host, port, username, password, ssl, sTrustStorePath,
            sTrustStorePassword);

        // set the task to be used.
        antTask = task;

        log("Connecting to remote host:" +
            ldapDirectory.getConnection().getHost() + " at port:" +
            ldapDirectory.getConnection().getPort());

        // Output the EIBProperties being used:
        Properties pTemp = EIBProperties.getProperties();
        log("EIBProperties being used:", Project.MSG_VERBOSE);

        for (Iterator<?> iKeys = pTemp.keySet().iterator(); iKeys.hasNext();) {
            String sKey = (String) iKeys.next();
            String sValue = pTemp.getProperty(sKey);
            log(sKey + ":" + sValue, Project.MSG_VERBOSE);
        }
    }

    /**
     * Returns the Connector Instance used in the
     * <code>SoapRequestManager</code>.
     *
     * @return The instance of Connector used.
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * Used to get the Document linked to the connector used in the
     * <code>SoapRequestManager</code>.
     *
     * @return The instance of Document used.
     */
    public Document getDocument() {
        return dSharedDoc;
    }

    /**
     * Returns the LDAP Directory used in the <code>SoapRequestManager</code>.
     *
     * @return The instance of LDAPDirectory.
     */
    public LDAPDirectory getLdapDirectory() {
        return ldapDirectory;
    }

    /**
     * @see com.cordys.tools.ant.soap.ISoapRequestManager#getLdapRoot()
     */
    public String getLdapRoot() {
        return GeneralUtils.getLdapRootFromOrganization(sOrganizationDN);

        /*
         * try { return GeneralUtils.getRootDN(ldapDirectory.getConnection()); }
         * catch (LDAPException e) { log("Unable to get the LDAP root: " + e,
         * Project.MSG_ERR); return null; }
         */
    }

    /**
     * Sets the timeout for the soap requests that are sent.
     *
     * @param timeout The timeout value to be set
     */
    public void setTimeout(long timeout) {
        connector.setTimeout(timeout);
    }

    /**
     * Creates the soap request
     *
     * @return The Soap Request Object
     *
     * @throws SoapRequestException If the creation of soap request object
     *         implementing the interface gives an error.
     */
    public ISoapRequest createSoapRequest() throws SoapRequestException {
        try {
            Class<?> clz = Class.forName(clazz);
            Object[] objects = new Object[] { ldapDirectory, this };
            Class<?>[] classes = new Class[] {
                    LDAPDirectory.class, BusSoapRequestManager.class
                };
            Constructor<?> constructor = clz.getConstructor(classes);
            soapRequestInstance = (ISoapRequest) constructor.newInstance(objects);
        } catch (SecurityException e) {
            throw new SoapRequestException(e);
        } catch (IllegalArgumentException e) {
            throw new SoapRequestException(e);
        } catch (ClassNotFoundException e) {
            throw new SoapRequestException(e);
        } catch (NoSuchMethodException e) {
            throw new SoapRequestException(e);
        } catch (InstantiationException e) {
            throw new SoapRequestException(e);
        } catch (IllegalAccessException e) {
            throw new SoapRequestException(e);
        } catch (InvocationTargetException e) {
            throw new SoapRequestException(e);
        }

        if (!(soapRequestInstance instanceof ISoapRequest)) {
            throw new SoapRequestException(
                "The soap request handling class should implement ISoapRequest interface");
        }

        // (mpoyhone) What is this now?????? Remove this perharps?
        soapRequestInstance = new BusSoapRequest(ldapDirectory, this);

        soapRequestInstance.setUser(getUserDN());

        return soapRequestInstance;
    }

    /**
     * Initializes this instance.
     */
    public void initialize() throws SoapRequestException {
        clazz = BusSoapRequest.class.getName();
    }

    /**
     * Resolves the receiver to send the Soap Message, creates the Soap
     * Message, sends the Soap Message and returns the Response XML Node.
     *
     * @param namespace The namespace of the method to be invoked.
     * @param method The name of the method to be invoked.
     * @param paramNames The parameter's names which are to be set.
     * @param paramValues The parameter's values which are to be set.
     *
     * @return The Soap Message Response XML Node.
     *
     * @throws SoapRequestException
     */
    public int makeSoapRequest(String namespace, String method,
        String[] paramNames, String[] paramValues) throws SoapRequestException {
        return makeSoapRequest(null, null, namespace, method, paramNames,
            paramValues);
    }

    /**
     * Resolves the receiver to send the Soap Message, creates the Soap
     * Message, sends the Soap Message and returns the Response XML Node.
     *
     * @param organization The dn of the organization.
     * @param namespace The namespace of the method to be invoked.
     * @param method The name of the method to be invoked.
     * @param paramNames The parameter's names which are to be set.
     * @param paramValues The parameter's values which are to be set.
     *
     * @return The Soap Message Response XML Node.
     *
     * @throws SoapRequestException
     */
    public int makeSoapRequest(String organization, String namespace,
        String method, String[] paramNames, String[] paramValues)
        throws SoapRequestException {
        return makeSoapRequest(null, organization, namespace, method,
            paramNames, paramValues);
    }

    /**
     * Resolves the receiver to send the Soap Message, creates the Soap
     * Message, sends the Soap Message and returns the Response XML Node.
     *
     * @param userDN The dn of the user which is used to send the request.
     * @param organization The dn of the organization.
     * @param namespace The namespace of the method to be invoked.
     * @param method The name of the method to be invoked.
     * @param paramNames The parameter's names which are to be set.
     * @param paramValues The parameter's values which are to be set.
     *
     * @return The Soap Message Response XML Node.
     *
     * @throws SoapRequestException
     */
    public int makeSoapRequest(String userDN, String organization,
        String namespace, String method, String[] paramNames,
        String[] paramValues) throws SoapRequestException {
        ISoapRequest soapRequest = createSoapRequest();
        soapRequest.setUser(userDN);
        soapRequest.addMethod(organization, namespace, method, paramNames,
            paramValues);

        return soapRequest.execute();
    }

    /**
     * Resolves the receiver to send the Soap Message, creates the Soap
     * Message, sends the Soap Message and returns the Response XML Node.
     *
     * @param namespace The namespace of the method to be invoked.
     * @param method The name of the method to be invoked.
     * @param paramNode The parameter tuple nodes which are to be send with the
     *        request.
     *
     * @return The Soap Message Response XML Node.
     *
     * @throws SoapRequestException
     */
    public int makeSoapRequest(String namespace, String method, int paramNode)
        throws SoapRequestException {
        return makeSoapRequest(null, null, namespace, method, paramNode);
    }

    /**
     * Resolves the receiver to send the Soap Message, creates the Soap
     * Message, sends the Soap Message and returns the Response XML Node.
     *
     * @param organization The dn of the organization.
     * @param namespace The namespace of the method to be invoked.
     * @param method The name of the method to be invoked.
     * @param paramNode The parameter tuple nodes which are to be send with the
     *        request.
     *
     * @return The Soap Message Response XML Node.
     *
     * @throws SoapRequestException
     */
    public int makeSoapRequest(String organization, String namespace,
        String method, int paramNode) throws SoapRequestException {
        return makeSoapRequest(null, organization, namespace, method, paramNode);
    }

    /**
     * Resolves the receiver to send the Soap Message, creates the Soap
     * Message, sends the Soap Message and returns the Response XML Node.
     *
     * @param userDN The dn of the user which is used to send the request.
     * @param organization The dn of the organization.
     * @param namespace The namespace of the method to be invoked.
     * @param method The name of the method to be invoked.
     * @param paramNode The parameter tuple nodes which are to be send with the
     *        request.
     *
     * @return The Soap Message Response XML Node.
     *
     * @throws SoapRequestException
     */
    public int makeSoapRequest(String userDN, String organization,
        String namespace, String method, int paramNode)
        throws SoapRequestException {
        ISoapRequest soapRequest = createSoapRequest();
        soapRequest.setUser(userDN);
        soapRequest.addMethod(organization, namespace, method, paramNode);

        return soapRequest.execute();
    }

    /**
     * Resolves the receiver to send the Soap Message, creates the Soap
     * Message, sends the Soap Message and returns the Response XML Node.
     *
     * @param namespace The namespace of the method to be invoked.
     * @param method The name of the method to be invoked.
     * @param paramNodes The parameter tuple nodes which are to be send with
     *        the request.
     *
     * @return The Soap Message Response XML Node.
     *
     * @throws SoapRequestException
     */
    public int makeSoapRequest(String namespace, String method, int[] paramNodes)
        throws SoapRequestException {
        return makeSoapRequest(null, null, namespace, method, paramNodes);
    }

    /**
     * Resolves the receiver to send the Soap Message, creates the Soap
     * Message, sends the Soap Message and returns the Response XML Node.
     *
     * @param organization The dn of the organization where the method is
     *        available.
     * @param namespace The namespace of the method to be invoked.
     * @param method The name of the method to be invoked.
     * @param paramNodes The parameter tuple nodes which are to be send with
     *        the request.
     *
     * @return The Soap Message Response XML Node.
     *
     * @throws SoapRequestException
     */
    public int makeSoapRequest(String organization, String namespace,
        String method, int[] paramNodes) throws SoapRequestException {
        return makeSoapRequest(null, organization, namespace, method, paramNodes);
    }

    /**
     * Resolves the receiver to send the Soap Message, creates the Soap
     * Message, sends the Soap Message and returns the Response XML Node.
     *
     * @param userDN The dn of the user which is used to send the request.
     * @param organization The dn of the organization where the method is
     *        available.
     * @param namespace The namespace of the method to be invoked.
     * @param method The name of the method to be invoked.
     * @param paramNodes The parameter tuple nodes which are to be send with
     *        the request.
     *
     * @return The Soap Message Response XML Node.
     *
     * @throws SoapRequestException
     */
    public int makeSoapRequest(String userDN, String organization,
        String namespace, String method, int[] paramNodes)
        throws SoapRequestException {
        ISoapRequest soapRequest = createSoapRequest();
        soapRequest.setUser(userDN);
        soapRequest.addMethod(organization, namespace, method, paramNodes);

        return soapRequest.execute();
    }

    /**
     * Creates and sets a new connection to the bus.
     *
     * @return The instance of the connector.
     *
     * @throws SoapRequestException If any error occurs while connecting to
     *         LDAP to get a connection
     */
    private Connector getConnectorInstance() throws SoapRequestException {
        try {
            connector = Connector.getInstance(getDefaultConnectorName());
        } catch (ExceptionGroup e) {
            throw new SoapRequestException(
                "Exception occured while getting a connector instance.\n" +
                e.getMessage(), e);
        } catch (DirectoryException e) {
            throw new SoapRequestException(
                "Exception occured while getting a connector instance.\n" +
                e.getMessage(), e);
        }

        connector.open();

        return connector;
    }

    /**
     * The method returns the connector name.
     *
     * @return The connector name.
     */
    private String getDefaultConnectorName() {
        String defaultName = "anonymous";

        try {
            defaultName = defaultName + "@" +
                InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe) {
            /* ignorable */
        }

        return defaultName;
    }

    /**
     * The method sets the LDAP Directory of the
     * <code>SoapRequestManager</code>.
     *
     * @param ldapDirectory The LDAP Directory to which connection has to be
     *        made.
     */
    private void setLDAPDirectory(LDAPDirectory ldapDirectory) {
        this.ldapDirectory = ldapDirectory;
    }
}
