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
package com.cordys.coe.ant.project;

import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * This class returns information about the current version of the build
 * framework.
 *
 * @author pgussow
 */
public class GetBFversionInfoTask extends Task
{
    /**
     * Holds the name of the version class.
     */
    private String classname = "com.cordys.coe.ant.bf.Version";
    /**
     * Holds teh prefix for the properties that will be set.
     */
    private String propertyPrefix = "coe.bf.";

    /**
     * This method executes the task. It loads the version class and
     * executes some of the methods to get the version info. Then that
     * information is written to the properties.
     *
     * @throws BuildException
     *
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute()
                 throws BuildException
    {
        //Load the class.
        Class<?> cClass = null;

        try
        {
            cClass = Class.forName(getClassname());
        }
        catch (ClassNotFoundException e)
        {
            throw new BuildException("Could not find class " + getClassname(), e);
        }

        String sReturn = execStringMethod(cClass, "getFullVersion");
        getProject().setProperty(propertyPrefix + "fullversion", sReturn);
        sReturn = execStringMethod(cClass, "getBuildDate");
        getProject().setProperty(propertyPrefix + "builddate", sReturn);
        sReturn = execStringMethod(cClass, "getVersionInfo");
        getProject().setProperty(propertyPrefix + "versioninfo", sReturn);

        super.execute();
    }

    /**
     * This method gets the classname of the version class.
     *
     * @return The classname of the version class.
     */
    public String getClassname()
    {
        return classname;
    }

    /**
     * This method gets the prefix for the properties that are
     * returned.
     *
     * @return The prefix for the properties that are returned.
     */
    public String getPropertyPrefix()
    {
        return propertyPrefix;
    }

    /**
     * This method sets the classname of the version class.
     *
     * @param classname The classname of the version class.
     */
    public void setClassname(String classname)
    {
        this.classname = classname;
    }

    /**
     * This method sets the prefix for the properties that are
     * returned.
     *
     * @param propertyPrefix The prefix for the properties that are returned.
     */
    public void setPropertyPrefix(String propertyPrefix)
    {
        this.propertyPrefix = propertyPrefix;
    }

    /**
     * This method executes the method which returns a string.
     *
     * @param cClass The class definition.
     * @param sMethod The name of the method.
     *
     * @return The return value.
     */
    private String execStringMethod(Class<?> cClass, String sMethod)
    {
        String sReturn = null;

        try
        {
            Method mMethod = cClass.getDeclaredMethod(sMethod, (Class[]) null);
            Object oResult = mMethod.invoke(null, (Object[]) null);
            sReturn = oResult.toString();
        }
        catch (Exception e)
        {
            log("Method " + sMethod + " not found on class " + getClassname() +
                "\n" + e, Project.MSG_WARN);
        }

        return sReturn;
    }
}
