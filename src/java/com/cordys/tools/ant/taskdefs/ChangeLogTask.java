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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

/**
* Generates text file from xml file.
*
* @author srkrishnan
*/
public class ChangeLogTask extends Task
{
    /**
    * Holds the name of the input xml file.
    */
    private String inFile = null;

    /**
    * Holds the name of the output text file.
    */
    private String outFile = null;
	    
	/**
    * This method sets the input xml file to generate output text file.
    *
    * @param infile The name of the input xml file.
    */
    public void setInfile(String infile)
    {
        this.inFile = infile;
    }
    
    /**
    * This method sets the output text file to be generated.
    *
    * @param outfile The name of the output text file.
    */
    public void setOutfile(String outfile)
    {
        this.outFile = outfile;
    }
    
    /**
    * This method executes the taks
    *
    * @see org.apache.tools.ant.Task#execute()
    */
    @Override
    public void execute() throws BuildException
    {
        try
        {
			String flag = getProject().getProperty("generate.changelog.txt");
			
			if(flag==null)
			{
				return;
			}
			
			flag = flag.trim();
			
			if(flag.equalsIgnoreCase("false"))
            {
                return;
            }
			
			if(!flag.equalsIgnoreCase("true"))
			{
				return;
			}
			
            // Defining file object
            File file = new File(inFile);
            
            // Fetching the path for output file
            if (file.getParent() != null)
            {
                outFile = file.getParent() + File.separator + outFile;
            }
            
            FileWriter fstream = new FileWriter(outFile, false);
            BufferedWriter out = new BufferedWriter(fstream);
            
            if (file.exists())
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                                
                // Reading the input xml file
                Document doc = builder.parse(file);
                NodeList list = doc.getElementsByTagName("release");
                                
                // For each release getting the version,date and action
                //aligning these details into text file.
                for (int i = 2; i < list.getLength(); i++)
                {
                    Element ele = (Element) list.item(i);
                    out.write("version " + ele.getAttribute("version") + " (released " + ele.getAttribute("date") + ")");
                    out.newLine();
                    NodeList actlist = ele.getElementsByTagName("action");
                    
                    for (int j = 0; j < actlist.getLength(); j++)
                    {
                        Element actele = (Element) actlist.item(j);
                        out.write("-" + actele.getAttribute("type") + ": " + actele.getTextContent());
                        out.newLine();
                    }
                    
                    out.newLine();
                }
                
                out.close();
                log(outFile + " generated successfully!!");
            } else
            {
                log(inFile + " (file name) doesn't found!");
            }
            
        } catch (Exception e)
        {
            log(e.getMessage());
        }
    }
}