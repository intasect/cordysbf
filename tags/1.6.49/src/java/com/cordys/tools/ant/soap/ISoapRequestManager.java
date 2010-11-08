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
package com.cordys.tools.ant.soap;

import com.cordys.coe.util.xml.Message;
import com.cordys.tools.ant.cm.BcpVersionInfo;

import com.eibus.xml.nom.Document;

import org.apache.tools.ant.Task;

/**
 * Interface for sending SOAP requests to Cordys. Refactored from the old
 * SOAPRequestManager class.
 *
 * @author mpoyhone
 */
public interface ISoapRequestManager
{
    /**
     * Message level of error messages.
     */
    public static final int MSG_ERR = 0;
    /**
     * Message level of warning messages.
     */
    public static final int MSG_WARN = 1;
    /**
     * Message level of information messages.
     */
    public static final int MSG_INFO = 2;
    /**
     * Message level of verbose messages.
     */
    public static final int MSG_VERBOSE = 3;
    /**
     * Message level of debug messages.
     */
    public static final int MSG_DEBUG = 4;

    /**
     * DOCUMENT ME!
     *
     * @param aAntTask The Ant task to set.
     */
    public abstract void setAntTask(Task aAntTask);

    /**
     * DOCUMENT ME!
     *
     * @return Returns the Ant task.
     */
    public abstract Task getAntTask();

    /**
     * Returns the top NOM collector from the stack.
     *
     * @return Topmost NOM collector or <code>null</code> if the stack was
     *         empty.
     */
    public abstract NomCollector getCurrentNomCollector();

    /**
     * Used to get the NOM Document.
     *
     * @return The instance of Document used.
     */
    public abstract Document getDocument();

    /**
     * Returns the LDAP root.
     *
     * @return LDAP root.
     */
    public abstract String getLdapRoot();

    /**
     * DOCUMENT ME!
     *
     * @param aNomCollectionEnabled The nomCollectionEnabled to set.
     */
    public abstract void setNomCollectionEnabled(boolean aNomCollectionEnabled);

    /**
     * DOCUMENT ME!
     *
     * @return Returns the nomCollectionEnabled.
     */
    public abstract boolean isNomCollectionEnabled();

    /**
     * Sets the current organization DN.
     *
     * @param sDN Organization DN.
     */
    public abstract void setOrganizationDN(String sDN);

    /**
     * Returns the current organization DN.
     *
     * @return Organization DN.
     */
    public abstract String getOrganizationDN();

    /**
     * Set the class which implements the interface <code>ISoapRequest</code>
     * This method can be used to set the class which implements interface
     * methods that the SoapRequestManager class uses.
     *
     * @param clazz The class which implements the interface
     *        <code>ISoapRequest</code>
     */
    public abstract void setSoapRequestClass(String clazz);

    /**
     * Sets the timeout for the soap requests that are sent.
     *
     * @param timeout The timeout value to be set
     */
    public abstract void setTimeout(long timeout);

    /**
     * Returns the timeout for the soap requests that are sent.
     *
     * @return The timeout value.
     */
    public abstract long getTimeout();

    /**
     * Sets the current user DN.
     *
     * @param sDN User DN.
     */
    public abstract void setUserDN(String sDN);

    /**
     * Returns the current user DN.
     *
     * @return User DN.
     */
    public abstract String getUserDN();

    /**
     * Adds a new collector to the stack.
     *
     * @param nmCollector NOM collector to be added.
     */
    public abstract void addNomCollector(NomCollector nmCollector);

    /**
     * Adds a NOM node to the topmost collector.
     *
     * @param iNode Node to be added.
     *
     * @throws IllegalStateException Thrown if there is no collector in the
     *         stack.
     */
    public abstract void addNomGarbage(int iNode)
                                throws IllegalStateException;

    /**
     * Creates the soap request
     *
     * @return The Soap Request Object
     *
     * @throws SoapRequestException If the creation of soap request object
     *         implementing the interface gives an error.
     */
    public abstract ISoapRequest createSoapRequest()
                                            throws SoapRequestException;

    /**
     * Initializes this instance.
     */
    public abstract void initialize()
                             throws SoapRequestException;

    /**
     * Resolves the receiver to send the Soap Message, creates the Soap
     * Message, sends the Soap Message and returns the Response XML Node.
     *
     * @param mRequest The namespace of the method to be invoked.
     *
     * @return The Soap Message Response XML Node.
     *
     * @throws SoapRequestException
     */
    public Message makeSoapRequest(Message mRequest)
                            throws SoapRequestException;

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
    public abstract int makeSoapRequest(String namespace, String method,
                                        String[] paramNames,
                                        String[] paramValues)
                                 throws SoapRequestException;

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
    public abstract int makeSoapRequest(String organization, String namespace,
                                        String method, String[] paramNames,
                                        String[] paramValues)
                                 throws SoapRequestException;

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
    public abstract int makeSoapRequest(String userDN, String organization,
                                        String namespace, String method,
                                        String[] paramNames,
                                        String[] paramValues)
                                 throws SoapRequestException;

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
    public abstract int makeSoapRequest(String namespace, String method,
                                        int paramNode)
                                 throws SoapRequestException;

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
    public abstract int makeSoapRequest(String organization, String namespace,
                                        String method, int paramNode)
                                 throws SoapRequestException;

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
    public abstract int makeSoapRequest(String userDN, String organization,
                                        String namespace, String method,
                                        int paramNode)
                                 throws SoapRequestException;

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
    public abstract int makeSoapRequest(String namespace, String method,
                                        int[] paramNodes)
                                 throws SoapRequestException;

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
    public abstract int makeSoapRequest(String organization, String namespace,
                                        String method, int[] paramNodes)
                                 throws SoapRequestException;

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
    public abstract int makeSoapRequest(String userDN, String organization,
                                        String namespace, String method,
                                        int[] paramNodes)
                                 throws SoapRequestException;

    /**
     * Removes the top NOM collector from the stack.
     *
     * @return Removed topmost NOM collector or <code>null</code> if the stack
     *         was empty.
     */
    public abstract NomCollector removeNomCollector();
    
    /**
     * Returns the bcpVersion.
     *
     * @return Returns the bcpVersion.
     */
    public BcpVersionInfo getBcpVersion();

    /**
     * Sets the bcpVersion.
     *
     * @param bcpVersion The bcpVersion to be set.
     */
    public void setBcpVersion(BcpVersionInfo bcpVersion);
}
