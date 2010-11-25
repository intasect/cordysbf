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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * This class contains utility methods for handling JDBC resultsets.
 *
 * @author pgussow
 */
public class Util
{
    /**
     * This method reads the resultset with all the tables, so that the
     * resultset can be closed.
     *
     * @param rsResultSet The resultset containing all the records.
     * @param sKeyName The name of the key of the record (Usually the PK of the
     *        resultset).
     *
     * @return A hashmap with all the results.
     */
    public static TypeHashMap<String, TypeHashMap<String, String>> buildHashMapForResultset(ResultSet rsResultSet,
                                                                                            String sKeyName)
        throws SQLException
    {
        return buildHashMapForResultset(rsResultSet, new String[] { sKeyName });
    }

    /**
     * This method reads the resultset with all the tables, so that the
     * resultset can be closed.
     *
     * @param rsResultSet The resultset containing all the records.
     * @param asKeyNames The name of the key of the record (Usually the PK of
     *        the resultset).
     *
     * @return A hashmap with all the results.
     */
    public static TypeHashMap<String, TypeHashMap<String, String>> buildHashMapForResultset(ResultSet rsResultSet,
                                                                                            String[] asKeyNames)
        throws SQLException
    {
        TypeHashMap<String, TypeHashMap<String, String>> hmReturn = new TypeHashMap<String, TypeHashMap<String, String>>();

        //Get all the columns.
        ResultSetMetaData rsmd = rsResultSet.getMetaData();
        ArrayList<String> hmColumns = new ArrayList<String>();
        int iColCount = rsmd.getColumnCount();

        for (int iCount = 1; iCount <= iColCount; iCount++)
        {
            hmColumns.add(rsmd.getColumnName(iCount));
        }

        while (rsResultSet.next())
        {
            TypeHashMap<String, String> hmValues = new TypeHashMap<String, String>();
            StringBuffer sbRowKey = new StringBuffer("");

            //Get the columns and their values.
            for (Iterator<String> iColumns = hmColumns.iterator();
                     iColumns.hasNext();)
            {
                String sColumnName = iColumns.next();

                String sData = "";

                try
                {
                    sData = rsResultSet.getString(sColumnName);
                }
                catch (Exception e)
                {
                    //Ignore it.
                    e.printStackTrace();
                }
                hmValues.put(sColumnName, sData);
            }

            //Build up the key for this record.
            for (int iCount = 0; iCount < asKeyNames.length; iCount++)
            {
                Object oValue = hmValues.get(asKeyNames[iCount]);
                String sValue = "";

                if (oValue != null)
                {
                    sValue = oValue.toString();
                }
                sbRowKey.append(sValue);

                if (iCount < (asKeyNames.length - 1))
                {
                    sbRowKey.append(":");
                }
            }

            hmReturn.put(sbRowKey.toString(), hmValues);
        }

        return hmReturn;
    }

    /**
     * This method will returns a HashMap with all the values in the
     * TypeHashMap grouped into a new HashMap.<pre>
     * - INDEXNAME (HashMap, indexed by sGroupByKey)
     *   - COLUMNNAME (HashMap, indexed by sSubKey)
     *            - All values (TypeHashMap)  - COLUMNNAME
     *            - All values</pre>
     *
     * @param thmIndices The hashmap to group.
     * @param sGroupByKey The name of the value to group by.
     * @param sSubKey The name of the field to use as a replacement for the old
     *        key.
     *
     * @return A grouped Hashmap.
     */
    public static LinkedHashMap<String, LinkedHashMap<String, TypeHashMap<String, String>>> groupByField(TypeHashMap<String, TypeHashMap<String, String>> thmIndices,
                                                                                                         String sGroupByKey,
                                                                                                         String sSubKey)
    {
        LinkedHashMap<String, LinkedHashMap<String, TypeHashMap<String, String>>> hmReturn = new LinkedHashMap<String, LinkedHashMap<String, TypeHashMap<String, String>>>();

        for (Iterator<String> iValues = thmIndices.keySet().iterator();
                 iValues.hasNext();)
        {
            String sKey = iValues.next();
            TypeHashMap<String, String> thmColValues = thmIndices.get(sKey);

            String sGroupByValue = thmColValues.getString(sGroupByKey);
            LinkedHashMap<String, TypeHashMap<String, String>> hmColumns = null;

            if (!hmReturn.containsKey(sGroupByValue))
            {
                //No column for this index yet, so add it.
                hmColumns = new LinkedHashMap<String, TypeHashMap<String, String>>();
                hmReturn.put(sGroupByValue, hmColumns);
            }
            else
            {
                hmColumns = hmReturn.get(sGroupByValue);
            }
            hmColumns.put(thmColValues.getString(sSubKey), thmColValues);
        }

        return hmReturn;
    }
}
