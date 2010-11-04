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
package com.cordys.coe.test;

import java.io.File;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Echo;
import org.tigris.subversion.svnant.SvnTask;
import org.tigris.subversion.svnant.commands.coe.CoELog;
import org.tigris.subversion.svnant.commands.coe.CoEStatusCheck;

/**
 * Class to test the StatusCheck task.
 *
 * @author pgussow
 */
public class TestBuildFile
{
    /**
     * Main method
     *
     * @param saArgs
     */
    public static void main(String[] saArgs)
    {
        try
        {
            Project pProject = new Project();
            DefaultLogger dl = new DefaultLogger();
            dl.setOutputPrintStream(System.out);
            dl.setErrorPrintStream(System.err);
            dl.setMessageOutputLevel(Project.MSG_DEBUG);
            pProject.addBuildListener(dl);
            pProject.setBaseDir(new File("C:\\installed\\workspaces\\EclipsePlugins\\SVN_BuildTasks_main"));
            pProject.init();
            System.out.println(pProject.getBaseDir());

            SvnTask stTask = new SvnTask();
            stTask.setUsername("pgussow");
            stTask.setDescription("bfsvn");
            stTask.setProject(pProject);

            CoEStatusCheck sc = new CoEStatusCheck();
            sc.setPath(new File("C:\\installed\\workspaces\\EclipsePlugins\\SVN_BuildTasks_main"));
            sc.setProject(pProject);
            sc.setFailOnModification(false);
            
            stTask.addCoEStatusCheck(sc);
            
            
            CoELog lLog = new CoELog();
            lLog.setProject(pProject);
            lLog.setPath(new File("C:\\installed\\workspaces\\runtime-workbench-workspace\\ccc"));
            //lLog.setUrl("http://srv-nl-apps7:5001/svn/playground/ccc/main");
            lLog.setPropLatestRevision("svn.latest.revision");
            
            stTask.addCoELog(lLog);
            
            lLog = new CoELog();
            lLog.setProject(pProject);
            //lLog.setPath(new File("C:\\installed\\workspaces\\runtime-workbench-workspace\\ccc"));
            lLog.setUrl("http://srv-nl-apps7:5001/svn/playground/ccc/main");
            lLog.setPropLatestRevision("svn.latest.revision2");
            
            stTask.addCoELog(lLog);
            
            stTask.execute();
            
            System.out.println(pProject.replaceProperties("Latest Path revision: ${svn.latest.revision}"));
            System.out.println(pProject.replaceProperties("Latest  URL revision: ${svn.latest.revision2}"));
            
            Echo eEcho = new Echo();
            eEcho.setMessage(pProject.replaceProperties("URL: ${svn.url} Revision: ${svn.revision} NodeKind: ${svn.node.kind} Latest Revision: ${svn.latest.revision}"));
            eEcho.setProject(pProject);
            
            eEcho.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
