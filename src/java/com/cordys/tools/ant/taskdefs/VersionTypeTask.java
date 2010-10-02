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
package com.cordys.tools.ant.taskdefs;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This task checks if the version number passed on is for a development
 * release or for a stable release.
 *
 * @author pgussow
 */
public class VersionTypeTask extends Task
{
    /**
     * Holds the name of the property that will be set to true if the
     * passed on version is a development version.
     */
    private String propertyDevelopment = "version.type.development";
    /**
     * Holds the name of the property that will be set to true if the
     * passed on version is a stable version.
     */
    private String propertyStable = "version.type.stable";
    /**
     * Holds teh version string to check
     */
    private String version;

    /**
     * This method executes the taks
     *
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute()
                 throws BuildException
    {
        if ((version == null) || (version.length() == 0) ||
                (version.indexOf('.') < 0))
        {
            throw new BuildException("Invalid version. Version must be filled and contain at least 1 '.'");
        }

        String[] aNumbers = version.split("\\.");

        if ((aNumbers == null) || (aNumbers.length <= 1) ||
                (aNumbers[1].length() == 0))
        {
            throw new BuildException("Invalid version. Version must be filled and of format #.# or #.#.#");
        }

        int iNumber = 0;

        try
        {
            iNumber = Integer.parseInt(aNumbers[1]);
        }
        catch (NumberFormatException nfe)
        {
            throw new BuildException("Invalid version number");
        }

        boolean bOdd = false;
        boolean bEven = false;

        if ((iNumber % 2) == 0)
        {
            bEven = true;
        }
        else
        {
            bOdd = true;
        }

        getProject().setProperty(getPropertyDevelopment(), String.valueOf(bOdd));
        getProject().setProperty(getPropertyStable(), String.valueOf(bEven));
    }

    /**
     * This method gets the name of the property that will be set to
     * true if the passed on version is a development version..
     *
     * @return The name of the property that will be set to true if the passed
     *         on version is a development version..
     */
    public String getPropertyDevelopment()
    {
        return propertyDevelopment;
    }

    /**
     * This method gets the name of the property that will be set to
     * true if the passed on version is a stable version..
     *
     * @return The name of the property that will be set to true if the passed
     *         on version is a stable version..
     */
    public String getPropertyStable()
    {
        return propertyStable;
    }

    /**
     * This method gets the version to check.
     *
     * @return The version to check.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * This method sets the name of the property that will be set to
     * true if the passed on version is a development version..
     *
     * @param propertyDevelopment The name of the property that will be set to
     *        true if the passed on version is a development version..
     */
    public void setPropertyDevelopment(String propertyDevelopment)
    {
        this.propertyDevelopment = propertyDevelopment;
    }

    /**
     * This method sets the name of the property that will be set to
     * true if the passed on version is a stable version..
     *
     * @param propertyStable The name of the property that will be set to true
     *        if the passed on version is a stable version..
     */
    public void setPropertyStable(String propertyStable)
    {
        this.propertyStable = propertyStable;
    }

    /**
     * This method sets the version to check.
     *
     * @param version The version to check.
     */
    public void setVersion(String version)
    {
        this.version = version;
    }
}
