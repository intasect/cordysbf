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

import com.cordys.coe.util.xml.XMLHelpers;

import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.FileUtil;
import com.cordys.tools.ant.util.GeneralUtils;
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

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * The class handles the content of type soapnodes. This extends most of
 * the features used commonly in LDAP kind of contents from the Class
 * <code>LDAPHelper</code>.
 *
 * @author msreejit
 */
public class SoapNodesHandler extends LDAPHelper
    implements ContentHandler
{
    /**
     * Message logged when no content is found to import from ECX
     */
    private static final String NO_IMPORT_CONTENT_FOUND = "No SoapNodes found to import to ECX.";
    /**
     * Message logged when no content is found to export from ECX
     */
    private static final String NO_EXPORT_CONTENT_FOUND = "No SoapNodes found to export from ECX.";
    /**
     * Message logged when no content is found to delete from ECX
     */
    private static final String NO_DELETE_CONTENT_FOUND = "No SoapNodes found in ECX to delete.";
    /**
     * Message logged when import from ECX is successful
     */
    private static final String ECX_TO_FILE_SUCCESS = "Successfully exported SoapNodes from ECX.";
    /**
     * Message logged when export from ECX is successful
     */
    private static final String FILE_TO_ECX_SUCCESS = "Successfully imported SoapNodes to ECX.";
    /**
     * Message logged when delete from ECX is successful
     */
    private static final String DELETE_SUCCESS = "Successfully deleted SoapNodes from ECX.";
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
    public SoapNodesHandler()
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
     * Deletes SoapNode information from the organization
     *
     * @param organization organimzation dn
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @throws SoapRequestException
     */
    public void deleteSoapNodes(String organization, ContentManagerTask task,
                                ISoapRequestManager soapRequestMgr)
                         throws SoapRequestException
    {
        int deleteCount = 0;

        /*
         * Sample Soap request for retriving the soap nodes from the backend
         * <GetSoapNodes xmlns="http://schemas.cordys.com/1.0/ldap">
         *                 <dn>o=RTF,cn=cordys,o=vanenburg.com</dn>
         *                 <namespace>*</namespace>
         *                 <sort>ascending</sort>
         * </GetSoapNodes>
         */
        String[] paramNames = { "dn", "namespace", "sort" };
        String[] paramValues = { organization, "*", "ascending" };

        //Making a request to get the soap nodes from the ldap
        int responseNode = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                          task.getOrganization(),
                                                          "http://schemas.cordys.com/1.0/ldap",
                                                          "GetSoapNodes",
                                                          paramNames,
                                                          paramValues);

        //check the response for Soap Fault.
        GeneralUtils.handleException(responseNode);

        //match for all soapnode's dn
        int[] tupleNodes = Find.match(responseNode,
                                      "?<GetSoapNodesResponse><tuple>");

        tupleNodes = filterLdapContents(tupleNodes, cCurrentContent);

        if (tupleNodes.length > 0)
        {
            //Making a request to get delete soap nodes from the ldap
            responseNode = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                          task.getOrganization(),
                                                          "http://schemas.cordys.com/1.0/ldap",
                                                          "DeleteRecursive",
                                                          tupleNodes);

            //check the response for Soap Fault.
            GeneralUtils.handleException(responseNode);

            deleteCount = tupleNodes.length;
        }

        task.log((deleteCount == 0) ? ("[soapnodes] " +
                                    NO_DELETE_CONTENT_FOUND)
                                    : ("[soapnodes] " + DELETE_SUCCESS),
                 Project.MSG_INFO);
        iProcessedCounter += deleteCount;
    }

    /**
     * Implementing the abstract method.
     *
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param content DOCUMENTME
     * @param soapRequestManager The instance of SoapRequestManager class for
     *        handling soap requests.
     */
    public void executeDelete(ContentManagerTask task, Content content,
                              ISoapRequestManager soapRequestManager)
    {
        cCurrentContent = content;

        try
        {
            task.log("[" + content.getType() +
                     "] Deleting SoapNodes in LDAP...", Project.MSG_INFO);

            deleteSoapNodes(task.getOrganization(), task, soapRequestManager);
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
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param content DOCUMENTME
     * @param soapRequestManager The instance of SoapRequestManager class for
     *        handling soap requests.
     */
    public void executeEcxToFile(ContentManagerTask task, Content content,
                                 ISoapRequestManager soapRequestManager)
    {
        cCurrentContent = content;

        StringBuffer stringBuffer = new StringBuffer();
        String soapNodesContent = "";

        try
        {
            soapNodesContent = getSoapNodes(task.getOrganization(), task,
                                            soapRequestManager);

            if (soapNodesContent.equals(""))
            {
                task.log("[" + content.getType() + "] " +
                         NO_EXPORT_CONTENT_FOUND, Project.MSG_INFO);

                return;
            }

            stringBuffer.append("<soapnodes>");
            stringBuffer.append(soapNodesContent);
            stringBuffer.append("</soapnodes>");

            InputStream xslStream = FileUtil.getResourceAsStream(SoapNodesHandler.class,
                                                                 "xsl/SOAPNODES_TO_DEV.xsl");

            //transform the soapnode contents to developer format.	
            int developerContentNode = XSLUtil.getXSLTransformAsNode(stringBuffer.toString(),
                                                                     xslStream,
                                                                     null, null);

            String soapnodeContent = "";

            //check if the directory has been specified.
            if (content.getDir() != null)
            {
                File xmlFile = null;
                File contentDir = content.getDir();

                //match for all SoapNodes.
                int[] soapnodeNodes = Find.match(developerContentNode,
                                                 "<soapnodes><soapnode>");

                //make the directory structure if not existing.
                if (!contentDir.exists())
                {
                    contentDir.mkdirs();
                }

                //write each soapnode to a file.
                for (int k = 0; k < soapnodeNodes.length; k++)
                {
                    task.log("[" + content.getType() + "] Writing soapnode " +
                             Node.getAttribute(soapnodeNodes[k], "name") +
                             " to the local filesystem.", Project.MSG_INFO);
                    //convert all non readable xml format to readable format.
                    XMLUtils.iterateAllCDATAToXML(soapnodeNodes[k]);
                    soapnodeContent = XMLUtils.XML_FILE_PREFIX +
                                      Node.writeToString(soapnodeNodes[k], true);

                    xmlFile = new File(content.getDir(),
                                       Node.getAttribute(soapnodeNodes[k],
                                                         "name") + ".xml");

                    task.log("[" + content.getType() +
                             "] Writing soapnode to file:" +
                             xmlFile.getAbsolutePath(), Project.MSG_VERBOSE);

                    FileUtil.writeToFile(soapnodeContent,
                                         xmlFile.getAbsolutePath());
                    addProcessItem();
                }
            }

            if (content.getContentFile() != null)
            {
                //convert all non readable xml format to readable format.
                soapnodeContent = XMLUtils.CDATAToXML(Node.writeToString(developerContentNode,
                                                                         true));
                soapnodeContent = XMLUtils.XML_FILE_PREFIX +
                                  XMLUtils.formatToNiceXML(soapnodeContent);

                task.log("[" + content.getType() +
                         "] Writing content to file:" +
                         content.getContentFile().getAbsolutePath(),
                         Project.MSG_INFO);

                FileUtil.writeToFile(soapnodeContent,
                                     content.getContentFile().getAbsolutePath());
            }

            task.log("[" + content.getType() + "] " + ECX_TO_FILE_SUCCESS,
                     Project.MSG_INFO);
        }
        catch (UnsupportedEncodingException ue)
        {
            GeneralUtils.handleException("Error occured while performing xml operation.\n" +
                                         ue.getMessage(), ue, task);
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
        catch (XMLException xe)
        {
            GeneralUtils.handleException("Error occured while performing xml file operation.\n" +
                                         xe.getMessage(), xe, task);
        }
    }

    /**
     * Implementing the abstract method.
     *
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param content DOCUMENTME
     * @param soapRequestManager The instance of SoapRequestManager class for
     *        handling soap requests.
     */
    public void executeFileToEcx(ContentManagerTask task, Content content,
                                 ISoapRequestManager soapRequestManager)
    {
        cCurrentContent = content;

        int xmlFileNode = 0;
        int updateXmlNode = 0;
        Document document = null;
        boolean isC3 = task.getVersionInfo().isLaterThan(EBcpVersion.BCP42_C3);
        
        try
        {
            document = soapRequestManager.getDocument();

            InputStream xslStream = FileUtil.getResourceAsStream(SoapNodesHandler.class,
                                                                 "xsl/DEV_TO_SOAPNODES.xsl");

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
                                                .load(content.getContentFile()
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

                String developerContents = extractLDAPContents(fss,
                                                               "soapnodes",
                                                               "soapnode",
                                                               task, content);

                if (developerContents.equals(""))
                {
                    task.log("[" + content.getType() + "] " +
                             NO_IMPORT_CONTENT_FOUND, Project.MSG_INFO);

                    return;
                }

                //load the developer content xml file. 
                xmlFileNode = document.parseString(developerContents);
            }

            //convert children of specific nodes to string format
            XMLUtils.convertNodeToString(xmlFileNode,
                                         "<soapnodes><soapnode><soapprocessor><soapprocessorconfiguration>");

            //Now we'll write which SOAP nodes are in this file.
            int[] aiSoapNodes = Find.match(xmlFileNode, "<soapnodes><soapnode>");

            for (int iCount = 0; iCount < aiSoapNodes.length; iCount++)
            {
                String sSoapNodeName = Node.getAttribute(aiSoapNodes[iCount],
                                                         "name");

                if (!cCurrentContent.isPathAccepted(sSoapNodeName))
                {
                    Node.delete(aiSoapNodes[iCount]);
                    aiSoapNodes[iCount] = 0;
                    continue;
                }
                
                task.log("[" + content.getType() + "] Synchronizing soapnode " +
                         sSoapNodeName, Project.MSG_INFO);
                addProcessItem();
            }

            //pgussow: The organizational method sets will be modified by the XSL file to reflect
            //the correct LDAP dn. But it could also be that you're deploying to a machine with
            //a different LDAP root. So all the ISV methodsets need to point to a different root DN.
            //The XSLT2.0 definition has a regex replace. But 1.0 doesn't, so we have to do this in
            //java.
            int[] aiMethodSets = Find.match(xmlFileNode,
                                            "<soapnodes><soapnode><methodset>");

            Pattern pPattern = Pattern.compile("cn=cordys,o=[^,]+$");
            String sLDAPRoot = GeneralUtils.getLdapRootFromOrganization(task.getOrganization());
            
            for (int iCount = 0; iCount < aiMethodSets.length; iCount++)
            {
                int iMethodSetNode = aiMethodSets[iCount];
                String sMethodsetDN = Node.getDataWithDefault(iMethodSetNode,
                                                              "");

                if (sMethodsetDN.length() > 0)
                {
                    task.log("[" + content.getType() + "] Fixing DN: " +
                             sMethodsetDN, Project.MSG_DEBUG);
                    sMethodsetDN = pPattern.matcher(sMethodsetDN).replaceAll(sLDAPRoot);
                    task.log("[" + content.getType() + "] New DN: " +
                            sMethodsetDN, Project.MSG_DEBUG);
                    
                    //Delete the current value
                    while (Node.getFirstChild(iMethodSetNode) != 0)
                    {
                        Node.delete(Node.getFirstChild(iMethodSetNode));
                    }
                    
                    Node.getDocument(iMethodSetNode).createText(sMethodsetDN, iMethodSetNode);
                }
            }

            updateXmlNode = XSLUtil.getXSLTransformAsNode(Node.writeToString(xmlFileNode,
                                                                             true),
                                                          xslStream,
                                                          new String[] { "orgDN" },
                                                          new String[]
                                                          {
                                                              task.getOrganization()
                                                          });

            task.log("[" + content.getType() +
                     "] Updating soapnodes in LDAP...", Project.MSG_INFO);

            // Try to find old content from LDAP so we can update that.
            findOldUpdateLDAPEntries(updateXmlNode, task, soapRequestManager);
            
            if (isC3) {
                // For C3 we delete the bussoapnodeconfiguration and buskeystore elements from the old version.
                // As they are not needed.
                int[] keyStoreNodes = Find.match(updateXmlNode, "?<tuple><old><entry><buskeystore>");
                int[] configNodes = Find.match(updateXmlNode, "?<tuple><old><entry><bussoapnodeconfiguration>");
                
                for (int n : keyStoreNodes)
                {
                    Node.delete(n);
                }

                for (int n : configNodes)
                {
                    Node.delete(n);
                }
            }
            
            // Check that connections points are configured correctly (input file might point to another machine).
            modifyConnectionInfo(updateXmlNode, task, soapRequestManager,
                                 content);

            //send the xml for update request.
            updateLDAPEntry(updateXmlNode, task, soapRequestManager);

            task.log("[" + content.getType() + "] " + FILE_TO_ECX_SUCCESS,
                     Project.MSG_INFO);
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
        cmtTask.log("[" + cContent.getType() + "] Content of type " +
                    cContent.getType() +
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
     * Get SoapNode information from the organization specified by the
     * organization dn and writes it to an xml file
     *
     * @param dn The distinguished name(DN) of the organization or ISV Package.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @return The content xml with all the entry tags.
     *
     * @throws SoapRequestException
     */
    public String getSoapNodes(String dn, ContentManagerTask task,
                               ISoapRequestManager soapRequestMgr)
                        throws SoapRequestException
    {
        /*
         * Sample Soap request for retriving the soap nodes from the backend
         * <GetSoapNodes xmlns="http://schemas.cordys.com/1.0/ldap">
         *                 <dn>o=RTF,cn=cordys,o=vanenburg.com</dn>
         *                 <namespace>*</namespace>
         *                 <sort>ascending</sort>
         * </GetSoapNodes>
         */
        String[] paramNames = { "dn", "namespace", "sort" };
        String[] paramValues = { dn, "*", "ascending" };

        StringBuffer stringBuffer = new StringBuffer();

        //Making a request to get the soap nodes from the ldap
        int SoapNodesResponse = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                               null,
                                                               "http://schemas.cordys.com/1.0/ldap",
                                                               "GetSoapNodes",
                                                               paramNames,
                                                               paramValues);

        //check the response for Soap Fault.
        GeneralUtils.handleException(SoapNodesResponse);

        if (SoapNodesResponse != 0)
        {
            stringBuffer.append(getLDAPEntries(SoapNodesResponse, task,
                                               cCurrentContent, soapRequestMgr));
        }

        return stringBuffer.toString();
    }

    /**
     * Finds an available port on the target BCP machine
     *
     * @param sHostName Target BCP host name.
     * @param sUriPrefix URI prefix
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestManager The instance of SoapRequestManager class for
     *        handling soap requests.
     * @param mAllocatedPorts A map containing already allocated ports
     *
     * @return Unused port number or -1 if the operation failed.
     */
    protected int findAvailablePort(String sHostName, String sUriPrefix,
                                    ContentManagerTask task,
                                    ISoapRequestManager soapRequestManager,
                                    Map<Integer, Boolean> mAllocatedPorts)
    {
        final int iMinPort = 10000;
        final int iMaxPort = 65000;
        final int iCount = iMaxPort - iMinPort + 1;

        for (int i = 0; i < iCount; i++)
        {
            int iPort = (int) ((Math.random() * iCount) + iMinPort);

            if (mAllocatedPorts.containsKey(new Integer(i)))
            {
                continue;
            }

            String[] paramNames = { "stringParam1" };
            String[] paramValues = { sUriPrefix + iPort };

            int responseNode;

            try
            {
                //Making a request to get the soap nodes from the ldap
                responseNode = soapRequestManager.makeSoapRequest(task.getUserdn(),
                                                                  task.getOrganization(),
                                                                  "http://schemas.cordys.com/1.0/javacall",
                                                                  "isPortInUse",
                                                                  paramNames,
                                                                  paramValues);
            }
            catch (SoapRequestException e)
            {
                return -1;
            }

            int iResNode = Find.firstMatch(responseNode, "?<isPortInUse>");

            if (iResNode == 0)
            {
                // Something went wrong with the request.
                return -1;
            }

            if (Node.getDataWithDefault(iResNode, "").equals("false"))
            {
                mAllocatedPorts.put(new Integer(iPort), Boolean.TRUE);

                return iPort;
            }
        }

        return -1;
    }

    /**
     * Checks that SOAP processors connection points point to the
     * target machine. Also connections points get a new free port. Note that
     * the port might be actually be used by another SOAP processor, but it is
     * running at the moment.
     *
     * @param iUpdateXmlNode The XML node containing SOAP nodes that will be
     *        updated.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestManager The instance of SoapRequestManager class for
     *        handling soap requests.
     * @param content The current content.
     */
    protected void modifyConnectionInfo(int iUpdateXmlNode,
                                        ContentManagerTask task,
                                        ISoapRequestManager soapRequestManager,
                                        Content content)
    {
        InetAddress iaTargetAddress;

        String sTargetAddress = task.getServerHostName();

        try
        {
            iaTargetAddress = InetAddress.getByName(sTargetAddress);
        }
        catch (UnknownHostException e)
        {
            task.log("[" + content.getType() +
                     "] Unable to resolve target BCP host address '" +
                     sTargetAddress +
                     "'. Connection point addresses are not checked : " + e,
                     Project.MSG_ERR);
            return;
        }

        // Get the host name only (no domain part).
        String sNewHostName = iaTargetAddress.getHostName();
        String sNewHostIP = iaTargetAddress.getHostAddress();

        if (sNewHostIP.equals(sNewHostName))
        {
            task.log("[" + content.getType() +
                     "] Unable to resolve target BCP host address '" +
                     sTargetAddress +
                     "'. Connection point addresses are not checked.",
                     Project.MSG_ERR);
            return;
        }

        sNewHostName = sNewHostName.replaceFirst("^([^.]+).*", "$1");

        // Replace all SOAP processors to point to this computer.
        int[] iaComputers = Find.match(iUpdateXmlNode,
                                       "?<new><entry><computer><string>");

        if (iaComputers == null)
        {
            iaComputers = new int[0];
        }

        for (int i = 0; i < iaComputers.length; i++)
        {
            int iComputer = iaComputers[i];

            Node.setDataElement(iComputer, "",
                                iaTargetAddress.getHostName()
                                               .replaceFirst("([^\\.]+).*", "$1"));
        }

        int[] iaConnPoints = Find.match(iUpdateXmlNode,
                                        "?<new>?<labeleduri><string>");

        if (iaConnPoints == null)
        {
            iaConnPoints = new int[0];
        }

        Map<Integer, Boolean> mAllocatedPorts = new HashMap<Integer, Boolean>();

        for (int i = 0; i < iaConnPoints.length; i++)
        {
            int iConnPoint = iaConnPoints[i];
            String sUri = Node.getData(iConnPoint);

            if ((sUri == null) ||
                    (!sUri.startsWith("socket://") &&
                        !sUri.startsWith("zipsocket://")))
            {
                continue;
            }

            String sHost = sUri.replaceFirst("[^:]+://([^:]*):.*", "$1");
            InetAddress iaHostAddress = null;

            try
            {
                iaHostAddress = InetAddress.getByName(sHost);
            }
            catch (UnknownHostException e)
            {
                // Possibly an unknown host. Replace the address.
            }

            if ((iaHostAddress != null) &&
                    iaHostAddress.equals(iaTargetAddress))
            {
                // Addresses match.
                continue;
            }

            String sNewUri;
            int iPort;

            // Build the new host string.
            sNewUri = sUri.replaceFirst("([^:]+://).*", "$1");
            sNewUri += (sNewHostName + ":");

            if ((iPort = findAvailablePort(sHost, sNewUri, task,
                                               soapRequestManager,
                                               mAllocatedPorts)) == -1)
            {
                task.log("[" + content.getType() +
                         "] Unable to find an available port for BCP host '" +
                         sTargetAddress +
                         "'. Connection point addresses are not checked.",
                         Project.MSG_ERR);
                return;
            }

            sNewUri += iPort;

            task.log("[" + content.getType() +
                     "] Replaced a connection point address to " + sNewUri,
                     Project.MSG_VERBOSE);

            // Set the new connection point string.
            XMLHelpers.setNodeText(iConnPoint, sNewUri);
        }
    }
}
