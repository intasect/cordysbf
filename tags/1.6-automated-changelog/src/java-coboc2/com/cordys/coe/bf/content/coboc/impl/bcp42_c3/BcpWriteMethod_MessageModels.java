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

import java.io.OutputStream;

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
import com.cordys.coe.bf.content.base.IContentWriteMethod;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BcpWriteMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.messagemodels.EmailModel;
import com.cordys.coe.bf.content.xmlstore.impl.bcp42_c1.BcpWriteMethod_XmlStore;
import com.cordys.coe.bf.databind.IBindingConstants;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.databind.IBindingTemplate;
import com.cordys.coe.bf.databind.ObjectConverter;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.SoapUtils;
import com.cordys.coe.util.log.LogInterface;

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
        String sSubType = "BCP-Update";
        QName qnMethodName;
        
        switch (ctType) {
        case COBOC_MESSAGE_TEMPLATE :
            qnMethodName = IBcpMethodNames.COBOC_METHOD_UPDATEMESSAGETEMPLATE;
            break;
            
        case COBOC_EMAIL_MODEL :
            qnMethodName = IBcpMethodNames.COBOC_METHOD_UPDATEEMAILMODEL;
            break;
        
        case COBOC_INBOX_MODEL_C3 : 
            if (ctTuple.isUpdate()) {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_UPDATEMESSAGEMODEL;
            } else if (ctTuple.isInsert()) {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_CREATEMESSAGEMODEL;
            } else {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_DELETEMESSAGEMODEL;
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
            String sOpName = ctTuple.isInsert() ? "Inserting" : "Updating";
            liLogger.info(sOpName + " " + cUpdateObject.getLogName() + " to " + EContentSourceType.BCP.getLogName());
        }
        
        if (cmMarshaller == null) {
            throw new BFException("No CoBOC update marshaller configured for content type " + ctType.getLogName());
        }
        
        // TODO: Figure out how to get this cleanly.
        IBindingTemplate btRequestTemplate = ((ObjectConverter) cmMarshaller).getBindingTemplate();
        IXmlDestination xdDestination;
        
        try
        {
            // Start the SOAP request.
            OutputStream osSoapRequest = srRequest.openSoapRequest();
            
            // Create the SOAP envelope and body.
            xdDestination = SoapUtils.startSoapRequestBody(osSoapRequest, null);
            
            IBindingContext bcContext = btRequestTemplate.createMarshallingContext(xdDestination);
            
            bcContext.setJavaBean(IBindingConstants.BEANNAME_DEFAULT, ctTuple);
            bcContext.getParameters().setBcpUpdateModeFromTuple(ctTuple);

            // Add the SOAP method and contents. This will use the CustomTupleHandler to 
            // marshall the right objects from the content tuple object.
            SoapUtils.addSoapRequestMethod(qnMethodName, bcContext, xdDestination);

            // Close the SOAP envelope and body.
            SoapUtils.endSoapRequest(xdDestination, false);
            
            // Send the request and receive response..
            OMElement oeResponse = srRequest.sendSoapRequest();
            
            // Read the response method.
            try {
                IContent cContent;
                
                cContent = handleUpdateResponse(oeResponse, ctTuple.getWriteObject());
                
                if (cContent.getType() == EContentType.COBOC_EMAIL_MODEL) {
                    writeEmailModelDetails(ctTuple);
                }
                
                return cContent;
            }
            catch (Exception e) {
                throw new BFException("Unable to read the CoBOC object SOAP response.", e);
            }
        }
        catch (Exception e)
        {
            throw new BFException("SOAP request failed.", e);
        }
        finally {
            srRequest.closeSoapRequest();
        }
    }
    
    protected void writeEmailModelDetails(ContentTuple ctTuple) throws BFException
    {
        ContentTuple ctDetailTuple = ctTuple;
        
        if (ctTuple.isUpdate()) {
            String lm = ((EmailModel) ctTuple.getOld()).getDetailLastModified();
            
            if (lm == null || lm.length() == 0)
            {
                // The XMLStore object is not inserted yet, so we need to change
                // this to an insert operation.
                ctDetailTuple = new ContentTuple(null, ctTuple.getNew());
            }
        }
        
        IContentWriteMethod writeMethod = new BcpWriteMethod_XmlStore(bcContext, csSource);
        IContent response = writeMethod.updateObject(ctDetailTuple);
        
        if (response != null) {
            EmailModel resModel = (EmailModel) response;
            EmailModel writeModel = (EmailModel) ctTuple.getWriteObject();
            
            writeModel.setDetailLastModified(resModel.getLastModified());
        }
    }
    
    protected IContent handleUpdateResponse(OMElement oeResponse, IContent cUpdateObject) throws BFException {
        OMElement oeTuple = oeResponse.getFirstElement();
        
        if (oeTuple == null) {
            return cUpdateObject;
        }
        
        EContentType ctType = cUpdateObject.getType();
        IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(ctType, null, getMethodVersion());
        IXmlSource sUnmarshallSource = IXmlSource.Factory.newInstance(oeTuple);
        
        if (cuUnmarshaller == null) {
            throw new BFException("No unmarshaller configured for CoBOC content type " + ctType);
        }
        
        return cuUnmarshaller.unmarshalObject(sUnmarshallSource);
    }      
}
