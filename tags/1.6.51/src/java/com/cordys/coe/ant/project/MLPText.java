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

import org.apache.tools.ant.ProjectComponent;

/**
 * This class enables a message to be printed.
 *
 * @author pgussow
 */
public class MLPText extends ProjectComponent
    implements IMultiLineProperty
{
    /**
     * Holds the message to add.
     */
    private String message = "";

    /**
     * This method is executed to build the final string.
     *
     * @param sbString The string to append it to.
     *
     * @see com.cordys.coe.ant.project.IMultiLineProperty#addContent(java.lang.StringBuilder)
     */
    public void addContent(StringBuilder sbString)
    {
        sbString.append(message);
    }

    /**
     * This method gets the message to add.
     *
     * @return The message to add.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * This method sets the message to add.
     *
     * @param message The message to add.
     */
    public void setMessage(String message)
    {
        this.message = message;
    }
}
