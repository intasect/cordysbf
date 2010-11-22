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
package com.cordys.coe.test;

import java.sql.Connection;
import java.sql.DriverManager;

import com.cordys.coe.ant.sqldump.jdbc.SQLDB;
import com.cordys.coe.ant.sqldump.sqlserver.SQLServerGenerator;

/**
 * Test class.
 */
public class TestClass
{
    /**
     * DOCUMENTME
     *
     * @param args DOCUMENTME
     */
    public static void main(String[] args)
    {
        try
        {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");

//*
               Connection cConnection = DriverManager.getConnection("jdbc:odbc:Driver={SQL Server};Server=cnd0626;Database=tempdb",
                                                                    "sa", "sa");
/*/
            Connection cConnection = DriverManager.getConnection("jdbc:odbc:Driver={SQL Server};Server=srv-nl-insynq0;Database=iSales_ConversieTest",
                                                                 "insynq1_appl",
                                                                 "SimpleAccess");
//*/            
            SQLDB sdbDB = new SQLDB(cConnection);
            System.out.println(sdbDB.toString());

            SQLServerGenerator ssg = new SQLServerGenerator(sdbDB);

            ssg.scriptDatabase(true, true, true, true, true, System.out);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
