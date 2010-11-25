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

package com.cordys.tools.ant.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.xml.sax.SAXException;

import com.eibus.xml.dom.NiceDOMWriter;
import com.eibus.xml.dom.SAXDocument;
import com.eibus.xml.nom.Node;

/**
 * Helper class for File operations.
 *
 * @author knayak
 */
public class FileUtil
{
    /**
     * Provides the resource from the class loader as an inputstream.
     *
     * @param clazz The class instance.
     * @param relativePath The relative path of the resource from the class.
     *
     * @return The inputstream of the resource.
     *
     * @throws IOException If the resource cannot be located.
     */
    public static InputStream getResourceAsStream(Class<?> clazz,
                                                  String relativePath)
                                           throws IOException
    {
        InputStream inputStream = clazz.getResourceAsStream(relativePath);

        if (inputStream == null)
        {
            throw new IOException("Could not find resource:" + relativePath);
        }

        return inputStream;
    }

    /**
     * Provides the resource from the class loader as a sring.
     *
     * @param clazz The class instance.
     * @param relativePath The relative path of the resource from the class.
     *
     * @return The contents of the resource as string.
     *
     * @throws IOException If the resource cannot be located.
     */
    public static String getResourceAsString(Class<?> clazz, String relativePath)
                                      throws IOException
    {
        InputStream inputStream = clazz.getResourceAsStream(relativePath);

        if (inputStream == null)
        {
            throw new IOException("Could not find resource:" + relativePath);
        }

        StringWriter reader = new StringWriter();
        int i = 0;

        while (i != -1)
        {
            i = inputStream.read();
            reader.write(i);
        }

        return reader.toString();
    }

    /**
     * Takes the XML node and writes the contents into a flat file
     *
     * @param node XML node to be written in to file
     * @param filename File name to which the contents are to be written
     *
     * @throws SAXException
     * @throws IOException
     */
    public static void fileWrite(int node, String filename)
                          throws SAXException, IOException
    {
        PrintWriter outputFile = new PrintWriter(new FileWriter(filename));

        //Parses the XML Node
        SAXDocument saxdocument = new SAXDocument(Node.writeToString(node, true)
                                                      .getBytes());

        outputFile.write((new NiceDOMWriter(saxdocument.getDocumentElement())).toString());
        outputFile.close();
    }

    /**
     * Writes the input string to the file in the UTF-8 encoding.
     *
     * @param data String to be written into the file
     * @param filename File name to which the contents are to be written
     *
     * @throws IOException
     */
    public static void writeToFile(String data, String filename)
                            throws IOException
    {
        Writer w = null;
        
        try {
            w = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");
            w.write(data);
        }
        catch (UnsupportedEncodingException ignored) {
            // This cannot happen.
        }
        finally {
            w.close();
        }
    }
    
    /**
     * Returns the path relative to the given folder. 
     * @param baseDir Base folder.
     * @param file File path.
     * @return Relative file object.
     * @throws IOException 
     */
    public static String getRelativePath(File baseDir, File file) throws IOException {
        String canBaseDir = baseDir.getCanonicalPath();
        String canFile = file.getCanonicalPath();
        
        if (canBaseDir.length() + 1 >= canFile.length()) {
            throw new IllegalArgumentException(String.format("File %s is not under folder %s.", baseDir.toString(), file.toString()));
        }

        return canFile.substring(canBaseDir.length() + 1);
    }
}
