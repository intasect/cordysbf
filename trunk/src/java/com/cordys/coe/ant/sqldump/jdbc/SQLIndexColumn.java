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

/**
 * This class is a wrapper around a column that is part of an index.
 *
 * @author pgussow
 */
public class SQLIndexColumn
{
    /**
     * Identifies the ascending type.
     */
    public static final int TYPE_ASCENDING = 0;
    /**
     * Identifies the descending type.
     */
    public static final int TYPE_DESCENDING = 1;
    /**
     * The name of the column.
     */
    private String sName;
    /**
     * The sorting of the column.
     */
    private int iAscDesc;
    /**
     * The ordinal position of the column.
     */
    private int iPosition;

    /**
     * Constructor.
     *
     * @param sName The name of the column.
     * @param iAscDesc The sorting of the column.
     * @param iPosition The ordinal position of the column.
     */
    public SQLIndexColumn(String sName, int iAscDesc, int iPosition)
    {
        super();
        this.sName = sName;
        this.iAscDesc = iAscDesc;
        this.iPosition = iPosition;
    }

    /**
     * This method sets the sorting of the column.
     *
     * @param ascDesc The sorting-type.
     */
    public void setAscDesc(int ascDesc)
    {
        iAscDesc = ascDesc;
    }

    /**
     * This method gets the sorting of the column.
     *
     * @return The sorting-type.
     */
    public int getAscDesc()
    {
        return iAscDesc;
    }

    /**
     * This method returns a new instance of the index column.
     *
     * @param thmValues Contains the data for this column.
     *
     * @return A new instance of the index column.
     */
    public static SQLIndexColumn getInstance(TypeHashMap<String, String> thmValues)
    {
        SQLIndexColumn sicReturn = null;

        String sName = thmValues.getString("COLUMN_NAME");
        int iPosition = thmValues.getInt("ORDINAL_POSITION");
        String sSorting = thmValues.getString("ASC_OR_DESC");
        int iSorting = TYPE_ASCENDING;

        if ("D".equalsIgnoreCase(sSorting))
        {
            iSorting = TYPE_DESCENDING;
        }

        sicReturn = new SQLIndexColumn(sName, iSorting, iPosition);

        return sicReturn;
    }

    /**
     * This method sets the name of the column.
     *
     * @param name The name of the column.
     */
    public void setName(String name)
    {
        sName = name;
    }

    /**
     * This method gets the name of the column.
     *
     * @return The name of the column.
     */
    public String getName()
    {
        return sName;
    }

    /**
     * This method sets the ordinal position of the column.
     *
     * @param position The ordinal position of the column.
     */
    public void setPosition(int position)
    {
        iPosition = position;
    }

    /**
     * This method gets the ordinal position of the column.
     *
     * @return The ordinal position of the column.
     */
    public int getPosition()
    {
        return iPosition;
    }

    /**
     * This method returns a string representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString()
    {
        return getName() + "(" + getPosition() + ")";
    }
}
