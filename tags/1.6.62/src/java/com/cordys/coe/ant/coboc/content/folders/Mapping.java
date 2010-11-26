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
public class Mapping
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
     * Mapping name.
     */
    public String sName;
    /**
     * The organization this mapping belongs to.
     */
    public String sOrganization;
    /**
     * The user DN that is the owner of this mapping.
     */
    public String sOwner;
    /**
     * The CoBOC key.
     */
    public String sPathKey;
    /**
     * Source template isShared attribute value.
     */
    public String sSourceIsShared;
    /**
     * Source template shareName attribute value.
     */
    public String sSourceShareName;
    /**
     * Source template version attribute value.
     */
    public String sSourceVersion;
    /**
     * Target template isShared attribute value.
     */
    public String sTargetIsShared;
    /**
     * Target template shareName attribute value.
     */
    public String sTargetShareName;
    /**
     * Target template version attribute value.
     */
    public String sTargetVersion;
    /**
     * Template ID (perharps the same as CoBOC entity ID).
     */
    public String sTemplateId;
    /**
     * CoBOC version string.
     */
    public String sVersion;
    /**
     * The XSLT file contents as string object.
     */
    public String sXslTransform;
    /**
     * The source template
     */
    public Template tSourceTemplate[];
    
    /**
     * The target template.
     */
    public Template tTargetTemplate[];
    /**
     * Eventflag value.
     */
    public int iEventFlag;
    /**
     * History value.
     */
    public int iHistory;
    /**
     * The CoBOC context object that this mapping belongs to.
     */
    protected CoBOCContext ccContext;
    /*       <tuple lastModified="1094463776556" key="/cordys/vghg" name="vghg" entity_id="10944637765562" entity_type="1005" customkey="10944637765562" level="organization">
       <old>
         <ENTITY>
           <ENTITY_ID>10944637765562</ENTITY_ID>
           <PARENT_ID>10891871404764</PARENT_ID>
           <ORGANIZATION>o=system,cn=cordys,o=vanenburg.com</ORGANIZATION>
           <OWNER>cn=mpoyhone,cn=organizational users,o=system,cn=cordys,o=vanenburg.com</OWNER>
           <DESCRIPTION/>
           <EVENT_FLAG>0</EVENT_FLAG>
           <HISTORY>0</HISTORY>
           <CUSTOMKEY>10944637765562</CUSTOMKEY>
           <TEMPLATE_ID>1005</TEMPLATE_ID>
           <OBJECT>
             <MAP_OBJECT>
               <MAP_ID>10944637765562</MAP_ID>
               <NAME>/cordys/vghg</NAME>
               <SOURCE templatePath="/system/systemReportTemplate" isShared="false" shareName="" version="organization">10891182030848</SOURCE>
               <TARGET templatePath="/system/systemGraphTemplate" isShared="false" shareName="" version="organization">10891182031840</TARGET>
               <DESCRIPTION/>
               <VERSION>1.0</VERSION>
               <MAP_DATA>
                 <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"/>
               </MAP_DATA>
             </MAP_OBJECT>
           </OBJECT>
         </ENTITY>
       </old>*/
    /**
     * Indicates whether this mapping has been read from ECX or file.
     */
    protected boolean bHasContent = false;

    /**
     * Creates a new Mapping object.
     *
     * @param ccContext The CoBOC context object.
     */
    public Mapping(CoBOCContext ccContext)
    {
        this.ccContext = ccContext;
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
     * Check if this XML structure represents a CoBOC mapping that was read
     * from the file system.
     *
     * @param iNode The XML structure read from the file.
     *
     * @return True if this is a mapping.
     */
    public static boolean isMapping(int iNode)
    {
        String sTuple = Node.getName(iNode);

        if ((sTuple == null) || !sTuple.equals("tuple"))
        {
            return false;
        }

        String sType = Node.getAttribute(iNode,
                                         CoBOCConstants.FILE_TYPE_ATTRIBUTE);

        if ((sType == null) || !sType.equals("mapping"))
        {
            return false;
        }

        return true;
    }

    /**
     * Converts the mapping from ECX format.
     *
     * @param iNode The mapping contents in ECX format.
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
            int iXsltNode;
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
            iMapNode = s.findNode(iEntityNode, "OBJECT/MAP_OBJECT");

            sVersion = s.readString(iMapNode, "VERSION");

            // Read the source and target template data, if present
            int iSourceNode;
            int iTargetNode;

            iSourceNode = s.findNode(iMapNode, "SOURCE");

            if (iSourceNode != 0)
            {
                // Read the source template ID and source template path.
                String sSourceId = s.readString(iSourceNode, ".");
                String sSourcePaths = s.readString(iSourceNode, "@templatePath");

                // Read source template parameters
                sSourceIsShared = s.readString(iSourceNode, "@isShared");
                sSourceShareName = s.readString(iSourceNode, "@shareName");
                sSourceVersion = s.readString(iSourceNode, "@version");

                String[] sourcePathsArr = sSourcePaths.split(",");
                tSourceTemplate = new Template[sourcePathsArr.length];
                for (int i = 0; i < sourcePathsArr.length; i++)
                {
                	String sSourcePath = sourcePathsArr[i];
	                // Find the source template either by ID or path.
	                if (sSourcePath != null && sSourcePath.length() > 0) 
	                {
	                	tSourceTemplate[i] = Template.findTemplateByPathKey(ccContext, sSourcePath, false);
	                }
	                
	                if (tSourceTemplate == null && sSourceId != null && sSourceId.length() > 0) 
	                {
		                tSourceTemplate[i] = Template.findTemplateById(ccContext,
		                        sSourceId, true);
	                } 
                }
            }

            iTargetNode = s.findNode(iMapNode, "TARGET");

            if (iTargetNode != 0)
            {
                // Read the target template ID and find the template object.
                String sTargetId = s.readString(iTargetNode, ".");
                String sTargetPaths = s.readString(iTargetNode, "@templatePath");

                // Read target template parameters
                sTargetIsShared = s.readString(iTargetNode, "@isShared");
                sTargetShareName = s.readString(iTargetNode, "@shareName");
                sTargetVersion = s.readString(iTargetNode, "@version");
                
                String[] targetPathsArr = sTargetPaths.split(",");
                tTargetTemplate = new Template[targetPathsArr.length];
                for (int i = 0; i < targetPathsArr.length; i++)
                {
                	String sTargetPath = targetPathsArr[i];
	                // Find the target template either by ID or path.
	                if (sTargetPath != null && sTargetPath.length() > 0) 
	                {
	                	tTargetTemplate[i] = Template.findTemplateByPathKey(ccContext, sTargetPath, false);
	                }
	                
	                if (tTargetTemplate == null && sTargetId != null && sTargetId.length() > 0 ) 
	                {
		                tTargetTemplate[i] = Template.findTemplateById(ccContext,
		                        sTargetId, true);
	                } 
                }
            }

            // Read the XSLT data.
            iXsltNode = s.readNode(iMapNode, "MAP_DATA/*");
            sXslTransform = ((iXsltNode != 0)
                             ? Node.writeToString(iXsltNode, false) : null);

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
     * Converts the mapping from file system format.
     *
     * @param iNode The mapping contents in file system format.
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
            int iXsltNode;
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
            iMapNode = s.findNode(iEntityNode, "OBJECT/MAP_OBJECT");

            sVersion = s.readString(iMapNode, "VERSION");

            // Read the source and target template data, if present
            int iSourceNode;
            int iTargetNode;

            iSourceNode = s.findNode(iMapNode, "SOURCE");

            if (iSourceNode != 0)
            {
                String sSourcePathKeys = s.readString(iSourceNode, "@templatePath");
                String[] sourcePathsKeysArr = sSourcePathKeys.split(",");
                tSourceTemplate = new Template[sourcePathsKeysArr.length];
                for (int i = 0; i < sourcePathsKeysArr.length; i++)
                {
                    // Read the source template ID and find the template object.
                	tSourceTemplate[i] = Template.findTemplateByPathKey(ccContext, 
                			sourcePathsKeysArr[i],
                			true);
                }
                
                // Read source template parameters
                sSourceIsShared = s.readString(iSourceNode, "@isShared");
                sSourceShareName = s.readString(iSourceNode, "@shareName");
                sSourceVersion = s.readString(iSourceNode, "@version");
            }

            iTargetNode = s.findNode(iMapNode, "TARGET");

            if (iTargetNode != 0)
            {
                String sTargetPathKeys = s.readString(iTargetNode, "@templatePath");
                String[] targetPathsKeysArr = sTargetPathKeys.split(",");
                tTargetTemplate = new Template[targetPathsKeysArr.length];
                for (int i = 0; i < targetPathsKeysArr.length; i++)
                {
                	tTargetTemplate[i] = Template.findTemplateByPathKey(ccContext,               	
                			targetPathsKeysArr[i],
                			true);
                }

                // Read source template parameters
                sTargetIsShared = s.readString(iTargetNode, "@isShared");
                sTargetShareName = s.readString(iTargetNode, "@shareName");
                sTargetVersion = s.readString(iTargetNode, "@version");
            }

            // Read the XSLT data.
            iXsltNode = s.readNode(iMapNode, "MAP_DATA/*");
            sXslTransform = ((iXsltNode != 0)
                             ? Node.writeToString(iXsltNode, false) : null);

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
     * Converts the mapping to ECX format.
     *
     * @param dDoc The document that will be used to create the XML elements.
     *
     * @return The mapping XML in ECX format.
     *
     * @throws ContentException Thrown if the conversion failed.
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
            //s.writeString(iTupleNode, "@entity_type", sEntityType);

            int iMapNode;
            int iEntityNode;

            // Create the entity node path.
            iEntityNode = s.createNode(iTupleNode, "new/ENTITY");

            // Write the entity nodes.
            //s.writeString(iEntityNode, "PARENT_ID", sParentId);
            //s.writeString(iEntityNode, "ORGANIZATION", sDestOrganization);
            //s.writeString(iEntityNode, "OWNER", sDestUserDN);
            s.writeString(iEntityNode, "DESCRIPTION", sDescription);
            s.writeInt(iEntityNode, "EVENT_FLAG", iEventFlag);
            s.writeInt(iEntityNode, "HISTORY", iHistory);
            //s.writeString(iEntityNode, "TEMPLATE_ID", sTemplateId);
            // Create the map object node
            iMapNode = s.createNode(iEntityNode, "OBJECT/MAP_OBJECT");

            Node.createElement("MAP_ID", iMapNode);
            s.writeString(iMapNode, "NAME", sPathKey);
            s.writeString(iMapNode, "DESCRIPTION", sDescription);

            // Write the source template data, if present
            if (tSourceTemplate != null)
            {
                int iSourceNode;

                iSourceNode = s.createNode(iMapNode, "SOURCE");
                s.writeString(iSourceNode, "@templatePath", 
                		getPathKeys(tSourceTemplate));
                s.writeString(iSourceNode, "@isShared", sSourceIsShared);
                s.writeString(iSourceNode, "@shareName", sSourceShareName);
                s.writeString(iSourceNode, "@version", sSourceVersion);
                s.writeString(iSourceNode, ".", getEntityIds(tSourceTemplate));
            }

            // Write the target template data, if present
            if (tTargetTemplate != null)
            {
                int iTargetNode;

                iTargetNode = s.createNode(iMapNode, "TARGET");
                s.writeString(iTargetNode, "@templatePath", 
                		getPathKeys(tTargetTemplate));
                s.writeString(iTargetNode, "@isShared", sTargetIsShared);
                s.writeString(iTargetNode, "@shareName", sTargetShareName);
                s.writeString(iTargetNode, "@version", sSourceVersion);
                s.writeString(iTargetNode, ".", getEntityIds(tTargetTemplate));
            }

            s.writeString(iMapNode, "VERSION", sVersion);
            s.writeNode(iMapNode, "MAP_DATA", dDoc.parseString(sXslTransform),
                        false);
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to write the XML: " + e);
        }

        return iTupleNode;
    }

    /**
     * Converts the mapping to file system format.
     *
     * @param dDoc The document that will be used to create the XML elements.
     *
     * @return The mapping XML in file system format.
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
                          "mapping");

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
            iMapNode = s.createNode(iEntityNode, "OBJECT/MAP_OBJECT");

            s.writeString(iMapNode, "NAME", sName);

            // Write the source template data, if present
            if (tSourceTemplate != null)
            {
                int iSourceNode;

                iSourceNode = s.createNode(iMapNode, "SOURCE");
                s.writeString(iSourceNode, "@templatePath", 
                		getPathKeys(tSourceTemplate));
                s.writeString(iSourceNode, "@isShared", sSourceIsShared);
                s.writeString(iSourceNode, "@shareName", sSourceShareName);
                s.writeString(iSourceNode, "@version", sSourceVersion);
            }

            // Write the target template data, if present
            if (tTargetTemplate != null)
            {
                int iTargetNode;

                iTargetNode = s.createNode(iMapNode, "TARGET");
                s.writeString(iTargetNode, "@templatePath", 
                		getPathKeys(tTargetTemplate));
                s.writeString(iTargetNode, "@isShared", sTargetIsShared);
                s.writeString(iTargetNode, "@shareName", sTargetShareName);
                s.writeString(iTargetNode, "@version", sTargetVersion);
            }

            s.writeString(iMapNode, "VERSION", sVersion);
            s.writeNode(iMapNode, "MAP_DATA", dDoc.parseString(sXslTransform),
                        false);
        }
        catch (Exception e)
        {
            throw new ContentException("Unable to write the XML: " + e);
        }

        return iTupleNode;
    }

    /**
     * Searches a mapping by the CoBOC entity ID.
     *
     * @param ccContext The CoBOC context to be searched.
     * @param sId The mapping entity ID that will be used in the search.
     * @param bCreate If true and the mapping was not found, a new mapping object is created.
     *
     * @return The found or created mapping object.
     */
    public static Mapping findMappingById(CoBOCContext ccContext, String sId,
                                          boolean bCreate)
    {
        Mapping mMapping = (Mapping) ccContext.getObjectById(sId);

        if (mMapping == null)
        {
            // Check if we can create a new instance.
            if (!bCreate)
            {
                return null;
            }

            // Create a new instance.
            mMapping = new Mapping(ccContext);
            mMapping.setEntityID(sId);

            // Add the instance to the context
            ccContext.updateObject(mMapping);
        }

        return mMapping;
    }

    /**
     * Searches a mapping by the CoBOC key.
     *
     * @param ccContext The CoBOC context to be searched.
     * @param sPathKey The mapping key that will be used in the search.
     * @param bCreate If true and the mapping was not found, a new mapping object is created.
     *
     * @return The found or created mapping object.
     */
    public static Mapping findMappingByPathKey(CoBOCContext ccContext,
                                               String sPathKey, boolean bCreate)
    {
        Mapping mMapping = (Mapping) ccContext.getObjectByPathKey(sPathKey);

        if (mMapping == null)
        {
            // Check if we can create a new instance.
            if (!bCreate)
            {
                return null;
            }

            // Create a new instance.
            mMapping = new Mapping(ccContext);
            mMapping.setEntityPathKey(sPathKey);

            // Add the instance to the context
            ccContext.updateObject(mMapping);
        }

        return mMapping;
    }

    /**
     * Sets the parent folder for this mapping.
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
     * Returns the parent folder object for this mapping.
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
     * Returns true if this mapping contents have been read from ECX or a file.
     *
     * @return True if this mapping contents have been read from ECX or a file.
     */
    public boolean hasContent()
    {
        return bHasContent;
    }

    /**
     * Returns a string representation of this mapping.
     *
     * @return A string representation of this mapping.
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
        sb.append("XSLT     : ")
          .append((sXslTransform != null) ? sXslTransform : "N/A").append("\n");

        sb.append("SOURCE   : ");
        for (int i = 0; i < tSourceTemplate.length; i++)
        {
            sb.append(getObjectId(tSourceTemplate[i])).append("\n");
        }

        sb.append("TARGET   : ");
        for (int i = 0; i < tTargetTemplate.length; i++)
        {
            sb.append(getObjectId(tTargetTemplate[i])).append("\n");
        }

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

    private String getPathKeys(Template[] templateArr)
    {
        StringBuffer templatePath = new StringBuffer("");
        for (int i = 0; i < templateArr.length; i++)
        {
        	if (templatePath.length() > 0) 
        		templatePath.append(",");
        	templatePath.append(templateArr[i].sPathKey);
        }
        return templatePath.toString();
    }
    
    private String getEntityIds(Template[] templateArr)
    {
        StringBuffer entityIds = new StringBuffer("");
        for (int i = 0; i < templateArr.length; i++)
        {
        	if (entityIds.length() > 0) 
        		entityIds.append(",");
        	entityIds.append(templateArr[i].sEntityId);
        }
        return entityIds.toString();
    }
}
