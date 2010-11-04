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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.Project;

import com.cordys.coe.ant.studio.task.StudioTaskHandler;
import com.cordys.coe.ant.studio.util.StudioInitializer;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.xml.Message;
import com.cordys.coe.util.xml.MessageContext;
import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.cm.IContent;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.NomCollector;
import com.eibus.util.Base64;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Imports Studio vcmdata files from local file system to ECX.
 *
 * @author mpoyhone
 */
public class FlowImporter
{
    /**
     * The static document used in message templates.
     */
    protected static Document dRequestDoc = StudioInitializer.dRequestDoc;
    /**
     * The Studio Import method SOAP message template.
     */
    protected static Message mMethodImport;
    /**
     * The Studio UploadFile method SOAP message template.
     */
    protected static Message mMethodUploadFile;
    /**
     * The Studio DeletesFile method SOAP message template.
     */
    protected static Message mMethodDeleteFiles;
    /**
     * Message to get the BPM that was just inserted.
     */
    private static Message mGetObject;
    /**
     * Message to update the flow.
     */
    private static Message m_mUpdateObject;

    static
    {
        // Builds the needed SOAP messages on class load.
        buildMessages();
    }

    /**
     * The build framework task that calls this object.
     */
    protected ContentManagerTask cmtTask;
    /**
     * The temporary directory used when extracting the vcmdata file.
     * If this null, system default will be used (will always be on local file
     * system).
     */
    protected File fImportTempDirectory = null;
    /**
     * Source directory for studio files.
     */
    protected File fSrcDir;
    /**
     * Soap request manager.
     */
    protected ISoapRequestManager srmSoap;
    /**
     * Studio content hanler.
     */
    protected StudioTaskHandler sthTask;

/**
     * Creates a new FlowImporter object.
     *
     * @param fSrcDir Source directory for studio files.
     */
    public FlowImporter(File fSrcDir)
    {
        this.fSrcDir = fSrcDir;
    }

    /**
     * Starts the import operation.
     *
     * @param sthTask The Studio content handler.
     * @param cmtTask The build framework task that calls this object.
     * @param srmSoap Soap manager object.
     * @param sSoapUser Calling user DN
     * @param sOrganizationDN Destination organization DN.
     *
     * @throws StudioException Thrown if the operation failed.
     */
    public void execute(StudioTaskHandler sthTask, ContentManagerTask cmtTask,
                        ISoapRequestManager srmSoap, String sSoapUser,
                        String sOrganizationDN)
                 throws StudioException
    {
        this.cmtTask = cmtTask;
        this.sthTask = sthTask;
        this.srmSoap = srmSoap;

        try
        {
            srmSoap.addNomCollector(new NomCollector());

            if (StudioInitializer.initializeStudio(srmSoap, getMessageContext()))
            {
                cmtTask.log("[" + IContent.TYPE_STUDIO_BPMS +
                            "] Initialized Studio for this organization.",
                            Project.MSG_INFO);
            }

            if (StudioInitializer.insertDefaultUserPreferences(srmSoap,
                                                                   getMessageContext()))
            {
                cmtTask.log("[" + IContent.TYPE_STUDIO_BPMS +
                            "] Set default user preferences.", Project.MSG_INFO);
            }

            // Handle the file system folder contents recursively.
            recurseFolders(fSrcDir, "");
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to import studio flows.", e);
        }
        finally
        {
            srmSoap.removeNomCollector().deleteNodes();
        }
    }

    /**
     * Sets the temporary directory used when extracting the vcmdata
     * file.
     *
     * @param fDir The temporary directory used when extracting the vcmdata
     *        file.
     */
    public void setTempDir(File fDir)
    {
        fImportTempDirectory = fDir;
    }

    /**
     * Builds the needed SOAP messages when this class is loaded.
     *
     * @throws IllegalStateException Thrown if the message parsing failed.
     */
    protected static void buildMessages()
                                 throws IllegalStateException
    {
        String sXml = "";

        try
        {
            sXml = "<Import xmlns='http://schemas.cordys.com/1.0/cas-vcm' fileformat='Studio' uncompressPath=''>" +
                   "	<fileName fileType='1' overwriteModelsIfExists='true' overwriteVersionsIfExists='false' overwriteFileTypesIfExists='false' overwriteBMCIfExists='true'></fileName>" +
                   "</Import>";
            mMethodImport = new Message(dRequestDoc, sXml);
            mMethodImport.getSharedXmlTree().setReadOnly(true);

            sXml = "<UploadFile xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">\r\n" +
                   "  <fileName fileType='1'></fileName>\n" +
                   "  <fileContent></fileContent>\n" + "</UploadFile>";
            mMethodUploadFile = new Message(dRequestDoc, sXml);
            mMethodUploadFile.getSharedXmlTree().setReadOnly(true);

            sXml = "<DeleteFiles xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">\r\n" +
                   "</DeleteFiles>";
            mMethodDeleteFiles = new Message(dRequestDoc, sXml);
            mMethodDeleteFiles.getSharedXmlTree().setReadOnly(true);

            sXml = "    <GetObject xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">\r\n" +
                   "        <key version=\"organization\" vcmApplication=\"vcmRepository\" vcmVersion=\"\" suppressError=\"false\"></key>" +
                   "    </GetObject>";
            mGetObject = new Message(dRequestDoc, sXml);
            mGetObject.getSharedXmlTree().setReadOnly(true);

            sXml = "<UpdateObject xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">" +
                   "</UpdateObject>";
            m_mUpdateObject = new Message(dRequestDoc, sXml);
            m_mUpdateObject.getSharedXmlTree().setReadOnly(true);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to parse the SOAP message " +
                                            sXml + " : " + e);
        }
    }

    /**
     * Deletes the vcmdata file from server file system.
     *
     * @param sType Type type to be deleted ('import' or 'export').
     * @param saFileNames A table of file names
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected void deleteFiles(String sType, String[] saFileNames)
                        throws Exception
    {
        Message mRequest = getMessageContext()
                               .createMessage(mMethodDeleteFiles);
        Message mResponse;
        String sExpectedResponse = "";

        for (int i = 0; i < saFileNames.length; i++)
        {
            String sName = saFileNames[i];
            Message mFileElem = getMessageContext()
                                    .createMessage(dRequestDoc,
                                                   "<filename type='" + sType +
                                                   "'>" + sName +
                                                   "</filename>");

            mRequest.append(".", mFileElem);
            sExpectedResponse += "1";
        }

        mResponse = srmSoap.makeSoapRequest(mRequest);

        String sCount = mResponse.getValue("//deleted", true);

        if (!sCount.equals(sExpectedResponse))
        {
            throw new Exception("Unable to delete all export files from the server.");
        }
    }

    /**
     * Imports the vcmdata file to Studio.
     *
     * @param fFile The File object pointing to the vcmdata file.
     * @param sFileName The studio name of this file.
     * @param sCurrentFilterPath The relative path within studio.
     *
     * @throws StudioException Thrown if the operation failed.
     */
    protected void importFile(File fFile, String sFileName,
                              String sCurrentFilterPath)
                       throws Exception
    {
        String sUploadPath = null;
        String sHostName = cmtTask.getServerHostName();

        try
        {
            // Upload the file.
            sUploadPath = uploadFile(fFile);

            try
            {
                // Create the import SOAP message
                Message mImportRequest = getMessageContext()
                                             .createMessage(mMethodImport);

                mImportRequest.setValue("Import/@uncompressPath", sUploadPath);
                mImportRequest.setValue("Import/fileName", sFileName);
                mImportRequest.setValue("Import/fileName/@hostName", sHostName);

                srmSoap.makeSoapRequest(mImportRequest);
            }
            catch (Exception e)
            {
                throw new StudioException("Unable to send the import SOAP request.",
                                          e);
            }

            //Now the flow is imported we need to update the flow to change the organizational roles
            //to reflect the actual organization to which is deployed.
            String sRelativeKey = sFileName.replaceAll("vcmdata$", "bpm");
            String sRealStudioKey = StudioConstants.DEFAULT_FLOW_ROOT_FOLDER +
                                    sCurrentFilterPath + "/" + sRelativeKey;

            //We need the version as well:
            String sVersion = sFileName.substring(sFileName.lastIndexOf("_") +
                                                  1,
                                                  sFileName.length() -
                                                  ".vcmdata".length());

            Message mGetRequest = getMessageContext().createMessage(mGetObject);
            mGetRequest.setValue("//key/@vcmVersion", sVersion);
            mGetRequest.setValue("//key", sRealStudioKey);

            Message mGetResponse = srmSoap.makeSoapRequest(mGetRequest);

            int iTuple = Find.firstMatch(mGetResponse.getXmlNode(),
                                         "fChild<tuple>");

            if (iTuple == 0)
            {
                throw new StudioException("Error reading the just imported flow.");
            }

            //Now replace all roles.
            int[] aiRoles = Find.match(iTuple, "?<roles><role>");

            if (aiRoles.length > 0)
            {
                Pattern pRole = Pattern.compile("cn=organizational roles,o=[^,]+$");

                //Get the plain organization name from the total DN
                Pattern pOrgName = Pattern.compile("o=([^,]+),cn=cordys");
                String sOrganization = cmtTask.getOrganization();
                Matcher mMatcher = pOrgName.matcher(sOrganization);

                if (!mMatcher.find())
                {
                    throw new StudioException("Cannot find the organization name in the current dn: " +
                                              sOrganization);
                }
                sOrganization = mMatcher.group(1);
                log("Organization name: " + sOrganization, Project.MSG_DEBUG);

                for (int iCount = 0; iCount < aiRoles.length; iCount++)
                {
                    int iRole = aiRoles[iCount];

                    String sValue = Node.getDataWithDefault(iRole, "");

                    if (sValue.length() > 0)
                    {
                        mMatcher = pRole.matcher(sValue);

                        if (mMatcher.find())
                        {
                            log("Found an organizational role: " + sValue,
                                Project.MSG_DEBUG);

                            sValue = mMatcher.replaceAll("cn=organizational roles,o=" +
                                                         sOrganization);

                            log("New organizational role: " + sValue,
                                Project.MSG_DEBUG);

                            while (Node.getFirstChild(iRole) != 0)
                            {
                                Node.delete(Node.getFirstChild(iRole));
                            }

                            Node.getDocument(iRole).createText(sValue, iRole);
                        }
                    }
                }

                //Replacement is done. Now do the update
                Message mUpdateObject = getMessageContext()
                                            .createMessage(m_mUpdateObject);

                int iOld = Find.firstMatch(iTuple, "<tuple><old>");

                if (iOld == 0)
                {
                    throw new StudioException("Cannot find the old tag.");
                }
                Node.setName(iOld, "new");

                //Update the tuple tag so that it will update the BPM.
                Node.setAttribute(iTuple, "alterLastModified", "false");
                Node.setAttribute(iTuple, "ignoreReference", "false");
                Node.setAttribute(iTuple, "isFolder", "false");
                Node.setAttribute(iTuple, "templatepath", "");
                Node.setAttribute(iTuple, "version", "organization");
                Node.setAttribute(iTuple, "vcmVersion", sVersion);
                Node.setAttribute(iTuple, "vcmApplication", "vcmRepository");
                Node.setAttribute(iTuple, "unconditional", "false");

                mUpdateObject.append(".", new Message(iTuple));

                srmSoap.makeSoapRequest(mUpdateObject);
            }
        }
        finally
        {
            try
            {
                if (sUploadPath != null)
                {
                    deleteFiles("import", new String[] { sUploadPath });
                }
            }
            catch (Exception e)
            {
                // Apparently the method deletes the folder, but gives a wrong status back.
                //cmtTask.log("Unable to delete the temporary directory " + sUploadPath, Project.MSG_ERR);
            }
        }

        log("Imported flow " + fFile.getName(), Project.MSG_INFO);

        sthTask.addProcessItem();
    }

    /**
     * Imports the file system directory contents and sub-directory
     * contents to Studio
     *
     * @param fCurrentDir The current file system directory to be imported.
     * @param sCurrentFilterPath The current path in Studio filter format.
     *
     * @throws StudioException Thrown if the operation failed.
     */
    protected void recurseFolders(File fCurrentDir, String sCurrentFilterPath)
                           throws StudioException
    {
        // List all files and directories under this directory.
        File[] faFiles = fCurrentDir.listFiles();

        for (int iIndex = 0; iIndex < faFiles.length; iIndex++)
        {
            File fFile = faFiles[iIndex];
            String sFileKey = sCurrentFilterPath + "/" + fFile.getName();

            if (fFile.isDirectory())
            {
                // Recurse into the sub-directory.
                recurseFolders(fFile, sFileKey);
                continue;
            }

            // Check if the filter matches
            if (!sthTask.getCurrentContent()
                            .isPathAccepted(sFileKey,
                                                StudioConstants.FILESYSTEM_FLOW_FILTER_MATCH_PATTERN))
            {
                continue;
            }

            // Check that this is a valid vcmdata file.
            if (!fFile.getName().toLowerCase().endsWith(".vcmdata"))
            {
                continue;
            }

            try
            {
                importFile(fFile, fFile.getName(), sCurrentFilterPath);
            }
            catch (Exception e)
            {
                throw new StudioException(e);
            }
        }
    }

    /**
     * Uploads the vcmdata file to server file system.
     *
     * @param fFile File to be uploaded
     *
     * @return The file path on the server.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected String uploadFile(File fFile)
                         throws Exception
    {
        byte[] baFileContents;
        InputStream isInput = null;

        try
        {
            isInput = new FileInputStream(fFile);
            baFileContents = FileUtils.readStreamContents(isInput);
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to read vcmdata file " + fFile);
        }
        finally
        {
            FileUtils.closeStream(isInput);
        }

        String sEncodedContents;

        try
        {
            sEncodedContents = new String(Base64.encode(baFileContents));
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to encode file " + fFile);
        }

        Message mRequest = getMessageContext().createMessage(mMethodUploadFile);
        Message mResponse;

        mRequest.setValue("//fileName", fFile.getName());
        mRequest.setValue("//fileContent", sEncodedContents);

        mResponse = srmSoap.makeSoapRequest(mRequest);

        return mResponse.getValue("//uncompressPath", true);
    }

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private MessageContext getMessageContext()
    {
        return srmSoap.getCurrentNomCollector().getMessageContext();
    }

    /**
     * Log helper method.
     *
     * @param sMessage The message to log.
     * @param iLevel The level for the message.
     */
    private void log(String sMessage, int iLevel)
    {
        if (cmtTask != null)
        {
            cmtTask.log("[" + IContent.TYPE_STUDIO_BPMS + "] " + sMessage,
                        iLevel);
        }
    }
}
