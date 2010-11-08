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
package com.cordys.coe.ant.studio.task;

import com.cordys.coe.ant.studio.content.FlowDeleter;
import com.cordys.coe.ant.studio.content.FlowExporter;
import com.cordys.coe.ant.studio.content.FlowImporter;
import com.cordys.coe.ant.studio.content.FlowPublisher;
import com.cordys.coe.ant.studio.content.StudioConstants;
import com.cordys.coe.ant.studio.content.StudioException;
import com.cordys.coe.ant.studio.content.XFormsDeleter;
import com.cordys.coe.ant.studio.content.XFormsExporter;
import com.cordys.coe.ant.studio.content.XFormsImporter;
import com.cordys.coe.ant.studio.content.XFormsPublisher;

import com.cordys.tools.ant.cm.Content;
import com.cordys.tools.ant.cm.ContentHandler;
import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.cm.IContent;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.util.FileUtil;
import com.cordys.tools.ant.util.GeneralUtils;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Project;

/**
 * A build framework content handler for Studio content. If content filtering is needed a custom
 * content element with the handler class of StudioFilterHandler must be placed before this content
 * element. In case of ECX export or import operations the destination or source directory must be
 * given with the 'dir'-attribute. A sample configuration :
 *
 * <pre>
      <content type="studio-flows" dir="${src.content.studio}/flows" handler="com.cordys.coe.ant.studio.task.StudioTaskHandler" />
 * </pre>
 *
 * @author  mpoyhone
 */
public class StudioTaskHandler
    implements ContentHandler
{
    /**
     * Current content element.
     */
    protected Content cCurrentContent;
    /**
     * Number of processed items.
     */
    protected int iProcessedCounter;

    /**
     * Creates a new StudioTaskHandler object.
     */
    public StudioTaskHandler()
    {
    }

    /**
     * Increments the number of processed items.
     */
    public void addProcessItem()
    {
        iProcessedCounter++;
    }

    /**
     * Executes the deletion Ant task. Deletes all the content from Studio that matches the
     * specified filter.
     *
     * @param  cmtTask   The Ant task object
     * @param  cContent  The content element corresponding to Studio content.
     * @param  srmSoap   The SOAP object used to make SOAP requests.
     */
    public void executeDelete(ContentManagerTask cmtTask, Content cContent,
                              ISoapRequestManager srmSoap)
    {
        cCurrentContent = cContent;

        cmtTask.log("[" + cContent.getType() + "] Deleting Studio content from installation...");

        String sType = cContent.getType();
        String sVersions;

        // Try to get version to delete. If not set, delete all versions.
        sVersions = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                    StudioConstants.EXPORT_VERSION_PROPERTY);

        if (sType.equals(IContent.TYPE_STUDIO_XFORMS))
        {
            XFormsDeleter fdDeleter = new XFormsDeleter(getXFormsRootFolder(cmtTask));

            fdDeleter.setDeleteVersions(sVersions);

            try
            {
                fdDeleter.execute(this, cmtTask, srmSoap, cmtTask.getUserdn(),
                                  cmtTask.getOrganization());
            }
            catch (StudioException e)
            {
                GeneralUtils.handleException("An error occurred when deleting Studio XForms. " +
                                             e.getMessage(), e, cmtTask);
                return;
            }
        }
        else if (sType.equals(IContent.TYPE_STUDIO_BPMS))
        {
            FlowDeleter fdDeleter = new FlowDeleter(getFlowsRootFolder(cmtTask));

            fdDeleter.setDeleteVersions(sVersions);

            try
            {
                fdDeleter.execute(this, cmtTask, srmSoap, cmtTask.getUserdn(),
                                  cmtTask.getOrganization());
            }
            catch (StudioException e)
            {
                GeneralUtils.handleException("An error occurred when deleting Studio flows. " +
                                             e.getMessage(), e, cmtTask);
                return;
            }
        }

        cmtTask.log("[" + cContent.getType() + "] Done.");
    }

    /**
     * This method exports the content from Studio to the local directory.
     *
     * @param  cmtTask   The task that is being executed.
     * @param  cContent  The content-definition
     * @param  srmSoap   The manager for sending soap-requests.
     */
    public void executeEcxToFile(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        cmtTask.log("[" + cContent.getType() +
                    "] Exporting Studio content from the ECX to the local files...",
                    Project.MSG_INFO);

        cCurrentContent = cContent;

        // Get the destination directory.
        File fBaseDir = getInputDirectory(cmtTask, cContent);

        if (fBaseDir == null)
        {
            GeneralUtils.handleException("[" + cContent.getType() +
                                         "] Output directory not set for the Studio content.");
            return;
        }

        cmtTask.log("[" + cContent.getType() + "] Export destination directory: " +
                    fBaseDir.getAbsolutePath(), Project.MSG_VERBOSE);

        String sVersions;

        // Try to get version to export. If not set, export all versions.
        sVersions = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                    StudioConstants.EXPORT_VERSION_PROPERTY);

        String sType = cContent.getType();

        if (sType.equals(IContent.TYPE_STUDIO_BPMS))
        {
            FlowExporter seExporter = new FlowExporter(getFlowsRootFolder(cmtTask), fBaseDir);

            seExporter.setExportVersions(sVersions);

            try
            {
                seExporter.execute(this, cmtTask, srmSoap, cmtTask.getUserdn(),
                                   cmtTask.getOrganization());
            }
            catch (StudioException e)
            {
                GeneralUtils.handleException("An error occurred when exporting Studio flows. " +
                                             e.getMessage(), e, cmtTask);
                return;
            }
        }
        else
        {
            XFormsExporter xeExporter = new XFormsExporter(getXFormsRootFolder(cmtTask), fBaseDir);

            xeExporter.setExportVersions(sVersions);

            try
            {
                xeExporter.execute(this, cmtTask, srmSoap, cmtTask.getUserdn(),
                                   cmtTask.getOrganization());
            }
            catch (StudioException e)
            {
                GeneralUtils.handleException("An error occurred when exporting Studio XForms. " +
                                             e.getMessage(), e, cmtTask);
                return;
            }
        }
    }

    /**
     * This method imports the content to Studio from the local directory.
     *
     * @param  cmtTask   The task that is being executed.
     * @param  cContent  The content-definition
     * @param  srmSoap   The manager for sending soap-requests.
     */
    public void executeFileToEcx(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        cCurrentContent = cContent;

        // Get the destination directory.
        File fBaseDir = getInputDirectory(cmtTask, cContent);
        String sType = cContent.getType();

        if (fBaseDir == null)
        {
            GeneralUtils.handleException("[" + cContent.getType() +
                                         "] Input directory not set for the Studio content.");
            return;
        }

        if (sType == null)
        {
            GeneralUtils.handleException("[" + cContent.getType() +
                                         "] Type not set for the Studio content.");
            return;
        }

        cmtTask.log("[" + cContent.getType() + "] Import source directory: " +
                    fBaseDir.getAbsolutePath(), Project.MSG_VERBOSE);

        if (sType.equals(IContent.TYPE_STUDIO_BPMS))
        {
            FlowImporter siImporter = new FlowImporter(fBaseDir);
            String sImportTempDir = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                                    "studio.import.temp.dir");

            if ((sImportTempDir != null) && (sImportTempDir.length() > 0))
            {
                File fDir = new File(sImportTempDir);

                if (!fDir.exists() || !fDir.isDirectory())
                {
                    GeneralUtils.handleException("[" + cContent.getType() +
                                                 "] Temporary directory " + fDir +
                                                 " does not exist or is not a directory.");
                    return;
                }

                cmtTask.log("[" + cContent.getType() + "] Using temporaty directory: " +
                            fDir.getAbsolutePath(), Project.MSG_VERBOSE);

                siImporter.setTempDir(fDir);
            }

            try
            {
                siImporter.execute(this, cmtTask, srmSoap, cmtTask.getUserdn(),
                                   cmtTask.getOrganization());
            }
            catch (StudioException e)
            {
                GeneralUtils.handleException("An error occurred when importing Studio flows. " +
                                             e.getMessage(), e, cmtTask);
                return;
            }
        }
        else if (sType.equals(IContent.TYPE_STUDIO_XFORMS))
        {
            XFormsImporter xiImported = new XFormsImporter(fBaseDir, getXFormsRootFolder(cmtTask));

            try
            {
                xiImported.execute(this, cmtTask, srmSoap, cmtTask.getUserdn(),
                                   cmtTask.getOrganization());
            }
            catch (StudioException e)
            {
                GeneralUtils.handleException("An error occurred when importing Studio XForms. " +
                                             e.getMessage(), e, cmtTask);
                return;
            }
        }
        else
        {
            GeneralUtils.handleException("[" + cContent.getType() + "] Invalid content type ");
            return;
        }

        cmtTask.log("[" + cContent.getType() + "] Done.", Project.MSG_INFO);
    }

    /**
     * This method should take care of publishing the specific content to runtime. It first checks
     * the tpye of the content. Either it's studio-bpms or studio-xforms. In both cases the
     * specified content should be published to runtime.
     *
     * @param  cmtTask   The current contentmanager task.
     * @param  cContent  The specific content that needs to be published.
     * @param  srmSoap   The object to use for sending soap messages.
     */
    public void executePublishToRuntime(ContentManagerTask cmtTask, Content cContent,
                                        ISoapRequestManager srmSoap)
    {
        cCurrentContent = cContent;

        // Get the destination directory.
        File fBaseDir = getInputDirectory(cmtTask, cContent);
        String sType = cContent.getType();

        if (fBaseDir == null)
        {
            GeneralUtils.handleException("[" + cContent.getType() +
                                         "] Input directory not set for the Studio content.");
            return;
        }

        if (sType == null)
        {
            GeneralUtils.handleException("[" + cContent.getType() +
                                         "] Type not set for the Studio content.");
            return;
        }

        cmtTask.log("[" + cContent.getType() + "] Import source directory: " +
                    fBaseDir.getAbsolutePath(), Project.MSG_VERBOSE);

        if (sType.equals(IContent.TYPE_STUDIO_BPMS))
        {
            FlowPublisher fpPublisher = new FlowPublisher(fBaseDir);

            try
            {
                fpPublisher.execute(this, cmtTask, srmSoap, cmtTask.getUserdn(),
                                    cmtTask.getOrganization());
            }
            catch (StudioException e)
            {
                GeneralUtils.handleException("An error occurred when publishing Studio flows. " +
                                             e.getMessage(), e, cmtTask);
                return;
            }
        }
        else if (sType.equals(IContent.TYPE_STUDIO_XFORMS))
        {
            XFormsPublisher xpPublisher = new XFormsPublisher(fBaseDir,
                                                              getXFormsRootFolder(cmtTask));

            try
            {
                xpPublisher.execute(this, cmtTask, srmSoap, cmtTask.getUserdn(),
                                    cmtTask.getOrganization());
            }
            catch (StudioException e)
            {
                GeneralUtils.handleException("An error occurred when publishing Studio XForms. " +
                                             e.getMessage(), e, cmtTask);
                return;
            }
        }
    }

    /**
     * Returns the current content element that this handler is processing.
     *
     * @return  Current content element.
     */
    public Content getCurrentContent()
    {
        return cCurrentContent;
    }

    /**
     * @see  com.cordys.tools.ant.cm.ContentHandler#getNumberOfProcessedItems()
     */
    public int getNumberOfProcessedItems()
    {
        return iProcessedCounter;
    }

    /**
     * @see  com.cordys.tools.ant.cm.ContentHandler#getSingleContentName(java.io.File,ContentManagerTask,
     *       Content, boolean)
     */
    public String getSingleContentName(File contentFile, ContentManagerTask cmtTask,
                                       Content content, boolean toEcx)
                                throws IOException
    {
        if ((contentFile == null) || !contentFile.exists())
        {
            return null;
        }

        String type = content.getType();
        File baseDir = content.getDir();
        String relFile = FileUtil.getRelativePath(baseDir, contentFile);

        if (type.equals(IContent.TYPE_STUDIO_BPMS))
        {
            return relFile.replaceFirst("_[^_]+\\.vcmdata$", "").replace("\\", "/");
        }
        else if (type.equals(IContent.TYPE_STUDIO_XFORMS))
        {
            String tmp = relFile.replaceFirst("\\.caf$", "");

            if (tmp.length() == relFile.length())
            {
                tmp = relFile.replaceFirst("(.*)([/\\\\])mlm\\2([^/\\\\]+)\\.mlm$", "$1$2$3");
            }

            return tmp.replace("\\", "/");
        }

        return null;
    }

    /**
     * Returns the file system input directory from the build configuration file for this task.
     *
     * @param   cmtTask   The Ant task object.
     * @param   cContent  The content element from configuration file.
     *
     * @return  The input directory or null if not found.
     */
    protected File getInputDirectory(ContentManagerTask cmtTask, Content cContent)
    {
        File fInputDir = null;

        // Get the input directory from the content tag if it is present.
        fInputDir = cContent.getDir();

        if (fInputDir != null)
        {
            return fInputDir;
        }

        return null;
    }

    /**
     * This method returns the base folder for the BPMs to export. This is needed to be able to
     * support extracting the full content.
     * 
     * @param   cmtTask  Current task.
     *
     * @return  The root folder for the flow.
     */
    private String getFlowsRootFolder(ContentManagerTask cmtTask)
    {
        String sStudioRootFolder;

        // Figure out the root folder from the property. If not set, use the XForms folder.
        sStudioRootFolder = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                            StudioConstants.STUDIO_ROOTFOLDER_PROPERTY);

        if ((sStudioRootFolder == null) || (sStudioRootFolder.length() == 0))
        {
            sStudioRootFolder = "/3.Business Process Models";
        }

        if (!sStudioRootFolder.startsWith("/"))
        {
            sStudioRootFolder = "/" + sStudioRootFolder;
        }

        sStudioRootFolder = StudioConstants.DEFAULT_FLOW_ROOT_FOLDER + sStudioRootFolder;

        return sStudioRootFolder;
    }

    /**
     * Returns Studio XForms root folder.
     *
     * @param   cmtTask  Current task.
     *
     * @return  Root folder.
     */
    private String getXFormsRootFolder(ContentManagerTask cmtTask)
    {
        String sStudioRootFolder;

        // Figure out the root folder from the property. If not set, use the XForms folder.
        sStudioRootFolder = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                            StudioConstants.XFORMS_ROOTFOLDER_PROPERTY);

        if ((sStudioRootFolder == null) || (sStudioRootFolder.length() == 0))
        {
            sStudioRootFolder = "/XForms";
        }

        if (!sStudioRootFolder.startsWith("/"))
        {
            sStudioRootFolder = "/" + sStudioRootFolder;
        }

        sStudioRootFolder = StudioConstants.DEFAULT_XFORMS_ROOT_FOLDER + sStudioRootFolder;

        return sStudioRootFolder;
    }
}
