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
import java.util.LinkedList;
import java.util.List;

import com.cordys.coe.ant.coboc.CoBOCConstants;
import com.cordys.coe.ant.coboc.content.CoBOCContext;
import com.cordys.coe.ant.coboc.content.CoBOCObject;
import com.cordys.coe.ant.coboc.content.ContentException;
import com.cordys.coe.ant.coboc.util.Serializer;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Contains CoBOC folder object data and methods to convert it to ECX and file
 * formats.
 *
 * @author mpoyhone
 */
public class Template
    implements CoBOCObject
{
    /**
     * The parent folder object.
     */
    public Folder fParentFolder;
    /**
     * The mapping description.
     */
    public String sDescription;
    /**
     * The mapping CoBOC entityId.
     */
    public String sEntityId;
    /**
     * Template icon URL.
     */
    public String sIconUrl;
    /**
     * Last modification time.
     */
    public String sLastModified;
    /**
     * CoBOC level string.
     */
    public String sLevel;
    /**
     * Template name.
     */
    public String sName;
    /**
     * The organization this template belongs to.
     */
    public String sOrganization;
    /**
     * The user DN that is the owner of this template.
     */
    public String sOwner;
    /**
     * The CoBOC key.
     */
    public String sPathKey;
    /**
     * Template plugin class.
     */
    public String sPluginClass;
    /**
     * The XML schema file as a string.
     */
    public String sXmlSchema;
    /**
     * Permanently in memory flag.
     */
    public boolean bPermanentlyInMemory;
    /**
     * Persist in DB flag.
     */
    public boolean bPersistInDB;
    /**
     * Validate rule flag.
     */
    public boolean bValidateRule;
    /**
     * Validate schema flag.
     */
    public boolean bValidateSchema;
    /**
     * Template history value.
     */
    public int iHistory;
    /**
     * The CoBOC context this template belongs to.
     */
    protected CoBOCContext ccContext;
    /**
     * A list containing template attributes.
     */
    protected List<CoBOCObject> lAttributeList = null;
    /**
     * Indicates whether this template has been read from ECX or a file.
     */
    protected boolean bHasContent = false;

    /**
     * Creates a new Template object.
     *
     * @param ccContext The CoBOC context object.
     */
    public Template(CoBOCContext ccContext)
    {
        this.ccContext = ccContext;
    }

    /**
     * Returns the attribute list iterator or null if no attributes are
     * present.
     *
     * @return The attribute list iterator or null if no attributes are
     *         present.
     */
    public Iterator<CoBOCObject> getAttributeIterator()
    {
        return (lAttributeList != null) ? lAttributeList.iterator() : null;
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
     * Check if this XML structure represents a CoBOC template that was read
     * from the file system.
     *
     * @param iNode The XML structure read from the file.
     *
     * @return The tuple node if this is a template or zero if this XML is not
     *         a template.
     */
    public static int getTemplateTuple(int iNode)
    {
        int iTupleNode;

        // Find the tuple node.
        if (!"tuple".equals(Node.getName(iNode)))
        {
            // This is the new format that starts with <content>
            iTupleNode = Find.firstMatch(iNode, "<template_content><tuple>");

            if (iTupleNode == 0)
            {
                return iTupleNode;
            }
        }
        else
        {
            iTupleNode = iNode;
        }

        String sTuple = Node.getName(iTupleNode);

        if ((sTuple == null) || !sTuple.equals("tuple"))
        {
            return 0;
        }

        String sType = Node.getAttribute(iTupleNode,
                                         CoBOCConstants.FILE_TYPE_ATTRIBUTE);

        if ((sType == null) || !sType.equals("template"))
        {
            return 0;
        }

        return iTupleNode;
    }

    /**
     * Returns the parent folder object for this template.
     *
     * @return The parent folder as a Folder object.
     */
    public CoBOCObject getParentObject()
    {
        return fParentFolder;
    }

    /**
     * Adds a template attribute object to this template.
     *
     * @param coObject The TemplateAttribute object to be added.
     */
    public void addChildObject(CoBOCObject coObject)
    {
        if (coObject instanceof TemplateAttribute)
        {
            if (lAttributeList == null)
            {
                lAttributeList = new LinkedList<CoBOCObject>();
            }
            lAttributeList.add(coObject);
        }
        else
        {
            throw new IllegalArgumentException("Objects of type " +
                                               coObject.getClass() +
                                               " cannot be added to a template.");
        }
    }

    /**
     * Converts the template from ECX format.
     *
     * @param iNode The template contents in ECX format.
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

            int iEntityNode;
            int iSchemaNode;

            // Read the entity node path.
            iEntityNode = s.findNode(iNode, "old/ENTITY");

            sOrganization = s.readString(iEntityNode, "ORGANIZATION");
            sOwner = s.readString(iEntityNode, "OWNER");
            sDescription = s.readString(iEntityNode, "DESCRIPTION");
            iSchemaNode = s.readNode(iEntityNode, "XSD_SCHEMA/*");
            sXmlSchema = ((iSchemaNode != 0)
                          ? Node.writeToString(iSchemaNode, false) : null);
            bPermanentlyInMemory = s.readBoolean(iEntityNode,
                                                 "PERMANENTLY_INMEMORY");
            bValidateSchema = s.readBoolean(iEntityNode, "VALIDATE_SCHEMA");
            sIconUrl = s.readString(iEntityNode, "ICONURL");
            bValidateRule = s.readBoolean(iEntityNode, "VALIDATE_RULE");
            bPersistInDB = s.readBoolean(iEntityNode, "PERSIST_INDB");
            iHistory = s.readInt(iEntityNode, "HISTORY");
            sPluginClass = s.readString(iEntityNode, "PLUGINCLASS");

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
     * Converts the template from file system format.
     *
     * @param iTopNode The template contents in file system format.
     *
     * @throws ContentException Thrown if the parsing failed.
     */
    public void convertFromFile(int iTopNode)
                         throws ContentException
    {
        Serializer s = new Serializer();
        int iTupleNode = getTemplateTuple(iTopNode);

        if (iTupleNode == 0)
        {
            throw new ContentException("Tuple node not found.");
        }

        try
        {
            // Read tuple node attributes.
            sLastModified = s.readString(iTupleNode, "@lastModified");
            sPathKey = s.readString(iTupleNode, "@key");
            sName = s.readString(iTupleNode, "@name");
            sLevel = s.readString(iTupleNode, "@level");

            int iEntityNode;
            int iSchemaNode;

            // Read the entity node path.
            iEntityNode = s.findNode(iTupleNode, "old/ENTITY");

            sOrganization = s.readString(iEntityNode, "ORGANIZATION");
            sOwner = s.readString(iEntityNode, "OWNER");
            sDescription = s.readString(iEntityNode, "DESCRIPTION");
            iSchemaNode = s.readNode(iEntityNode, "XSD_SCHEMA/*");
            sXmlSchema = ((iSchemaNode != 0)
                          ? Node.writeToString(iSchemaNode, false) : null);
            bPermanentlyInMemory = s.readBoolean(iEntityNode,
                                                 "PERMANENTLY_INMEMORY");
            bValidateSchema = s.readBoolean(iEntityNode, "VALIDATE_SCHEMA");
            sIconUrl = s.readString(iEntityNode, "ICONURL");
            bValidateRule = s.readBoolean(iEntityNode, "VALIDATE_RULE");
            bPersistInDB = s.readBoolean(iEntityNode, "PERSIST_INDB");
            iHistory = s.readInt(iEntityNode, "HISTORY");
            sPluginClass = s.readString(iEntityNode, "PLUGINCLASS");

            String sParentPathKey;

            // Get the parent folder from thet tuple.
            sParentPathKey = s.readString(iEntityNode, "PARENT_KEY");

            // Find the parent folder or create it with the given ID.
            if (sParentPathKey != null)
            {
                fParentFolder = Folder.findFolderByPathKey(ccContext,
                                                           sParentPathKey, true);
            }

            // Read the attributes.
            int[] iAttribNodes = Find.match(iTopNode,
                                            "<template_content><attributes><tuple>");

            if (iAttribNodes != null)
            {
                for (int i = 0; i < iAttribNodes.length; i++)
                {
                    int iAttribTupleNode = iAttribNodes[i];
                    TemplateAttribute taAttrib = new TemplateAttribute(ccContext);

                    taAttrib.convertFromFile(iAttribTupleNode);

                    taAttrib.setParentObject(this);
                    addChildObject(taAttrib);
                }
            }

            bHasContent = true;
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to read the XML: " + e);
        }
    }

    /**
     * Converts the template to ECX format.
     *
     * @param dDoc The document that will be used to create the XML elements.
     *
     * @return The template XML in ECX format.
     *
     * @throws ContentException ContentException Thrown if the conversion
     *         failed.
     */
    public int convertToECX(Document dDoc)
                     throws ContentException
    {
        Serializer s = new Serializer(dDoc);
        int iTupleNode;

        try
        {
            iTupleNode = dDoc.createElement("tuple");

            // Write tuple node attributes.
            s.writeString(iTupleNode, "@key", sPathKey);
            s.writeString(iTupleNode, "@name", sName);
            s.writeString(iTupleNode, "@version", sLevel);

            int iEntityNode;

            // Create the entity node path.
            iEntityNode = s.createNode(iTupleNode, "new/ENTITY");

            // Write the entity nodes.
            //s.writeString(iEntityNode, "PARENT_ID", sParentId);
            //s.writeString(iEntityNode, "ORGANIZATION", sDestOrganization);
            //s.writeString(iEntityNode, "OWNER", sDestUserDN);
            s.writeString(iEntityNode, "DESCRIPTION", sDescription);
            s.writeNode(iEntityNode, "XSD_SCHEMA",
                        dDoc.parseString(sXmlSchema), false);
            s.writeBoolean(iEntityNode, "PERMANENTLY_INMEMORY",
                           bPermanentlyInMemory);
            s.writeBoolean(iEntityNode, "VALIDATE_SCHEMA", bValidateSchema);
            s.writeString(iEntityNode, "ICONURL", sIconUrl);
            s.writeBoolean(iEntityNode, "VALIDATE_RULE", bValidateRule);
            s.writeBoolean(iEntityNode, "PERSIST_INDB", bPersistInDB);
            s.writeInt(iEntityNode, "HISTORY", iHistory);
            s.writeString(iEntityNode, "PLUGINCLASS", sPluginClass);
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to write the XML: " + e);
        }

        return iTupleNode;
    }

    /**
     * Converts the template to file system format.
     *
     * @param dDoc The document that will be used to create the XML elements.
     *
     * @return The template XML in file system format.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public int convertToFile(Document dDoc)
                      throws ContentException
    {
        Serializer s = new Serializer(dDoc);
        int iRootNode;
        int iTupleNode;

        iRootNode = dDoc.createElement("template_content");

        try
        {
            iTupleNode = dDoc.createElement("tuple", iRootNode);

            // Write tuple node attributes.
            s.writeString(iTupleNode, "@lastModified", sLastModified);
            s.writeString(iTupleNode, "@key", sPathKey);
            s.writeString(iTupleNode, "@name", sName);
            s.writeString(iTupleNode, "@level", sLevel);
            s.writeString(iTupleNode, "@" + CoBOCConstants.FILE_TYPE_ATTRIBUTE,
                          "template");

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
            s.writeNode(iEntityNode, "XSD_SCHEMA",
                        dDoc.parseString(sXmlSchema), false);
            s.writeBoolean(iEntityNode, "PERMANENTLY_INMEMORY",
                           bPermanentlyInMemory);
            s.writeBoolean(iEntityNode, "VALIDATE_SCHEMA", bValidateSchema);
            s.writeString(iEntityNode, "ICONURL", sIconUrl);
            s.writeBoolean(iEntityNode, "VALIDATE_RULE", bValidateRule);
            s.writeBoolean(iEntityNode, "PERSIST_INDB", bPersistInDB);
            s.writeInt(iEntityNode, "HISTORY", iHistory);
            s.writeString(iEntityNode, "PLUGINCLASS", sPluginClass);

            // Add the attributes if there are any.
            if (lAttributeList != null)
            {
                int iAttribRoot;

                iAttribRoot = dDoc.createElement("attributes", iRootNode);

                for (Iterator<CoBOCObject> iIter = lAttributeList.iterator();
                         iIter.hasNext();)
                {
                    TemplateAttribute taAttrib = (TemplateAttribute) iIter.next();
                    int iAttribNode;

                    iAttribNode = taAttrib.convertToFile(dDoc);

                    if (iAttribNode != 0)
                    {
                        Node.appendToChildren(iAttribNode, iAttribRoot);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to write the XML: " + e);
        }

        return iRootNode;
    }

    /**
     * Searches a template by the CoBOC entity ID.
     *
     * @param ccContext The CoBOC context to be searched.
     * @param sId The template entity ID that will be used in the search.
     * @param bCreate If true and the template was not found, a new template
     *        object is created.
     *
     * @return The found or created template object.
     */
    public static Template findTemplateById(CoBOCContext ccContext, String sId,
                                            boolean bCreate)
    {
        Template tTemplate = (Template) ccContext.getObjectById(sId);

        if (tTemplate == null)
        {
            // Check if we can create a new instance.
            if (!bCreate)
            {
                return null;
            }

            // Create a new instance.
            tTemplate = new Template(ccContext);
            tTemplate.setEntityID(sId);

            // Add the instance to the context
            ccContext.updateObject(tTemplate);
        }

        return tTemplate;
    }

    /**
     * Searches a template by the CoBOC key.
     *
     * @param ccContext The CoBOC context to be searched.
     * @param sPathKey The template key that will be used in the search.
     * @param bCreate If true and the mapping was not found, a new template
     *        object is created.
     *
     * @return The found or created template object.
     */
    public static Template findTemplateByPathKey(CoBOCContext ccContext,
                                                 String sPathKey,
                                                 boolean bCreate)
    {
        Template tTemplate = (Template) ccContext.getObjectByPathKey(sPathKey);

        if (tTemplate == null)
        {
            // Check if we can create a new instance.
            if (!bCreate)
            {
                return null;
            }

            // Create a new instance.
            tTemplate = new Template(ccContext);
            tTemplate.setEntityPathKey(sPathKey);

            // Add the instance to the context
            ccContext.updateObject(tTemplate);
        }

        return tTemplate;
    }

    /**
     * Sets the parent folder for this template.
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
     * Returns true if this template contents have been read from ECX or a
     * file.
     *
     * @return True if this template contents have been read from ECX or a
     *         file.
     */
    public boolean hasContent()
    {
        return bHasContent;
    }

    /**
     * Returns a string representation of this template.
     *
     * @return A string representation of this template.
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
        sb.append("SCHEMA   : ")
          .append((sXmlSchema != null) ? sXmlSchema : "N/A").append("\n");
        ;
        sb.append("PERM_MEM : ").append(bPermanentlyInMemory).append("\n");
        ;
        sb.append("VAL_XSD  : ").append(bValidateSchema).append("\n");
        ;
        sb.append("ICON URL : ").append(sIconUrl).append("\n");
        ;
        sb.append("VAL_RULE : ").append(bValidateRule).append("\n");
        ;
        sb.append("PERS_DB  : ").append(bPersistInDB).append("\n");
        ;
        sb.append("HISTORY  : ").append(iHistory).append("\n");
        ;
        sb.append("PLUG_CLS : ").append(sPluginClass).append("\n");
        ;

        return sb.toString();
    }
}
