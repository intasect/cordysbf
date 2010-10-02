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
package com.cordys.coe.bf.content.types.coboc.mdm;

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * A base class for all MDM objects.
 *
 * @author mpoyhone
 */
public abstract class MDMBase extends ObjectInstanceBase
{
    /**
     * Contains the document properties element. 
     */
    protected XmlStructure xDocumentProperties;
    /**
     * XPath for the last modified by in document properties.
     */
    protected static AXIOMXPath axDocumentLastModifiedByPath;
    /**
     * XPath for the last modification date in document properties.
     */
    protected static AXIOMXPath axDocumentLastModificationDatePath;    
    
    static {
        try
        {
            // Build the XmlStructure paths.
            axDocumentLastModificationDatePath = new AXIOMXPath("./lastModificationDate");
            axDocumentLastModifiedByPath = new AXIOMXPath("./lastModifiedBy");
        }
        catch (JaxenException e)
        {
            // Just domp it. This should not happen.
            System.err.println("Unable to parse XPath.");
            e.printStackTrace();
        }
    }    

    /**
     * Constructor for MDMBase
     */
    public MDMBase(EContentType ctType)
    {
        super(ctType);
    }
    
    /**
     * Helper method for updateFromNewVersion and updateFromOldVersion.
     * @param cBcpObject
     * @throws BFException
     */
    protected void copyDataFromBcpObject(IContent cBcpObject) throws BFException {
        if (! (cBcpObject instanceof MDMBase)) {
            throw new BFException(getLogName() + " BCP object is not an MDM object.");
        }
        
        MDMBase mbBcp = (MDMBase) cBcpObject;
        
        if (mbBcp.xDocumentProperties != null && xDocumentProperties != null) {
            String sLastModifiedDate = mbBcp.xDocumentProperties.getValue(axDocumentLastModificationDatePath, true);
            String sLastModifiedBy = mbBcp.xDocumentProperties.getValue(axDocumentLastModifiedByPath, true);
            
            xDocumentProperties.setValue(axDocumentLastModificationDatePath, sLastModifiedDate);
            xDocumentProperties.setValue(axDocumentLastModifiedByPath, sLastModifiedBy);
        }        
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.coboc.CobocContentBase#updateFromNewVersion(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateFromNewVersion(IContent cNewObject, IContentSource csSrc) throws BFException
    {
        super.updateFromNewVersion(cNewObject, csSrc);
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType() == EContentSourceType.ISV) {                
            copyDataFromBcpObject(cNewObject);
        }
    }

    /**
     * @see com.cordys.coe.bf.content.types.coboc.CobocContentBase#updateFromOldVersion(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateFromOldVersion(IContent cOldObject, IContentSource csSrc) throws BFException
    {
        super.updateFromOldVersion(cOldObject, csSrc);
        
        if (csSrc.getType() == EContentSourceType.BCP ||
                csSrc.getType() == EContentSourceType.ISV) {                
                copyDataFromBcpObject(cOldObject);
            }
    }

    /**
     * Returns the documentProperties.
     *
     * @return Returns the documentProperties.
     */
    public XmlStructure getDocumentProperties()
    {
        return xDocumentProperties;
    }

    /**
     * The documentProperties to set.
     *
     * @param aDocumentProperties The documentProperties to set.
     */
    public void setDocumentProperties(XmlStructure aDocumentProperties)
    {
        xDocumentProperties = aDocumentProperties;
    }
}
