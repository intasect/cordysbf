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
import com.cordys.coe.ant.sqldump.jdbc.SQLTable;
import com.cordys.coe.ant.sqldump.sqlserver.SQLServerGenerator;

import java.io.IOException;
import java.io.OutputStream;

import java.sql.SQLException;

/**
 * This abstract class is the base for generation SQL statements.
 *
 * @author  pgussow
 */
public abstract class SQLGenerator
{
    /**
     * Identifies the type for SQL server.
     */
    private static final String TYPE_SQLSERVER = "sqlserver";
    /**
     * Holds the JDBC connection.
     */
    private SQLDB dbDatabase;

    /**
     * Default constructor.
     *
     * @param  dbDatabase  The database metadata.
     */
    protected SQLGenerator(SQLDB dbDatabase)
    {
        this.dbDatabase = dbDatabase;
    }

    /**
     * This method generates insert-requests based on the specified SQL statement. The SQL statement
     * has to return results for 1 specific table.
     *
     * @param   stTablename           The name of the table to dump.
     * @param   sStatement            The SQL statement to run.
     * @param   osStream              The outputstream to write the statements to.
     * @param   enableIdentityInsert  Whether or not identity inserts are enabled.
     *
     * @throws  SQLException  In case of any exceptions
     * @throws  IOException   In case of any exceptions
     */
    public abstract void generateInsertSQL(SQLTable stTablename, String sStatement,
                                           OutputStream osStream, boolean enableIdentityInsert)
                                    throws SQLException, IOException;

    /**
     * This method generates the metadata (DDL) SQL scripts.
     *
     * @param   astTables           The names of the table to dump.
     * @param   osStream            The outputstream to write it to.
     * @param   includeCreateTable  weh
     * @param   includeIndex        DOCUMENTME
     * @param   includeForeignKey   DOCUMENTME
     *
     * @throws  SQLException  In case of any exceptions
     * @throws  IOException   In case of any exceptions
     */
    public abstract void generateMetadata(SQLTable[] astTables, OutputStream osStream,
                                          boolean includeCreateTable, boolean includeIndex,
                                          boolean includeForeignKey)
                                   throws SQLException, IOException;

    /**
     * This method scripts the entire database.
     *
     * @param   bMetadata            Whether or not to include the metadata.
     * @param   bData                Whether or not to include the data.
     * @param   bIncludeCreateTable  Whether or not to include the create table statements
     * @param   bIncludeIndex        Whether or not to include the indexes
     * @param   bIincludeForeignKey  Whether or not to include the foreignkeys
     * @param   osStream             The outputstream to write to.
     *
     * @throws  SQLException  In case of any exceptions
     * @throws  IOException   In case of any exceptions
     */
    public abstract void scriptDatabase(boolean bMetadata, boolean bData,
                                        boolean bIncludeCreateTable, boolean bIncludeIndex,
                                        boolean bIincludeForeignKey, OutputStream osStream)
                                 throws SQLException, IOException;

    /**
     * This method returns the specific instance that can be used for generating SQL scripts on the
     * specified connection.
     *
     * @param   dbDatabase  The database metadata.
     * @param   sType       The type.
     *
     * @return  The instance of the SQL generator that can be used.
     */
    public static SQLGenerator getInstance(SQLDB dbDatabase, String sType)
    {
        SQLGenerator sgReturn = null;

        if (sType.equals(TYPE_SQLSERVER))
        {
            sgReturn = new SQLServerGenerator(dbDatabase);
        }
        return sgReturn;
    }

    /**
     * This method gets the database metadata.
     *
     * @return  The database metadata.
     */
    public SQLDB getDatabase()
    {
        return dbDatabase;
    }

    /**
     * This method sets the database metadata.
     *
     * @param  dbDatabase  The database metadata.
     */
    public void setDatabase(SQLDB dbDatabase)
    {
        this.dbDatabase = dbDatabase;
    }
}
