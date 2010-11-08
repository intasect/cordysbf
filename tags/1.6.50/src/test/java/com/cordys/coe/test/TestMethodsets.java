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

/**
 * This class can be used to the import/export of methodsets.
 *
 * @author pgussow
 */
public class TestMethodsets
{
    /**
     * Main method.
     *
     * @param saArgs The commandline arguments.
     */
    public static void main(String[] saArgs)
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
        cmt.setOrganization("o=Windesheim,cn=cordys,o=vanenburg.com");
        cmt.setUserdn("cn=pgussow,cn=organizational users,o=Windesheim,cn=cordys,o=vanenburg.com");

        ContentManagerTask.OperationAttribute oaAttr = new ContentManagerTask.OperationAttribute();
        oaAttr.setValue("filetoecx");
        cmt.setOperation(oaAttr);
        cmt.setFilter("");
        cmt.setHost("cnd0626");
        cmt.setPort(3899);
        cmt.setUsername("cn=Directory Manager,o=vanenburg.com");
        cmt.setPassword("coemanager");
        
        Content cContent = new Content();
        cContent.setType("methodsets");
        cContent.setDir(new File("C:\\installed\\workspaces\\CoE\\GenLDAPConnector_main\\src\\content\\methodsets"));
        
        PatternSet ps = new PatternSet();
        ps.setIncludes("/**.*");

        cContent.addConfiguredPatternset(ps);

        cmt.addConfiguredContent(cContent);

        cmt.execute();



    }
}
