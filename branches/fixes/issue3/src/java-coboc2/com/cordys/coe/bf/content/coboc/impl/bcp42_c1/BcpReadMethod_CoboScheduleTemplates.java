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

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentHandle;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IContentStore;
import com.cordys.coe.bf.content.base.IContentUnmarshaller;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.BcpReadMethodBase;
import com.cordys.coe.bf.content.base.impl.ContentSourceBcp;
import com.cordys.coe.bf.content.base.impl.IBcpMethodNames;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;
import com.cordys.coe.bf.content.types.coboc.schedules.ScheduleTemplate;
import com.cordys.coe.bf.databind.BindingParameters;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.soap.ISoapRequest;
import com.cordys.coe.bf.soap.ISoapRequestTemplate;
import com.cordys.coe.bf.utils.AxiomUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;

/**
 * Implements a read method for CoBOC schedule template. Because there doesn't seem to be a method
 * to read one schedule template at a time the first call to this method reads all templates and
 * puts them into a cache and subsequent calls just return those objects.
 *
 * @author  mpoyhone
 */
public class BcpReadMethod_CoboScheduleTemplates extends BcpReadMethodBase
{
    /**
     * Contains caches schedule template objects.
     */
    private static IContentStore csScheduleCache = null;
    /**
     * QName of the schedule template response tuple element.
     */
    private static QName SCHEDULETEMPLATE_TUPLE = new QName(IBcpMethodNames.COBOC_SCHEDULER_NAMESPACE,
                                                            "tuple");

    /**
     * Constructor for BcpReadMethod_CoboScheduleTemplates.
     *
     * @param   bcContext  Context
     * @param   csSource   Content source.
     *
     * @throws  BFException  Thrown if failed.
     */
    public BcpReadMethod_CoboScheduleTemplates(BFContext bcContext, IContentSource csSource)
                                        throws BFException
    {
        super(bcContext, (ContentSourceBcp) csSource);
    }

    /**
     * Clears all cached objects.
     */
    public static void clearCaches()
    {
        if (csScheduleCache != null)
        {
            csScheduleCache.clear();
            csScheduleCache = null;
        }
    }

    /**
     * @see  com.cordys.coe.bf.content.base.IContentReadMethod#readObject(com.cordys.coe.bf.content.base.IContentHandle)
     */
    @Override
    public IContent readObject(IContentHandle hHandle)
                        throws BFException
    {
        EContentType ctType = hHandle.getContentType();

        if (ctType == null)
        {
            throw new IllegalArgumentException("Content type parameter is null.");
        }

        if (csScheduleCache == null)
        {
            readAllIntoCaches();
        }

        String sObjectKey = ((CobocContentHandle) hHandle).getKey();

        if (bUseContentFiltering &&
            !csSource.checkForAccess(hHandle.getContentType(), sObjectKey, false))
        {
            // This item is filtered out.
            return null;
        }

        IContent cResult;

        switch (ctType)
        {
            case COBOC_SCHEDULE_TEMPLATE:
                cResult = csScheduleCache.findObject(hHandle);
                break;

            default:
                throw new BFException("CoBOC schedule template method content type must be schedule template. Got: " +
                                      ctType.getLogName());
        }

        return cResult;
    }

    /**
     * @see  com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(EContentType, boolean)
     */
    public List<IContent> readObjects(EContentType ctType, boolean bReadFully)
                               throws BFException
    {
        if (ctType == null)
        {
            throw new IllegalArgumentException("Content type parameter is null.");
        }

        if (csScheduleCache == null)
        {
            readAllIntoCaches();
        }

        Collection<IContent> cContentList;

        switch (ctType)
        {
            case COBOC_SCHEDULE_TEMPLATE:
                cContentList = csScheduleCache.getObjects();
                break;

            default:
                throw new BFException("CoBOC schedule template method content type must be schedule template. Got: " +
                                      ctType.getLogName());
        }

        List<IContent> res = new ArrayList<IContent>(cContentList);

        if (bUseContentFiltering)
        {
            for (ListIterator<IContent> iter = res.listIterator(); iter.hasNext();)
            {
                IContent obj = iter.next();
                String sObjectKey = ((CobocContentHandle) obj.getHandle()).getKey();

                if (!csSource.checkForAccess(obj.getType(), sObjectKey, false))
                {
                    iter.remove();
                }
            }
        }

        return res;
    }

    /**
     * @see  com.cordys.coe.bf.content.base.IContentReadMethod#readObjects(com.cordys.coe.bf.content.base.IContentHandle,
     *       boolean, boolean)
     */
    public List<IContent> readObjects(IContentHandle chParentHandle, boolean bReadFully,
                                      boolean bRecursive)
                               throws BFException
    {
        // Schedule templates do no have subobjects.
        return new ArrayList<IContent>();
    }

    /**
     * @see  com.cordys.coe.bf.content.base.impl.BcpReadMethodBase#handleSoapResponse(org.apache.axiom.om.OMElement,
     *       com.cordys.coe.bf.content.types.EContentType)
     */
    @Override
    protected List<IContent> handleSoapResponse(OMElement oeResponse, EContentType objectType)
                                         throws BFException
    {
        OMElement oeResult = oeResponse.getFirstElement();

        if (oeResult == null)
        {
            throw new BFException("'result' element missing from the GetAllScheduleTemplatesResponse.");
        }

        List<IContent> lRes = new ArrayList<IContent>(20);
        OMElement oeTuple = oeResult.getFirstChildWithName(SCHEDULETEMPLATE_TUPLE);

        if (oeTuple != null)
        {
            IXmlSource xsUnmarshallSource = IXmlSource.Factory.newInstance();
    
            try
            {
                for (; oeTuple != null; oeTuple = AxiomUtils.getNextSiblingElement(oeTuple))
                {
                    OMElement oeOld = oeTuple.getFirstElement();
    
                    if ((oeOld == null) || !"old".equals(oeOld.getQName().getLocalPart()))
                    {
                        continue;
                    }
    
                    OMElement oeSCHEDULETEMPLATE = oeOld.getFirstElement();
    
                    if (oeSCHEDULETEMPLATE == null)
                    {
                        throw new BFException("'SCHEDULETEMPLATE' element missing from the GetAllScheduleTemplatesResponse.");
                    }
    
                    IContentUnmarshaller cuTupleUnmarshaller = csSource.getContentUnmarshaller(EContentType.COBOC_SCHEDULE_TEMPLATE,
                                                                                               "BCP-tuple",
                                                                                               getMethodVersion());
                    BindingParameters bpParams = new BindingParameters();
    
                    xsUnmarshallSource.set(oeSCHEDULETEMPLATE);
                    cuTupleUnmarshaller.unmarshalObject(xsUnmarshallSource, bpParams);
    
                    String sTemplateId = (String) bpParams.getParameter("TEMPLATEID");
                    String sScheduleXml = (String) bpParams.getParameter("SCHEDULEXML");
                    String sLastUpdated = (String) bpParams.getParameter("LASTUPDATED");
    
                    if (sTemplateId == null)
                    {
                        throw new BFException("'TEMPLATEID' element missing or empty in the GetAllScheduleTemplatesResponse.");
                    }
    
                    if (sScheduleXml == null)
                    {
                        throw new BFException("'SCHEDULEXML' element missing or empty in the GetAllScheduleTemplatesResponse.");
                    }
    
                    // Parse the XML string into OMElement and unmarshall it into a bean.
                    OMElement oScheduleRoot = AxiomUtils.parseString(sScheduleXml);
                    IContentUnmarshaller cuUnmarshaller = csSource.getContentUnmarshaller(EContentType.COBOC_SCHEDULE_TEMPLATE,
                                                                                          null,
                                                                                          getMethodVersion());
    
                    if (cuUnmarshaller == null)
                    {
                        throw new BFException("Unable to get content unmarshaller for content type " +
                                              EContentType.COBOC_SCHEDULE_TEMPLATE.getLogName());
                    }
    
                    xsUnmarshallSource.set(oScheduleRoot);
    
                    ScheduleTemplate stTemplate = (ScheduleTemplate) cuUnmarshaller.unmarshalObject(xsUnmarshallSource);
    
                    stTemplate.setObjectId(sTemplateId);
                    stTemplate.setLastModified(sLastUpdated);
                    lRes.add(stTemplate);
                }
            }
            catch (Exception e)
            {
                throw new BFException("SOAP response parsing failed.", e);
            }
        }
        
        return lRes;
    }

    /**
     * Reads all schedules into the cache.
     *
     * @throws  BFException  Thrown if the operation failed.
     */
    protected void readAllIntoCaches()
                              throws BFException
    {
        csScheduleCache = new CobocContentStore();

        if (
            !bcContext.getConfig().getCobocConfig().isContentEnabled(EContentType.COBOC_SCHEDULE_TEMPLATE))
        {
            // This type has been disabled in the configuration.
            return;
        }

        ISoapRequest srRequest;
        ISoapRequestTemplate srtSoapTemplate;
        List<IContent> lResultList;

        // Read schedule templates.
        srtSoapTemplate = csSource.getSoapRequestTemplate(IBcpMethodNames.COBOC_TEMPLATEID_GETALLSCHEDULETEMPLATES,
                                                          this);
        srRequest = csSource.createSoapRequest();

        try
        {
            lResultList = sendSoapRequest(srRequest, srtSoapTemplate.getBindingTemplate(), null,
                                          null);
        }
        catch (Exception e)
        {
            // Try to check the SOAP:Fault
            Throwable notRunning = findSoapProcessorNotRunningException(e);

            if (notRunning != null)
            {
                String sMessage = notRunning.getMessage();

                if (sMessage != null)
                {
                    bcContext.getLogger().error("Schedule SOAP processor is not available. Schedules are not read.");

                    if (bcContext.getLogger().isDebugEnabled())
                    {
                        bcContext.getLogger().debug("Got exception: " + sMessage);
                    }
                    return;
                }
            }

            throw new BFException(e);
        }

        for (IContent cTmp : lResultList)
        {
            CobocContentHandle handle = (CobocContentHandle) cTmp.getHandle();
            
            if (bUseContentFiltering && 
                ! csSource.checkForAccess(handle.getContentType(), handle.getKey(), false)) {
                // This item is filtered out.
                continue;
            }
            
            csScheduleCache.insertObject(cTmp);
        }
    }
}
