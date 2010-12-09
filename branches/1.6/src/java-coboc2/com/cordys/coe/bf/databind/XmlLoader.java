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
package com.cordys.coe.bf.databind;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;

import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.FileUtils;


/**
 * A simple utility class that can load XML from a File handle or from a file
 * in the classpath.
 *
 * @author mpoyhone
 */
public class XmlLoader
{
    private File fRootFolder;
    private Class<?> cRefClass;
    private String sRootPackage;
    
    private XmlLoader(File fRootFolder) {
        this.fRootFolder = fRootFolder;
    }
    
    private XmlLoader(Class<?> cRefClass, String sRootPackage) {
        this.cRefClass = cRefClass;
        this.sRootPackage = sRootPackage;
    }
    
    public static XmlLoader createFromFolder(File fFolder) {
        return new XmlLoader(fFolder);
    }
    
    public static XmlLoader createFromClasspathFile(Class<?> cRefClass, String sFilePath) {
        String sPackage = sFilePath;
        int iPos = sPackage.lastIndexOf('/');
        
        if (iPos >= 0 && iPos < sFilePath.length() - 1) {
            sPackage = sPackage.substring(0, iPos);
        }
        
        return new XmlLoader(cRefClass, sPackage);
    }
    
    public static XmlLoader createFromClasspathPackage(Class<?> cRefClass, String sPackage) {
        return new XmlLoader(cRefClass, sPackage);
    }
    
    public XmlLoader getChildLoader(String sFileName) {
        if (cRefClass != null) {
            String sChildRootFile = createRelativeResourceName(sRootPackage, sFileName);
            
            return createFromClasspathFile(cRefClass, sChildRootFile);
        } else {
            File fChildRoot = new File(fRootFolder, sFileName);
            
            if (fChildRoot.getParent() != null) {
                fChildRoot = fChildRoot.getParentFile();
            }
            
            try
            {
                fChildRoot = fChildRoot.getCanonicalFile();
            }
            catch (IOException ignored)
            {
            }
            
            return new XmlLoader(fChildRoot);
        }
    }
    
    public OMElement load(String sFileName) throws BindingException {
        try
        {
            if (cRefClass != null) {
                return loadFromClasspath(sFileName);
            } else {
                return loadFromFile(sFileName);
            }
        }
        catch (BindingException e)
        {
            throw e;
        }
        catch (Throwable t) {
            throw new BindingException("Exception while reading XML file " + sFileName, t);
        }
    }
    
    private OMElement loadFromFile(String sFileName) throws BindingException
    {
        File fFile = new File(fRootFolder, sFileName);
        OMElement oeRoot;
        
        try
        {
            oeRoot = AxiomUtils.loadFile(fFile.getAbsolutePath());
        }
        catch (FileNotFoundException e)
        {
            throw new BindingException("File not found: " + fFile);
        }
        catch (XMLStreamException e)
        {
            throw new BindingException("Unable to parse the XML file: " + fFile, e);
        }
        catch (FactoryConfigurationError e)
        {
            throw new BindingException("Unable to create an XML parser for the XML file: " + fFile, e);        
        }
        
        return oeRoot;
    }

    private OMElement loadFromClasspath(String sPath) throws BindingException {
        OMElement oeRoot;
        InputStream isInput = null;
        String sBaseFile = createRelativeResourceName(sRootPackage, sPath);
        
        try
        {
            isInput = cRefClass.getResourceAsStream(sBaseFile);
            
            if (isInput == null) {
                throw new BindingException("File not found: " + sBaseFile);
            }
            
            oeRoot = AxiomUtils.loadStream(isInput);
        }
        catch (XMLStreamException e)
        {
            throw new BindingException("Unable to parse the XML file: " + sBaseFile, e);
        }
        catch (FactoryConfigurationError e)
        {
            throw new BindingException("Unable to create an XML parser for the file: " + sBaseFile, e);        
        }
        finally {
            FileUtils.closeStream(isInput);
        }
        
        return oeRoot;
    }
    
    private static String createRelativeResourceName(String sBaseName, String sChildName) {
        if (sBaseName != null && (sBaseName.equals("") || sBaseName.equals("."))) {
            return sChildName;
        }
        
        File fChild = new File(new File(sBaseName), sChildName);
        File fRoot = new File(".");
        
        try
        {
            fRoot = fRoot.getCanonicalFile();
            fChild = fChild.getCanonicalFile();
        }
        catch (IOException ignored)
        {
            return null;
        }
        
        String sRes = fChild.getPath().replace('\\', '/');
        
        return sRes.substring(fRoot.getPath().length() + 1);
    }
}
