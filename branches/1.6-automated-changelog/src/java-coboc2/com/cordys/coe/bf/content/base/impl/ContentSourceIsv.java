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
package com.cordys.coe.bf.content.base.impl;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;

/**
 * ISV content source.
 *
 * @author mpoyhone
 */
public class ContentSourceIsv  extends ContentSourceBase
{
    private IXmlDestination xdIsvDestination;
    
    /**
     * Constructor for ContentSourceBcp
     * @param bcContext Context.
     */
    public ContentSourceIsv(BFContext bcContext)
    {
        super(bcContext);
    }

    /**
     * Returns the isvDestination.
     *
     * @return Returns the isvDestination.
     */
    public IXmlDestination getIsvDestination()
    {
        return xdIsvDestination;
    }

    /**
     * The isvDestination to set.
     *
     * @param aIsvDestination The isvDestination to set.
     */
    public void setIsvDestination(IXmlDestination aIsvDestination)
    {
        xdIsvDestination = aIsvDestination;
    }    
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#registerSoapMethodTemplate(java.lang.String, java.lang.String, com.cordys.coe.bf.soap.ISoapRequestTemplate)
     */
    public void registerSoapMethodTemplate(String sTemplateId, String version, ISoapRequestTemplate srtTemplate) throws BFException {
        throw new BFException("SOAP request templates are not supported by " + getType().getLogName() + " content source.");
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#getType()
     */
    public EContentSourceType getType()
    {
        return EContentSourceType.ISV;
    }

    
    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#getContentTypeFromXml(com.cordys.coe.bf.content.base.IXmlSource)
     */
    public EContentType getContentTypeFromXml(IXmlSource xsXml) throws BFException
    {
        throw new BFException("getContentTypeFromXml is not implemented.");
    }
}
