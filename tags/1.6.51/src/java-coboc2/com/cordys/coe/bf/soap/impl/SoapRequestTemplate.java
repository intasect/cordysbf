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
package com.cordys.coe.bf.soap.impl;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.databind.IBindingHandler;
import com.cordys.coe.bf.databind.IBindingTemplate;
import com.cordys.coe.bf.databind.XmlLoader;
import com.cordys.coe.bf.databind.impl.BindingTemplateImpl;
import com.cordys.coe.bf.soap.ISoapConstants;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;

/**
 * Implementation of the SOAP method template interface.
 *
 * @author mpoyhone
 */
public class SoapRequestTemplate implements ISoapRequestTemplate
{
    IBindingTemplate btTemplate;
    QName[] qnaMethodNames;
    IBindingHandler[] bhaSoapMethodHandlers;
    
    public SoapRequestTemplate(String sPath, XmlLoader xlLoader) throws BindingException
    {
        btTemplate = new BindingTemplateImpl();
        btTemplate.parseConfiguration(sPath, xlLoader);
        
        findSoapMethodHandlers();
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequestTemplate#getTemplateId()
     */
    public String getTemplateId()
    {
        return btTemplate.getTemplateId();
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequestTemplate#getMethodName()
     */
    public QName getMethodName()
    {
        return qnaMethodNames.length > 0 ? qnaMethodNames[0] : null;
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequestTemplate#getBindingTemplate()
     */
    public IBindingTemplate getBindingTemplate()
    {
        return btTemplate;
    }
    
    private void findSoapMethodHandlers(IBindingHandler bhCurrent, List<IBindingHandler> lResList) {
        if (ISoapConstants.SOAP_NAMESPACE.equals(bhCurrent.getXmlName().getNamespaceURI())) {
            // This is SOAP:Envelope or SOAP:Body, so get its children. 
            List<IBindingHandler> lChildren = bhCurrent.getChildren();
            
            for (IBindingHandler bhChildHandler : lChildren)
            {
                findSoapMethodHandlers(bhChildHandler, lResList);
            }
        } else {
            lResList.add(bhCurrent);
        }
    }

    private void findSoapMethodHandlers() {
        List<IBindingHandler> lMethodHandlerList = new LinkedList<IBindingHandler>();
        IBindingHandler bhHandler = btTemplate.getRootHandler();
        
        findSoapMethodHandlers(bhHandler, lMethodHandlerList);
        
        bhaSoapMethodHandlers = (IBindingHandler[]) lMethodHandlerList.toArray(new IBindingHandler[lMethodHandlerList.size()]);
        qnaMethodNames = new QName[bhaSoapMethodHandlers.length];
        
        int i = 0;
        
        for (IBindingHandler bhTmp : bhaSoapMethodHandlers)
        {
            QName qnName = bhTmp.getXmlName();

            qnaMethodNames[i++] = qnName;
        }
    }

    /**
     * @see com.cordys.coe.bf.soap.ISoapRequestTemplate#getSoapMethodHandlers()
     */
    public IBindingHandler[] getSoapMethodHandlers()
    {
        return bhaSoapMethodHandlers;
    }
}
