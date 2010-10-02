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
 * Class containing C3 CoBOC inbox message model information.
 *
 * @author mpoyhone
 */
public class InboxModel_C3 extends InboxModel
{
    /**
     * Contains the namespace information.
     */
    private XmlStructure namespaceXml;
    
    /**
     * Constructor for C3 Inbox Model
     */
    public InboxModel_C3()
    {
        super(EContentType.COBOC_INBOX_MODEL_C3);
        cchParentHandle = new CobocContentHandle(EContentType.COBOC_MESSAGE_TEMPLATE);
    }
    
    /**
     * 
     * @see com.cordys.coe.bf.content.types.ContentBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        List<IContentHandle> lResList = new LinkedList<IContentHandle>();

        if ((cchParentHandle != null) && cchParentHandle.isSet())
        {
            lResList.add(cchParentHandle);
        }

        return lResList;
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        super.updateReferences(csSrc);
        
        if (bcContext == null) {
            // This has not been added to the context.
            return;
        }
        
        if (cchParentHandle != null) {
            CobocUtils.updateCobocHandle(bcContext, cchParentHandle);
            
            if (csSrc.getType() == EContentSourceType.BCP) {
                createKey();
            }
        }
    }
    
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#onLoad(com.cordys.coe.bf.content.base.IContentSource)
     */
    @Override
    public void onLoad(IContentSource csSrc) throws BFException
    {
        super.onLoad(csSrc);
        
        if (csSrc.getType() == EContentSourceType.FILESYSTEM) {
            createKey();
        }        
    }
    
    public void createKey()
    {
        String parentKey = getParentKey();
        String name = getName();
        
        if (parentKey != null && name != null) {
            setKey(parentKey + "/" + name);
        }
    }
    
    /**
     * Returns the namespaceXml.
     *
     * @return Returns the namespaceXml.
     */
    public XmlStructure getNamespaceXml()
    {
        return namespaceXml;
    }

    /**
     * Sets the namespaceXml.
     *
     * @param namespaceXml The namespaceXml to be set.
     */
    public void setNamespaceXml(XmlStructure namespaceXml)
    {
        this.namespaceXml = namespaceXml;
    }
}
