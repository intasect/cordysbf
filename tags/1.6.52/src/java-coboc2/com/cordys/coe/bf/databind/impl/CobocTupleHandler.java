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

import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.IBindingConstants;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.databind.IBindingHandler;
import com.cordys.coe.bf.utils.AxiomUtils;

/**
 * Tuple/old/new handler for CoBOC content which outputs the old and
 * new elements based on the given ContentTuple object. This object
 * must be in the binding context with the default JavaBean name ('default').
 * 
 * The templates must have the tuple-old-new format.
 * Also the keepchildren attribute must be set for this handler in the
 * configuration in order to allow this handler to have the child handlers
 * as well. 
 * 
 * This handler does not support unmarshalling. 
 * 
 * @author mpoyhone
 */
public class CobocTupleHandler extends XmlElementHandler
{
    public CobocTupleHandler(QName qnName) {
        super(qnName, null);
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
        
        if (currentName.getNamespaceURI() != null && currentName.getNamespaceURI().length() > 0)
        {
            if (! AxiomUtils.namespaceEquals(currentName, bcContext.getCurrentNamespace())) {
                // Set the current namespace to the context
                oldNamespace = bcContext.setCurrentNamespace(currentName);
                writeNamespace = true;
            }
        } else {
            currentName = getCurrentName(bcContext);
        }
        
        xswWriter.writeStartElement(currentName.getPrefix(), currentName.getLocalPart(), currentName.getNamespaceURI());
        
        if (writeNamespace) {
            xswWriter.writeNamespace(currentName.getPrefix(), currentName.getNamespaceURI());
        }
        
        Object oBean = bcContext.getJavaBean(IBindingConstants.BEANNAME_DEFAULT);
        
        if (oBean == null) {
            throw new BindingException("No ContentTuple found with ID " + IBindingConstants.BEANNAME_DEFAULT);
        }
        
        if (! (oBean instanceof ContentTuple)) {
            throw new BindingException("JavaBean must be of type ContentTuple.");
        }
        
        ContentTuple ctTuple = (ContentTuple) oBean;
        
        // Set the write object for the tuple element attributes.
        bcContext.setJavaBean(IBindingConstants.BEANNAME_DEFAULT, ctTuple.getWriteObject());
        
        // Marshall all the children setting the old and new objects when needed.
        for (IBindingHandler bhHandler : lChildHandlers)
        {
            String sName = bhHandler.getHandlerName();
            
            if ("old".equals(sName)) {
                // Marshall the old element if the old object is set.
                if (ctTuple.getOld() != null) {
                    // Set the old object and marshall the element.
                    bcContext.setJavaBean(IBindingConstants.BEANNAME_DEFAULT, ctTuple.getOld());
                    bhHandler.marshallField(bcContext);
                    // Set the write object back.
                    bcContext.setJavaBean(IBindingConstants.BEANNAME_DEFAULT, ctTuple.getWriteObject());
                }
            } else if ("new".equals(sName)) {
                // Marshall the new element if the new object is set.
                if (ctTuple.getNew() != null) {
                    // Set the new object and marshall the element.
                    bcContext.setJavaBean(IBindingConstants.BEANNAME_DEFAULT, ctTuple.getNew());
                    bhHandler.marshallField(bcContext);
                    // Set the write object back.
                    bcContext.setJavaBean(IBindingConstants.BEANNAME_DEFAULT, ctTuple.getWriteObject());
                }
/*            } else if ("@lastModified".equals(sName)) {
                if (ctTuple.isInsert()) {
                    // Skip the 'lastModified' attribute on inserts.
                    continue;
                }
                
                // Marshall the tuple attribute.
                bhHandler.marshallField(bcContext);*/
            } else {
                // Just marshall the tuple attribute.
                bhHandler.marshallField(bcContext);
            }
        }
        
        // Replace the ContentTuple bean in the context. Not probably needed but this is done for consistency.
        bcContext.setJavaBean(IBindingConstants.BEANNAME_DEFAULT, ctTuple);
        
        xswWriter.writeEndElement();
        
        if (oldNamespace != null) {
            // Set the old namespace back.
            bcContext.setCurrentNamespace(oldNamespace);
        }
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#unmarshallField(com.cordys.coe.bf.databind.IBindingContext)
     */
    public void unmarshallField(IBindingContext bcContext) throws BindingException
    {
        throw new BindingException("CobocTupleHandler does not support unmarshalling.");
    }
    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#getDefaultValue()
     */
    public String getDefaultValue()
    {
        return null;
    }
    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(String sValue)
    {
        
    }

}
