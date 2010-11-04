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
 * Class containing CoBOC content map information.
 *
 * @author mpoyhone
 */
public class ContentMap extends ObjectInstanceBase
{
    private XmlStructure xContentMapData;

    /**
     * Constructor for ContentMap
     */
    public ContentMap()
    {
        super(EContentType.COBOC_FOLDERS_CONTENTMAP);
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
     * Returns the contentMapData.
     *
     * @return Returns the contentMapData.
     */
    public XmlStructure getContentMapData()
    {
        return xContentMapData;
    }

    /**
     * The contentMapData to set.
     *
     * @param aContentMapData The contentMapData to set.
     */
    public void setContentMapData(XmlStructure aContentMapData)
    {
        xContentMapData = aContentMapData;
    }  
}
