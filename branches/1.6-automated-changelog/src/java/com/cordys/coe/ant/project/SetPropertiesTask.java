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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * This task will set the properties of the current project to the ones
 * that are provided. The task expects the sourcePropertyFile attribute to be
 * filled. It will read all the properties that are defined there and expects
 * each property there to be prefixed with either 'pp' for project.properties
 * and 'up' to be user properties. Based on all the properties there the
 * properties will be properly set.
 *
 * @author pgussow
 */
public class SetPropertiesTask extends Task
{
    /**
     * Holds the name of the project.properties file.
     */
    private static final String FILE_PROJECT_PROPERTIES = "project.properties";
    /**
     * Holds the name of the user.properties file.
     */
    private static final String FILE_USER_PROPERTIES = "user.properties";
    /**
     * Holds the project.properties file.
     */
    private File m_fProjectProperties;
    /**
     * Holds the user.properties file.
     */
    private File m_fUserProperties;
    /**
     * Holds the base project folder.
     */
    private File projectFolder;
    /**
     * Holds the file which contains the properties that should be set.
     */
    private File sourcePropertyFile;

    /**
     * This method executes the task.
     *
     * @throws BuildException
     *
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute()
                 throws BuildException
    {
        //validate the configuration of the task.
        validateAttributes();

        //Load the file which contains the properties that need to be set.
        Properties pSourceProperties = new Properties();

        try
        {
            pSourceProperties.load(new FileInputStream(getSourcePropertyFile()));
        }
        catch (Exception e)
        {
            throw new BuildException("Error loading base property file", e);
        }

        //Update the project.properties.
        log("Going to update the project.properties", Project.MSG_INFO);
        try
        {
            PropertiesConfiguration pcProjectProperties = new PropertiesConfiguration(m_fProjectProperties);
            updateProperties(pcProjectProperties, "pp.", pSourceProperties);
            pcProjectProperties.save();
        }
        catch (ConfigurationException e)
        {
            throw new BuildException("Error updating the project.properties file.");
        }

        //Update the user.properties.
        log("Going to update the user.properties", Project.MSG_INFO);
        try
        {
            PropertiesConfiguration pcUserProperties = new PropertiesConfiguration(m_fUserProperties);
            updateProperties(pcUserProperties, "up.", pSourceProperties);
            pcUserProperties.save();
        }
        catch (ConfigurationException e)
        {
            throw new BuildException("Error updating the user.properties file.");
        }
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
     * This method gets the file containing the properties.
     *
     * @return The file containing the properties.
     */
    public File getSourcePropertyFile()
    {
        return sourcePropertyFile;
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
     * This method sets the file containing the properties.
     *
     * @param sourcePropertyFile The file containing the properties.
     */
    public void setSourcePropertyFile(File sourcePropertyFile)
    {
        this.sourcePropertyFile = sourcePropertyFile;
    }

    /**
     * This method updates the given property file with the values from
     * the source properties. The layout and order should be maintained.
     *
     * @param pcProjectProperties The project properties.
     * @param sPrefix The prefix for the properties that should go into the
     *        given property file.
     * @param pSourceProperties The source properties.
     */
    private void updateProperties(PropertiesConfiguration pcProjectProperties,
                                  String sPrefix, Properties pSourceProperties)
    {
        for (Iterator<?> iKeys = pSourceProperties.keySet().iterator();
                 iKeys.hasNext();)
        {
            String sPropertyName = (String) iKeys.next();

            if (sPropertyName.startsWith(sPrefix))
            {
                //It should be in this property file
                String sRealProperty = sPropertyName.substring(sPrefix.length());
                String sValue = pSourceProperties.getProperty(sPropertyName);
                
                log("Setting property " + sRealProperty + " to value " + sValue + " in file " + pcProjectProperties.getFileName(), Project.MSG_DEBUG);
                
                pcProjectProperties.setProperty(sRealProperty, sValue);
            }
        }
    }

    /**
     * This method validates the configuration of this task.
     */
    private void validateAttributes()
                             throws BuildException
    {
        //Check the sourcePropertyFile.
        if (sourcePropertyFile == null)
        {
            throw new BuildException("The sourcePropertyFile is not set.");
        }

        if (!getSourcePropertyFile().exists())
        {
            throw new BuildException("The sourcePropertyFile(" +
                                     getSourcePropertyFile().getAbsolutePath() +
                                     ") does not exist.");
        }

        //Check the project folder.
        if (getProjectFolder() == null)
        {
            throw new BuildException("The projectFolder is not set.");
        }

        if (!getProjectFolder().exists())
        {
            throw new BuildException("The projectFolder(" +
                                     getSourcePropertyFile().getAbsolutePath() +
                                     ") does not exist.");
        }

        //Check whether or not the project.properties and the user.properties are available.
        m_fProjectProperties = new File(getProjectFolder(),
                                        FILE_PROJECT_PROPERTIES);

        if (!m_fProjectProperties.exists())
        {
            try
            {
                throw new BuildException("The file project.properties(" +
                                         m_fProjectProperties.getCanonicalPath() +
                                         ") does not exist.");
            }
            catch (IOException e)
            {
                throw new BuildException("The file project.properties(" +
                                         m_fProjectProperties.getAbsolutePath() +
                                         ") does not exist.");
            }
        }

        m_fUserProperties = new File(getProjectFolder(), FILE_USER_PROPERTIES);

        if (!m_fUserProperties.exists())
        {
            try
            {
                throw new BuildException("The file user.properties(" +
                                         m_fUserProperties.getCanonicalPath() +
                                         ") does not exist.");
            }
            catch (IOException e)
            {
                throw new BuildException("The file user.properties(" +
                                         m_fUserProperties.getAbsolutePath() +
                                         ") does not exist.");
            }
        }
        
        //Configure Log4J if it's not configured yet
        
    }
}
