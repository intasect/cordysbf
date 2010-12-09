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
package com.cordys.coe.bf.content.base.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.bf.utils.XmlPrettyPrintOutputStream;
import com.cordys.coe.util.log.LogInterface;

/**
 * Base class for all file system write methods.
 *
 * @author mpoyhone
 */
public abstract class FileSystemWriteMethodBase extends WriteMethodBase
{
    /**
     * Contains the method version ID.
     */
    protected String sMethodVersion;
    protected BFContext bcContext;
    protected ContentSourceFileSystem csSource;
    
    public FileSystemWriteMethodBase(BFContext bcContext, ContentSourceFileSystem csSource) {
        this.bcContext = bcContext;
        this.csSource = csSource; 
        this.sMethodVersion = bcContext.getConfig().getVersionInfo().getMajorVersion().getCobocString();
    }
    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#deleteObject(com.cordys.coe.bf.content.base.IContent)
     */
    public IContent deleteObject(IContent cObject) throws BFException
    {
        return updateObject(cObject, null);
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
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#insertObject(com.cordys.coe.bf.content.base.IContent, boolean)
     */
    public IContent insertObject(IContent cObject, boolean bCheckForOld) throws BFException
    {
        return updateObject(null, cObject);
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
        String key = ((CobocContentHandle) cUpdateObject.getHandle()).getKey();
        
        if (key == null || key.length() == 0)
        {
            throw new BFException("Key not set for " + cUpdateObject.getLogName());
        }
        
        if (bUseContentFiltering && 
            ! csSource.checkForAccess(ctType, key, 
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

        File fFile = getDestFile(cUpdateObject);
        
        if (ctTuple.isDelete()) {
            // This is a delete operation.
            if (! fFile.delete()) {
                throw new BFException("Unable to delete " + ctType.getLogName() + " file " + fFile);
            }
            
            return null;
        }
        
        OutputStream osOutputStream = null;
        XMLStreamWriter xswWriter = null;
        boolean bSuccess = false;
        IXmlDestination xdWriteDest;
        BindingParameters bpParams = new BindingParameters();
        
        bpParams.setParameter("bf-version", getMethodVersion());
        
        try {
            osOutputStream = openFile(ctType, fFile);
            xswWriter = createWriter(osOutputStream);
            xdWriteDest = IXmlDestination.Factory.newInstance(xswWriter);
            
            xswWriter.writeStartDocument();
            xswWriter.setDefaultNamespace("");
            
            cmMarshaller.marshalObject(cUpdateObject, xdWriteDest, bpParams);
            
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
    
    protected File getDestFile(IContent obj) throws BFException
    {
        EContentType objType = obj.getType();
        File rootFolder = csSource.getContentRootDirectory(objType);
        File res;
        
        if (rootFolder == null) {
            throw new BFException("Content root directory not configured for content type " + objType);
        }
        
        res = csSource.getContentFileName(obj.getHandle(), ((CobocContentBase) obj).getName());
           
        return res;
    }
    
    
    protected XMLStreamWriter createWriter(OutputStream osOutput) throws BFException {
        XMLStreamWriter xswWriter;
        
        try
        {
            xswWriter = AxiomUtils.xofXmlOuputFactory.createXMLStreamWriter(osOutput);
        }
        catch (XMLStreamException e)
        {
            throw new BFException("Exception while creating the XML output writer.", e);
        }
        catch (FactoryConfigurationError e)
        {
            throw new BFException("Unable to create the XML output writer.", e);
        }   
        
        return xswWriter;
    }
    
    protected OutputStream openFile(EContentType ctType, File fFile) throws FileNotFoundException {
        // Create the parent directories.
        File fParent = fFile.getParentFile();
        
        if (fParent != null && ! fParent.exists()) {
            fParent.mkdirs();
        }
        
        OutputStream osRes = new FileOutputStream(fFile);
        
        if (bcContext.getConfig().isContentPrettyPrint(ctType, EContentSourceType.FILESYSTEM)) {
            osRes = new XmlPrettyPrintOutputStream(osRes);
        }
        
        return osRes;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentHandlingMethod#getMethodVersion()
     */
    public String getMethodVersion()
    {
        return sMethodVersion;
    }    
}
