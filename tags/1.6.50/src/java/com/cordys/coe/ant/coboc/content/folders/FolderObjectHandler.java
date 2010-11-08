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

import com.cordys.coe.ant.coboc.content.CoBOCContext;
import com.cordys.coe.ant.coboc.content.CoBOCObject;
import com.cordys.coe.ant.coboc.content.CoBOCObjectHandler;
import com.cordys.coe.ant.coboc.content.ContentException;
import com.cordys.coe.ant.coboc.util.CoBOCUtils;
import com.cordys.tools.ant.cm.FilterSet;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Class for reading and writing CoBOC and file object objects (e.g. templates
 * and mappings). The CoBOCObject classes are the containers for individual
 * objects and this class contains the implementation to transfer them between
 * files and CoBOC.
 *
 * @author mpoyhone
 */
public abstract class FolderObjectHandler
    implements CoBOCObjectHandler
{
    /**
     * The CoBOC context containing the folder objects.
     */
    protected CoBOCContext ccContext;
    /**
     * The root directory on file system to write the files or to read the
     * files from.
     */
    protected File fRootDirectory;
    /**
     * The CoBOC level that the objects are read from or written to. Possible
     * values are "organization", "isv" or "user".
     */
    protected String sLevel = "organization";
    /**
     * The filter used for folder objects.
     */
    private FilterSet fsObjectFilterSet;

    /**
     * Creates a new FolderObjectHandler object.
     *
     * @param ccContext The CoBOC context for this handler.
     */
    public FolderObjectHandler(CoBOCContext ccContext)
    {
        this.ccContext = ccContext;
    }

    /**
     * Sets the filter set.
     *
     * @param fsFilterSet The filter set to be set for this handler.
     */
    public void setFilterSet(FilterSet fsFilterSet)
    {
        fsObjectFilterSet = fsFilterSet;
    }

    /**
     * Returns the filter set.
     *
     * @return The filter set used by this handler.
     */
    public FilterSet getFilterSet()
    {
        return fsObjectFilterSet;
    }

    /**
     * Sets the CoBOC level that the objects are read from or written to.
     * Possible values are "organization", "isv" or "user".
     *
     * @param sLevel The CoBOC level.
     */
    public void setLevel(String sLevel)
    {
        this.sLevel = sLevel;
    }

    /**
     * Returns the CoBOC level that the objects are read from or written to.
     * Possible values are "organization", "isv" or "user".
     *
     * @return The CoBOC level.
     */
    public String getLevel()
    {
        return sLevel;
    }

    /**
     * Sets the root directory the files from read from or written to.
     *
     * @param fRootDir he file system root directory.
     */
    public void setRootDirectory(File fRootDir)
    {
        fRootDirectory = fRootDir;
    }

    /**
     * Returns the root directory the files from read from or written to.
     *
     * @return The file system root directory.
     */
    public File getRootDirectory()
    {
        return fRootDirectory;
    }

    /**
     * Called when the objects must be read from the ECX.
     *
     * @throws ContentException Thrown if objects could not be read.
     */
    public void convertObjectsFromEcx()
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
                readFolderObjectsFormECX(sLevel, fFolder);
                return true;
            }
        };

        fRootFolder.recurseFolderContentsBreadthFirst(FolderCallback.MASK_FOLDER,
                                                      fcHandler);
    }

    /**
     * Called when the objectsmust be read from the file system.
     *
     * @throws ContentException Thrown if objects could not be read.
     */
    public void convertObjectsFromFiles()
                                 throws ContentException
    {
        if (fRootDirectory == null)
        {
            throw new ContentException("Root directory is not set.");
        }

        // Create a callback object that is called for every file found.
        CoBOCUtils.FileCallback fcHandler = new CoBOCUtils.FileCallback()
        {
            public boolean handleFile(File fFile)
                               throws Exception
            {
                // Try to read a object from the file. This will
                // do all the needed operations, so we don't need to manipulate the
                // returned object here.
                readObjectFromFile(fFile);

                return true;
            }
        };

        // Call the file system recursion utility method that will 
        // call the callback object.
        try
        {
            CoBOCUtils.recurseDirectoryTreeBreadthFirst(fRootDirectory,
                                                        fcHandler);
        }
        catch (Exception e)
        {
            throw new ContentException("Object read failed.", e);
        }
    }

    /**
     * Called when the objects must be written to the ECX.
     *
     * @throws ContentException Thrown if objects could not be written.
     */
    public void convertObjectsToEcx()
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

                writeObjectToECX(coObject);

                return true;
            }
        };

        fRootFolder.recurseFolderContentsBreadthFirst(getFolderObjectMask(),
                                                      fcHandler);
    }

    /**
     * Called when the objects must be written to the file system.
     *
     * @throws ContentException Thrown if the object write failed.
     */
    public void convertObjectsToFiles()
                               throws ContentException
    {
        Folder fRootFolder = ccContext.getRootFolder();

        if (fRootFolder == null)
        {
            throw new ContentException("Root folder is not set.");
        }

        if (fRootDirectory == null)
        {
            throw new ContentException("Root directory is not set.");
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

                File fTemplateFile;
                String sPath = coObject.getEntityPathKey();

                // Remove the '/' character from the beginning.
                if (sPath.startsWith("/"))
                {
                    sPath = sPath.substring(1);
                }

                // Add the file extension
                sPath += ".xml";

                // Create the template file name.
                fTemplateFile = new File(fRootDirectory, sPath);

                if (!writeObjectToFile(coObject, fTemplateFile))
                {
                    return false;
                }

                return true;
            }
        };

        fRootFolder.recurseFolderContentsBreadthFirst(getFolderObjectMask(),
                                                      fcHandler);
    }

    /**
     * Called when objects and their contents need to be deleted from the ECX.
     * All the template and mapping deletion is done is FolderHandler, so this
     * is not implemented.
     *
     * @throws ContentException Thrown if the operation failed.
     */
    public void deleteObjectsFromEcx()
                              throws ContentException
    {
        throw new UnsupportedOperationException("deleteObjectsFromEcx is not implemented");
    }

    /**
     * Reads the content from CoBOC based on the location XML node (a
     * tuple-element containing the key and entity ID) and returns the read
     * CoBOCObject object.
     *
     * @param iTupleNode The object XML node.
     *
     * @return The CoBOCObject object containing the data. Can be a previously
     *         created object that was added to CoBOCContext.
     */
    public CoBOCObject readObjectFromECX(int iTupleNode)
                                  throws ContentException
    {
        CoBOCObject coObject;

        // Get the entity ID from folder tuple node.
        String sEntityId = Node.getAttribute(iTupleNode, "entity_id");

        // Locate the object from context, or create a new one. This
        // also adds it to the context.
        coObject = findObjectById(sEntityId, true);

        // Read the object data into the object 
        coObject.convertFromECX(iTupleNode);

        // If this is a template and it has instances there also is a folder created
        // with the same key, so we just remove this folder before
        // adding the template.
        if (coObject instanceof Template)
        {
            CoBOCObject coInstanceFolder = ccContext.getObjectByPathKey(coObject.getEntityPathKey());

            if ((coInstanceFolder != null) &&
                    coInstanceFolder instanceof Folder)
            {
                // Remove the folder.
                ((Folder) coInstanceFolder).removeFromContext();
            }
        }

        // Add the object to the parent folder, if there is one. 
        if (coObject.getParentObject() != null)
        {
            coObject.getParentObject().addChildObject(coObject);
        }

        // Indicate to the context that this object has been updated.
        ccContext.updateObject(coObject);

        ccContext.getLogger().debug("\tRead object " +
                                   coObject.getEntityPathKey() + " with ID " +
                                   coObject.getEntityID());
        ccContext.getLogger().debug("\tRead object from ECX: " +
                                    coObject.toString());

        return coObject;
    }

    /**
     * Returns the bit mask that identifies the types of objects that are
     * fetched from folders. The mask values are defined in  FolderCallback
     * class.
     *
     * @return The bit mask indicating the types of objects this handler
     *         fetched from folders.
     */
    protected abstract int getFolderObjectMask();

    /**
     * Called when an object is being modified in CoBOC. This allows
     * sub-classes to modify the update request if necessary.
     *
     * @param xUpdateRequestNode The update request &lt;tuple; node that
     *        contains the &lt;new&gt; element.
     * @param xOldObjectNode Old object &tuple; node from CoBOC that contains
     *        the &lt;old&gt; or zero if it does not exists.
     *
     * @return The new update &lt;tuple&gt; node.
     *
     * @throws ContentException Thrown if the operation failed.
     */
    protected abstract int modifyUpdateRequest(int xUpdateRequestNode,
                                               int xOldObjectNode)
                                        throws ContentException;

    /**
     * Reads all objects from a CoBOC folder.
     *
     * @param sLevel The level from which the objects are read (e.g.
     *        "organization" or "isv").
     * @param fFolder The CoBOC folder that will be read.
     */
    protected void readFolderObjectsFormECX(String sLevel, Folder fFolder)
                                     throws ContentException
    {
        // Don't read objects from the root folder.
        if (fFolder.isRootFolder())
        {
            return;
        }

        // Check if the filter matches this object.
        if (fsObjectFilterSet != null)
        {
            if (!fsObjectFilterSet.isPathAccepted(fFolder.getEntityPathKey(),
                                                      true))
            {
                // It doesn't match, so do not process this sub tree.
                return;
            }
        }

        int iResponse;

        // Send the request
        try
        {
            iResponse = ccContext.cmGetCollection.execute(ccContext.getSoapManager(),
                                                          fFolder.getEntityPathKey(),
                                                          getCoBOCEntityFilter(),
                                                          getCoBOCEntityType(),
                                                          sLevel,
                                                          ccContext.getSoapTimeout());
        }
        catch (SoapRequestException e)
        {
            // Some of the objects might not be accessible, so this might
            // be a valid error.
            return;
        }

        // Find the first object tuple node.
        int iTuple = Find.firstMatch(iResponse, "?<tuple>");

        while (iTuple != 0)
        {
            String sEntityId = Node.getAttribute(iTuple, "entity_id");
            String sPathKey = Node.getAttribute(iTuple, "key");

            // Check if the filter matches this key.
            if (fsObjectFilterSet != null)
            {
                if (!fsObjectFilterSet.isPathAccepted(sPathKey))
                {
                    // It doesn't match, so do not process this element.
                    iTuple = Node.getNextSibling(iTuple);
                    continue;
                }
            }

            int iObjectResponse;

            // Request the folder contents.
            try
            {
                iObjectResponse = ccContext.cmGetXMLObject.execute(ccContext.getSoapManager(),
                                                                   sPathKey,
                                                                   getCoBOCEntityFilter(),
                                                                   getCoBOCEntityType(),
                                                                   sLevel,
                                                                   ccContext.getSoapTimeout(),
                                                                   ccContext.getLogger());
            }
            catch (SoapRequestException e)
            {
                throw new ContentException("Unable to send the soap request. Object ID=" +
                                           sEntityId + ", key=" + sPathKey +
                                           " : " + e);
            }

            // Find the tuple node.
            int iObjectTuple = Find.firstMatch(iObjectResponse, "?<tuple>");

            if (iObjectTuple == 0)
            {
                // Nothing found. Possibly an error occured, so skip this object.
                iTuple = Node.getNextSibling(iTuple);
                continue;
            }

            // Create the object and read object data from the tuple.
            readObjectFromECX(iObjectTuple);

            // Continue with the next object.
            iTuple = Node.getNextSibling(iTuple);
        }
    }

    /**
     * Reads the objects from a file.
     *
     * @param fObjectFile The file that is to be read and parsed.
     *
     * @return The object that contains the file contents or null of the file
     *         was empty.
     */
    private CoBOCObject readObjectFromFile(File fObjectFile)
                                    throws ContentException
    {
        if (!fObjectFile.exists())
        {
            throw new ContentException("File " + fObjectFile +
                                       " does not exist.");
        }

        // Read the file as an XML structure
        int iNode = CoBOCUtils.readXmlFileContents(fObjectFile,
                                                   ccContext.getDocument());
        int iTupleNode;

        if (iNode == 0)
        {
            return null;
        }

        // Check that is a objectfile.
        iTupleNode = checkXmlFileType(iNode);

        if (iTupleNode == 0)
        {
            Node.delete(iNode);
            return null;
        }

        // Get the path key attribute from tuple element.
        String sPathKey = Node.getAttribute(iTupleNode, "key");

        if ((sPathKey == null) || sPathKey.equals(""))
        {
            Node.delete(iNode);
            throw new ContentException("No key attribute in file " +
                                       fObjectFile);
        }

        // Check if the filter matches this object.
        if (fsObjectFilterSet != null)
        {
            if (!fsObjectFilterSet.isPathAccepted(sPathKey, false))
            {
                // It doesn't match, so do not process this object.
                return null;
            }
        }

        // Try to locate the template instance or create a new one.
        CoBOCObject coObject = findObjectByPathKey(sPathKey, true);

        // Read the template data from the XML structure.
        try
        {
            coObject.convertFromFile(iNode);
        }
        finally
        {
            Node.delete(iNode);
        }

        // Add the object to the parent folder, if there is one. 
        if (coObject.getParentObject() != null)
        {
            coObject.getParentObject().addChildObject(coObject);
        }

        // Indicate to the context that this template has been updated.
        ccContext.updateObject(coObject);

        ccContext.getLogger().debug("Read object " +
                                   coObject.getEntityPathKey());
        ccContext.getLogger().debug("\tRead object from FS: " +
                                    coObject.toString());

        return coObject;
    }

    /**
     * Writes the object data to CoBOC. If the object already exists, its ID is
     * copied to the object and the object data in CoBOC is updated. This
     * method also updates the object's entity ID field, so  objects refering
     * to this object have the right entity  ID when they are are updated.
     *
     * @param coObject The object to be updated.
     */
    private void writeObjectToECX(CoBOCObject coObject)
                           throws ContentException
    {
        // Check if the filter matches this object.
        if (fsObjectFilterSet != null)
        {
            if (!fsObjectFilterSet.isPathAccepted(coObject.getEntityPathKey(),
                                                      false))
            {
                // It doesn't match, so do not process this sub tree.
                return;
            }
        }

        int iOldResponse = 0;
        String sEntityId = null;

        // Check if the object already exists.
        try
        {
            iOldResponse = ccContext.cmGetXMLObject.execute(ccContext.getSoapManager(),
                                                            coObject.getEntityPathKey(),
                                                            getCoBOCEntityFilter(),
                                                            getCoBOCEntityType(),
                                                            sLevel,
                                                            ccContext.getSoapTimeout(),
                                                            ccContext.getLogger());

            // Find the tuple node.
            iOldResponse = Find.firstMatch(iOldResponse, "?<tuple>");

            if (iOldResponse != 0)
            {
                // The object exists and we can get its ID.
                sEntityId = Node.getAttribute(iOldResponse, "entity_id");

                if ((sEntityId == null) || sEntityId.equals(""))
                {
                    throw new ContentException("Unable to get existing entity ID for object  " +
                                               coObject.getEntityPathKey());
                }
            }
        }
        catch (SoapRequestException ignored)
        {
            // The object did not exist, so do nothing here.
        }

        int iTupleNode;
        int iObjectNode;

        // Create the XML representation of the object data.
        iObjectNode = coObject.convertToECX(ccContext.getDocument());

        // Call the sub-class to do the correct way of modifying the update request.
        iTupleNode = modifyUpdateRequest(iObjectNode, iOldResponse);

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
            throw new ContentException("Unable to update the object : ", e);
        }

        // Find the object tuple node from the response.
        iResponse = Find.firstMatch(iResponse, "?<tuple>");

        if (iResponse == 0)
        {
            throw new ContentException("No tuple node in the object response.");
        }

        // Get the ID attribute from the response tuple node.
        sEntityId = Node.getAttribute(iResponse, "entity_id");

        if (sEntityId == null)
        {
            throw new ContentException("Unable to get entity ID from response.");
        }

        // Set objects's ID and update the object in the context.
        coObject.setEntityID(sEntityId);
        ccContext.updateObject(coObject);

        ccContext.getLogger().info("Wrote object " +
                                   coObject.getEntityPathKey() + " with ID " +
                                   coObject.getEntityID());
    }

    /**
     * Creates objects's file to an XML structure and writes it to the file.
     *
     * @param coObject The object to be written.
     * @param fFile The destination file.
     *
     * @return True, if the writing succeeded.
     */
    private boolean writeObjectToFile(CoBOCObject coObject, File fFile)
                               throws ContentException
    {
        // Check if the filter matches this object.
        if (fsObjectFilterSet != null)
        {
            if (!fsObjectFilterSet.isPathAccepted(coObject.getEntityPathKey(),
                                                      false))
            {
                // It doesn't match, so do not process this object.
                return true;
            }
        }

        int iObjectNode;

        iObjectNode = coObject.convertToFile(ccContext.getDocument());

        if (iObjectNode == 0)
        {
            return false;
        }

        if (!CoBOCUtils.writeXmlContentsToFile(iObjectNode, fFile))
        {
            Node.delete(iObjectNode);
            throw new ContentException("Unable to write object info file " +
                                       fFile);
        }

        Node.delete(iObjectNode);

        ccContext.getLogger().info("Wrote object " +
                                   coObject.getEntityPathKey());

        return true;
    }
}
