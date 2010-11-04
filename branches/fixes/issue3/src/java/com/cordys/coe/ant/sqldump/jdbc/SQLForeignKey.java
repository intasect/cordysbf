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

/**
 * This class is a wrapper around a foreign key relation.
 *
 * @author pgussow
 */
public class SQLForeignKey
{
    /**
     * Holds the columns that are used in this foreign key.
     */
    private LinkedHashMap<String, SQLFKColumn> hmFKColumns;
    /**
     * Holds the name of the foreign key.
     */
    private String sFKName;
    /**
     * Holds the name of the foreign key table.
     */
    private String sFKTable;
    /**
     * Holds the name of the primary key this key references to.
     */
    private String sPKName;
    /**
     * Holds the name of the table to which this key references.
     */
    private String sPKTable;
    /**
     * Holds whether or not to to a cascaded delete.
     */
    private boolean bDeleteCascade;
    /**
     * Holds whether or not to to a cascaded update.
     */
    private boolean bUpdateCascade;
    /**
     * Holds whether or not the evaluation of the foreign key constraints can
     * be deferred until commit.
     */
    private short sDeferability;

    /**
     * Constructor.
     *
     * @param sFKName The name of the foreign key.
     * @param sFKTable The name of the foreign key table.
     * @param sPKName The name of the primary key this key references to.
     * @param sPKTable The name of the table to which this key references.
     * @param bUpdateCascade Whether or not to to a cascaded update.
     * @param bDeleteCascade Whether or not to to a cascaded delete.
     * @param sDeferability Whether or not the evaluation of the foreign key
     *        constraints can be deferred until commit.
     */
    public SQLForeignKey(String sFKName, String sFKTable, String sPKName,
                         String sPKTable, boolean bUpdateCascade,
                         boolean bDeleteCascade, short sDeferability)
    {
        this.sFKName = sFKName;
        this.sFKTable = sFKTable;
        this.sPKName = sPKName;
        this.sPKTable = sPKTable;
        this.bUpdateCascade = bUpdateCascade;
        this.bDeleteCascade = bDeleteCascade;
        this.sDeferability = sDeferability;
        hmFKColumns = new LinkedHashMap<String, SQLFKColumn>();
    }

    /**
     * This method sets the deferability.
     *
     * @param sDeferability The deferability.
     */
    public void setDeferability(short sDeferability)
    {
        this.sDeferability = sDeferability;
    }

    /**
     * This method gets the deferability.
     *
     * @return The deferability.
     */
    public short getDeferability()
    {
        return sDeferability;
    }

    /**
     * This method sets wether or not to do a cascaded delete.
     *
     * @param bDeleteCascade Whether or not to do a cascaded delete.
     */
    public void setDeleteCascade(boolean bDeleteCascade)
    {
        this.bDeleteCascade = bDeleteCascade;
    }

    /**
     * This method gets whether or not to do a cascaded delete.
     *
     * @return Whether or not to do a cascaded delete.
     */
    public boolean getDeleteCascade()
    {
        return bDeleteCascade;
    }

    /**
     * This method sets the name of the foreign key table.
     *
     * @param sFKTable The name of the foreign key table.
     */
    public void setFKTable(String sFKTable)
    {
        this.sFKTable = sFKTable;
    }

    /**
     * This method gets the name of the foreign key table.
     *
     * @return The name of the foreign key table.
     */
    public String getFKTable()
    {
        return sFKTable;
    }

    /**
     * This method returns a new instance of this object that wraps the foreign
     * key data that is passed on.
     *
     * @param stTable The table object.
     * @param sFKName The name of the foreign key.
     * @param hmColumns HashMap containing all the column-information.
     *
     * @return A new instance of the SQLForeignKey object.
     */
    public static SQLForeignKey getInstance(SQLTable stTable, String sFKName,
            LinkedHashMap<String, TypeHashMap<String, String>> hmColumns)
    {
        SQLForeignKey sfkReturn = null;
        TypeHashMap<String, String> thmTemp = hmColumns.get(hmColumns.keySet()
                                                                   .iterator()
                                                                   .next());
        boolean bUpdateCascade = thmTemp.getBoolean("UPDATE_RULE");
        boolean bDeleteCascade = thmTemp.getBoolean("DELETE_RULE");
        short sDeferability = thmTemp.getShort("DEFERRABILITY");
        String sFKTable = thmTemp.getString("FKTABLE_NAME");
        String sPKTable = thmTemp.getString("PKTABLE_NAME");
        String sPKName = thmTemp.getString("PK_NAME");

        sfkReturn = new SQLForeignKey(sFKName, sFKTable, sPKName, sPKTable,
                                      bUpdateCascade, bDeleteCascade,
                                      sDeferability);

        //Create all the columns.
        for (Iterator<String> iColumns = hmColumns.keySet().iterator();
                 iColumns.hasNext();)
        {
            String sKey = iColumns.next();
            TypeHashMap<String, String> thmColInfo = hmColumns.get(sKey);

            SQLFKColumn sfkcColumn = SQLFKColumn.getInstance(thmColInfo);
            sfkReturn.addColumn(sfkcColumn);
        }

        return sfkReturn;
    }

    /**
     * This method sets the name of the foreign key.
     *
     * @param sFKName The name of the foreign key.
     */
    public void setFKName(String sFKName)
    {
        this.sFKName = sFKName;
    }

    /**
     * This method gets the name of the foreign key.
     *
     * @return The name of the foreign key.
     */
    public String getFKName()
    {
        return sFKName;
    }

    /**
     * This method sets the name of the primary key it references.
     *
     * @param sPKName The name of the primary key it references.
     */
    public void setPKName(String sPKName)
    {
        this.sPKName = sPKName;
    }

    /**
     * This method gets the name of the primary key it references.
     *
     * @return The name of the primary key it references.
     */
    public String getPKName()
    {
        return sPKName;
    }

    /**
     * This method sets the name of the PK-table.
     *
     * @param sPKTable The name of the PK-table.
     */
    public void setPKTable(String sPKTable)
    {
        this.sPKTable = sPKTable;
    }

    /**
     * This method gets the name of the PK-table.
     *
     * @return The name of the PK-table.
     */
    public String getPKTable()
    {
        return sPKTable;
    }

    /**
     * This method sets whether or not to to a cascaded update.
     *
     * @param bUpdateCascade whether or not to to a cascaded update.
     */
    public void setUpdateCascade(boolean bUpdateCascade)
    {
        this.bUpdateCascade = bUpdateCascade;
    }

    /**
     * This method gets whether or not to to a cascaded update.
     *
     * @return whether or not to to a cascaded update.
     */
    public boolean getUpdateCascade()
    {
        return bUpdateCascade;
    }

    /**
     * This method adds the column to the index columns.
     *
     * @param sfkcColumn The column to add.
     */
    public void addColumn(SQLFKColumn sfkcColumn)
    {
        if (!hmFKColumns.containsKey(sfkcColumn.getFKColumnName()))
        {
            hmFKColumns.put(sfkcColumn.getFKColumnName(), sfkcColumn);
        }
    }

    /**
     * Returns a string representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString()
    {
        StringBuffer sbReturn = new StringBuffer("");
        sbReturn.append("Foreign Key ");
        sbReturn.append(getFKName());
        sbReturn.append(": ");

        SQLFKColumn[] afkcColumns = getSortedColumns();

        for (int iCount = 0; iCount < afkcColumns.length; iCount++)
        {
            sbReturn.append(afkcColumns[iCount].toString());

            if (iCount < (afkcColumns.length - 1))
            {
                sbReturn.append(", ");
            }
        }

        return sbReturn.toString();
    }

    /**
     * This method returns the columns sorted by sequence number.
     *
     * @return The sorted columns.
     */
    public SQLFKColumn[] getSortedColumns()
    {
        SQLFKColumn[] afkcReturn = new SQLFKColumn[hmFKColumns.size()];

        for (Iterator<SQLFKColumn> iColumns = hmFKColumns.values().iterator();
                 iColumns.hasNext();)
        {
            SQLFKColumn sfkcColumn = iColumns.next();
            afkcReturn[(sfkcColumn.getSequence() - 1)] = sfkcColumn;
        }

        return afkcReturn;
    }
}
