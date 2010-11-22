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
package com.cordys.coe.bf.content.coboc.impl.bcp42_c3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem;
import com.cordys.coe.bf.content.base.impl.FileSystemWriteMethodBase;
import com.cordys.coe.bf.content.types.EContentCategory;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.LogInterface;

/**
 * Write method for C3 CoBOC message templates.
 *
 * @author mpoyhone
 */
public class FileSystemWriteMethod_MessageModels extends FileSystemWriteMethodBase
{
    public FileSystemWriteMethod_MessageModels(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceFileSystem) csSource);
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#updateObject(com.cordys.coe.bf.content.base.ContentTuple)
     */
    public IContent updateObject(ContentTuple ctTuple) throws BFException
    {
        IContent updateObject = ctTuple.getWriteObject();
        EContentType ctType = updateObject.getType();
        
        if (ctType.getCategory() != EContentCategory.COBOC_MESSAGE_MODELS) {
            return null;
        }
        
        // Check that we won't overwrite the original-id with another value.
        if (bcContext.getConfig().getCobocConfig().usesOriginalId(updateObject)) {
            File file = getDestFile(updateObject);

            if (file.exists()) {
                String origId = getOriginalId(updateObject, file);
                
                if (origId != null && origId.length() > 0)
                {
                    String newOrig = ((CobocContentBase) updateObject).getOriginalObjectId();
                    
                    if (! origId.equals(newOrig)) {
                        throw new BFException(updateObject.getLogName() + 
                                              ": Original object ID is different from the one in the file. " +
                                              "Multiple BCP servers are supported with " + 
                                              updateObject.getType().getLogName(true));
                    }
                }
            }
        }
        
        return super.updateObject(ctTuple);
    }

    protected String getOriginalId(IContent obj, File f) {
        FileReader frReader = null;
        XMLStreamReader xsrReader = null;
        
        try
        {
            frReader = new FileReader(f.getAbsolutePath());
            xsrReader = AxiomUtils.xifXmlInputFactory.createXMLStreamReader(frReader);
            
            xsrReader.nextTag();
            
            return xsrReader.getAttributeValue(null, "original-id");
        }
        catch (Exception ignored)
        {
            return null;
        }
        finally {
            if (xsrReader != null) {
                try
                {
                    xsrReader.close();
                }
                catch (XMLStreamException ignored)
                {
                }
                xsrReader = null;
            }
            if (frReader != null) {
                try
                {
                    frReader.close();
                }
                catch (IOException ignored)
                {
                }
                frReader = null;
            }
        }       
    }
}
