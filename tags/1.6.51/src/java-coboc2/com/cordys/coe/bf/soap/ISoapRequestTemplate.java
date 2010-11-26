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
package com.cordys.coe.bf.soap;

import javax.xml.namespace.QName;

import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.IBindingHandler;
import com.cordys.coe.bf.databind.IBindingTemplate;
import com.cordys.coe.bf.databind.XmlLoader;
import com.cordys.coe.bf.soap.impl.SoapRequestTemplate;

/**
 * Interface for a SOAP request template. This interface defines a method for getting
 * the IBindingTemplate as well as getting the SOAP method QName. 
 *
 * @author mpoyhone
 */
public interface ISoapRequestTemplate
{
    public static class Factory {
        public static ISoapRequestTemplate newInstance(String sPath, XmlLoader xlLoader) throws BindingException {
            return new SoapRequestTemplate(sPath, xlLoader);
        }
    }
    
    /**
     * Returns the configured binding template ID for this template. This is
     * usually read from the configuration file.
     * @return Binding template ID.
     */
    public String getTemplateId();
    
    /**
     * Returns the SOAP reqeust method QName.
     * @return SOAP request QName.
     */
    public QName getMethodName();
    
    /**
     * Returns the underlying binding template for this method.
     * @return Binding template.
     */
    public IBindingTemplate getBindingTemplate();
    
    /**
     * Returns the an array of all handler for SOAP method elements. If the template specifies
     * a SOAP:Body that can contain multiple methods.  
     * @return All SOAP method element handlers configured for this template.
     */
    public IBindingHandler[] getSoapMethodHandlers();
}
