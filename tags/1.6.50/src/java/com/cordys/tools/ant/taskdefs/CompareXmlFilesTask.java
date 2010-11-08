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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.selectors.SelectorContainer;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.xml.dom.XMLHelper;
import com.cordys.coe.util.xml.dom.XPathHelper;
import com.eibus.xml.nom.XMLException;

/**
 * Ant task that compares XML files using XMLUnit.
 *
 * @author mpoyhone
 */
public class CompareXmlFilesTask extends MatchingTask implements SelectorContainer
{
    private FileSet fsSourceFiles;
    /**
     * Used for vcmdata files content scanning.
     */
    private ZipFileSet zfsZipSourceFiles; 
    private File fDestFolder;
    private List<XmlFilter> lXmlFilterList = new ArrayList<XmlFilter>(10);
    private String sCondition;
    
    private static com.eibus.xml.nom.Document dNomDoc = new com.eibus.xml.nom.Document();
 
    public void addFileSet(FileSet fsSet) {
        fsSourceFiles = fsSet;
    }

    public void setDestination(File fFolder) {
        fDestFolder = fFolder;
    }
    
    public void addZipfileset(ZipFileSet zfsFileSet) {
        zfsZipSourceFiles = zfsFileSet;
    }

    public XmlFilter createXmlfilter() {
        XmlFilter xfFilter = new XmlFilter();
        
        lXmlFilterList.add(xfFilter);
        
        return xfFilter;
    }
    
    public void setCondition(String sExpr) {
        sCondition = sExpr;
    }
    
    /**
     * Implementing the abstract method of class Task
     */
    public void execute() throws BuildException {
        if (sCondition != null && ! checkExecutionCondition(sCondition)) {
            log("Skiping as condition '" + sCondition + "' was not met.", Project.MSG_VERBOSE);
            return;
        }
        
        if (fsSourceFiles == null) {
            throw new BuildException("Source file set is not set.");
        }

        if (fDestFolder == null) {
            throw new BuildException("Destination folder is not set.");
        }

        if (! fDestFolder.exists()) {
            throw new BuildException("Destination folder does not exist: " + fDestFolder);
        }
        
        DirectoryScanner dsScanner = fsSourceFiles.getDirectoryScanner(getProject());
        String[] saFiles;
        
        dsScanner.scan();
        saFiles = dsScanner.getIncludedFiles();
        
        for (int i = 0; i < saFiles.length; i++)
        {   
            String sSource = saFiles[i];
            File fSourcePath = new File(dsScanner.getBasedir(), sSource);
            File fDestPath = new File(fDestFolder, sSource);
            
            if (! fDestPath.exists()) {
                throw new BuildException("File '" + fDestPath +
                        "' does not exist.");
            }
            
            log("Source file " + fSourcePath, Project.MSG_VERBOSE);
            log("Destination file " + fDestPath, Project.MSG_VERBOSE);
            
            if (zfsZipSourceFiles == null) {
                compareFiles(fSourcePath, fDestPath);
            } else {
                zfsZipSourceFiles.setSrc(fSourcePath);
                
                ZipFile zfSourceZip = null;
                ZipFile zfDestZip = null;
                
                try {
                    try
                    {
                        zfSourceZip = new ZipFile(fSourcePath);
                    }
                    catch (Exception e)
                    {
                        throw new BuildException("Unable to read source ZIP file: " + fSourcePath, e);
                    }
                    
                    try
                    {
                        zfDestZip = new ZipFile(fDestPath);
                    }
                    catch (Exception e)
                    {
                        throw new BuildException("Unable to read destination ZIP file: " + fDestPath, e);
                    }
                    
                    DirectoryScanner dsZipScanner = zfsZipSourceFiles.getDirectoryScanner(getProject());
                    String[] saZipFiles;
                    
                    dsScanner.scan();
                    saZipFiles = dsZipScanner.getIncludedFiles();
                    
                    for (int j = 0; j < saZipFiles.length; j++)
                    {   
                        String sZipSource = saZipFiles[j];
                        
                        log("Zip entry " + sZipSource, Project.MSG_VERBOSE);
                        
                        String sSourceXml = readZipEntryContents(zfSourceZip, sZipSource, fSourcePath);
                        String sDestXml = readZipEntryContents(zfDestZip, sZipSource, fDestPath);
                        
                        compareXml(sSourceXml, sDestXml, fSourcePath, fDestPath);
                    }
                }
                finally {
                    try
                    {
                        if (zfSourceZip != null) {
                            zfSourceZip.close();
                        }
                    }
                    catch (IOException e)
                    {
                    }
                    
                    try
                    {
                        if (zfDestZip != null) {
                            zfDestZip.close();
                        }
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
        }
    }
    
    /**
     * Checks if the given execution condition is valid. 
     * @param sCondition Condition expression. This must be of form 'string in [value1, value2, ..]'
     * @return <code>true</code> if the condition was valid
     */
    private boolean checkExecutionCondition(String sCondition)
    {
        Pattern p = Pattern.compile("([^\\s,]+)\\s+in\\s+\\[([^\\]]+)\\]");
        Matcher mMatcher = p.matcher(sCondition.trim());
        
        if (! mMatcher.matches()) {
            throw new BuildException("Invalid execution condition: " + sCondition);
        }
        
        String sTestValue = mMatcher.group(1);
        String sMatchValueString = mMatcher.group(2);
        String[] saMatchValues = sMatchValueString.split("\\s*,\\s*");
       
        for (String sTmp : saMatchValues)
        {
            if (sTestValue.equalsIgnoreCase(sTmp)) {
                return true;
            }
        }
        
        return false;
    }

    private String readZipEntryContents(ZipFile zfFile, String sEntry, File fPath) {
        ZipEntry zeDestEntry = zfFile.getEntry(sEntry);
        String sXml;
        
        if (zeDestEntry == null) {
            // Try again with path separator changed.
            if (sEntry.indexOf('/') >= 0) {
                sEntry = sEntry.replace('/', '\\');
            } else {
                sEntry = sEntry.replace('\\', '/');
            }
            
            zeDestEntry = zfFile.getEntry(sEntry);
            
            if (zeDestEntry == null) {
                throw new BuildException("Zip entry: " + sEntry + " does not exist in file " + fPath);
            }
        }
        
        try
        {
            sXml = FileUtils.readTextZipEntryContents(zfFile, zeDestEntry);
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to read zip entry: " + sEntry + " form file " + fPath, e);
        }      
        
        return sXml;
    }
    
    public static Document createDocumentFromXml(String sXml) {
        DocumentBuilder dbBuilder = XMLHelper.createDocumentBuilder(false);

        Document dReturn;

        try
        {
            dReturn = dbBuilder.parse(new ByteArrayInputStream(sXml.getBytes()));
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }    
        
        return dReturn;
    }
    
    public static Document parseUnformattedXml(String sXml) {
        int xRoot = 0;
        
        try
        {
            xRoot = dNomDoc.parseString(sXml);
            sXml = com.eibus.xml.nom.Node.writeToString(xRoot, false);
        }
        catch (Exception e)
        {
            throw new BuildException("Unable to load XML: " + sXml, e);
        }
        finally {
            if (xRoot != 0) {
                com.eibus.xml.nom.Node.delete(xRoot);
                xRoot = 0;
            }
        }
        
        return createDocumentFromXml(sXml);
    }
    
    public static Document loadUnformattedXmlFile(File fFile) {
        int xRoot = 0;
        String sXml;
        
        try
        {
            xRoot = dNomDoc.load(fFile.getAbsolutePath());
            sXml = com.eibus.xml.nom.Node.writeToString(xRoot, false);
        }
        catch (XMLException e)
        {
            throw new BuildException("Unable to load XML file " + fFile, e);
        }
        finally {
            if (xRoot != 0) {
                com.eibus.xml.nom.Node.delete(xRoot);
                xRoot = 0;
            }
        }
        
        return createDocumentFromXml(sXml);
    }
    
    protected void compareFiles(File fSource, File fDest) throws BuildException {
        Document dSourceDoc = loadUnformattedXmlFile(fSource);
        Document dDestDoc = loadUnformattedXmlFile(fDest);
        
        compareXml(dSourceDoc, dDestDoc, fSource, fDest);
    }
    
    protected void compareXml(String sSource, String sDest, File fSource, File fDest) throws BuildException {
        Document dSourceDoc = parseUnformattedXml(sSource);
        Document dDestDoc = parseUnformattedXml(sDest);
        
        compareXml(dSourceDoc, dDestDoc, fSource, fDest);        
    }
    
    protected void compareXml(Document dSourceDoc, Document dDestDoc, File fSource, File fDest) throws BuildException {
        
        for (Iterator<XmlFilter> iIter = lXmlFilterList.iterator(); iIter.hasNext(); ) {
            XmlFilter xfFilter = iIter.next();

            setNodeValues(dSourceDoc.getDocumentElement(), xfFilter);
            setNodeValues(dDestDoc.getDocumentElement(), xfFilter);
        }
        
        Diff myDiff = new Diff(dSourceDoc, dDestDoc);
        
        myDiff.overrideElementQualifier(new ElementNameAndTextQualifier());
        org.custommonkey.xmlunit.XMLAssert.assertXMLEqual("Files " + fSource + " and " + fDest + " are not equal.",
            myDiff, true); 
    }
    
    private void setNodeValues(Node nRoot, XmlFilter xfFilter) {
        NodeList nlNodes;
        
        try
        {
            nlNodes = XPathHelper.selectNodeList(nRoot, xfFilter.sXPath);
        }
        catch (TransformerException e)
        {
            throw new BuildException("Unable to set XML nodes with XPath: " + xfFilter.sXPath, e);
        }
        
        int len = nlNodes.getLength();
        
        if (len == 0 && ! xfFilter.bOptional) {
            throw new BuildException("XPath " + xfFilter.sXPath + " did not match any nodes.");
        }
        
        for (int i = 0; i < len; i++) {
            Node nNode = nlNodes.item(i);
            
            if (xfFilter.bDeleteNode) {
                if (nNode.getParentNode() != null) {
                    nNode.getParentNode().removeChild(nNode);
                } else {
                    throw new BuildException("Unable to delete the root node!");
                }
            } else {
                String sValue = xfFilter.getValue(nNode);
           
                nNode.setNodeValue(sValue);
            }
        }        
    }
    
    public class XmlFilter {
        String sXPath;
        String sValue;
        Pattern pValueRegex; 
        Pattern pReplaceRegex;
        String sReplaceValue;
        int regexFlags = 0;
        boolean bDeleteNode;
        boolean bOptional = true;
        boolean bReplaceAll = true;
        
        public void setXpath(String v) {
            sXPath = v;
        }
        
        public void setValue(String v) {
            sValue = v;
        }
        
        public void setValueregex(String v) {
            pValueRegex = Pattern.compile(v, regexFlags);
        }
        
        public void setReplaceregex(String v) {
            pReplaceRegex = Pattern.compile(v, regexFlags);
        }
        
        public void setReplacevalue(String v) {
            sReplaceValue = v;
        }
        
        public void setReplaceall(boolean v) {
            bReplaceAll = v;
        }
        
        public void setDeletenode(boolean v) {
            bDeleteNode = v;
        }
        
        public void setOptional(boolean v) {
            bOptional = v;
        }
        
        public void setRegexflags(String v)
        {
            String[] flags = v.split("\\s*,\\s*");
            
            for (String flag : flags)
            {
                if (flag.equals("multiline")) {
                    regexFlags |= Pattern.MULTILINE;
                } else if (flag.equals("ignorecase")) {
                    regexFlags |= Pattern.CASE_INSENSITIVE;
                } else {
                    throw new BuildException("Invalid regex flag: " + flag);
                }
            }
            
            if (pReplaceRegex != null) {
                setReplaceregex(pReplaceRegex.pattern());
            }
            
            if (pValueRegex != null) {
                setValueregex(pValueRegex.pattern());
            }

        }
        
        public String getValue(Node nNode) {
            if (sValue != null) {
                return sValue;
            }
            
            if (pValueRegex != null) {
                String sNodeValue = nNode.getNodeValue();
          
                if (sNodeValue == null) {
                    return "";
                }
                
                Matcher mMatcher = pValueRegex.matcher(sNodeValue);
            
                if (! mMatcher.find()) {
                    return "";
                }
                
                StringBuffer sb = new StringBuffer();
                
                for (int i = 0; i < mMatcher.groupCount(); i++) {
                    sb.append(mMatcher.group(i + 1));
                }
                
                // Return the matched groups.
                return sb.toString();
            } else if (pReplaceRegex != null) {
                String sNodeValue = nNode.getNodeValue();
                
                if (sNodeValue == null) {
                    return "";
                }
                
                Matcher mMatcher = pReplaceRegex.matcher(sNodeValue);

                if (sReplaceValue == null) {
                    throw new BuildException("replacevalue not set for replaceregex");
                }
                
                String res;
                
                if (bReplaceAll) {
                    res = mMatcher.replaceAll(sReplaceValue);
                } else {
                    res = mMatcher.replaceFirst(sReplaceValue);
                }
                
                return res;
            }
            
            return "";
        }
    }
}
