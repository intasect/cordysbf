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

import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This class checks the project to see if all the information is provided
 * to be able to call the SVN integration properly.
 *
 * @author pgussow
 */
public class CheckSVNPropsTask extends Task
{
    /**
     * Identifies the base URL of the project within subversion.
     */
    public static final String SVN_PROJECT = "svn.project";
    /**
     * Identifies the name of the main(trunk) folder within subversion.
     */
    public static final String SVN_MAIN = "svn.main";
    /**
     * Identifies the name of the branches folder within subversion
     */
    public static final String SVN_BRANCHES = "svn.branches";

    /**
     * This method is called when the task is executed. It will check
     * if the following properties are available: -
     *
     * @throws BuildException
     *
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute()
                 throws BuildException
    {
        Hashtable<?, ?> pProperties = getProject().getProperties();

        if (!pProperties.containsKey(SVN_PROJECT))
        {
            throw new BuildException("Missing property: " + SVN_PROJECT);
        }

        if (!pProperties.containsKey(SVN_MAIN))
        {
            throw new BuildException("Missing property: " + SVN_MAIN);
        }

        if (!pProperties.containsKey(SVN_BRANCHES))
        {
            throw new BuildException("Missing property: " + SVN_BRANCHES);
        }

        super.execute();
    }
}
