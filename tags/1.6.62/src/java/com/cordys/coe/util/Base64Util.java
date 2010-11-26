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
package com.cordys.coe.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import sun.misc.BASE64Decoder;

/**
 * This class is used to abstract Base64 operations.
 *
 * @author pgussow
 */
public class Base64Util
{
    /**
     * This method writes the given BASE64 content to the given file. The file is overwritten if it already exists.
     *
     * @param   sBase64Content  The BAS64 content.
     * @param   fDestination    the destination file.
     *
     * @throws  IOException  In case of any exceptions.
     */
    public static void writeBase64File(String sBase64Content, File fDestination)
                                throws IOException
    {
        FileOutputStream fos = new FileOutputStream(fDestination, false);

        try
        {
            new BASE64Decoder().decodeBuffer(new ByteArrayInputStream(sBase64Content.getBytes("UTF-8")),
                                             fos);
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }
        }
    }
}
