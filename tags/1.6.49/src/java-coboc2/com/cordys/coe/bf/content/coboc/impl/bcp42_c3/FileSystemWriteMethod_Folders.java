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
package com.cordys.coe.bf.content.coboc.impl.bcp42_c3;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem;
import com.cordys.coe.bf.content.coboc.impl.bcp42_c1.CobocConstants;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.exception.BFException;

/**
 * Write method for CoBOC schedule templates.
 *
 * @author mpoyhone
 */
public class FileSystemWriteMethod_Folders extends com.cordys.coe.bf.content.coboc.impl.bcp42_c1.FileSystemWriteMethod_Folders
{
    /**
     * Creates a new FileSystemWriteMethod_Folders object.
     *
     * @param bcContext DOCUMENTME
     * @param csSource DOCUMENTME
     *
     * @throws BFException DOCUMENTME
     */
    public FileSystemWriteMethod_Folders(BFContext bcContext,
            IContentSource csSource)
                                  throws BFException
    {
        super(bcContext, (ContentSourceFileSystem) csSource);
    }

    /**
     * 
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#updateObject(com.cordys.coe.bf.content.base.ContentTuple)
     */
    public IContent updateObject(ContentTuple ctTuple)
                          throws BFException
    {
        IContent cUpdateObject = ctTuple.getWriteObject();
        EContentType ctType = cUpdateObject.getType();

        switch(ctType)
        {
        case COBOC_FOLDERS_FOLDER :
            String key = ((CobocContentBase) cUpdateObject).getKey();

            if (key.startsWith(CobocConstants.COBOC_BPML_ROOT) || key.startsWith(CobocConstants.COBOC_BPMN_ROOT)) {
                return null;
            }
            break;
        
        case COBOC_FOLDERS_PROCESSBPMN :
        case COBOC_FOLDERS_PROCESSBPML :
            // These are written along with process template into the same file.
            return null;
        }

        return super.updateObject(ctTuple);
    }
}
