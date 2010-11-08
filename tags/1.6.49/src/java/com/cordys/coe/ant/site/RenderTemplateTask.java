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
package com.cordys.coe.ant.site;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Node;

import com.cordys.coe.util.xml.dom.XPathHelper;

import freemarker.core.Environment;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Renders a Freemarker template into a HTML file. This task
 * also looks for an XML file with the same name as the template
 * and inserts the XML into the render context for the template.
 * This way this task can be used to transform XML content
 * into HTML pages as well.
 *
 * @author mpoyhone
 */
public class RenderTemplateTask extends Task
{
    private FileSet fsSourceFileset;
    private File fDestFolder;
    private String sDestext;
    private boolean bAutoloadxml;
    
    public void addFileSet(FileSet fsSet) {
        fsSourceFileset = fsSet;
    }    
    
    public void setDestination(File fFolder) {
        fDestFolder = fFolder;
    }

    public void setDestext(String aDestext)
    {
        sDestext = aDestext;
    }

    public void setAutoloadxml(boolean aAutoloadxml)
    {
        bAutoloadxml = aAutoloadxml;
    }

    /**
     * Implementing the abstract method of class Task
     */
    public void execute() throws BuildException {
        if (fsSourceFileset == null) {
            throw new BuildException("Source file set is not set.");
        }

        if (fDestFolder == null) {
            throw new BuildException("Destination folder is not set.");
        }

        if (! fDestFolder.exists()) {
            throw new BuildException("Destination folder does not exist: " + fDestFolder);
        }
        
        Project pProject = getProject();
        Configuration cTemplateConfig = new Configuration();
        
        cTemplateConfig.setObjectWrapper(new DefaultObjectWrapper());        
        
        DirectoryScanner dsScanner = fsSourceFileset.getDirectoryScanner(pProject);
        String[] saFiles;
        File fBaseDir;
        
        dsScanner.scan();
        saFiles = dsScanner.getIncludedFiles();
        fBaseDir = dsScanner.getBasedir();
        
        SortedSet<String> sPropNames = new TreeSet<String>();
        Hashtable<?, ?> hAntProps = pProject.getProperties();
        SimpleHash shRoot = new SimpleHash();
        AntProperties mAntProperties = new AntProperties();
        AntProperties mAntEscapedProperties = new AntProperties();
        
        // Sort properties for logging.
        for (Iterator<?> iIter = pProject.getProperties().keySet().iterator(); iIter.hasNext();) {
            sPropNames.add((String) iIter.next());
        }
        
        // Put all Ant properties into the 'ant' hash and as escaped into 'ant_escaped' hash.
        for (Iterator<String> iIter = sPropNames.iterator(); iIter.hasNext();) {
            String sName = iIter.next();
            String sValue = (String) hAntProps.get(sName);
            String sEscapedName = sName.replaceAll("[.-]", "_");
            
            log("Property: " + sName + " (" + sEscapedName + ") = " + sValue, Project.MSG_DEBUG);
            
            mAntProperties.put(sName, sValue);
            
            // Replace all dots with underscore because Freemarker uses that notation
            // for bean fields.
            mAntEscapedProperties.put(sEscapedName, sValue);
        }
        
        // Register extra methods and properties.
        shRoot.put("loadXml", new LoadXmlMethod(fBaseDir));
        shRoot.put("parseDate", new ParseDateMethod());
        shRoot.put("condExpr", new ConditionalExpressionMethod());
        shRoot.put("listFiles", new ListFilesMethod());
        shRoot.put("ant", mAntProperties);
        shRoot.put("ant_escaped", mAntEscapedProperties);
        shRoot.put("currentTimeMillis", System.currentTimeMillis());
        
        try
        {
            cTemplateConfig.setDirectoryForTemplateLoading(fBaseDir);
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to set template base folder.", e);
        }
        
        for (int i = 0; i < saFiles.length; i++)
        {   
            String sSource = saFiles[i];
            String sSourceWithoutExt = sSource.replaceFirst("(.*)[.][^.]+$", "$1.");
            File fSourcePath = new File(fBaseDir, sSource);
            File fDestPath = sDestext != null ? new File(fDestFolder, sSourceWithoutExt + sDestext) : new File(fDestFolder, sSourceWithoutExt);
            File fSourceXmlContent = new File(fBaseDir, sSourceWithoutExt + "xml");
            
            log("Source file " + fSourcePath, Project.MSG_VERBOSE);
            
            if (bAutoloadxml && fSourceXmlContent.exists()) {
                log("Source XML content file " + fSourceXmlContent, Project.MSG_VERBOSE);
                
                NodeModel nmContent;
                
                try
                {
                    nmContent = NodeModel.parse(fSourceXmlContent);
                }
                catch (Exception e)
                {
                    throw new BuildException("Unable to parse XML content file: " + fSourceXmlContent, e);
                }
                
                shRoot.put("xml", nmContent);
            } else {
                shRoot.remove("xml");
            }
            
            log("Destination file " + fDestPath, Project.MSG_VERBOSE);
            
            File fDestParent = fDestPath.getParentFile();
            
            if (fDestParent != null && ! fDestParent.exists()) {
                if (! fDestParent.mkdirs()) {
                    throw new BuildException("Unable to create output parent folder: " + fDestParent);
                }
            }
            
            Template tSourceTemplate;
            
            try
            {
                tSourceTemplate = cTemplateConfig.getTemplate(sSource);
            }
            catch (IOException e)
            {
                throw new BuildException("Unable to load source template: " + sSource, e);
            }

            Writer wDestWriter = null;
            
            try
            {
                wDestWriter = new FileWriter(fDestPath);
                tSourceTemplate.process(shRoot, wDestWriter);
                wDestWriter.flush();
            }
            catch (IOException e)
            {
                throw new BuildException("Unable to write destination file: " + fDestPath, e);
            }
            catch (TemplateException e)
            {
                throw new BuildException("Unable to render template: " + fSourcePath, e);
            }            
            finally {
                if (wDestWriter != null) {
                    try
                    {
                        wDestWriter.close();
                    }
                    catch (IOException ignored)
                    {
                    }
                }
            }
        }
    }

    /**
     * Method that loads an XML document in to the Freemarker context.
     * This can be used from the templates like
     *  ${loadXml("mydoc", "file.xml")} 
     * Now the XML can be used with:
     *  ${mydoc.rootnode.child}
     * @author mpoyhone
     */
    public class LoadXmlMethod implements TemplateMethodModel {
        private File fBaseDir;

        public LoadXmlMethod(File aBaseDir)
        {
            this.fBaseDir = aBaseDir;
        }

        /**
         * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
         */
        @SuppressWarnings("unchecked")
        public TemplateModel exec(List lArgs) throws TemplateModelException
        {
            Environment eEnv = Environment.getCurrentEnvironment();
            
            if (lArgs.size() < 2) {
                throw new TemplateModelException("Invalid number of arguments to loadXml method: " + lArgs.size());
            }
            
            String sVarName = lArgs.get(0).toString();
            String sFileName = lArgs.get(1).toString();
            boolean bAbsPath = false;
            boolean bNoFail = false;
            String sSelectXPath = null;
            
            for (int i = 2; i < lArgs.size(); i++) {
                String sArg = lArgs.get(i).toString();
                
                if (sArg == null) {
                    continue;
                }
                
                if ("abspath".equals(sArg)) {
                    bAbsPath = true;
                } else if ("nofail".equals(sArg)) {
                    bNoFail = true;
                } else if (sArg.startsWith("xpath=")) {
                    if (sArg.length() <= 6) {
                        throw new TemplateModelException("Missing XPath expression from argument " + sArg);
                    }
                    
                    sSelectXPath = sArg.substring(6);
                } else {
                    throw new TemplateModelException("Invalid argument for loadXml: " + sArg);
                }
            }
            
            File fFile = ! bAbsPath ? new File(fBaseDir, sFileName) : new File(sFileName);
            
            log("Loading XML file " + fFile, Project.MSG_VERBOSE);
            
            NodeModel nmContent;
            
            try
            {
                nmContent = NodeModel.parse(fFile);
                
                if (sSelectXPath != null) {
                    Node nRoot = nmContent.getNode();
                    
                    log("Selecting node with XPath " + sSelectXPath, Project.MSG_VERBOSE);
                    
                    Node nNewNode;
                    
                    try
                    {
                        nNewNode = XPathHelper.selectSingleNode(nRoot, sSelectXPath);
                    }
                    catch (TransformerException e)
                    {
                        throw new BuildException("Unable to set XML nodes with XPath: " + sSelectXPath, e);
                    }
                    
                    if (nNewNode != null) {
                        nmContent = NodeModel.wrap(nNewNode);
                        
                        eEnv.setVariable(sVarName, nmContent);
                    } else {
                        log("XPath returned no nodes.", Project.MSG_VERBOSE);
                    }
                } else {
                    eEnv.setVariable(sVarName, nmContent);
                }
            }
            catch (Exception e)
            {
                log("Unable to load the XML file " + fFile + " : " + e.getMessage(), Project.MSG_VERBOSE);
                
                if (! bNoFail) {
                    throw new BuildException("Unable to parse XML file: " + fFile, e);
                }
            }            
            
            return new SimpleScalar("");
        }
    }  
    
    /**
     * Lists files (and folders) under the given folder and returns them in a list containing.
     * 
     * This can be used from the templates like
     *  &lt;#assign fileList = listFiles("folder" [, "recursive"])&gt; 
     * @author mpoyhone
     */
    public class ListFilesMethod implements TemplateMethodModel {
        /**
         * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
         */
        @SuppressWarnings("unchecked")
        public TemplateModel exec(List lArgs) throws TemplateModelException
        {
            if (lArgs.size() < 1) {
                throw new TemplateModelException("Invalid number of arguments to listFiles method: " + lArgs.size());
            }
            
            String sFolderName = lArgs.get(0).toString();
            boolean bRecursive = false;
            boolean bReturnFolders = false;
            
            for (int i = 1; i < lArgs.size(); i++) {
                String sArg = lArgs.get(i).toString();
                
                if (sArg == null) {
                    continue;
                }
                
                if ("recursive".equals(sArg)) {
                    bRecursive = true;
                } else if ("returnfolders".equals(sArg)) {
                    bReturnFolders = true;
                } else {
                    throw new TemplateModelException("Invalid argument for listFiles: " + sArg);
                }
            }
            
            File fRootFolder = new File(sFolderName);
            
            SimpleSequence ssResList = new SimpleSequence();
            
            executeListOperation(fRootFolder, fRootFolder, ssResList, bRecursive, bReturnFolders);
            
            return ssResList;
        }
        
        /**
         * Lists all files under the root folder and adds them to the result list.
         * @param fRoot Recursion root folder.
         * @param fCurrent Current folder.
         * @param ssResList Result list.
         * @param bRecurse If <code>true</code> folders are listed recursively.
         * @param bReturnFolders If <code>true</code> also folders are returned.
         * @throws TemplateModelException 
         */
        private void executeListOperation(File fRoot, File fCurrent, SimpleSequence ssResList, boolean bRecurse, boolean bReturnFolders) throws TemplateModelException {
            log("Listing files under folder " + fCurrent, Project.MSG_VERBOSE);
            
            File[] faList = fCurrent.listFiles();
            
            if (faList == null) {
                log("No files found.", Project.MSG_VERBOSE);
                return;
            }
            
            for (File fFile : faList)
            {
                log(String.format("\tFound %s: " + fFile, fFile.isFile() ? "file" : "folder"), Project.MSG_VERBOSE);
                
                if (! fFile.isDirectory() || bReturnFolders) {
                    ssResList.add(new FileWrapper(fRoot, fFile));
                }
                
                if (fFile.isDirectory() && bRecurse) {
                    executeListOperation(fRoot, fFile, ssResList, bRecurse, bReturnFolders);
                }
            }
        }
        
        public class FileWrapper {
            private File fRootFolder;
            private File fFile;
            
            private FileWrapper(File fRootFolder, File fFile) {
                this.fRootFolder = fRootFolder.getAbsoluteFile();
                this.fFile = fFile.getAbsoluteFile();
            }
            
            public File getFile() {
                return fFile;
            }
            
            private String toRelativePath(boolean bUrlEncode) throws UnsupportedEncodingException {
                List<File> lElements = new LinkedList<File>();
                
                for (File f = fFile; f != null && ! f.equals(fRootFolder); f = f.getParentFile()) {
                    lElements.add(0, f);
                }
                
                StringBuilder sb = new StringBuilder(128);
                
                for (File f : lElements)
                {
                    if (sb.length() > 0) {
                        sb.append('/');
                    }
                    
                    if (bUrlEncode) {
                        sb.append(URLEncoder.encode(f.getName(), "UTF-8").replace("+", "%20"));
                    }
                }
                
                return sb.toString();
            }      
            
            public String toRelativePath() throws UnsupportedEncodingException {
                return toRelativePath(false);
            }
            
            public String toRelativeUrl() throws UnsupportedEncodingException {
               return toRelativePath(true);
            }
        }
    }  
    
    /**
     * Parses a date string in to Java date object.
     * @author mpoyhone
     */
    public class ParseDateMethod implements TemplateMethodModel {
        /**
         * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
         */
        @SuppressWarnings("unchecked")
        public Object exec(List lArgs) throws TemplateModelException
        {
            if (lArgs.size() < 1) {
                // Return the current time.
                return new Date();
            }
            
            String sDate = lArgs.get(0).toString();
            String sDateFormat = lArgs.size() > 1 ? lArgs.get(1).toString() : "yyyy-MM-dd'T'HH:mm:ss zzz";
            
            try
            {
                return new SimpleDateFormat(sDateFormat).parse(sDate);
            }
            catch (ParseException e)
            {
                throw new TemplateModelException("Unable to parse date: " + sDate, e);
            }
        }
    }  
    
    /**
     * Implements the 'expr ? truevalue : falsevalue' expression. 
     * @author mpoyhone
     */
    public class ConditionalExpressionMethod implements TemplateMethodModelEx {
        /**
         * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
         */
        @SuppressWarnings("unchecked")
        public Object exec(List lArgs) throws TemplateModelException
        {
            if (lArgs.size() != 3) {
                throw new TemplateModelException("Invalid number of arguments: " + lArgs.size());
            }
            
            Boolean bCond = (Boolean) lArgs.get(0);
            
            return bCond ? lArgs.get(1).toString() : lArgs.get(2).toString();
        }
    }  
    
    public class AntProperties extends HashMap<String, Object> {
        @Override
        public Object get(Object sName) {
            Object oValue = super.get(sName.toString());
            
            return oValue != null ? oValue.toString() : "";
        }
    }
}


