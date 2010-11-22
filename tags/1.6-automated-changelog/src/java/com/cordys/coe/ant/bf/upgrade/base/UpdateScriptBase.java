/**
 * Copyright 2007 Cordys R&D B.V. 
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
package com.cordys.coe.ant.bf.upgrade.base;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

import com.cordys.coe.ant.bf.upgrade.IUpgradeScript;

/**
 * A base class for project update scripts. This contains
 * method for most common operations.
 *
 * @author mpoyhone
 */
public abstract class UpdateScriptBase implements IUpgradeScript
{
    /**
     * Pattern to extract version from the class name.
     */
    protected static final Pattern pVersionExtractPattern = Pattern.compile("^UpgradeTo([0-9_]+)$");
    /**
     * Holds the current Ant project.
     */
    protected Project m_pProject;
    
    /**
     * This method returns the version number for which this upgrade
     * script is created.
     *
     * @return The version number for this script.
     */
    public String getVersion()
    {
        String sClassName = getClass().getSimpleName();
        Matcher mMatcher = pVersionExtractPattern.matcher(sClassName);
        
        if (! mMatcher.matches()) {
            throw new IllegalStateException("Unable to extract version from class name: " + sClassName);
        }
        
        return mMatcher.group(1).replace('_', '.');
    }

    /**
     * This method copies the build.xml file from the base to the
     * project root.
     */
    protected void copyBuildXML()
    {
        Copy cCopy = new Copy();
        cCopy.setTaskName("copy-build.xml");
        cCopy.setProject(m_pProject);
        cCopy.setFile(new File(m_pProject.getBaseDir(),
                               "./sdk/build/new/base/build.xml"));
        cCopy.setTodir(m_pProject.getBaseDir());
        cCopy.setOverwrite(true);
        cCopy.execute();
    }
    
    /**
     * This method copies the build.bat file from the base to the
     * project root.
     */
    protected void copyBuildBat()
    {
        Copy cCopy = new Copy();
        cCopy.setTaskName("copy-build.bat");
        cCopy.setProject(m_pProject);
        cCopy.setFile(new File(m_pProject.getBaseDir(),
                               "./sdk/build/new/base/build.bat"));
        cCopy.setTodir(m_pProject.getBaseDir());
        cCopy.setOverwrite(true);
        cCopy.execute();
    }
    
    /**
     * This method copies the cmd files to the project root.
     */
    protected void copyCMDFiles()
    {
        Copy cCopy = new Copy();
        cCopy.setTaskName("copy-cmd");
        cCopy.setProject(m_pProject);
        
        FileSet fs = new FileSet();
        fs.setDir(new File(m_pProject.getBaseDir(),
                "./sdk/build/new/base"));
        
        fs.setIncludes("*.cmd");
        cCopy.addFileset(fs);
        
        cCopy.setTodir(m_pProject.getBaseDir());
        cCopy.setOverwrite(true);
        cCopy.execute();
    }
}
