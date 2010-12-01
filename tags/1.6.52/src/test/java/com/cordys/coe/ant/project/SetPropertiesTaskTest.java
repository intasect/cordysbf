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

import junit.framework.TestCase;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Copy;

/**
 * Test class for the set properties task.
 *
 * @author pgussow
 */
public class SetPropertiesTaskTest extends TestCase
{
    /**
     * Holds the temp build folder.
     */
    private File m_fBuildTemp = new File("./build/test/tempprj");
    /**
     * Holds the project to use.
     */
    private Project m_pProject;

    /**
     * This test tests to see if the setting of the project properties
     * works.
     */
    public void testOnlyPPProjecties()
    {
        Target tMain = new Target();
        tMain.setName("testOnlyPPProjecties");
        tMain.setProject(m_pProject);
        m_pProject.addTarget(tMain);

        resetFiles(tMain);
        
        SetPropertiesTask spt = new SetPropertiesTask();
        spt.setTaskName("SetProperties");
        spt.setProjectFolder(m_fBuildTemp);
        spt.setSourcePropertyFile(new File("./src/test/java/com/cordys/coe/ant/project/only_pp.properties"));
        spt.setProject(m_pProject);
        
        tMain.addTask(spt);
        
        m_pProject.executeTarget("testOnlyPPProjecties");
    }

    /**
     * This method sets up the base project that will be used.
     *
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp()
                  throws Exception
    {
        m_pProject = new Project();

        DefaultLogger dl = new DefaultLogger();
        dl.setOutputPrintStream(System.out);
        dl.setErrorPrintStream(System.err);
        dl.setMessageOutputLevel(Project.MSG_DEBUG);
        m_pProject.addBuildListener(dl);
        m_pProject.init();

        if (!m_fBuildTemp.exists())
        {
            m_fBuildTemp.mkdirs();
        }
    }

    /**
     * This method copies the current user.properties and
     * project.properties to a temp test folder.
     *
     * @param tTarget The target to add the resetting to.
     */
    private void resetFiles(Target tTarget)
    {
        Copy cCopy = new Copy();
        cCopy.setProject(m_pProject);
        cCopy.setTodir(m_fBuildTemp);
        cCopy.setOverwrite(true);
        cCopy.setFile(new File("./src/buildfiles_external/build/new/base/project.properties"));

        cCopy.setProject(m_pProject);
        tTarget.addTask(cCopy);

        cCopy = new Copy();
        cCopy.setProject(m_pProject);
        cCopy.setTodir(m_fBuildTemp);
        cCopy.setOverwrite(true);
        cCopy.setFile(new File("./src/buildfiles_external/build/new/base/user.properties"));

        cCopy.setProject(m_pProject);
        tTarget.addTask(cCopy);
    }
}
