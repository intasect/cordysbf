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

import com.cordys.tools.ant.util.NodeUtil;

import com.eibus.connector.nom.Connector;
import com.eibus.connector.nom.SOAPMessage;

import com.eibus.directory.soap.DirectoryException;
import com.eibus.directory.soap.LDAPDirectory;

import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * The class represents a Soap Request Object. The Soap Request object can help
 * abstract the complexity involved in sending soap requests. The Soap Request
 * can contain multiple method request also. Usage Example:
 * <pre>
 * SoapRequestManager soapRequestMgr = new SoapRequestManager();
 * <br>
 * ISoapRequest soapRequest = soapRequestMgr.createSoapRequest();
 * <br>
 * soapRequest.addMethod(namespace, method, paramNames, paramValues);
 * <br>
 * soapRequest.addMethod(organization, namespace, method, paramNode);
 * <br>
 * soapRequest.execute();
 * <br>
 * </pre>
 *
 * @author msreejit
 */
public class BusSoapRequest
    implements ISoapRequest
{
    /**
     * Current SOAP request manager;
     */
    private BusSoapRequestManager bsrmManager;
    /**
     * A Connector holds an XML connection to Cordys ECX. Used to send and
     * receive SOAP messages.
     */
    private Connector connector = null;
    /**
     * The LDAP Directory to which connection has to be made.
     */
    private LDAPDirectory ldapDirectory = null;
    /**
     * The distinguished name(dn) of the receiver soap node to which the
     * request has to be sent.
     */
    private String receiver = "";
    /**
     * The user's distinguished name(dn) which is used to send the request.
     */
    private String userDN = "";
    /**
     * The soap request node which has to be created and sent.
     */
    private int soapRequestNode = 0;

    /**
     * Parameterized Constructor Initialize the ISoapRequest
     *
     * @param ldapDirectory The instance of LDAPDirectory class to which
     *        connection has to be made.
     * @param bsrmManager The instance of the Connector class.
     */
    public BusSoapRequest(LDAPDirectory ldapDirectory,
                          BusSoapRequestManager bsrmManager)
    {
        this.ldapDirectory = ldapDirectory;
        this.connector = bsrmManager.getConnector();
        this.bsrmManager = bsrmManager;
    }

    /**
     * Default Constructor. Should not be used hence private.
     */
    @SuppressWarnings("unused")
    private BusSoapRequest()
    {
    }

    /**
     * Sets the user dn. The user dn will be appended to the soap request when
     * execute method is called.
     *
     * @param userDN The user's distinguished name(DN) which will be used to
     *        send the request.
     */
    public void setUser(String userDN)
    {
        this.userDN = userDN;
    }

    /**
     * Adds methods to the request
     *
     * @param namespace The namespace of the method
     * @param method The name of the method to be added
     * @param paramNames The array of names of parameters
     * @param paramValues The array of values of parameters
     *
     * @return The newly created method node reference
     *
     * @throws SoapRequestException
     */
    public int addMethod(String namespace, String method, String[] paramNames,
                         String[] paramValues)
                  throws SoapRequestException
    {
        return addMethod(null, namespace, method, paramNames, paramValues);
    }

    /**
     * Adds methods to the request
     *
     * @param organization The dn of the organization
     * @param namespace The namespace of the method
     * @param method The name of the method to be added
     * @param paramNames The array of names of parameters
     * @param paramValues The array of values of parameters
     *
     * @return The newly created method node reference
     *
     * @throws SoapRequestException
     */
    public int addMethod(String organization, String namespace, String method,
                         String[] paramNames, String[] paramValues)
                  throws SoapRequestException
    {
        int methodNode = 0;

        String resolvedReceiver = "";

        // resolve the receiver.
        resolvedReceiver = getReceiver(organization, namespace, method);

        if (!receiver.equals("") && !receiver.equals(resolvedReceiver))
        {
            throw new SoapRequestException("The request cannot be sent because the receivers are different.");
        }

        receiver = resolvedReceiver;

        if (soapRequestNode == 0)
        {
            // Create a new SOAP Envelope node, with a header and a body in it.
            soapRequestNode = connector.createSOAPMessage(receiver, null);

            if (bsrmManager.getCurrentNomCollector() != null)
            {
                bsrmManager.getCurrentNomCollector().addNode(soapRequestNode);
            }
        }

        // creates the method node.
        methodNode = createMethod(organization, namespace, method);

        if (methodNode != 0)
        {
            for (int j = 0; j < paramNames.length; j++)
            {
                // check if null value is being passed because it can make jvm
                // crash.
                if (paramNames[j] == null)
                {
                    paramValues[j] = "";
                }

                if (paramValues[j] == null)
                {
                    paramValues[j] = "";
                }

                // Append the param nodes to the method node.
                Node.setDataElement(methodNode, paramNames[j], paramValues[j]);
            }

            // append the method created to the temporary methodRootNode
            methodNode = Node.appendToChildren(methodNode, soapRequestNode);
        }

        return methodNode;
    }

    /**
     * Adds methods to the request
     *
     * @param namespace The namespace of the method
     * @param method The name of the method to be added
     * @param paramNode The parameter node to be appended to method node root
     *
     * @return The newly created method node reference
     *
     * @throws SoapRequestException
     */
    public int addMethod(String namespace, String method, int paramNode)
                  throws SoapRequestException
    {
        return addMethod(null, namespace, method, paramNode);
    }

    /**
     * Adds methods to the request
     *
     * @param namespace The namespace of the method
     * @param method The name of the method to be added
     *
     * @return The newly created method node reference
     *
     * @throws SoapRequestException
     */
    public int addMethod(String namespace, String method)
                  throws SoapRequestException
    {
        return addMethod(null, namespace, method, 0);
    }

    /**
     * Adds methods to the request
     *
     * @param organization The dn of the organization
     * @param namespace The namespace of the method
     * @param method The name of the method to be added
     * @param paramNode The parameter node to be appended to method node root
     *
     * @return The newly created method node reference
     *
     * @throws SoapRequestException
     */
    public int addMethod(String organization, String namespace, String method,
                         int paramNode)
                  throws SoapRequestException
    {
        int methodNode = 0;
        String resolvedReceiver = "";

        // resolve the receiver.
        resolvedReceiver = getReceiver(organization, namespace, method);

        if (!receiver.equals("") && !receiver.equals(resolvedReceiver))
        {
            throw new SoapRequestException("The request cannot be sent because the receivers are different.");
        }

        receiver = resolvedReceiver;

        if (soapRequestNode == 0)
        {
            // Create a new SOAP Envelope node, with a header and a body in it.
            soapRequestNode = connector.createSOAPMessage(receiver, null);

            if (bsrmManager.getCurrentNomCollector() != null)
            {
                bsrmManager.getCurrentNomCollector().addNode(soapRequestNode);
            }
        }

        // creates the method node.
        methodNode = createMethod(organization, namespace, method);

        if (methodNode != 0)
        {
            if (paramNode != 0)
            {
                Node.appendToChildren(paramNode, methodNode);
            }

            // append the method created to the temporary methodRootNode
            methodNode = Node.appendToChildren(methodNode, soapRequestNode);
        }

        return methodNode;
    }

    /**
     * Adds methods to the request
     *
     * @param namespace The namespace of the method
     * @param method The name of the method to be added
     * @param paramNodes The parameter nodes which are to be appended to the
     *        method root node
     *
     * @return The request node with newly added methods
     *
     * @throws SoapRequestException
     */
    public int addMethod(String namespace, String method, int[] paramNodes)
                  throws SoapRequestException
    {
        return addMethod(null, namespace, method, paramNodes);
    }

    /**
     * Adds methods to the request This method does not resolve the dn of the
     * soap node to which request is send The receiver dn is specified as a
     * parameter
     *
     * @param aReceiverDn dn of the reciever.
     * @param organization The dn of the organization.
     * @param namespace The namespace of the method.
     * @param method The name of the method to be added.
     * @param paramNodes The parameter nodes which are to be appended to the
     *        method root node.
     *
     * @return The request node with newly added methods.
     */
    public int addMethod(String aReceiverDn, String organization,
                         String namespace, String method, int[] paramNodes)
                  throws SoapRequestException
    {
        int methodNode = 0;

        // dont resolve the reciever dn
        receiver = aReceiverDn;

        if (soapRequestNode == 0)
        {
            // Create a new SOAP Envelope node, with a header and a body in it.
            soapRequestNode = connector.createSOAPMessage(receiver, null);

            if (bsrmManager.getCurrentNomCollector() != null)
            {
                bsrmManager.getCurrentNomCollector().addNode(soapRequestNode);
            }
        }

        // Creates the method node.
        methodNode = createMethod(organization, namespace, method);

        if (methodNode != 0)
        {
            if (paramNodes.length > 0)
            {
                for (int k = 0; k < paramNodes.length; k++)
                {
                    Node.appendToChildren(paramNodes[k], methodNode);
                }
            }

            // append the method created to the temporary methodRootNode
            methodNode = Node.appendToChildren(methodNode, soapRequestNode);
        }

        return methodNode;
    }

    /**
     * Adds methods to the request
     *
     * @param organization The dn of the organization
     * @param namespace The namespace of the method
     * @param method The name of the method to be added
     * @param paramNodes The parameter nodes which are to be appended to the
     *        method root node
     *
     * @return The request node with newly added methods
     *
     * @throws SoapRequestException
     */
    public int addMethod(String organization, String namespace, String method,
                         int[] paramNodes)
                  throws SoapRequestException
    {
        int methodNode = 0;
        String resolvedReceiver = "";

        // resolve the receiver.
        resolvedReceiver = getReceiver(organization, namespace, method);

        if (!receiver.equals("") && !receiver.equals(resolvedReceiver))
        {
            throw new SoapRequestException("The request cannot be sent because the receivers are different.");
        }

        receiver = resolvedReceiver;

        if (soapRequestNode == 0)
        {
            // Create a new SOAP Envelope node, with a header and a body in it.
            soapRequestNode = connector.createSOAPMessage(receiver, null);

            if (bsrmManager.getCurrentNomCollector() != null)
            {
                bsrmManager.getCurrentNomCollector().addNode(soapRequestNode);
            }
        }

        // Creates the method node.
        methodNode = createMethod(organization, namespace, method);

        if (methodNode != 0)
        {
            if (paramNodes.length > 0)
            {
                for (int k = 0; k < paramNodes.length; k++)
                {
                    Node.appendToChildren(paramNodes[k], methodNode);
                }
            }

            // append the method created to the temporary methodRootNode
            methodNode = Node.appendToChildren(methodNode, soapRequestNode);
        }

        return methodNode;
    }

    /**
     * Adds methods to the request
     *
     * @param organization The dn of the organization
     * @param node Request Node
     *
     * @return The request node with newly added methods
     *
     * @throws SoapRequestException
     */
    public int addMethod(String organization, int node)
                  throws SoapRequestException
    {
        int methodNode = 0;
        String resolvedReceiver = "";
        String namespace = Node.getAttribute(node, "xmlns");
        String method = Node.getName(node);

        // resolve the receiver.
        resolvedReceiver = getReceiver(organization, namespace, method);

        if (!receiver.equals("") && !receiver.equals(resolvedReceiver))
        {
            throw new SoapRequestException("The request cannot be sent because the receivers are different.");
        }

        receiver = resolvedReceiver;

        if (soapRequestNode == 0)
        {
            // Create a new SOAP Envelope node, with a header and a body in it.
            soapRequestNode = connector.createSOAPMessage(receiver, null);

            if (bsrmManager.getCurrentNomCollector() != null)
            {
                bsrmManager.getCurrentNomCollector().addNode(soapRequestNode);
            }

            // Creates the method node.
            methodNode = createMethod(organization, namespace, method);
            addMethodNodeAttributes(node, methodNode);

            if (methodNode != 0)
            {
                int[] nodes = NodeUtil.getChildrenNodes(node);

                if (nodes.length > 0)
                {
                    for (int k = 0; k < nodes.length; k++)
                    {
                        Node.appendToChildren(nodes[k], methodNode);
                    }
                }

                // append the method created to the temporary methodRootNode
                methodNode = Node.appendToChildren(methodNode, soapRequestNode);

                return methodNode;
            }

            return 0;
        }
        else
        {
            node = Node.appendToChildren(node, soapRequestNode);

            return node;
        }
    }

    /**
     * Executes the soap request and returns the soap response
     *
     * @return The response of request
     *
     * @throws SoapRequestException
     */
    public int execute()
                throws SoapRequestException
    {
        return execute(30000);
    }

    /**
     * Executes the soap request and returns the soap response
     *
     * @param timeout The timeout to overide the default timeout.
     *
     * @return The response of request
     *
     * @throws SoapRequestException
     */
    public int execute(long timeout)
                throws SoapRequestException
    {
        int soapResponseNode = 0;

        try
        {
            if ((receiver != null) && (receiver.length() > 0))
            {
                // if username has been set then change the username.
                setRequestUser(soapRequestNode);
                BusSoapRequestManager.log("Sending the request ...\n" +
                                          Node.writeToString(soapRequestNode,
                                                             true),
                                          ISoapRequestManager.MSG_DEBUG);

                if (timeout == 30000)
                {
                    soapResponseNode = connector.sendAndWait(soapRequestNode);
                }
                else
                {
                    soapResponseNode = connector.sendAndWait(soapRequestNode,
                                                             timeout);
                }

                if (bsrmManager.isNomCollectionEnabled())
                {
                    bsrmManager.getCurrentNomCollector().addNode(soapResponseNode);
                }

                BusSoapRequestManager.log("Returning the response ...\n" +
                                          Node.writeToString(soapResponseNode,
                                                             true),
                                          ISoapRequestManager.MSG_DEBUG);
            }
        }
        catch (TimeoutException te)
        {
            throw new SoapRequestException(te.getMessage(), te);
        }
        catch (ExceptionGroup eg)
        {
            throw new SoapRequestException(eg.getMessage(), eg);
        }

        return soapResponseNode;
    }
    
    /**
     * @see com.cordys.tools.ant.soap.ISoapRequest#setSoapRequest(int)
     */
    public void setSoapRequest(int envelope)
    {
        if (soapRequestNode != 0) {
            throw new IllegalStateException("SOAP request node is already set.");
        }
        
        soapRequestNode = envelope;
    }

    // *************************** Getter Methods ******************************
    /**
     * Resolves the receiver when the method parameters are specified.
     *
     * @param organization The organization in which the soap node has to be
     *        resolved
     * @param namespace The namespace of the method.
     * @param method The name of the method.
     *
     * @return The dn of the receiver soap node to which the request has to be
     *         sent.
     *
     * @throws SoapRequestException
     */
    private String getReceiver(String organization, String namespace,
                               String method)
                        throws SoapRequestException
    {
        String receiver;

        try
        {
            if ((organization == null) || (organization.length() == 0))
            {
                organization = bsrmManager.getOrganizationDN();
            }

            if ((organization == null) || (organization.length() == 0))
            {
                // Resolve the Receiver to whom to send.
                receiver = ldapDirectory.findSOAPNode(namespace, method);
            }
            else
            {
                // Resolve the Receiver to whom to send.
                receiver = ldapDirectory.findSOAPNode(organization, namespace,
                                                      method);
            }
        }
        catch (DirectoryException de)
        {
            throw new SoapRequestException(de.getMessage(), de);
        }

        BusSoapRequestManager.log("Resolved receiver as:" + receiver,
                                  ISoapRequestManager.MSG_DEBUG);

        return receiver;
    }

    // *************************** Setter Methods ******************************
    /**
     * Sets the user dn if it has been specified.
     *
     * @param soapMessageNode The soap request message node
     */
    private void setRequestUser(int soapMessageNode)
    {
        // if username has been set then change the username.
        if ((userDN != null) && (userDN.length() > 0))
        {
            int headerNode = SOAPMessage.getHeaderNode(soapMessageNode);
            int senderNode = Find.firstMatch(headerNode,
                                             "<SOAP:Header><header><sender>");
            Node.setDataElement(senderNode, "user", userDN);

            BusSoapRequestManager.log("Set the request user as:" + userDN,
                                      ISoapRequestManager.MSG_DEBUG);
        }
    }

    // **************************** Helper Methods
    // ******************************
    /**
     * Used internally for creating the soap method by various overloaded
     * methods.
     *
     * @param organization The dn of the organization.
     * @param namespace The namespace of the method.
     * @param method The name of the method.
     *
     * @return The method node created.
     *
     * @throws SoapRequestException
     */
    private int createMethod(String organization, String namespace,
                             String method)
                      throws SoapRequestException
    {
        int methodNode;

        try
        {
            if ((organization == null) || (organization.length() == 0))
            {
                methodNode = connector.createSOAPMethod(namespace, method);
            }
            else
            {
                methodNode = connector.createSOAPMethod(organization,
                                                        namespace, method);
            }
        }
        catch (DirectoryException de)
        {
            // handle exception
            throw new SoapRequestException(de.getMessage(), de);
        }

        return methodNode;
    }
    
    private void addMethodNodeAttributes(int iOrigNode, int iDestMethodNode) {
        int iCount = Node.getNumAttributes(iOrigNode);
        
        for (int i = 1; i <= iCount; i++) {
            String sAttribName = Node.getAttributeName(iOrigNode, i);
            String sLocalName = Node.getAttributeLocalName(iOrigNode, i);

            if (sAttribName == null || sLocalName == null) {
                continue;
            }
            
            if (sLocalName.equals("xmlns")) {
                continue;
            }
            
            Node.setAttribute(iDestMethodNode, sAttribName, Node.getAttribute(iOrigNode, sAttribName));
        }
    }
    

    /**
     * Returns the receiver.
     *
     * @return Returns the receiver.
     */
    public String getReceiver()
    {
        return receiver;
    }

    /**
     * Sets the receiver.
     *
     * @param receiver The receiver to be set.
     */
    public void setReceiver(String receiver)
    {
        this.receiver = receiver;
    }    
}
