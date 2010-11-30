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
import java.util.List;
import java.util.ListIterator;

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
import com.cordys.coe.bf.content.types.coboc.rules.Rule;
import com.cordys.coe.bf.content.types.coboc.rules.RuleGroup;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.tools.ant.cm.EBcpVersion;

/**
 * Implements a read method for CoBOC rule content. Because there doesn't seem to be
 * a method to read one rule or rule group at a time the first call to this method
 * reads all rules and rule groups and puts them into a cache and subsequent calls
 * just return those objects. 
 *
 * @author mpoyhone
 */
public class BcpReadMethod_CobocRules extends BcpReadMethodBase
{
    private EContentType ctTempContentType;
    private static IContentStore csRuleCache = null;
    private static IContentStore csRuleGroupCache = null;
    
    public BcpReadMethod_CobocRules(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceBcp) csSource);
    }
    
    public static void clearCaches() {
        if (csRuleCache != null) {
            csRuleCache.clear();
            csRuleCache = null;
        }
        
        if (csRuleGroupCache != null) {
            csRuleGroupCache.clear();
            csRuleGroupCache = null;
        }        
    }
    
    /**
     * Reads all CoBOC rules or rule groups.
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(EContentType, boolean)
     */
    public List<IContent> readObjects(EContentType ctType, boolean bReadFully) throws BFException
    {
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        if (csRuleCache == null || csRuleGroupCache == null) {
            readAllIntoCaches();
        }
        
        Collection<IContent> cContentList;
        
        switch (ctType) {
        case COBOC_RULES_RULEGROUP :
            cContentList = csRuleGroupCache.getObjects();
            break;
            
        case COBOC_RULES_RULE :
            cContentList = csRuleCache.getObjects();
            break;
            
        default:
            throw new BFException("CoBOC rule method content type must be rule or rule group. Got: " + ctType.getLogName());
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
        EContentType ctType = chParentHandle.getContentType();
        
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        if (ctType != EContentType.COBOC_RULES_RULEGROUP) {
            return new ArrayList<IContent>();
        }
        
        if (csRuleCache == null || csRuleGroupCache == null) {
            readAllIntoCaches();
        }
        
        String sParentKey = ((CobocContentHandle) chParentHandle).getKey();
        
        if (bUseContentFiltering && 
            ! csSource.checkForAccess(chParentHandle.getContentType(), sParentKey, true)) {
            // This item is filtered out.
            return null;
        }
        
        IContent cRuleGroup = csRuleGroupCache.findObject(chParentHandle);
        
        if (cRuleGroup == null) {
            throw new BFException("Rule group not found with handle " + chParentHandle.getLogName());
        }
        
        Collection<IContentHandle> cChildHandleList = cRuleGroup.getChildren();
        List<IContent> lResList = new ArrayList<IContent>(cChildHandleList.size());
        
        for (IContentHandle cChildHandle : cChildHandleList)
        {
            IContent cRule = csRuleCache.findObject(cChildHandle);
            
            if (cRuleGroup == null) {
                throw new BFException("Rule not found with handle " + cChildHandle.getLogName());
            }
            
            lResList.add(cRule);
        }
         
        return lResList;
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
        
        if (csRuleCache == null || csRuleGroupCache == null) {
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
        case COBOC_RULES_RULEGROUP :
            cResult = csRuleGroupCache.findObject(hHandle);
            break;
            
        case COBOC_RULES_RULE :
            cResult = csRuleCache.findObject(hHandle);
            break;
            
        default:
            throw new BFException("CoBOC rule method content type must be rule or rule group. Got: " + ctType.getLogName());
        }
        
        return cResult;
    }
    
    protected void readAllIntoCaches() throws BFException {
        csRuleCache = new CobocContentStore();
        csRuleGroupCache = new CobocContentStore();
        
        ISoapRequest srRequest;
        ISoapRequestTemplate srtSoapTemplate;
        List<IContent> lResultList;
        BindingParameters bpParams = null;
        
        if (bcContext.getConfig().getVersionInfo().isLaterThan(EBcpVersion.BCP42_C3)) {
            bpParams = new BindingParameters();
            bpParams.setParameter("ORG_DN", bcContext.getConfig().getOrganizationDn());
        }
        
        // Read the rule groups.
        ctTempContentType = EContentType.COBOC_RULES_RULEGROUP;
        
        if (bcContext.getConfig().getCobocConfig().isContentEnabled(ctTempContentType)) {
            srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_GETRULEGROUPSBYOWNER, this);
            srRequest = csSource.createSoapRequest();
            
            try {
                lResultList = sendSoapRequest(srRequest, srtSoapTemplate.getBindingTemplate(), bpParams, null);
            }
            catch (Exception e) 
            {
                // Try to check the SOAP:Fault
                Throwable notRunning = findSoapProcessorNotRunningException(e);
                
                if (notRunning != null) {
                    String sMessage = notRunning.getMessage();
                    
                    if (sMessage != null) {
                        bcContext.getLogger().error("Rule SOAP processor is not available. Rules are not read.");
                        bcContext.getLogger().debug("Got exception: " + sMessage);
                        return;
                    }
                }          
                
                throw new BFException(e);
            }            
            
            for (IContent cTmp : lResultList)
            {
                CobocContentHandle handle = (CobocContentHandle) cTmp.getHandle();
                
                if (bUseContentFiltering && 
                    ! csSource.checkForAccess(handle.getContentType(), handle.getKey(), true)) {
                    // This item is filtered out.
                    continue;
                }
                
                RuleGroup rgRuleGroup = (RuleGroup) cTmp;
                
                csRuleGroupCache.insertObject(rgRuleGroup);
            }
        }
        
        // Read the rules.
        ctTempContentType = EContentType.COBOC_RULES_RULE;
        
        if (bcContext.getConfig().getCobocConfig().isContentEnabled(ctTempContentType)) {
            srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_GETRULESBYOWNER, this);
            srRequest = csSource.createSoapRequest();
            
            try {
                lResultList = sendSoapRequest(srRequest, srtSoapTemplate.getBindingTemplate(), bpParams, null);
            }
            catch (Exception e) 
            {
                // Try to check the SOAP:Fault
                Throwable notRunning = findSoapProcessorNotRunningException(e);
                
                if (notRunning != null) {
                    String sMessage = notRunning.getMessage();
                    
                    if (sMessage != null) {
                        bcContext.getLogger().error("Rule SOAP processor is not available. Rules are not read.");
                        bcContext.getLogger().debug("Got exception: " + sMessage);
                        return;
                    }
                }          
                
                throw new BFException(e);
            }    
            
            for (IContent cTmp : lResultList)
            {
                Rule rRule = (Rule) cTmp;
                
                // Find the rule group and create the rule key.
                rRule.findRuleGroup(csRuleGroupCache);
                
                CobocContentHandle handle = rRule.getHandle();
                
                if (bUseContentFiltering && 
                    ! csSource.checkForAccess(handle.getContentType(), handle.getKey(), false)) {
                    // This item is filtered out.
                    continue;
                }
                
                csRuleCache.insertObject(rRule);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected List<IContent> handleSoapResponse(OMElement oeResponse, EContentType objectType) throws BFException {
        OMElement oeTuple = oeResponse.getFirstElement();
        IXmlSource sUnmarshallSource = IXmlSource.Factory.newInstance();
        List<IContent> lRes = new ArrayList<IContent>(20);
        
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
                
                IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(ctTempContentType, null, getMethodVersion());
                    
                if (cuUnmarshaller == null) {
                    throw new BFException("Unable to get content unmarshaller for content type " + ctTempContentType);
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
