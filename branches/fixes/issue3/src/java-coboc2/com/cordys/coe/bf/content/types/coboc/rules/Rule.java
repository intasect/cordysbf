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
package com.cordys.coe.bf.content.types.coboc.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentStore;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.bf.utils.CobocUtils;
import com.cordys.coe.bf.utils.CobocXmlStructure;
import com.cordys.coe.bf.utils.XmlStructure;
import com.cordys.tools.ant.cm.EBcpVersion;


/**
 * Class containing CoBOC rule information.
 *
 * @author mpoyhone
 */
public class Rule extends CobocContentBase
{   
    private String sPriority;
    private String sName;
    private String sVersion;
    private String sEffectiveDate;
    private String sExpiryDate;
    private String sModifiedOn;
    private String sModifiedBy;
    private String sEnabled;
    private String sRuleType;
    private CobocContentHandle cchLinkHandle = new CobocContentHandle(EContentType.COBOC_RULES_RULE);
    private CobocContentHandle cchRuleTemplateHandle = new CobocContentHandle(EContentType.COBOC_RULES_RULE);
    private CobocXmlStructure xsMutexRules;
    private CobocXmlStructure xsOverridesRules;
    private CobocXmlStructure xsTriggers;
    private XmlStructure xsRuleDefinition;
    private String isMultiple;
    private String opensWith;
    private XmlStructure modelData; 
    private boolean wsAppsTemplate;
    
    private static AXIOMXPath axTriggersTemplatePath;
    private static AXIOMXPath axMutexRulesRuleIdPath;
    private static AXIOMXPath axOverridesRulesRuleIdPath;
    
    private static Map<String, OMElement> wsappsClassregistryMap;
    
    static {
        try
        {
            // Build the XmlStructure object ID paths.
            axTriggersTemplatePath = new AXIOMXPath("./trigger/template_id");
            axMutexRulesRuleIdPath = new AXIOMXPath("./mutex/@ruleid");
            axOverridesRulesRuleIdPath = new AXIOMXPath("./overrides/@ruleid");
        }
        catch (JaxenException e)
        {
            // Just domp it. This should not happen.
            System.err.println("Unable to parse XPath.");
            e.printStackTrace();
        }
    }
    
    /**
     * Constructor for CobocFolderContentBase
     */
    public Rule()
    {
        super(EContentType.COBOC_RULES_RULE);
        cchParentHandle = new CobocContentHandle(EContentType.COBOC_RULES_RULEGROUP);
    }
    
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#onLoad(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void onLoad(IContentSource csSrc) throws BFException
    {
        boolean bCreateHandles = false;
        boolean bUseObjectIds = false;
        
        if (csSrc.getType() == EContentSourceType.BCP || 
            csSrc.getType() == EContentSourceType.ISV) {
            // Create handles for triggers XML using object ID's.
            bCreateHandles = true;
            bUseObjectIds = true;
        } else  if (csSrc.getType() == EContentSourceType.FILESYSTEM) {
            // Create handles for triggers XML using keys.
            bCreateHandles = true;
            bUseObjectIds = false;
        }

        if (bCreateHandles) {
            if (! wsAppsTemplate) {
                xsTriggers.createHandles(axTriggersTemplatePath, EContentType.COBOC_FOLDERS_TEMPLATE, bUseObjectIds, true);
            }
            
            xsMutexRules.createHandles(axMutexRulesRuleIdPath, EContentType.COBOC_RULES_RULE, bUseObjectIds, true);
            xsOverridesRules.createHandles(axOverridesRulesRuleIdPath, EContentType.COBOC_RULES_RULE, bUseObjectIds, true);
        }
        
       // Check that we have valid handles after load.
       for (CobocContentHandle cchHandle : xsTriggers.getHandles(true))
       {
           if (checkForC3WsAppsTemplate(csSrc)) {
               wsAppsTemplate = true;
               xsTriggers.clearHandles();
               break;
           }
           
           if (! cchHandle.isSet()) {
               throw new BFException(getLogName() + ": Trigger template not set.");
           }
       }
       
       for (CobocContentHandle cchHandle : xsMutexRules.getHandles(true))
       {
           if (! cchHandle.isSet()) {
               throw new BFException(getLogName() + ": Mutex rule not set.");
           }
       }
       
       for (CobocContentHandle cchHandle : xsOverridesRules.getHandles(true))
       {
           if (! cchHandle.isSet()) {
               throw new BFException(getLogName() + ": Override rule not set.");
           }
       }
    }
    
    /**
     * In C3 rules created on a WS-AppServer object do not create a CoBOC template anymore,
     * so we need to check if the template ID points to a WS-AppServer object. 
     * 
     * @return <code>true</code> if this rule points to a C3 WS-AppServer template.
     */
    private boolean checkForC3WsAppsTemplate(IContentSource csSrc) throws BFException
    {
        if (csSrc.getType() != EContentSourceType.BCP) {
            // We can determine this only from BCP itself.
            return false;
        }
        
        if (! bcContext.isVersionLaterThan(EBcpVersion.BCP42_C3)) {
            // Server is not C3.
            return false;
        }
        
        String sTemplateId = xsTriggers != null ? xsTriggers.getValue(axTriggersTemplatePath, false) : null;
        
        if (sTemplateId == null) {
            return false;
        }
        
        if (wsappsClassregistryMap == null) {
            ContentSourceBcp csBcp = (ContentSourceBcp) csSrc;
            BindingParameters bpParams = new BindingParameters();
            AXIOMXPath axResPath;
            OMElement[] oeaResult;
            String sNamespace;
            
            try
            {
                sNamespace = "http://schemas.cordys.com/1.0/xmlstore";
                axResPath = new AXIOMXPath("//ns:classregistry/ns:class");
                axResPath.addNamespace("ns", sNamespace);
            }
            catch (JaxenException e)
            {
                throw new BFException("Unable to parse XPath.");
            }
            
            oeaResult = csBcp.sendSoapRequest(IBcpMethodNames.COBOC_TEMPLATEID_GETWSAPPSCLASSREGISTRY, "bcp42_c3", bpParams, axResPath);
            wsappsClassregistryMap = new HashMap<String, OMElement>();
            
            QName templateIdName = AxiomUtils.createQName("templateid", null, sNamespace);
            
            for (OMElement e : oeaResult)
            {
                OMElement templateIdElem = e.getFirstChildWithName(templateIdName);
                String id = templateIdElem != null ? AxiomUtils.getNodeText(templateIdElem) : null;
                    
                if (id != null) {
                    wsappsClassregistryMap.put(id, e);
                }
            }
        }
        
        return wsappsClassregistryMap.containsKey(sTemplateId);                
    }

    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#createWriteVersion(com.cordys.coe.bf.content.base.IContentSource)
     */
    public IContent createWriteVersion(IContentSource csDest) throws BFException
    {
        Rule rWriteVersion = (Rule) super.createWriteVersion(csDest);
        
        if (csDest.getType() == EContentSourceType.BCP ||
             csDest.getType()  == EContentSourceType.ISV) {
            // Always clear the date.
            rWriteVersion.setEffectiveDate(null);
            
            // Clear the effective date if it is in the past.
            long timenow = System.currentTimeMillis();
            String timenowStr = String.format("%tFT%tT", timenow, timenow);
            String writeVersionExpiryDate = rWriteVersion.getExpiryDate();
            
            if (writeVersionExpiryDate != null && writeVersionExpiryDate.compareTo(timenowStr) < 0) {
                rWriteVersion.setExpiryDate(null);
            }
            
            // Set object ID's in the triggers XML.
            rWriteVersion.xsTriggers.updateHandleXmlValues(true);
            rWriteVersion.xsMutexRules.updateHandleXmlValues(true);
            rWriteVersion.xsOverridesRules.updateHandleXmlValues(true);
            
            if (csDest.getType() == EContentSourceType.BCP) {
                // Set the modified by to the current user DN.
                String sUserDn  = bcContext.getConfig().getUserDn();
                
                if (sUserDn == null) {
                    throw new BFException("User DN not set in the config.");
                }
                
                rWriteVersion.sModifiedBy = sUserDn;
            }
        } else if (csDest.getType() == EContentSourceType.FILESYSTEM) {
            // Set object keys in the triggers XML.
            rWriteVersion.xsTriggers.updateHandles(bcContext, false);
            rWriteVersion.xsTriggers.updateHandleXmlValues(false);
            rWriteVersion.xsMutexRules.updateHandles(bcContext, false);
            rWriteVersion.xsMutexRules.updateHandleXmlValues(false);
            rWriteVersion.xsOverridesRules.updateHandles(bcContext, false);
            rWriteVersion.xsOverridesRules.updateHandleXmlValues(false);            
        }
        
        return rWriteVersion;
    }

    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType()  == EContentSourceType.ISV) {
            CobocUtils.updateCobocHandle(bcContext, cchParentHandle);
            CobocUtils.updateCobocHandle(bcContext, cchLinkHandle);
            CobocUtils.updateCobocHandle(bcContext, cchRuleTemplateHandle);
            
            if (xsTriggers != null) {
                xsTriggers.updateHandles(bcContext, true);
            }
            
            if (xsMutexRules != null) {
                xsMutexRules.updateHandles(bcContext, true);
            }
            
            if (xsOverridesRules != null) {
                xsOverridesRules.updateHandles(bcContext, true);
            }
        }
    }    
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        List<IContentHandle> lResList = new LinkedList<IContentHandle>();
        
        if (cchParentHandle != null && cchParentHandle.isSet()) {
            lResList.add(cchParentHandle);
        }
        
        if (cchLinkHandle.isSet()) {
            lResList.add(cchLinkHandle);
        }
        
        if (cchRuleTemplateHandle.isSet()) {
            lResList.add(cchRuleTemplateHandle);
        }
        
        if (xsTriggers != null) {
            Collection<CobocContentHandle> cTriggerHandles = xsTriggers.getHandles(false);
            
            if (cTriggerHandles != null) {
                lResList.addAll(cTriggerHandles);
            }
        }
        
        if (xsMutexRules != null) {
            Collection<CobocContentHandle> cMutexHandles = xsMutexRules.getHandles(false);
            
            if (cMutexHandles != null) {
                lResList.addAll(cMutexHandles);
            }
        }
        
        if (xsOverridesRules != null) {
            Collection<CobocContentHandle> cOverridesHandles = xsOverridesRules.getHandles(false);
            
            if (cOverridesHandles != null) {
                lResList.addAll(cOverridesHandles);
            }
        }
        
        return lResList;
    }    

    /**
     * Returns the priority.
     *
     * @return Returns the priority.
     */
    public String getPriority()
    {
        return sPriority;
    }

    /**
     * The priority to set.
     *
     * @param aPriority The priority to set.
     */
    public void setPriority(String aPriority)
    {
        sPriority = aPriority;
    }

    /**
     * Returns the effectiveDate.
     *
     * @return Returns the effectiveDate.
     */
    public String getEffectiveDate()
    {
        return sEffectiveDate;
    }

    /**
     * The effectiveDate to set.
     *
     * @param aEffectiveDate The effectiveDate to set.
     */
    public void setEffectiveDate(String aEffectiveDate)
    {
        sEffectiveDate = aEffectiveDate;
    }

    /**
     * Returns the enabled.
     *
     * @return Returns the enabled.
     */
    public String getEnabled()
    {
        return sEnabled;
    }

    /**
     * The enabled to set.
     *
     * @param aEnabled The enabled to set.
     */
    public void setEnabled(String aEnabled)
    {
        sEnabled = aEnabled;
    }

    /**
     * Returns the expiryDate.
     *
     * @return Returns the expiryDate.
     */
    public String getExpiryDate()
    {
        return sExpiryDate;
    }

    /**
     * The expiryDate to set.
     *
     * @param aExpiryDate The expiryDate to set.
     */
    public void setExpiryDate(String aExpiryDate)
    {
        sExpiryDate = aExpiryDate;
    }

    /**
     * Returns the modifiedBy.
     *
     * @return Returns the modifiedBy.
     */
    public String getModifiedBy()
    {
        return sModifiedBy;
    }

    /**
     * The modifiedBy to set.
     *
     * @param aModifiedBy The modifiedBy to set.
     */
    public void setModifiedBy(String aModifiedBy)
    {
        sModifiedBy = aModifiedBy;
    }

    /**
     * Returns the modifiedOn.
     *
     * @return Returns the modifiedOn.
     */
    public String getModifiedOn()
    {
        return sModifiedOn;
    }

    /**
     * The modifiedOn to set.
     *
     * @param aModifiedOn The modifiedOn to set.
     */
    public void setModifiedOn(String aModifiedOn)
    {
        sModifiedOn = aModifiedOn;
    }

    /**
     * Returns the mutexRules.
     *
     * @return Returns the mutexRules.
     */
    public CobocXmlStructure getMutexRules()
    {
        return xsMutexRules;
    }

    /**
     * The mutexRules to set.
     *
     * @param aMutexRules The mutexRules to set.
     */
    public void setMutexRules(CobocXmlStructure xsMutexRules)
    {
        this.xsMutexRules = xsMutexRules;
    }

    /**
     * Returns the name.
     *
     * @return Returns the name.
     */
    public String getName()
    {
        return sName;
    }

    /**
     * The name to set.
     *
     * @param aName The name to set.
     */
    public void setName(String aName)
    {
        sName = aName;
    }

    /**
     * Returns the overridesRules.
     *
     * @return Returns the overridesRules.
     */
    public CobocXmlStructure getOverridesRules()
    {
        return xsOverridesRules;
    }

    /**
     * The overridesRules to set.
     *
     * @param aOverridesRules The overridesRules to set.
     */
    public void setOverridesRules(CobocXmlStructure xsOverridesRules)
    {
        this.xsOverridesRules = xsOverridesRules;
    }

    /**
     * Returns the ruleDefinition.
     *
     * @return Returns the ruleDefinition.
     */
    public XmlStructure getRuleDefinition()
    {
        return xsRuleDefinition;
    }

    /**
     * The ruleDefinition to set.
     *
     * @param xsRuleDefinition The ruleDefinition to set.
     */
    public void setRuleDefinition(XmlStructure xsRuleDefinition)
    {
        this.xsRuleDefinition = xsRuleDefinition;
    }

    /**
     * Returns the ruleGroupId.
     *
     * @return Returns the ruleGroupId.
     */
    public String getRuleGroupId()
    {
        return cchParentHandle.getObjectId();
    }

    /**
     * The ruleGroupId to set.
     *
     * @param aRuleGroupId The ruleGroupId to set.
     */
    public void setRuleGroupId(String aRuleGroupId)
    {
        cchParentHandle.setObjectId(aRuleGroupId);
    }

    /**
     * Returns the ruleType.
     *
     * @return Returns the ruleType.
     */
    public String getRuleType()
    {
        return sRuleType;
    }

    /**
     * The ruleType to set.
     *
     * @param aRuleType The ruleType to set.
     */
    public void setRuleType(String aRuleType)
    {
        sRuleType = aRuleType;
    }

    /**
     * Returns the triggers.
     *
     * @return Returns the triggers.
     */
    public CobocXmlStructure getTriggers()
    {
        return xsTriggers;        
    }

    /**
     * The triggers to set.
     *
     * @param aTriggers The triggers to set.
     */
    public void setTriggers(CobocXmlStructure xsTriggers)
    {
        this.xsTriggers = xsTriggers;
    }

    /**
     * Returns the version.
     *
     * @return Returns the version.
     */
    public String getVersion()
    {
        return sVersion;
    }

    /**
     * The version to set.
     *
     * @param aVersion The version to set.
     */
    public void setVersion(String aVersion)
    {
        sVersion = aVersion;
    }
    
    /**
     * Generates the key based on the rule group name and the rule name. 
     */
    public void generateKey() throws BFException {
        String sRuleName = sName;
        String sRuleGroupName = cchParentHandle.getKey();
        
        if (sRuleName == null) {
            throw new BFException("Rule name not set.");
        }
        
        if (sRuleGroupName == null) {
            throw new BFException("Rule group key not set.");
        }
        
        setKey(sRuleGroupName + "/" + sRuleName);
    }
    
    /**
     * Sets the rule group of this rule and adds this as a child of the rule group
     * @param rgGroup New rule group fro this rule.
     * @throws BFException Thrown if the rule group is invalid.
     */
    public void setRuleGroup(RuleGroup rgGroup) throws BFException {
        if (! rgGroup.getChildren().contains(chHandle)) {
            rgGroup.addChild(chHandle);
        }
        
        cchParentHandle.copyFrom(rgGroup.getHandle());
        generateKey();
    }
    
    /**
     * Finds the rule group from the given content store and adds this rule to that rule group. 
     */
    public void findRuleGroup(IContentStore csRuleGroupStore) throws BFException {
        RuleGroup rgGroup = (RuleGroup) csRuleGroupStore.findObject(cchParentHandle);
        
        if (rgGroup == null) {
            // Rule group might have been filtered out.
            return;
        }
            
        setRuleGroup(rgGroup);
    }


    /**
     * Returns the cchLinkHandle.
     *
     * @return Returns the cchLinkHandle.
     */
    public CobocContentHandle getLinkHandle()
    {
        return cchLinkHandle;
    }


    /**
     * The cchLinkHandle to set.
     *
     * @param aCchLinkHandle The cchLinkHandle to set.
     */
    public void setLinkHandle(CobocContentHandle aLinkHandle)
    {
        cchLinkHandle = aLinkHandle;
    }


    /**
     * Returns the cchRuleTemplateHandle.
     *
     * @return Returns the cchRuleTemplateHandle.
     */
    public CobocContentHandle getRuleTemplateHandle()
    {
        return cchRuleTemplateHandle;
    }


    /**
     * The cchRuleTemplateHandle to set.
     *
     * @param aCchRuleTemplateHandle The cchRuleTemplateHandle to set.
     */
    public void setRuleTemplateHandle(CobocContentHandle aRuleTemplateHandle)
    {
        cchRuleTemplateHandle = aRuleTemplateHandle;
    }


    /**
     * Returns the isMultiple.
     *
     * @return Returns the isMultiple.
     */
    public String getIsMultiple()
    {
        return isMultiple;
    }


    /**
     * Sets the isMultiple.
     *
     * @param isMultiple The isMultiple to be set.
     */
    public void setIsMultiple(String isMultiple)
    {
        this.isMultiple = isMultiple;
    }


    /**
     * Returns the opensWith.
     *
     * @return Returns the opensWith.
     */
    public String getOpensWith()
    {
        return opensWith;
    }


    /**
     * Sets the opensWith.
     *
     * @param opensWith The opensWith to be set.
     */
    public void setOpensWith(String opensWith)
    {
        this.opensWith = opensWith;
    }


    /**
     * Returns the modelData.
     *
     * @return Returns the modelData.
     */
    public XmlStructure getModelData()
    {
        return modelData;
    }


    /**
     * Sets the modelData.
     *
     * @param modelData The modelData to be set.
     */
    public void setModelData(XmlStructure modelData)
    {
        this.modelData = modelData;
    }


    /**
     * Returns the wsAppsTemplate.
     *
     * @return Returns the wsAppsTemplate.
     */
    public boolean isWsAppsTemplate()
    {
        return wsAppsTemplate;
    }


    /**
     * Sets the wsAppsTemplate.
     *
     * @param wsAppsTemplate The wsAppsTemplate to be set.
     */
    public void setWsAppsTemplate(boolean wsAppsTemplate)
    {
        this.wsAppsTemplate = wsAppsTemplate;
    }
}
