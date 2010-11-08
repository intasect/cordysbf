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
import org.apache.tools.ant.taskdefs.optional.EchoProperties;

import com.cordys.coe.ant.project.GetBFversionInfoTask;
import com.cordys.coe.ant.sqldump.Database;
import com.cordys.coe.ant.sqldump.Sql;
import com.cordys.coe.ant.sqldump.SqlDataScriptingTask;

/**
 * DOCUMENTME
 *
 * @author $author$
 */
public class TestVersiontask
{
    /**
     * Main method.
     *
     * @param saArguments The commandline arguments.
     */
    public static void main(String[] saArguments)
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

            SqlDataScriptingTask sq = new SqlDataScriptingTask();
            sq.setProject(pProject);
            
            Database db = new Database();
            db.setSrcType("sqlserver");
            db.setDestType("sqlserver");
            db.setDestfile(new File("d:/temp/create.sql"));
            db.setUser("sa");
            db.setPassword("platformdba");
            db.setIncludeData(false);
            db.setConnectionstring("jdbc:odbc:Driver={SQL Server};Server=srv-nl-crd71;Database=SFS_CoF_General_New");
            db.setProject(pProject);
            
            sq.addConfiguredDatabase(db);

            sq.execute();
            
            EchoProperties ep = new EchoProperties();
            ep.setProject(pProject);
            ep.execute();
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
