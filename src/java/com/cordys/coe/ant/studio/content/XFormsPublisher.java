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

import org.apache.tools.ant.Project;

import com.cordys.coe.ant.studio.task.StudioTaskHandler;
import com.cordys.coe.ant.studio.util.StudioInitializer;
import com.cordys.coe.ant.studio.util.XFormsUtils;
import com.cordys.coe.util.xml.Message;
import com.cordys.coe.util.xml.MessageContext;
import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.cm.IContent;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.NomCollector;
import com.eibus.xml.nom.Document;

/**
 * This class publishes all the XForms that are in the organization to the
 * runtime environment.
 *
 * @author pgussow
 */
public class XFormsPublisher
{
    /**
     * The static document used in message templates.
     */
    protected static Document dRequestDoc = StudioInitializer.dRequestDoc;
    /**
     * The Studio Publish method SOAP message template.
     */
    protected static Message mPublish;
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
    public XFormsPublisher(File fSrcDir, String sRootKey)
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
            // Handle the file system folder contents recursively.
            recurseFolders(fSrcDir, "");
        }
        catch (Exception e)
        {
            throw new StudioException("Unable to publish XForms to runtime.", e);
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
            sXml = "    <Publish xmlns=\"http://schemas.cordys.com/1.0/xform\">\r\n" +
                   "      <key type=\"\" version=\"\" vcmApplication=\"\" vcmVersion=\"\"></key>" +
                   "    </Publish>";

            mPublish = new Message(dRequestDoc, sXml);
            mPublish.getSharedXmlTree().setReadOnly(true);

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
     * Imports the XForms file to Studio.
     *
     * @param fFile The XForms file.
     * @param sCurrentFilterPath The current hierarchy in the folder structure.
     *
     * @throws StudioException Thrown if the operation failed.
     */
    protected void publishXFormToRuntime(File fFile, String sCurrentFilterPath)
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
        cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS + "] Publishing " +
                    sXFormName + " to the runtime environment.",
                    Project.MSG_INFO);

        Message mPublishRequest = getMessageContext().createMessage(mPublish);
        mPublishRequest.setValue("//key/@type", "form");
        mPublishRequest.setValue("//key/@version", "organization");
        mPublishRequest.setValue("//key/@vcmApplication", "vcmRepository");
        mPublishRequest.setValue("//key/@vcmVersion", sVersion);

        //Build up the real key in Studio. It consists of the designated root 
        //folder, the relative path and the actual filename.
        String sFullCobocKey;
        
        sFullCobocKey = String.format("%s%s%s",
                sStudioXFormsRootKey,
                (sCurrentFilterPath.length() > 0 ? (sCurrentFilterPath + "/") : ""),
                sFileName);
        cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS + "] Full CobocKey: " +
                    sFullCobocKey, Project.MSG_DEBUG);
        mPublishRequest.setValue("//key", sFullCobocKey);

        try
        {
            srmSoap.makeSoapRequest(mPublishRequest);
        }
        catch (Exception e)
        {
            cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS +
                        "] Error publishing XForm " + sXFormName + ":\n" +
                        e.toString(), Project.MSG_INFO);
        }

        cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS + "] Published XForm " +
                    sFullCobocKey, Project.MSG_DEBUG);
        cmtTask.log("[" + IContent.TYPE_STUDIO_XFORMS + "] Input File " +
                    fFile, Project.MSG_DEBUG);
        
        sthTask.addProcessItem();
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

            // Check that this is a valid XForms file.
            if (!fFile.getName().toLowerCase().endsWith(".caf"))
            {
                continue;
            }

            // Check if the filter matches
            if (!sthTask.getCurrentContent().isPathAccepted(sCurrentFilterPath +
                                                                "/" +
                                                                fFile.getName(),
                                                                StudioConstants.FILESYSTEM_XFORMS_FILTER_MATCH_PATTERN))
            {
                continue;
            }

            publishXFormToRuntime(fFile, sCurrentFilterPath);
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
