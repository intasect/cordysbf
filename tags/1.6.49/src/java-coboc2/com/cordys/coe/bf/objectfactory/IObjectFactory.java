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

import java.lang.reflect.Constructor;

import com.cordys.coe.bf.exception.BFException;

/**
 * Generic interface for object factory configuration.
 *
 * @author mpoyhone
 */
public interface IObjectFactory
{
    /**
     * Sets an singleton object that will be returned by the newInstance method.
     * @param cClass Object class.
     */
    public void setSingleton(Object oObject);
    /**
     * Sets the class for objects that will be created by the newInstance method.
     * @param cClass Object class.
     */
    public void setObjectClass(Class<?> cClass);
    /**
     * Sets the class for objects that will be created by the newInstance method.
     * The given constructor will be used by the createInstance(...) method.
     * @param cClass Object class.
     * @param cConstructor
     */
    public void setObjectClass(Class<?> cClass, Constructor<?> cConstructor);    
    /**
     * Creates a new instance of the configured class. The arguments must
     * match the configured constructor. If no arguments are given the no-args constructor
     * will be called.
     * @param oaArgs Constructor arguments.
     * @return Created object instance.
     * @throws BFException Thrown if the instantiation failed.
     */
    public Object createInstance(Object ... oaArgs) throws BFException;    
}
