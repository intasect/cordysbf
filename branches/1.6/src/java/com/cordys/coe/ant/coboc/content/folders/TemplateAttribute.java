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

import com.cordys.coe.ant.coboc.content.CoBOCContext;
import com.cordys.coe.ant.coboc.content.CoBOCObject;
import com.cordys.coe.ant.coboc.content.ContentException;
import com.cordys.coe.ant.coboc.util.Serializer;
import com.eibus.xml.nom.Document;

/**
 * A class that contains template attribute info.
 * 
 * @author mpoyhone
 */
public class TemplateAttribute implements CoBOCObject {
	/**
	 * Contains the template that this attribute belongs to.
	 */
	protected Template tTemplate;
	
	/**
	 * The attribute name.
	 */
	public String sAttributeName;

	/**
	 * The attribute type string.
	 */
	public String sAttributeType;
	
	/**
	 * Primary key flag.
	 */
	public boolean bPrimaryKey;
	
	/**
	 * Attribute ID
	 */
	public String sAttributeId;
	/**
	 * The CoBOC context this attribute belongs to.
	 */
	protected CoBOCContext ccContext;
	
	/**
	 * Indicates whether this attribute has been read from ECX or from file system.
	 */
	protected boolean bHasContent = false;
	
    /**
     * Creates a new TemplateAttribute object.
     *
     * @param ccContext The CoBOC context object.
     */
    public TemplateAttribute(CoBOCContext ccContext)
    {
        this.ccContext = ccContext;
    }	
    /**
     * Returns the iterator that can be used to access the other CoBOCObjects
     * that this object is dependent of.
     * Not implemented!
     *
     * @return An iterator that returns CoBOCObject objects.
     */
    public Iterator<?> getDependencies() {
    	return null;
    }

    /**
     * Sets the attribute ID for this element.
     *
     * @param sId The attribute ID for this element.
     */
    public void setEntityID(String sId) {
    	sAttributeId = sId;
    }

    /**
     * Returns the attribute ID for this element.
     *
     * @return The attribute ID for this element.
     */
    public String getEntityID() {
    	return sAttributeId;
    }

    /**
     * Sets the path key for this element. Note that all object do not have
     * path keys.
     * Not implemented!
     *
     * @param sPathKey The CoBOC path key.
     */
    public void setEntityPathKey(String sPathKey){
    }

    /**
     * Returns the path key for this element.
     * Not implemented!
     *
     * @return The CoBOC path key or null if this object does not support
     *         paths.
     */
    public String getEntityPathKey() {
    	return null;
    }

    /**
     * Sets the template that this attribute belongs to.
     * Not implemented!
     *
     * @param coParent The parent template.
     */
    public void setParentObject(CoBOCObject coParent) {
    	tTemplate = (Template) coParent;
    }

    /**
     * Returns the template that this attribute belongs to.
     *
     * @return The parent template.
     */
    public CoBOCObject getParentObject() {
    	return tTemplate;
    }

    /**
     * Adds a child object to this object. This is supported only for folders.
     * Not implemented!
     *
     * @param coObject
     */
    public void addChildObject(CoBOCObject coObject) {
    }

    /**
     * Converts the ECX message structure to the internal representation.
     *
     * @param iNode The ECX message structure that needs to be parsed.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public void convertFromECX(int iNode)
                        throws ContentException {
        Serializer s = new Serializer();

        try
        {
            int iAttribTemplateNode;
            String sTemplateId;

            // Read the entity node path.
            iAttribTemplateNode = s.findNode(iNode, "old/attribute_template");

            sAttributeName = s.readString(iAttribTemplateNode, "attribute_name");
            sTemplateId = s.readString(iAttribTemplateNode, "template_id");
            sAttributeType = s.readString(iAttribTemplateNode, "attribute_type");
            bPrimaryKey = s.readBoolean(iAttribTemplateNode, "primary_key");
            sAttributeId = s.readString(iAttribTemplateNode, "attribtemplate_id");

            // Find the parent template.
            if (sTemplateId != null)
            {
                tTemplate = Template.findTemplateById(ccContext, sTemplateId, false);
                if (tTemplate == null) {
                	throw new ContentException("Template not found with key " + sTemplateId);
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
     * Converts the file structure to the internal representation.
     *
     * @param iNode The file XML structure that needs to be parsed.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public void convertFromFile(int iNode)
                         throws ContentException {
        Serializer s = new Serializer();

        try
        {
            int iAttribTemplateNode;

            // Read the entity node path.
            iAttribTemplateNode = s.findNode(iNode, "old/attribute_template");

            sAttributeName = s.readString(iAttribTemplateNode, "attribute_name");
            sAttributeType = s.readString(iAttribTemplateNode, "attribute_type");
            bPrimaryKey = s.readBoolean(iAttribTemplateNode, "primary_key");

            bHasContent = true;
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to read the XML: " + e);
        }        	
    }

    /**
     * Converts the content element to an XML structure than can be send to
     * ECX. The structure must contain tuple and new-tags.
     *
     * @param dDoc The XML document object used to create the resulting XML
     *        structure.
     *
     * @return The converted ECX message XML structure.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public int convertToECX(Document dDoc)
                     throws ContentException {
        Serializer s = new Serializer(dDoc);
        int iTupleNode;
        
        if (tTemplate == null) {
        	throw new ContentException("Attribute has not template set.");
        }
        
        try
        {
            iTupleNode = dDoc.createElement("tuple");

            int iAttribTemplateNode;

            // Create the entity node path.
            iAttribTemplateNode = s.createNode(iTupleNode, "new/attribute_template");

            // Write the entity nodes.
            s.writeString(iAttribTemplateNode, "attribute_name", sAttributeName);
            s.writeString(iAttribTemplateNode, "template_id", tTemplate.getEntityID());
            s.writeString(iAttribTemplateNode, "attribute_type", sAttributeType);
            s.writeBoolean(iAttribTemplateNode, "primary_key", bPrimaryKey);
            
            bHasContent = true;
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to write the XML: " + e);
        }

        return iTupleNode;
    }

    /**
     * Converts the content element XML format that is written in the content
     * file.
     *
     * @param dDoc The XML document object used to create the resulting XML
     *        structure.
     *
     * @return The converted file XML structure.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public int convertToFile(Document dDoc)
                      throws ContentException {
        Serializer s = new Serializer(dDoc);
        int iTupleNode;
        
        try
        {
            iTupleNode = dDoc.createElement("tuple");

            int iAttribTemplateNode;

            // Create the entity node path.
            iAttribTemplateNode = s.createNode(iTupleNode, "old/attribute_template");

            // Write the entity nodes.
            s.writeString(iAttribTemplateNode, "attribute_name", sAttributeName);
            s.writeString(iAttribTemplateNode, "attribute_type", sAttributeType);
            s.writeBoolean(iAttribTemplateNode, "primary_key", bPrimaryKey);
            
            bHasContent = true;
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to write the XML: " + e);
        }

        return iTupleNode;
    }

    /**
     * Returns true if this object has been read succesfully either from CoBOC
     * or a file.
     *
     * @return True, if this object has been read succesfully either from CoBOC
     *         or a file, otherwise false.
     */
    public boolean hasContent() {
    	return bHasContent;
    }
}
