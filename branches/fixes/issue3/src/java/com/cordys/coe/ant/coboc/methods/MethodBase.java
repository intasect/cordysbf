/**
 * Copyright 2005 Cordys R&D B.V. 
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
package com.cordys.coe.ant.coboc.methods;


/**
 * A SOAP method base class. Handles the SOAP node lookup.
 *
 * @author mpoyhone
 */
public abstract class MethodBase
{
    /**
     * Contains the receiving SOAP node DN.
     */
    protected String sReceiver = null;

    /**
     * Constructs a new MethodBase object.
     */
    public MethodBase()
    {
    }

    /**
     * Returns the method name.
     *
     * @return The method name.
     */
    public abstract String getMethodName();

    /**
     * Returns the method namespace.
     *
     * @return The method namespace.
     */
    public abstract String getMethodNameSpace();

    /**
     * Returns the implementing SOAP node DN.
     *
     * @return The implementing SOAP node DN.
     */
    public String getReceiverDN()
    {
        return sReceiver;
    }
}
