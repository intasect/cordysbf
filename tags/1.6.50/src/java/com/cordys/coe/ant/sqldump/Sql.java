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

import com.cordys.coe.ant.sqldump.jdbc.SQLDB;
import com.cordys.coe.util.log.AntTaskLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.sql.SQLException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This class wraps around the sql-task which is nested withing the database-task.
 *
 * @author  pgussow
 */
public class Sql extends Task
{
    /**
     * Whether or not to append the data to the file. Defaults to false.
     */
    private boolean append = false;
    /**
     * Holds the wrapper around the ant logger.
     */
    private AntTaskLogger atlLog;
    /**
     * Holds the destination-file.
     */
    private File destfile;
    /**
     * Holds whether or not to enable the identity insert on the data.
     */
    private boolean enableIdentityInsert;
    /**
     * Whether or not to overwrite the existing file. Defaults to true
     */
    private boolean overwrite = true;
    /**
     * Holds the statement to execute.
     */
    private String sStatement;
    /**
     * Holds the name of the table that is being dumped.
     */
    private String tablename;

    /**
     * Default constructor.
     */
    public Sql()
    {
        super();
        atlLog = new AntTaskLogger(this);
    }

    /**
     * This method sets the statement to execute.
     *
     * @param  sText  The statement to execute.
     */
    public void addText(String sText)
    {
        sStatement = sText;
    }

    /**
     * This method returns the destination file.
     *
     * @return  The destfile.
     */
    public File getDestfile()
    {
        return destfile;
    }

    /**
     * This method gets whether or not to enable the identity insert on the data.
     *
     * @return  Whether or not to enable the identity insert on the data.
     */
    public boolean getEnableIdentityInsert()
    {
        return enableIdentityInsert;
    }

    /**
     * This method returns the statement to execute.
     *
     * @return  Returns the statement to execute.
     */
    public String getStatement()
    {
        return sStatement;
    }

    /**
     * This method returns the name of the table.
     *
     * @return  Returns the tablename.
     */
    public String getTablename()
    {
        return tablename;
    }

    /**
     * This method returns whether or not the content should be appended to the existing file.
     *
     * @return  Whether or not the content should be appended to the existing file.
     */
    public boolean isAppend()
    {
        return append;
    }

    /**
     * This method returns whether or not the existing file should be overwritten.
     *
     * @return  Whether or not the existing file should be overwritten.
     */
    public boolean isOverwrite()
    {
        return overwrite;
    }

    /**
     * This method executes the SQL statement and generates the specified SQL statement. It writes
     * the result to the specified file.
     *
     * @param   dbDatabase  The database metadata.
     * @param   sType       The database-type.
     *
     * @throws  SQLException  In case of any exceptions
     * @throws  IOException   In case of any exceptions
     */
    public void runStatement(SQLDB dbDatabase, String sType)
                      throws SQLException, IOException
    {
        SQLGenerator sgGenerator = SQLGenerator.getInstance(dbDatabase, sType);
        File fRealFile = getDestfile();

        if ((isAppend() == false) && fRealFile.exists() && (isOverwrite() == true))
        {
            fRealFile.delete();
        }
        else if ((isAppend() == false) && fRealFile.exists() && (isOverwrite() == false))
        {
            throw new BuildException("Not allowed to overwrite the file " +
                                     fRealFile.getAbsolutePath());
        }

        if (atlLog.isDebugEnabled())
        {
            atlLog.debug("Creating file outputstream for file " + fRealFile.getAbsolutePath());
        }

        FileOutputStream fos = new FileOutputStream(fRealFile, isAppend());

        if (atlLog.isDebugEnabled())
        {
            atlLog.debug("Generating script for table " + getTablename());
        }
        sgGenerator.generateInsertSQL(dbDatabase.getTable(getTablename()), getStatement(), fos,
                                      getEnableIdentityInsert());
    }

    /**
     * This method sets whether or not the content should be appended to the existing file.
     *
     * @param  append  Whether or not the content should be appended to the existing file.
     */
    public void setAppend(boolean append)
    {
        this.append = append;
    }

    /**
     * This method sets the destination file.
     *
     * @param  destfile  The destfile to set.
     */
    public void setDestfile(File destfile)
    {
        this.destfile = destfile;
    }

    /**
     * This method sets whether or not to enable the identity insert on the data.
     *
     * @param  enableIdentityInsert  Whether or not to enable the identity insert on the data.
     */
    public void setEnableIdentityInsert(boolean enableIdentityInsert)
    {
        this.enableIdentityInsert = enableIdentityInsert;
    }

    /**
     * This method sets whether or not the existing file should be overwritten.
     *
     * @param  overwrite  Whether or not the existing file should be overwritten.
     */
    public void setOverwrite(boolean overwrite)
    {
        this.overwrite = overwrite;
    }

    /**
     * This method sets the name of the table.
     *
     * @param  tablename  The tablename to set.
     */
    public void setTablename(String tablename)
    {
        this.tablename = tablename;
    }
}
