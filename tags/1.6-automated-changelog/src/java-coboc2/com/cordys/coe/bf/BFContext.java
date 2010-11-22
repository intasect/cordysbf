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
package com.cordys.coe.bf;

import java.util.HashMap;
import java.util.Map;

import com.cordys.coe.bf.config.BFConfig;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentStore;
import com.cordys.coe.bf.content.coboc.impl.bcp42_c1.CobocTemplateRegistry;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.util.log.LogInterface;
import com.cordys.coe.util.log.StdoutLogger;
import com.cordys.tools.ant.cm.EBcpVersion;

/**
 * A context class for all BF operations. Contains all configured information
 * for accessing the ECX and file system as well as all read objects.
 *
 * @author mpoyhone
 */
public class BFContext {
    private BFConfig bcConfig = new BFConfig();
    private Map<EContentSourceType, IContentSource> mContentSourceMap = new HashMap<EContentSourceType, IContentSource>();
    private Map<EContentType, IContentStore> mContentStoreMap = new HashMap<EContentType, IContentStore>();
    private LogInterface liLogger = new StdoutLogger();
    private CobocTemplateRegistry ctrCobocTemplateRegistry = new CobocTemplateRegistry(this);
    
    public void setContentStore(EContentType ctType, IContentStore csStore) {
        mContentStoreMap.put(ctType, csStore);
    }
    
    public IContentStore getContentStore(EContentType ctType) {
        return mContentStoreMap.get(ctType);
    }
    
    public IContentSource getContentSource(EContentSourceType cstType) throws BFException {
        return mContentSourceMap.get(cstType);
    }
    
    public void registerContentSource(IContentSource csSource) {
        mContentSourceMap.put(csSource.getType(), csSource);
    }
    
    public void unregisterContentSource(IContentSource csSource) {
        mContentSourceMap.remove(csSource.getType());
    }
    
	public void registerContent(IContent cContent) throws BFException {
		if (cContent == null) {
			throw new BFException("Content is null");
		}
		
		if (cContent.getHandle() == null) {
			throw new BFException("Content handle not set.");
		}

		if (cContent.getType() == null) {
			throw new BFException("Content type not set.");
		}
        
        IContentStore csStore = getContentStore(cContent.getType());
        
        if (csStore == null) {
            throw new BFException("No content store is registered for content type " + cContent.getType());
        }
        
        csStore.insertObject(cContent);
        cContent.setContext(this);
	}
	
	public void unregisterContent(IContent cContent) throws BFException {
		if (cContent == null) {
			throw new BFException("Content is null");
		}
		
		if (cContent.getHandle() == null) {
			throw new BFException("Content handle not set.");
		}

		if (cContent.getType() == null) {
			throw new BFException("Content type not set.");
		}
		
        IContentStore csStore = getContentStore(cContent.getType());
        
        if (csStore == null) {
            throw new BFException("No content store is registered for content type " + cContent.getType());
        }
        
        csStore.removeObject(cContent.getHandle());
        cContent.setContext(null);

	}
	
	public IContent findContent(IContentHandle hHandle) {
        if (hHandle == null) {
            throw new IllegalArgumentException("Content is null");
        }
        
        if (hHandle.getContentType() == null) {
            throw new IllegalArgumentException("Content type not set.");
        }
        
        IContentStore csStore = getContentStore(hHandle.getContentType());
        
        if (csStore == null) {
            throw new IllegalStateException("No content store is registered for content type " + hHandle.getContentType());
        }
        
        return csStore.findObject(hHandle);
	}
    
     public void renameObjectReferences(IContentHandle chFromHandle, IContentHandle chToHandle) throws BFException {
         for (IContentStore csStore : mContentStoreMap.values())
         {
             csStore.renameObjectReferences(chFromHandle, chToHandle);
         }
         
         IContentStore csStore = getContentStore(chFromHandle.getContentType());
         
         if (csStore == null) {
             throw new BFException("No content store is registered for content type " + chFromHandle.getContentType().getLogName());
         }
         
         IContent cContent = csStore.removeObject(chFromHandle);
         
         if (cContent != null) {
             csStore.insertObject(cContent);
         }
     }
	
    public BFConfig getConfig() {
        return bcConfig;
    }

    /**
     * Returns the logger object.
     *
     * @return Returns the logger object.
     */
    public LogInterface getLogger()
    {
        return liLogger;
    }

    /**
     * Sets the logger object.
     *
     * @param liLogger The logger object to set.
     */
    public void setLiLogger(LogInterface liLogger)
    {
        this.liLogger = liLogger;
    }

    /**
     * Returns the cobocTemplateRegistry.
     *
     * @return Returns the cobocTemplateRegistry.
     */
    public CobocTemplateRegistry getCobocTemplateRegistry()
    {
        return ctrCobocTemplateRegistry;
    }

    /**
     * The cobocTemplateRegistry to set.
     *
     * @param aCobocTemplateRegistry The cobocTemplateRegistry to set.
     */
    public void setCobocTemplateRegistry(
            CobocTemplateRegistry aCobocTemplateRegistry)
    {
        ctrCobocTemplateRegistry = aCobocTemplateRegistry;
    }
    
    /**
     * Compares the given major version to the configured BCP version and
     * returns <code>true</code> if the given version
     * is later or equal than this version.
     * @param Version to be compared.
     * @return Comparison result.
     */
    public boolean isVersionLaterThan(EBcpVersion version)
    {
        if (bcConfig == null || bcConfig.getVersionInfo() == null) {
            throw new IllegalStateException("Version information is not set.");
        }
        
        return bcConfig.getVersionInfo().isLaterThan(version);
    }
}
