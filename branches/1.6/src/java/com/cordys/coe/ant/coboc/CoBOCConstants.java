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
package com.cordys.coe.ant.coboc;

/**
 * Contains constants related to CoBOC or the CoBOC task.
 *
 * @author mpoyhone
 */
public class CoBOCConstants
{
    /**
     * Contains the folder data when it is stored in the file system.
     */
    public static final String FN_FOLDER_INFO_FILENAME = "00-FOLDER_INFO.xml";
    /**
     * The entity ID of the root folder. It is not certain of this is a fixed
     * value. TODO Find out how this is specified.
     */
    public static final String COBOC_ROOT_FOLDER_ID = "1";
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
}
