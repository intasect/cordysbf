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
package com.cordys.coe.bf.content.types.coboc.messagemodels;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.coboc.impl.bcp42_c1.CobocConstants;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.CobocUtils;
import com.cordys.coe.bf.utils.XmlStructure;

/**
 * Class containing C3 CoBOC email model information.
 *
 * @author mpoyhone
 */
public class EmailModel extends CobocContentBase
{
    private String templatetype;
    private String textform;
    private String subject;
    private XmlStructure emailform;
    private XmlStructure namespaceXml;
    private String detailLastModified;

    /**
     * Constructor for MessageModel
     */
    public EmailModel()
    {
        super(EContentType.COBOC_EMAIL_MODEL);
        cchParentHandle = new CobocContentHandle(EContentType.COBOC_MESSAGE_TEMPLATE);
    }
    
    /**
     * 
     * @see com.cordys.coe.bf.content.types.ContentBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        List<IContentHandle> lResList = new LinkedList<IContentHandle>();

        if ((cchParentHandle != null) && cchParentHandle.isSet())
        {
            lResList.add(cchParentHandle);
        }

        return lResList;
    }    
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        super.updateReferences(csSrc);
        
        if (bcContext == null) {
            // This has not been added to the context.
            return;
        }
        
        if (cchParentHandle != null) {
            CobocUtils.updateCobocHandle(bcContext, cchParentHandle);
            
            if (csSrc.getType() == EContentSourceType.BCP) {
                createKey();
            }            
        }
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.coboc.CobocContentBase#updateFromOldVersion(com.cordys.coe.bf.content.base.IContent, com.cordys.coe.bf.content.base.IContentSource)
     */
    @Override
    public void updateFromOldVersion(IContent oldObject, IContentSource csSrc)
            throws BFException
    {
        String oldDetailLastModified = ((EmailModel) oldObject).getDetailLastModified();
        
        if (oldDetailLastModified != null && oldDetailLastModified.length() > 0)
        {
            detailLastModified = oldDetailLastModified;
        }
        
        super.updateFromOldVersion(oldObject, csSrc);
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#onLoad(com.cordys.coe.bf.content.base.IContentSource)
     */
    @Override
    public void onLoad(IContentSource csSrc) throws BFException
    {
        super.onLoad(csSrc);
        
        if (csSrc.getType() == EContentSourceType.FILESYSTEM) {
            createKey();
        } 
    }

    public void createKey()
    {
        String parentKey = getParentKey();
        String name = getName();
        
        if (parentKey != null && name != null) {
            setKey(parentKey + "/" + name);
        }
    }    

    /**
     * Returns the templatetype.
     *
     * @return Returns the templatetype.
     */
    public String getTemplatetype()
    {
        return templatetype;
    }

    /**
     * Sets the templatetype.
     *
     * @param templatetype The templatetype to be set.
     */
    public void setTemplatetype(String templatetype)
    {
        this.templatetype = templatetype;
    }

    /**
     * Returns the textform.
     *
     * @return Returns the textform.
     */
    public String getTextform()
    {
        return textform;
    }

    /**
     * Sets the textform.
     *
     * @param textform The textform to be set.
     */
    public void setTextform(String textform)
    {
        this.textform = textform;
    }

    /**
     * Returns the emailform.
     *
     * @return Returns the emailform.
     */
    public XmlStructure getEmailform()
    {
        return emailform;
    }

    /**
     * Sets the emailform.
     *
     * @param emailform The emailform to be set.
     */
    public void setEmailform(XmlStructure emailform)
    {
        this.emailform = emailform;
    }

    /**
     * Returns the namespaceXml.
     *
     * @return Returns the namespaceXml.
     */
    public XmlStructure getNamespaceXml()
    {
        return namespaceXml;
    }

    /**
     * Sets the namespaceXml.
     *
     * @param namespaceXml The namespaceXml to be set.
     */
    public void setNamespaceXml(XmlStructure namespaceXml)
    {
        this.namespaceXml = namespaceXml;
    }
    
    public String getDetailKey()
    {
        String xmlstoreKey = String.format("%s/%s/%s", CobocConstants.XMLSTORE_EMAIL_MODEL_ROOT, 
                                           getParentId(), getName());
        
        return xmlstoreKey;
    }
    
    
    /**
     * Returns the detailLastModified.
     *
     * @return Returns the detailLastModified.
     */
    public String getDetailLastModified()
    {
        return detailLastModified;
    }

    /**
     * Sets the detailLastModified.
     *
     * @param detailLastModified The detailLastModified to be set.
     */
    public void setDetailLastModified(String detailLastModified)
    {
        this.detailLastModified = detailLastModified;
    }

    /**
     * Returns the subject.
     *
     * @return Returns the subject.
     */
    public String getSubject()
    {
        return subject;
    }

    /**
     * Sets the subject.
     *
     * @param subject The subject to be set.
     */
    public void setSubject(String subject)
    {
        this.subject = subject;
    }    
}
