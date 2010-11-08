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
package com.cordys.coe.bf.content.types.coboc.messagemodels;

import java.util.ArrayList;

import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * Class containing C3 CoBOC message template information.
 *
 * @author mpoyhone
 */
public class MessageTemplate extends CobocContentBase
{
    /**
     * Contains the message template XML data.
     */
    private XmlStructure message;

    /**
     * Constructor for MessageModel
     */
    public MessageTemplate()
    {
        super(EContentType.COBOC_MESSAGE_TEMPLATE);
        lSubContentList = new ArrayList<IContentHandle>(5);
    }
    
    
    /**
     * Returns the message.
     *
     * @return Returns the message.
     */
    public XmlStructure getMessage()
    {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message The message to be set.
     */
    public void setMessage(XmlStructure message)
    {
        this.message = message;
    }


    /**
     * @see com.cordys.coe.bf.content.types.coboc.CobocContentBase#setKey(java.lang.String)
     */
    @Override
    public void setKey(String key)
    {
        super.setKey(key);
        setName(key);
    }
    
    
}
