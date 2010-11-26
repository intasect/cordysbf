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

import java.util.ArrayList;

import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.types.EContentType;


/**
 * Class containing method set information.
 *
 * @author mpoyhone
 */
public class MethodSet extends LdapContentBase
{
    private String sName;
    private String sNamespace;
    private String sImplementationClass;
    
    /**
     * Constructor for MethodSet
     */
    public MethodSet() {
        super(EContentType.METHOD_SET);
        lSubContentList = new ArrayList<IContentHandle>(5);
    }
    
    /**
     * Returns the implementationClass.
     *
     * @return Returns the implementationClass.
     */
    public String getImplementationClass()
    {
        return sImplementationClass;
    }

    /**
     * The implementationClass to set.
     *
     * @param aImplementationClass The implementationClass to set.
     */
    public void setImplementationClass(String aImplementationClass)
    {
        sImplementationClass = aImplementationClass;
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
     * Returns the namespace.
     *
     * @return Returns the namespace.
     */
    public String getNamespace()
    {
        return sNamespace;
    }

    /**
     * The namespace to set.
     *
     * @param aNamespace The namespace to set.
     */
    public void setNamespace(String aNamespace)
    {
        sNamespace = aNamespace;
    }
    

}
