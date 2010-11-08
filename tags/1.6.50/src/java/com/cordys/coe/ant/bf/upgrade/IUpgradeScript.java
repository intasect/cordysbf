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
package com.cordys.coe.ant.bf.upgrade;

import org.apache.tools.ant.Project;

/**
 * This interface needs to be implemented by all upgrade scripts. Be aware of the folowing:
 * The update-script should have as little dependencies as possible to the other classes.
 * This is because they might change during versions.
 * 
 * Let's take this example: you have a project which is currently configured for 1.6.0. 
 * Now you are already at 1.6.5. But for going to 1.6.3 you needed something as well.
 * When building 1.6.5 you should make sure that the 1.6.3 upgrade script still works, because the
 * sequence of upgrading will be 1.6.0 --> 1.6.3 --> 1.6.5
 * 
 * @author pgussow
 */
public interface IUpgradeScript
{
    /**
     * This method upgrades the project to the proper version.
     *
     * @param pProject The current Ant project.
     * @param umtTask The task that is currently being executed.
     *
     * @throws BFUpgradeException In case of any exceptions.
     */
    public void execute(Project pProject, UpgradeManagerTask umtTask)
                 throws BFUpgradeException;

    /**
     * This method returns the version number for which this upgrade
     * script is created.
     *
     * @return The version number for this script.
     */
    public String getVersion();
}
