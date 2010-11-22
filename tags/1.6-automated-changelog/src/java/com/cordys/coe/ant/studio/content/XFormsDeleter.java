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
 * Deletes Studio XForms from ECX.
 *
 * @author mpoyhone
 */
public class XFormsDeleter
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
     * The root Studio path in that is passed to the first GetCollection
     * method.
     */
    protected String sStudioXFormRootDir;
    /**
     * Studio content hanler.
     */
    protected StudioTaskHandler sthTask;
    /**
     * Soap request manager.
     */
    protected ISoapRequestManager srmSoap;

    /**
     * Creates a new XFormsDeleter object.
     *
     * @param sStudioXFormRootDir The SOAP connection used to call ECX methods.
     */
    public XFormsDeleter(String sStudioXFormRootDir)
    {
        this.sStudioXFormRootDir = sStudioXFormRootDir;
    }

    /**
     * Sets all versions that will be delete. Multiple versions are separated
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
     * Starts the import operation.
     *
     * @param sthTask The Studio content handler.
     * @param cmtTask The build framework task that calls this object.
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
            recurseFolders(sStudioXFormRootDir, "");
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to delete studio XForms.", e);
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

            sXml = "<GetObject xmlns='http://schemas.cordys.com/1.0/cas-vcm'>" +
                   "       <key version='organization' vcmApplication='vcmRepository' vcmVersion='vcmDefault'></key>" +
                   "</GetObject>";
            mMethodGetObject = new Message(dRequestDoc, sXml);
            mMethodGetObject.getSharedXmlTree().setReadOnly(true);

            sXml = "<UpdateObject xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">\r\n" +
                   "   <tuple key='' name='' objectID='' lastModified='' recursive='false' isFolder='false' unconditional='false' version='organization' ignoreReference='true' vcmApplication='vcmRepository' vcmVersion=''>\r\n" +
                   "      <old/>\r\n" + "   </tuple>\r\n" + "</UpdateObject>";
            mMethodUpdateObject = new Message(dRequestDoc, sXml);
            mMethodUpdateObject.getSharedXmlTree().setReadOnly(true);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to parse the SOAP message " +
                                            sXml + " : " + e);
        }
    }

    /**
     * Deletes the given XForm folder from ECX
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
        /*       String sVcmVersion = sKey.replaceFirst(".*_([^.]+)\\.caf$", "$1");
        
                   if (sVcmVersion.equals(sKey))
                   {
                       throw new StudioException("Folder version could not be found from the tuple.");
                   }*/

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

        cmtTask.log("[studio-xforms] Deleting folder " + sFolderPath,
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
    }

    /**
     * Deletes the given XForm from ECX
     *
     * @param mXFormTuple The XForm tuple.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected void deleteXForm(Message mXFormTuple)
                        throws Exception
    {
        // Get the XForm properties needed for deletion
        String sKey = mXFormTuple.getValue("./@key", true);
        String sName = mXFormTuple.getValue("./@name", true);
        String sObjectId = mXFormTuple.getValue("./@objectID", true);
        String sLastModified = mXFormTuple.getValue("./@lastModified", true);
        String sVcmVersion = sKey.replaceFirst(".*_([^.]+)\\.caf$", "$1");

        cmtTask.log("[studio-xforms] Deleting XForm " + sName, Project.MSG_INFO);

        if (sVcmVersion.equals(sKey))
        {
            throw new StudioException("XForm version could not be found from the tuple.");
        }

        // Create XForm deletion request
        Message mDeleteRequest = getMessageContext().createMessage(mMethodUpdateObject);

        mDeleteRequest.setValue(".//tuple/@key", sKey);
        mDeleteRequest.setValue(".//tuple/@name", sName);
        mDeleteRequest.setValue(".//tuple/@objectID", sObjectId);
        mDeleteRequest.setValue(".//tuple/@lastModified", sLastModified);
        mDeleteRequest.setValue(".//tuple/@vcmVersion", sVcmVersion);

        srmSoap.makeSoapRequest(mDeleteRequest);
        sthTask.addProcessItem();

        // Delete the XForm translation file as well, if present.
        Message mGetTranslationRequest = getMessageContext().createMessage(mMethodGetObject);
        Message mGetTranslationResponse = null;
        String sTranslationName = sName.replaceFirst("(.*).caf$", "$1.mlm");

        mGetTranslationRequest.setValue(".//key",
                                        StudioConstants.DEFAULT_XFORMS_TRANSLATION_ROOT_FOLDER +
                                        "/" + sTranslationName);
        mGetTranslationRequest.setValue(".//key/@vcmVersion", sVcmVersion);

        try
        {
            mGetTranslationResponse = srmSoap.makeSoapRequest(mGetTranslationRequest);
        }
        catch (SoapRequestException e)
        {
            // No translation was found.
        }

        if (mGetTranslationResponse != null)
        {
            mDeleteRequest = getMessageContext().createMessage(mMethodUpdateObject);

            mDeleteRequest.setValue(".//tuple/@key",
                                    mGetTranslationResponse.getValue("//tuple/@key",
                                                                     true));
            mDeleteRequest.setValue(".//tuple/@name",
                                    mGetTranslationResponse.getValue("//tuple/@name",
                                                                     true));
            mDeleteRequest.setValue(".//tuple/@objectID",
                                    mGetTranslationResponse.getValue("//tuple/@objectID",
                                                                     true));
            mDeleteRequest.setValue(".//tuple/@lastModified",
                                    mGetTranslationResponse.getValue("//tuple/@lastModified",
                                                                     true));
            mDeleteRequest.setValue(".//tuple/@vcmVersion", sVcmVersion);

            cmtTask.log("[studio-xforms] Deleting XForm translation " +
                        mDeleteRequest.getValue(".//tuple/@name", true),
                        Project.MSG_INFO);

            srmSoap.makeSoapRequest(mDeleteRequest);
            
            sthTask.addProcessItem();
        }
    }

    /**
     * Deletes the Studio folders recursively based on the configured content
     * folder.
     *
     * @param sFolderName The folder to be processed.
     * @param sCurrentFilterPath The current path in Studio filter format.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected void recurseFolders(String sFolderName, String sCurrentFilterPath)
                           throws Exception
    {
        if (sFolderName.endsWith("/")) {
            sFolderName = sFolderName.substring(0, sFolderName.length() - 1);
        }
        
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
                if (sthTask.getCurrentContent().isPathAccepted(sRelativeKey, StudioConstants.ECX_XFORMS_FILTER_MATCH_PATTERN))
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
                    String sVersion = sKey.replaceFirst(".*_([^\\.]+)\\.caf$",
                                                        "$1");

                    if ((sVersion.length() != sKey.length()) &&
                            !mVersionMap.containsKey(sVersion))
                    {
                        // No.
                        continue;
                    }
                }

                // Delete this XForm.
                deleteXForm(mTuple);
            }
        }
    }
    
    private MessageContext getMessageContext() {
    	return srmSoap.getCurrentNomCollector().getMessageContext();
    }    
}
