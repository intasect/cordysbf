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
package com.cordys.coe.bf.objectfactory;

import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.impl.SoapRequest_JdkHttpUrlConnection;

/**
 * Factory class for ISoapRequest implementations.
 *
 * @author mpoyhone
 */
public class SoapRequestFactory extends ObjectFactoryBase
{
    private static SoapRequestFactory factoryInstance = new SoapRequestFactory();
    
    protected SoapRequestFactory() {
        super(SoapRequest_JdkHttpUrlConnection.class);
    }
    
    public static SoapRequestFactory setFactoryInstance(SoapRequestFactory srfNewInstance) {
        SoapRequestFactory srfOld  = factoryInstance;
        
        factoryInstance = srfNewInstance;
        
        return srfOld;
    }    
    
    public static SoapRequestFactory getFactoryInstance() {
        return factoryInstance;
    }
    
    /**
     * @see com.cordys.coe.bf.objectfactory.IObjectFactory#createInstance()
     */
    public ISoapRequest createInstance() throws BFException {
        return (ISoapRequest) super.createInstance();
    }
}
