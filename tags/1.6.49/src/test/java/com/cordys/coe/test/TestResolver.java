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
package com.cordys.coe.test;

import java.io.File;

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;
import com.cordys.coe.ant.bf.properties.BuildFrameworkResolver;

public class TestResolver
{
    /**
     * Main method.
     *
     * @param saArguments The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            BuildFrameworkResolver bfr = new BuildFrameworkResolver(new File("D:\\development\\workspaces\\EclipsePlugins\\svnAnt"));
            File fTemp = bfr.resolveFile(BuildFrameworkProperty.SDK_DIR);
            System.out.println("SDK folder: " + fTemp.getCanonicalPath());
            fTemp = bfr.resolveFile(BuildFrameworkProperty.SDK_BUILD_DIR);
            System.out.println("SDK Build folder: " + fTemp.getCanonicalPath());
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
