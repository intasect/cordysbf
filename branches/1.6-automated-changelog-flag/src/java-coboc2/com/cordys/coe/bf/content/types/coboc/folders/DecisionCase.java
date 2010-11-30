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
package com.cordys.coe.bf.content.types.coboc.folders;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.CobocUtils;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * Class containing CoBOC action template information.
 *
 * @author mpoyhone
 */
public class DecisionCase extends ObjectInstanceBase
{
    private String sDecisionCaseName;
    private String sActionType;
    private String sActionName;
    private String sActionTemplateDescription;
    private String sActionRepositoryVersion;
    private String sAssignments;
    private XmlStructure xsParameters;
    private CobocContentHandle cchSourceTemplateHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_TEMPLATE);
    private String sSourceParentFolder;
    private String sDecisionCaseDescription;
    private String sRulePrefix;
    private List<DecisionCaseAttribute> lAttributes = new LinkedList<DecisionCaseAttribute>();
    private XmlStructure xsMappings;
    private List<DecisionCaseRule> lRules = new LinkedList<DecisionCaseRule>();
    
    /**
     * Constructor for DecisionCase
     */
    public DecisionCase()
    {
        super(EContentType.COBOC_FOLDERS_DECISIONCASE);
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        super.updateReferences(csSrc);
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType()  == EContentSourceType.ISV) {
            CobocUtils.updateCobocHandleId(bcContext, cchSourceTemplateHandle);
        }
        
        for (DecisionCaseAttribute dcaAttrib : lAttributes)
        {
            dcaAttrib.updateReferences(bcContext, csSrc);
        }
        
        for (DecisionCaseRule dcrRule : lRules)
        {
            dcrRule.updateReferences(bcContext, csSrc);
        }
    }

    /**
     * @see com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        Collection<IContentHandle> lSuperContent = super.getReferencedContent();
        List<IContentHandle> lRes = new LinkedList<IContentHandle>();
        
        if (cchSourceTemplateHandle != null) {
            lRes.add(cchSourceTemplateHandle);
        }
        
        for (DecisionCaseAttribute dcaAttrib : lAttributes)
        {
            dcaAttrib.getReferencedContent(lRes);
        }
        
        for (DecisionCaseRule dcrRule : lRules)
        {
            dcrRule.getReferencedContent(lRes);
        }
        
        if (lSuperContent != null) {
            lRes.addAll(lSuperContent);
        }
        
        return lRes;
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#onLoad(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void onLoad(IContentSource csSrc) throws BFException
    {
        super.onLoad(csSrc);
        
        for (DecisionCaseRule dcrRule : lRules)
        {
            dcrRule.setContext(bcContext);
        }
    }

    /**
     * Returns the actionName.
     *
     * @return Returns the actionName.
     */
    public String getActionName()
    {
        return sActionName;
    }

    /**
     * The actionName to set.
     *
     * @param aActionName The actionName to set.
     */
    public void setActionName(String aActionName)
    {
        sActionName = aActionName;
    }

    /**
     * Returns the actionRepositoryVersion.
     *
     * @return Returns the actionRepositoryVersion.
     */
    public String getActionRepositoryVersion()
    {
        return sActionRepositoryVersion;
    }

    /**
     * The actionRepositoryVersion to set.
     *
     * @param aActionRepositoryVersion The actionRepositoryVersion to set.
     */
    public void setActionRepositoryVersion(String aActionRepositoryVersion)
    {
        sActionRepositoryVersion = aActionRepositoryVersion;
    }

    /**
     * Returns the actionTemplateDescription.
     *
     * @return Returns the actionTemplateDescription.
     */
    public String getActionTemplateDescription()
    {
        return sActionTemplateDescription;
    }

    /**
     * The actionTemplateDescription to set.
     *
     * @param aActionTemplateDescription The actionTemplateDescription to set.
     */
    public void setActionTemplateDescription(String aActionTemplateDescription)
    {
        sActionTemplateDescription = aActionTemplateDescription;
    }

    /**
     * Returns the actionType.
     *
     * @return Returns the actionType.
     */
    public String getActionType()
    {
        return sActionType;
    }

    /**
     * The actionType to set.
     *
     * @param aActionType The actionType to set.
     */
    public void setActionType(String aActionType)
    {
        sActionType = aActionType;
    }

    /**
     * Returns the assignments.
     *
     * @return Returns the assignments.
     */
    public String getAssignments()
    {
        return sAssignments;
    }

    /**
     * The assignments to set.
     *
     * @param aAssignments The assignments to set.
     */
    public void setAssignments(String aAssignments)
    {
        sAssignments = aAssignments;
    }

    /**
     * Returns the parameters.
     *
     * @return Returns the parameters.
     */
    public XmlStructure getParameters()
    {
        return xsParameters;
    }

    /**
     * The parameters to set.
     *
     * @param aParameters The parameters to set.
     */
    public void setParameters(XmlStructure aParameters)
    {
        xsParameters = aParameters;
    }

    /**
     * Returns the templateId.
     *
     * @return Returns the templateId.
     */
    public String getSourceTemplateId()
    {
        return cchSourceTemplateHandle.getObjectId();
    }

    /**
     * The templateId to set.
     *
     * @param aTemplateId The templateId to set.
     */
    public void setSourceTemplateId(String aTemplateId)
    {
        cchSourceTemplateHandle.setObjectId(aTemplateId);
    }

    /**
     * Returns the templateKey.
     *
     * @return Returns the templateKey.
     */
    public String getSourceTemplateKey()
    {
        return cchSourceTemplateHandle.getKey();
    }

    /**
     * The templateKey to set.
     *
     * @param aTemplateKey The templateKey to set.
     */
    public void setSourceTemplateKey(String aTemplateKey)
    {
        cchSourceTemplateHandle.setKey(aTemplateKey);
    }

    /**
     * Returns the rules.
     *
     * @return Returns the rules.
     */
    public List<DecisionCaseRule> getRules()
    {
        return lRules;
    }

    /**
     * The rules to set.
     *
     * @param aRules The rules to set.
     */
    public void setRules(List<DecisionCaseRule> aRules)
    {
        lRules = aRules;
    }

    /**
     * Returns the decisionCaseDescription.
     *
     * @return Returns the decisionCaseDescription.
     */
    public String getDecisionCaseDescription()
    {
        return sDecisionCaseDescription;
    }

    /**
     * The decisionCaseDescription to set.
     *
     * @param aDecisionCaseDescription The decisionCaseDescription to set.
     */
    public void setDecisionCaseDescription(String aDecisionCaseDescription)
    {
        sDecisionCaseDescription = aDecisionCaseDescription;
    }

    /**
     * Returns the decisionCaseName.
     *
     * @return Returns the decisionCaseName.
     */
    public String getDecisionCaseName()
    {
        return sDecisionCaseName;
    }

    /**
     * The decisionCaseName to set.
     *
     * @param aDecisionCaseName The decisionCaseName to set.
     */
    public void setDecisionCaseName(String aDecisionCaseName)
    {
        sDecisionCaseName = aDecisionCaseName;
    }

    /**
     * Returns the rulePrefix.
     *
     * @return Returns the rulePrefix.
     */
    public String getRulePrefix()
    {
        return sRulePrefix;
    }

    /**
     * The rulePrefix to set.
     *
     * @param aRulePrefix The rulePrefix to set.
     */
    public void setRulePrefix(String aRulePrefix)
    {
        sRulePrefix = aRulePrefix;
    }

    /**
     * Returns the sourceParentFolder.
     *
     * @return Returns the sourceParentFolder.
     */
    public String getSourceParentFolder()
    {
        return sSourceParentFolder;
    }

    /**
     * The sourceParentFolder to set.
     *
     * @param aSourceParentFolder The sourceParentFolder to set.
     */
    public void setSourceParentFolder(String aSourceParentFolder)
    {
        sSourceParentFolder = aSourceParentFolder;
    }

    /**
     * Returns the attributes.
     *
     * @return Returns the attributes.
     */
    public  List<DecisionCaseAttribute> getAttributes()
    {
        return lAttributes;
    }

    /**
     * The attributes to set.
     *
     * @param aAttributes The attributes to set.
     */
    public void setAttributes( List<DecisionCaseAttribute> aAttributes)
    {
        lAttributes = aAttributes;
    }

    /**
     * Returns the mappings.
     *
     * @return Returns the mappings.
     */
    public XmlStructure getMappings()
    {
        return xsMappings;
    }

    /**
     * The mappings to set.
     *
     * @param aMappings The mappings to set.
     */
    public void setMappings(XmlStructure aMappings)
    {
        xsMappings = aMappings;
    }

    /**
     * Returns the xsParameters.
     *
     * @return Returns the xsParameters.
     */
    public XmlStructure getXsParameters()
    {
        return xsParameters;
    }

    /**
     * The xsParameters to set.
     *
     * @param aXsParameters The xsParameters to set.
     */
    public void setXsParameters(XmlStructure aXsParameters)
    {
        xsParameters = aXsParameters;
    }  
}
