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
package com.cordys.coe.bf.ant;

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;
import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.config.BFConfig;
import com.cordys.coe.bf.config.SoapConfig;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentReadMethod;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BlockContentFilter;
import com.cordys.coe.bf.content.base.impl.CobocContentFilter;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem;
import com.cordys.coe.bf.content.coboc.impl.bcp42_c1.CobocConstants;
import com.cordys.coe.bf.content.coboc.impl.bcp42_c1.CobocContentManager;
import com.cordys.coe.bf.content.coboc.impl.bcp42_c1.ContentSourceConfigurator;
import com.cordys.coe.bf.content.types.EContentCategory;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.objectfactory.SoapRequestFactory;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.AntTaskLogger;

import com.cordys.tools.ant.cm.Content;
import com.cordys.tools.ant.cm.ContentHandler;
import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.cm.Filter;
import com.cordys.tools.ant.cm.FilterSet;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.util.FileUtil;
import com.cordys.tools.ant.util.GeneralUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import org.apache.tools.ant.Project;

/**
 * A build framework content handler for CoBOC content. If content filtering is needed a custom
 * content element with the handler class of CoBOCFilterHandler must be placed before this content
 * element. In case of ECX export or import operations the destination or source directory must be
 * given with the 'dir'-attribute. A sample configuration :
 *
 * <pre>
      <content type="coboc2" dir="${src.content.coboc}/folders" /></pre>
 *
 * @author  mpoyhone
 */
public class CoBOCAntContentHandler
    implements ContentHandler
{
    /**
     * Message logged when xml handling related exception occurs.
     */
    private static final String XML_ERROR = "Error occured while performing CoBOC operation.\n";
    /**
     * Context.
     */
    private BFContext bcContext;
    /**
     * CoBOC content manager object.
     */
    private CobocContentManager ccmCobocManager;
    /**
     * BCP content source. Contains the binding information for BCP format.
     */
    private ContentSourceBcp csBcp;
    /**
     * File system content source. Contains the binding information for File system format.
     */
    private ContentSourceFileSystem csFileSystem;

    /**
     * Creates a new CoBOCTaskHandler object.
     */
    public CoBOCAntContentHandler()
    {
    }

    /**
     * Determines content type from the CoBOC content folder (e.g. coboc/folders).
     *
     * @param   folderName  Folder name.
     *
     * @return  Content type or <code>null</code> if this is an unknown folder.
     */
    public static EContentType determineContentTypeFromFolder(String folderName)
    {
        if (folderName.equals("folders"))
        {
            return EContentType.COBOC_FOLDERS_FOLDER;
        }
        else if (folderName.equals("inbox-models"))
        {
            return EContentType.COBOC_INBOX_MODEL_C1;
        }
        else if (folderName.equals("message-templates"))
        {
            return EContentType.COBOC_MESSAGE_TEMPLATE;
        }
        else if (folderName.equals("rules"))
        {
            return EContentType.COBOC_RULES_RULE;
        }
        else if (folderName.equals("schedules"))
        {
            return EContentType.COBOC_SCHEDULE_TEMPLATE;
        }

        return null;
    }

    /**
     * Executes the deletion Ant task. Deletes all the content from CoBOC that matches the specified
     * filter.
     *
     * @param  cmtTask   The Ant task object
     * @param  cContent  The content element corresponding to CoBOC content.
     * @param  srmSoap   The SOAP object used to make SOAP requests.
     */
    public void executeDelete(ContentManagerTask cmtTask, Content cContent,
                              ISoapRequestManager srmSoap)
    {
        try
        {
            initialize(cmtTask, cContent, srmSoap);

            bcContext.getLogger().info("Deleting CoBOC content from installation.");
            setInputDirectory(cmtTask, cContent);

            if (!initializeSingleFileContent(csBcp, csBcp, cmtTask, cContent))
            {
                initializeSingleContent(cmtTask, cContent);
            }
        }
        catch (Exception e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }

        try
        {
            ccmCobocManager.deleteContentFromSource(csBcp);
        }
        catch (Exception e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }

        bcContext.getLogger().info("Done.");

        try
        {
            uninitialize();
        }
        catch (Exception e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }
    }

    /**
     * This method exports the content in CoBOC to the local directory.
     *
     * @param  cmtTask   The task that is being executed.
     * @param  cContent  The content-definition
     * @param  srmSoap   The manager for sending soap-requests.
     */
    public void executeEcxToFile(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        try
        {
            initialize(cmtTask, cContent, srmSoap);

            bcContext.getLogger().info("Exporting CoBOC content from ECX to file system.");
            setInputDirectory(cmtTask, cContent);

            if (!initializeSingleFileContent(csBcp, csFileSystem, cmtTask, cContent))
            {
                initializeSingleContent(cmtTask, cContent);
            }
        }
        catch (Exception e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }

        try
        {
            ccmCobocManager.transfrerContent(csBcp, csFileSystem, false);
        }
        catch (Exception e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }

        bcContext.getLogger().info("Done.");

        try
        {
            uninitialize();
        }
        catch (Exception e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }
    }

    /**
     * This method imports the content to CoBOC from the local directory.
     *
     * @param  cmtTask   The task that is being executed.
     * @param  cContent  The content-definition
     * @param  srmSoap   The manager for sending soap-requests.
     */
    public void executeFileToEcx(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        try
        {
            initialize(cmtTask, cContent, srmSoap);
            setInputDirectory(cmtTask, cContent);

            if (!initializeSingleFileContent(csFileSystem, csBcp, cmtTask, cContent))
            {
                initializeSingleContent(cmtTask, cContent);
            }
        }
        catch (Exception e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }

        try
        {
            ccmCobocManager.transfrerContent(csFileSystem, csBcp, false);
        }
        catch (Exception e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }

        bcContext.getLogger().info("Done.");

        try
        {
            uninitialize();
        }
        catch (Exception e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }
    }

    /**
     * This method should take care of publishing the specific content to runtime. This content does
     * not need any publishing.
     *
     * @param  cmtTask   The current contentmanager task.
     * @param  cContent  The specific content that needs to be published.
     * @param  srmSoap   The object to use for sending soap messages.
     */
    public void executePublishToRuntime(ContentManagerTask cmtTask, Content cContent,
                                        ISoapRequestManager srmSoap)
    {
        // This target does not need any publishing.
        cmtTask.log("Content of type " + cContent.getType() +
                    " does not need any publishing to runtime.", Project.MSG_INFO);
    }

    /**
     * @see  com.cordys.tools.ant.cm.ContentHandler#getNumberOfProcessedItems()
     */
    public int getNumberOfProcessedItems()
    {
        // Not implemented.
        return -1;
    }

    /**
     * @see  com.cordys.tools.ant.cm.ContentHandler#getSingleContentName(java.io.File,
     *       ContentManagerTask, Content, boolean)
     */
    public String getSingleContentName(File contentFile, ContentManagerTask cmtTask,
                                       Content content, boolean toEcx)
                                throws IOException
    {
        // We cannot implement this method as we can determine file content type only after the
        // initialize method.
        return null;
    }

    /**
     * Returns the file system directory from the build configuration file for this task.
     *
     * @param   cmtTask   The Ant task object.
     * @param   cContent  The content element from configuration file.
     *
     * @throws  BFException
     */
    protected void setInputDirectory(ContentManagerTask cmtTask, Content cContent)
                              throws BFException
    {
        File fBaseDir = null;

        // Get the input directory from the content tag if it is present.
        fBaseDir = cContent.getDir();

        if (fBaseDir == null)
        {
            GeneralUtils.handleException("CoBOC content folder is not set.");
            return;
        }

        // Check for the old root directory (folders).
        if ((fBaseDir.getParentFile() != null) && "folders".equals(fBaseDir.getName()))
        {
            // Use the coboc folder.
            fBaseDir = fBaseDir.getParentFile();
        }

        ccmCobocManager.setContentRootDirectory(fBaseDir);

        bcContext.getLogger().info("CoBOC content folder is " + fBaseDir);
    }

    /**
     * Configures single content operation.
     *
     * @param   csSource    Content source.
     * @param   csDest      Content destination.
     * @param   singleFile  Contains files to be copied as a semi-colon separated list.
     * @param   cmtTask     Ant task.
     * @param   content     Ant content element.
     *
     * @throws  IOException
     * @throws  BFException
     */
    private void configureSingleContentFiles(IContentSource csSource, IContentSource csDest,
                                             String singleFile, ContentManagerTask cmtTask,
                                             Content content)
                                      throws IOException, BFException
    {
        String[] files = singleFile.split(";");
        File baseDir = content.getDir();

        if (baseDir.getName().equals("folders"))
        {
            // We need to point to the coboc-folder.
            baseDir = baseDir.getParentFile();

            if (baseDir == null)
            {
                return;
            }
        }

        CobocContentFilter contentFilter = new CobocContentFilter();
        Set<EContentType> enabledContentSet = new LinkedHashSet<EContentType>();

        for (String f : files)
        {
            File contentFile = new File(f);
            String relContentPath = FileUtil.getRelativePath(baseDir, contentFile);
            String escapedSeparator = File.separator.replace("\\", "\\\\");
            String contentRootFolder = relContentPath.replaceFirst("^([^" + escapedSeparator +
                                                                   "]+).*$", "$1");

            if (contentRootFolder.length() == relContentPath.length())
            {
                throw new BFException("CoBOC content root folder selection is not supported: " + contentRootFolder);
            }

            EContentType rootType = determineContentTypeFromFolder(contentRootFolder);

            if (rootType == null)
            {
                continue;
            }

            // If the path points to a file, read the contents to get the actual path.
            if (contentFile.isFile())
            {
                IContentReadMethod readMethod = csFileSystem.getReadMethod(rootType);

                if (readMethod == null)
                {
                    continue;
                }
                
                readMethod.setContentFilterStatus(false);

                FileReader frReader = null;
                XMLStreamReader xsrReader = null;

                try
                {
                    frReader = new FileReader(contentFile.getAbsolutePath());
                    xsrReader = AxiomUtils.xifXmlInputFactory.createXMLStreamReader(frReader);

                    OMFactory omFactory = OMAbstractFactory.getOMFactory();
                    StAXOMBuilder builder = new StAXOMBuilder(omFactory, xsrReader);
                    OMElement documentElement = builder.getDocumentElement();
                    IXmlSource xsUnmarshallSource = IXmlSource.Factory.newInstance(documentElement);

                    List<IContent> lTmp = readMethod.readObjectsFromXml(xsUnmarshallSource);

                    if (lTmp.size() == 0)
                    {
                        continue;
                    }

                    for (Iterator<IContent> iter = lTmp.iterator(); iter.hasNext();)
                    {
                        IContent obj = iter.next();

                        switch (obj.getType())
                        {
                            case COBOC_FOLDERS_PROCESSBPML:
                            case COBOC_FOLDERS_PROCESSBPMN:
                                iter.remove();
                                continue;
                        }

                        bcContext.registerContent(obj);
                        obj.onLoad(csFileSystem);
                    }

                    for (IContent obj : lTmp)
                    {
                        obj.updateReferences(csFileSystem);
                    }

                    for (IContent obj : lTmp)
                    {
                        String key = obj.getHandle().getContentId();

                        switch (obj.getType())
                        {
                            case COBOC_FOLDERS_PROCESSTEMPLATE:
                                key = key.replaceFirst("^(/)?Business Processes/(.*)$", "$2");
                                key = key.replaceFirst("^(.*/[^/]+)_[^/]+$", "$1");
                                enabledContentSet.add(obj.getType());
                                break;
                        }

                        contentFilter.addFilter(key, true, obj.getType());
                    }
                }
                catch (Exception e)
                {
                    throw (IOException) new IOException("Unable to read file: " + contentFile)
                        .initCause(e);
                }
                finally
                {
                    if (xsrReader != null)
                    {
                        try
                        {
                            xsrReader.close();
                        }
                        catch (XMLStreamException ignored)
                        {
                        }
                    }

                    if (frReader != null)
                    {
                        try
                        {
                            frReader.close();
                        }
                        catch (IOException ignored)
                        {
                        }
                    }
                }
            }
            else
            {
                // Path points to a folder, so set up a single content filter for this.
                String path = relContentPath.substring(contentRootFolder.length() + 1) + "/**";

                path = path.replace("\\", "/");
                
                if (rootType.getCategory() == EContentCategory.COBOC_FOLDERS &&
                    path.startsWith("Business Processes/")) {
                    path = path.substring(19);
                    enabledContentSet.add(EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
                    contentFilter.addFilter(path, true, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
                } else {
                    contentFilter.addFilter(path, true);
                }
            }
        }

        cmtTask.log("[" + content.getType() + "] Processing only content with name '" +
                    contentFilter + "'", Project.MSG_INFO);

        if (enabledContentSet.size() > 0)
        {
            List<String> enabledContentTypeNames = new ArrayList<String>(16);
    
            for (EContentType ctType : enabledContentSet)
            {
                if (ctType.getConfigName() != null)
                {
                    enabledContentTypeNames.add(ctType.getConfigName());
                }
            }
    
            bcContext.getConfig().getCobocConfig().setEnabledContentTypes(enabledContentTypeNames
                                                                          .toArray(new String[enabledContentTypeNames
                                                                                   .size()]),
                                                                          bcContext.getConfig());
        }
        
        Set<EContentType> tmpSet = bcContext.getConfig().getCobocConfig().getEnabledContentTypes();

        for (EContentType ctType : EContentType.values())
        {
            if (tmpSet.contains(ctType))
            {
                csSource.addContentFilter(ctType, contentFilter);

                if (csSource != csDest)
                {
                    csDest.addContentFilter(ctType, contentFilter);
                }
            }
            else
            {
                csSource.addContentFilter(ctType, new BlockContentFilter());

                if (csSource != csDest)
                {
                    csDest.addContentFilter(ctType, new BlockContentFilter());
                }
            }
        }
    }

    /**
     * Initializes this handler.
     *
     * @param   cmtTask   Ant task.
     * @param   cContent  Ant content element.
     * @param   srmSoap   SOAP manager.
     *
     * @throws  BFException
     * @throws  BindingException
     */
    private void initialize(ContentManagerTask cmtTask, Content cContent,
                            ISoapRequestManager srmSoap)
                     throws BFException, BindingException
    {
        bcContext = new BFContext();
        bcContext.setLiLogger(new AntTaskLogger(cmtTask, "coboc"));
        setParameters(cmtTask, cContent, bcContext);

        csBcp = new ContentSourceBcp(bcContext);
        ContentSourceConfigurator.loadFromClasspath(csBcp, bcContext);

        csFileSystem = new ContentSourceFileSystem(bcContext);
        ContentSourceConfigurator.loadFromClasspath(csFileSystem, bcContext);

        ccmCobocManager = new CobocContentManager();
        ccmCobocManager.initialize(bcContext);

        SoapRequestFactory.setFactoryInstance(new SoapRequest_ISoapRequestManager.Factory(srmSoap));

        // Check for object ID mapping file support.
        if ("true".equals(cmtTask.getProject().getProperty(CobocConstants.PROPERTY_USE_OBJECT_ID_MAP)))
        {
            bcContext.getConfig().getCobocConfig().setUseObjectIdMappingFile(true);
        }
    }

    /**
     * Initializes single content operation. This is used by command line.
     *
     * @param  task     Current task
     * @param  content  Current content object.
     */
    private void initializeSingleContent(ContentManagerTask task, Content content)
    {
        String singleContentProperty = GeneralUtils.getTrimmedProperty(task.getProject(),
                                                                       com.cordys.tools.ant.cm
                                                                       .IContent.SINGLE_CONTENT_PROPERTY);

        if (singleContentProperty == null)
        {
            singleContentProperty = "";
        }

        CobocContentFilter contentFilter = new CobocContentFilter();

        // First add the patterns from the build file to folder objects.
        FilterSet filterSet = content.getFilterSet();

        if (filterSet != null)
        {
            for (Filter filter : filterSet.getFilterList())
            {
                String path = filter.getPath();

                if ((path != null) && (path.length() > 0))
                {
                    contentFilter.addFilter(path, filter.getInclusive());
                }
            }
        }

        // Add the paths from the single content property as inclusive filters.
        String[] paths = singleContentProperty.split(";");

        for (String path : paths)
        {
            path = path.trim();

            if ((path != null) && (path.length() > 0))
            {
                // As we don't know what the content type is, we add it to all content
                // and business process models.
                contentFilter.addFilter(path, true);
                contentFilter.addFilter(path, true, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
            }
        }

        // Add the configured filter to all content types.
        for (EContentType ctType : EContentCategory.COBOC.getContentTypes())
        {
            csFileSystem.addContentFilter(ctType, contentFilter);
            csBcp.addContentFilter(ctType, contentFilter);
        }
    }

    /**
     * Initializes single content operation for file property. This is used by the Eclipse plug-in.
     *
     * @param   csSource  Content source.
     * @param   csDest    Content destination.
     * @param   cmtTask   Ant task.
     * @param   cContent  Ant content element.
     *
     * @return  <code>true</code> if single file content property was configured.
     *
     * @throws  BFException  Thrown if the operation failed.
     */
    private boolean initializeSingleFileContent(IContentSource csSource, IContentSource csDest,
                                                ContentManagerTask cmtTask, Content cContent)
                                         throws BFException
    {
        // Check for single content files.
        String sSingleContentFile = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                                    com.cordys.tools.ant.cm.IContent.SINGLE_CONTENT_FILE_PROPERTY);

        if ((sSingleContentFile == null) || (sSingleContentFile.length() == 0))
        {
            return false;
        }

        try
        {
            configureSingleContentFiles(csSource, csDest, sSingleContentFile, cmtTask, cContent);
        }
        catch (IOException e)
        {
            GeneralUtils.handleException("Unable to get single content name from the file\n" +
                                         ContentManagerTask.getExceptionMessage(e), e, cmtTask);
        }

        return true;
    }

    /**
     * Reads the parameters from the Ant project and sets the in the configuration object.
     *
     * @param   cmtTask    Task.
     * @param   cContent   Content element.
     * @param   bcContext  Context that contains the configuration.
     *
     * @throws  BFException  Thrown if the configuration was invalid.
     */
    private void setParameters(ContentManagerTask cmtTask, Content cContent, BFContext bcContext)
                        throws BFException
    {
        BFConfig bcConfig = bcContext.getConfig();

        // Set the platform version information.
        bcConfig.setVersionInfo(cmtTask.getVersionInfo());

        // Set general parameters
        bcConfig.setUserDn(cmtTask.getUserdn());
        bcConfig.setOrganizationDn(cmtTask.getOrganization());

        // Set CoBOC parameters.
        String sCobocContentTypes = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                                    BuildFrameworkProperty
                                                                    .COBOC_CONTENT.getName());

        if (sCobocContentTypes != null)
        {
            bcConfig.getCobocConfig().setEnabledContentTypes(sCobocContentTypes, bcConfig);
        }

        String sUseFileContentTypes = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                                      BuildFrameworkProperty
                                                                      .COBOC_USE_FILETYPES
                                                                      .getName());

        if ("true".equals(sUseFileContentTypes))
        {
            bcConfig.getCobocConfig().setUseFileContentTypes(true);
        }

        // Add the webgateway URL to the Soap Config
        SoapConfig sc = bcConfig.getSoapConfig();
        String sGatewayURL = GeneralUtils.getTrimmedProperty(cmtTask.getProject(),
                                                             BuildFrameworkProperty.WEBGATEWAY_URL
                                                             .getName());

        if ((sGatewayURL != null) && (sGatewayURL.length() > 0))
        {
            try
            {
                sc.setWebGatewayUrl(new URL(sGatewayURL));
            }
            catch (MalformedURLException e)
            {
                GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            }
        }
    }

    /**
     * Unnitializes this handler.
     *
     * @throws  BFException
     */
    private void uninitialize()
                       throws BFException
    {
        if (ccmCobocManager != null)
        {
            ccmCobocManager.uninitialize(bcContext);
            ccmCobocManager = null;
        }

        bcContext = null;
    }
}
