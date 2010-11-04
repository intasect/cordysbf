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

import org.apache.axiom.om.OMElement;
import org.apache.commons.beanutils.ConvertUtils;

import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.EBindingHandlerParameter;
import com.cordys.coe.bf.databind.IBindingContext;

/**
 * Base class for all context property handlers.
 *
 * @author mpoyhone
 */
public class PropertyHandlerBase extends ValueHandlerBase
{
    private String sPropertyName;
    
    public PropertyHandlerBase(String sPropertyName, boolean bIsAttribute){
        super(bIsAttribute);
        this.sPropertyName = sPropertyName;
    }    
    
    public PropertyHandlerBase(QName qnName, String sPropertyName, boolean bIsAttribute) {
        super(qnName, bIsAttribute);
        this.sPropertyName = sPropertyName;
    }
    
    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#marshallField(com.cordys.coe.bf.databind.IBindingContext)
     */
    public void marshallField(IBindingContext bcContext) throws BindingException, XMLStreamException
    {
        if (! shouldExecute(bcContext)) {
            return;
        }
        
        if (sPropertyName == null) {
            return;
        }
        
        String sValue = ConvertUtils.convert(bcContext.getParameters().getParameter(sPropertyName));
        XMLStreamWriter xswWriter = bcContext.getMarshallingDestination().getStreamWriter();
        
        if (sValue != null) {
            writeElementWithValue(xswWriter, sValue, bcContext);
        } else {
            writeEmptyElement(xswWriter, bcContext);
        }
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#unmarshallField(com.cordys.coe.bf.databind.IBindingContext)
     */
    public void unmarshallField(IBindingContext bcContext) throws BindingException
    {
        if (! shouldExecute(bcContext)) {
            return;
        }
        
        if (sPropertyName == null) {
            return;
        }
        
        String sValue = null;
        OMElement oeElem = bcContext.getUnmarshallingSource().getOMElement();
        
        if (! bIsAttribute) {
            sValue = oeElem.getText().trim();
        } else {
            sValue = oeElem.getAttributeValue(qnName);
        }
        
        bcContext.getParameters().setParameter(sPropertyName, sValue);
        
        unmarshallChildren(bcContext);
    }
    
    /**
     * Checks the parameters if this handler should execute. 
     * @return <code>true</code> if this handler should execute.
     */
    protected boolean shouldExecute(IBindingContext bcContext) {
        if (! super.shouldExecute(bcContext)) {
            return false;
        }

        if (bcContext.isUnmarshallingContext()) {
            if (sPropertyName == null) {
                return true;
            }
            
            String sValue = ConvertUtils.convert(bcContext.getParameters().getParameter(sPropertyName));                

            if (sValue != null && sValue.length() > 0) {
                return getParameter(EBindingHandlerParameter.REPLACE) != null;
            }
        }
        
        return true;
    }    
}
