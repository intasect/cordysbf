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
package com.cordys.coe.ant.bf;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.cordys.coe.util.general.Util;
import com.cordys.tools.ant.util.GeneralUtils;

/**
 * This task can be used to print the version numbers. The version numbers
 * shold be in a propertyfile called versions.properties in the
 * com.cordys.coe.ant.bf package.
 *
 * @author pgussow
 */
public class VersionTask extends Task
{
    /**
     * Holds the name of the propertyfile that contains all the versions.
     */
    private static final String VERSIONS_PROPERTIES = "versions.properties";

    /**
     * This method executes the task. It reads the property file as displays
     * the version information. Then it stores the versionnumbers in the
     * passed on variable names.
     */
    public void execute()
                 throws BuildException
    {
        Properties pProps = new Properties();

        try
        {
            //Load the properties file
            pProps.load(VersionTask.class.getResource(VERSIONS_PROPERTIES)
                                         .openStream());

            //Key the different keys
            HashMap<Integer, String> alKeys = getDifferentVersions(pProps);

            //Get the longest name
            int iSize = 0;

            for (Iterator<String> iTemp = alKeys.values().iterator(); iTemp.hasNext();)
            {
                String sName = iTemp.next();
                String sCaption = pProps.getProperty("version." + sName +
                                                     ".label");

                if (sCaption.length() > iSize)
                {
                    iSize = sCaption.length();
                }
            }
            iSize += 2;

            //Print the versions.
            int iCount = 1;

            while (alKeys.containsKey(new Integer(iCount)))
            {
                String sName = alKeys.get(new Integer(iCount));
                String sLabel = pProps.getProperty("version." + sName +
                                                   ".label");
                String sClass = pProps.getProperty("version." + sName +
                                                   ".class");
                String sMethod = pProps.getProperty("version." + sName +
                                                    ".method");
                String sProperty = pProps.getProperty("version." + sName +
                                                      ".property");

                //Get the real version based on the configured class.
                String sVersion = getRealVersion(sClass, sMethod);

                if ((sVersion != null) && (sVersion.length() > 0))
                {
                    log(Util.padLeft(sLabel, " ", iSize) + ": " +
                        sVersion);
                    getProject().setProperty(sProperty, sVersion);
                }

                iCount++;
            }
        }
        catch (Exception e)
        {
            GeneralUtils.handleException("Error getting the versions from the property files",
                                         e, this);
        }
    }

    /**
     * This method returns the different version numbers that are in the
     * properties file. For each version there are 3 values:
     * version._name_.label, version._name_.version and
     * version._name_.property. This method will return only the different
     * names.
     *
     * @param pProps The current properties file.
     *
     * @return The different version names.
     */
    private HashMap<Integer, String> getDifferentVersions(Properties pProps)
    {
        HashMap<Integer, String> hmReturn = new HashMap<Integer, String>(pProps.size());
        Pattern pPattern = Pattern.compile(".+\\.(.+)\\..+");

        for (Iterator<?> iKeys = pProps.keySet().iterator(); iKeys.hasNext();)
        {
            String sKey = (String) iKeys.next();
            Matcher mMatcher = pPattern.matcher(sKey);

            if (mMatcher.matches())
            {
                String sName = mMatcher.group(1);

                if (!hmReturn.containsValue(sName))
                {
                    //Get the sequence
                    int iSequence = Integer.parseInt(pProps.getProperty("version." +
                                                                        sName +
                                                                        ".sequence"));
                    hmReturn.put(new Integer(iSequence), sName);
                }
            }
        }

        return hmReturn;
    }

    /**
     * This method reads the version via the given class and method and returns
     * it.
     *
     * @param sClass The name of the class.
     * @param sMethod The name of the method.
     *
     * @return The response from that method.
     */
    private String getRealVersion(String sClass, String sMethod)
    {
        String sReturn = null;

        try
        {
            Class<?> cClass = Class.forName(sClass);
            Method mMethod = cClass.getDeclaredMethod(sMethod, (Class[]) null);

            if (mMethod != null)
            {
                sReturn = (String) mMethod.invoke(null, (Object[]) null);
            }
        }
        catch (Exception e)
        {
            log("Error getting the version via " + sClass + ":" + sMethod +
                "\n" + e, Project.MSG_DEBUG);
        }

        return sReturn;
    }
}
