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
package com.cordys.coe.bf.content.types.coboc.mdm;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.CobocUtils;

/**
 * Class containing MDM model information.
 *
 * @author mpoyhone
 */
public class Model extends MDMBase
{
    private List<ModelBackendRef> lBackendRefs;
    private List<ModelLink> lLinks;
    private String sReceiverDn;
    private String sProcessorDn;
    private String sModelDescription;
    private String sHubName;
    private CobocContentHandle cchHubHandle;

    /**
     * Constructor for Model
     */
    public Model()
    {
        super(EContentType.MDM_MODEL);
        cchHubHandle = new CobocContentHandle(EContentType.MDM_BACKEND);
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        Collection<IContentHandle> cSuperRes = super.getReferencedContent();
        List<IContentHandle> lRes = new LinkedList<IContentHandle>();
        
        if (cSuperRes != null) {
            lRes.addAll(cSuperRes);
        }
        
        if (cchHubHandle != null && cchHubHandle.isSet()) {
            lRes.add(cchHubHandle);
        }
        
        for (Iterator<ModelBackendRef> iIter = lBackendRefs.iterator(); iIter.hasNext();)
        {
            ModelBackendRef mbrBackend = iIter.next();
            
            mbrBackend.getReferencedContent(lRes);
        }
        
        for (Iterator<ModelLink> iIter = lLinks.iterator(); iIter.hasNext();)
        {
            ModelLink mlLink = iIter.next();
            
            mlLink.getReferencedContent(lRes);
        }
        
        return lRes;
    }

    /**
     * @see com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        super.updateReferences(csSrc);
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType()  == EContentSourceType.ISV) {
            CobocUtils.updateCobocHandle(bcContext, cchHubHandle);
        }
        
        for (Iterator<ModelBackendRef> iIter = lBackendRefs.iterator(); iIter.hasNext();)
        {
            ModelBackendRef mbrBackend = iIter.next();
            
            mbrBackend.updateReferences(bcContext, csSrc);
        }
        
        for (Iterator<ModelLink> iIter = lLinks.iterator(); iIter.hasNext();)
        {
            ModelLink mlLink = iIter.next();
            
            mlLink.updateReferences(bcContext, csSrc);
        }        
    }   

    /**
     * Returns the backendRefs.
     *
     * @return Returns the backendRefs.
     */
    public List<ModelBackendRef> getBackendRefs()
    {
        return lBackendRefs;
    }

    /**
     * The backendRefs to set.
     *
     * @param aBackendRefs The backendRefs to set.
     */
    public void setBackendRefs(List<ModelBackendRef> aBackendRefs)
    {
        lBackendRefs = aBackendRefs;
    }

    /**
     * Returns the links.
     *
     * @return Returns the links.
     */
    public List<ModelLink> getLinks()
    {
        return lLinks;
    }

    /**
     * The links to set.
     *
     * @param aLinks The links to set.
     */
    public void setLinks(List<ModelLink> aLinks)
    {
        lLinks = aLinks;
    }

    /**
     * Returns the cchHubHandle.
     *
     * @return Returns the cchHubHandle.
     */
    public CobocContentHandle getHubHandle()
    {
        return cchHubHandle;
    }

    /**
     * The cchHubHandle to set.
     *
     * @param aCchHubHandle The cchHubHandle to set.
     */
    public void setHubHandle(CobocContentHandle aCchHubHandle)
    {
        cchHubHandle = aCchHubHandle;
    }

    /**
     * Returns the hubName.
     *
     * @return Returns the hubName.
     */
    public String getHubName()
    {
        return sHubName;
    }

    /**
     * The hubName to set.
     *
     * @param aHubName The hubName to set.
     */
    public void setHubName(String aHubName)
    {
        sHubName = aHubName;
    }

    /**
     * Returns the modelDescription.
     *
     * @return Returns the modelDescription.
     */
    public String getModelDescription()
    {
        return sModelDescription;
    }

    /**
     * The modelDescription to set.
     *
     * @param aModelDescription The modelDescription to set.
     */
    public void setModelDescription(String aModelDescription)
    {
        sModelDescription = aModelDescription;
    }

    /**
     * Returns the processorDn.
     *
     * @return Returns the processorDn.
     */
    public String getProcessorDn()
    {
        return sProcessorDn;
    }

    /**
     * The processorDn to set.
     *
     * @param aProcessorDn The processorDn to set.
     */
    public void setProcessorDn(String aProcessorDn)
    {
        sProcessorDn = aProcessorDn;
    }

    /**
     * Returns the receiverDn.
     *
     * @return Returns the receiverDn.
     */
    public String getReceiverDn()
    {
        return sReceiverDn;
    }

    /**
     * The receiverDn to set.
     *
     * @param aReceiverDn The receiverDn to set.
     */
    public void setReceiverDn(String aReceiverDn)
    {
        sReceiverDn = aReceiverDn;
    }    

}
