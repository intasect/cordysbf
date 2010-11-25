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
package com.cordys.tools.ant.taskdefs;

import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Executes Ant targets. This does not reload the whole build file
 * like 'antcall' does. 
 *
 * @author mpoyhone
 */
public class ExecuteTargets extends Task
{
    private String sTargetnames;
    
    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        super.execute();
        
        if (sTargetnames == null || sTargetnames.length() == 0)
        {
            throw new BuildException("Parameter 'targetnames' is not set.");
        }
        
        String[] saTargets = sTargetnames.split("\\s*,\\s*");
        Vector<String> vTargets = new Vector<String>(10);
        StringBuilder sb = new StringBuilder(512);
        
        sb.append("Executing targets: ");
        
        
        for (String sName : saTargets)
        {
            vTargets.add(sName);
            sb.append(sName).append(' ');
        }
        
        getProject().log(sb.toString(), Project.MSG_VERBOSE);
        
        getProject().executeTargets(vTargets);
    }

    /**
     * Returns the targetnames.
     *
     * @return Returns the targetnames.
     */
    public String getTargetnames()
    {
        return sTargetnames;
    }

    /**
     * The targetnames to set.
     *
     * @param aTargetnames The targetnames to set.
     */
    public void setTargetnames(String aTargetnames)
    {
        sTargetnames = aTargetnames;
    }
    
}
