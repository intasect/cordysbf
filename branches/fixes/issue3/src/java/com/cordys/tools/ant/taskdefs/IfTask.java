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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;

/**
 * Implements a simple 'if' task that executes the 'then' part if the condition was 
 * evaluated to <code>true</code> otherwise the 'else' part is executed.
 * 
 * @author mpoyhone
 */
public class IfTask extends ConditionBase
{
    private BranchTaskContainer btcThenBranch;
    private BranchTaskContainer btcElseBranch;
    private Pattern pRegex;
    private String sValue;
    
    public BranchTaskContainer createThen() {
        if (btcThenBranch != null) {
            throw new BuildException("'then' branch already configured for 'if'.");
        }
        
        btcThenBranch =  new BranchTaskContainer();
        
        return btcThenBranch;
    }
    
    public BranchTaskContainer createElse() {
        if (btcElseBranch != null) {
            throw new BuildException("'else' branch already configured for 'if'.");
        }
        
        btcElseBranch =  new BranchTaskContainer();
        
        return btcElseBranch;
    }    
    
    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        Enumeration<?> eCondEnum = getConditions();
        Condition cCondition = (Condition) (eCondEnum.hasMoreElements() ? eCondEnum.nextElement() : null);
        
        if (cCondition == null && pRegex == null) {
            throw new BuildException("No 'condition' or 'regex' specified for 'if'.");
        }
        
        if (pRegex != null) {
            if (sValue == null) {
                throw new BuildException("Parameter 'value' must be set when 'regex' is set.");
            }
        }
        
        if (btcThenBranch == null && btcElseBranch == null) {
            throw new BuildException("'if' task must have a 'then' or 'else' branch.");
        }
        
        boolean bValue;
        
        if (cCondition != null) {
            bValue = cCondition.eval();
        } else {
            bValue = pRegex.matcher(sValue).matches();
        }
        
        if (bValue) {
            if (btcThenBranch != null) {
                btcThenBranch.execute();
            }
        } else {
            if (btcElseBranch != null) {
                btcElseBranch.execute();
            }            
        }
    }

    protected class BranchTaskContainer implements TaskContainer {
        List<Task> lTasks = new ArrayList<Task>(10);
        
        /**
         * @see org.apache.tools.ant.TaskContainer#addTask(org.apache.tools.ant.Task)
         */
        public void addTask(Task t)
        {
            lTasks.add(t);
        }
        
        public void execute() {
            for (Task tTask : lTasks)
            {
                tTask.perform();
            }
        }
    }

    /**
     * Returns the value.
     *
     * @return Returns the value.
     */
    public String getValue()
    {
        return sValue;
    }

    /**
     * The value to set.
     *
     * @param aValue The value to set.
     */
    public void setValue(String aValue)
    {
        sValue = aValue;
    }

    /**
     * The regexp to set.
     *
     * @param aRegex The regex to set.
     */
    public void setRegex(String aRegex)
    {
        pRegex = Pattern.compile(aRegex);
    }
}
