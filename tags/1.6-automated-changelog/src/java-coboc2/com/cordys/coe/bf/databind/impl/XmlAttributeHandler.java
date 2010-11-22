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
package com.cordys.coe.bf.databind.impl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.IBindingContext;

/**
 * TODO Describe the class.
 *
 * @author mpoyhone
 */
public class XmlAttributeHandler extends BindingHandlerBase
{
    protected String sValue;
    
    public XmlAttributeHandler(QName qnName, String sValue) {
        super(qnName, true);
        
        this.sValue = sValue;
    }
    
    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#marshallField(com.cordys.coe.bf.databind.IBindingContext)
     */
    public void marshallField(IBindingContext bcContext) throws BindingException, XMLStreamException
    {
        if (! shouldExecute(bcContext)) {
            return;
        }
        
        XMLStreamWriter xswWriter = bcContext.getMarshallingDestination().getStreamWriter();
        QName currentName = getCurrentName(bcContext);
        
        xswWriter.writeAttribute(currentName.getPrefix(), currentName.getNamespaceURI(), currentName.getLocalPart(), sValue);
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#unmarshallField(com.cordys.coe.bf.databind.IBindingContext)
     */
    public void unmarshallField(IBindingContext bcContext) throws BindingException
    {
        
    }

    /**
     * Returns the value.
     *
     * @return Returns the value.
     */
    public String getValue()
    {
        return sValue;
    }

    /**
     * The value to set.
     *
     * @param aValue The value to set.
     */
    public void setValue(String aValue)
    {
        sValue = aValue;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#getDefaultValue()
     */
    public String getDefaultValue()
    {
        return sValue;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(String sValue)
    {
        this.sValue = sValue;
    }

}
