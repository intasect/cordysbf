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
package com.cordys.coe.bf.content.types.xmlstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.types.EContentType;


/**
 * Class containing XMLStore folder information. This object
 * is needed only for containing the hierarchy and it is not
 * written to any content source.
 *
 * @author mpoyhone
 */
public class Folder extends XmlStoreContentBase
{   
    /**
     * Constructor for CobocFolderContentBase
     */
    public Folder()
    {
        this(EContentType.XMLSTORE_FOLDER);
    }
    
    /**
     * Constructor for CobocFolderContentBase
     */
    public Folder(EContentType ctType)
    {
        super(ctType);
        cchParentHandle = new XmlStoreContentHandle(EContentType.XMLSTORE_FOLDER);
        lSubContentList = new ArrayList<IContentHandle>(5);
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        List<IContentHandle> lResList = new LinkedList<IContentHandle>();
        
        if (cchParentHandle != null && cchParentHandle.isSet()) {
            lResList.add(cchParentHandle);
        }
        
        return lResList;
    }
}
