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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.tools.ant.Project;

import com.cordys.coe.ant.studio.task.StudioTaskHandler;
import com.cordys.coe.ant.studio.util.StudioInitializer;
import com.cordys.coe.exception.GeneralException;
import com.cordys.coe.util.xml.Message;
import com.cordys.coe.util.xml.MessageContext;
import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.cm.EBcpVersion;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.NomCollector;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.GeneralUtils;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Exports Studio XForms from ECX to local file system.
 *
 * @author mpoyhone
 */
public class XFormsExporter
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
     * The build framework task that calls this object.
     */
    protected ContentManagerTask cmtTask;
    /**
     * Destination directory for studio files.
     */
    protected File fDestDir;
    /**
     * Soap request manager.
     */
    protected ISoapRequestManager srmSoap;
    /**
     * A map containing requested versions. If this is null, all
     * versions are exported.
     */
    protected Map<String, Boolean> mVersionMap = null;
    /**
     * The root Studio path in that is passed to the first
     * GetCollection method.
     */
    protected String sStudioXFormRootDir;
    /**
     * Studio content hanler.
     */
    protected StudioTaskHandler sthTask;

/**
     * Creates a new XFormsExporter object.
     *
     * @param sStudioXFormRootDir The SOAP connection used to call ECX methods.
     * @param fDestDir Destination directory for studio files.
     */
    public XFormsExporter(String sStudioXFormRootDir, File fDestDir)
    {
        this.sStudioXFormRootDir = sStudioXFormRootDir;
        this.fDestDir = fDestDir;
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
            // Handle the Studio folder contents recursively.
            recurseFolders(sStudioXFormRootDir, "");
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to export studio XForms.", e);
        }
        finally
        {
            srmSoap.removeNomCollector().deleteNodes();
        }
    }

    /**
     * Sets all versions that will be exported. Multiple versions are
     * separated with a semi-colon ( ; ).
     *
     * @param sVersions Version string.
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

            sXml = "<Export xmlns='http://schemas.cordys.com/1.0/cas-vcm'>" +
                   "	<XformsModels>" +
                   "   		<exportfilename></exportfilename>" + "   		<Models>" +
                   "   		</Models>" + "	</XformsModels>" + "</Export>";
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
            throw new IllegalStateException("Unable to parse the SOAP message " +
                                            sXml + " : " + e);
        }
    }

    /**
     * Exports the given XForm to the file system
     *
     * @param sXFormKey The XForm key to be passed to the Export method.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected void exportXForm(String sXFormKey)
                        throws Exception
    {
        // Get the object status.
        Message mObjectRequest = getMessageContext()
                                     .createMessage(mMethodGetObject);
        Message mObjectResponse;

        mObjectRequest.setValue(".//key", sXFormKey);

        mObjectResponse = srmSoap.makeSoapRequest(mObjectRequest);

        // Check that this is a correct XForm object.
        if (Find.firstMatch(mObjectResponse.getXmlNode(), "?<tuple><old><caf>") == 0)
        {
            // No, it wasn't.
            return;
        }

        // Get the properties needed for Export method.
        String sXFormVersion = mObjectResponse.getValue(".//caf/documentProperties/version");
        String sXFormFileName = mObjectResponse.getValue(".//tuple/@name", true);

        String sDestFileName;

        // Create the destination file name
        sDestFileName = sXFormKey.substring(sStudioXFormRootDir.length());
        sDestFileName = sDestFileName.replaceFirst("_" + sXFormVersion, "");

        File fDestFile = new File(fDestDir, sDestFileName);
        File fDestFileParentDir = fDestFile.getParentFile();

        // Create the destination directory if needed.    
        if (!fDestFileParentDir.exists())
        {
            fDestFileParentDir.mkdirs();
        }

        // Get XForm contents
        Message mCafContent = mObjectResponse.select(".//caf/content/xformhtml");

        if (mCafContent == null)
        {
            cmtTask.log("[studio-xforms] XForm '" + sDestFileName +
                        "' has no contents.", Project.MSG_ERR);
            return;
        }

        Message mCaf = mObjectResponse.select(".//caf");

        modifyDocumentProperties(mCaf);

        // Set the key as set for the runtime version.
        mCaf.setValue("/caf/content/xformhtml/@key", "/" + sXFormFileName);

        // Set the version information
        mCaf.setValue("/caf/content/xformhtml/@wcpforms:vcmVersion",
                      sXFormVersion);

        String sPrettyPrintFiles = GeneralUtils.getTrimmedProperty(cmtTask.getProject(), "studio.xforms.export.formatxml");
        int iWriteMode = Node.WRITE_NORMAL;
        
        if (sPrettyPrintFiles != null && sPrettyPrintFiles.trim().toLowerCase().equals("true")) {
            iWriteMode = Node.WRITE_PRETTY;
        }
        
        // Remove any namespace declarations from the root.
        int xFileNode = mCaf.getXmlNode();
        
        for (int i = 1; i <= Node.getNumAttributes(xFileNode); i++)
        {
            String name = Node.getAttributeName(xFileNode, i);
            
            if (name != null && (name.equals("xmlns") || name.startsWith("xmlns:"))) {
                Node.removeAttribute(xFileNode, name);
                i--;
            }
        }
        
        // Write XForm contents to the file.
        Node.writeToFile(xFileNode, 0,
                         fDestFile.getAbsolutePath(), iWriteMode); 

        cmtTask.log("[studio-xforms] Exported XForm " + sDestFileName,
                    Project.MSG_INFO);
        cmtTask.log("[studio-xforms] Output File " +
                    fDestFile.getAbsolutePath(), Project.MSG_VERBOSE);
        
        sthTask.addProcessItem();
    }

    /**
     * Exports the translation file of the given XForm to the file
     * system
     *
     * @param mXFormTuple The XForm tuple.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected void exportXFormTranslation(Message mXFormTuple)
                                   throws Exception
    {
        String sKey = mXFormTuple.getValue("./@key", true);
        String sName = mXFormTuple.getValue("./@name", true);

        //Determine the version. Studio prevents using underscores in the version,
        //so we can safely search for that.
        int iVersionStart = sKey.lastIndexOf('_') + 1;
        int iVersionEnd = sKey.lastIndexOf(".caf");

        if ((iVersionStart == -1) || (iVersionEnd == -1))
        {
            throw new StudioException("XForm version could not be found from the key.");
        }

        String sVcmVersion = sKey.substring(iVersionStart, iVersionEnd);

        // Delete the XForm translation file as well, if present.
        Message mGetTranslationRequest = getMessageContext()
                                             .createMessage(mMethodGetObject);
        Message mGetTranslationResponse = null;
        String sTranslationName;
        
        if (cmtTask.getVersionInfo().isLaterThan(EBcpVersion.BCP42_C3)) {
            sTranslationName = sKey.substring(sStudioXFormRootDir.length()).replaceFirst("(.*).caf$", "$1.mlm");
        } else {
            sTranslationName = sName.replaceFirst("(.*).caf$", "$1.mlm");
        }

        String sTranslationKey = StudioConstants.DEFAULT_XFORMS_TRANSLATION_ROOT_FOLDER +
                                 "/" + sTranslationName;

        mGetTranslationRequest.setValue(".//key", sTranslationKey);
        mGetTranslationRequest.setValue(".//key/@vcmVersion", sVcmVersion);

        try
        {
            mGetTranslationResponse = srmSoap.makeSoapRequest(mGetTranslationRequest);
        }
        catch (SoapRequestException e)
        {
            // No translation found.
            return;
        }

        // Get XForm translation contents
        Message mContents = mGetTranslationResponse.select("./tuple/old/mlm");

        if (mContents == null)
        {
            cmtTask.log("[studio-xforms] XForm translation '" +
                        sTranslationName + "' has no contents.", Project.MSG_ERR);
            return;
        }

        modifyDocumentProperties(mContents);

        String sDestFileName;

        // Create the destination file name
        sDestFileName = sKey.substring(sStudioXFormRootDir.length());
        sDestFileName = sDestFileName.replaceFirst("_" + sVcmVersion, "");
        sDestFileName = sDestFileName.replaceFirst("(.*).caf$", "$1.mlm");

        File fDestFile = new File(fDestDir, sDestFileName);
        File fDestFileParentDir = fDestFile.getParentFile();

        // Use .mlm directory for translations.
        fDestFileParentDir = new File(fDestFileParentDir, "mlm");
        fDestFile = new File(fDestFileParentDir, fDestFile.getName());

        // Create the destination directory if needed.    
        if (!fDestFileParentDir.exists())
        {
            fDestFileParentDir.mkdirs();
        }

        // Write XForm translation contents to the file.
        String sPrettyPrintFiles = GeneralUtils.getTrimmedProperty(cmtTask.getProject(), "studio.xforms.export.formatxml");
        int iWriteMode = Node.WRITE_NORMAL;

        if ((sPrettyPrintFiles != null) &&
                sPrettyPrintFiles.trim().toLowerCase().equals("true"))
        {
            iWriteMode = Node.WRITE_PRETTY;
        }

        Node.writeToFile(mContents.getXmlNode(), 0,
                         fDestFile.getAbsolutePath(), iWriteMode);

        cmtTask.log("[studio-xforms] Exported XForm translation " +
                    sDestFileName, Project.MSG_INFO);
        cmtTask.log("[studio-xforms] Output File " +
                    fDestFile.getAbsolutePath(), Project.MSG_VERBOSE);
        
        sthTask.addProcessItem();
    }

    /**
     * Modifies the document properties for the file system. Currently
     * the last lastModifiedBy and lastModificationDate are removed.
     *
     * @param mContents XForm/mlm strucuture.
     */
    protected void modifyDocumentProperties(Message mContents)
    {
        if (mContents == null)
        {
            return;
        }

        try
        {
            mContents.setValue(".//documentProperties/createdBy", "");
            mContents.setValue(".//documentProperties/creationDate", "");
            
            mContents.setValue(".//documentProperties/lastModifiedBy", "");
            mContents.setValue(".//documentProperties/lastModificationDate", "");
            
            mContents.setValue(".//documentProperties/customProperties/published", "");
            mContents.setValue(".//documentProperties/customProperties/publishDate", "");
            mContents.setValue(".//documentProperties/customProperties/publishUser", "");
        }
        catch (GeneralException e)
        {
        }
    }

    /**
     * Exports the Studio folders recursively based on the configured
     * content folder.
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
        Message mRequest = getMessageContext()
                               .createMessage(mMethodGetCollection);
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
            }
            else
            {
                // Check if the filter matches
                if (!sthTask.getCurrentContent()
                                .isPathAccepted(sRelativeKey,
                                                    StudioConstants.ECX_XFORMS_FILTER_MATCH_PATTERN))
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

                // Export this XForm.
                exportXForm(sKey);
                exportXFormTranslation(mTuple);
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
