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
 * This class is a wrapper around a foreign key column.
 *
 * @author pgussow
 */
public class SQLFKColumn
{
    /**
     * Holds the name of the foreign key table.
     */
    private String sFKColumnName;
    /**
     * Holds the name of the primary key table.
     */
    private String sPKColumnName;
    /**
     * Holds the sequence of the key in the foreign key.
     */
    private short sSequence;

    /**
     * Constructor.
     *
     * @param sSequence The sequence of the key in the foreig key.
     * @param sFKColumnName The name of the foreign key table.
     * @param sPKColumnName The name of the primary key table.
     */
    public SQLFKColumn(short sSequence, String sFKColumnName,
                       String sPKColumnName)
    {
        this.sSequence = sSequence;
        this.sFKColumnName = sFKColumnName;
        this.sPKColumnName = sPKColumnName;
    }

    /**
     * This method sets the foreign key column name.
     *
     * @param sFKColumnName The foreign key column name.
     */
    public void setFKColumnName(String sFKColumnName)
    {
        this.sFKColumnName = sFKColumnName;
    }

    /**
     * This method gets the foreign key column name.
     *
     * @return The foreign key column name.
     */
    public String getFKColumnName()
    {
        return sFKColumnName;
    }

    /**
     * This method returns a new instance of the SQLFKColumn.
     *
     * @param thmColInfo TypeHashMap with all the needed information.
     *
     * @return A new instance of the SQLFKColumn.
     */
    public static SQLFKColumn getInstance(TypeHashMap<String, String> thmColInfo)
    {
        SQLFKColumn skfcReturn = null;

        short sSequence = thmColInfo.getShort("KEY_SEQ");
        String sFKColumnName = thmColInfo.getString("FKCOLUMN_NAME");
        String sPKColumnName = thmColInfo.getString("PKCOLUMN_NAME");

        skfcReturn = new SQLFKColumn(sSequence, sFKColumnName, sPKColumnName);

        return skfcReturn;
    }

    /**
     * This method sets the primary key column name.
     *
     * @param sPKColumnName The primary key column name.
     */
    public void setPKColumnName(String sPKColumnName)
    {
        this.sPKColumnName = sPKColumnName;
    }

    /**
     * This method gets the primary key column name.
     *
     * @return The primary key column name.
     */
    public String getPKColumnName()
    {
        return sPKColumnName;
    }

    /**
     * This method sets the sequence within the foreign key.
     *
     * @param sSequence The sequence within the foreign key.
     */
    public void setSequence(short sSequence)
    {
        this.sSequence = sSequence;
    }

    /**
     * This method gets the sequence within the foreign key.
     *
     * @return The sequence within the foreign key.
     */
    public short getSequence()
    {
        return sSequence;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString()
    {
        return getFKColumnName() + " => " + getPKColumnName() + "(" + getSequence() + ")";
    }
}
