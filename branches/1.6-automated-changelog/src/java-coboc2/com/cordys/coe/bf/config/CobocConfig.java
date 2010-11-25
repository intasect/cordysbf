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
package com.cordys.coe.bf.config;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.types.EContentCategory;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.tools.ant.cm.EBcpVersion;

/**
 * Contains CoBOC configuration information.
 *
 * @author mpoyhone
 */
public class CobocConfig
{
    /**
     * Specifies if content types are added to the end of the content files.
     * This fixes problems if different content are used with the same name.
     * This is <code>false</code> by default. 
     */
    private boolean bUseFileContentTypes;
    /**
     * Contains all enabled CoBOC content types. If this is not set,
     * it means the all types are enabled.
     */
    private Set<EContentType> sEnabledContentTypes = null; 
    /**
     * Used for setting the autodeploy="true" when message models
     * are packaged in an ISV package.
     */
    private boolean autoDeployMessageModels = false;
    /**
     * If <code>true</code> C3 message templates use original object ID
     */
    private boolean fixC3MessageTemplateIds = true;
    /**
     * If <code>true</code> object ID/key mapping for is used when
     * an ISV package is created. It is also written to the file system with 'fromcordys'. 
     */
    private boolean useObjectIdMappingFile = false;
    /**
     * Contains the location of the content mapping file.
     */
    private File objectIdMappingFile;
    /**
     * Contains the object ID map, if the feature is enabled.
     */
    private CobocObjectIdMap objectIdMap;

    /**
     * Returns the useFileContentTypes.
     *
     * @return Returns the useFileContentTypes.
     */
    public boolean isUseFileContentTypes()
    {
        return bUseFileContentTypes;
    }

    /**
     * The useFileContentTypes to set.
     *
     * @param aUseFileContentTypes The useFileContentTypes to set.
     */
    public void setUseFileContentTypes(boolean aUseFileContentTypes)
    {
        bUseFileContentTypes = aUseFileContentTypes;
    }
    
    /**
     * Checks if the content type is enabled.
     * @param ctType Type.
     * @return <code>true</code> if the content type is enabled.
     */
    public boolean isContentEnabled(EContentType ctType) {
        if (sEnabledContentTypes == null) {
            return true;
        }
        
        return sEnabledContentTypes.contains(ctType);
    }
    
    /**
     * Checks if the any of the content types in the given content category is enabled.
     * @param ccCategory Category.
     * @return <code>true</code> if the content category is enabled.
     */
    public boolean isContentCategoryEnabled(EContentCategory ccCategory) {
        if (sEnabledContentTypes == null) {
            return true;
        }
        
        for (EContentType type : ccCategory.getContentTypes()) {
            if (sEnabledContentTypes.contains(type)) {
                return true;
            }
        }
    
        return false;
    }
    
    /**
     * Returns enabled content types.
     * @return Set of enabled types.
     */
    public Set<EContentType> getEnabledContentTypes()
    {
        return sEnabledContentTypes;
    }

    /**
     * Sets the enabled content types for CoBOC content.
     * @param sTypeList Types names as a comma separated list.
     * @param bcConfig Configuration.
     * @throws BFException Thrown if the operation failed.
     */
    public void setEnabledContentTypes(String sTypeList, BFConfig bcConfig) throws BFException {
        String[] saTmp = sTypeList.split(",");
        
        for (int i = 0; i < saTmp.length; i++)
        {
            saTmp[i] = saTmp[i].trim();
        }
        
        setEnabledContentTypes(saTmp, bcConfig);
        
    }
        
    /**
     * Sets the enabled content types for CoBOC content.
     * @param saTypes Types names as array.
     * @param bcConfig Configuration.
     * @throws BFException Thrown if the operation failed.
     */
    public void setEnabledContentTypes(String[] saTypes, BFConfig bcConfig) throws BFException {
        // Create a map from all names to the types.
        Map<String, EContentType> mNameMap = new HashMap<String, EContentType>();
        
        for (EContentType ctType : EContentType.values()) {
            mNameMap.put(ctType.getConfigName(), ctType);
        }
        
        // This will contain all types configured to be removed.
        Set<EContentType> removedTypes = new HashSet<EContentType>();
        
        // Set the specified content types to the enabled set.
        if (sEnabledContentTypes == null) {
            sEnabledContentTypes = new HashSet<EContentType>();
        } else {
            sEnabledContentTypes.clear();
        }
        
        for (int i = 0; i < saTypes.length; i++)
        {
            boolean remove = false;
            String sTypeName = saTypes[i];
            
            if (sTypeName.length() > 0) {
                if (sTypeName.charAt(0) == '-') {
                    remove = true;
                    sTypeName = sTypeName.substring(1);
                } else if (sTypeName.charAt(0) == '+') {
                    sTypeName = sTypeName.substring(1);
                }
            }
            
            if ("all".equals(sTypeName)) {
                for (EContentType type : EContentCategory.COBOC.getContentTypes())
                {
                    sEnabledContentTypes.add(type);
                }
                
                continue;
            }
            
            EContentType ctType = mNameMap.get(sTypeName);
            
            if (ctType == null) {
                throw new BFException("Invalid CoBOC content type: " + sTypeName);
            }
            
            if (remove) {
                removedTypes.add(ctType);
                continue;
            }
            
            sEnabledContentTypes.add(ctType);
            
            if (ctType.getCategory() == EContentCategory.COBOC_FOLDERS ||
                ctType.getCategory() == EContentCategory.MDM) {
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_FOLDER);
            }
            
            // Some types need to be enabled if this type needs them.
            switch (ctType) {
            case COBOC_FOLDERS_SPECIAL_ATTRIBUTE :
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_TEMPLATE);
                break;
            
            case COBOC_FOLDERS_MAPPING :
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_TEMPLATE);
                break;

            case COBOC_FOLDERS_DECISIONCASE :
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_TEMPLATE);
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE);
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM);
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE);
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM);
                break;
                
            case COBOC_FOLDERS_CONDITIONTEMPLATE :
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_TEMPLATE);
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM);
                break;
                
            case COBOC_FOLDERS_ACTIONTEMPLATE :
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_TEMPLATE);
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM);
                break;

            case COBOC_FOLDERS_PROCESSBPMN :
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
                break;

            case COBOC_FOLDERS_PROCESSBPML :
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
                break;

            case COBOC_FOLDERS_PROCESSTEMPLATE :
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_PROCESSBPMN);
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_PROCESSBPML);
                break;
            
            case COBOC_INBOX_MODEL_C3 :
            case COBOC_EMAIL_MODEL :
                sEnabledContentTypes.add(EContentType.COBOC_INBOX_MODEL_C1);
                sEnabledContentTypes.add(EContentType.COBOC_MESSAGE_TEMPLATE);
                break;
                
            case COBOC_RULES_RULE :
                sEnabledContentTypes.add(EContentType.COBOC_RULES_RULEGROUP);
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_FOLDER);
                sEnabledContentTypes.add(EContentType.COBOC_FOLDERS_TEMPLATE);
                break;
                
            case COBOC_RULES_RULEGROUP :
                sEnabledContentTypes.add(EContentType.COBOC_RULES_RULE);
                break;
                
            case MDM_ENTITY_FOLDER :
            case MDM_ENTITY :
            case MDM_BACKEND :
            case MDM_MODEL :    
                // Just enable them all to be sure.
                sEnabledContentTypes.add(EContentType.MDM_ENTITY_FOLDER);
                sEnabledContentTypes.add(EContentType.MDM_ENTITY);
                sEnabledContentTypes.add(EContentType.MDM_BACKEND);
                sEnabledContentTypes.add(EContentType.MDM_MODEL);
                break;
            }
        }
        
        for (Iterator<EContentType> iter = sEnabledContentTypes.iterator(); iter.hasNext(); )
        {
            EContentType type = iter.next();
            EBcpVersion typeVersion = type.getMinimumVersion();
            
            if (typeVersion != null && ! bcConfig.getVersionInfo().isLaterThan(typeVersion)) {
                iter.remove();
            }
        }
        
        for (EContentType type : removedTypes)
        {
            sEnabledContentTypes.remove(type);
        }
    }

    /**
     * Returns the autoDeployMessageModels.
     *
     * @return Returns the autoDeployMessageModels.
     */
    public boolean isAutoDeployMessageModels()
    {
        return autoDeployMessageModels;
    }

    /**
     * Sets the autoDeployMessageModels.
     *
     * @param autoDeployMessageModels The autoDeployMessageModels to be set.
     */
    public void setAutoDeployMessageModels(boolean autoDeployMessageModels)
    {
        this.autoDeployMessageModels = autoDeployMessageModels;
    }

    /**
     * Returns the fixC3MessageTemplateIds.
     *
     * @return Returns the fixC3MessageTemplateIds.
     */
    public boolean isFixC3MessageTemplateIds()
    {
        return fixC3MessageTemplateIds;
    }

    /**
     * Sets the fixC3MessageTemplateIds.
     *
     * @param fixC3MessageTemplateIds The fixC3MessageTemplateIds to be set.
     */
    public void setFixC3MessageTemplateIds(boolean fixC3MessageTemplateIds)
    {
        this.fixC3MessageTemplateIds = fixC3MessageTemplateIds;
    }

    /**
     * Returns true if the given content is configured to use
     * the original object ID instead of a generated ID.
     * @param content Content object.
     * @return <code>true</code> if the original ID should be used.
     */
    public boolean usesOriginalId(IContent content)
    {
        if (! fixC3MessageTemplateIds) {
            return false;
        }
        
        switch (content.getType()) {
        case COBOC_EMAIL_MODEL :
        case COBOC_MESSAGE_TEMPLATE :
        case COBOC_INBOX_MODEL_C3 : 
            return true;
        }
        
        return false;
    }

    /**
     * Returns the useObjectIdMappingFile.
     *
     * @return Returns the useObjectIdMappingFile.
     */
    public boolean isUseObjectIdMappingFile()
    {
        return useObjectIdMappingFile;
    }

    /**
     * Sets the useObjectIdMappingFile.
     *
     * @param useObjectIdMappingFile The useObjectIdMappingFile to be set.
     */
    public void setUseObjectIdMappingFile(boolean useObjectIdMappingFile)
    {
        this.useObjectIdMappingFile = useObjectIdMappingFile;
        
        if (useObjectIdMappingFile) {
            if (objectIdMap == null) {
                objectIdMap = new CobocObjectIdMap();
            }
        } else {
            objectIdMap = null;
        }
    }

    /**
     * Returns the objectIdMap.
     *
     * @return Returns the objectIdMap.
     */
    public CobocObjectIdMap getObjectIdMap()
    {
        return objectIdMap;
    }

    /**
     * Returns the objectIdMappingFile.
     *
     * @return Returns the objectIdMappingFile.
     */
    public File getObjectIdMappingFile()
    {
        return objectIdMappingFile;
    }

    /**
     * Sets the objectIdMappingFile.
     *
     * @param objectIdMappingFile The objectIdMappingFile to be set.
     */
    public void setObjectIdMappingFile(File objectIdMappingFile)
    {
        this.objectIdMappingFile = objectIdMappingFile;
    }
}
