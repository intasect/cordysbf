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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.xmlstore.XmlStoreContentBase;
import com.cordys.coe.bf.content.types.xmlstore.XmlStoreContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.bf.utils.CobocUtils;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * Class containing CoBOC action template XForm information.
 *
 * @author mpoyhone
 */
public class ActionTemplateXForm extends XmlStoreContentBase
{
    /**
     * Contains the handle of the action template.
     */
    private CobocContentHandle cchActionTemplateHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE);
    /**
     * Contains the XForm XML.
     */
    private XmlStructure xsXformContent;
    /**
     * XPath for the last key in XForm XML.
     */
    protected static AXIOMXPath axXFormKeyPath;   
    /**
     * XPath for the last key in XForm XML.
     */
    protected static AXIOMXPath axXFormContentTemplateKeyPath;   
    
    static {
        try
        {
            // Build the XmlStructure paths.
            axXFormKeyPath = new AXIOMXPath("./xformhtml/@key");
            axXFormContentTemplateKeyPath = new AXIOMXPath("./xformhtml/@at_key");
        }
        catch (JaxenException e)
        {
            // Just domp it. This should not happen.
            System.err.println("Unable to parse XPath.");
            e.printStackTrace();
        }
    } 
    
    /**
     * Constructor for ActionTemplateXForm
     */
    public ActionTemplateXForm()
    {
        super(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM);
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        super.updateReferences(csSrc);
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType()  == EContentSourceType.ISV) {
            if (cchActionTemplateHandle.getObjectId() == null) {
                if (sName != null) {
                    int iPos = sName.indexOf('.');
                   
                    if (iPos >= 0) {
                        String sId = sName.substring(0, iPos);
                        
                        cchActionTemplateHandle.setObjectId(sId);                                                
                    }
                }
            }
            
            CobocUtils.updateCobocHandle(bcContext, cchActionTemplateHandle);
        }
    }

    /**
     * @see com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        Collection<IContentHandle> lSuperContent = super.getReferencedContent();
        List<IContentHandle> lRes = new LinkedList<IContentHandle>();
        
        if (cchActionTemplateHandle != null) {
            lRes.add(cchActionTemplateHandle);
        }
        
        if (lSuperContent != null) {
            lRes.addAll(lSuperContent);
        }
        
        return lRes;
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#createWriteVersion(com.cordys.coe.bf.content.base.IContentSource)
     */
    public IContent createWriteVersion(IContentSource csDest) throws BFException
    {
        ActionTemplateXForm atxWriteVersion = (ActionTemplateXForm) super.createWriteVersion(csDest);
        
        if (csDest.getType() == EContentSourceType.BCP ||
            csDest.getType()  == EContentSourceType.ISV) {
            CobocUtils.updateCobocHandle(bcContext, cchActionTemplateHandle);
            
            String sTemplateId = cchActionTemplateHandle.getObjectId();
            
            if (sTemplateId == null) {
                throw new BFException(getLogName() + ": Template ID not set for action template.");
            }
            
            atxWriteVersion.setName(sTemplateId + ".caf");
            atxWriteVersion.setKey("/Cordys/WCP/XForms/runtime/" + getName());
            atxWriteVersion.xsXformContent.setValue(axXFormKeyPath, "/" + sTemplateId + "_vcmdemo10.caf");
            
            // Remove the content template key attribute
            OMElement oeRoot = atxWriteVersion.xsXformContent.getRootElement();
            OMAttribute oaAttrib = null;
            
            try
            {
                oaAttrib = (OMAttribute) axXFormContentTemplateKeyPath.selectSingleNode(oeRoot);
            }
            catch (JaxenException e)
            {
            }
            
            if (oaAttrib != null) {
                AxiomUtils.removeAttribute(oeRoot.getFirstElement(), oaAttrib.getLocalName());
            }
        } else if (csDest.getType() == EContentSourceType.FILESYSTEM) {
            String sFileSystemKey = getFileSystemKey();
            String sFileSystemName = getFileSystemName();
            
            if (sFileSystemKey == null || sFileSystemName == null) {
                throw new BFException(getLogName() + ": Template key not set for action template.");
            }
            
            atxWriteVersion.setName(sFileSystemName);
            atxWriteVersion.setKey(sFileSystemKey);
            atxWriteVersion.xsXformContent.setValue(axXFormKeyPath, sFileSystemKey);
            
            // Set the content template key attribute
            OMElement oeRoot = xsXformContent.getRootElement();
            OMAttribute oaAttrib = null;
            
            try
            {
                oaAttrib = (OMAttribute) axXFormContentTemplateKeyPath.selectSingleNode(oeRoot);
            }
            catch (JaxenException e)
            {
            }
            
            if (oaAttrib == null) {
                OMNamespace onNamespace = oeRoot.getNamespace();
                
                oeRoot.getFirstElement().addAttribute("at_key", cchActionTemplateHandle.getKey(), onNamespace);
            } else {
                oaAttrib.setAttributeValue(cchActionTemplateHandle.getKey());
            }
        }
        
        return this;
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContent#getHandleForContentSource(com.cordys.coe.bf.content.base.IContentSource)
     */
    public IContentHandle getHandleForContentSource(IContentSource csSource)
    {
        if (csSource.getType() == EContentSourceType.BCP ||
            csSource.getType()  == EContentSourceType.ISV) {
            // Update the handlde to be on the safe side.
            CobocUtils.updateCobocHandle(bcContext, cchActionTemplateHandle);
            
            String sTemplateId = cchActionTemplateHandle.getObjectId();
            
            if (sTemplateId != null) {
                XmlStoreContentHandle xscRes = new XmlStoreContentHandle(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM);

                xscRes.setKey("/Cordys/WCP/XForms/runtime/" + sTemplateId + ".caf");
                
                return xscRes;
            }
        } else if (csSource.getType() == EContentSourceType.FILESYSTEM) {
            String sFileSystemKey = getFileSystemKey();

            if (sFileSystemKey != null) {
                XmlStoreContentHandle xscRes = new XmlStoreContentHandle(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM);

                xscRes.setKey(sFileSystemKey);
                
                return xscRes;
            }
        }            
         
        return getHandle();
    }

    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#onLoad(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void onLoad(IContentSource csSrc) throws BFException
    {
        super.onLoad(csSrc);
        
        if (csSrc.getType() == EContentSourceType.FILESYSTEM) {
            String sTemplateKey = xsXformContent.getValue(axXFormContentTemplateKeyPath, true);
            
            cchActionTemplateHandle.setKey(sTemplateKey);
        }
    }

    /**
     * Returns the xformContent.
     *
     * @return Returns the xformContent.
     */
    public XmlStructure getXformContent()
    {
        return xsXformContent;
    }

    /**
     * The xformContent to set.
     *
     * @param aXformContent The xformContent to set.
     */
    public void setXformContent(XmlStructure aXformContent)
    {
        xsXformContent = aXformContent;
    }

    public String getFileSystemName() {
        String sTemplateKey = cchActionTemplateHandle.getKey();
        
        if (sTemplateKey == null) {
            return null;
        }
        
        int iPos = sTemplateKey.lastIndexOf('/');
        
        if (iPos < 0 || iPos >= sTemplateKey.length() - 1) {
            return null;
        }
        
        return sTemplateKey.substring(iPos + 1) + "_XForm";
    }
    
    public String getFileSystemKey() {
        String sTemplateKey = cchActionTemplateHandle.getKey();
        
        return sTemplateKey + "_XForm";
    }
}
