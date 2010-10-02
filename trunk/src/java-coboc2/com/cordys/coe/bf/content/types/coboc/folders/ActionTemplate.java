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
 * Class containing CoBOC action template information.
 *
 * @author mpoyhone
 */
public class ActionTemplate extends ObjectInstanceBase
{
    private String sActionType;
    private String sActionName;
    private String sActionRepositoryName;
    private String sActionTemplateDescription;
    private String sActionRepositoryVersion;
    private XmlStructure xsParameters;
    private XmlStructure xsRuleActionXML;
    private XmlStructure xsMetainfo;
    private String usexpath;
    
    // Coboc Object Insert/Update/Delete
    private String sCobocAssignments;
    private CobocContentHandle cchCobocTemplateHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_TEMPLATE);

    // Inbox
    private String sInboxUser;
    private String sInboxRole;
    private String sInboxSubject;
    private String sInboxUrlToLoad;
    private String sInboxDescription;
    private String sInboxMessage;
    
    // Abort transaction
    private String sAbortMessage;
    
    // Assignment
    private String sAssignmentExpression;
    
    // Web service
    private String sMethodName;
    private String sMethodNameSpace;
    private String sSoapRequest;
    
    // BPM
    private String sProcessName;
    private String sProcessInputMessage;
    
    /**
     * Constructor for ActionTemplate
     */
    public ActionTemplate()
    {
        super(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE);
    }
    
    /**
     * @see com.cordys.coe.bf.content.types.ContentBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc) throws BFException
    {
        super.updateReferences(csSrc);
        
        if (csSrc.getType() == EContentSourceType.BCP ||
            csSrc.getType()  == EContentSourceType.ISV) {
            CobocUtils.updateCobocHandleId(bcContext, cchCobocTemplateHandle);
        }
    }

    /**
     * @see com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        Collection<IContentHandle> lSuperContent = super.getReferencedContent();
        List<IContentHandle> lRes = new LinkedList<IContentHandle>();
        
        if (cchCobocTemplateHandle.isSet()) {
            lRes.add(cchCobocTemplateHandle);
        }
        
        if (lSuperContent != null) {
            lRes.addAll(lSuperContent);
        }
        
        return lRes;
    }

    /**
     * Returns the actionName.
     *
     * @return Returns the actionName.
     */
    public String getActionName()
    {
        return sActionName;
    }

    /**
     * The actionName to set.
     *
     * @param aActionName The actionName to set.
     */
    public void setActionName(String aActionName)
    {
        sActionName = aActionName;
    }

    /**
     * Returns the actionRepositoryVersion.
     *
     * @return Returns the actionRepositoryVersion.
     */
    public String getActionRepositoryVersion()
    {
        return sActionRepositoryVersion;
    }

    /**
     * The actionRepositoryVersion to set.
     *
     * @param aActionRepositoryVersion The actionRepositoryVersion to set.
     */
    public void setActionRepositoryVersion(String aActionRepositoryVersion)
    {
        sActionRepositoryVersion = aActionRepositoryVersion;
    }

    /**
     * Returns the actionTemplateDescription.
     *
     * @return Returns the actionTemplateDescription.
     */
    public String getActionTemplateDescription()
    {
        return sActionTemplateDescription;
    }

    /**
     * The actionTemplateDescription to set.
     *
     * @param aActionTemplateDescription The actionTemplateDescription to set.
     */
    public void setActionTemplateDescription(String aActionTemplateDescription)
    {
        sActionTemplateDescription = aActionTemplateDescription;
    }

    /**
     * Returns the actionType.
     *
     * @return Returns the actionType.
     */
    public String getActionType()
    {
        return sActionType;
    }

    /**
     * The actionType to set.
     *
     * @param aActionType The actionType to set.
     */
    public void setActionType(String aActionType)
    {
        sActionType = aActionType;
    }

    /**
     * Returns the assignments.
     *
     * @return Returns the assignments.
     */
    public String getCobocAssignments()
    {
        return sCobocAssignments;
    }

    /**
     * The assignments to set.
     *
     * @param aAssignments The assignments to set.
     */
    public void setCobocAssignments(String aAssignments)
    {
        sCobocAssignments = aAssignments;
    }

    /**
     * Returns the parameters.
     *
     * @return Returns the parameters.
     */
    public XmlStructure getParameters()
    {
        return xsParameters;
    }

    /**
     * The parameters to set.
     *
     * @param aParameters The parameters to set.
     */
    public void setParameters(XmlStructure aParameters)
    {
        xsParameters = aParameters;
    }
    
    /**
     * Returns the metainfo.
     *
     * @return Returns the metainfo.
     */
    public XmlStructure getMetainfo()
    {
        return xsMetainfo;
    }

    /**
     * The metainfo to set.
     *
     * @param aMetainfo The metainfo to set.
     */
    public void setMetainfo(XmlStructure aMetainfo)
    {
        xsMetainfo = aMetainfo;
    }
    
    /**
     * Returns the ruleActionXML.
     *
     * @return Returns the ruleActionXML.
     */
    public XmlStructure getRuleActionXML()
    {
        return xsRuleActionXML;
    }

    /**
     * The ruleActionXML to set.
     *
     * @param aRuleActionXML The ruleActionXML to set.
     */
    public void setRuleActionXML(XmlStructure aRuleActionXML)
    {
        xsRuleActionXML = aRuleActionXML;
    }

    /**
     * Returns the templateId.
     *
     * @return Returns the templateId.
     */
    public String getCobocTemplateId()
    {
        return cchCobocTemplateHandle.getObjectId();
    }

    /**
     * The templateId to set.
     *
     * @param aTemplateId The templateId to set.
     */
    public void setCobocTemplateId(String aTemplateId)
    {
        cchCobocTemplateHandle.setObjectId(aTemplateId);
    }

    /**
     * Returns the templateKey.
     *
     * @return Returns the templateKey.
     */
    public String getCobocTemplateKey()
    {
        return cchCobocTemplateHandle.getKey();
    }

    /**
     * The templateKey to set.
     *
     * @param aTemplateKey The templateKey to set.
     */
    public void setCobocTemplateKey(String aTemplateKey)
    {
        cchCobocTemplateHandle.setKey(aTemplateKey);
    }

    /**
     * Returns the inboxDescription.
     *
     * @return Returns the inboxDescription.
     */
    public String getInboxDescription()
    {
        return sInboxDescription;
    }

    /**
     * The inboxDescription to set.
     *
     * @param aInboxDescription The inboxDescription to set.
     */
    public void setInboxDescription(String aInboxDescription)
    {
        sInboxDescription = aInboxDescription;
    }

    /**
     * Returns the inboxMessage.
     *
     * @return Returns the inboxMessage.
     */
    public String getInboxMessage()
    {
        return sInboxMessage;
    }

    /**
     * The inboxMessage to set.
     *
     * @param aInboxMessage The inboxMessage to set.
     */
    public void setInboxMessage(String aInboxMessage)
    {
        sInboxMessage = aInboxMessage;
    }

    /**
     * Returns the inboxRole.
     *
     * @return Returns the inboxRole.
     */
    public String getInboxRole()
    {
        return sInboxRole;
    }

    /**
     * The inboxRole to set.
     *
     * @param aInboxRole The inboxRole to set.
     */
    public void setInboxRole(String aInboxRole)
    {
        sInboxRole = aInboxRole;
    }

    /**
     * Returns the inboxSubject.
     *
     * @return Returns the inboxSubject.
     */
    public String getInboxSubject()
    {
        return sInboxSubject;
    }

    /**
     * The inboxSubject to set.
     *
     * @param aInboxSubject The inboxSubject to set.
     */
    public void setInboxSubject(String aInboxSubject)
    {
        sInboxSubject = aInboxSubject;
    }

    /**
     * Returns the inboxUrlToLoad.
     *
     * @return Returns the inboxUrlToLoad.
     */
    public String getInboxUrlToLoad()
    {
        return sInboxUrlToLoad;
    }

    /**
     * The inboxUrlToLoad to set.
     *
     * @param aInboxUrlToLoad The inboxUrlToLoad to set.
     */
    public void setInboxUrlToLoad(String aInboxUrlToLoad)
    {
        sInboxUrlToLoad = aInboxUrlToLoad;
    }

    /**
     * Returns the inboxUser.
     *
     * @return Returns the inboxUser.
     */
    public String getInboxUser()
    {
        return sInboxUser;
    }

    /**
     * The inboxUser to set.
     *
     * @param aInboxUser The inboxUser to set.
     */
    public void setInboxUser(String aInboxUser)
    {
        sInboxUser = aInboxUser;
    }

    /**
     * Returns the actionRepositoryName.
     *
     * @return Returns the actionRepositoryName.
     */
    public String getActionRepositoryName()
    {
        return sActionRepositoryName;
    }

    /**
     * The actionRepositoryName to set.
     *
     * @param aActionRepositoryName The actionRepositoryName to set.
     */
    public void setActionRepositoryName(String aActionRepositoryName)
    {
        sActionRepositoryName = aActionRepositoryName;
    }

    /**
     * Returns the abortMessage.
     *
     * @return Returns the abortMessage.
     */
    public String getAbortMessage()
    {
        return sAbortMessage;
    }

    /**
     * The abortMessage to set.
     *
     * @param aAbortMessage The abortMessage to set.
     */
    public void setAbortMessage(String aAbortMessage)
    {
        sAbortMessage = aAbortMessage;
    }

    /**
     * Returns the assignmentExpression.
     *
     * @return Returns the assignmentExpression.
     */
    public String getAssignmentExpression()
    {
        return sAssignmentExpression;
    }

    /**
     * The assignmentExpression to set.
     *
     * @param aAssignmentExpression The assignmentExpression to set.
     */
    public void setAssignmentExpression(String aAssignmentExpression)
    {
        sAssignmentExpression = aAssignmentExpression;
    }

    /**
     * Returns the methodName.
     *
     * @return Returns the methodName.
     */
    public String getMethodName()
    {
        return sMethodName;
    }

    /**
     * The methodName to set.
     *
     * @param aMethodName The methodName to set.
     */
    public void setMethodName(String aMethodName)
    {
        sMethodName = aMethodName;
    }

    /**
     * Returns the methodNameSpace.
     *
     * @return Returns the methodNameSpace.
     */
    public String getMethodNameSpace()
    {
        return sMethodNameSpace;
    }

    /**
     * The methodNameSpace to set.
     *
     * @param aMethodNameSpace The methodNameSpace to set.
     */
    public void setMethodNameSpace(String aMethodNameSpace)
    {
        sMethodNameSpace = aMethodNameSpace;
    }

    /**
     * Returns the soapRequest.
     *
     * @return Returns the soapRequest.
     */
    public String getSoapRequest()
    {
        return sSoapRequest;
    }

    /**
     * The soapRequest to set.
     *
     * @param aSoapRequest The soapRequest to set.
     */
    public void setSoapRequest(String aSoapRequest)
    {
        sSoapRequest = aSoapRequest;
    }

    /**
     * Returns the processInputMessage.
     *
     * @return Returns the processInputMessage.
     */
    public String getProcessInputMessage()
    {
        return sProcessInputMessage;
    }

    /**
     * The processInputMessage to set.
     *
     * @param aProcessInputMessage The processInputMessage to set.
     */
    public void setProcessInputMessage(String aProcessInputMessage)
    {
        sProcessInputMessage = aProcessInputMessage;
    }

    /**
     * Returns the processName.
     *
     * @return Returns the processName.
     */
    public String getProcessName()
    {
        return sProcessName;
    }

    /**
     * The processName to set.
     *
     * @param aProcessName The processName to set.
     */
    public void setProcessName(String aProcessName)
    {
        sProcessName = aProcessName;
    }

    /**
     * Returns the usexpath.
     *
     * @return Returns the usexpath.
     */
    public String getUsexpath()
    {
        return usexpath;
    }

    /**
     * Sets the usexpath.
     *
     * @param usexpath The usexpath to be set.
     */
    public void setUsexpath(String usexpath)
    {
        this.usexpath = usexpath;
    }  
}
