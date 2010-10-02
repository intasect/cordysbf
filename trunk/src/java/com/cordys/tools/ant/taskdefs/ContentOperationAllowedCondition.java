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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.taskdefs.condition.Condition;

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;

/**
 * Checks that the passed operation (e.g. fromcordys) and content type (e.g. xforms)
 * can be allowed based on the properties and prompts the used for confirmation, if
 * necessary.
 *
 * @author mpoyhone
 */
public class ContentOperationAllowedCondition extends Task implements Condition
{
    /**
     * The operation: fromcordys, tocordys or deletecordys.
     */
    private String operation;
    /**
     * Content type: all, xforms, methodsets, etc.
     */
    private String contentType;
    /**
     * Contains the prompt string which is shown to user if a confirmation is needed.
     */
    private String prompt;
    
    private static final Pattern allowPattern = Pattern.compile("(\\w+)\\[(\\w+)\\]");
    
    private static Map<String, Boolean> validPromptAnswers = new HashMap<String, Boolean>();
    
    static {
        validPromptAnswers.put("y", true);
        validPromptAnswers.put("yes", true);
        validPromptAnswers.put("n", false);
        validPromptAnswers.put("no", false);
    }
    
    /**
     * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
     */
    public boolean eval() throws BuildException
    {
        // Build the property name based on the CONTENT_ALLOW_FROMCORDYS name
        // and the passed operation.
        String propName = BuildFrameworkProperty.CONTENT_ALLOW_FROMCORDYS.getName();
        
        propName = propName.substring(0, propName.lastIndexOf('.') + 1) + operation;
        
        String propValue = getProject().getProperty(propName);
        
        if (propValue == null || propValue.length() == 0)
        {
            // If the property is not set, then all content is denied by default.
            log("Content type is not configured. Not allowing this operation.", Project.MSG_VERBOSE);
            return false;
        }
        
        log(String.format("Checking if operation '%s %s' is allowed: %s", operation, contentType, propValue), 
            Project.MSG_DEBUG);
        
        String[] propTypes = propValue.split("[\\s,]+");
        Map<String, EAllowValue> typeMap = new HashMap<String, EAllowValue>();
        
        for (String type : propTypes)
        {
            Matcher m = allowPattern.matcher(type);
            
            if (! m.matches()) {
                throw new IllegalArgumentException(String.format("Invalid allow string '%s' for property %s", type, propName));
            }
            
            String typeName = m.group(1);
            String allowName = m.group(2);
            EAllowValue value = EAllowValue.valueOf(allowName.toUpperCase());
            
            typeMap.put(typeName, value);
        }
         
        EAllowValue value = typeMap.get(contentType);
            
        if (value == null) {
            // This content type is not explicitly configured, so use 'all'
            value = typeMap.get("all");
        }
            
        if (value == null) {
            throw new IllegalStateException(String.format("Content type %s or all is not configured for operation %s",
                                            contentType, operation));
        }
        
        switch (value) {
        case ALLOW :
            log("Content type is allowed by the configuration", Project.MSG_VERBOSE);
            return true;
        case DENY :
            log("Content type is denied by the configuration", Project.MSG_VERBOSE);
            return false;
        }
            
        // Prompt the user for an action.
        InputRequest input = new InputRequest(prompt) {
            /**
             * @see org.apache.tools.ant.input.InputRequest#isInputValid()
             */
            @Override
            public boolean isInputValid()
            {
                String v = getInput();
                return v != null ? validPromptAnswers.containsKey(v.toLowerCase()) : false;
            }
        };
        
        InputHandler handler = getProject().getInputHandler();
        
        if (handler == null) {
            throw new IllegalStateException("No input handler defined.");
        }
        
        handler.handleInput(input);
        
        String inputValue = input.getInput();
        
        if (inputValue != null) {
            inputValue = inputValue.toLowerCase();
        }
        
        return validPromptAnswers.containsKey(inputValue) && validPromptAnswers.get(inputValue); 
    }

    /**
     * Returns the operation.
     *
     * @return Returns the operation.
     */
    public String getOperation()
    {
        return operation;
    }

    /**
     * Sets the operation.
     *
     * @param operation The operation to be set.
     */
    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    /**
     * Returns the contentType.
     *
     * @return Returns the contentType.
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * Sets the contentType.
     *
     * @param contentType The contentType to be set.
     */
    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }
    
    private enum EAllowValue
    {
        ALLOW, DENY, PROMPT;
    }

    /**
     * Returns the prompt.
     *
     * @return Returns the prompt.
     */
    public String getPrompt()
    {
        return prompt;
    }

    /**
     * Sets the prompt.
     *
     * @param prompt The prompt to be set.
     */
    public void setPrompt(String prompt)
    {
        this.prompt = prompt;
    }
}
