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
package com.cordys.coe.bf.content.types.coboc.folders;

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
 * Class containing CoBOC mapping information.
 *
 * @author mpoyhone
 */
public class Mapping extends ObjectInstanceBase
{
    private String sVersion;
    private CobocContentHandle[] cchaSourceHandles;
    private String sSourceIsShared;
    private String sSourceVersion;
    private String sSourceShareName;
    private CobocContentHandle[] cchaTargetHandles;
    private String sTargetIsShared;
    private String sTargetVersion;
    private String sTargetShareName;
    private XmlStructure xMapData;
    
    /**
     * Constructor for Mapping
     */
    public Mapping()
    {
        super(EContentType.COBOC_FOLDERS_MAPPING);
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        super.updateReferences(csSrc);
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType()  == EContentSourceType.ISV) {
            CobocUtils.updateCobocHandleIds(bcContext, cchaSourceHandles);
            CobocUtils.updateCobocHandleIds(bcContext, cchaTargetHandles);
        }
    }

    /**
     * @see com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        Collection<IContentHandle> lSuperContent = super.getReferencedContent();
        List<IContentHandle> lRes = new LinkedList<IContentHandle>();
        
        if (cchaSourceHandles != null) {
            for (CobocContentHandle hHandle : cchaSourceHandles)
            {
                lRes.add(hHandle);
            }
        }
        
        if (cchaTargetHandles != null) {
            for (CobocContentHandle hHandle : cchaTargetHandles)
            {
                lRes.add(hHandle);
            }
        }
        
        if (lSuperContent != null) {
            lRes.addAll(lSuperContent);
        }
        
        return lRes;
    }  

    /**
     * Returns the sourceIsShared.
     *
     * @return Returns the sourceIsShared.
     */
    public String getSourceIsShared()
    {
        return sSourceIsShared;
    }


    /**
     * The sourceIsShared to set.
     *
     * @param aSourceIsShared The sourceIsShared to set.
     */
    public void setSourceIsShared(String aSourceIsShared)
    {
        sSourceIsShared = aSourceIsShared;
    }


    /**
     * Returns the sourceIsVersion.
     *
     * @return Returns the sourceIsVersion.
     */
    public String getSourceVersion()
    {
        return sSourceVersion;
    }


    /**
     * The sourceIsVersion to set.
     *
     * @param aSourceIsVersion The sourceIsVersion to set.
     */
    public void setSourceVersion(String aSourceIsVersion)
    {
        sSourceVersion = aSourceIsVersion;
    }


    /**
     * Returns the sourceShareName.
     *
     * @return Returns the sourceShareName.
     */
    public String getSourceShareName()
    {
        return sSourceShareName;
    }


    /**
     * The sourceShareName to set.
     *
     * @param aSourceShareName The sourceShareName to set.
     */
    public void setSourceShareName(String aSourceShareName)
    {
        sSourceShareName = aSourceShareName;
    }


    /**
     * Returns the targetIsShared.
     *
     * @return Returns the targetIsShared.
     */
    public String getTargetIsShared()
    {
        return sTargetIsShared;
    }


    /**
     * The targetIsShared to set.
     *
     * @param aTargetIsShared The targetIsShared to set.
     */
    public void setTargetIsShared(String aTargetIsShared)
    {
        sTargetIsShared = aTargetIsShared;
    }


    /**
     * Returns the targetIsVersion.
     *
     * @return Returns the targetIsVersion.
     */
    public String getTargetVersion()
    {
        return sTargetVersion;
    }


    /**
     * The targetIsVersion to set.
     *
     * @param aTargetIsVersion The targetIsVersion to set.
     */
    public void setTargetVersion(String aTargetIsVersion)
    {
        sTargetVersion = aTargetIsVersion;
    }


    /**
     * Returns the targetShareName.
     *
     * @return Returns the targetShareName.
     */
    public String getTargetShareName()
    {
        return sTargetShareName;
    }


    /**
     * The targetShareName to set.
     *
     * @param aTargetShareName The targetShareName to set.
     */
    public void setTargetShareName(String aTargetShareName)
    {
        sTargetShareName = aTargetShareName;
    }


    /**
     * Returns the mapData.
     *
     * @return Returns the mapData.
     */
    public XmlStructure getMapData()
    {
        return xMapData;
    }


    /**
     * The mapData to set.
     *
     * @param aMapData The mapData to set.
     */
    public void setMapData(XmlStructure aMapData)
    {
        xMapData = aMapData;
    }
    
    private CobocContentHandle[] updateHandleArray(CobocContentHandle[] cchaArray, String sValueString, boolean bUpdateId) {
        String[] saValueArray = sValueString.split(",");
        
        if (cchaArray == null) {
            cchaArray = new CobocContentHandle[saValueArray.length];
            for (int i = 0; i < cchaArray.length; i++)
            {
                cchaArray[i] = new CobocContentHandle(EContentType.COBOC_FOLDERS_TEMPLATE);
            }
        } else {
            if (cchaArray.length != saValueArray.length) {
                CobocContentHandle[] cchaOld = cchaArray;
                
                cchaArray = new CobocContentHandle[saValueArray.length];
                System.arraycopy(cchaOld, 0, cchaArray, 0, Math.min(cchaOld.length, cchaArray.length));
                
                // If new one is larger, create the new handles.
                for (int i = cchaOld.length; i < cchaArray.length; i++)
                {
                    cchaArray[i] = new CobocContentHandle(EContentType.COBOC_FOLDERS_TEMPLATE);
                }                
            }
        }
        
        for (int i = 0; i < saValueArray.length; i++)
        {
            if (bUpdateId) {
                cchaArray[i].setObjectId(saValueArray[i]);
            } else {
                cchaArray[i].setKey(saValueArray[i]);
            }
        }
        
        return cchaArray;
    }
    
    public String getHandleArrayValue(CobocContentHandle[] cchaArray, boolean bGetId) {
        StringBuffer sbRes = new StringBuffer(50);
        
        for (CobocContentHandle cchHandle : cchaArray)
        {
            String sValue = bGetId ? cchHandle.getObjectId() : cchHandle.getKey();
            
            if (sValue == null || sValue.length() == 0) {
                continue;
            }
            
            if (sbRes.length() > 0) {
                sbRes.append(",");
            }
            
            sbRes.append(sValue);
        }
        
        return sbRes.length() > 0 ? sbRes.toString() : null;
    }
    
    /**
     * Sets the sourceId.
     * @param sId Source ID.
     */
    public void setSourceId(String sId) {
        if (sId == null) {
            cchaSourceHandles = null;
            return;
        }
        
        cchaSourceHandles = updateHandleArray(cchaSourceHandles, sId, true);
    }
    
    /**
     * Returns the sourceId;
     * @return Source ID.
     */
    public String getSourceId() {
        if (cchaSourceHandles == null) {
            return null;
        }
    
        return getHandleArrayValue(cchaSourceHandles, true);
    }
    
    /**
     * Sets the sourceKey.
     * @param sId Source key.
     */
    public void setSourceKey(String sKey) {
        if (sKey == null) {
            cchaSourceHandles = null;
            return;
        }
        
        cchaSourceHandles = updateHandleArray(cchaSourceHandles, sKey, false);
    }
    
    /**
     * Returns the sourceKey;
     * @return Source key.
     */
    public String getSourceKey() {
        if (cchaSourceHandles == null) {
            return null;
        }
    
        return getHandleArrayValue(cchaSourceHandles, false);
    }   
    
    /**
     * Sets the targetId.
     * @param sId Target ID.
     */
    public void setTargetId(String sId) {
        if (sId == null) {
            cchaTargetHandles = null;
            return;
        }
        
        cchaTargetHandles = updateHandleArray(cchaTargetHandles, sId, true);
    }
    
    /**
     * Returns the targetId;
     * @return Target ID.
     */
    public String getTargetId() {
        if (cchaTargetHandles == null) {
            return null;
        }
    
        return getHandleArrayValue(cchaTargetHandles, true);
    }
    
    /**
     * Sets the targetKey.
     * @param sId Target key.
     */
    public void setTargetKey(String sKey) {
        if (sKey == null) {
            cchaTargetHandles = null;
            return;
        }
        
        cchaTargetHandles = updateHandleArray(cchaTargetHandles, sKey, false);
    }
    
    /**
     * Returns the targetKey;
     * @return Target key.
     */
    public String getTargetKey() {
        if (cchaTargetHandles == null) {
            return null;
        }
    
        return getHandleArrayValue(cchaTargetHandles, false);
    }

    /**
     * Returns the mapId.
     *
     * @return Returns the mapId.
     */
    public String getMapId()
    {
        return getObjectId();
    }

    /**
     * The mapId to set.
     *
     * @param aMapId The mapId to set.
     */
    public void setMapId(String aMapId)
    {
        // Sometimes the MAP_ID not set so that can mess up the object ID.
        if (aMapId != null && aMapId.trim().length() > 0) {
            setObjectId(aMapId);
        }
    }

    /**
     * Returns the version.
     *
     * @return Returns the version.
     */
    public String getVersion()
    {
        return sVersion;
    }

    /**
     * The version to set.
     *
     * @param aVersion The version to set.
     */
    public void setVersion(String aVersion)
    {
        sVersion = aVersion;
    }
}
