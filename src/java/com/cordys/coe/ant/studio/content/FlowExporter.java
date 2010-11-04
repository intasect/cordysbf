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
package com.cordys.coe.ant.studio.content;

import com.cordys.coe.ant.studio.task.StudioTaskHandler;
import com.cordys.coe.ant.studio.util.StudioInitializer;
import com.cordys.coe.util.xml.Message;
import com.cordys.coe.util.xml.MessageContext;

import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.NomCollector;
import com.cordys.tools.ant.util.GeneralUtils;

import com.eibus.util.Base64;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.tools.ant.Project;

/**
 * Imports Studio vcmdata files from local file system to ECX.
 *
 * @author  mpoyhone
 */
@SuppressWarnings("deprecation")
public class FlowExporter
{
    /**
     * The static document used in message templates.
     */
    protected static Document dRequestDoc = StudioInitializer.dRequestDoc;
    /**
     * The Studio GetColletion method SOAP message template.
     */
    protected static Message mMethodGetCollection;
    /**
     * The Studio GetObject method SOAP message template.
     */
    protected static Message mMethodGetObject;
    /**
     * The Studio Export method SOAP message template.
     */
    protected static Message mMethodExport;
    /**
     * The Studio DownloadFile method SOAP message template.
     */
    protected static Message mMethodDownloadFile;
    /**
     * The Studio DeleteFile method SOAP message template.
     */
    protected static Message mMethodDeleteFile;

    static
    {
        // Builds the needed SOAP messages on class load.
        buildMessages();
    }

    /**
     * Flag for checking the last modification time from the server and on the file.
     */
    protected boolean checkLastModified = false;
    /**
     * The build framework task that calls this object.
     */
    protected ContentManagerTask cmtTask;
    /**
     * Destination directory for studio files.
     */
    protected File fDestDir;
    /**
     * A map containing requested versions. If this is null, all versions are exported.
     */
    protected Map<String, Boolean> mVersionMap = null;
    /**
     * Soap request manager.
     */
    protected ISoapRequestManager srmSoap;
    /**
     * The root CoBOC path in that is passed to the first GetCollection method.
     */
    protected String sStudioFlowRootDir;
    /**
     * Studio content handler.
     */
    protected StudioTaskHandler sthTask;

    /**
     * Creates a new FlowExporter object.
     *
     * @param  sStudioFlowRootDir  The root CoBOC path in that is passed to the first GetCollection
     *                             method.
     * @param  fDestDir            Destination directory for studio files.
     */
    public FlowExporter(String sStudioFlowRootDir, File fDestDir)
    {
        this.sStudioFlowRootDir = sStudioFlowRootDir;
        this.fDestDir = fDestDir;

        if (sStudioFlowRootDir.endsWith("/"))
        {
            this.sStudioFlowRootDir = sStudioFlowRootDir.substring(0,
                                                                   sStudioFlowRootDir.length() - 1);
        }
    }

    /**
     * Starts the export operation.
     *
     * @param   sthTask          The Studio content handler.
     * @param   cmtTask          The build framework task that calls this object.
     * @param   srmSoap          Soap manager object.
     * @param   sSoapUser        Calling user DN
     * @param   sOrganizationDN  Destination organization DN.
     *
     * @throws  StudioException  Thrown if the operation failed.
     */
    public void execute(StudioTaskHandler sthTask, ContentManagerTask cmtTask,
                        ISoapRequestManager srmSoap, String sSoapUser, String sOrganizationDN)
                 throws StudioException
    {
        this.cmtTask = cmtTask;
        this.sthTask = sthTask;
        this.srmSoap = srmSoap;

        try
        {
            srmSoap.addNomCollector(new NomCollector());

            String checkLastModifiedStr = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                                          StudioConstants.CHECK_LASTMODIFIED_PROPERTY);

            if (checkLastModifiedStr != null)
            {
                checkLastModifiedStr = checkLastModifiedStr.toLowerCase();

                if (!checkLastModifiedStr.equals("true") && !checkLastModifiedStr.equals("false"))
                {
                    throw new StudioException("Invalid value '" + checkLastModified +
                                              "' for property '" +
                                              StudioConstants.CHECK_LASTMODIFIED_PROPERTY + "'");
                }
                else if (checkLastModifiedStr.equals("true"))
                {
                    checkLastModified = true;

                    cmtTask.log("[studio-bpms] Over-writing only modified flows.",
                                Project.MSG_INFO);
                }
            }

            // Handle the Studio folder contents recursively.
            String rootFolder = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                                StudioConstants.STUDIO_ROOTFOLDER_PROPERTY);

            if (rootFolder.startsWith("/"))
            {
                rootFolder = rootFolder.substring(1);
            }

//            // Fix the destination folder if a sub path is used.
//            fDestDir = new File(fDestDir, rootFolder);

            // It could be that just a single '/' was entered as the root folder. Then the root
            // relative path should be an empty string.

            String tempRootFolder = sStudioFlowRootDir.substring(StudioConstants
                                                                 .DEFAULT_FLOW_ROOT_FOLDER
                                                                 .length());

            // We need to remove the / as well.
            if (rootFolder.length() > 1)
            {
                tempRootFolder = tempRootFolder.substring(1);
            }

            recurseFolders(sStudioFlowRootDir, "", tempRootFolder);
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to export studio flows.", e);
        }
        finally
        {
            srmSoap.removeNomCollector().deleteNodes();
        }
    }

    /**
     * Sets all versions that will be exported. Multiple versions are separated with a semi-colon (
     * ; ).
     *
     * @param  sVersions  Version string.
     */
    public void setExportVersions(String sVersions)
    {
        if ((sVersions == null) || (sVersions.length() == 0))
        {
            mVersionMap = null;

            return;
        }

        if (mVersionMap == null)
        {
            mVersionMap = new HashMap<String, Boolean>();
        }

        StringTokenizer tok = new StringTokenizer(sVersions, ";");

        while (tok.hasMoreTokens())
        {
            String sVersion = tok.nextToken().trim();

            if (sVersion.length() > 0)
            {
                mVersionMap.put(sVersion, Boolean.TRUE);
            }
        }
    }

    /**
     * Builds the needed SOAP messages when this class is loaded.
     *
     * @throws  IllegalStateException  Thrown if the message parsing failed.
     */
    protected static void buildMessages()
                                 throws IllegalStateException
    {
        String sXml = "";

        try
        {
            sXml = "<GetCollection xmlns='http://schemas.cordys.com/1.0/cas-vcm'>" +
                   "      <folder detail='false' recursive='false' version='organization' vcmApplication='vcmRepository' vcmVersion='vcmDefault'></folder>" +
                   "</GetCollection>";
            mMethodGetCollection = new Message(dRequestDoc, sXml);
            mMethodGetCollection.getSharedXmlTree().setReadOnly(true);

            sXml = "<GetObject xmlns='http://schemas.cordys.com/1.0/cas-vcm'>" +
                   "       <key version='organization' vcmApplication='vcmDefault' vcmVersion='vcmDefault'></key>" +
                   "</GetObject>";
            mMethodGetObject = new Message(dRequestDoc, sXml);
            mMethodGetObject.getSharedXmlTree().setReadOnly(true);

            sXml = "<Export xmlns='http://schemas.cordys.com/1.0/cas-vcm'>" +
                   "       <information>" +
                   "               <exportfilename>exporttest</exportfilename>" +
                   "               <filetypes key=''></filetypes>" +
                   "       <versions key=''></versions>" +
                   "       <modelrepositorycontents exportSubprocesses='true' exportUsedContent='true' exportSubDocuments='true'>" +
                   "          <models>" + "          </models>" +
                   "          <projects/>" + "          <workspaces/>" +
                   "        </modelrepositorycontents>" +
                   "        <casrepositorycontents>" + "          <vcmview/>" +
                   "          <bcmview/>" + "          <bpmview/>" +
                   "       </casrepositorycontents>" + "     </information>" +
                   "</Export>";
            mMethodExport = new Message(dRequestDoc, sXml);
            mMethodExport.getSharedXmlTree().setReadOnly(true);

            sXml = "<DownloadFile xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">\r\n" +
                   "	<filename></filename>\r\n" + "</DownloadFile>";
            mMethodDownloadFile = new Message(dRequestDoc, sXml);
            mMethodDownloadFile.getSharedXmlTree().setReadOnly(true);

            sXml = "<DeleteFiles xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">\r\n" +
                   "</DeleteFiles>";
            mMethodDeleteFile = new Message(dRequestDoc, sXml);
            mMethodDeleteFile.getSharedXmlTree().setReadOnly(true);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to parse the SOAP message " + sXml + " : " + e);
        }
    }

    /**
     * Creates flow vcmdata file name from the flow key.
     *
     * @param   sFlowKey  Flow key.
     *
     * @return  File name.
     */
    protected String createFlowFileName(String sFlowKey)
    {
        // The filename should be relative from the root path of the CoBOC repository. So removing
        // the default path should be enough to get the path needed for the vcmdata file.
        String sDestFileName = sFlowKey.substring(StudioConstants.DEFAULT_FLOW_ROOT_FOLDER.length() +
                                                  1);

        return sDestFileName.replaceFirst("\\.bpm$", ".vcmdata");
    }

    /**
     * Deletes the vcmdata file from server file system.
     *
     * @param   sType        Type type to be deleted ('import' or 'export').
     * @param   saFileNames  A table of file names
     *
     * @throws  Exception  Thrown if the operation failed.
     */
    protected void deleteFiles(String sType, String[] saFileNames)
                        throws Exception
    {
        Message mRequest = getMessageContext().createMessage(mMethodDeleteFile);
        Message mResponse;

        for (int i = 0; i < saFileNames.length; i++)
        {
            String sName = saFileNames[i];
            Message mFileElem = getMessageContext().createMessage(dRequestDoc,
                                                                  "<filename type='" + sType +
                                                                  "'>" + sName + "</filename>");

            mRequest.append(".", mFileElem);
        }

        mResponse = srmSoap.makeSoapRequest(mRequest);

        String sStatus = mResponse.getValue("//deleted", true);

        // It seems that BCP 4.2 build 23 patchtrain 6 (Used at Etam) returns a 1 instead of
        // true/false.
        if (!"true".equals(sStatus) && !"1".equals(sStatus))
        {
            throw new Exception("Unable to delete all export files from the server.");
        }
    }

    /**
     * Downloads the vcmdata file from server file system.
     *
     * @param   sFileName  File name
     *
     * @return  The file contents.
     *
     * @throws  Exception  Thrown if the operation failed.
     */
    protected String downloadFile(String sFileName)
                           throws Exception
    {
        Message mRequest = getMessageContext().createMessage(mMethodDownloadFile);
        Message mResponse;

        mRequest.setValue("//filename", sFileName);

        mResponse = srmSoap.makeSoapRequest(mRequest);

        return mResponse.getValue("//filedata", true);
    }

    /**
     * Exports the given flow to the file system.
     *
     * @param   sFlowKey  The flow key to be passed to the Export method.
     *
     * @throws  Exception  Thrown if the operation failed.
     */
    protected void exportFlow(String sFlowKey)
                       throws Exception
    {
        // Get the object status.
        Message mObjectRequest = getMessageContext().createMessage(mMethodGetObject);
        Message mObjectResponse;

        mObjectRequest.setValue(".//key", sFlowKey);

        mObjectResponse = srmSoap.makeSoapRequest(mObjectRequest);

        // Check that this is a correct flow object.
        if (Find.firstMatch(mObjectResponse.getXmlNode(), "?<tuple><old><bpm>") == 0)
        {
            // No, it wasn't.
            return;
        }

        // Get the properties needed for Export method.
        String sFlowName = mObjectResponse.getValue(".//bpm/documentProperties/name");

        // Create a template for folder entries and the bmp entry in the Export method XML data.
        Message mModel = getMessageContext().createMessage(dRequestDoc,
                                                           "<model type='' key=''></model>");
        Message mExportRequest = getMessageContext().createMessage(mMethodExport);
        Message mExportResponse;
        String sIterKey;
        LinkedList<String> lDirList = new LinkedList<String>();

        // Add the parent directories to a list so they can be added to the
        // message in the right order.
        sIterKey = sFlowKey.replaceFirst("(.*)/(.*)$", "$1");

        while (!sIterKey.equals(sStudioFlowRootDir))
        {
            lDirList.addFirst(sIterKey);

            sIterKey = sIterKey.replaceFirst("(.*)/(.*)$", "$1");

            if (!sIterKey.equals(sStudioFlowRootDir) && (sIterKey.length() == 0))
            {
                throw new IllegalArgumentException("The path is incorrect. Did you accidently include a / at the end?");
            }
        }

        // Add all folder information for the Export request.
        for (Iterator<String> iKeyIter = lDirList.iterator(); iKeyIter.hasNext();)
        {
            String sKey = iKeyIter.next();
            String sName;
            int iPos;

            // Find out the folder name.
            if ((iPos = sKey.lastIndexOf('/')) != -1)
            {
                sName = sKey.substring(iPos + 1);
            }
            else
            {
                sName = sKey;
            }

            mModel.setValue("./@type", "folder");
            mModel.setValue("./@key", sKey);
            mModel.setValue(".", sName);
            mExportRequest.append("//models", mModel);
        }

        // Append the flow key to the Export request.
        mModel.setValue("./@type", "bpm");
        mModel.setValue("./@key", sFlowKey);
        mModel.setValue(".", sFlowName);
        mExportRequest.append(".//models", mModel);

        // Set the subprocess export flag if specified.
        String sExportSubprocesses = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                                     StudioConstants.EXPORT_SUBPROCESSES_PROPERTY);

        if (sExportSubprocesses != null)
        {
            sExportSubprocesses = sExportSubprocesses.toLowerCase();

            if (!sExportSubprocesses.equals("true") && !sExportSubprocesses.equals("false"))
            {
                throw new StudioException("Invalid value '" + sExportSubprocesses +
                                          "' for property '" +
                                          StudioConstants.EXPORT_SUBPROCESSES_PROPERTY + "'");
            }

            mExportRequest.setValue("./information/modelrepositorycontents/@exportSubprocesses",
                                    sExportSubprocesses);
            mExportRequest.setValue("./information/modelrepositorycontents/@exportSubDocuments",
                                    sExportSubprocesses);
        }

        // Send the export request.
        mExportResponse = srmSoap.makeSoapRequest(mExportRequest);
        String sReceiver = mExportResponse.getParent().getParent().getValue(".//reply-to",true);
        
        // Create the destination file name
        String sDestFileName = createFlowFileName(sFlowKey);

        String sExportFile = mExportResponse.getValue("ExportResponse/@key", true);
        File fDestFile = new File(fDestDir, sDestFileName);
        File fDestFileParentDir = fDestFile.getParentFile();

        // Create the destination directory if needed.
        if (!fDestFileParentDir.exists())
        {
            fDestFileParentDir.mkdirs();
        }
        
        //set the Receiver (fix for  export, download, delete sequence issue in the case of clustered environment)
        srmSoap.setReceiver(sReceiver);
        // Download the file
        String sFileData = downloadFile(sExportFile);

        // The file is a base64 encoded zip file. Decode it and write it to the destination file.
        // We are assuming that the files are relatively small.
        FileOutputStream fosOut = null;

        try
        {
            fosOut = new FileOutputStream(fDestFile);

            if (sFileData.length() <= 0)
            {
                throw new IOException("The export file is empty.");
            }

            // Convert file contents to char array.
            char[] caData = sFileData.toCharArray();

            // Decode the data
            byte[] baDecodedData = Base64.decode(caData);

            // Write the decoded data to the destination file.
            fosOut.write(baDecodedData);
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to read the exported file '" + sExportFile + "'", e);
        }
        finally
        {
            if (fosOut != null)
            {
                try
                {
                    fosOut.close();
                }
                catch (Exception ignored)
                {
                }
            }
        }

        cmtTask.log("[studio-bpms] Exported flow " + sDestFileName + " to file " +
                    fDestFile.getCanonicalPath(), Project.MSG_INFO);

        // Delete the export file.
        String[] saFilesToBeDeleted;

        saFilesToBeDeleted = new String[] { sExportFile };

        deleteFiles("export", saFilesToBeDeleted);
        //Set the receiver back to empty
        srmSoap.setReceiver("");
        sthTask.addProcessItem();
    }

    /**
     * Returns the last modification timestamp from the vcmdata file.
     *
     * @param   flowKey  Flow key.
     *
     * @return  <code>true</code> if the modification times are different.
     */
    protected String getFlowFileLastModification(String flowKey)
    {
        File flowFile = new File(fDestDir, createFlowFileName(flowKey));

        if (!flowFile.exists())
        {
            // File does not exist.
            return null;
        }

        String fileLastModified = null;
        InputStream fileInput = null;
        ZipInputStream zipInput = null;
        XMLStreamReader xmlReader = null;

        try
        {
            String tmp = sStudioFlowRootDir;

            if (tmp.endsWith("/3.Business Process Models"))
            {
                tmp = tmp.substring(0, tmp.length() - 26);
            }

            String bpmFileName = flowKey.substring(tmp.length() + 1).replaceAll("[/\\\\]", "_");

            bpmFileName = "businessmodelrepository/businessmodels/" + bpmFileName;

            fileInput = new FileInputStream(flowFile);
            zipInput = new ZipInputStream(fileInput);

            ZipEntry bpmFileEntry = zipInput.getNextEntry();

            while (bpmFileEntry != null)
            {
                String entryName = bpmFileEntry.getName().replace('\\', '/');

                if (bpmFileName.equals(entryName))
                {
                    break;
                }

                bpmFileEntry = zipInput.getNextEntry();
            }

            if (bpmFileEntry == null)
            {
                cmtTask.log("[studio-bpms] Unable to find BPM file " + bpmFileEntry +
                            " from vcmdata file. Last modification check skipped.",
                            Project.MSG_VERBOSE);

                return null;
            }

            xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(zipInput);

            loop:
            while (true)
            {
                int event = xmlReader.next();

                switch (event)
                {
                    case XMLStreamReader.END_DOCUMENT:
                        break loop;

                    case XMLStreamReader.START_ELEMENT:
                        fileLastModified = xmlReader.getAttributeValue("", "lastModified");

                        break loop;
                }
            }

            if (fileLastModified == null)
            {
                cmtTask.log("[studio-bpms] Unable to find lastModified attribute from BPM file " +
                            bpmFileEntry + ". Last modification check skipped.",
                            Project.MSG_VERBOSE);

                return null;
            }
        }
        catch (Exception e)
        {
            // File could not be read for some reason.
            cmtTask.log("[studio-bpms] Unable to read vcmdata file " + flowFile +
                        ". Last modification check skipped.", Project.MSG_VERBOSE);

            return null;
        }
        finally
        {
            try
            {
                if (xmlReader != null)
                {
                    xmlReader.close();
                }
            }
            catch (XMLStreamException ignored)
            {
            }

            try
            {
                if (zipInput != null)
                {
                    zipInput.close();
                }
            }
            catch (IOException ignored)
            {
            }

            try
            {
                if (fileInput != null)
                {
                    fileInput.close();
                }
            }
            catch (IOException ignored)
            {
            }
        }

        return fileLastModified;
    }

    /**
     * Exports the Studio folders recursively based on the configured content filter.
     *
     * @param   sFolderName         The folder to be processed.
     * @param   sCurrentFilterPath  The current path in Studio filter format.
     * @param   rootFolder          Studio root folder.
     *
     * @throws  Exception  Thrown if the operation failed.
     */
    protected void recurseFolders(String sFolderName, String sCurrentFilterPath, String rootFolder)
                           throws Exception
    {
        // Create the GetCollection method message.
        Message mRequest = getMessageContext().createMessage(mMethodGetCollection);
        Message mResponse;

        cmtTask.log("[studio-bpms] Recursing folder " + sFolderName + " with filterpath " +
                    sCurrentFilterPath, Project.MSG_DEBUG);

        mRequest.setValue("GetCollection/folder", sFolderName);

        // Send the request.
        mResponse = srmSoap.makeSoapRequest(mRequest);

        // Handle each <tuple> element separately from the response.
        Iterator<?> iTupleIter = mResponse.selectAll(".//tuple");

        while (iTupleIter.hasNext())
        {
            Message mTuple = (Message) iTupleIter.next();
            String sIsFolder = mTuple.getValue("./@isFolder", "");
            String sKey = mTuple.getValue("./@key", "");
            String sObjectName;

            // Check if we have a valid key attribute
            if (sKey.equals(""))
            {
                continue;
            }

            int iPos;

            if ((iPos = sKey.lastIndexOf("/")) != -1)
            {
                sObjectName = sKey.substring(iPos + 1);
            }
            else
            {
                sObjectName = sKey;
            }

            String sRelativeKey = sCurrentFilterPath + "/" + sObjectName;

            // Check the tuple type.
            if (sIsFolder.equals("true"))
            {
                // Recurse into this folder.
                recurseFolders(sKey, sRelativeKey, rootFolder);
            }
            else
            {
                // Check if the filter matches
                String checkKey = (!rootFolder.isEmpty()) ? (rootFolder + "/" + sRelativeKey)
                                                          : sRelativeKey;

                checkKey = checkKey.replace("//", "/");

                if (
                    !sthTask.getCurrentContent().isPathAccepted(checkKey,
                                                                    StudioConstants.ECX_FLOW_FILTER_MATCH_PATTERN))
                {
                    cmtTask.log("[studio-bpms] " + sRelativeKey + " is not accepted.",
                                Project.MSG_DEBUG);

                    continue;
                }

                if (mVersionMap != null)
                {
                    // Check if the version is what we want.
                    String sVersion = sKey.replaceFirst(".*_([^\\.]+)\\.bpm$", "$1");

                    if ((sVersion.length() != sKey.length()) && !mVersionMap.containsKey(sVersion))
                    {
                        // No.
                        cmtTask.log("[studio-bpms] " + sRelativeKey +
                                    " is ignored because it is not the version we want.",
                                    Project.MSG_VERBOSE);

                        continue;
                    }
                }

                if (checkLastModified)
                {
                    // Check for the last modified time stamp.
                    String tupleLastModified = null;

                    try
                    {
                        tupleLastModified = mTuple.getValue("./@lastModified", false);
                    }
                    catch (Exception e)
                    {
                        // XML could not be read for some reason.
                        cmtTask.log("[studio-bpms] Unable to get lastModified attribute from the tuple. Last modification check skipped.",
                                    Project.MSG_VERBOSE);
                    }

                    if (tupleLastModified != null)
                    {
                        String fileLastModified = getFlowFileLastModification(sKey);

                        if ((fileLastModified != null) &&
                                fileLastModified.equals(tupleLastModified))
                        {
                            cmtTask.log("[studio-bpms] " + sRelativeKey +
                                        " is skipped because it has not changed according to last modified date.",
                                        Project.MSG_VERBOSE);

                            // Add this item as processes because otherwise we get an error that
                            // it wasn't found.
                            sthTask.addProcessItem();
                            continue;
                        }
                    }
                }

                // Export this flow.
                exportFlow(sKey);
            }
        }
    }

    /**
     * Returns the message context used by this object.
     *
     * @return  Message context.
     */
    private MessageContext getMessageContext()
    {
        return srmSoap.getCurrentNomCollector().getMessageContext();
    }
}
