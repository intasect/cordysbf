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
package com.cordys.coe.bf.ant.taskdefs;

import com.cordys.coe.bf.content.types.EContentCategory;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.xml.dom.CoEDOMWriter;
import com.cordys.coe.util.xml.dom.XMLHelper;
import com.cordys.coe.util.xml.dom.XPathHelper;

import com.cordys.tools.ant.cm.MethodsetsHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.filters.StringInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Prepares an isv.xml file so that it can be compared properly with
 * CompareXmlFileTask. All CoBOC object ID's are replace with the object key.
 * Also in the filesystem entry, all file paths are stipped and an attribute
 * is added that contains the MD5 sum of the file's contents.
 *
 * @author mpoyhone
 */
public class PrepareIsvXmlForCompare extends Task
{
    /**
     * DOCUMENTME
     */
    private static final String ISV_ROOT_XPATH = "/ISVPackage/content";
    /**
     * DOCUMENTME
     */
    private static final String ISV_COBOC_ROOT_XPATH = ISV_ROOT_XPATH +
                                                       "/CPCImporter";
    /**
     * DOCUMENTME
     */
    private static final String ISV_COBOC_NOTIFICATION_ROOT_XPATH = ISV_ROOT_XPATH +
                                                                    "/CPCNotificationImporter";
    /**
     * DOCUMENTME
     */
    private static final String ISV_ACTIONTEMPATE_XFORM_ROOT_XPATH = ISV_ROOT_XPATH +
                                                                     "/xmlstore[@description='XMLStore loader for action template XForms']";
    /**
     * DOCUMENTME
     */
    private static final String ISV_METHODSET_ROOT_XPATH = ISV_ROOT_XPATH +
                                                           "/busmethodsets/busmethodset";
    /**
     * DOCUMENTME
     */
    private static final String ISV_ROLE_ROOT_XPATH = ISV_ROOT_XPATH +
                                                           "/busorganizationalroles/busorganizationalrole";    
    /**
     * DOCUMENTME
     */
    private static final String ISV_WSAPPSERVER_ROOT_XPATH = ISV_ROOT_XPATH +
                                                             "/wsappserver-content/Envelope";
    /**
     * DOCUMENTME
     */
    private static EnumMap<EContentType, String> mRootXPathMap = new EnumMap<EContentType, String>(EContentType.class);

    static
    {
        mRootXPathMap.put(EContentType.COBOC_FOLDERS_FOLDER,
                          ISV_COBOC_ROOT_XPATH + "/FolderContent/folder");
        mRootXPathMap.put(EContentType.COBOC_FOLDERS_TEMPLATE,
                          ISV_COBOC_ROOT_XPATH + "/TemplateContent/template");
        mRootXPathMap.put(EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE,
                          ISV_COBOC_ROOT_XPATH +
                          "/SpecialAttributeContent/attribute_template");
        mRootXPathMap.put(EContentType.COBOC_FOLDERS_MAPPING,
                          ISV_COBOC_ROOT_XPATH + "/MappingContent/map");
        mRootXPathMap.put(EContentType.COBOC_FOLDERS_CONTENTMAP,
                          ISV_COBOC_ROOT_XPATH +
                          "/ContentMapContent/contentmap");
        mRootXPathMap.put(EContentType.COBOC_FOLDERS_DECISIONCASE,
                          ISV_COBOC_ROOT_XPATH +
                          "/DecisionCaseContent/decisioncase");
        mRootXPathMap.put(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE,
                          ISV_COBOC_ROOT_XPATH +
                          "/ConditionTemplateContent/conditiontemplate");
        mRootXPathMap.put(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE,
                          ISV_COBOC_ROOT_XPATH +
                          "/ActionTemplateContent/actiontemplate");
        mRootXPathMap.put(EContentType.COBOC_FOLDERS_PROCESSTEMPLATE,
                          ISV_COBOC_ROOT_XPATH + "/ProcessFlowContent/process | /ISVPackage/content/ProcessFlowContent/ProcessFlowContent/process");

        mRootXPathMap.put(EContentType.COBOC_RULES_RULEGROUP,
                          ISV_COBOC_ROOT_XPATH + "/RuleGroupContent/rulegroup");
        mRootXPathMap.put(EContentType.COBOC_RULES_RULE,
                          ISV_COBOC_ROOT_XPATH + "/RuleContent/rule");

        mRootXPathMap.put(EContentType.COBOC_SCHEDULE_TEMPLATE,
                          ISV_COBOC_ROOT_XPATH + "/ScheduleContent/tuple");

        mRootXPathMap.put(EContentType.COBOC_INBOX_MODEL_C3,
                          ISV_COBOC_NOTIFICATION_ROOT_XPATH +
                          "/CPCNotificationContent/MESSAGE_MODEL");
        mRootXPathMap.put(EContentType.COBOC_MESSAGE_TEMPLATE,
                          ISV_COBOC_NOTIFICATION_ROOT_XPATH +
                          "/CPCNotificationContent/messageTemplate");      
        mRootXPathMap.put(EContentType.COBOC_EMAIL_MODEL,
                          ISV_COBOC_NOTIFICATION_ROOT_XPATH +
                          "/CPCNotificationContent/EmailModelContent");            
    }

    /**
     * DOCUMENTME
     */
    private EnumMap<EContentType, Map<String, String>> mContentTypeMap = new EnumMap<EContentType, Map<String, String>>(EContentType.class);
    /**
     * DOCUMENTME
     */
    private File fFile;
    /**
     * DOCUMENTME
     */
    private File fOutfile;

    /**
     * DOCUMENTME
     *
     * @param fFile DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public static Document createDocumentFromXmlFile(File fFile)
    {
        DocumentBuilder dbBuilder = XMLHelper.createDocumentBuilder(false);

        Document dReturn;

        try
        {
            dReturn = dbBuilder.parse(fFile);
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }

        return dReturn;
    }

    /**
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute()
                 throws BuildException
    {
        super.execute();

        if (fFile == null)
        {
            throw new BuildException("Attribute 'file' not set.");
        }

        if (!fFile.exists())
        {
            throw new BuildException("File does not exist: " + fFile);
        }

        // Load the file XML.
        Document dIsvContent;
        Node nRoot;

        log("Reading ISV file: " + fFile, Project.MSG_VERBOSE);
        dIsvContent = createDocumentFromXmlFile(fFile);
        nRoot = dIsvContent.getDocumentElement();

        // Modify the filesystem entries.
        log("Checking filesystem entries.", Project.MSG_VERBOSE);
        replaceFileSystemEntries(nRoot, fFile.getParentFile());

        log("Creating CoBOC object mappings.", Project.MSG_VERBOSE);

        // Read all object ID -> key mappings
        for (EContentType ctType : EContentCategory.COBOC_FOLDERS.getContentTypes())
        {
            switch (ctType)
            {
                case COBOC_FOLDERS_ACTIONTEMPLATE_XFORM:
                case COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM:
                case COBOC_FOLDERS_GENERIC_INSTANCE:
                case COBOC_FOLDERS_PROCESSINSTANCE:
                    // Unimplemented or these don't have object ID's.
                    break;
                    
                case COBOC_FOLDERS_PROCESSBPMN:
                    // Not sure if this put into an ISV package.
                    break;

                case COBOC_FOLDERS_PROCESSTEMPLATE:
                    // Needed for sorting.
                    createCobocIdMappings(ctType, nRoot, "./processname | ./bizprocess/processname",
                                          "./processname | ./bizprocess/processname");
                    break;

                case COBOC_FOLDERS_SPECIAL_ATTRIBUTE:
                    createCobocIdMappings(ctType, nRoot, "./attribtemplate_id",
                                          "./attribute_name");
                    break;

                default:
                    createCobocIdMappings(ctType, nRoot, "./tuple/@entity_id",
                                          "./tuple/@key");
                    break;
            }
        }

        createCobocIdMappings(EContentType.COBOC_RULES_RULEGROUP, nRoot,
                              "./rulegroupid", "./rulegroupname");
        createCobocIdMappings(EContentType.COBOC_RULES_RULE, nRoot, "./ruleid",
                              "./rulename");

        createCobocIdMappings(EContentType.COBOC_SCHEDULE_TEMPLATE, nRoot,
                              "./old/SCHEDULETEMPLATE/TEMPLATEID",
                              "./old/SCHEDULETEMPLATE/TEMPLATENAME");

        createCobocIdMappings(EContentType.COBOC_MESSAGE_TEMPLATE, nRoot,
                              "./messagetemplateid", "./messagetemplatename");        
        createCobocIdMappings(EContentType.COBOC_INBOX_MODEL_C3, nRoot,
                              "./MODEL_ID", "./MODEL_NAME");
        createCobocIdMappings(EContentType.COBOC_EMAIL_MODEL, nRoot,
                              "./EmailModelId", "./tuple/@key");  
        
        // Replace all object ID's with keys.
        log("Replacing CoBOC object ID's.", Project.MSG_VERBOSE);
        replaceReferences(EContentType.COBOC_FOLDERS_FOLDER, nRoot,
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/@entity_id"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/ENTITY_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/PARENT_ID"));
        replaceReferences(EContentType.COBOC_FOLDERS_TEMPLATE, nRoot,
                          new ObjectReference(EContentType.COBOC_FOLDERS_TEMPLATE,
                                              "./tuple/@entity_id"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_TEMPLATE,
                                              "./tuple/old/ENTITY/ENTITY_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/PARENT_ID"));
        replaceReferences(EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE, nRoot,
                          new ObjectReference(EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE,
                                              "./attribtemplate_id"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_TEMPLATE,
                                              "./template_id"));
        replaceReferences(EContentType.COBOC_FOLDERS_MAPPING, nRoot,
                          new ObjectReference(EContentType.COBOC_FOLDERS_MAPPING,
                                              "./tuple/@entity_id"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_MAPPING,
                                              "./tuple/@customkey"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_MAPPING,
                                              "./tuple/old/ENTITY/ENTITY_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_MAPPING,
                                              "./tuple/old/ENTITY/CUSTOMKEY"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/PARENT_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_MAPPING,
                                              "./tuple/old/ENTITY/OBJECT/MAP_OBJECT/MAP_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_TEMPLATE,
                                              "./tuple/old/ENTITY/OBJECT/MAP_OBJECT/SOURCE"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_TEMPLATE,
                                              "./tuple/old/ENTITY/OBJECT/MAP_OBJECT/TARGET"));
        replaceReferences(EContentType.COBOC_FOLDERS_CONTENTMAP, nRoot,
                          new ObjectReference(EContentType.COBOC_FOLDERS_CONTENTMAP,
                                              "./tuple/@entity_id"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_CONTENTMAP,
                                              "./tuple/@customkey"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_CONTENTMAP,
                                              "./tuple/old/ENTITY/ENTITY_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_CONTENTMAP,
                                              "./tuple/old/ENTITY/CUSTOMKEY"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/PARENT_ID"));
        replaceReferences(EContentType.COBOC_FOLDERS_DECISIONCASE, nRoot,
                          new ObjectReference(EContentType.COBOC_FOLDERS_DECISIONCASE,
                                              "./tuple/@entity_id"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_DECISIONCASE,
                                              "./tuple/@customkey"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_DECISIONCASE,
                                              "./tuple/old/ENTITY/ENTITY_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_DECISIONCASE,
                                              "./tuple/old/ENTITY/CUSTOMKEY"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/PARENT_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_TEMPLATE,
                                              "./tuple/old/ENTITY/OBJECT/DecisionCase/Source/@templateid"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_TEMPLATE,
                                              "./tuple/old/ENTITY/OBJECT/DecisionCase/AttributesList/Attribute/@TemplateID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE,
                                              "./tuple/old/ENTITY/OBJECT/DecisionCase/RuleGroup/Rule/Condition/ConditionTemplateId",
                                              false),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/OBJECT/DecisionCase/RuleGroup/Rule/Condition/ConditionRepositoryId"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE,
                                              "./tuple/old/ENTITY/OBJECT/DecisionCase/RuleGroup/Rule/Action/ActionDetails/ActionTemplateId"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/OBJECT/DecisionCase/RuleGroup/Rule/Action/ActionDetails/ActionRepositoryId"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_TEMPLATE,
                                              "./tuple/old/ENTITY/OBJECT/DecisionCase/RuleGroup/Rule/Action/ActionDetails/TemplateID",
                                              false));
        replaceReferences(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE, nRoot,
                          new ObjectReference(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE,
                                              "./tuple/@entity_id"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE,
                                              "./tuple/@customkey"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE,
                                              "./tuple/old/ENTITY/ENTITY_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE,
                                              "./tuple/old/ENTITY/CUSTOMKEY"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/PARENT_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/OBJECT/Condition/ConditionRepositoryId"));
        replaceReferences(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE, nRoot,
                          new ObjectReference(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE,
                                              "./tuple/@entity_id"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE,
                                              "./tuple/@customkey"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE,
                                              "./tuple/old/ENTITY/ENTITY_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE,
                                              "./tuple/old/ENTITY/CUSTOMKEY"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/PARENT_ID"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_FOLDER,
                                              "./tuple/old/ENTITY/OBJECT/Action/ActionDetails/ActionRepositoryId"),
                          new ObjectReference(EContentType.COBOC_FOLDERS_TEMPLATE,
                                              "./tuple/old/ENTITY/OBJECT/Action/ActionDetails/TemplateID",
                                              false));
        replaceReferences(EContentType.COBOC_RULES_RULEGROUP, nRoot,
                          new ObjectReference(EContentType.COBOC_RULES_RULEGROUP,
                                              "./rulegroupid"));
        replaceReferences(EContentType.COBOC_RULES_RULE, nRoot,
                          new ObjectReference(EContentType.COBOC_RULES_RULE,
                                              "./ruleid"),
                          new ObjectReference(EContentType.COBOC_RULES_RULEGROUP,
                                              "./rulegroupid"),
                          new ObjectReference(EContentType.COBOC_RULES_RULE,
                                              "./link", false),
                          new ObjectReference(EContentType.COBOC_RULES_RULE,
                                              "./ruletemplateid", false),
                          new ObjectReference(EContentType.COBOC_FOLDERS_TEMPLATE,
                                              "./br_triggers/trigger/template_id"),
                          new ObjectReference(EContentType.COBOC_RULES_RULE,
                                              "./overrides_rules/overrides/@ruleid"));

        replaceReferences(EContentType.COBOC_SCHEDULE_TEMPLATE, nRoot,
                          new ObjectReference(EContentType.COBOC_SCHEDULE_TEMPLATE,
                                              "./old/SCHEDULETEMPLATE/TEMPLATEID"));

        replaceReferences(EContentType.COBOC_INBOX_MODEL_C3, nRoot,
                          new ObjectReference(EContentType.COBOC_MESSAGE_TEMPLATE,
                                              "./MESSAGE_TEMPLATE_ID"),
                          new ObjectReference(EContentType.COBOC_INBOX_MODEL_C3,
                                              "./MODEL_ID"));
        replaceReferences(EContentType.COBOC_MESSAGE_TEMPLATE, nRoot,
                          new ObjectReference(EContentType.COBOC_MESSAGE_TEMPLATE,
                                              "./messagetemplateid"));        
        replaceReferences(EContentType.COBOC_EMAIL_MODEL, nRoot,
                          new ObjectReference(EContentType.COBOC_EMAIL_MODEL,
                                              "./EmailModelId"));        

        // Modify action/condition template XForms.
        log("Modifying CoBOC action/condition template XForms.",
            Project.MSG_VERBOSE);
        fixCobocActionTemplateXForms(nRoot);
        
        // Sort roles.
        log("Modifying roles.", Project.MSG_VERBOSE);
        fixRoles(nRoot);

        // Modify method implemention for CoBOC mapping methods.
        log("Modifying CoBOC mapping methods.", Project.MSG_VERBOSE);
        fixCobocMethods(nRoot);

        // Modify CoBOC template ID's for Ws-AppServer content.
        log("Modifying Ws-AppServer content.", Project.MSG_VERBOSE);
        fixWsAppServerContent(nRoot);

        // Write the XML back to the file.
        OutputStream osOutput = null;
        File fDestFile = (fOutfile != null) ? fOutfile : fFile;

        log("Writing ISV XML to file: " + fDestFile, Project.MSG_VERBOSE);

        try
        {
            osOutput = new FileOutputStream(fDestFile);

            new CoEDOMWriter(nRoot, osOutput).flush();
        }
        catch (Exception e)
        {
            throw new BuildException("Unable to write file: " + fDestFile);
        }
        finally
        {
            FileUtils.closeStream(osOutput);
        }
    }

    /**
     * Returns the file.
     *
     * @return Returns the file.
     */
    public File getFile()
    {
        return fFile;
    }

    /**
     * DOCUMENTME
     *
     * @param args DOCUMENTME
     */
    public static void main(String[] args)
    {
        Project p = new Project();
        
        p.setBaseDir(new File(".").getAbsoluteFile());
        
        PrepareIsvXmlForCompare t = new PrepareIsvXmlForCompare();

        t.setProject(p);
        t.setFile(new File("build/test/test-isv/isv.xml"));
        t.setOutfile(new File("build/test/test-isv/prepared-isv.xml"));
        t.execute();
    }

    /**
     * Returns the outfile.
     *
     * @return Returns the outfile.
     */
    public File getOutfile()
    {
        return fOutfile;
    }

    /**
     * The file to set.
     *
     * @param aFile The file to set.
     */
    public void setFile(File aFile)
    {
        fFile = aFile;
    }

    /**
     * The outfile to set.
     *
     * @param aOutfile The outfile to set.
     */
    public void setOutfile(File aOutfile)
    {
        fOutfile = aOutfile;
    }

    /**
     * DOCUMENTME
     *
     * @param ctType DOCUMENTME
     * @param sObjectId DOCUMENTME
     * @param sKey DOCUMENTME
     */
    private void addMapping(EContentType ctType, String sObjectId, String sKey)
    {
        Map<String, String> mMap = mContentTypeMap.get(ctType);

        if (mMap == null)
        {
            mMap = new HashMap<String, String>();
            mContentTypeMap.put(ctType, mMap);
        }

        mMap.put(sObjectId, sKey);
    }

    private String calculateChecksum(InputStream is) throws IOException
    {
        MessageDigest md;

        try
        {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new BuildException("Unable to get the hash algoritm : " + e);
        }

        byte[] baBuffer = new byte[2048];
        int iReadBytes;

        while ((iReadBytes = is.read(baBuffer)) != -1)
        {
            md.update(baBuffer, 0, iReadBytes);
        }

        byte[] baResult = md.digest();
        StringBuffer sbRes = new StringBuffer(baResult.length * 2);
        final String sHex = "0123456789ABCDEF";

        for (int i = 0; i < baResult.length; i++)
        {
            int b = baResult[i];

            sbRes.append(sHex.charAt((b & 0xF0) >> 4));
            sbRes.append(sHex.charAt(b & 0x0F));
        }

        return sbRes.toString();
    }
    
    /**
     * DOCUMENTME
     *
     * @param fFile DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private String calculateFileChecksum(File fFile)
    {
        String sName = fFile.getName().toLowerCase();
        
        if (sName.endsWith(".jar")) {
            return calculateJarChecksum(fFile);
        }
        
        InputStream is = null;

        try
        {
            is = new FileInputStream(fFile);

            return calculateChecksum(is);
        }
        catch (FileNotFoundException e)
        {
            throw new BuildException("File not found: " + fFile, e);
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to read file: " + fFile, e);
        }
        finally
        {
            FileUtils.closeStream(is);
        }
    }
    
    /**
     * DOCUMENTME
     *
     * @param fFile DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private String calculateJarChecksum(File fFile)
    {
        JarFile jfJarFile = null;
        StringBuilder sbChecksum = new StringBuilder(512);
       
        try
        {
            jfJarFile = new JarFile(fFile);
            
            Enumeration<JarEntry> eEntries = jfJarFile.entries();
            
            while (eEntries.hasMoreElements())
            {
                JarEntry jeEntry = eEntries.nextElement();
                String sEntryName = jeEntry.getName();
                
                if (jeEntry.isDirectory()) {
                    continue;
                }
                
                if (sEntryName.startsWith("META-INF")) {
                    continue;
                }
                
                sbChecksum.append("\n").append(sEntryName).append("*");
                
                InputStream isEntry = null;

                try
                {
                    isEntry = jfJarFile.getInputStream(jeEntry);
                    sbChecksum.append(calculateChecksum(isEntry));
                }
                catch (IOException e)
                {
                    throw new BuildException("Unable to read jar entry: " + sEntryName, e);
                }
                finally
                {
                    FileUtils.closeStream(isEntry);
                }    
            }
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to read jar file: ");
        }
        finally {
            if (jfJarFile != null) {
                try
                {
                    jfJarFile.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }
        
        try
        {
            return calculateChecksum(new StringInputStream(sbChecksum.toString()));
        }
        catch (IOException e)
        {
            // This should never happen.
            return "";
        }
    }    

    /**
     * DOCUMENTME
     *
     * @param fFolder DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private String calculateFolderChecksum(File fFolder)
    {
        List<String> lChecksumList = new ArrayList<String>();

        calculateFolderChecksum(fFolder, lChecksumList);

        MessageDigest md;

        try
        {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new BuildException("Unable to get the hash algoritm : " + e);
        }

        for (String sChksum : lChecksumList)
        {
            md.update(sChksum.getBytes());
        }

        byte[] baResult = md.digest();
        StringBuffer sbRes = new StringBuffer(baResult.length * 2);
        final String sHex = "0123456789ABCDEF";

        for (int i = 0; i < baResult.length; i++)
        {
            int b = baResult[i];

            sbRes.append(sHex.charAt((b & 0xF0) >> 4));
            sbRes.append(sHex.charAt(b & 0x0F));
        }

        return sbRes.toString();
    }

    /**
     * DOCUMENTME
     *
     * @param fFolder DOCUMENTME
     * @param lChecksumList DOCUMENTME
     */
    private void calculateFolderChecksum(File fFolder,
                                         List<String> lChecksumList)
    {
        String[] saFiles = fFolder.list();

        // Sort files, so they are always in the same order.
        Arrays.sort(saFiles);

        for (String sFileName : saFiles)
        {
            File fFile = new File(fFolder, sFileName);

            if (fFile.isDirectory())
            {
                calculateFolderChecksum(fFile, lChecksumList);
            }
            else
            {
                lChecksumList.add(calculateFileChecksum(fFile));
            }
        }
    }

    /**
     * DOCUMENTME
     *
     * @param ctType DOCUMENTME
     * @param nRoot DOCUMENTME
     * @param sObjectIdXPath DOCUMENTME
     * @param sKeyXPath DOCUMENTME
     */
    private void createCobocIdMappings(EContentType ctType, Node nRoot,
                                       String sObjectIdXPath, String sKeyXPath)
    {
        switch (ctType) {
        case COBOC_FOLDERS_PROCESSBPML :
        case COBOC_FOLDERS_PROCESSBPMN :
            return;
        }
        
        NodeList nlNodes;
        String sObjectXPath = mRootXPathMap.get(ctType);

        if (sObjectXPath == null)
        {
            throw new BuildException("Root XPath not set for content type: " +
                                     ctType.getLogName());
        }

        try
        {
            nlNodes = XPathHelper.selectNodeList(nRoot, sObjectXPath);
        }
        catch (TransformerException e)
        {
            throw new BuildException("Unable to set XML nodes with XPath: " +
                                     sObjectXPath, e);
        }

        int len = nlNodes.getLength();
        ArrayList<SortKey> alNodeSortList = new ArrayList<SortKey>(len);

        for (int i = 0; i < len; i++)
        {
            Node nObjectNode = nlNodes.item(i);
            Node nObjectIdNode;

            try
            {
                nObjectIdNode = XPathHelper.selectSingleNode(nObjectNode,
                                                             sObjectIdXPath);
            }
            catch (TransformerException e)
            {
                throw new BuildException("[" + ctType.getLogName() +
                                         "] Unable to set XML nodes with XPath: " +
                                         sObjectIdXPath, e);
            }

            if (nObjectIdNode == null)
            {
                throw new BuildException("[" + ctType.getLogName() +
                                         "] Object ID node not found from node: " +
                                         nObjectNode + ". XPath: " + sObjectIdXPath);
            }

            String sObjectId = getNodeValue(nObjectIdNode);

            if ((sObjectId == null) || (sObjectId.length() == 0))
            {
                throw new BuildException("[" + ctType.getLogName() +
                                         "] Object ID not found from node: " +
                                         nObjectIdNode + ". XPath: " + sObjectIdXPath);
            }

            Node nKeyNode;

            try
            {
                nKeyNode = XPathHelper.selectSingleNode(nObjectNode, sKeyXPath);
            }
            catch (TransformerException e)
            {
                throw new BuildException("[" + ctType.getLogName() +
                                         "] Unable to set XML nodes with XPath: " +
                                         sKeyXPath, e);
            }

            if (nKeyNode == null)
            {
                throw new BuildException("[" + ctType.getLogName() +
                                         "] Key node not found for object ID: " +
                                         sObjectId + ". XPath: " + sKeyXPath);
            }

            String sKey = getNodeValue(nKeyNode);

            if ((sKey == null) || (sKey.length() == 0))
            {
                throw new BuildException("[" + ctType.getLogName() +
                                         "] Object ID not found from node: " +
                                         nKeyNode + ". XPath: " + sKeyXPath);
            }

            String sTmpValue;
            Node nTmpNode;
            String sTmpKey;

            switch (ctType)
            {
                case COBOC_FOLDERS_SPECIAL_ATTRIBUTE:

                    try
                    {
                        nTmpNode = XPathHelper.selectSingleNode(nObjectNode,
                                                                "./template_id");
                    }
                    catch (TransformerException e)
                    {
                        throw new BuildException("[" + ctType.getLogName() +
                                                 "] Unable to set XML nodes with XPath: " +
                                                 "./template_id", e);
                    }
                    sTmpValue = getNodeValue(nTmpNode);

                    if ((sTmpValue == null) || (sTmpValue.length() == 0))
                    {
                        throw new BuildException("[" + ctType.getLogName() +
                                                 "] Template object ID not found from node: " +
                                                 nObjectNode);
                    }

                    sTmpKey = getKeyByObjectId(EContentType.COBOC_FOLDERS_TEMPLATE,
                                               sTmpValue);

                    if (sTmpKey == null)
                    {
                        throw new BuildException("[" + ctType.getLogName() +
                                                 "] Key node not found for object ID: " +
                                                 sObjectId);
                    }

                    sKey = sTmpKey + "/" + sKey;
                    break;

                case COBOC_RULES_RULE:

                    try
                    {
                        nTmpNode = XPathHelper.selectSingleNode(nObjectNode,
                                                                "./rulegroupid");
                    }
                    catch (TransformerException e)
                    {
                        throw new BuildException("[" + ctType.getLogName() +
                                                 "] Unable to set XML nodes with XPath: " +
                                                 "./rulegroupid", e);
                    }
                    sTmpValue = getNodeValue(nTmpNode);

                    if ((sTmpValue == null) || (sTmpValue.length() == 0))
                    {
                        throw new BuildException("[" + ctType.getLogName() +
                                                 "] Rule group object ID not found from node: " +
                                                 nObjectNode);
                    }

                    sTmpKey = getKeyByObjectId(EContentType.COBOC_RULES_RULEGROUP,
                                               sTmpValue);

                    if (sTmpKey == null)
                    {
                        throw new BuildException("[" + ctType.getLogName() +
                                                 "] Key node not found for object ID: " +
                                                 sObjectId);
                    }

                    sKey = sTmpKey + "/" + sKey;
                    break;
                    
                case COBOC_INBOX_MODEL_C3:
                    try
                    {
                        nTmpNode = XPathHelper.selectSingleNode(nObjectNode,
                                                                "./MESSAGE_TEMPLATE_ID");
                    }
                    catch (TransformerException e)
                    {
                        throw new BuildException("[" + ctType.getLogName() +
                                                 "] Unable to set XML nodes with XPath: " +
                                                 "./MESSAGE_TEMPLATE_ID", e);
                    }
                    sTmpValue = getNodeValue(nTmpNode);

                    if ((sTmpValue == null) || (sTmpValue.length() == 0))
                    {
                        // C1/C2 inbox model
                        break;
                    }

                    sTmpKey = getKeyByObjectId(EContentType.COBOC_MESSAGE_TEMPLATE,
                                               sTmpValue);

                    if (sTmpKey == null)
                    {
                        throw new BuildException("[" + ctType.getLogName() +
                                                 "] Key node not found for object ID: " +
                                                 sObjectId);
                    }

                    sKey = sTmpKey + "/" + sKey;
                    break;                    
            }

            addMapping(ctType, sObjectId, sKey);

            alNodeSortList.add(new SortKey(sKey, nObjectNode));
        }

        // Add nodes sorted by keys.
        for (SortKey skKey : alNodeSortList)
        {
            skKey.unlinkNode();
        }

        Collections.sort(alNodeSortList);

        for (SortKey skKey : alNodeSortList)
        {
            skKey.addNode();
        }
    }

    /**
     * DOCUMENTME
     *
     * @param nRoot DOCUMENTME
     */
    private void fixCobocActionTemplateXForms(Node nRoot)
    {
        NodeList nlXFormNodes;

        try
        {
            nlXFormNodes = XPathHelper.selectNodeList(nRoot,
                                                      ISV_ACTIONTEMPATE_XFORM_ROOT_XPATH +
                                                      "/Envelope");
        }
        catch (TransformerException e)
        {
            throw new BuildException("Unable to select action template XForm nodes.",
                                     e);
        }

        int len = nlXFormNodes.getLength();
        ArrayList<SortKey> alNodeSortList = new ArrayList<SortKey>(len);

        for (int i = 0; i < len; i++)
        {
            Node nXFormNode = nlXFormNodes.item(i);
            Node nKeyNode;
            Node nKeyNode2;

            try
            {
                nKeyNode = XPathHelper.selectSingleNode(nXFormNode,
                                                        "./Body/UpdateXMLObject/tuple/@key");
            }
            catch (TransformerException e)
            {
                throw new BuildException("Action template XForm key XPath failed.");
            }

            if (nKeyNode == null)
            {
                throw new BuildException("Key node not found for action template XForm: " +
                                         nXFormNode);
            }

            try
            {
                nKeyNode2 = XPathHelper.selectSingleNode(nXFormNode,
                                                         "./Body/UpdateXMLObject/tuple/new/xformhtml/@key");
            }
            catch (TransformerException e)
            {
                throw new BuildException("Action template XForm key XPath failed.");
            }

            if (nKeyNode2 == null)
            {
                throw new BuildException("Key node not found for action template XForm: " +
                                         nXFormNode);
            }

            String sXFormKey = nKeyNode.getNodeValue();

            if ((sXFormKey == null) || (sXFormKey.length() == 0))
            {
                throw new BuildException("Action template XForm key is not set.");
            }

            // Extract the object ID from the XForm key (form: /Cordys/WCP/XForms/runtime/116835258806043.caf).
            Pattern pPattern = Pattern.compile("(.*/)([^.]+)(\\.caf|mlm^)");
            Matcher mMatcher = pPattern.matcher(sXFormKey);

            if (!mMatcher.matches())
            {
                throw new BuildException("Unable to extract object ID from action template XForm. Key: " +
                                         sXFormKey);
            }

            String sObjectId = mMatcher.group(2);
            String sTemplateKey;

            // Try to find the action/condition template key.
            sTemplateKey = getKeyByObjectId(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE,
                                            sObjectId);

            if (sTemplateKey == null)
            {
                sTemplateKey = getKeyByObjectId(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE,
                                                sObjectId);
            }

            if (sTemplateKey == null)
            {
                throw new BuildException("Unable to find action/condition template by object ID: " +
                                         sObjectId);
            }

            // Replace the object id with the template key.
            sXFormKey = mMatcher.group(1) + sTemplateKey + mMatcher.group(3);
            nKeyNode.setNodeValue(sXFormKey);
            nKeyNode2.setNodeValue(sXFormKey);

            alNodeSortList.add(new SortKey(sXFormKey, nXFormNode));
        }

        // Add nodes sorted by keys.
        for (SortKey skKey : alNodeSortList)
        {
            skKey.unlinkNode();
        }

        Collections.sort(alNodeSortList);

        for (SortKey skKey : alNodeSortList)
        {
            skKey.addNode();
        }
    }

    /**
     * DOCUMENTME
     *
     * @param nRoot DOCUMENTME
     */
    private void fixCobocMethods(Node nRoot)
    {
        NodeList nlMethodSetNodes;

        try
        {
            nlMethodSetNodes = XPathHelper.selectNodeList(nRoot,
                                                          ISV_METHODSET_ROOT_XPATH);
        }
        catch (TransformerException e)
        {
            throw new BuildException("Unable to select methodset nodes.", e);
        }

        int len = nlMethodSetNodes.getLength();

        for (int i = 0; i < len; i++)
        {
            Node nMethodsetNode = nlMethodSetNodes.item(i);
            Node nImplementationNode;

            try
            {
                nImplementationNode = XPathHelper.selectSingleNode(nMethodsetNode,
                                                                   "./entry/busmethodimplementation/string/text()");
            }
            catch (TransformerException e)
            {
                throw new BuildException("Methodset implementation XPath failed.");
            }

            if (nImplementationNode == null)
            {
                continue;
            }

            String sImplText = nImplementationNode.getNodeValue();

            Matcher mMatcher = MethodsetsHandler.pCobocMapIdPattern.matcher(sImplText);

            if (!mMatcher.matches())
            {
                continue;
            }

            String sMapId = mMatcher.group(2);

            if ((sMapId == null) || (sMapId.length() == 0))
            {
                continue;
            }

            sMapId = sMapId.trim();

            String sMapKey = getKeyByObjectId(EContentType.COBOC_FOLDERS_MAPPING,
                                              sMapId);

            if (sMapKey == null)
            {
                throw new BuildException("Unable to find " +
                                         EContentType.COBOC_FOLDERS_MAPPING.getLogName() +
                                         " with object ID: " + sMapId);
            }

            String sNewImplText = mMatcher.group(1) + sMapKey +
                                  mMatcher.group(3);

            nImplementationNode.setNodeValue(sNewImplText);
        }
    }

    /**
     * DOCUMENTME
     *
     * @param nObjectNode DOCUMENTME
     */
    private void fixCobocScheduleTemplateContent(Node nObjectNode)
    {
        final String sXmlXPath = "./old/SCHEDULETEMPLATE/SCHEDULEXML";
        Node nXmlNode;

        try
        {
            nXmlNode = XPathHelper.selectSingleNode(nObjectNode, sXmlXPath);
        }
        catch (TransformerException e)
        {
            throw new BuildException("[" +
                                     EContentType.COBOC_SCHEDULE_TEMPLATE.getLogName() +
                                     "] Unable to get XML node with XPath: " +
                                     sXmlXPath, e);
        }

        if (nXmlNode == null)
        {
            throw new BuildException("[" +
                                     EContentType.COBOC_SCHEDULE_TEMPLATE.getLogName() +
                                     "] SCHEDULEXML node not found.");
        }

        final String sKeyXPath = "./old/SCHEDULETEMPLATE/TEMPLATENAME";
        Node nKeyNode;

        try
        {
            nKeyNode = XPathHelper.selectSingleNode(nObjectNode, sKeyXPath);
        }
        catch (TransformerException e)
        {
            throw new BuildException("[" +
                                     EContentType.COBOC_SCHEDULE_TEMPLATE.getLogName() +
                                     "] Unable to get XML node with XPath: " +
                                     sKeyXPath, e);
        }

        String sXmlValue = getNodeValue(nXmlNode);

        if ((sXmlValue == null) || (sXmlValue.length() == 0))
        {
            return;
        }

        String sKeyValue = getNodeValue(nKeyNode);

        if ((sKeyValue == null) || (sKeyValue.length() == 0))
        {
            throw new BuildException("[" +
                                     EContentType.COBOC_SCHEDULE_TEMPLATE.getLogName() +
                                     "] Key not found with XPath: " +
                                     sKeyXPath);
        }

        sXmlValue = sXmlValue.replaceFirst("(\\stemplateid)=\"[^\"]+\"",
                                           "$1=\"" + sKeyValue + "\"");
        sXmlValue = sXmlValue.replaceFirst("(\\slastupdated)=\"[^\"]*\"",
                                           "$1=\"\"");

        setNodeValue(nXmlNode, sXmlValue);
    }
    
    /**
     * Sorts the roles.
     * 
     * @param root
     */
    private void fixRoles(Node root)
    {
        NodeList roleNodes;

        try
        {
            roleNodes = XPathHelper.selectNodeList(root,
                                                      ISV_ROLE_ROOT_XPATH);
        }
        catch (TransformerException e)
        {
            throw new BuildException("Unable to select role nodes.",
                                     e);
        }

        int len = roleNodes.getLength();
        ArrayList<SortKey> alNodeSortList = new ArrayList<SortKey>(len);

        for (int i = 0; i < len; i++)
        {
            Node roleNode = roleNodes.item(i);
            Node keyNode;

            try
            {
                keyNode = XPathHelper.selectSingleNode(roleNode,
                                                        "./entry/cn/string");
            }
            catch (TransformerException e)
            {
                throw new BuildException("Role key XPath failed.");
            }

            if (keyNode == null)
            {
                throw new BuildException("Key node not found for role: " +
                                         roleNode);
            }

            String keyValue = getNodeValue(keyNode);

            if ((keyValue == null) || (keyValue.length() == 0))
            {
                throw new BuildException("Role name is not set.");
            }

            alNodeSortList.add(new SortKey(keyValue, roleNode));
        }

        // Add nodes sorted by keys.
        for (SortKey skKey : alNodeSortList)
        {
            skKey.unlinkNode();
        }

        Collections.sort(alNodeSortList);

        for (SortKey skKey : alNodeSortList)
        {
            skKey.addNode();
        }
        
    }

    /**
     * DOCUMENTME
     *
     * @param nRoot DOCUMENTME
     */
    private void fixWsAppServerContent(Node nRoot)
    {
        NodeList nlWsAppServerNodes;

        try
        {
            nlWsAppServerNodes = XPathHelper.selectNodeList(nRoot,
                                                            ISV_WSAPPSERVER_ROOT_XPATH);
        }
        catch (TransformerException e)
        {
            throw new BuildException("Unable to select Ws-AppServer nodes.", e);
        }

        int len = nlWsAppServerNodes.getLength();

        for (int i = 0; i < len; i++)
        {
            Node nWsAppServerNode = nlWsAppServerNodes.item(i);
            NodeList nlTemplateIdNodes;

            try
            {
                nlTemplateIdNodes = XPathHelper.selectNodeList(nWsAppServerNode,
                                                               "./Body/UpdateXMLObject/tuple/new//class/templateid/text()");
            }
            catch (TransformerException e)
            {
                throw new BuildException("Ws-AppServer template ID XPath failed.");
            }

            int len2 = nlTemplateIdNodes.getLength();

            for (int j = 0; j < len2; j++)
            {
                Node nTemplateIdNode = nlTemplateIdNodes.item(j);
                String sObjectId = nTemplateIdNode.getNodeValue();

                if ((sObjectId == null) || (sObjectId.length() == 0) ||
                        sObjectId.equals("1020"))
                {
                    continue;
                }

                String sKey = getKeyByObjectId(EContentType.COBOC_FOLDERS_TEMPLATE,
                                               sObjectId);

                if (sKey == null)
                {
                    throw new BuildException("No " +
                                             EContentType.COBOC_FOLDERS_TEMPLATE.getLogName() +
                                             " found for object ID: " +
                                             sObjectId);
                }

                nTemplateIdNode.setNodeValue(sKey);
            }
        }
    }

    /**
     * DOCUMENTME
     *
     * @param nNode DOCUMENTME
     * @param sAttribName DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private Node getAttribute(Node nNode, String sAttribName)
    {
        NamedNodeMap attribs = nNode.getAttributes();

        if (attribs == null)
        {
            return null;
        }

        return attribs.getNamedItem(sAttribName);
    }

    /**
     * DOCUMENTME
     *
     * @param nNode DOCUMENTME
     * @param sAttribName DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private String getAttributeValue(Node nNode, String sAttribName)
    {
        Node nAttrib = getAttribute(nNode, sAttribName);

        if (nAttrib == null)
        {
            return null;
        }

        return nAttrib.getNodeValue();
    }

    /**
     * DOCUMENTME
     *
     * @param ctType DOCUMENTME
     * @param sObjectId DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private String getKeyByObjectId(EContentType ctType, String sObjectId)
    {
        Map<String, String> mMap = mContentTypeMap.get(ctType);

        if (mMap == null)
        {
            return null;
        }

        return mMap.get(sObjectId);
    }

    /**
     * DOCUMENTME
     *
     * @param nNode DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private String getNodeValue(Node nNode)
    {
        if (nNode == null) {
            return null;
        }
        
        switch (nNode.getNodeType())
        {
            case Node.ELEMENT_NODE:

                StringBuilder sb = new StringBuilder(100);

                for (Node nChild = nNode.getFirstChild(); nChild != null;
                         nChild = nChild.getNextSibling())
                {
                    String s = nChild.getNodeValue();

                    if (s != null)
                    {
                        sb.append(s);
                    }
                }

                return sb.toString();

            default:
                return nNode.getNodeValue();
        }
    }

    /**
     * DOCUMENTME
     *
     * @param nRoot DOCUMENTME
     * @param fRootDir DOCUMENTME
     */
    private void replaceFileSystemEntries(Node nRoot, File fRootDir)
    {
        NodeList nlRefNodes;

        try
        {
            nlRefNodes = XPathHelper.selectNodeList(nRoot, ISV_ROOT_XPATH + "/filesystem/file");
        }
        catch (TransformerException e)
        {
            throw new BuildException("Unable to select filesystem XML nodes.", e);
        }

        int len = nlRefNodes.getLength();

        for (int i = 0; i < len; i++)
        {
            Node nFileNode = nlRefNodes.item(i);
            String sSource = getAttributeValue(nFileNode, "source");

            if ((sSource == null) || (sSource.length() == 0))
            {
                throw new BuildException("'source' attribute missing or is empty for file entry.");
            }
            
            String sDest = getAttributeValue(nFileNode, "destination");

            if ((sDest == null) || (sDest.length() == 0))
            {
                throw new BuildException("'destination' attribute missing or is empty for file entry.");
            }

            String sNewSource = sDest + "/" + new File(sSource).getName();

            setAttributeValue(nFileNode, "source", sNewSource);

            File fRealFile = new File(fRootDir, sSource);

            if (!fRealFile.exists())
            {
                throw new BuildException("File '" + sSource +
                                         "' does not exist in file system.");
            }

            if (sSource.endsWith(".vcmdata" )) {
                // Skip vcmdata file for now because we cannot calculate a good checksum from them.
                continue;
            }
            
            String sChecksum;

            if (fRealFile.isDirectory())
            {
                sChecksum = calculateFolderChecksum(fRealFile);
            }
            else
            {
                sChecksum = calculateFileChecksum(fRealFile);
            }

            setAttributeValue(nFileNode, "file-checksum", sChecksum);
        }
    }

    /**
     * DOCUMENTME
     *
     * @param ctObjectType DOCUMENTME
     * @param nRoot DOCUMENTME
     */
    private void replaceReferences(EContentType ctObjectType, Node nRoot,
                                   ObjectReference... oraReferences)
    {
        NodeList nlNodes;
        String sObjectXPath = mRootXPathMap.get(ctObjectType);

        if (sObjectXPath == null)
        {
            throw new BuildException("[" + ctObjectType.getLogName() +
                                     "] Root XPath not set for content type: " +
                                     ctObjectType.getLogName());
        }

        try
        {
            nlNodes = XPathHelper.selectNodeList(nRoot, sObjectXPath);
        }
        catch (TransformerException e)
        {
            throw new BuildException("[" + ctObjectType.getLogName() +
                                     "] Unable to set XML nodes with XPath: " +
                                     sObjectXPath, e);
        }

        int len = nlNodes.getLength();

        for (int i = 0; i < len; i++)
        {
            Node nObjectNode = nlNodes.item(i);

            for (ObjectReference orRef : oraReferences)
            {
                NodeList nlRefNodes = orRef.getReferenceNodes(nObjectNode, ctObjectType);

                int reflen = nlRefNodes.getLength();

                if (reflen == 0)
                {
                    if (!orRef.bRequired)
                    {
                        continue;
                    }

                    throw new BuildException("[" + ctObjectType.getLogName() +
                                             "] Reference node not found from node: " +
                                             nObjectNode + " and XPath: " +
                                             orRef.sXPath);
                }

                for (int j = 0; j < reflen; j++)
                {
                    Node nReferenceNode = nlRefNodes.item(j);
                    String sObjectId = getNodeValue(nReferenceNode);

                    if ((sObjectId == null) || (sObjectId.length() == 0))
                    {
                        if (!orRef.bRequired)
                        {
                            continue;
                        }

                        throw new BuildException("[" +
                                                 ctObjectType.getLogName() +
                                                 "] Object ID not found from node: " +
                                                 nReferenceNode);
                    }

                    if ("1".equals(sObjectId))
                    {
                        // This is the root folder ID.
                        continue;
                    }

                    String sKey = orRef.getKeyFromObjectId(nObjectNode, sObjectId, ctObjectType);

                    if ((sKey == null) || (sKey.length() == 0))
                    {
                        throw new BuildException("[" +
                                                 ctObjectType.getLogName() +
                                                 "] Key not found from object ID: " +
                                                 sObjectId +
                                                 ". Content type: " +
                                                 orRef.ctType.getLogName() +
                                                 " and XPath: " + orRef.sXPath);
                    }

                    setNodeValue(nReferenceNode, sKey);
                }
            }

            if (ctObjectType == EContentType.COBOC_SCHEDULE_TEMPLATE)
            {
                fixCobocScheduleTemplateContent(nObjectNode);
            }
        }
    }

    /**
     * DOCUMENTME
     *
     * @param nNode DOCUMENTME
     * @param sAttribName DOCUMENTME
     * @param sValue DOCUMENTME
     */
    private void setAttributeValue(Node nNode, String sAttribName, String sValue)
    {
        Node nAttrib = getAttribute(nNode, sAttribName);

        if (nAttrib == null)
        {
            nAttrib = nNode.getOwnerDocument().createAttribute(sAttribName);
            nNode.getAttributes().setNamedItem(nAttrib);
        }

        nAttrib.setNodeValue(sValue);
    }

    /**
     * DOCUMENTME
     *
     * @param nNode DOCUMENTME
     * @param sValue DOCUMENTME
     */
    private void setNodeValue(Node nNode, String sValue)
    {
        switch (nNode.getNodeType())
        {
            case Node.ELEMENT_NODE:

                for (Node nChild = nNode.getFirstChild(); nChild != null;)
                {
                    Node nNextChild = nChild.getNextSibling();

                    if ((nChild.getNodeType() == Node.TEXT_NODE) ||
                            (nChild.getNodeType() == Node.CDATA_SECTION_NODE))
                    {
                        nNode.removeChild(nChild);
                    }

                    nChild = nNextChild;
                }

                Document dDoc = nNode.getOwnerDocument();
                Node nTemp = dDoc.createTextNode(sValue);

                nNode.appendChild(nTemp);
                break;

            default:
                nNode.setNodeValue(sValue);
                break;
        }
    }

    /**
     * DOCUMENTME
     *
     * @author $author$
     */
    private class ObjectReference
    {
        /**
         * DOCUMENTME
         */
        EContentType ctType;
        /**
         * DOCUMENTME
         */
        String sXPath;
        /**
         * DOCUMENTME
         */
        String sParentXPath;
        /**
         * DOCUMENTME
         */
        boolean bRequired;

        /**
         * Creates a new ObjectReference object.
         *
         * @param ctType DOCUMENTME
         * @param sXPath DOCUMENTME
         */
        ObjectReference(EContentType ctType, String sXPath)
        {
            this.ctType = ctType;
            this.sXPath = sXPath;
        }        
        
        /**
         * Creates a new ObjectReference object.
        *
        * @param ctType DOCUMENTME
        * @param sXPath DOCUMENTME
        */
       ObjectReference(EContentType ctType, String sXPath, String sParentXPath)
       {
           this.ctType = ctType;
           this.sXPath = sXPath;
           this.sParentXPath = sParentXPath;
       }       
       
       /**
         * Creates a new ObjectReference object.
         *
         * @param ctType DOCUMENTME
         * @param sXPath DOCUMENTME
         * @param bRequired DOCUMENTME
         */
        ObjectReference(EContentType ctType, String sXPath, boolean bRequired)
        {
            this.ctType = ctType;
            this.sXPath = sXPath;
            this.bRequired = bRequired;
        }
        
        
        public NodeList getReferenceNodes(Node nObjectNode, EContentType ctObjectType)
        {
            NodeList nlRefNodes;

            try
            {
                nlRefNodes = XPathHelper.selectNodeList(nObjectNode,
                                                        sXPath);
            }
            catch (TransformerException e)
            {
                throw new BuildException("[" + ctObjectType.getLogName() +
                                         "] Unable to set XML nodes with XPath: " +
                                         sXPath, e);
            }
            
            return nlRefNodes;
        }
        
        public String getKeyFromObjectId(Node nObjectNode, String sObjectId, EContentType ctObjectType)
        {
            switch (ctObjectType)
            {
            case COBOC_FOLDERS_MAPPING :
                {  
                    // Source and target references have multiple entries.
                    String[] saIds = sObjectId.split(",");
                    StringBuffer sb = new StringBuffer(50);
    
                    for (int k = 0; k < saIds.length; k++)
                    {
                        String sTmpId = saIds[k];
                        String sTmp = getKeyByObjectId(ctType, sTmpId);
    
                        if ((sTmp == null) || (sTmp.length() == 0))
                        {
                            throw new BuildException("[" +
                                                     ctObjectType.getLogName() +
                                                     "] Key not found from object ID: " +
                                                     sTmpId +
                                                     ". Content type: " +
                                                     ctType.getLogName() +
                                                     " and XPath: " +
                                                     sXPath);
                        }
    
                        sb.append(sTmp);
                    }
    
                    return sb.toString();
                }
                
            default:
                return getKeyByObjectId(ctType, sObjectId);
            }
        }
    }

    /**
     * DOCUMENTME
     *
     * @author $author$
      */
    private class SortKey
        implements Comparable<SortKey>
    {
        /**
         * DOCUMENTME
         */
        private Node parent;
        /**
         * DOCUMENTME
         */
        private Node value;
        /**
         * DOCUMENTME
         */
        private String key;

        /**
         * Creates a new SortKey object.
         *
         * @param key DOCUMENTME
         * @param value DOCUMENTME
         */
        SortKey(String key, Node value)
        {
            this.key = key;
            this.value = value;
            this.parent = value.getParentNode();
        }

        /**
         * DOCUMENTME
         */
        public void addNode()
        {
            parent.appendChild(value);
        }

        /**
         * 
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(SortKey o)
        {
            return key.compareTo(o.key);
        }

        /**
         * DOCUMENTME
         */
        public void unlinkNode()
        {
            parent.removeChild(value);
        }
    }
}
