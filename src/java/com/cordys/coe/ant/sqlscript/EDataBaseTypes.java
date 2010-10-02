/**
 * Copyright 2007 Cordys R&D B.V. 
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
package com.cordys.coe.ant.sqlscript;

import com.cordys.coe.ant.sqlscript.providers.GenericDatabaseProvider;
import com.cordys.coe.ant.sqlscript.providers.SqlServerDatabaseProvider;

/**
 * Enumeration for all supported database types.
 *
 * @author mpoyhone
 */
public enum EDataBaseTypes
{
    /**
     * Generic database provider for default behaviour.
     */
    GENERIC(GenericDatabaseProvider.class),
    /**
     * SQLServer provider.
     */
    SQLSERVER(SqlServerDatabaseProvider.class);
    
    /**
     * Class of the factory class for this type. 
     */
    private final Class<? extends IDatabaseProvider> databaseFactoryClass;
    
    /**
     * Constructor for EDataBaseTypes
     * @param factoryClass Class of the factory class for this type. 
     */
    private EDataBaseTypes(Class<? extends IDatabaseProvider> factoryClass) {
        this.databaseFactoryClass = factoryClass;
    }
 
    /**
     * Create a new factory instance.
     * @return New factory instance.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public IDatabaseProvider getProviderInstance() throws InstantiationException, IllegalAccessException {
        return databaseFactoryClass.newInstance();
    }
}
