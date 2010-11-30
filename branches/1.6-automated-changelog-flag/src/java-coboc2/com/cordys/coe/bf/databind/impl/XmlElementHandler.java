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
import com.cordys.coe.bf.databind.EBindingHandlerParameter;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.utils.AxiomUtils;

/**
 * TODO Describe the class.
 *
 * @author mpoyhone
 */
public class XmlElementHandler extends BindingHandlerBase
{
    protected String sValue;
    
    public XmlElementHandler(QName qnName, String sValue) {
        super(qnName);
        
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
        QName oldNamespace = null;
        QName currentName = qnName;
        boolean writeNamespace = false;
        boolean revertOldNamespace = false;
        
        if (currentName.getNamespaceURI() != null && currentName.getNamespaceURI().length() > 0)
        {
            if (! AxiomUtils.namespaceEquals(currentName, bcContext.getCurrentNamespace())) {
                // Set the current namespace to the context
                oldNamespace = bcContext.setCurrentNamespace(currentName);
                writeNamespace = true;
                revertOldNamespace = true;
            }
        } else {
            currentName = getCurrentName(bcContext);
            
            // Write the empty namespace, if requested.
            if (getParameter(EBindingHandlerParameter.PRESERVE_NAMESPACE) != null) {
                writeNamespace = true;
            }
        }

        xswWriter.writeStartElement(currentName.getPrefix(), currentName.getLocalPart(), currentName.getNamespaceURI());
        
        if (writeNamespace) {
            xswWriter.writeNamespace(currentName.getPrefix(), currentName.getNamespaceURI());
        }

        marshallChildren(bcContext);
        
        if (sValue != null) {
            xswWriter.writeCharacters(sValue);
        }
        
        xswWriter.writeEndElement();
        
        if (revertOldNamespace) {
            // Set the old namespace back.
            bcContext.setCurrentNamespace(oldNamespace);
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
        
        unmarshallChildren(bcContext);
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
