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
package com.cordys.coe.bf.content.types.coboc.schedules;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.XmlStructure;


/**
 * Class containing CoBOC schedule template information.
 *
 * @author mpoyhone
 */
public class ScheduleTemplate extends CobocContentBase
{   
    private String sCount;
    private XmlStructure xsDatetime;
    private String sListener;
    private XmlStructure xsParameters;
    private String autodeploy;
    
    /**
     * Constructor for CobocFolderContentBase
     */
    public ScheduleTemplate()
    {
        super(EContentType.COBOC_SCHEDULE_TEMPLATE);
    }
    
    
    /**
     * @see com.cordys.coe.bf.content.types.coboc.CobocContentBase#createWriteVersion(com.cordys.coe.bf.content.base.IContentSource)
     */
    @Override
    public IContent createWriteVersion(IContentSource csDest) throws BFException
    {
        ScheduleTemplate res = (ScheduleTemplate) super.createWriteVersion(csDest);
        
        if (csDest.getType() == EContentSourceType.ISV) {
            if (bcContext.getConfig().getCobocConfig().isAutoDeployMessageModels()) {
                res.setAutodeploy("true");
            }
        }
        
        return res;
    }



    /**
     * This also updates the name as this objects key.
     * @see com.cordys.coe.bf.content.types.coboc.CobocContentBase#setName(java.lang.String)
     */
    public void setName(String aName)
    {
        super.setName(aName);
        chHandle.setKey(aName);
    }

    /**
     * Returns the count.
     *
     * @return Returns the count.
     */
    public String getCount()
    {
        return sCount;
    }

    /**
     * The count to set.
     *
     * @param aCount The count to set.
     */
    public void setCount(String aCount)
    {
        sCount = aCount;
    }

    /**
     * Returns the datetime.
     *
     * @return Returns the datetime.
     */
    public XmlStructure getDatetime()
    {
        return xsDatetime;
    }

    /**
     * The datetime to set.
     *
     * @param aDatetime The datetime to set.
     */
    public void setDatetime(XmlStructure aDatetime)
    {
        xsDatetime = aDatetime;
    }

    /**
     * Returns the listener.
     *
     * @return Returns the listener.
     */
    public String getListener()
    {
        return sListener;
    }

    /**
     * The listener to set.
     *
     * @param aListener The listener to set.
     */
    public void setListener(String aListener)
    {
        sListener = aListener;
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
     * Returns the autodeploy.
     *
     * @return Returns the autodeploy.
     */
    public String getAutodeploy()
    {
        return autodeploy;
    }

    /**
     * Sets the autodeploy.
     *
     * @param autodeploy The autodeploy to be set.
     */
    public void setAutodeploy(String autodeploy)
    {
        this.autodeploy = autodeploy;
    }
}
