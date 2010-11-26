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
 /**
 *         Project         :        BuildFramework
 *         File                :        SoapDispatcher.java
 *         Author                :        manesh@cordys.com
 *         Created on         :        Oct 4, 2004
 *
 */
package com.cordys.tools.ant.taskdefs;

import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.soap.ISoapRequest;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.GeneralUtils;
import com.cordys.tools.ant.util.NodeUtil;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.eibus.xml.xpath.NodeSet;
import com.eibus.xml.xpath.ResultNode;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This class sends a soap message to a service.
 */
public class SoapDispatcher extends Task
{
    /**
     * Copying the attribute values from ContentManagerTask &
     * ISVCreatorTask
     */
    public static final String DEFAULT_NAME = "SoapDispatcher";
    /**
     * File in which requests are stored
     */
    private File requestFile = null;
    /**
     * Contains ResponseData object for reading values from the
     * response.
     */
    private List<ResponseData> responseDataList = new ArrayList<ResponseData>(10);
    /**
     * Attribute which stores the ldapHost name of the LDAP.
     */
    private String ldapHost = null;
    /**
     * Attribute which stores the ldapPassword used to connect to the
     * LDAP.
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
     * Name of the connector instance that needs to be created
     */
    private String name = DEFAULT_NAME;
    /**
     * Attribute which stores the distiguished name(dn) of the
     * organization A soap processor will be searched in this organization
     * first
     */
    private String orgdn = null;
    /**
     * Name of the soap processor to which you need to send request
     */
    private String soapprocessordn = null;
    /**
     * Attribute which stores the distnguished name(dn) of the user in
     * whose name the  Soap Requests have to be sent to the ECX. Optional - If
     * not specified it defaults to user in whose context the ANT file is run.
     */
    private String userdn = null;
    /**
     * String buffer containing the SOAP request XML inlined in the Ant
     * task. Only the file or this can be set.
     */
    private StringBuilder inlineRequestXml = null;
    /**
     * Fail the build process if a SOAP:Fault was received.
     */
    private boolean failOnSoapFault = true;
    /**
     * Attribute which stores the the ldapPort number of the LDAP.
     */
    private int ldapPort = -1;

    /**
     * Adds inline SOAP request XML.
     *
     * @param sXml SOAP request xml.
     */
    public void addText(String sXml)
    {
        if (inlineRequestXml == null)
        {
            inlineRequestXml = new StringBuilder(sXml.length() + 50);
            inlineRequestXml.append("<root>");
        }

        inlineRequestXml.append(sXml);
    }

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public ResponseData createResponse()
    {
        ResponseData data = new ResponseData();

        responseDataList.add(data);

        return data;
    }

    /**
     * Implementing the abstract method of class Task
     */
    public void execute()
                 throws BuildException
    {
        validateAttributes();

        //Create SoapRequestManager object
        ISoapRequestManager soapRequestMGR = null;
        ISoapRequest soapRequest = null;

        try
        {
            soapRequestMGR = ContentManagerTask.createRequestManager(this,
                                                                     ldapHost,
                                                                     ldapUser,
                                                                     ldapPassword,
                                                                     ldapPort,
                                                                     userdn,
                                                                     orgdn);

            soapRequest = soapRequestMGR.createSoapRequest();
        }
        catch (SoapRequestException sre)
        {
            GeneralUtils.handleException("Error occured while sending request " +
                                         "to ECX machine.\n" +
                                         sre.getMessage(), sre, this);
        }

        //Set required properties
        soapRequest.setUser(userdn);
        soapRequest.setReceiver(soapprocessordn);
        soapRequestMGR.setTimeout(0);

        int response = 0;
        int[] methods = null;

        try
        {
            methods = getRequestMethods(soapRequestMGR.getDocument());

            for (int i = 0; i < methods.length; i++)
            {
                String methodString = Node.writeToString(methods[i], false);

                methodString = getProject().replaceProperties(methodString);

                int finalMethod = soapRequestMGR.getDocument()
                                                .parseString(methodString);

                soapRequest.addMethod(orgdn, finalMethod);
            }

            response = soapRequest.execute();

            if (responseDataList.size() > 0)
            {
                for (ResponseData data : responseDataList)
                {
                    setResponseValue(response, data);
                }
            }
        }
        catch (SoapRequestException sre)
        {
            if (failOnSoapFault)
            {
                GeneralUtils.handleException("Error occured while sending request " +
                                             "to ECX machine.\n" +
                                             sre.getMessage(), sre, this);
            }
        }
        catch (XMLException xe)
        {
            GeneralUtils.handleException("Error occured while sending request " +
                                         "to ECX machine.\n" + xe.getMessage(),
                                         xe, this);
        }
        catch (java.io.UnsupportedEncodingException uee)
        {
            GeneralUtils.handleException("Error occured while sending request " +
                                         "to ECX machine.\n" +
                                         uee.getMessage(), uee, this);
        }
        finally
        {
            Node.delete(response);

            if (methods != null)
            {
                for (int m : methods)
                {
                    Node.delete(m);
                }
            }
        }
    }

    /**
     * Returns the remote ldapHost in which the content needs to be
     * handled.
     *
     * @return The LDAP host.
     */
    public String getLdapHost()
    {
        return ldapHost;
    }

    /**
     * Returns the ldapPassword to used for connecting to the remote
     * ldapHost.
     *
     * @return The ldapPassword used for connecting to the remote ldapHost.
     */
    public String getLdapPassword()
    {
        return ldapPassword;
    }

    /**
     * Returns the ldapPort number used to connect to remote ldapHost
     * machine.
     *
     * @return The ldapPort number.
     */
    public int getLdapPort()
    {
        return ldapPort;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the ldapSSL.
     */
    public String getLdapSSL()
    {
        return ldapSSL;
    }

    /**
     * Returns the name of the User to be used for connecting to ECX
     * machine.
     *
     * @return The ldapUser used for connecting to LDAP.
     */
    public String getLdapUser()
    {
        return ldapUser;
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
     * Returns the organizational context from which soap node will be
     * serached for.
     *
     * @return The DN of the organization.
     */
    public String getOrgDN()
    {
        return orgdn;
    }

    /**
     * Returns the request xml file
     *
     * @return The request file.
     */
    public File getRequestFile()
    {
        return requestFile;
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
     * Returns the user context from which requests will be fired
     *
     * @return The DN of the user.
     */
    public String getUserDN()
    {
        return userdn;
    }

    /**
     * Returns the failOnSoapFault.
     *
     * @return Returns the failOnSoapFault.
     */
    public boolean isFailOnSoapFault()
    {
        return failOnSoapFault;
    }

    /**
     * Sets the failOnSoapFault.
     *
     * @param failOnSoapFault The failOnSoapFault to be set.
     */
    public void setFailOnSoapFault(boolean failOnSoapFault)
    {
        this.failOnSoapFault = failOnSoapFault;
    }

    /**
     * Sets the remote ldapHost machine name to connect.
     *
     * @param host The remote ldapHost machine name.
     */
    public void setLdapHost(String host)
    {
        this.ldapHost = host;
    }

    /**
     * Sets the ldapPassword to be used for connecting to the remote
     * ldapHost.
     *
     * @param pwd The ldapPassword to be used.
     */
    public void setLdapPassword(String pwd)
    {
        this.ldapPassword = pwd;
    }

    /**
     * Sets the ldapPort number used to connect to remote ldapHost
     * machine.
     *
     * @param i The ldapPort number.
     */
    public void setLdapPort(int i)
    {
        this.ldapPort = i;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ldapSSL The ldapSSL to set.
     */
    public void setLdapSSL(String ldapSSL)
    {
        this.ldapSSL = ldapSSL;
    }

    /**
     * Sets the ldapUser of the LDAP to connect.
     *
     * @param user The ldapUser used for connecting to LDAP.
     */
    public void setLdapUser(String user)
    {
        this.ldapUser = user;
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
     * Sets the organizational context from which soap node has to be
     * found out
     *
     * @param dn DN of the organization
     */
    public void setOrgDN(String dn)
    {
        this.orgdn = dn;
    }

    /**
     * Set the file in which requests are stored
     *
     * @param file The request file.
     */
    public void setRequestFile(File file)
    {
        requestFile = file;
    }

    /**
     * Sets the user context from which requests will be fired If not
     * set, then it will take the default user context
     *
     * @param dn DN of the user
     */
    public void setSoapProcessorDN(String dn)
    {
        this.soapprocessordn = dn;
    }

    /**
     * Sets the user context from which requests will be fired If not
     * set, then it will take the default user context
     *
     * @param dn DN of the user
     */
    public void setUserDN(String dn)
    {
        this.userdn = dn;
    }

    /**
     * Ensure we have a consistent and legal set of attributes, and set
     * any internal flags necessary based on different combinations of
     * attributes.
     */
    protected void validateAttributes()
                               throws BuildException
    {
        if ((requestFile == null) && (inlineRequestXml == null))
        {
            throw new BuildException("requestfile property or inline XML are mandatory");
        }

        if ((requestFile != null) && (inlineRequestXml != null))
        {
            throw new BuildException("Only requestfile property or inline XML can be specified.");
        }

        if ((requestFile != null) && !requestFile.exists())
        {
            throw new BuildException("File doesnt exists :" +
                                     requestFile.getPath());
        }
    }

    /**
     * Gets the SOAP request XML from the parameters. TODO Describe the
     * method.
     *
     * @param dDoc DOCUMENTME
     *
     * @return
     */
    private int[] getRequestMethods(Document dDoc)
    {
        boolean bExceptionOccured = false;
        int request = 0;

        if (requestFile != null)
        {
            //Load the request xml files
            FileInputStream fis = null;

            byte[] byteArr = new byte[(int) requestFile.length()];

            try
            {
                fis = new FileInputStream(requestFile);
                fis.read(byteArr);
                request = dDoc.load(byteArr);
            }
            catch (FileNotFoundException fnfe)
            {
                bExceptionOccured = true;
                GeneralUtils.handleException("Error occured while sending request " +
                                             "to ECX machine.\n" +
                                             fnfe.getMessage(), fnfe, this);
            }
            catch (IOException ioe)
            {
                bExceptionOccured = true;
                GeneralUtils.handleException("Error occured while sending request " +
                                             "to ECX machine.\n" +
                                             ioe.getMessage(), ioe, this);
            }
            catch (XMLException xe)
            {
                bExceptionOccured = true;
                GeneralUtils.handleException("Error occured while sending request " +
                                             "to ECX machine.\n" +
                                             xe.getMessage(), xe, this);
            }
            finally
            {
                try
                {
                    if (fis != null)
                    {
                        fis.close();
                    }
                }
                catch (IOException ioe)
                {
                    //Ignore
                }

                if (bExceptionOccured && (request != 0))
                {
                    Node.delete(request);
                }
            }
        }
        else
        {
            inlineRequestXml.append("</root>");

            try
            {
                request = dDoc.parseString(inlineRequestXml.toString());
            }
            catch (Exception e)
            {
                bExceptionOccured = true;
                GeneralUtils.handleException("Error occured while sending request " +
                                             "to ECX machine.\n" +
                                             e.getMessage(), e, this);
            }
        }

        int[] methods = NodeUtil.getChildrenNodes(request);

        for (int i = 0; i < methods.length; i++)
        {
            methods[i] = Node.unlink(methods[i]);
        }

        Node.delete(request);

        return methods;
    }

    /**
     * Sets a value from the SOAP response into the response property.
     *
     * @param response DOCUMENTME
     * @param data DOCUMENTME
     */
    private void setResponseValue(int response, ResponseData data)
    {
        StringBuilder tmp = new StringBuilder(100);
        XPathResult result = data.responseXPath.evaluate(response);

        int type = result.getType();

        switch (type)
        {
            case XPathResult.XPATH_BOOLEAN:
                tmp.append(result.getBooleanResult());
                break;

            case XPathResult.XPATH_NODESET:

                NodeSet ns = result.removeNodeSetFromResult();

                while (ns.hasNext())
                {
                    long iResultNode = ns.next();

                    if (ResultNode.isAttribute(iResultNode))
                    {
                        tmp.append(ResultNode.getStringValue(iResultNode));
                    }
                    else
                    {
                        tmp.append(Node.writeToString(ResultNode.getElementNode(iResultNode),
                                                      false));
                    }
                }
                break;

            case XPathResult.XPATH_NUMBER:
                tmp.append(result.getNumberResult());
                break;

            case XPathResult.XPATH_STRING:
                tmp.append(result.getStringResult());
                break;
        }

        getProject().setProperty(data.getProperty(), tmp.toString());
    }

    /**
     * DOCUMENTME
     *
     * @author $author$
     */
    public static class ResponseData
    {
        /**
         * Name property of that will be set to the response value
         * specified by the response XPath.
         */
        String responseProperty;
        /**
         * XPath for reading a value from the response XML.
         */
        XPath responseXPath;

        /**
         * Returns the responseProperty.
         *
         * @return Returns the responseProperty.
         */
        public String getProperty()
        {
            return responseProperty;
        }

        /**
         * Returns the responseXPath.
         *
         * @return Returns the responseXPath.
         */
        public String getXPath()
        {
            return "";
        }

        /**
         * Sets the responseProperty.
         *
         * @param responseProperty The responseProperty to be set.
         */
        public void setProperty(String responseProperty)
        {
            this.responseProperty = responseProperty;
        }

        /**
         * Sets the responseXPath.
         *
         * @param responseXPath The responseXPath to be set.
         */
        public void setXPath(String responseXPath)
        {
            this.responseXPath = new XPath(responseXPath);
        }
    }
}
