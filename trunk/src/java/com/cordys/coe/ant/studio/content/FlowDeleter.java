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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.tools.ant.Project;

import com.cordys.coe.ant.studio.task.StudioTaskHandler;
import com.cordys.coe.ant.studio.util.StudioInitializer;
import com.cordys.coe.util.xml.Message;
import com.cordys.coe.util.xml.MessageContext;
import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.NomCollector;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.eibus.xml.nom.Document;

/**
 * Deletes Studio flows from ECX.
 *
 * @author mpoyhone
 */
public class FlowDeleter
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
     * The Studio DeleteProcessModel method SOAP message template.
     */
    protected static Message mMethodDeleteProcessModel;
    /**
     * The Studio UpdateObject method SOAP message template for deleting a
     * flow.
     */
    protected static Message mMethodUpdateObject;

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
     * A map containing requested versions. If this is null, all versions are
     * deleted.
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
     * Studio content hanler.
     */
    protected StudioTaskHandler sthTask;

    /**
     * Creates a new FlowDeleter object.
     *
     * @param sStudioFlowRootDir The SOAP connection used to call ECX methods.
     */
    public FlowDeleter(String sStudioFlowRootDir)
    {
        this.sStudioFlowRootDir = sStudioFlowRootDir;
    }

    /**
     * Sets all versions that will be deleted. Multiple versions are separated
     * with a semi-colon ( ; ).
     *
     * @param sVersions Version string.
     */
    public void setDeleteVersions(String sVersions)
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
     * Starts the delete operation.
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
            // Handle the Studio folder contents recursively.
            recurseFolders(sStudioFlowRootDir, "");
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to delete studio flows.", e);
        }
        finally {
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
            sXml = "<GetCollection xmlns='http://schemas.cordys.com/1.0/cas-vcm'>" +
                   "      <folder detail='false' recursive='false' version='organization' vcmApplication='vcmRepository' vcmVersion='vcmDefault'></folder>" +
                   "</GetCollection>";
            mMethodGetCollection = new Message(dRequestDoc, sXml);
            mMethodGetCollection.getSharedXmlTree().setReadOnly(true);

            sXml = "<GetObject xmlns='http://schemas.cordys.com/1.0/cas-vcm'>\r\n" +
                   "  <key version='organization' vcmApplication='vcmDefault' vcmVersion='' suppressError='false'></key>\r\n" +
                   "</GetObject>";
            mMethodGetObject = new Message(dRequestDoc, sXml);
            mMethodGetObject.getSharedXmlTree().setReadOnly(true);

            sXml = "<UpdateObject xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">\r\n" +
                   "   <tuple key='' name='' objectID='' lastModified='' recursive='false' isFolder='false' unconditional='false' version='organization' ignoreReference='true' vcmApplication='vcmRepository' vcmVersion=''>\r\n" +
                   "      <old/>\r\n" + "   </tuple>\r\n" + "</UpdateObject>";
            mMethodUpdateObject = new Message(dRequestDoc, sXml);
            mMethodUpdateObject.getSharedXmlTree().setReadOnly(true);

            sXml = "<DeleteProcessModel xmlns='http://schemas.cordys.com/1.0/coboc'>\r\n" +
                   "   <processname></processname>\r\n" +
                   "</DeleteProcessModel>";
            mMethodDeleteProcessModel = new Message(dRequestDoc, sXml);
            mMethodDeleteProcessModel.getSharedXmlTree().setReadOnly(true);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to parse the SOAP message " +
                                            sXml + " : " + e);
        }
    }

    /**
     * Exports the given flow to the file system
     *
     * @param mFlowTuple The flow key to be passed to the Export method.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected void deleteFlow(Message mFlowTuple)
                       throws Exception
    {
        // Get the flow properties needed for deletion
        String sKey = mFlowTuple.getValue("./@key", true);
        String sName = mFlowTuple.getValue("./@name", true);
        String sObjectId = mFlowTuple.getValue("./@objectID", true);
        String sLastModified = mFlowTuple.getValue("./@lastModified", true);
        String sVcmVersion = sKey.replaceFirst(".*_([^.]+)\\.bpm$", "$1");

        cmtTask.log("[studio-bpms] Deleting flow " + sName, Project.MSG_INFO);

        if (sVcmVersion.equals(sKey))
        {
            throw new StudioException("Flow version could not be found from the tuple.");
        }

        // Create flow deletion request
        Message mDeleteRequest = getMessageContext().createMessage(mMethodUpdateObject);

        mDeleteRequest.setValue(".//tuple/@key", sKey);
        mDeleteRequest.setValue(".//tuple/@name", sName);
        mDeleteRequest.setValue(".//tuple/@objectID", sObjectId);
        mDeleteRequest.setValue(".//tuple/@lastModified", sLastModified);
        mDeleteRequest.setValue(".//tuple/@vcmVersion", sVcmVersion);

        srmSoap.makeSoapRequest(mDeleteRequest);

        // Create published flow deletion request.
        mDeleteRequest = getMessageContext().createMessage(mMethodDeleteProcessModel);
        mDeleteRequest.setValue(".//processname", sName);

        try
        {
            srmSoap.makeSoapRequest(mDeleteRequest);
        }
        catch (SoapRequestException e)
        {
            cmtTask.log("[studio-bpms]     Process model deletion failed (probably was not published).",
                        Project.MSG_INFO);
        }

        // Delete BPML as well, if present.
        Message mGetBpmlRequest = getMessageContext().createMessage(mMethodGetObject);
        Message mGetBpmlResponse = null;

        mGetBpmlRequest.setValue(".//key",
                                 StudioConstants.DEFAULT_FLOW_BPML_ROOT_FOLDER +
                                 "/" + sName + "l");
        mGetBpmlRequest.setValue(".//key/@vcmVersion", sVcmVersion);

        try
        {
            mGetBpmlResponse = srmSoap.makeSoapRequest(mGetBpmlRequest);
        }
        catch (SoapRequestException e)
        {
            cmtTask.log("[studio-bpms]     BPML deletion failed (probably was not generated).",
                        Project.MSG_INFO);
        }

        if (mGetBpmlResponse != null)
        {
            mDeleteRequest = getMessageContext().createMessage(mMethodUpdateObject);

            mDeleteRequest.setValue(".//tuple/@key",
                                    mGetBpmlResponse.getValue("//tuple/@key",
                                                              true));
            mDeleteRequest.setValue(".//tuple/@name",
                                    mGetBpmlResponse.getValue("//tuple/@name",
                                                              true));
            mDeleteRequest.setValue(".//tuple/@objectID",
                                    mGetBpmlResponse.getValue("//tuple/@objectID",
                                                              true));
            mDeleteRequest.setValue(".//tuple/@lastModified",
                                    mGetBpmlResponse.getValue("//tuple/@lastModified",
                                                              true));
            mDeleteRequest.setValue(".//tuple/@vcmVersion", sVcmVersion);

            srmSoap.makeSoapRequest(mDeleteRequest);
        }
        
        sthTask.addProcessItem();
    }

    /**
     * Deletes the given flow folder from ECX
     *
     * @param mFolderTuple The XForm folder tuple.
     * @param sFolderPath Folder's relative path.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected void deleteFolder(Message mFolderTuple, String sFolderPath)
                         throws Exception
    {
        // Get the XForm properties needed for deletion
        String sKey = mFolderTuple.getValue("./@key", true);
        String sName = mFolderTuple.getValue("./@name", true);
        String sObjectId = mFolderTuple.getValue("./@objectID", true);
        String sLastModified = mFolderTuple.getValue("./@lastModified", true);

        // Check if the folder is empty.
        Message mGetCollectionRequest = getMessageContext().createMessage(mMethodGetCollection);
        Message mGetCollectionResponse;

        mGetCollectionRequest.setValue("GetCollection/folder", sKey);

        // Send the request.
        mGetCollectionResponse = srmSoap.makeSoapRequest(mGetCollectionRequest);

        if (mGetCollectionResponse.select(".//tuple") != null)
        {
            // Folder was not empty.
            return;
        }

        cmtTask.log("[studio-bpms] Deleting folder " + sFolderPath,
                    Project.MSG_INFO);

        // Create XForm deletion request
        Message mDeleteRequest = getMessageContext().createMessage(mMethodUpdateObject);

        mDeleteRequest.setValue(".//tuple/@key", sKey);
        mDeleteRequest.setValue(".//tuple/@name", sName);
        mDeleteRequest.setValue(".//tuple/@objectID", sObjectId);
        mDeleteRequest.setValue(".//tuple/@isFolder", "true");
        mDeleteRequest.setValue(".//tuple/@ignoreReference", "false");
        mDeleteRequest.setValue(".//tuple/@lastModified", sLastModified);
        mDeleteRequest.setValue(".//tuple/@vcmVersion", "vcmDefault");

        srmSoap.makeSoapRequest(mDeleteRequest);
        
        sthTask.addProcessItem();
    }

    /**
     * Deletes the Studio flows recursively based on the configured content
     * filter.
     *
     * @param sFolderName The folder to be processed.
     * @param sCurrentFilterPath The current path in Studio filter format.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected void recurseFolders(String sFolderName, String sCurrentFilterPath)
                           throws Exception
    {
        // Create the GetCollection method message.
        Message mRequest = getMessageContext().createMessage(mMethodGetCollection);
        Message mResponse;

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
                recurseFolders(sKey, sRelativeKey);

                // Check if the filter matches so we can delete this foler.
                if (sthTask.getCurrentContent().isPathAccepted(sRelativeKey, StudioConstants.ECX_FLOW_FILTER_MATCH_PATTERN))
                {
                    deleteFolder(mTuple, sRelativeKey);
                }
            }
            else
            {
                // Check if the filter matches
                if (!sthTask.getCurrentContent().isPathAccepted(sRelativeKey))
                {
                    continue;
                }

                if (mVersionMap != null)
                {
                    // Check if the version is what we want.
                    String sVersion = sKey.replaceFirst(".*_([^\\.]+)\\.bpm$",
                                                        "$1");

                    if ((sVersion.length() != sKey.length()) &&
                            !mVersionMap.containsKey(sVersion))
                    {
                        // No.
                        continue;
                    }
                }

                // Export this flow.
                deleteFlow(mTuple);
            }
        }
    }
    
    private MessageContext getMessageContext() {
    	return srmSoap.getCurrentNomCollector().getMessageContext();
    }
}
