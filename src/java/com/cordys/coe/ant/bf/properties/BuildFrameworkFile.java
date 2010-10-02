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
 * This enum describes all the files which reside within the sdk folder and their proper paths.
 * 
 * @author pgussow
 */
public enum BuildFrameworkFile
{
    PROJECT_PROPERTIES("project.properties", "Holds the project specific properties."),
    USER_PROPERTIES("user.properties", "Holds the project specific properties."),
    SDK_FOLDER("sdk", "The base SDK folder"),
    SDK_LIB_FOLDER(SDK_FOLDER.getLocation() + "/lib", "The sdk/lib folder containing all buildframework related libraries."),
    SDK_LIB_ANT_FOLDER(SDK_LIB_FOLDER.getLocation() + "/ant", "The location of Ant."),
    SDK_LIB_COBERTURA_FOLDER(SDK_LIB_FOLDER.getLocation() + "/cobertura", "The location of Cobertura jars."),
    SDK_LIB_COMMONS_FOLDER(SDK_LIB_FOLDER.getLocation() + "/commons", "The location of Apache commons jars."),
    SDK_LIB_LIBS_COBOC2_FOLDER(SDK_LIB_FOLDER.getLocation() + "/libs-coboc2", "The location of new Coboc jars."),
    SDK_LIB_SVN_FOLDER(SDK_LIB_FOLDER.getLocation() + "/svn", "The location of SubVersion jars."),
    SDK_LIB_BUILDTASKS_JAR(SDK_LIB_FOLDER.getLocation() + "/buildtasks.jar", "The jar file containing all the custom buildtasks"),
    SDK_LIB_COELIB_JAR(SDK_LIB_FOLDER.getLocation() + "/coelib.jar", "The CoE library"),
    SDK_LIB_BUILDTASKS_COBOC2_JAR(SDK_LIB_FOLDER.getLocation() + "/buildtasks-coboc2.jar", "The jar file containing all the buildtasks for the CoBOC2 handler"),
    SDK_LIB_FREEMARKER_JAR(SDK_LIB_FOLDER.getLocation() + "/freemarker.jar", "The jar file containing all the FreeMarker binaries"),
    SDK_LIB_JUNIT_JAR(SDK_LIB_FOLDER.getLocation() + "/junit.jar", "The jar file containing the JUnit binaries."),
    SDK_BUILD_FOLDER(SDK_FOLDER.getLocation() + "/build", "This folder contains all standard build files."),
    SDK_BUILD_NEW_FOLDER(SDK_BUILD_FOLDER.getLocation() + "/new", "This folder contains all files for creating a new project."),
    SDK_BUILD_UPGRADE_FOLDER(SDK_BUILD_FOLDER.getLocation() + "/upgrade", "This folder contains all files for upgrading the current project."),
    SDK_BUILD_FRAMEWORK_PROPERTIES(SDK_BUILD_FOLDER.getLocation() + "/framework.properties", "This file contains the framework.proeprties."),
    SDK_BUILD_DELETECORDYS_XML(SDK_BUILD_FOLDER.getLocation() + "/build-deletecordys.xml", "This file contains all the delete-cordys content tasks."),
    SDK_BUILD_FRAMEWORK_XML(SDK_BUILD_FOLDER.getLocation() + "/build-deletecordys.xml", "This file contains all the delete-cordys content tasks."),
    SDK_BUILD_NEW_PROJECT_XML(SDK_BUILD_NEW_FOLDER.getLocation() + "/build-new-project.xml", "This file contains the Ant file to create a new project."),
    SDK_BUILD_UPGRADE_PROJECT_XML(SDK_BUILD_UPGRADE_FOLDER.getLocation() + "/build-upgrade.xml", "This file contains the Ant file to upgrade the current project."),
    SDK_CONFIG_FOLDER(SDK_FOLDER.getLocation() + "/config", "The sdk/config folder containing buildframework related configuration files."),
    SDK_CONFIG_BUILD_PROPERTIES_XML(SDK_CONFIG_FOLDER.getLocation() + "/build.properties.xml", "The file that contains how all properties need to be shown."),
    SDK_CONFIG_LABELS_PROPERTIES(SDK_CONFIG_FOLDER.getLocation() + "/Labels.properties", "The file that contains the labels for the proeprty pages."),
    PLATFORM_FOLDER("platform", "Holds all the platform binaries."),
    PLATFORM_INT_FOLDER(PLATFORM_FOLDER.getLocation() + "/int", "Holds all the Integrator binaries."),
    PLATFORM_ORC_FOLDER(PLATFORM_FOLDER.getLocation() + "/orc", "Holds all the Orchestrator binaries."),
    PLATFORM_EXT_FOLDER(PLATFORM_FOLDER.getLocation() + "/ext", "Holds all the external binaries."),
    PLATFORM_POR_FOLDER(PLATFORM_FOLDER.getLocation() + "/por", "Holds all the Portal binaries."),
    PLATFORM_STU_FOLDER(PLATFORM_FOLDER.getLocation() + "/stu", "Holds all the Studio binaries."),
    PLATFORM_BIN_FOLDER(PLATFORM_FOLDER.getLocation() + "/bin", "Holds all the DLL binaries.");
    /**
     * Holds the location of the file relative to the SDK folder.
     */
    private String m_sLocation;
    /**
     * Holds the description of the file.
     */
    private String m_sDescription;
    
    /**
     * Constructor. Creates the enum with the given properties.
     * 
     * @param sName The name of the file.
     * @param sLocation The location of the file relative to the SDK folder.
     * @param sDescription The description of the file.
     */
    private BuildFrameworkFile(String sLocation, String sDescription)
    {
        m_sLocation = sLocation;
        m_sDescription = sDescription;
    }
    
    /**
     * This method gets the location of the file relative to the SDK folder.
     * 
     * @return The location of the file relative to the SDK folder.
     */
    public String getLocation()
    {
        return m_sLocation;
    }
    
    /**
     * This method gets the description of the file.
     * 
     * @return The description of the file.
     */
    public String getDescription()
    {
        return m_sDescription;
    }

}
