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

import org.apache.tools.ant.BuildException;

/**
 * Contains database configuration.
 *
 * @author mpoyhone
 */
public class DatabaseConfig
{
    /**
     * Holds the database type. Values are defined in EDataBaseTypes.
     */
    private String type;
    /**
     * Data base name.
     */
    private String dbname;
    /**
     * Connection string. Must be set if the database name and server are not set.
     */
    private String url;
    /**
     * Host name of the database server.
     */
    private String serverName;
    /**
     * DB user.
     */
    private String user;
    /**
     * DB password.
     */
    private String password;
    /**
     * JDBC driver class. Must be set if the generic dbtype is used.
     */
    private String driver;    
    /**
     * Returns the dbType.
     *
     * @return Returns the dbType.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the dbType.
     *
     * @param dbType The dbType to be set.
     */
    public void setType(String dbType)
    {
        this.type = dbType;
    }
    

    /**
     * Returns the connectionString.
     *
     * @return Returns the connectionString.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Sets the connectionString.
     *
     * @param connectionString The connectionString to be set.
     */
    public void setUrl(String connectionString)
    {
        this.url = connectionString;
    }

    /**
     * Returns the databaseName.
     *
     * @return Returns the databaseName.
     */
    public String getDbname()
    {
        return dbname;
    }

    /**
     * Sets the databaseName.
     *
     * @param databaseName The databaseName to be set.
     */
    public void setDbname(String databaseName)
    {
        this.dbname = databaseName;
    }

    /**
     * Returns the driverClass.
     *
     * @return Returns the driverClass.
     */
    public String getDriver()
    {
        return driver;
    }

    /**
     * Sets the driverClass.
     *
     * @param driverClass The driverClass to be set.
     */
    public void setDriver(String driverClass)
    {
        this.driver = driverClass;
    }

    /**
     * Returns the password.
     *
     * @return Returns the password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password The password to be set.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Returns the user.
     *
     * @return Returns the user.
     */
    public String getUser()
    {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user The user to be set.
     */
    public void setUser(String user)
    {
        this.user = user;
    }
    
    /**
     * Creates a new provider instance.
     * @return
     */
    public IDatabaseProvider createDatabasePrivider() {
        if (type == null || type.length() == 0)
        {
            throw new BuildException("Parameter dbtype is not set.");
        }
        
        EDataBaseTypes db = EDataBaseTypes.valueOf(type.toUpperCase());
        
        try
        {
            return db.getProviderInstance();
        }
        catch (Exception e)
        {
            throw new BuildException("Unable to create DB provider: " + type, e);
        }
    }

    /**
     * Returns the serverName.
     *
     * @return Returns the serverName.
     */
    public String getServerName()
    {
        return serverName;
    }

    /**
     * Sets the serverName.
     *
     * @param serverName The serverName to be set.
     */
    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }    
}
