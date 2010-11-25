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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentReadMethod;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentStore;
import com.cordys.coe.bf.content.base.impl.BcpReadMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.coboc.impl.bcp42_c1.CobocContentStore;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.coboc.messagemodels.EmailModel;
import com.cordys.coe.bf.content.types.coboc.messagemodels.InboxModel;
import com.cordys.coe.bf.content.types.coboc.messagemodels.InboxModel_C3;
import com.cordys.coe.bf.content.types.coboc.messagemodels.MessageTemplate;
import com.cordys.coe.bf.content.types.xmlstore.XmlStoreContentHandle;
import com.cordys.coe.bf.content.xmlstore.impl.bcp42_c1.BcpReadMethod_XmlStore;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;

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
    private static IContentStore csMessageTemplateCache = null;
    private static IContentStore csInboxModelCache = null;
    private static IContentStore csEmailModelCache = null;
    
    public BcpReadMethod_MessageModels(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceBcp) csSource);
    }
    
    public static void clearCaches() {
        if (csMessageTemplateCache != null) {
            csMessageTemplateCache.clear();
            csMessageTemplateCache = null;
        }
        
        if (csInboxModelCache != null) {
            csInboxModelCache.clear();
            csInboxModelCache = null;
        }
        
        if (csEmailModelCache != null) {
            csEmailModelCache.clear();
            csEmailModelCache = null;
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
        
        if (csMessageTemplateCache == null) {
            readAllIntoCaches();
        }
        
        List<IContent> cContentList;
        
        switch (ctType) {
        case COBOC_MESSAGE_TEMPLATE :
            cContentList = new ArrayList<IContent>(csMessageTemplateCache.getObjects());
            break;
            
        case COBOC_INBOX_MODEL_C3 :
            cContentList = new ArrayList<IContent>(csInboxModelCache.getObjects());
            break;
            
        case COBOC_EMAIL_MODEL :
            cContentList = new ArrayList<IContent>(csEmailModelCache.getObjects());
            break;
            
        default:
            throw new BFException("Invalid CoBOC message model content type: " + ctType.getLogName());
        }
        
        return cContentList;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle, boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive) throws BFException
    {
        if (chParentHandle.getContentType() != EContentType.COBOC_MESSAGE_TEMPLATE) {
            // Inbox and email models don't have children.
            return Collections.emptyList();
        }
        
        String parentId = ((CobocContentHandle) chParentHandle).getObjectId();
        
        if (parentId == null || parentId.length() == 0)
        {
            throw new BFException("Message template object ID not set.");
        }
        
        List<IContent> resList = new ArrayList<IContent>();
        
        for (IContent model : csInboxModelCache.getObjects())
        {
            if (model.getParent().equals(chParentHandle)) {
                resList.add(model);
            }
        }
        
        for (IContent model : csEmailModelCache.getObjects())
        {
            if (model.getParent().equals(chParentHandle)) {
                resList.add(model);
            }
        }
        
        return resList;
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
        
        if (csMessageTemplateCache == null) {
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
        case COBOC_MESSAGE_TEMPLATE :
            cResult = csMessageTemplateCache.findObject(hHandle);
            break;

        case COBOC_INBOX_MODEL_C3 :
            cResult = csInboxModelCache.findObject(hHandle);
            break;
            
        case COBOC_EMAIL_MODEL :
            cResult = csEmailModelCache.findObject(hHandle);
            break;
            
        default:
            throw new BFException("Invalid CoBOC message model content type: " + ctType.getLogName());
        }
        
        return cResult;
    }
    
    protected MessageTemplate readTemplateDetails(MessageTemplate mmOrig) throws BFException {
        if (! bcContext.getConfig().getCobocConfig().isContentEnabled(EContentType.COBOC_MESSAGE_TEMPLATE)) {
            // This type has been disabled in the configuration. 
            return null;
        }
        
        ISoapRequest srRequest;
        ISoapRequestTemplate srtSoapTemplate;
        List<IContent> lResultList;

        // Read message model details.
        BindingParameters bpParams = new BindingParameters();
        
        bpParams.setParameter("messagetemplateid", mmOrig.getObjectId());
        
        srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_GETMESSAGETEMPLATEDETAILS, this);
        srRequest = csSource.createSoapRequest();
        lResultList = sendSoapRequest(srRequest, srtSoapTemplate.getBindingTemplate(), bpParams, EContentType.COBOC_MESSAGE_TEMPLATE);

        return (MessageTemplate) (lResultList != null && lResultList.size() > 0 ? lResultList.get(0) : EContentType.COBOC_MESSAGE_TEMPLATE);
    }
    
    protected InboxModel readInboxModelDetails(IContentHandle hHandle) throws BFException {
        if (! bcContext.getConfig().getCobocConfig().isContentEnabled(EContentType.COBOC_INBOX_MODEL_C3)) {
            // This type has been disabled in the configuration. 
            return null;
        }
        
        ISoapRequest srRequest;
        ISoapRequestTemplate srtSoapTemplate;
        List<IContent> lResultList;

        // Read message model details.
        BindingParameters bpParams = new BindingParameters();
        
        bpParams.setParameter("MODEL_ID", ((CobocContentHandle) hHandle).getObjectId());
        
        srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_GETMESSAGEMODELDETAILS, this);
        srRequest = csSource.createSoapRequest();
        lResultList = sendSoapRequest(srRequest, srtSoapTemplate.getBindingTemplate(), bpParams, EContentType.COBOC_INBOX_MODEL_C3);

        return (InboxModel) (lResultList != null && lResultList.size() > 0 ? lResultList.get(0) : null);
    }
    
    protected EmailModel readEmailModelDetails(EmailModel orig) throws BFException {
        if (! bcContext.getConfig().getCobocConfig().isContentEnabled(EContentType.COBOC_EMAIL_MODEL)) {
            // This type has been disabled in the configuration. 
            return null;
        }
        
        String parentId = orig.getParentId();
        
        if (parentId == null || parentId.length() == 0)
        {
            throw new BFException("Parent ID not found for: " + orig.getLogName());
        }
        
        String xmlstoreKey = orig.getDetailKey();
        XmlStoreContentHandle detailHandle = new XmlStoreContentHandle(EContentType.COBOC_EMAIL_MODEL);
        IContentReadMethod readMethod = new BcpReadMethod_XmlStore(bcContext, csSource);
        
        detailHandle.setKey(xmlstoreKey);
        
        EmailModel detail = (EmailModel) readMethod.readObject(detailHandle);
        
        if (detail == null) {
            return null;
        }
        
        // Set the handlers from the original object as they still point to XML store.
        detail.getHandle().copyFrom(orig.getHandle());
        detail.getParent().copyFrom(orig.getParent());
        
        return detail;
    }
    
    protected void readAllIntoCaches() throws BFException {
        csMessageTemplateCache = new CobocContentStore();
        csInboxModelCache = new CobocContentStore();
        csEmailModelCache = new CobocContentStore();
        
        if (! bcContext.getConfig().getCobocConfig().isContentEnabled(EContentType.COBOC_MESSAGE_TEMPLATE)) {
            // This type has been disabled in the configuration. 
            return;
        }
        
        ISoapRequest srRequest;
        ISoapRequestTemplate srtSoapTemplate;
        List<IContent> lResultList;

        // Read message templates.
        srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_GETMESSAGETEMPLATES, this);
        srRequest = csSource.createSoapRequest();
        
        try {
            lResultList = sendSoapRequest(srRequest, srtSoapTemplate.getBindingTemplate(), null, EContentType.COBOC_MESSAGE_TEMPLATE);
        }
        catch (Exception e) {
            handleSoapFault(e);
            return;
        }
        
        for (Iterator<IContent> iter = lResultList.iterator(); iter.hasNext(); )
        {
            IContent cTmp = iter.next();
            CobocContentHandle handle = (CobocContentHandle) cTmp.getHandle();
            
            if (bUseContentFiltering && 
                ! csSource.checkForAccess(handle.getContentType(), handle.getKey(), false)) {
                // This item is filtered out.
                iter.remove();
                continue;
            }
            
            IContent cDetail = readTemplateDetails((MessageTemplate) cTmp);
            
            csMessageTemplateCache.insertObject(cDetail != null ? cDetail : cTmp);
        }    
        
        for (IContent messageTemplate : lResultList)
        {
            String parentId = ((MessageTemplate) messageTemplate).getObjectId();
            // Read inbox models.
            List<IContent> lContentList;
            BindingParameters bpParams;
            
            bpParams = new BindingParameters();
            bpParams.setParameter("messagetemplateid", parentId);
            srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_GETINBOXMODELS, this);
            srRequest = csSource.createSoapRequest();
            
            try {
                lContentList = sendSoapRequest(srRequest, srtSoapTemplate.getBindingTemplate(), bpParams, EContentType.COBOC_INBOX_MODEL_C3);
            }
            catch (Exception e) {
                handleSoapFault(e);
                return;
            }                
            
            for (IContent cTmp : lContentList)
            {
                InboxModel_C3 cDetail = (InboxModel_C3) readInboxModelDetails(cTmp.getHandle());
                    
                cDetail = (cDetail != null ? cDetail : (InboxModel_C3) cTmp);

                cDetail.setParent(messageTemplate.getHandle());
                cDetail.createKey();
                
                CobocContentHandle handle = cDetail.getHandle();
                
                if (! bUseContentFiltering || 
                    csSource.checkForAccess(handle.getContentType(), handle.getKey(), false)) {
                    csInboxModelCache.insertObject(cDetail);
                }
            }           
            
            // Read email models.
            bpParams = new BindingParameters();
            bpParams.setParameter("messagetemplateid", parentId);
            srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_GETEMAILMODELS, this);
            srRequest = csSource.createSoapRequest();
            
            try {
                lContentList = sendSoapRequest(srRequest, srtSoapTemplate.getBindingTemplate(), bpParams, EContentType.COBOC_EMAIL_MODEL);
            }
            catch (Exception e) {
                handleSoapFault(e);
                return;
            }                
            
            for (IContent cTmp : lContentList)
            {
                EmailModel orig = (EmailModel) cTmp;
                EmailModel cDetail = readEmailModelDetails(orig);
                
                cDetail = (cDetail != null ? cDetail : orig);
                cDetail.setLastModified(orig.getLastModified());
                cDetail.setParent(messageTemplate.getHandle());
                cDetail.createKey();
                
                CobocContentHandle handle = cDetail.getHandle();
                
                if (! bUseContentFiltering || 
                    csSource.checkForAccess(handle.getContentType(), handle.getKey(), false)) {
                    csEmailModelCache.insertObject(cDetail);
                }
            }
        }
    }
    
    private void handleSoapFault(Exception e) throws BFException
    {
        // Try to check the SOAP:Fault
        Throwable notRunning = findSoapProcessorNotRunningException(e);
        
        if (notRunning != null) {
            String sMessage = notRunning.getMessage();
            
            if (sMessage != null) {
                bcContext.getLogger().error("Notification SOAP processor is not available. Message models are not read.");
                bcContext.getLogger().debug("Got exception: " + sMessage);
                return;
            }
        }          
        
        throw new BFException(e);
    }
    
    protected List<IContent> handleSoapResponse(OMElement oeResponse, EContentType objectType) throws BFException {
        OMElement oeRoot = oeResponse;
        
        if (oeRoot != null && objectType == EContentType.COBOC_INBOX_MODEL_C3) {
            oeRoot = oeRoot.getFirstElement();
        }
        
        if (oeRoot == null) {
            return Collections.emptyList();
        }
        
        return super.handleSoapResponse(oeRoot, objectType);
    }
}
