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
package com.cordys.coe.bf.content.coboc.impl.bcp42_c1;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContentReadMethod;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentWriteMethod;
import com.cordys.coe.bf.content.types.EContentCategory;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.IBindingConstants;
import com.cordys.coe.bf.databind.ObjectConverter;
import com.cordys.coe.bf.databind.XmlLoader;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;
import com.cordys.coe.bf.templates.ConfigRoot;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.FileUtils;

/**
 * An utility class that loads content source templates from an XML file
 * and add them to the content source. 
 *
 * @author mpoyhone
 */
public class ContentSourceConfigurator
{
    /**
     * Loads the configuration from the given file that is located with
     * the given class's classloader.
     * @param bcContext 
     * @param cRefClass
     * @param sFilePath
     * @throws BFException Thrown if the loading failed.
     * @throws BindingException 
     */
    public static void loadFromClasspath(IContentSource csDest, BFContext bcContext) throws BFException, BindingException {
        // Load the main property file and get the content source configuration file path from there.
        Properties pMainProps = new Properties();
        InputStream isInput = null;
        
        try {
            isInput = ConfigRoot.class.getResourceAsStream("content-source.properties");
            
            if (isInput == null) {
                throw new BFException("Content source configuration file 'content-source.properties' not found.");
            }
            
            pMainProps.load(isInput);
        }
        catch (IOException e) {
            throw new BFException("Unable to load the main property file");
        }
        finally {
            FileUtils.closeStream(isInput);
            isInput = null;
        }
        
        String propNameBase = "contentsource." + csDest.getType().toString() + ".config.";
        Map<String, Collection<String>> fileToVersionMap = new HashMap<String, Collection<String>>();
        
        for (Iterator<Object> iter = pMainProps.keySet().iterator(); iter.hasNext();)
        {
            String name = (String) iter.next();
            String value = pMainProps.getProperty(name);
            
            if (! name.startsWith(propNameBase) || name.length() <= propNameBase.length()) {
                continue;
            }
            
            value = value.trim();
            
            String version = name.substring(propNameBase.length()).trim();
            Collection<String> versionList = fileToVersionMap.get(value);
            
            if (versionList == null) {
                versionList = new ArrayList<String>(10);
                fileToVersionMap.put(value, versionList);
            }
            
            versionList.add(version);
        }

        if (fileToVersionMap.size() == 0) {
            throw new BFException("Configuration file location not found for content source " + csDest.getType().toString());
        }
        
        // Load and parse the content source configuration.
        for (Map.Entry<String, Collection<String>> entry : fileToVersionMap.entrySet())
        {
            XmlLoader xlLoader = XmlLoader.createFromClasspathPackage(ConfigRoot.class, "");
            
            loadConfig(csDest, entry.getValue(), xlLoader, entry.getKey());
        }
        
    }
    
    @SuppressWarnings("unchecked")
    protected static void loadConfig(IContentSource csDest, Collection<String> cVersions, XmlLoader xlLoader, String sFileName) throws BFException, BindingException {
        
        OMElement oeRoot = xlLoader.load(sFileName);
        XmlLoader xlChildLoader = xlLoader.getChildLoader(sFileName);
        
        // First see if we have an import template.
        String sExtendsTemplate = oeRoot.getAttributeValue(new QName("extends"));
        
        if (sExtendsTemplate != null && sExtendsTemplate.length() > 0)
        {
            loadConfig(csDest, cVersions, xlChildLoader, sExtendsTemplate);
        }
        
        OMElement oeSoapTemplatesSection = oeRoot.getFirstChildWithName(new QName("soaprequest-templates"));
        
        if (oeSoapTemplatesSection != null) {
            for (Iterator<?> iIter = oeSoapTemplatesSection.getChildrenWithName(new QName("template")); iIter.hasNext(); ) {
                OMElement oeLocation = (OMElement) iIter.next();
                String sPath = oeLocation.getAttributeValue(new QName("path"));
                ISoapRequestTemplate srtTemplate;
                String sTemplateId;
                
                srtTemplate = ISoapRequestTemplate.Factory.newInstance(sPath, xlChildLoader);
                sTemplateId = srtTemplate.getTemplateId();

                if (sTemplateId == null) {
                    throw new BFException("SOAP request template ID not found in file: " + sFileName);
                }
                
                for (String version : cVersions)
                {
                    csDest.registerSoapMethodTemplate(sTemplateId, version, srtTemplate);
                }
            }
        }
        
        OMElement oeBindingTemplatesSection = oeRoot.getFirstChildWithName(new QName("binding-templates"));
        
        if (oeBindingTemplatesSection != null) {
            for (Iterator<?> iIter = oeBindingTemplatesSection.getChildrenWithName(new QName("template")); iIter.hasNext(); ) {
                OMElement oeLocation = (OMElement) iIter.next();
                String sPath = oeLocation.getAttributeValue(new QName("path"));
                String sContentType = oeLocation.getAttributeValue(new QName("content-type"));
                String sSubType = oeLocation.getAttributeValue(new QName("sub-type"));
                ObjectConverter ocConverter;
                EContentType ctType = EContentType.valueOf(sContentType);
                
                if (ctType == null) {
                    throw new BFException(sFileName + ": Invalid content type: " + sContentType);
                }
                
                ocConverter = new ObjectConverter(IBindingConstants.BEANNAME_DEFAULT, sPath, xlChildLoader);
                
                for (String version : cVersions)
                {                
                    csDest.registerConversionTemplate(ctType, sSubType, version, ocConverter);
                }
            }
        }
        
        OMElement oeReadMethodsSection = oeRoot.getFirstChildWithName(new QName("read-methods"));
        
        if (oeReadMethodsSection != null) {
            for (Iterator<?> iIter = oeReadMethodsSection.getChildrenWithName(new QName("method")); iIter.hasNext(); ) {
                OMElement oeLocation = (OMElement) iIter.next();
                String sContentTypes = AxiomUtils.getNodeText(oeLocation.getFirstChildWithName(new QName("content-types")));
                String sClassName= AxiomUtils.getNodeText(oeLocation.getFirstChildWithName(new QName("class")));
                String[] saContentTypes = sContentTypes.split(",");
                Class<IContentReadMethod> cClass;
                
                try
                {
                    cClass = (Class<IContentReadMethod>) Class.forName(sClassName);
                }
                catch (ClassNotFoundException e)
                {
                    throw new BFException("Unable to load method class: " + sClassName);
                }
                
                EContentType[] contentTypes = fetchMethodContentTypes(saContentTypes, sFileName);
                
                for (String version : cVersions)
                {
                    csDest.registerContentReadMethod(version, contentTypes, cClass);
                }
            }
        }        
        
        OMElement oeWriteMethodsSection = oeRoot.getFirstChildWithName(new QName("write-methods"));
        
        if (oeWriteMethodsSection != null) {
            for (Iterator<?> iIter = oeWriteMethodsSection.getChildrenWithName(new QName("method")); iIter.hasNext(); ) {
                OMElement oeLocation = (OMElement) iIter.next();
                String sContentTypes = AxiomUtils.getNodeText(oeLocation.getFirstChildWithName(new QName("content-types")));
                String sClassName= AxiomUtils.getNodeText(oeLocation.getFirstChildWithName(new QName("class")));
                String[] saContentTypes = sContentTypes.split(",");
                Class<IContentWriteMethod> cClass;
                
                try
                {
                    cClass = (Class<IContentWriteMethod>) Class.forName(sClassName);
                }
                catch (ClassNotFoundException e)
                {
                    throw new BFException("Unable to load write method class: " + sClassName);
                }
                
                EContentType[] contentTypes = fetchMethodContentTypes(saContentTypes, sFileName);
                
                for (String version : cVersions)
                {
                    csDest.registerContentWriteMethod(version, contentTypes, cClass);
                }
            }
        }            
    }
    
    private static EContentType[] fetchMethodContentTypes(String[] saContentTypes, String sFileName) throws BFException
    {
        List<EContentType> contentTypes = new ArrayList<EContentType>(saContentTypes.length);
        
        for (String s : saContentTypes)
        {   
            // First try to see if this is a category.
            try {
                EContentCategory cat = EContentCategory.valueOf(s);
                
                if (cat != null) {
                    for (EContentType ct : cat.getContentTypes())
                    {
                        contentTypes.add(ct);
                    }
                    
                    continue;
                }
            }
            catch (Exception ignore) {
            }
            
            // Check if this is a value content type.
            EContentType ctType = EContentType.valueOf(s);
            
            if (ctType == null) {
                throw new BFException(sFileName + ": Invalid content type: " + s);
            }
            
            contentTypes.add(ctType);
        }   
        
        return (EContentType[]) contentTypes.toArray(new EContentType[contentTypes.size()]);
    }
}
