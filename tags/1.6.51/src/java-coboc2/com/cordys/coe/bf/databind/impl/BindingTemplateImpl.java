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
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.EBindingHandlerParameter;
import com.cordys.coe.bf.databind.IBindingContext;
import com.cordys.coe.bf.databind.IBindingHandler;
import com.cordys.coe.bf.databind.IBindingTemplate;
import com.cordys.coe.bf.databind.XmlLoader;
import com.cordys.coe.bf.utils.AxiomUtils;

/**
 * Implementation of a binding template.
 *
 * @author mpoyhone
 */
public class BindingTemplateImpl implements IBindingTemplate
{
    private IBindingHandler bhBaseHandler;
    private Map<String, Class<?>> mBeanClassMap = new HashMap<String, Class<?>>();
    private String sTemplateId;
    
    public BindingTemplateImpl() {
    }
    

    /**
     * @see com.cordys.coe.bf.databind.IBindingTemplate#getRootHandler()
     */
    public IBindingHandler getRootHandler()
    {
        return bhBaseHandler;
    }   
    
    /**
     * @see com.cordys.coe.bf.databind.IBindingTemplate#parseHandlersFromXml(org.apache.axis2.om.OMElement)
     */
    public void parseHandlersFromXml(OMElement oeRootElement) throws BindingException
    {
        bhBaseHandler = new XmlElementHandler(oeRootElement.getQName(), null);
        
        parseHandlersFromTemplate(oeRootElement, bhBaseHandler);
    }    
    
    private void parseHandlersFromTemplate(OMElement oeRootElement, IBindingHandler bhRootHandler) throws BindingException {
        for (Iterator<?> iAttribIter = oeRootElement.getAllAttributes(); iAttribIter.hasNext();)
        {
            OMAttribute oaAttrib = (OMAttribute) iAttribIter.next();
            IBindingHandler bhAttrib = new XmlAttributeHandler(oaAttrib.getQName(), oaAttrib.getAttributeValue());
            
            bhRootHandler.addChildHandler(bhAttrib);
        }   
        
        for (Iterator<?> iChildIter = oeRootElement.getChildElements(); iChildIter.hasNext();)
        {
            OMElement oeChildElement = (OMElement) iChildIter.next();
            String sValue = null;
            
            if (oeChildElement.getFirstElement() == null) {
                sValue = oeChildElement.getText();
            }
            
            IBindingHandler bhChildHandler = new XmlElementHandler(oeChildElement.getQName(), sValue);
            
            bhRootHandler.addChildHandler(bhChildHandler);
            
            parseHandlersFromTemplate(oeChildElement, bhChildHandler);
        }
    }
    
    public IBindingHandler findHandler(String sHandlerPath, boolean bMustExist) throws BindingException
    {
        String[] saPath = sHandlerPath.split("/");
        IBindingHandler bhCurrent = bhBaseHandler;
        
        if (saPath.length == 0) {
            throw new BindingException("Handler path cannot be empty.");
        }
        
        if (bhCurrent == null) {
            throw new BindingException("Root handler is not set.");
        }
        
        for (int i = 0; i < saPath.length; i++)
        {
            String sElem = saPath[i];
            
            if (sElem.equals(".")) {
                // Means the current.
                continue;
            }
            
            bhCurrent = bhCurrent.findChildHandler(sElem);
            if (bhCurrent == null) {
                if (bMustExist) {
                    throw new BindingException("Cannot find handler for path element '" + sElem + "' from XML path=" + sHandlerPath);
                } else {
                    return null;
                }
            }
        }
        
        return bhCurrent;
    }    
    
    /**
     * @see com.cordys.coe.bf.databind.IBindingTemplatet#replaceHandler(java.lang.String, com.cordys.coe.bf.databind.IBindingHandler)
     */
    public void replaceHandler(String sHandlerPath, IBindingHandler bhNewHandler) throws BindingException
    {
        IBindingHandler bhOldHandler = findHandler(sHandlerPath, true);
        
        replaceHandler(bhOldHandler, bhNewHandler);
    }
    
    private void replaceHandler(IBindingHandler bhOldHandler, IBindingHandler bhNewHandler) throws BindingException
    {
        if (bhOldHandler.getParent() != null) {
            bhOldHandler.getParent().replaceChildHandler(bhOldHandler, bhNewHandler);
        } else {
            bhBaseHandler = bhNewHandler;
        }
    }


    /**
     * @see com.cordys.coe.bf.databind.IBindingTemplate#createMarshallingContext(com.cordys.coe.bf.content.base.IXmlDestination)
     */
    public IBindingContext createMarshallingContext(IXmlDestination xdDestination)
    {
        return new BindingContextImpl(this, bhBaseHandler, xdDestination);
    }


    /**
     * @see com.cordys.coe.bf.databind.IBindingTemplate#createUnmarshallingContext(org.apache.axis2.om.OMElement)
     */
    public IBindingContext createUnmarshallingContext(IXmlSource xsSource)
    {
        return new BindingContextImpl(this, bhBaseHandler, xsSource);
    }


    /**
     * @see com.cordys.coe.bf.databind.IBindingTemplate#parseConfigurationFromXml(org.apache.axis2.om.OMElement)
     */
    public void parseConfigurationFromXml(OMElement oeRootElement, XmlLoader xlLoader) throws BindingException
    {
        String sTmpTemplateId = oeRootElement.getAttributeValue(new QName("templateid"));
        
        if (sTmpTemplateId != null) {
            this.sTemplateId = sTmpTemplateId;
        }
        
        OMElement oeImportsSection = oeRootElement.getFirstChildWithName(new QName("imports"));
        
        if (oeImportsSection != null) {
            parseTemplateImports(oeRootElement, oeImportsSection, xlLoader);
        }

        OMElement oeTemplateSection = oeRootElement.getFirstChildWithName(new QName("template"));
        OMElement oeHandlerSection = oeRootElement.getFirstChildWithName(new QName("handlers"));
        OMElement oeBeanSection = oeRootElement.getFirstChildWithName(new QName("beans"));
        
        if (oeHandlerSection == null) {
            throw new BindingException("Configuration 'handlers' section is missing.");
        }

        if (oeTemplateSection == null) {
            throw new BindingException("Configuration 'template' section is missing.");
        }
        
        if (oeBeanSection != null) {
            for (Iterator<?> iIter = oeBeanSection.getChildrenWithName(new QName("bean")); iIter.hasNext(); ) {
                OMElement oeHandler = (OMElement) iIter.next();
                String sBeanId = oeHandler.getAttributeValue(new QName("beanid"));
                String sBeanClassName = oeHandler.getAttributeValue(new QName("class"));
                
                if (sBeanId == null) {
                    throw new BindingException("Bean ID is missing for bean definition.");
                }
                
                if (sBeanClassName == null) {
                    throw new BindingException("Bean class name is missing for bean definition.");
                }     
                
                if (mBeanClassMap.containsKey(sBeanId)) {
                    throw new BindingException("Bean already defined with ID " + sBeanId);
                }
                
                try
                {
                    Class<?> cBeanClass = Class.forName(sBeanClassName);
                    
                    mBeanClassMap.put(sBeanId, cBeanClass);
                }
                catch (ClassNotFoundException e)
                {
                    throw new BindingException("Bean class not found: " + sBeanClassName, e);
                }                
            }            
        }
        
        
        OMElement oeTemplateRoot = oeTemplateSection.getFirstElement();
        
        if (oeTemplateRoot == null) {
            throw new BindingException("Configuration 'template' section is empty.");
        }
        
        // Parses the XML template as XML handlers. 
        parseHandlersFromXml(oeTemplateRoot);
        
        // Replaces the default XML handlers with custom handlers.
        for (Iterator<?> iIter = oeHandlerSection.getChildElements(); iIter.hasNext(); ) {
            OMElement oeHandler = (OMElement) iIter.next();
            
            if ("handler".equals(oeHandler.getLocalName())) {
                parseHandler(oeHandler);
            } else if ("remove-handler".equals(oeHandler.getLocalName())) {
                removeHandler(oeHandler);
            } else {
                throw new BindingException("Invalid element '" + oeHandler.getLocalName() + "' in handlers section.");
            }
        }
    }
    
    private void parseTemplateImports(OMElement oeTemplateRoot, OMElement oeImportsSection, XmlLoader xlLoader) throws BindingException {
        Map<String, OMElement> mImportedHandlers = new HashMap<String, OMElement>();
        OMElement oeTemplateSection = oeTemplateRoot.getFirstChildWithName(new QName("template"));
        OMElement oeBeansSection = oeTemplateRoot.getFirstChildWithName(new QName("beans"));
        OMElement oeHandlerSection = oeTemplateRoot.getFirstChildWithName(new QName("handlers"));
        
        if (oeBeansSection == null) {
            oeBeansSection = oeTemplateRoot.getOMFactory().createOMElement(new QName("beans"), oeTemplateRoot);
        }
        
        if (oeHandlerSection == null) {
            oeHandlerSection = oeTemplateRoot.getOMFactory().createOMElement(new QName("handlers"), oeTemplateRoot);
        }

        if (oeTemplateSection == null) {
            oeTemplateSection = oeTemplateRoot.getOMFactory().createOMElement(new QName("template"), oeTemplateRoot);
        }
        
        for (Iterator<?> iIter = oeImportsSection.getChildrenWithName(new QName("template")); iIter.hasNext(); ) {
            OMElement oeTemplate = (OMElement) iIter.next();
            String sFileName = oeTemplate.getAttributeValue(new QName("file"));
            String sTemplateId = oeTemplate.getAttributeValue(new QName("template-id"));
            String sDestPath = oeTemplate.getAttributeValue(new QName("destpath"));
            
            if (sFileName == null) {
                throw new BindingException("File name is missing for template import statement.");
            }

            if (sTemplateId == null) {
                throw new BindingException("Template ID is missing for template import statement.");
            }

            if (sDestPath == null) {
                throw new BindingException("Destination XPath is missing for template import statement.");
            }
            
            String sBeanIdRewrite = oeTemplate.getAttributeValue(new QName("bean-rewrite"));
            String sRewriteFrom = null;
            String sRewriteTo = null;
            
            if (sBeanIdRewrite != null) {
                int iIndex = sBeanIdRewrite.indexOf('>');
                
                if (iIndex <= 0) {
                    throw new BindingException("Invalid bean ID rewrite attribute: " + sBeanIdRewrite);
                }
                
                sRewriteFrom = sBeanIdRewrite.substring(0, iIndex);
                sRewriteTo = sBeanIdRewrite.substring(iIndex + 1);
            }            

            OMElement oeImportTemplateRoot = xlLoader.load(sFileName);
            XmlLoader xlChildLoader = xlLoader.getChildLoader(sFileName);
            
            // Import templates imported by the imported template.
            OMElement oeBaseImportsSection = oeImportTemplateRoot.getFirstChildWithName(new QName("imports"));
            OMElement oeBaseBeansSection = oeImportTemplateRoot.getFirstChildWithName(new QName("beans"));
            OMElement oeBaseTemplatesSection = oeImportTemplateRoot.getFirstChildWithName(new QName("template"));
            
            if (oeBaseImportsSection != null) {
                parseTemplateImports(oeImportTemplateRoot, oeBaseImportsSection, xlChildLoader);
            }
            
            // Add the imported handlers to the map.
            OMElement oeBaseHandlersSection = oeImportTemplateRoot.getFirstChildWithName(new QName("handlers"));
            
            if (oeBaseHandlersSection == null) {
                throw new BindingException("Configuration 'handlers' section is missing from the imported template.");
            }
            
            // Modify the xmlpath to include the despath.
            for (Iterator<?> iHandlerIter = oeBaseHandlersSection.getChildElements(); iHandlerIter.hasNext(); ) {
                OMElement oeHandler = (OMElement) iHandlerIter.next();   
                
                if (sDestPath.length() >  0) {
                    OMAttribute oaXmlPath = oeHandler.getAttribute(new QName("xmlpath"));
                    
                    if (oaXmlPath != null) {
                        // Handler paths start from under the first element, so modify the dest path for that.
                        String sDestPathChild = sDestPath;
                        int iPos = sDestPathChild.indexOf('/');
                        
                        if (iPos > 0 && iPos < sDestPathChild.length() - 1) {
                            sDestPathChild = sDestPathChild.substring(iPos + 1);
                        } else {
                            sDestPathChild = "";
                        }
                        
                        StringBuffer sbTmp = new StringBuffer(sDestPathChild);
                        
                        if (sbTmp.length() > 0 && sbTmp.charAt(sbTmp.length() -1) != '/') {
                            sbTmp.append('/');
                        }
                        
                        OMElement oeTmp = oeBaseTemplatesSection.getFirstElement();
                        
                        if (oeTmp != null) {
                            sbTmp.append(oeTmp.getLocalName()).append('/');
                        }
                        
                        sbTmp.append(oaXmlPath.getAttributeValue());
                        
                        oaXmlPath.setAttributeValue(sbTmp.toString());
                    }
                }
                
                if (sRewriteFrom != null && sRewriteTo != null) {
                    String sBeanId = oeHandler.getAttributeValue(new QName("beanid"));
                    
                    if (sRewriteFrom.equals(sBeanId)) {
                        AxiomUtils.setAttribute(oeHandler, "beanid", sRewriteTo);
                    }
                }                    
            }
            
            mImportedHandlers.put(sTemplateId, oeBaseHandlersSection);
            
            // Merge the XML template.
            if (oeBaseTemplatesSection != null && oeBaseTemplatesSection.getFirstElement() != null) {
                OMElement oeCurrent = oeTemplateSection;

                // If dest path is set, use that as the root for the template, otherwise
                // just add the base template to the template section.
                if (sDestPath != null && sDestPath.length() > 0) {
                    // Add the imported template XML into the templates section and create the necessary root elements.
                    String[] saPath = sDestPath.split("/");

                    if (saPath.length == 0) {
                        throw new BindingException("Imported template destination path cannot be empty.");
                    }
                    
                    for (int i = 0; i < saPath.length; i++)
                    {
                        String sElem = saPath[i];
                        
                        if (sElem.equals(".")) {
                            // Means the current.
                            continue;
                        }
                        
                        OMElement oeExisting = AxiomUtils.getChildElementWithLocalName(oeCurrent, sElem);

                        if (oeExisting != null) {
                            oeCurrent = oeExisting;
                        } else {
                            OMNamespace onCurrentNamespace = oeCurrent.getNamespace();
                            QName qnElemName = AxiomUtils.createQName(sElem, null, onCurrentNamespace != null ? onCurrentNamespace.getNamespaceURI() : null);

                            oeCurrent = oeCurrent.getOMFactory().createOMElement(qnElemName, oeCurrent);
                        }
                    }
                }
                 
                OMElement oeTmp = oeBaseTemplatesSection.getFirstElement();
                    
                oeTmp.detach();
                oeCurrent.addChild(oeTmp);
            }
            
            // Merge the beans.
            if (oeBaseBeansSection != null) {
                for (Iterator<?> iBeanIter = oeBaseBeansSection.getChildrenWithName(new QName("bean")); iBeanIter.hasNext(); ) {
                    OMElement oeBean = (OMElement) iBeanIter.next();
                    
                    if (sRewriteFrom != null && sRewriteTo != null) {
                        String sBeanId = oeBean.getAttributeValue(new QName("beanid"));
                        
                        if (sRewriteFrom.equals(sBeanId)) {
                            AxiomUtils.setAttribute(oeBean, "beanid", sRewriteTo);
                        }
                    }                       
                    
                    oeBean.detach();
                    oeBeansSection.addChild(oeBean);
                }
            }
        }    
        
        // Add the imported handlers to the place where we have import-handlers statement.
        for (Iterator<?> iIter = oeHandlerSection.getChildElements(); iIter.hasNext(); ) {
            OMElement oeHandler = (OMElement) iIter.next();      
         
            if (! "import-handlers".equals(oeHandler.getLocalName())) {
                continue;
            }
            
            String sTemplateId = oeHandler.getAttributeValue(new QName("template-id"));
            
            if (sTemplateId == null) {
                throw new BindingException("Attribute 'template-id' missing from 'import-handlers'");
            }
            
            OMElement oeImportedHandlersRoot = mImportedHandlers.get(sTemplateId);
            
            if (oeImportedHandlersRoot == null) {
                throw new BindingException("import-handlers: No handlers found with template ID " + sTemplateId);
            }
            
            for (Iterator<?> iHandlersIter = oeImportedHandlersRoot.getChildElements(); iHandlersIter.hasNext(); ) {
                OMElement oeImportedHandler = (OMElement) iHandlersIter.next();
                
                oeImportedHandler.detach();
                oeHandler.insertSiblingBefore(oeImportedHandler);              
            }
            
            oeHandler.discard();
        }
    }
    
    private void removeHandler(OMElement oeHandler) throws BindingException
    {
        String sPath = oeHandler.getAttributeValue(new QName("xmlpath"));
        IBindingHandler bhHandler = findHandler(sPath, true);
        
        if (bhHandler == null) {
            throw new BindingException("remove-handler: No handler found with path " + sPath);
        }
        
        if (bhHandler.getParent() != null) {
            bhHandler.getParent().removeChildHandler(bhHandler);
        } else {
            bhBaseHandler = null;
        }
    }

    private void parseHandler(OMElement oeHandler) throws BindingException
    {
        String sTmp;
        String sType = oeHandler.getAttributeValue(new QName("type"));
        String sPath = oeHandler.getAttributeValue(new QName("xmlpath"));
        boolean bKeepChildren = false;
        boolean bReplaceWithNew = true;
        
        if (sType == null) {
            throw new BindingException("Handler attribute 'type' is missing.");
        }

        if (sPath == null) {
            throw new BindingException("Handler attribute 'xmlpath' is missing.");
        }
        
        if ((sTmp = oeHandler.getAttributeValue(new QName("keepchildren"))) != null) {
            bKeepChildren = (sTmp.equals("true"));
        }
        
        IBindingHandler bhPrevHandler = findHandler(sPath, true);
        IBindingHandler bhNewHandler = null;
        
        if (sType.equals("property")) {
            String sPropName = oeHandler.getAttributeValue(new QName("propname"));
            
            if (sPropName == null) {
                throw new BindingException("Property handler attribute 'propname' is missing.");
            }
            
            bhNewHandler = new PropertyHandlerBase(sPropName, bhPrevHandler.isAttribute());
        } else if (sType.equals("bean")) {
            String sBeanId = oeHandler.getAttributeValue(new QName("beanid"));
            String sFieldName = oeHandler.getAttributeValue(new QName("fieldname"));
            String sXmlStructureField = oeHandler.getAttributeValue(new QName("xmlfield"));
            String sFieldBeanId = oeHandler.getAttributeValue(new QName("fieldbean"));
            boolean bIsXmlStructureField = false;
            Class<?> cBeanClass;
            
            if (sBeanId == null) {
                throw new BindingException("Bean ID not set for bean property handler.");
            }            
            
            cBeanClass = mBeanClassMap.get(sBeanId);
            
            if (cBeanClass == null) {
                throw new BindingException("Bean class not defined with bean ID " + sBeanId);
            }
            
            if (sFieldName == null) {
                throw new BindingException("Bean handler attribute 'fieldname' is missing.");
            }
            
            if (sXmlStructureField != null && sXmlStructureField.equals("true")) {
                bIsXmlStructureField = true;
            }
            
            BeanHandlerBase bhHandler;
            
            try
            {
                bhHandler = new BeanHandlerBase(sBeanId, sFieldName, cBeanClass, bhPrevHandler.isAttribute(), bIsXmlStructureField);
            }
            catch (BindingException e)
            {
                throw new BindingException("Bean handler creation failed.", e);
            }
            
            if (sFieldBeanId != null) {
                bhHandler.setFieldBeanId(sFieldBeanId);
            }
            
            bhNewHandler = bhHandler;
        } else if (sType.equals("coboctuple")) {            
            bhNewHandler = new CobocTupleHandler(bhPrevHandler.getXmlName());
        } else if (sType.equals("xml")) {
            bReplaceWithNew = false;
            bhNewHandler = bhPrevHandler;
        } else {
            throw new BindingException("Invalid handler type '" + sType + "'");
        }
        
        if (bKeepChildren && bReplaceWithNew) {
            List<IBindingHandler> lChildren = bhPrevHandler.getChildren();
            
            for (IBindingHandler bhChild : lChildren)
            {
                bhNewHandler.addChildHandler(bhChild);
            }
        }
        
        String sParameters = oeHandler.getAttributeValue(new QName("params"));
        
        if (sParameters != null) {
            parseHandlerParameters(bhNewHandler, sParameters);
        }
        
        if (bReplaceWithNew) {
            replaceHandler(bhPrevHandler, bhNewHandler);
        }
    }

    /**
     * Parses handler parameters.
     * @param bhHandler Handler.
     * @param sParameters Handler parameter list string.
     */
    private void parseHandlerParameters(IBindingHandler bhHandler, String sParameters) throws BindingException
    {
        String[] saParams = sParameters.split(",");
        
        if (saParams == null || saParams.length == 0) {
            return;
        }
        
        for (String sParam : saParams)
        {
            int iPos = sParam.indexOf("=");
            String sName = iPos >= 0 ? sParam.substring(0, iPos).trim() : sParam;
            String sValue = iPos >= 0 && iPos < sParam.length() ? sParam.substring(iPos + 1) : "";
            EBindingHandlerParameter bpParam = EBindingHandlerParameter.findByConfigurationName(sName);
            
            if (bpParam == null) {
                throw new BindingException("Invalid binding handler parameter '" + sName + "'.");
                
            }
            
            bhHandler.setParameter(bpParam, sValue);
        }
    }

    public void parseConfiguration(String sPath, XmlLoader xlLoader) throws BindingException {
        OMElement oeRoot = xlLoader.load(sPath);
        XmlLoader xlChildLoader = xlLoader.getChildLoader(sPath);
        
        // Check if this template extends another one.
        String sExtends = oeRoot.getAttributeValue(new QName("extends"));
        
        if (sExtends != null) {
            OMElement oeBaseRoot = xlChildLoader.load(sExtends);
            XmlLoader xlBaseChildLoader = xlChildLoader.getChildLoader(sExtends);
            
            appendBaseTemplate(oeRoot, oeBaseRoot, xlBaseChildLoader);
        }
        
        try
        {
            parseConfigurationFromXml(oeRoot, xlChildLoader);
        }
        catch (BindingException e)
        {
            throw new BindingException(sPath + ": " + e.getMessage(), e);
        }
    }

    /**
     * Appends the given template to the base template loaded from the given file. Bean section
     * is not added as it probably is always different.
     * @param oeTemplate Template that extends another one.
     * @param fBaseTemplateFile Name of the base template file.
     * @throws BindingException Thrown if the operation failed.
     */
    private void appendBaseTemplate(OMElement oeTemplate, OMElement oeBaseRoot, XmlLoader xlLoader) throws BindingException {
        // Check if the base template extends another one.
        String sBaseExtends = oeBaseRoot.getAttributeValue(new QName("extends"));
        
        if (sBaseExtends != null) {
            OMElement oeBaseBaseRoot = xlLoader.load(sBaseExtends);
            XmlLoader xlBaseChildLoader = xlLoader.getChildLoader(sBaseExtends);
            
            appendBaseTemplate(oeBaseRoot, oeBaseBaseRoot, xlBaseChildLoader);
        }
        
        OMElement oeBaseHandlerSection = oeBaseRoot.getFirstChildWithName(new QName("handlers"));
        OMElement oeTemplateHandlerSection = oeTemplate.getFirstChildWithName(new QName("handlers"));
        
        if (oeBaseHandlerSection != null) {
            if (oeTemplateHandlerSection != null) {
                for (Iterator<?> iIter = oeBaseHandlerSection.getChildElements(); iIter.hasNext(); ) {
                    oeTemplateHandlerSection.addChild(((OMElement) iIter.next()).cloneOMElement());
                }
            } else {
                oeTemplate.addChild(oeBaseHandlerSection.cloneOMElement());
            }
        }
        
        OMElement oeBaseTemplateSection = oeBaseRoot.getFirstChildWithName(new QName("template"));
        OMElement oeTemplateTemplateSection = oeTemplate.getFirstChildWithName(new QName("template"));
        
        if (oeBaseTemplateSection != null) {
            if (oeTemplateTemplateSection != null) {
                for (Iterator<?> iIter = oeBaseTemplateSection.getChildElements(); iIter.hasNext(); ) {
                    oeTemplateTemplateSection.addChild(((OMElement) iIter.next()).cloneOMElement());
                }
            } else {
                oeTemplate.addChild(oeBaseTemplateSection.cloneOMElement());
            }
        }     
    }

    /**
     * @see com.cordys.coe.bf.databind.IBindingTemplate#getBeanClass(java.lang.String)
     */
    public Class<?> getBeanClass(String sBeanId)
    {
        return mBeanClassMap.get(sBeanId);
    }


    /**
     * @see com.cordys.coe.bf.databind.IBindingTemplate#getTemplateId()
     */
    public String getTemplateId()
    {
        return sTemplateId;
    }


    /**
     * @see com.cordys.coe.bf.databind.IBindingTemplate#setTemplateId(java.lang.String)
     */
    public void setTemplateId(String sId)
    {
        sTemplateId = sId;
    }



}
