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
import java.lang.reflect.InvocationTargetException;

import com.cordys.coe.bf.exception.BFException;

/**
 * A base class for all object factories that implement the IObjectFactory interface.
 *
 * @author mpoyhone
 */
public class ObjectFactoryBase implements IObjectFactory
{
    /**
     * Class of the objects the will be created with the newInstance method.
     */
    protected Class<?> cObjectClass;
    /**
     * Constructor that will be called for the object that will be created.
     */
    protected Constructor<?> cObjectConstrcutor;
    /**
     * Singleton object that will be returned by newInstance method. If this is set
     * the cObjectClass will be ignored.
     */
    protected Object oSingletonObject;

    /**
     * Constructor for ObjectFactoryBase
     * @param cClass Object class.
     */
    protected ObjectFactoryBase(Class<?> cClass) {
        setObjectClass(cClass);
    }
    
    /**
     * Constructor for ObjectFactoryBase
     * @param cClass Object class.
     */
    protected ObjectFactoryBase(Class<?> cClass, Constructor<?> cConstructor) {
        setObjectClass(cClass, cConstructor);
    }    
    
    /**
     * Constructor for ObjectFactoryBase
     * @param oSingleton Singleton object.
     */
    protected ObjectFactoryBase(Object oSingleton) {
        this.oSingletonObject = oSingleton;
    }

    /**
     * @see com.cordys.coe.bf.objectfactory.IObjectFactory#setObjectClass(java.lang.Class)
     */
    public void setObjectClass(Class<?> cClass)
    {
        this.cObjectClass = cClass;
        this.cObjectConstrcutor = null;
    }
    
    /**
     * @see com.cordys.coe.bf.objectfactory.IObjectFactory#setObjectClass(java.lang.Class, java.lang.reflect.Constructor)
     */
    public void setObjectClass(Class<?> cClass, Constructor<?> cConstructor)
    {
        this.cObjectClass = cClass;
        this.cObjectConstrcutor = cConstructor;
    }    
    
    /**
     * @see com.cordys.coe.bf.objectfactory.IObjectFactory#setSingleton(java.lang.Object)
     */
    public void setSingleton(Object oObject)
    {
        this.oSingletonObject = oObject;
    }

    /**
     * @see com.cordys.coe.bf.objectfactory.IObjectFactory#createInstance(java.lang.Object[])
     */
    public Object createInstance(Object ... oaArgs) throws BFException
    {
        if (oSingletonObject != null) {
            return oSingletonObject;
        }
        
        if (cObjectClass == null) {
            throw new IllegalStateException("Object factory does not have a class set.");
        }
        
        try
        {
            if (oaArgs == null || oaArgs.length == 0) {
                return cObjectClass.newInstance();
            } else {
                if (cObjectConstrcutor == null) {
                    throw new IllegalArgumentException("newInstance() was used with arguments but no constructor is defined.");
                }
                
                return cObjectConstrcutor.newInstance(oaArgs);
            }
        }
        catch (InstantiationException e)
        {
            throw new BFException("Unable to instantiate the class " + cObjectClass, e);
        }
        catch (IllegalAccessException e)
        {
            throw new BFException("Unable to instantiate the class " + cObjectClass, e);        
        }
        catch (IllegalArgumentException e)
        {
            throw new BFException("Unable to instantiate the class " + cObjectClass, e);    
        }
        catch (InvocationTargetException e)
        {
            throw new BFException("Unable to instantiate the class " + cObjectClass, e);    
        }
    }
}
