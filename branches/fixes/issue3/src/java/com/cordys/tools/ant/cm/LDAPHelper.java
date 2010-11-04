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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.FileUtil;
import com.cordys.tools.ant.util.GeneralUtils;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * Helper class which contains common LDAP related methods.
 *
 * @author msreejit
 */
public abstract class LDAPHelper
{
    /**
     * Stores XML Document used for XML manipulation
     */
    private static Document document = new Document();

    /**
     * Default constructor
     */
    protected LDAPHelper()
    {
        super();
    }

    /**
     * Method to return the response entry tags of the LDAP entry passed as
     * parameter parentNode and gets all the child entries.  The children for
     * method sets are its methods and the children for roles  are the ACL
     * information.
     *
     * @param parentNode The parent node which is to be fetched.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestManager The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @return The content xml in string format.
     *
     * @throws SoapRequestException
     */
    protected static String getLDAPEntries(int parentNode,
                                           ContentManagerTask task,
                                           Content cCurrentContent,
                                           ISoapRequestManager soapRequestManager)
                                    throws SoapRequestException
    {
        StringBuffer stringBuffer = new StringBuffer();

        //gets the entry collection from the parent node
        int[] tupleEntryNodes = Find.match(parentNode, "?<tuple><old><entry>");

        if (tupleEntryNodes.length > 0)
        {
            String[] paramNames = { "dn", "sort" };
            String[] paramValues = new String[2];
            paramValues[1] = "ascending";

            for (int i = 0; i < tupleEntryNodes.length; i++)
            {
                String entryDn = Node.getAttribute(tupleEntryNodes[i], "dn");
                String entryCn = entryDn.substring(3, entryDn.indexOf(","));
                String everyoneInOrgCn = "everyoneIn" +
                                         task.getOrganization().substring(2,
                                                                          task.getOrganization()
                                                                              .indexOf(","));

                //If the role is everyoneIn<org> ignore the role
                if (everyoneInOrgCn.equals(entryCn))
                {
                    continue;
                }
                
                if (cCurrentContent != null && ! cCurrentContent.isPathAccepted(entryCn)) {
                    continue;
                }

                //Appends the entry of the parent to the string buffer 
                stringBuffer.append(Node.writeToString(tupleEntryNodes[i], true));
                paramValues[0] = entryDn;

                /*
                 * Sample XML for sending soap request to the GetChildren method
                 * <GetChildren xmlns="http://schemas.cordys.com/1.0/ldap">
                 *     <dn>cn=test,cn=organizational roles,o=CRTM Apps,cn=cordys,o=vanenburg.com</dn>
                 *     <sort>ascending</sort>
                 * </GetChildren>
                 */

                //Makes the soap request to get the children
                int responseNode = soapRequestManager.makeSoapRequest(task.getUserdn(),
                                                                      task.getOrganization(),
                                                                      "http://schemas.cordys.com/1.0/ldap",
                                                                      "GetChildren",
                                                                      paramNames,
                                                                      paramValues);

                //check the response for Soap Fault.
                GeneralUtils.handleException(responseNode);

                //get recursively LDAP entries to any level of depth.
                stringBuffer.append(getLDAPEntries(responseNode, task,
                                    null, soapRequestManager));
            }
        }

        return stringBuffer.toString();
    }

    /**
     * Method for extracting LDAPContents from the files specified in the
     * fileset.
     *
     * @param filesets The filesets which specify the set of files to be
     *        scanned for.
     * @param contentRootTag The name of the root tag for content xml creation.
     * @param contentTag The name of the tag for content xml creation.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param cContent
     *
     * @return The content xml in string format.
     *
     * @throws XMLException
     */
    protected static String extractLDAPContents(FileSet[] filesets,
                                                String contentRootTag,
                                                String contentTag,
                                                ContentManagerTask task, Content cContent)
                                         throws XMLException
    {
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer sbContent = new StringBuffer("");

        // deal with the filesets
        for (int i = 0; i < filesets.length; i++)
        {
            FileSet fs = filesets[i];
            DirectoryScanner ds = fs.getDirectoryScanner(task.getProject());
            String[] srcFiles = ds.getIncludedFiles();
            File baseDir = ds.getBasedir();

            sbContent.append(extractLDAPContents(srcFiles, baseDir, task, cContent.getType()));
        }

        String contents = sbContent.toString();
        if (!"".equals(contents))
        {
            stringBuffer.append("<");
            stringBuffer.append(contentRootTag);
            stringBuffer.append(">");
            stringBuffer.append(contents);
            stringBuffer.append("</");
            stringBuffer.append(contentRootTag);
            stringBuffer.append(">");
        }

        return stringBuffer.toString();
    }

    /**
     * Method fetches previous objects for update request so we can actually
     * update an object instead of just inserting it. Expects the content
     * format passed in the parameter updateXMLNode  with tuple tags which are
     * matched for appending in update request.
     *
     * @param updateXMLNode The xml node containing the update requests in
     *        tuple format.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestManager The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @throws SoapRequestException
     */
    protected static void findOldUpdateLDAPEntries(int updateXMLNode,
                                                   ContentManagerTask task,
                                                   ISoapRequestManager soapRequestManager)
                                            throws SoapRequestException
    {
        //match for all the tuples of methodsets or roles to be sent for update.
        int[] entryNodes = Find.match(updateXMLNode, "<><tuple><new><entry>");

        for (int i = 0; i < entryNodes.length; i++)
        {
            int iUpdateEntryNode = entryNodes[i];
            String sDN = Node.getAttribute(iUpdateEntryNode, "dn");
            int iReqNode = Node.getDocument(updateXMLNode).createTextElement("dn",
                                                                             sDN);

            //Makes the soap request for updating the methodsets/roles
            int iResponseNode = soapRequestManager.makeSoapRequest(task.getUserdn(),
                                                                   task.getOrganization(),
                                                                   "http://schemas.cordys.com/1.0/ldap",
                                                                   "GetLDAPObject",
                                                                   iReqNode);
            int iResponseEntryNode = Find.firstMatch(iResponseNode,
                                                     "?<tuple><old>");

            if (iResponseEntryNode != 0)
            {
                Node.appendToChildren(Node.clone(iResponseEntryNode, true),
                                      Node.getParent(Node.getParent(iUpdateEntryNode)));
            }
        }
    }

    /**
     * Method creates update request which can be sent as SOAP Requests.
     * Expects the content format passed in the parameter updateXMLNode  with
     * tuple tags which are matched for appending in update request.
     *
     * @param updateXMLNode The xml node containing the update requests in
     *        tuple format.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestManager The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @throws SoapRequestException
     */
    protected static void updateLDAPEntry(int updateXMLNode,
                                          ContentManagerTask task,
                                          ISoapRequestManager soapRequestManager)
                                   throws SoapRequestException
    {
        //match for all the tuples of methodsets or roles to be sent for update.
        int[] tupleNodes = Find.match(updateXMLNode, "<><tuple>");

        if (tupleNodes.length > 0)
        {
            //Makes the soap request for updating the methodsets/roles
            int responseNode = soapRequestManager.makeSoapRequest(task.getUserdn(),
                                                                  task.getOrganization(),
                                                                  "http://schemas.cordys.com/1.0/ldap",
                                                                  "Update",
                                                                  tupleNodes);

            try {
                //check the response for Soap Fault.
                GeneralUtils.handleException(responseNode);
            }
            finally {
                if (responseNode != 0)
                {
                    Node.delete(responseNode);
                    responseNode = 0;
                }
            }
            
        }
    }

    /**
     * Method for internal use.
     *
     * @param contentFilePaths The files which have been included in the
     *        Fileset scan.
     * @param baseDir The base directory in which the files have been scanned.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param sType The type of content that is being handled.
     *
     * @return The content xml in string format.
     */
    protected static String extractLDAPContents(String[] contentFilePaths,
                                              File baseDir, 
                                              ContentManagerTask task, String sType)
    {
        StringBuffer stringBuffer = new StringBuffer();

        for (int j = 0; j < contentFilePaths.length; j++)
        {
            stringBuffer.append(extractLDAPContents(contentFilePaths[j], baseDir, task, sType));
        }

        return stringBuffer.toString();
    }
    
    /**
     * Method for internal use.
     *
     * @param contentFilePaths The files which have been included in the
     *        Fileset scan.
     * @param baseDir The base directory in which the files have been scanned.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param sType The type of content that is being handled.
     *
     * @return The content xml in string format.
     */
    protected static String extractLDAPContents(String filePath,
                                              File baseDir,
                                              ContentManagerTask task, String sType)
    {
        int xmlNode = 0;
        StringBuffer stringBuffer = new StringBuffer();

        File contentFile = new File(baseDir, filePath);

        task.log("[" + sType + "] Parsing Content File:" + contentFile.getAbsolutePath(),
                 Project.MSG_VERBOSE);

        try
        {
            xmlNode = document.load(contentFile.getAbsolutePath());
        }
        catch (XMLException e)
        {
            throw new BuildException("[" + sType + "] File not well formed! Ignoring file:" +
                     contentFile.getAbsolutePath() +
                     "\nUse exclude statements in filesets to ignore unwanted files.");
        }

        if (xmlNode != 0)
        {
            stringBuffer.append(Node.writeToString(xmlNode, true));
            Node.delete(xmlNode);
            xmlNode = 0;
        }

        return stringBuffer.toString();
    }
    
    /**
     * Filters the LDAP tuples based on the content filter.
     * @param iaTupleNodes Tuple nodes.
     * @param cCurrentContent Content object containing the filter.
     * @return Filtered tuple array.
     */
    protected int[] filterLdapContents(int[] iaTupleNodes, Content cCurrentContent) {
        int iValidCount = iaTupleNodes.length;
        
        for (int i = 0; i < iaTupleNodes.length; i++)
        {
            int iTupleNode = iaTupleNodes[i];
            int iEntryNode = Find.firstMatch(iTupleNode, "<tuple><><entry>");
            
            if (iEntryNode == 0) {
                continue;
            }
            
            String sKey = getEntryName(iEntryNode);
            
            if (! cCurrentContent.isPathAccepted(sKey)) {
                Node.delete(iTupleNode);
                iaTupleNodes[i] = 0;
                iValidCount--;
            }
        }
        
        if (iValidCount != iaTupleNodes.length) {
            int[] iaResult = new int[iValidCount];
            
            for (int i = 0, j = 0; i < iaTupleNodes.length; i++)
            {
                int iTuple = iaTupleNodes[i];
                
                if (iTuple != 0) {
                    iaResult[j++] = iTuple;
                }
            }
            
            return iaResult;
        } else {
            return iaTupleNodes;
        }
    }
    
    /**
     * Returns the entry name from the &lt;entry&gt; element dn attribute. 
     * @param iEntryNode LDAP &lt;entry&gt; element.
     * @return Entry name.
     */
    protected static String getEntryName(int iEntryNode) {
        String sDN = Node.getAttribute(iEntryNode, "dn", "");
        
        if (! sDN.startsWith("cn=")) {
            return null;
        }
        
        int iEnd = sDN.indexOf(',');
        
        if (iEnd <= 2) {
            return null;
        }
        
        return sDN.substring(3, iEnd);
    }
    
    /**
     * @see com.cordys.tools.ant.cm.ContentHandler#getSingleContentName(java.io.File, ContentManagerTask, Content, boolean)
     */
    public String getSingleContentName(File contentFile, ContentManagerTask cmtTask, Content content, boolean toEcx) throws IOException
    {
        if (contentFile == null || ! contentFile.exists()) {
            return null;
        }
        
        File baseDir = content.getDir();
        
        if (baseDir == null) {
            Vector<FileSet> v = content.getFileSet();
            
            if (v != null) {
                for (Iterator<FileSet> iter = v.iterator(); iter.hasNext();)
                {
                    FileSet fs = iter.next();
                    
                    if ((baseDir = fs.getDir(cmtTask.getProject())) != null) {
                        break;
                    }
                }
            }
            
            if (baseDir == null) {
                return null;
            }
        }
        
        String name = FileUtil.getRelativePath(baseDir, contentFile);
        
        name = name.replace('\\', '/');
        name = name.replaceFirst("^(.*)\\.xml$", "$1");

        return name;
    }        
}
