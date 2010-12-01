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
package com.cordys.coe.bf.content.ldap.impl.bcp42_c1;

import java.util.LinkedList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.xpath.AXIOMXPath;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BcpReadMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.ldap.LdapContentHandle;
import com.cordys.coe.bf.content.types.ldap.Method;
import com.cordys.coe.bf.content.types.ldap.MethodSet;
import com.cordys.coe.bf.content.types.ldap.MethodXsd;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.databind.IBindingTemplate;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.utils.AxiomUtils;

/**
 * TODO Describe the class.
 *
 * @author mpoyhone
 */
public class BcpReadMethod_LDAP extends BcpReadMethodBase
{
    private List<IContent> lTempContentList;
    
    public BcpReadMethod_LDAP(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceBcp) csSource);
    }
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(EContentType, boolean)
     */
    public List<IContent> readObjects(EContentType ctType, boolean bReadFully) throws BFException
    {
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        BindingParameters bpRequestParameters = new BindingParameters();
        IBindingTemplate btRequestTemplate;
        
        switch (ctType) {
        case METHOD_SET :
            btRequestTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.LDAP_TEMPLATEID_GETMETHODSETS, this).getBindingTemplate();
            bpRequestParameters.setParameter("ORG_DN", "o=testing,cn=cordys,o=vanenburg.com");
            break;
            
        default:
            throw new BFException("Invalid LDAP root content type: " + ctType);
        }
        
        if (btRequestTemplate == null) {
            throw new BFException("Request template not found for content type " + ctType);
        }
        
        ISoapRequest srRequest = csSource.createSoapRequest();
        
        lTempContentList = new LinkedList<IContent>();
        sendSoapRequest(srRequest, btRequestTemplate, bpRequestParameters, null);
        
        List<IContent> lRes = lTempContentList;
        
        lTempContentList = null;
        
        return lRes;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle, boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive) throws BFException
    {
        if (chParentHandle == null) {
            throw new IllegalArgumentException("Parent handle parameter is null.");
        }
        
        if (! (chParentHandle instanceof LdapContentHandle)) {
            throw new IllegalArgumentException("Invalid content handle for LDAP opeation.");
        }
        
        BindingParameters bpRequestParameters = new BindingParameters();
        IBindingTemplate btRequestTemplate;
        String sParentDn = chParentHandle.getContentId();
        
        if (sParentDn == null) {
            throw new IllegalArgumentException("Parent DN is not set.");
        }
        
        switch (chParentHandle.getContentType()) {
        case METHOD_SET :
            btRequestTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.LDAP_TEMPLATEID_GETCHILDREN, this).getBindingTemplate();
            bpRequestParameters.setParameter("PARENT_DN", sParentDn);
            break;
            
        default:
            throw new BFException("Invalid LDAP root content type: " + chParentHandle.getContentType());
        }
        
        ISoapRequest srRequest = csSource.createSoapRequest();
        
        lTempContentList = new LinkedList<IContent>();
        sendSoapRequest(srRequest, btRequestTemplate, bpRequestParameters, null);
        
        List<IContent> lRes = lTempContentList;
        
        lTempContentList = null;
        
        return lRes;        
    }  
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObject(com.cordys.coe.bf.content.base.IContentSource, com.cordys.coe.bf.content.base.IContentUnmarshaller, com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent readObject(IContentHandle hHandle) throws BFException
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @SuppressWarnings("unchecked")
    protected List<IContent> handleSoapResponse(OMElement oeResponse, EContentType objectType) throws BFException {
        OMElement oeChild = oeResponse.getFirstElement();
        
        try
        {
            AXIOMXPath axEntryPath = new AXIOMXPath("./ns:old/ns:entry");
            AXIOMXPath axObjectclassPath = new AXIOMXPath("./ns:objectclass/ns:string/text()");
            IXmlSource sUnmarshallSource = IXmlSource.Factory.newInstance();
            
            axEntryPath.addNamespace("ns", oeResponse.getQName().getNamespaceURI());
            axObjectclassPath.addNamespace("ns", oeResponse.getQName().getNamespaceURI());
            
            for (; oeChild != null; oeChild = AxiomUtils.getNextSiblingElement(oeChild)) {
                OMElement oeEntry = (OMElement) axEntryPath.selectSingleNode(oeChild);
                
                if (oeEntry == null) {
                    continue;
                }
                    
                List lObjectclassList = (List) axObjectclassPath.selectNodes(oeEntry);

                if (lObjectclassList == null || lObjectclassList.size() < 2) {
                    continue;
                }
                
                OMText[] otaObjectclasses = (OMText[]) lObjectclassList.toArray(new OMText[lObjectclassList
                        .size()]);
                IContent cCreatedContent = null;

                if ("busmethodset".equals(otaObjectclasses[1].getText())) {
                    sUnmarshallSource.set(oeEntry);
                    cCreatedContent = handleMethodsetEntryResponse(sUnmarshallSource);
                } else if ("busmethod".equals(otaObjectclasses[1].getText())) {
                    if (otaObjectclasses.length == 2) {
                        sUnmarshallSource.set(oeEntry);
                        cCreatedContent = handleMethodEntryResponse(sUnmarshallSource);
                    } else if ("busmethodtype".equals(otaObjectclasses[2].getText())) {
                        sUnmarshallSource.set(oeEntry);
                        cCreatedContent = handleMethodXsdEntryResponse(sUnmarshallSource);
                    }
                }
                
                if (cCreatedContent != null) {
                    lTempContentList.add(cCreatedContent);
                }
            }
            
            return lTempContentList;
        }
        catch (Exception e)
        {
            throw new BFException("SOAP response parsing failed.", e);
        }
    }
    
    protected MethodSet handleMethodsetEntryResponse(IXmlSource sSrc) throws BFException {
        IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(EContentType.METHOD_SET, null, getMethodVersion());
        
        return (MethodSet) cuUnmarshaller.unmarshalObject(sSrc);
    } 

    protected Method handleMethodEntryResponse(IXmlSource sSrc) throws BFException {
        IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(EContentType.METHOD, null, getMethodVersion());
        
        return (Method) cuUnmarshaller.unmarshalObject(sSrc);
    }     
    
    protected MethodXsd handleMethodXsdEntryResponse(IXmlSource sSrc) throws BFException {
        IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(EContentType.METHOD_XSD, null, getMethodVersion());
        
        return (MethodXsd) cuUnmarshaller.unmarshalObject(sSrc);
    }
}
