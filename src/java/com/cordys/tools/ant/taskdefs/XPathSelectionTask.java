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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.cordys.coe.util.xml.dom.XMLHelper;
import com.cordys.coe.util.xml.dom.XPathHelper;

/**
 * Ant task that reads an XML file set an Ant property based on
 * the given XPath expression. 
 *
 * @author mpoyhone
 */
public class XPathSelectionTask extends Task
{
    /**
     * XPath expression.
     */
    private String xpath;
    /**
     * XML File to be read.
     */
    private File file;
    /**
     * Name of the Ant property to be set. 
     */
    private String property;
    /**
     * If <code>true</code> and the XPath didn't find any value, the property is set to empty string.
     */
    private boolean setalways = true;
    
    /**
     * Implementing the abstract method of class Task
     */
    public void execute() throws BuildException {
        if (xpath == null || xpath.length() == 0)
        {
            throw new BuildException("XPath is not set.");
        }
        
        if (property == null || property.length() == 0)
        {
            throw new BuildException("Property name is not set.");
        }

        if (file == null) {
            throw new BuildException("Source file is not set.");
        }

        if (! file.exists()) {
            throw new BuildException("Source file does not exist: " + file);
        }
        
        log("Selecting a value from file '" + file +
                "' with XPath '" + xpath + "'", Project.MSG_DEBUG);
        
        DocumentBuilder dbBuilder = XMLHelper.createDocumentBuilder(false);
        Document dDoc;

        try
        {
            dDoc = dbBuilder.parse(file);
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }    

        Node nNode;
        
        try
        {
            nNode = XPathHelper.selectSingleNode(dDoc, xpath);
        }
        catch (TransformerException e)
        {
            throw new BuildException("Unable to set XML nodes with XPath: " + xpath, e);
        }
        
        String sNodeValue = nNode != null ? nNode.getNodeValue() : null;
        
        if (sNodeValue == null) {
            log("XPath returned no value.", Project.MSG_DEBUG);
            
            if (setalways) {
                getProject().setProperty(property, "");
            }
        } else {
            log("XPath returned value: " + sNodeValue, Project.MSG_DEBUG);
            
            getProject().setProperty(property, sNodeValue);
        }
    }
    
    /**
     * Returns the file.
     *
     * @return Returns the file.
     */
    public File getFile()
    {
        return file;
    }
    /**
     * The file to set.
     *
     * @param aFile The file to set.
     */
    public void setFile(File aFile)
    {
        file = aFile;
    }
    /**
     * Returns the xpath.
     *
     * @return Returns the xpath.
     */
    public String getXpath()
    {
        return xpath;
    }
    /**
     * The xpath to set.
     *
     * @param aXpath The xpath to set.
     */
    public void setXpath(String aXpath)
    {
        xpath = aXpath;
    }

    /**
     * The property to set.
     *
     * @param aProperty The property to set.
     */
    public void setProperty(String aProperty)
    {
        property = aProperty;
    }

    /**
     * The setallways to set.
     *
     * @param aSetallways The setallways to set.
     */
    public void setSetalways(boolean aSetalways)
    {
        setalways = aSetalways;
    }
}
