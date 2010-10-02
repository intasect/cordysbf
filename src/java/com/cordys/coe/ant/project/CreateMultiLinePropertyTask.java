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

import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * This class is used to create properties over multiple lines.
 *
 * @author pgussow
 */
public class CreateMultiLinePropertyTask extends Task
{
    /**
     * Holds all the types that are used for this task.
     */
    private ArrayList<IMultiLineProperty> m_alBuilder = new ArrayList<IMultiLineProperty>();
    /**
     * Holds the name of the projectproperty to return the combined
     * values in.
     */
    private String propertyDestination;

    /**
     * This method adds the newline to the sequence of steps.
     *
     * @param mnl The newline to add.
     */
    public void addMLPNewLine(MLPNewLine mnl)
    {
        m_alBuilder.add(mnl);
    }

    /**
     * This method adds the newline to the sequence of steps.
     *
     * @param mnl The newline to add.
     */
    public void addMLPText(MLPText mnl)
    {
        m_alBuilder.add(mnl);
    }

    /**
     * This method executes the task and creates the multi-line property. 
     *
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute()
                 throws BuildException
    {
        StringBuilder sbFinal = new StringBuilder();

        for (IMultiLineProperty mlp : m_alBuilder)
        {
            mlp.addContent(sbFinal);
        }

        getProject().setProperty(getPropertyDestination(), sbFinal.toString());
        
        getProject().log("The value of the multiline property:", Project.MSG_VERBOSE);
        getProject().log(sbFinal.toString(), Project.MSG_VERBOSE);
    }

    /**
     * This method gets the name of the projectproperty to return the
     * combined values in .
     *
     * @return The name of the projectproperty to return the combined values in
     *         .
     */
    public String getPropertyDestination()
    {
        return propertyDestination;
    }

    /**
     * This method sets the name of the projectproperty to return the
     * combined values in .
     *
     * @param propertyDestination The name of the projectproperty to return the
     *        combined values in .
     */
    public void setPropertyDestination(String propertyDestination)
    {
        this.propertyDestination = propertyDestination;
    }
}
