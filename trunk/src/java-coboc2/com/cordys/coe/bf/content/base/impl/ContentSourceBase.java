/**
 * Copyright 2007 Cordys R&D B.V. 
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
package com.cordys.coe.bf.content.base.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContentFilter;
import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IContentReadMethod;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IContentWriteMethod;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.databind.ObjectConverter;
import com.cordys.coe.bf.exception.BFException;

/**
 * A base class for all content sources.
 *
 * @author mpoyhone
 */
public abstract class ContentSourceBase implements IContentSource
{
    protected BFContext bcContext;
    protected Map<EContentType, Map<String, ObjectConverter>> mConverterMap = new HashMap<EContentType, Map<String, ObjectConverter>>();
    protected Map<EContentType, IContentFilter> mContentFilterMap = new HashMap<EContentType, IContentFilter>();
    protected Map<EContentType, Map<String, Class<IContentReadMethod>>> mReadMethodMap = new HashMap<EContentType, Map<String, Class<IContentReadMethod>>>();
    protected Map<EContentType, Map<String, Class<IContentWriteMethod>>> mWriteMethodMap = new HashMap<EContentType, Map<String, Class<IContentWriteMethod>>>();

    public ContentSourceBase(BFContext bcContext)
    {
        this.bcContext = bcContext;
        
        if (this.bcContext != null) {
            this.bcContext.registerContentSource(this);
        }
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#registerContentReadMethod(java.lang.String, java.util.Collection, java.lang.Class)
     */
    public void registerContentReadMethod(String version,
            EContentType[] types,
            Class<IContentReadMethod> methodClass)
    {
        for (EContentType contentType : types)
        {
            Map<String, Class<IContentReadMethod>> mSubtypeMap = mReadMethodMap.get(contentType);
            
            if (mSubtypeMap == null) {
                mSubtypeMap = new HashMap<String, Class<IContentReadMethod>>();
                mReadMethodMap.put(contentType, mSubtypeMap);
            }
            
            mSubtypeMap.put(version, methodClass);
        }
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#registerContentWriteMethod(java.lang.String, java.util.Collection, java.lang.Class)
     */
    public void registerContentWriteMethod(String version,
            EContentType[] types,
            Class<IContentWriteMethod> methodClass)
    {
        for (EContentType contentType : types)
        {
            Map<String, Class<IContentWriteMethod>> mSubtypeMap = mWriteMethodMap.get(contentType);
            
            if (mSubtypeMap == null) {
                mSubtypeMap = new HashMap<String, Class<IContentWriteMethod>>();
                mWriteMethodMap.put(contentType, mSubtypeMap);
            }
            
            mSubtypeMap.put(version, methodClass);
        }
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#getReadMethod(com.cordys.coe.bf.content.base.IContentType)
     */
    public IContentReadMethod getReadMethod(EContentType ctType) throws BFException
    {
        String version = bcContext.getConfig().getVersionInfo().getMajorVersion().getCobocString();
        Map<String, Class<IContentReadMethod>> mSubtypeMap = mReadMethodMap.get(ctType);
        Class<IContentReadMethod> methodClass = null;
        
        if (mSubtypeMap != null) {
            methodClass = mSubtypeMap.get(version);
            
            try
            {
                if (methodClass != null) {
                    Constructor<IContentReadMethod> constructor = methodClass.getConstructor(BFContext.class, IContentSource.class);
                    
                    return constructor.newInstance(bcContext, this);
                }
            }
            catch (Exception e)
            {
                throw new BFException("Unable to instantiate read method class: " + methodClass.getName());
            }
        }
        
        throw new BFException("No read method configured in " + getType().getLogName() + " for content type " + ctType.getLogName());
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#getWriteMethod(com.cordys.coe.bf.content.types.EContentType)
     */
    public IContentWriteMethod getWriteMethod(EContentType ctType) throws BFException
    {
        String version = bcContext.getConfig().getVersionInfo().getMajorVersion().getCobocString();
        Map<String, Class<IContentWriteMethod>> mSubtypeMap = mWriteMethodMap.get(ctType);
        Class<IContentWriteMethod> methodClass = null;
        
        if (mSubtypeMap != null) {
            methodClass = mSubtypeMap.get(version);

            try {
                if (methodClass != null) {
                    Constructor<IContentWriteMethod> constructor = methodClass.getConstructor(BFContext.class, IContentSource.class);
                    
                    return constructor.newInstance(bcContext, this);
                }
            }
            catch (Exception e)
            {
                throw new BFException("Unable to instantiate write method class: " + methodClass.getName());
            }                
        }
        
        throw new BFException("No write method configured in " + getType().getLogName() + " for content type " + ctType.getLogName());
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#getContentUnmarshaller(com.cordys.coe.bf.content.types.EContentType, String, String)
     */
    public IContentUnmarshaller getContentUnmarshaller(EContentType ctType, String sSubtype, String sVersion) throws BFException
    {
        if (sSubtype == null) {
            sSubtype = "";
        }
        
        if (sVersion == null) {
            sVersion = "";
        }
        
        Map<String, ObjectConverter> mSubtypeMap = mConverterMap.get(ctType);
        
        if (mSubtypeMap == null) {
            return null;
        }
        
        return mSubtypeMap.get(sSubtype + "@" + sVersion);        
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#getContentMarshaller(com.cordys.coe.bf.content.types.EContentType, String, String)
     */
    public IContentMarshaller getContentMarshaller(EContentType ctType, String sSubtype, String sVersion) throws BFException
    {
        if (sSubtype == null) {
            sSubtype = "";
        }
        
        if (sVersion == null) {
            sVersion = "";
        }
        
        Map<String, ObjectConverter> mSubtypeMap = mConverterMap.get(ctType);
        
        if (mSubtypeMap == null) {
            return null;
        }
        
        return mSubtypeMap.get(sSubtype + "@" + sVersion);  
    }
    
    public void registerConversionTemplate(EContentType ctContentType, String sSubtype, String version, ObjectConverter ocTemplate) {
        if (sSubtype == null) {
            sSubtype = "";
        }
        
        if (version == null) {
            version = "";
        }
                
        Map<String, ObjectConverter> mSubtypeMap = mConverterMap.get(ctContentType);
        
        if (mSubtypeMap == null) {
            mSubtypeMap = new HashMap<String, ObjectConverter>();
            mConverterMap.put(ctContentType, mSubtypeMap);
        }
        
        mSubtypeMap.put(sSubtype + "@" + version, ocTemplate);
    }
    
    /**
     * Set a content filter for the given content type.
     * @param ctType Content type.
     * @param cfFilter Content filter object.
     */
    public void addContentFilter(EContentType ctType, IContentFilter cfFilter) {
        mContentFilterMap.put(ctType, cfFilter);
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#getContentFilter(com.cordys.coe.bf.content.types.EContentType)
     */
    public IContentFilter getContentFilter(EContentType ctType)
    {
        return mContentFilterMap.get(ctType);
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentSource#checkForAccess(com.cordys.coe.bf.content.types.EContentType, java.lang.String, boolean)
     */
    public boolean checkForAccess(EContentType ctType, String sKey, boolean bIsFolder)
    {
        IContentFilter cfFilter = mContentFilterMap.get(ctType);
        
        if (cfFilter != null) {
            if (bIsFolder) {
                return cfFilter.checkFolderAccess(sKey);
            } 
            
            return cfFilter.checkItemAccess(sKey, ctType);
        }
        
        return true;
    }    
}
