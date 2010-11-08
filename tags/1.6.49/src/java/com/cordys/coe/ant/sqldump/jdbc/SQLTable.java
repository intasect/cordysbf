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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Class contains the metadata for a MySQL table.
 */
public class SQLTable
{
    /**
     * Holds the columns for this table with their metadata
     */
    private LinkedHashMap<String, SQLColumn> hmColumns;
    /**
     * Holds all the foreign keys that are defined.
     */
    private LinkedHashMap<String, SQLForeignKey> hmForeignKeys;
    /**
     * Holds all the indices that are defined.
     */
    private LinkedHashMap<String, SQLIndex> hmIndices;
    /**
     * Holds the promary key for this table.
     */
    private SQLPrimaryKey spkPrimaryKey;
    /**
     * Holds the category
     */
    private String sCatalog;
    /**
     * Holds the name of the table.
     */
    private String sName;
    /**
     * Holds teh remarks for the table.
     */
    private String sRemarks;
    /**
     * Holds the schema
     */
    private String sSchema;
    /**
     * Holds the type of table.
     */
    private String sTableType;

/**
     * Constructor.
     *
     * @param sName The name of the table.
     * @param sCatalog The catalog of the table.
     * @param sSchema The schema of the table.
     * @param sTableType The type of the table.
     * @param sRemarks The remarks for the table.
     */
    public SQLTable(String sName, String sCatalog, String sSchema,
                    String sTableType, String sRemarks)
    {
        this.sName = sName;
        this.sCatalog = sCatalog;
        this.sSchema = sSchema;
        this.sTableType = sTableType;
        this.sRemarks = sRemarks;
        hmColumns = new LinkedHashMap<String, SQLColumn>();
        hmIndices = new LinkedHashMap<String, SQLIndex>();
        hmForeignKeys = new LinkedHashMap<String, SQLForeignKey>();
    }

    /**
     * This method creates a new instance of the SQL table based on a
     * hashmap with the needed values. It is expected that the hashmap holds
     * values for: TABLE_NAME, TABLE_CAT, TABLE_SCHEM, TABLE_TYPE, REMARKS
     *
     * @param dmdMetaData The MetaData object.
     * @param thmValues The hashmap with the values.
     *
     * @return A new instance of the SQL table based on the passed on values.
     */
    public static SQLTable getInstance(DatabaseMetaData dmdMetaData,
                                       TypeHashMap<String, String> thmValues)
                                throws SQLException
    {
        String sTableName = thmValues.getString("TABLE_NAME");
        String sCategory = thmValues.getString("TABLE_CAT");
        String sSchema = thmValues.getString("TABLE_SCHEM");
        String sType = thmValues.getString("TABLE_TYPE");
        String sRemarks = thmValues.getString("REMARKS");

        SQLTable mstReturn = new SQLTable(sTableName, sCategory, sSchema,
                                          sType, sRemarks);
        mstReturn.parseColumns(dmdMetaData);
        mstReturn.parsePrimaryKey(dmdMetaData);
        mstReturn.parseIndices(dmdMetaData);
        mstReturn.parseForeignKeys(dmdMetaData);
        return mstReturn;
    }

    /**
     * This method gets the catalog.
     *
     * @return The catalog.
     */
    public String getCatalog()
    {
        return sCatalog;
    }

    /**
     * This method returns the column for the given name.
     *
     * @param sColumnName The name of the column.
     *
     * @return The corresponding column.
     */
    public SQLColumn getColumn(String sColumnName)
    {
        SQLColumn scReturn = null;

        if (hmColumns.containsKey(sColumnName))
        {
            scReturn = hmColumns.get(sColumnName);
        }
        return scReturn;
    }

    /**
     * This method returns all the columns.
     *
     * @return All the columns.
     */
    public SQLColumn[] getColumns()
    {
        SQLColumn[] maReturn = new SQLColumn[hmColumns.size()];
        int iCount = 0;

        for (Iterator<String> iColums = hmColumns.keySet().iterator();
                 iColums.hasNext();)
        {
            maReturn[iCount] = hmColumns.get(iColums.next());
            iCount++;
        }

        return maReturn;
    }

    /**
     * This method returns all the foreign keys that are defined on
     * this table.
     *
     * @return All foreign keys.
     */
    public SQLForeignKey[] getForeignKeys()
    {
        SQLForeignKey[] asfkReturn = new SQLForeignKey[hmForeignKeys.size()];

        int iCount = 0;

        for (Iterator<SQLForeignKey> iTables = hmForeignKeys.values().iterator();
                 iTables.hasNext();)
        {
            SQLForeignKey stTable = iTables.next();
            asfkReturn[iCount] = stTable;
            iCount++;
        }

        return asfkReturn;
    }

    /**
     * This method returns all the indices that are set on this table.
     * The indices are returned in a sorted way.
     *
     * @return The indices that are configured on this table.
     */
    public SQLIndex[] getIndices()
    {
        SQLIndex[] asiReturn = new SQLIndex[hmIndices.size()];
        int iCount = 0;

        for (Iterator<SQLIndex> iIndices = hmIndices.values().iterator();
                 iIndices.hasNext();)
        {
            SQLIndex siIndex = iIndices.next();
            asiReturn[iCount] = siIndex;
            iCount++;
        }
        return asiReturn;
    }

    /**
     * This method gets the tablename.
     *
     * @return The tablename.
     */
    public String getName()
    {
        return sName;
    }

    /**
     * This method returns the primary key.
     *
     * @return The primary key.
     */
    public SQLPrimaryKey getPrimaryKey()
    {
        return spkPrimaryKey;
    }

    /**
     * This method gets the remarks.
     *
     * @return The remarks.
     */
    public String getRemarks()
    {
        return sRemarks;
    }

    /**
     * This method gets the schema.
     *
     * @return The schema.
     */
    public String getSchema()
    {
        return sSchema;
    }

    /**
     * This method gets the tabletype.
     *
     * @return The tabletype.
     */
    public String getTableType()
    {
        return sTableType;
    }

    /**
     * This method retrieves the information for the columns.
     *
     * @param dmdMetaData The metadata to retrieve it from.
     */
    public void parseColumns(DatabaseMetaData dmdMetaData)
                      throws SQLException
    {
        ResultSet rsColumns = dmdMetaData.getColumns(sCatalog, sSchema, sName,
                                                     "%");

        LinkedHashMap<String, TypeHashMap<String, String>> hmTableColumns = Util.buildHashMapForResultset(rsColumns,
                                                                                                          new String[]
                                                                                                          {
                                                                                                              "TABLE_NAME",
                                                                                                              "COLUMN_NAME"
                                                                                                          });

        rsColumns.close();

        for (Iterator<String> iTables = hmTableColumns.keySet().iterator();
                 iTables.hasNext();)
        {
            String sKey = (String) iTables.next();
            TypeHashMap<String, String> thmValues = hmTableColumns.get(sKey);
            SQLColumn mscColum = SQLColumn.getInstance(dmdMetaData, thmValues);
            hmColumns.put(mscColum.getColumnName(), mscColum);
        }
    }

    /**
     * This method gets all the indices that are set on the table.
     *
     * @param dmdMetaData The metadata object.
     */
    public void parseIndices(DatabaseMetaData dmdMetaData)
                      throws SQLException
    {
        ResultSet rsIndices = dmdMetaData.getIndexInfo(getCatalog(),
                                                       getSchema(), getName(),
                                                       false, true);
        TypeHashMap<String, TypeHashMap<String, String>> thmIndices = Util.buildHashMapForResultset(rsIndices,
                                                                                                    new String[]
                                                                                                    {
                                                                                                        "INDEX_NAME",
                                                                                                        "COLUMN_NAME"
                                                                                                    });
        rsIndices.close();

        //The hashmap contains all the indices that are on the table. We need to make instances by index name.
        //So we'll convert the array to group the data by index_name
        LinkedHashMap<String, LinkedHashMap<String, TypeHashMap<String, String>>> hmTmpIndices = Util.groupByField(thmIndices,
                                                                                                                   "INDEX_NAME",
                                                                                                                   "COLUMN_NAME");

        //Now for each index we can create an index-object.
        for (Iterator<String> iIndices = hmTmpIndices.keySet().iterator();
                 iIndices.hasNext();)
        {
            String sIndexName = iIndices.next();
            LinkedHashMap<String, TypeHashMap<String, String>> hmColumns = hmTmpIndices.get(sIndexName);

            SQLIndex siIndex = SQLIndex.getInstance(this, sIndexName, hmColumns);

            hmIndices.put(sIndexName, siIndex);
        }
    }

    /**
     * Finds the primary key for this object.
     *
     * @param dmdMetaData The metadata.
     */
    public void parsePrimaryKey(DatabaseMetaData dmdMetaData)
                         throws SQLException
    {
        ResultSet rsKey = dmdMetaData.getPrimaryKeys(sCatalog, sSchema, sName);
        TypeHashMap<String, TypeHashMap<String, String>> thmValues = Util.buildHashMapForResultset(rsKey,
                                                                                                   "COLUMN_NAME");

        spkPrimaryKey = SQLPrimaryKey.getInstance(this, thmValues);
    }

    /**
     * This method sets the catalog.
     *
     * @param catalog The catalog.
     */
    public void setCatalog(String catalog)
    {
        sCatalog = catalog;
    }

    /**
     * This method sets the tablename.
     *
     * @param name The tablename.
     */
    public void setName(String name)
    {
        sName = name;
    }

    /**
     * this method sets the remarks.
     *
     * @param remarks The remarks.
     */
    public void setRemarks(String remarks)
    {
        sRemarks = remarks;
    }

    /**
     * This method sets the schema.
     *
     * @param schema The schema.
     */
    public void setSchema(String schema)
    {
        sSchema = schema;
    }

    /**
     * This method sets the tabletype.
     *
     * @param tableType The tabletype.
     */
    public void setTableType(String tableType)
    {
        sTableType = tableType;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuffer sbBuffer = new StringBuffer();
        sbBuffer.append("====================\nTable ");
        sbBuffer.append(getName());
        sbBuffer.append("\n");

        SQLColumn[] maCols = getColumns();

        for (int iCount = 0; iCount < maCols.length; iCount++)
        {
            SQLColumn maCol = maCols[iCount];
            sbBuffer.append("  ");
            sbBuffer.append(maCol.toString());

            sbBuffer.append("\n");
        }

        //Primary key
        if (spkPrimaryKey != null)
        {
            sbBuffer.append("\n");
            sbBuffer.append(spkPrimaryKey.toString());
        }

        //Indices
        for (Iterator<SQLIndex> iIndices = hmIndices.values().iterator();
                 iIndices.hasNext();)
        {
            SQLIndex siIndex = iIndices.next();
            sbBuffer.append(siIndex.toString());
            sbBuffer.append("\n");
        }

        //Foreign Key
        for (Iterator<SQLForeignKey> iFKs = hmForeignKeys.values().iterator();
                 iFKs.hasNext();)
        {
            SQLForeignKey sfkIndex = iFKs.next();
            sbBuffer.append(sfkIndex.toString());
            sbBuffer.append("\n");
        }
        sbBuffer.append("\n");

        return sbBuffer.toString();
    }

    /**
     * This method adds the passed on foreign key to the current list
     * of foreign keys.
     *
     * @param sfkKey The foreign key to add.
     */
    private void addForeignKey(SQLForeignKey sfkKey)
    {
        if (!hmForeignKeys.containsKey(sfkKey.getFKName()))
        {
            hmForeignKeys.put(sfkKey.getFKName(), sfkKey);
        }
    }

    /**
     * This method parses all the foreign keys that are set on this
     * table and stores it internally.
     *
     * @param dmdMetaData The metadata object for the database.
     */
    private void parseForeignKeys(DatabaseMetaData dmdMetaData)
                           throws SQLException
    {
        ResultSet rsFkResultSets = dmdMetaData.getImportedKeys(getCatalog(),
                                                               getSchema(),
                                                               getName());
        TypeHashMap<String, TypeHashMap<String, String>> thmResults = Util.buildHashMapForResultset(rsFkResultSets,
                                                                                                    new String[]
                                                                                                    {
                                                                                                        "FK_NAME",
                                                                                                        "FKTABLE_NAME",
                                                                                                        "FKCOLUMN_NAME"
                                                                                                    });

        rsFkResultSets.close();

        //Group the results by Fk name
        LinkedHashMap<String, LinkedHashMap<String, TypeHashMap<String, String>>> hmGrouped = Util.groupByField(thmResults,
                                                                                                                "FK_NAME",
                                                                                                                "FKCOLUMN_NAME");

        for (Iterator<String> iFKs = hmGrouped.keySet().iterator();
                 iFKs.hasNext();)
        {
            String sKey = iFKs.next();
            LinkedHashMap<String, TypeHashMap<String, String>> hmFKColumns = hmGrouped.get(sKey);

            //Now we have all the columns for this foreign key, so we can create the corresponding object.
            SQLForeignKey sfkKey = SQLForeignKey.getInstance(this, sKey,
                                                             hmFKColumns);

            addForeignKey(sfkKey);
        }
    }
}
