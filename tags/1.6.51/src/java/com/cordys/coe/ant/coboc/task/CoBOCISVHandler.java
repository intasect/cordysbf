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
import com.cordys.coe.ant.coboc.content.folders.Folder;
import com.cordys.coe.ant.coboc.content.folders.FolderCallback;
import com.cordys.coe.ant.coboc.content.folders.Mapping;
import com.cordys.coe.ant.coboc.content.folders.Template;
import com.cordys.coe.ant.coboc.content.folders.TemplateAttribute;

import com.cordys.tools.ant.isv.Content;
import com.cordys.tools.ant.isv.ISVContentHandler;
import com.cordys.tools.ant.isv.ISVContentHelper;
import com.cordys.tools.ant.isv.ISVCreatorTask;
import com.cordys.tools.ant.util.GeneralUtils;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import java.util.Iterator;

import org.apache.tools.ant.Project;

/**
 * Ant content handler for putting the CoBOC contents to an ISV file.
 *
 * @author mpoyhone
 */
public class CoBOCISVHandler extends ISVContentHelper
    implements ISVContentHandler
{
    /**
     * Message logged when the root tag of the content type is incorrect
     */
    private static final String ROOT_TAG_ERROR = "Root tag of CoBOC content should be ";
    /**
     * Message logged when xml handling related exception occurs
     */
    private static final String XML_ERROR = "Error occured while performing xml operation.";
    /**
     * The xml document that is used to handle xml functions.
     */
    private static Document dDocument;

    //private String isvFolderContentRootTag = "coboc_folders";
    /**
     * The namespace attribute value for tuples. Needed for some reason.
     */
    private static final String COBOC_TUPLE_NAMESPACE = "http://schemas.cordys.com/4.2/coboc";
    /**
     * The namespace attribute value for template attributes. Needed for some
     * reason.
     */
    private static final String COBOC_ATTRIBUTE_NAMESPACE = "http://schemas.cordys.com/1.0/coboc";
    /**
     * The CoBOC context object that will be used when processing the input
     * objects.
     */
    protected CoBOCContext ccContext;
    /**
     * Holds the contenttask for which the isv-xml should be generated.
     */
    private Content cContentTask;
    /**
     * The ant task that calls this handler.
     */
    private ISVCreatorTask ictTask = null;
    /**
     * The root tag of the CoBOC folder content type in the input XML file.
     */
    private String folderContentRootTag = "coboc_folders";
    /**
     * The xpath pattern to be matched in the content XML passed.
     */
    private String isvContentLoader = "com.cordys.cpc.deployment.loaders.DeploymentLoader";

    //private String isvContentLoader = "com.cordys.coe.ant.isvloader.CoBOCISVLoader";
    /**
     * The ISV root node description.
     */
    private String isvFolderContentDescription = "CoBOC Folder Contents";
    /**
     * The name of the root tag in the final ISV file for folder contents.
     */
    private String isvFolderContentRootTag = "CPCImporter";

    /**
     * Implementation of the abstract method getContentXML
     *
     * @param isvTask The instance of ISVCreatorTask class representing the tag
     *        'isv'.
     * @param contentTask The instance of ContentTask class representing the
     *        tag 'content'.
     * @param inputXML The content XML which has to be processed.
     * @param iCurrentIsvContentNode DOCUMENTME
     * @param iCurrentIsvPromptsetNode DOCUMENTME
     *
     * @return The content XML created by this content creator
     */
    public int[] getISVContentXML(ISVCreatorTask isvTask, Content contentTask,
                                  int inputXML, int iCurrentIsvContentNode,
                                  int iCurrentIsvPromptsetNode)
    {
        ictTask = isvTask;
        cContentTask = contentTask;
        dDocument = isvTask.getDocument();

        String sRootTagName = null;

        // Get the root tag from the input file.
        sRootTagName = Node.getName(inputXML);

        // Check that the root tag is valid.
        if (! folderContentRootTag.equals(sRootTagName))
        {
            GeneralUtils.handleException(ROOT_TAG_ERROR + "'" +
                                         folderContentRootTag + "'.");
        }

        ictTask.log("Adding CoBOC content.", Project.MSG_VERBOSE);

        // Create the CoBOC context and handler objects.
        ccContext = new CoBOCContext();

        int iContentRootNode = 0;

        try
        {
            // Replaces the file pointers with the XML content read from
            // the corresponding files.
            processExternalFileContents(isvTask, contentTask, inputXML, "",
                                        "<" + sRootTagName + "><content>");

            // Don't add any CoBOC nodes if we don't have any content.
            // Otherwise we will get the CoBOC loader screen for every ISV package.
            if (Node.getFirstElement(inputXML) == 0)
            {
                return null;
            }

            // Create the result ISV XML structure. 
            iContentRootNode = dDocument.createElement(isvFolderContentRootTag);
            Node.setAttribute(iContentRootNode, "loader", isvContentLoader);
            Node.setAttribute(iContentRootNode, "description",
                              isvFolderContentDescription);
            Node.setAttribute(iContentRootNode, "type", "ISV");
            Node.setAttribute(iContentRootNode, "url",
                              "/cordys/cpc/deployment/deploymentwizard.htm");

            // Find all CoBOC content nodes from the input XML structure
            // that now has the file contents inlined.
            int[] cobocContentNodes = Find.match(inputXML,
                                                 "<" + sRootTagName +
                                                 "><content><>");

            // Parse the content into the CoBOC context. 
            for (int i = 0; i < cobocContentNodes.length; i++)
            {
                int iOrigContentNode = cobocContentNodes[i];

                handleContentNode(iOrigContentNode);
            }

            int iTmpNode;

            // Create folders
            iTmpNode = dDocument.createElement("FolderContent", iContentRootNode);
            createFolderContentNodes(iTmpNode);

            // Create templates
            iTmpNode = dDocument.createElement("TemplateContent",
                                               iContentRootNode);
            createTemplateContentNodes(iTmpNode);

            // Create templates attributes
            iTmpNode = dDocument.createElement("SpecialAttributeContent",
                                               iContentRootNode);
            createTemplateAttributeContentNodes(iTmpNode);

            // Create mappings
            iTmpNode = dDocument.createElement("MappingContent",
                                               iContentRootNode);
            createMappingContentNodes(iTmpNode);

            // Create content maps
            iTmpNode = dDocument.createElement("ContentMapContent",
                                               iContentRootNode);
            createContentMapContentNodes(iTmpNode);
        }
        catch (Exception xe)
        {
            GeneralUtils.handleException(XML_ERROR + xe.getMessage(), xe,
                                         contentTask);
        }

        contentTask.log("Final Content Node:" +
                        Node.writeToString(iContentRootNode, true),
                        Project.MSG_DEBUG);

        return new int[] { iContentRootNode };
    }

    /**
     * Converts the CoBOC object to XML format that will be put to the ISV
     * package. This method handles the common operations for folders,
     * templates and mappings.
     *
     * @param coObject The object to be converted.
     *
     * @return The ISV package XML structure.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    protected int convertObjectToIsvFormat(CoBOCObject coObject)
                                    throws ContentException
    {
        int iResNode = coObject.convertToECX(ccContext.getDocument());
        int iEntityNode;
        int iNewNode;

        // Rename the <new> node to <old>
        if ((iNewNode = Find.firstMatch(iResNode, "?<new>")) != 0)
        {
            Node.setName(iNewNode, "old");
        }

        // Add the entity and parent ID's
        Node.setAttribute(iResNode, "entity_id", coObject.getEntityID());

        if ((iEntityNode = Find.firstMatch(iResNode, "?<ENTITY>")) != 0)
        {
            int iParentIdNode;
            String sParentId = coObject.getParentObject().getEntityID();

            // Set the entity ID.
            Node.createTextElement("ENTITY_ID", coObject.getEntityID(),
                                   iEntityNode);

            // Set the parent ID.
            if ((iParentIdNode = Node.getFirstDataNode(Find.firstMatch(iEntityNode,
                                                                           "<><PARENT_ID>"))) != 0)
            {
                Node.setData(iParentIdNode, sParentId);
            }
            else
            {
                Node.createTextElement("PARENT_ID", sParentId, iEntityNode);
            }
        }

        // Set the lastModified attribute
        if (Node.getAttribute(iResNode, "lastModified") == null)
        {
            Node.setAttribute(iResNode, "lastModified",
                              "" + System.currentTimeMillis());
        }

        // Set the tuple namespace attribute for some reason.
        Node.setAttribute(iResNode, "xmlns", COBOC_TUPLE_NAMESPACE);

        return iResNode;
    }

    /**
     * Creates the content map nodes under the given content map content root
     * node from the CoBOC context.
     *
     * @param iContentRootNode The content map content root node.
     */
    protected void createContentMapContentNodes(final int iContentRootNode)
                                         throws ContentException
    {
        Folder fRootFolder = ccContext.getRootFolder();

        if (fRootFolder == null)
        {
            throw new ContentException("Root folder is not set.");
        }

        FolderCallback fcHandler = new FolderCallback()
        {
            public boolean handleObject(CoBOCObject coObject)
                                 throws ContentException
            {
                // Check if the object has been read.
                if (!coObject.hasContent())
                {
                    // No, so no reason to write this.
                    return true;
                }

                ictTask.log("[" + cContentTask.getType() +
                            "] Adding content map " +
                            coObject.getEntityPathKey(), Project.MSG_INFO);

                int iResNode = convertObjectToIsvFormat(coObject);
                int iEntityNode;

                // Change version attribute to level
                if (Node.getAttribute(iResNode, "version") != null)
                {
                    Node.setAttribute(iResNode, "level",
                                      Node.getAttribute(iResNode, "version"));
                    Node.removeAttribute(iResNode, "version");
                }

                // Add a customkey field
                Node.setAttribute(iResNode, "customkey", coObject.getEntityID());

                // Add  entity_type="1010" attribute
                Node.setAttribute(iResNode, "entity_type", "1010");

                if ((iEntityNode = Find.firstMatch(iResNode, "?<ENTITY>")) != 0)
                {
                    // Set the CUSTOMKEY.
                    Node.createTextElement("CUSTOMKEY", coObject.getEntityID(),
                                           iEntityNode);

                    // Set the TEMPLATE_ID (for some reason this is the type)
                    Node.createTextElement("TEMPLATE_ID", "1010", iEntityNode);
                }

                // Add the tuple to the ISV XML.
                Node.appendToChildren(iResNode,
                                      Node.createElement("contentmap",
                                                         iContentRootNode));

                return true;
            }
        };

        fRootFolder.recurseFolderContentsBreadthFirst(FolderCallback.MASK_CONTENT_MAPPING,
                                                      fcHandler);
    }

    /**
     * Creates a new CoBOC entity ID.
     *
     * @return The created entity ID.
     */
    protected String createEntityId()
    {
        String sId;

        do
        {
            sId = System.currentTimeMillis() + "" +
                  ((int) (Math.random() * 100));
        }
        while (ccContext.getObjectById(sId) != null);

        return sId;
    }

    /**
     * Creates the folder nodes under the given folder content root node from
     * the CoBOC context.
     *
     * @param iContentRootNode The folder content root node.
     */
    protected void createFolderContentNodes(final int iContentRootNode)
                                     throws ContentException
    {
        Folder fRootFolder = ccContext.getRootFolder();

        if (fRootFolder == null)
        {
            throw new ContentException("Root folder is not set.");
        }

        FolderCallback fcHandler = new FolderCallback()
        {
            public boolean handleFolder(Folder fFolder)
                                 throws ContentException
            {
                // Write all folders except the root.
                if (fFolder.isRootFolder())
                {
                    return true;
                }

                ictTask.log("[" + cContentTask.getType() + "] Adding folder " +
                            fFolder.getEntityPathKey(), Project.MSG_INFO);

                int iResNode = convertObjectToIsvFormat(fFolder);

                // Add the tuple to the ISV XML.
                Node.appendToChildren(iResNode,
                                      Node.createElement("folder",
                                                         iContentRootNode));

                return true;
            }
        };

        fRootFolder.recurseFolderContentsBreadthFirst(FolderCallback.MASK_FOLDER,
                                                      fcHandler);
    }

    /**
     * Creates the mapping nodes under the given mapping content root node from
     * the CoBOC context.
     *
     * @param iContentRootNode The mapping content root node.
     */
    protected void createMappingContentNodes(final int iContentRootNode)
                                      throws ContentException
    {
        Folder fRootFolder = ccContext.getRootFolder();

        if (fRootFolder == null)
        {
            throw new ContentException("Root folder is not set.");
        }

        FolderCallback fcHandler = new FolderCallback()
        {
            public boolean handleObject(CoBOCObject coObject)
                                 throws ContentException
            {
                // Check if the object has been read.
                if (!coObject.hasContent())
                {
                    // No, so no reason to write this.
                    return true;
                }

                ictTask.log("[" + cContentTask.getType() + "] Adding mapping " +
                            coObject.getEntityPathKey(), Project.MSG_INFO);

                int iResNode = convertObjectToIsvFormat(coObject);
                int iEntityNode;

                // Change version attribute to level
                if (Node.getAttribute(iResNode, "version") != null)
                {
                    Node.setAttribute(iResNode, "level",
                                      Node.getAttribute(iResNode, "version"));
                    Node.removeAttribute(iResNode, "version");
                }

                // Add a customkey field
                Node.setAttribute(iResNode, "customkey", coObject.getEntityID());

                // Add  entity_type="1005" attribute
                Node.setAttribute(iResNode, "entity_type", "1005");

                if ((iEntityNode = Find.firstMatch(iResNode, "?<ENTITY>")) != 0)
                {
                    // Set the CUSTOMKEY.
                    Node.createTextElement("CUSTOMKEY", coObject.getEntityID(),
                                           iEntityNode);

                    // Set the TEMPLATE_ID (for some reason this is the type)
                    Node.createTextElement("TEMPLATE_ID", "1005", iEntityNode);
                }

                // Add the tuple to the ISV XML.
                Node.appendToChildren(iResNode,
                                      Node.createElement("map", iContentRootNode));

                return true;
            }
        };

        fRootFolder.recurseFolderContentsBreadthFirst(FolderCallback.MASK_MAPPING,
                                                      fcHandler);
    }

    /**
     * Creates the template attribute nodes under the given template attribute
     * content root node from the CoBOC context.
     *
     * @param iContentRootNode The template attribute content root node.
     */
    protected void createTemplateAttributeContentNodes(final int iContentRootNode)
                                                throws ContentException
    {
        Folder fRootFolder = ccContext.getRootFolder();

        if (fRootFolder == null)
        {
            throw new ContentException("Root folder is not set.");
        }

        FolderCallback fcHandler = new FolderCallback()
        {
            public boolean handleObject(CoBOCObject coObject)
                                 throws ContentException
            {
                // Check if the object has been read.
                if (!coObject.hasContent())
                {
                    // No, so no reason to write this.
                    return true;
                }

                Template tTemplate = (Template) coObject;

                if (tTemplate.getAttributeIterator() == null)
                {
                    // No attributes specified.
                    return true;
                }

                for (Iterator<?> iIter = tTemplate.getAttributeIterator();
                         iIter.hasNext();)
                {
                    TemplateAttribute tAttrib = (TemplateAttribute) iIter.next();
                    int iResNode;

                    iResNode = Find.firstMatch(tAttrib.convertToECX(ccContext.getDocument()),
                                               "?<attribute_template>");

                    if (iResNode == 0)
                    {
                        continue;
                    }

                    ictTask.log("[" + cContentTask.getType() +
                                "] Adding template attribute " +
                                tAttrib.sAttributeName, Project.MSG_INFO);

                    // Set the attribute namespace. Needed for some reason.
                    Node.setAttribute(iResNode, "xmlsn",
                                      COBOC_ATTRIBUTE_NAMESPACE);

                    // Create the attribute ID.
                    Node.createTextElement("attribtemplate_id",
                                           createEntityId(), iResNode);

                    // Add the tuple to the ISV XML.
                    Node.appendToChildren(iResNode, iContentRootNode);
                }

                return true;
            }
        };

        fRootFolder.recurseFolderContentsBreadthFirst(FolderCallback.MASK_TEMPLATE,
                                                      fcHandler);
    }

    /**
     * Creates the template nodes under the given template content root node
     * from the CoBOC context.
     *
     * @param iContentRootNode The template content root node.
     */
    protected void createTemplateContentNodes(final int iContentRootNode)
                                       throws ContentException
    {
        Folder fRootFolder = ccContext.getRootFolder();

        if (fRootFolder == null)
        {
            throw new ContentException("Root folder is not set.");
        }

        FolderCallback fcHandler = new FolderCallback()
        {
            public boolean handleObject(CoBOCObject coObject)
                                 throws ContentException
            {
                // Check if the object has been read.
                if (!coObject.hasContent())
                {
                    // No, so no reason to write this.
                    return true;
                }

                ictTask.log("[" + cContentTask.getType() +
                            "] Adding template " + coObject.getEntityPathKey(),
                            Project.MSG_INFO);

                int iResNode = convertObjectToIsvFormat(coObject);

                // Add the tuple to the ISV XML.
                Node.appendToChildren(iResNode,
                                      Node.createElement("template",
                                                         iContentRootNode));

                return true;
            }
        };

        fRootFolder.recurseFolderContentsBreadthFirst(FolderCallback.MASK_TEMPLATE,
                                                      fcHandler);
    }

    /**
     * Convents the XML structure to the corresponsing CoBOC object instance.
     *
     * @param iNode The XML structure to be converted.
     *
     * @return The Created CoBOC object.
     */
    protected CoBOCObject handleContentNode(int iNode)
    {
        int iTupleNode;
        boolean bIsFolder = false;
        boolean bIsTemplate = false;
        boolean bIsMapping = false;
        boolean bIsContentMapping = false;

        // Find the tuple node from the XML.
        if (Folder.isFolder(iNode))
        {
            bIsFolder = true;
            iTupleNode = iNode;
        }
        else if (Template.getTemplateTuple(iNode) != 0)
        {
            bIsTemplate = true;
            iTupleNode = Template.getTemplateTuple(iNode);
        }
        else if (Mapping.isMapping(iNode))
        {
            bIsMapping = true;
            iTupleNode = iNode;
        }
        else if (ContentMapping.isContentMapping(iNode))
        {
            bIsContentMapping = true;
            iTupleNode = iNode;
        }
        else
        {
            throw new IllegalArgumentException("Unknown CoBOC content node.");
        }

        // Get the path key attribute from tuple element.
        String sPathKey = Node.getAttribute(iTupleNode, "key");

        if ((sPathKey == null) || sPathKey.equals(""))
        {
            throw new IllegalArgumentException("No key attribute in file");
        }

        // Try to locate the template instance or create a new one.
        CoBOCObject coObject = null;

        if (bIsFolder)
        {
            coObject = Folder.findFolderByPathKey(ccContext, sPathKey, true);
        }
        else if (bIsTemplate)
        {
            coObject = Template.findTemplateByPathKey(ccContext, sPathKey, true);
        }
        else if (bIsMapping)
        {
            coObject = Mapping.findMappingByPathKey(ccContext, sPathKey, true);
        }
        else if (bIsContentMapping)
        {
            coObject = ContentMapping.findContentMappingByPathKey(ccContext,
                                                                  sPathKey, true);
        }

        // Read the object data from the XML structure.
        try
        {
            coObject.convertFromFile(iNode);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(e.getMessage());
        }

        // Create a new entity ID if necessary.
        if (coObject.getEntityID() == null)
        {
            coObject.setEntityID(createEntityId());
        }

        // Add the object to the parent folder, if there is one. 
        if (coObject.getParentObject() != null)
        {
            CoBOCObject coParent = coObject.getParentObject();

            coParent.addChildObject(coObject);

            if (!bIsFolder && (coParent instanceof Folder))
            {
                Folder fParent = (Folder) coParent;

                fParent.createParentFolders();

                // Create a new entity ID if necessary.
                while ((fParent != null) && !fParent.isRootFolder())
                {
                    if (fParent.getEntityID() == null)
                    {
                        fParent.setEntityID(createEntityId());
                        ccContext.updateObject(fParent);
                    }

                    fParent = (Folder) fParent.getParentObject();
                }
            }
        }

        // Indicate to the context that this template has been updated.
        ccContext.updateObject(coObject);

        return coObject;
    }
}
