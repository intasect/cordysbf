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

import com.cordys.coe.util.log.LogInterface;
import com.cordys.coe.util.soap.SOAPException;
import com.cordys.coe.util.soap.SoapHelpers;
import com.cordys.coe.util.xml.NamespaceDefinitions;
import com.cordys.coe.util.xml.nom.NamespaceConstants;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.UUID;

/**
 * Wrapper around the Cordys web gateway connection.
 *
 * @author  mpoyhone
 */
public class GatewaySoapConnection
{
    /**
     * This holds the request to get a SAML token.
     */
    protected static final byte[] XML_SSO_LOGIN = ("<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                                                   "<SOAP:Header><wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
                                                   "<wsse:UsernameToken xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
                                                   "<wsse:Username></wsse:Username>" +
                                                   "<wsse:Password></wsse:Password>" +
                                                   "</wsse:UsernameToken>" +
                                                   "</wsse:Security></SOAP:Header>" +
                                                   "<SOAP:Body>" +
                                                   "<samlp:Request xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" MajorVersion=\"1\" MinorVersion=\"1\" IssueInstant=\"\" RequestID=\"\"><samlp:AuthenticationQuery>" +
                                                   "<saml:Subject xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\">" +
                                                   "<saml:NameIdentifier Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\"></saml:NameIdentifier>" +
                                                   "</saml:Subject></samlp:AuthenticationQuery></samlp:Request>" +
                                                   "</SOAP:Body></SOAP:Envelope>").getBytes();
    /**
     * Holds the namespace prefix for the SOAP namespace.
     */
    private static final String PRE_SOAP = NamespaceDefinitions.PREFIX_SOAP_1_1;
    /**
     * Holds the namespace prefix for the WS-Security namespace.
     */
    private static final String PRE_WSSE = NamespaceConstants.registerPrefix("wsse",
                                                                             "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
    /**
     * Holds the namespace prefix for the SAML protocol namespace.
     */
    private static final String PRE_SAMLP = NamespaceConstants.registerPrefix("samlp",
                                                                              "urn:oasis:names:tc:SAML:1.0:protocol");
    /**
     * Holds the namespace prefix for the SAML protocol namespace.
     */
    private static final String PRE_SAML = NamespaceConstants.registerPrefix("saml",
                                                                             "urn:oasis:names:tc:SAML:1.0:assertion");
    /**
     * Holds the namespace prefix for the XML Signature namespace.
     */
    private static final String PRE_XMLSIG = NamespaceConstants.registerPrefix("xmlsig",
                                                                               "http://www.w3.org/2000/09/xmldsig#");

    /**
     * Contains the web gateway connection configuration.
     */
    private HttpConnectionConfig connectionConfig;
    /**
     * Holds the current SAML token.
     */
    private int samlToken = 0;

    /**
     * DN of the default organization.
     */
    private String sDefaultOrganization;

    /**
     * Constructor.
     *
     * @param  connectionConfig  uBaseUrl Gateway base URL.
     */
    public GatewaySoapConnection(HttpConnectionConfig connectionConfig)
    {
        this.connectionConfig = connectionConfig;
    }

    /**
     * Sends the SOAP request to the web gateway and returns the response.
     *
     * @param   iRequest       SOAP request envelope NOM element.
     * @param   sOrganization  DOCUMENTME
     * @param   sReceiver      DOCUMENTME
     * @param   lTimeout       Request timeout in milliseconds.
     * @param   liLogger       Logger object or <code>null</code> if no loggin is needed.
     *
     * @return  SOAP response envelope NOM element.
     *
     * @throws  SoapRequestException  DOCUMENTME
     */
    public int execute(int iRequest, String sOrganization, String sReceiver, long lTimeout,
                       LogInterface liLogger)
                throws SoapRequestException
    {
        int iResponseNode;
        StringBuffer sbParams = new StringBuffer(256);
        URL uBaseUrl = connectionConfig.getGatewayUrl();
        URL uRequestUrl;

        if ((sOrganization == null) || (sOrganization.length() == 0))
        {
            sOrganization = sDefaultOrganization;
        }

        if (lTimeout == 0)
        {
            // The timeout of zero (disabled) doesn't seem to work, so we just
            // use a really big value.
            lTimeout = 24L * 3600L * 1000L;
        }

        try
        {
            sbParams.append(uBaseUrl.getPath());
            sbParams.append("?");

            if ((sOrganization != null) && (sOrganization.length() > 0))
            {
                sbParams.append("organization=").append(URLEncoder.encode(sOrganization, "UTF-8"));
            }

            if (sReceiver != null)
            {
                if (sbParams.length() > 1)
                {
                    sbParams.append("&");
                }

                sbParams.append("receiver=").append(URLEncoder.encode(sReceiver, "UTF-8"));
            }

            if (sbParams.length() > 1)
            {
                sbParams.append("&");
            }

            sbParams.append("timeout=").append(lTimeout);

            uRequestUrl = new URL(uBaseUrl, sbParams.toString());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new SoapRequestException("Unable to build the URL", e);
        }
        catch (MalformedURLException e)
        {
            throw new SoapRequestException("Invalid base URL " + uBaseUrl, e);
        }

        if (liLogger != null)
        {
            liLogger.debug("Sending SOAP request. User='" + connectionConfig.getUserName() +
                           "', URL: " + uRequestUrl);
        }

        // Handle SSO servers
        if (connectionConfig.getSSO() == true)
        {
            try
            {
                fixSSOHeader(uRequestUrl, iRequest, liLogger);
            }
            catch (Exception e)
            {
                throw new SoapRequestException("Cannot handle SSO login", e);
            }
        }

        try
        {
            connectionConfig.createAuthenticator();
            iResponseNode = SoapHelpers.sendExternalSOAPRequest(uRequestUrl, iRequest, null, null,
                                                                "");
        }
        catch (SOAPException e)
        {
            throw new SoapRequestException("Gateway SOAP request failed.", e);
        }

        return iResponseNode;
    }

    /**
     * Returns the DN of the default organization.
     *
     * @return  DN of the default organization.
     */
    public String getDefaultOrganization()
    {
        return sDefaultOrganization;
    }

    /**
     * Sets the DN of the default organization.
     *
     * @param  sDefaultOrganization  DN of the default organization.
     */
    public void setDefaultOrganization(String sDefaultOrganization)
    {
        this.sDefaultOrganization = sDefaultOrganization;
    }

    /**
     * This method will attempt to log in to Cordys using SSO.
     *
     * @param   uRequestUrl  The gateway URL.
     * @param   request      The actual request to fix.
     * @param   liLogger     The logger to use.
     *
     * @throws  Exception  In case of any exceptions.
     */
    private void fixSSOHeader(URL uRequestUrl, int request, LogInterface liLogger)
                       throws Exception
    {
        XPathMetaInfo xmi = NamespaceConstants.getXPathMetaInfo();

        if (samlToken == 0)
        {
            // Retrieve it
            Document dDoc = Node.getDocument(request);

            int iLoginRequest = dDoc.load(XML_SSO_LOGIN);

            int iUsername = XPathHelper.selectSingleNode(iLoginRequest,
                                                         "//" + PRE_WSSE + ":UsernameToken/" +
                                                         PRE_WSSE + ":Username", xmi);
            dDoc.createText(connectionConfig.getUserName(), iUsername);

            int iPassword = XPathHelper.selectSingleNode(iLoginRequest,
                                                         "//" + PRE_WSSE + ":UsernameToken/" +
                                                         PRE_WSSE + ":Password", xmi);
            dDoc.createText(connectionConfig.getPassword(), iPassword);

            int iNameIdentifier = XPathHelper.selectSingleNode(iLoginRequest,
                                                               "//" + PRE_SAML + ":Subject/" +
                                                               PRE_SAML + ":NameIdentifier", xmi);
            dDoc.createText(connectionConfig.getUserName(), iNameIdentifier);

            // Fill in the datetime and request id.
            int eRequest = XPathHelper.selectSingleNode(iLoginRequest,
                                                        "//" + PRE_SOAP + ":Body/" + PRE_SAMLP +
                                                        ":Request", xmi);

            Node.setAttribute(eRequest, "IssueInstant",
                              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
            Node.setAttribute(eRequest, "RequestID", UUID.randomUUID().toString());

            int iLoginResponse = SoapHelpers.sendExternalSOAPRequest(uRequestUrl, iLoginRequest,
                                                                     null, null, "");

            // First check the login status
            String sStatus = XPathHelper.getStringValue(iLoginResponse,
                                                        "//" + PRE_SAMLP + ":Status/" + PRE_SAMLP +
                                                        ":StatusCode/text()", xmi, "samlp:Success");

            if (!sStatus.endsWith("Success"))
            {
                throw new Exception("Error logging on to Cordys: " + sStatus);
            }

            // Now build up the token.
            int iSAMLToken = dDoc.parseString("<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\"/>");

            int nAssertion = XPathHelper.selectSingleNode(iLoginResponse,
                                                          "//" + PRE_SOAP + ":Body/" + PRE_SAMLP +
                                                          ":Response/" + PRE_SAML + ":Assertion",
                                                          xmi);

            if (nAssertion == 0)
            {
                throw new Exception("Could not find the assertion");
            }

            int nSignature = XPathHelper.selectSingleNode(iLoginResponse,
                                                          "//" + PRE_SOAP + ":Body/" + PRE_SAMLP +
                                                          ":Response/" + PRE_XMLSIG + ":Signature",
                                                          xmi);

            if (nSignature == 0)
            {
                throw new Exception("Could not find the signature");
            }

            Node.appendToChildren(nAssertion, iSAMLToken);
            Node.appendToChildren(nSignature, iSAMLToken);

            samlToken = iSAMLToken;

            if (liLogger != null)
            {
                liLogger.debug("Obtained SAML token: " + Node.writeToString(samlToken, false));
            }
        }

        // Now fix the request
        Document dDoc = Node.getDocument(request);
        int nHeader = XPathHelper.selectSingleNode(request, PRE_SOAP + ":Header", xmi);

        if (nHeader == 0)
        {
            nHeader = dDoc.createElementNS("Header", null, PRE_SOAP,
                                           NamespaceDefinitions.XMLNS_SOAP_1_1, 0);

            if (Node.getFirstChild(request) != 0)
            {
                Node.insert(nHeader, Node.getFirstChild(request));
            }
            else
            {
                // No child found.
                Node.appendToChildren(nHeader, request);
            }
        }

        int nSecurity = XPathHelper.selectSingleNode(nHeader, PRE_WSSE + ":Security", xmi);

        if (nSecurity != 0)
        {
            // Remove it
            Node.delete(nSecurity);
        }

        // Create it
        Node.appendToChildren(Node.duplicate(samlToken), nHeader);
    }
}
