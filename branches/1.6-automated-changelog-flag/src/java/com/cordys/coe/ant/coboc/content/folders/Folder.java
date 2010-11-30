/**
 * Copyright 2009 Cordys R&D B.V. 
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.cordys.coe.ant.coboc.CoBOCConstants;
import com.cordys.coe.ant.coboc.content.CoBOCContext;
import com.cordys.coe.ant.coboc.content.CoBOCObject;
import com.cordys.coe.ant.coboc.content.ContentException;
import com.cordys.coe.ant.coboc.util.CoBOCUtils;
import com.cordys.coe.ant.coboc.util.Serializer;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * Contains CoBOC folder object data and methods to convert it to ECX and file
 * formats.
 *
 * @author mpoyhone
 */
public class Folder
    implements CoBOCObject
{
    /**
     * The parent folder object
     */
    public Folder fParentFolder;
    /**
     * Folder description
     */
    public String sDescription;
    /**
     * Folder entity ID.
     */
    public String sEntityId;
    /**
     * Last modification time.
     */
    public String sLastModified;
    /**
     * CoBOC level value (isv, organization or user)
     */
    public String sLevel;
    /**
     * Folder name
     */
    public String sName;
    /**
     * Folder organization DN.
     */
    public String sOrganization;
    /**
     * Folder owner's user DN.
     */
    public String sOwner;
    /**
     * Folders  CoBOC key.
     */
    public String sPathKey;
    /**
     * The CoBOC context that this folder belong to.
     */
    protected CoBOCContext ccContext;
    /**
     * A list of subentities (e.g. templates and mappings).
     */
    protected List<CoBOCObject> lSubEntities = new LinkedList<CoBOCObject>();
    /**
     * A list of subfolders.
     */
    protected List<Folder> lSubFolders = new LinkedList<Folder>();
    /**
     * Indicates whether the data has been read to this object.
     */
    protected boolean bHasContent = false;
    /**
     * Indicates whether this is the root folder.
     */
    protected boolean bIsRootFolder = false;

    /**
     * Creates a new Folder object.
     *
     * @param ccContext The CoBOC context object.
     */
    public Folder(CoBOCContext ccContext)
    {
        this.ccContext = ccContext;
    }

    /**
     * Not implemented.
     *
     * @return null.
     */
    public Iterator<?> getDependencies()
    {
        return null;
    }

    /**
     * Sets the folder ID.
     *
     * @param sId The Folder ID.
     */
    public void setEntityID(String sId)
    {
        sEntityId = sId;
    }

    /**
     * Returns the Folder ID.
     *
     * @return The Folder ID.
     */
    public String getEntityID()
    {
        return sEntityId;
    }

    /**
     * Sets the folder CoBOC key.
     *
     * @param sPathKey The folder CoBOC key.
     */
    public void setEntityPathKey(String sPathKey)
    {
        this.sPathKey = sPathKey;
    }

    /**
     * Returns the folder CoBOC key.
     *
     * @return The folder CoBOC key.
     */
    public String getEntityPathKey()
    {
        return sPathKey;
    }

    /**
     * Check if this XML structure represents a CoBOC folder that was read from
     * the file system.
     *
     * @param iNode The XML structure read from the file.
     *
     * @return True if this is a template.
     */
    public static boolean isFolder(int iNode)
    {
        String sTuple = Node.getName(iNode);

        if ((sTuple == null) || !sTuple.equals("tuple"))
        {
            return false;
        }

        String sType = Node.getAttribute(iNode,
                                         CoBOCConstants.FILE_TYPE_ATTRIBUTE);

        if ((sType == null) || !sType.equals("folder"))
        {
            return false;
        }

        return true;
    }

    /**
     * Returns an iterator for subentity list.
     *
     * @return An iterator for subentity list.
     */
    public Iterator<CoBOCObject> getEntityIterator()
    {
        return lSubEntities.iterator();
    }

    /**
     * Sets the root folder flag for this folder.
     *
     * @param bValue The root folder flag value for this folder.
     */
    public void setIsRootFolder(boolean bValue)
    {
        bIsRootFolder = bValue;
    }

    /**
     * Sets the parent folder object for this folder.
     *
     * @param coParent The parent folder object for this folder.
     */
    public void setParentObject(CoBOCObject coParent)
    {
        if ((coParent != null) && !(coParent instanceof Folder))
        {
            throw new IllegalArgumentException(coParent + " is a not Folder.");
        }

        fParentFolder = (Folder) coParent;
    }

    /**
     * Returns the parent folder object for this folder.
     *
     * @return The parent folder object for this folder.
     */
    public CoBOCObject getParentObject()
    {
        return fParentFolder;
    }

    /**
     * Returns true if this is the root folder.
     *
     * @return true if this is the root folder.
     */
    public boolean isRootFolder()
    {
        return bIsRootFolder;
    }

    /**
     * Returns an interator for subfolder list.
     *
     * @return An interator for subfolder list.
     */
    public Iterator<Folder> getSubFolderIterator()
    {
        return lSubFolders.iterator();
    }

    /**
     * Adds a child object in this folder. The object can be a folder object
     * (e.g.  a mapping or a template) or a sub folder.
     *
     * @param coObject The child object to be added.
     */
    public void addChildObject(CoBOCObject coObject)
    {
        if (coObject instanceof Folder)
        {
            addSubFolder((Folder) coObject);
        }
        else
        {
            addEntity(coObject);
        }
    }

    /**
     * Adds a subentity (e.g.  a mapping or a template) to this folder.
     *
     * @param coEntity The entity to be added.
     */
    public void addEntity(CoBOCObject coEntity)
    {
        if (!lSubEntities.contains(coEntity))
        {
            lSubEntities.add(coEntity);
        }
    }

    /**
     * Adds a subfolder to this folder.
     *
     * @param fSubFolder The subfolder to be added.
     */
    public void addSubFolder(Folder fSubFolder)
    {
        if (!lSubFolders.contains(fSubFolder))
        {
            lSubFolders.add(fSubFolder);
        }
    }

    /**
     * Converts the folder from ECX format.
     *
     * @param iNode The XML structure in ECX format
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public void convertFromECX(int iNode)
                        throws ContentException
    {
        Serializer s = new Serializer();

        try
        {
            // Read tuple node attributes.
            sLastModified = s.readString(iNode, "@lastModified");
            sPathKey = s.readString(iNode, "@key");
            sName = s.readString(iNode, "@name");
            sLevel = s.readString(iNode, "@level");
            sEntityId = s.readString(iNode, "@entity_id");

            int iEntityNode;

            // Read the entity node path.
            iEntityNode = s.findNode(iNode, "old/ENTITY");

            sOrganization = s.readString(iEntityNode, "ORGANIZATION");
            sOwner = s.readString(iEntityNode, "OWNER");
            sDescription = s.readString(iEntityNode, "DESCRIPTION");

            String sParentId;

            // Get the parent folder from thet tuple.
            sParentId = s.readString(iEntityNode, "PARENT_ID");

            // Find the parent folder or create it with the given ID.
            if (sParentId != null)
            {
                fParentFolder = findFolderById(ccContext, sParentId, true);
            }

            bHasContent = true;
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to read the XML: " + e);
        }
    }

    /**
     * Converts the folder from file format.
     *
     * @param iNode The XML structure in file format
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public void convertFromFile(int iNode)
                         throws ContentException
    {
        Serializer s = new Serializer();

        try
        {
            // Read tuple node attributes.
            sLastModified = s.readString(iNode, "@lastModified");
            sPathKey = s.readString(iNode, "@key");
            sName = s.readString(iNode, "@name");
            sLevel = s.readString(iNode, "@level");

            int iEntityNode;

            // Read the entity node path.
            iEntityNode = s.findNode(iNode, "old/ENTITY");

            sOrganization = s.readString(iEntityNode, "ORGANIZATION");
            sOwner = s.readString(iEntityNode, "OWNER");
            sDescription = s.readString(iEntityNode, "DESCRIPTION");

            String sParentPathKey;

            // Get the parent folder from thet tuple.
            sParentPathKey = s.readString(iEntityNode, "PARENT_KEY");

            // Find the parent folder or create it with the given ID.
            if (sParentPathKey != null)
            {
                fParentFolder = findFolderByPathKey(ccContext, sParentPathKey,
                                                    true);
            }

            bHasContent = true;
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to read the XML: " + e);
        }
    }

    /**
     * Converts the folder to ECX format
     *
     * @param dDoc The XML document to be used for creating XML elements.
     *
     * @return The resulting XML structure in ECX format
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public int convertToECX(Document dDoc)
                     throws ContentException
    {
        Serializer s = new Serializer(dDoc);
        int iTupleNode;
        String sParentId;

        // Get the parent entity ID. We cannot get the root folder ID
        // so we set it to empty.
        if ((fParentFolder != null) && !fParentFolder.isRootFolder())
        {
            sParentId = fParentFolder.getEntityID();
        }
        else
        {
            sParentId = "";
        }

        try
        {
            iTupleNode = dDoc.createElement("tuple");

            // Write tuple node attributes.
            s.writeString(iTupleNode, "@key", sPathKey);
            s.writeString(iTupleNode, "@name", sName);
            s.writeString(iTupleNode, "@isFolder", "true");
            s.writeString(iTupleNode, "@level", sLevel);

            int iEntityNode;

            // Create the entity node path.
            iEntityNode = s.createNode(iTupleNode, "new/ENTITY");

            // Write the entity nodes.
            if ((sParentId != null) && !sParentId.equals(""))
            {
                s.writeString(iEntityNode, "PARENT_ID", sParentId);
            }

            //s.writeString(iEntityNode, "ORGANIZATION", sDestOrganization);
            //s.writeString(iEntityNode, "OWNER", sDestUserDN);
            s.writeString(iEntityNode, "DESCRIPTION", sDescription);
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to write the XML: " + e);
        }

        return iTupleNode;
    }

    /**
     * Converts the folder to file format
     *
     * @param dDoc The XML document to be used for creating XML elements.
     *
     * @return The resulting XML structure in file format
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public int convertToFile(Document dDoc)
                      throws ContentException
    {
        Serializer s = new Serializer(dDoc);
        int iTupleNode;

        try
        {
            iTupleNode = dDoc.createElement("tuple");

            // Write tuple node attributes.
            s.writeString(iTupleNode, "@lastModified", sLastModified);
            s.writeString(iTupleNode, "@key", sPathKey);
            s.writeString(iTupleNode, "@name", sName);
            s.writeString(iTupleNode, "@isFolder", "true");
            s.writeString(iTupleNode, "@level", sLevel);
            s.writeString(iTupleNode, "@" + CoBOCConstants.FILE_TYPE_ATTRIBUTE,
                          "folder");

            int iEntityNode;

            // Create the entity node path.
            iEntityNode = s.createNode(iTupleNode, "old/ENTITY");

            // Write the entity nodes.
            s.writeString(iEntityNode, "PARENT_KEY",
                          (fParentFolder != null)
                          ? fParentFolder.getEntityPathKey() : "");
            s.writeString(iEntityNode, "ORGANIZATION", sOrganization);
            s.writeString(iEntityNode, "OWNER", sOwner);
            s.writeString(iEntityNode, "DESCRIPTION", sDescription);
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to write the XML: " + e);
        }

        return iTupleNode;
    }

    /**
     * Finds a folder object by a specific entity ID
     *
     * @param ccContext The CoBOC content to be searched.
     * @param sId The entity ID.
     * @param bCreate Indicates whether a new object should be created if there
     *        is no exisiting one.
     *
     * @return The found folder object or null if no was present in the CoBOC
     *         context.
     */
    public static Folder findFolderById(CoBOCContext ccContext, String sId,
                                        boolean bCreate)
    {
        Folder fFolder = (Folder) ccContext.getObjectById(sId);

        if (fFolder == null)
        {
            // Check if we can create a new instance.
            if (!bCreate)
            {
                return null;
            }

            // Create a new instance.
            fFolder = new Folder(ccContext);
            fFolder.setEntityID(sId);

            // Add the instance to the context
            ccContext.updateObject(fFolder);
        }

        return fFolder;
    }

    /**
     * Finds a folder object by a specific CoBOC key.
     *
     * @param ccContext The CoBOC content to be searched.
     * @param sPathKey The CoBOC key.
     * @param bCreate Indicates whether a new object should be created if there
     *        is no exisiting one.
     *
     * @return The found folder object or null if no was present in the CoBOC
     *         context.
     */
    public static Folder findFolderByPathKey(CoBOCContext ccContext,
                                             String sPathKey, boolean bCreate)
    {
        Folder fFolder = (Folder) ccContext.getObjectByPathKey(sPathKey);

        if (fFolder == null)
        {
            // Check if we can create a new instance.
            if (!bCreate)
            {
                return null;
            }

            // Create a new instance.
            fFolder = new Folder(ccContext);
            fFolder.setEntityPathKey(sPathKey);

            // Add the instance to the context
            ccContext.updateObject(fFolder);
        }

        return fFolder;
    }

    /**
     * Creates all parent folders, if needed.
     */
    public void createParentFolders()
    {
        if (isRootFolder() || "/".equals(sPathKey))
        {
            // Root folder has not parents.
            return;
        }

        if (fParentFolder != null)
        {
            // Already created.
            return;
        }

        int iPos = sPathKey.lastIndexOf('/');

        if ((iPos < 0) || (iPos >= (sPathKey.length() - 1)))
        {
            // Invalid path.
            return;
        }

        String sParentKey = sPathKey.substring(0, iPos);

        if (sParentKey.length() == 0)
        {
            // Root folder.
            sParentKey = "/";
        }

        fParentFolder = findFolderByPathKey(ccContext, sParentKey, true);

        if (fParentFolder == null)
        {
            // Could not create.
            return;
        }

        fParentFolder.addChildObject(this);

        fParentFolder.createParentFolders();
    }

    /**
     * Compares this folder to another folder object.
     *
     * @param oFolder The Folder-object that this is to be compared with.
     *
     * @return True, if the folders have the same parameters.
     */
    public boolean equals(Object oFolder)
    {
        if (!(oFolder instanceof Folder))
        {
            return false;
        }

        // Get the other folder as Folder object
        Folder fOther = (Folder) oFolder;

        // Compare the simple parameters.
        if (!CoBOCUtils.equals(sEntityId, fOther.sEntityId) ||
                !CoBOCUtils.equals(sName, fOther.sName) ||
                !CoBOCUtils.equals(sDescription, fOther.sDescription) ||
                !CoBOCUtils.equals(sPathKey, fOther.sPathKey) ||
                !CoBOCUtils.equals(sLastModified, fOther.sLastModified) ||
                !CoBOCUtils.equals(sLevel, fOther.sLevel) ||
                !CoBOCUtils.equals(sOrganization, fOther.sOrganization) ||
                !CoBOCUtils.equals(sOwner, fOther.sOwner))
        {
            return false;
        }

        // If the parent folder in either object is null, then it must be
        // null in both.
        if ((fParentFolder == null) || (fOther.fParentFolder == null))
        {
            return (fParentFolder == null) && (fOther.fParentFolder == null);
        }

        // Parent folder entity id's must be equals
        if (!CoBOCUtils.equals(fParentFolder.getEntityID(),
                                   fOther.fParentFolder.getEntityID()))
        {
            return false;
        }

        // All the tests passed, so these folders are the same.
        return true;
    }

    /**
     * Returns true if the object data has been read.
     *
     * @return true if the object data has been read.
     */
    public boolean hasContent()
    {
        return bHasContent;
    }

    /**
     * Recurses the folder contents and calls the callback method for every
     * requested object. The types of objects can be controlled with the mask.
     * If folders are requested they are recursed in a breadth first,  i.e.
     * the folder contents are returned first and then subfolders are
     * recursed.
     *
     * @param iMask The bit mask defining the requested object types. Values
     *        are defined in
     * @param fcCallback The callback object.
     *
     * @return If the callback returned false, this will be also false.
     *
     * @throws ContentException Thrown from the callback method.
     *
     * @see FolderCallback
     */
    public boolean recurseFolderContentsBreadthFirst(int iMask,
                                                     FolderCallback fcCallback)
                                              throws ContentException
    {
        // Recurse the folder contents, i.e. templates, mapping, etc.
        if ((iMask & (~FolderCallback.MASK_FOLDER)) != 0)
        {
            for (Iterator<CoBOCObject> iIter = lSubEntities.iterator(); iIter.hasNext();)
            {
                CoBOCObject coObject = iIter.next();

                if (((iMask & FolderCallback.MASK_TEMPLATE) != 0) &&
                        coObject instanceof Template)
                {
                    if (!fcCallback.handleObject(coObject))
                    {
                        return false;
                    }
                }
                /*else if ((iMask & FolderCallback.MASK_PROCESS_TEMPLATE) != 0 &&
                   coObject instanceof ProcessTemplate) {
                   if (! fcCallback.handleProcessTemplate((ProcessTemplate) coObject)) {
                           return false;
                   }
                   }*/
                else if (((iMask & FolderCallback.MASK_MAPPING) != 0) &&
                             coObject instanceof Mapping)
                {
                    if (!fcCallback.handleObject(coObject))
                    {
                        return false;
                    }
                }
                else if (((iMask & FolderCallback.MASK_CONTENT_MAPPING) != 0) &&
                             coObject instanceof ContentMapping)
                {
                    if (!fcCallback.handleObject(coObject))
                    {
                        return false;
                    }
                }
            }
        }

        // Recurse the folders
        for (Iterator<Folder> iIter = lSubFolders.iterator(); iIter.hasNext();)
        {
            Folder fFolder = iIter.next();

            if ((iMask & FolderCallback.MASK_FOLDER) != 0)
            {
                // Call the handler.
                fcCallback.handleFolder(fFolder);
            }

            // Recurse to the subfolder.
            if (!fFolder.recurseFolderContentsBreadthFirst(iMask, fcCallback))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Recurses the folder contents and calls the callback method for every
     * requested object. The types of objects can be controlled with the mask.
     * If folders are requested they are recursed in a depth first,  i.e. the
     * subfolders are recursed first and then folder contents are returned.
     *
     * @param iMask The bit mask defining the requested object types. Values
     *        are defined in
     * @param fcCallback The callback object.
     *
     * @return If the callback returned false, this will be also false.
     *
     * @throws ContentException Thrown from the callback method.
     *
     * @see FolderCallback
     */
    public boolean recurseFolderContentsDepthFirst(int iMask,
                                                   FolderCallback fcCallback)
                                            throws ContentException
    {
        // Recurse the folders
        for (Iterator<Folder> iIter = lSubFolders.iterator(); iIter.hasNext();)
        {
            Folder fFolder = iIter.next();

            // Recurse to the subfolder.
            if (!fFolder.recurseFolderContentsDepthFirst(iMask, fcCallback))
            {
                return false;
            }
        }

        if ((iMask & FolderCallback.MASK_FOLDER) != 0)
        {
            // Call the handler.
            fcCallback.handleFolder(this);
        }

        // Recurse the folder contents, i.e. templates, mapping, etc.
        if ((iMask & (~FolderCallback.MASK_FOLDER)) != 0)
        {
            for (Iterator<CoBOCObject> iIter = lSubEntities.iterator(); iIter.hasNext();)
            {
                CoBOCObject coObject = iIter.next();

                if (((iMask & FolderCallback.MASK_TEMPLATE) != 0) &&
                        coObject instanceof Template)
                {
                    if (!fcCallback.handleObject(coObject))
                    {
                        return false;
                    }
                }
                /*else if ((iMask & FolderCallback.MASK_PROCESS_TEMPLATE) != 0 &&
                   coObject instanceof ProcessTemplate) {
                   if (! fcCallback.handleProcessTemplate((ProcessTemplate) coObject)) {
                           return false;
                   }
                   }*/
                else if (((iMask & FolderCallback.MASK_MAPPING) != 0) &&
                             coObject instanceof Mapping)
                {
                    if (!fcCallback.handleObject(coObject))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Removes this folder from the CoBOCContext and from the parent folder.
     */
    public void removeFromContext()
    {
        if (ccContext == null)
        {
            return;
        }

        // Remove this from the parent folder.
        if (fParentFolder != null)
        {
            fParentFolder.lSubFolders.remove(this);
            fParentFolder = null;
        }

        // Set parent of all children to null.
        for (Iterator<CoBOCObject> iIter = lSubEntities.iterator(); iIter.hasNext();)
        {
            CoBOCObject coObject = iIter.next();

            coObject.setParentObject(null);
        }
        lSubEntities.clear();

        for (Iterator<Folder> iIter = lSubFolders.iterator(); iIter.hasNext();)
        {
            Folder coObject = iIter.next();

            coObject.setParentObject(null);
        }
        lSubFolders.clear();

        // Remove this folder from the CoBOC.
        ccContext.removeObject(this);
        ccContext = null;
    }

    /**
     * Returns a string representataion of this Folder.
     *
     * @return a string representataion of this Folder.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer(128);

        sb.append("ID       : ").append(sEntityId).append("\n");
        sb.append("KEY      : ").append(sPathKey).append("\n");
        sb.append("NAME     : ").append(sName).append("\n");
        sb.append("DESC     : ").append(sDescription).append("\n");
        sb.append("MODIFIED : ").append(sLastModified).append("\n");
        sb.append("LEVEL    : ").append(sLevel).append("\n");
        sb.append("ORG      : ").append(sOrganization).append("\n");
        sb.append("OWNER    : ").append(sOwner).append("\n");
        sb.append("PARENT   : ")
          .append((fParentFolder != null) ? fParentFolder.getEntityID() : "N/A")
          .append("\n");

        return sb.toString();
    }

    /**
     * Updates the root folder entity ID from a child object.
     *
     * @param iChildTuple
     */
    public void updateRootEntityId(int iChildTuple)
    {
        Serializer s = new Serializer();
        String sId = null;

        try
        {
            // Get the parent folder from thet tuple.
            sId = s.readString(iChildTuple, "tuple/old/ENTITY/PARENT_ID", null);
        }
        catch (XMLException e)
        {
            // This should not happen.
        }

        // Set the entity id and update the state in studio.
        if (sId != null)
        {
            ccContext.removeObject(this);
            setEntityID(sId);
            ccContext.updateObject(this);
        }
    }
}
