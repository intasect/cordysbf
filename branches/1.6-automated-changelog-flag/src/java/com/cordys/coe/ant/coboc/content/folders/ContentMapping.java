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

import java.util.Iterator;

import com.cordys.coe.ant.coboc.CoBOCConstants;
import com.cordys.coe.ant.coboc.content.CoBOCContext;
import com.cordys.coe.ant.coboc.content.CoBOCObject;
import com.cordys.coe.ant.coboc.content.ContentException;
import com.cordys.coe.ant.coboc.util.Serializer;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * Contains CoBOC folder object data and methods to convert it to ECX and file
 * formats.
 *
 * @author mpoyhone
 */
public class ContentMapping
    implements CoBOCObject
{
    /**
     * The parent folder object.
     */
    public Folder fParentFolder;
    /**
     * The content map contents.
     */
    public String sContentMap;
    /**
     * The content map description.
     */
    public String sDescription;
    /**
     * The content map CoBOC entityId.
     */
    public String sEntityId;
    /**
     * CoBOC entity type.
     */
    public String sEntityType;
    /**
     * Last modification time.
     */
    public String sLastModified;
    /**
     * CoBOC level string.
     */
    public String sLevel;
    /**
     * Content map name.
     */
    public String sName;
    /**
     * The organization this content map belongs to.
     */
    public String sOrganization;
    /**
     * The user DN that is the owner of this content map.
     */
    public String sOwner;
    /**
     * The CoBOC key.
     */
    public String sPathKey;
    /**
     * Template ID (perharps the same as CoBOC entity ID).
     */
    public String sTemplateId;
    /**
     * CoBOC version string.
     */
    public String sVersion;
    /**
     * Eventflag value.
     */
    public int iEventFlag;
    /**
     * History value.
     */
    public int iHistory;
    /**
     * The CoBOC context object that this content map belongs to.
     */
    protected CoBOCContext ccContext;
    /*       <tuple template="/CONTENTMAP" lastModified="1115035565702" key="/cordys/mappings/cmap" name="cmap" version="organization">
       <old>
         <ENTITY>
           <ENTITY_ID>11137545498219</ENTITY_ID>
         </ENTITY>
       </old>
       <new>
         <ENTITY>
           <ENTITY_NAME>cmap</ENTITY_NAME>
           <DESCRIPTION>cmap</DESCRIPTION>
           <EVENT_FLAG>0</EVENT_FLAG>
           <HISTORY>0</HISTORY>
           <OBJECT>
             <contentmap>
               <record entity="a" sourcesystem="s1" sourcevalue="x" destinationsystem="s2">y</record>
               <record entity="b" sourcesystem="s1" sourcevalue="1" destinationsystem="s2">2</record>
             </contentmap>
           </OBJECT>
         </ENTITY>
       </new>
       </tuple>*/
    /**
     * Indicates whether this content map has been read from ECX or file.
     */
    protected boolean bHasContent = false;

    /**
     * Creates a new ContentMapping object.
     *
     * @param ccContext The CoBOC context object.
     */
    public ContentMapping(CoBOCContext ccContext)
    {
        this.ccContext = ccContext;
    }

    /**
     * Check if this XML structure represents a CoBOC content map that was read
     * from the file system.
     *
     * @param iNode The XML structure read from the file.
     *
     * @return True if this is a content map.
     */
    public static boolean isContentMapping(int iNode)
    {
        String sTuple = Node.getName(iNode);

        if ((sTuple == null) || !sTuple.equals("tuple"))
        {
            return false;
        }

        String sType = Node.getAttribute(iNode,
                                         CoBOCConstants.FILE_TYPE_ATTRIBUTE);

        if ((sType == null) || !sType.equals("content-mapping"))
        {
            return false;
        }

        return true;
    }

    /**
     * Not implemented.
     *
     * @return Nothing.
     */
    public Iterator<?> getDependencies()
    {
        return null;
    }

    /**
     * Sets the CoBOC entity ID.
     *
     * @param sId The CoBOC entity ID.
     */
    public void setEntityID(String sId)
    {
        sEntityId = sId;
    }

    /**
     * Returns the CoBOC entity ID.
     *
     * @return The CoBOC entity ID.
     */
    public String getEntityID()
    {
        return sEntityId;
    }

    /**
     * Sets the CoBOC key.
     *
     * @param sPathKey The CoBOC key.
     */
    public void setEntityPathKey(String sPathKey)
    {
        this.sPathKey = sPathKey;
    }

    /**
     * Returns the CoBOC key.
     *
     * @return The CoBOC key.
     */
    public String getEntityPathKey()
    {
        return sPathKey;
    }

    /**
     * Converts the content map from ECX format.
     *
     * @param iNode The content map contents in ECX format.
     *
     * @throws ContentException Thrown if the parsing failed.
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
            sEntityType = s.readString(iNode, "@entity_type");

            int iEntityNode;
            int iMapNode;

            // Read the entity node path.
            iEntityNode = s.findNode(iNode, "old/ENTITY");

            sOrganization = s.readString(iEntityNode, "ORGANIZATION");
            sOwner = s.readString(iEntityNode, "OWNER");
            sDescription = s.readString(iEntityNode, "DESCRIPTION");
            iEventFlag = s.readInt(iEntityNode, "EVENT_FLAG");
            iHistory = s.readInt(iEntityNode, "HISTORY");
            sTemplateId = s.readString(iEntityNode, "TEMPLATE_ID");

            // Read the map object node
            iMapNode = s.findNode(iEntityNode, "OBJECT/contentmap");

            sContentMap = ((iMapNode != 0)
                           ? Node.writeToString(iMapNode, false) : null);

            String sParentId;

            // Get the parent folder from thet tuple.
            sParentId = s.readString(iEntityNode, "PARENT_ID");

            // Find the parent folder or create it with the given ID.
            if (sParentId != null)
            {
                fParentFolder = Folder.findFolderById(ccContext, sParentId, true);
            }

            bHasContent = true;
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to read the XML: " + e);
        }
    }

    /**
     * Converts the content map from file system format.
     *
     * @param iNode The content map contents in file system format.
     *
     * @throws ContentException Thrown if the parsing failed.
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
            sEntityType = s.readString(iNode, "@entity_type");

            int iEntityNode;
            int iMapNode;

            // Read the entity node path.
            iEntityNode = s.findNode(iNode, "old/ENTITY");

            sOrganization = s.readString(iEntityNode, "ORGANIZATION");
            sOwner = s.readString(iEntityNode, "OWNER");
            sDescription = s.readString(iEntityNode, "DESCRIPTION");
            iEventFlag = s.readInt(iEntityNode, "EVENT_FLAG");
            iHistory = s.readInt(iEntityNode, "HISTORY");
            sTemplateId = s.readString(iEntityNode, "TEMPLATE_ID");

            // Read the map object node
            iMapNode = s.findNode(iEntityNode, "OBJECT/contentmap");

            sContentMap = ((iMapNode != 0)
                           ? Node.writeToString(iMapNode, false) : null);

            String sParentPathKey;

            // Get the parent folder from thet tuple.
            sParentPathKey = s.readString(iEntityNode, "PARENT_KEY");

            // Find the parent folder or create it with the given ID.
            if (sParentPathKey != null)
            {
                fParentFolder = Folder.findFolderByPathKey(ccContext,
                                                           sParentPathKey, true);
            }

            bHasContent = true;
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to read the XML: ", e);
        }
    }

    /**
     * Converts the content map to ECX format.
     *
     * @param dDoc The document that will be used to create the XML elements.
     *
     * @return The content map XML in ECX format.
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
            s.writeString(iTupleNode, "@version", sLevel);

            int iEntityNode;
            int iMapNode;

            // Create the entity node path.
            iEntityNode = s.createNode(iTupleNode, "new/ENTITY");

            // Write the entity nodes.
            s.writeString(iEntityNode, "PARENT_ID", sParentId);
            s.writeString(iEntityNode, "DESCRIPTION", sDescription);
            s.writeInt(iEntityNode, "EVENT_FLAG", iEventFlag);
            s.writeInt(iEntityNode, "HISTORY", iHistory);

            // Create the content map object node
            iMapNode = s.createNode(iEntityNode, "OBJECT");
            
            if (sContentMap != null && sContentMap.trim().length() > 0) {
            	Node.appendToChildren(dDoc.parseString(sContentMap), iMapNode);
            }
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to write the XML: " + e);
        }

        return iTupleNode;
    }

    /**
     * Converts the content map to file system format.
     *
     * @param dDoc The document that will be used to create the XML elements.
     *
     * @return The content map XML in file system format.
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
            s.writeString(iTupleNode, "@level", sLevel);
            s.writeString(iTupleNode, "@entity_type", sEntityType);
            s.writeString(iTupleNode, "@" + CoBOCConstants.FILE_TYPE_ATTRIBUTE,
                          "content-mapping");

            int iEntityNode;
            int iMapNode;

            // Create the entity node path.
            iEntityNode = s.createNode(iTupleNode, "old/ENTITY");

            // Write the entity nodes.
            s.writeString(iEntityNode, "PARENT_KEY",
                          (fParentFolder != null)
                          ? fParentFolder.getEntityPathKey() : "");
            s.writeString(iEntityNode, "ORGANIZATION", sOrganization);
            s.writeString(iEntityNode, "OWNER", sOwner);
            s.writeString(iEntityNode, "DESCRIPTION", sDescription);
            s.writeInt(iEntityNode, "EVENT_FLAG", iEventFlag);
            s.writeInt(iEntityNode, "HISTORY", iHistory);
            s.writeString(iEntityNode, "TEMPLATE_ID", sTemplateId);

            // Create the map object node
            iMapNode = s.createNode(iEntityNode, "OBJECT");
            
            if (sContentMap != null && sContentMap.trim().length() > 0) {
            	Node.appendToChildren(dDoc.parseString(sContentMap), iMapNode);
            }
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to write the XML: " + e);
        }

        return iTupleNode;
    }

    /**
     * Searches a content map by the CoBOC entity ID.
     *
     * @param ccContext The CoBOC context to be searched.
     * @param sId The content map entity ID that will be used in the search.
     * @param bCreate If true and the content map was not found, a new content
     *        map object is created.
     *
     * @return The found or created content map object.
     */
    public static ContentMapping findContentMappingById(CoBOCContext ccContext,
                                                        String sId,
                                                        boolean bCreate)
    {
        ContentMapping mMapping = (ContentMapping) ccContext.getObjectById(sId);

        if (mMapping == null)
        {
            // Check if we can create a new instance.
            if (!bCreate)
            {
                return null;
            }

            // Create a new instance.
            mMapping = new ContentMapping(ccContext);
            mMapping.setEntityID(sId);

            // Add the instance to the context
            ccContext.updateObject(mMapping);
        }

        return mMapping;
    }

    /**
     * Searches a content map by the CoBOC key.
     *
     * @param ccContext The CoBOC context to be searched.
     * @param sPathKey The content map key that will be used in the search.
     * @param bCreate If true and the content map was not found, a new content
     *        map object is created.
     *
     * @return The found or created content map object.
     */
    public static ContentMapping findContentMappingByPathKey(CoBOCContext ccContext,
                                                             String sPathKey,
                                                             boolean bCreate)
    {
        ContentMapping mMapping = (ContentMapping) ccContext.getObjectByPathKey(sPathKey);

        if (mMapping == null)
        {
            // Check if we can create a new instance.
            if (!bCreate)
            {
                return null;
            }

            // Create a new instance.
            mMapping = new ContentMapping(ccContext);
            mMapping.setEntityPathKey(sPathKey);

            // Add the instance to the context
            ccContext.updateObject(mMapping);
        }

        return mMapping;
    }

    /**
     * Sets the parent folder for this content map.
     *
     * @param coParent The parent folder object. This must be a Folder object.
     */
    public void setParentObject(CoBOCObject coParent)
    {
        if ((coParent != null) && !(coParent instanceof Folder))
        {
            throw new IllegalArgumentException(coParent + " is not a Folder.");
        }

        fParentFolder = (Folder) coParent;
    }

    /**
     * Returns the parent folder object for this content map.
     *
     * @return The parent folder as a Folder object.
     */
    public CoBOCObject getParentObject()
    {
        return fParentFolder;
    }

    /**
     * Not implemented.
     *
     * @param coObject None.
     */
    public void addChildObject(CoBOCObject coObject)
    {
        throw new UnsupportedOperationException("addChildObject is not supported.");
    }

    /**
     * Returns true if this content map contents have been read from ECX or a
     * file.
     *
     * @return True if this content map contents have been read from ECX or a
     *         file.
     */
    public boolean hasContent()
    {
        return bHasContent;
    }

    /**
     * Returns a string representation of this content map.
     *
     * @return A string representation of this content map.
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
        sb.append("PARENT   : ").append(getObjectId(fParentFolder)).append("\n");
        sb.append("MAP     : ")
          .append((sContentMap != null) ? sContentMap : "N/A").append("\n");

        return sb.toString();
    }

    /**
     * Returns the object ID as string. Used in toString-method.
     *
     * @param coObj The object in questiuon.
     *
     * @return The object's entity ID or path key depending which is set.
     */
    private String getObjectId(CoBOCObject coObj)
    {
        String sResult = null;

        if (coObj != null)
        {
            if ((sResult = coObj.getEntityID()) == null)
            {
                sResult = coObj.getEntityPathKey();
            }
        }

        return (sResult != null) ? sResult : "N/A";
    }
}
