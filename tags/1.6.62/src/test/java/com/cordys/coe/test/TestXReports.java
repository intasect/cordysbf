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
import org.apache.tools.ant.Target;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;

import com.cordys.tools.ant.cm.Content;
import com.cordys.tools.ant.cm.ContentManagerTask;

/**
 * Test class for testing the XReports content.
 *
 * @author pgussow
 */
public class TestXReports
{
    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            Project pProject = new Project();
            DefaultLogger dl = new DefaultLogger();
            dl.setOutputPrintStream(System.out);
            dl.setErrorPrintStream(System.err);
            dl.setMessageOutputLevel(Project.MSG_DEBUG);
            pProject.addBuildListener(dl);
            pProject.init();
            
            pProject.getProperties();
            pProject.setBaseDir(new File("D:\\development\\workspaces\\EclipsePlugins\\XReports13_C3_main"));
            pProject.setProperty("connection.mode", "webgateway");
            pProject.setProperty("webgateway.url", "http://srv-nl-ces70/cordys/com.eibus.web.soap.Gateway.wcp");
            pProject.setProperty("webgateway.user", "NTDOM\\pgussow");
            pProject.setProperty("webgateway.password", "phillip27");
            pProject.setProperty("src.content.xreports", "d:/temp/bfxreport");
            pProject.setProperty("single.content", "AgentDetail");
            
            ContentManagerTask cmt = new ContentManagerTask();
            cmt.setProject(pProject);
            cmt.setOrganization("o=XReports,cn=cordys,o=vanenburg.com");
            cmt.setUserdn("cn=pgussow,cn=organizational users,o=XReports,cn=cordys,o=vanenburg.com");

            ContentManagerTask.OperationAttribute oaAttr = new ContentManagerTask.OperationAttribute();
            //oaAttr.setValue("ecxtofile");
            oaAttr.setValue("publish");
            cmt.setOperation(oaAttr);
            
            Content cContent = new Content();
            cContent.setType("xreports");
            
            FileSet fs = new FileSet();
            fs.setDir(new File("d:/temp/bfxreport"));
            fs.setProject(pProject);
            fs.createInclude().setName("**/xreport.metadata");

            cContent.addFileset(fs);

            cmt.addConfiguredContent(cContent);

            cmt.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
