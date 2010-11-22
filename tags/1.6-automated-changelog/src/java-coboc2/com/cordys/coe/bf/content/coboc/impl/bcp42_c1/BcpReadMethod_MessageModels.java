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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentStore;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BcpReadMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.coboc.messagemodels.InboxModel;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;
import com.cordys.coe.bf.utils.AxiomUtils;

/**
 * Implements a read method for CoBOC inbox message models. Because there doesn't seem to be
 * a method to read a message model based on the name this method
 * reads all models and puts them into a cache and subsequent calls
 * just return those objects. 
 *
 * @author mpoyhone
 */
public class BcpReadMethod_MessageModels extends BcpReadMethodBase
{
    private static IContentStore csMessageModelCache = null;
    
    public BcpReadMethod_MessageModels(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceBcp) csSource);
    }
    
    public static void clearCaches() {
        if (csMessageModelCache != null) {
            csMessageModelCache.clear();
            csMessageModelCache = null;
        }
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(EContentType, boolean)
     */
    public List<IContent> readObjects(EContentType ctType, boolean bReadFully) throws BFException
    {
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        if (csMessageModelCache == null) {
            readAllIntoCaches();
        }
        
        Collection<IContent> cContentList;
        
        switch (ctType) {
        case COBOC_INBOX_MODEL_C1 :
            cContentList = csMessageModelCache.getObjects();
            break;
            
        default:
            throw new BFException("CoBOC inbox message model method content type must be inbox message model. Got: " + ctType.getLogName());
        }
        
        List<IContent> res = new ArrayList<IContent>(cContentList);
        
        if (bUseContentFiltering) {
            for (ListIterator<IContent> iter = res.listIterator(); iter.hasNext();)
            {
                IContent obj = iter.next();
                String sObjectKey = ((CobocContentHandle) obj.getHandle()).getKey(); 
                
                if (! csSource.checkForAccess(obj.getType(), sObjectKey, false)) {
                    iter.remove();
                }
            }
        }
         
        return res;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle, boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive) throws BFException
    {
        // Message models do no have subobjects.
        return new ArrayList<IContent>();
    }  
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObject(com.cordys.coe.bf.content.base.IContentSource, com.cordys.coe.bf.content.base.IContentUnmarshaller, com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent readObject(IContentHandle hHandle) throws BFException
    {
        EContentType ctType = hHandle.getContentType();
        
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        if (csMessageModelCache == null) {
            readAllIntoCaches();
        }
        
        String sObjectKey = ((CobocContentHandle) hHandle).getKey();
        
        if (bUseContentFiltering && 
            ! csSource.checkForAccess(hHandle.getContentType(), sObjectKey, true)) {
            // This item is filtered out.
            return null;
        }
        
        IContent cResult;
        
        switch (ctType) {
        case COBOC_INBOX_MODEL_C1 :
            cResult = csMessageModelCache.findObject(hHandle);
            break;
            
        default:
            throw new BFException("CoBOC inbox message model method content type must be inbox message model. Got: " + ctType.getLogName());
        }
        
        return cResult;
    }
    
    protected InboxModel readModelDetails(InboxModel mmOrig) throws BFException {
        if (! bcContext.getConfig().getCobocConfig().isContentEnabled(EContentType.COBOC_INBOX_MODEL_C1)) {
            // This type has been disabled in the configuration. 
            return null;
        }
        
        ISoapRequest srRequest;
        ISoapRequestTemplate srtSoapTemplate;
        List<IContent> lResultList;

        // Read message model details.
        BindingParameters bpParams = new BindingParameters();
        
        bpParams.setParameter("MODEL_ID", mmOrig.getObjectId());
        
        srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_GETMESSAGEMODELDETAILS, this);
        srRequest = csSource.createSoapRequest();
        lResultList = sendSoapRequest(srRequest, srtSoapTemplate.getBindingTemplate(), bpParams, null);

        return (InboxModel) (lResultList != null && lResultList.size() > 0 ? lResultList.get(0) : null);
    }
    
    protected void readAllIntoCaches() throws BFException {
        csMessageModelCache = new CobocContentStore();
        
        if (! bcContext.getConfig().getCobocConfig().isContentEnabled(EContentType.COBOC_INBOX_MODEL_C1)) {
            // This type has been disabled in the configuration. 
            return;
        }
        
        ISoapRequest srRequest;
        ISoapRequestTemplate srtSoapTemplate;
        List<IContent> lResultList;

        // Read message models.
        srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_GETALLMESSAGEMODELS, this);
        srRequest = csSource.createSoapRequest();
        
        try {
            lResultList = sendSoapRequest(srRequest, srtSoapTemplate.getBindingTemplate(), null, null);
        }
        catch (Exception e) 
        {
            // Try to check the SOAP:Fault
            Throwable notRunning = findSoapProcessorNotRunningException(e);
            
            if (notRunning != null) {
                String sMessage = notRunning.getMessage();
                
                if (sMessage != null) {
                    bcContext.getLogger().error("Notification SOAP processor is not available. Inbox models are not read.");
                    bcContext.getLogger().debug("Got exception: " + sMessage);
                    return;
                }
            }          
            
            throw new BFException(e);
        }    
        
        for (IContent cTmp : lResultList)
        {
            IContent cDetail = readModelDetails((InboxModel) cTmp);
            
            csMessageModelCache.insertObject(cDetail != null ? cDetail : cTmp);
        }        
    }
    
    @SuppressWarnings("unchecked")
    protected List<IContent> handleSoapResponse(OMElement oeResponse, EContentType objectType) throws BFException {
        OMElement oeResult = oeResponse.getFirstElement();
        
        if (oeResult == null) {
            return Collections.emptyList();
        }
        
        QName qnTmp = oeResult.getQName();
        OMElement oeTuple= oeResult.getFirstChildWithName(AxiomUtils.createQName("tuple", qnTmp.getPrefix(), qnTmp.getNamespaceURI()));
        
        if (oeTuple == null) {
            throw new BFException("'tuple' element missing from the GetAllMessageModels.");
        }
        
        IXmlSource xsUnmarshallSource = IXmlSource.Factory.newInstance();
        List<IContent> lRes = new ArrayList<IContent>(20);
        IContentUnmarshaller cuTupleUnmarshaller = csSource.getContentUnmarshaller(EContentType.COBOC_INBOX_MODEL_C1, null, getMethodVersion());
        
        if (cuTupleUnmarshaller == null) {
            throw new BFException("No content unmarshaller configured for inbox message models.");
        }
        
        try
        {
            for (; oeTuple != null; oeTuple = AxiomUtils.getNextSiblingElement(oeTuple)) {
                BindingParameters bpParams = new BindingParameters();
                IContent cResObject;
                
                xsUnmarshallSource.set(oeTuple);
                cResObject = cuTupleUnmarshaller.unmarshalObject(xsUnmarshallSource, bpParams);
                
                if (cResObject != null) {
                    lRes.add(cResObject);
                }
            }
        }
        catch (Exception e)
        {
            throw new BFException("SOAP response parsing failed.", e);
        }
        
        return lRes;
    }  
}
