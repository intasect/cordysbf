/**
 * Copyright 2007 Cordys R&D B.V. 
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
package com.cordys.coe.ant.sqlscript;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Ant task for running SQL scripts with JDBC. The standard 'sql' task 
 * does not support running SQL command outside of transaction.
 * @author mpoyhone
 */
public class RunSqlScriptTask extends Task
{
    /**
     * Contains configured SQL scripts.
     */
    private List<SqlScript> scriptList = new ArrayList<SqlScript>(10);
    /**
     * Contains confgured database provider.
     */
    private DatabaseConfig database;
    
    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException
    {
        if (database == null) {
            throw new BuildException("No database is configured.");
        }
        
        if (scriptList.isEmpty()) {
            throw new BuildException("No scripts defined.");
        }
        
        IDatabaseProvider provider;
        
        provider = database.createDatabasePrivider();
        
        for (SqlScript s : scriptList)
        {
            try
            {
                provider.execute(this, database, s);
            }
            catch (SQLException e)
            {
                if (s.isHaltOnError()) {
                    throw new BuildException("Script execution failed.", e);
                } else {
                    log("Script execution failed: " + e.getMessage(), Project.MSG_WARN);
                }
            }
        }
    }

    public SqlScript createScript() {
        SqlScript res = new SqlScript();
        
        scriptList.add(res);
        
        return res; 
    }

    public DatabaseConfig createDatabase() {
        if (database != null) {
            throw new BuildException("Database is already set.");
        }
        
        database = new DatabaseConfig();
        
        return database;
    }
    /**
     * Returns the scriptList.
     *
     * @return Returns the scriptList.
     */
    public List<SqlScript> getScriptList()
    {
        return scriptList;
    }

    /**
     * Sets the scriptList.
     *
     * @param scriptList The scriptList to be set.
     */
    public void setScriptList(List<SqlScript> scriptList)
    {
        this.scriptList = scriptList;
    }
}
