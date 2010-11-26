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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import com.cordys.coe.exception.GeneralException;
import com.cordys.coe.util.FileUtils;
import com.cordys.tools.ant.soap.ISoapRequest;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.FileUtil;
import com.cordys.tools.ant.util.GeneralUtils;
import com.cordys.tools.ant.util.XMLUtils;
import com.cordys.tools.ant.util.XSLUtil;
import com.eibus.util.Base64;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * The class handles the content of type methodsets. This extends most of
 * the features used commonly in LDAP kind of contents from the Class
 * <code>LDAPHelper</code>.
 *
 * @author msreejit
 */
public class MethodsetsHandler extends LDAPHelper
    implements ContentHandler
{
    /**
     * Message logged when no content is found to import from ECX
     */
    private static final String NO_IMPORT_CONTENT_FOUND = "No methodsets found to import from ECX.";
    /**
     * Message logged when no content is found to export from ECX
     */
    private static final String NO_EXPORT_CONTENT_FOUND = "No methodsets found to export from ECX.";
    /**
     * Message logged when no content is found to delete from ECX
     */
    private static final String NO_DELETE_CONTENT_FOUND = "No methodsets found in ECX to delete.";
    /**
     * Message logged when import from ECX is successful
     */
    private static final String ECX_TO_FILE_SUCCESS = "Successfully exported methodsets from ECX.";
    /**
     * Message logged when export from ECX is successful
     */
    private static final String FILE_TO_ECX_SUCCESS = "Successfully imported methodsets to ECX.";
    /**
     * Message logged when delete from ECX is successful
     */
    private static final String DELETE_SUCCESS = "Successfully deleted methodsets from ECX.";
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
     * Regex for extracting the CoBOC mapping ID from the method implementation. Note: The DOTALL flag must be enabled.
     */
    public static Pattern pCobocMapIdPattern = Pattern.compile("\\s*(<implementation\\s+type=\"CGMAP\">\\s*<mapid>)([^<]+)(</mapid>.*</implementation>)\\s*", Pattern.DOTALL);
    
/**
     * Default Constructor
     */
    public MethodsetsHandler()
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
     * Deletes MethodSet information from the organization
     *
     * @param organization organimzation dn
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @throws SoapRequestException
     */
    public void deleteMethodSets(String organization, ContentManagerTask task,
                                 ISoapRequestManager soapRequestMgr)
                          throws SoapRequestException
    {
        int deleteCount = 0;

        /*
         * Sample Soap request for retriving the method sets from the backend
         * <GetMethodSets xmlns="http://schemas.cordys.com/1.0/ldap">
         *                 <dn>o=RTF,cn=cordys,o=vanenburg.com</dn>
         *                 <labeleduri>*</labeleduri>
         *                 <sort>ascending</sort>
         * </GetMethodSets>
         */
        String[] paramNames = { "dn", "labeleduri", "sort" };
        String[] paramValues = { organization, "*", "ascending" };

        //Making a request to get the method sets from the ldap
        int responseNode = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                          task.getOrganization(),
                                                          "http://schemas.cordys.com/1.0/ldap",
                                                          "GetMethodSets",
                                                          paramNames,
                                                          paramValues);

        //check the response for Soap Fault.
        GeneralUtils.handleException(responseNode);

        //match for all methodset's dn
        int[] tupleNodes = Find.match(responseNode,
                                      "?<GetMethodSetsResponse><tuple>");

        tupleNodes = filterLdapContents(tupleNodes, cCurrentContent);

        if (tupleNodes.length > 0)
        {
            //Making a request to get delete method sets from the ldap
            responseNode = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                          task.getOrganization(),
                                                          "http://schemas.cordys.com/1.0/ldap",
                                                          "DeleteRecursive",
                                                          tupleNodes);

            //check the response for Soap Fault.
            GeneralUtils.handleException(responseNode);

            deleteCount = tupleNodes.length;
            addProcessItem();
        }

        task.log((deleteCount == 0) ? ("[methodsets] " +
                                    NO_DELETE_CONTENT_FOUND)
                                    : ("[methodsets] " + DELETE_SUCCESS),
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
            task.log("[" + content.getType() +
                     "] Deleting methodsets in LDAP...", Project.MSG_INFO);

            deleteMethodSets(task.getOrganization(), task, soapRequestManager);
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

        int[] methodsetTupleNodes;
        byte[] xslString;
        
        try
        {
            xslString = FileUtils.readResourceContents("xsl/METHODSETS_TO_DEV.xsl",
                    MethodsetsHandler.class);

            //if isvdn has been specified execute related method.
            if (task.getIsvdn() != null)
            {
                methodsetTupleNodes = getMethodSets(task.getIsvdn(), task,
                                                  soapRequestManager);
            }
            else
            {
                methodsetTupleNodes = getMethodSets(task.getOrganization(), task,
                                                  soapRequestManager);
            }

            if (methodsetTupleNodes.length == 0)
            {
                task.log("[" + content.getType() + "] " +
                         NO_EXPORT_CONTENT_FOUND, Project.MSG_INFO);

                return;
            }
            
            for (int tupleNode : methodsetTupleNodes)
            {
                StringBuilder stringBuffer = new StringBuilder();
                
                stringBuffer.append("<methodsets>");
                stringBuffer.append(getLDAPEntries(tupleNode, task,
                                                   cCurrentContent, soapRequestManager));
                stringBuffer.append("</methodsets>");
    
                //transform the methodset contents to developer format.
                int developerContentNode = 0;
    
                try
                {
                    developerContentNode = XSLUtil.getXSLTransformAsNode(stringBuffer.toString(),
                                                                         new ByteArrayInputStream(xslString),
                                                                         null, null);
                }
                catch (TransformerException te)
                {
                    task.log("[" + content.getType() +
                             "] Error transforming. The XML being transformed:\n" +
                             stringBuffer.toString(), Project.MSG_DEBUG);
                    throw te;
                }
    
                // Fix methods related to CoBOC content, e.g. methods based on a mapping.
                fixCobocMethodsForFileSystem(task, soapRequestManager,
                                             developerContentNode);
                
                //check if the directory has been specified.
                if (content.getDir() != null)
                {
                    File xmlFile = null;
                    File contentDir = content.getDir();
    
                    //match for all methodsets.
                    int[] methodsetNodes = Find.match(developerContentNode,
                                                      "<methodsets><methodset>");
    
                    //make the directory structure if not existing.
                    if (!contentDir.exists())
                    {
                        contentDir.mkdirs();
                    }
    
                    //write each methodset to a file.
                    for (int k = 0; k < methodsetNodes.length; k++)
                    {
                        // Sort the methodsets children.
                        sortMethodsetContents(methodsetNodes[k], task, content);
                        
                        //convert all non readable xml format to readable format.
                        String methodsetContent = XMLUtils.CDATAToXML(Node.writeToString(methodsetNodes[k],
                                                                                  true));
    
                        try
                        {
                            methodsetContent = XMLUtils.XML_FILE_PREFIX +
                                               XMLUtils.formatToNiceXML(methodsetContent);
                        }
                        catch (Exception ex)
                        {
                            // something was wrong, create a file with the latest status and give proper exception
                            String errFile = Node.getAttribute(methodsetNodes[k],
                                                               "name") +
                                             ".xml.err";
                            xmlFile = new File(content.getDir(), errFile);
    
                            FileUtil.writeToFile(methodsetContent,
                                                 xmlFile.getAbsolutePath());
    
                            throw new XMLException("Error formatting xml : see " +
                                                   errFile + ". Exception:" +
                                                   ex.toString());
                        }
    
                        task.log("[" + content.getType() + "] Writing methodset " +
                                 Node.getAttribute(methodsetNodes[k], "name") +
                                 " to the local filesystem.", Project.MSG_INFO);
    
                        xmlFile = new File(content.getDir(),
                                           Node.getAttribute(methodsetNodes[k],
                                                             "name") + ".xml");
    
                        task.log("[" + content.getType() +
                                 "] Writing methodset to file:" +
                                 xmlFile.getAbsolutePath(), Project.MSG_VERBOSE);
    
                        FileUtil.writeToFile(methodsetContent,
                                             xmlFile.getAbsolutePath());
                        addProcessItem();
                    }
                }
    
                if (content.getContentFile() != null)
                {
                    //convert all non readable xml format to readable format.
                    String methodsetContent = XMLUtils.CDATAToXML(Node.writeToString(developerContentNode,
                                                                              true));
                    methodsetContent = XMLUtils.XML_FILE_PREFIX +
                                       XMLUtils.formatToNiceXML(methodsetContent);
    
                    task.log("[" + content.getType() +
                             "] Writing content to file:" +
                             content.getContentFile().getAbsolutePath(),
                             Project.MSG_INFO);
    
                    FileUtil.writeToFile(methodsetContent,
                                         content.getContentFile().getAbsolutePath());
                }
            }
            
            task.log("[" + content.getType() + "] " + ECX_TO_FILE_SUCCESS,
                     Project.MSG_INFO);
        }
        catch (GeneralException ue)
        {
            GeneralUtils.handleException("Error occured while performing xml operation.\n" +
                                         ue.getMessage(), ue, task);
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
     * Sorts the methods and xsd's alphabetically inside the method set node. This
     * way the file contents are not modified when the method set is not modified, but
     * the methods are returned in a different order from LDAP. 
     * @param methodsetNode Method set root node.
     */
    private void sortMethodsetContents(int methodsetNode, ContentManagerTask task, Content content) {
        int[] contentNodes = Find.match(methodsetNode, "<><>");
        Map<String, Integer> sortMap = new TreeMap<String, Integer>();
        List<Integer> unknownNodeList = new LinkedList<Integer>();
        
        for (int node : contentNodes)
        {
            String name = Node.getAttribute(node, "name", "");
            
            node = Node.unlink(node);
            
            if (name.length() > 0) {
                if (sortMap.containsKey(name)) {
                    task.log(String.format("[%s] %s with name %s already exists in this methodset.",
                            content.getType(),
                            name,
                            Node.getLocalName(node)),
                            Project.MSG_WARN);
                    unknownNodeList.add(node);
                } else {
                    sortMap.put(name, node);
                }
            } else {
                unknownNodeList.add(node);
            }
        }
        
        for (int node : sortMap.values())
        {
            Node.appendToChildren(node, methodsetNode);
        }
        sortMap.clear();
        
        for (int node : unknownNodeList)
        {
            Node.appendToChildren(node, methodsetNode);
        }
        unknownNodeList.clear();
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
        Document document = null;
        byte[] xslString;

        try
        {
            document = soapRequestManager.getDocument();

            xslString = FileUtils.readResourceContents("xsl/DEV_TO_METHODSETS.xsl",
                                                                 MethodsetsHandler.class);

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
                
                updateMethodsetToEcx(task, content, soapRequestManager, xmlFileNode, xslString);
                Node.delete(xmlFileNode);
                xmlFileNode = 0;
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

                // Flatten the fileset array.
                for (int i = 0; i < filesets.size(); i++)
                {
                    FileSet fs = (FileSet) filesets.elementAt(i);
                    vfss.addElement(fs);
                }
                
                // For each file in the filesets, update the methodset to ECX.
                for (int i = 0; i < vfss.size(); i++)
                {
                    FileSet fs = (FileSet) vfss.elementAt(i);
                    DirectoryScanner ds = fs.getDirectoryScanner(task.getProject());
                    String[] srcFiles = ds.getIncludedFiles();
                    File baseDir = ds.getBasedir();
                    
                    for (String fileName : srcFiles)
                    {
                        String methodsetContent = extractLDAPContents(fileName, baseDir, task, content.getType());
                        
                        if ("".equals(methodsetContent))
                        {
                            continue;
                        }
                        
                        StringBuilder tmp = new StringBuilder(methodsetContent.length() + 30);
                        
                        tmp.append("<methodsets>").append(methodsetContent).append("</methodsets>");
                        
                        String developerContents = tmp.toString();
    
                        //load the developer content xml file. 
                        xmlFileNode = document.parseString(developerContents);
                        
                        try {
                            updateMethodsetToEcx(task, content, soapRequestManager, xmlFileNode, xslString);
                        } 
                        finally {
                            Node.delete(xmlFileNode);
                            xmlFileNode = 0;
                        }
                    }
                }
            }
            
            if (getNumberOfProcessedItems() == 0) {
                task.log("[" + content.getType() + "] " +
                        NO_IMPORT_CONTENT_FOUND, Project.MSG_INFO);
            } else {
                task.log("[" + content.getType() + "] " + FILE_TO_ECX_SUCCESS,
                        Project.MSG_INFO);
            }
        }
        catch (GeneralException ue)
        {
            GeneralUtils.handleException("Error occured while performing xml operation.\n" +
                                         ue.getMessage(), ue, task);
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
        finally {
            if (xmlFileNode != 0)
            {
                Node.delete(xmlFileNode);
                xmlFileNode = 0;
            }
        }
    }

    /**
     * Sends a methodset to ECX.
     * @param task Current task.
     * @param content Current content object.
     * @param soapRequestManager SOAP request manager to be used.
     * @param xmlFileNode Method set root node.
     * @param xslString XSLT to be used for transformation.
     * @throws SoapRequestException
     * @throws GeneralException
     * @throws UnsupportedEncodingException
     * @throws XMLException
     * @throws TransformerException
     */
    private void updateMethodsetToEcx(ContentManagerTask task, 
                                        Content content, 
                                        ISoapRequestManager soapRequestManager, 
                                        int xmlFileNode, 
                                        byte[] xslString) throws SoapRequestException, GeneralException, UnsupportedEncodingException, XMLException, TransformerException
    {
        int updateXmlNode;
        // Fix methods related to CoBOC content, e.g. methods based on a mapping.
        fixCobocMethodsForECX(soapRequestManager, xmlFileNode, task);

        //convert children of specific nodes to string format
        XMLUtils.convertNodeToString(xmlFileNode,
                                     "<methodsets><methodset><method><methodimplementation>");
        XMLUtils.convertNodeToString(xmlFileNode,
                                     "<methodsets><methodset><method><methodwsdl>");
        XMLUtils.convertNodeToString(xmlFileNode,
                                     "<methodsets><methodset><method><methodinterface>");
        XMLUtils.convertNodeToString(xmlFileNode,
                                     "<methodsets><methodset><method><busmethodsignature>");
        XMLUtils.convertNodeToString(xmlFileNode,
                                     "<methodsets><methodset><xsd><methodxsd>");

        //Now we'll write which methodsets are in this file.
        int[] aiMethodSets = Find.match(xmlFileNode,
                                        "<methodsets><methodset>");

        for (int iCount = 0; iCount < aiMethodSets.length; iCount++)
        {
            String sMethodSetName = Node.getAttribute(aiMethodSets[iCount],
                                                      "name");

            if (!cCurrentContent.isPathAccepted(sMethodSetName))
            {
                Node.delete(aiMethodSets[iCount]);
                aiMethodSets[iCount] = 0;
                continue;
            }

            task.log("[" + content.getType() +
                     "] Synchronizing methodset " + sMethodSetName,
                     Project.MSG_INFO);
            addProcessItem();
        }

        updateXmlNode = XSLUtil.getXSLTransformAsNode(Node.writeToString(xmlFileNode,
                                                                         true),
                                                      new ByteArrayInputStream(xslString),
                                                      new String[] { "orgDN" },
                                                      new String[]
                                                      {
                                                          task.getOrganization()
                                                      });

        try {
            task.log("[" + content.getType() +
                     "] Updating methodsets in LDAP...", Project.MSG_VERBOSE);
    
            //send the xml for update request.
            findOldUpdateLDAPEntries(updateXmlNode, task, soapRequestManager);
            updateLDAPEntry(updateXmlNode, task, soapRequestManager);
            
            addProcessItem();
        }
        finally {
            if (updateXmlNode != 0)
            {
                Node.delete(updateXmlNode);
                updateXmlNode = 0;
            }
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
     * Get MethodSet information from the organization specified by the
     * organization dn and writes it to an xml file
     *
     * @param dn The distinguished name(DN) of the organization or ISV Package.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @return An array of found method set tuple nodes. These nodes do not contain any children yet.
     *
     * @throws SoapRequestException
     */
    public int[] getMethodSets(String dn, ContentManagerTask task,
                                ISoapRequestManager soapRequestMgr)
                         throws SoapRequestException
    {
        /*
         * Sample Soap request for retriving the method sets from the backend
         * <GetMethodSets xmlns="http://schemas.cordys.com/1.0/ldap">
         *                 <dn>o=RTF,cn=cordys,o=vanenburg.com</dn>
         *                 <labeleduri>*</labeleduri>
         *                 <sort>ascending</sort>
         * </GetMethodSets>
         */
        String[] paramNames = { "dn", "labeleduri", "sort" };
        String[] paramValues = { dn, "*", "ascending" };

        //Making a request to get the method sets from the ldap
        int methodSetsResponse = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                                null,
                                                                "http://schemas.cordys.com/1.0/ldap",
                                                                "GetMethodSets",
                                                                paramNames,
                                                                paramValues);

        //check the response for Soap Fault.
        GeneralUtils.handleException(methodSetsResponse);
        
        //gets the entry collection from the parent node
        int[] tupleEntryNodes = Find.match(methodSetsResponse, "<><><><tuple>");

        for (int i = 0; i < tupleEntryNodes.length; i++)
        {
            int dummy = Node.getDocument(tupleEntryNodes[i]).createElement("root");
            
            Node.appendToChildren(tupleEntryNodes[i], dummy);
            tupleEntryNodes[i] = dummy;
        }
        
        Node.delete(methodSetsResponse);
        methodSetsResponse = 0;

        return tupleEntryNodes;
    }

    /**
     * @see com.cordys.tools.ant.cm.ContentHandler#getNumberOfProcessedItems()
     */
    public int getNumberOfProcessedItems()
    {
        return iProcessedCounter;
    }

    /**
     * Fixes methods that have a reference to a CoBOC content, e.g.
     * method based on a mapping.
     *
     * @param srmSoapRequestManager SOAP request manager
     * @param xContentNode Method set root node.
     * @param cmtTask The current ant task
     *
     * @throws SoapRequestException Thrown if the CoBOC SOAP call failed.
     * @throws GeneralException Thrown if the content handling failed.
     */
    private void fixCobocMethodsForECX(ISoapRequestManager srmSoapRequestManager,
                                       int xContentNode,
                                       ContentManagerTask cmtTask)
                                throws SoapRequestException, GeneralException
    {
        int[] xaMethodImplNodes = Find.match(xContentNode,
                                             "<methodsets><methodset><method><methodimplementation><implementation type=\"CGMAP\">");

        for (int i = 0; i < xaMethodImplNodes.length; i++)
        {
            //First we'll try to fix based on the mapping ID. If that fails we'll try via the map name.
            int xImplNode = xaMethodImplNodes[i];
            int xMapKeyNode = Find.firstMatch(xImplNode, "<><mapid>");

            if (xMapKeyNode == 0)
            {
                continue;
            }

            String sMapKey = Node.getDataWithDefault(xMapKeyNode, "");

            if (sMapKey.length() == 0)
            {
                continue;
            }

            String sMapId = "";

            //First try via the map ID
            try
            {
                sMapId = getCobocMapID(srmSoapRequestManager, sMapKey);
            }
            catch (SoapRequestException e)
            {
                //For some reason the mapping could not be resolved. Let's make sure
                //we let the user know which method goes wrong.
                int iMethodNode = xImplNode;

                while ((iMethodNode != 0) &&
                           !Node.getLocalName(iMethodNode).equals("method"))
                {
                    iMethodNode = Node.getParent(iMethodNode);
                }

                String sMethodName = Node.getAttribute(iMethodNode, "name",
                                                       "unknown");

                cmtTask.log("[" + cCurrentContent.getType() +
                            "] Could not find method " + sMethodName +
                            " via the mapid.", Project.MSG_DEBUG);

                //Now try it via the mapname.
                int xMapNameNode = Find.firstMatch(xImplNode, "<><mapname>");

                if (xMapNameNode != 0)
                {
                    String sTemp = Node.getDataWithDefault(xMapNameNode, "");

                    if (sTemp.length() > 0)
                    {
                        sMapId = getCobocMapID(srmSoapRequestManager, sTemp);
                    }
                }

                cmtTask.log("[" + cCurrentContent.getType() +
                            "] MapID after using the mapname: " + sMethodName +
                            " via the mapid.", Project.MSG_DEBUG);
            }

            if (sMapId.length() == 0)
            {
                throw new GeneralException("No CoBOC mapping found with key " +
                                           sMapKey);
            }

            Node.setDataElement(xMapKeyNode, "", sMapId);
        }
    }

    /**
     * Fixes methods that have a reference to a CoBOC content, e.g.
     * method based on a mapping.
     * @param task The current ANT task.
     *
     * @param srmSoapRequestManager SOAP request manager
     * @param xContentNode Method set root node.
     *
     * @throws SoapRequestException Thrown if the CoBOC SOAP call failed.
     * @throws GeneralException Thrown if the content handling failed.
     */
    private void fixCobocMethodsForFileSystem(ContentManagerTask task, ISoapRequestManager srmSoapRequestManager,
                                              int xContentNode)
                                       throws SoapRequestException,
                                              GeneralException
    {
        int[] xaMethodImplNodes = Find.match(xContentNode,
                                             "<methodsets><methodset><method><methodimplementation>"); //<implementation type=\"CGMAP\">");
 
        for (int i = 0; i < xaMethodImplNodes.length; i++)
        {
            int xImplNode = xaMethodImplNodes[i];
            String sImplText = Node.getDataWithDefault(xImplNode, "");

            if (sImplText.length() == 0)
            {
                task.log("Ignoring text:\n" + sImplText, Project.MSG_DEBUG);
                continue;
            }

            Matcher mMatcher = pCobocMapIdPattern.matcher(sImplText);

            if (!mMatcher.matches())
            {
                
                task.log("Ignoring text (no regex match):\n" + Base64.encode(sImplText) + "\nRegEx: " + pCobocMapIdPattern.pattern() + "\nFlags: " + pCobocMapIdPattern.flags(), Project.MSG_DEBUG);
                continue;
            }

            String sMapId = mMatcher.group(2);

            if ((sMapId == null) || (sMapId.length() == 0))
            {
                task.log("Ignoring text (mapid is null:\n" + sImplText, Project.MSG_DEBUG);
                continue;
            }
            
            sMapId = sMapId.trim();

            ISoapRequest srRequest = srmSoapRequestManager.createSoapRequest();
            int xRequestNode;

            xRequestNode = srRequest.addMethod("http://schemas.cordys.com/1.0/coboc",
                                               "GetObject");
            Node.createTextElement("object_id", sMapId, xRequestNode);

            int xResponseNode = srRequest.execute();
            int xMapKeyNode = Find.firstMatch(xResponseNode,
                                              "?<object><MAP_OBJECT><NAME>");
            String sMapKey = (xMapKeyNode != 0)
                             ? Node.getDataWithDefault(xMapKeyNode, "") : "";

            if (sMapKey.length() == 0)
            {
                throw new GeneralException("No CoBOC mapping found with object ID " +
                                           sMapId);
            }

            Node.setDataElement(xImplNode, "",
                                mMatcher.group(1) + sMapKey +
                                mMatcher.group(3));
        }
    }
    
    /**
     * This method gets the CoBOC map id for a certain key.
     *
     * @param srmSoapRequestManager The soap request manager.
     * @param sKey The key of the mapping.
     *
     * @return The map ID.
     */
    private String getCobocMapID(ISoapRequestManager srmSoapRequestManager,
                                 String sKey)
                          throws SoapRequestException
    {
        String sReturn = null;

        ISoapRequest srRequest = srmSoapRequestManager.createSoapRequest();
        int xRequestNode;
        int xRequestKeyNode;

        xRequestNode = srRequest.addMethod("http://schemas.cordys.com/4.2/coboc",
                                           "GetXMLObject");
        xRequestKeyNode = Node.createTextElement("key", sKey, xRequestNode);
        Node.setAttribute(xRequestKeyNode, "filter", "/MAP");
        Node.setAttribute(xRequestKeyNode, "type", "instance");
        Node.setAttribute(xRequestKeyNode, "version", "organization");

        int xResponseNode = srRequest.execute();

        int xMapIdNode = Find.firstMatch(xResponseNode,
                                         "?<tuple><old><ENTITY><ENTITY_ID>");
        sReturn = Node.getDataWithDefault(xMapIdNode, "");

        return sReturn;
    }
}
