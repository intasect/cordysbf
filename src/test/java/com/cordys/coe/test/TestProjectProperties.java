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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.cordys.coe.ant.bf.properties.ProjectProperties;
import com.cordys.coe.ant.bf.properties.ProjectProperties.PropertyLevel;

public class TestProjectProperties
{
    /**
     * Main method.
     *
     * @param saArguments The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            String sProjectFolder = "C:/installed/workspaces/runtime-workbench-workspace/Test151_nosvn";
//            String sProjectFolder = "C:/installed/workspaces/runtime-workbench-workspace/SVN_CoELibrary_main_0986";
            ProjectProperties pp = new ProjectProperties(sProjectFolder);
            
            String sPropName = "debug";
            System.out.println("Old: " + pp.getProperty(sPropName));
            pp.remove(sPropName, ProjectProperties.PropertyLevel.USER);
            pp.setProperty("phillip", "true1", ProjectProperties.PropertyLevel.USER);
            System.out.println("New: " + pp.getProperty(sPropName));
            
            pp.saveConfiguration();
            
            HashMap<PropertyLevel, List<String>> hmTemp = pp.getNonStandardProperties();
            for (Iterator<PropertyLevel> iLevels = hmTemp.keySet().iterator(); iLevels.hasNext();)
            {
                PropertyLevel plTemp = (PropertyLevel) iLevels.next();
                List<String> alTemp = hmTemp.get(plTemp);
                if (alTemp != null)
                {
                    System.out.println("Nonstandard at " + plTemp + " level:");
                    for (String sTemp : alTemp)
                    {
                        System.out.println(sTemp);
                    }
                }
            }
            
//            PropertiesConfiguration pc = new PropertiesConfiguration(sProjectFolder + "/user.properties");
//            pc.setProperty("ldap.password", "bla");
//            pc.save();
            
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
