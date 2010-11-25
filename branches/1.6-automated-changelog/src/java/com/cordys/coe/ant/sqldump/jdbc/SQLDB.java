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
package com.cordys.coe.ant.sqldump.jdbc;

import com.cordys.coe.util.Logger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * This class is a wrapper around the metadata of a complete database.
 *
 * @author  pgussow
 */
public class SQLDB
{
    /**
     * Holds the connection to the SQL database.
     */
    private Connection cConn;
    /**
     * Holds the metadata.
     */
    private DatabaseMetaData dmdMetaData;
    /**
     * Holds all the tables in this database.
     */
    private LinkedHashMap<String, SQLTable> hmAllTables;
    /**
     * Holds the tables that should be included.
     */
    private String sOnlyTables;

    /**
     * Constructor. Retrieves all the metadata from SQL
     *
     * @param   cConn  The connection to use.
     *
     * @throws  SQLException  DOCUMENTME
     */
    public SQLDB(Connection cConn)
          throws SQLException
    {
        this(cConn, "");
    }

    /**
     * Constructor. Retrieves all the metadata from SQL
     *
     * @param   cConn        The connection to use.
     * @param   sOnlyTables  The tables that should be parsed.
     *
     * @throws  SQLException  DOCUMENTME
     */
    public SQLDB(Connection cConn, String sOnlyTables)
          throws SQLException
    {
        this.cConn = cConn;
        this.sOnlyTables = sOnlyTables;

        dmdMetaData = (DatabaseMetaData) cConn.getMetaData();
        hmAllTables = new LinkedHashMap<String, SQLTable>();

        getAllTables(dmdMetaData);
    }

    /**
     * This method gets the JDBC connection.
     *
     * @return  The JDBC connection.
     */
    public Connection getConnection()
    {
        return cConn;
    }

    /**
     * This method returns a list of tables in the order that they should be created. It takes the
     * foreign key relations into account so the list returned in in the same order as they should
     * be created.
     *
     * @return  The sorted list of tables.
     */
    public SQLTable[] getScriptingOrder()
    {
        SQLTable[] astReturn = null;

        // This hashmap will contain the names of the tables that are already in the resonse.
        LinkedHashMap<String, String> hmDone = new LinkedHashMap<String, String>();

        // This arraylist will contain the tables in the proper order for creation.
        ArrayList<SQLTable> alSortedTables = new ArrayList<SQLTable>();

        // First we'll iterate all the tables. When a table is examined we'll check if the table has
        // any FK relations to any other tables. If those are found it will make sure that the table
        // it references is added before this table.
        for (Iterator<SQLTable> iTables = hmAllTables.values().iterator(); iTables.hasNext();)
        {
            SQLTable stTable = iTables.next();
            checkTable(stTable, hmDone, alSortedTables, null);
        }

        // Arraylist is sorted, now convert it to an array.
        astReturn = alSortedTables.toArray(new SQLTable[alSortedTables.size()]);

        return astReturn;
    }

    /**
     * This method returns the table object for the passed on tablename.
     *
     * @param   sTablename  The name of the table.
     *
     * @return  The corresponding SQLTable. Returns null if the table could not be found.
     */
    public SQLTable getTable(String sTablename)
    {
        return hmAllTables.get(sTablename);
    }

    /**
     * This method returns all the columns.
     *
     * @return  All the columns.
     */
    public SQLTable[] getTables()
    {
        SQLTable[] maReturn = new SQLTable[hmAllTables.size()];

        int iCount = 0;

        for (Iterator<SQLTable> iTables = hmAllTables.values().iterator(); iTables.hasNext();)
        {
            SQLTable stTable = iTables.next();
            maReturn[iCount] = stTable;
            iCount++;
        }

        return maReturn;
    }

    /**
     * This method sets the JDBC connection.
     *
     * @param  cConn  The JDBC connection.
     */
    public void setConnection(Connection cConn)
    {
        this.cConn = cConn;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return  a string representation of the object.
     */
    public String toString()
    {
        StringBuffer sbBuffer = new StringBuffer();
        sbBuffer.append("Database\n");

        SQLTable[] mtTables = getTables();

        for (int iCount = 0; iCount < mtTables.length; iCount++)
        {
            SQLTable mtTable = mtTables[iCount];
            sbBuffer.append(mtTable.toString());
            sbBuffer.append("\n");
        }

        return sbBuffer.toString();
    }

    /**
     * This method checks if the passed on table has any FK relations. If so, the table it has
     * references to will eb processed first.
     *
     * @param  stTable         The table.
     * @param  hmDone          HashMap holding all the already-processed tables.
     * @param  alSortedTables  Holds the final arraylist in the sorted order.
     * @param  alParentTables  The parent tables. This is needed to make sure we're able to
     *                         determine cyclic references.
     */
    private void checkTable(SQLTable stTable, LinkedHashMap<String, String> hmDone,
                            ArrayList<SQLTable> alSortedTables, ArrayList<String> alParentTables)
    {
        Logger.log("Checking table " + stTable.getName());

        if (!hmDone.containsKey(stTable.getName()))
        {
            // Table is not yet added.
            SQLForeignKey[] asfkFKs = stTable.getForeignKeys();

            for (int iCount = 0; iCount < asfkFKs.length; iCount++)
            {
                SQLForeignKey sfkKey = asfkFKs[iCount];
                String sReferencesTable = sfkKey.getPKTable();

                // If the table has not been processed, first process the table it references to.
                // Off course self-references are possible. If that is the case, no need to go
                // deeper.
                if (!hmDone.containsKey(sReferencesTable) &&
                        !stTable.getName().equals(sReferencesTable))
                {
                    // To avoid cyclic references.
                    if (!((alParentTables != null) && alParentTables.contains(stTable.getName())))
                    {
                        SQLTable stRefTable = hmAllTables.get(sReferencesTable);

                        if (alParentTables == null)
                        {
                            alParentTables = new ArrayList<String>();
                        }

                        alParentTables.add(stTable.getName());

                        checkTable(stRefTable, hmDone, alSortedTables, alParentTables);
                    }
                }
            }

            // Dependencies have been added. So now add this table to the list.
            alSortedTables.add(stTable);
            hmDone.put(stTable.getName(), stTable.getName());
        }
    }

    /**
     * This method returns the list of all tables in this database.
     *
     * @param   dmdMetaData  The metadata object.
     *
     * @throws  SQLException  In case of any exceptions
     */
    private void getAllTables(DatabaseMetaData dmdMetaData)
                       throws SQLException
    {
        // MySQL:
        // ResultSet rsTables = dmdMetaData.getTables("", "", "%", null);
        // SQLServer:
        ResultSet rsTables = dmdMetaData.getTables(null, null, null, new String[] { "TABLE" });

        TypeHashMap<String, TypeHashMap<String, String>> hmTables = Util.buildHashMapForResultset(rsTables,
                                                                                                  "TABLE_NAME");

        rsTables.close();

        for (Iterator<String> iTables = hmTables.keySet().iterator(); iTables.hasNext();)
        {
            String sTablename = iTables.next();
            TypeHashMap<String, String> thmValues = hmTables.get(sTablename);
            boolean bParse = true;

            if ((sOnlyTables != null) && (sOnlyTables.length() > 0))
            {
                if (sTablename.indexOf(sOnlyTables) < 0)
                {
                    bParse = false;
                }
            }

            // For SQL server:
            if (sTablename.equals("dtproperties"))
            {
                bParse = false;
            }

            if (bParse == true)
            {
                SQLTable mstTable = SQLTable.getInstance(dmdMetaData, thmValues);
                Logger.log("Got table " + mstTable.getName());
                hmAllTables.put(mstTable.getName(), mstTable);
            }
            else
            {
                Logger.log("Skipping table " + sTablename);
            }
        }
    }
}
