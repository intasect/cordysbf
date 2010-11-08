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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.cordys.coe.ant.sqldump.jdbc.SQLDB;
import com.cordys.coe.util.LogWindow;
import com.cordys.coe.util.Logger;
import com.cordys.coe.util.log.AntTaskLogger;

/**
 * This task holds the configuration for a specific database.
 *
 * @author pgussow
 */
public class Database extends Task
    implements LogWindow
{
    /**
     * Holds the wrapper around the ant logger.
     */
    private AntTaskLogger atlLog;
    /**
     * Holds all the tables for which the metadata needs to be dumped.
     */
    private ArrayList<Metadata> alMetadata;
    /**
     * Holds all the configured SQL statements.
     */
    private ArrayList<Sql> alStatements;
    /**
     * Holds the destination-file.
     */
    private File destfile;
    /**
     * Holds the connectionstring.
     */
    private String connectionstring;
    /**
     * Holds the password.
     */
    private String password;
    /**
     * Holds the destination type for the SQL script.
     */
    private String destType;
    /**
     * Holds the type of source database.
     */
    private String srcType;
    /**
     * Holds the user.
     */
    private String user;
    /**
     * Whether or not to append the data to the file. Defaults to false.
     */
    private boolean append = false;
    /**
     * Indicates that all the data should be scripted.
     */
    private boolean includeData = true;
    /**
     * Indicates that the metadata for this database schould be scripted.
     */
    private boolean includeMetadata = true;
    /**
     * Whether or not to overwrite the existing file. Defaults to true
     */
    private boolean overwrite = true;
    /**
     * Indicates that table ddl must be scripted. Defaults to true
     */
    private boolean includeCreateTable = true;
    /**
     * Indicates that indices must be scripted. Defaults to true
     */
    private boolean includeIndex = true;
    /**
     * Indicates that foreignkeys must be scripted. Defaults to true
     */
    private boolean includeForeignKey = true;
    
    /**
     * Default Constructor.
     */
    public Database()
    {
        super();
        alStatements = new ArrayList<Sql>();
        alMetadata = new ArrayList<Metadata>();
        atlLog = new AntTaskLogger(this);
    }

    /**
     * This method sets whether or not the content should be appended to the
     * existing file.
     *
     * @param append Whether or not the content should be appended to the
     *        existing file.
     */
    public void setAppend(boolean append)
    {
        this.append = append;
    }

    /**
     * This method returns whether or not the content should be appended to the
     * existing file.
     *
     * @return Whether or not the content should be appended to the existing
     *         file.
     */
    public boolean isAppend()
    {
        return append;
    }

    /**
     * This method sets the configured connectionstring.
     *
     * @param connectionstring The new connectionstring.
     */
    public void setConnectionstring(String connectionstring)
    {
        this.connectionstring = connectionstring;
    }

    /**
     * This mehtod returns the configured connectionstring.
     *
     * @return The configured connectionstring.
     */
    public String getConnectionstring()
    {
        return connectionstring;
    }

    /**
     * This method sets the destination file.
     *
     * @param destfile The destfile to set.
     */
    public void setDestfile(File destfile)
    {
        this.destfile = destfile;
    }

    /**
     * This method returns the destination file.
     *
     * @return The destfile.
     */
    public File getDestfile()
    {
        return destfile;
    }

    /**
     * This method sets whether or not to script the data.
     *
     * @param includeData Whether or not to script the data.
     */
    public void setIncludeData(boolean includeData)
    {
        this.includeData = includeData;
    }

    /**
     * This method returns whether or not to script the data.
     *
     * @return Whether or not to script the data.
     */
    public boolean isIncludeData()
    {
        return includeData;
    }

    /**
     * This method sets whether or not to script the metadata.
     *
     * @param  Whether or not to script the metadata.
     */
    public void setIncludeMetadata(boolean includeMetadata)
    {
        this.includeMetadata = includeMetadata;
    }

    /**
     * This method returns whether or not to script the metadata.
     *
     * @return Whether or not to script the metadata.
     */
    public boolean isIncludeMetadata()
    {
        return includeMetadata;
    }

    /**
	 * @return the includeForeignKeyConstraints
	 */
	public boolean isIncludeForeignKey()
	{
		return includeForeignKey;
	}

	/**
	 * @param includeForeignKeyConstraints the includeForeignKeyConstraints to set
	 */
	public void setIncludeForeignKey(boolean includeForeignKeyConstraints)
	{
		this.includeForeignKey = includeForeignKeyConstraints;
	}

	/**
	 * @return the includeCreateTable
	 */
	public boolean isIncludeCreateTable()
	{
		return includeCreateTable;
	}

	/**
	 * @param includeCreateTable the includeCreateTable to set
	 */
	public void setIncludeCreateTable(boolean includeCreateTable)
	{
		this.includeCreateTable = includeCreateTable;
	}

	/**
	 * @return the includeIndices
	 */
	public boolean isIncludeIndex()
	{
		return includeIndex;
	}

	/**
	 * @param includeIndices the includeIndices to set
	 */
	public void setIncludeIndex(boolean includeIndices)
	{
		this.includeIndex = includeIndices;
	}

    /**
     * This method sets whether or not the existing file should be overwritten.
     *
     * @param overwrite Whether or not the existing file should be overwritten.
     */
    public void setOverwrite(boolean overwrite)
    {
        this.overwrite = overwrite;
    }

    /**
     * This method returns whether or not the existing file should be
     * overwritten.
     *
     * @return Whether or not the existing file should be overwritten.
     */
    public boolean isOverwrite()
    {
        return overwrite;
    }

    /**
     * This method sets the configured password.
     *
     * @param password The new password.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * This method returns the configured password.
     *
     * @return The configured password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * This method sets the destType.
     *
     * @param destType The new destType.
     */
    public void setDestType(String type)
    {
        this.destType = type;
    }

    /**
     * This method returns the destType.
     *
     * @return The destType.
     */
    public String getDestType()
    {
        return destType;
    }
    
    /**
     * This method sets the source type.
     *
     * @param srcType The source type.
     */
    public void setSrcType(String srcType)
    {
        this.srcType = srcType;
    }

    /**
     * This method returns the source type.
     *
     * @return The source type.
     */
    public String getSrcType()
    {
        return srcType;
    }

    /**
     * This method sets the configured user.
     *
     * @param user The new user.
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * This method returns the configured user.
     *
     * @return The configured user.
     */
    public String getUser()
    {
        return user;
    }
    
    /**
     * This method adds the configured metadata to the arraylist.
     *
     * @param mMetadata The configured metadata.
     */
    public void addConfiguredMetadata(Metadata mMetadata)
    {
        alMetadata.add(mMetadata);
    }

    /**
     * This method adds the configured statement to the arraylist.
     *
     * @param sSQL The configured SQL statement.
     */
    public void addConfiguredSql(Sql sSQL)
    {
        alStatements.add(sSQL);
    }

    /**
     * This mehtod executes all statements for this database.
     */
    public void execute()
                 throws BuildException
    {
        try
        {
            //Connect to the database.
            atlLog.info("Connecting to " + getConnectionstring());
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");

            Connection cConnection = DriverManager.getConnection(getConnectionstring(),
                                                                 getUser(),
                                                                 getPassword());
            atlLog.info("Succesfully connected to the database.");
            
            //Now we need to get the metadata from the database. But we first need to set up the logger
            Logger.getLogger().addMsgDisplay(this);
            Logger.getLogger().setStdOut(false);
            
            SQLDB dbDatabase = new SQLDB(cConnection);
            atlLog.debug(dbDatabase.toString());

            //Check if there are any metadata or sql tasks configured. If not, we'll fill them based on the metadata.
            if ((alMetadata.size() == 0) && (alStatements.size() == 0))
            {
                createTasks(dbDatabase);
            }
            else
            {
                for (Iterator<Metadata> iMetadata = alMetadata.iterator();
                         iMetadata.hasNext();)
                {
                    Metadata mMetadata = iMetadata.next();
                    atlLog.info("Dumping metadata for table " +
                                mMetadata.getTablename());
                    mMetadata.runStatement(dbDatabase, getDestType());
                }

                //Execute the statements and write them to a file.
                for (Iterator<Sql> iStatements = alStatements.iterator();
                         iStatements.hasNext();)
                {
                    Sql sStatement = iStatements.next();
                    atlLog.info("Executing statement: " +
                                sStatement.getStatement());
                    sStatement.runStatement(dbDatabase, getDestType());
                }
            }
        }
        catch (Exception e)
        {
            throw new BuildException("Error dumping the database.", e);
        }
    }

    /**
     * This method receives logmessages from the DB class.
     *
     * @param iCategory The log category.
     * @param sMessage The message to log.
     */
    public void onMessage(int iCategory, String sMessage)
    {
        if (atlLog != null)
        {
            switch (iCategory)
            {
                case Logger.LOG_DEBUG:
                    atlLog.debug(sMessage);
                    break;

                case Logger.LOG_EXCEPTION:
                    atlLog.error(sMessage);
                    break;

                case Logger.LOG_MESSAGE:
                    atlLog.info(sMessage);
                    break;

                case Logger.LOG_WARNING:
                    atlLog.error(sMessage);
                    break;

                default:
                    atlLog.debug(sMessage);
                    break;
            }
        }
    }

    /**
     * This method creates the metadata- and sql-tasks based on the metadata of
     * the database.
     *
     * @param cConnection The JDBC connection.
     */
    private void createTasks(SQLDB dbDatabase)
                      throws IOException, SQLException
    {
        SQLGenerator sgGenerator = SQLGenerator.getInstance(dbDatabase,
                                                            getDestType());
        File fRealFile = getDestfile();

        if ((isAppend() == false) && fRealFile.exists() &&
                (isOverwrite() == true))
        {
            fRealFile.delete();
        }
        else if ((isAppend() == false) && fRealFile.exists() &&
                     (isOverwrite() == false))
        {
            throw new BuildException("Not allowed to overwrite the file " +
                                     fRealFile.getAbsolutePath());
        }

        atlLog.debug("Creating file outputstream for file " +
                     fRealFile.getAbsolutePath());

        FileOutputStream fos = new FileOutputStream(fRealFile, isAppend());

        atlLog.debug("Generating metadata for database.");
        sgGenerator.scriptDatabase(isIncludeMetadata(), isIncludeData(), 
        		isIncludeCreateTable(), isIncludeIndex(), 
        		isIncludeForeignKey(), fos);
    }
}
