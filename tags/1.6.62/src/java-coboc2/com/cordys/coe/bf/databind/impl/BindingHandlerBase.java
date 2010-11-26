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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.databind.EBindingHandlerParameter;
import com.cordys.coe.bf.databind.IBindingConstants;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.databind.IBindingHandler;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * A base class for all binding handlers
 * 
 * @author mpoyhone
 */
public abstract class BindingHandlerBase implements IBindingHandler
{
    /**
     * Fully qualified name of the XML element.
     */
    protected QName qnName;
    /**
     * Contains the XML element name of this field. 
     */
    protected String sHandlerName;
    /**
     * Indicates whether the target XML node is an attribute. 
     */
    protected boolean bIsAttribute;
    /**
     * Contains all binding handler parameters.
     */
    protected Map<EBindingHandlerParameter, String> mParameterMap = new HashMap<EBindingHandlerParameter, String>();
    /**
     * Contains mappings from child handler name to handler object. 
     */
    protected Map<String, IBindingHandler> mNameToChildHandlerMap = new HashMap<String, IBindingHandler>(); 
    /**
     *  Contains all child handlers that are defined under this one.
     */
    protected List<IBindingHandler> lChildHandlers = new LinkedList<IBindingHandler>();

    /**
     * Parent handler.
     */
    protected IBindingHandler bhParentHandler;
    
    public BindingHandlerBase() {
        
    }
    
    public BindingHandlerBase(QName qnName) {
        this(qnName, false);
    }
    
    public BindingHandlerBase(boolean bIsAttribute) {
        this.bIsAttribute = bIsAttribute;
    }    
    
    public BindingHandlerBase(QName qnName, boolean bIsAttribute) {
        this.bIsAttribute = bIsAttribute;
        setXmlName(qnName);
    }
    
    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#getChildren()
     */
    public List<IBindingHandler> getChildren()
    {
        return lChildHandlers;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#getHandlerName()
     */
    public String getHandlerName()
    {
        return sHandlerName;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#addChildHandler(com.cordys.coe.bf.databind.IBindingHandler)
     */
    public boolean addChildHandler(IBindingHandler bhHandler) throws BindingException
    {
        String sChildName = bhHandler.getHandlerName();
        
        if (sChildName != null) {
            String name = sChildName;
            
            if (! name.startsWith("@")) {
                int num = 2;
                
                if (name.indexOf('[') < 0) {
                    name += "[1]";
                }
                
                while (mNameToChildHandlerMap.containsKey(name)) {
                    // A handler already exists, so get the next number.
                    name = sChildName + "[" + num + "]";
                    num++;
                }
            }
            
            mNameToChildHandlerMap.put(name, bhHandler);
        }
        
        lChildHandlers.add(bhHandler);
        bhHandler.setParent(this);
        
        return true;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#findChildHandler(java.lang.String)
     */
    public IBindingHandler findChildHandler(String sHandlerName) throws BindingException
    {
        String name = sHandlerName;
        IBindingHandler res = mNameToChildHandlerMap.get(name);
        
        if (res == null) {
            int i;
            
            // No handler found with this index, so get the first one.
            if ((i = name.lastIndexOf('[')) > 0) {
                name = name.substring(0, i) + "[1]";
            } else {
                name += "[1]";
            }
            
            res = mNameToChildHandlerMap.get(name);
        }
        
        return res;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#getParent()
     */
    public IBindingHandler getParent()
    {
        return bhParentHandler;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#removeChildHandler(com.cordys.coe.bf.databind.IBindingHandler)
     */
    public boolean removeChildHandler(IBindingHandler bhHandler) throws BindingException
    {
        String sChildName = bhHandler.getHandlerName();
        
        if (sChildName != null) {
            String name = sChildName;
            
            if (! mNameToChildHandlerMap.containsKey(name)) {
                int i;
                
                // No handler found with this index, so get the first one.
                if ((i = name.lastIndexOf('[')) > 0) {
                    name = name.substring(0, i) + "[1]";
                } else {
                    name += "[1]";
                }
                
                if (! mNameToChildHandlerMap.containsKey(name)) {
                    return false;
                }
            }
            
            mNameToChildHandlerMap.remove(name);
        }
        
        lChildHandlers.remove(bhHandler);
        bhHandler.setParent(null);
        
        return true;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#removeChildHandler(java.lang.String)
     */
    public boolean removeChildHandler(String sHandlerName) throws BindingException
    {
        if (sHandlerName == null) {
            throw new BindingException("Handler name cannot be null.");
        }

        if (! mNameToChildHandlerMap.containsKey(sHandlerName)) {
            return false;
        }
            
        IBindingHandler bhHandler = mNameToChildHandlerMap.remove(sHandlerName);
        
        lChildHandlers.remove(bhHandler);
        bhHandler.setParent(null);
        
        return true;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#replaceChildHandler(com.cordys.coe.bf.databind.IBindingHandler, com.cordys.coe.bf.databind.IBindingHandler)
     */
    public boolean replaceChildHandler(IBindingHandler bhOriginalHandler, IBindingHandler bhNewHandler) throws BindingException
    {
        String sOrigName = bhOriginalHandler.getHandlerName();
        boolean bFound = false;
        
        if (sOrigName != null) {
            for (Map.Entry<String, IBindingHandler> entry : mNameToChildHandlerMap.entrySet())
            {
                if (entry.getValue() == bhOriginalHandler) {
                    entry.setValue(bhNewHandler);
                    bFound = true;
                    break;
                }
            }            
        }
        
        if (bFound) {
            bFound = false;
            for (ListIterator<IBindingHandler> liIter = lChildHandlers.listIterator(); liIter.hasNext();)
            {
                IBindingHandler bhChild = liIter.next();
                
                if (bhChild == bhOriginalHandler) {
                    liIter.set(bhNewHandler);
                    bFound = true;
                    break;
                }
            }
        }

        if (! bFound) {
            throw new BindingException("Unable to find handler with name '" + sOrigName + "'");
        }
        
        if (bhNewHandler.getXmlName() == null) {
            bhNewHandler.setXmlName(bhOriginalHandler.getXmlName());
        }        
        
        bhOriginalHandler.setParent(null);
        bhNewHandler.setParent(this);        
        
        return true;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#replaceChildHandler(java.lang.String, com.cordys.coe.bf.databind.IBindingHandler)
     */
    public boolean replaceChildHandler(String sOriginalHandlerName, IBindingHandler bhNewHandler) throws BindingException
    {
        IBindingHandler bhOrigHandler = findChildHandler(sOriginalHandlerName);
        
        if (bhOrigHandler == null) {
            throw new BindingException("Unable to find handler with name '" + sOriginalHandlerName + "'");
        }
        
        return replaceChildHandler(bhOrigHandler, bhNewHandler);
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#setParent(com.cordys.coe.bf.databind.IBindingHandler)
     */
    public void setParent(IBindingHandler bhHandler)
    {
        bhParentHandler = bhHandler;
    }
    
    protected String readTextElement(XMLStreamReader xsrReader) throws BindingException {
        StringBuffer sbRes = new StringBuffer(256);
        
loop:  while (true) {
            try
            {
                int iEvent = xsrReader.next();
                
                switch (iEvent) {
                case XMLStreamReader.CHARACTERS : 
                case XMLStreamReader.CDATA :
                    sbRes.append(xsrReader.getText());
                    break;
                case XMLStreamReader.END_ELEMENT :
                    break loop;
                }
            }
            catch (XMLStreamException e)
            {
                throw new BindingException("XML parsing exception while unmarshalling.", e);
            }
        }
        
        return sbRes.toString();
    }
    
    protected String readAttributeText(XMLStreamReader xsrReader) throws BindingException {
        int iCurrentEvent = xsrReader.getEventType();
        
        if (iCurrentEvent != XMLStreamReader.ATTRIBUTE) {
            throw new BindingException("Current XML event is not an attribute type.");
        }
        
        return xsrReader.getText();
    }    
    
    /**
     * @deprecated Not used anywhere
     */
    protected void skipElement(XMLStreamReader xsrReader) throws XMLStreamException {
       int iChildLevel = 1;
        
        while (true) {
            int iEvent = xsrReader.next();
            
            switch (iEvent) {
            case XMLStreamReader.START_ELEMENT :
                iChildLevel++;
                break;
                
            case XMLStreamReader.END_ELEMENT :
                iChildLevel--;
                if (iChildLevel <= 0) {
                    return;
                }
                break;
                
            case XMLStreamReader.END_DOCUMENT:
                throw new XMLStreamException("End of document reached while skipping element children .");
            }
        }
    }
    
    /**
     * Calls the unmarshall method for all child handlers.
     * @param bcContext Current binding context.
     * @throws BindingException
     */
    protected void unmarshallChildren(IBindingContext bcContext) throws BindingException {
        IXmlSource xsSrc = bcContext.getUnmarshallingSource();
        OMElement oeStartElem = bcContext.getUnmarshallingSource().getOMElement();

        try
        {
            OMElement oeElem = oeStartElem;
            
            // Handle attributes.
            for (Iterator<?> iIter = oeElem.getAllAttributes(); iIter.hasNext(); ) {
                OMAttribute atAttrib = (OMAttribute) iIter.next();
                QName qnElemName = atAttrib.getQName();
                String sChildName = "@"+ qnElemName.getLocalPart();
                IBindingHandler bhHandler = findChildHandler(sChildName);
                
                if (bhHandler != null) {
                    bhHandler.unmarshallField(bcContext);
                }
            }
            
            Map<String, Integer> nameToIndexMap = new HashMap<String, Integer>();
            
            for (oeElem = oeElem.getFirstElement(); oeElem != null; oeElem = AxiomUtils.getNextSiblingElement(oeElem)){
                QName qnElemName = oeElem.getQName();
                String sChildName = qnElemName.getLocalPart();
                String sHandlerName;
                
                if (! nameToIndexMap.containsKey(sChildName)) {
                    sHandlerName = sChildName + "[1]";
                    nameToIndexMap.put(sChildName, 1);
                } else {
                    int index = nameToIndexMap.get(sChildName);
                    index++;
                    sHandlerName = sChildName + "[" + index + "]";
                    nameToIndexMap.put(sChildName, index);
                }

                IBindingHandler bhHandler = findChildHandler(sHandlerName);

                if (bhHandler != null) {
                    xsSrc.set(oeElem);
                    bhHandler.unmarshallField(bcContext);
                }
            }
        }
        finally {
            xsSrc.set(oeStartElem);
        }
    }
    
    /**
     * Calls the marshall method for all child handlers.
     * @param bcContext Current binding context.
     * @throws BindingException
     * @throws XMLStreamException 
     */
    protected void marshallChildren(IBindingContext bcContext) throws BindingException, XMLStreamException {
        // First marshall the attributes
        for (IBindingHandler bhHandler : lChildHandlers)
        {
            if (bhHandler.getHandlerName().startsWith("@")) {
                bhHandler.marshallField(bcContext);
            }
        }        
        
        // Then the elements.
        for (IBindingHandler bhHandler : lChildHandlers)
        {
            if (! bhHandler.getHandlerName().startsWith("@")) {
                bhHandler.marshallField(bcContext);
            }
        }
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#getXmlName()
     */
    public QName getXmlName()
    {
        return qnName;
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#setXmlName(javax.xml.namespace.QName)
     */
    public void setXmlName(QName qnName)
    {
        if (! bIsAttribute) {
            this.sHandlerName = qnName.getLocalPart();
        } else {
            this.sHandlerName = "@" + qnName.getLocalPart();
        }
        
        this.qnName = qnName;
    }

    /**
     * Returns the isAttribute.
     *
     * @return Returns the isAttribute.
     */
    public boolean isAttribute()
    {
        return bIsAttribute;
    }

    /**
     * The isAttribute to set.
     *
     * @param aIsAttribute The isAttribute to set.
     */
    public void setAttribute(boolean aIsAttribute)
    {
        bIsAttribute = aIsAttribute;
    }

    protected void writeElementWithValue(XMLStreamWriter xswWriter, String sValue, IBindingContext bcContext) throws XMLStreamException, BindingException
    {
        if (! shouldMarshall(sValue)) {
            return;
        }
        
        QName currentName = getCurrentName(bcContext);
        
        if (! bIsAttribute) {
            QName oldNamespace = null;
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

            if (lChildHandlers.size() > 0) {
                marshallChildren(bcContext);
            }
            
            if (sValue != null) {
                xswWriter.writeCharacters(sValue);
            }
    
            xswWriter.writeEndElement();
            
            if (oldNamespace != null) {
                // Set the old namespace back.
                bcContext.setCurrentNamespace(oldNamespace);
            }
        } else {
            currentName = getCurrentName(bcContext);
            xswWriter.writeAttribute(currentName.getPrefix(), currentName.getNamespaceURI(), currentName.getLocalPart(), sValue);
        }        
    }

    protected void writeElementWithValue(XMLStreamWriter xswWriter, XmlStructure xsValue, IBindingContext bcContext) throws XMLStreamException, BindingException
    {
        if (! shouldMarshall(xsValue)) {
            return;
        }
        
        if (! bIsAttribute) {
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
            
            xswWriter.writeStartElement(qnName.getPrefix(), qnName.getLocalPart(), qnName.getNamespaceURI());
            
            if (writeNamespace) {
                xswWriter.writeNamespace(qnName.getPrefix(), qnName.getNamespaceURI());
            }
            
            if (lChildHandlers.size() > 0) {
                marshallChildren(bcContext);
            }            
            
            if (xsValue != null) {
                xsValue.writeToXml(xswWriter);
            }
    
            xswWriter.writeEndElement();
            
            if (oldNamespace != null) {
                // Set the old namespace back.
                bcContext.setCurrentNamespace(oldNamespace);
            }
        } else {
            throw new IllegalArgumentException("XmlStructure cannot be written to an attribute.");
        }        
    }

    protected void writeEmptyElement(XMLStreamWriter xswWriter, IBindingContext bcContext) throws XMLStreamException, BindingException
    {
        if (! shouldMarshall((Object) null)) {
            return;
        }
        
        QName currentName = getCurrentName(bcContext);
        
        if (! bIsAttribute) {
            if (lChildHandlers.size() > 0) {
                writeElementWithValue(xswWriter, (String) null, bcContext);
                return;
            }
            
            xswWriter.writeEmptyElement(currentName.getPrefix(), currentName.getLocalPart(), currentName.getNamespaceURI());
        } else {
            xswWriter.writeAttribute(currentName.getPrefix(), currentName.getNamespaceURI(), currentName.getLocalPart(), "");
        }
    }    

    
    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#setParameter(com.cordys.coe.bf.databind.EBindingHandlerParameter, java.lang.String)
     */
    public void setParameter(EBindingHandlerParameter bpParam, String sValue)
    {
        mParameterMap.put(bpParam, sValue);
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingHandler#getParameter(com.cordys.coe.bf.databind.EBindingHandlerParameter)
     */
    public String getParameter(EBindingHandlerParameter bpParam)
    {
        return mParameterMap.get(bpParam);
    }
    
    protected boolean getBooleanParameter(EBindingHandlerParameter bpParam)
    {
        String sValue = mParameterMap.get(bpParam);
        
        if ("true".equalsIgnoreCase(sValue) || "".equalsIgnoreCase(sValue)) {
            return true;
        }
        
        return false;
    }    

    /**
     * Checks the parameters if this handler should execute. 
     * @return <code>true</code> if this handler should execute.
     */
    protected boolean shouldExecute(IBindingContext bcContext) {
        BindingParameters bpContextParams = bcContext.getParameters();
        
        for (EBindingHandlerParameter bfParam : mParameterMap.keySet())
        {
            switch (bfParam) {
            case SKIP_ON_INSERT :
                if (bpContextParams.getBcpUpdateMode() == IBindingConstants.EUpdateMode.INSERT) {
                    return false;
                }
                break;
                
            case SKIP_ON_UPDATE :
                if (bpContextParams.getBcpUpdateMode() == IBindingConstants.EUpdateMode.UPDATE) {
                    return false;
                }
                break;                
                
            case SKIP_ON_DELETE :
                if (bpContextParams.getBcpUpdateMode() == IBindingConstants.EUpdateMode.DELETE) {
                    return false;
                }
                break;                
            }
        }
        
        return true;
    }
    
    /**
     * Checks the parameters if this handler should marshall the given value. 
     * @return <code>true</code> if this handler should marshall the given value.
     */
    private boolean shouldMarshall(String sValue) {
        if (sValue == null) {
            if (getParameter(EBindingHandlerParameter.SKIP_IF_NULL) != null ||
                getParameter(EBindingHandlerParameter.SKIP_IF_EMPTY) != null) {
                return false;
            }
        } else if (sValue.length() == 0) {
            if (getParameter(EBindingHandlerParameter.SKIP_IF_EMPTY) != null) {
                return false;
            }            
        }
        
        return true;
    }
    
    /**
     * Checks the parameters if this handler should marshall the given value. 
     * @return <code>true</code> if this handler should marshall the given value.
     */
    private boolean shouldMarshall(Object oValue) {
        if (oValue == null) {
            if (getParameter(EBindingHandlerParameter.SKIP_IF_NULL) != null ||
                getParameter(EBindingHandlerParameter.SKIP_IF_EMPTY) != null) {
                return false;
            }
        }
        
        return true;
    }    
    
    
    
    /**
     * Returns the QName that should be written to the outgoing XML based on the
     * namespace URI in this element and the one in the context.
     * @param bcContext Current context.
     * @return QName for the result XML of this element.
     */
    protected QName getCurrentName(IBindingContext bcContext)
    {
        if (qnName.getNamespaceURI() == null || qnName.getNamespaceURI().length() == 0)
        {
            // Namespace URI not set, so use the namespace from the context. Note that we
            // don't support empty namespaces this way.
            QName tmp = bcContext.getCurrentNamespace();
        
            if (tmp != null) {
                return AxiomUtils.createQName(qnName.getLocalPart(), tmp.getPrefix(), tmp.getNamespaceURI());
            }        
        } 
        
        return qnName;
    }    
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getClass().getName().replaceFirst(".*\\.([^.]+)$", "$1") + "[" + getXmlName() + "]";
    }
}
