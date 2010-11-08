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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Ant task for replacing property values. 
 *
 * @author mpoyhone
 */
public class ReplacePropertyTask extends Task
{
    /**
     * Source value.
     */
    private String srcValue;
    /**
     * Destination property name.
     */
    private String destProperty;
    
    /**
     * Match pattern.
     */
    private String matchString;
    /**    
    /**
     * Replacement string.
     */
    private String replacementString;
    /**
     * Replace mode enumeration value.
     */
    private Mode mode = Mode.REGEX;
    
    /**
     * Contains all supported replacement modes.
     *
     * @author mpoyhone
     */
    private enum Mode {
        REGEX,
        JAVA_FQN_TO_PATH,
        PATH_TO_JAVA_FQN,
    }
    

    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException
    {
        if (mode == null) {
            throw new BuildException("Replacement mode is not set.");
        }
        
        Pattern regex;
        
        switch (mode) {
        case REGEX :
            if (matchString == null || matchString.length() == 0)
            {
                throw new BuildException("Match string is not set.");
            }
            
            if (replacementString == null || replacementString.length() == 0)
            {
                throw new BuildException("Replacment string is not set.");
            }            
            
            regex = Pattern.compile(matchString);
            break;
            
        case JAVA_FQN_TO_PATH :
            regex = Pattern.compile("\\.");
            replacementString = File.separator.replace("\\", "\\\\");
            break;
            
        case PATH_TO_JAVA_FQN :
            regex = Pattern.compile(File.separator.replace("\\", "\\\\"));
            replacementString = ".";
            break;
            
        default:
            throw new BuildException("Invalid replacement mode: " + mode);
        }
        
        Project p = getProject();
        
        p.log("Replacing value: " + srcValue, Project.MSG_VERBOSE);
        p.log("Replacement string: " + replacementString, Project.MSG_VERBOSE);
        
        Matcher matcher = regex.matcher(srcValue);
        String value = matcher.replaceAll(replacementString);
        
        p.log("New value: " + value, Project.MSG_VERBOSE);
        
        p.setProperty(destProperty, value);
    }

    /**
     * Returns the matchString.
     *
     * @return Returns the matchString.
     */
    public String getMatch()
    {
        return matchString;
    }

    /**
     * Sets the matchString.
     *
     * @param matchString The matchString to be set.
     */
    public void setMatch(String matchString)
    {
        this.matchString = matchString;
    }

    /**
     * Returns the mode.
     *
     * @return Returns the mode.
     */
    public String getMode()
    {
        return mode.toString();
    }

    /**
     * Sets the mode.
     *
     * @param mode The mode to be set.
     */
    public void setMode(String value)
    {
        this.mode = Mode.valueOf(value.toUpperCase());
    }
    /**
     * Returns the replacmentString.
     *
     * @return Returns the replacmentString.
     * @deprecated Typo name.
     */
    public String getReplacment()
    {
        return replacementString;
    }

    /**
     * Sets the replacmentString.
     *
     * @param replacmentString The replacmentString to be set.
     * @deprecated Typo name.
     */
    public void setReplacment(String replacmentString)
    {
        this.replacementString = replacmentString;
    }

    /**
     * Returns the destProperty.
     *
     * @return Returns the destProperty.
     */
    public String getDestProperty()
    {
        return destProperty;
    }

    /**
     * Sets the destProperty.
     *
     * @param destProperty The destProperty to be set.
     */
    public void setDestProperty(String destProperty)
    {
        this.destProperty = destProperty;
    }

    /**
     * Returns the srcValue.
     *
     * @return Returns the srcValue.
     */
    public String getSrcValue()
    {
        return srcValue;
    }

    /**
     * Sets the srcValue.
     *
     * @param srcValue The srcValue to be set.
     */
    public void setSrcValue(String srcValue)
    {
        this.srcValue = srcValue;
    }

    
    public static void main(String[] args)
    {
        Project p = new Project();
        
        ReplacePropertyTask rep = new ReplacePropertyTask();
        
        rep.setProject(p);
        
        rep.setSrcValue("mikko.test");
        rep.setMode(Mode.JAVA_FQN_TO_PATH.toString());
        rep.setDestProperty("test");
        rep.execute();
        
    }

    /**
     * Returns the replacementString.
     *
     * @return Returns the replacementString.
     */
    public String getReplacement()
    {
        return replacementString;
    }

    /**
     * Sets the replacementString.
     *
     * @param replacementString The replacementString to be set.
     */
    public void setReplacement(String replacementString)
    {
        this.replacementString = replacementString;
    }
}
