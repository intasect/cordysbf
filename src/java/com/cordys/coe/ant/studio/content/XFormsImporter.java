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
import java.util.Iterator;

import org.apache.tools.ant.Project;

import com.cordys.coe.ant.studio.task.StudioTaskHandler;
import com.cordys.coe.ant.studio.util.StudioInitializer;
import com.cordys.coe.ant.studio.util.XFormsUtils;
import com.cordys.coe.exception.GeneralException;
import com.cordys.coe.util.xml.Message;
import com.cordys.coe.util.xml.MessageContext;
import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.cm.EBcpVersion;
import com.cordys.tools.ant.cm.IContent;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.NomCollector;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * Imports Studio XForms from local file system to ECX.
 *
 * @author mpoyhone
 */
public class XFormsImporter
{
    /**
     * The static document used in message templates.
     */
    protected static Document dRequestDoc = StudioInitializer.dRequestDoc;
    /**
     * The Studio UpdateObject method SOAP message template.
     */
    protected static Message mMethodUpdateObject;
    /**
     * The Studio UpdateObject method SOAP message template.
     */
    protected static Message mMethodUpdateObjectTranslation;
    /**
     * The Studio GetObject method SOAP message template.
     */
    protected static Message mMethodGetObject;

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
     * Source directory for studio files.
     */
    protected File fSrcDir;
    /**
     * Soap request manager.
     */
    protected ISoapRequestManager srmSoap;
    /**
     * Studio XForms root key.
     */
    protected String sStudioXFormsRootKey;
    /**
     * Studio content hanler.
     */
    protected StudioTaskHandler sthTask;

    /**
     * Creates a new FlowImporter object.
     *
     * @param fSrcDir Source directory for studio files.
     * @param sRootKey Studio XForms root key.
     */
    public XFormsImporter(File fSrcDir, String sRootKey)
    {
        this.fSrcDir = fSrcDir;
        this.sStudioXFormsRootKey = sRootKey;
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
            
            if (StudioInitializer.initializeStudio(srmSoap, getMessageContext())) {
                cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS + "] Initialized Studio for this organization.", Project.MSG_INFO);
            }
            
            if (StudioInitializer.insertDefaultUserPreferences(srmSoap, getMessageContext())) {
                cmtTask.log("[" + IContent.TYPE_STUDIO_BPMS + "] Set default user preferences.", Project.MSG_INFO);
            }
            
            // Handle the file system folder contents recursively.
            recurseFolders(fSrcDir, "");
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to import studio XForms.", e);
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
            sXml = "    <UpdateObject xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">\r\n" +
                   "      <tuple key=\"\" name=\"\" objectID=\"0\" lastModified=\"0\" " +
                   "			  isFolder=\"false\" templatePath=\"\" unconditional=\"true\" " +
                   "		      version=\"organization\" ignoreReference=\"false\" vcmApplication=\"vcmRepository\" " +
                   "			  vcmVersion=\"\">\r\n" + "        <new>\r\n" +
                   "          <caf>\r\n" +
                   "            <documentProperties>\r\n" +
                   "              <name></name>\r\n" +
                   "              <description></description>\r\n" +
                   "              <caption/>\r\n" +
                   "              <mimeType>caf</mimeType>\r\n" +
                   "              <notes/>\r\n" +
                   "              <version></version>\r\n" +
                   "              <revision/>\r\n" +
                   "              <createdBy></createdBy>\r\n" +
                   "              <creationDate></creationDate>\r\n" +
                   "              <lastModifiedBy></lastModifiedBy>\r\n" +
                   "              <lastModificationDate></lastModificationDate>\r\n" +
                   "            </documentProperties>\r\n" +
                   "            <content></content>" + "          </caf>\r\n" +
                   "        </new>\r\n" + "      </tuple>\r\n" +
                   "    </UpdateObject>";

            mMethodUpdateObject = new Message(dRequestDoc, sXml);
            mMethodUpdateObject.getSharedXmlTree().setReadOnly(true);

            sXml = "    <UpdateObject xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">\r\n" +
                   "      <tuple key=\"\" name=\"\" objectID=\"0\" lastModified=\"0\" " +
                   "			  isFolder=\"false\" templatePath=\"\" unconditional=\"true\" " +
                   "		      version=\"organization\" ignoreReference=\"false\" vcmApplication=\"vcmRepository\" " +
                   "			  vcmVersion=\"\">\r\n" + "        <new>\r\n" +
                   "        </new>\r\n" + "      </tuple>\r\n" +
                   "    </UpdateObject>";

            mMethodUpdateObjectTranslation = new Message(dRequestDoc, sXml);
            mMethodUpdateObjectTranslation.getSharedXmlTree().setReadOnly(true);

            sXml = "    <GetObject xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">\r\n" +
                   "      <key version=\"organization\" vcmApplication=\"vcmRepository\" vcmVersion=\"\"></key>\r\n" +
                   "    </GetObject>";

            mMethodGetObject = new Message(dRequestDoc, sXml);
            mMethodGetObject.getSharedXmlTree().setReadOnly(true);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to parse the SOAP message " +
                                            sXml + " : " + e);
        }
    }

    /**
     * Imports the XForms translation file to Studio.
     *
     * @param fFile The XForms  translation file.
     * @param sCurrentFilterPath The current relative path.
     *
     * @throws StudioException Thrown if the operation failed.
     */
    protected void importTranslationFile(File fFile, String sCurrentFilterPath)
                                  throws Exception
    {
        Message mFileContents;

        try
        {
            mFileContents = getMessageContext().createMessage(dRequestDoc, fFile);
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to load XForms translation file " +
                                      fFile);
        }

        String sName = mFileContents.getValue("./documentProperties/name", true);
        String sVersion = mFileContents.getValue("./documentProperties/version",
                                                 true);
        String sKey = StudioConstants.DEFAULT_XFORMS_TRANSLATION_ROOT_FOLDER + "/" + sName;
        
        // Use full path for the C3
        if (cmtTask.getVersionInfo().isLaterThan(EBcpVersion.BCP42_C3)) {
            int pos = sCurrentFilterPath.lastIndexOf('/');
            
            if (pos > 0) {
                sKey = String.format("%s/%s/%s", 
                                     StudioConstants.DEFAULT_XFORMS_TRANSLATION_ROOT_FOLDER,
                                     sCurrentFilterPath.substring(1, pos),
                                     sName);
            }
        }

        cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS +
                    "] Synchronizing translation " + sName + " to cordys.",
                    Project.MSG_INFO);

        Message mUpdateRequest = getMessageContext().createMessage(mMethodUpdateObjectTranslation);
        String sUserName = cmtTask.getUserdn().replaceFirst("cn=([^,]*),.*",
                                                            "$1");

        mUpdateRequest.setValue("//tuple/@key", sKey);
        mUpdateRequest.setValue("//tuple/@name", sName);
        mUpdateRequest.setValue("//tuple/@vcmVersion", sName);

        mUpdateRequest.append("//tuple/new", mFileContents.select("."));

        long lNow = System.currentTimeMillis();
        String sCreatedBy;
        String sLastModifiedBy;
        String sCreationDate;
        String sLastModificationDate;

        // Try to get the creation and last modification properties from the XForm.
        sCreatedBy = mFileContents.getValue("./documentProperties/createdBy",
                                            sUserName);
        sCreationDate = mFileContents.getValue("./documentProperties/creationDate",
                                               Long.toString(lNow));
        sLastModifiedBy = mFileContents.getValue("./documentProperties/lastModifiedBy",
                                                 sUserName);
        sLastModificationDate = mFileContents.getValue("./documentProperties/lastModificationDate",
                                                       Long.toString(lNow));

        try
        {
            // First try to see if we have an old version.
            Message mGetRequest = getMessageContext().createMessage(mMethodGetObject);
            Message mGetResponse = null;

            mGetRequest.setValue("GetObject/key", sKey);
            mGetRequest.setValue("GetObject/key/@vcmVersion", sVersion);

            mGetResponse = srmSoap.makeSoapRequest(mGetRequest);

            mUpdateRequest.setValue("//tuple/@objectID",
                                    mGetResponse.getValue("//tuple/@objectID",
                                                          true));
            mUpdateRequest.setValue("//tuple/@lastModified",
                                    mGetResponse.getValue("//tuple/@lastModified",
                                                          true));

            for (Iterator<?> iIter = mGetResponse.selectAll("//tuple//documentProperties/.");
                     iIter.hasNext();)
            {
                Message mNode = (Message) iIter.next();

                mUpdateRequest.setValue("//tuple//documentProperties/" +
                                        mNode.getName(), mNode.getValue("."));
            }

            mUpdateRequest.setValue("//tuple//documentProperties/lastModifiedBy",
                                    sLastModifiedBy);
            mUpdateRequest.setValue("//tuple//documentProperties/lastModificationDate",
                                    sLastModificationDate);
        }
        catch (Exception e)
        {
            mUpdateRequest.setValue("//tuple//documentProperties/name", sName);
            mUpdateRequest.setValue("//tuple//documentProperties/version",
                                    sVersion);
            mUpdateRequest.setValue("//tuple//documentProperties/createdBy",
                                    sCreatedBy);
            mUpdateRequest.setValue("//tuple//documentProperties/creationDate",
                                    sCreationDate);
        }

        try
        {
            srmSoap.makeSoapRequest(mUpdateRequest);
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to send the import SOAP request.",
                                      e);
        }

        cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS +
                    "] Imported XForm translation " + sKey, Project.MSG_VERBOSE);
        cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS + "] Input File " +
                    fFile, Project.MSG_VERBOSE);
        
        sthTask.addProcessItem();
    }

    /**
     * Imports the XForms file to Studio.
     *
     * @param fFile The XForms file.
     * @param sCurrentFilterPath The current relative path.
     *
     * @throws StudioException Thrown if the operation failed.
     */
    protected void importXFormFile(File fFile, String sCurrentFilterPath)
                            throws Exception
    {
        Message mFile;
        Message mFileContents;

        try
        {
            mFile = getMessageContext().createMessage(dRequestDoc, fFile);
            mFileContents = XFormsUtils.getXFormContent(mFile);
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to load XForms file " + fFile);
        }

        String sKey = mFileContents.getValue("./@key", true);
        String sVersion = mFileContents.getValue("./@wcpforms:vcmVersion", true);
        String sFileName;
        String sXFormName;
        String sXFormDescription;

        int iPos;

        if ((iPos = sKey.lastIndexOf('/')) != -1)
        {
            sFileName = sKey.substring(iPos + 1);
        }
        else
        {
            sFileName = sKey;
        }

        sXFormName = sKey.substring(1, sKey.length() - (sVersion.length() + 5));
        sXFormDescription = mFile.getValue("caf/documentProperties/description");

        if (sXFormDescription == null)
        {
            sXFormDescription = sXFormName;
        }
        
        // Update the namespaces for C3
        if (cmtTask.getVersionInfo().isLaterThan(EBcpVersion.BCP42_C3)) {
            declareNamespaces(mFileContents);
        }

        cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS + "] Synchronizing " +
                    sXFormName + " to cordys.", Project.MSG_INFO);

        Message mUpdateRequest = getMessageContext().createMessage(mMethodUpdateObject);
        String sUserName = cmtTask.getUserdn().replaceFirst("cn=([^,]*),.*",
                                                            "$1");
        String sFullCobocKey;
        
        if (sCurrentFilterPath != null && sCurrentFilterPath.length() > 0)
        {
            String tmp = sCurrentFilterPath.startsWith("/") ? sCurrentFilterPath.substring(1) : sCurrentFilterPath;
            
            sFullCobocKey = sStudioXFormsRootKey + tmp + "/" + sFileName;
        } else {
            sFullCobocKey = sStudioXFormsRootKey + sFileName;
        }

        mUpdateRequest.setValue("//tuple/@name", sFileName);

        long lNow = System.currentTimeMillis();
        String sCreatedBy;
        String sLastModifiedBy;
        String sCreationDate;
        String sLastModificationDate;

        // Try to get the creation and last modification properties from the XForm.
        sCreatedBy = mFile.getValue("caf/documentProperties/createdBy",
                                    sUserName);
        sCreationDate = mFile.getValue("caf/documentProperties/creationDate",
                                       Long.toString(lNow));
        sLastModifiedBy = mFile.getValue("caf/documentProperties/lastModifiedBy",
                                         sUserName);
        sLastModificationDate = mFile.getValue("caf/documentProperties/lastModificationDate",
                                               Long.toString(lNow));

        try
        {
            // First try to see if we have an old version.
            Message mGetRequest = getMessageContext().createMessage(mMethodGetObject);
            Message mGetResponse = null;

            mGetRequest.setValue("GetObject/key", sFullCobocKey);
            mGetRequest.setValue("GetObject/key/@vcmVersion", sVersion);

            mGetResponse = srmSoap.makeSoapRequest(mGetRequest);

            mUpdateRequest.setValue("//tuple/@objectID",
                                    mGetResponse.getValue("//tuple/@objectID",
                                                          true));
            mUpdateRequest.setValue("//tuple/@lastModified",
                                    mGetResponse.getValue("//tuple/@lastModified",
                                                          true));

            for (Iterator<?> iIter = mGetResponse.selectAll("//tuple//documentProperties/.");
                     iIter.hasNext();)
            {
                Message mNode = (Message) iIter.next();

                mUpdateRequest.setValue("//tuple//documentProperties/" +
                                        mNode.getName(), mNode.getValue("."));
            }
            mUpdateRequest.setValue("//tuple//documentProperties/description",
                                    sXFormDescription);
            mUpdateRequest.setValue("//tuple//documentProperties/lastModifiedBy",
                                    sLastModifiedBy);
            mUpdateRequest.setValue("//tuple//documentProperties/lastModificationDate",
                                    sLastModificationDate);
        }
        catch (Exception e)
        {
            mUpdateRequest.setValue("//tuple//documentProperties/name",
                                    sXFormName);
            mUpdateRequest.setValue("//tuple//documentProperties/version",
                                    sVersion);
            mUpdateRequest.setValue("//tuple//documentProperties/description",
                                    sXFormDescription);
            mUpdateRequest.setValue("//tuple//documentProperties/createdBy",
                                    sCreatedBy);
            mUpdateRequest.setValue("//tuple//documentProperties/creationDate",
                                    sCreationDate);
        }

        mUpdateRequest.append("//content", mFileContents);

        try
        {
            // Set update tuple information
            mUpdateRequest.setValue("UpdateObject/tuple/@key", sFullCobocKey);
            mUpdateRequest.setValue("UpdateObject/tuple/@vcmVersion", sVersion);

            // We need to modify the key in xformhtml element also.
            mUpdateRequest.setValue("//caf/content/xformhtml/@key",
                                    sFullCobocKey);

            srmSoap.makeSoapRequest(mUpdateRequest);
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to send the import SOAP request.",
                                      e);
        }

        cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS + "] Imported XForm " +
                    sFullCobocKey, Project.MSG_VERBOSE);
        cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS + "] Input File " +
                    fFile, Project.MSG_VERBOSE);
        
        sthTask.addProcessItem();
    }
    

    /**
     * Declares the used namespaces in the 'xformhtml' element, so that the XML
     * is valid (required by C3). 
     * @param mContents Content root element.
     * @throws GeneralException 
     */
    protected void declareNamespaces(Message mContents) throws GeneralException
    {
        Message xformhtmlNode = mContents.select("//xformhtml");
        
        if (xformhtmlNode == null) {
            return;
        }
        
        if (Node.getAttribute(xformhtmlNode.getXmlNode(), "xmlns", "").length() == 0) {
            Node.setAttribute(xformhtmlNode.getXmlNode(), "xmlns", "http://schemas.cordys.com/1.0/xform");
        }
        
        if (Node.getAttribute(xformhtmlNode.getXmlNode(), "xmlns:xforms", "").length() == 0) {
            Node.setAttribute(xformhtmlNode.getXmlNode(), "xmlns:xforms", "http://www.w3.org/2002/xforms/cr");
        }
            
        if (Node.getAttribute(xformhtmlNode.getXmlNode(), "xmlns:ev", "").length() == 0) {
            Node.setAttribute(xformhtmlNode.getXmlNode(), "xmlns:ev", "http://www.w3.org/2001/xml-events");
        }
        
        if (Node.getAttribute(xformhtmlNode.getXmlNode(), "xmlns:wcpforms", "").length() == 0) {
            Node.setAttribute(xformhtmlNode.getXmlNode(), "xmlns:wcpforms", "http://schemas.cordys.com/wcp/xforms");
        }
        
        if (Node.getAttribute(xformhtmlNode.getXmlNode(), "xmlns:eibus", "").length() == 0) {
            Node.setAttribute(xformhtmlNode.getXmlNode(), "xmlns:eibus", "http://schemas.cordys.com/wcp/webframework");
        }
        
        Message xheadNode = mContents.select("./xhead");
        
        if (xheadNode != null && Node.getAttribute(xheadNode.getXmlNode(), "xmlns", "").length() == 0) {
            Node.removeAttribute(xheadNode.getXmlNode(), "xmlns");
        }
    }

    /**
     * Imports the file system directory contents and sub-directory contents to
     * Studio
     *
     * @param fCurrentDir The current file system directory to be imported.
     * @param sCurrentFilterPath The current path in Studio filter format.
     *
     * @throws StudioException Thrown if the operation failed.
     */
    protected void recurseFolders(File fCurrentDir, String sCurrentFilterPath)
                           throws Exception
    {
        // List all files and directories under this directory.
        File[] faFiles = fCurrentDir.listFiles();

        for (int iIndex = 0; iIndex < faFiles.length; iIndex++)
        {
            File fFile = faFiles[iIndex];

            if (fFile.isDirectory())
            {
                // Recurse into the sub-directory.
                recurseFolders(fFile, sCurrentFilterPath + "/" +
                               fFile.getName());
                continue;
            }

            String sPathKey = sCurrentFilterPath + "/" + fFile.getName();

            // Check that this is a valid XForms file.
            if (fFile.getName().toLowerCase().endsWith(".caf"))
            {
                // Check if the filter matches
                if (!sthTask.getCurrentContent().isPathAccepted(sPathKey,
                                                                    StudioConstants.FILESYSTEM_XFORMS_FILTER_MATCH_PATTERN))
                {
                    continue;
                }

                importXFormFile(fFile, sCurrentFilterPath);
            }
            else if (fFile.getName().toLowerCase().endsWith(".mlm"))
            {
                // Check if the filter matches
                if (!sthTask.getCurrentContent().isPathAccepted(sPathKey,
                                                                    StudioConstants.FILESYSTEM_XFORMS_MLM_FILTER_MATCH_PATTERN))
                {
                    continue;
                }

                importTranslationFile(fFile, sCurrentFilterPath);
            }
        }
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
}
