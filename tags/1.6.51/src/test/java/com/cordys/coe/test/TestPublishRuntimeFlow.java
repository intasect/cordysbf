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

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;
import com.cordys.tools.ant.cm.Content;
import com.cordys.tools.ant.cm.ContentManagerTask;

public class TestPublishRuntimeFlow
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
            dl.setMessageOutputLevel(Project.MSG_DEBUG);
            pProject.addBuildListener(dl);
            pProject.init();
            pProject.setBaseDir(new File("c:/temp/demotla"));

            pProject.setProperty("connection.mode", "webgateway");
            pProject.setProperty("webgateway.url",
                                 "http://cnd0986/cordys/com.eibus.web.soap.Gateway.wcp");
            pProject.setProperty(BuildFrameworkProperty.SRC_CONTENT_COBOC_FOLDERS.getName(), "C:\\temp\\testruntimeflows\\coboc");

            ContentManagerTask cmt = new ContentManagerTask();
            cmt.setProject(pProject);
            cmt.setOrganization("o=pgdemo,cn=cordys,o=vanenburg.com");
            cmt.setUserdn("cn=pgussow,cn=organizational users,o=pgdemo,cn=cordys,o=vanenburg.com");

            ContentManagerTask.OperationAttribute oaAttr = new ContentManagerTask.OperationAttribute();
            oaAttr.setValue("filetoecx");
            
            cmt.setOperation(oaAttr);
            cmt.setFilter("");
            cmt.setHost("localhost");
            cmt.setPort(6366);
            cmt.setUsername("cn=Directory Manager,o=vanenburg.com");
            cmt.setPassword("coemanager");

            Content cContent = new Content();
            cContent.setType("studio-bpms");
            cContent.setDir(new File("C:\\temp\\testruntimeflows\\studioflows"));

            cmt.addConfiguredContent(cContent);

            cmt.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
