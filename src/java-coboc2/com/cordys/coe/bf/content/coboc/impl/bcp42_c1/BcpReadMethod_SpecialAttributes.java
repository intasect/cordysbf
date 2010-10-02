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
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentReadMethod;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BcpReadMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.coboc.folders.SpecialAttribute;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;
import com.cordys.coe.bf.utils.AxiomUtils;

/**
 * Implements a read method for CoBOC special attributes for templates. 
 *
 * @author mpoyhone
 */
public class BcpReadMethod_SpecialAttributes extends BcpReadMethodBase
{
    /**
     * Pattern: no\s+record(s)?\s+found
     */
    private static final Pattern pObjectNotFoundPattern =
        Pattern.compile("no\\s+record(s)?\\s+found", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    
    public BcpReadMethod_SpecialAttributes(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceBcp) csSource);
    }
    
    /**
     * Not implemented. Use the single content read method instread.
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(EContentType, boolean)
     */
    public List<IContent> readObjects(EContentType ctType, boolean bReadFully) throws BFException
    {
        throw new BFException("readObjects: Not implemented.");
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle, boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive) throws BFException
    {
        EContentType ctType = chParentHandle.getContentType();
        
        if (ctType == null) {
            throw new BFException("Content type parameter is null.");
        }
        
        if (ctType != EContentType.COBOC_FOLDERS_TEMPLATE) {
            return Collections.emptyList();
        }
        
        String sTemplateId = ((CobocContentHandle) chParentHandle).getObjectId();
        
        if (sTemplateId == null) {
            throw new BFException("Special attribute template object ID is not set:" + chParentHandle.getLogName());
        }
        
        ISoapRequest srRequest;
        ISoapRequestTemplate srtSoapTemplate;
        BindingParameters bpRequestParameters = new BindingParameters();
        List<IContent> lResultList;
        
        srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_GETTEMPLATESPECIAL_ATTRIBUTES, this);
        bpRequestParameters.setParameter("template_id", sTemplateId);
        srRequest = csSource.createSoapRequest();
        
        try {
            lResultList = sendSoapRequest(srRequest, srtSoapTemplate.getBindingTemplate(), bpRequestParameters, null);
        }
        catch (BFException e) {
            // Try to check the SOAP:Fault
            Throwable tCause = e.getCause();
            
            while (tCause != null) {
                String sMessage = tCause.getMessage();
                
                if (sMessage != null) {
                    if (pObjectNotFoundPattern.matcher(sMessage).find()) {
                        // The object was not found.
                        return null;
                    }                    
                }
                
                tCause = tCause.getCause();
            }
            
            // Re-throw the exception.
            throw new BFException(e);
        }
        
        return lResultList;
    }  
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObject(com.cordys.coe.bf.content.base.IContentSource, com.cordys.coe.bf.content.base.IContentUnmarshaller, com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent readObject(IContentHandle hHandle) throws BFException
    {
        EContentType ctType = hHandle.getContentType();
        
        if (ctType == null) {
            throw new BFException("Content type parameter is null.");
        }
        
        if (ctType != EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE) {
            throw new BFException("Invalid content type " + ctType + ". Expected " + EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE);
        }
        
        // Figure out the template key from the special attribute key.
        CobocContentHandle cchAttribHandle = (CobocContentHandle) hHandle; 
        String sAttribKey = cchAttribHandle.getKey();
        
        if (sAttribKey == null) {
            throw new BFException("Special attribute key is not set.");
        }
        
        int iPos = sAttribKey.lastIndexOf('/');
        
        if (iPos == -1 || iPos == 0) {
            throw new BFException("Invalid special attribute key.");
        }
        
        String sTemplateKey = sAttribKey.substring(0, iPos);
        String sAttribName = sAttribKey.substring(iPos + 1);
        CobocContentHandle cchTemplateHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_TEMPLATE);
        
        cchTemplateHandle.setKey(sTemplateKey);
        
        IContentReadMethod crmTemplateReadMethod = csSource.getReadMethod(cchTemplateHandle.getContentType());
        
        if (crmTemplateReadMethod == null) {
            throw new BFException("No read method configured for " + cchTemplateHandle.getContentType().getLogName());
        }
        
        crmTemplateReadMethod.setContentFilterStatus(false);
        
        // Read the template and get the handle from it.
        IContent cTemplate = crmTemplateReadMethod.readObject(cchTemplateHandle);
        
        if (cTemplate == null) {
            return null;
        }
        
        cchTemplateHandle.copyFrom(cTemplate.getHandle());
        
        // Read all template special attribute and return the one that matches the given name.
        List<IContent> lAttribList = readObjects(cchTemplateHandle, true, false);
        
        if (lAttribList != null) {
            for (IContent cAttrib : lAttribList)
            {
                SpecialAttribute saAttrib = (SpecialAttribute) cAttrib;
                
                if (sAttribName.equals(saAttrib.getName())) {
                    return cAttrib;
                }
            }
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    protected List<IContent> handleSoapResponse(OMElement oeResponse, EContentType objectType) throws BFException {
        OMElement oeTuple = oeResponse.getFirstElement();
        IXmlSource sUnmarshallSource = IXmlSource.Factory.newInstance();
        List<IContent> lRes = new ArrayList<IContent>(20);
        EContentType ctType = EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE;
        
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
                
                IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(ctType, null, getMethodVersion());
                    
                if (cuUnmarshaller == null) {
                    throw new BFException("Unable to get content unmarshaller for content type " + ctType);
                }
                
                sUnmarshallSource.set(oeContentElement);
                 
                IContent cCreatedContent = cuUnmarshaller.unmarshalObject(sUnmarshallSource);
                
                if (cCreatedContent != null) {
                    lRes.add(cCreatedContent);
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
