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

import com.cordys.tools.ant.taskdefs.VersionTypeTask;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

/**
 * Testcase for the version type task.
 *
 * @author pgussow
 */
public class VersionTypeTaskTest extends TestCase
{
    /**
     * The Ant project to use.
     */
    private Project m_pProject;

    /**
     * Testcase for the development version.
     */
    public void testDevelopment()
    {
        Target tTarget = new Target();
        tTarget.setName("testdevelopment");
        tTarget.setProject(m_pProject);

        VersionTypeTask vtt = new VersionTypeTask();
        vtt.setProject(m_pProject);
        vtt.setVersion("1.5.0");

        tTarget.addTask(vtt);

        tTarget.execute();

        assertEquals("This has to be a development version", true,
                     Boolean.parseBoolean(m_pProject.getProperty(vtt.getPropertyDevelopment())));
    }

    /**
     * Testcase for the development version.
     */
    public void testStable()
    {
        Target tTarget = new Target();
        tTarget.setName("teststable");
        tTarget.setProject(m_pProject);

        VersionTypeTask vtt = new VersionTypeTask();
        vtt.setProject(m_pProject);
        vtt.setVersion("1.6.0");

        tTarget.addTask(vtt);

        tTarget.execute();

        assertEquals("This has to be a development version", true,
                     Boolean.parseBoolean(m_pProject.getProperty(vtt.getPropertyStable())));
        
        tTarget = new Target();
        tTarget.setName("teststable");
        tTarget.setProject(m_pProject);

        vtt = new VersionTypeTask();
        vtt.setProject(m_pProject);
        vtt.setVersion("1.6");

        tTarget.addTask(vtt);

        tTarget.execute();

        assertEquals("This has to be a development version", true,
                     Boolean.parseBoolean(m_pProject.getProperty(vtt.getPropertyStable())));
    }
    
    /**
     * Testcase for invalid arguments.
     */
    public void testInvalid()
    {
        Target tTarget = new Target();
        tTarget.setName("invalid");
        tTarget.setProject(m_pProject);

        VersionTypeTask vtt = new VersionTypeTask();
        vtt.setProject(m_pProject);
        vtt.setVersion("10.");

        tTarget.addTask(vtt);

        try
        {
            tTarget.execute();
        }
        catch(BuildException be)
        {
            assertEquals("Expecting error about version format.", true,
                    (be.getMessage().indexOf("#.# or #.#.#") > -1));
        }
        
        vtt = new VersionTypeTask();
        vtt.setProject(m_pProject);
        vtt.setVersion("a.a");

        tTarget.addTask(vtt);

        try
        {
            tTarget.execute();
        }
        catch(BuildException be)
        {
            assertEquals("Expecting error about version format.", true,
                    (be.getMessage().indexOf("#.# or #.#.#") > -1));
        }
        
        tTarget = new Target();
        tTarget.setName("invalid");
        tTarget.setProject(m_pProject);

        vtt = new VersionTypeTask();
        vtt.setProject(m_pProject);
        vtt.setVersion("10");

        tTarget.addTask(vtt);

        try
        {
            tTarget.execute();
        }
        catch(BuildException be)
        {
            assertEquals("Expecting message about invalid version", true,
                    (be.getMessage().indexOf("Version must be filled and contain at least 1") > -1));
        }

    }

    /**
     * Sets up the test.
     *
     * @throws Exception DOCUMENTME
     */
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
    }
}
