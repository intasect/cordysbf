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

import com.cordys.coe.util.log.AntTaskLogger;
import com.cordys.coe.util.soap.SoapHelpers;

import com.cordys.tools.ant.cm.BcpVersionInfo;
import com.cordys.tools.ant.cm.EBcpVersion;
import com.cordys.tools.ant.util.NodeUtil;

import com.eibus.connector.nom.SOAPMessage;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * The class represents a Soap Request Object. The Soap Request object can help abstract the
 * complexity involved in sending soap requests. The Soap Request can contain multiple method
 * request also. Usage Example:
 *
 * <pre>
   SoapRequestManager soapRequestMgr = new SoapRequestManager();
   <br>
   ISoapRequest soapRequest = soapRequestMgr.createSoapRequest();
   <br>
   soapRequest.addMethod(namespace, method, paramNames, paramValues);
   <br>
   soapRequest.addMethod(organization, namespace, method, paramNode);
   <br>
   soapRequest.execute();
   <br>
 * </pre>
 *
 * @author  msreejit/mpoyhone
 */
public class GatewaySoapRequest
    implements ISoapRequest
{
    /**
     * Web gateway connection.
     */
    private GatewaySoapConnection gscConnection;
    /**
     * SOAP request manager for this request.
     */
    private GatewaySoapRequestManager gsrmManager;
    /**
     * The distinguished name(dn) of the receiver soap node to which the request has to be sent.
     */
    private String receiver = "";
    /**
     * The soap request node which has to be created and sent.
     */
    private int soapRequestNode = 0;
    /**
     * The distinguished name(dn) of the organization to which the request has to be sent.
     */
    private String sUserOrganization = "";

    /**
     * Parameterized Constructor Initialize the ISoapRequest.
     *
     * @param  gscConnection  Web gateway connection.
     * @param  gsrmManager    request manager for this request.
     */
    public GatewaySoapRequest(GatewaySoapConnection gscConnection,
                              GatewaySoapRequestManager gsrmManager)
    {
        this.gscConnection = gscConnection;
        this.gsrmManager = gsrmManager;
    }

    /**
     * Converts a SOAP request that contains multiple methods to C3 format. If the request contains
     * only one method, nothing is done.
     *
     * @param  envelope  SOAP envelope.
     */
    @SuppressWarnings("deprecation")
    public static void convertMulticallMethodRequest(int envelope)
    {
        int bodyNode = SOAPMessage.getBodyNode(envelope);

        if (bodyNode == 0)
        {
            return;
        }

        int[] methods = Find.match(SOAPMessage.getBodyNode(envelope), "<><>");

        if (methods.length < 2)
        {
            return;
        }

        int multimethodRoot = Node.createElement("multicall:__Cordys_MultipleRequestWrapper",
                                                 bodyNode);

        Node.setAttribute(multimethodRoot, "xmlns:multicall",
                          Node.getAttribute(methods[0], "xmlns"));

        for (int methodNode : methods)
        {
            int tmp;

            tmp = Node.createElement("SOAP:Envelope", multimethodRoot);
            Node.setAttribute(tmp, "xmlns:SOAP", "http://schemas.xmlsoap.org/soap/envelope/");
            tmp = Node.createElement("SOAP:Body", tmp);

            Node.appendToChildren(methodNode, tmp);
        }
    }

    /**
     * Converts a SOAP response that contains multiple response elements from C3 format.
     *
     * @param  envelope  SOAP envelope.
     */
    @SuppressWarnings("deprecation")
    public static void convertMulticallMethodResponse(int envelope)
    {
        int bodyNode = SOAPMessage.getBodyNode(envelope);

        if (bodyNode == 0)
        {
            return;
        }

        int multicallResponseNode = Find.firstMatch(bodyNode,
                                                    "<><multicall:__Cordys_MultipleRequestWrapperResponse>");

        if (multicallResponseNode == 0)
        {
            return;
        }

        int[] methodEnvelopes = Find.match(multicallResponseNode, "<><>");

        for (int envNode : methodEnvelopes)
        {
            String soapPrefix = SoapHelpers.getSoapNamespacePrefix(envNode);
            int methodNode = Find.firstMatch(envNode, String.format("<><%sBody><>", soapPrefix));

            if (methodNode != 0)
            {
                Node.appendToChildren(methodNode, bodyNode);
            }
        }

        Node.delete(multicallResponseNode);
        multicallResponseNode = 0;
    }

    /**
     * Adds methods to the request.
     *
     * @param   namespace  The namespace of the method
     * @param   method     The name of the method to be added
     *
     * @return  The newly created method node reference
     *
     * @throws  SoapRequestException
     */
    public int addMethod(String namespace, String method)
                  throws SoapRequestException
    {
        return addMethod(null, namespace, method, 0);
    }

    /**
     * Adds methods to the request.
     *
     * @param   organization  The dn of the organization
     * @param   node          Request Node
     *
     * @return  The request node with newly added methods
     *
     * @throws  SoapRequestException
     */
    public int addMethod(String organization, int node)
                  throws SoapRequestException
    {
        int methodNode = 0;
        String namespace = Node.getAttribute(node, "xmlns");
        String method = Node.getName(node);

        if (((organization != null) && (organization.length() > 0)) &&
                ((sUserOrganization == null) || (sUserOrganization.length() == 0)))
        {
            sUserOrganization = organization;
        }

        if (soapRequestNode == 0)
        {
            // Create a new SOAP Envelope node, with a header and a body in it.
            soapRequestNode = createSOAPMessage(receiver);

            // Creates the method node.
            methodNode = createMethod(namespace, method);
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
     * Adds methods to the request.
     *
     * @param   namespace  The namespace of the method
     * @param   method     The name of the method to be added
     * @param   paramNode  The parameter node to be appended to method node root
     *
     * @return  The newly created method node reference
     *
     * @throws  SoapRequestException
     */
    public int addMethod(String namespace, String method, int paramNode)
                  throws SoapRequestException
    {
        return addMethod(null, namespace, method, paramNode);
    }

    /**
     * Adds methods to the request.
     *
     * @param   namespace   The namespace of the method
     * @param   method      The name of the method to be added
     * @param   paramNodes  The parameter nodes which are to be appended to the method root node
     *
     * @return  The request node with newly added methods
     *
     * @throws  SoapRequestException
     */
    public int addMethod(String namespace, String method, int[] paramNodes)
                  throws SoapRequestException
    {
        return addMethod(null, namespace, method, paramNodes);
    }

    /**
     * Adds methods to the request.
     *
     * @param   namespace    The namespace of the method
     * @param   method       The name of the method to be added
     * @param   paramNames   The array of names of parameters
     * @param   paramValues  The array of values of parameters
     *
     * @return  The newly created method node reference
     *
     * @throws  SoapRequestException
     */
    public int addMethod(String namespace, String method, String[] paramNames, String[] paramValues)
                  throws SoapRequestException
    {
        return addMethod(null, namespace, method, paramNames, paramValues);
    }

    /**
     * Adds methods to the request.
     *
     * @param   organization  The dn of the organization
     * @param   namespace     The namespace of the method
     * @param   method        The name of the method to be added
     * @param   paramNode     The parameter node to be appended to method node root
     *
     * @return  The newly created method node reference
     *
     * @throws  SoapRequestException
     */
    public int addMethod(String organization, String namespace, String method, int paramNode)
                  throws SoapRequestException
    {
        int methodNode = 0;

        if (((organization != null) && (organization.length() > 0)) &&
                ((sUserOrganization == null) || (sUserOrganization.length() == 0)))
        {
            sUserOrganization = organization;
        }

        if (soapRequestNode == 0)
        {
            // Create a new SOAP Envelope node, with a header and a body in it.
            soapRequestNode = createSOAPMessage(receiver);
        }

        // creates the method node.
        methodNode = createMethod(namespace, method);

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
     * Adds methods to the request.
     *
     * @param   organization  The dn of the organization
     * @param   namespace     The namespace of the method
     * @param   method        The name of the method to be added
     * @param   paramNodes    The parameter nodes which are to be appended to the method root node
     *
     * @return  The request node with newly added methods
     *
     * @throws  SoapRequestException
     */
    public int addMethod(String organization, String namespace, String method, int[] paramNodes)
                  throws SoapRequestException
    {
        int methodNode = 0;

        if (((organization != null) && (organization.length() > 0)) &&
                ((sUserOrganization == null) || (sUserOrganization.length() == 0)))
        {
            sUserOrganization = organization;
        }

        if (soapRequestNode == 0)
        {
            // Create a new SOAP Envelope node, with a header and a body in it.
            soapRequestNode = createSOAPMessage(receiver);
        }

        // Creates the method node.
        methodNode = createMethod(namespace, method);

        if (methodNode != 0)
        {
            if (paramNodes.length > 0)
            {
                for (int k = 0; k < paramNodes.length; k++)
                {
                    if (paramNodes[k] != 0)
                    {
                        Node.appendToChildren(paramNodes[k], methodNode);
                    }
                }
            }

            // append the method created to the temporary methodRootNode
            methodNode = Node.appendToChildren(methodNode, soapRequestNode);
        }

        return methodNode;
    }

    /**
     * Adds methods to the request.
     *
     * @param   organization  The dn of the organization
     * @param   namespace     The namespace of the method
     * @param   method        The name of the method to be added
     * @param   paramNames    The array of names of parameters
     * @param   paramValues   The array of values of parameters
     *
     * @return  The newly created method node reference
     *
     * @throws  SoapRequestException
     */
    public int addMethod(String organization, String namespace, String method, String[] paramNames,
                         String[] paramValues)
                  throws SoapRequestException
    {
        int methodNode = 0;

        if (((organization != null) && (organization.length() > 0)) &&
                ((sUserOrganization == null) || (sUserOrganization.length() == 0)))
        {
            sUserOrganization = organization;
        }

        if (soapRequestNode == 0)
        {
            // Create a new SOAP Envelope node, with a header and a body in it.
            soapRequestNode = createSOAPMessage(receiver);
        }

        // creates the method node.
        methodNode = createMethod(namespace, method);

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
     * Adds methods to the request This method does not resolve the dn of the soap node to which
     * request is send The receiver dn is specified as a parameter.
     *
     * @param   aReceiverDn   dn of the reciever.
     * @param   organization  The dn of the organization.
     * @param   namespace     The namespace of the method.
     * @param   method        The name of the method to be added.
     * @param   paramNodes    The parameter nodes which are to be appended to the method root node.
     *
     * @return  The request node with newly added methods.
     *
     * @throws  SoapRequestException  DOCUMENTME
     */
    public int addMethod(String aReceiverDn, String organization, String namespace, String method,
                         int[] paramNodes)
                  throws SoapRequestException
    {
        int methodNode = 0;

        if (soapRequestNode == 0)
        {
            // Create a new SOAP Envelope node, with a header and a body in it.
            soapRequestNode = createSOAPMessage(receiver);
        }

        if (((organization != null) && (organization.length() > 0)) &&
                ((sUserOrganization == null) || (sUserOrganization.length() == 0)))
        {
            sUserOrganization = organization;
        }

        // Creates the method node.
        methodNode = createMethod(namespace, method);

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
     * Executes the soap request and returns the soap response.
     *
     * @return  The response of request
     *
     * @throws  SoapRequestException
     */
    public int execute()
                throws SoapRequestException
    {
        return execute(gsrmManager.getTimeout());
    }

    /**
     * Executes the soap request and returns the soap response.
     *
     * @param   timeout  The timeout to overide the default timeout.
     *
     * @return  The response of request
     *
     * @throws  SoapRequestException
     */
    public int execute(long timeout)
                throws SoapRequestException
    {
        int soapResponseNode = 0;
        String soapnodeDn = ((receiver != null) && (receiver.length() > 0)) ? receiver : null;
        int reqEnvelopeNode = Node.getRoot(soapRequestNode);
        BcpVersionInfo bcpVersion = gsrmManager.getBcpVersion();
        boolean needMulticallWrapper = (bcpVersion != null)
                                       ? bcpVersion.isLaterThan(EBcpVersion.BCP42_C3) : false;

        GatewaySoapRequestManager.log("Request to be send:\n" +
                                      Node.writeToString(reqEnvelopeNode, true),
                                      ISoapRequestManager.MSG_DEBUG);

        if (needMulticallWrapper)
        {
            convertMulticallMethodRequest(reqEnvelopeNode);
        }

        soapResponseNode = gscConnection.execute(reqEnvelopeNode, sUserOrganization, soapnodeDn,
                                                 timeout,
                                                 new AntTaskLogger(gsrmManager.getAntTask()));

        if (gsrmManager.isNomCollectionEnabled())
        {
            gsrmManager.getCurrentNomCollector().addNode(soapResponseNode);
        }

        if (needMulticallWrapper)
        {
            convertMulticallMethodResponse(soapResponseNode);
        }

        GatewaySoapRequestManager.log("Returning the response ...\n" +
                                      Node.writeToString(soapResponseNode, true),
                                      ISoapRequestManager.MSG_DEBUG);

        return soapResponseNode;
    }

    /**
     * Returns the receiver.
     *
     * @return  Returns the receiver.
     */
    public String getReceiver()
    {
        return receiver;
    }

    /**
     * Sets the receiver.
     *
     * @param  receiver  The receiver to be set.
     */
    public void setReceiver(String receiver)
    {
        this.receiver = receiver;
    }

    /**
     * @see  com.cordys.tools.ant.soap.ISoapRequest#setSoapRequest(int)
     */
    public void setSoapRequest(int envelope)
    {
        if (soapRequestNode != 0)
        {
            throw new IllegalStateException("SOAP request node is already set.");
        }

        soapRequestNode = envelope;
    }

    /**
     * Sets the user dn. This method does nothing for web gateway as the user DN is determined by
     * the authenticated user.
     *
     * @param  userDN  The user's distinguished name(DN) which will be used to send the request.
     */
    public void setUser(String userDN)
    {
    }

    /**
     * DOCUMENTME.
     *
     * @param  iOrigNode        DOCUMENTME
     * @param  iDestMethodNode  DOCUMENTME
     */
    private void addMethodNodeAttributes(int iOrigNode, int iDestMethodNode)
    {
        int iCount = Node.getNumAttributes(iOrigNode);

        for (int i = 1; i <= iCount; i++)
        {
            String sAttribName = Node.getAttributeName(iOrigNode, i);
            String sLocalName = Node.getAttributeLocalName(iOrigNode, i);

            if ((sAttribName == null) || (sLocalName == null))
            {
                continue;
            }

            if (sLocalName.equals("xmlns"))
            {
                continue;
            }

            Node.setAttribute(iDestMethodNode, sAttribName,
                              Node.getAttribute(iOrigNode, sAttribName));
        }
    }

    // *************************** Getter Methods ******************************
    /**
     * Used internally for creating the soap method by various overloaded methods.
     *
     * @param   namespace  The namespace of the method.
     * @param   method     The name of the method.
     *
     * @return  The method node created.
     *
     * @throws  SoapRequestException
     */
    private int createMethod(String namespace, String method)
                      throws SoapRequestException
    {
        int methodNode;

        methodNode = gsrmManager.getDocument().createElement(method);
        Node.setAttribute(methodNode, "xmlns", namespace);

        return methodNode;
    }

    /**
     * Creates a new SOAP message.
     *
     * @param   sReceiver  Receiving SOAP node DN. Not used for web gateway SOAP XML.
     *
     * @return  Create SOAP message.
     */
    private int createSOAPMessage(String sReceiver)
    {
        int iEnvelopeNode = gsrmManager.getDocument().createElement("SOAP:Envelope");

        if (gsrmManager.getCurrentNomCollector() != null)
        {
            gsrmManager.getCurrentNomCollector().addNode(iEnvelopeNode);
        }

        Node.setAttribute(iEnvelopeNode, "xmlns:SOAP", "http://schemas.xmlsoap.org/soap/envelope/");

        int iBodyNode = Node.createElement("SOAP:Body", iEnvelopeNode);

        return iBodyNode;
    }
}
