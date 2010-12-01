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

import org.apache.tools.ant.Task;

import com.cordys.tools.ant.util.GeneralUtils;
import com.eibus.xml.nom.Document;

/**
 * This Class creates Soap Messages and sends them to appropriate backend based
 * on the organization. It provides the Document object for handling tuples.
 *
 * @author msreejit
 * @author mviswam
 */
public class GatewaySoapRequestManager extends SoapRequestManagerBase
{
    /**
     * NOM Document.
     */
    private Document dDocument = new Document();
    /**
     * Containst the web gateway connection.
     */
    private GatewaySoapConnection gscConnection;
    /**
     * The instance of the Class which implements the interface
     * <code>ISoapRequest</code>
     */
    private ISoapRequest soapRequestInstance;
    /**
     * LDAP root.
     */
    private String sLdapRoot;

    /**
     * Default Constructor.
     *
     * @throws SoapRequestException
     */
    public GatewaySoapRequestManager()
                              throws SoapRequestException
    {
        clazz = GatewaySoapRequest.class.getName();
    }

    /**
     * Parameterized Constructor for sending request to different ECX. Creates
     * a new instance of SoapRequestManager
     *
     * @param uGatewayUrl The ECX gateway URL to which the soap request has to
     *        be sent.
     * @param username The user name for connecting to the LDAP Server.
     * @param password The password for connecting to the LDAP Server.
     *
     * @throws SoapRequestException If any error happens while sending request
     *         to backend.
     */
    public GatewaySoapRequestManager(HttpConnectionConfig connectionConfig)
                              throws SoapRequestException
    {
        this();
        
        gscConnection = new GatewaySoapConnection(connectionConfig);
    }

    /**
     * Parameterized Constructor for sending request to different ECX. Creates
     * a new instance of SoapRequestManager
     *
     * @param uGatewayUrl The ECX gateway URL to which the soap request has to
     *        be sent.
     * @param username The user name for connecting to the LDAP Server.
     * @param password The password for connecting to the LDAP Server.
     * @param task The ANT Task to be used with this manager. The logging
     *        mechanism of ANT would be used for logging the messages. Can be
     *        null if logging is not required.
     *
     * @throws SoapRequestException If any error happens while sending request
     *         to backend.
     */
    public GatewaySoapRequestManager(HttpConnectionConfig connectionConfig, Task task)
                              throws SoapRequestException
    {
        this(connectionConfig);

        // set the task to be used.
        antTask = task;
        
        log("Connecting to web gateway:\n" + connectionConfig);
    }

    /**
     * Returns the top NOM collector from the stack.
     *
     * @return Topmost NOM collector or <code>null</code> if the stack was
     *         empty.
     */
    public NomCollector getCurrentNomCollector()
    {
        if (lNomCollectorStack.isEmpty())
        {
            return null;
        }

        return (NomCollector) lNomCollectorStack.getLast();
    }

    /**
     * Used to get the Document linked to the connector used in the
     * <code>SoapRequestManager</code>.
     *
     * @return The instance of Document used.
     */
    public Document getDocument()
    {
        return dDocument;
    }

    /**
     * DOCUMENTME
     *
     * @param sLdapRoot DOCUMENTME
     */
    public void setLdapRoot(String sLdapRoot)
    {
        this.sLdapRoot = sLdapRoot;
    }

    /**
     * @see com.cordys.tools.ant.soap.ISoapRequestManager#getLdapRoot()
     */
    public String getLdapRoot()
    {
        return sLdapRoot;
    }

    /**
     * DOCUMENT ME!
     *
     * @param aNomCollectionEnabled The nomCollectionEnabled to set.
     */
    public void setNomCollectionEnabled(boolean aNomCollectionEnabled)
    {
        bNomCollectionEnabled = aNomCollectionEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the nomCollectionEnabled.
     */
    public boolean isNomCollectionEnabled()
    {
        return (!lNomCollectorStack.isEmpty()) ? bNomCollectionEnabled : false;
    }

    /**
     * Set the class which implements the interface <code>ISoapRequest</code>
     * This method can be used to set the class which implements interface
     * methods that the SoapRequestManager class uses.
     *
     * @param clazz The class which implements the interface
     *        <code>ISoapRequest</code>
     */
    public void setSoapRequestClass(String clazz)
    {
        this.clazz = clazz;
    }

    /**
     * Adds a new collector to the stack.
     *
     * @param nmCollector NOM collector to be added.
     */
    public void addNomCollector(NomCollector nmCollector)
    {
        lNomCollectorStack.add(nmCollector);
    }

    /**
     * @see com.cordys.tools.ant.soap.ISoapRequestManager#addNomGarbage(int)
     */
    public void addNomGarbage(int iNode)
                       throws IllegalStateException
    {
        NomCollector ncColl = getCurrentNomCollector();

        if (ncColl != null)
        {
            ncColl.addNode(iNode);
        }
    }

    /**
     * Creates the soap request
     *
     * @return The Soap Request Object
     *
     * @throws SoapRequestException If the creation of soap request object
     *         implementing the interface gives an error.
     */
    public ISoapRequest createSoapRequest()
                                   throws SoapRequestException
    {
        try
        {
            Class<?> clz = Class.forName(clazz);
            Object[] objects = new Object[] { gscConnection, this };
            Class<?>[] classes = new Class[]
                              {
                                  GatewaySoapConnection.class,
                                  GatewaySoapRequestManager.class
                              };
            Constructor<?> constructor = clz.getConstructor(classes);
            soapRequestInstance = (ISoapRequest) constructor.newInstance(objects);
        }
        catch (SecurityException e)
        {
            throw new SoapRequestException(e);
        }
        catch (IllegalArgumentException e)
        {
            throw new SoapRequestException(e);
        }
        catch (ClassNotFoundException e)
        {
            throw new SoapRequestException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new SoapRequestException(e);
        }
        catch (InstantiationException e)
        {
            throw new SoapRequestException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new SoapRequestException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new SoapRequestException(e);
        }

        if (!(soapRequestInstance instanceof ISoapRequest))
        {
            throw new SoapRequestException("The soap request handling class should implement ISoapRequest interface");
        }

        return soapRequestInstance;
    }

    /**
     * Initializes this instance.
     */
    public void initialize()
                    throws SoapRequestException
    {
        clazz = GatewaySoapRequest.class.getName();
        this.sLdapRoot = GeneralUtils.getLdapRootFromOrganization(sOrganizationDN);

        if (sLdapRoot == null)
        {
            throw new SoapRequestException("Unable to get the LDAP root from organization '" +
                                           sOrganizationDN + "'.");
        }

        gscConnection.setDefaultOrganization(sOrganizationDN);
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
                               String[] paramNames, String[] paramValues)
                        throws SoapRequestException
    {
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
                               String method, String[] paramNames,
                               String[] paramValues)
                        throws SoapRequestException
    {
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
                               String namespace, String method,
                               String[] paramNames, String[] paramValues)
                        throws SoapRequestException
    {
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
                        throws SoapRequestException
    {
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
                               String method, int paramNode)
                        throws SoapRequestException
    {
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
                        throws SoapRequestException
    {
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
                        throws SoapRequestException
    {
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
                               String method, int[] paramNodes)
                        throws SoapRequestException
    {
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
                        throws SoapRequestException
    {
        ISoapRequest soapRequest = createSoapRequest();
        soapRequest.setUser(userDN);
        soapRequest.addMethod(organization, namespace, method, paramNodes);

        return soapRequest.execute();
    }

    /**
     * Removes the top NOM collector from the stack.
     *
     * @return Removed topmost NOM collector or <code>null</code> if the stack
     *         was empty.
     */
    public NomCollector removeNomCollector()
    {
        if (lNomCollectorStack.isEmpty())
        {
            return null;
        }

        return (NomCollector) lNomCollectorStack.removeLast();
    }
}
