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
package com.cordys.coe.ant.sqlscript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.cordys.coe.util.FileUtils;

/**
 * Defines an SQL script to be run. These objects are added under the runsqlscript-task.
 *
 * @author mpoyhone
 */
public class SqlScript
{
    /**
     * Determines the script files.
     */
    private FileSet scriptFileSet;
    /**
     * For single script file
     */
    private File scriptFile;
    /**
     * If <code>true</code> processing is halted when a script fails.
     */
    private boolean haltOnError = true;
    /**
     * If <code>true</code> scripts are executed in a transaction.
     */
    private boolean transaction = true;
    /**
     * If <code>true</code>, Ant properties are replaced in the script files.
     */
    private boolean replaceproperties = false;
    
    /**
     * Returns configured script files.
     * @param t Containg task.
     * @return An array of configured files.
     * @throws BuildException Thrown if the parameters where invalid.
     */
    public File[] getScriptFiles(Task t) throws BuildException
    {
        if (scriptFile != null && scriptFileSet != null) {
            throw new BuildException("Only parameter 'src' or a fileset can be set.");
        }

        if (scriptFile == null && scriptFileSet == null) {
            throw new BuildException("Either parameter 'src' or a fileset must be set.");
        }

        if (scriptFile != null) {
            return new File[] { scriptFile };
        }
        
        DirectoryScanner dsScanner = scriptFileSet.getDirectoryScanner(t.getProject());
        String[] fileNames;
        
        dsScanner.scan();
        fileNames = dsScanner.getIncludedFiles();
        
        File[] res = new File[fileNames.length];
        
        for (int i = 0; i < fileNames.length; i++)
        {
            res[i] = new File(dsScanner.getBasedir(), fileNames[i]);
        }
        
        return res;
    }
    
    /**
     * Parses all the configured script files into a list of SQL commands.
     * @param t Parent task.
     * @param separator SQL command separator.
     * @return A list of SQL commands.
     * @throws BuildException
     */
    public List<String> parseScriptFiles(Task t, String separator) throws BuildException, IOException
    {
        File[] files = getScriptFiles(t);
        List<String> res = new ArrayList<String>(500);
        
        for (File file : files)
        {
            List<String> tmp = parseSqlScript(t, file, separator);
            
            res.addAll(tmp);
        }
        
        return res;
    }
    
    /**
     * Parses the given file into a list of SQL commands.
     * @param file File to be parsed.
     * @param separator SQL command separator.
     * @return A list of SQL commands.
     * @throws IOException 
     */
    public List<String> parseSqlScript(Task t, File file, String separator) throws IOException {
        BufferedReader reader = null;
        StringBuilder currentCmd = new StringBuilder(256);
        List<String> cmdList = new ArrayList<String>(100);
        
        separator = separator.toUpperCase();
        
        try {
            t.log("Reading SQL script file: " + file, Project.MSG_VERBOSE);
            
            reader = new BufferedReader(new FileReader(file));
            
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.toUpperCase().endsWith(separator)) {
                    int sepPos = line.lastIndexOf(separator); 

                    if (sepPos > 0) {
                        if (currentCmd.length() > 0) {
                            currentCmd.append(' ');
                        }
                        currentCmd.append(line.substring(0, sepPos));
                    }
                    
                    if (currentCmd.length() > 0) {
                        cmdList.add(currentCmd.toString());
                        currentCmd.setLength(0);
                    }
                } else {
                    if (currentCmd.length() > 0) {
                        currentCmd.append(' ');
                    }
                    currentCmd.append(line);
                }
            }
            
            if (currentCmd.length() > 0) {
                cmdList.add(currentCmd.toString());
                currentCmd.setLength(0);
            }
        }
        finally {
            FileUtils.closeReader(reader);
        }
        
        if (replaceproperties) {
            Project p = t.getProject();
            
            for (ListIterator<String> iter = cmdList.listIterator(); iter.hasNext();)
            {
                String cmd = iter.next();
                
                iter.set(p.replaceProperties(cmd));
            }
        }
        
        return cmdList;
    }
    
    public void addFileSet(FileSet fs) {
        scriptFileSet = fs;
    }

    /**
     * Returns the transaction.
     *
     * @return Returns the transaction.
     */
    public boolean isTransaction()
    {
        return transaction;
    }

    /**
     * Sets the transaction.
     *
     * @param transaction The transaction to be set.
     */
    public void setTransaction(boolean transaction)
    {
        this.transaction = transaction;
    }

    /**
     * Returns the haltOnError.
     *
     * @return Returns the haltOnError.
     */
    public boolean isHaltOnError()
    {
        return haltOnError;
    }

    /**
     * Sets the haltOnError.
     *
     * @param haltOnError The haltOnError to be set.
     */
    public void setHaltOnError(boolean haltOnError)
    {
        this.haltOnError = haltOnError;
    }

    /**
     * Returns the replaceproperties.
     *
     * @return Returns the replaceproperties.
     */
    public boolean isReplaceproperties()
    {
        return replaceproperties;
    }

    /**
     * Sets the replaceproperties.
     *
     * @param replaceproperties The replaceproperties to be set.
     */
    public void setReplaceproperties(boolean replaceproperties)
    {
        this.replaceproperties = replaceproperties;
    }
}
 