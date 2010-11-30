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
package com.cordys.coe.bf.content.ldap.impl.bcp42_c1;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.impl.BcpWriteMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;

/**
 * Write method for BCP LDAP content.
 *
 * @author mpoyhone
 */
public class BcpWriteMethod_LDAP extends BcpWriteMethodBase
{
    public BcpWriteMethod_LDAP(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceBcp) csSource);
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#deleteObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent deleteObject(IContentHandle chHandle) throws BFException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#updateObject(com.cordys.coe.bf.content.base.ContentTuple)
     */
    public IContent updateObject(ContentTuple ctTuple) throws BFException
    {
        if (ctTuple.isUpdate()) {
            if (ctTuple.getOld().getType() != ctTuple.getNew().getType()) {
                throw new BFException("Old object is of different type than the new object.");
            }
        }
        
        EContentType ctType = ctTuple.getWriteObject().getType();
        IContentMarshaller cmMarshaller = csSource.getContentMarshaller(ctType, null, getMethodVersion());
        ISoapRequest srRequest = csSource.createSoapRequest();
        
        sendUpdateRequest(srRequest, IBcpMethodNames.LDAP_METHOD_UPDATE, ctTuple, cmMarshaller);
        
        return null;
    }

}
