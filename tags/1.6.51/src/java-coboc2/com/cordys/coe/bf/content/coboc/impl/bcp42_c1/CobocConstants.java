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
package com.cordys.coe.bf.content.coboc.impl.bcp42_c1;

/**
 * Contains all CoBOC releated constats.
 *
 * @author mpoyhone
 */
public class CobocConstants
{
    /**
     * Root folder key.
     */
    public static final String ROOT_FOLDER_KEY = "/";
    /**
     * The entity ID of the root folder. It is not certain of this is a fixed
     * value. TODO Find out how this is specified.
     */
    public static final String ROOT_FOLDER_ID = "1";    
    /**
     * Contains the folder data when it is stored in the file system.
     */
    public static final String FOLDER_INFO_FILENAME = "00-FOLDER_INFO.xml";

    /**
     * The tuple-attribute that indicates type file type when the CoBOC object
     * is stored in the file system.
     */
    public static final String FILE_TYPE_ATTRIBUTE = "FILE_TYPE";
    /**
     * The CoBOC table name for template attributes.
     */
    public static final String COBOC_ATTRIBUTE_TABLE_NAME = "attribute_template";
    /**
     * The template ID field name in CoBOC template attribute table. 
     */
    public static final String COBOC_ATTRIBUTE_FIELD_NAME = "template_id";    
    /**
     * Business process BPMN content is read under this folder.
     */
    public static final String COBOC_BPMN_ROOT = "/Business Processes/BPMN";
    /**
     * Business process BPML content is read under this folder.
     */
    public static final String COBOC_BPML_ROOT = "/Business Processes/BPML";    
    /**
     * Email models are stored under this folder in XMLStore.
     */
    public static final String XMLSTORE_EMAIL_MODEL_ROOT = "/Cordys/notification/emailmodels/models";    
    /**
     * File to be used for CoBOC object ID/key mappings.
     */
    public static final String COBOC_OBJECT_ID_MAP_FILE = "object-id-map.xml";
    /**
     * Name of Ant property for enabling the object ID mapping file.
     */
    public static final String PROPERTY_USE_OBJECT_ID_MAP = "coboc.use.objectid.mapping.file";
    
}
