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
 package com.cordys.coe.ant.bf.properties;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This class wraps around the project properties. There are 3 levels for properties: user, project
 * and framework. In the pre-1.5 they were all located in the project root. With 1.5 the
 * framework.properties moved to the sdk/build folder. For people using this class that is
 * transparent.<br>
 * This class can also be used to update, remove or rename properties in the files retaining the
 * layout (and thus the comments in the files).
 *
 * @author  pgussow
 */
public class ProjectProperties
{
    /**
     * Holds the name of the framework.properties file.
     */
    private static final String FRAMEWORK_PROPERTIES = "framework.properties";
    /**
     * Indicates whether or not the project is before 1.5.
     */
    private boolean m_bPre15 = false;
    /**
     * Indicates whether or not the project version >= 2.0 (or 1.9).
     */
    private boolean m_bVersion20 = false;
    /**
     * Holds the base folder of the project.
     */
    private File m_fProjectFolder;
    /**
     * Indicates whether or not a file is dirty.
     */
    private HashMap<PropertyLevel, Boolean> m_hmDirty = new HashMap<PropertyLevel, Boolean>();
    /**
     * Holds all the property files.
     */
    private LinkedHashMap<PropertyLevel, PropertiesConfiguration> m_hmProps = new LinkedHashMap<PropertyLevel, PropertiesConfiguration>();

    /**
     * Constructor. Creates the object and loads the property files.
     *
     * @param   sProjectFolder  The base folder of the project.
     *
     * @throws  ConfigurationException  If the property loading failed.
     */
    public ProjectProperties(String sProjectFolder)
                      throws ConfigurationException
    {
        m_fProjectFolder = new File(sProjectFolder);

        if (!m_fProjectFolder.exists())
        {
            throw new ConfigurationException("Folder " + sProjectFolder + " does not exist.");
        }

        loadFiles();
    }

    /**
     * This method will reload the property files from the disk.
     *
     * @throws  ConfigurationException  In case of any exceptions during loading.
     */
    public void reload()
                throws ConfigurationException
    {
        loadFiles();
    }

    /**
     * This method removes a value from the properties.
     *
     * @param   sKey     The key to remove.
     * @param   plLevel  The level on which to remove it.
     *
     * @return  The removed value.
     */
    public synchronized String remove(String sKey, PropertyLevel plLevel)
    {
        String sReturn = null;

        if (plLevel == PropertyLevel.FRAMEWORK)
        {
            throw new RuntimeException("Cannot change the framework level properties.");
        }

        PropertiesConfiguration pcTemp = m_hmProps.get(plLevel);

        if (pcTemp.containsKey(sKey))
        {
            sReturn = pcTemp.getString(sKey);
            pcTemp.clearProperty(sKey);
        }

        return sReturn;
    }

    /**
     * This method renames the property of name sOldName to sNewName. It also tries to retain the
     * comments.
     *
     * @param  sOldName  The old name of the property.
     * @param  sNewName  The new name of the property.
     * @param  plLevel   The level on which to do it.
     */
    public void renameProperty(String sOldName, String sNewName, PropertyLevel plLevel)
    {
        // First check if the property is defined on the given level.
        PropertiesConfiguration pcTemp = m_hmProps.get(plLevel);

        if (pcTemp.containsKey(sOldName))
        {
            // The old name is in this file, so we need to rename. First see it there is a comment.
            String sValue = pcTemp.getString(sOldName);
            String sComment = pcTemp.getLayout().getComment(sOldName);
            int iBlankLinesBefore = pcTemp.getLayout().getBlancLinesBefore(sOldName);

            // Remove the property.
            pcTemp.clearProperty(sOldName);

            // Add it again.
            pcTemp.addProperty(sNewName, sValue);
            pcTemp.getLayout().setComment(sNewName, sComment);
            pcTemp.getLayout().setBlancLinesBefore(sNewName, iBlankLinesBefore);

            m_hmDirty.put(plLevel, true);
        }
    }

    /**
     * This method will save all changes to the property files.
     *
     * @throws  ConfigurationException  In case of any exceptions.
     */
    public synchronized void saveConfiguration()
                                        throws ConfigurationException
    {
        if (m_hmDirty.get(PropertyLevel.USER) == true)
        {
            PropertiesConfiguration pcTemp = m_hmProps.get(PropertyLevel.USER);
            pcTemp.save();
            m_hmDirty.put(PropertyLevel.USER, false);
        }

        if (m_hmDirty.get(PropertyLevel.PROJECT) == true)
        {
            PropertiesConfiguration pcTemp = m_hmProps.get(PropertyLevel.PROJECT);
            pcTemp.save();
            m_hmDirty.put(PropertyLevel.PROJECT, false);
        }
    }

    /**
     * This method returns a list of preoprties which are not defined by the framework, but defined
     * by the current project per property level. This method will also include the
     * framework.properties.This can be used for debugging purposes.
     *
     * @return  The list of custom defined properties per property level.
     */
    public HashMap<PropertyLevel, List<String>> getAllNonStandardProperties()
    {
        return internalGetNonStandardProperties(PropertyLevel.values());
    }

    /**
     * This method returns a list of preoprties which are not defined by the framework, but defined
     * by the current project per property level.
     *
     * @return  The list of custom defined properties per property level.
     */
    public HashMap<PropertyLevel, List<String>> getNonStandardProperties()
    {
        PropertyLevel[] aplTemp = new PropertyLevel[] { PropertyLevel.USER, PropertyLevel.PROJECT };

        return internalGetNonStandardProperties(aplTemp);
    }

    /**
     * This method returns the value for the given property. If the property was not found null is
     * returned.
     *
     * @param   sName  The name of the property to return.
     *
     * @return  The value for the property. null if the property was not found.
     */
    public String getProperty(String sName)
    {
        return getProperty(sName, null);
    }

    /**
     * This method returns the value for the given property. If the property was not found the
     * specified default value is returned.
     *
     * @param   sName          The name of the property to get.
     * @param   sDefaultValue  The default value in case the value was not found.
     *
     * @return  The value of the property. If the property was not found the specified default value
     *          is returned.
     */
    public String getProperty(String sName, String sDefaultValue)
    {
        // We'll itterate of the different levels of proeprtyfiles to return the proepr value.
        String sReturn = null;

        PropertyLevel[] aplLevels = PropertyLevel.values();

        for (int iCount = 0; iCount < aplLevels.length; iCount++)
        {
            PropertyLevel plLevel = aplLevels[iCount];

            PropertiesConfiguration pcTemp = m_hmProps.get(plLevel);
            sReturn = (String) pcTemp.getString(sName);

            if (sReturn != null)
            {
                break;
            }
        }

        if ((sReturn == null) && (sDefaultValue != null))
        {
            sReturn = sDefaultValue;
        }

        return sReturn;
    }

    /**
     * This method returns the value of the property on a specific level.
     *
     * @param   sName          The name of the property.
     * @param   sDefaultValue  The default value.
     * @param   plLevel        The level on which the property should be (USER, PROJECT or
     *                         FRAMEWORK).
     *
     * @return  The value of the property. If the property is not found and the default value is not
     *          equal to null the default value is returned.
     */
    public String getProperty(String sName, String sDefaultValue, PropertyLevel plLevel)
    {
        String sReturn = m_hmProps.get(plLevel).getString(sName);

        if ((sReturn == null) && (sDefaultValue != null))
        {
            sReturn = sDefaultValue;
        }

        return sReturn;
    }

    /**
     * Returns the version20.
     *
     * @return  Returns the version20.
     */
    public boolean isVersion20()
    {
        return m_bVersion20;
    }

    /**
     * This method gets whether or not the project is pre-1.5.
     *
     * @return  Whether or not the project is pre-1.5.
     */
    public boolean isPre15()
    {
        return m_bPre15;
    }

    /**
     * This method sets the value for a certain property. The value will be set at USER level.
     *
     * @param   sName   The name of the property.
     * @param   sValue  The value of the property.
     *
     * @return  The previous value of the specified key in this property list, or null if it did not
     *          have one.
     */
    public synchronized String setProperty(String sName, String sValue)
    {
        return setProperty(sName, sValue, PropertyLevel.USER);
    }

    /**
     * This method sets the value for a certain property. If this class encapsulates all
     * propertyfiles the setProperty is executed on the the user.properties.
     *
     * @param   sName    The name of the property
     * @param   sValue   The value of the property.
     * @param   plLevel  The level on which the property should be set.
     *
     * @return  The previous value of the specified key in this property list, or null if it did not
     *          have one.
     */
    public synchronized String setProperty(String sName, String sValue, PropertyLevel plLevel)
    {
        String sReturn = null;

        if (plLevel == PropertyLevel.FRAMEWORK)
        {
            throw new RuntimeException("Cannot change the framework level properties.");
        }

        PropertiesConfiguration pcTemp = m_hmProps.get(plLevel);

        if (pcTemp.containsKey(sName))
        {
            sReturn = pcTemp.getString(sName);
            pcTemp.setProperty(sName, sValue);
        }
        else
        {
            pcTemp.addProperty(sName, sValue);
        }

        m_hmDirty.put(plLevel, true);

        return sReturn;
    }

    /**
     * This method returns a list of properties which are not defined by the framework, but defined
     * by the current project per property level.
     *
     * @param   aplProperties  Property levels.
     *
     * @return  The list of custom defined properties per property level.
     */
    private HashMap<PropertyLevel, List<String>> internalGetNonStandardProperties(PropertyLevel[] aplProperties)
    {
        HashMap<PropertyLevel, List<String>> hmReturn = new LinkedHashMap<PropertyLevel, List<String>>();

        for (int iCount = 0; iCount < aplProperties.length; iCount++)
        {
            PropertyLevel plTemp = aplProperties[iCount];

            ArrayList<String> alTemp = new ArrayList<String>();

            PropertiesConfiguration pcTemp = m_hmProps.get(plTemp);

            for (Iterator<?> iTemp = pcTemp.getKeys(); iTemp.hasNext();)
            {
                String sKey = (String) iTemp.next();

                if (!BuildFrameworkProperty.isProperty(sKey))
                {
                    alTemp.add(sKey);
                }
            }

            // Sort it and add it.
            if (alTemp.size() == 0)
            {
                alTemp = null;
            }
            else
            {
                // Sort the list.
                Collections.sort(alTemp);
            }

            hmReturn.put(plTemp, alTemp);
        }

        return hmReturn;
    }

    /**
     * This method loads the property files.
     *
     * @throws  ConfigurationException  In case of any exceptions.
     */
    private void loadFiles()
                    throws ConfigurationException
    {
        // Check if this is version 2.0 project.
        m_bVersion20 = new File(m_fProjectFolder,
                                BuildFrameworkFile.SDK_BUILD_FOLDER.getLocation() +
                                "/c3/framework.properties").exists();

        // Load the framework.properties.
        File fFramework = new File(m_fProjectFolder, FRAMEWORK_PROPERTIES);

        if (fFramework.exists())
        {
            m_bPre15 = true;
        }
        else
        {
            fFramework = new File(m_fProjectFolder,
                                  BuildFrameworkFile.SDK_BUILD_FRAMEWORK_PROPERTIES.getLocation());
        }

        if (!fFramework.exists())
        {
            throw new ConfigurationException("framework.properties not found at " +
                                             fFramework.getAbsolutePath());
        }

        PropertiesConfiguration pcNew = ((!m_bVersion20)
                                         ? new PropertiesConfiguration(fFramework)
                                         : loadVersion20FrameworkProperties(fFramework));
        m_hmProps.put(PropertyLevel.FRAMEWORK, pcNew);

        // Load the project.properties
        File fProject = new File(m_fProjectFolder,
                                 BuildFrameworkFile.PROJECT_PROPERTIES.getLocation());

        if (!fProject.exists())
        {
            throw new ConfigurationException("project.properties not found at " +
                                             fProject.getAbsolutePath());
        }
        pcNew = new PropertiesConfiguration(fProject);
        m_hmProps.put(PropertyLevel.PROJECT, pcNew);

        // Load the user.properties if present.
        File fUser = new File(m_fProjectFolder, BuildFrameworkFile.USER_PROPERTIES.getLocation());
        pcNew = new PropertiesConfiguration(fUser);
        m_hmProps.put(PropertyLevel.USER, pcNew);

        m_hmDirty.put(PropertyLevel.USER, false);
        m_hmDirty.put(PropertyLevel.PROJECT, false);
    }

    /**
     * Loads version 2.0 properties.
     *
     * @param   frameworkFile  sdk/build/framework.properties file.
     *
     * @return  Loaded properties.
     *
     * @throws  ConfigurationException  If the properties could not be loaded.
     */
    private PropertiesConfiguration loadVersion20FrameworkProperties(File frameworkFile)
                                                              throws ConfigurationException
    {
        PropertiesConfiguration pcNew = new PropertiesConfiguration();
        File buildFolder = frameworkFile.getParentFile();
        File file;
        
        pcNew.addProperty("project.root.dir", ".");

        file = new File(buildFolder, "c3/folders.properties");
        pcNew.load(file);
        file = new File(buildFolder, "c3/framework.properties");
        pcNew.load(file);
        file = new File(buildFolder, "c3/legacy.properties");
        pcNew.load(file);
        file = new File(buildFolder, "folders.properties");
        pcNew.load(file);
        pcNew.load(frameworkFile);

        return pcNew;
    }

    /**
     * Enum that holds the different levels.
     */
    public enum PropertyLevel
    {
        USER,
        PROJECT,
        FRAMEWORK
    }
}
