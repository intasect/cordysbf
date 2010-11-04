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
package com.cordys.coe.ant.project;

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;
import com.cordys.coe.util.xml.dom.CoENiceDOMWriter;
import com.cordys.coe.util.xml.dom.XMLHelper;
import com.cordys.coe.util.xml.dom.XPathHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This task creates the .project and the .classpath files which can be
 * used within eclipse.
 *
 * @author pgussow
 */
public class CreateEclipseProjectFilesTask extends Task
{
    /**
     * Holds the name of the .project file.
     */
    private static final String FILE_PROJECT = ".project";
    /**
     * Holds the location of the project folder.
     */
    private File projectFolder;

    /**
     * This method executes the task. It will update the .project file
     * with the proper project name.
     *
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute()
                 throws BuildException
    {
        if ((getProjectFolder() == null) || !getProjectFolder().exists() ||
                !getProjectFolder().isDirectory())
        {
            throw new BuildException("Project folder is null, does not exist or is not a directory.");
        }

        String sPropName = BuildFrameworkProperty.PROJECT_NAME.getName();

        if ((getProject().getProperty(sPropName) == null) ||
                (getProject().getProperty(sPropName).length() == 0))
        {
            throw new BuildException("The project name property (" + sPropName +
                                     ") is not set for the project.");
        }

        //First create the project file
        updateProjectFile();
    }

    /**
     * This method gets the project folder location.
     *
     * @return The project folder location.
     */
    public File getProjectFolder()
    {
        return projectFolder;
    }

    /**
     * This method sets the project folder location.
     *
     * @param projectFolder The project folder location.
     */
    public void setProjectFolder(File projectFolder)
    {
        this.projectFolder = projectFolder;
    }

    /**
     * This method updates the default .project file with the values
     * from the project and user.properties.
     */
    private void updateProjectFile()
    {
        File fFile = new File(projectFolder, FILE_PROJECT);

        if (fFile.exists())
        {
            Document dDoc = null;

            try
            {
                dDoc = XMLHelper.createDocumentBuilder(false).parse(fFile);
            }
            catch (Exception e)
            {
                throw new BuildException("Error parsing the .project file", e);
            }

            try
            {
                Node nNode = XPathHelper.selectSingleNode(dDoc.getDocumentElement(),
                                                          ".//name");

                if (nNode != null)
                {
                    nNode.setNodeValue(getProject()
                                           .getProperty(BuildFrameworkProperty.PROJECT_NAME.getName()));
                }
            }
            catch (Exception e)
            {
                throw new BuildException("Error modifying the project name", e);
            }

            FileOutputStream fos = null;

            try
            {
                fos = new FileOutputStream(fFile, false);
                fos.write(new CoENiceDOMWriter(dDoc.getDocumentElement()).getBytes());
            }
            catch (IOException ioe)
            {
                throw new BuildException("Error writing new project file", ioe);
            }
            finally
            {
                try
                {
                    if (fos != null)
                    {
                        fos.flush();
                        fos.close();
                    }
                }
                catch (IOException e)
                {
                    log("Error closing file: " + e, Project.MSG_WARN);
                }
            }
        }
        else
        {
            log("File " + fFile.getAbsolutePath() + " does not exist.",
                Project.MSG_WARN);
        }
    }
}
