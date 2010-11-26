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

import com.cordys.tools.ant.cm.Content;
import com.cordys.tools.ant.cm.ContentManagerTask;

import java.io.File;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

/**
 * DOCUMENT ME!
 *
 * @author pgussow
 */
public class TestCoboc
{
    /**
     * DOCUMENTME
     *
     * @param args DOCUMENTME
     */
    public static void main(String[] args)
    {
        try
        {
            Project pProject = new Project();
            DefaultLogger dl = new DefaultLogger();
            dl.setOutputPrintStream(System.out);
            dl.setErrorPrintStream(System.err);
            dl.setMessageOutputLevel(Project.MSG_INFO);
            pProject.addBuildListener(dl);
            pProject.init();
            pProject.setBaseDir(new File("D:\\development\\workspaces\\MainWorkspace\\Siemens CoF_main"));
            

            pProject.setProperty("connection.mode", "webgateway");
            pProject.setProperty("webgateway.url",
                                 "http://srv-de-coe01/cordys/com.eibus.web.soap.Gateway.wcp");

            ContentManagerTask cmt = new ContentManagerTask();
            cmt.setProject(pProject);
            cmt.setOrganization("o=SiemensCoF,cn=cordys,o=vanenburg.com");
            cmt.setUserdn("cn=pgussow,cn=organizational users,o=SiemensCoF,cn=cordys,o=vanenburg.com");

            ContentManagerTask.OperationAttribute oaAttr = new ContentManagerTask.OperationAttribute();
            oaAttr.setValue("ecxtofile");
            cmt.setOperation(oaAttr);

            Content cContent = new Content();
            cContent.setType("coboc2");
            cContent.setDir(new File("c:/temp/"));

            cmt.addConfiguredContent(cContent);

            cmt.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
