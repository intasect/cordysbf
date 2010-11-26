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
 package com.cordys.coe.ant.bf.upgrade.bf150;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Replace;

import com.cordys.coe.ant.bf.upgrade.BFUpgradeException;
import com.cordys.coe.ant.bf.upgrade.IUpgradeScript;
import com.cordys.coe.ant.bf.upgrade.UpgradeManagerTask;

public class UpgradeTo1_5_14 implements IUpgradeScript
{
    /**
     * Holds the current Ant project.
     */
    private Project m_pProject;
    
    /**
     * This method upgrades the project to the proper version.
     *
     * @param pProject The current Ant project.
     * @param umtTask The task that is currently being executed.
     *
     * @throws BFUpgradeException In case of any exceptions.
     *
     * @see com.cordys.coe.ant.bf.upgrade.IUpgradeScript#execute(org.apache.tools.ant.Project,
     *      com.cordys.coe.ant.bf.upgrade.UpgradeManagerTask)
     */
    public void execute(Project pProject, UpgradeManagerTask umtTask)
                 throws BFUpgradeException
    {
        m_pProject = pProject;
        
        replaceChangedPropertyNames();
    }
    
    /**
     * This method returns the version number for which this upgrade
     * script is created.
     *
     * @return The version number for this script.
     */
    public String getVersion()
    {
        return "1.5.14";
    }
    
    /**
     * Replaces all property names that have been changed in project ISV files with
     * the new names.  
     */
    private void replaceChangedPropertyNames()
    {
        Replace reReplace;
        
        // Replace "${src.content.xforms}" with "${src.content.studio.xforms}"
        reReplace = new Replace();
        reReplace.setProject(m_pProject);
        reReplace.setToken("${src.content.xforms}");
        reReplace.setValue("${src.content.studio.xforms}");
        reReplace.setDir(new File(m_pProject.getBaseDir(), "src/content/isv"));
        reReplace.setIncludes("XForms.xml,FileSystem.xml");
        reReplace.setSummary(true);
        reReplace.execute();
    }
}
