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

import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.Properties;

/**
 * DOCUMENTME
 *
 * @author pgussow
  */
public class TestProperties
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
            Properties pProp = new Properties();
            pProp.setProperty("test", "tast1\r\ntest2");
            pProp.store(new FileOutputStream("c:/temp/test.properties", false),
                        null);

            pProp = new Properties();
            pProp.load(new FileInputStream("c:/temp/test2.properties"));
            System.out.println(pProp.get("test"));
            System.out.println(pProp.get("test2"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
