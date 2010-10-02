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
import java.util.LinkedList;
import java.util.List;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.CobocUtils;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * Class containing MDM entity information.
 *
 * @author mpoyhone
 */
public class Entity extends MDMBase
{
    private CobocContentHandle cchBackendHandle;
    private String sDisplayName;
    private String sIdentity;
    private String sTemplateLocation;
    private String sTemplateVersion;
    private String sAuditTableName;
    private String sScheduleInterval;
    private String sGetMethod;
    private String sPullMethod;
    private String sUpdateMethod;
    private String sKeyFields;
    
    /**
     * Constructor for Entity
     */
    public Entity()
    {
        super(EContentType.MDM_ENTITY);
        cchParentHandle = new CobocContentHandle(EContentType.MDM_ENTITY_FOLDER);
        cchBackendHandle = new CobocContentHandle(EContentType.MDM_BACKEND);
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
        
        if (cchBackendHandle != null && cchBackendHandle.isSet()) {
            lRes.add(cchBackendHandle);
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
            CobocUtils.updateCobocHandle(bcContext, cchBackendHandle);
        }
    }   
    
    /**
     * Changes the key and name to match the destination format.
     * @param csDest Destination content source.
     * @throws BFException 
     */
    public void changeKeyFor(EContentSourceType cstType) throws BFException {
        setKey(getKeyForSource(cstType));
    }
    
    /**
     * Returns a new handle for the given content source.
     * @param cstType Content source type.
     * @return Entity key.
     * @throws BFException 
     */
    public IContentHandle getHandleForSource(EContentSourceType cstType) throws BFException {
        CobocContentHandle cchRes = (CobocContentHandle) getHandle().cloneHandle();
        
        cchRes.setKey(getKeyForSource(cstType));
        
        return cchRes;
    }
    
    /**
     * Returns a key for the given content source.
     * @param cstType Content source type.
     * @return Entity key.
     * @throws BFException 
     */
    public String getKeyForSource(EContentSourceType cstType) throws BFException {
       Backend bBackend = (Backend) bcContext.findContent(cchBackendHandle);
        
        if (bBackend == null) {
            throw new BFException(getLogName() + ": Backend not found: " + cchBackendHandle);
        }
        
        String sBackendId;
        
        if (cstType == EContentSourceType.BCP || cstType == EContentSourceType.ISV) {
            sBackendId = bBackend.getObjectId();
        } else {
            sBackendId = bBackend.getKey();
        }

        if (sBackendId == null) {
            throw new BFException(getLogName() + ": Backend name/object ID not set. ");
        }
        
        return getKey().replaceFirst("^(.+)/[^/]+/([^/]+)$", "$1/" + sBackendId + "/$2");
    }

    /**
     * Returns the auditTableName.
     *
     * @return Returns the auditTableName.
     */
    public String getAuditTableName()
    {
        return sAuditTableName;
    }

    /**
     * The auditTableName to set.
     *
     * @param aAuditTableName The auditTableName to set.
     */
    public void setAuditTableName(String aAuditTableName)
    {
        sAuditTableName = aAuditTableName;
    }

    /**
     * Returns the displayName.
     *
     * @return Returns the displayName.
     */
    public String getDisplayName()
    {
        return sDisplayName;
    }

    /**
     * The displayName to set.
     *
     * @param aDisplayName The displayName to set.
     */
    public void setDisplayName(String aDisplayName)
    {
        sDisplayName = aDisplayName;
    }

    /**
     * Returns the getMethod.
     *
     * @return Returns the getMethod.
     */
    public String getGetMethod()
    {
        return sGetMethod;
    }

    /**
     * The getMethod to set.
     *
     * @param aGetMethod The getMethod to set.
     */
    public void setGetMethod(String aGetMethod)
    {
        sGetMethod = aGetMethod;
    }

    /**
     * Returns the identity.
     *
     * @return Returns the identity.
     */
    public String getIdentity()
    {
        return sIdentity;
    }

    /**
     * The identity to set.
     *
     * @param aIdentity The identity to set.
     */
    public void setIdentity(String aIdentity)
    {
        sIdentity = aIdentity;
    }

    /**
     * Returns the keyFields.
     *
     * @return Returns the keyFields.
     */
    public String getKeyFields()
    {
        return sKeyFields;
    }

    /**
     * The keyFields to set.
     *
     * @param aKeyFields The keyFields to set.
     */
    public void setKeyFields(String aKeyFields)
    {
        sKeyFields = aKeyFields;
    }

    /**
     * Returns the pullMethod.
     *
     * @return Returns the pullMethod.
     */
    public String getPullMethod()
    {
        return sPullMethod;
    }

    /**
     * The pullMethod to set.
     *
     * @param aPullMethod The pullMethod to set.
     */
    public void setPullMethod(String aPullMethod)
    {
        sPullMethod = aPullMethod;
    }

    /**
     * Returns the scheduleInterval.
     *
     * @return Returns the scheduleInterval.
     */
    public String getScheduleInterval()
    {
        return sScheduleInterval;
    }

    /**
     * The scheduleInterval to set.
     *
     * @param aScheduleInterval The scheduleInterval to set.
     */
    public void setScheduleInterval(String aScheduleInterval)
    {
        sScheduleInterval = aScheduleInterval;
    }

    /**
     * Returns the templateLocation.
     *
     * @return Returns the templateLocation.
     */
    public String getTemplateLocation()
    {
        return sTemplateLocation;
    }

    /**
     * The templateLocation to set.
     *
     * @param aTemplateLocation The templateLocation to set.
     */
    public void setTemplateLocation(String aTemplateLocation)
    {
        sTemplateLocation = aTemplateLocation;
    }

    /**
     * Returns the templateVersion.
     *
     * @return Returns the templateVersion.
     */
    public String getTemplateVersion()
    {
        return sTemplateVersion;
    }

    /**
     * The templateVersion to set.
     *
     * @param aTemplateVersion The templateVersion to set.
     */
    public void setTemplateVersion(String aTemplateVersion)
    {
        sTemplateVersion = aTemplateVersion;
    }

    /**
     * Returns the updateMethod.
     *
     * @return Returns the updateMethod.
     */
    public String getUpdateMethod()
    {
        return sUpdateMethod;
    }

    /**
     * The updateMethod to set.
     *
     * @param aUpdateMethod The updateMethod to set.
     */
    public void setUpdateMethod(String aUpdateMethod)
    {
        sUpdateMethod = aUpdateMethod;
    }

    /**
     * Returns the documentProperties.
     *
     * @return Returns the documentProperties.
     */
    public XmlStructure getDocumentProperties()
    {
        return xDocumentProperties;
    }

    /**
     * The documentProperties to set.
     *
     * @param aDocumentProperties The documentProperties to set.
     */
    public void setDocumentProperties(XmlStructure aDocumentProperties)
    {
        xDocumentProperties = aDocumentProperties;
    }


    /**
     * Returns the backendId.
     *
     * @return Returns the backendId.
     */
    public String getBackendId()
    {
        return cchBackendHandle.getObjectId();
    }



    /**
     * The backendId to set.
     *
     * @param aBackendId The backendId to set.
     */
    public void setBackendId(String aBackendId)
    {
        cchBackendHandle.setObjectId(aBackendId);
    }



    /**
     * Returns the backendKey.
     *
     * @return Returns the backendKey.
     */
    public String getBackendKey()
    {
        return cchBackendHandle.getKey();
    }



    /**
     * The backendKey to set.
     *
     * @param aBackendKey The backendKey to set.
     */
    public void setBackendKey(String aBackendKey)
    {
        cchBackendHandle.setKey(aBackendKey);
    }
}
