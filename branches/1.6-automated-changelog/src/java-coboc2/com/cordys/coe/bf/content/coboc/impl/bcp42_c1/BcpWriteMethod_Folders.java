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

import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.beanutils.BeanUtils;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BcpWriteMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.databind.IBindingConstants;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.databind.IBindingTemplate;
import com.cordys.coe.bf.databind.ObjectConverter;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.exception.SoapFaultException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.SoapUtils;
import com.cordys.coe.util.log.LogInterface;

/**
 * Write method for BCP CoBOC folder content. 
 *
 * @author mpoyhone
 */
public class BcpWriteMethod_Folders extends BcpWriteMethodBase
{
    public BcpWriteMethod_Folders(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceBcp) csSource);
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
        
        IContent cUpdateObject = ctTuple.getWriteObject();
        EContentType ctType = cUpdateObject.getType();
        
        if (! checkFiltering(cUpdateObject)) {
            // This item is filtered out.
            return null;
        }

        if (ctType == EContentType.COBOC_FOLDERS_FOLDER) {
            if ("/".equals(((CobocContentBase) cUpdateObject).getKey())) {
                // Do not modify the root folder.
                return null;
            }
            
            if (ctTuple.isUpdate()) {
                // We do not update folders anymore because the only updateable field is
                // description and that probably isn't used by anyone.
                return null;
            }
        }
        
        if (ctTuple.isDelete()) {
            return handleDeletion(ctTuple);
        }
        
        // Root folder updates require the parent ID to be copied from the old.
        if (ctTuple.isUpdate() && ! ctTuple.getNew().getParent().isSet()) {
            try
            {
                BeanUtils.setProperty(ctTuple.getNew(), "parentId",  BeanUtils.getProperty(ctTuple.getOld(), "parentId"));
            }
            catch (Exception e)
            {
                throw new BFException("Unable to copy CoBOC object property parentId.");
            }
        }
        
        IContentMarshaller cmMarshaller = csSource.getContentMarshaller(ctType, IBindingConstants.SUBTYPE_BCP_UPDATE, getMethodVersion());
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
            SoapUtils.addSoapRequestMethod(IBcpMethodNames.COBOC_METHOD_UPDATEXMLOBJECT, 
                    bcContext, xdDestination);

            // Close the SOAP envelope and body.
            SoapUtils.endSoapRequest(xdDestination, false);
            
            // Send the request and receive response..
            OMElement oeResponse = srRequest.sendSoapRequest();
            
            // Read the response method.
            try {
                // For each response element figure process the correct content types. 
                OMElement oeTuple = oeResponse.getFirstElement();
                
                if (oeTuple != null) {
                    IContent cContent;
                    
                    cContent = unmarshallTupleElement(oeTuple, ctType);
                    
                    return cContent;
                }
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
        
        return null;
    }
    
    private IContent unmarshallTupleElement(OMElement oeTuple, EContentType ctType) throws BFException {
        IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(ctType, null, getMethodVersion());
        IXmlSource sUnmarshallSource = IXmlSource.Factory.newInstance(oeTuple);
        
        if (cuUnmarshaller == null) {
            throw new BFException("No unmarshaller configured for CoBOC content type " + ctType);
        }
        
        return cuUnmarshaller.unmarshalObject(sUnmarshallSource);        
    }    
    
    private IContent handleDeletion(ContentTuple ctTuple) throws BFException {
        IContent cDeleteObject = ctTuple.getWriteObject();
        LogInterface liLogger = bcContext.getLogger();
        EContentType ctType = cDeleteObject.getType();
        
        if (liLogger.isInfoEnabled()) {
            liLogger.info("Deleting " + cDeleteObject.getLogName() + " from " + EContentSourceType.BCP.getLogName());
        }
        
        QName qnMethodName = IBcpMethodNames.COBOC_METHOD_UPDATEXMLOBJECT;        
        ISoapRequest srRequest = csSource.createSoapRequest();
        IXmlDestination xdDest;
        XMLStreamWriter xswWriter = null;
        
        try {
            OutputStream osSoapRequest;
            
            // Open the SOAP request and write SOAP envelope and body.
            osSoapRequest = srRequest.openSoapRequest();
            xdDest = SoapUtils.startSoapRequestBody(osSoapRequest, qnMethodName);
            xswWriter = xdDest.getStreamWriter();
            xswWriter.setDefaultNamespace(qnMethodName.getNamespaceURI());

            xswWriter.writeEmptyElement(qnMethodName.getNamespaceURI(), "tuple");
            xswWriter.writeAttribute("lastModified", BeanUtils.getProperty(cDeleteObject, "lastModified"));
            xswWriter.writeAttribute("key", BeanUtils.getProperty(cDeleteObject, "key"));
            xswWriter.writeAttribute("name", BeanUtils.getProperty(cDeleteObject, "name"));
            xswWriter.writeAttribute("version", "organization");
            
            if (ctType != EContentType.COBOC_FOLDERS_FOLDER && ctType != EContentType.COBOC_FOLDERS_TEMPLATE) {
                xswWriter.writeAttribute("template", BeanUtils.getProperty(cDeleteObject, "templateKey"));
                xswWriter.writeAttribute("entity_id", BeanUtils.getProperty(cDeleteObject, "objectId"));
            }

            if (ctType == EContentType.COBOC_FOLDERS_FOLDER) {
                xswWriter.writeAttribute("isFolder", "true");
            }
            
            // Finish the request document.
            SoapUtils.endSoapRequest(xdDest, true);
            
            // Send the request and ignore the response.
            srRequest.sendSoapRequest();
        }
        catch (SoapFaultException e) {
            String sFaultString = e.getFaultString();
            if (sFaultString != null) {
                sFaultString = sFaultString.toLowerCase();
                
                if (sFaultString.contains("does not exist") ||
                    sFaultString.contains("no data found")) {
                    // The object was not found.
                    return null;
                }
            }
            
            // Re-throw the exception.
            throw new BFException(e);
        }
        catch (Exception e) {
            throw new BFException("SOAP request failed.", e);
        }
        finally {
            srRequest.closeSoapRequest();
        }     
        
        return null;
    }
}
