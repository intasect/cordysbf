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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentReadMethod;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BcpReadMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.coboc.folders.ActionTemplate;
import com.cordys.coe.bf.content.types.coboc.folders.ConditionTemplate;
import com.cordys.coe.bf.content.types.coboc.folders.ObjectInstanceBase;
import com.cordys.coe.bf.content.types.coboc.folders.ProcessTemplate;
import com.cordys.coe.bf.content.types.xmlstore.XmlStoreContentHandle;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapConstants;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.SoapUtils;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.LogInterface;

/**
 * Implements a read method for CoBOC folders content.
 *
 * @author mpoyhone
 */
public class BcpReadMethod_CobocFolders extends BcpReadMethodBase
{
    /**
     * Pattern (?:does\s+not\s+exist)|(?:no\s+data)
     */
    private static final Pattern pObjectNotFoundPattern =
        Pattern.compile("(?:does\\s+not\\s+exist)|(?:no\\s+data)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    /**
     * Constructor for BcpReadMethod_CobocFolders
     * @param bcContext Context
     * @param csSource Content source
     * @throws BFException Thrown if failed
     */
    public BcpReadMethod_CobocFolders(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceBcp) csSource);
    }
    
    /**
     * Note: This method is not not implemented.
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(EContentType, boolean)
     */
    public List<IContent> readObjects(EContentType ctType, boolean bReadFully) throws BFException
    {
        if (ctType == null) {
            throw new IllegalArgumentException("Content type parameter is null.");
        }
        
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle, boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully, boolean bRecursive) throws BFException
    {
        if (chParentHandle == null) {
            throw new IllegalArgumentException("Parent handle parameter is null.");
        }
        
        if (chParentHandle.getContentType() != EContentType.COBOC_FOLDERS_FOLDER &&
            chParentHandle.getContentType() != EContentType.MDM_ENTITY_FOLDER) {
            return new LinkedList<IContent>();
        }
        
        if (! (chParentHandle instanceof CobocContentHandle)) {
            throw new IllegalArgumentException("Invalid content handle for CoBOC folders opeation.");
        }
        
        CobocContentHandle cchCobocHandle = (CobocContentHandle) chParentHandle;
        
        List<IContent> lResList = new LinkedList<IContent>();
        
        readFolderContents(cchCobocHandle, bReadFully, bRecursive, lResList);
        
        return lResList;
    }  
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentReadMethod#readObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent readObject(IContentHandle chHandle) throws BFException
    {
        if (! (chHandle instanceof CobocContentHandle)) {
            throw new IllegalArgumentException("Invalid content handle for CoBOC folders opeation.");
        }
        
        CobocContentHandle cchFolderHandle = (CobocContentHandle) chHandle;
        QName qnMethodName;
        String sObjectKey = cchFolderHandle.getKey();
        EContentType ctType = cchFolderHandle.getContentType();
        boolean isFolder = cchFolderHandle.getContentType() == EContentType.COBOC_FOLDERS_FOLDER;
        
        if (sObjectKey == null) {
            throw new IllegalArgumentException(chHandle.getLogName() + ": CoBOC object key is not set.");
        }
        
        if (bUseContentFiltering && 
            ! csSource.checkForAccess(cchFolderHandle.getContentType(), sObjectKey, isFolder)) {
            // This item is filtered out.
            LogInterface liLogger = bcContext.getLogger();
            
            if (liLogger.isDebugEnabled()) {
                liLogger.debug("  Skipping " + ctType.getLogName() + " " + cchFolderHandle.getLogName());
            }                   
            
            return FILTERED_CONTENT;
        }
        
        if (ctType == EContentType.COBOC_FOLDERS_FOLDER && sObjectKey.equals("/")) {
            // We cannot fetch the root folder object and we even don't need that.
            return null;
        }

        String sFilterAttrib = "";
        String sTypeAttrib;

        switch (ctType) {
        case MDM_ENTITY_FOLDER :
        case COBOC_FOLDERS_FOLDER :
            sFilterAttrib = "folder";
            sTypeAttrib = "entity";
            break;

        case COBOC_FOLDERS_TEMPLATE :
            sFilterAttrib = "template";
            sTypeAttrib = "entity";
            break;
            
        default :
            // This is an object instance.
            sFilterAttrib = bcContext.getCobocTemplateRegistry().getTemplateKeyFromContentType(ctType);
            sTypeAttrib = "instance";
            break;
        }
        
        if (sFilterAttrib == null || sFilterAttrib.length() == 0)
        {
            throw new BFException("Filter attribute could not be determined.");
        }
        
        qnMethodName = IBcpMethodNames.COBOC_METHOD_GETXMLOBJECT;        
        
        ISoapRequest srRequest = csSource.createSoapRequest();
        IXmlDestination xdDest;
        XMLStreamWriter xswWriter = null;
        
        try {
            OutputStream osSoapRequest;
            
            // Open the SOAP request and write SOAP envelope and body.
            osSoapRequest = srRequest.openSoapRequest();
            xdDest = SoapUtils.startSoapRequestBody(osSoapRequest, qnMethodName);
            xswWriter = xdDest.getStreamWriter();
            xswWriter.setDefaultNamespace(qnMethodName.getNamespaceURI());

            xswWriter.writeStartElement(qnMethodName.getNamespaceURI(), "key");

            xswWriter.writeAttribute("filter", sFilterAttrib);
            xswWriter.writeAttribute("type", sTypeAttrib);
            
            if (ctType != EContentType.COBOC_FOLDERS_FOLDER) {
                // Everything else but folders we fetch from the organizational level.
                // Folders can also be defined in the ISV level (e.g. /cordys), so we need
                // to check for there too.
                xswWriter.writeAttribute("version", "organization");
            }
            
            xswWriter.writeCharacters(sObjectKey);
            xswWriter.writeEndElement(); // key
            
            // Finish the request document.
            SoapUtils.endSoapRequest(xdDest, true);
            
            // Send the request.
            OMElement oeResponse = srRequest.sendSoapRequest();
            
            // Read the response object. 
            OMElement oeTuple = oeResponse.getFirstElement();
            
            if (oeTuple != null) {
                IContent cContent;
                
                cContent = unmarshallTupleElement(oeTuple, sFilterAttrib);
                
                return cContent;
            }
        }
        catch (Exception e) {
            // Try to check the SOAP:Fault
            Throwable tCause = e;
            
            while (tCause != null) {
                String sMessage = tCause.getMessage();
                
                if (sMessage != null) {
                    if (pObjectNotFoundPattern.matcher(sMessage).find()) {
                        // The object was not found.
                        return null;
                    }
                }
                
                tCause = tCause.getCause();
            }            
            
            // Re-throw the exception.
            throw new BFException(e);
        }
        finally {
            srRequest.closeSoapRequest();
        }
        
        return null;
    }    
    
    private void readFolderContents(IContentHandle chHandle, boolean bReadFully, boolean bRecursive, List<IContent> lResList) throws BFException {
        if (! (chHandle instanceof CobocContentHandle)) {
            throw new IllegalArgumentException("Invalid content handle for CoBOC folders opeation.");
        }
        
        CobocContentHandle cchFolderHandle = (CobocContentHandle) chHandle;
        QName qnMethodName;
        String sFolderKey = cchFolderHandle.getKey();
        
        if (sFolderKey == null) {
            throw new IllegalArgumentException("CoBOC folder key is not set.");
        }
        
        if (bUseContentFiltering &&
            ! csSource.checkForAccess(cchFolderHandle.getContentType(), sFolderKey, true)) {
            // This folder is filtered out.
            LogInterface liLogger = bcContext.getLogger();
            
            if (liLogger.isDebugEnabled()) {
                liLogger.debug("  Skipping folder " + sFolderKey);
            }                
            
            return;
        }
        
        qnMethodName = IBcpMethodNames.COBOC_METHOD_GETCOLLECTION;
        
        // Build the GetCollection method filter and type attributes by including folder and template as
        // well as the instance template keys. 
        boolean bIsRoot = CobocConstants.ROOT_FOLDER_KEY.equals(sFolderKey);
        boolean bIsBpmExtraRoot = false;
        Collection<String> lInstanceTemplateKeyList = new ArrayList<String>(bcContext.getCobocTemplateRegistry().getSupportedTemplateKeys());
        String[][] saParamList;
        
        if (sFolderKey.startsWith(CobocConstants.COBOC_BPMN_ROOT) ||
            sFolderKey.startsWith(CobocConstants.COBOC_BPML_ROOT)) {
            // We don't read all of these objects anymore, but read
            // the latest ones for each BPM. This flags means that
            // we read only the folders under these folders.
            bIsBpmExtraRoot = true;
        }
        
        if (! bIsRoot && ! bIsBpmExtraRoot) {
            saParamList = new String[lInstanceTemplateKeyList.size() + 2][];
            int iPos;
            
            saParamList[0]  = new String[] { "folder", "entity" };
            saParamList[1]  = new String[] { "template", "entity" };
            iPos = 2;
            for (String sKey : lInstanceTemplateKeyList)
            {
                saParamList[iPos++]  = new String[] { sKey, "instance" };
            }
        } else {
            // For root folder we only list subfolders.
            saParamList = new String[][] { { "folder", "entity" } };
        }
        
        List<IContent> lChildList = new LinkedList<IContent>(); 
        
        handleContentListRequest(sFolderKey, "organization", qnMethodName, saParamList, lChildList);
        
        if (bReadFully) {
            // Re-read the objects with full contents.
            List<IContent> lTmpList = new LinkedList<IContent>();
            
            for (IContent cContent : lChildList)
            {
                switch(cContent.getType()) {
                case COBOC_FOLDERS_ACTIONTEMPLATE_XFORM :
                case COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM :
                case COBOC_FOLDERS_SPECIAL_ATTRIBUTE :
                    // These are already full versions.
                    lTmpList.add(cContent);
                    continue;
                }
                
                IContent cFullVersion = readObject(cContent.getHandle());
                
                if (cFullVersion != FILTERED_CONTENT) {
                    if (cFullVersion != null) {
                        lTmpList.add(cFullVersion);
                    } else {
                        // For some reason the read failed, so add the simple version.
                        lTmpList.add(cContent);
                    }
                    
                    switch(cContent.getType()) {
                    case COBOC_FOLDERS_PROCESSTEMPLATE :
                        if (cFullVersion != null) {
                            lTmpList.addAll(readBpmExtraObject(cFullVersion));
                        }
                        break;
                    }
                }
            }
            
            lChildList.clear();
            lChildList = lTmpList;
        }

        lResList.addAll(lChildList);
        
        if (bRecursive) {
            // Read all subfolders.
            for (IContent cChild : lChildList) {
                switch (cChild.getType()) {
                case COBOC_FOLDERS_FOLDER :
                case MDM_ENTITY_FOLDER :
                    readFolderContents(cChild.getHandle(), bReadFully, bRecursive, lResList);
                    break;
                }
            }
        }
    }
    
    private Collection<IContent> readBpmExtraObject(IContent content) throws BFException
    {
        if (content.getType() != EContentType.COBOC_FOLDERS_PROCESSTEMPLATE) {
            throw new BFException("BPM object must be of type process template.");
        }
        
        List<IContent> resList = new ArrayList<IContent>(2);
        ProcessTemplate bpm = (ProcessTemplate) content;
        CobocContentHandle handle;
        
        // Needed to get the BPMN and BPML keys.
        bpm.setContext(bcContext);
        bpm.updateReferences(csSource);
        
        handle = (CobocContentHandle) bpm.getBpmlHandle();
        if (handle != null && handle.isSet()) {
            IContent bpml = readObject(handle);
            
            if (bpml != null && bpml != FILTERED_CONTENT) {
                resList.add(bpml);
                handle.copyFrom(bpml.getHandle());
            }
        }
        
        handle = (CobocContentHandle) bpm.getBpmnHandle();
        if (handle != null && handle.isSet()) {
            IContent bpmn = readObject(handle);
            
            if (bpmn != null && bpmn != FILTERED_CONTENT) {
                resList.add(bpmn);
                handle.copyFrom(bpmn.getHandle());
            }
        }
        
        return resList;
    }

    private void handleContentListRequest(String sFolderKey, String sVersion, QName qnMethodName, String[][] saTypeParams, List<IContent> lResList) throws BFException {
        ISoapRequest srRequest = csSource.createSoapRequest();
        XMLStreamWriter xswWriter = null;
        boolean bIsRoot = CobocConstants.ROOT_FOLDER_KEY.equals(sFolderKey);
        
        try {
            OutputStream osSoapRequest;
            IXmlDestination xdDest;
            
            // Open the SOAP request and write SOAP envelope and body.
            osSoapRequest = srRequest.openSoapRequest();
            xdDest = SoapUtils.startSoapRequestBody(osSoapRequest, null);
            xswWriter = xdDest.getStreamWriter();
            xswWriter.setDefaultNamespace(qnMethodName.getNamespaceURI());
            
            // For each content type in the array create one GetCollection method with
            // correct parameters. The response will have these method in the same order
            // and we can unmarshall the content elements based on these types.
            for (String[] saParams : saTypeParams)
            {
                xswWriter.writeStartElement(qnMethodName.getNamespaceURI(), qnMethodName.getLocalPart());
                xswWriter.writeNamespace("", qnMethodName.getNamespaceURI());
                
                if (! bIsRoot) {
                    xswWriter.writeStartElement(qnMethodName.getNamespaceURI(), "folder");
                } else {
                    xswWriter.writeEmptyElement(qnMethodName.getNamespaceURI(), "folder");
                }
                
                xswWriter.writeAttribute("filter", saParams[0]);
                xswWriter.writeAttribute("type", saParams[1]);
                xswWriter.writeAttribute("version", sVersion);
                
                if (! bIsRoot) {
                    xswWriter.writeCharacters(sFolderKey);
                    xswWriter.writeEndElement(); // folder.
                }
                
                xswWriter.writeEndElement();
            }
            
            // Finish the request document.
            SoapUtils.endSoapRequest(xdDest, false);
            
            // Send the request.
            OMElement oeResponse = srRequest.sendSoapRequest();
            
            // Read the response methods.
            try {
                int iCurrentMethod = 0;
                
                while (oeResponse != null) {
                    boolean bHandle = true;
                    
                    // Check that it is not a SOAP:Fault.
                    if ("Fault".equals(oeResponse.getLocalName())) {
                        OMNamespace onNs = oeResponse.getNamespace();
                        
                        if (ISoapConstants.SOAP_NAMESPACE.equals(onNs.getName())) {
                            //throw new SoapFaultException(oeResponse);
                            
                            // This might be cause by not having MDM installed in this organization.
                            bHandle = false;
                        }
                    }
                    
                    if (bHandle) {
                        // For each response element figure process the correct content types. 
                        for (Iterator<?> iTupleIter = oeResponse.getChildElements(); iTupleIter.hasNext(); ) {
                            OMElement oeTuple = (OMElement) iTupleIter.next();
                            IContent cContent;
                            
                            cContent = unmarshallTupleElement(oeTuple, saTypeParams[iCurrentMethod][0]);
                            
                            if (cContent != null) {
                                boolean addThis = true;

                                if (bUseContentFiltering && cContent instanceof CobocContentBase) {
                                    CobocContentHandle handle = (CobocContentHandle) cContent.getHandle();
                                    boolean isFolder = handle.getContentType() == EContentType.COBOC_FOLDERS_FOLDER;
                                    
                                    if (! csSource.checkForAccess(handle.getContentType(), handle.getKey(), isFolder)) {
                                        // This item is filtered out.
                                        LogInterface liLogger = bcContext.getLogger();
                                        
                                        if (liLogger.isDebugEnabled()) {
                                            liLogger.debug("  Skipping " + handle.getContentType().getLogName() + " " + handle.getLogName());
                                        }
                                        
                                        addThis = false;
                                    }       
                                }
                                 
                                if (addThis) {
                                    lResList.add(cContent);
                                }
                            }
                        }
                    }
                    
                    iCurrentMethod++;
                    oeResponse = AxiomUtils.getNextSiblingElement(oeResponse);
                }
            }
            catch (Exception e) {
                throw new BFException("CoBOC folder list SOAP request failed.", e);
            }
        }
        catch (Exception e) {
            throw new BFException("SOAP request failed.", e);
        }
        finally {
            srRequest.closeSoapRequest();
        }
        
        // Read extra related content e.g. XForms for action templates and special attributes
        // for templates.
        List<IContent> lExtraContent = new ArrayList<IContent>(30);
        
        for (IContent cTmp : lResList) {
            switch (cTmp.getType()) {
            case COBOC_FOLDERS_ACTIONTEMPLATE :
            {
                // Try to read the XForm.
                IContentReadMethod crmMethod = csSource.getReadMethod(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM);
                XmlStoreContentHandle xchXFormHandle = new XmlStoreContentHandle(EContentType.COBOC_FOLDERS_ACTIONTEMPLATE_XFORM);
                String sObjectId = ((ActionTemplate) cTmp).getObjectId();
                IContent cXForm;
                
                xchXFormHandle.setKey("/Cordys/WCP/XForms/runtime/" + sObjectId + ".caf");
                crmMethod.setContentVersion("isv");
                
                cXForm = crmMethod.readObject(xchXFormHandle);
                
                if (cXForm != null && cXForm != FILTERED_CONTENT) {
                    lExtraContent.add(cXForm);
                }
            } break;
            
            case COBOC_FOLDERS_CONDITIONTEMPLATE :
            {
                // Try to read the XForm.
                IContentReadMethod crmMethod = csSource.getReadMethod(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM);
                XmlStoreContentHandle xchXFormHandle = new XmlStoreContentHandle(EContentType.COBOC_FOLDERS_CONDITIONTEMPLATE_XFORM);
                String sObjectId = ((ConditionTemplate) cTmp).getObjectId();
                IContent cXForm;
                
                xchXFormHandle.setKey("/Cordys/WCP/XForms/runtime/" + sObjectId + ".caf");
                crmMethod.setContentVersion("isv");
                
                cXForm = crmMethod.readObject(xchXFormHandle);
                
                if (cXForm != null && cXForm != FILTERED_CONTENT) {
                    lExtraContent.add(cXForm);
                }
            } break;            
            
            case COBOC_FOLDERS_TEMPLATE :
            {
                IContentReadMethod crmMethod = csSource.getReadMethod(EContentType.COBOC_FOLDERS_SPECIAL_ATTRIBUTE);
                List<IContent> lSpecialAttributes;
                
                lSpecialAttributes = crmMethod.readObjects(cTmp.getHandle(), true, false);
                
                if (lSpecialAttributes != null) {
                    for (IContent cAttrib : lSpecialAttributes)
                    {
                        cAttrib.setParent(cTmp.getHandle());
                        lExtraContent.add(cAttrib);
                    }
                    
                }
            } break;

            }
        }
        
        lResList.addAll(lExtraContent);
    }  
    
    private IContent unmarshallTupleElement(OMElement oeTuple, String sFilterValue) throws BFException {
        String sInstanceTemplateKey = null;
        String sIsFolderAttribute;
        String sEntityTypeAttribute;
        EContentType ctType;
        
        // Check if the tuple is a folder, template or an instance.
        sIsFolderAttribute = oeTuple.getAttributeValue(new QName("isFolder"));
        sEntityTypeAttribute = oeTuple.getAttributeValue(new QName("entity_type"));
        
        if (sIsFolderAttribute != null && "true".equalsIgnoreCase(sIsFolderAttribute)) {
            String sFolderKey = oeTuple.getAttributeValue(new QName("key"));

            // Check for special folder types. Only MDM implemented atm.
            if (sFolderKey != null && sFolderKey.startsWith("/cordys/mdm/modeler/components/entities/")) {
                ctType = EContentType.MDM_ENTITY_FOLDER;
            } else {
                ctType = EContentType.COBOC_FOLDERS_FOLDER;
            }
        } else if (sEntityTypeAttribute == null){
            ctType = EContentType.COBOC_FOLDERS_TEMPLATE;
        } else  {
            // Try to resolve the instance key and type from the registry.
            sEntityTypeAttribute = oeTuple.getAttributeValue(new QName("entity_type"));
            
            if (sEntityTypeAttribute == null) {
                throw new BFException("CoBOC object instance tuple is missing 'entity_type' attribute.");
            }
            
            sInstanceTemplateKey = sFilterValue;
            ctType = bcContext.getCobocTemplateRegistry().getContentTypeFromTemplateKey(sInstanceTemplateKey);
            
            if (ctType == null) {
                // Check for content types that cannot be determined from the template key.
                if (sInstanceTemplateKey.equals("/SUPERTEMPLATE")) {
                    String sKey = oeTuple.getAttributeValue(new QName("key"));

                    if (sKey != null) {
                        if (sKey.startsWith(CobocConstants.COBOC_BPMN_ROOT)) {
                            ctType = EContentType.COBOC_FOLDERS_PROCESSBPMN;
                        } else if (sKey.startsWith(CobocConstants.COBOC_BPML_ROOT)) {
                            ctType = EContentType.COBOC_FOLDERS_PROCESSBPML;
                        }
                    }
                }
                    
                if (ctType == null) {
                    bcContext.getLogger().error("Skipping unsupported object instance with key " + sInstanceTemplateKey);
                    return null;
                }
            }            
        }
        
        if (! bcContext.getConfig().getCobocConfig().isContentEnabled(ctType)) {
            // This type has been disabled in the configuration. 
            LogInterface liLogger = bcContext.getLogger();
            
            if (liLogger.isDebugEnabled()) {
                liLogger.debug("  Skipping content of type " + ctType);
            }     
            
            return null;
        }
        
        switch (ctType) {
        case MDM_BACKEND :
        case MDM_ENTITY :
        case MDM_ENTITY_FOLDER :
        case MDM_MODEL :
        case COBOC_FOLDERS_PROCESSINSTANCE :
        case COBOC_FOLDERS_SPECIAL_ATTRIBUTE :
            if (bcContext.getLogger().isDebugEnabled()) {
                bcContext.getLogger().debug("Content type " + ctType.getLogName() + 
                                " is not implemented. Skipping object " +
                                oeTuple.getAttributeValue(new QName("key")));
            }
            return null;
        }
        
        IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(ctType, null, getMethodVersion());
        IXmlSource sUnmarshallSource = IXmlSource.Factory.newInstance(oeTuple);
        
        if (cuUnmarshaller == null) {
            throw new BFException(csSource.getType().getLogName() + ": No unmarshaller configured for CoBOC content type " + ctType);
        }
        
        IContent cResult = cuUnmarshaller.unmarshalObject(sUnmarshallSource);
        
        if (sInstanceTemplateKey != null) {
            // Set the template key for this instance.
            if (! (cResult instanceof ObjectInstanceBase)) {
                throw new BFException("Object instance class " + cResult.getClass() + " doesn't extend ObjectInstanceBase.");
            }
            
            ((ObjectInstanceBase) cResult).setTemplateKey(sInstanceTemplateKey);
        }
        
        if (! cResult.getHandle().isSet()) {
            throw new BFException(ctType.getLogName() + ": Content handle is not set.");
        }        
        
        return cResult;
    }
}
