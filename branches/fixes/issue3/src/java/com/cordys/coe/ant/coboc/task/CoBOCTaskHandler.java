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
package com.cordys.coe.ant.coboc.task;

import com.cordys.coe.ant.coboc.content.CoBOCContext;
import com.cordys.coe.ant.coboc.content.CoBOCObject;
import com.cordys.coe.ant.coboc.content.ContentException;
import com.cordys.coe.ant.coboc.content.folders.ContentMapping;
import com.cordys.coe.ant.coboc.content.folders.ContentMappingHandler;
import com.cordys.coe.ant.coboc.content.folders.Folder;
import com.cordys.coe.ant.coboc.content.folders.FolderHandler;
import com.cordys.coe.ant.coboc.content.folders.Mapping;
import com.cordys.coe.ant.coboc.content.folders.MappingHandler;
import com.cordys.coe.ant.coboc.content.folders.Template;
import com.cordys.coe.ant.coboc.content.folders.TemplateAttribute;
import com.cordys.coe.ant.coboc.content.folders.TemplateHandler;
import com.cordys.coe.util.log.AntTaskLogger;

import com.cordys.tools.ant.cm.Content;
import com.cordys.tools.ant.cm.ContentHandler;
import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.NomCollector;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.cordys.tools.ant.util.GeneralUtils;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Constructor;

import org.apache.tools.ant.Project;

/**
 * A build framework content handler for CoBOC content. If content
 * filtering is needed a custom content element with the handler class of
 * CoBOCFilterHandler must be placed before this content element.  In case of
 * ECX export or import operations the destination or source directory must be
 * given with the 'dir'-attribute.   A sample configuration :<pre>
 *    <content type="coboc" dir="${src.content.coboc}/folders" handler="com.cordys.coe.ant.coboc.task.CoBOCTaskHandler" />
 * </pre>
 *
 * @author mpoyhone
 */
public class CoBOCTaskHandler
    implements ContentHandler
{
    /**
     * Message logged when xml handling related exception occurs
     */
    private static final String XML_ERROR = "Error occured while performing CoBOC operation.";

/**
     * Creates a new CoBOCTaskHandler object.
     */
    public CoBOCTaskHandler()
    {
    }

    /**
     * Executes the deletion Ant task. Deletes all the content from
     * CoBOC that matches the specified filter.
     *
     * @param cmtTask The Ant task object
     * @param cContent The content element corresponding to CoBOC content.
     * @param srmSoap The SOAP object used to make SOAP requests.
     */
    public void executeDelete(ContentManagerTask cmtTask, Content cContent,
                              ISoapRequestManager srmSoap)
    {
        cmtTask.log("[" + cContent.getType() +
                    "] Deleting CoBOC content from installation...");

        // Create a new logger object that will be passed to handlers.
        AntTaskLogger tlLogger = new AntTaskLogger(cmtTask, cContent.getType());

        // Create a folder object handler and read all the folders from ECX.
        CoBOCContext ccContext = new CoBOCContext(srmSoap);
        FolderHandler fhFolderHandler = new FolderHandler(ccContext);

        // Set the log object to context.
        ccContext.setLogger(tlLogger);

        // Get the content at organizational level.
        fhFolderHandler.setLevel("organization");

        // Set the filter to the handler.
        fhFolderHandler.setFilterSet(cContent.getFilterSet());

        NomCollector ncCollector = new NomCollector();

        srmSoap.addNomCollector(ncCollector);

        try
        {
            // First read all folders from CoBOC.
            try
            {
                tlLogger.debug("Reading CoBOC folders.");
                fhFolderHandler.convertObjectsFromEcx();
            }
            catch (ContentException e)
            {
                GeneralUtils.handleException(XML_ERROR + e.getMessage(), e,
                                             cmtTask);
                return;
            }

            // Call the handler's delete method with filter list. This
            try
            {
                tlLogger.debug("Deleting CoBOC folders.");
                fhFolderHandler.deleteObjectsFromEcx();
            }
            catch (ContentException e)
            {
                GeneralUtils.handleException(XML_ERROR + e.getMessage(), e,
                                             cmtTask);
                return;
            }
        }
        finally
        {
            ncCollector.deleteNodes();
        }

        srmSoap.removeNomCollector();

        tlLogger.debug("Done.");
    }

    /**
     * This method exports the content in CoBOC to the local directory.
     *
     * @param cmtTask The task that is being executed.
     * @param cContent The content-definition
     * @param srmSoap The manager for sending soap-requests.
     */
    public void executeEcxToFile(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        boolean bIsIsvTask = cmtTask.getIsvdn() != null;

        if (!bIsIsvTask)
        {
            cmtTask.log("[" + cContent.getType() +
                        "] Getting the content from the Cordys server and writing it to the local files.",
                        Project.MSG_VERBOSE);
        }
        else
        {
            cmtTask.log("[" + cContent.getType() +
                        "] Getting the content from an ISV package and writing it to the local files.",
                        Project.MSG_VERBOSE);
        }

        // Create a new logger object that will be passed to handlers.
        AntTaskLogger tlLogger = new AntTaskLogger(cmtTask, cContent.getType());

        // Get the destination directory.
        File fBaseDir = getInputDirectory(cmtTask, tlLogger, cContent);

        if (fBaseDir == null)
        {
            GeneralUtils.handleException("Output directory not set for the CoBOC content.");
            return;
        }

        tlLogger.debug("Export directory: " + fBaseDir.getAbsolutePath());

        // Create the CoBOC context and folder handler object.
        CoBOCContext ccContext = new CoBOCContext(srmSoap);
        FolderHandler fhFolderHandler = new FolderHandler(ccContext);

        // Set the log object to context.
        ccContext.setLogger(tlLogger);

        // Get the content at organizational level.
        fhFolderHandler.setLevel("organization");

        // Set the folder filter
        fhFolderHandler.setFilterSet(cContent.getFilterSet());

        NomCollector ncCollector = new NomCollector();

        srmSoap.addNomCollector(ncCollector);

        // First read all folders from CoBOC. Templates and mappings will
        // be read based on the folder structure.
        try
        {
            if (!bIsIsvTask)
            {
                tlLogger.debug("Reading CoBOC folders.");
                fhFolderHandler.convertObjectsFromEcx();
            }
        }
        catch (ContentException e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }
        finally
        {
            ncCollector.deleteNodes();
        }

        // Create folder object handlers.
        TemplateHandler thTemplateHandler = new TemplateHandler(ccContext);
        MappingHandler mhMappingHandler = new MappingHandler(ccContext);
        ContentMappingHandler mhContentMapHandler = new ContentMappingHandler(ccContext);

        // Set the filter for objects.
        thTemplateHandler.setFilterSet(cContent.getFilterSet());
        mhMappingHandler.setFilterSet(cContent.getFilterSet());
        mhContentMapHandler.setFilterSet(cContent.getFilterSet());

        try
        {
            if (!bIsIsvTask)
            {
                // Read CoBOC templates.
                tlLogger.debug("Reading CoBOC templates.");
                thTemplateHandler.convertObjectsFromEcx();
                thTemplateHandler.convertTemplateAttributesFromECX();

                // Read CoBOC mappings.
                tlLogger.debug("Reading CoBOC mappings.");
                mhMappingHandler.convertObjectsFromEcx();

                // Read CoBOC content maps.
                tlLogger.debug("Reading CoBOC content maps.");
                mhContentMapHandler.convertObjectsFromEcx();
            }
        }
        catch (ContentException e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
        }
        finally
        {
            ncCollector.deleteNodes();
        }

        // If this is an ISV package load task, the load the package here.
        if (bIsIsvTask)
        {
            executeExcIsvToFile(ccContext, cmtTask, cContent, srmSoap);
        }

        // Write the folders to the destination directory.
        try
        {
            tlLogger.debug("Writing file system folders.");
            fhFolderHandler.setRootDirectory(fBaseDir);
            fhFolderHandler.convertObjectsToFiles();
        }
        catch (ContentException e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }
        finally
        {
            ncCollector.deleteNodes();
        }

        try
        {
            // Write CoBOC templates.
            tlLogger.debug("Writing file system templates.");
            thTemplateHandler.setRootDirectory(fBaseDir);
            thTemplateHandler.convertObjectsToFiles();

            // Write CoBOC mappings.
            tlLogger.debug("Writing file system mappings.");
            mhMappingHandler.setRootDirectory(fBaseDir);
            mhMappingHandler.convertObjectsToFiles();

            // Write CoBOC mappings.
            tlLogger.debug("Writing file system content maps.");
            mhContentMapHandler.setRootDirectory(fBaseDir);
            mhContentMapHandler.convertObjectsToFiles();
        }
        catch (ContentException e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }
        finally
        {
            ncCollector.deleteNodes();
        }

        srmSoap.removeNomCollector();

        debug(cmtTask, "Done.");
    }

    /**
     * This method imports the content to CoBOC from the local
     * directory.
     *
     * @param cmtTask The task that is being executed.
     * @param cContent The content-definition
     * @param srmSoap The manager for sending soap-requests.
     */
    public void executeFileToEcx(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap)
    {
        // Create a new logger object that will be passed to handlers.
        AntTaskLogger tlLogger = new AntTaskLogger(cmtTask, cContent.getType());

        // Get the source directory.
        File fBaseDir = getInputDirectory(cmtTask, tlLogger, cContent);

        if (fBaseDir == null)
        {
            GeneralUtils.handleException("Input directory not set for the CoBOC content.");
            return;
        }

        tlLogger.debug("Import directory: " + fBaseDir.getAbsolutePath());

        // Create the CoBOC context and handler objects.
        CoBOCContext ccContext = new CoBOCContext(srmSoap);
        FolderHandler fhFolderHandler = new FolderHandler(ccContext);

        // Set the log object to context.
        ccContext.setLogger(tlLogger);

        // Get the destination organization and user DN for CoBOC content.
        String sDestOrganization = GeneralUtils.getTrimmedProperty(cmtTask.getProject(), "coboc.dest.org");
        String sDestUserDN = GeneralUtils.getTrimmedProperty(cmtTask.getProject(), "coboc.dest.user");

        if (sDestOrganization != null)
        {
            tlLogger.debug("Setting CoBOC destination organization to " +
                           sDestOrganization);
            ccContext.setContentDestOrganization(sDestOrganization);
        }

        if (sDestUserDN != null)
        {
            tlLogger.debug("Setting CoBOC destination user DN to " +
                           sDestUserDN);
            ccContext.setContentDestUser(sDestUserDN);
        }

        // Write the content to organizational level.
        fhFolderHandler.setLevel("organization");

        // Set the folder filter
        fhFolderHandler.setFilterSet(cContent.getFilterSet());

        NomCollector ncCollector = new NomCollector();

        srmSoap.addNomCollector(ncCollector);

        // First read all folders from file system. Templates and mappings will
        // be read based on the folder structure.
        try
        {
            tlLogger.debug("Reading file sytem folders.");
            fhFolderHandler.setRootDirectory(fBaseDir);
            fhFolderHandler.convertObjectsFromFiles();
        }
        catch (ContentException e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }
        finally
        {
            ncCollector.deleteNodes();
        }

        // Create folder object handlers.
        TemplateHandler thTemplateHandler = new TemplateHandler(ccContext);
        MappingHandler mhMappingHandler = new MappingHandler(ccContext);
        ContentMappingHandler mhContentMapHandler = new ContentMappingHandler(ccContext);

        // Set the filter for objects.
        thTemplateHandler.setFilterSet(cContent.getFilterSet());
        mhMappingHandler.setFilterSet(cContent.getFilterSet());
        mhContentMapHandler.setFilterSet(cContent.getFilterSet());

        try
        {
            // Read file system templates.
            tlLogger.debug("Reading file system templates.");
            thTemplateHandler.setRootDirectory(fBaseDir);
            thTemplateHandler.convertObjectsFromFiles();

            // Read file system mappings.
            tlLogger.debug("Reading file system mappings.");
            mhMappingHandler.setRootDirectory(fBaseDir);
            mhMappingHandler.convertObjectsFromFiles();

            // Read file system mappings.
            tlLogger.debug("Reading file system content maps.");
            mhContentMapHandler.setRootDirectory(fBaseDir);
            mhContentMapHandler.convertObjectsFromFiles();
        }
        catch (ContentException e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }
        finally
        {
            ncCollector.deleteNodes();
        }

        // Write the folders to CoBOC.
        try
        {
            tlLogger.debug("Writing CoBOC folders.");
            fhFolderHandler.convertObjectsToEcx();
        }
        catch (ContentException e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }
        finally
        {
            ncCollector.deleteNodes();
        }

        try
        {
            // Write CoBOC templates.
            tlLogger.debug("Writing CoBOC templates.");
            thTemplateHandler.convertObjectsToEcx();
            thTemplateHandler.convertTemplateAttributesToECX();

            // Write CoBOC mappings.
            tlLogger.debug("Writing CoBOC mappings.");
            mhMappingHandler.convertObjectsToEcx();

            // Write CoBOC mappings.
            tlLogger.debug("Writing CoBOC content maps.");
            mhContentMapHandler.convertObjectsToEcx();
        }
        catch (ContentException e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, cmtTask);
            return;
        }
        finally
        {
            ncCollector.deleteNodes();
        }

        srmSoap.removeNomCollector();

        tlLogger.debug("Done.");
    }

    /**
     * This method should take care of publishing the specific content
     * to runtime. This content does not need any publishing.
     *
     * @param cmtTask The current contentmanager task.
     * @param cContent The specific content that needs to be published.
     * @param srmSoap The object to use for sending soap messages.
     */
    public void executePublishToRuntime(ContentManagerTask cmtTask,
                                        Content cContent,
                                        ISoapRequestManager srmSoap)
    {
        //This target does not need any publishing.
        cmtTask.log("Content of type " + cContent.getType() +
                    " does not need any publishing to runtime.",
                    Project.MSG_INFO);
    }

    /**
     * 
     * @see com.cordys.tools.ant.cm.ContentHandler#getNumberOfProcessedItems()
     */
    public int getNumberOfProcessedItems()
    {
        // Not implemented.
        return -1;
    }

    /**
     * This method parses the content of an IsvPackage and reads it
     * into the local filesystem.
     *
     * @param ccContext CoBOC context
     * @param cmtTask BF content manager task.
     * @param cContent CoBOC content element.
     * @param srmSoap BF soap request manager.
     */
    protected void executeExcIsvToFile(CoBOCContext ccContext,
                                       ContentManagerTask cmtTask,
                                       Content cContent,
                                       ISoapRequestManager srmSoap)
    {
        int iIsvContents;

        // Load the ISV package contents
        iIsvContents = getIsvPackageContents(cmtTask.getIsvdn(), cmtTask,
                                             srmSoap);

        try
        {
            // Parse folders
            loadIsvObjects(ccContext, iIsvContents,
                           "?<CPCImporter><FolderContent><folder><tuple>",
                           Folder.class);
        }
        catch (Exception e)
        {
            GeneralUtils.handleException("Error while parsing folders. " +
                                         e.getMessage(), e, cmtTask);
            return;
        }

        try
        {
            // Parse templates
            loadIsvObjects(ccContext, iIsvContents,
                           "?<CPCImporter><TemplateContent><template><tuple>",
                           Template.class);
        }
        catch (Exception e)
        {
            GeneralUtils.handleException("Error while parsing templates. " +
                                         e.getMessage(), e, cmtTask);
            return;
        }

        try
        {
            // Parse template attributes
            loadIsvTemplateAttributes(ccContext, iIsvContents,
                                      "?<CPCImporter><SpecialAttributeContent><attribute_template>");
        }
        catch (Exception e)
        {
            GeneralUtils.handleException("Error while parsing templates attributes." +
                                         e.getMessage(), e, cmtTask);
            return;
        }

        try
        {
            // Parse mappings
            loadIsvObjects(ccContext, iIsvContents,
                           "?<CPCImporter><MappingContent><map><tuple>",
                           Mapping.class);
        }
        catch (Exception e)
        {
            GeneralUtils.handleException("Error while parsing mappings. " +
                                         e.getMessage(), e, cmtTask);
            return;
        }

        try
        {
            // Parse content maps
            loadIsvObjects(ccContext, iIsvContents,
                           "?<CPCImporter><ContentMapContent><contentmap><tuple>",
                           ContentMapping.class);
        }
        catch (Exception e)
        {
            GeneralUtils.handleException("Error while parsing content maps. " +
                                         e.getMessage(), e, cmtTask);
            return;
        }
    }

    /**
     * Returns the file system input directory from the build
     * configuration file for this task.
     *
     * @param cmtTask The Ant task object.
     * @param tlLogger The logger object for writing log messages.
     * @param cContent The content element from configuration file.
     *
     * @return The input directory or null if not found.
     */
    protected File getInputDirectory(ContentManagerTask cmtTask,
                                     AntTaskLogger tlLogger, Content cContent)
    {
        File fInputDir = null;

        // Get the input directory from the content tag if it is present.
        fInputDir = cContent.getDir();

        if (fInputDir != null)
        {
            return fInputDir;
        }

        return null;
    }

    /**
     * Retreives contents of an ISV package that is installed in ECX.
     *
     * @param sIsvDN ISV package DN
     * @param cmtTask Current Ant task.
     * @param srmSoap Soap request manager used for SOAP messages
     *
     * @return ISV package content XML.
     */
    protected int getIsvPackageContents(String sIsvDN,
                                        ContentManagerTask cmtTask,
                                        ISoapRequestManager srmSoap)
    {
        cmtTask.log("Retrieving ISV package contents...", Project.MSG_DEBUG);

        srmSoap.getDocument().createTextElement("dn", cmtTask.getIsvdn());

        //get the cn from the dn
        String cn = (sIsvDN.split(",")[0]).split("=")[1];

        int fileNode = srmSoap.getDocument().createTextElement("file", cn);

        Node.setAttribute(fileNode, "type", "isvpackage");

        /*
         * Sends the request to the ISVPackageDefinition in order to get
         * the menu contents because no method is available to get the
         * menus of a particular ISV from XMLStore.
         */
        int iResponseMsg = 0;

        try
        {
            iResponseMsg = srmSoap.makeSoapRequest(cmtTask.getUserdn(),
                                                   cmtTask.getOrganization(),
                                                   "http://schemas.cordys.com/1.0/isvpackage",
                                                   "GetISVPackageDefinition",
                                                   fileNode);

            //check the response for Soap Fault.
            GeneralUtils.handleException(iResponseMsg);
        }
        catch (SoapRequestException e)
        {
            GeneralUtils.handleException("ISV package content request failed",
                                         e, cmtTask);
            return 0;
        }

        return iResponseMsg;
    }

    /**
     * Loads CoBOC object from ISV XML
     *
     * @param ccContext CoBOC context
     * @param iIsvNode ISV XML structure
     * @param sSearchPath Object search XPath
     * @param cObjectClass Class of the object to be created.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected void loadIsvObjects(CoBOCContext ccContext, int iIsvNode,
                                  String sSearchPath, Class<?> cObjectClass)
                           throws Exception
    {
        int[] iaNodes = Find.match(iIsvNode, sSearchPath);

        if ((iaNodes == null) || (iaNodes.length == 0))
        {
            return;
        }

        for (int i = 0; i < iaNodes.length; i++)
        {
            int iNode = iaNodes[i];
            Constructor<?> cObjConstr = cObjectClass.getConstructor(new Class[]
                                                                 {
                                                                     CoBOCContext.class
                                                                 });
            CoBOCObject coObject = (CoBOCObject) cObjConstr.newInstance(new Object[]
                                                                        {
                                                                            ccContext
                                                                        });

            coObject.convertFromECX(iNode);

            if (cObjectClass.equals(Folder.class))
            {
                Folder fFolder = (Folder) coObject;

                // Add the folder to the parent folder, if there is one. 
                if (fFolder.fParentFolder != null)
                {
                    fFolder.fParentFolder.addSubFolder(fFolder);
                }
            }
            else
            {
                // Add the object to the parent folder, if there is one. 
                if (coObject.getParentObject() != null)
                {
                    coObject.getParentObject().addChildObject(coObject);
                }
            }

            // Indicate to the context that this object has been added.
            ccContext.updateObject(coObject);
        }
    }

    /**
     * Loads template attributes from the ISV XML
     *
     * @param ccContext CoBOC context
     * @param iIsvNode ISV XML structure
     * @param sSearchPath Template attribute XPath
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected void loadIsvTemplateAttributes(CoBOCContext ccContext,
                                             int iIsvNode, String sSearchPath)
                                      throws Exception
    {
        int[] iaNodes = Find.match(iIsvNode, sSearchPath);

        if ((iaNodes == null) || (iaNodes.length == 0))
        {
            return;
        }

        for (int i = 0; i < iaNodes.length; i++)
        {
            int iNode = iaNodes[i];
            TemplateAttribute taAttrib = new TemplateAttribute(ccContext);
            int iTupleNode;

            // We have to create <tuple><old> around the attribute. Also clone the attribute
            // under the <old> node.
            iTupleNode = Node.getDocument(iIsvNode).createElement("tuple");
            Node.appendToChildren(Node.clone(iNode, true),
                                  Node.createElement("old", iTupleNode));

            try
            {
                taAttrib.convertFromECX(iTupleNode);
            }
            finally
            {
                Node.delete(iTupleNode);
            }

            // Add the object to the parent folder, if there is one. 
            if (taAttrib.getParentObject() != null)
            {
                taAttrib.getParentObject().addChildObject(taAttrib);
            }

            // Indicate to the context that this object has been added.
            ccContext.updateObject(taAttrib);
        }
    }

    /**
     * This method logs a debug-message.
     *
     * @param cmtTask The task to log to
     * @param sMessage The message to log.
     */
    private void debug(ContentManagerTask cmtTask, String sMessage)
    {
        cmtTask.log(sMessage, Project.MSG_VERBOSE);
    }

    /**
     * @see com.cordys.tools.ant.cm.ContentHandler#getSingleContentName(java.io.File, ContentManagerTask, Content, boolean)
     */
    public String getSingleContentName(File contentFile, ContentManagerTask cmtTask, Content content, boolean toEcx) throws IOException
    {
        // The old CoBOC handler does not support this.
        return null;
    }
}
