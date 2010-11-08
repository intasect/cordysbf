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
package com.cordys.coe.ant.sqldump;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.cordys.coe.util.log.AntTaskLogger;

/**
 * This class enables database content to be scripted via ant. The
 * configuration in the antfile should look something like this:
 * <pre>
 * <sqldump>
 *   <database type="sqlserver" user="sa" password="sa" connectionstring"jdbc:odbc:Driver={SQL Server};Server=cnd0626;Database=VerhalenDB">
 * 	   <sql destfile="${src.content.dbschema}/tableinit.sql" append="true|false" overwrite="true|false">select * from table</sql>
 *   </database>
 * </sqldump>
 * </pre>
 *
 * @author pgussow
 */
public class SqlDataScriptingTask extends Task
{
    /**
     * Holds the wrapper around the ant logger.
     */
    private AntTaskLogger atlLog;
    /**
     * Holds all the nested database-elements.
     */
    private ArrayList<Database> alDatabases;

    /**
     * Default constructor.
     */
    public SqlDataScriptingTask()
    {
        super();

        alDatabases = new ArrayList<Database>();
        atlLog = new AntTaskLogger(this);
    }

    /**
     * This method adds the configured database to the databases-map.
     *
     * @param dDatabase The database to add.
     */
    public void addConfiguredDatabase(Database dDatabase)
    {
        alDatabases.add(dDatabase);
    }

    /**
     * This method does the actual execution of the task. It goes through all
     * the configured databases and scripts the data to SQL files.
     */
    public void execute()
                 throws BuildException
    {
        atlLog.info("Dumping " + alDatabases.size() + " databases.");

        for (Iterator<Database> iDatabases = alDatabases.iterator();
                 iDatabases.hasNext();)
        {
            Database dDatabase = iDatabases.next();
            dDatabase.execute();
        }
    }
}
