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

import org.apache.commons.configuration.ConfigurationException;

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;
import com.cordys.coe.ant.bf.properties.ProjectProperties;
import com.cordys.coe.ant.bf.properties.ProjectProperties.PropertyLevel;
import com.cordys.coe.ant.bf.upgrade.BFUpgradeException;

public class TestCustomProp
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
            String sFilename = "C:/installed/workspaces/runtime-workbench-workspace/SVN_CoELibrary_main_0986";
            
            HashMap<String, String> hmProperties = new HashMap<String, String>();
            hmProperties.put("debug", BuildFrameworkProperty.JAVAC_DEBUG.getName());
            hmProperties.put("deprecation", BuildFrameworkProperty.JAVAC_DEPRECATION.getName());
            hmProperties.put("optimize", BuildFrameworkProperty.JAVAC_OPTIMIZE.getName());
            hmProperties.put("jvm.target.version", BuildFrameworkProperty.JAVAC_SOURCE_VERSION.getName());

            renameProperties(hmProperties, sFilename);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * This method renames the properties with name 'key' to 'value'.
     *  
     * @param hmProperties The properties to rename.
     */
    private static void renameProperties(HashMap<String, String> hmProperties, String sFilename) throws BFUpgradeException
    {
        try
        {
            ProjectProperties ppProps = new ProjectProperties(sFilename);
            
            PropertyLevel[] aplLevels = new ProjectProperties.PropertyLevel[]{PropertyLevel.USER, PropertyLevel.PROJECT};
            
            
            for (Iterator<String> iKeys = hmProperties.keySet().iterator(); iKeys.hasNext();)
            {
                String sOldName = (String) iKeys.next();
                String sNewName = hmProperties.get(sOldName);
                
                //We need to check both USER and PROJECT level.
                for (int iCount = 0; iCount < aplLevels.length; iCount++)
                {
                    ppProps.renameProperty(sOldName, sNewName, aplLevels[iCount]);
                }
            }
            
            ppProps.saveConfiguration();
        }
        catch (ConfigurationException e)
        {
            throw new BFUpgradeException("Error renaming the properties", e);
        }
    }
}
