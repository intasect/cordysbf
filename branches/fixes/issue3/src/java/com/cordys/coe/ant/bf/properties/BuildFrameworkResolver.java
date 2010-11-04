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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;

/**
 * This class can be used to resolve properties for a certain project. It
 * will load the framework.properties, project.properties and the
 * user.properties of the given project.
 *
 * @author pgussow
 */
public class BuildFrameworkResolver
{
    /**
     * Holds the Ant project that can resolve the properties based on
     * the current project.
     */
    private Project m_pProject;

/**
     * Creates a new BuildFrameworkResolver object.
     *
     * @param fBase The base folder of the project.
     */
    public BuildFrameworkResolver(File fBase)
    {
        m_pProject = new Project();
        m_pProject.setBaseDir(fBase);
        m_pProject.init();

        //        DefaultLogger dl = new DefaultLogger();
        //        dl.setOutputPrintStream(System.out);
        //        dl.setErrorPrintStream(System.err);
        //        dl.setMessageOutputLevel(Project.MSG_DEBUG);
        //        m_pProject.addBuildListener(dl);

        //Load the user.properties.
        Property pProperty = new Property();
        pProperty.setProject(m_pProject);
        pProperty.setFile(new File(fBase,
                                   BuildFrameworkFile.USER_PROPERTIES.getLocation()));
        pProperty.execute();

        //Load the project.properties.
        pProperty = new Property();
        pProperty.setProject(m_pProject);
        pProperty.setFile(new File(fBase,
                                   BuildFrameworkFile.PROJECT_PROPERTIES.getLocation()));
        pProperty.execute();

        //Load the framework.properties.
        pProperty = new Property();
        pProperty.setProject(m_pProject);

        File fFrameworkProperties = new File(fBase,
                                             BuildFrameworkFile.SDK_BUILD_FRAMEWORK_PROPERTIES.getLocation());

        if (!fFrameworkProperties.exists())
        {
            //For backwards compatibility
            fFrameworkProperties = new File(fBase, "framework.properties");
        }
        pProperty.setFile(fFrameworkProperties);
        pProperty.execute();
    }

    /**
     * This method resolves the property for the given property.
     *
     * @param bfpProperty The property to resolve.
     *
     * @return The resolved file.
     */
    public File resolveFile(BuildFrameworkProperty bfpProperty)
    {
        File fReturn = null;

        String sResult = m_pProject.getProperty(bfpProperty.getName());
        fReturn = new File(m_pProject.getBaseDir(), sResult);

        return fReturn;
    }
}
