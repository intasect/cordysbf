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
package com.cordys.coe.bf.content.xmlstore.impl.bcp42_c1;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BcpReadMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.xmlstore.XmlStoreContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.exception.SoapFaultException;
import com.cordys.coe.bf.soap.ISoapConstants;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.SoapUtils;
import com.cordys.coe.bf.utils.AxiomUtils;

/**
 * Implements a read method for XMLStore content.
 *
 * @author mpoyhone
 */
public class BcpReadMethod_XmlStore extends BcpReadMethodBase
{
    public BcpReadMethod_XmlStore(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceBcp) csSource);
    }
    
    /**
     * Note: This method is not not implemented.
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(EContentType, boolean)
     */
    public List<IContent> readObjects(EContentType ctType, boolean bReadFully) throws BFException
    {
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle, boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive) throws BFException
    {
        if (chParentHandle == null) {
            throw new IllegalArgumentException("Parent handle parameter is null.");
        }
        
        if (chParentHandle.getContentType() != EContentType.XMLSTORE_FOLDER) {
            return new LinkedList<IContent>();
        }
        
        if (! (chParentHandle instanceof XmlStoreContentHandle)) {
            throw new IllegalArgumentException("Invalid content handle for XMLStore operation.");
        }
        
        XmlStoreContentHandle xschHandle = (XmlStoreContentHandle) chParentHandle;
        
        List<IContent> lResList = new LinkedList<IContent>();
        
        readFolderContents(xschHandle, bReadFully, bRecursive, lResList);
        
        return lResList;
    }  
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent readObject(IContentHandle chHandle) throws BFException
    {
        if (! (chHandle instanceof XmlStoreContentHandle)) {
            throw new IllegalArgumentException("Invalid content handle for XMLStore opeation.");
        }
        
        XmlStoreContentHandle xchObjectHandle = (XmlStoreContentHandle) chHandle;
        QName qnMethodName;
        String sObjectKey = xchObjectHandle.getKey();
        EContentType ctType = xchObjectHandle.getContentType();
        
        if (sObjectKey == null) {
            throw new IllegalArgumentException(chHandle.getLogName() + ": XMLStore key is not set.");
        }
        
        if (bUseContentFiltering && 
            ! csSource.checkForAccess(xchObjectHandle.getContentType(), sObjectKey, true)) {
            // This item is filtered out.
            return null;
        }
        
        if (ctType == EContentType.XMLSTORE_FOLDER) {
            // We cannot fetch the folder information.
            return null;
        }

        qnMethodName = IBcpMethodNames.XMLSTORE_METHOD_GETXMLOBJECT;        
        
        ISoapRequest srRequest = csSource.createSoapRequest();
        IXmlDestination xdDest;
        XMLStreamWriter xswWriter = null;
        String sVersion = sContentVersion != null ? sContentVersion : "organization";
        
        try {
            OutputStream osSoapRequest;
            
            // Open the SOAP request and write SOAP envelope and body.
            osSoapRequest = srRequest.openSoapRequest();
            xdDest = SoapUtils.startSoapRequestBody(osSoapRequest, qnMethodName);
            xswWriter = xdDest.getStreamWriter();
            xswWriter.setDefaultNamespace(qnMethodName.getNamespaceURI());

            xswWriter.writeStartElement(qnMethodName.getNamespaceURI(), "key");
            xswWriter.writeAttribute("version", sVersion);
            xswWriter.writeCharacters(sObjectKey);
            xswWriter.writeEndElement(); // key
            
            // Finish the request document.
            SoapUtils.endSoapRequest(xdDest, true);
            
            // Send the request.
            OMElement oeResponse = srRequest.sendSoapRequest();
            
            // Read the response object. 
            OMElement oeTuple = oeResponse.getFirstElement();
            
            if (oeTuple != null) {
                IContent cContent;
                
                cContent = unmarshallTupleElement(oeTuple, ctType);
                
                return cContent;
            }
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
    
    private void readFolderContents(IContentHandle chHandle, boolean bReadFully, boolean bRecursive, List<IContent> lResList) throws BFException {
        if (! (chHandle instanceof XmlStoreContentHandle)) {
            throw new IllegalArgumentException("Invalid content handle for XMLStore opeation.");
        }
        
        XmlStoreContentHandle xschFolderHandle = (XmlStoreContentHandle) chHandle;
        QName qnMethodName;
        String sFolderKey = xschFolderHandle.getKey();
        
        if (sFolderKey == null) {
            throw new IllegalArgumentException("XMLStore key is not set.");
        }
        
        if (bUseContentFiltering &&
            ! csSource.checkForAccess(xschFolderHandle.getContentType(), sFolderKey, true)) {
            // This folder is filtered out.
            return;
        }
        
        qnMethodName = IBcpMethodNames.XMLSTORE_METHOD_GETCOLLECTION;
        
        List<IContent> lChildList = new LinkedList<IContent>(); 
        String sVersion = sContentVersion != null ? sContentVersion : "organization";
        
        handleContentListRequest(sFolderKey, sVersion, qnMethodName, lChildList);
        
        if (bReadFully) {
            // Re-read the objects with full contents.
            List<IContent> lTmpList = new LinkedList<IContent>();
            
            for (IContent cContent : lChildList)
            {
                IContent cFullVersion = readObject(cContent.getHandle());
                
                if (cFullVersion != null) {
                    lTmpList.add(cFullVersion);
                } else {
                    // For some reason the read failed, so add the simple version.
                    lTmpList.add(cContent);
                }
            }
            
            lChildList.clear();
            lChildList = lTmpList;
        }

        lResList.addAll(lChildList);
        
        if (bRecursive) {
            // Read all subfolders.
            for (IContent cChild : lChildList) {
                switch (cChild.getType()) {
                case XMLSTORE_FOLDER :
                    readFolderContents(cChild.getHandle(), bReadFully, bRecursive, lResList);
                    break;
                }
            }
        }
    }
    
    private void handleContentListRequest(String sFolderKey, String sVersion, QName qnMethodName, List<IContent> lResList) throws BFException {
        ISoapRequest srRequest = csSource.createSoapRequest();
        XMLStreamWriter xswWriter = null;
        
        try {
            OutputStream osSoapRequest;
            IXmlDestination xdDest;
            
            // Open the SOAP request and write SOAP envelope and body.
            osSoapRequest = srRequest.openSoapRequest();
            xdDest = SoapUtils.startSoapRequestBody(osSoapRequest, null);
            xswWriter = xdDest.getStreamWriter();
            xswWriter.setDefaultNamespace(qnMethodName.getNamespaceURI());
            
            xswWriter.writeStartElement(qnMethodName.getNamespaceURI(), qnMethodName.getLocalPart());
            xswWriter.writeNamespace("", qnMethodName.getNamespaceURI());
            
            xswWriter.writeStartElement(qnMethodName.getNamespaceURI(), "folder");
            xswWriter.writeAttribute("version", sVersion);
            xswWriter.writeCharacters(sFolderKey);
            xswWriter.writeEndElement(); // folder.
            
            xswWriter.writeEndElement(); // Method
            
            // Finish the request document.
            SoapUtils.endSoapRequest(xdDest, false);
            
            // Send the request.
            OMElement oeResponse = srRequest.sendSoapRequest();
            
            // Read the response methods.
            try {
                while (oeResponse != null) {
                    boolean bHandle = true;
                    
                    // Check that it is not a SOAP:Fault.
                    if ("Fault".equals(oeResponse.getLocalName())) {
                        OMNamespace onNs = oeResponse.getNamespace();
                        
                        if (ISoapConstants.SOAP_NAMESPACE.equals(onNs.getName())) {
                            bHandle = false;
                        }
                    }
                    
                    if (bHandle) {
                        // For each response element figure process the correct content types. 
                        for (Iterator<?> iTupleIter = oeResponse.getChildElements(); iTupleIter.hasNext(); ) {
                            OMElement oeTuple = (OMElement) iTupleIter.next();
                            IContent cContent;
                            
                            cContent = unmarshallTupleElement(oeTuple, null);
                            if (cContent != null) {
                                lResList.add(cContent);
                            }
                        }
                    }
                    
                    oeResponse = AxiomUtils.getNextSiblingElement(oeResponse);
                }
            }
            catch (Exception e) {
                throw new BFException("XMLStore content list SOAP request failed.", e);
            }
        }
        catch (Exception e) {
            throw new BFException("SOAP request failed.", e);
        }
        finally {
            srRequest.closeSoapRequest();
        }
    }  
    
    private IContent unmarshallTupleElement(OMElement oeTuple, EContentType ctRequestedType) throws BFException {
        String sIsFolderAttribute;
        EContentType ctType;
        String subType = null;
        
        // Check if the tuple is a folder, template or an instance.
        sIsFolderAttribute = oeTuple.getAttributeValue(new QName("isFolder"));
        
        if (sIsFolderAttribute != null && "true".equalsIgnoreCase(sIsFolderAttribute)) {
            ctType = EContentType.XMLSTORE_FOLDER;
        } else if (ctRequestedType != null) {
            ctType = ctRequestedType;
        } else {
            String sKey = oeTuple.getAttributeValue(new QName("key"));
            
            if (sKey == null || sKey.length() == 0) {
                throw new BFException("XMLStore object is missing 'key' attribute.");
            }

            // Figure out the type based on the key.
            if (sKey.startsWith("/Cordys/WCP/XForms/runtime/")) {
                ctType = EContentType.RUNTIME_XFORM;
            } else {
                // Unknown/unsupported type.
                return null;
            }
        }
        
        switch (ctType) {
        case COBOC_EMAIL_MODEL : subType = "BCP-Details"; break;
        }
        
        IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(ctType, subType, getMethodVersion());
        IXmlSource sUnmarshallSource = IXmlSource.Factory.newInstance(oeTuple);
        
        if (cuUnmarshaller == null) {
            throw new BFException(csSource.getType().getLogName() + ": No unmarshaller configured for XMLStore type " + ctType);
        }
        
        IContent cResult = cuUnmarshaller.unmarshalObject(sUnmarshallSource);
        
        if (! cResult.getHandle().isSet()) {
            throw new BFException(ctType.getLogName() + ": Content handle is not set.");
        }        
        
        return cResult;
    }
}
