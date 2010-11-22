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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.base.IContent;
import com.cordys.coe.bf.content.base.IContentReadMethod;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.folders.ProcessBpml;
import com.cordys.coe.bf.content.types.coboc.folders.ProcessBpmn;
import com.cordys.coe.bf.content.types.coboc.folders.ProcessTemplate;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.FileUtils;
import com.cordys.tools.ant.util.XSLUtil;

/**
 * Implements a read method for CoBOC folders content.
 *
 * @author mpoyhone
 */
public class FileSystemReadMethod_Folders extends com.cordys.coe.bf.content.coboc.impl.bcp42_c1.FileSystemReadMethod_Folders
{
    public FileSystemReadMethod_Folders(BFContext bcContext, IContentSource csSource) throws BFException {
        super(bcContext, (ContentSourceFileSystem) csSource);
    }
    
    protected List<IContent> readContentFromXml(OMElement oeRoot, String sSourceName) throws BFException {
        List<IContent> lResultList = super.readContentFromXml(oeRoot, sSourceName);
        List<IContent> lExtraObjects = new ArrayList<IContent>(20);
        Set<String> sRemoveObjectKeys = new HashSet<String>();
        
        for (IContent obj : lResultList)
        {
            switch (obj.getType()) {
            case COBOC_FOLDERS_PROCESSTEMPLATE : 
                if (((ProcessTemplate) obj).getBpml() != null) {
                    ProcessBpml bpml = ((ProcessTemplate) obj).getBpml();

                    if (bpml != null && bpml.getHandle().isSet()) {
                        lExtraObjects.add(bpml);
                        sRemoveObjectKeys.add(bpml.getKey());
                    }
                }
                if (((ProcessTemplate) obj).getBpmn() != null) {
                    ProcessBpmn bpmn = ((ProcessTemplate) obj).getBpmn();
                    
                    if (bpmn != null && bpmn.getHandle().isSet()) {
                        lExtraObjects.add(bpmn);
                        sRemoveObjectKeys.add(bpmn.getKey());
                    }
                }
                break;
            /*// Removed for now as the XSLT does not work properly.   
            case COBOC_FOLDERS_DECISIONCASE :
                // Decision cases need to be converted to rule groups.
                lExtraObjects.addAll(convertDecisionCase(oeRoot));
                sRemoveObjectKeys.add(((CobocContentBase) obj).getKey());
                break;
             */
            }
        }
        
        // Remove any objects that were added to the extra object list.
        for (Iterator<IContent> iter = lResultList.iterator(); iter.hasNext(); )
        {
            IContent obj = iter.next();
            
            if (obj instanceof CobocContentBase) {
                if (sRemoveObjectKeys.contains(((CobocContentBase) obj).getKey())) {
                    iter.remove();
                }
                break;
            }            
        }
        
        lResultList.addAll(lExtraObjects);
        
        return lResultList;
    }

    /**
     * Converts a decision case to C3 rule group and rule objects. 
     * @param oeRoot Decision case root node.
     * @return A list of rule group and rule objects.
     * @throws BFException Thrown if the operation failed.
     */
    private List<IContent> convertDecisionCase(OMElement oeRoot) throws BFException
    {
        InputStream isXslt = getClass().getResourceAsStream("decisioncasetorule.xsl");
        
        if (isXslt == null) {
            throw new BFException("Unable to load the decision case XSLT file.");
        }
        
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        
        OMNamespace onOldNamespace = omFactory.createOMNamespace("http://schemas.cordys.com/4.2/coboc", "");
        OMNamespace onNewNamespace = omFactory.createOMNamespace("", "");
        AxiomUtils.changeNamespace(oeRoot, onOldNamespace, onNewNamespace);
        
        try {
            String[] paramNames = {
            };
            String[] paramValues = {
            };
            String ruleGroupXmlText;
            
            try
            {
                ruleGroupXmlText = XSLUtil.getXSLTransformAsString(AxiomUtils.writeToString(oeRoot), isXslt, paramNames, paramValues);
            }
            catch (Exception e)
            {
                throw new BFException("XSLT conversion from a decision case to rule group failed.", e);
            }
            
            IContentReadMethod ruleGroupMethod = csSource.getReadMethod(EContentType.COBOC_RULES_RULEGROUP);
            
            if (ruleGroupMethod == null) {
                throw new BFException("Unable to get the read method for " + EContentType.COBOC_RULES_RULEGROUP.getLogName());
            }
            
            OMElement ruleGroupRoot;
            
            try
            {
                ruleGroupRoot = AxiomUtils.parseString(ruleGroupXmlText);
            }
            catch (Exception e)
            {
                throw new BFException("Unable to parse the rule group XML", e);
            }
            
            IXmlSource xsSource = IXmlSource.Factory.newInstance(ruleGroupRoot);
            
            return ruleGroupMethod.readObjectsFromXml(xsSource);
        } finally {
            FileUtils.closeStream(isXslt);
        }
    }
}
