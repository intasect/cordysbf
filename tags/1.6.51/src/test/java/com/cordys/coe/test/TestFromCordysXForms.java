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
import org.apache.tools.ant.types.PatternSet;

import com.cordys.tools.ant.cm.Content;
import com.cordys.tools.ant.cm.ContentManagerTask;

public class TestFromCordysXForms
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
            

            ContentManagerTask cmt = new ContentManagerTask();
            cmt.setProject(pProject);
            cmt.setOrganization("o=Development,cn=cordys,o=ewals.com");
            cmt.setUserdn("cn=Administrator,cn=organizational users,o=Development,cn=cordys,o=ewals.com");

            ContentManagerTask.OperationAttribute oaAttr = new ContentManagerTask.OperationAttribute();
            oaAttr.setValue("ecxtofile");
            cmt.setOperation(oaAttr);
            cmt.setFilter("");
            cmt.setHost("ewnltegs100");
            cmt.setPort(3899);
            cmt.setUsername("cn=Directory Manager,o=ewals.com");
            cmt.setPassword("ldap123");

            Content cContent = new Content();
            cContent.setType("studio-xforms");
            cContent.setDir(new File("d:/temp/xforms"));

            PatternSet ps = new PatternSet();
            ps.setIncludes("/**");

            cContent.addConfiguredPatternset(ps);

            cmt.addConfiguredContent(cContent);

            cmt.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
