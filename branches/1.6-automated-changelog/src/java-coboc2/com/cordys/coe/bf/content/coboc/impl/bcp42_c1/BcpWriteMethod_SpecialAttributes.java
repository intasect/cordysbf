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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BcpWriteMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;
import com.cordys.coe.util.log.LogInterface;

/**
 * Write method for BCP CoBOC template special attributes. 
 *
 * @author mpoyhone
 */
public class BcpWriteMethod_SpecialAttributes extends BcpWriteMethodBase
{
    public BcpWriteMethod_SpecialAttributes(BFContext bcContext, IContentSource csSource) throws BFException {
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
        LogInterface liLogger = bcContext.getLogger();
        ISoapRequestTemplate srtSoapTemplate = null;

        switch (ctType) {
        case COBOC_FOLDERS_SPECIAL_ATTRIBUTE : 
            if (ctTuple.isUpdate()) {
                // First delete the special attribute then insert.
                ContentTuple ctDeleteTuple = new ContentTuple(ctTuple.getOld(), null);
                ContentTuple ctInsertTuple = new ContentTuple(null, ctTuple.getNew());
                
                // Copy the key for logging.
                ctTuple.getOld().getHandle().copyFrom(ctTuple.getNew().getHandle());

                updateObject(ctDeleteTuple);
                
                return updateObject(ctInsertTuple);
            } else if (ctTuple.isInsert()) {
                srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_CREATETEMPLATESPECIAL_ATTRIBUTE, this);
            } else if (ctTuple.isDelete()) {
                srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_DELETETEMPLATESPECIAL_ATTRIBUTE, this);
            }
            break;
            
        default:
            throw new BFException("Illegal update type " + ctType);
        }

        if (srtSoapTemplate == null) {
            throw new BFException("No SOAP request template configured for special attributes.");
        }
        
        ISoapRequest srRequest;
        BindingParameters bpRequestParameters = new BindingParameters();

        bpRequestParameters.setBcpUpdateModeFromTuple(ctTuple);
        srRequest = csSource.createSoapRequest();
        
        if (liLogger.isInfoEnabled()) {
            if (! ctTuple.isDelete()) {
                String sOpName = ctTuple.isInsert() ? "Inserting" : "Updating";
                liLogger.info(sOpName + " " + cUpdateObject.getLogName() + " to " + EContentSourceType.BCP.getLogName());
            } else {
                liLogger.info("Deleting " + cUpdateObject.getLogName() + " from " + EContentSourceType.BCP.getLogName());
            }
        }
        
        IContent cUpdateResult;
            
        cUpdateResult = sendUpdateRequest(srRequest, srtSoapTemplate, ctTuple, bpRequestParameters);
            
        return cUpdateResult;
    }
    
    protected IContent handleUpdateResponse(OMElement oeResponse, IContent cUpdateObject) throws BFException {
        try
        {
            AXIOMXPath xpNewPath = new AXIOMXPath("./ns:tuple/*");
            OMElement oeNew;
            
            xpNewPath.addNamespace("ns", oeResponse.getQName().getNamespaceURI());
            oeNew = (OMElement) xpNewPath.selectSingleNode(oeResponse);
            
            OMElement oeContent = oeNew.getFirstElement();
                
            if (oeContent == null) {
                return null;
            }
                
            IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE, null, getMethodVersion());
            IXmlSource xsUnmarshallSource = IXmlSource.Factory.newInstance(oeContent);
            
            return cuUnmarshaller.unmarshalObject(xsUnmarshallSource);
        }
        catch (Exception e)
        {
            throw new BFException("SOAP response parsing failed.", e);
        }
    }      
}
