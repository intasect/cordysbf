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
package com.cordys.coe.bf.content.coboc.impl.bcp42_c1;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem;
import com.cordys.coe.bf.content.base.impl.FileSystemWriteMethodBase;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.folders.ActionTemplateXForm;
import com.cordys.coe.bf.content.types.coboc.folders.ConditionTemplateXForm;
import com.cordys.coe.bf.content.types.coboc.folders.Folder;
import com.cordys.coe.bf.content.types.coboc.folders.SpecialAttribute;
import com.cordys.coe.bf.content.types.coboc.folders.Template;
import com.cordys.coe.bf.content.types.xmlstore.XmlStoreContentBase;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.util.log.LogInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Write method for CoBOC schedule templates.
 *
 * @author mpoyhone
 */
public class FileSystemWriteMethod_Folders extends FileSystemWriteMethodBase
{
    /**
     * Creates a new FileSystemWriteMethod_Folders object.
     *
     * @param bcContext DOCUMENTME
     * @param csSource DOCUMENTME
     *
     * @throws BFException DOCUMENTME
     */
    public FileSystemWriteMethod_Folders(BFContext bcContext,
            IContentSource csSource)
                                  throws BFException
    {
        super(bcContext, (ContentSourceFileSystem) csSource);
    }

    /**
     * 
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#deleteObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent deleteObject(IContentHandle chHandle)
                          throws BFException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#updateObject(com.cordys.coe.bf.content.base.ContentTuple)
     */
    public IContent updateObject(ContentTuple ctTuple)
                          throws BFException
    {
        if (ctTuple.isUpdate())
        {
            if (ctTuple.getOld().getType() != ctTuple.getNew().getType())
            {
                throw new BFException("Old object is of different type than the new object.");
            }
        }

        LogInterface liLogger = bcContext.getLogger();
        IContent cUpdateObject = ctTuple.getWriteObject();
        EContentType ctType = cUpdateObject.getType();

        if (ctType == EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE)
        {
            // These are written along with templates into the same file.
            return null;
        }

        if (bUseContentFiltering)
        {
            String key;

            if (cUpdateObject instanceof CobocContentBase)
            {
                key = ((CobocContentBase) cUpdateObject).getKey();
            }
            else if (cUpdateObject instanceof XmlStoreContentBase)
            {
                key = ((XmlStoreContentBase) cUpdateObject).getKey();
            }
            else
            {
                return null;
            }

            if (!csSource.checkForAccess(ctType, key,
                                             ctType == EContentType.COBOC_FOLDERS_FOLDER))
            {
                // This item is filtered out.
                return null;
            }
        }

        if (liLogger.isDebugEnabled())
        {
            liLogger.debug("Writing CoBOC " + ctType.getLogName() + " " +
                           cUpdateObject.getHandle().getLogName() + " to " +
                           EContentSourceType.FILESYSTEM.getLogName());
        }

        IContentMarshaller cmMarshaller = csSource.getContentMarshaller(ctType,
                                                                        null,
                                                                        getMethodVersion());

        if (cmMarshaller == null)
        {
            throw new BFException("No content marshaller found for " +
                                  ctType.getLogName(true));
        }

        File fFile;
        String sObjectName;

        switch (ctType)
        {
            case COBOC_FOLDERS_ACTIONTEMPLATE_XFORM:
                sObjectName = ((ActionTemplateXForm) cUpdateObject).getFileSystemName();
                break;

            case COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM:
                sObjectName = ((ConditionTemplateXForm) cUpdateObject).getFileSystemName();
                break;

            default:
                sObjectName = ((CobocContentBase) cUpdateObject).getName();
                break;
        }

        fFile = csSource.getContentFileName(cUpdateObject.getHandle(),
                                            sObjectName);

        if (ctTuple.isDelete())
        {
            // This is a delete operation.
            if (!fFile.delete())
            {
                throw new BFException("Unable to delete file " + fFile);
            }

            return null;
        }

        if (ctType == EContentType.COBOC_FOLDERS_FOLDER)
        {
            // We do not write the folder files anymore, but we create the folders.
            // Get file's parent because the file points to the 00-FOLDER_INFO.xml file. 
            File fParent = fFile.getParentFile();
            Folder fFolder = (Folder) cUpdateObject;
            boolean bCreateFolder = !fParent.exists();

            if (fFolder.getChildren().size() == 0)
            {
                // Folder is empty, so check if children were filtered out.
                // In that case we won't write this folder either.
                String sKey = fFolder.getKey();
                boolean bChildrenFiltered = !csSource.checkForAccess(ctType,
                                                                     sKey +
                                                                     "/dummy",
                                                                     false);

                if (bChildrenFiltered)
                {
                    bCreateFolder = false;
                }
            }

            if (bCreateFolder)
            {
                if (!fParent.mkdirs())
                {
                    throw new BFException("Unable to create directory " +
                                          fParent);
                }
            }

            return null;
        }

        OutputStream osOutputStream = null;
        XMLStreamWriter xswWriter = null;
        boolean bSuccess = false;
        IXmlDestination xdWriteDest;
        BindingParameters bpParams = new BindingParameters();

        try
        {
            osOutputStream = openFile(ctType, fFile);
            xswWriter = createWriter(osOutputStream);
            xdWriteDest = IXmlDestination.Factory.newInstance(xswWriter);

            xswWriter.writeStartDocument();
            xswWriter.setDefaultNamespace("");

            if (ctType == EContentType.COBOC_FOLDERS_TEMPLATE)
            {
                xswWriter.writeStartElement("template_content");
                xswWriter.writeDefaultNamespace("");
            }

            bpParams.setParameter("bf-version", getMethodVersion());
            cmMarshaller.marshalObject(cUpdateObject, xdWriteDest, bpParams);

            if (ctType == EContentType.COBOC_FOLDERS_TEMPLATE)
            {
                writeSpecialAttributes(xswWriter, (Template) cUpdateObject);
                xswWriter.writeEndElement(); // template_content
            }

            xswWriter.writeEndDocument();
            bSuccess = true;
        }
        catch (FileNotFoundException e)
        {
            throw new BFException("Unable to create " + ctType.getLogName() +
                                  " file " + fFile, e);
        }
        catch (XMLStreamException e)
        {
            throw new BFException("Unable to create " + ctType.getLogName() +
                                  " file " + fFile, e);
        }
        catch (IOException e)
        {
            throw new BFException("Unable to create " + ctType.getLogName() +
                                  " file " + fFile, e);
        }
        finally
        {
            if (xswWriter != null)
            {
                try
                {
                    xswWriter.flush();
                }
                catch (XMLStreamException ignored)
                {
                }

                try
                {
                    xswWriter.close();
                }
                catch (XMLStreamException ignored)
                {
                }
                xswWriter = null;
            }

            if (osOutputStream != null)
            {
                try
                {
                    osOutputStream.close();
                }
                catch (IOException ignored)
                {
                }
                osOutputStream = null;
            }

            if (!bSuccess)
            {
                fFile.delete();
            }
        }

        if (liLogger.isInfoEnabled())
        {
            liLogger.info("Wrote " + cUpdateObject.getType().getLogName() + " " + cUpdateObject.getHandle().getLogName());
        }

        return null;
    }

    /**
     * DOCUMENTME
     *
     * @param xswWriter DOCUMENTME
     * @param tTemplate DOCUMENTME
     *
     * @throws BFException DOCUMENTME
     * @throws XMLStreamException DOCUMENTME
     * @throws IOException DOCUMENTME
     */
    private void writeSpecialAttributes(XMLStreamWriter xswWriter,
                                        Template tTemplate)
                                 throws BFException, XMLStreamException,
                                        IOException
    {
        Collection<IContentHandle> cAttribList = tTemplate.getChildren();

        if ((cAttribList == null) || cAttribList.isEmpty())
        {
            return;
        }

        xswWriter.writeStartElement("attributes");

        EContentType ctType = EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE;
        IContentMarshaller cmMarshaller = csSource.getContentMarshaller(ctType,
                                                                        null,
                                                                        getMethodVersion());

        if (cmMarshaller == null)
        {
            throw new BFException("No content marshaller found for " +
                                  ctType.getLogName(true));
        }

        IXmlDestination xdWriteDest;
        BindingParameters bpParams = new BindingParameters();

        xdWriteDest = IXmlDestination.Factory.newInstance(xswWriter);
        bpParams.setParameter("bf-version", "bcp42_c1");

        Map<String, IContentHandle> mSortedAttribs = new TreeMap<String, IContentHandle>();

        // Sort the attributes so that they are always written in the same order (helps comparing different versions).
        for (IContentHandle chAttribHandle : cAttribList)
        {
            mSortedAttribs.put(chAttribHandle.getLogName(), chAttribHandle);
        }

        for (IContentHandle chAttribHandle : mSortedAttribs.values())
        {
            IContent cTmp = bcContext.findContent(chAttribHandle);

            if (cTmp == null)
            {
                throw new BFException("Unable to find " + ctType.getLogName() +
                                      " with ID " +
                                      chAttribHandle.getLogName());
            }

            if (!(cTmp instanceof SpecialAttribute))
            {
                throw new BFException("Invalid child object class for template: " +
                                      cTmp.getClass());
            }

            cmMarshaller.marshalObject(cTmp, xdWriteDest, bpParams);
        }

        xswWriter.writeEndElement(); // attributes.
    }
}
