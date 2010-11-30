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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.ContentTuple;
import com.cordys.coe.bf.content.base.EContentSourceType;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentMarshaller;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BcpWriteMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.schedules.ScheduleTemplate;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.databind.IBindingConstants;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.LogInterface;

/**
 * Write method for BCP CoBOC schedule templates. 
 *
 * @author mpoyhone
 */
public class BcpWriteMethod_ScheduleTemplates extends BcpWriteMethodBase
{
    /**
     * <code>true</code> if this method should return a result object.
     */
    private boolean bReturnResult;
    
    public BcpWriteMethod_ScheduleTemplates(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceBcp) csSource);
    }
    
    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#deleteObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    public IContent deleteObject(IContentHandle chHandle) throws BFException
    {
        throw new BFException("Delete with handle not supported at the moment.");
    }

    /**
     * @see com.cordys.coe.bf.content.base.IContentWriteMethod#updateObject(com.cordys.coe.bf.content.base.ContentTuple)
     */
    public IContent updateObject(ContentTuple ctTuple) throws BFException
    {
        if (ctTuple.isUpdate()) {
            if (ctTuple.getOld().getType() != ctTuple.getNew().getType()) {
                throw new BFException("Old object is of different type than the new object.");
            }
        }
        
        IContent cUpdateObject = ctTuple.getWriteObject();
        EContentType ctType = cUpdateObject.getType();
        QName qnMethodName;
        String sSubType = null;
        
        switch (ctType) {
        case COBOC_SCHEDULE_TEMPLATE : 
            if (ctTuple.isUpdate()) {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_UPDATESCHEDULETEMPLATE;
                bReturnResult = true;
            } else if (ctTuple.isInsert()) {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_CREATESCHEDULETEMPLATE;
                bReturnResult = true;
            } else {
                qnMethodName = IBcpMethodNames.COBOC_METHOD_DELETESCHEDULETEMPLATE;
                sSubType = IBindingConstants.SUBTYPE_BCP_DELETE;
                bReturnResult = false;
            }
            break;
            
        default:
            throw new BFException("Illegal update type " + ctType);
        }
        
        IContentMarshaller cmMarshaller = csSource.getContentMarshaller(ctType, sSubType, getMethodVersion());
        ISoapRequest srRequest = csSource.createSoapRequest();
        LogInterface liLogger = bcContext.getLogger();
        
        if (liLogger.isInfoEnabled()) {
            if (! ctTuple.isDelete()) {
                String sOpName = ctTuple.isInsert() ? "Inserting" : "Updating";
                liLogger.info(sOpName + " " + cUpdateObject.getLogName() + " to " + EContentSourceType.BCP.getLogName());
            } else {
                liLogger.info("Deleting " + cUpdateObject.getLogName() + " from " + EContentSourceType.BCP.getLogName());
            }
        }
        
        return sendUpdateRequest(srRequest, qnMethodName, ctTuple, cmMarshaller);
    }
    
    protected IContent handleUpdateResponse(OMElement oeResponse, IContent cUpdateObject) throws BFException {
        if (! bReturnResult) {
            return null;
        }
        
        IXmlSource xsUnmarshallSource = IXmlSource.Factory.newInstance();
        
        try
        {
            AXIOMXPath xpTuplePath = new AXIOMXPath("./ns:result/ns:update/ns:tuple");
            OMElement oeTuple;
            
            xpTuplePath.addNamespace("ns", oeResponse.getQName().getNamespaceURI());
            oeTuple = (OMElement) xpTuplePath.selectSingleNode(oeResponse);
            
            for (; oeTuple != null; oeTuple = AxiomUtils.getNextSiblingElement(oeTuple)) {
                OMElement oeOld = oeTuple.getFirstElement();
                
                if (oeOld == null) {
                    continue;
                }
                
                OMElement oeSCHEDULETEMPLATE = oeOld.getFirstElement();
                
                if (oeSCHEDULETEMPLATE == null) {
                    throw new BFException("'SCHEDULETEMPLATE' element missing from the SOAP response.");
                }
                
                IContentUnmarshaller cuTupleUnmarshaller = csSource.getContentUnmarshaller(EContentType.COBOC_SCHEDULE_TEMPLATE, "BCP-tuple", getMethodVersion());
                BindingParameters bpParams = new BindingParameters();
                
                xsUnmarshallSource.set(oeSCHEDULETEMPLATE);
                cuTupleUnmarshaller.unmarshalObject(xsUnmarshallSource, bpParams);
                
                String sTemplateId = (String) bpParams.getParameter("TEMPLATEID");
                String sScheduleXml = (String) bpParams.getParameter("SCHEDULEXML");
                String sLastUpdated = (String) bpParams.getParameter("LASTUPDATED");
                
                if (sTemplateId == null) {
                    throw new BFException("'TEMPLATEID' element missing or empty in the GetAllScheduleTemplatesResponse.");
                }
                
                if (sScheduleXml == null) {
                    throw new BFException("'SCHEDULEXML' element missing or empty in the GetAllScheduleTemplatesResponse.");
                }
                
                // Parse the XML string into OMElement and unmarshall it into a bean.
                OMElement oScheduleRoot = AxiomUtils.parseString(sScheduleXml);
                IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(EContentType.COBOC_SCHEDULE_TEMPLATE, null, getMethodVersion());
                    
                if (cuUnmarshaller == null) {
                    throw new BFException("Unable to get content unmarshaller for content type " + EContentType.COBOC_SCHEDULE_TEMPLATE.getLogName());
                }
                
                xsUnmarshallSource.set(oScheduleRoot);
                 
                ScheduleTemplate stTemplate = (ScheduleTemplate) cuUnmarshaller.unmarshalObject(xsUnmarshallSource);
                
                stTemplate.setObjectId(sTemplateId);
                stTemplate.setLastModified(sLastUpdated);
                
                return stTemplate;
            }
        }
        catch (Exception e)
        {
            throw new BFException("SOAP response parsing failed.", e);
        }
        
        return null;
    }      
}
