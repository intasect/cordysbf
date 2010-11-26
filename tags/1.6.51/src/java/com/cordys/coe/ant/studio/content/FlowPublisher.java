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

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;
import com.cordys.coe.ant.studio.task.StudioTaskHandler;
import com.cordys.coe.ant.studio.util.StudioInitializer;
import com.cordys.coe.bf.utils.LDAPStringUtil;
import com.cordys.coe.util.xml.Message;
import com.cordys.coe.util.xml.MessageContext;

import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.cm.IContent;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.NomCollector;
import com.cordys.tools.ant.util.CordysVersion;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Date;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.Project;

/**
 * This class publishes the flows that are defined in the project to the
 * runtime environment.  First the current BPML is retrieved from Studio. Then
 * the SaveProcessModel is called to publish the flow.
 *
 * @author pgussow
 */
public class FlowPublisher
{
    /**
     * The static document used in message templates.
     */
    protected static Document dRequestDoc = StudioInitializer.dRequestDoc;
    /**
     * The Studio method to get the current flow from Studio.
     */
    protected static Message mGetObject;
    /**
     * The Studio method to publish the flow to runtime.
     */
    protected static Message mSaveProcessModel;
    /**
     * The Studio method to publish the flow to runtime for C2.
     */
    protected static Message mSaveProcessModelC2;
    /**
     * Holds the message for updating the design time version of the
     * flow.
     */
    private static Message m_mUpdateObject;

    static
    {
        // Builds the needed SOAP messages on class load.
        buildMessages();
    }

    /**
     * Holds the regex for the organization in the schema location.
     */
    private static Pattern s_pOrganization = Pattern.compile("organization=([^&])+");
    /**
     * Holds the regex for the servername.
     */
    private static Pattern s_pServer = Pattern.compile("^(http[s]*)://([^:/]+)");
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
     * Creates a new FlowPublisher object.
     *
     * @param fSrcDir Source directory for studio files.
     */
    public FlowPublisher(File fSrcDir)
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
            sXml = "    <GetObject xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">\r\n" +
                   "        <key version=\"organization\" vcmApplication=\"vcmRepository\" vcmVersion=\"\" suppressError=\"false\"></key>" +
                   "    </GetObject>";
            mGetObject = new Message(dRequestDoc, sXml);
            mGetObject.getSharedXmlTree().setReadOnly(true);

            sXml = "    <SaveProcessModel xmlns=\"http://schemas.cordys.com/1.0/coboc\">" +
                   "        <processname></processname>" +
                   "        <description></description>" +
                   "        <version></version>" + "        <processmodel/>" +
                   "    </SaveProcessModel>";
            mSaveProcessModel = new Message(dRequestDoc, sXml);
            mSaveProcessModel.getSharedXmlTree().setReadOnly(true);

            //SaveProcessModel for C2
            sXml = "<SaveProcessModel xmlns=\"http://schemas.cordys.com/1.0/coboc\">" +
                   "    <processname></processname>" +
                   "    <description></description>" +
                   "    <version></version>" + "    <processmodel/>" +
                   "    <bpmn><bpmtuplewsdl></bpmtuplewsdl></bpmn>" +
                   "</SaveProcessModel>";
            mSaveProcessModelC2 = new Message(dRequestDoc, sXml);
            mSaveProcessModelC2.getSharedXmlTree().setReadOnly(true);

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
     * This method actually publishes the flow. It first retrieves the
     * proper BPML from Studio and then publishes the flow.
     *
     * @param fFile The File object pointing to the vcmdata file.
     * @param sCurrentFilterPath The relative path of the flow.
     * @param sFileName The studio name of this file.
     *
     * @throws StudioException Thrown if the operation failed.
     */
    protected void publishFlowToRuntime(File fFile, String sCurrentFilterPath,
                                        String sFileName)
                                 throws Exception
    {
        //We first need the actual key of the flow in Studio. sFilename contains the vcmdata
        //filename. We'll replace vcmdata with bpm and add the sCurrentFilterpath to it. Then
        //we have the relative key. Adding the DEFAULT_FLOW_ROOT_FOLDER gives us the full path.
        String sRelativeKey = sFileName.replaceAll("vcmdata$", "bpm");
        String sRealStudioKey = StudioConstants.DEFAULT_FLOW_ROOT_FOLDER +
                                sCurrentFilterPath + "/" + sRelativeKey;

        //We need the version as well:
        String sVersion = sFileName.substring(sFileName.lastIndexOf("_") + 1,
                                              sFileName.length() -
                                              ".vcmdata".length());

        log("sCurrentFilterPath: " + sCurrentFilterPath, Project.MSG_DEBUG);
        log("sFileName: " + sFileName, Project.MSG_DEBUG);
        log("sRealStudioKey: " + sRealStudioKey, Project.MSG_DEBUG);
        log("sVersion: " + sVersion, Project.MSG_DEBUG);

        //Now we have the full key, so we're going to retrieve the bpm from Studio.
        Message mGetRequest = getMessageContext().createMessage(mGetObject);
        mGetRequest.setValue("//key/@vcmVersion", sVersion);
        mGetRequest.setValue("//key", sRealStudioKey);

        Message mGetResponse = srmSoap.makeSoapRequest(mGetRequest);

        if (CordysVersion.getCordysVersion() == CordysVersion.BCP_4_2_C1)
        {
            //Get the bpml from the response.
            Message mBPML = mGetResponse.select("//tuple/old/bpm/content/bpml/package");

            if (mBPML == null)
            {
                throw new StudioException("BPML not found for " + sFileName);
            }

            //Now we have the BPML, so lets build the request for saving it/
            int iBPMLNode = mBPML.getXmlNode();

            Message mSaveModelRequest = getMessageContext()
                                            .createMessage(mSaveProcessModel);
            int iReqNode = mSaveModelRequest.getXmlNode();
            log("Root: " + Node.getName(iReqNode), Project.MSG_DEBUG);
            mSaveModelRequest.setValue("./processname",
                                       Node.getAttribute(iBPMLNode, "name") +
                                       "_" + sVersion + ".bpm");
            mSaveModelRequest.setValue("./description",
                                       Node.getAttribute(iBPMLNode, "name"));
            mSaveModelRequest.setValue("./version", sVersion);
            mSaveModelRequest.append("./processmodel", new Message(iBPMLNode));

            srmSoap.makeSoapRequest(mSaveModelRequest);
        }
        else if (CordysVersion.getCordysVersion() == CordysVersion.BCP_4_2_C2)
        {
            log("In Cordys C2 the build framework can't publish a flow. It will however fix the " +
                "links if you have imported the runtime flows via the CoBOC handler.",
                Project.MSG_VERBOSE);

            //We will first check if there is a runtime version of this flow. If this is the case we will 
            //make sure that CoBOC can link the runtime and designtime versions.
            //There are also a couple of things we need to fix in the bpmn (desgintime):
            //1. The linked organizational roles. The name of the organization should match the current org.
            //2. The hard server reference in the BPMN for XSDs in the schema location.
            doPublishC2(sRelativeKey, sCurrentFilterPath + "/" + sRelativeKey,
                        mGetResponse, sVersion);
        }

        log("Published the flow " + fFile.getName(), Project.MSG_INFO);

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
                                                StudioConstants.ECX_FLOW_FILTER_MATCH_PATTERN))
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
                publishFlowToRuntime(fFile, sCurrentFilterPath, fFile.getName());
            }
            catch (Exception e)
            {
                throw new StudioException(e);
            }
        }
    }

    /**
     * This method does the publish to runtime of a BPM.It will first
     * check if there is a runtime version of this flow. If this is the case
     * we will  make sure that CoBOC can link the runtime and designtime
     * versions. There are also a couple of things we need to fix in the bpmn (desgintime):<br>
     * 1. The linked organizational roles. The name of the organization should
     * match the current org.<br>
     * 2. The hard server reference in the BPMN for XSDs in the schema
     * location.
     *
     * @param sRelativeKey The relative key of the BPM within studio.
     * @param sRelativeKey The relative key of the BPM within studio.
     * @param mDesignTime Holds the designtime object of the studio flow.
     * @param sVersion The version of the BPM.
     */
    private void doPublishC2(String sBPMName, String sRelativeKey,
                             Message mDesignTime, String sVersion)
                      throws StudioException
    {
        if ((sBPMName == null) || (sBPMName.length() == 0))
        {
            throw new StudioException("Could not get the actual BPM name from the filename.");
        }

        //Read the runtime version of the flow from the CoBOC folders.
        int iBPML = readRuntimeVersion(sBPMName);

        int iWSDL = readRuntimeWSDL(sBPMName);

        int iBPMN = mDesignTime.getXmlNode();

        int iBPMNTuple = Find.firstMatch(iBPMN, "fChild<tuple>");

        if (iBPMNTuple == 0)
        {
            throw new StudioException("Cannot find the desgintime version of the flow. First run a 'tocordys flows' before trying to publish them to runtime.");
        }
        Node.unlink(iBPMNTuple);
        Node.delete(iBPMN);
        iBPMN = iBPMNTuple;

        int iDuplicateBPMN = Node.duplicate(iBPMN);

        try
        {
            fixRuntimeBPML(iBPML);

            doSaveProcessModelRequest(iBPML, iBPMN, iWSDL);
        }
        catch (Exception e)
        {
            Node.delete(iDuplicateBPMN);

            if (e instanceof StudioException)
            {
                throw (StudioException) e;
            }
            else
            {
                throw new StudioException("Error saving the process model.", e);
            }
        }

        //Now update the BPM model itself and tell studio that it has been published.
        updateDesignTime(iBPMN, sVersion);
    }

    /**
     * This method does the actual saving of the runtime version.
     *
     * @param iBPML The correct BPML.
     * @param iBPMN The correct BPMN.
     * @param iWSDL The correct runtime WSDL.
     */
    private void doSaveProcessModelRequest(int iBPML, int iBPMN, int iWSDL)
                                    throws StudioException
    {
        //First get the proper data from the source XMLs.
        String sBPMName = Node.getDataWithDefault(Find.firstMatch(iBPMN,
                                                                  "?<documentProperties><name>"),
                                                  "");
        String sVersion = Node.getDataWithDefault(Find.firstMatch(iBPMN,
                                                                  "?<documentProperties><version>"),
                                                  "");
        String sDescription = Node.getDataWithDefault(Find.firstMatch(iBPMN,
                                                                      "?<documentProperties><description>"),
                                                      "");

        String sFinalName = sBPMName + "_" + sVersion + ".bpm";

        //Now build up the request.
        Message mSaveModelRequest = getMessageContext()
                                        .createMessage(mSaveProcessModelC2);

        try
        {
            mSaveModelRequest.setValue("./processname", sFinalName);
            mSaveModelRequest.setValue("./description", sDescription);
            mSaveModelRequest.setValue("./version", sVersion);
            mSaveModelRequest.append("./processmodel", new Message(iBPML));
            mSaveModelRequest.append("./bpmn/bpmtuplewsdl", new Message(iBPMN));
            mSaveModelRequest.append("./bpmn/bpmtuplewsdl", new Message(iWSDL));

            srmSoap.makeSoapRequest(mSaveModelRequest);
        }
        catch (Exception e)
        {
            throw new StudioException("Error saving the process model.", e);
        }
    }

    /**
     * This method fixes the runtime BPML for the current organization.
     * What it does is make sure that the LDAP DNs used are translated to
     * proper DNs based on the LDAp root. As an additional step also the DN of
     * organizational users is changed to the proper organization.
     *
     * @param iBPML The BPML that needs fixing.
     */
    private void fixRuntimeBPML(int iBPML)
                         throws StudioException
    {
        //First do the Role DN's
        int[] aiRoles = Find.match(iBPML, "?<user>");

        for (int iCount = 0; iCount < aiRoles.length; iCount++)
        {
            int iRole = aiRoles[iCount];

            String sValue = Node.getDataWithDefault(iRole, "");

            if (sValue.length() > 0)
            {
                if (sValue.indexOf(",cn=organizational roles,") > -1)
                {
                    //Organizational role
                    sValue = LDAPStringUtil.replaceRoleDn(sValue,
                                                          cmtTask.getOrganization());
                }
                else if (sValue.indexOf(",cn=organizational users,") > -1)
                {
                    //Organizational user
                    sValue = LDAPStringUtil.replaceOrganization(sValue,
                                                                cmtTask.getOrganization());
                }
                else if (sValue.indexOf(",cn=cordys,o=") > -1)
                {
                    //An ISV role.
                    sValue = LDAPStringUtil.replaceRoleDn(sValue,
                                                          cmtTask.getOrganization());
                }
            }

            while (Node.getFirstChild(iRole) != 0)
            {
                Node.delete(Node.getFirstChild(iRole));
            }
            Node.getDocument(iRole).createText(sValue, iRole);
        }

        //Now do the schema locations.
        int[] aiSchemaLocations = Find.match(iBPML, "?<xsd:include>");

        for (int iCount = 0; iCount < aiSchemaLocations.length; iCount++)
        {
            int iSchemaLocation = aiSchemaLocations[iCount];
            String sLocation = Node.getAttribute(iSchemaLocation,
                                                 "schemaLocation", "");

            if (sLocation.length() > 0)
            {
                //              Now we need to replace the organization. We'll need a regex to find the organization
                //But we need to not only replace the organization name, but also the servername if specified.
                Matcher mMatcher = s_pOrganization.matcher(sLocation);
                sLocation = mMatcher.replaceAll("organization=" +
                                                cmtTask.getOrganization());

                mMatcher = s_pServer.matcher(sLocation);

                //Now get the servername of where we're deploying to.
                String sURL = cmtTask.getProject()
                                     .getProperty(BuildFrameworkProperty.WEBGATEWAY_URL.getName());

                if ((sURL == null) || (sURL.length() == 0))
                {
                    throw new StudioException("The webgateway URL must be filled.");
                }

                URL uTemp;

                try
                {
                    uTemp = new URL(sURL);
                }
                catch (MalformedURLException e)
                {
                    throw new StudioException("Malformed web gateway URL: " +
                                              sURL, e);
                }

                String sReplacement = uTemp.getProtocol() + "://" +
                                      uTemp.getHost();

                if (uTemp.getPort() > -1)
                {
                    sReplacement += (":" + uTemp.getPort());
                }

                sLocation = mMatcher.replaceAll(sReplacement);
            }
        }
    }

    /**
     * This method returns the current message context for this
     * publisher.
     *
     * @return The message context to user.
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

    /**
     * This method reads the runtime version from the local CoBOC
     * folders. If there is no runtime version available it will throw an
     * exception.
     *
     * @param sBPMName The name of the BPM to read.
     *
     * @return The runtime version of the BPM
     */
    private int readRuntimeVersion(String sBPMName)
                            throws StudioException
    {
        int iReturn = 0;

        try
        {
            String sCoBOCBase = cmtTask.getProject()
                                       .getProperty(BuildFrameworkProperty.SRC_CONTENT_COBOC_FOLDERS.getName());
            log("CoBOC folder: " + sCoBOCBase, Project.MSG_DEBUG);

            File fTemp = new File(sCoBOCBase);

            if (!fTemp.exists())
            {
                throw new StudioException("The folder " + sCoBOCBase +
                                          " does not exist.");
            }

            File fBPMLFile = new File(fTemp,
                                      "Business Processes/" + sBPMName +
                                      ".xml");

            if (!fBPMLFile.exists())
            {
                throw new StudioException("There is no runtime version for flow " +
                                          sBPMName + " (" +
                                          fBPMLFile.getAbsolutePath() + ")");
            }

            //Now we can read the file.
            iReturn = dRequestDoc.load(fBPMLFile.getAbsolutePath());

            //Now remove all the unneeded tags around it.
            int iRealReturn = Find.firstMatch(iReturn,
                                              "?<ENTITY><OBJECT><processinstance><processmodel><package>");

            if (iRealReturn == 0)
            {
                throw new StudioException("Could not find the BPML in the runtime version.");
            }

            //Remove unneeded stuff.
            Node.unlink(iRealReturn);
            Node.delete(iReturn);
            iReturn = iRealReturn;
        }
        catch (Exception e)
        {
            if (iReturn != 0)
            {
                Node.delete(iReturn);
            }

            if (e instanceof StudioException)
            {
                throw (StudioException) e;
            }
            else
            {
                throw new StudioException("Error checking if there is a runtime version available.",
                                          e);
            }
        }
        return iReturn;
    }

    /**
     * This method reads the runtime WSDL tag for publishing the flow.
     *
     * @param sBPMName The name of the BPM.
     *
     * @return The int holding the WSDL that should be used.
     */
    private int readRuntimeWSDL(String sBPMName)
                         throws StudioException
    {
        int iReturn = 0;

        try
        {
            String sCoBOCBase = cmtTask.getProject()
                                       .getProperty(BuildFrameworkProperty.SRC_CONTENT_COBOC_FOLDERS.getName());
            log("CoBOC folder: " + sCoBOCBase, Project.MSG_DEBUG);

            File fTemp = new File(sCoBOCBase);

            if (!fTemp.exists())
            {
                throw new StudioException("The folder " + sCoBOCBase +
                                          " does not exist.");
            }

            File fBPMLFile = new File(fTemp,
                                      "Business Processes/BPMN/Business Processes/" +
                                      sBPMName);

            if (!fBPMLFile.exists())
            {
                throw new StudioException("There is no runtime BPMN version for flow " +
                                          sBPMName + " (" +
                                          fBPMLFile.getAbsolutePath() + ")");
            }

            //Now we have the folder that contains the runtime BPMNs. We will pick the first file there.
            //It's the responsibility of the developer to make sure that the latest version is there
            //and that there is only a single file.
            String[] asFiles = fBPMLFile.list();

            if (asFiles.length == 0)
            {
                throw new StudioException("There is no runtime BPMN version found in folder " +
                                          fBPMLFile.getAbsolutePath());
            }

            String sFilename = "";

            for (int iCount = 0; iCount < asFiles.length; iCount++)
            {
                if (asFiles[iCount].endsWith(".xml"))
                {
                    sFilename = asFiles[iCount];
                    break;
                }
            }

            if (sFilename.length() == 0)
            {
                throw new StudioException("There are no XML files in folder " +
                                          fBPMLFile.getAbsolutePath());
            }

            //Now we can read the file.
            iReturn = dRequestDoc.load(new File(fBPMLFile, sFilename).getAbsolutePath());

            //Now remove all the unneeded tags around it.
            int iRealReturn = Find.firstMatch(iReturn,
                                              "?<ENTITY><OBJECT><bpmtuplewsdl><wsdl>");

            if (iRealReturn == 0)
            {
                throw new StudioException("Could not find the runtime WSDL in the runtime BPMN version.");
            }

            //Remove unneeded stuff.
            Node.unlink(iRealReturn);
            Node.delete(iReturn);
            iReturn = iRealReturn;
        }
        catch (Exception e)
        {
            if (iReturn != 0)
            {
                Node.delete(iReturn);
            }

            if (e instanceof StudioException)
            {
                throw (StudioException) e;
            }
            else
            {
                throw new StudioException("Error checking if there is a runtime version available.",
                                          e);
            }
        }
        return iReturn;
    }

    /**
     * This method updates the BPMN to indicate that the process has
     * been published.
     *
     * @param iBPMN The tuple containing the old designtime version.
     * @param sVersion DOCUMENTME
     */
    private void updateDesignTime(int iBPMN, String sVersion)
                           throws StudioException
    {
        Message mUpdateObject = getMessageContext()
                                    .createMessage(m_mUpdateObject);

        try
        {
            int iOld = Find.firstMatch(iBPMN, "<tuple><old>");

            if (iOld == 0)
            {
                throw new StudioException("Cannot find the old tag.");
            }
            Node.setName(iOld, "new");

            int iPublished = Find.firstMatch(iBPMN,
                                             "?<modelProperties><dataObject>");

            if (iPublished == 0)
            {
                throw new StudioException("Cannot find the model properties.");
            }

            Node.setDataElement(iPublished, "published", "Yes");
            Node.setDataElement(iPublished, "publishDate",
                                "" + new Date().getTime());

            //TODO: Put the current user there.
            Node.setDataElement(iPublished, "publishUser", "SYSTEM");

            //Update the tuple tag so that it will update the BPM.
            Node.setAttribute(iBPMN, "alterLastModified", "false");
            Node.setAttribute(iBPMN, "ignoreReference", "false");
            Node.setAttribute(iBPMN, "isFolder", "false");
            Node.setAttribute(iBPMN, "templatepath", "");
            Node.setAttribute(iBPMN, "version", "organization");
            Node.setAttribute(iBPMN, "vcmVersion", sVersion);
            Node.setAttribute(iBPMN, "vcmApplication", "vcmRepository");
            Node.setAttribute(iBPMN, "unconditional", "false");

            mUpdateObject.append(".", new Message(iBPMN));

            srmSoap.makeSoapRequest(mUpdateObject);
        }
        catch (Exception e)
        {
            if (e instanceof StudioException)
            {
                throw (StudioException) e;
            }
            else
            {
                throw new StudioException("Error saving the desgin time model.",
                                          e);
            }
        }
    }
}
