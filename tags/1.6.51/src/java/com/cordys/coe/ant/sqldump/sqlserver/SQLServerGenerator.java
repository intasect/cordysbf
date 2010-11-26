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
package com.cordys.coe.ant.sqldump.sqlserver;

import com.cordys.coe.ant.sqldump.SQLGenerator;
import com.cordys.coe.ant.sqldump.jdbc.SQLColumn;
import com.cordys.coe.ant.sqldump.jdbc.SQLDB;
import com.cordys.coe.ant.sqldump.jdbc.SQLFKColumn;
import com.cordys.coe.ant.sqldump.jdbc.SQLForeignKey;
import com.cordys.coe.ant.sqldump.jdbc.SQLIndex;
import com.cordys.coe.ant.sqldump.jdbc.SQLIndexColumn;
import com.cordys.coe.ant.sqldump.jdbc.SQLPrimaryKey;
import com.cordys.coe.ant.sqldump.jdbc.SQLTable;
import com.cordys.coe.util.Logger;

import java.io.IOException;
import java.io.OutputStream;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Map;

/**
 * This class generates the SQL statements for SQL Server.
 *
 * @author  pgussow
 */
public class SQLServerGenerator extends SQLGenerator
{
    /**
     * Constructor.
     *
     * @param  dbDatabase  The database metadata.
     */
    public SQLServerGenerator(SQLDB dbDatabase)
    {
        super(dbDatabase);
    }

    /**
     * @see  SQLGenerator#generateInsertSQL(SQLTable, String, OutputStream, boolean)
     */
    public void generateInsertSQL(SQLTable stTable, String sStatement, OutputStream osStream,
                                  boolean enableIdentityInsert)
                           throws SQLException, IOException
    {
        Connection cConnection = getDatabase().getConnection();
        Statement stmt = cConnection.createStatement();
        String sTablename = stTable.getName();

        try
        {
            // First we need to determine if the current table has an auto identity field. If so and
            // enableIdentityInsert is set to true, the command needs to be prefixed with SET
            // IDENTITY_INSERT [table] ON
            boolean doIdentity = false;
            SQLPrimaryKey pk = stTable.getPrimaryKey();

            if (pk != null)
            {
                Map<Integer, SQLColumn> cols = pk.getColumns();

                if ((cols != null) && (cols.size() > 0))
                {
                    SQLColumn column = cols.get(cols.keySet().iterator().next());

                    if ((column.getTypeName().indexOf("identity") > -1) && enableIdentityInsert)
                    {
                        doIdentity = true;
                    }
                }
            }

            // Write the set identity
            if (doIdentity)
            {
                osStream.write(("SET IDENTITY_INSERT " + sTablename + " ON\n\n").getBytes());
            }

            // Get the actual statement.
            String sRealSQL = sStatement;

            // Define the actual SQL statement that will be executed.
            if ((sRealSQL == null) || (sRealSQL.trim().length() == 0))
            {
                sRealSQL = "select * from " + sTablename;
            }

            Logger.log("Executing statement: " + sRealSQL);

            // Execute the query and get the metadata.
            ResultSet rsResults = stmt.executeQuery(sRealSQL);
            ResultSetMetaData rsmd = rsResults.getMetaData();
            int iNumberOfColumns = rsmd.getColumnCount();

            // Generate the base insert
            StringBuffer sbBase = new StringBuffer("INSERT INTO [");
            sbBase.append(sTablename);
            sbBase.append("] ([");

            // Get the names of the columns
            for (int iCount = 1; iCount <= iNumberOfColumns; iCount++)
            {
                sbBase.append(rsmd.getColumnName(iCount));

                if (iCount < iNumberOfColumns)
                {
                    sbBase.append("], [");
                }
            }
            sbBase.append("]) VALUES (");

            String sBaseInsert = sbBase.toString();

            while (rsResults.next())
            {
                StringBuffer sbCurrent = new StringBuffer(sBaseInsert);

                for (int iCount = 1; iCount <= iNumberOfColumns; iCount++)
                {
                    sbCurrent.append(getEscapedValue(rsResults.getString(iCount),
                                                     rsmd.getColumnTypeName(iCount)));

                    if (iCount < iNumberOfColumns)
                    {
                        sbCurrent.append(", ");
                    }
                }

                sbCurrent.append(");\n");

                // Write the generated statement to the outputstream.
                osStream.write(sbCurrent.toString().getBytes());
            }

            if (doIdentity)
            {
                osStream.write(("SET IDENTITY_INSERT " + sTablename + " OFF\n\n").getBytes());
            }
        }
        finally
        {
            stmt.close();
        }

        osStream.write("\n".getBytes());
    }

    /**
     * This method generates the metadata (DDL) SQL scripts.
     *
     * @param   astTables            The tables to dump.
     * @param   osStream             The outputstream to write it to.
     * @param   bIncludeCreateTable  Whether or not to include the create table statements
     * @param   bIncludeIndex        Whether or not to include the indexes
     * @param   bIncludeForeignKey   Whether or not to include the foreignkeys
     *
     * @throws  SQLException  In case of any exceptions
     * @throws  IOException   In case of any exceptions
     */
    public void generateMetadata(SQLTable[] astTables, OutputStream osStream,
                                 boolean bIncludeCreateTable, boolean bIncludeIndex,
                                 boolean bIncludeForeignKey)
                          throws SQLException, IOException
    {
        // Generate the create-table-script.
        if (bIncludeCreateTable)
        {
            for (int iCount = 0; iCount < astTables.length; iCount++)
            {
                SQLTable stTable = astTables[iCount];
                String sColumnName = "";
                String sTableName = stTable.getName();

                Logger.log("Generating metadata for " + sTableName);

                StringBuffer sbSQLStatement = new StringBuffer();
                sbSQLStatement.append("PRINT \'Creating table " + sTableName + "...\'\nGO\n");
                sbSQLStatement.append("CREATE TABLE [dbo].[");
                sbSQLStatement.append(sTableName);
                sbSQLStatement.append("](");

                // Get all the columns and script them
                SQLColumn[] ascColumns = stTable.getColumns();

                boolean bFirst = true;

                for (int iColumnCounter = 0; iColumnCounter < ascColumns.length; iColumnCounter++)
                {
                    SQLColumn scColumn = ascColumns[iColumnCounter];

                    if (!bFirst)
                    {
                        sbSQLStatement.append(" ,\n");
                    }
                    else
                    {
                        bFirst = false;
                        sbSQLStatement.append("\n");
                    }

                    // Write the name of the column
                    sColumnName = scColumn.getColumnName();
                    sbSQLStatement.append("\t[");
                    sbSQLStatement.append(sColumnName);
                    sbSQLStatement.append("] ");

                    // Write the type of the column
                    String sType = scColumn.getTypeName();
                    sbSQLStatement.append(handleColumnSize(sType, scColumn.getColumnSize()));

                    // Write possible NULL values
                    sbSQLStatement.append(handleNULL(scColumn.isNullable()));

                    // Handle default value
                    sbSQLStatement.append(handleDefautValue(scColumn.getDefaultValue()));
                }
                sbSQLStatement.append("\n) ON [PRIMARY]\nGO\n\n");
                osStream.write(sbSQLStatement.toString().getBytes());
            }
        }

        if (bIncludeIndex)
        {
            // Generate index and foreign-key contraints.
            generateIndexScripts(astTables, osStream);
        }

        if (bIncludeForeignKey)
        {
            generateFKConstraintScripts(astTables, osStream);
        }
    }

    /**
     * This method scripts the entire database.
     *
     * @param   bMetadata                     Whether or not to include the metadata.
     * @param   bData                         Whether or not to include the data.
     * @param   includeCreateTable            DOCUMENTME
     * @param   includeIndices                DOCUMENTME
     * @param   includeForeignKeyConstraints  DOCUMENTME
     * @param   osStream                      The outputstream to write to.
     *
     * @throws  SQLException  In case of any exceptions
     * @throws  IOException   In case of any exceptions
     */
    public void scriptDatabase(boolean bMetadata, boolean bData, boolean includeCreateTable,
                               boolean includeIndices, boolean includeForeignKeyConstraints,
                               OutputStream osStream)
                        throws SQLException, IOException
    {
        // Get the database object to script
        SQLDB dbDatabase = getDatabase();

        // Get the tables in the appropiate order to script, taking the dependencies into account.
        SQLTable[] astTables = dbDatabase.getScriptingOrder();

        if (bMetadata)
        {
            generateMetadata(astTables, osStream, includeCreateTable, includeIndices,
                             includeForeignKeyConstraints);
        }

        if (bData)
        {
            for (int iCount = 0; iCount < astTables.length; iCount++)
            {
                generateInsertSQL(astTables[iCount], null, osStream, true);
            }
        }
    }

    /**
     * The function checks runs a while loop for the vector , which holds the list of table names.
     * For the each table name it creates the Constraints script for the table name.
     *
     * @param   astTables  Table array which holds all the tableNames for which Contraints scripts
     *                     should be created.
     * @param   osStream   PrintWriter object to write the scripts.
     *
     * @throws  SQLException  In case of any exceptions
     * @throws  IOException   In case of any exceptions
     */
    private void generateFKConstraintScripts(SQLTable[] astTables, OutputStream osStream)
                                      throws SQLException, IOException
    {
        for (int iCount = 0; iCount < astTables.length; iCount++)
        {
            SQLTable stTable = astTables[iCount];
            String sTableName = stTable.getName();

            SQLForeignKey[] asfkFKs = stTable.getForeignKeys();

            if (asfkFKs.length > 0)
            {
                StringBuffer sbSQL = new StringBuffer();
                sbSQL.append("PRINT \'Creating foreign key constraint(s) for table " + sTableName +
                             "...\'\nGO\n");
                sbSQL.append("ALTER TABLE [dbo].[" + sTableName + "] ADD\n");

                // Iterate through all the foreign keys for this table
                for (int iFKCount = 0; iFKCount < asfkFKs.length; iFKCount++)
                {
                    SQLForeignKey sfkKey = asfkFKs[iFKCount];

                    Logger.log("Generating foreign key " + sfkKey.getFKName());

                    sbSQL.append("\tCONSTRAINT [");
                    sbSQL.append(sfkKey.getFKName());
                    sbSQL.append("] FOREIGN KEY\n(\n");

                    // Generate the SQL for the columns.
                    SQLFKColumn[] asfkcColumns = sfkKey.getSortedColumns();
                    StringBuffer sbOwnColumns = new StringBuffer("");
                    StringBuffer sbRefColumns = new StringBuffer("");

                    for (int iColumnCounter = 0; iColumnCounter < asfkcColumns.length;
                             iColumnCounter++)
                    {
                        SQLFKColumn sfkcColumn = asfkcColumns[iColumnCounter];

                        // The table's own columns
                        sbOwnColumns.append("\t\t[");
                        sbOwnColumns.append(sfkcColumn.getFKColumnName());
                        sbOwnColumns.append("]");

                        if (iColumnCounter < (asfkcColumns.length - 1))
                        {
                            sbOwnColumns.append(",");
                        }
                        sbOwnColumns.append("\n");

                        // The tables reference columns
                        sbRefColumns.append("\t\t[");
                        sbRefColumns.append(sfkcColumn.getPKColumnName());
                        sbRefColumns.append("]");

                        if (iColumnCounter < (asfkcColumns.length - 1))
                        {
                            sbRefColumns.append(",");
                        }
                        sbRefColumns.append("\n");
                    }

                    // Finish up the rest of the SQL
                    sbSQL.append(sbOwnColumns.toString());
                    sbSQL.append(") REFERENCES [dbo].[");
                    sbSQL.append(sfkKey.getPKTable());
                    sbSQL.append("] (\n");
                    sbSQL.append(sbRefColumns.toString());
                    sbSQL.append(")");

                    if (iFKCount < (asfkFKs.length - 1))
                    {
                        sbSQL.append(",");
                    }
                    sbSQL.append("\n");
                }

                // Add the GO statement for this table.
                sbSQL.append("GO\n\n");

                osStream.write(sbSQL.toString().getBytes());
            }
        }
    }

    /**
     * The function generateIndexScripts creates the index scripts for all the tables in the Vector
     * which is passed as argument.It runs a while loop and calls the getIndexInfo() method of the
     * DatabaseMetaData and runs a while loop for the ResultSet which is returned by the method
     * getIndexInfo(), and creates the Index scripts.
     *
     * @param   astTables  Table array which holds all the tableNames for which Index scripts should
     *                     be created.
     * @param   osStream   PrintWriter object to write the scripts.
     *
     * @throws  SQLException  In case of any exceptions
     * @throws  IOException   In case of any exceptions
     */
    private void generateIndexScripts(SQLTable[] astTables, OutputStream osStream)
                               throws SQLException, IOException
    {
        for (int iCount = 0; iCount < astTables.length; iCount++)
        {
            SQLTable stTable = astTables[iCount];
            String sTableName = stTable.getName();

            Logger.log("Generating indices for table " + sTableName);

            // Get the indices defined on the table.
            SQLIndex[] asiIndices = stTable.getIndices();

            short sType = -1;
            String sIndexName = "";
            boolean bFirst = true;

            StringBuffer sbSQL = new StringBuffer();
            sbSQL.append("PRINT \'Creating indices for table " + sTableName + "...\'\nGO\n");
            sbSQL.append("ALTER TABLE [dbo].[" + sTableName + "] WITH NOCHECK ADD\n");

            StringBuffer sbNormalIndices = new StringBuffer("");

            // Iterate all indices. Only create the statements for the named indices.
            for (int iIndexCount = 0; iIndexCount < asiIndices.length; iIndexCount++)
            {
                SQLIndex siIndex = asiIndices[iIndexCount];
                sIndexName = siIndex.getName();

                if ((sIndexName != null) && (sIndexName.length() > 0))
                {
                    Logger.log("Generating index " + sIndexName);
                    sType = siIndex.getIndexType();

                    if (!((sType == DatabaseMetaData.tableIndexOther) && !siIndex.isUnique()))
                    {
                        if (bFirst == true)
                        {
                            // It's the first one, so remove the flag.
                            bFirst = false;
                        }
                        else
                        {
                            // There was 1 index before this one, so add a ','.
                            // But only do this if it's a constraint index.
                            sbSQL.append("\t,\n");
                        }
                    }

                    SQLIndexColumn[] asicColumns = siIndex.getSortedColumns();
                    boolean bConstraint = true;

                    if (sType == DatabaseMetaData.tableIndexClustered)
                    {
                        // It's the primary key
                        sbSQL.append("\tCONSTRAINT [");
                        sbSQL.append(sIndexName);
                        sbSQL.append("] PRIMARY KEY  CLUSTERED\n\t(");
                    }
                    else if ((sType == DatabaseMetaData.tableIndexOther) && siIndex.isUnique())
                    {
                        // It's a 'normal' index.
                        sbSQL.append("\tCONSTRAINT [");
                        sbSQL.append(sIndexName);
                        sbSQL.append("] UNIQUE NONCLUSTERED \n\t(");
                    }
                    else if ((sType == DatabaseMetaData.tableIndexOther) && !siIndex.isUnique())
                    {
                        bConstraint = false;
                        sbNormalIndices.append(" CREATE INDEX [");
                        sbNormalIndices.append(sIndexName);
                        sbNormalIndices.append("] ON [dbo].[");
                        sbNormalIndices.append(sTableName);
                        sbNormalIndices.append("](");
                    }

                    // Add the columns
                    for (int iColumnCounter = 0; iColumnCounter < asicColumns.length;
                             iColumnCounter++)
                    {
                        SQLIndexColumn sicColumn = asicColumns[iColumnCounter];

                        if (bConstraint == true)
                        {
                            sbSQL.append("\n\t\t[");
                            sbSQL.append(sicColumn.getName());
                            sbSQL.append("] ");

                            if (sicColumn.getAscDesc() == SQLIndexColumn.TYPE_DESCENDING)
                            {
                                sbSQL.append("DESC ");
                            }

                            if (iColumnCounter < (asicColumns.length - 1))
                            {
                                sbSQL.append(",");
                            }
                        }
                        else
                        {
                            sbNormalIndices.append("[");
                            sbNormalIndices.append(sicColumn.getName());
                            sbNormalIndices.append("]");

                            if (iColumnCounter < (asicColumns.length - 1))
                            {
                                sbNormalIndices.append(",");
                            }
                        }
                    }

                    // Add closing marker
                    if (bConstraint == true)
                    {
                        sbSQL.append("\n\t) ON [PRIMARY]\n");
                    }
                    else
                    {
                        sbNormalIndices.append(") ON [PRIMARY]\nGO\n\n");
                    }
                }
            }
            sbSQL.append("GO\n\n");

            // Only write the SQL to the file if there were any indices. Otherwise ignore it.
            if (bFirst == false)
            {
                osStream.write(sbSQL.toString().getBytes());
                osStream.write(sbNormalIndices.toString().getBytes());
            }
        }
    }

    /**
     * This method returns the value as it should be appended in the SQL statement. All special
     * characters are escaped, so the response of this function can be added to an SQL file.
     *
     * @param   sDBValue     The database value.
     * @param   sColumnType  The type of the field.
     *
     * @return  The escaped value.
     */
    private String getEscapedValue(String sDBValue, String sColumnType)
    {
        String sReturn = sDBValue;

        if (!((sReturn == null) || sColumnType.equals("bigit") || sColumnType.equals("binary") ||
                  sColumnType.equals("bit") || sColumnType.equals("float") ||
                  sColumnType.equals("decimal") || (sColumnType.indexOf("int") > 0) ||
                  (sColumnType.indexOf("money") > 0) || sColumnType.equals("numeric") ||
                  sColumnType.equals("binary")))
        {
            sReturn = "'" + sReturn.replaceAll("'", "''") + "'";
        }
        return sReturn;
    }

    /**
     * The method handleColumnSize checks for the data type if it's numeric then it return
     * "[numeric] ([MaxSize],0)", Otherwise if for varchar,char or nchar it returns
     * "[Data_TYPE](size)", for all other types it returns "[DATA_TYPE]"
     *
     * @param   sType  Date Type of the column.
     * @param   iSize  Maximum size for the column.
     *
     * @return  Returns the handled String.
     */
    private String handleColumnSize(String sType, int iSize)
    {
        String sReturn;

        if (sType.equals("numeric"))
        {
            sReturn = "[" + sType + "] (" + iSize + "," + "0)";
        }
        else
        {
            if (sType.indexOf("char") >= 0)
            {
                sReturn = "[" + sType + "] (" + iSize + ")";
            }
            else if (sType.indexOf("identity") >= 0)
            {
                // Support for identity-field.
                sReturn = sType;
            }
            else
            {
                sReturn = "[" + sType + "]";
            }
        }
        return sReturn;
    }

    /**
     * This method handles the default value.
     *
     * @param   sDefaultValue  The default value.
     *
     * @return  The default value.
     */
    private String handleDefautValue(String sDefaultValue)
    {
        String sReturnValue = "";

        if (sDefaultValue != null)
        {
            sReturnValue = " Default " + sDefaultValue;
        }
        return sReturnValue;
    }

    /**
     * This method checks for the input .If the input is "1" then returns " NULL " , otherwise " NOT
     * NULL "
     *
     * @param   bNullable  String to be checked for null or not.
     *
     * @return  Returns the String " NULL " if the input is "1" otherwise " NOT NULL".
     */
    private String handleNULL(boolean bNullable)
    {
        String sReturnNull;

        if (bNullable == true)
        {
            sReturnNull = " NULL ";
        }
        else
        {
            sReturnNull = " NOT NULL ";
        }
        return sReturnNull;
    }
}
