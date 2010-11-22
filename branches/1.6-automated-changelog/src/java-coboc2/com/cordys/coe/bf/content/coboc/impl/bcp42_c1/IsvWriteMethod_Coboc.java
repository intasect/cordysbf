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
package com.cordys.coe.bf.content.coboc.impl.bcp42_c1;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.impl.ContentSourceIsv;
import com.cordys.coe.bf.content.base.impl.WriteMethodBase;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.folders.Folder;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.LogInterface;

/**
 * Write method for writing all CoBOC content to ISV format. 
 *
 * @author mpoyhone
 */
public class IsvWriteMethod_Coboc extends WriteMethodBase
{
    protected ContentSourceIsv csSource;
    protected BFContext bcContext;
    protected Set<EContentFlags> contentFlags = new HashSet<EContentFlags>();
    
    public IsvWriteMethod_Coboc(BFContext bcContext, IContentSource csSource) throws BFException {
        this.csSource = (ContentSourceIsv) csSource;
        this.bcContext = bcContext;
        this.sMethodVersion = bcContext.getConfig().getVersionInfo().getMajorVersion().getCobocString();
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#deleteObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent deleteObject(IContentHandle chHandle) throws BFException
    {
        throw new BFException("ISV write method does not support deletion.");
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#updateObject(com.cordys.coe.bf.content.base.ContentTuple)
     */
    public IContent updateObject(ContentTuple ctTuple) throws BFException
    {
        if (ctTuple.isUpdate() || ctTuple.isDelete()) {
            throw new BFException("ISV write method only does supports inserted.");
        }

        throw new BFException("ISV write method does not support single object inserts.");
    }

    /**
     * @see com.cordys.coe.bf.content.base.impl.WriteMethodBase#updateObjects(java.util.List)
     */
    public List<IContent> updateObjects(List<ContentTuple> lTupleList) throws BFException
    {
        configureContentFlags(lTupleList);
        
        IXmlDestination xdDest = csSource.getIsvDestination();
        XMLStreamWriter xswWriter = xdDest.getStreamWriter();
        
        if (xswWriter == null) {
            throw new BFException("ISV write method only supports XML stream destination.");
        }
        
        writeFoldersContent(lTupleList, xdDest, xswWriter);
        writeNotificationContent(lTupleList, xdDest, xswWriter);
        writeXmlStoreContent(lTupleList, xdDest, xswWriter);
        
        // We don't return anything.
        return null;
    }

    /**
     * Writes the folder objects to the ISV XML.
     * @param lTupleList List containing the objects to be written.
     * @param xdDest XML destination object.
     * @param xswWriter Used for writing the XML.
     * @throws BFException Thrown if the operation failed.
     */
    protected void writeFoldersContent(List<ContentTuple> lTupleList,
            IXmlDestination xdDest, XMLStreamWriter xswWriter)
            throws BFException
    {
        /*
         * CoBOC loader format is
         * <pre>
         * <CPCImporter xmlns="http://schemas.cordys.com/1.0/coboc" 
         *              loader="com.cordys.cpc.deployment.loaders.DeploymentLoader" 
         *              description="Orchestrator deployment loader" 
         *              type="ISV" 
         *              url="/cordys/cpc/deployment/deploymentwizard.htm">
         * </CPCImporter>
         * </pre>
         */
        String sIsvNamespace = "http://schemas.cordys.com/1.0/coboc";
        
        try
        {
            xswWriter.setDefaultNamespace(sIsvNamespace);

            xswWriter.writeStartElement(sIsvNamespace, "CPCImporter");
            xswWriter.writeNamespace("", sIsvNamespace);
            xswWriter.writeAttribute("loader", "com.cordys.cpc.deployment.loaders.DeploymentLoader");
            xswWriter.writeAttribute("description", "Orchestrator deployment loader");
            xswWriter.writeAttribute("type", "ISV");
            xswWriter.writeAttribute("url", "/cordys/cpc/deployment/deploymentwizard.htm");
            
            // Write all supported content from the content list.
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_FOLDER, "FolderContent", "", "folder", xdDest);
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_TEMPLATE, "TemplateContent", "", "template", xdDest);
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE, "SpecialAttributeContent", "", null, xdDest);
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_MAPPING, "MappingContent", "", "map", xdDest);
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_CONTENTMAP, "ContentMapContent", "", "contentmap", xdDest);
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_DECISIONCASE, "DecisionCaseContent", "", "decisioncase", xdDest);
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE, "ConditionTemplateContent", "", "conditiontemplate", xdDest);
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_ACTIONTEMPLATE, "ActionTemplateContent", "", "actiontemplate", xdDest);
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE, "ProcessFlowContent", "", null, xdDest);
            writeContent(lTupleList, EContentType.COBOC_RULES_RULEGROUP, "RuleGroupContent", "", null, xdDest);
            writeContent(lTupleList, EContentType.COBOC_RULES_RULE, "RuleContent", "", null, xdDest);
            writeContent(lTupleList, EContentType.COBOC_SCHEDULE_TEMPLATE, "ScheduleContent", "", null, xdDest);
            
            xswWriter.writeEndElement(); // CPCImporter
        }
        catch (XMLStreamException e)
        {
            throw new BFException("Error while writing the CoBOC ISV package XML.", e);
        }
    }

    /**
     * Writes the notification content (inbox models, etc.) to the ISV XML.
     * @param lTupleList List containing the objects to be written.
     * @param xdDest XML destination object.
     * @param xswWriter Used for writing the XML.
     * @throws BFException Thrown if the operation failed.
     */
    protected void writeNotificationContent(List<ContentTuple> lTupleList,
            IXmlDestination xdDest, XMLStreamWriter xswWriter)
            throws BFException
    {
        /*
         * Write Inbox Message Models.
         * Loader format is
         * <pre>
         * <CPCNotificationImporter xmlns="http://schemas.cordys.com/1.0/notification" description="The Orchestrator Notification Loader" loader="com.cordys.cpc.notification.content.CPCNSContentLoader" type="ISV" url="/cordys/cpc/deployment/ns_deploymentwizard.htm">
         *     <CPCNotificationContent xmlns="">
         *         <MESSAGE_MODEL xmlns="http://schemas.cordys.com/1.0/notification">
         *             ...
         *         </MESSAGE_MODEL>
         *     </CPCNotificationContent>
         * </CPCNotificationImporter>             
         * </pre>
         */
        try
        {
            if (contentFlags.contains(EContentFlags.NOTIFICATION_CONTENT)) {
                xswWriter.writeStartElement("CPCNotificationImporter");
                xswWriter.writeNamespace("", "http://schemas.cordys.com/1.0/notification");
                xswWriter.writeAttribute("loader", "com.cordys.cpc.notification.content.CPCNSContentLoader");
                xswWriter.writeAttribute("description", "The Orchestrator Notification Loader");
                xswWriter.writeAttribute("type", "ISV");
                xswWriter.writeAttribute("url", "/cordys/cpc/deployment/ns_deploymentwizard.htm");            
                
                // Write all supported content from the content list.
                writeContent(lTupleList, EContentType.COBOC_INBOX_MODEL_C1, "CPCNotificationContent", "", null, xdDest);
                
                xswWriter.writeEndElement(); // xmlstore
            }
        }
        catch (XMLStreamException e)
        {
            throw new BFException("Error while writing the CoBOC ISV package XML.", e);
        }
    }

    /**
     * Writes the CoBOC XMLStore content (e.g. action template XForms) to the ISV XML.
     * @param lTupleList List containing the objects to be written.
     * @param xdDest XML destination object.
     * @param xswWriter Used for writing the XML.
     * @throws BFException Thrown if the operation failed.
     */
    protected void writeXmlStoreContent(List<ContentTuple> lTupleList,
            IXmlDestination xdDest, XMLStreamWriter xswWriter)
            throws BFException
    {
        /*
         * Write action template XFomrs.
         * XMLStore loader format is
         * <pre>
         * <xmlstore loader="com.eibus.contentmanagement.ISVSOAPManager" description="XMLStore">
         *    <SOAP:Envelope xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/">
         *       <SOAP:Body>
         *          <UpdateXMLObject xmlns="http://schemas.cordys.com/1.0/xmlstore">
         *          </UpdateXMLObject>
         *       </SOAP:Body>
         *     </SOAP:Envelope>
         * </xmlstore>
         * </pre>
         */
        try
        {
            xswWriter.writeStartElement("xmlstore");
            xswWriter.writeAttribute("loader", "com.eibus.contentmanagement.ISVSOAPManager");
            xswWriter.writeAttribute("description", "XMLStore loader for action template XForms");
            
            // Write all supported content from the content list.
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM, null, null, null, xdDest);
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM, null, null, null, xdDest);
            
            xswWriter.writeEndElement(); // xmlstore
        }
        catch (XMLStreamException e)
        {
            throw new BFException("Error while writing the CoBOC ISV package XML.", e);
        }
    }
    
    protected void writeContent(List<ContentTuple> lTupleList, EContentType ctType, 
            String sSectionName, 
            String sSectionNamespace, 
            String sContentElementName, 
            IXmlDestination xdDest) throws BFException {
        XMLStreamWriter xswWriter = xdDest.getStreamWriter();
        LogInterface liLogger = bcContext.getLogger();
        boolean bSectionElementWritten = false;
        
        IContentMarshaller cuMarshaller = csSource.getContentMarshaller(ctType, null, getMethodVersion());
        
        if (cuMarshaller == null) {
            throw new BFException(csSource.getType().getLogName() + ": No content marshaller configured for type " + ctType.getLogName());
        }
        
        for (ContentTuple ctTuple : lTupleList)
        {
            IContent cContent = ctTuple.getWriteObject();
            BindingParameters bpParams = null;
            
            if (cContent.getType() != ctType) {
                continue;
            }
            
            switch (ctType) {
                case COBOC_FOLDERS_FOLDER :
                {
                    Folder fFolder = (Folder) cContent;
    
                    if (fFolder.getChildren().isEmpty()) {
                        // Folder has no children, so do not add it to the ISV.
                        continue;
                    }
                    
                    if (fFolder.isParentRootFolder()) {
                        // This is under this root folder, so use that key.
                        fFolder.setParentId(CobocConstants.ROOT_FOLDER_ID);
                    }
                } break;
                
                case COBOC_SCHEDULE_TEMPLATE :
                {
                    // We need to add the schedule template content in to a property
                    // called SCHEDULEXML.
                    try {
                        IContentMarshaller cuXmlMarshaller = csSource.getContentMarshaller(ctType, "SCHEDULEXML", getMethodVersion());
                        
                        if (cuXmlMarshaller == null) {
                            throw new BFException(csSource.getType().getLogName() + ": No content marshalled configured for type " + ctType.getLogName() + " and subtype SCHEDULEXML");
                        }
                        
                        StringWriter swXmlWriter = new StringWriter(1024);
                        XMLStreamWriter xswXmlStringWriter = AxiomUtils.xofXmlOuputFactory.createXMLStreamWriter(swXmlWriter);
                        IXmlDestination xdXmlStringDest = IXmlDestination.Factory.newInstance(xswXmlStringWriter);
                        
                        cuXmlMarshaller.marshalObject(cContent, xdXmlStringDest);
                        xswXmlStringWriter.flush();
                        xswXmlStringWriter.close();
                        
                        bpParams = new BindingParameters();
                        bpParams.setParameter("SCHEDULEXML", swXmlWriter.toString());
                    } 
                    catch (Exception e) {
                        throw new BFException("Unable to write the schedule template XML in to a string.", e);
                    }
                } break;
            }
            
            if (sSectionName != null && ! bSectionElementWritten) {
                try
                {
                    xswWriter.setDefaultNamespace(sSectionNamespace);
                    xswWriter.writeStartElement(sSectionNamespace, sSectionName);
                    bSectionElementWritten = true;
                }
                catch (XMLStreamException e)
                {
                    throw new BFException("Error while writing the CoBOC ISV package XML.", e);
                }        
            }            
            
            try
            {
                if (sContentElementName != null) {
                     xswWriter.writeStartElement(sSectionNamespace, sContentElementName);
                }
                
                if (liLogger.isInfoEnabled()) {
                    liLogger.info("Adding " + ctType.getLogName() + " " + cContent.getHandle().getLogName());
                }
                
                cuMarshaller.marshalObject(cContent, xdDest, bpParams);
                
                if (sContentElementName != null) {
                    xswWriter.writeEndElement();
               }                
            }
            catch (IOException e)
            {
                throw new BFException("Error while writing the CoBOC ISV content " + cContent.getHandle().getLogName(), e);
            }            
            catch (XMLStreamException e)
            {
                throw new BFException("Error while writing the CoBOC ISV content " + cContent.getHandle().getLogName(), e);
            }
        }
        
        if (sSectionName != null && bSectionElementWritten) {
            try
            {
                xswWriter.writeEndElement(); // Section name.
            }
            catch (XMLStreamException e)
            {
                throw new BFException("Error while writing the CoBOC ISV package XML.", e);
            }
        }
    }

    /**
     * Configures the content flags based on the passed content.
     * @param lTupleList Content list.
     */
    protected void configureContentFlags(List<ContentTuple> lTupleList)
    {
        for (ContentTuple contentTuple : lTupleList)
        {
            switch (contentTuple.getWriteObject().getType()) {
            case COBOC_FOLDERS_TEMPLATE :
            case COBOC_FOLDERS_SPECIAL_ATTRIBUTE :
            case COBOC_FOLDERS_GENERIC_INSTANCE :
            case COBOC_FOLDERS_MAPPING :
            case COBOC_FOLDERS_CONDITIONTEMPLATE :
            case COBOC_FOLDERS_ACTIONTEMPLATE :
            case COBOC_FOLDERS_CONTENTMAP :
            case COBOC_FOLDERS_DECISIONCASE :
                contentFlags.add(EContentFlags.FOLDERS_CONTENT);
                contentFlags.add(EContentFlags.FOLDERS_NON_BPM_CONTENT);
                break;
            case COBOC_FOLDERS_PROCESSTEMPLATE :
            case COBOC_FOLDERS_PROCESSINSTANCE :
            case COBOC_FOLDERS_PROCESSBPMN :
            case COBOC_FOLDERS_PROCESSBPML :
                contentFlags.add(EContentFlags.FOLDERS_CONTENT);
                contentFlags.add(EContentFlags.FOLDERS_BPM_CONTENT);
                break;
            case COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM :
            case COBOC_FOLDERS_ACTIONTEMPLATE_XFORM :
                contentFlags.add(EContentFlags.XMLSTORE_CONTENT);
                break;

            case COBOC_RULES_RULE :
            case COBOC_RULES_RULEGROUP :
                contentFlags.add(EContentFlags.RULES_CONTENT);
                contentFlags.add(EContentFlags.FOLDERS_NON_BPM_CONTENT);
                break;
                
            case COBOC_SCHEDULE_TEMPLATE :
                contentFlags.add(EContentFlags.SCHEDULE_CONTENT);
                contentFlags.add(EContentFlags.FOLDERS_NON_BPM_CONTENT);
                break;
                
            case COBOC_MESSAGE_TEMPLATE :
            case COBOC_INBOX_MODEL_C3 :
            case COBOC_INBOX_MODEL_C1 :
                contentFlags.add(EContentFlags.NOTIFICATION_CONTENT);
                break;

            case COBOC_EMAIL_MODEL :            
                contentFlags.add(EContentFlags.NOTIFICATION_CONTENT);
                contentFlags.add(EContentFlags.XMLSTORE_CONTENT);
                break;
            }
        }
    }
    
    /**
     * Flags for determining what kind of content is being written to the ISV package.
     * These are used to filter out unused sections.
     *
     * @author mpoyhone
     */
    protected enum EContentFlags
    {
        FOLDERS_BPM_CONTENT,
        FOLDERS_NON_BPM_CONTENT,
        NOTIFICATION_CONTENT,
        XMLSTORE_CONTENT,
        FOLDERS_CONTENT,
        RULES_CONTENT,
        SCHEDULE_CONTENT,
    }
}
