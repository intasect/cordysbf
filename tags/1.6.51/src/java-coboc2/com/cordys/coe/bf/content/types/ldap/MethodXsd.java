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
public class MethodXsd extends LdapContentBase
{
    private String sName;
    private XmlStructure xsXsd;
    
    
    /**
     * Constructor for Method
     */
    public MethodXsd()
    {
        super(EContentType.METHOD_XSD);
    }


    /**
     * Returns the XSD.
     *
     * @return Returns the XSD.
     */
    public XmlStructure getXsd()
    {
        return xsXsd;
    }


    /**
     * The XSD to set.
     *
     * @param XSD The implementation to set.
     */
    public void setXsd(XmlStructure aXsd)
    {
        xsXsd = aXsd;
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
  
}
