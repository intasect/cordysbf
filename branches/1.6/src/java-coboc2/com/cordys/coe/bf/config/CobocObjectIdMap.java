/**
 * Copyright 2009 Cordys R&D B.V. 
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
package com.cordys.coe.bf.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.cordys.coe.bf.content.types.EContentType;

/**
 * Contains mappings from CoBOC keys to CoBOC object ID's.
 * This is used for storing the object ID's in a configuration file.
 * These mappings are used to generate right ID's when ISV package is generated.
 * @author mpoyhone
 */
public class CobocObjectIdMap
{
    /**
     * Contains categories.
     */
    private Map<EContentType, CategoryEntry> categoryMap = new HashMap<EContentType, CategoryEntry>();
    
    /**
     * Adds a new entry.
     * 
     * @param type Content type.
     * @param key Key.
     * @param objectId Object ID.
     */
    public void addEntry(EContentType type, String key, String objectId)
    {
        CategoryEntry e = getEntry(type);
        
        e.keyToObjectIdMap.put(key, objectId);
        e.objectIdtoKeyMap.put(objectId, key);
    }

    /**
     * Returns the object ID for the given key.
     * 
     * @param type Content type.
     * @param key Content key.
     * @return Object ID or <code>null</code> if none was found.
     */
    public String getObjectId(EContentType type, String key)
    {
        CategoryEntry e = getEntry(type);
        
        return e.keyToObjectIdMap.get(key);
    }
    
    /**
     * Returns category entry or creates a new one.
     * 
     * @param type Content type.
     * @return Category entry.
     */
    private CategoryEntry getEntry(EContentType type)
    {
        CategoryEntry e = categoryMap.get(type);
        
        if (e == null) {
            e = new CategoryEntry();
            categoryMap.put(type, e);
        }
        
        return e;
    }
    
    /**
     * Loads mappings from a file.
     * 
     * @param file File to be loaded.
     * @throws IOException Thrown if the loading failed.
     */
    public void loadFromFile(File file) throws IOException
    {
        InputStream is = null;
        XMLStreamReader reader = null;
        
        try {
            is = new FileInputStream(file);
            reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            
            while (true) {
                int event = reader.next();
                
                switch (event) {
                case XMLStreamReader.END_DOCUMENT :
                    return;
                
                case XMLStreamReader.START_ELEMENT :
                    if ("config".equals(reader.getLocalName())) {
                        if (! readConfig(reader)) {
                            return;
                        }
                    }
                    break;
                }
            }
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException(e);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception ignored) {
                }
            }
        }
    }
    
    private boolean readConfig(XMLStreamReader reader) throws XMLStreamException
    {
        int level = 0;
        
        while (true) {
            int event = reader.next();
            
            switch (event) {
            case XMLStreamReader.END_DOCUMENT :
                return false;
                
            case XMLStreamReader.END_ELEMENT :
                if (level-- <= 0) {
                    return true;
                }
                break;
            
            case XMLStreamReader.START_ELEMENT :
                if ("category".equals(reader.getLocalName())) {
                    String typeStr = reader.getAttributeValue(null, "name");
                    
                    if (typeStr != null) {
                        EContentType type = EContentType.valueOf(typeStr);
                        
                        if (! readCategory(type, reader)) {
                            return false;
                        }
                    }
                } else {
                    level++;
                }
                
                break;
            }
        }
    }
    
    private boolean readCategory(EContentType type, XMLStreamReader reader) throws XMLStreamException
    {
        int level = 0;
        
        while (true) {
            int event = reader.next();
            
            switch (event) {
            case XMLStreamReader.END_DOCUMENT :
                return false;
                
            case XMLStreamReader.END_ELEMENT :
                if (level-- <= 0) {
                    return true;
                }
                break;
            
            case XMLStreamReader.START_ELEMENT :
                if ("entry".equals(reader.getLocalName())) {
                    String key = reader.getAttributeValue(null, "key");
                    String objectId = reader.getAttributeValue(null, "objectid");
                    
                    if (key != null && objectId != null) {
                        addEntry(type, key, objectId);
                    }
                }
                
                level++;
                break;
            }
        }
    }
    
    /**
     * Loads mappings from a file.
     * 
     * @param file File to be loaded.
     * @throws IOException Thrown if the loading failed.
     */
    public void writeToFile(File file) throws IOException
    {
        OutputStream os = null;
        XMLStreamWriter writer = null;
        
        try {
            os = new FileOutputStream(file);
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(os);
            
            writer.writeStartDocument();
            writer.writeCharacters("\n");
            writer.writeStartElement("config");
            writer.writeCharacters("\n");
            
            // Add the types to map, to sort them.
            Map<String, EContentType> typeMap = new TreeMap<String, EContentType>();
            
            for (EContentType type : categoryMap.keySet())
            {
                typeMap.put(type.name(), type);
            }
            
            for (String typeName : typeMap.keySet())
            {
                EContentType type = typeMap.get(typeName);
                
                writer.writeCharacters("\t");
                writer.writeStartElement("category");
                writer.writeAttribute("name", type.name());
                writer.writeCharacters("\n");
                
                CategoryEntry entry = categoryMap.get(type);
                
                for (Map.Entry<String, String> me : entry.keyToObjectIdMap.entrySet()) {
                    writer.writeCharacters("\t\t");
                    writer.writeEmptyElement("entry");
                    writer.writeAttribute("key", me.getKey());
                    writer.writeAttribute("objectid", me.getValue());
                    writer.writeCharacters("\n");
                }
                
                writer.writeCharacters("\t");
                writer.writeEndElement(); // category
                writer.writeCharacters("\n");
            }
            
            writer.writeEndElement(); // config
            writer.writeEndDocument();
            
            writer.flush();
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException(e);
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (Exception ignored) {
                }
            }
        }
    }
    
    /**
     * Contains content category entry. 
     *
     * @author mpoyhone
     */
    private static class CategoryEntry
    {
        /**
         * Maps from key to object ID.
         */
        private Map<String, String> keyToObjectIdMap = new TreeMap<String, String>();
        /**
         * Maps from object ID to key.
         */
        private Map<String, String> objectIdtoKeyMap = new HashMap<String, String>();
    }
}
