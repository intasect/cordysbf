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

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BcpWriteMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.LogInterface;

/**
 * Write method for BCP CoBOC rules and rule groups. When an object is updated or inserted this 
 * method will update the object ID in the given object and not return a new instance based on the
 * data received in the SOAP response.   
 *
 * @author mpoyhone
 */
public class BcpWriteMethod_CobocRules extends BcpWriteMethodBase
{
    public BcpWriteMethod_CobocRules(BFContext bcContext, IContentSource csSource) throws BFException {
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
        QName qnMethodName;
        
        switch (ctType) {
        case COBOC_RULES_RULE : 
            if (ctTuple.isUpdate()) {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_UPDATERULE;
            } else if (ctTuple.isInsert()) {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_ADDRULE;
            } else {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_DELETERULE;
            }
            break;
            
        case COBOC_RULES_RULEGROUP : 
            if (ctTuple.isUpdate()) {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_UPDATERULEGROUP;
            } else if (ctTuple.isInsert()) {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_CREATERULEGROUP;
            } else {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_DELETERULEGROUP;
            }
            break;       
            
        default:
            throw new BFException("Illegal update type " + ctType);
        }
        
        IContentMarshaller cmMarshaller = csSource.getContentMarshaller(ctType, null, getMethodVersion());
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
        
        return sendUpdateRequest(srRequest, qnMethodName, ctTuple, cmMarshaller);
    }
    
    protected IContent handleUpdateResponse(OMElement oeResponse, IContent cUpdateObject) throws BFException {
        OMElement oeTuple = oeResponse.getFirstElement();
        IXmlSource sUnmarshallSource = IXmlSource.Factory.newInstance();
        
        try
        {
            for (; oeTuple != null; oeTuple = AxiomUtils.getNextSiblingElement(oeTuple)) {
                OMElement oeOld = oeTuple.getFirstElement();
                
                if (oeOld == null) {
                    continue;
                }
                
                OMElement oeContentElement = oeOld.getFirstElement();
                
                if (oeContentElement == null) {
                    continue;
                }
                
                IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(cUpdateObject.getType(), null, getMethodVersion());
                    
                if (cuUnmarshaller == null) {
                    throw new BFException("Unable to get content unmarshaller for content type " + cUpdateObject.getType());
                }
                
                sUnmarshallSource.set(oeContentElement);
                 
                IContent cCreatedContent = cuUnmarshaller.unmarshalObject(sUnmarshallSource);
                
                return cCreatedContent;
            }
        }
        catch (Exception e)
        {
            throw new BFException("SOAP response parsing failed.", e);
        }
        
        return null;
    }
}
