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
import com.cordys.tools.ant.util.GeneralUtils;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;

/**
 * This class handles the content with regard to X-AS.
 *
 * @author pgussow
 */
public class XASTaskHandler
    implements ContentHandler
{
    /**
     * Identifies the name of the cordys.home property in the project.
     */
    private static final String PROP_CORDYS_HOME = "cordys.home";
    /**
     * Identifies the name of the xas.package property in the project.
     */
    private static final String PROP_XAS_PACKAGE = "xas.package";
    /**
     * Number of processed items.
     */
    protected int iProcessedCounter;

/**
     * Default contrustor.
     */
    public XASTaskHandler()
    {
        super();
    }

    /**
     * Increments the number of processed items.
     */
    public void addProcessItem()
    {
        iProcessedCounter++;
    }

    /**
     * This method deletes the files from the repository that are
     * related to the configured packages. In contrary of other delete tasks
     * that clear out the entire organizational content that is not done here.
     * Because XAS only has a system-wide repository with no means to track
     * back the organziation from which they are created.
     *
     * @param cmtTask DOCUMENTME
     * @param cContent DOCUMENTME
     * @param srmSoap DOCUMENTME
     */
    public void executeDelete(ContentManagerTask cmtTask, Content cContent,
                              ISoapRequestManager srmSoap)
    {
        Project pProject = cmtTask.getProject();
        Hashtable<?, ?> hProperties = pProject.getProperties();

        if (!hProperties.containsKey(PROP_CORDYS_HOME) ||
                !hProperties.containsKey(PROP_XAS_PACKAGE))
        {
            throw new BuildException("Missing either " + PROP_CORDYS_HOME +
                                     " or " + PROP_XAS_PACKAGE +
                                     " property in the project.");
        }

        //Get the Cordys home folder
        String sCordysHome = GeneralUtils.getTrimmedProperty(pProject, PROP_CORDYS_HOME);
        File fCordysHome = new File(sCordysHome);

        if (!fCordysHome.exists() || !fCordysHome.isDirectory())
        {
            throw new BuildException("cordys.home (" + sCordysHome +
                                     ") is not a valid folder.");
        }

        //Get the XAS packages to export.
        String sXASPackage = GeneralUtils.getTrimmedProperty(pProject, PROP_XAS_PACKAGE);
        String[] saXASPackages = parsePackages(sXASPackage);

        if ((saXASPackages != null) && (saXASPackages.length > 0))
        {
            //Check the BSF repository.
            File fRealPackageFolder = new File(fCordysHome,
                                               "bsf/repository/packages");
            File fRealClassmapFolder = new File(fCordysHome,
                                                "bsf/runtime/classmap");

            if (!checkIfBsfFoldersExist(cmtTask, cContent, fRealPackageFolder,
                                            fRealClassmapFolder))
            {
                return;
            }

            //Create the task that will delete the files from the repository
            Delete dDelete = new Delete();
            dDelete.setProject(cmtTask.getProject());
            dDelete.setTaskName(cmtTask.getTaskName());
            dDelete.setIncludeEmptyDirs(true);
            dDelete.setFailOnError(false);

            //First add the repository files to it.
            for (int iCount = 0; iCount < saXASPackages.length; iCount++)
            {
                FileSet fsTemp = new FileSet();
                fsTemp.setDir(new File(fRealPackageFolder, saXASPackages[iCount]));
                dDelete.addFileset(fsTemp);
            }
            dDelete.execute();

            //Now add the classmap file
            dDelete = new Delete();
            dDelete.setProject(cmtTask.getProject());
            dDelete.setTaskName(cmtTask.getTaskName());
            dDelete.setIncludeEmptyDirs(true);
            dDelete.setFailOnError(false);

            for (int iCount = 0; iCount < saXASPackages.length; iCount++)
            {
                FileSet fsTemp = new FileSet();
                fsTemp.setDir(fRealClassmapFolder);
                fsTemp.setIncludes(saXASPackages[iCount] + ".cmx");
                dDelete.addFileset(fsTemp);
            }

            //Execute the delete-task.
            cmtTask.log("[" + cContent.getType() + "] Executing the delete");
            dDelete.execute();
        }
        else
        {
            cmtTask.log("[" + cContent.getType() +
                        "] No XAS Packages found to delete.", Project.MSG_INFO);
        }
    }

    /**
     * This method copies the XAS content from the Cordys server to the
     * project. It needs  the following properties specified: - cordys.home:
     * The actual home directory of Cordys of the server you want to export
     * from. - xas.package: The name of the xas-package that you want to
     * export. The xas.package variable can contain multiple packes, as long
     * as they are seperated by ';'
     *
     * @param cmtTask DOCUMENTME
     * @param cContent DOCUMENTME
     * @param srmSoap DOCUMENTME
     */
    public void executeEcxToFile(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        Project pProject = cmtTask.getProject();
        Hashtable<?, ?> hProperties = pProject.getProperties();

        if (!hProperties.containsKey(PROP_CORDYS_HOME) ||
                !hProperties.containsKey(PROP_XAS_PACKAGE))
        {
            throw new BuildException("Missing either " + PROP_CORDYS_HOME +
                                     " or " + PROP_XAS_PACKAGE +
                                     " property in the project.");
        }

        //Get the Cordys home folder
        String sCordysHome = GeneralUtils.getTrimmedProperty(pProject, PROP_CORDYS_HOME);
        File fCordysHome = new File(sCordysHome);

        if (!fCordysHome.exists() || !fCordysHome.isDirectory())
        {
            throw new BuildException("cordys.home (" + sCordysHome +
                                     ") is not a valid folder.");
        }

        //Get the XAS packages to export.
        String sXASPackage = GeneralUtils.getTrimmedProperty(pProject, PROP_XAS_PACKAGE);
        String[] saXASPackages = parsePackages(sXASPackage);

        if ((saXASPackages != null) && (saXASPackages.length > 0))
        {
            //Create the folders where the content needs to be written.
            cmtTask.log("[" + cContent.getType() + "] Creating local folders",
                        Project.MSG_INFO);

            File fBaseDir = cContent.getDir();
            File fRepository = new File(fBaseDir, "repository");
            File fRuntime = new File(fBaseDir, "runtime");

            if (!fRepository.exists())
            {
                fRepository.mkdir();
            }

            if (!fRuntime.exists())
            {
                fRuntime.mkdir();
            }

            //Check the BSF repository.
            File fRealPackageXASFolder = new File(fCordysHome,
                                                  "bsf/repository/packages");
            File fRealClassmapXASFolder = new File(fCordysHome,
                                                   "bsf/runtime/classmap");

            if (!checkIfBsfFoldersExist(cmtTask, cContent,
                                            fRealPackageXASFolder,
                                            fRealClassmapXASFolder))
            {
                return;
            }

            //Copy the files from the ECX to the project
            Copy cCopy = new Copy();
            cCopy.setProject(cmtTask.getProject());
            cCopy.setTodir(fRepository);
            cCopy.setTaskName(cmtTask.getTaskName());

            for (int iCount = 0; iCount < saXASPackages.length; iCount++)
            {
                cmtTask.log("[" + cContent.getType() +
                            "] Getting content for package " +
                            saXASPackages[iCount], Project.MSG_INFO);

                FileSet fsFileSet = new FileSet();
                fsFileSet.setDir(fRealPackageXASFolder);

                FilenameSelector fsSelector = new FilenameSelector();
                fsSelector.setName(saXASPackages[iCount] + "/**/*.b?x");
                fsFileSet.addFilename(fsSelector);
                cCopy.addFileset(fsFileSet);
            }
            cmtTask.log("[" + cContent.getType() +
                        "] About to copy repository files.", Project.MSG_INFO);
            cCopy.execute();

            //Now copy the runtime classmap
            cCopy = new Copy();
            cCopy.setProject(cmtTask.getProject());
            cCopy.setTodir(fRuntime);
            cCopy.setTaskName(cmtTask.getTaskName());

            for (int iCount = 0; iCount < saXASPackages.length; iCount++)
            {
                FileSet fsFileSet = new FileSet();
                fsFileSet.setDir(fRealClassmapXASFolder);

                FilenameSelector fsSelector = new FilenameSelector();
                fsSelector.setName(saXASPackages[iCount] + ".cmx");
                fsFileSet.addFilename(fsSelector);
                cCopy.addFileset(fsFileSet);
            }
            cmtTask.log("[" + cContent.getType() +
                        "] About to copy runtime files.", Project.MSG_INFO);
            cCopy.execute();
        }
        else
        {
            cmtTask.log("[" + cContent.getType() +
                        "] There are no XAS packages specified in the project's properties.",
                        Project.MSG_INFO);
        }
    }

    /**
     * This method copies the local files the the configured ECX. This
     * means that the local files are copied to the cordys.home folder. After
     * that the standard XAS UI can be used to publish the classes.
     *
     * @param cmtTask DOCUMENTME
     * @param cContent DOCUMENTME
     * @param srmSoap DOCUMENTME
     */
    public void executeFileToEcx(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        Project pProject = cmtTask.getProject();
        Hashtable<?, ?> hProperties = pProject.getProperties();

        if (!hProperties.containsKey(PROP_CORDYS_HOME))
        {
            throw new BuildException("Missing property: " + PROP_CORDYS_HOME);
        }

        //Get the Cordys home folder
        String sCordysHome = GeneralUtils.getTrimmedProperty(pProject, PROP_CORDYS_HOME);
        File fCordysHome = new File(sCordysHome);

        if (!fCordysHome.exists() || !fCordysHome.isDirectory())
        {
            throw new BuildException("cordys.home (" + sCordysHome +
                                     ") is not a valid folder.");
        }

        File fBaseDir = cContent.getDir();
        File fRepository = new File(fBaseDir, "repository");
        File fRuntime = new File(fBaseDir, "runtime");

        if (!fRepository.exists())
        {
            fRepository.mkdir();
        }

        if (!fRuntime.exists())
        {
            fRuntime.mkdir();
        }

        //Check the BSF repository.
        File fRealPackageXASFolder = new File(fCordysHome,
                                              "bsf/repository/packages");
        File fRealClassmapXASFolder = new File(fCordysHome,
                                               "bsf/runtime/classmap");

        if (!checkIfBsfFoldersExist(cmtTask, cContent, fRealPackageXASFolder,
                                        fRealClassmapXASFolder))
        {
            return;
        }

        //Copy the files from the ECX to the project
        Copy cCopy = new Copy();
        cCopy.setProject(cmtTask.getProject());
        cCopy.setTodir(fRealPackageXASFolder);
        cCopy.setTaskName(cmtTask.getTaskName());
        cCopy.setOverwrite(true);

        FileSet fsFileSet = new FileSet();
        fsFileSet.setDir(fRepository);
        fsFileSet.setIncludes("**/*.b?x");
        cCopy.addFileset(fsFileSet);

        cmtTask.log("[" + cContent.getType() +
                    "] About to copy repository files.", Project.MSG_INFO);
        cCopy.execute();

        //Now copy the runtime classmap
        cCopy = new Copy();
        cCopy.setProject(cmtTask.getProject());
        cCopy.setTodir(fRealClassmapXASFolder);
        cCopy.setTaskName(cmtTask.getTaskName());
        cCopy.setOverwrite(true);

        fsFileSet = new FileSet();
        fsFileSet.setDir(fRuntime);
        fsFileSet.setIncludes("**/*.cmx");
        cCopy.addFileset(fsFileSet);

        cmtTask.log("[" + cContent.getType() +
                    "] About to copy runtime files.", Project.MSG_INFO);
        cCopy.execute();
    }

    /**
     * This method should take care of publishing the specific content
     * to runtime. This content does not need any publishing.
     *
     * @param cmtTask The current contentmanager task.
     * @param cContent The specific content that needs to be published.
     * @param srmSoap The object to use for sending soap messages.
     */
    public void executePublishToRuntime(ContentManagerTask cmtTask,
                                        Content cContent,
                                        ISoapRequestManager srmSoap)
    {
        //This target does not need any publishing.
        cmtTask.log("[" + cContent.getType() + "] Content of type " +
                    cContent.getType() +
                    " does not need any publishing to runtime.",
                    Project.MSG_INFO);
    }

    /**
     * 
     * @see com.cordys.tools.ant.cm.ContentHandler#getNumberOfProcessedItems()
     */
    public int getNumberOfProcessedItems()
    {
        return iProcessedCounter;
    }

    /**
     * Checks if BSF folders are present on the ECX server,
     *
     * @param cmtTask Ant task.
     * @param cContent Current content.
     * @param fRealPackageFolder ECX BSF package folder.
     * @param fRealClassmapFolder ECX BSF classmap folder.
     *
     * @return <code>true</code> if the given folders are found.
     */
    private boolean checkIfBsfFoldersExist(ContentManagerTask cmtTask,
                                           Content cContent,
                                           File fRealPackageFolder,
                                           File fRealClassmapFolder)
    {
        if (!fRealPackageFolder.exists() || !fRealPackageFolder.isDirectory())
        {
            cmtTask.log("[" + cContent.getType() +
                        "] BSF repository not found: " +
                        fRealPackageFolder.getAbsolutePath(), Project.MSG_INFO);
            cmtTask.log("[" + cContent.getType() + "] Skipping BSF content.",
                        Project.MSG_INFO);
            return false;
        }

        if (!fRealClassmapFolder.exists() ||
                !fRealClassmapFolder.isDirectory())
        {
            cmtTask.log("[" + cContent.getType() +
                        "] BSF classmap not found: " +
                        fRealPackageFolder.getAbsolutePath(), Project.MSG_INFO);
            cmtTask.log("[" + cContent.getType() + "] Skipping BSF content.",
                        Project.MSG_INFO);
            return false;
        }

        return true;
    }

    /**
     * This method parses the packages as they are in the sXASPackages.
     * They are seperated by semicolons.
     *
     * @param sXASPackages All the packages.
     *
     * @return A string array containing the seperate packages.
     */
    private String[] parsePackages(String sXASPackages)
    {
        String[] saReturn = null;
        StringTokenizer stPackages = new StringTokenizer(sXASPackages, ";");
        ArrayList<Object> alPackages = Collections.list(stPackages);
        saReturn = alPackages.toArray(new String[alPackages.size()]);

        return saReturn;
    }
    
    /**
     * @see com.cordys.tools.ant.cm.ContentHandler#getSingleContentName(java.io.File, ContentManagerTask, Content, boolean)
     */
    public String getSingleContentName(File contentFile, ContentManagerTask cmtTask, Content content, boolean toEcx) throws IOException
    {
        // The old XAS handler does not support this.
        return null;
    }    
}
