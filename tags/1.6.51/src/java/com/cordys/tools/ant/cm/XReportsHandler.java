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
package com.cordys.tools.ant.cm;

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;
import com.cordys.coe.util.Base64Util;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.FileUtil;
import com.cordys.tools.ant.util.GeneralUtils;

import com.cordys.xreport.XReportConstants;
import com.cordys.xreport.exception.CordysException;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.eibus.xml.xpath.NodeSet;
import com.eibus.xml.xpath.ResultNode;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * This class takes care of handling the XReports content type.
 *
 * @author  pgussow
 */
@SuppressWarnings("deprecation")
public class XReportsHandler
    implements ContentHandler
{
    /**
     * Holds the property for the report GUID.
     */
    public static final String PROP_REPORT_GUID = "report.guid";
    /**
     * Holds the property for the namespace of the report.
     */
    public static final String PROP_NAMESPACE = "namespace";
    /**
     * Holds the property for the methodset name.
     */
    public static final String PROP_METHODSET = "methodset";
    /**
     * Holds the property for the main report file.
     */
    public static final String PROP_MAIN_REPORT_FILE = "main.report.file";
    /**
     * Holds the property for the main report name.
     */
    public static final String PROP_MAIN_REPORT_NAME = "main.report.name";

    /**
     * This method selects a set of nodes.
     *
     * @param   iXML    The XML to search.
     * @param   sXPath  The XPath to execute.
     * @param   xmi     The namespace mappings.
     *
     * @return  The list of nodes.
     */
    public static int[] selectNodes(int iXML, String sXPath, XPathMetaInfo xmi)
    {
        ArrayList<Integer> alReturn = new ArrayList<Integer>();

        XPath xpath = new XPath(sXPath);

        NodeSet ns = xpath.selectNodeSet(iXML, xmi);

        if (ns.hasNext())
        {
            do
            {
                if (!ns.hasNext())
                {
                    break;
                }

                long lResult = ns.next();

                if (ResultNode.isElement(lResult))
                {
                    alReturn.add(Integer.valueOf(ResultNode.getElementNode(lResult)));
                }
            }
            while (true);
        }

        int[] aiReturn = new int[alReturn.size()];

        for (int iCount = 0; iCount < alReturn.size(); iCount++)
        {
            aiReturn[iCount] = alReturn.get(iCount);
        }

        return aiReturn;
    }

    /**
     * This method selects a node based on the XPath.
     *
     * @param   iXML    The XML to search.
     * @param   sXPath  The XPath to execute.
     * @param   xmi     The namespace mappings.
     *
     * @return  A single node.
     */
    public static int selectSingleNode(int iXML, String sXPath, XPathMetaInfo xmi)
    {
        int iReturn = 0;

        XPath xpath = new XPath(sXPath);

        NodeSet ns = xpath.selectNodeSet(iXML, xmi);

        if (ns.hasNext())
        {
            do
            {
                if (!ns.hasNext())
                {
                    break;
                }

                long lResult = ns.next();

                if (ResultNode.isElement(lResult))
                {
                    iReturn = ResultNode.getElementNode(lResult);
                    break;
                }
            }
            while (true);
        }

        return iReturn;
    }

    /**
     * The method should be implemented by the content handlers and will be called according the
     * operation specified. The method deletes the content specified. The task instance is passed on
     * for performing certain tasks in the ant workspace.
     *
     * @param  cmtTask   The current contentmanager task.
     * @param  cContent  The specific content that needs to be published.
     * @param  srmSoap   The object to use for sending soap messages.
     *
     * @see    com.cordys.tools.ant.cm.ContentHandler#executeDelete(com.cordys.tools.ant.cm.ContentManagerTask,
     *         com.cordys.tools.ant.cm.Content, com.cordys.tools.ant.soap.ISoapRequestManager)
     */
    public void executeDelete(ContentManagerTask cmtTask, Content cContent,
                              ISoapRequestManager srmSoap)
    {
        cmtTask.log("[" + cContent.getType() + "] Delete not implemented for XReports.",
                    Project.MSG_WARN);
    }

    /**
     * The method should be implemented by the content handlers and will be called according the
     * operation specified. The method exports contents from the ECX to a file or files in a
     * directory. The task instance is passed on for performing certains tasks in the ant workspace.
     *
     * @param  cmtTask   The current contentmanager task.
     * @param  cContent  The specific content that needs to be published.
     * @param  srmSoap   The object to use for sending soap messages.
     *
     * @see    com.cordys.tools.ant.cm.ContentHandler#executeEcxToFile(com.cordys.tools.ant.cm.ContentManagerTask,
     *         com.cordys.tools.ant.cm.Content, com.cordys.tools.ant.soap.ISoapRequestManager)
     */
    public void executeEcxToFile(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        try
        {
            // Get the folder in which the reports should be created.
            File fBaseFolder = new File(cmtTask.getProject().getProperty(BuildFrameworkProperty
                                                                         .SRC_CONTENT_XREPORTS
                                                                         .getName()));
            cmtTask.log("[" + cContent.getType() + "] Base folder: " +
                        fBaseFolder.getCanonicalPath(), Project.MSG_VERBOSE);

            // First we'll execute the GetPublishedReports to get all currently published reports.

            cmtTask.log("[" + cContent.getType() + "] Getting currently published XReports",
                        Project.MSG_VERBOSE);

            int iResponse = srmSoap.makeSoapRequest(cmtTask.getUserdn(), cmtTask.getOrganization(),
                                                    "http://schemas.cordys.com/xreport/util/1.0",
                                                    "GetPublishedXReports", new String[] { "dn" },
                                                    new String[] { cmtTask.getOrganization() });
            XPathMetaInfo xmi = new XPathMetaInfo();
            xmi.addNamespaceBinding("xr", "http://schemas.cordys.com/xreport/util/1.0");

            int[] aiMethodSets = selectNodes(iResponse, "//xr:xreports/xr:methodset", xmi);
            cmtTask.log("[" + cContent.getType() + "] Found " + aiMethodSets.length +
                        " method sets with possible reports to get from Cordys.",
                        Project.MSG_VERBOSE);

            for (int iMethodSet : aiMethodSets)
            {
                int[] aiReports = selectNodes(iMethodSet, "./xr:xreport[@guid]", xmi);

                if (aiReports.length > 0)
                {
                    String sNamespace = XPathHelper.getStringValue(iMethodSet,
                                                                   new XPath("./xr:namespace/text()"),
                                                                   xmi);
                    String sMethodSet = XPathHelper.getStringValue(iMethodSet,
                                                                   new XPath("./xr:dn/text()"),
                                                                   xmi);
                    String sFixedNamespace = sNamespace.replaceAll("(http://|urn:)", "");
                    sFixedNamespace = sFixedNamespace.replaceAll("[\\W]+", "_");

                    File fMethodSetFolder = new File(fBaseFolder, sFixedNamespace);

                    if (!fMethodSetFolder.exists())
                    {
                        fMethodSetFolder.mkdirs();
                    }

                    // Now do all methods.
                    for (int iReport : aiReports)
                    {
                        String sName = Node.getAttribute(iReport, "name");
                        sName = sName.replaceAll("[\\W]+", "_");

                        File fReport = new File(fMethodSetFolder, sName);

                        if (fReport.exists())
                        {
                            // Delete all files within.
                            FileUtils.deleteRecursively(fReport);
                        }

                        fReport.mkdirs();

                        // Now export the actual report.
                        String sGUID = Node.getAttribute(iReport, "guid");

                        exportReport(cmtTask, cContent, srmSoap, sGUID, fReport, xmi, sMethodSet,
                                     sNamespace);
                    }
                }
                else
                {
                    String sName = XPathHelper.getStringValue(iMethodSet,
                                                              new XPath("./xr:name/text()"), xmi);
                    cmtTask.log("[" + cContent.getType() + "] Method set " + sName +
                                " does not contain GUID based published reports.",
                                Project.MSG_VERBOSE);
                }
            }

            cmtTask.log("[" + cContent.getType() + "] Done.", Project.MSG_VERBOSE);
        }
        catch (Exception e)
        {
            GeneralUtils.handleException("Error getting the currently published XReports", e,
                                         cmtTask);
        }
    }

    /**
     * The method should be implemented by the content handlers and will be called according the
     * operation specified. The method exports contents from the file or files found inside a
     * directory to the ECX machine. The task instance is passed on for performing certains tasks in
     * the ant workspace.
     *
     * @param  cmtTask   The current contentmanager task.
     * @param  cContent  The specific content that needs to be published.
     * @param  srmSoap   The object to use for sending soap messages.
     *
     * @see    com.cordys.tools.ant.cm.ContentHandler#executeFileToEcx(com.cordys.tools.ant.cm.ContentManagerTask,
     *         com.cordys.tools.ant.cm.Content, com.cordys.tools.ant.soap.ISoapRequestManager)
     */
    public void executeFileToEcx(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        // For XReports the tocordys and publish to runtime are the same action.
        executePublishToRuntime(cmtTask, cContent, srmSoap);
    }

    /**
     * This method should take care of publishing the specific content to runtime. For example
     * studio flows need to be published to the runtime environment before they can be run. This
     * method should take care of that.
     *
     * @param  cmtTask   The current contentmanager task.
     * @param  cContent  The specific content that needs to be published.
     * @param  srmSoap   The object to use for sending soap messages.
     *
     * @see    com.cordys.tools.ant.cm.ContentHandler#executePublishToRuntime(com.cordys.tools.ant.cm.ContentManagerTask,
     *         com.cordys.tools.ant.cm.Content, com.cordys.tools.ant.soap.ISoapRequestManager)
     */
    public void executePublishToRuntime(ContentManagerTask cmtTask, Content cContent,
                                        ISoapRequestManager srmSoap)
    {
        // Before we can publish we need to make sure the proper JAR is on the classpath
        try
        {
            Class.forName("com.cordys.xreport.publish.ReportWrapper");
        }
        catch (ClassNotFoundException e)
        {
            throw new BuildException("Could not find the xreport-shared.jar on the current classpath.");
        }

        if (cContent.getContentFile() != null)
        {
            if (!cContent.getContentFile().exists())
            {
                GeneralUtils.handleException("File does not exist!\nFile:" +
                                             cContent.getContentFile().getAbsolutePath());
            }

            if (!cContent.getContentFile().getAbsolutePath().endsWith("xreport.metadata"))
            {
                cmtTask.log("[" + cContent.getType() + "] Ignoring file " +
                            cContent.getContentFile().getAbsolutePath() +
                            " because it's not a xreport metadata file.", Project.MSG_VERBOSE);
            }
            else
            {
                // Load the properties for this report
                Properties pProps = new Properties();

                try
                {
                    pProps.load(new FileInputStream(cContent.getContentFile()));
                    publishReport(cmtTask, cContent, srmSoap, pProps,
                                  cContent.getContentFile().getParentFile());
                }
                catch (Exception e)
                {
                    GeneralUtils.handleException("Error publishing report", e, cmtTask);
                }
            }
        }
        else
        {
            // collect filesets to pass them to extractLDAPContents
            Vector<?> filesets = cContent.getFileSet();
            Vector<FileSet> vfss = new Vector<FileSet>();

            if (cContent.getDir() != null)
            {
                if (!cContent.getDir().exists())
                {
                    GeneralUtils.handleException("Folder does not exist!\nFolder:" +
                                                 cContent.getContentFile().getAbsolutePath());
                }

                FileSet fs = (FileSet) cContent.getImplicitFileSetUsed().clone();
                fs.setDir(cContent.getDir());
                vfss.addElement(fs);
            }

            // Flatten the fileset array.
            for (int i = 0; i < filesets.size(); i++)
            {
                FileSet fs = (FileSet) filesets.elementAt(i);
                vfss.addElement(fs);
            }

            // For each file in the filesets, update the methodset to ECX.
            for (int i = 0; i < vfss.size(); i++)
            {
                FileSet fs = (FileSet) vfss.elementAt(i);
                DirectoryScanner ds = fs.getDirectoryScanner(cmtTask.getProject());
                String[] srcFiles = ds.getIncludedFiles();
                File fBaseDir = ds.getBasedir();

                for (String fileName : srcFiles)
                {
                    if (!fileName.endsWith("xreport.metadata"))
                    {
                        cmtTask.log("[" + cContent.getType() + "] Ignoring file " + fileName +
                                    " because it's not a xreport metadata file.",
                                    Project.MSG_VERBOSE);
                    }
                    else
                    {
                        // Now we need to actually publish the report.
                        Properties pProps = new Properties();

                        try
                        {
                            File fPropertyFile = new File(fBaseDir, fileName);
                            pProps.load(new FileInputStream(fPropertyFile));
                            publishReport(cmtTask, cContent, srmSoap, pProps,
                                          fPropertyFile.getParentFile());
                        }
                        catch (Exception e)
                        {
                            GeneralUtils.handleException("Error publishing report " + fileName, e,
                                                         cmtTask);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the number of items that were processed during the last execution method.
     *
     * @return  Number of processed items or -1 if this task does not support this method.
     *
     * @see     com.cordys.tools.ant.cm.ContentHandler#getNumberOfProcessedItems()
     */
    public int getNumberOfProcessedItems()
    {
        return -1;
    }

    /**
     * Returns the single content name from the given content file. The file must be recognized by
     * this handler.
     *
     * @param   contentFile  Content file.
     * @param   cmtTask      TODO
     * @param   content      Content object.
     * @param   toEcx        TODO
     *
     * @return  Single content name or <code>null</code> if the content name could not be
     *          determined.
     *
     * @throws  IOException
     *
     * @see     com.cordys.tools.ant.cm.ContentHandler#getSingleContentName(java.io.File,com.cordys.tools.ant.cm.ContentManagerTask,
     *          com.cordys.tools.ant.cm.Content, boolean)
     */
    public String getSingleContentName(File contentFile, ContentManagerTask cmtTask,
                                       Content content, boolean toEcx)
                                throws IOException
    {
        if ((contentFile == null) || !contentFile.exists())
        {
            return null;
        }

        File baseDir = content.getDir();

        if (baseDir == null)
        {
            Vector<FileSet> v = content.getFileSet();

            if (v != null)
            {
                for (Iterator<FileSet> iter = v.iterator(); iter.hasNext();)
                {
                    FileSet fs = (FileSet) iter.next();

                    if ((baseDir = fs.getDir(cmtTask.getProject())) != null)
                    {
                        break;
                    }
                }
            }

            if (baseDir == null)
            {
                return null;
            }
        }

        String name = FileUtil.getRelativePath(baseDir, contentFile);

        name = name.replace('\\', '/');

        return name;
    }

    /**
     * This method will get the report details from the server.
     *
     * @param   cmtTask     The current contentmanager task.
     * @param   cContent    The specific content that needs to be published.
     * @param   srmSoap     The object to use for sending soap messages.
     * @param   sGUID       The GUID for the report.
     * @param   fReport     The location where to write the report details.
     * @param   xmi         The XPath namespace-prefix mapping.
     * @param   sMethodSet  The name of the method set in which this report is published.
     * @param   sNamespace  The namespace for the method set in which this report is published.
     *
     * @throws  Exception  In case of any exceptions.
     */
    private void exportReport(ContentManagerTask cmtTask, Content cContent,
                              ISoapRequestManager srmSoap, String sGUID, File fReport,
                              XPathMetaInfo xmi, String sMethodSet, String sNamespace)
                       throws Exception
    {
        cmtTask.log("[" + cContent.getType() + "] Exporting report with guid: " + sGUID +
                    " to folder " + fReport.getCanonicalPath(), Project.MSG_INFO);

        int iResponse = srmSoap.makeSoapRequest(cmtTask.getUserdn(), cmtTask.getOrganization(),
                                                "http://schemas.cordys.com/xreport/util/1.0",
                                                "ImportXReport", new String[] { "guid" },
                                                new String[] { sGUID });

        int iReport = selectSingleNode(iResponse, "//xr:ImportXReportResponse/xr:report", xmi);

        if (iReport != 0)
        {
            writeReport(fReport, iReport, xmi, true, sMethodSet, sNamespace, sGUID);

            // Save the publish XML.
            Node.writeToFile(iReport, iReport, new File(fReport, "publish.xml").getAbsolutePath(),
                             Node.WRITE_HEADER);
        }
        else
        {
            cmtTask.log("[" + cContent.getType() + "] Could not get the details for report: " +
                        sGUID, Project.MSG_WARN);
        }
    }

    /**
     * This method will publish the actual report to Cordys.
     *
     * @param   cmtTask    The current contentmanager task.
     * @param   cContent   The specific content that needs to be published.
     * @param   srmSoap    The object to use for sending soap messages.
     * @param   pMetadata  The metadata for this report.
     * @param   fBasePath  The base path.
     *
     * @throws  CordysException       In case of any exceptions.
     * @throws  SoapRequestException  In case of any SOAP exceptions.
     */
    private void publishReport(ContentManagerTask cmtTask, Content cContent,
                               ISoapRequestManager srmSoap, Properties pMetadata,
                               File fBasePath)
                        throws CordysException, SoapRequestException
    {
        String sReportName = pMetadata.getProperty(PROP_MAIN_REPORT_NAME);
        String sMethodSet = pMetadata.getProperty(PROP_METHODSET);
        String sNamespace = pMetadata.getProperty(PROP_NAMESPACE);
        String sGUID = pMetadata.getProperty(PROP_REPORT_GUID);

        cmtTask.log("[" + cContent.getType() + "] Going to publish report " + sReportName,
                    Project.MSG_VERBOSE);

        // We should avoid the dependency to the new C3 methods for the CoElib. Just read the
        // publish.xml and send it to the server.
        File fPublish = new File(fBasePath, "publish.xml");

        if (!fPublish.exists())
        {
            throw new CordysException("Missing the publish file: " + fPublish.getAbsolutePath());
        }

        // Build up the request for publishing the report.
        Document dDoc = srmSoap.getDocument();
        int[] aiParams = new int[4];
        aiParams[0] = dDoc.createElementNS("methodset", sMethodSet, null,
                                           XReportConstants.XREPORT_UTIL_NS, 0);
        aiParams[1] = dDoc.createElementNS("namespace", sNamespace, null,
                                           XReportConstants.XREPORT_UTIL_NS, 0);
        aiParams[2] = dDoc.createElementNS("guid", sGUID, null, XReportConstants.XREPORT_UTIL_NS,
                                           0);

        // Last parameter is the report, so let's read the publish file.
        try
        {
            aiParams[3] = dDoc.load(fPublish.getAbsolutePath());
        }
        catch (XMLException e)
        {
            throw new CordysException("Error reading the report details from the publish file: " +
                                      fPublish.getAbsolutePath(), e);
        }

        srmSoap.makeSoapRequest(cmtTask.getUserdn(), cmtTask.getOrganization(),
                                XReportConstants.XREPORT_UTIL_NS, "PublishXReport", aiParams);
    }

    /**
     * This method writes the actual report to a file.
     *
     * @param   fReport      The base location of the report.
     * @param   iReport      The XML defining the report.
     * @param   xmi          The XPath namespace-prefix mapping.
     * @param   bMainreport  Whether or not the current report is the main report.
     * @param   sMethodSet   The name of the method set in which this report is published.
     * @param   sNamespace   The namespace for the method set in which this report is published.
     * @param   sGUID        THe GUID for this report.
     *
     * @throws  IOException  In case the writing of the file fails.
     */
    private void writeReport(File fReport, int iReport, XPathMetaInfo xmi, boolean bMainreport,
                             String sMethodSet, String sNamespace, String sGUID)
                      throws IOException
    {
        // Get the jrxml.
        String sName = XPathHelper.getStringValue(iReport, new XPath("./xr:name/text()"), xmi);
        int iJRXML = Node.getFirstElement(selectSingleNode(iReport, "./xr:jrxml", xmi));

        // Write the JRXML.
        String sJRXMLContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                               "<!DOCTYPE jasperReport PUBLIC \"//JasperReports//DTD Report Design//EN\" \"http://jasperreports.sourceforge.net/dtds/jasperreport.dtd\">\n" +
                               Node.writeToString(iJRXML, true);

        FileOutputStream fos = new FileOutputStream(new File(fReport, sName + ".jrxml"));

        try
        {
            fos.write(sJRXMLContent.getBytes("UTF-8"));
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
                fos = null;
            }
        }

        // If it's the main report write the control file
        if (bMainreport == true)
        {
            Properties pTemp = new Properties();
            pTemp.setProperty(PROP_MAIN_REPORT_NAME, sName);
            pTemp.setProperty(PROP_MAIN_REPORT_FILE, sName + ".jrxml");
            pTemp.setProperty(PROP_METHODSET, sMethodSet);
            pTemp.setProperty(PROP_NAMESPACE, sNamespace);
            pTemp.setProperty(PROP_REPORT_GUID, sGUID);

            fos = new FileOutputStream(new File(fReport, "xreport.metadata"));

            try
            {
                pTemp.store(fos, "XReport metadata info");
            }
            finally
            {
                if (fos != null)
                {
                    fos.close();
                }
            }
        }

        // Write the images.
        int[] aiImages = selectNodes(iReport, "./xr:images/xr:image", xmi);

        if (aiImages.length > 0)
        {
            File fImages = new File(fReport, "images");

            if (!fImages.exists())
            {
                fImages.mkdirs();
            }

            for (int iImage : aiImages)
            {
                String sImageName = XPathHelper.getStringValue(iImage,
                                                               new XPath("./xr:name/text()"), xmi);
                String sContent = XPathHelper.getStringValue(iImage,
                                                             new XPath("./xr:content/text()"), xmi);

                Base64Util.writeBase64File(sContent, new File(fImages, sImageName));
            }
        }

        // Write the locale.
        int[] aiLocales = selectNodes(iReport, "./xr:locales/xr:locale", xmi);

        if (aiLocales.length > 0)
        {
            String sBase = Node.getAttribute(iJRXML, "resourceBundle");

            for (int iLocale : aiLocales)
            {
                String sLocaleName = XPathHelper.getStringValue(iLocale,
                                                                new XPath("./xr:name/text()"), xmi);

                if ("default".equals(sLocaleName))
                {
                    sLocaleName = "";
                }
                else
                {
                    sLocaleName = "_" + sLocaleName;
                }

                String sFilename = sBase + sLocaleName + ".properties";
                String sContent = XPathHelper.getStringValue(iLocale,
                                                             new XPath("./xr:content/text()"), xmi);

                Base64Util.writeBase64File(sContent, new File(fReport, sFilename));
            }
        }

        // Do the sub reports.
        int[] aiSubReports = selectNodes(iReport, "./xr:subreports/xr:report", xmi);

        for (int iSubReport : aiSubReports)
        {
            writeReport(fReport, iSubReport, xmi, false, sMethodSet, sNamespace, sGUID);
        }
    }
}
