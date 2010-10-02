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
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * This class is a wrapper around an index on a table.
 *
 * @author pgussow
 */
public class SQLIndex
{
    /**
     * Holds all the columns for this index.
     */
    private LinkedHashMap<String, SQLIndexColumn> hmIndexColumns;
    /**
     * Filter condition, if any. (may be null).
     */
    private String sFilterCondition;
    /**
     * Holds the name of the index.
     */
    private String sName;
    /**
     * Index catalog (may be null); null when TYPE is tableIndexStatistic.
     */
    private String sQualifier;
    /**
     * Holds whether or not this is a unique index.
     */
    private boolean bUnique;
    /**
     * When TYPE is tableIndexStatistic, then this is the number of rows in the
     * table; otherwise, it is the number of unique values in the index.
     */
    private int iCardinality;
    /**
     * When TYPE is tableIndexStatisic then this is the number of pages used
     * for the table, otherwise it is the number of pages used for the current
     * index.
     */
    private int iPages;
    /**
     * Holds the index type: tableIndexStatistic - this identifies table
     * statistics that are returned in conjuction with a table's index
     * descriptions. tableIndexClustered - this is a clustered index.
     * tableIndexHashed - this is a hashed index. tableIndexOther - this is
     * some other style of index
     */
    private short sIndexType;

    /**
     * Constructor.
     *
     * @param bUnique Whether or not this is a unique index.
     * @param sIndexType The type of the index.
     * @param sName The name of the index.
     * @param sQualifier Index catalog (may be null); null when TYPE is
     *        tableIndexStatistic.
     * @param sFilterCondition Filter condition, if any. (may be null)
     * @param iPages When TYPE is tableIndexStatisic then this is the number of
     *        pages used for the table, otherwise it is the number of pages
     *        used for the current index.
     * @param iCardinality The cardinality.
     */
    public SQLIndex(boolean bUnique, short sIndexType, String sName,
                    String sQualifier, String sFilterCondition, int iPages,
                    int iCardinality)
    {
        super();
        this.bUnique = bUnique;
        this.sIndexType = sIndexType;
        this.sName = sName;
        this.sQualifier = sQualifier;
        this.sFilterCondition = sFilterCondition;
        this.iPages = iPages;
        this.iCardinality = iCardinality;
        hmIndexColumns = new LinkedHashMap<String, SQLIndexColumn>();
    }

    /**
     * This method returns a new instance of this object that wraps the
     * index-data that is passed on.
     *
     * @param stTable The table object.
     * @param sIndexName The name of the index.
     * @param hmColumns HashMap containing all the column-information.
     *
     * @return A new instance of the SQLIndex object.
     */
    public static SQLIndex getInstance(SQLTable stTable, String sIndexName,
            LinkedHashMap<String, TypeHashMap<String, String>> hmColumns)
    {
        SQLIndex siReturn = null;
        TypeHashMap<String, String> thmTemp = hmColumns.get(hmColumns.keySet()
                                                                   .iterator()
                                                                   .next());

        boolean bUnique = thmTemp.getBoolean("NON_UNIQUE");
        short sIndexType = thmTemp.getShort("TYPE");
        String sQualifier = thmTemp.getString("INDEX_QUALIFIER");
        int iCardinality = thmTemp.getInt("CARDINALITY");
        int iPages = thmTemp.getInt("PAGES");
        String sFilterCondition = thmTemp.getString("FILTER_CONDITION");

        siReturn = new SQLIndex(bUnique, sIndexType, sIndexName, sQualifier,
                                sFilterCondition, iPages, iCardinality);

        //Create all the columns
        for (Iterator<String> iColumns = hmColumns.keySet().iterator();
                 iColumns.hasNext();)
        {
            String sKey = iColumns.next();
            TypeHashMap<String, String> thmColumn = hmColumns.get(sKey);
            
            SQLIndexColumn sicColumn = SQLIndexColumn.getInstance(thmColumn);

            siReturn.addColumn(sicColumn);
        }

        return siReturn;
    }

    /**
     * This method sets the cardinality.
     *
     * @param cardinality The cardinality.
     */
    public void setCardinality(int cardinality)
    {
        iCardinality = cardinality;
    }

    /**
     * This method gets the cardinality.
     *
     * @return The cardinality.
     */
    public int getCardinality()
    {
        return iCardinality;
    }

    /**
     * This method sets the filter condition.
     *
     * @param filterCondition The filter condition.
     */
    public void setFilterCondition(String filterCondition)
    {
        sFilterCondition = filterCondition;
    }

    /**
     * This method gets the filter condition.
     *
     * @return The filter condition.
     */
    public String getFilterCondition()
    {
        return sFilterCondition;
    }

    /**
     * This method sets the index type.
     *
     * @param indexType The index type.
     */
    public void setIndexType(short indexType)
    {
        sIndexType = indexType;
    }

    /**
     * This method gets the index type.
     *
     * @return The index type.
     */
    public short getIndexType()
    {
        return sIndexType;
    }

    /**
     * This method sets the index name.
     *
     * @param name The index name.
     */
    public void setName(String name)
    {
        sName = name;
    }

    /**
     * This method gets the index name.
     *
     * @return The index name.
     */
    public String getName()
    {
        return sName;
    }

    /**
     * This method sets the number of pages.
     *
     * @param pages The number of pages.
     */
    public void setPages(int pages)
    {
        iPages = pages;
    }

    /**
     * This method gets the number of pages.
     *
     * @return The number of pages.
     */
    public int getPages()
    {
        return iPages;
    }

    /**
     * This method sets the qualifier.
     *
     * @param qualifier The qualifier.
     */
    public void setQualifier(String qualifier)
    {
        sQualifier = qualifier;
    }

    /**
     * This method gets the qualifier.
     *
     * @return The qualifier.
     */
    public String getQualifier()
    {
        return sQualifier;
    }

    /**
     * This method sets whether or not the index is unique.
     *
     * @param unique Whether or not the index is unique.
     */
    public void setUnique(boolean unique)
    {
        bUnique = unique;
    }

    /**
     * This method gets whether or not the index is unique.
     *
     * @return Whether or not the index is unique.
     */
    public boolean isUnique()
    {
        return bUnique;
    }

    /**
     * This method adds the column to the index columns.
     *
     * @param sicColumn The column to add.
     */
    public void addColumn(SQLIndexColumn sicColumn)
    {
        if (!hmIndexColumns.containsKey(sicColumn.getName()))
        {
            hmIndexColumns.put(sicColumn.getName(), sicColumn);
        }
    }

    /**
     * This method returns a string representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString()
    {
        StringBuffer sbReturn = new StringBuffer("");

        sbReturn.append("Index ");
        sbReturn.append(getName());
        sbReturn.append("(");

        if (isUnique())
        {
            sbReturn.append("UNIQUE");
        }
        else
        {
            sbReturn.append("NOT UNIQUE");
        }
        sbReturn.append(", ");

        short sType = getIndexType();

        switch (sType)
        {
            case DatabaseMetaData.tableIndexClustered:
                sbReturn.append("CLUSTERED");
                break;

            case DatabaseMetaData.tableIndexHashed:
                sbReturn.append("HASHED");
                break;

            case DatabaseMetaData.tableIndexStatistic:
                sbReturn.append("STATISTIC");
                break;

            case DatabaseMetaData.tableIndexOther:
                sbReturn.append("OTHER");
                break;

            default:
                sbReturn.append("TYPE_UNKNOWN");
                break;
        }
        sbReturn.append("): ");

        boolean bFirst = true;

        for (Iterator<String> iColumns = hmIndexColumns.keySet().iterator();
                 iColumns.hasNext();)
        {
            String sKey = iColumns.next();
            SQLIndexColumn sicColumn = hmIndexColumns.get(sKey);

            if (bFirst == true)
            {
                bFirst = false;
            }
            else
            {
                sbReturn.append(" ,");
            }
            sbReturn.append(sicColumn.toString());
        }
        return sbReturn.toString();
    }
    
    /**
     * This method returns the columns sorted by position number.
     *
     * @return The sorted columns.
     */
    public SQLIndexColumn[] getSortedColumns()
    {
    	SQLIndexColumn[] afkcReturn = new SQLIndexColumn[hmIndexColumns.size()];

        for (Iterator<SQLIndexColumn> iColumns = hmIndexColumns.values().iterator();
                 iColumns.hasNext();)
        {
        	SQLIndexColumn sfkcColumn = iColumns.next();
            afkcReturn[(sfkcColumn.getPosition() - 1)] = sfkcColumn;
        }

        return afkcReturn;
    }
}
