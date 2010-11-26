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
package com.cordys.coe.bf.content.types.ldap;

import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.utils.XmlStructure;


/**
 * Class containing method set method information.
 *
 * @author mpoyhone
 */
public class Method extends LdapContentBase
{
    private String sName;
    private String sReturnType;
    private XmlStructure xsImplementation;
    private XmlStructure xsWsdl;
    
    
    /**
     * Constructor for Method
     */
    public Method()
    {
        super(EContentType.METHOD);
    }


    /**
     * Returns the implementation.
     *
     * @return Returns the implementation.
     */
    public XmlStructure getImplementation()
    {
        return xsImplementation;
    }


    /**
     * The implementation to set.
     *
     * @param aImplementation The implementation to set.
     */
    public void setImplementation(XmlStructure aImplementation)
    {
        xsImplementation = aImplementation;
    }


    /**
     * Returns the name.
     *
     * @return Returns the name.
     */
    public String getName()
    {
        return sName;
    }


    /**
     * The name to set.
     *
     * @param aName The name to set.
     */
    public void setName(String aName)
    {
        sName = aName;
    }


    /**
     * Returns the returnType.
     *
     * @return Returns the returnType.
     */
    public String getReturnType()
    {
        return sReturnType;
    }


    /**
     * The returnType to set.
     *
     * @param aReturnType The returnType to set.
     */
    public void setReturnType(String aReturnType)
    {
        sReturnType = aReturnType;
    }


    /**
     * Returns the wsdl.
     *
     * @return Returns the wsdl.
     */
    public XmlStructure getWsdl()
    {
        return xsWsdl;
    }


    /**
     * The wsdl to set.
     *
     * @param aWsdl The wsdl to set.
     */
    public void setWsdl(XmlStructure aWsdl)
    {
        xsWsdl = aWsdl;
    }
}
