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

import com.cordys.tools.ant.soap.ISoapRequest;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.ContentTree;
import com.cordys.tools.ant.util.ContentTree.TreeNode;
import com.cordys.tools.ant.util.FileUtil;
import com.cordys.tools.ant.util.GeneralUtils;
import com.cordys.tools.ant.util.XMLUtils;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

/**
 * The class handles the content of type xmlstore and features used
 * commonly in XML Store kind of contents.
 *
 * @author msreejit
 */
public class XMLStoreHandler
    implements ContentHandler
{
    /**
     * Stores XML Document used for XML manipulation
     */
    private static Document document = new Document();
    /**
     * Stores the instance of the FileUtils.
     */
    private static FileUtils fileUtils = FileUtils.newFileUtils();
    /**
     * Contains the Content element that this class is currently
     * processing.
     */
    protected Content cCurrentContent;
    /**
     * Contains file extensions that are not pretty printed when the
     * contents are written to a file.
     */
    protected Map<String, Boolean> mNonFormatedFileTypeMap = new HashMap<String, Boolean>();
    /**
     * Number of processed items.
     */
    protected int iProcessedCounter;
    /**
     * Stores the root content tag of the content.
     */
    private String contentRootTag = "xmlcontents";
    /**
     * Stores the content tag of each content.
     */
    private String contentTag = "xmlcontent";
    /**
     * XML pattern to be matched against the ISV XML.
     */
    private String isvContentPattern = "?<xmlstore><><><><tuple>";
    /**
     * Stores the message logged when no content is found to delete
     * from ECX.
     */
    private String msgDeleteContentNotFound = "No xmlstore contents found in ECX to delete.";
    /**
     * Stores the message logged when delete from ECX is successful.
     */
    private String msgDeleteSucess = "Successfully deleted xmlstore contents from ECX.";
    /**
     * Stores the message logged when export from ECX is successful.
     */
    private String msgEcxToFileSucess = "Successfully exported xmlstore contents from ECX.";
    /**
     * Stores the message logged when no content is found to export
     * from ECX.
     */
    private String msgExportContentNotFound = "No xmlstore contents found to export from ECX.";
    /**
     * Stores the message logged when import to ECX is successful.
     */
    private String msgFileToEcxSucess = "Successfully imported xmlstore contents to ECX.";
    /**
     * Stores the message logged when no content is found to import
     * from ECX.
     */
    private String msgImportContentNotFound = "No xmlstore contents found to import from ECX.";
    /**
     * The XMLStore key handled by the handler.
     */
    private String xmlStoreKey = "";
    /**
     * If true, export files are put under the destination folder with
     * a full XMLSTore path.  Otherwise the contents are relative to
     * 'xmlStoreKey'.
     */
    private boolean bUseFullXMLStorePath = true;
    /**
     * The flag which sets whether the content is to be formatted or
     * not.
     */
    private boolean isXMLFormatted = true;

/**
     * Default constructor
     */
    protected XMLStoreHandler()
    {
        super();

        mNonFormatedFileTypeMap.put("caf", Boolean.TRUE);
    }

    /**
     * Increments the number of processed items.
     */
    public void addProcessItem()
    {
        iProcessedCounter++;
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
            int deleteCount = deleteOrganizationContents(xmlStoreKey, task,
                                                         soapRequestManager);

            task.log((deleteCount == 0)
                     ? ("[" + cCurrentContent.getType() + "] " +
                     msgDeleteContentNotFound)
                     : ("[" + cCurrentContent.getType() + "] " +
                     msgDeleteSucess), Project.MSG_INFO);
            iProcessedCounter += deleteCount;
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
        String xmlStoreContents = "";

        setXMLFormatted(content.isFormatOutput());

        try
        {
            //if isvdn has been specified execute related method.
            if (task.getIsvdn() != null)
            {
                xmlStoreContents = getIsvXMLStoreContents(isvContentPattern,
                                                          task,
                                                          soapRequestManager);
            }
            else
            {
                xmlStoreContents = getOrganizationContents(xmlStoreKey, task,
                                                           soapRequestManager);
            }

            if (xmlStoreContents.equals(""))
            {
                task.log("[" + cCurrentContent.getType() + "] " +
                         msgExportContentNotFound, Project.MSG_INFO);

                return;
            }

            stringBuffer.append("<");
            stringBuffer.append(contentRootTag);
            stringBuffer.append(">");
            stringBuffer.append(xmlStoreContents);
            stringBuffer.append("</");
            stringBuffer.append(contentRootTag);
            stringBuffer.append(">");

            //parse the contents
            int developerContentNode = document.parseString(stringBuffer.toString());
            modifyContentBeforeWriteToFolder(developerContentNode, soapRequestManager);

            //check if the directory has been specified.
            if (content.getDir() != null)
            {
                writeContentsToFolder(content.getDir(),
                                      Node.clone(developerContentNode, true),
                                      "<" + contentRootTag + "><" + contentTag +
                                      ">", task);
            }

            if (content.getContentFile() != null)
            {
                String xmlcontents = Node.writeToString(developerContentNode,
                                                        isXMLFormatted());

                //write the Developer Content to the content file 
                FileUtil.writeToFile(XMLUtils.XML_FILE_PREFIX + xmlcontents,
                                     content.getContentFile().getAbsolutePath());
            }

            task.log("[" + cCurrentContent.getType() + "] " +
                     msgEcxToFileSucess, Project.MSG_INFO);
        }
        catch (UnsupportedEncodingException ue)
        {
            GeneralUtils.handleException("Error occured while performing xml operation.\n" +
                                         ue.getMessage(), ue, task);
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
     * @param task DOCUMENTME
     * @param content DOCUMENTME
     * @param soapRequestManager DOCUMENTME
     */
    public void executeFileToEcx(ContentManagerTask task, Content content,
                                 ISoapRequestManager soapRequestManager)
    {
        cCurrentContent = content;

        int updateXMLNode = 0;
        File contentFile = content.getContentFile();
        Document doc = soapRequestManager.getDocument();

        try
        {
            //check if the file has been specified.
            if (contentFile != null)
            {
                if (!contentFile.exists())
                {
                    GeneralUtils.handleException("File does not exist!\nFile:" +
                                                 contentFile.getAbsolutePath());
                }

                int contentRootNode = doc.load(contentFile.getAbsolutePath());

                //check for root tag.
                if (!contentRootTag.equals(Node.getName(contentRootNode)))
                {
                    GeneralUtils.handleException("Root tag '" + contentRootTag +
                                                 "' for the content is missing!\nFile:" +
                                                 contentFile.getAbsolutePath());
                }

                updateXMLNode = document.load(contentFile.getAbsolutePath());
            }
            else
            {
                // collect filesets to pass them to extractXMLContents
                Vector<?> filesets = content.getFileSet();
                Vector<FileSet> vfss = new Vector<FileSet>();

                if (content.getDir() != null)
                {
                    if (!content.getDir().exists())
                    {
                        GeneralUtils.handleException("Folder does not exist!\nFolder:" +
                                                     content.getDir()
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

                String developerContents = extractXMLContents(fss,
                                                              contentRootTag,
                                                              contentTag, task);

                if (developerContents.equals(""))
                {
                    task.log("[" + cCurrentContent.getType() + "] " +
                             msgImportContentNotFound, Project.MSG_INFO);
                    return;
                }

                updateXMLNode = document.parseString(developerContents);
                modifyContentBeforeUpdateXmlStoreContent(updateXMLNode, soapRequestManager);
                addProcessItem();
            }

            updateXMLStoreContents(Node.clone(updateXMLNode, true), task,
                                   content, soapRequestManager);

            task.log("[" + cCurrentContent.getType() + "] " +
                     msgFileToEcxSucess, Project.MSG_INFO);
        }
        catch (UnsupportedEncodingException ue)
        {
            GeneralUtils.handleException("Error occured while performing xml operation.\n" +
                                         ue.getMessage(), ue, task);
        }
        catch (SoapRequestException sre)
        {
            GeneralUtils.handleException("Error occured while sending request to ECX.\n" +
                                         sre.getMessage(), sre, task);
        }
        catch (XMLException xe)
        {
            GeneralUtils.handleException("Error occured while performing xml file operation.\n" +
                                         xe.getMessage(), xe, task);
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
        cmtTask.log("[" + cCurrentContent.getType() + "] Content of type " +
                    cContent.getType() +
                    " does not need any publishing to runtime.",
                    Project.MSG_INFO);
    }

    /**
     * Returns the root content tag of the content.
     *
     * @return The root content tag of the content.
     */
    public String getContentRootTag()
    {
        return contentRootTag;
    }

    /**
     * Returns the content tag of the content.
     *
     * @return The content tag of the content.
     */
    public String getContentTag()
    {
        return contentTag;
    }

    /**
     * Returns the XML pattern to be matched against the ISV XML.
     *
     * @return XML pattern to be matched against the ISV XML.
     */
    public String getIsvContentPattern()
    {
        return isvContentPattern;
    }

    /**
     * Returns the message logged when no content is found to delete
     * from ECX.
     *
     * @return The message string.
     */
    public String getMsgDeleteContentNotFound()
    {
        return msgDeleteContentNotFound;
    }

    /**
     * Returns the message logged when delete from ECX is successful.
     *
     * @return The message string.
     */
    public String getMsgDeleteSucess()
    {
        return msgDeleteSucess;
    }

    /**
     * Returns the message logged when export from ECX is successful.
     *
     * @return The message string.
     */
    public String getMsgEcxToFileSucess()
    {
        return msgEcxToFileSucess;
    }

    /**
     * Returns the message logged when no content is found to export
     * from ECX.
     *
     * @return The message string.
     */
    public String getMsgExportContentNotFound()
    {
        return msgExportContentNotFound;
    }

    /**
     * Returns the message logged when import to ECX is successful.
     *
     * @return The message string.
     */
    public String getMsgFileToEcxSucess()
    {
        return msgFileToEcxSucess;
    }

    /**
     * Return the message logged when no content is found to import
     * from ECX.
     *
     * @return The message string.
     */
    public String getMsgImportContentNotFound()
    {
        return msgImportContentNotFound;
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
     * Returns the full path usage status.
     *
     * @return The full path usage status.
     */
    public boolean getUseFullPath()
    {
        return bUseFullXMLStorePath;
    }

    /**
     * Returns the XML store Key which is handled by this handler.
     *
     * @return The XML Store Key.
     */
    public String getXmlStoreKey()
    {
        return xmlStoreKey;
    }

    /**
     * Returns the isXMLFormatted.
     *
     * @return Returns the isXMLFormatted.
     */
    public boolean isXMLFormatted()
    {
        return isXMLFormatted;
    }

    /**
     * Returns the isXMLFormatted status based on a key.
     *
     * @param sKey They key.
     *
     * @return Returns the isXMLFormatted for the given key.
     */
    public boolean isXMLFormatted(String sKey)
    {
        if (!isXMLFormatted)
        {
            return false;
        }

        // Check the file type.		
        int iPos = sKey.lastIndexOf('.');

        if ((iPos != -1) && (iPos < (sKey.length() - 1)))
        {
            String sFileExt = sKey.substring(iPos + 1).toLowerCase();

            if (mNonFormatedFileTypeMap.containsKey(sFileExt))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets the root content tag of the content.
     *
     * @param string The root content tag of the content.
     */
    public void setContentRootTag(String string)
    {
        contentRootTag = string;
    }

    /**
     * Sets the content tag of the content.
     *
     * @param string
     */
    public void setContentTag(String string)
    {
        contentTag = string;
    }

    /**
     * Sets the XML pattern to be matched against the ISV XML.
     *
     * @param string XML pattern to be matched
     */
    public void setIsvContentPattern(String string)
    {
        isvContentPattern = string;
    }

    /**
     * Sets the message logged when no content is found to delete from
     * ECX.
     *
     * @param string The message string.
     */
    public void setMsgDeleteContentNotFound(String string)
    {
        msgDeleteContentNotFound = string;
    }

    /**
     * Sets the message logged when delete from ECX is successful.
     *
     * @param string The message string.
     */
    public void setMsgDeleteSucess(String string)
    {
        msgDeleteSucess = string;
    }

    /**
     * Sets the message logged when export from ECX is successful.
     *
     * @param string The message string.
     */
    public void setMsgEcxToFileSucess(String string)
    {
        msgEcxToFileSucess = string;
    }

    /**
     * Sets the message logged when no content is found to export from
     * ECX.
     *
     * @param string The message string.
     */
    public void setMsgExportContentNotFound(String string)
    {
        msgExportContentNotFound = string;
    }

    /**
     * Sets the message logged when import to ECX is successful.
     *
     * @param string The message string.
     */
    public void setMsgFileToEcxSucess(String string)
    {
        msgFileToEcxSucess = string;
    }

    /**
     * Sets the message logged when no content is found to import from
     * ECX.
     *
     * @param string The message string.
     */
    public void setMsgImportContentNotFound(String string)
    {
        msgImportContentNotFound = string;
    }

    /**
     * If set true, export files are put under the destination folder
     * with a full XMLSTore path.  Otherwise the contents are relative to the
     * XMLStore key.
     *
     * @param bValue Parameter value.
     */
    public void setUseFullPath(boolean bValue)
    {
        bUseFullXMLStorePath = bValue;
    }

    /**
     * Sets XML pretty print status when writing files.
     *
     * @param isXMLFormatted The isXMLFormatted to set.
     */
    public void setXMLFormatted(boolean isXMLFormatted)
    {
        this.isXMLFormatted = isXMLFormatted;
    }

    /**
     * Sets the XML store Key which is handled by this handler.
     *
     * @param string The XML Store Key.
     */
    public void setXmlStoreKey(String string)
    {
        xmlStoreKey = string;
    }

    /**
     * Gets the XMLStore contents for an organization and deletes them.
     *
     * @param folderName Folder path of the organizational contents.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @return The number of contents found and deleted.
     *
     * @throws SoapRequestException
     */
    protected int deleteOrganizationContents(String folderName,
                                             final ContentManagerTask task,
                                             final ISoapRequestManager soapRequestMgr)
                                      throws SoapRequestException
    {
        int deleteCount = 0;

        task.log("[" + cCurrentContent.getType() +
                 "] Deleting XML Store contents in folder " + folderName,
                 Project.MSG_VERBOSE);

        int folderNode = soapRequestMgr.getDocument()
                                       .createTextElement("folder", folderName);
        Node.setAttribute(folderNode, "recursive", "true");
        Node.setAttribute(folderNode, "version", "organization");

        //Sends request. Gets all the menus for the organization
        int responseNode = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                          task.getOrganization(),
                                                          "http://schemas.cordys.com/1.0/xmlstore",
                                                          "GetCollection",
                                                          folderNode);

        //check the response for Soap Fault.
        GeneralUtils.handleException(responseNode);

        //Gets a collection of the tuples tag. 
        int[] tupleNodes = Find.match(responseNode,
                                      "?<GetCollectionResponse><tuple>");

        // Create a new content tree node so we can recurse the tree depth-first.
        ContentTree ctTree = new ContentTree(folderName, '/');
        final Object oFilteredMarker = new Object();

        if (tupleNodes.length > 0)
        {
            for (int k = 0; k < tupleNodes.length; k++)
            {
                int iNode = tupleNodes[k];
                String sKey = Node.getAttribute(iNode, "key");

                if (!cCurrentContent.isPathAccepted(sKey))
                {
                    // Mark the key as being filtered out, so we don't try to delete 
                    // the parent folder.
                    ctTree.addElement(sKey, oFilteredMarker);
                    continue;
                }

                ctTree.addElement(sKey, new Integer(iNode));
            }
        }

        // Create deletion callback
        ContentTree.IterationCallback icDeleteCallback = new ContentTree.IterationCallback()
        {
            public int iDeleteCount = 0;

            public String toString()
            {
                return "" + iDeleteCount;
            }

            public boolean handleElement(TreeNode tnNode)
                                  throws Exception
            {
                if ((tnNode == null) || (tnNode.getData() == null) || (tnNode.getData() == oFilteredMarker))
                {
                    // Invalid, already deleted or filtered node.
                    return true;
                }

                int iNode = ((Integer) tnNode.getData()).intValue();

                /* The SOAP message given below deletes an XML Object from the XML Store.
                 * <UpdateXMLObject xmlns="http://schemas.cordys.com/1.0/xmlstore">
                 *                 <tuple key="/cordys/wcp/emp" lastModified="1006212444000"
                 *                         recursive="true" />
                 * </UpdateXMLObject>
                 */
                boolean bIsFolder = Find.firstMatch(iNode,
                                                    "<tuple isFolder=\"true\">") != 0;
                String sKey = tnNode.getKey();
                
                if (bIsFolder)
                {
                    List<TreeNode> lChildList = tnNode.getChildren();

                    // Check if we have deleted all children
                    for (Iterator<TreeNode> iIter = lChildList.iterator();
                             iIter.hasNext();)
                    {
                        TreeNode tnChildNode = (TreeNode) iIter.next();

                        if (tnChildNode.getData() != null)
                        {
                            // This one wasn't deleted.
                            return true;
                        }
                    }

                    // All children were deleted so we can delete the folder as well.
                }
                else
                {
                    // Check for filtering.
                    if (!cCurrentContent.isPathAccepted(sKey))
                    {
                        return true;
                    }
                }

                task.log("[" + cCurrentContent.getType() +
                         "] Deleting xmlstore content with key:" + sKey,
                         Project.MSG_VERBOSE);

                int iResponseNode;

                iResponseNode = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                               task.getOrganization(),
                                                               "http://schemas.cordys.com/1.0/xmlstore",
                                                               "UpdateXMLObject",
                                                               iNode);

                /*
                 * Ignore Soap Fault.
                 * The error message "Key does not exist/Unable to delete the object since it is an ISV version"
                 * will definitely be encountered for folders.
                 */
                if (!bIsFolder)
                {
                    GeneralUtils.handleException(iResponseNode);
                }

                // Indicate that this has been deleted.
                tnNode.setData(null);

                iDeleteCount++;

                return true;
            }
        };

        try
        {
            // Delete content in depth-first fashion
            ctTree.recurseDepthFirst(icDeleteCallback);
        }
        catch (Exception e)
        {
            GeneralUtils.handleException("Exception while deleting content.",
                                         e, task);
        }

        deleteCount = Integer.parseInt(icDeleteCallback.toString());

        return deleteCount;
    }

    /**
     * Extracts the XML contents from the files specified in the
     * fileset.
     *
     * @param filesets The filesets which specify the set of files to be
     *        scanned for.
     * @param contentRootTag The name of the root tag for content xml creation.
     * @param contentTag The name of the tag for content xml creation.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     *
     * @return The content xml with the root tag specified and the individual
     *         contents wrapped with content tag. Will return empty string if
     *         no content if found.
     */
    protected String extractXMLContents(FileSet[] filesets,
                                        String contentRootTag,
                                        String contentTag,
                                        ContentManagerTask task)
    {
        StringBuffer stringBuffer = new StringBuffer();
        String contents = "";

        // deal with the filesets
        for (int i = 0; i < filesets.length; i++)
        {
            FileSet fs = filesets[i];
            DirectoryScanner ds = fs.getDirectoryScanner(task.getProject());
            String[] srcFiles = ds.getIncludedFiles();
            File baseDir = ds.getBasedir();

            contents += extractXMLContents(srcFiles, baseDir, contentTag, task);
        }

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
     * Method for internal use.
     *
     * @param contentFilePaths The files which have been included in the
     *        Fileset scan.
     * @param baseDir The base directory in which the files have been scanned.
     * @param contentTag The name of the tag for content xml creation.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     *
     * @return The content xml in string format.
     */
    protected String extractXMLContents(String[] contentFilePaths,
                                        File baseDir, String contentTag,
                                        ContentManagerTask task)
    {
        int xmlNode = 0;
        StringBuffer stringBuffer = new StringBuffer();

        for (int j = 0; j < contentFilePaths.length; j++)
        {
            File contentFile = new File(baseDir, contentFilePaths[j]);
            String key = fileUtils.removeLeadingPath(baseDir, contentFile)
                                  .replaceAll("\\\\", "/");
            String sRelativeKey = key;

            // Add the base path if necessary.
            if (!bUseFullXMLStorePath)
            {
                key = xmlStoreKey + "/" + key;
            }

            if (!cCurrentContent.isPathAccepted(sRelativeKey))
            {
                continue;
            }

            task.log("[" + cCurrentContent.getType() +
                     "] Parsing Content File:" + contentFile.getAbsolutePath(),
                     Project.MSG_VERBOSE);

            try
            {
                xmlNode = document.load(contentFile.getAbsolutePath());
            }
            catch (XMLException e)
            {
                throw new BuildException("[" + cCurrentContent.getType() +
                         "] File not well formed! Ignoring file: " +
                         contentFile.getAbsolutePath() +
                         "\nUse exclude statements in filesets to ignore unwanted files.");
            }

            if (xmlNode != 0)
            {
                stringBuffer.append("<");
                stringBuffer.append(contentTag);
                stringBuffer.append(" key='");
                stringBuffer.append(key);
                stringBuffer.append("' name='");
                stringBuffer.append(contentFile.getName());
                stringBuffer.append("'>");
                stringBuffer.append(Node.writeToString(xmlNode, isXMLFormatted()));
                stringBuffer.append("</");
                stringBuffer.append(contentTag);
                stringBuffer.append(">");
                Node.delete(xmlNode);
                xmlNode = 0;
            }
        }

        return stringBuffer.toString();
    }

    /**
     * Sends the request for the ISV Package details and extracts
     * details specified  by matching the xml search pattern specified as
     * parameter.
     *
     * @param patternInISV xml search pattern to match in ISV xml of ISV
     *        Package.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @return The ISV contents with respective matched tags as string.
     *
     * @throws SoapRequestException
     */
    protected String getIsvXMLStoreContents(String patternInISV,
                                            ContentManagerTask task,
                                            ISoapRequestManager soapRequestMgr)
                                     throws SoapRequestException
    {
        task.log("[" + cCurrentContent.getType() +
                 "] Retrieving ISV package contents...", Project.MSG_DEBUG);

        StringBuffer stringBuffer = new StringBuffer();
        soapRequestMgr.getDocument().createTextElement("dn", task.getIsvdn());

        //Get the name of the ISVP file.
        String cn = task.getIsvfile();

        int fileNode = soapRequestMgr.getDocument().createTextElement("file", cn);
        Node.setAttribute(fileNode, "type", "isvpackage");

        /*
         * Sends the request to the ISVPackageDefinition in order to get
         * the menu contents because no method is available to get the
         * menus of a particular ISV from XMLStore.
         */
        int responseMsg = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                         task.getOrganization(),
                                                         "http://schemas.cordys.com/1.0/isvpackage",
                                                         "GetISVPackageDefinition",
                                                         fileNode);

        //check the response for Soap Fault.
        GeneralUtils.handleException(responseMsg);

        //Gets the collection of tuples
        int[] xmlNodes;

        xmlNodes = Find.match(responseMsg, patternInISV);

        //Appends the tuples to the string buffer
        for (int j = 0; j < xmlNodes.length; j++)
        {
            int tupleNode = xmlNodes[j];
            String key = Node.getAttribute(tupleNode, "key", "");
            String name = key.replaceFirst(".*/([^/]+)$", "$1");

            if (key.equals(""))
            {
                continue;
            }

            if ((xmlStoreKey != null) && (xmlStoreKey.length() > 0))
            {
                if (!key.startsWith(xmlStoreKey))
                {
                    continue;
                }
            }

            if (!cCurrentContent.isPathAccepted(key))
            {
                continue;
            }

            int iContentNode = Find.firstMatch(tupleNode, "<><><>");

            if (iContentNode == 0)
            {
                continue;
            }

            //Appends the contents of the xml contents to the string buffer with content tags
            stringBuffer.append("<");
            stringBuffer.append(contentTag);
            stringBuffer.append(" key='");
            stringBuffer.append(key);
            stringBuffer.append("' name='");
            stringBuffer.append(name);
            stringBuffer.append("'>");
            stringBuffer.append(Node.writeToString(iContentNode,
                                                   isXMLFormatted(key)));
            stringBuffer.append("</");
            stringBuffer.append(contentTag);
            stringBuffer.append(">");
        }

        return stringBuffer.toString();
    }

    /**
     * Gets all the organization contents as a string with the tuple
     * tags.
     *
     * @param folderName Folder path of the organizational contents.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @return The organizational contents of the xmlstore in string format.
     *
     * @throws SoapRequestException
     */
    protected String getOrganizationContents(String folderName,
                                             ContentManagerTask task,
                                             ISoapRequestManager soapRequestMgr)
                                      throws SoapRequestException
    {
        task.log("[" + cCurrentContent.getType() +
                 "] Retrieving Organization contents...", Project.MSG_DEBUG);

        StringBuffer stringBuffer = new StringBuffer();

        //send request to get all the xmlstore contents for the organization
        int folderNode = soapRequestMgr.getDocument()
                                       .createTextElement("folder", folderName);
        Node.setAttribute(folderNode, "recursive", "true");
        Node.setAttribute(folderNode, "version", "organization");

        int responseMsg = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                         task.getOrganization(),
                                                         "http://schemas.cordys.com/1.0/xmlstore",
                                                         "GetCollection",
                                                         folderNode);

        //check the response for Soap Fault.
        GeneralUtils.handleException(responseMsg);

        //Gets a collection of the tuples tag. 
        int[] node = Find.match(responseMsg, "?<GetCollectionResponse><tuple>");
        int[] xmlNodes;
        boolean hasContent = false;

        //Iterates through the tuple tag, in order to get the contents of each menu
        for (int i = 0; i < node.length; i++)
        {
            ISoapRequest soapRequest = soapRequestMgr.createSoapRequest();
            
            soapRequest.setUser(task.getUserdn());
            
            //Checks if the XMLStore object is a folder
            if ("true".equals(Node.getAttribute(node[i], "isFolder")))
            {
                // Ignore folders.
                continue;
            }

            String sKey = Node.getAttribute(node[i], "key");
            String sRelativeKey = sKey;

            if (!bUseFullXMLStorePath)
            {
                sRelativeKey = sRelativeKey.substring(xmlStoreKey.length());
            }

            if (!cCurrentContent.isPathAccepted(sRelativeKey))
            {
                continue;
            }

            int keyNode = soapRequestMgr.getDocument().createTextElement("key", sKey);
            soapRequest.addMethod(task.getOrganization(),
                                  "http://schemas.cordys.com/1.0/xmlstore",
                                  "GetXMLObject", keyNode);
            

            //execute the request.
            responseMsg = soapRequest.execute();

            //check the response for Soap Fault.
            GeneralUtils.handleException(responseMsg);
            
            //Matches for the xml content tag within the response
            xmlNodes = Find.match(responseMsg, "?<GetXMLObjectResponse><tuple><><>");

            String key;
            String name;

            for (int j = 0; j < xmlNodes.length; j++)
            {
                int tupleNode = Node.getParent(Node.getParent(xmlNodes[j]));
                key = Node.getAttribute(tupleNode, "key", "");
                name = Node.getAttribute(tupleNode, "name", "");

                //Appends the contents of the xml contents to the string buffer with content tags
                stringBuffer.append("<");
                stringBuffer.append(contentTag);
                stringBuffer.append(" key='");
                stringBuffer.append(key);
                stringBuffer.append("' name='");
                stringBuffer.append(name);
                stringBuffer.append("'>");
                stringBuffer.append(Node.writeToString(xmlNodes[j],
                                                       isXMLFormatted(key)));
                stringBuffer.append("</");
                stringBuffer.append(contentTag);
                stringBuffer.append(">");
            }
            
            hasContent = true;
        }
        
        if (! hasContent) {
            return "";
        }

        return stringBuffer.toString();
    }

    /**
     * DOCUMENTME
     *
     * @param sObjectKey DOCUMENTME
     * @param task DOCUMENTME
     * @param soapRequestMgr DOCUMENTME
     *
     * @return DOCUMENTME
     *
     * @throws SoapRequestException DOCUMENTME
     */
    protected int getOrganizationXmlObject(String sObjectKey,
                                           ContentManagerTask task,
                                           ISoapRequestManager soapRequestMgr)
                                    throws SoapRequestException
    {
        //send request to get the xmlstore content for this key
        int iKeyNode = soapRequestMgr.getDocument()
                                     .createTextElement("key", sObjectKey);
        Node.setAttribute(iKeyNode, "version", "organization");

        int responseMsg = soapRequestMgr.makeSoapRequest(task.getUserdn(),
                                                         task.getOrganization(),
                                                         "http://schemas.cordys.com/1.0/xmlstore",
                                                         "GetXMLObject",
                                                         iKeyNode);

        try
        {
            //check the response for Soap Fault.
            GeneralUtils.handleException(responseMsg);
        }
        catch (BuildException e)
        {
            Node.delete(responseMsg);
            return 0;
        }

        //Gets a collection of the tuples tag. 
        int iResponseNode = Find.firstMatch(responseMsg,
                                            "?<GetXMLObjectResponse><tuple>");

        ISoapRequest soapRequest = soapRequestMgr.createSoapRequest();
        soapRequest.setUser(task.getUserdn());

        return iResponseNode;
    }

    /**
     * This method can be overloaded to change the content before
     * updating the XMLStore.
     *
     * @param updateXMLNode The XML content.
     */
    protected void modifyContentBeforeUpdateXmlStoreContent(int updateXMLNode, ISoapRequestManager srmSoapRequestManager)
    {
    }

    /**
     * This method can be overwritten to add logic before content is
     * written to a file.
     *
     * @param contentNode Content node
     */
    protected void modifyContentBeforeWriteToFolder(int contentNode, ISoapRequestManager srmSoapRequestManager)
    {
    }

    /**
     * Updates the contents to the XML Store
     *
     * @param xmlNode
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     * @param cContent The current content.
     * @param soapRequestMgr The instance of SoapRequestManager class for
     *        handling soap requests.
     *
     * @throws SoapRequestException
     */
    protected void updateXMLStoreContents(int xmlNode, ContentManagerTask task,
                                          Content cContent,
                                          ISoapRequestManager soapRequestMgr)
                                   throws SoapRequestException
    {
        if (xmlNode != 0)
        {
            int[] xmlNodes = Find.match(xmlNode,
                                        "<" + contentRootTag + "><" +
                                        contentTag + ">");

            if (xmlNodes.length > 0)
            {
                //Iterate through the xml nodes collection
                for (int i = 0; i < xmlNodes.length; i++)
                {
                    ISoapRequest soapRequest = soapRequestMgr.createSoapRequest();
                    soapRequest.setUser(task.getUserdn());
                    
                    String sKey = Node.getAttribute(xmlNodes[i], "key", "");

                    task.log("[" + cContent.getType() + "] Synchronizing " +
                             sKey, Project.MSG_INFO);

                    int tupleNode = document.createElement("tuple");
                    Node.setAttribute(tupleNode, "key", sKey);
                    Node.setAttribute(tupleNode, "name",
                                      Node.getAttribute(xmlNodes[i], "name", ""));
                    Node.setAttribute(tupleNode, "level", "organization");
                    Node.setAttribute(tupleNode, "unconditional", "true");

                    //PGUSSOW: Fixed a bug with regard to the creation of the update-request. 
                    //The data wasn't appended to the new-node, but to the tuple-node.
                    int iNewNode = document.createElement("new", tupleNode);
                    Node.appendToChildren(Node.getFirstElement(xmlNodes[i]),
                                          iNewNode);

                    //append to the tuple to a soap method.
                    soapRequest.addMethod(task.getOrganization(),
                                          "http://schemas.cordys.com/1.0/xmlstore",
                                          "UpdateXMLObject", tupleNode);
                    
                    //send the soap request.
                    int responseMsg = soapRequest.execute();

                    //check the response for Soap Fault.
                    GeneralUtils.handleException(responseMsg);
                }
            }
        }
    }

    /**
     * Helper method for writing contents to a folder by providing the
     * root node and the the search path. The children of the node matched by
     * the search path  will be written to the folder specified by the key of
     * the xml content.
     *
     * @param contentFolder The content folder where xmlstore contents are
     *        placed.
     * @param developerContentNode The Developer content as a Node reference
     * @param searchPattern The xml search pattern to matched to located
     *        individual contents.
     * @param task The instance of the ContentManagerTask class for getting
     *        task related informations.
     *
     * @throws IOException
     */
    protected void writeContentsToFolder(File contentFolder,
                                         int developerContentNode,
                                         String searchPattern, Task task)
                                  throws IOException
    {
        //write each xml file to folder specified by the key
        int[] xmlstoreNodes = Find.match(developerContentNode, searchPattern);
        File xmlFile = null;

        String xmlcontents;

        for (int k = 0; k < xmlstoreNodes.length; k++)
        {
            String sFileKey = Node.getAttribute(xmlstoreNodes[k], "key");
            String sFilePath;

            task.log("[" + cCurrentContent.getType() + "] Writing " + sFileKey +
                     " to the local filesystem.", Project.MSG_INFO);

            // Create relative file path. Use the file key as-is. This should
            // work on Window and Linux.
            sFilePath = sFileKey;

            if (!bUseFullXMLStorePath)
            {
                sFilePath = sFilePath.substring(xmlStoreKey.length());
            }

            //create a new file representing the xml file.
            xmlFile = new File(contentFolder, sFilePath);

            //create the parent directories.
            xmlFile.getParentFile().mkdirs();

            task.log("[" + cCurrentContent.getType() +
                     "] Writing xml content to file:" +
                     xmlFile.getAbsolutePath(), Project.MSG_VERBOSE);

            xmlcontents = Node.writeToString(Node.getFirstChild(xmlstoreNodes[k]),
                                             isXMLFormatted(sFileKey));

            try
            {
                //write the Developer Content to the content file
                FileUtil.writeToFile(XMLUtils.XML_FILE_PREFIX + xmlcontents,
                                     xmlFile.getAbsolutePath());
                iProcessedCounter++;
            }
            catch (IOException e)
            {
                task.log("[" + cCurrentContent.getType() +
                         "] ERROR: Unable to write file: " +
                         xmlFile.getAbsolutePath() + e.toString(),
                         Project.MSG_ERR);
            }
        }
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
                    FileSet fs = (FileSet) iter.next();
                    
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

        return name;
    }        
}
