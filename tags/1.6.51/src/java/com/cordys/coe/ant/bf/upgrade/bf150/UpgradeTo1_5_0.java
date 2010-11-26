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
 package com.cordys.coe.ant.bf.upgrade.bf150;

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;
import com.cordys.coe.ant.bf.properties.ProjectProperties;
import com.cordys.coe.ant.bf.properties.ProjectProperties.PropertyLevel;
import com.cordys.coe.ant.bf.upgrade.BFUpgradeException;
import com.cordys.coe.ant.bf.upgrade.IUpgradeScript;
import com.cordys.coe.ant.bf.upgrade.UpgradeManagerTask;

import java.io.File;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;

/**
 * This class upgrades the project to the 1.5.0 level. The things that need
 * to be done:<br>
 * 1. Copy the build.xml to the project root (since the framework.properties
 * is included from a different location).<br>
 * 2. Delete the framework.properties from the project and IF the project is
 * under SVN also commit it to SubVersion<br>
 *
 * @author pgussow
 */
public class UpgradeTo1_5_0
    implements IUpgradeScript
{
    /**
     * Holds the current Ant project.
     */
    private Project m_pProject;

    /**
     * This method upgrades the project to the proper version.
     *
     * @param pProject The current Ant project.
     * @param umtTask The task that is currently being executed.
     *
     * @throws BFUpgradeException In case of any exceptions.
     *
     * @see com.cordys.coe.ant.bf.upgrade.IUpgradeScript#execute(org.apache.tools.ant.Project,
     *      com.cordys.coe.ant.bf.upgrade.UpgradeManagerTask)
     */
    public void execute(Project pProject, UpgradeManagerTask umtTask)
                 throws BFUpgradeException
    {
        m_pProject = pProject;
        copyBuildXML();
        copyBuildBAT();

        //Some properties were renamed. So they need to be renamed 
        //at the project and user level.
        HashMap<String, String> hmProperties = new HashMap<String, String>();
        hmProperties.put("debug", BuildFrameworkProperty.JAVAC_DEBUG.getName());
        hmProperties.put("deprecation",
                         BuildFrameworkProperty.JAVAC_DEPRECATION.getName());
        hmProperties.put("optimize",
                         BuildFrameworkProperty.JAVAC_OPTIMIZE.getName());
        hmProperties.put("jvm.target.version",
                         BuildFrameworkProperty.JAVAC_SOURCE_VERSION.getName());

        renameProperties(hmProperties);

        //Remove the framework.properties (it's now externalized or in the sdk/build).
        deleteFrameworkProperties();
    }

    /**
     * This method returns the version number for which this upgrade
     * script is created.
     *
     * @return The version number for this script.
     */
    public String getVersion()
    {
        return "1.5.0";
    }

    /**
     * This method copies the build.bat file from the base to the
     * project root. There is a new LIB in this bacthfile.
     */
    private void copyBuildBAT()
    {
        Copy cCopy = new Copy();
        cCopy.setTaskName("copy-build.bat");
        cCopy.setProject(m_pProject);
        cCopy.setFile(new File(m_pProject.getBaseDir(),
                               "./sdk/build/new/base/build.bat"));
        cCopy.setTodir(m_pProject.getBaseDir());
        cCopy.setOverwrite(true);
        cCopy.execute();
    }

    /**
     * This method copies the build.xml file from the base to the
     * project root.
     */
    private void copyBuildXML()
    {
        Copy cCopy = new Copy();
        cCopy.setTaskName("copy-build.xml");
        cCopy.setProject(m_pProject);
        cCopy.setFile(new File(m_pProject.getBaseDir(),
                               "./sdk/build/new/base/build.xml"));
        cCopy.setTodir(m_pProject.getBaseDir());
        cCopy.setOverwrite(true);
        cCopy.execute();
    }

    /**
     * This method copies the tocordys.cmd file from the base to the
     * project root.
     */
    private void deleteFrameworkProperties()
    {
        Delete dDelete = new Delete();
        dDelete.setTaskName("delete-framework.properties");
        dDelete.setProject(m_pProject);
        dDelete.setFile(new File(m_pProject.getBaseDir(), "framework.properties"));
        dDelete.execute();
    }

    /**
     * This method renames the properties with name 'key' to 'value'.
     *
     * @param hmProperties The properties to rename.
     */
    private void renameProperties(HashMap<String, String> hmProperties)
                           throws BFUpgradeException
    {
        try
        {
            ProjectProperties ppProps = new ProjectProperties(m_pProject.getBaseDir()
                                                                        .getAbsolutePath());

            PropertyLevel[] aplLevels = new ProjectProperties.PropertyLevel[]
                                        {
                                            PropertyLevel.USER,
                                            PropertyLevel.PROJECT
                                        };

            for (Iterator<String> iKeys = hmProperties.keySet().iterator();
                     iKeys.hasNext();)
            {
                String sOldName = (String) iKeys.next();
                String sNewName = hmProperties.get(sOldName);

                //We need to check both USER and PROJECT level.
                for (int iCount = 0; iCount < aplLevels.length; iCount++)
                {
                    ppProps.renameProperty(sOldName, sNewName, aplLevels[iCount]);
                }
            }

            ppProps.saveConfiguration();
        }
        catch (ConfigurationException e)
        {
            throw new BFUpgradeException("Error renaming the properties", e);
        }
    }
}
