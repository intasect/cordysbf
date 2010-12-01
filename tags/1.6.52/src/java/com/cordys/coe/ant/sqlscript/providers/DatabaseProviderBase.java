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
package com.cordys.coe.ant.sqlscript.providers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.cordys.coe.ant.sqlscript.DatabaseConfig;
import com.cordys.coe.ant.sqlscript.IDatabaseProvider;
import com.cordys.coe.ant.sqlscript.SqlScript;

/**
 * Database creator class for SQLServer.
 *
 * @author mpoyhone
 */
public abstract class DatabaseProviderBase implements IDatabaseProvider
{
    protected Task task;

    protected abstract String getDefaultDriverClass();
    protected abstract String getScriptSeparator();
    protected abstract String getUrl(DatabaseConfig dbcfg);
    
    /**
     * @see com.cordys.coe.ant.sqlscript.IDatabaseProvider#execute(org.apache.tools.ant.Task, com.cordys.coe.ant.sqlscript.DatabaseConfig, com.cordys.coe.ant.sqlscript.SqlScript)
     */
    public void execute(Task t, DatabaseConfig dbcfg, SqlScript scriptDefinition) throws SQLException
    {
        task = t;
        
        // Connect to the database.
        String driverClass = dbcfg.getDriver();
        String url = dbcfg.getUrl();
        
        if (driverClass == null) {
            driverClass = getDefaultDriverClass();
        }
        
        if (url == null) {
            url = getUrl(dbcfg);
        }
        
        task.log("Connecting to " + url, Project.MSG_INFO);
        task.log("Database driver: " + driverClass, Project.MSG_VERBOSE);    
    
        try
        {
            Class.forName(driverClass);
        }
        catch (ClassNotFoundException e)
        {
            throw new SQLException("Unable to load driver class '" + driverClass + "': " + e.getMessage());
        }
    
        Connection conn = DriverManager.getConnection(url,
                                                      dbcfg.getUser(),
                                                      dbcfg.getPassword());
        
        task.log("Succesfully connected to the database.", Project.MSG_INFO);    
    
        if (! scriptDefinition.isTransaction()) {
            conn.setTransactionIsolation(Connection.TRANSACTION_NONE);
        }
        
        // Parse the SQL script file and execute all statements. 
        List<String> cmdList;
        
        try
        {
            cmdList = scriptDefinition.parseScriptFiles(task, getScriptSeparator());
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to parse the source file(s).", e);
        }
        
        runSqlScript(cmdList, conn);
        
        if (scriptDefinition.isTransaction()) {
            conn.commit();
        }
    }
    private void runSqlScript(Collection<String> cmdList, Connection conn) throws SQLException
    {
        for (String cmd : cmdList)
        {
            Statement st = conn.createStatement();
            
            try {
                task.log("  Executing statement: " + cmd, Project.MSG_DEBUG);
                
                st.executeUpdate(cmd);
                conn.commit();
            }
            finally {
                st.close();
            }
        }
    }
}
