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
import com.cordys.tools.ant.soap.SoapRequestException;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Class for reading and writing CoBOC and file template objects. The Template
 * class is the container for individual templates and this class contains the
 * implementations.
 *
 * @author mpoyhone
 */
public class TemplateHandler extends FolderObjectHandler
{
    /**
     * Creates a new MappingHandler instance.
     *
     * @param ccContext The CoBOCContext that is used when storing and accesing
     *        CoBOC objects.
     */
    public TemplateHandler(CoBOCContext ccContext)
    {
        super(ccContext);
    }

    /**
     * Returns the filter attribute value for GetCollection and GetXMLObject.
     *
     * @return The CoBOC entity filter.
     */
    public String getCoBOCEntityFilter()
    {
        return "template";
    }

    /**
     * Returns the entity attribute value for GetCollection and GetXMLObject.
     * Valid values are "entity" or "instance".
     *
     * @return The CoBOC entity filter.
     */
    public String getCoBOCEntityType()
    {
        return "entity";
    }

    /**
     * Checks if the read file was of correct type and returns the tuple XML
     * structure.
     *
     * @param iFileNode The XML structure that was read from the file.
     *
     * @return None-zero, if this class can handle objects of this type,
     *         otherwise zero.
     */
    public int checkXmlFileType(int iFileNode)
    {
        return Template.getTemplateTuple(iFileNode);
    }

    /**
     * Fetches the attributes for all templates from ECX.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public void convertTemplateAttributesFromECX()
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
                int[] iaTupleNodes;
                Template tTemplate = (Template) coObject;

                try
                {
                    iaTupleNodes = ccContext.cmSelect.execute(ccContext.getSoapManager(),
                                                              CoBOCConstants.COBOC_ATTRIBUTE_TABLE_NAME,
                                                              CoBOCConstants.COBOC_ATTRIBUTE_FIELD_NAME,
                                                              tTemplate.getEntityID(),
                                                              ccContext.getSoapTimeout(),
                                                              ccContext.getLogger());

                    for (int i = 0; i < iaTupleNodes.length; i++)
                    {
                        int iTuple = iaTupleNodes[i];
                        TemplateAttribute taAttrib = new TemplateAttribute(ccContext);

                        taAttrib.convertFromECX(iTuple);

                        tTemplate.addChildObject(taAttrib);
                        ccContext.getLogger().info("    Read attribute " +
                                                   taAttrib.sAttributeName);
                    }
                }
                catch (SoapRequestException e)
                {
                	// When no attributes can be found, a SOAP:Fault is generated.
                	String sMsg = e.toString();
                	
                	if (sMsg.indexOf("no records found") == 0) {
                		throw new ContentException(e);
                	}
                }

                return true;
            }
        };

        fRootFolder.recurseFolderContentsBreadthFirst(FolderCallback.MASK_TEMPLATE,
                                                      fcHandler);
    }

    /**
     * Writes the attributes of all templates to ECX.
     *
     * @throws ContentException Thrown if the conversion failed.
     */
    public void convertTemplateAttributesToECX()
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
                Template tTemplate = (Template) coObject;
                Iterator<CoBOCObject> iAttribIter = tTemplate.getAttributeIterator();

                if (iAttribIter == null)
                {
                    return true;
                }

                // Write all attributes to ECX.
                while (iAttribIter.hasNext())
                {
                    TemplateAttribute taAttrib = (TemplateAttribute) iAttribIter.next();
                    int iNewTupleNode;
                    int iOldTupleNode;

                    // Try to fetch an old value of this attribute.
                    iOldTupleNode = fetchTemplateAttribute(tTemplate,
                                                           taAttrib.sAttributeName);

                    // Convert the attribute to ECX XML format.
                    iNewTupleNode = taAttrib.convertToECX(ccContext.getDocument());

                    // Do an insert or update.
                    if (iOldTupleNode != 0)
                    {
                        try
                        {
                            ccContext.cmModify.execute(ccContext.getSoapManager(),
                                                       CoBOCConstants.COBOC_ATTRIBUTE_TABLE_NAME,
                                                       iOldTupleNode,
                                                       iNewTupleNode,
                                                       ccContext.getSoapTimeout(),
                                                       ccContext.getLogger());
                        }
                        catch (SoapRequestException e)
                        {
                            throw new ContentException(e);
                        }
                    }
                    else
                    {
                        try
                        {
                            ccContext.cmCreate.execute(ccContext.getSoapManager(),
                                                       CoBOCConstants.COBOC_ATTRIBUTE_TABLE_NAME,
                                                       iNewTupleNode,
                                                       ccContext.getSoapTimeout(),
                                                       ccContext.getLogger());
                        }
                        catch (SoapRequestException e)
                        {
                            throw new ContentException(e);
                        }
                    }

                    ccContext.getLogger().info("    Wrote attribute " +
                                               taAttrib.sAttributeName);
                }

                return true;
            }
        };

        fRootFolder.recurseFolderContentsBreadthFirst(FolderCallback.MASK_TEMPLATE,
                                                      fcHandler);
    }

    /**
     * Finds an object from the context by the entity ID or creates a new one,
     * if bCreate parameter is true.
     *
     * @param sEntityId The entity ID that is used in the search.
     * @param bCreate If true, a new object is created.
     *
     * @return The found or created object, or null if no object was found.
     */
    public CoBOCObject findObjectById(String sEntityId, boolean bCreate)
    {
        return Template.findTemplateById(ccContext, sEntityId, bCreate);
    }

    /**
     * Finds an object from the context by the entity key or creates a new one,
     * if bCreate parameter is true.
     *
     * @param sEntityPathKey The entity ID that is used in the search.
     * @param bCreate If true, a new object is created.
     *
     * @return The found or created object, or null if no object was found.
     */
    public CoBOCObject findObjectByPathKey(String sEntityPathKey,
                                           boolean bCreate)
    {
        return Template.findTemplateByPathKey(ccContext, sEntityPathKey, bCreate);
    }

    /**
     * Returns the bit mask that identifies the types of objects that are
     * fetched from folders. The mask values are defined in  FolderCallback
     * class.
     *
     * @return The bit mask indicating the types of objects this handler
     *         fetched from folders.
     */
    protected int getFolderObjectMask()
    {
        return FolderCallback.MASK_TEMPLATE;
    }

    /**
     * Returns the XML structure of a template attribute.
     *
     * @param tTemplate The template in question.
     * @param sAttribName Attribute name.
     *
     * @return The tuple XML structure of a template attribute or null if
     *         nothing was found.
     *
     * @throws ContentException Thrown if the fetching failed.
     */
    protected int fetchTemplateAttribute(Template tTemplate, String sAttribName)
                                  throws ContentException
    {
        int[] iaTupleNodes;

        try
        {
            iaTupleNodes = ccContext.cmSelect.execute(ccContext.getSoapManager(),
                                                      CoBOCConstants.COBOC_ATTRIBUTE_TABLE_NAME,
                                                      CoBOCConstants.COBOC_ATTRIBUTE_FIELD_NAME,
                                                      tTemplate.getEntityID(),
                                                      ccContext.getSoapTimeout(),
                                                      ccContext.getLogger());

            for (int i = 0; i < iaTupleNodes.length; i++)
            {
                int iTuple = iaTupleNodes[i];
                int iNameNode = Find.firstMatch(iTuple, "?<attribute_name>");

                if ((iNameNode != 0) &&
                        sAttribName.equals(Node.getData(iNameNode)))
                {
                    return iTuple;
                }
            }
        }
        catch (SoapRequestException e)
        {
            throw new ContentException(e);
        }

        return 0;
    }

    /**
     * @see com.cordys.coe.ant.coboc.content.folders.FolderObjectHandler#modifyUpdateRequest(int,
     *      int)
     */
    protected int modifyUpdateRequest(int xUpdateRequestNode, int xOldObjectNode)
                               throws ContentException
    {
        if (xOldObjectNode == 0)
        {
            return xUpdateRequestNode;
        }

		// Copy the last modified field.
        String sLastModified = Node.getAttribute(xOldObjectNode,
                                                 "lastModified");

        if (sLastModified == null)
        {
            throw new ContentException("Attibute lastModified not found from old node.");
        }

        Node.setAttribute(xUpdateRequestNode, "lastModified", sLastModified);

        return xUpdateRequestNode;
    }
}
