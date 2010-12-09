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
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

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
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.coboc.rules.RuleGroup;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.util.log.LogInterface;

/**
 * Write method for CoBOC rules.
 *
 * @author mpoyhone
 */
public class FileSystemWriteMethod_CobocRules extends FileSystemWriteMethodBase
{
    public FileSystemWriteMethod_CobocRules(BFContext bcContext, IContentSource csSource) throws BFException {
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
        
        if (ctType != EContentType.COBOC_RULES_RULEGROUP) {
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
        
        RuleGroup rgRuleGroup = (RuleGroup) cUpdateObject;
        IContentMarshaller cmRuleGroupMarshaller = csSource.getContentMarshaller(EContentType.COBOC_RULES_RULEGROUP, null, getMethodVersion());
        IContentMarshaller cmRuleMarshaller = csSource.getContentMarshaller(EContentType.COBOC_RULES_RULE, null, getMethodVersion());
        
        if (cmRuleGroupMarshaller == null) {
            throw new BFException("No content marshaller found for rule groups.");
        }

        if (cmRuleMarshaller == null) {
            throw new BFException("No content marshaller found for rules.");
        }
        
        File fRootFolder = csSource.getContentRootDirectory(ctType);
        File fRuleGroupFile;
        
        if (fRootFolder == null) {
            throw new BFException("Content root directory not configured for content type " + ctType);
        }
        
        fRuleGroupFile = csSource.getContentFileName(cUpdateObject.getHandle(), rgRuleGroup.getName());
        
        if (ctTuple.isDelete()) {
            // This is a delete operation.
            if (! fRuleGroupFile.delete()) {
                throw new BFException("Unable to delete rule group file " + fRuleGroupFile);
            }
            
            return null;
        }
        
        OutputStream osOutputStream = null;
        XMLStreamWriter xswWriter = null;
        boolean bSuccess = false;
        IXmlDestination xdWriteDest;
        
        try {
            osOutputStream = openFile(ctType, fRuleGroupFile);
            xswWriter = createWriter(osOutputStream);
            xdWriteDest = IXmlDestination.Factory.newInstance(xswWriter);
            
            xswWriter.writeStartDocument();
            xswWriter.writeStartElement("rulegroup");
            xswWriter.writeAttribute("bf-version", getMethodVersion());
            
            xswWriter.writeStartElement("rulegroup-content");
            cmRuleGroupMarshaller.marshalObject(rgRuleGroup, xdWriteDest);
            xswWriter.writeEndElement(); // rulegroup-content
            
            xswWriter.writeStartElement("rules");
            
            Collection<IContentHandle> lRules = rgRuleGroup.getChildren();
            Map<String, IContentHandle> mSortedRules = new TreeMap<String, IContentHandle>();
            
            // Sort the rules so that they are always written in the same order (helps comparing different versions).
            for (IContentHandle chRuleHandle : lRules) {
                mSortedRules.put(chRuleHandle.getLogName(), chRuleHandle);
            }
            
            for (IContentHandle chRuleHandle : mSortedRules.values())
            {
                if (liLogger.isInfoEnabled()) {
                    liLogger.info("Writing CoBOC " + chRuleHandle.getContentType().getLogName() + " " + 
                            chRuleHandle.getLogName() + " to file " + fRuleGroupFile);
                }           
                
                IContent cRule = bcContext.findContent(chRuleHandle);
                
                if (cRule == null) {
                    throw new BFException("Rule object not found:" + cRule);
                }
                
                cmRuleMarshaller.marshalObject(cRule, xdWriteDest);
            }
            
            xswWriter.writeEndElement(); //rules
            
            xswWriter.writeEndElement(); // rulegroup
            xswWriter.writeEndDocument();
            bSuccess = true;
            
        }
        catch (FileNotFoundException e)
        {
            throw new BFException("Unable to create rule group file " + fRuleGroupFile, e);
        }
        catch (XMLStreamException e)
        {
            throw new BFException("Unable to write rule group file " + fRuleGroupFile, e);
        }
        catch (IOException e)
        {
            throw new BFException("Unable to write rule group file " + fRuleGroupFile, e);
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
                fRuleGroupFile.delete();
            }
        }
        
        if (liLogger.isInfoEnabled()) {
            liLogger.info("Wrote "+ cUpdateObject.getHandle().getLogName() + " to file " + fRuleGroupFile.getAbsolutePath());
        }             
        
        return null;
    }

}
