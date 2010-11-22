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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.util.log.LogInterface;

/**
 * Write method for CoBOC schedule templates.
 *
 * @author mpoyhone
 */
public class FileSystemWriteMethod_ScheduleTemplates extends FileSystemWriteMethodBase
{
    public FileSystemWriteMethod_ScheduleTemplates(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceFileSystem) csSource);
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#deleteObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent deleteObject(IContentHandle chHandle) throws BFException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#updateObject(com.cordys.coe.bf.content.base.ContentTuple)
     */
    public IContent updateObject(ContentTuple ctTuple) throws BFException
    {
        if (ctTuple.isUpdate()) {
            if (ctTuple.getOld().getType() != ctTuple.getNew().getType()) {
                throw new BFException("Old object is of different type than the new object.");
            }
        }
        
        LogInterface liLogger = bcContext.getLogger();
        IContent cUpdateObject = ctTuple.getWriteObject();
        EContentType ctType = cUpdateObject.getType();
        
        if (ctType != EContentType.COBOC_SCHEDULE_TEMPLATE) {
            return null;
        }
        
        if (bUseContentFiltering && 
            ! csSource.checkForAccess(ctType, ((CobocContentHandle) cUpdateObject.getHandle()).getKey(), 
                                      false)) {
            // This item is filtered out.
            return null;
        }
        
        if (liLogger.isDebugEnabled()) {
            liLogger.debug("Writing CoBOC " + ctType.getLogName() + " " + cUpdateObject.getHandle().getLogName() + " to " + EContentSourceType.FILESYSTEM.getLogName());
        }           
        
        IContentMarshaller cmMarshaller = csSource.getContentMarshaller(ctType, null, getMethodVersion());
                 
        if (cmMarshaller == null) {
            throw new BFException("No content marshaller found for " + ctType.getLogName(true));
        }

        File fRootFolder = csSource.getContentRootDirectory(ctType);
        File fFile;
        
        if (fRootFolder == null) {
            throw new BFException("Content root directory not configured for content type " + ctType);
        }
        
        fFile = csSource.getContentFileName(cUpdateObject.getHandle(), ((CobocContentBase) cUpdateObject).getName());
        
        if (ctTuple.isDelete()) {
            // This is a delete operation.
            if (! fFile.delete()) {
                throw new BFException("Unable to delete schedule template file " + fFile);
            }
            
            return null;
        }
        
        OutputStream osOutputStream = null;
        XMLStreamWriter xswWriter = null;
        boolean bSuccess = false;
        IXmlDestination xdWriteDest;
        
        try {
            osOutputStream = openFile(ctType, fFile);
            xswWriter = createWriter(osOutputStream);
            xdWriteDest = IXmlDestination.Factory.newInstance(xswWriter);
            
            xswWriter.writeStartDocument();
            xswWriter.setDefaultNamespace("");
            xswWriter.writeStartElement("schedule-template");
            xswWriter.writeDefaultNamespace("");
            xswWriter.writeAttribute("bf-version", getMethodVersion());
            
            cmMarshaller.marshalObject(cUpdateObject, xdWriteDest);
            
            xswWriter.writeEndElement(); // schedule-template
            xswWriter.writeEndDocument();
            bSuccess = true;
            
        }
        catch (FileNotFoundException e)
        {
            throw new BFException("Unable to create " + ctType.getLogName() + " file " + fFile, e);
        }
        catch (XMLStreamException e)
        {
            throw new BFException("Unable to create " + ctType.getLogName() + " file " + fFile, e);
        }
        catch (IOException e)
        {
            throw new BFException("Unable to create " + ctType.getLogName() + " file " + fFile, e);
        }
        finally {
            if (xswWriter != null) {
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
            
            if (osOutputStream != null) {
                try
                {
                    osOutputStream.close();
                }
                catch (IOException ignored)
                {
                }
                osOutputStream = null;
            }
            
            if (! bSuccess) {
                fFile.delete();
            }
        }
        
        if (liLogger.isInfoEnabled()) {
            liLogger.info("Wrote "+ cUpdateObject.getHandle().getLogName() + " to file " + fFile.getAbsolutePath());
        }           
        
        return null;
    }

}
