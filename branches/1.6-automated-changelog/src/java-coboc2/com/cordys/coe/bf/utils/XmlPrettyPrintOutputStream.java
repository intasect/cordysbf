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
package com.cordys.coe.bf.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * An output stream that pretty prints the XML written to it.
 * Currently this just reads the contents into a byte array
 * and when this is closed the contents are written to the destination stream.
 *
 * @author mpoyhone
 */
public class XmlPrettyPrintOutputStream extends OutputStream
{
    private OutputStream osOutput;
    private ByteArrayOutputStream basBuffer;
    
    public XmlPrettyPrintOutputStream(OutputStream osOutput) {
        this.osOutput = osOutput;
        this.basBuffer = new ByteArrayOutputStream(1024); 
    }
    
    /**
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException
    {
        Document dResponseDoc;
        
        DocumentBuilderFactory dbfFactory = DocumentBuilderFactory.newInstance();
        
        dbfFactory.setNamespaceAware(true);
        
        try
        {
            dResponseDoc = dbfFactory.newDocumentBuilder().parse(new ByteArrayInputStream(basBuffer.toByteArray()));
        }
        catch (Exception e)
        {
            throw (IOException) new IOException().initCause(e);
        }
        
        osOutput.write(NodeHelper.writeToString(dResponseDoc.getDocumentElement(), true).getBytes("UTF-8"));
        osOutput.flush();
        osOutput.close();
        
        basBuffer = null;
        osOutput = null;
    }
    /**
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException
    {
        basBuffer.flush();
    }
    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException
    {
        basBuffer.write(b, off, len);
    }
    /**
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] b) throws IOException
    {
        basBuffer.write(b);
    }
    /**
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException
    {
        basBuffer.write(b);
    }
}
