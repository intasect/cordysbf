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
package com.cordys.coe.ant.sqlscript.providers;

import org.apache.tools.ant.BuildException;

import com.cordys.coe.ant.sqlscript.DatabaseConfig;

/**
 * Default provider. Tries to have sane defaults.
 *
 * @author mpoyhone
 */
public class GenericDatabaseProvider extends DatabaseProviderBase
{
    /**
     * @see com.cordys.coe.ant.sqlscript.providers.DatabaseProviderBase#getDefaultDriverClass()
     */
    @Override
    protected String getDefaultDriverClass()
    {
       throw new BuildException("Database driver class is missing.");
    }

    /**
     * @see com.cordys.coe.ant.sqlscript.providers.DatabaseProviderBase#getScriptSeparator()
     */
    @Override
    protected String getScriptSeparator()
    {
        return ";";
    }

    /**
     * @see com.cordys.coe.ant.sqlscript.providers.DatabaseProviderBase#getUrl()
     */
    @Override
    protected String getUrl(DatabaseConfig dbcfg)
    {
        throw new BuildException("Connection URL must be set.");
    }
}
