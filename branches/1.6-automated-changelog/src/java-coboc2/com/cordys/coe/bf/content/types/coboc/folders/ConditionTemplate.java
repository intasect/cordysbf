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

import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * Class containing CoBOC condition template information.
 *
 * @author mpoyhone
 */
public class ConditionTemplate extends ObjectInstanceBase
{
    private String sConditionTemplateDescription;
    private XmlStructure xsTableData;
    private String sConditionExpression;
    private XmlStructure xsParameters;
    
    // Used for DecisionCases
    private String sReference;
    private String sDisplayName;
    private String sExpression;
    private String sConditionRepositoryName;
    private String sConditionRepositoryVersion;
    /**
     * Constructor for ConditionTemplate
     */
    public ConditionTemplate()
    {
        super(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE);
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        super.updateReferences(csSrc);
    }

    /**
     * @see com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        Collection<IContentHandle> lSuperContent = super.getReferencedContent();
        
        return lSuperContent;
    }

    /**
     * Returns the conditionExpression.
     *
     * @return Returns the conditionExpression.
     */
    public String getConditionExpression()
    {
        return sConditionExpression;
    }

    /**
     * The conditionExpression to set.
     *
     * @param aConditionExpression The conditionExpression to set.
     */
    public void setConditionExpression(String aConditionExpression)
    {
        sConditionExpression = aConditionExpression;
    }

    /**
     * Returns the conditionTemplateDescription.
     *
     * @return Returns the conditionTemplateDescription.
     */
    public String getConditionTemplateDescription()
    {
        return sConditionTemplateDescription;
    }

    /**
     * The conditionTemplateDescription to set.
     *
     * @param aConditionTemplateDescription The conditionTemplateDescription to set.
     */
    public void setConditionTemplateDescription(String aConditionTemplateDescription)
    {
        sConditionTemplateDescription = aConditionTemplateDescription;
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
     * Returns the tableData.
     *
     * @return Returns the tableData.
     */
    public XmlStructure getTableData()
    {
        return xsTableData;
    }

    /**
     * The tableData to set.
     *
     * @param aTableData The tableData to set.
     */
    public void setTableData(XmlStructure aTableData)
    {
        xsTableData = aTableData;
    }
    
    
    /**
     * Returns the displayName.
     *
     * @return Returns the displayName.
     */
    public String getDisplayName()
    {
        return sDisplayName;
    }

    /**
     * The displayName to set.
     *
     * @param aDisplayName The displayName to set.
     */
    public void setDisplayName(String aDisplayName)
    {
        sDisplayName = aDisplayName;
    }

    /**
     * Returns the expression.
     *
     * @return Returns the expression.
     */
    public String getExpression()
    {
        return sExpression;
    }

    /**
     * The expression to set.
     *
     * @param aExpression The expression to set.
     */
    public void setExpression(String aExpression)
    {
        sExpression = aExpression;
    }

    /**
     * Returns the reference.
     *
     * @return Returns the reference.
     */
    public String getReference()
    {
        return sReference;
    }

    /**
     * The reference to set.
     *
     * @param aReference The reference to set.
     */
    public void setReference(String aReference)
    {
        sReference = aReference;
    }

    /**
     * Returns the conditionRepositoryName.
     *
     * @return Returns the conditionRepositoryName.
     */
    public String getConditionRepositoryName()
    {
        return sConditionRepositoryName;
    }

    /**
     * The conditionRepositoryName to set.
     *
     * @param aConditionRepositoryName The conditionRepositoryName to set.
     */
    public void setConditionRepositoryName(String aConditionRepositoryName)
    {
        sConditionRepositoryName = aConditionRepositoryName;
    }

    /**
     * Returns the conditionRepositoryVersion.
     *
     * @return Returns the conditionRepositoryVersion.
     */
    public String getConditionRepositoryVersion()
    {
        return sConditionRepositoryVersion;
    }

    /**
     * The conditionRepositoryVersion to set.
     *
     * @param aConditionRepositoryVersion The conditionRepositoryVersion to set.
     */
    public void setConditionRepositoryVersion(String aConditionRepositoryVersion)
    {
        sConditionRepositoryVersion = aConditionRepositoryVersion;
    }    
}
