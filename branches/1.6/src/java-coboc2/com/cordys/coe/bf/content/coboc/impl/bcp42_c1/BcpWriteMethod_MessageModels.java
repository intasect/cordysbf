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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.impl.BcpWriteMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.messagemodels.InboxModel_C1;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.LogInterface;
import com.cordys.tools.ant.cm.EBcpVersion;

/**
 * Write method for BCP CoBOC message models.
 *
 * @author mpoyhone
 */
public class BcpWriteMethod_MessageModels extends BcpWriteMethodBase
{
    public BcpWriteMethod_MessageModels(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceBcp) csSource);
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#deleteObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent deleteObject(IContentHandle chHandle) throws BFException
    {
        throw new BFException("Delete with handle not supported at the moment.");
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
        
        IContent cUpdateObject = ctTuple.getWriteObject();
        EContentType ctType = cUpdateObject.getType();
        String sSubType = null;
        QName qnMethodName;
        
        switch (ctType) {
        case COBOC_INBOX_MODEL_C1 : 
            if (ctTuple.isUpdate()) {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_UPDATEMESSAGEMODEL;
                sSubType = "BCP-Update";
            } else if (ctTuple.isInsert()) {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_CREATEMESSAGEMODEL;
                sSubType = "BCP-Insert";
            } else {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_DELETEMESSAGEMODEL;
                sSubType = "BCP-Delete";
            }
            break;
            
        default:
            throw new BFException("Illegal update type " + ctType);
        }
        
        if (! checkFiltering(cUpdateObject)) {
            // This item is filtered out.
            return null;
        }
        
        IContentMarshaller cmMarshaller = csSource.getContentMarshaller(ctType, sSubType, getMethodVersion());
        ISoapRequest srRequest = csSource.createSoapRequest();
        LogInterface liLogger = bcContext.getLogger();
        
        if (liLogger.isInfoEnabled()) {
            if (! ctTuple.isDelete()) {
                String sOpName = ctTuple.isInsert() ? "Inserting" : "Updating";
                liLogger.info(sOpName + " " + cUpdateObject.getLogName() + " to " + EContentSourceType.BCP.getLogName());
            } else {
                liLogger.info("Deleting " + cUpdateObject.getLogName() + " from " + EContentSourceType.BCP.getLogName());
            }
        }
        
        return sendSimpleUpdateRequest(srRequest, qnMethodName, ctTuple.getWriteObject(), cmMarshaller);
    }
    
    protected IContent handleUpdateResponse(OMElement oeResponse, IContent cUpdateObject) throws BFException {
        if (bcContext.getConfig().getVersionInfo().isLaterThan(EBcpVersion.BCP42_C3)) {
            // C3 does not return any data on successful delete.
            return null;
        }
        
        try
        {
            AXIOMXPath xpMessagePath = new AXIOMXPath(".//ns:tuple/*/ns:MESSAGE_MODEL");
            OMElement oeMessage;
            
            xpMessagePath.addNamespace("ns", oeResponse.getQName().getNamespaceURI());
            oeMessage = (OMElement) xpMessagePath.selectSingleNode(oeResponse);
            
            if (oeMessage == null) {
                // For deletes we get just a simple text.
                AXIOMXPath xpDeletePath = new AXIOMXPath(".//ns:tuple");
                
                xpDeletePath.addNamespace("ns", oeResponse.getQName().getNamespaceURI());
                oeMessage = (OMElement) xpDeletePath.selectSingleNode(oeResponse);
                
                if (oeMessage != null) {
                    String sText = oeMessage.getText();
                    
                    if (sText != null) {
                        sText = sText.toLowerCase();
                        
                        if (sText.contains("deleted") && sText.contains("sucessfully")) {
                            // Deletion succeeded.
                            return null;
                        }
                    }
                }
                
                throw new BFException("'MESSAGE_MODEL' element is missing from the SOAP response.");
            }
            
            QName qnTmp = oeMessage.getQName();
            String sModelId = null;
            String sLastModified = null;
            OMElement oeTmp;
            
            if ((oeTmp = oeMessage.getFirstChildWithName(AxiomUtils.createQName("MODEL_ID", qnTmp.getPrefix(), qnTmp.getNamespaceURI()))) != null) {
                sModelId = oeTmp.getText();
            }
            
            if ((oeTmp = oeMessage.getFirstChildWithName(AxiomUtils.createQName("LAST_MODIFIED", qnTmp.getPrefix(), qnTmp.getNamespaceURI()))) != null) {
                sLastModified = oeTmp.getText();
            }
            
            if (sModelId == null) {
                throw new BFException("'MODEL_ID' element is missing from the SOAP response.");
            }
             
            if (sLastModified == null) {
                throw new BFException("'LAST_MODIFIED' element is missing from the SOAP response.");
            }
            
            InboxModel_C1 mmRes = new InboxModel_C1();
            
            mmRes.setName(((InboxModel_C1) cUpdateObject).getName());
            mmRes.setObjectId(sModelId);
            mmRes.setLastModified(sLastModified);

            return mmRes;
        }
        catch (Exception e)
        {
            throw new BFException("SOAP response parsing failed.", e);
        }
    }      
}
