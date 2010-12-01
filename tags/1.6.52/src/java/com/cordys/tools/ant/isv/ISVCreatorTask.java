/**
 * Copyright 2005 Cordys R&D B.V. 
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

package com.cordys.tools.ant.isv;

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;

import com.cordys.tools.ant.cm.BcpVersionInfo;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.util.GeneralUtils;
import com.cordys.tools.ant.util.NOMHandlesCollection;

import com.eibus.applicationconnector.isvpackage.ISVCreator;

import com.eibus.util.system.EIBProperties;

import com.eibus.version.Version;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Iterator;
import java.util.Map;

import java.util.Map.Entry;

import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.util.FileUtils;

/**
 * This Custom Task extends the ant Task. Creates ISV package based on the
 * given isv content xml file.  Example:<pre><xmp>
 * <target name="CreateISV"  description="Creates the ISV Package">
 *                         <isv
 *                                 organization="${content.org}"
 *                                 owner="${isv.owner}"
 *                                 destdir="${build.dir}/isv"
 *                                 productname="${isv.owner}"
 *                                 version="${isv.version}"
 *                                 name="${isv.name}"
 *                                 filename="${isv.filename}"
 *                                 type="${isv.type}"
 *                                 reciever="${monitor.sp.dn}"
 *                                 copyisv="true|false"
 *                                 eulafile="{isv.eula.dir}\eula.html"
 *                                 imagefile="{isv.sidebar.dir}\sidebar.bmp" >
 *                                         <content type="roles" contentfile="" />
 *                                         <content type="filesystem" contentfile="" />
 *                                         <content type="methodsets" contentfile="" />
 *                                         <content type="menus" contentfile="" />
 *                                         <content type="systemenvironment" contentfile="" />
 *                                         <content type="prompts" contentfile="" />
 *                                         <content type="xmlstore" contentfile="" />
 *                                         <content type="applicationconnectors" contentfile="" />
 *                                         <content type="styles" contentfile="" />
 *                                         <content type="dbschema" contentfile="" />
 *                                         <content type="virtualdirectories" contentfile="" />
 *                                         <content type="custom" handler="com.cordys.cpc.ant.BusinessObjectLoader" contentfile="" />
 *                         </isv>          </target></xmp></pre>
 *
 * @author msreejit
 */
public class ISVCreatorTask extends Task
{
    /**
     * Constant which stores the Invalid Character regular expression.
     */
    private static final String INVALID_CHAR_REGEXP = "[/*&\\\\\\[\\]\\(\\)<>@]";
    /**
     * Constant which stores the Invalid Character message.
     */
    private static final String INVALID_CHAR_ERROR = " cannot contain the following characters: / * & \\ [ ]( ) < > @";
    /**
     * Constant which stores the message shown is the content attribute
     * specified is not valid.
     */
    private static final String PARAMETER_USAGE = "Mandatory attribute is empty or has not been specified - ";
    /**
     * The File class which will provide the path of the End User
     * License Argreement(EULA).
     */
    private java.io.File eulafile;
    /**
     * The File class which will provide the path of the Side bar image
     * shown while uploading the ISV package.
     */
    private java.io.File imagefile;
    /**
     * Contains version information as read from the BCP server.
     */
    private BcpVersionInfo versionInfo;
    /**
     * The xml document that is used to handle xml functions.
     */
    private Document document;
    /**
     * The destination directory where the ISV package should be placed
     * after creation.
     */
    private File destdir;
    /**
     * The instance of <code>SoapRequestManager</code> class which is
     * used for handling soap requests.
     */
    private ISoapRequestManager soapRequestMgr;
    /**
     * The map containing contents handled by ISV with key as content
     * type.
     */
    private Map<String, Content> contents;
    /**
     * DOCUMENTME
     */
    private NOMHandlesCollection nomCollection = null;
    /**
     * The build number of the ISV Package.
     */
    private String buildNr;
    /**
     * Indicated whether or not to copy the ISV package to the
     * build-folder.
     */
    private String copyisv;
    /**
     * The name of file which is created by the ISV. The name doesn't
     * contain extension of the file.
     */
    private String filename;
    /**
     * Attribute which stores the distiguished name(dn) of the isv.
     */
    private String isvDN;
    /**
     * Name of the ISV package to be created. Optional - If not
     * specified will default to owner + productname + version.
     */
    private String name;
    /**
     * Attribute which stores the distiguished name(dn) of the
     * organization.
     */
    private String organization;
    /**
     * Name of the owner of the ISV Package.
     */
    private String owner;
    /**
     * Name of the product that will be contained in the ISV Package.
     */
    private String productname;
    /**
     * The reciever soap processor dn, optional. Can be used in a
     * distributed edition environment , to build the isv locally
     */
    private String receiver = null;
    /**
     * Attribute which stores the distiguished name(dn) of the user.
     */
    private String userdn;
    /**
     * Version of the product being created.
     */
    private String version;
    /**
     * The type of the ISV Package. The value could be 'standard' or
     * 'componentized'.
     */
    private TypeAttribute type;
    /**
     * DOCUMENTME
     */
    private boolean bDebugEnabled = false;
    /**
     * Whether roles has been configured.
     */
    private boolean isRolesConfigured = false;
    /**
     * DOCUMENTME
     */
    private int customContentCount = 0;
    /**
     * The node which represnts the ISV xml which is created
     * dynamically and sent with the request for creating the ISV.
     */
    private int isvNode;

    /*
     * Now custom task with a specified type can be specified only once in nested loop.
     * This should be avoided for tasks such as static & custom.
     * For this, we need to rewrite the index key in contents map with some other value
     */
    /**
     * DOCUMENTME
     */
    private int staticContentCount = 0;

/**
     * Default Constructor
     */
    public ISVCreatorTask()
    {
        super();
    }

    /* ******************Nested Tag configuration methods******************* */
    /**
     * Handles the nested element tag tag name: content attributes
     * supported: type, handler, contentfile
     *
     * @param content The ISVContent object instance passed by ANT after
     *        setting the attributes.
     */
    public void addConfiguredContent(Content content)
    {
        StringBuffer message = new StringBuffer();

        if (contents == null)
        {
            //We need contents in the order of insertion.
            //contents = new HashMap();
            contents = new java.util.LinkedHashMap<String, Content>();
        }

        if ((content.getType() == null) || "".equals(content.getType()))
        {
            message.append("\n").append(PARAMETER_USAGE).append("type");
            message.append(" of nested tag 'content'");
            GeneralUtils.handleException(message.toString());
        }

        content.checkAndSetHandler();

        if (content.getType().equals(Content.TYPE_ROLES))
        {
            isRolesConfigured = true;
        }

        message.append("\n").append("Type: ").append(content.getType());
        message.append("\n").append("Details: ");

        if ((content.getContentFile() == null) ||
                "".equals(content.getContentFile()))
        {
            message.append(PARAMETER_USAGE).append("contentfile");
            message.append(" for nested tag  'content'");
            GeneralUtils.handleException(message.toString());
        }
        else if (!content.getContentFile().exists())
        {
            if (content.isOptional()) {
                return;
            }
            
            message.append("Invalid path specified! Check if path exists.");
            message.append("\n").append("Path:");
            message.append(content.getContentFile().getAbsolutePath());
            GeneralUtils.handleException(message.toString());
        }

        String indexKey = content.getType();

        if ("static".equals(content.getType()))
        {
            indexKey = indexKey + ++staticContentCount;
        }
        else if ("custom".equals(content.getType()))
        {
            indexKey = indexKey + ++customContentCount;
        }

        //System.out.println ("Putting content: " + indexKey);
        contents.put(indexKey, content);
    }

    /**
     * Implementing the abstract method of class Task
     */
    public void execute()
                 throws BuildException
    {
        if (bDebugEnabled)
        {
            Document.setXMLExceptionsEnable(true);
            Document.setLeakInfoBaseline();
        }

        /*
         * Show message if mandatory parameters have not been specified.
         */
        boolean isParameterSet = true;
        StringBuffer message = new StringBuffer();

        //ID-105:We no longer need user credentials for creating ISVP

        /*
           //check for organization.
           if ((organization == null) || (organization.length() == 0))
           {
                   message.append(PARAMETER_USAGE).append("organization").append("\n");
                   isParameterSet = false;
           }
           //check for userdn.
           if ((userdn == null) || (userdn.length() == 0))
           {
                   message.append(PARAMETER_USAGE).append("userdn").append("\n");
                   isParameterSet = false;
           }
         */

        //check for owner.
        if ((owner == null) || (owner.length() == 0))
        {
            message.append(PARAMETER_USAGE).append("owner").append("\n");
            isParameterSet = false;
        }
        else if (checkInvalidCharacters(owner))
        {
            message.append("owner").append(INVALID_CHAR_ERROR).append("\n");
            isParameterSet = false;
        }

        //check for productname.
        if ((productname == null) || (productname.length() == 0))
        {
            message.append(PARAMETER_USAGE).append("productname").append("\n");
            isParameterSet = false;
        }
        else if (checkInvalidCharacters(productname))
        {
            message.append("productname").append(INVALID_CHAR_ERROR).append("\n");
            isParameterSet = false;
        }

        //check for version.
        if ((version == null) || (version.length() == 0))
        {
            message.append(PARAMETER_USAGE).append("version").append("\n");
            isParameterSet = false;
        }
        else if (checkInvalidCharacters(version))
        {
            message.append("version").append(INVALID_CHAR_ERROR).append("\n");
            isParameterSet = false;
        }

        //check for type.
        if (type == null)
        {
            message.append(PARAMETER_USAGE).append("type").append("\n");
            isParameterSet = false;
        }

        //check for name.
        if ((name == null) || (name.length() == 0))
        {
            name = owner + " " + productname + " " + version;
        }

        isvDN = "cn=" + name + "," +
                organization.substring(organization.indexOf(",") + 1,
                                       organization.length());

        //check for filename.
        if ((filename == null) || (filename.length() == 0))
        {
            filename = owner + " " + productname + " " + version;
        }
        else
        {
            Pattern pattern = Pattern.compile("\\.isvp$",
                                              Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(filename);

            if (matcher.find())
            {
                GeneralUtils.handleException("'filename' attribute should not end with '.isvp'");
            }
        }

        //check for eula file.
        if (eulafile != null)
        {
            GeneralUtils.checkDirectoryExists(eulafile);
        }

        //check for eula file.
        if (imagefile != null)
        {
            GeneralUtils.checkDirectoryExists(imagefile);
        }

        //check for dest directory file.
        if (destdir != null)
        {
            if (!destdir.exists())
            {
                destdir.mkdirs();
            }

            if (!destdir.isDirectory())
            {
                GeneralUtils.handleException("The destination directory specified should point to a directory.\n" +
                                             destdir.getAbsolutePath());
            }
        }

        if (!isParameterSet)
        {
            GeneralUtils.handleException(message.toString());
        }
        else
        {
            //With BCP 4.2 Roles is not mandatory.
            //It will be inserted by ISVCreator by default.
            //if (!isRolesConfigured)
            //{
            //	GeneralUtils.handleException("Mandatory <content> tag with type roles is missing.");
            //}
            int requestNode = 0;
            int responseNode = 0;

            // Get the platform version
            String bcpVersionProperty = getProject()
                                            .getProperty(BuildFrameworkProperty.PLATFORM_VERSION_STRING.getName());

            if ((bcpVersionProperty != null) &&
                    (bcpVersionProperty.length() > 0))
            {
                // Try to use version from the project property.
                try
                {
                    versionInfo = BcpVersionInfo.createFromProperty(bcpVersionProperty);
                }
                catch (Exception e)
                {
                    GeneralUtils.handleException("Could not parse the BCP version property: " +
                                                 bcpVersionProperty, e, this);
                }
            }
            else
            {
                // Get the version from the local wcp.jar
                try
                {
                    Version tmp = new Version();
                    String bcpVersion = tmp.getVersion();
                    String bcpBuildNumber = tmp.getBuild();

                    versionInfo = BcpVersionInfo.createFromVersionAndBuildNumber(bcpVersion,
                                                                                 bcpBuildNumber);
                }
                catch (Exception e)
                {
                    GeneralUtils.handleException("Unable to get the BCP version.",
                                                 e, this);
                }
            }

            log("BCP version: " + versionInfo);

            /*
             * Get ready execute the ISV TASK.
             */
            try
            {
                //Code changes for ISVCreation using api rather than soap request
                //IDENTIFICATION number : ID-101
                //ID-101:No need of SoapRequestManager
                //soapRequestMgr = new SoapRequestManager(this);
                //document = soapRequestMgr.getDocument();
                document = new Document();

                //log("Organization used for creating ISV: " + organization);
                //log("User dn for creating ISV: " + userdn);
                //ID-101:No need to check for valid user & organization & role

                /*
                   try
                   {
                           //check whether the organization is valid.
                           ValidationUtil.checkOrganization(organization,
                                                                                            soapRequestMgr);
                           //check whether the user is valid.
                           ValidationUtil.checkUser(userdn, soapRequestMgr);
                   }
                   catch (IllegalArgumentException iae)
                   {
                           GeneralUtils.handleException(iae.getMessage(), iae, this);
                   }
                   //check whether the user sending ISV creation request has developer role attached.
                   int responseNode = soapRequestMgr.makeSoapRequest("cn=SYSTEM,cn=organizational users," +
                                                                                                                     organization,
                                                                                                                     organization,
                                                                                                                     "http://schemas.cordys.com/1.0/ldap",
                                                                                                                     "GetLDAPObject",
                                                                                                                     new String[]
                                                                                                                     {
                                                                                                                             "dn"
                                                                                                                     },
                                                                                                                     new String[]
                                                                                                                     {
                                                                                                                             userdn
                                                                                                                     });
                   //check the response for Soap Fault.
                   GeneralUtils.handleException(responseNode);
                   //match for role nodes.
                   int[] roleNodes = Find.match(responseNode, "?<role><string>");
                   boolean isDeveloper = false;
                   for (int k = 0; k < roleNodes.length; k++)
                   {
                           int roleNode = roleNodes[k];
                           String roleDN = Node.getData(roleNode);
                           if (roleDN.startsWith(DEVELOPER_ROLE_DN))
                           {
                                   isDeveloper = true;
                                   break;
                           }
                   }
                   if (!isDeveloper)
                   {
                           GeneralUtils.handleException("The user specified does not have developer role attached.");
                   }
                 */

                //ID-101:WCP Version has to be taken from the wcp.jar file

                /*
                   //get the WCP version information.
                   responseNode = soapRequestMgr.makeSoapRequest("http://schemas.cordys.com/1.0/monitor",
                                                                                                             "GetVersion", 0);
                   //check the response for Soap Fault.
                   GeneralUtils.handleException(responseNode);
                   //match for status message in the response and display.
                   int versionNode = Find.firstMatch(responseNode,
                                                                                     "?<GetVersionResponse><version>");
                   String wcpVersion = Node.getData(versionNode);
                 */

                //ID-101:WCP Version is fetched from version class
                String wcpVersion = new Version().getVersion();

                //TODO Change here to ISVComponent for creating ISV component.
                isvNode = document.createElement("ISVPackage");

                //TODO Create description tag only for ISVP.

                /*
                 * create the description tag.
                 * <description>
                 *                 <owner>RTF</owner>
                 *                 <name>Radar</name>
                 *                 <version>1.0</version>
                 *                 <eula source=""/>
                 *                 <sidebar source=""/>
                 *                 <cn>RTF Radar 1.0</cn>
                 *                 <wcpversion>1.5</wcpversion>
                 * </description>
                 */
                int descNode = document.createElement("description", isvNode);
                int contentNode = document.createElement("content", isvNode);
                int promptsetNode = document.createElement("promptset", isvNode);
                document.createTextElement("owner", owner, descNode);
                document.createTextElement("name", productname, descNode);
                document.createTextElement("version", version, descNode);
                document.createTextElement("cn", name, descNode);
                document.createTextElement("wcpversion", wcpVersion, descNode);
                document.createTextElement("build", buildNr, descNode);

                //create the eula tag and set the source attribute.
                int eulaNode = document.createElement("eula", descNode);
                Node.setAttribute(eulaNode, "source",
                                  (eulafile == null) ? ""
                                                     : eulafile.getAbsolutePath());

                //create the sidebar tag and set the source attribute.
                int sidebarNode = document.createElement("sidebar", descNode);
                Node.setAttribute(sidebarNode, "source",
                                  (imagefile == null) ? ""
                                                      : imagefile.getAbsolutePath());

                if (contents != null)
                {
                    Set<Entry<String, Content>> entries = contents.entrySet();
                    Iterator<Entry<String, Content>> iter = entries.iterator();

                    while (iter.hasNext())
                    {
                        Entry<String, Content> entry = iter.next();
                        Content content = (Content) entry.getValue();
                        log("---> " + content.getType() + " <---");

                        int[] isvContentNodes;

                        try
                        {
                            isvContentNodes = content.getISVContentXML(this,
                                                                       contentNode,
                                                                       promptsetNode);

                            if (isvContentNodes != null)
                            {
                                for (int i = 0; i < isvContentNodes.length;
                                         i++)
                                {
                                    int iNode = isvContentNodes[i];

                                    if (content.getType()
                                                   .equals(Content.TYPE_PROMPTS))
                                    {
                                        Node.appendToChildren(iNode,
                                                              promptsetNode);
                                    }
                                    else if (content.getType()
                                                        .equals(Content.TYPE_DEPENDENCIES))
                                    {
                                        Node.appendToChildren(iNode, isvNode);
                                    }
                                    else
                                    {
                                        // Only add the content node if it has content (i.e. child nodes).
                                        if (Node.getFirstChild(iNode) != 0) {
                                            Node.appendToChildren(iNode, contentNode);
                                        } else {
                                            Node.delete(iNode);
                                        }
                                    }
                                }
                            }
                        }
                        catch (XMLException xe)
                        {
                            GeneralUtils.handleException("Could not parse the xml file: " +
                                                         content.getContentFile()
                                                                .getAbsolutePath(),
                                                         xe, this);
                        }
                    }
                }

                //ID-101:No soap request is to be fired
                //Finally send the soap request for ISV creation.
                //ISoapRequest soapRequest = soapRequestMgr.createSoapRequest();
                //soapRequest.setUser(userdn);
                int fileNameNode = document.createTextElement("filename",
                                                              filename);

                int[] paramNodes = new int[] { fileNameNode, isvNode };

                //set the timeout of the soap requests to indefinite.
                //soapRequestMgr.setTimeout(0);
                //TODO Change here to CreateISVComponent for creating ISV component.
                // modified by jthomas to allow ISV creation in distributed env
                // directly provide the dn of the monitor soap node

                /*                                soapRequest.addMethod(organization,
                   "http://schemas.cordys.com/1.0/isvpackage",
                   "CreateISVPackage", paramNodes);
                 */

                /*
                   if( receiver == null || "".equals(receiver)) {
                       soapRequest.addMethod(organization,
                                             "http://schemas.cordys.com/1.0/isvpackage",
                                             "CreateISVPackage", paramNodes);
                   }
                   else {
                       // dont resolve the receiver dn
                       soapRequest.addMethod(receiver,organization,
                                             "http://schemas.cordys.com/1.0/isvpackage",
                                             "CreateISVPackage", paramNodes);
                   }
                   responseNode = soapRequest.execute();
                 */

                //				ID-105: If EIBProperties.getInstallDir returns null, Exception is throws
                //				Set installation directory, so that null is not retrurned from ISVPackageMethod
                String installDir = EIBProperties.getInstallDir();

                if (installDir == null)
                {
                    System.setProperty(EIBProperties.INSTALL_DIR,
                                       System.getProperty("user.home") +
                                       System.getProperty("file.separator") +
                                       "Cordys");
                }

                if (bDebugEnabled)
                {
                    log("Cordys Installation Directory: " +
                        EIBProperties.getInstallDir());
                }

                log("Creating ISVPackage ...");
                //				ID-101:Invoke the api exposed to create ISVPackage
                requestNode = getCreateISVPackageRequest(paramNodes);

                ISVCreator isvCreator = new ISVCreator();

                if (destdir != null)
                {
                    isvCreator.setISVPDir(destdir.getAbsolutePath());
                }
                responseNode = isvCreator.createISVP(requestNode, this);

                //check the response for Soap Fault. We have to simulate the SOAP:Envelope here.
                int soapEnvelope = Node.getDocument(responseNode)
                                       .createElement("SOAP:Envelope");
                Node.setAttribute(soapEnvelope, "xmlns:SOAP",
                                  "http://schemas.xmlsoap.org/soap/envelope/");

                int soapBody = Node.createElement("SOAP:Body", soapEnvelope);
                responseNode = Node.appendToChildren(responseNode, soapBody);
                GeneralUtils.handleException(soapEnvelope);

                //match for status message in the response and display.
                int statusNode = Find.firstMatch(responseNode, "?<status>");
                String status = Node.getData(statusNode);
                log("Status: " + status);

                //				ID-101:No copying is required
                //PGUSSOW: Made it so that it can be configured whether or not the ISV needs
                //to be copied to the destination folder.
                String sCopyISV = getCopyisv();

                if ((status.indexOf("successfully") != 0) &&
                        ((sCopyISV != null) &&
                            sCopyISV.toLowerCase().equals("true")))
                {
                    StringBuffer isvPath = new StringBuffer();
                    isvPath.append(EIBProperties.getInstallDir())
                           .append(java.io.File.separator).append("web")
                           .append(java.io.File.separator).append("wcp")
                           .append(java.io.File.separator).append("isvcontent")
                           .append(java.io.File.separator).append("packages")
                           .append(java.io.File.separator).append(filename)
                           .append(".isvp");

                    //TODO Change here for ISVC
                    java.io.File inputFile = new java.io.File(isvPath.toString());

                    StringBuffer outputPath = new StringBuffer();

                    //if the destdir has not been set then take basedir
                    outputPath.append((destdir == null)
                                      ? getProject().getBaseDir()
                                            .getAbsolutePath()
                                      : destdir.getAbsolutePath())
                              .append(java.io.File.separator).append(filename)
                              .append(".isvp");

                    //TODO Change here for ISVC
                    java.io.File outputFile = new java.io.File(outputPath.toString());

                    log("copying the isv file to " + destdir);

                    try
                    {
                        FileUtils.newFileUtils().copyFile(inputFile, outputFile);
                    }
                    catch (FileNotFoundException fnfe)
                    {
                        GeneralUtils.handleException("ISV File not found",
                                                     fnfe, this);
                    }
                    catch (IOException ioe)
                    {
                        GeneralUtils.handleException("Error occured while performing file operations.",
                                                     ioe, this);
                    }
                }
            }
            catch (Exception e)
            {
                if (bDebugEnabled)
                {
                    e.printStackTrace();
                }
                GeneralUtils.handleException("Error occured while creating ISVP (api invocation)\n" +
                                             e.getMessage(), e, this);
            }
            finally
            {
                //TODO: All clean up operations need to be done here			  
                nomCollection.cleanup();
                Node.delete(Node.getRoot(requestNode));
                Node.delete(Node.getRoot(responseNode));

                if (bDebugEnabled)
                {
                    log("Used nodes  : " + document.getNumUsedNodes(false));
                    log("Total memory: " + Runtime.getRuntime().totalMemory());
                    log("Free memory : " + Runtime.getRuntime().freeMemory());
                    log("Leaked NOM Nodes:: " + Document.getLeakInfo(0));
                }
                document = null;
            }
            /*
               catch (SoapRequestException sre)
               {
                       GeneralUtils.handleException("Error occured while sending request to ECX machine.\n" +
                                                                                sre.getMessage(), sre, this);
               }
             */
        }
    }

    /**
     * Returns the build number of the ISV Package.
     *
     * @return The build number of the ISV Package.
     */
    public String getBuild()
    {
        return this.buildNr;
    }

    /**
     * The method provides the Map of all contents used by this task.
     *
     * @return The Mapping of all contents
     */
    public Map<String, Content> getContents()
    {
        return contents;
    }

    /**
     * This method returns whether or not the ISV should be copied to
     * the build-folder.
     *
     * @return Whether or not the ISV should be copied to the build-folder.
     */
    public String getCopyisv()
    {
        return copyisv;
    }

    /**
     * Returns the directory where the ISV is copied after creation.
     *
     * @return The destination directory of the ISV.
     */
    public File getDestdir()
    {
        return destdir;
    }

    /**
     * Returns the document got from the connection.
     *
     * @return The Document used by the ISVCreatorTask
     */
    public Document getDocument()
    {
        return document;
    }

    /**
     * Returns the End User License Agreement File.
     *
     * @return The End User License Agreement File.
     */
    public java.io.File getEulafile()
    {
        return eulafile;
    }

    /**
     * Returns the file name of the ISV Package.
     *
     * @return The file name of the ISV Package.
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * Returns the Image File for the ISV Package.
     *
     * @return The Image File for the ISV Package.
     */
    public java.io.File getImagefile()
    {
        return imagefile;
    }

    /**
     * Method returns the distinguished name of the ISV
     *
     * @return The distinguished name of the ISV to be created by ISV task.
     */
    public String getIsvDN()
    {
        return isvDN;
    }

    /**
     * Returns the name of the ISV Package.
     *
     * @return The name of the ISV Package.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Method returns distinguished name of the organization.
     *
     * @return The distinguished name of the organization used by the ISV task.
     */
    public String getOrganization()
    {
        return organization;
    }

    /**
     * Returns the owner of the ISV Package.
     *
     * @return The owner of the ISV Package.
     */
    public String getOwner()
    {
        return owner;
    }

    /**
     * Returns the product name of the ISV.
     *
     * @return The product name of the ISV.
     */
    public String getProductname()
    {
        return productname;
    }

    /**
     * This method gets the receiver.
     *
     * @return The receiver.
     */
    public String getReceiver()
    {
        return receiver;
    }

    /**
     * The instance of the <code>SoapRequestManager</code> class useful
     * for handling soap request.
     *
     * @return The SoapRequestManager.
     */
    public ISoapRequestManager getSoapRequestMgr()
    {
        return soapRequestMgr;
    }

    /**
     * Returns the type of the ISV Package. Can be either standard or
     * componentized.
     *
     * @return The type of the ISV Package.
     */
    public TypeAttribute getType()
    {
        return type;
    }

    /**
     * Returns the user in whose context the ISV package is created.
     *
     * @return The distinguished name(dn) of the user in the LDAP.
     */
    public String getUserdn()
    {
        return userdn;
    }

    /**
     * Returns the version of the ISV Package.
     *
     * @return The version number.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the versionInfo.
     *
     * @return Returns the versionInfo.
     */
    public BcpVersionInfo getVersionInfo()
    {
        return versionInfo;
    }

    /**
     * Enable debug if debug.isvtask is enabled
     */
    public void init()
              throws BuildException
    {
        super.init();

        String debug = GeneralUtils.getTrimmedProperty(getOwningTarget()
                                                           .getProject(),
                                                       "debug.isvtask");

        if ((debug != null) && !"off".equals(debug) && !"false".equals(debug) &&
                !"no".equals(debug))
        {
            bDebugEnabled = true;
        }

        nomCollection = new NOMHandlesCollection(32);
    }

    /**
     * Returns a booean whether the roles have been configured in the
     * ISV.
     *
     * @return Whether the roles has been configured in the ISV.
     */
    public boolean isRolesConfigured()
    {
        return isRolesConfigured;
    }

    /**
     * Nested tasks can register created nom nodes here. Once execution
     * of  isv task is finished, all the registered nodes will be deleted
     *
     * @param node nom handle
     */
    public void registerNodeForCleanup(int node)
    {
        nomCollection.addHandle(node);
    }

    /**
     * Sets build number of the ISV Package.
     *
     * @param buildNr The build number of the ISV Package.
     */
    public void setBuild(String buildNr)
    {
        this.buildNr = buildNr;
    }

    /**
     * This method sets whether or not the ISV should be copied to the
     * build-folder.
     *
     * @param string Whether or not the ISV should be copied to the
     *        build-folder.
     */
    public void setCopyisv(String string)
    {
        copyisv = string;
    }

    /**
     * Sets the destination directory where the ISV is copied after
     * creation.
     *
     * @param file The destination directory of the ISV.
     */
    public void setDestdir(java.io.File file)
    {
        destdir = file;
    }

    /**
     * Sets the End User License Agreement File for the ISV Package.
     *
     * @param eulafile The End User License Agreement File.
     */
    public void setEulafile(java.io.File eulafile)
    {
        this.eulafile = eulafile;
    }

    /**
     * Sets the file name of the ISV Package.
     *
     * @param filename The file name of the ISV Package.
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**
     * Sets the Image File for the ISV Package.
     *
     * @param imagefile The Image File for the ISV Package.
     */
    public void setImagefile(java.io.File imagefile)
    {
        this.imagefile = imagefile;
    }

    /**
     * Sets the name of the ISV Package.
     *
     * @param name The name of the ISV Package.
     */
    public void setName(String name)
    {
        this.name = ((name != null) && (name.length() > 0)) ? name.trim() : null;
    }

    /**
     * Sets the organization in which the ISV is created.
     *
     * @param org The distinguished name(dn) of the organization.
     */
    public void setOrganization(String org)
    {
        organization = ((org != null) && (org.length() > 0)) ? org.trim() : null;
    }

    /**
     * Sets the owner of the ISV Package.
     *
     * @param owner The owner of the ISV Package.
     */
    public void setOwner(String owner)
    {
        this.owner = ((owner != null) && (owner.length() > 0)) ? owner.trim()
                                                               : null;
    }

    /**
     * Sets the product name of the ISV to be created.
     *
     * @param string The product name of the ISV.
     */
    public void setProductname(String string)
    {
        productname = string;
    }

    /**
     * DOCUMENTME
     *
     * @param receiver DOCUMENTME
     */
    public void setReceiver(String receiver)
    {
        this.receiver = receiver;
    }

    /**
     * Sets the Side Bar Image to be used by ISV.
     *
     * @param sidebar The Side Bar Image File.
     */
    public void setSidebar(java.io.File sidebar)
    {
        this.imagefile = sidebar;
    }

    /**
     * Sets type of the ISV Package. Can take two values  standard or
     * componentized.
     *
     * @param type The type of the ISV Package.
     */
    public void setType(TypeAttribute type)
    {
        this.type = type;
    }

    /**
     * Sets the user in whose context the ISV package will be created.
     *
     * @param userdn The distinguished name(dn) of the user in the LDAP.
     */
    public void setUserdn(String userdn)
    {
        this.userdn = userdn;
    }

    /**
     * Sets the version of the ISV Package.
     *
     * @param version The version number.
     */
    public void setVersion(String version)
    {
        this.version = ((version != null) && (version.length() > 0))
                       ? version.trim() : null;
    }

    /**
     * method for checking invalid characters in the String passed.
     *
     * @param string The string which has to checked.
     *
     * @return The result of the check.
     */
    private boolean checkInvalidCharacters(String string)
    {
        Pattern pattern = Pattern.compile(INVALID_CHAR_REGEXP);
        Matcher matcher = pattern.matcher(string);
        boolean found = false;

        while (matcher.find())
        {
            found = true;
        }

        return found;
    }

    /**
     * DOCUMENTME
     *
     * @param nodes DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private int getCreateISVPackageRequest(int[] nodes)
    {
        int body = document.createElement("SOAP:Body"); //Required for BodyBlock
        int node = document.createElement("CreateISVPackage");
        Node.appendToChildren(node, body);
        Node.setAttribute(node, "xmlns",
                          "http://schemas.cordys.com/1.0/isvpackage");

        for (int i = 0; i < nodes.length; i++)
        {
            Node.appendToChildren(nodes[i], node);
        }
        return node;
    }

    /**
     * Inner class which handles the attribute 'type' Enumerated
     * attribute with the values "standard"  and "componentized". Used to
     * enumerate options for 'type' attribute.
     */
    public static class TypeAttribute extends EnumeratedAttribute
    {
        /**
         * The standard ISV package type.
         */
        static final String STANDARD = "standard";
        /**
         * The componentized ISV package type.
         */
        static final String COMPONENTIZED = "componentized";

        /**
         * Implementing the getValues method
         *
         * @see org.apache.tools.ant.types.EnumeratedAttribute#getValues()
         */
        public String[] getValues()
        {
            return new String[] { STANDARD, COMPONENTIZED };
        }
    }
}
