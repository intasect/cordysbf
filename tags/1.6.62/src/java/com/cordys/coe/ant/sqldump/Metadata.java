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
import java.sql.SQLException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.cordys.coe.ant.sqldump.jdbc.SQLDB;
import com.cordys.coe.ant.sqldump.jdbc.SQLTable;
import com.cordys.coe.util.log.AntTaskLogger;

/**
 * This class wraps around the metadata-tag which is nested within the
 * database-task.
 *
 * @author pgussow
 */
public class Metadata extends Task
{
    /**
     * Holds the wrapper around the ant logger.
     */
    private AntTaskLogger atlLog;
    /**
     * Holds the destination-file.
     */
    private File destfile;
    /**
     * Holds the name of the table that is being dunmped.
     */
    private String tablename;
    /**
     * Whether or not to append the data to the file. Defaults to false.
     */
    private boolean append = false;
    /**
     * Whether or not to overwrite the existing file. Defaults to true
     */
    private boolean overwrite = true;

    /**
     * Default constructor.
     */
    public Metadata()
    {
        super();
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
     * This method sets the name of the table.
     *
     * @param tablename The tablename to set.
     */
    public void setTablename(String tablename)
    {
        this.tablename = tablename;
    }

    /**
     * This method returns the name of the table.
     *
     * @return Returns the tablename.
     */
    public String getTablename()
    {
        return tablename;
    }

    /**
     * This method executes the SQL statement and generates the specified SQL
     * statement. It writes the result to the specified file.
     *
     * @param dbDatabase The database metadata.
     * @param sType The database-type.
     */
    public void runStatement(SQLDB dbDatabase, String sType)
                      throws SQLException, IOException
    {
        SQLGenerator sgGenerator = SQLGenerator.getInstance(dbDatabase, sType);
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

        atlLog.debug("Generating metadata for table " + getTablename());
        sgGenerator.generateMetadata(new SQLTable[]
                                     {
                                         dbDatabase.getTable(getTablename())
                                     }, fos, true, true, true);
    }
}
