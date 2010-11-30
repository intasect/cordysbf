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

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * Class containing C2 and C3 CoBOC inbox message model information.
 *
 * @author mpoyhone
 */
public class InboxModel extends CobocContentBase
{
    /**
     * DOCUMENTME
     */
    private String sDisplay;
    /**
     * DOCUMENTME
     */
    private String sFlowMetaData;
    /**
     * DOCUMENTME
     */
    private String sMessageMetaData;
    /**
     * DOCUMENTME
     */
    private String sSubject;
    /**
     * DOCUMENTME
     */
    private XmlStructure xColumns;
    /**
     * DOCUMENTME
     */
    private XmlStructure xMessage;

    /**
     * Constructor for InboxModel
     * @param ctType Content type.
     */
    public InboxModel(EContentType ctType)
    {
        super(ctType);
    }

    /**
     * Returns the columns.
     *
     * @return Returns the columns.
     */
    public XmlStructure getColumns()
    {
        return xColumns;
    }

    /**
     * Returns the display.
     *
     * @return Returns the display.
     */
    public String getDisplay()
    {
        return sDisplay;
    }

    /**
     * Returns the flowMetaData.
     *
     * @return Returns the flowMetaData.
     */
    public String getFlowMetaData()
    {
        return sFlowMetaData;
    }

    /**
     * Returns the message.
     *
     * @return Returns the message.
     */
    public XmlStructure getMessage()
    {
        return xMessage;
    }

    /**
     * Returns the messageMetaData.
     *
     * @return Returns the messageMetaData.
     */
    public String getMessageMetaData()
    {
        return sMessageMetaData;
    }

    /**
     * Returns the subject.
     *
     * @return Returns the subject.
     */
    public String getSubject()
    {
        return sSubject;
    }

    /**
     * The columns to set.
     *
     * @param aColumns The columns to set.
     */
    public void setColumns(XmlStructure aColumns)
    {
        xColumns = aColumns;
    }

    /**
     * The display to set.
     *
     * @param aDisplay The display to set.
     */
    public void setDisplay(String aDisplay)
    {
        sDisplay = aDisplay;
    }

    /**
     * The flowMetaData to set.
     *
     * @param aFlowMetaData The flowMetaData to set.
     */
    public void setFlowMetaData(String aFlowMetaData)
    {
        sFlowMetaData = aFlowMetaData;
    }

    /**
     * The message to set.
     *
     * @param aMessage The message to set.
     */
    public void setMessage(XmlStructure aMessage)
    {
        xMessage = aMessage;
    }

    /**
     * The messageMetaData to set.
     *
     * @param aMessageMetaData The messageMetaData to set.
     */
    public void setMessageMetaData(String aMessageMetaData)
    {
        sMessageMetaData = aMessageMetaData;
    }

    /**
     * This also updates the name as this objects key.
     *
     * @see com.cordys.coe.bf.content.types.coboc.CobocContentBase#setName(java.lang.String)
     */
    public void setName(String aName)
    {
        super.setName(aName);
        chHandle.setKey(aName);
    }

    /**
     * The subject to set.
     *
     * @param aSubject The subject to set.
     */
    public void setSubject(String aSubject)
    {
        sSubject = aSubject;
    }
}
