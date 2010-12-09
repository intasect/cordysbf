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
package com.cordys.coe.ant.coboc.content.folders;

import com.cordys.coe.ant.coboc.content.CoBOCObject;
import com.cordys.coe.ant.coboc.content.ContentException;

/**
 * A callback interface to be used when iterating the folder hierarchy.
 *
 * @author mpoyhone
 */
public class FolderCallback
{
    /**
     * Mask bit for iterating folders.
     */
    public static final int MASK_FOLDER = 0x001;
    /**
     * Mask bit for iterating templates.
     */
    public static final int MASK_TEMPLATE = 0x002;
    /**
     * Mask bit for iterating process templates (reserved).
     */
    public static final int MASK_PROCESS_TEMPLATE = 0x004;
    /**
     * Mask bit for iterating mappings.
     */
    public static final int MASK_MAPPING = 0x008;
    /**
     * Mask bit for iterating content mappings.
     */
    public static final int MASK_CONTENT_MAPPING = 0x010;
    /**
     * Mask bit for iterating all non-folder objects.
     */
    public static final int MASK_ALL_OBJECTS = 0xFFFE;

    /**
     * The callback method that is called for every requested folder.
     *
     * @param fFolder The current folder object in the iteration.
     *
     * @return If true, the iteration can continue and if false the iteration
     *         stops.
     *
     * @throws ContentException Thrown if there was an error in the handling.
     */
    public boolean handleFolder(Folder fFolder)
                         throws ContentException
    {
        return true;
    }

    /**
     * The callback method that is called for every requested entity.
     *
     * @param coObject The COBOCObject that was encountered during iteration.
     *
     * @return If true, the iteration can continue and if false the iteration
     *         stops.
     *
     * @throws ContentException Thrown if there was an error in the handling.
     */
    public boolean handleObject(CoBOCObject coObject)
                         throws ContentException
    {
        return true;
    }
}
