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


/**
 * The soap request interface allows users to implement this interface and add
 * their own methods. The Soap Request hence created by implementing this
 * interface can be got by using the createSoapRequest method in
 * SoapRequestManager class by typecasting the same.
 * 
 * @author msreejit
 */
public interface ISoapRequest
{
    /**
     * Sets the user dn. The user dn will be appended to the soap request when
     * execute method is called.
     * 
     * @param userDN
     *            The user's distinguished name(DN) which will be used to send
     *            the request.
     */
    public abstract void setUser(String userDN);

    /**
     * Adds methods to the request
     * 
     * @param namespace
     *            The namespace of the method.
     * @param method
     *            The name of the method to be added.
     * @param paramNames
     *            The array of names of parameters.
     * @param paramValues
     *            The array of values of parameters.
     * @return The newly created method node reference
     * @throws SoapRequestException
     */
    public abstract int addMethod(String namespace, String method,
            String[] paramNames, String[] paramValues)
            throws SoapRequestException;

    /**
     * Adds methods to the request.
     * 
     * @param organization
     *            The dn of the organization.
     * @param namespace
     *            The namespace of the method.
     * @param method
     *            The name of the method to be added.
     * @param paramNames
     *            The array of names of parameters.
     * @param paramValues
     *            The array of values of parameters.
     * @return The newly created method node reference
     * @throws SoapRequestException
     */
    public abstract int addMethod(String organization, String namespace,
            String method, String[] paramNames, String[] paramValues)
            throws SoapRequestException;

    /**
     * Adds methods to the request
     * 
     * @param namespace
     *            The namespace of the method
     * @param method
     *            The name of the method to be added
     * @param paramNode
     *            The parameter node to be appended to method node root
     * @return The newly created method node reference
     * @throws SoapRequestException
     */
    public abstract int addMethod(String namespace, String method, int paramNode)
            throws SoapRequestException;

    /**
     * Adds methods to the request
     * 
     * @param organization
     *            The dn of the organization
     * @param namespace
     *            The namespace of the method
     * @param method
     *            The name of the method to be added
     * @param paramNode
     *            The parameter node to be appended to method node root
     * @return The newly created method node reference
     * @throws SoapRequestException
     */
    public abstract int addMethod(String organization, String namespace,
            String method, int paramNode) throws SoapRequestException;

    /**
     * Adds methods to the request
     * 
     * @param namespace
     *            The namespace of the method
     * @param method
     *            The name of the method to be added
     * @param paramNodes
     *            The parameter nodes which are to be appended to the method
     *            root node
     * @return The request node with newly added methods
     * @throws SoapRequestException
     */
    public abstract int addMethod(String namespace, String method,
            int[] paramNodes) throws SoapRequestException;

    /**
     * Adds methods to the request
     * 
     * @param namespace
     *            The namespace of the method
     * @param method
     *            The name of the method to be added
     * @return The request node with newly added methods
     * @throws SoapRequestException
     */
    public abstract int addMethod(String namespace, String method)
            throws SoapRequestException;

    /**
     * Adds methods to the request
     * 
     * @param receiver
     *            The dn of the soap node to which request is to be send
     * @param organization
     *            The dn of the organization
     * @param namespace
     *            The namespace of the method
     * @param method
     *            The name of the method to be added
     * @param paramNodes
     *            The parameter nodes which are to be appended to the method
     *            root node
     * @return The request node with newly added methods
     * @throws SoapRequestException
     */
    public abstract int addMethod(String receiver, String organization,
            String namespace, String method, int[] paramNodes)
            throws SoapRequestException;

    /**
     * Adds methods to the request
     * 
     * @param organization
     *            The dn of the organization
     * @param namespace
     *            The namespace of the method
     * @param method
     *            The name of the method to be added
     * @param paramNodes
     *            The parameter nodes which are to be appended to the method
     *            root node
     * @return The request node with newly added methods
     * @throws SoapRequestException
     */
    public abstract int addMethod(String organization, String namespace,
            String method, int[] paramNodes) throws SoapRequestException;

    /**
     * Adds methods to the request
     * 
     * @param organization
     *            The dn of the organization
     * @param node
     *            Request Node
     * @return The request node with newly added methods
     * @throws SoapRequestException
     */
    public abstract int addMethod(String organization, int node)
            throws SoapRequestException;
    
    /**
     * Sets the SOAP request envelope.
     * @param envelope SOAP envelope.
     */
    public abstract void setSoapRequest(int envelope);

    /**
     * Executes the soap request and returns the soap response
     * 
     * @return The response of request
     * @throws SoapRequestException
     */
    public abstract int execute() throws SoapRequestException;

    /**
     * Executes the soap request and returns the soap response
     *
     * @param timeout The timeout to overide the default timeout.
     *
     * @return The response of request
     *
     * @throws SoapRequestException
     */
    public int execute(long timeout) throws SoapRequestException;

    /**
     * Returns the receiver.
     *
     * @return Returns the receiver.
     */
    public String getReceiver();

    /**
     * Sets the receiver.
     *
     * @param receiver The receiver to be set.
     */
    public void setReceiver(String receiver);
}
