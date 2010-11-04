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

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;

import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.bf.utils.CobocUtils;
import com.cordys.coe.bf.utils.LDAPStringUtil;
import com.cordys.coe.bf.utils.XmlStructure;
import com.cordys.tools.ant.cm.EBcpVersion;

/**
 * Class containing CoBOC process template information.
 *
 * @author mpoyhone
 */
public class ProcessTemplate extends ObjectInstanceBase
{
    /**
     * Holds the XPath to replace the roles of the BPML with the proper
     * user who is synchronizing the BPML.
     */
    private static AXIOMXPath s_axUserPath;
    /**
     * Holds the XPath to BPMN key in C2.
     */
    private static AXIOMXPath s_axBpmnKeyPath_C2;    
    /**
     * Holds the XPath to BPMN key in C3.
     */
    private static AXIOMXPath s_axBpmnKeyPath_C3;    
    /**
     * Holds the XPath to BPML key.
     */
    private static AXIOMXPath s_axBpmlKeyPath;       
    /**
     * Holds the XPath to replace the organization in the schema
     * location of the includes in an XSD.
     */
    private static AXIOMXPath s_axSchemaLocation;
    /**
     * Holds the regex for the organization in the schema location.
     */
    private static Pattern s_pOrganization = Pattern.compile("organization=([^&])+");
    /**
     * Holds the regex for the servername.
     */
    private static Pattern s_pServer = Pattern.compile("^(http[s]*)://([^:/]+)");

    static
    {
        try
        {
            // Build the XmlStructure subcriber paths.
            s_axUserPath = new AXIOMXPath("//ns1:package//ns1:user");
            s_axUserPath.addNamespace("ns1", "http://schemas.xmlsoap.org/wsdl/");

            s_axSchemaLocation = new AXIOMXPath("//ns1:package//ns1:definitions/ns1:types/xsd:schema/xsd:include");
            s_axSchemaLocation.addNamespace("xsd",
                                            "http://www.w3.org/2001/XMLSchema");
            s_axSchemaLocation.addNamespace("ns1",
                                            "http://schemas.xmlsoap.org/wsdl/");
            
            s_axBpmnKeyPath_C2 = new AXIOMXPath("./messagemap/instanceProperties/bpmn");

            s_axBpmnKeyPath_C3 = new AXIOMXPath("./ns1:messagemap/ns1:instanceProperties/ns1:bpmn");
            s_axBpmnKeyPath_C3.addNamespace("ns1", "http://schemas.cordys.com/bpm/instance/1.0");            

            s_axBpmlKeyPath = new AXIOMXPath("./@BPMRevision");
        }
        catch (JaxenException e)
        {
            // Just domp it. This should not happen.
            System.err.println("Unable to parse XPath.");
            e.printStackTrace();
        }
    }

    /**
     * Holds the XML structure that holds the actual BPML for this
     * process instance.
     */
    private XmlStructure xProcessTemplateData;
    /**
     * Handle object for the BPMN object.
     */
    protected CobocContentHandle chBpmnHandle;
    /**
     * Handle object for the BPML object.
     */
    protected CobocContentHandle chBpmlHandle;
    /**
     * Used while reading the BPML from file system.
     */
    protected ProcessBpml cachedBpmlObject;
    /**
     * Used while reading the BPMN from file system.
     */
    protected ProcessBpmn cachedBpmnObject;
    
    /**
     * Constructor for ProcessTemplate
     */
    public ProcessTemplate()
    {
        super(EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
        this.chBpmnHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_PROCESSBPMN);
        this.chBpmlHandle = new CobocContentHandle(EContentType.COBOC_FOLDERS_PROCESSBPML);
    }

    /**
     * This method will make sure that all roles that are assigned will
     * be replaced with the correct roles of the organization to which is
     * being deployed. It will look in all user tags to see to whom an
     * activity is assigned: an actual user, an organizational role or an ISV role.<br>
     * Also the XSD includes contain the organization name. So that one needs
     * to be replaced as well. Otherwise it's not possible to generate web
     * services from the BPM.
     *
     * @see com.cordys.coe.bf.content.types.ContentBase#createWriteVersion(com.cordys.coe.bf.content.base.IContentSource)
     */
    public IContent createWriteVersion(IContentSource csDest)
                                throws BFException
    {
        ProcessTemplate rWriteVersion = (ProcessTemplate) super.createWriteVersion(csDest);

        if (csDest.getType() == EContentSourceType.BCP)
        {
            XmlStructure xsProcessData = rWriteVersion.getProcessTemplateData();

            //First we do the users and roles.
            for (OMNode onNode : xsProcessData.selectNodes(s_axUserPath))
            {
                String sValue = AxiomUtils.getNodeText(onNode);

                //Now we have the role assignment. We need to figure out what kind of role it's using.
                if ((sValue != null) && (sValue.length() > 0))
                {
                    if (sValue.indexOf(",cn=organizational roles,") > -1)
                    {
                        //Organizational role
                        sValue = LDAPStringUtil.replaceRoleDn(sValue,
                                                              bcContext.getConfig()
                                                                       .getOrganizationDn());
                    }
                    else if (sValue.indexOf(",cn=organizational users,") > -1)
                    {
                        //Organizational user
                        sValue = LDAPStringUtil.replaceOrganization(sValue,
                                                                    bcContext.getConfig()
                                                                             .getOrganizationDn());
                    }
                    else if (sValue.indexOf(",cn=cordys,o=") > -1)
                    {
                        //An ISV role.
                        sValue = LDAPStringUtil.replaceRoleDn(sValue,
                                                              bcContext.getConfig()
                                                                       .getOrganizationDn());
                    }
                    AxiomUtils.setNodeText(onNode, sValue);
                }
            }

            //Now do the schema locations
            try
            {
                List<OMNode> alTemp = xsProcessData.selectNodes(s_axSchemaLocation);

                for (OMNode onNode : alTemp)
                {
                    OMAttribute aSchemaLocation = ((OMElement) onNode).getAttribute(new QName("",
                                                                                              "schemaLocation"));

                    if (aSchemaLocation != null)
                    {
                        String sLocation = aSchemaLocation.getAttributeValue();

                        if (bcContext.getLogger().isDebugEnabled())
                        {
                            bcContext.getLogger()
                                     .debug("Found a schemaLocation: " +
                                            sLocation);
                        }

                        if ((sLocation != null) && (sLocation.length() > 0))
                        {
                            //Now we need to replace the organization. We'll need a regex to find the organization
                            //But we need to not only replace the organization name, but also the servername if specified.
                            Matcher mMatcher = s_pOrganization.matcher(sLocation);
                            sLocation = mMatcher.replaceAll("organization=" +
                                                            bcContext.getConfig()
                                                                     .getOrganizationDn());

                            mMatcher = s_pServer.matcher(sLocation);

                            //Now get the servername of where we're deploying to.
                            URL uTemp = bcContext.getConfig().getSoapConfig()
                                                 .getWebGatewayUrl();
                            String sReplacement = uTemp.getProtocol() + "://" +
                                                  uTemp.getHost();

                            if (uTemp.getPort() > -1)
                            {
                                sReplacement += (":" + uTemp.getPort());
                            }

                            sLocation = mMatcher.replaceAll(sReplacement);
                        }

                        if (bcContext.getLogger().isDebugEnabled())
                        {
                            bcContext.getLogger()
                                     .debug("New schema location: " +
                                            sLocation);
                        }

                        aSchemaLocation.setAttributeValue(sLocation);
                    }
                    else
                    {
                        if (bcContext.getLogger().isDebugEnabled())
                        {
                            bcContext.getLogger()
                                     .debug("Ignoring include because schemaLocation was not found: " +
                                            onNode.toString());
                        }
                    }
                }
            }
            catch (Exception e)
            {
                bcContext.getLogger().error("Error finding the includes", e);
            }
        }

        return rWriteVersion;
    }

    /**
     * Returns the processTemplateData.
     *
     * @return Returns the processTemplateData.
     */
    public XmlStructure getProcessTemplateData()
    {
        return xProcessTemplateData;
    }

    /**
     * @see com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase#getReferencedContent()
     */
    public Collection<IContentHandle> getReferencedContent()
    {
        Collection<IContentHandle> lSuperContent = super.getReferencedContent();

        if ((chBpmnHandle != null) && chBpmnHandle.isSet())
        {
            lSuperContent.add(chBpmnHandle);
        }
        
        if ((chBpmlHandle != null) && chBpmlHandle.isSet())
        {
            lSuperContent.add(chBpmlHandle);
        }
        
        return lSuperContent;
    }

    /**
     * The processTemplateData to set.
     *
     * @param aProcessTemplateData The processTemplateData to set.
     */
    public void setProcessTemplateData(XmlStructure aProcessTemplateData)
    {
        xProcessTemplateData = aProcessTemplateData;
    }

    /**
     * 
     * @see com.cordys.coe.bf.content.types.ContentBase#updateReferences(com.cordys.coe.bf.content.base.IContentSource)
     */
    public void updateReferences(IContentSource csSrc)
                          throws BFException
    {
        boolean isC3 = bcContext.getConfig().getVersionInfo().isLaterThan(EBcpVersion.BCP42_C3);
        boolean handleBpmn = isC3 || csSrc.getType() != EContentSourceType.ISV;
        boolean handleBpml = isC3;
        
        if (chBpmnHandle != null)
        {
            if (handleBpmn) {
                if (xProcessTemplateData != null) {
                    String key = null;
                    if (isC3) {
                        key = xProcessTemplateData.getValue(s_axBpmnKeyPath_C3, false);
                    } else {
                        key = xProcessTemplateData.getValue(s_axBpmnKeyPath_C2, false);
                    }
                    
                    if (key != null && key.length() > 0)
                    {
                        // C2.005 has paths like this:
                        // /Business Processes/BPMN//Business Processes/
                        key = key.replaceAll("//", "/");
                        
                        chBpmnHandle.setKey(key);
                    }
                }
                
                if (csSrc.getType() == EContentSourceType.BCP || 
                    csSrc.getType() == EContentSourceType.ISV) {
                    CobocUtils.updateCobocHandle(bcContext, chBpmnHandle);
                }
            } else {
                chBpmnHandle.setKey(null);
                chBpmnHandle.setObjectId(null);
            }
        }
        
        if (chBpmlHandle != null)
        {
            if (handleBpml) {
                if (xProcessTemplateData != null) {
                    chBpmlHandle.setKey(xProcessTemplateData.getValue(s_axBpmlKeyPath, false));
                }
                
                if (csSrc.getType() == EContentSourceType.BCP || 
                    csSrc.getType() == EContentSourceType.ISV) {
                    CobocUtils.updateCobocHandle(bcContext, chBpmlHandle);
                }
            } else {
                chBpmlHandle.setKey(null);
                chBpmlHandle.setObjectId(null);
            }
        }
        
        super.updateReferences(csSrc);
    }
    
    public String getProcessName()
    {
        String key = getKey();
        
        if (key != null && key.length() > 2) {
            int index = key.indexOf('/', 1);
            
            if (index >= 0 && index < key.length()) {
                key = key.substring(index + 1);
            }
        }
        
        return key;
    }
    
    public void setBpmn(ProcessBpmn bpmn) throws BFException
    {
        chBpmnHandle.copyFrom(bpmn.getHandle());
        cachedBpmnObject = bpmn;
    }
    
    public void setBpml(ProcessBpml bpml) throws BFException
    {
        chBpmlHandle.copyFrom(bpml.getHandle());
        cachedBpmlObject = bpml;
    }

    public ProcessBpmn getBpmn()
    {
        ProcessBpmn res = null;
        
        if (bcContext != null) {
            res = (ProcessBpmn) bcContext.findContent(chBpmnHandle);
        } else {
            res = cachedBpmnObject;
        }
        
        if (res == null) {
            // We cannot return a null because the bean property will not be parsed correctly.
            res = new ProcessBpmn();
        } else {
            cachedBpmnObject = res;
        }

        return res;
    }

    public ProcessBpml getBpml()
    {
        ProcessBpml res = null;
        
        if (bcContext != null) {
            res = (ProcessBpml) bcContext.findContent(chBpmlHandle);
        } else {
            res = cachedBpmlObject;
        }
        
        if (res == null) {
            // We cannot return a null because the bean property will not be parsed correctly.
            res = new ProcessBpml();
        } else {
            cachedBpmlObject = res;
        }

        return res;
    }
    
    public IContentHandle getBpmlHandle() {
        return chBpmlHandle;
    }
    
    public IContentHandle getBpmnHandle() {
        return chBpmnHandle;
    }

}

