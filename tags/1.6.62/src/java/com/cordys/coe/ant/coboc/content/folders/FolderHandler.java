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
package com.cordys.coe.ant.coboc.content.folders;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

import com.cordys.coe.ant.coboc.CoBOCConstants;
import com.cordys.coe.ant.coboc.content.CoBOCContext;
import com.cordys.coe.ant.coboc.content.CoBOCObject;
import com.cordys.coe.ant.coboc.content.CoBOCObjectHandler;
import com.cordys.coe.ant.coboc.content.ContentException;
import com.cordys.coe.ant.coboc.util.CoBOCUtils;
import com.cordys.tools.ant.cm.FilterSet;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Class for reading and writing CoBOC and file folder objects. The Folder
 * class is the container for individual folders and this class contains the
 * implementations.
 *
 * @author mpoyhone
 */
public class FolderHandler
    implements CoBOCObjectHandler
{
    /**
     * The CoBOC context containing the folders.
     */
    protected CoBOCContext ccContext;
    /**
     * The CoBOC content root directory on file system.
     */
    protected File fFolderRootDirectory;
    /**
     * The CoBOC level to be used for reading and writing content.
     */
    protected String sLevel = "organization";
    /**
     * The filter used for folders.
     */
    private FilterSet fsFolderFilterSet;

    /**
     * Creates a new FolderHandler object.
     *
     * @param ccContext The CoBOC content to be used.
     */
    public FolderHandler(CoBOCContext ccContext)
    {
        this.ccContext = ccContext;
    }

    /**
     * Defined to satify CoBOCObjectHandler interface. Not implemented.
     *
     * @return Nothing.
     *
     * @throws UnsupportedOperationException Indicated that this method is not
     *         implemented.
     */
    public String getCoBOCEntityFilter()
    {
        throw new UnsupportedOperationException("getCoBOCEntityFilter is not implemented.");
    }

    /**
     * Defined to satify CoBOCObjectHandler interface. Not implemented.
     *
     * @return Nothing.
     *
     * @throws UnsupportedOperationException Indicated that this method is not
     *         implemented.
     */
    public String getCoBOCEntityType()
    {
        throw new UnsupportedOperationException("getCoBOCEntityType is not implemented.");
    }

    /**
     * Sets the filter set.
     *
     * @param fsFilterSet The filter set to be set for this handler.
     */
    public void setFilterSet(FilterSet fsFilterSet)
    {
        fsFolderFilterSet = fsFilterSet;
    }

    /**
     * Returns the filter set.
     *
     * @return The filter set used by this handler.
     */
    public FilterSet getFilterSet()
    {
        return fsFolderFilterSet;
    }

    /**
     * Set the CoBOC level that will be used for reading and writing. Possible
     * values are "organization", "isv" or "user.
     *
     * @param sLevel The CoBOC level to be used.
     */
    public void setLevel(String sLevel)
    {
        this.sLevel = sLevel;
    }

    /**
     * Returns the CoBOC level that is used for reading and writing. Possible
     * values are "organization", "isv" or "user.
     *
     * @return The CoBOC level.
     */
    public String getLevel()
    {
        return sLevel;
    }

    /**
     * Sets the CoBOC content root directory on the file system.
     *
     * @param fRootDir The root directory.
     */
    public void setRootDirectory(File fRootDir)
    {
        fFolderRootDirectory = fRootDir;
    }

    /**
     * Returns the CoBOC content root directory on the file system.
     *
     * @return The root directory.
     */
    public File getRootDirectory()
    {
        return fFolderRootDirectory;
    }

    /**
     * Defined to satify CoBOCObjectHandler interface. This is not implemented.
     *
     * @param iFileNode The XML structure.
     *
     * @return Nothing.
     *
     * @throws UnsupportedOperationException Indicated that this method is not
     *         implemented.
     */
    public int checkXmlFileType(int iFileNode)
    {
        throw new UnsupportedOperationException("checkXmlFileType is not implemented.");
    }

    /**
     * Called when the folders must be read from the ECX.
     *
     * @throws ContentException
     */
    public void convertObjectsFromEcx()
                               throws ContentException
    {
        readAllFoldersFromECX(sLevel);
    }

    /**
     * Called when the folders must be read from the file system.
     *
     * @throws ContentException
     */
    public void convertObjectsFromFiles()
                                 throws ContentException
    {
        if (fFolderRootDirectory == null)
        {
            throw new ContentException("Root directory is not set.");
        }

        readAllFoldersFromFileSystem(fFolderRootDirectory);
    }

    /**
     * Called when the folders must be written to the ECX.
     *
     * @throws ContentException
     */
    public void convertObjectsToEcx()
                             throws ContentException
    {
        Folder fRootFolder = ccContext.getRootFolder();

        if (fRootFolder == null)
        {
            throw new ContentException("Root folder is not set.");
        }

        writeAllFoldersToECX(fRootFolder);
    }

    /**
     * Called when the folders must be written to the file system.
     *
     * @throws ContentException
     */
    public void convertObjectsToFiles()
                               throws ContentException
    {
        Folder fRootFolder = ccContext.getRootFolder();

        if (fRootFolder == null)
        {
            throw new ContentException("Root folder is not set.");
        }

        if (fFolderRootDirectory == null)
        {
            throw new ContentException("Root directory is not set.");
        }

        writeAllFoldersToFileSystem(fFolderRootDirectory, fRootFolder);
    }

    /**
     * Deletes objects (templates or mappings) from the given folder.
     *
     * @param fFolder The folder that this delete operation is related to.
     * @param cfFilter The CoBOC content filter.
     * @param bDeleteFolder If true, this folder will be delete also.
     * @param bDeleteTemplates If true, templates will be deleted.
     * @param bDeleteMappings If true, mappings will be deleted.
     * @param bDeleteContentMaps If true, content maps will be deleted.
     *
     * @throws ContentException Thrown if the operation failed.
     */
    public void deleteFolderObjects(Folder fFolder, FilterSet cfFilter,
                                    boolean bDeleteFolder,
                                    boolean bDeleteTemplates,
                                    boolean bDeleteMappings,
                                    boolean bDeleteContentMaps)
                             throws ContentException
    {
        /* Folder deletion SOAP message :
           <UpdateXMLObject xmlns="http://schemas.cordys.com/1.0/coboc">
              <tuple lastModified="1095240467122"
                                     key="/testing/asd"
                                     version="organization"
                                     name="asd"
                                     isFolder="true" />
            </UpdateXMLObject>
        
           Template deletion SOAP message :
            <UpdateXMLObject xmlns="http://schemas.cordys.com/1.0/coboc">
               <tuple lastModified="1095238172069"
                                     key="/testing/aa"
                                    version="organization"
                                    name="aa" />
            </UpdateXMLObject>
        
           Mapping deletion SOAP message
            <UpdateXMLObject xmlns="http://schemas.cordys.com/1.0/coboc">
              <tuple lastModified="1095173313153"
                               key="/testing"
                               template="/MAP"
                               name="vghg"
                               version="organization">
                <old>
                  <ENTITY>
                    <ENTITY_ID>10951691839176</ENTITY_ID>
                  </ENTITY>
                </old>
              </tuple>
            </UpdateXMLObject>
         */

        // Check if this folder matches the filter.
        if (!cfFilter.isPathAccepted(fFolder.getEntityPathKey(), true))
        {
            return;
        }

        // Delete the folder contents. This lists the object types to be deleted
        // as well as the deletion order.
        Object[][] oaTypeList = 
                                {
                                    {
                                        "/CONTENTMAP", "instance",
                                        new Boolean(bDeleteContentMaps)
                                    },
                                    {
                                        "/MAP", "instance",
                                        new Boolean(bDeleteMappings)
                                    },
                                    {
                                        "template", "entity",
                                        new Boolean(bDeleteTemplates)
                                    }
                                };

        for (int i = 0; i < oaTypeList.length; i++)
        {
            String sObjectFilter = (String) oaTypeList[i][0];
            String sObjectType = (String) oaTypeList[i][1];
            Boolean bDeleteThis = (Boolean) oaTypeList[i][2];
            boolean bIsMapping = sObjectFilter.equals("/MAP") ||
                                 sObjectFilter.equals("/CONTENTMAP");
            boolean bIsTemplate = sObjectFilter.equals("template");
            int iNode;

            // Check if we should delete this type
            if (!bDeleteThis.booleanValue())
            {
                continue;
            }

            // Fetch folder contents. This returns 
            try
            {
                iNode = ccContext.cmGetCollection.execute(ccContext.getSoapManager(),
                                                          fFolder.getEntityPathKey(),
                                                          sObjectFilter,
                                                          sObjectType, sLevel,
                                                          ccContext.getSoapTimeout(),
                                                          ccContext.getLogger());

                if ((iNode == 0) || (Node.getNumChildren(iNode) == 0))
                {
                    throw new ContentException("Folder content reply was empty.");
                }

                iNode = Node.getFirstChild(iNode);
            }
            catch (Exception e)
            {
                // As we were able to read the folder before, this exception
                // possibly means that there is no mappings or templates.
                continue;
            }

            // Delete every tuple individually.
            while (iNode != 0)
            {
                int iReqTuple;
                Document dDoc = ccContext.getDocument();
                String sKey = Node.getAttribute(iNode, "key");

                if (!cfFilter.isPathAccepted(sKey))
                {
                    return;
                }

                // Create a new tuple from the gGetColletion response tuple.
                iReqTuple = dDoc.createElement("tuple");
                Node.setAttribute(iReqTuple, "lastModified",
                                  Node.getAttribute(iNode, "lastModified"));
                Node.setAttribute(iReqTuple, "key",
                                  Node.getAttribute(iNode, "key"));
                Node.setAttribute(iReqTuple, "name",
                                  Node.getAttribute(iNode, "name"));
                Node.setAttribute(iReqTuple, "version",
                                  Node.getAttribute(iNode, "level"));

                // Mappings need extra information.
                if (bIsMapping)
                {
                    int iTmpNode;

                    Node.setAttribute(iReqTuple, "template", sObjectFilter);
                    iTmpNode = dDoc.createElement("old", iReqTuple);
                    iTmpNode = dDoc.createElement("ENTITY", iTmpNode);
                    dDoc.createTextElement("ENTITY_ID",
                                           Node.getAttribute(iNode, "entity_id"),
                                           iTmpNode);
                }
                else if (bIsTemplate)
                {
                    // First we need to delete the template attributes.
                    deleteTemplateAttributesFromECX(Node.getAttribute(iNode,
                                                                      "entity_id"));
                }

                ccContext.getLogger().info("Deleting object " +
                                           Node.getAttribute(iReqTuple, "key"));

                try
                {
                    // Send the deletion request. If it failes, this throws a SOAPException
                    ccContext.cmUpdateXMLObject.execute(ccContext.getSoapManager(),
                                                        iReqTuple,
                                                        ccContext.getSoapTimeout(),
                                                        ccContext.getLogger());
                }
                catch (Exception e)
                {
                    throw new ContentException("Unable to send the deletion request.",
                                               e);
                }

                iNode = Node.getNextSibling(iNode);
            }
        }

        // Delete folder 
        if (bDeleteFolder && !fFolder.isRootFolder())
        {
            int iReqTuple;
            Document dDoc = ccContext.getDocument();

            if (!cfFilter.isPathAccepted(fFolder.getEntityPathKey()))
            {
                return;
            }

            // Create a new tuple from the gGetColletion response tuple.
            iReqTuple = dDoc.createElement("tuple");
            Node.setAttribute(iReqTuple, "lastModified", fFolder.sLastModified);
            Node.setAttribute(iReqTuple, "key", fFolder.getEntityPathKey());
            Node.setAttribute(iReqTuple, "name", fFolder.sName);
            Node.setAttribute(iReqTuple, "version", fFolder.sLevel);
            Node.setAttribute(iReqTuple, "isFolder", "true");

            ccContext.getLogger().info("Deleting folder " +
                                       fFolder.getEntityPathKey());

            try
            {
                // Send the deletion request. If it failes, this throws a SOAPException
                ccContext.cmUpdateXMLObject.execute(ccContext.getSoapManager(),
                                                    iReqTuple,
                                                    ccContext.getSoapTimeout(),
                                                    ccContext.getLogger());
            }
            catch (Exception e)
            {
                throw new ContentException("Unable to send the deletion request.",
                                           e);
            }
        }
    }

    /**
     * Called when folders and their contents need to be deleted from the ECX.
     *
     * @throws ContentException Thrown if the operation failed.
     */
    public void deleteObjectsFromEcx()
                              throws ContentException
    {
        if (fsFolderFilterSet == null)
        {
            throw new ContentException("Folder filter not set for delete operation. This would delete all CoBOC content!");
        }

        Folder fRootFolder = ccContext.getRootFolder();

        if (fRootFolder == null)
        {
            throw new ContentException("Root folder is not set.");
        }

        // Delete all content maps.
        FolderCallback fhContentMapCB = new FolderCallback()
        {
            public boolean handleFolder(Folder fFolder)
                                 throws ContentException
            {
                deleteFolderObjects(fFolder, fsFolderFilterSet, false, false,
                                    false, true);
                return true;
            }
        };
        fRootFolder.recurseFolderContentsBreadthFirst(FolderCallback.MASK_FOLDER,
                                                      fhContentMapCB);

        // Delete all mappings.
        FolderCallback fhMappingCB = new FolderCallback()
        {
            public boolean handleFolder(Folder fFolder)
                                 throws ContentException
            {
                deleteFolderObjects(fFolder, fsFolderFilterSet, false, false,
                                    true, false);
                return true;
            }
        };
        fRootFolder.recurseFolderContentsBreadthFirst(FolderCallback.MASK_FOLDER,
                                                      fhMappingCB);

        // Delete all templates.
        FolderCallback fhTemplateCB = new FolderCallback()
        {
            public boolean handleFolder(Folder fFolder)
                                 throws ContentException
            {
                deleteFolderObjects(fFolder, fsFolderFilterSet, false, true,
                                    false, false);
                return true;
            }
        };
        fRootFolder.recurseFolderContentsBreadthFirst(FolderCallback.MASK_FOLDER,
                                                      fhTemplateCB);

        // Delete all folders in depth first fashion.
        FolderCallback fhFolderCB = new FolderCallback()
        {
            public boolean handleFolder(Folder fFolder)
                                 throws ContentException
            {
                deleteFolderObjects(fFolder, fsFolderFilterSet, true, false,
                                    false, false);
                return true;
            }
        };
        fRootFolder.recurseFolderContentsDepthFirst(FolderCallback.MASK_FOLDER,
                                                    fhFolderCB);
    }

    /**
     * Defined to satify CoBOCObjectHandler interface. This method is not
     * implemented.
     *
     * @param sEntityId The object entity ID.
     * @param bCreate If true and the object was not found, a new one will be
     *        created.
     *
     * @return Nothing.
     *
     * @throws UnsupportedOperationException Indicated that this method is not
     *         implemented.
     */
    public CoBOCObject findObjectById(String sEntityId, boolean bCreate)
    {
        throw new UnsupportedOperationException("findObjectById is not implemented.");
    }

    /**
     * Defined to satify CoBOCObjectHandler interface. This method is not
     * implemented.
     *
     * @param sEntityId Nothing.
     * @param bCreate Nothing.
     *
     * @return Nothing.
     *
     * @throws UnsupportedOperationException Indicated that this method is not
     *         implemented.
     */
    public CoBOCObject findObjectByPathKey(String sEntityId, boolean bCreate)
    {
        throw new UnsupportedOperationException("findObjectByPathKey is not implemented.");
    }

    /**
     * Reads all folders from CoBOC based on the given parameters.
     *
     * @param sLevel The level from which the folders are read (e.g.
     *        "organization" or "isv").
     *
     * @return The root Folder object.
     */
    public Folder readAllFoldersFromECX(String sLevel)
                                 throws ContentException
    {
        Folder fRoot = ccContext.getRootFolder();

        if (fRoot == null)
        {
            throw new ContentException("Root folder is not set.");
        }

        recurseReadAllFoldersFormECX(sLevel, fRoot);

        return fRoot;
    }

    /**
     * Reads all folders from the file system.
     *
     * @param fRootDir The root directory.
     *
     * @return The root Folder object.
     */
    public Folder readAllFoldersFromFileSystem(File fRootDir)
                                        throws ContentException
    {
        Folder fRoot = ccContext.getRootFolder();

        recurseReadAllFoldersFormFileSystem(fRootDir, fRoot);

        return fRoot;
    }

    /**
     * Reads the folder content from CoBOC based on the folder locatotion XML
     * node (a tuple-element containing the key and entity ID) and returns the
     * read Folder object.
     *
     * @param iFolderTupleNode The SOAPWrapper that will be used to connect to
     *        the ECX.
     *
     * @return The Folder object containing the folder data. Can be a
     *         previously created object that was added to CoBOCContext.
     */
    public Folder readFolderFromECX(int iFolderTupleNode)
                             throws ContentException
    {
        Folder fFolder;

        // Get the folder entity ID from folder tuple node.
        String sEntityId = Node.getAttribute(iFolderTupleNode, "entity_id");

        // Locate the folder from context, or create a new one. This
        // also adds it to the context.
        fFolder = Folder.findFolderById(ccContext, sEntityId, true);

        // Read the folder data into the folder object
        fFolder.convertFromECX(iFolderTupleNode);

        // Add the folder to the parent folder, if there is one. 
        if (fFolder.fParentFolder != null)
        {
            fFolder.fParentFolder.addSubFolder(fFolder);
        }

        // Indicate to the context that this folder has been updated.
        ccContext.updateObject(fFolder);

        ccContext.getLogger().debug("Read folder " + fFolder.getEntityPathKey() +
                                   " with ID " + fFolder.getEntityID());
        ccContext.getLogger().debug("Read folder from ECX: " +
                                    fFolder.toString());

        return fFolder;
    }

    /**
     * Writes all folders to CoBOC.
     *
     * @param fFolder The roor directory to be written.
     *
     * @throws ContentException Thrown if the writing operation failed.
     */
    public void writeAllFoldersToECX(Folder fFolder)
                              throws ContentException
    {
        // Check if the filter matches this folder.
        if (!fFolder.isRootFolder() && (fsFolderFilterSet != null))
        {
            if (!fsFolderFilterSet.isPathAccepted(fFolder.getEntityPathKey(),
                                                      true))
            {
                // It doesn't match, so do not process this sub tree.
                return;
            }
        }

        // First write the folder to ECX. If this is the root folder, it is
        // not written.
        writeFolderToECX(fFolder);

        // Process every subfolder
        for (Iterator<Folder> iIter = fFolder.getSubFolderIterator(); iIter.hasNext();)
        {
            Folder fSubFolder = (Folder) iIter.next();

            // Process the subfolders.
            writeAllFoldersToECX(fSubFolder);
        }
    }

    /**
     * Writes all folders to file system under the given directory.
     *
     * @param fDir The directory that the folder and it's descendants are to be
     *        written to.
     * @param fFolder The folder to be written.
     *
     * @throws ContentException Thrown if the writing operation failed.
     */
    public void writeAllFoldersToFileSystem(File fDir, Folder fFolder)
                                     throws ContentException
    {
        // Check if the filter matches this folder.
        if (!fFolder.isRootFolder() && (fsFolderFilterSet != null))
        {
            if (!fsFolderFilterSet.isPathAccepted(fFolder.getEntityPathKey(),
                                                      true))
            {
                // It doesn't match, so do not process this sub tree.
                return;
            }
        }

        if (!fDir.exists())
        {
            // The directory doesn't exist, so we have to create it.
            if (!fDir.mkdir())
            {
                throw new ContentException("Unable to create directory " +
                                           fDir);
            }
        }

        // First write the info file for this folder. 
        writeFolderInfoFile(fFolder, fDir);

        ccContext.getLogger().info("Wrote folder " +
                                   fFolder.getEntityPathKey());

        // Process every subfolder
        for (Iterator<Folder> iIter = fFolder.getSubFolderIterator(); iIter.hasNext();)
        {
            Folder fSubFolder = (Folder) iIter.next();
            File fSubDir;

            // Create the subdirectory name
            fSubDir = new File(fDir, fSubFolder.sName);

            // Process the subfolders.
            writeAllFoldersToFileSystem(fSubDir, fSubFolder);
        }
    }

    /**
     * Writes the folder data to CoBOC. If the folder already exists its ID is
     * copied to the folder object and the folder data in CoBOC is updated.
     * This method also updates the folder object's entity ID field, so child
     * objects have the right parent folder ID when they are are updated.
     *
     * @param fFolder The folder to be updated.
     */
    public void writeFolderToECX(Folder fFolder)
                          throws ContentException
    {
        if (fFolder.isRootFolder())
        {
            // We cannot write the root folder.
            return;
        }

        int iOldFolderResponse = 0;
        String sFolderId = null;

        // Check if the folder already exists.
        try
        {
            iOldFolderResponse = ccContext.cmGetXMLObject.execute(ccContext.getSoapManager(),
                                                                  fFolder.getEntityPathKey(),
                                                                  "folder",
                                                                  "entity",
                                                                  sLevel,
                                                                  ccContext.getSoapTimeout(),
                                                                  ccContext.getLogger());

            // Find the folder tuple node.
            iOldFolderResponse = Find.firstMatch(iOldFolderResponse, "?<tuple>");

            if (iOldFolderResponse != 0)
            {
                // The folder exists and we can get its ID.
                sFolderId = Node.getAttribute(iOldFolderResponse, "entity_id");

                if ((sFolderId == null) || sFolderId.equals(""))
                {
                    throw new ContentException("Unable to get existing entity ID for folder " +
                                               fFolder.getEntityPathKey());
                }
            }
        }
        catch (SoapRequestException e)
        {
            // The folder did not exist, so do nothing here.
        }

        int iTupleNode;
        int iFolderNode;

        // Create the XML representation of the folder data.
        iFolderNode = fFolder.convertToECX(ccContext.getDocument());

        // Create the update message
        if (iOldFolderResponse != 0)
        {
            int iNewNode;

            // We had an old folder, so we use the tuple structure from the reply,
            // and just copy the folder's new-structure under it.
            iTupleNode = Node.clone(iOldFolderResponse, true);
            iNewNode = Find.firstMatch(iFolderNode, "?<new>");

            if (iNewNode == 0)
            {
                throw new ContentException("Folder message has no new-node");
            }

            // Add the new node to the message tuple.
            Node.appendToChildren(Node.clone(iNewNode, true), iTupleNode);

            // We don't need the folder data anymore, so add it to the garbage list.
            ccContext.getSoapManager().addNomGarbage(iFolderNode);
        }
        else
        {
            // We just use this node as the message.
            iTupleNode = iFolderNode;
        }

        int iResponse;

        // Call the update method
        try
        {
            iResponse = ccContext.cmUpdateXMLObject.execute(ccContext.getSoapManager(),
                                                            iTupleNode,
                                                            ccContext.getSoapTimeout(),
                                                            ccContext.getLogger());
        }
        catch (SoapRequestException e)
        {
            throw new ContentException("Unable to update the folder : " + e);
        }

        // Find the folder tuple node.
        iResponse = Find.firstMatch(iResponse, "?<tuple>");

        if (iResponse == 0)
        {
            throw new ContentException("No tuple node in the folder response.");
        }

        // Get the ID attribute from the response tuple node.
        sFolderId = Node.getAttribute(iResponse, "entity_id");

        if (sFolderId == null)
        {
            throw new ContentException("Unable to get folder entity ID from response.");
        }

        // Set folder's ID and update the folder in the context.
        fFolder.setEntityID(sFolderId);
        ccContext.updateObject(fFolder);

        ccContext.getLogger().info("Wrote folder " +
                                   fFolder.getEntityPathKey() + " with ID " +
                                   fFolder.getEntityPathKey());
    }

    /**
     * Deletes the template attributes from ECX.
     *
     * @param sTemplateId The Template ID to be deleted.
     *
     * @throws ContentException Thrown if the deletion failed.
     */
    protected void deleteTemplateAttributesFromECX(String sTemplateId)
                                            throws ContentException
    {
        int[] iaTupleNodes;

        try
        {
            iaTupleNodes = ccContext.cmSelect.execute(ccContext.getSoapManager(),
                                                      CoBOCConstants.COBOC_ATTRIBUTE_TABLE_NAME,
                                                      CoBOCConstants.COBOC_ATTRIBUTE_FIELD_NAME,
                                                      sTemplateId,
                                                      ccContext.getSoapTimeout(),
                                                      ccContext.getLogger());

            for (int i = 0; i < iaTupleNodes.length; i++)
            {
                int iTuple = iaTupleNodes[i];

                ccContext.cmDelete.execute(ccContext.getSoapManager(),
                                           CoBOCConstants.COBOC_ATTRIBUTE_TABLE_NAME,
                                           iTuple, ccContext.getSoapTimeout(),
                                           ccContext.getLogger());
            }
        }
        catch (SoapRequestException e)
        {
            throw new ContentException(e);
        }
    }

    /**
     * Recursively reads all folders from CoBOC that are subfolders of the
     * given folder.
     *
     * @param sLevel The level from which the folders are read (e.g.
     *        "organization" or "isv").
     * @param fFolder The SOAPWrapper that will be used to connect to the ECX.
     */
    protected void recurseReadAllFoldersFormECX(String sLevel, Folder fFolder)
                                         throws ContentException
    {
        int iResponse;

        // Check if the filter matches this folder.
        if (!fFolder.isRootFolder() && (fsFolderFilterSet != null))
        {
            if (!fsFolderFilterSet.isPathAccepted(fFolder.getEntityPathKey(),
                                                      true))
            {
                // It doesn't match, so do not process this sub tree.
                return;
            }
        }

        // Send the request
        try
        {
            iResponse = ccContext.cmGetCollection.execute(ccContext.getSoapManager(),
                                                          fFolder.getEntityPathKey(),
                                                          "folder", "entity",
                                                          sLevel,
                                                          ccContext.getSoapTimeout());
        }
        catch (SoapRequestException e)
        {       	
            // Some of the folders might not be accessible, so this might
            // be a valid error.
        	String sMsg = e.toString();
        	
        	if (sMsg.indexOf("does not have any role") >= 0) {
        		throw new ContentException("Unable to send the soap request. ", e);
        	}

            return;
        }

        // Find the first folder node.
        int iTuple = Find.firstMatch(iResponse, "?<tuple>");

        while (iTuple != 0)
        {
            String sEntityId = Node.getAttribute(iTuple, "entity_id");
            String sPathKey = Node.getAttribute(iTuple, "key");
            Folder fSubFolder;

            int iFolderResponse;

            // Request the folder contents.
            try
            {
                iFolderResponse = ccContext.cmGetXMLObject.execute(ccContext.getSoapManager(),
                                                                   sPathKey,
                                                                   "folder",
                                                                   "entity",
                                                                   sLevel,
                                                                   ccContext.getSoapTimeout(),
                                                                   ccContext.getLogger());
            }
            catch (SoapRequestException e)
            {
                throw new ContentException("Unable to send the soap request. Folder ID=" +
                                           sEntityId + ", key=" + sPathKey +
                                           " : " + e);
            }

            // Find the folder tuple node.
            int iFolderTuple = Find.firstMatch(iFolderResponse, "?<tuple>");

            if (iFolderTuple == 0)
            {
                // Nothing found. Possibly an error occured, so skip this folder.
                iTuple = Node.getNextSibling(iTuple);
                continue;
            }

            // Create the folder and read folder data from the tuple.
            fSubFolder = readFolderFromECX(iFolderTuple);

            // Recurse to the subfolder
            recurseReadAllFoldersFormECX(sLevel, fSubFolder);

            // Continue with the next folder
            iTuple = Node.getNextSibling(iTuple);
        }
    }

    /**
     * Tries to locate folder's info file in the given directory and if it is
     * found, reads the data into the folder object.
     *
     * @param fFolder The folder object.
     * @param fDirectory The filesystem directory that the info file is to be
     *        searched.
     *
     * @return True, if the operation succeeded.
     */
    private boolean readFolderInfoFile(Folder fFolder, File fDirectory)
    {
        File fInfoFile = new File(fDirectory,
                                  CoBOCConstants.FN_FOLDER_INFO_FILENAME);

        if (!fInfoFile.exists())
        {
            return false;
        }

        int iInfoNode = CoBOCUtils.readXmlFileContents(fInfoFile,
                                                       ccContext.getDocument());

        if (iInfoNode == 0)
        {
            return false;
        }

        try
        {
            fFolder.convertFromFile(iInfoNode);
        }
        catch (ContentException e)
        {
            return false;
        }
        finally
        {
            Node.delete(iInfoNode);
        }

        return true;
    }

    /**
     * Reads all directories from the file system recursively.
     *
     * @param fRootDir The directory on the file system that is to be recursed.
     * @param fRoot The root folder object.
     */
    private void recurseReadAllFoldersFormFileSystem(File fRootDir, Folder fRoot)
                                              throws ContentException
    {
        // Check if the filter matches this folder.
        if (!fRoot.isRootFolder() && (fsFolderFilterSet != null))
        {
            if (!fsFolderFilterSet.isPathAccepted(fRoot.getEntityPathKey(), true))
            {
                // It doesn't match, so do not process this sub tree.
                return;
            }
        }

        // Try to locate the folder info file.
        if (readFolderInfoFile(fRoot, fRootDir))
        {
            // Add this as a child to the parent folder.
            if (fRoot.fParentFolder != null)
            {
                fRoot.fParentFolder.addSubFolder(fRoot);
            }

            ccContext.updateObject(fRoot);
        }

        ccContext.getLogger().debug("Read folder " + fRoot.getEntityPathKey());
        ccContext.getLogger().debug("Read folder from fs: " + fRoot.toString());

        // Create a filter that returns only directories.
        FileFilter ffDirFilter = new FileFilter()
        {
            public boolean accept(File fPathname)
            {
                return fPathname.isDirectory();
            }
        };

        // List all directories undir this directory.
        File[] faSubFolders = fRootDir.listFiles(ffDirFilter);

        for (int iIndex = 0; iIndex < faSubFolders.length; iIndex++)
        {
            File fSubDir = faSubFolders[iIndex];
            Folder fSubFolder;
            String sSubFolderName;

            // Create the entity path key.
            if (!fRoot.getEntityPathKey().equals("/"))
            {
                sSubFolderName = fRoot.getEntityPathKey() + "/" +
                                 fSubDir.getName();
            }
            else
            {
                sSubFolderName = "/" + fSubDir.getName();
            }

            // Find the folder or create it if it doesn't exist.
            fSubFolder = Folder.findFolderByPathKey(ccContext, sSubFolderName,
                                                    true);

            // Process the subdirectory
            recurseReadAllFoldersFormFileSystem(fSubDir, fSubFolder);
        }
    }

    /**
     * Creates folder's info file in the given directory and writes the folder
     * data into it.
     *
     * @param fFolder The folder object.
     * @param fDirectory The filesystem directory that the info file is to be
     *        created.
     *
     * @return True if the operation succeeded.
     */
    private boolean writeFolderInfoFile(Folder fFolder, File fDirectory)
                                 throws ContentException
    {
        File fInfoFile = new File(fDirectory,
                                  CoBOCConstants.FN_FOLDER_INFO_FILENAME);
        int iFolderNode;

        iFolderNode = fFolder.convertToFile(ccContext.getDocument());

        if (!CoBOCUtils.writeXmlContentsToFile(iFolderNode, fInfoFile))
        {
            Node.delete(iFolderNode);
            throw new ContentException("Unable to write folder info file " +
                                       fInfoFile);
        }

        Node.delete(iFolderNode);

        return true;
    }
}
