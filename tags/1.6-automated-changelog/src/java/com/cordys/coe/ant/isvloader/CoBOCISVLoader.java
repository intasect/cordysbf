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
package com.cordys.coe.ant.isvloader;

import com.eibus.contentmanagement.ISVPackage;

/**
 * A class that load the content to CoBOC when it called during ISV
 * installation.
 *
 * @author mpoyhone
 */
public class CoBOCISVLoader extends ISVLoaderBase
{
    /**
     * Called when the ISV package is being loaded.
     *
     * @param isvPackage The ISV package object.
     * @param contentItem The CoBOC content XML structure that is in the ISV
     *        package.
     * @param logcontentItem The log XML structure.
     *
     * @throws Exception Thrown if the operation failed.
     */
    public void load(ISVPackage isvPackage, int contentItem, int logcontentItem)
              throws Exception
    {
        super.load(isvPackage, contentItem, logcontentItem);
    }

    /**
     * Called when the ISV package is being unloaded.
     *
     * @param isvPackage The ISV package object.
     * @param contentItem The CoBOC content XML structure that is in the ISV
     *        package.
     * @param logcontentItem The log XML structure.
     *
     * @throws Exception Thrown if the operation failed.
     */
    public void unload(ISVPackage isvPackage, int contentItem,
                       int logcontentItem)
                throws Exception
    {
        super.unload(isvPackage, contentItem, logcontentItem);
    }
}
