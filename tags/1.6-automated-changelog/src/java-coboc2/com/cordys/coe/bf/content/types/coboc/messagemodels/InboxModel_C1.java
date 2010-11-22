/**
 * Copyright 2007 Cordys R&D B.V. 
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
package com.cordys.coe.bf.content.types.coboc.messagemodels;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.bf.utils.LDAPStringUtil;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * Class containing C1 and C2 CoBOC inbox message model information.
 *
 * @author mpoyhone
 */
public class InboxModel_C1 extends InboxModel
{
    /**
     * Contains the subscriber information
     */
    private XmlStructure xSubscribers;
    
    /**
     * XPAth of the PARTICIPANT element
     */
    private static AXIOMXPath axParticipantPath;

    static
    {
        try
        {
            // Build the XmlStructure subcriber paths.
            axParticipantPath = new AXIOMXPath("./PARTICIPANT");
        }
        catch (JaxenException e)
        {
            // Just domp it. This should not happen.
            System.err.println("Unable to parse XPath.");
            e.printStackTrace();
        }
    }
    
    /**
     * Constructor for C1 Inbox Model
     */
    public InboxModel_C1()
    {
        super(EContentType.COBOC_INBOX_MODEL_C1);
    }
    
    /**
     * 
     * @see com.cordys.coe.bf.content.types.ContentBase#createWriteVersion(com.cordys.coe.bf.content.base.IContentSource)
     */
    public IContent createWriteVersion(IContentSource csDest)
                                throws BFException
    {
        InboxModel_C1 rWriteVersion = (InboxModel_C1) super.createWriteVersion(csDest);

        if ((csDest.getType() == EContentSourceType.BCP) ||
                (csDest.getType() == EContentSourceType.ISV))
        {
            if (rWriteVersion.xSubscribers != null) {
                // Fix the organiztion DN.
                for (OMNode onNode : rWriteVersion.xSubscribers.selectNodes(axParticipantPath))
                {
                    String sValue = AxiomUtils.getNodeText(onNode);
    
                    if (sValue != null)
                    {
                        sValue = LDAPStringUtil.replaceRoleDn(sValue,
                                                              bcContext.getConfig()
                                                                       .getOrganizationDn());
                        AxiomUtils.setNodeText(onNode, sValue);
                    }
                }
            }
        }

        return rWriteVersion;
    }
    

    /**
     * Returns the subscribers.
     *
     * @return Returns the subscribers.
     */
    public XmlStructure getSubscribers()
    {
        return xSubscribers;
    }


    /**
     * The subscribers to set.
     *
     * @param aSubscribers The subscribers to set.
     */
    public void setSubscribers(XmlStructure aSubscribers)
    {
        xSubscribers = aSubscribers;
    }
}
