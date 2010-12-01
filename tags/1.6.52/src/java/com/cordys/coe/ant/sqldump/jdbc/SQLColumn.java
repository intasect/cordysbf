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
import java.sql.SQLException;

/**
 * Contains the metadat for a certain columm.
 */
public class SQLColumn
{
    /**
     * Holds the catalog.
     */
    private String sCatalog;
    /**
     * Holds the columnname.
     */
    private String sColumnName;
    /**
     * Holds the default value.
     */
    private String sDefaultValue;
    /**
     * Holds the remarks.
     */
    private String sRemarks;
    /**
     * Holds the schema.
     */
    private String sSchema;
    /**
     * Holds the tablename.
     */
    private String sTableName;
    /**
     * Holds the type name.
     */
    private String sTypeName;
    /**
     * Holds whether or not the column is nullable.
     */
    private boolean bNullable;
    /**
     * Holds the character length.
     */
    private int iCharLength;
    /**
     * Holds the column size.
     */
    private int iColumnSize;
    /**
     * Holds the decimal digits.
     */
    private int iDecimalDigets;
    /**
     * Holds the ordinal position.
     */
    private int iOrdinalPosition;
    /**
     * Holds the radix.
     */
    private int iRadix;
    /**
     * Holds the datatype.
     */
    private short sDataType;

/**
     * Creates a new SQLColumn object.
     *
     * @param sCatalog The catalog
     * @param sSchema The schema.
     * @param sTableName The tablename.
     * @param sColumnName The column name.
     * @param sDataType The datatype.
     * @param sTypeName The typename.
     * @param iColumnSize The columnsize.
     * @param iDecimalDigets The decimal digits.
     * @param iRadix The radix.
     * @param sRemarks The remarks.
     * @param sDefaultValue The default value.
     * @param iCharLength The character length.
     * @param iOrdinalPosition The ordinal position.
     * @param bNullable Whether or not the column is nullable.
     */
    private SQLColumn(String sCatalog, String sSchema, String sTableName,
                      String sColumnName, short sDataType, String sTypeName,
                      int iColumnSize, int iDecimalDigets, int iRadix,
                      String sRemarks, String sDefaultValue, int iCharLength,
                      int iOrdinalPosition, boolean bNullable)
    {
        this.sCatalog = sCatalog;
        this.sSchema = sSchema;
        this.sTableName = sTableName;
        this.sColumnName = sColumnName;
        this.sDataType = sDataType;
        this.sTypeName = sTypeName;
        this.iColumnSize = iColumnSize;
        this.iDecimalDigets = iDecimalDigets;
        this.iRadix = iRadix;
        this.sRemarks = sRemarks;
        this.sDefaultValue = sDefaultValue;
        this.iCharLength = iCharLength;
        this.iOrdinalPosition = iOrdinalPosition;
        this.bNullable = bNullable;
        
        //If a field is defined as numeric AND is identity, the SQL is generated incorrectly.
        if (this.sTypeName.startsWith("numeric()"))
        {
        	this.sTypeName = this.sTypeName.replaceFirst("numeric\\(\\)", "numeric");	
        }
    }

    /**
     * This method returns the instance of SQLColumn based on the data
     * in the resultset.
     *
     * @param dmdMetaData The metadataobject.
     * @param thmValues The hashmap containing all the metainformation for this
     *        column.
     *
     * @return The instance on SQLColumn.
     */
    public static SQLColumn getInstance(DatabaseMetaData dmdMetaData,
                                        TypeHashMap<String, String> thmValues)
                                 throws SQLException
    {
        String sNullable = thmValues.getString("IS_NULLABLE");
        boolean bNullable = false;

        if ((sNullable != null) && (sNullable.length() > 0) &&
                sNullable.equals("YES"))
        {
            bNullable = true;
        }

        SQLColumn mcReturn = new SQLColumn(thmValues.getString("TABLE_CAT"),
                                           thmValues.getString("TABLE_SCHEM"),
                                           thmValues.getString("TABLE_NAME"),
                                           thmValues.getString("COLUMN_NAME"),
                                           thmValues.getShort("DATA_TYPE"),
                                           thmValues.getString("TYPE_NAME"),
                                           thmValues.getInt("COLUMN_SIZE"),
                                           thmValues.getInt("DECIMAL_DIGITS"),
                                           thmValues.getInt("NUM_PREC_RADIX"),
                                           thmValues.getString("REMARKS"),
                                           thmValues.getString("COLUMN_DEF"),
                                           thmValues.getInt("CHAR_OCTET_LENGTH"),
                                           thmValues.getInt("ORDINAL_POSITION"),
                                           bNullable);
        return mcReturn;
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
     * This method gets the characterlength.
     *
     * @return The characterlength.
     */
    public int getCharLength()
    {
        return iCharLength;
    }

    /**
     * This method gets the columnname.
     *
     * @return The columnname.
     */
    public String getColumnName()
    {
        return sColumnName;
    }

    /**
     * This method gets the columnsize.
     *
     * @return The columnsize.
     */
    public int getColumnSize()
    {
        return iColumnSize;
    }

    /**
     * This method gets the datatype.
     *
     * @return The datatype.
     */
    public short getDataType()
    {
        return sDataType;
    }

    /**
     * This method gets the decimal digits.
     *
     * @return The decimal digits.
     */
    public int getDecimalDigets()
    {
        return iDecimalDigets;
    }

    /**
     * This method gets the default value.
     *
     * @return The default value.
     */
    public String getDefaultValue()
    {
        return sDefaultValue;
    }

    /**
     * This method gets the ordinal position.
     *
     * @return The ordinal position.
     */
    public int getOrdinalPosition()
    {
        return iOrdinalPosition;
    }

    /**
     * This method gets the radix.
     *
     * @return The radix.
     */
    public int getRadix()
    {
        return iRadix;
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
     * This method gets the tablename.
     *
     * @return The tablename.
     */
    public String getTableName()
    {
        return sTableName;
    }

    /**
     * This method gets the typename.
     *
     * @return The typename.
     */
    public String getTypeName()
    {
        return sTypeName;
    }

    /**
     * This method gets whether or not the column is nullable.
     *
     * @return Whether or not the column is nullable.
     */
    public boolean isNullable()
    {
        return bNullable;
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
     * This method sets the characterlength.
     *
     * @param charLength The characterlength.
     */
    public void setCharLength(int charLength)
    {
        iCharLength = charLength;
    }

    /**
     * This method sets the columnname.
     *
     * @param columnName The columnname.
     */
    public void setColumnName(String columnName)
    {
        sColumnName = columnName;
    }

    /**
     * This method sets the columnsize.
     *
     * @param columnSize The columnsize.
     */
    public void setColumnSize(int columnSize)
    {
        iColumnSize = columnSize;
    }

    /**
     * This method sets the datatype.
     *
     * @param dataType The datatype.
     */
    public void setDataType(short dataType)
    {
        sDataType = dataType;
    }

    /**
     * This method sets the decimal digits.
     *
     * @param decimalDigets The decimal digits.
     */
    public void setDecimalDigets(int decimalDigets)
    {
        iDecimalDigets = decimalDigets;
    }

    /**
     * This method sets the default value.
     *
     * @param defaultValue The default value.
     */
    public void setDefaultValue(String defaultValue)
    {
        sDefaultValue = defaultValue;
    }

    /**
     * This method sets whether or not the column is nullable.
     *
     * @param nullable Whether or not the column is nullable.
     */
    public void setNullable(boolean nullable)
    {
        bNullable = nullable;
    }

    /**
     * This method sets the ordinal position.
     *
     * @param ordinalPosition The ordinal position.
     */
    public void setOrdinalPosition(int ordinalPosition)
    {
        iOrdinalPosition = ordinalPosition;
    }

    /**
     * This method sets the radix.
     *
     * @param radix The radix.
     */
    public void setRadix(int radix)
    {
        iRadix = radix;
    }

    /**
     * This method sets the remarks.
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
     * This method sets the tablename.
     *
     * @param tableName The tablename.
     */
    public void setTableName(String tableName)
    {
        sTableName = tableName;
    }

    /**
     * This method sets the typename.
     *
     * @param typeName The typename.
     */
    public void setTypeName(String typeName)
    {
        sTypeName = typeName;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuffer sbBuffer = new StringBuffer();
        sbBuffer.append(getColumnName());
        sbBuffer.append("(");
        sbBuffer.append(String.valueOf(getColumnSize()));
        sbBuffer.append("), ");

        if (!isNullable())
        {
            sbBuffer.append("NOT ");
        }
        sbBuffer.append("NULL");
        sbBuffer.append(", ");
        sbBuffer.append(getDataType());
        sbBuffer.append(", ");
        sbBuffer.append(getDecimalDigets());
        sbBuffer.append(", ");
        sbBuffer.append(getDefaultValue());
        sbBuffer.append(", ");
        sbBuffer.append(getOrdinalPosition());
        sbBuffer.append(", ");
        sbBuffer.append(getRadix());
        sbBuffer.append(", ");
        sbBuffer.append(getRemarks());
        sbBuffer.append(", ");
        sbBuffer.append(getTypeName());

        return sbBuffer.toString();
    }
}
