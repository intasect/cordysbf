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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class identifies the primary key. It holds all the information for the primary key.
 *
 * @author  pgussow
 */
public class SQLPrimaryKey
{
    /**
     * Holds the list of columns that belong to the pk. The list is already ordered by sequence
     * number.
     */
    private LinkedHashMap<Integer, SQLColumn> hmColumns;
    /**
     * Holds the category.
     */
    private String sCatalog;
    /**
     * Holds the name of the primary key.
     */
    private String sKeyName;
    /**
     * Holds the schema.
     */
    private String sSchema;
    /**
     * Holds the name of the table.
     */
    private String sTablename;

    /**
     * Constructs the primary key object.
     *
     * @param  sCatalog    The table's catalog.
     * @param  sTablename  The table's name.
     * @param  sSchema     The table's schema.
     * @param  sKeyName    The name of the primary key. Can be null.
     */
    public SQLPrimaryKey(String sCatalog, String sTablename, String sSchema, String sKeyName)
    {
        super();
        this.sCatalog = sCatalog;
        this.sTablename = sTablename;
        this.sSchema = sSchema;
        this.sKeyName = sKeyName;
        hmColumns = new LinkedHashMap<Integer, SQLColumn>();
    }

    /**
     * This method returns a new instance of the primary key object. It parses the list of values
     * that were returned by the database.
     *
     * @param   stTable   The table object.
     * @param   hmValues  The TypeHashMap with all the rows that were returned.
     *
     * @return  A new instance of this class.
     */
    public static SQLPrimaryKey getInstance(SQLTable stTable,
                                            TypeHashMap<String, TypeHashMap<String, String>> hmValues)
    {
        SQLPrimaryKey spkReturn = null;

        // Try to find the name of the primary key.
        String sPKName = null;

        for (Iterator<String> iTables = hmValues.keySet().iterator();
                 iTables.hasNext() && (sPKName == null);)
        {
            String sKey = iTables.next();
            TypeHashMap<String, String> thmColValues = hmValues.get(sKey);
            sPKName = thmColValues.getString("PK_NAME");
        }

        spkReturn = new SQLPrimaryKey(stTable.getCatalog(), stTable.getName(), stTable.getSchema(),
                                      sPKName);

        for (Iterator<String> iTables = hmValues.keySet().iterator(); iTables.hasNext();)
        {
            String sKey = (String) iTables.next();
            TypeHashMap<String, String> thmColValues = hmValues.get(sKey);
            String sColumnName = thmColValues.getString("COLUMN_NAME");
            spkReturn.addColumn(thmColValues.getShort("KEY_SEQ"), stTable.getColumn(sColumnName));
        }

        return spkReturn;
    }

    /**
     * This method adds the column to.
     *
     * @param  iSequence  The sequence number for the column.
     * @param  scColumn   The actual column.
     */
    public void addColumn(int iSequence, SQLColumn scColumn)
    {
        Integer iKey = new Integer(iSequence);

        if (!hmColumns.containsKey(iKey))
        {
            hmColumns.put(iKey, scColumn);
        }
    }

    /**
     * This method gets the catalog.
     *
     * @return  The catalog.
     */
    public String getCatalog()
    {
        return sCatalog;
    }

    /**
     * This method gets the columns for this PK.
     *
     * @return  The columns for this PK.
     */
    public Map<Integer, SQLColumn> getColumns()
    {
        return new LinkedHashMap<Integer, SQLColumn>(hmColumns);
    }

    /**
     * This method gets the name of the key.
     *
     * @return  The name of the key.
     */
    public String getKeyName()
    {
        return sKeyName;
    }

    /**
     * This method gets the schema.
     *
     * @return  The schema.
     */
    public String getSchema()
    {
        return sSchema;
    }

    /**
     * This method gets the tablename.
     *
     * @return  The tablename.
     */
    public String gettablename()
    {
        return sTablename;
    }

    /**
     * This method returns a string representation of the object.
     *
     * @return  A string representation of the object.
     */
    public String toString()
    {
        StringBuffer sbReturn = new StringBuffer("Primary key: ");

        int iSize = hmColumns.size();

        for (int iCount = 1; iCount <= iSize; iCount++)
        {
            sbReturn.append(hmColumns.get(new Integer(iCount)).getColumnName());

            if (iCount < (iSize - 1))
            {
                sbReturn.append(", ");
            }
        }
        sbReturn.append("\n");

        return sbReturn.toString();
    }
}
