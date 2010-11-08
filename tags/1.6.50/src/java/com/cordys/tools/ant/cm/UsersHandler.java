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

package com.cordys.tools.ant.cm;

import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.FileUtil;
import com.cordys.tools.ant.util.GeneralUtils;
import com.cordys.tools.ant.util.LdapUtils;
import com.cordys.tools.ant.util.XMLUtils;
import com.cordys.tools.ant.util.XSLUtil;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * The class handles the content of type users. This extends most of the
 * features used commonly in LDAP kind of contents from the Class
 * <code>LDAPHelper</code>.
 *
 * @author msreejit
 */
public class UsersHandler extends LDAPHelper
    implements ContentHandler
{
    /**
     * Message logged when no content is found to import from ECX
     */
    private static final String NO_IMPORT_CONTENT_FOUND = "No users found to import from ECX.";
    /**
     * Message logged when no content is found to export from ECX
     */
    private static final String NO_EXPORT_CONTENT_FOUND = "No users found to export from ECX.";
    /**
     * Message logged when no content is found to delete from ECX
     */
    private static final String NO_DELETE_CONTENT_FOUND = "No users found in ECX to delete.";
    /**
     * Message logged when import from ECX is successful
     */
    private static final String ECX_TO_FILE_SUCCESS = "Successfully exported users from ECX.";
    /**
     * Message logged when export from ECX is successful
     */
    private static final String FILE_TO_ECX_SUCCESS = "Successfully imported users to ECX.";
    /**
     * Message logged when delete from ECX is successful
     */
    private static final String DELETE_SUCCESS = "Successfully deleted users from ECX.";
    /**
     * Contains the Content element that this class is currently
     * processing.
     */
    protected Content cCurrentContent;
    /**
     * Number of processed items.
     */
    protected int iProcessedCounter;

    /**
     * Default Constructor
     */
    public UsersHandler()
    {
        super();
    }

    /**
     * Increments the number of processed items.
     */
    public void addProcessItem()
    {
        iProcessedCounter++;
    }

    /**
     * Deletes users from the organization
     *
     * @param organization organimzation dn
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @throws SoapRequestException
     */
    public void deleteUsers(String organization, ContentManagerTask task,
                            ISoapRequestManager soapRequestMgr)
                     throws SoapRequestException
    {
        int deleteCount = 0;

        /*
         * Creates the following request in order to get the users
         *   <GetOrganizationalUsers xmlns="http://schemas.cordys.com/1.1/ldap">
         *      <dn>o=test2,cn=cordys,o=localdomain</dn>
         *      <filter>*</filter>
         *      <sort>ascending</sort>
         *  </GetOrganizationalUsers>
         */
        String[] paramNames = { "dn", "labeleduri", "sort" };
        String[] paramValues = { organization, "*", "ascending" };

        //Making a request to get the users from the ldap
        int responseNode = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                          task.getOrganization(),
                                                          "http://schemas.cordys.com/1.0/ldap",
                                                          "GetOrganizationalUsers",
                                                          paramNames,
                                                          paramValues);

        //check the response for Soap Fault.
        GeneralUtils.handleException(responseNode);

        //match for all user dn's
        int[] tupleNodes = Find.match(responseNode,
                                      "?<GetOrganizationalUsersResponse><tuple>");

        for (int j = 0; j < tupleNodes.length; j++)
        {
            int systemUserNode = Find.firstMatch(tupleNodes[j], "<><><entry><cn><string>\"SYSTEM\"");
            
            if (systemUserNode != 0) {
                task.log("Ignoring SYSTEM user...", Project.MSG_INFO);
                tupleNodes[j] = 0;
            }
            else
            {
                deleteCount++;
            }
        }

        tupleNodes = filterLdapContents(tupleNodes, cCurrentContent);

        if (tupleNodes.length > 0)
        {
            //If the organization user was only found and set to 0, don't send request.
            if ((tupleNodes.length == 1) && (tupleNodes[0] == 0))
            {
                //Ignoring organizational user...
            }
            else
            {
                //Making a request to delete users from the ldap
                responseNode = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                              task.getOrganization(),
                                                              "http://schemas.cordys.com/1.0/ldap",
                                                              "DeleteRecursive",
                                                              tupleNodes);

                //check the response for Soap Fault.
                GeneralUtils.handleException(responseNode);
            }
        }

        task.log((deleteCount == 0) ? NO_DELETE_CONTENT_FOUND : DELETE_SUCCESS,
                 Project.MSG_INFO);
    }

    /**
     * Implementing the abstract method.
     *
     * @param task DOCUMENTME
     * @param content DOCUMENTME
     * @param soapRequestManager DOCUMENTME
     */
    public void executeDelete(ContentManagerTask task, Content content,
                              ISoapRequestManager soapRequestManager)
    {
        cCurrentContent = content;

        try
        {
            task.log("Deleting users in LDAP...", Project.MSG_INFO);

            deleteUsers(task.getOrganization(), task, soapRequestManager);
        }
        catch (SoapRequestException sre)
        {
            GeneralUtils.handleException("Error occured while sending request to ECX.\n" +
                                         sre.getMessage(), sre, task);
        }
    }

    /**
     * Implementing the abstract method.
     *
     * @param task DOCUMENTME
     * @param content DOCUMENTME
     * @param soapRequestManager DOCUMENTME
     */
    public void executeEcxToFile(ContentManagerTask task, Content content,
                                 ISoapRequestManager soapRequestManager)
    {
        cCurrentContent = content;

        StringBuffer stringBuffer = new StringBuffer();
        String usersContent = "";

        try
        {
            //if isvdn has been specified execute related method.
            if (task.getIsvdn() != null)
            {
                usersContent = getUsers(task.getIsvdn(), task,
                                        soapRequestManager);
            }
            else
            {
                usersContent = getUsers(task.getOrganization(), task,
                                        soapRequestManager);
            }

            if (usersContent.equals(""))
            {
                task.log(NO_EXPORT_CONTENT_FOUND, Project.MSG_INFO);

                return;
            }

            stringBuffer.append("<users>");
            stringBuffer.append(usersContent);
            stringBuffer.append("</users>");

            InputStream xslStream = FileUtil.getResourceAsStream(UsersHandler.class,
                                                                 "xsl/USERS_TO_DEV.xsl");

            int developerContentNode = XSLUtil.getXSLTransformAsNode(stringBuffer.toString(),
                                                                     xslStream,
                                                                     new String[]
                                                                     {
                                                                         "orgDN"
                                                                     },
                                                                     new String[]
                                                                     {
                                                                         task.getOrganization()
                                                                     });

            String userContent = "";

            //check if the directory has been specified.
            if (content.getDir() != null)
            {
                File xmlFile = null;
                File contentDir = content.getDir();

                //match for all users.
                int[] userNodes = Find.match(developerContentNode,
                                             "<users><user>");

                //make the directory structure if not existing.
                if (!contentDir.exists())
                {
                    contentDir.mkdirs();
                }

                //write each user to a file.
                for (int iCount = 0; iCount < userNodes.length; iCount++)
                {
                    String name = Node.getAttribute(userNodes[iCount], "name");
                    String authname = Node.getAttribute(userNodes[iCount], "authname");
                    
                    if (authname != null) {
                        authname = authname.replaceFirst("^cn=([^,]*),cn=authenticated users,.*$", "$1");
                        Node.setAttribute(userNodes[iCount], "authname", authname);
                    }
                    
                    if ("SYSTEM".equals(name)) {
                        task.log("[" + content.getType() + "] Ignoring the SYSTEM user.", Project.MSG_INFO);
                        continue;
                    }
                    
                    task.log("[" + content.getType() + "] Writing user " + name +
                             " to local filesystem.", Project.MSG_INFO);
                    //convert all non readable xml format to readable format.
                    userContent = XMLUtils.CDATAToXML(Node.writeToString(userNodes[iCount],
                                                                         true));
                    userContent = XMLUtils.XML_FILE_PREFIX +
                                  XMLUtils.formatToNiceXML(userContent);

                    xmlFile = new File(contentDir,
                                       Node.getAttribute(userNodes[iCount],
                                                         "name") + ".xml");

                    task.log("Writing user to file:" +
                             xmlFile.getAbsolutePath(), Project.MSG_VERBOSE);

                    FileUtil.writeToFile(userContent, xmlFile.getAbsolutePath());
                }
            }

            if (content.getContentFile() != null)
            {
                //convert all non readable xml format to readable format.
                userContent = XMLUtils.CDATAToXML(Node.writeToString(developerContentNode,
                                                                     true));
                userContent = XMLUtils.XML_FILE_PREFIX +
                              XMLUtils.formatToNiceXML(userContent);

                task.log("Writing content to file:" +
                         content.getContentFile().getAbsolutePath(),
                         Project.MSG_INFO);

                //write the users content to a flat file.
                FileUtil.writeToFile(userContent,
                                     content.getContentFile().getAbsolutePath());
            }

            task.log("[" + content.getType() + "] " + ECX_TO_FILE_SUCCESS,
                     Project.MSG_INFO);
        }
        catch (UnsupportedEncodingException ue)
        {
            GeneralUtils.handleException("Error occured while performing xml operation.\n" +
                                         ue.getMessage() +
                                         "\nStacktrace of the error:\n" +
                                         GeneralUtils.getStackTrace(ue), ue,
                                         task);
        }
        catch (XMLException xe)
        {
            GeneralUtils.handleException("Error occured while performing xml file operation.\n" +
                                         xe.getMessage() +
                                         "\nStacktrace of the error:\n" +
                                         GeneralUtils.getStackTrace(xe), xe,
                                         task);
        }
        catch (TransformerException te)
        {
            GeneralUtils.handleException("Error occured while applying xsl transforms.\n" +
                                         te.getMessage() +
                                         "\nStacktrace of the error:\n" +
                                         GeneralUtils.getStackTrace(te), te,
                                         task);
        }
        catch (SoapRequestException sre)
        {
            GeneralUtils.handleException("Error occured while sending request to ECX.\n" +
                                         sre.getMessage() +
                                         "\nStacktrace of the error:\n" +
                                         GeneralUtils.getStackTrace(sre), sre,
                                         task);
        }
        catch (IOException ioe)
        {
            GeneralUtils.handleException("Error occured while performing file operation.\n" +
                                         ioe.getMessage() +
                                         "\nStacktrace of the error:\n" +
                                         GeneralUtils.getStackTrace(ioe), ioe,
                                         task);
        }
    }

    /**
     * Implementing the abstract method.
     *
     * @param task DOCUMENTME
     * @param content DOCUMENTME
     * @param soapRequestManager DOCUMENTME
     */
    public void executeFileToEcx(ContentManagerTask task, Content content,
                                 ISoapRequestManager soapRequestManager)
    {
        cCurrentContent = content;

        int xmlFileNode = 0;
        int xmlNode = 0;
        Document document = null;

        try
        {
            document = soapRequestManager.getDocument();

            InputStream xslStream = FileUtil.getResourceAsStream(UsersHandler.class,
                                                                 "xsl/DEV_TO_USERS.xsl");

            //check if the file has been specified.
            if (content.getContentFile() != null)
            {
                if (!content.getContentFile().exists())
                {
                    GeneralUtils.handleException("File does not exist!\nFile:" +
                                                 content.getContentFile()
                                                        .getAbsolutePath());
                }

                //load the developer content xml file. 
                xmlFileNode = soapRequestManager.getDocument()
                                                .load(content.getDir()
                                                             .getAbsolutePath());
            }
            else
            {
                // collect filesets to pass them to extractLDAPContents
                Vector<?> filesets = content.getFileSet();
                Vector<FileSet> vfss = new Vector<FileSet>();

                if (content.getDir() != null)
                {
                    if (!content.getDir().exists())
                    {
                        GeneralUtils.handleException("Folder does not exist!\nFolder:" +
                                                     content.getContentFile()
                                                            .getAbsolutePath());
                    }

                    FileSet fs = (FileSet) content.getImplicitFileSetUsed()
                                                  .clone();
                    fs.setDir(content.getDir());
                    vfss.addElement(fs);
                }

                for (int i = 0; i < filesets.size(); i++)
                {
                    FileSet fs = (FileSet) filesets.elementAt(i);
                    vfss.addElement(fs);
                }

                FileSet[] fss = new FileSet[vfss.size()];
                vfss.copyInto(fss);

                String developerContents = extractLDAPContents(fss, "users",
                                                               "user", task,
                                                               content);

                if (developerContents.equals(""))
                {
                    task.log(NO_IMPORT_CONTENT_FOUND, Project.MSG_INFO);

                    return;
                }

                //load the developer content xml file. 
                xmlFileNode = document.parseString(developerContents);
            }

            //convert children of specific nodes to string format
            XMLUtils.convertNodeToString(xmlFileNode,
                                         "<users><user><acl><acobjecttree>");

            //Now we'll write which users are in this file.
            int[] aiUsers = Find.match(xmlFileNode, "<users><user>");

            for (int iCount = 0; iCount < aiUsers.length; iCount++)
            {
                String sUserName = Node.getAttribute(aiUsers[iCount], "name");
                String sAuthUserName = Node.getAttribute(aiUsers[iCount], "authname");

                if (!cCurrentContent.isPathAccepted(sUserName))
                {
                    Node.delete(aiUsers[iCount]);
                    aiUsers[iCount] = 0;
                    continue;
                }
                
                if (sAuthUserName != null) {
                    sAuthUserName = "cn=" + sAuthUserName + ",cn=authenticated users," + soapRequestManager.getLdapRoot();
                    Node.setAttribute(aiUsers[iCount], "authname", sAuthUserName);
                }

                // Fix the organization DN in roles.
                int[] xaRoles = Find.match(aiUsers[iCount], "<><role>");

                if (xaRoles != null)
                {
                    for (int i = 0; i < xaRoles.length; i++)
                    {
                        int xSubRole = xaRoles[i];
                        String sValue = Node.getData(xSubRole);

                        if (sValue != null)
                        {
                            sValue = LdapUtils.replaceRoleDn(sValue,
                                                             soapRequestManager.getOrganizationDN());
                            Node.setDataElement(xSubRole, "", sValue);
                        }
                    }
                }
                
                // Fix the organization DN in ACL.
                int xaACL = Find.firstMatch(aiUsers[iCount], "<><acl>");

                if (xaACL != 0) {
                    String sValue = Node.getAttribute(xaACL, "acl");

                    if (sValue != null)
                    {
                        sValue = LdapUtils.replaceOrgUserDn(sValue,
                                                            soapRequestManager.getOrganizationDN());
                        Node.setAttribute(xaACL, "acl", sValue);
                    }
                }                

                task.log("[" + content.getType() + "] Synchronizing user " +
                         sUserName, Project.MSG_INFO);
            }

            //apply the final xsl transformation to generate the update format. 
            xmlNode = XSLUtil.getXSLTransformAsNode(Node.writeToString(xmlFileNode,
                                                                       true),
                                                    xslStream,
                                                    new String[] { "orgDN", "ldapRootDN" },
                                                    new String[]
                                                    {
                                                        task.getOrganization(),
                                                        soapRequestManager.getLdapRoot(),
                                                    });

            task.log("Updating users in LDAP...", Project.MSG_INFO);

            //send the xml for update request.
            findOldUpdateLDAPEntries(xmlNode, task, soapRequestManager);
            updateLDAPEntry(xmlNode, task, soapRequestManager);

            task.log(FILE_TO_ECX_SUCCESS, Project.MSG_INFO);
        }
        catch (UnsupportedEncodingException ue)
        {
            GeneralUtils.handleException("Error occured while performing xml operation.\n" +
                                         ue.getMessage(), ue, task);
        }
        catch (XMLException xe)
        {
            GeneralUtils.handleException("Error occured while performing xml file operation.\n" +
                                         xe.getMessage(), xe, task);
        }
        catch (TransformerException te)
        {
            GeneralUtils.handleException("Error occured while applying xsl transforms.\n" +
                                         te.getMessage(), te, task);
        }
        catch (SoapRequestException sre)
        {
            GeneralUtils.handleException("Error occured while sending request to ECX.\n" +
                                         sre.getMessage(), sre, task);
        }
        catch (IOException ioe)
        {
            GeneralUtils.handleException("Error occured while performing file operation.\n" +
                                         ioe.getMessage(), ioe, task);
        }
    }

    /**
     * This method should take care of publishing the specific content
     * to runtime. This content does not need any publishing.
     *
     * @param cmtTask The current contentmanager task.
     * @param cContent The specific content that needs to be published.
     * @param srmSoap The object to use for sending soap messages.
     */
    public void executePublishToRuntime(ContentManagerTask cmtTask,
                                        Content cContent,
                                        ISoapRequestManager srmSoap)
    {
        cCurrentContent = cContent;

        //This target does not need any publishing.
        cmtTask.log("Content of type " + cContent.getType() +
                    " does not need any publishing to runtime.",
                    Project.MSG_INFO);
    }

    /**
     * 
     * @see com.cordys.tools.ant.cm.ContentHandler#getNumberOfProcessedItems()
     */
    public int getNumberOfProcessedItems()
    {
        return iProcessedCounter;
    }

    /**
     * Get users information from the organization specified by the
     * organization dn and writes it to an xml file
     *
     * @param dn The distinguished name(DN) of the organization or ISV Package.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @return The users content xml with entry tags.
     *
     * @throws SoapRequestException
     */
    public String getUsers(String dn, ContentManagerTask task,
                           ISoapRequestManager soapRequestMgr)
                    throws SoapRequestException
    {
        String methodName;
        String userContent = "";

        /*
         * Creates the following request in order to get the users
         *   <GetOrganizationalUsers xmlns="http://schemas.cordys.com/1.1/ldap">
         *      <dn>o=test2,cn=cordys,o=localdomain</dn>
         *      <filter>*</filter>
         *      <sort>ascending</sort>
         *  </GetOrganizationalUsers>
         */
        if (dn.startsWith("cn=")) {
            return userContent;
        }
        
        methodName = "GetOrganizationalUsers";

        String[] paramNames = { "dn", "filter", "sort" };
        String[] paramValues = { dn, "*", "ascending" };

        //Making a request to get the method sets from the ldap
        int usersResponse = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                           null,
                                                           "http://schemas.cordys.com/1.0/ldap",
                                                           methodName,
                                                           paramNames,
                                                           paramValues);

        //check the response for Soap Fault.
        GeneralUtils.handleException(usersResponse);

        if (usersResponse != 0)
        {
            getAuthenticatedUsers(usersResponse, task, soapRequestMgr);

            userContent = getLDAPEntries(usersResponse, task, cCurrentContent,
                                         soapRequestMgr);
        }

        return userContent;
    }
    
    /**
     * Fetches authenticated user information for each organizational user entry.
     * The contents of the user entry element 'authenticationuser' is replaced with
     * the authenticated user entry.
     * @param usersRoot Root node cotaining the tuple/old/entry elements.
     * @throws SoapRequestException 
     */
    private void getAuthenticatedUsers(int usersRoot, ContentManagerTask task, ISoapRequestManager soapRequestMgr) throws SoapRequestException {
        String[] paramNames = { "dn", "filter", "sort" };
        String[] paramValues = { soapRequestMgr.getLdapRoot(), "*", "ascending" };

        //Making a request to get the method sets from the ldap
        int authUsersResponse = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                           null,
                                                           "http://schemas.cordys.com/1.0/ldap",
                                                           "GetAuthenticatedUsers",
                                                           paramNames,
                                                           paramValues);

        //check the response for Soap Fault.
        GeneralUtils.handleException(authUsersResponse);
        
        Map<String, Integer> authUserMap = new HashMap<String, Integer>();
        int[] authUserEntries = Find.match(authUsersResponse, "?<tuple><old><entry>");
        
        for (int entry : authUserEntries)
        {
            String dn = Node.getAttribute(entry, "dn", "");
            
            if (dn.length() == 0) {
                continue;
            }
            
            authUserMap.put(dn, entry);
        }
        
        int[] userEntries = Find.match(usersRoot, "?<tuple><old><entry><authenticationuser>");
        
        for (int entry : userEntries)
        {
            String dn = Node.getDataElement(entry, "string", "");
            
            if (dn.length() == 0) {
                continue;
            }
            
            if (! authUserMap.containsKey(dn)) {
                throw new IllegalArgumentException("Authenticated user not found with DN: " + dn);
            }
            
            int auth = authUserMap.get(dn);
            
            for (int child = Node.getFirstChild(entry); child != 0; ) {
                int next = Node.getNextSibling(child);
                
                Node.delete(child);
                child = next;
            }
            
            Node.appendToChildren(Node.clone(auth, true), entry);
        }
    }
}
