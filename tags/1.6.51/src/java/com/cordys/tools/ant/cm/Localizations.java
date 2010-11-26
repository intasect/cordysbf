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
package com.cordys.tools.ant.cm;

import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.util.FileUtil;
import com.cordys.tools.ant.util.GeneralUtils;

import com.eibus.version.Version;

import java.io.File;
import java.io.IOException;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

/**
 * This content handler is used for synchronizaing the localizations file.
 *
 * @author  pgussow
 */
public class Localizations
    implements ContentHandler
{
    /**
     * Identifies the name of the cordys.home property in the project.
     */
    private static final String PROP_CORDYS_HOME = "cordys.home";
    /**
     * Identifies the name of the cordys.home property in the project.
     */
    private static final String LOCALIZATION_FOLDER = "localization";
    /**
     * Identifies the name of the cordys.home property in the project.
     */
    private static final String PROP_LOCALIZATION_FOLDER = "src.content.localizations";
    /**
     * The minimal build number for the Cordys installation to support this content type.
     */
    private static final int MINIMAL_CORDYS_VERSION = 387;
    /**
     * Number of processed items.
     */
    protected int iProcessedCounter;

    /**
     * Increments the number of processed items.
     */
    public void addProcessItem()
    {
        iProcessedCounter++;
    }

    /**
     * The method should be implemented by the content handlers and will be called according the
     * operation specified. The method deletes the content specified. The task instance is passed on
     * for performing certains tasks in the ant workspace.
     *
     * @param  cmtTask   The current contentmanager task.
     * @param  cContent  The specific content that needs to be published.
     * @param  srmSoap   The object to use for sending soap messages.
     *
     * @see    com.cordys.tools.ant.cm.ContentHandler#executeDelete(com.cordys.tools.ant.cm.ContentManagerTask,
     *         com.cordys.tools.ant.cm.Content, com.cordys.tools.ant.soap.ISoapRequestManager)
     */
    public void executeDelete(ContentManagerTask cmtTask, Content cContent,
                              ISoapRequestManager srmSoap)
    {
        if (doesCurrentCordysSupportContent(cmtTask))
        {
            Project pProject = cmtTask.getProject();
            Hashtable<?, ?> hProperties = pProject.getProperties();

            if (!hProperties.containsKey(PROP_CORDYS_HOME))
            {
                throw new BuildException("Could not find the " + PROP_CORDYS_HOME +
                                         " property in the project.");
            }

            if (!hProperties.containsKey(PROP_LOCALIZATION_FOLDER))
            {
                throw new BuildException("Could not find the " + PROP_LOCALIZATION_FOLDER +
                                         " property in the project.");
            }

            // Get the Cordys home folder
            String sCordysHome = GeneralUtils.getTrimmedProperty(pProject, PROP_CORDYS_HOME);
            File fCordysHome = new File(sCordysHome);

            if (!fCordysHome.exists() || !fCordysHome.isDirectory())
            {
                throw new BuildException("cordys.home (" + sCordysHome +
                                         ") is not a valid folder.");
            }

            File fLocalization = new File(fCordysHome, LOCALIZATION_FOLDER);

            if (!fLocalization.exists())
            {
                fLocalization.mkdirs();
            }

            File fLocalFolder = new File(GeneralUtils.getTrimmedProperty(pProject,
                                                                         PROP_LOCALIZATION_FOLDER));

            if (!fLocalFolder.exists())
            {
                fLocalFolder.mkdirs();
            }

            // Do the delete.
            try
            {
                FilterSet fsFilters = cContent.getFilterSet();
                String[] asFiles = fLocalization.list();

                for (int iCount = 0; iCount < asFiles.length; iCount++)
                {
                    String sFilename = asFiles[iCount];
                    File fToBeDeleted = new File(fLocalization, sFilename);

                    if (fsFilters.isPathAccepted(sFilename))
                    {
                        cmtTask.log("Deleting file " + fToBeDeleted.getCanonicalPath(),
                                    Project.MSG_DEBUG);

                        if (fToBeDeleted.delete())
                        {
                            addProcessItem();
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new BuildException("Error deleting localization files.", e);
            }
        }
        else
        {
            cmtTask.log("The Cordys version you are connecting to does not support this content type.",
                        Project.MSG_VERBOSE);
        }
    }

    /**
     * The method should be implemented by the content handlers and will be called according the
     * operation specified. The method exports contents from the ECX to a file or files in a
     * directory. The task instance is passed on for performing certains tasks in the ant workspace.
     *
     * @param  cmtTask   The current contentmanager task.
     * @param  cContent  The specific content that needs to be published.
     * @param  srmSoap   The object to use for sending soap messages.
     *
     * @see    com.cordys.tools.ant.cm.ContentHandler#executeEcxToFile(com.cordys.tools.ant.cm.ContentManagerTask,
     *         com.cordys.tools.ant.cm.Content, com.cordys.tools.ant.soap.ISoapRequestManager)
     */
    public void executeEcxToFile(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        if (doesCurrentCordysSupportContent(cmtTask))
        {
            Project pProject = cmtTask.getProject();
            Hashtable<?, ?> hProperties = pProject.getProperties();

            if (!hProperties.containsKey(PROP_CORDYS_HOME))
            {
                throw new BuildException("Could not find the " + PROP_CORDYS_HOME +
                                         " property in the project.");
            }

            // Get the Cordys home folder
            String sCordysHome = GeneralUtils.getTrimmedProperty(pProject, PROP_CORDYS_HOME);
            File fCordysHome = new File(sCordysHome);

            if (!fCordysHome.exists() || !fCordysHome.isDirectory())
            {
                throw new BuildException("cordys.home (" + sCordysHome +
                                         ") is not a valid folder.");
            }

            File fLocalization = new File(fCordysHome, LOCALIZATION_FOLDER);

            if (!fLocalization.exists())
            {
                fLocalization.mkdirs();
            }

            if (!fLocalization.canRead())
            {
                throw new BuildException("Cannot write to the localization folder " +
                                         fLocalization.getAbsolutePath() + ".");
            }

            File fLocalFolder = new File(GeneralUtils.getTrimmedProperty(pProject,
                                                                         PROP_LOCALIZATION_FOLDER));

            if (!fLocalFolder.exists())
            {
                fLocalFolder.mkdirs();
            }

            Copy cCopy = new Copy();
            cCopy.setProject(pProject);
            cCopy.setTodir(fLocalFolder);
            cCopy.setOverwrite(true);
            cCopy.setTaskName(cmtTask.getTaskName());

            FilterSet fsFilters = cContent.getFilterSet();
            List<?> lFilters = fsFilters.getFilterList();

            for (Iterator<?> iFilter = lFilters.iterator(); iFilter.hasNext();)
            {
                Filter fFilter = (Filter) iFilter.next();
                FileSet fsFiles = new FileSet();
                fsFiles.setDir(fLocalization);

                if (fFilter.getInclusive())
                {
                    fsFiles.setIncludes(fFilter.getRegexp());
                }
                else
                {
                    fsFiles.setExcludes(fFilter.getRegexp());
                }
                cCopy.addFileset(fsFiles);
                addProcessItem();
            }

            cCopy.execute();
        }
        else
        {
            cmtTask.log("The Cordys version you are connecting to does not support this content type.",
                        Project.MSG_VERBOSE);
        }
    }

    /**
     * This method will write the localization files that are in the project to the Cordys
     * environment.
     *
     * @param  cmtTask   The current contentmanager task.
     * @param  cContent  The specific content that needs to be published.
     * @param  srmSoap   The object to use for sending soap messages.
     *
     * @see    com.cordys.tools.ant.cm.ContentHandler#executeFileToEcx(com.cordys.tools.ant.cm.ContentManagerTask,
     *         com.cordys.tools.ant.cm.Content, com.cordys.tools.ant.soap.ISoapRequestManager)
     */
    public void executeFileToEcx(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        if (doesCurrentCordysSupportContent(cmtTask))
        {
            Project pProject = cmtTask.getProject();
            Hashtable<?, ?> hProperties = pProject.getProperties();

            if (!hProperties.containsKey(PROP_CORDYS_HOME))
            {
                throw new BuildException("Could not find the " + PROP_CORDYS_HOME +
                                         " property in the project.");
            }

            // Get the Cordys home folder
            String sCordysHome = GeneralUtils.getTrimmedProperty(pProject, PROP_CORDYS_HOME);
            File fCordysHome = new File(sCordysHome);

            if (!fCordysHome.exists() || !fCordysHome.isDirectory())
            {
                throw new BuildException("cordys.home (" + sCordysHome +
                                         ") is not a valid folder.");
            }

            File fLocalization = new File(fCordysHome, LOCALIZATION_FOLDER);

            if (!fLocalization.exists())
            {
                fLocalization.mkdirs();
            }

            if (!fLocalization.canWrite())
            {
                throw new BuildException("Cannot write to the localization folder " +
                                         fLocalization.getAbsolutePath() + ".");
            }

            // We will simply do a file copy from the local filesystem
            FileSet[] afsFiles = getFilesets(cContent);

            Copy cCopy = new Copy();
            cCopy.setProject(pProject);
            cCopy.setTodir(fLocalization);
            cCopy.setOverwrite(true);

            for (int iCount = 0; iCount < afsFiles.length; iCount++)
            {
                cCopy.addFileset(afsFiles[iCount]);
                addProcessItem();
            }

            cCopy.execute();
        }
        else
        {
            cmtTask.log("The Cordys version you are connecting to does not support this content type.",
                        Project.MSG_VERBOSE);
        }
    }

    /**
     * This method should take care of publishing the specific content to runtime. For example
     * studio flows need to be published to the runtime environment before they can be run. This
     * method should take care of that.
     *
     * @param  cmtTask   The current contentmanager task.
     * @param  cContent  The specific content that needs to be published.
     * @param  srmSoap   The object to use for sending soap messages.
     *
     * @see    com.cordys.tools.ant.cm.ContentHandler#executePublishToRuntime(com.cordys.tools.ant.cm.ContentManagerTask,
     *         com.cordys.tools.ant.cm.Content, com.cordys.tools.ant.soap.ISoapRequestManager)
     */
    public void executePublishToRuntime(ContentManagerTask cmtTask, Content cContent,
                                        ISoapRequestManager srmSoap)
    {
        // Noting to be done here.
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

        File baseDir = content.getDir();

        return FileUtil.getRelativePath(baseDir, contentFile);
    }

    /**
     * This method checks if the current version of Cordys actually supports this content type.
     *
     * @param   cmtTask  DOCUMENTME
     *
     * @return  true if the Cordys version supports this content type. Otherwise false.
     */
    private boolean doesCurrentCordysSupportContent(ContentManagerTask cmtTask)
    {
        boolean bReturn = true;

        Version vCordysVersion = new Version();

        cmtTask.log(Version.getString() + "\nBuild: " + vCordysVersion.getBuild() + "\nVersion: " +
                    vCordysVersion.getVersion() + "\nSourceControlLabel: " +
                    vCordysVersion.getSourceControlLabel(), Project.MSG_DEBUG);

        // Just checking for the build number is not sufficient anymore.
        // Localizations are supported from C2 build 387 upwards.
        String sBuildNumber = vCordysVersion.getBuild().replaceAll("[^\\d]", "");
        int buildNumber = 0;

        try
        {
            buildNumber = Integer.parseInt(sBuildNumber);

            if ((buildNumber < MINIMAL_CORDYS_VERSION) &&
                    !vCordysVersion.getVersion().startsWith("C3"))
            {
                bReturn = false;
            }
        }
        catch (Exception e)
        {
            // Default we'll return true
            cmtTask.log("[localizations] Exception parsing versions. Assuming Cordys supports the content",
                        Project.MSG_INFO);
            bReturn = true;
        }

        return bReturn;
    }

    /**
     * This method returns the actual filesets that should be used.
     *
     * @param   cContent  The content
     *
     * @return  The actual filesets.
     */
    private FileSet[] getFilesets(Content cContent)
    {
        Vector<?> vFilesets = cContent.getFileSet();
        Vector<FileSet> vActualFilesets = new Vector<FileSet>();

        if (cContent.getDir() != null)
        {
            if (!cContent.getDir().exists())
            {
                GeneralUtils.handleException("Folder does not exist!\nFolder:" +
                                             cContent.getContentFile().getAbsolutePath());
            }

            FileSet fs = (FileSet) cContent.getImplicitFileSetUsed().clone();
            fs.setDir(cContent.getDir());
            vActualFilesets.addElement(fs);
        }

        for (int i = 0; i < vFilesets.size(); i++)
        {
            FileSet fs = (FileSet) vFilesets.elementAt(i);
            vActualFilesets.addElement(fs);
        }

        FileSet[] afsReturn = new FileSet[vActualFilesets.size()];
        vActualFilesets.copyInto(afsReturn);

        return afsReturn;
    }
}
