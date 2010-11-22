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

import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * The class handles the content of type roles. This extends most of the
 * features used commonly in LDAP kind of contents from the Class
 * <code>LDAPHelper</code>.
 *
 * @author msreejit
 */
public class RolesHandler extends LDAPHelper
    implements ContentHandler
{
    /**
     * Message logged when no content is found to import from ECX
     */
    private static final String NO_IMPORT_CONTENT_FOUND = "No roles found to import from ECX.";
    /**
     * Message logged when no content is found to export from ECX
     */
    private static final String NO_EXPORT_CONTENT_FOUND = "No roles found to export from ECX.";
    /**
     * Message logged when no content is found to delete from ECX
     */
    private static final String NO_DELETE_CONTENT_FOUND = "No roles found in ECX to delete.";
    /**
     * Message logged when import from ECX is successful
     */
    private static final String ECX_TO_FILE_SUCCESS = "Successfully exported roles from ECX.";
    /**
     * Message logged when export from ECX is successful
     */
    private static final String FILE_TO_ECX_SUCCESS = "Successfully imported roles to ECX.";
    /**
     * Message logged when delete from ECX is successful
     */
    private static final String DELETE_SUCCESS = "Successfully deleted roles from ECX.";
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
    public RolesHandler()
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
     * Deletes Roles information from the organization
     *
     * @param organization organimzation dn
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @throws SoapRequestException
     */
    public void deleteRoles(String organization, ContentManagerTask task,
                            ISoapRequestManager soapRequestMgr)
                     throws SoapRequestException
    {
        int deleteCount = 0;

        /*
         * Creates the following request inorder to get the roles from the organization
         *  <GetRolesForOrganization xmlns="http://schemas.cordys.com/1.0/ldap">
         *          <dn>o=CRTM Apps,cn=cordys,o=vanenburg.com</dn>
         *          <sort>ascending</sort>
         *  </GetRolesForOrganization>
         */
        String[] paramNames = { "dn", "labeleduri", "sort" };
        String[] paramValues = { organization, "*", "ascending" };

        //Making a request to get the roles from the ldap
        int responseNode = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                          task.getOrganization(),
                                                          "http://schemas.cordys.com/1.0/ldap",
                                                          "GetRolesForOrganization",
                                                          paramNames,
                                                          paramValues);

        //check the response for Soap Fault.
        GeneralUtils.handleException(responseNode);

        //match for all roles's dn
        int[] tupleNodes = Find.match(responseNode,
                                      "?<GetRolesForOrganizationResponse><tuple>");
        String everyOneInOrgCN = "everyoneIn" +
                                 (organization.split(",")[0]).split("=")[1];

        int everyoneInOrgRoleNode = Find.firstMatch(responseNode,
                                                    "?<entry><cn><string>\"" +
                                                    everyOneInOrgCN + "\"");

        if (everyoneInOrgRoleNode != 0)
        {
            for (int j = 0; j < tupleNodes.length; j++)
            {
                int cnNode = Find.firstMatch(tupleNodes[j],
                                             "?<entry><cn><string>");

                if (everyOneInOrgCN.equals(Node.getData(cnNode)))
                {
                    task.log("Ignoring organizational role..." +
                             everyOneInOrgCN, Project.MSG_INFO);
                    tupleNodes[j] = 0;
                }
                else
                {
                    deleteCount++;
                }
            }
        }
        else
        {
            deleteCount = tupleNodes.length;
        }

        tupleNodes = filterLdapContents(tupleNodes, cCurrentContent);

        if (tupleNodes.length > 0)
        {
            //If the organization role was only found and set to 0, don't send request.
            if ((tupleNodes.length == 1) && (tupleNodes[0] == 0))
            {
                //Ignoring organizational role...
            }
            else
            {
                //Making a request to delete roles from the ldap
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
            task.log("Deleting roles in LDAP...", Project.MSG_INFO);

            deleteRoles(task.getOrganization(), task, soapRequestManager);
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
        String rolesContent = "";

        try
        {
            //if isvdn has been specified execute related method.
            if (task.getIsvdn() != null)
            {
                rolesContent = getRoles(task.getIsvdn(), task,
                                        soapRequestManager);
            }
            else
            {
                rolesContent = getRoles(task.getOrganization(), task,
                                        soapRequestManager);
            }

            if (rolesContent.equals(""))
            {
                task.log(NO_EXPORT_CONTENT_FOUND, Project.MSG_INFO);

                return;
            }

            stringBuffer.append("<roles>");
            stringBuffer.append(rolesContent);
            stringBuffer.append("</roles>");

            InputStream xslStream = FileUtil.getResourceAsStream(RolesHandler.class,
                                                                 "xsl/ROLES_TO_DEV.xsl");

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

            //check if the directory has been specified.
            if (content.getDir() != null)
            {
                File xmlFile = null;
                File contentDir = content.getDir();

                //match for all roles.
                int[] roleNodes = Find.match(developerContentNode,
                                             "<roles><role>");

                //make the directory structure if not existing.
                if (!contentDir.exists())
                {
                    contentDir.mkdirs();
                }

                //write each role to a file.
                for (int iCount = 0; iCount < roleNodes.length; iCount++)
                {
                    task.log("[" + content.getType() + "] Writing role " +
                             Node.getAttribute(roleNodes[iCount], "name") +
                             " to local filesystem.", Project.MSG_INFO);
                    String roleContent;
                    
                    // Convert the encoded XML string into XML structure.
                    XMLUtils.iterateAllCDATAToXML(roleNodes[iCount]);

                    roleContent = XMLUtils.XML_FILE_PREFIX +
                                  Node.writeToString(roleNodes[iCount], true);

                    xmlFile = new File(contentDir,
                                       Node.getAttribute(roleNodes[iCount],
                                                         "name") + ".xml");

                    task.log("Writing role to file:" +
                             xmlFile.getAbsolutePath(), Project.MSG_VERBOSE);

                    FileUtil.writeToFile(roleContent, xmlFile.getAbsolutePath());
                }
            }

            if (content.getContentFile() != null)
            {
                String roleContent;

                roleContent = XMLUtils.XML_FILE_PREFIX +
                              Node.writeToString(developerContentNode, true);

                task.log("Writing content to file:" +
                         content.getContentFile().getAbsolutePath(),
                         Project.MSG_INFO);

                //write the roles content to a flat file.
                FileUtil.writeToFile(roleContent,
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

            InputStream xslStream = FileUtil.getResourceAsStream(RolesHandler.class,
                                                                 "xsl/DEV_TO_ROLES.xsl");

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

                String developerContents = extractLDAPContents(fss, "roles",
                                                               "role", task,
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
                                         "<roles><role><acl><acobjecttree>");

            //Now we'll write which roles are in this file.
            int[] aiRoles = Find.match(xmlFileNode, "<roles><role>");

            for (int iCount = 0; iCount < aiRoles.length; iCount++)
            {
                String sRoleName = Node.getAttribute(aiRoles[iCount], "name");

                if (!cCurrentContent.isPathAccepted(sRoleName))
                {
                    Node.delete(aiRoles[iCount]);
                    aiRoles[iCount] = 0;
                    continue;
                }

                // Fix the organization DN in subroles.
                int[] xaSubRoles = Find.match(aiRoles[iCount], "<><sub-role>");

                if (xaSubRoles != null)
                {
                    for (int i = 0; i < xaSubRoles.length; i++)
                    {
                        int xSubRole = xaSubRoles[i];
                        String sValue = Node.getData(xSubRole);

                        if (sValue != null)
                        {
                            sValue = LdapUtils.replaceRoleDn(sValue,
                                                             soapRequestManager.getOrganizationDN());
                            Node.setDataElement(xSubRole, "", sValue);
                        }
                    }
                }

                task.log("[" + content.getType() + "] Synchronizing role " +
                         sRoleName, Project.MSG_INFO);
                addProcessItem();
            }

            //apply the final xsl transformation to generate the update format. 
            xmlNode = XSLUtil.getXSLTransformAsNode(Node.writeToString(xmlFileNode,
                                                                       true),
                                                    xslStream,
                                                    new String[] { "orgDN" },
                                                    new String[]
                                                    {
                                                        task.getOrganization()
                                                    });

            task.log("Updating roles in LDAP...", Project.MSG_INFO);

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
     * Get Roles information from the organization specified by the
     * organization dn and writes it to an xml file
     *
     * @param dn The distinguished name(DN) of the organization or ISV Package.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @return The roles content xml with entry tags.
     *
     * @throws SoapRequestException
     */
    public String getRoles(String dn, ContentManagerTask task,
                           ISoapRequestManager soapRequestMgr)
                    throws SoapRequestException
    {
        String methodName;
        String roleContent = "";

        /*
         * Creates the following request inorder to get the roles from the ISV
         * <GetRolesForSoftwarePackage xmlns="http://schemas.cordys.com/1.0/ldap">
         *                 <dn>cn=Cordys BSF Beta,cn=cordys,o=vanenburg.com</dn>
         *                 <sort>ascending</sort>
         *         </GetRolesForSoftwarePackage>
         *
         * Creates the following request inorder to get the roles from the organization
         *  <GetRolesForOrganization xmlns="http://schemas.cordys.com/1.0/ldap">
         *          <dn>o=CRTM Apps,cn=cordys,o=vanenburg.com</dn>
         *          <sort>ascending</sort>
         *  </GetRolesForOrganization>
         */
        methodName = dn.startsWith("cn=") ? "GetRolesForSoftwarePackage"
                                          : "GetRolesForOrganization";

        String[] paramNames = { "dn", "labeleduri", "sort" };
        String[] paramValues = { dn, "*", "ascending" };

        //Making a request to get the method sets from the ldap
        int rolesResponse = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                           null,
                                                           "http://schemas.cordys.com/1.0/ldap",
                                                           methodName,
                                                           paramNames,
                                                           paramValues);

        //check the response for Soap Fault.
        GeneralUtils.handleException(rolesResponse);

        if (rolesResponse != 0)
        {
            roleContent = getLDAPEntries(rolesResponse, task, cCurrentContent,
                                         soapRequestMgr);
        }

        return roleContent;
    }
}
