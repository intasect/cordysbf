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

/**
 * This enum identifies all the preoprties that can be used for the new project ant script.
 * 
 * @author pgussow
 */
public enum NewProjectProperty
{
    CREATE_SVN("create.svn", "Whether or not the project should be shared to subversion."),
    CREATE_SVN_EXTERNALS("create.svn.externals", "Whether or not the externals should be configured."),
    PROJECT_ROOT("project.root", "The root location of the project."),
    SOURCE_PROPERTY_FILE("source.property.file", "Holds the up. and pp. properties that should be set in the new project."),
    SRC_PLATFORM_FOLDER("src.platform.folder", "The location of the platform folder."),
    SRC_SDK_FOLDER("src.sdk.folder", "The location of the SDK folder."),
    SVN_EXTERNALS_PLATFORM("svn.externals.platform", "The SVN url where to find the externals for the platform."),
    SVN_EXTERNALS_SDK("svn.externals.sdk", "The SVN url where to find the externals for the sdk.");
    /**
     * Holds the name of the property in the properties file.
     */
    private String m_sName;
    /**
     * Holds teh short description of the property.
     */
    private String m_sShort;
    /**
     * Holds the long description of the property.
     */
    private String m_sLong;

    /**
     * Constructor. Creates the enum with the given properties.
     * 
     * @param sName The name of the property.
     * @param sShort The short description.
     * @param sLong The long description.
     */
    private NewProjectProperty(String sName, String sShort, String sLong)
    {
        m_sName = sName;
        m_sShort = sShort;
        m_sLong = sLong;
    }
    
    /**
     * Constructor. Creates the enum with the given properties.
     * 
     * @param sName The name of the property.
     * @param sShort The short description.
     * @param sLong The long description.
     */
    private NewProjectProperty(String sName, String sShort)
    {
        this(sName, sShort, sShort);
    }

    /**
     * This method gets the name of the property.
     *
     * @return The name of the property.
     */
    public String getName()
    {
        return m_sName;
    }

    /**
     * This method gets the short description.
     *
     * @return The short description.
     */
    public String getShortDescription()
    {
        return m_sShort;
    }

    /**
     * This method gets the long description.
     *
     * @return The long description.
     */
    public String getLongDescription()
    {
        return m_sLong;
    }
}
