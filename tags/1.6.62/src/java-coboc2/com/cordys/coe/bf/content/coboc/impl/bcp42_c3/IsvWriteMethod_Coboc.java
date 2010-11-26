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
package com.cordys.coe.bf.content.coboc.impl.bcp42_c3;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.folders.Folder;
import com.cordys.coe.bf.exception.BFException;

/**
 * Write method for writing all CoBOC content to ISV format. 
 *
 * @author mpoyhone
 */
public class IsvWriteMethod_Coboc extends com.cordys.coe.bf.content.coboc.impl.bcp42_c1.IsvWriteMethod_Coboc
{
    public IsvWriteMethod_Coboc(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, csSource);
    }

    /**
     * This method packages the runtime BPM objects. The parent class method
     * does all the other content.
     * 
     * @see com.cordys.coe.bf.content.base.impl.WriteMethodBase#updateObjects(java.util.List)
     */
    public List<IContent> updateObjects(List<ContentTuple> lTupleList) throws BFException
    {
        filterOutBpmFolders(lTupleList);
        configureContentFlags(lTupleList);
        
        IXmlDestination xdDest = csSource.getIsvDestination();
        XMLStreamWriter xswWriter = xdDest.getStreamWriter();
        
        if (xswWriter == null) {
            throw new BFException("ISV write method only supports XML stream destination.");
        }

        if (contentFlags.contains(EContentFlags.FOLDERS_NON_BPM_CONTENT)) {
            writeFoldersContent(lTupleList, xdDest, xswWriter);
        }
        
        if (contentFlags.contains(EContentFlags.NOTIFICATION_CONTENT)) {
            writeNotificationContent(lTupleList, xdDest, xswWriter);
        }
        
        if (contentFlags.contains(EContentFlags.XMLSTORE_CONTENT)) {
            writeXmlStoreContent(lTupleList, xdDest, xswWriter);
        }
        
        if (contentFlags.contains(EContentFlags.FOLDERS_BPM_CONTENT)) {
            writeBpmRuntimeContent(lTupleList, xdDest, xswWriter);
        }
        
        // We don't return anything.
        return null;
    }
    
    /**
     * Filters out folders which contain only BPM content.
     * 
     * @param lTupleList Content list to be filtered out.
     * @throws BFException
     */
    private void filterOutBpmFolders(List<ContentTuple> lTupleList) throws BFException
    {
        for (Iterator<ContentTuple> iter = lTupleList.iterator(); iter.hasNext();)
        {
            ContentTuple tuple = (ContentTuple) iter.next();
            IContent obj = tuple.getWriteObject();
            
            if (obj.getType() == EContentType.COBOC_FOLDERS_FOLDER) {
                String key = ((Folder) obj).getKey();
            
                if (key != null) {
                    if (key.startsWith("/Business Processes/") || key.equals("/Business Processes")) {
                        // Skip this folder.
                        iter.remove();
                    }
                }
            }                
        }
    }

    /**
     * Writes the runtime BPM's to the ISV XML.
     * @param lTupleList List containing the objects to be written.
     * @param xdDest XML destination object.
     * @param xswWriter Used for writing the XML.
     * @throws BFException Thrown if the operation failed.
     */
    private void writeBpmRuntimeContent(List<ContentTuple> lTupleList,
            IXmlDestination xdDest, XMLStreamWriter xswWriter)
            throws BFException
    {
        /*
         * CoBOC loader format is
         * <ProcessFlowContent xmlns="http://schemas.cordys.com/bpm/deployment/1.0" loader="com.cordys.bpm.deployment.loader.ProcessDeployer" description="Business Processes Deployer" url="/cordys/bpm/deployment/deploymentwizard.htm">
         *    <process xmlns="">
         *    </process >
         * </ProcessFlowContent>  
         */
        String sIsvNamespace = "http://schemas.cordys.com/bpm/deployment/1.0";
        
        try
        {
            xswWriter.setDefaultNamespace(sIsvNamespace);

            xswWriter.writeStartElement(sIsvNamespace, "ProcessFlowContent");
            xswWriter.writeNamespace("", sIsvNamespace);
            xswWriter.writeAttribute("loader", "com.cordys.bpm.deployment.loader.ProcessDeployer");
            xswWriter.writeAttribute("description", "Business Processes Deployer");
            xswWriter.writeAttribute("url", "/cordys/bpm/deployment/deploymentwizard.htm");
            
            // Write all supported content from the content list.
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE, null, "", null, xdDest);
            
            xswWriter.writeEndElement(); // CPCImporter
        }
        catch (XMLStreamException e)
        {
            throw new BFException("Error while writing the CoBOC ISV package XML.", e);
        }
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
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE, "ConditionTemplateContent", "", "conditiontemplate", xdDest);
            writeContent(lTupleList, EContentType.COBOC_FOLDERS_ACTIONTEMPLATE, "ActionTemplateContent", "", "actiontemplate", xdDest);
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
         * <CPCNotificationImporter xmlns="http://schemas.cordys.com/1.0/notification" description="Notification Content Loader" loader="com.cordys.cpc.notification.content.CPCNSContentLoader" type="ISV" url="/cordys/cpc/deployment/ns_deploymentwizard.htm">
         *     <CPCNotificationContent xmlns="http://schemas.cordys.com/1.0/notification">
         *         <messageTemplate>
         *             ...
         *         </messageTemplate>
         *         <MESSAGE_MODEL>
         *             ...
         *         </MESSAGE_MODEL>
         *         <EmailModelContent>
         *             ...
         *         </EmailModelContent>
         *     </CPCNotificationContent>
         * </CPCNotificationImporter>             
         * </pre>
         */
        try
        {
            xswWriter.writeStartElement("CPCNotificationImporter");
            xswWriter.writeNamespace("", "http://schemas.cordys.com/1.0/notification");
            xswWriter.writeAttribute("loader", "com.cordys.cpc.notification.content.CPCNSContentLoader");
            xswWriter.writeAttribute("description", "The Orchestrator Notification Loader");
            xswWriter.writeAttribute("type", "ISV");
            xswWriter.writeAttribute("url", "/cordys/cpc/deployment/ns_deploymentwizard.htm");            
            
            xswWriter.setDefaultNamespace("http://schemas.cordys.com/1.0/notification");
            xswWriter.writeStartElement("http://schemas.cordys.com/1.0/notification", "CPCNotificationContent");            
            
            // Write all supported content from the content list.
            writeContent(lTupleList, EContentType.COBOC_INBOX_MODEL_C1, null, "", null, xdDest);
            writeContent(lTupleList, EContentType.COBOC_MESSAGE_TEMPLATE, null, "", null, xdDest);
            writeContent(lTupleList, EContentType.COBOC_INBOX_MODEL_C3, null, "", null, xdDest);
            writeContent(lTupleList, EContentType.COBOC_EMAIL_MODEL, null, "", null, xdDest);
            
            xswWriter.writeEndElement(); // CPCNotificationContent
            xswWriter.writeEndElement(); // xmlstore
        }
        catch (XMLStreamException e)
        {
            throw new BFException("Error while writing the CoBOC ISV package XML.", e);
        }
    }    
}
