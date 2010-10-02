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

package com.cordys.tools.ant.cm;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;
import com.cordys.tools.ant.soap.BusSoapRequestManager;
import com.cordys.tools.ant.soap.GatewaySoapRequestManager;
import com.cordys.tools.ant.soap.HttpConnectionConfig;
import com.cordys.tools.ant.soap.ISoapRequest;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.GeneralUtils;
import com.cordys.tools.ant.util.ValidationUtil;
import com.eibus.connector.nom.SOAPMessage;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Find;

/**
 * This Custom Task extends the ant Task. Manages the content types. The operations can be performed
 * for exporting content types from ECX to File and vice versa and for deleting content types.
 * Example:
 *
 * <pre><xmp>
           <target name="exportcontent"
                     description="Exports the contents from ECX to File System">
               <contentmanager
                           organization="${content.org}"
                           userdn="${content.userdn}"
                           operation="ecxtofile"
                           host="${content.host}"
                           port="${content.port}"
                           username="${content.username}"
                           password="${content.password}"
                           filter="roles,methodsets">
                            <content type="roles" contentfile="${src.content}/Roles.xml" />
                            <content type="methodsets" dir="${src.content}/Methodsets" />
                            <content type="menus" contentfile="${src.content}/Menus.xml" />
                            <content type="xmlstore" dir="${src.content}/XMLStore" />
                            <content type="styles" dir="${src.content}/Styles" />
                            <content type="applicationconnectors" dir="${src.content}/ApplicationConnectors" />
                                   <content type="custom" handler="com.cordys.cpc.ant.BusinessObjectLoader" contentfile|dir="" />
                   </contentmanager>
             </target>

 *
<!--Target for importing contents from File System to ECX -->
    <target name="importcontent"
                           description="Imports the contents from File System to ECX">
                   <contentmanager
                    organization="${content.org}"
                           userdn="${content.userdn}"
                     operation="filetoecx"
                           filter="roles,methodsets">
                           <content type="methodsets">
                                           <fileset dir="${src.content}\Methodsets" includes="" />
                                   </content>
                            <content type="roles">
                                    <fileset dir="${src.content}\Roles" includes="" />
                            </content>
                            <content type="menus">
                                    <fileset dir="${src.content}\Menus" includes="" />
                            </content>
                            <content type="xmlstore">
                                    <fileset dir="${src.content}\XMLStore" includes="" />
                            </content>
                            <content type="applicationconnectors">
                                    <fileset dir="${src.content}\ApplicationConnectors" includes="" />
                            </content>
                            <content type="styles">
                                    <fileset dir="${src.content}\Styles" includes="" />
                            </content>
                     </contentmanager>
                   </target>

 *
<!--Target for deleting contents from their sources -->
            <target name="deletecontent"
                    description="Deletes the contents from the sources">
                      <contentmanager
                            organization="${content.org}"
                                   userdn="${content.userdn}"
                             operation="delete"
                                   filter="roles,methodsets">
                                     <content type="roles" />
                                     <content type="methodsets" />
                                     <content type="applicationconnectors" />
                                     <content type="styles" />
                                     <content type="menus" />
                                     <content type="xmlstore" />
                      </contentmanager>
                   </target>

   </xmp></pre>
 *
 * @author  msreejit
 */
public class ContentManagerTask extends Task
{
    /**
     * Property name for LDAP SSL.
     */
    private static final String PROPERTY_BUS_SSL = "bus.ldap.processor.ssl";

    /**
     * Property name for LDAP SSL truststore path.
     */
    public static final String PROPERTY_BUS_TRUSTSTORE_PATH = "bus.ssl.truststore";

    /**
     * Property name for LDAP SSL truststore password.
     */
    public static final String PROPERTY_BUS_TRUSTSTORE_PASSWORD = "bus.ssl.truststorepassword";

    /**
     * Property value for bus connection mode.
     */
    private static final String CONNECTIONMODE_BUS = "bus";

    /**
     * Property name for bus connection mode.
     */
    private static final String CONNECTIONMODE_GATEWAY = "webgateway";

    /**
     * Property name for connection mode.
     */
    private static final String PROPERTY_CONNECTION_MODE = "connection.mode";

    /**
     * Constant which stores the message shown is the content attribute specified is not valid.
     */
    private static final String PARAMETER_USAGE = "Mandatory attribute is empty or has not been specified - ";
    /**
     * Contains mappings from handler name to actual content type contained in the isv.content
     * property. If
     */
    private static Map<String, String> mContentTypeMap = new HashMap<String, String>();

    static
    {
        mContentTypeMap.put(Content.TYPE_COBOC2, "coboc");
    }

    /**
     * The vector which contains all the content to be managed.
     */
    private Vector<Content> contents;

    /**
     * The filter is comma separated string consisting of types that only need to executed.
     */
    private String filter;

    /**
     * The array of filter types which need to be run finally.
     */
    private String[] filterTypes = null;

    /**
     * Attribute which stores the host name of the LDAP.
     */
    private String host;

    /**
     * The distinguished name(DN) of the ISV Package from which the content is to be extracted.
     */
    private String isvdn;

    /**
     * Holds the name of the ISVP file.
     */
    private String isvfile;

    /**
     * The operation in which the content manager should work. The supported modes are ecxtofile,
     * filetoecx & delete.
     */
    private OperationAttribute operation;

    /**
     * Attribute which stores the distiguished name(dn) of the organization or the isv package.
     */
    private String organization;

    /**
     * Attribute which stores the password used to connect to the LDAP.
     */
    private String password;

    /**
     * Attribute which stores the the port number of the LDAP.
     */
    private int port;

    /**
     * Used for sending soap requests to the backend SOAP processor.
     */
    private ISoapRequestManager soapRequestMgr;

    /**
     * Attribute which stores the distnguished name(dn) of the user in whose name the Soap Requests
     * have to be sent to the ECX. Optional - If not specified it defaults to user in whose context
     * the ANT file is run.
     */
    private String userdn;

    /**
     * Attribute which stores the username used to connect to the LDAP.
     */
    private String username;
    /**
     * Contains version information as read from the BCP server.
     */
    private BcpVersionInfo versionInfo;
    /**
     * Gateway connection configuration
     */
    private static HttpConnectionConfig connectionConfig;
    
    /**
     * Default constructor.
     */
    public ContentManagerTask()
    {
        super();
    }

    /**
     * DOCUMENTME.
     *
     * @param   tTask            DOCUMENTME
     * @param   sLdapHost        DOCUMENTME
     * @param   sLdapUser        DOCUMENTME
     * @param   sLdapPassword    DOCUMENTME
     * @param   iLdapPort        DOCUMENTME
     * @param   sUserDN          DOCUMENTME
     * @param   sOrganizationDN  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SoapRequestException  DOCUMENTME
     */
    public static ISoapRequestManager createRequestManager(Task tTask, String sLdapHost,
                                                           String sLdapUser, String sLdapPassword,
                                                           int iLdapPort, String sUserDN,
                                                           String sOrganizationDN)
                                                    throws SoapRequestException
    {
        Project pProject = tTask.getProject();
        ISoapRequestManager soapRequestMgr;
        String sConnectionMode = GeneralUtils.getTrimmedProperty(pProject,
                                                                 PROPERTY_CONNECTION_MODE);

        if ((sConnectionMode == null) || (sConnectionMode.length() == 0))
        {
            // Use the default bus connection.
            sConnectionMode = CONNECTIONMODE_BUS;
        }

        if ((sOrganizationDN == null) || (sOrganizationDN.length() == 0))
        {
            sOrganizationDN = GeneralUtils.getTrimmedProperty(pProject,
                                                              BuildFrameworkProperty.CONTENT_ORG
                                                              .getName());
        }

        if ((sUserDN == null) || (sUserDN.length() == 0))
        {
            sUserDN = GeneralUtils.getTrimmedProperty(pProject,
                                                      BuildFrameworkProperty.CONTENT_USERDN
                                                      .getName());
        }

        if (sConnectionMode.equals(CONNECTIONMODE_BUS))
        {
            String sBusSSL = GeneralUtils.getTrimmedProperty(pProject, PROPERTY_BUS_SSL);
            boolean bBusSSL = (sBusSSL != null) && (sBusSSL.equalsIgnoreCase("true"));
            String sTrustStorePath = GeneralUtils.getTrimmedProperty(pProject,
                                                                     PROPERTY_BUS_TRUSTSTORE_PATH);
            String sTrustStorePassword = GeneralUtils.getTrimmedProperty(pProject,
                                                                         PROPERTY_BUS_TRUSTSTORE_PASSWORD);

            // Initialize the SoapRequestManager
            if ((sLdapHost == null) || (sLdapHost.length() == 0))
            {
                sLdapHost = GeneralUtils.getTrimmedProperty(pProject,
                                                            BuildFrameworkProperty.LDAP_SERVER
                                                            .getName());
            }

            if (iLdapPort < 0)
            {
                String tmp = GeneralUtils.getTrimmedProperty(pProject,
                                                             BuildFrameworkProperty.LDAP_PORT
                                                             .getName());

                if ((tmp != null) && (tmp.length() > 0))
                {
                    iLdapPort = Integer.parseInt(tmp);
                }
            }

            if ((sLdapPassword == null) || (sLdapPassword.length() == 0))
            {
                sLdapPassword = GeneralUtils.getTrimmedProperty(pProject,
                                                                BuildFrameworkProperty.LDAP_PASSWORD
                                                                .getName());
            }

            if ((sLdapUser == null) || (sLdapUser.length() == 0))
            {
                sLdapUser = GeneralUtils.getTrimmedProperty(pProject,
                                                            BuildFrameworkProperty.LDAP_USER
                                                            .getName());
            }

            soapRequestMgr = new BusSoapRequestManager(sLdapHost, iLdapPort, sLdapUser,
                                                       sLdapPassword, bBusSSL, sTrustStorePath,
                                                       sTrustStorePassword, tTask);
        }
        else if (sConnectionMode.equals(CONNECTIONMODE_GATEWAY))
        {
            HttpConnectionConfig config = new HttpConnectionConfig();
            
            config.configureFromTask(tTask, sConnectionMode);

            soapRequestMgr = new GatewaySoapRequestManager(config,
                                                           tTask);
            
            connectionConfig = config;
        }
        else
        {
            throw new SoapRequestException("Invalid connection mode: " + sConnectionMode);
        }

        soapRequestMgr.setUserDN(sUserDN);
        soapRequestMgr.setOrganizationDN(sOrganizationDN);

        soapRequestMgr.initialize();

        return soapRequestMgr;
    }

    /**
     * DOCUMENTME.
     *
     * @param   tException  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public static String getExceptionMessage(Throwable tException)
    {
        String sMsg = tException.getMessage();

        if ((sMsg == null) && (tException.getCause() != null))
        {
            return getExceptionMessage(tException.getCause());
        }

        return sMsg;
    }

    /* ******************Nested Tag configuration methods******************* */
    /**
     * Handles the nested element tag tag name: content attributes supported: type, handler,
     * contentfile
     *
     * @param  content  The ISVContent object instance passed by ANT after setting the attributes.
     */
    public void addConfiguredContent(Content content)
    {
        if (contents == null)
        {
            contents = new Vector<Content>();
        }

        StringBuffer message = new StringBuffer();

        if ((content.getType() == null) || "".equals(content.getType()))
        {
            message.append("\n").append(PARAMETER_USAGE).append("type");
            message.append(" of nested tag 'content'");
            GeneralUtils.handleException(message.toString());
        }

        content.checkAndSetHandler();

        message.append("\n").append("Type: ").append(content.getType());
        message.append("\n").append("Details: ");

        // check if nothing has been specified and operation is not a delete operation.
        if ((content.getDir() == null) && (content.getContentFile() == null) &&
                !OperationAttribute.OPERATION_DELETE.equals(getOperation().toString()))
        {
            // Only if operation is FILE to ECX, The fileset need to be specified.
            if (OperationAttribute.OPERATION_FILE_TO_ECX.equals(getOperation().toString()) &&
                    (content.getFileSet().size() == 0))
            {
                message.append("Atleast a contentfile|dir attribute or nested fileset should be specified.");
                GeneralUtils.handleException(message.toString());
            }
            else if (OperationAttribute.OPERATION_ECX_TO_FILE.equals(getOperation().toString()))
            {
                message.append("Atleast a contentfile|dir attribute should be specified.");
                GeneralUtils.handleException(message.toString());
            }
        }
        else if ((content.getDir() != null) && (content.getContentFile() != null))
        {
            message.append("Both contentfile and dir cannot be specified together for nested tag 'content'");
            GeneralUtils.handleException(message.toString());
        }
        else if (OperationAttribute.OPERATION_FILE_TO_ECX.equals(getOperation().toString()) &&
                     (content.getFileSet().size() > 0) && (content.getDir() != null))
        {
            message.append("Both dir and fileset cannot be specified together for nested tag 'content'");
            GeneralUtils.handleException(message.toString());
        }
        else if (OperationAttribute.OPERATION_FILE_TO_ECX.equals(getOperation().toString()) &&
                     (content.getFileSet().size() > 0) && (content.getContentFile() != null))
        {
            message.append("Both contentfile and fileset cannot be specified together for nested tag 'content'");
            GeneralUtils.handleException(message.toString());
        }

        // add it to the collection of contents.
        contents.add(content);
    }

    /**
     * Implementing the abstract execute method of Task.
     *
     * @throws  BuildException  DOCUMENTME
     */
    @Override public void execute()
                           throws BuildException
    {
        log("Checking parameters...", Project.MSG_INFO);

        /*
         * Show message if mandatory parameters have not been specified.
         */
        boolean isParamaterSet = true;
        StringBuffer message = new StringBuffer();

        // check for organization.
        if ((organization == null) || (organization.length() == 0))
        {
            message.append(PARAMETER_USAGE);
            message.append("organization\n");
            isParamaterSet = false;
        }

        // check for operation.
        if (operation == null)
        {
            message.append(PARAMETER_USAGE);
            message.append("operation\n");
            isParamaterSet = false;
        }

        if ((contents == null) || (contents.size() == 0))
        {
            message.append("Atleast one content type should be specified using nested tag 'content'.");
            isParamaterSet = false;
        }

        if (EIBProperties.getBoolean("bus.socket.localloop") && isLocalMachineName(host))
        {
            message.append("Cordys is configured for Standalone configuration, ");
            message.append("remote connections are not possible.\n\n");
            message.append("Set hostname to 'localhost' or enable remote connections (use Cordys Management Console).");
            isParamaterSet = false;
        }

        if (!isParamaterSet)
        {
            GeneralUtils.handleException(message.toString());
        }

        // Check if we are supposed to operate on a specified content instead of all of them.
        String sSingleContentName = GeneralUtils.getTrimmedProperty(getProject(),
                                                                    IContent.SINGLE_CONTENT_PROPERTY);

        try
        {
            soapRequestMgr = createRequestManager(this, host, username, password, port, userdn,
                                                  organization);

            // set the timeout of the soap requests to indefinite.
            soapRequestMgr.setTimeout(0);

            try
            {
                getPlatformVersion();
            }
            catch (Exception e)
            {
                GeneralUtils.handleException("Unable to get Cordys BCP server version.\n" +
                                             getExceptionMessage(e), e, this);
            }

            log("Server version: " + versionInfo);
            soapRequestMgr.setBcpVersion(versionInfo);

            log("Organization used: " + organization);
            // check whether the organization is valid.
            ValidationUtil.checkOrganization(organization, soapRequestMgr);

            if (userdn != null) // check whether the user is valid.
            {
                log("User dn used: " + userdn);
                ValidationUtil.checkUser(userdn, soapRequestMgr);
            }

            // if the filter has been specified.
            if ((filter != null) && !filter.equals(""))
            {
                if (filter.indexOf(",") != -1)
                {
                    filterTypes = filter.split(",");
                }
                else
                {
                    filterTypes = new String[] { filter };
                }
            }

            for (Iterator<Content> iter = contents.iterator(); iter.hasNext();)
            {
                Content content = iter.next();
                String sContentType = mContentTypeMap.get(content.getType());

                if (sContentType == null)
                {
                    sContentType = content.getType();
                }

                log("---> " + sContentType + " <---");

                // if the filter has been specified.
                if ((filter != null) && !filter.equals(""))
                {
                    boolean isTypeOK = false;

                    for (int i = 0; i < filterTypes.length; i++)
                    {
                        if (sContentType.equals(filterTypes[i]))
                        {
                            isTypeOK = true;
                            break;
                        }
                    }

                    if (!isTypeOK)
                    {
                        log("[" + sContentType + "] Skipping this contenttype.", Project.MSG_INFO);

                        continue;
                    }
                }

                ContentHandler chHandler = content.getHandlerObject();
                String sSingleTmp = sSingleContentName;

                if ((sSingleTmp == null) || (sSingleTmp.length() == 0))
                {
                    String sSingleContentFile = GeneralUtils.getTrimmedProperty(getProject(),
                                                                                IContent.SINGLE_CONTENT_FILE_PROPERTY);

                    if ((sSingleContentFile != null) && (sSingleContentFile.length() > 0))
                    {
                        try
                        {
                            boolean toEcx = OperationAttribute.OPERATION_FILE_TO_ECX.equals(operation
                                                                                            .getValue());

                            sSingleTmp = convertSingleContentFileToName(sSingleContentFile,
                                                                        chHandler, content, toEcx);
                        }
                        catch (IOException e)
                        {
                            GeneralUtils.handleException("Unable to get single content name from the file\n" +
                                                         getExceptionMessage(e), e, this);
                        }
                    }
                }

                if (sSingleTmp != null)
                {
                    log("[" + sContentType + "] Processing only content with name '" + sSingleTmp +
                        "'", Project.MSG_INFO);
                }

                content.setSingleContentName(sSingleTmp);
                content.setBcpVersion(versionInfo);

                if (OperationAttribute.OPERATION_ECX_TO_FILE.equals(operation.getValue()))
                {
                    log("[" + sContentType + "] Getting content from the Cordys server: " +
                        sContentType, Project.MSG_INFO);
                    chHandler.executeEcxToFile(this, content, soapRequestMgr);
                }
                else if (OperationAttribute.OPERATION_FILE_TO_ECX.equals(operation.getValue()))
                {
                    log("[" + sContentType + "] Sending the content to the Cordys server: " +
                        sContentType, Project.MSG_INFO);
                    chHandler.executeFileToEcx(this, content, soapRequestMgr);
                }
                else if (OperationAttribute.OPERATION_DELETE.equals(operation.getValue()))
                {
                    log("[" + sContentType + "] Deleting content from the Cordys server: " +
                        sContentType, Project.MSG_INFO);
                    chHandler.executeDelete(this, content, soapRequestMgr);
                }
                else if (OperationAttribute.OPERATION_PUBLISH_TO_RUNTIME.equals(operation
                                                                                    .getValue()))
                {
                    log("[" + sContentType + "] Publishing content to runtime: " + sContentType,
                        Project.MSG_INFO);
                    chHandler.executePublishToRuntime(this, content, soapRequestMgr);
                }

                if ((sSingleContentName != null) && (chHandler.getNumberOfProcessedItems() == 0))
                {
                    GeneralUtils.handleException("No content with name " + sSingleContentName +
                                                 " found.");
                }
            }
        }
        catch (IllegalArgumentException iae)
        {
            GeneralUtils.handleException("The argument passed is invalid." +
                                         getExceptionMessage(iae), iae, this);
        }
        catch (SoapRequestException sre)
        {
            GeneralUtils.handleException("Error occured while sending request to ECX.\n" +
                                         getExceptionMessage(sre), sre, this);
        }
    }

    /**
     * Returns the filter of types that only need to executed.
     *
     * @return  The filter of types that only need to executed.
     */
    public String getFilter()
    {
        return filter;
    }

    /**
     * Returns the remote host in which the content needs to be handled.
     *
     * @return  The remote host.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Returns the distinguished name of the ISV package.
     *
     * @return  The dn of the ISV package.
     */
    public String getIsvdn()
    {
        return isvdn;
    }

    /**
     * This method returns the name of the ISVP file.
     *
     * @return  The name of the ISVP file.
     */
    public String getIsvfile()
    {
        return isvfile;
    }

    /**
     * Returns the operation mode in which the contentmanager should be executed. Can be any of the
     * three types - ecxtofile, filetoecx or delete.
     *
     * @return  The operation mode.
     */
    public OperationAttribute getOperation()
    {
        return operation;
    }

    /**
     * Returns the distinguished name of organization in the LDAP.
     *
     * @return  The organization dn.
     */
    public String getOrganization()
    {
        return organization;
    }

    /**
     * Returns the password to used for connecting to the remote host.
     *
     * @return  The password used for connecting to the remote host.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Returns the port number used to connect to remote host machine.
     *
     * @return  The port number.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Returns the server's host name either from LDAP host parameter or the web gateway URL.
     *
     * @return  Host name.
     */
    public String getServerHostName()
    {
        String sHostName = null;
        URL uGatewayUrl = connectionConfig != null ? connectionConfig.getGatewayUrl() : null;

        if (uGatewayUrl != null)
        {
            sHostName = uGatewayUrl.getHost();
        }
        else if (host != null)
        {
            sHostName = host.replaceFirst("[^:]+://([^:]*):.*", "$1");
            ;
        }

        if ((sHostName == null) || (sHostName.length() == 0))
        {
            throw new IllegalArgumentException("Unable to determine server's host name.");
        }

        return sHostName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  Returns the soapRequestMgr.
     */
    public ISoapRequestManager getSoapRequestMgr()
    {
        return soapRequestMgr;
    }

    /**
     * Returns the distinguished name of the User to be used for connecting to ECX machine.
     *
     * @return  The userdn to use.
     */
    public String getUserdn()
    {
        return userdn;
    }

    /**
     * Returns the name of the User to be used for connecting to ECX machine.
     *
     * @return  The username used for connecting to LDAP.
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Returns the versionInfo.
     *
     * @return  Returns the versionInfo.
     */
    public BcpVersionInfo getVersionInfo()
    {
        return versionInfo;
    }

    /**
     * Sets the filter of types that only need to executed.
     *
     * @param  string  The filter of types that only need to executed.
     */
    public void setFilter(String string)
    {
        filter = GeneralUtils.safeTrim(string);
    }

    /**
     * Sets the remote host machine name to connect.
     *
     * @param  host  The remote host machine name.
     */
    public void setHost(String host)
    {
        this.host = GeneralUtils.safeTrim(host);
    }

    /**
     * Sets the distinguished name of the ISV package.
     *
     * @param  isvdn  The dn of the ISV package.
     */
    public void setIsvdn(String isvdn)
    {
        this.isvdn = GeneralUtils.safeTrim(isvdn);
    }

    /**
     * This method sets the name of the ISVP file.
     *
     * @param  isvfile  The name of the ISVP file.
     */
    public void setIsvfile(String isvfile)
    {
        this.isvfile = GeneralUtils.safeTrim(isvfile);
    }

    /**
     * Sets the operation to be used.
     *
     * @param  opr
     */
    public void setOperation(OperationAttribute opr)
    {
        this.operation = opr;
    }

    /**
     * Sets the distinguished name of the organization in which the content has to be managed.
     *
     * @param  org  The organization dn.
     */
    public void setOrganization(String org)
    {
        organization = GeneralUtils.safeTrim(org);
    }

    /**
     * Sets the password to be used for connecting to the remote host.
     *
     * @param  pwd  The password to be used.
     */
    public void setPassword(String pwd)
    {
        password = GeneralUtils.safeTrim(pwd);
    }

    /**
     * Sets the port number used to connect to remote host machine.
     *
     * @param  i  The port number.
     */
    public void setPort(int i)
    {
        port = i;
    }

    /**
     * Sets the distinguished name of the User to be used for connecting to ECX machine.
     *
     * @param  userdn  The User DN.
     */
    public void setUserdn(String userdn)
    {
        this.userdn = GeneralUtils.safeTrim(userdn);
    }

    /**
     * Sets the username of the LDAP to connect.
     *
     * @param  string  The username used for connecting to LDAP.
     */
    public void setUsername(String string)
    {
        username = GeneralUtils.safeTrim(string);
    }

    /**
     * Converts the file to a single content name.
     *
     * @param   singleFile
     * @param   handler     DOCUMENTME
     * @param   content     DOCUMENTME
     * @param   toEcx       DOCUMENTME
     *
     * @return
     *
     * @throws  IOException
     */
    private String convertSingleContentFileToName(String singleFile, ContentHandler handler,
                                                  Content content, boolean toEcx)
                                           throws IOException
    {
        String[] files = singleFile.split(";");
        StringBuilder res = new StringBuilder(100);

        for (String f : files)
        {
            File contentFile = new File(f);

            if (!contentFile.exists())
            {
                throw new BuildException("Content file does not exist: " + contentFile);
            }

            String name = handler.getSingleContentName(contentFile, this, content, toEcx);

            if ((name != null) && (name.length() > 0))
            {
                if (res.length() > 0)
                {
                    res.append(";");
                }

                res.append(name);
            }
        }

        return (res.length() > 0) ? res.toString() : null;
    }

    /**
     * Reads the version from the BCP server.
     *
     * @throws  Exception  SoapRequestException
     */
    private void getPlatformVersion()
                             throws Exception
    {
        String property = getProject().getProperty(BuildFrameworkProperty.PLATFORM_VERSION_STRING
                                                   .getName());

        if ((property != null) && (property.length() > 0))
        {
            versionInfo = BcpVersionInfo.createFromProperty(property);
            return;
        }

        ISoapRequest req = soapRequestMgr.createSoapRequest();

        req.addMethod("http://schemas.cordys.com/1.0/monitor", "GetInstallationInfo");

        int response = req.execute();

        if (response != 0)
        {
            int body = SOAPMessage.getBodyNode(response);
            int method = (body != 0) ? Find.firstMatch(body, "<><>") : 0;

            if (method != 0)
            {
                versionInfo = BcpVersionInfo.createFromInstallationInfo(method);
            }
        }

        if (versionInfo == null)
        {
            versionInfo = BcpVersionInfo.getDefault();
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param   hostName  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private boolean isLocalMachineName(String hostName)
    {
        // First check if we are using the bus connection mode.
        String sConnectionMode = GeneralUtils.getTrimmedProperty(getProject(),
                                                                 PROPERTY_CONNECTION_MODE);

        if (CONNECTIONMODE_GATEWAY.equals(sConnectionMode))
        {
            // We are conneting through web gateway, so the connection mode does not matter.
            return false;
        }

        InetAddress localHostAddr;

        try
        {
            localHostAddr = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e)
        {
            log("Unable to determine localhost address: " + hostName, Project.MSG_ERR);
            return false;
        }

        if (hostName.equals(localHostAddr.getHostName()) ||
                hostName.equals(localHostAddr.getHostAddress()))
        {
            return true;
        }

        return false;
    }

    /**
     * Inner Class for handling OperationAttribute as a enumerated attribute.
     */
    public static class OperationAttribute extends EnumeratedAttribute
    {
        /**
         * Operation type ECX to File.
         */
        static final String OPERATION_ECX_TO_FILE = "ecxtofile";

        /**
         * Operation type File to ECX.
         */
        static final String OPERATION_FILE_TO_ECX = "filetoecx";

        /**
         * Operation type delete.
         */
        static final String OPERATION_DELETE = "delete";

        /**
         * Operation type publish.
         */
        static final String OPERATION_PUBLISH_TO_RUNTIME = "publish";

        /**
         * Implementing the getValues of EnumeratedAttributes class.
         *
         * @return  DOCUMENTME
         *
         * @see     org.apache.tools.ant.types.EnumeratedAttribute#getValues()
         */
        @Override public String[] getValues()
        {
            return new String[]
                   {
                       OPERATION_ECX_TO_FILE, OPERATION_FILE_TO_ECX, OPERATION_DELETE,
                       OPERATION_PUBLISH_TO_RUNTIME
                   };
        }
    }
}
