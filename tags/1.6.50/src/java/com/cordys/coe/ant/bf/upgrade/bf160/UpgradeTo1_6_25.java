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
 package com.cordys.coe.ant.bf.upgrade.bf160;

import com.cordys.coe.ant.bf.upgrade.BFUpgradeException;
import com.cordys.coe.ant.bf.upgrade.UpgradeManagerTask;
import com.cordys.coe.ant.bf.upgrade.base.UpdateScriptBase;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;

/**
 * This class upgrades the project to 1.6.25. It will make sure the support for XReports is added to
 * the project.
 *
 * @author  pgussow
 */
public class UpgradeTo1_6_25 extends UpdateScriptBase
{
    /**
     * This method upgrades the project to the proper version.
     *
     * @param   pProject  The current Ant project.
     * @param   umtTask   The task that is currently being executed.
     *
     * @throws  BFUpgradeException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ant.bf.upgrade.IUpgradeScript#execute(org.apache.tools.ant.Project,
     *          com.cordys.coe.ant.bf.upgrade.UpgradeManagerTask)
     */
    public void execute(Project pProject, UpgradeManagerTask umtTask)
                 throws BFUpgradeException
    {
        m_pProject = pProject;

        // Create the source folder for XReports.
        new File(m_pProject.getBaseDir(), "./src/content/xreports").mkdirs();
        
        //Copy the XReports ISV file
        Copy cCopy = new Copy();
        cCopy.setTaskName("copy-isv-xreports.xml");
        cCopy.setProject(m_pProject);
        cCopy.setFile(new File(m_pProject.getBaseDir(),
                               "./sdk/build/new/isvfiles/XReports.xml"));
        cCopy.setTodir(new File(m_pProject.getBaseDir(), "./src/content/isv"));
        cCopy.setOverwrite(true);
        cCopy.execute();
        
        copyBuildBat();
    }
}
