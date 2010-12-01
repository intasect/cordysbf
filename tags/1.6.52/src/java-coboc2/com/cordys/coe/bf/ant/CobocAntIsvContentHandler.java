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
package com.cordys.coe.bf.ant;

import java.io.File;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.tools.ant.Project;

import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;
import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.config.BFConfig;
import com.cordys.coe.bf.config.CobocConfig;
import com.cordys.coe.bf.config.CobocObjectIdMap;
import com.cordys.coe.bf.content.base.IContentSource;
import com.cordys.coe.bf.content.base.IXmlDestination;
import com.cordys.coe.bf.content.base.IXmlSource;
import com.cordys.coe.bf.content.base.impl.ContentSourceFileSystem;
import com.cordys.coe.bf.content.base.impl.ContentSourceIsv;
import com.cordys.coe.bf.content.coboc.impl.bcp42_c1.CobocConstants;
import com.cordys.coe.bf.content.coboc.impl.bcp42_c1.CobocContentManager;
import com.cordys.coe.bf.content.coboc.impl.bcp42_c1.ContentSourceConfigurator;
import com.cordys.coe.bf.databind.BindingException;
import com.cordys.coe.bf.exception.BFException;
import com.cordys.coe.bf.utils.AxiomUtils;
import com.cordys.coe.util.log.AntTaskLogger;
import com.cordys.tools.ant.cm.BcpVersionInfo;
import com.cordys.tools.ant.cm.EBcpVersion;
import com.cordys.tools.ant.isv.Content;
import com.cordys.tools.ant.isv.ISVContentHandler;
import com.cordys.tools.ant.isv.ISVContentHelper;
import com.cordys.tools.ant.isv.ISVCreatorTask;
import com.cordys.tools.ant.util.GeneralUtils;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Build framework ISV handler for CoBOC content.
 *
 * @author mpoyhone
 */
public class CobocAntIsvContentHandler extends ISVContentHelper implements ISVContentHandler
{
    /**
     * Context.
     */
    private BFContext bcContext;
    /**
     * CoBOC content manager object.
     */
    private CobocContentManager ccmCobocManager;
    /**
     * ISV content source. Contains the binding information for ISV format.
     */
    private ContentSourceIsv csIsv;
    /**
     * File system content source. Contains the binding information for File system format.
     */
    private IContentSource csFileSystem;
    /**
     * Message logged when xml handling related exception occurs
     */
    private static final String XML_ERROR = "Error occured while performing xml operation.\n";
    
    /**
     * @see com.cordys.tools.ant.isv.ISVContentHandler#getISVContentXML(com.cordys.tools.ant.isv.ISVCreatorTask, com.cordys.tools.ant.isv.Content, int, int, int)
     */
    public int[] getISVContentXML(ISVCreatorTask ictTask, Content cContent, int xInput, 
                                  int iCurrentIsvContentNode,
                                  int iCurrentIsvPromptsetNode)
    {
        try
        {
            initialize(ictTask, cContent);
            
            bcContext.getLogger().info("Adding CoBOC content to the ISV package.");
            
            // Check if we have to write the object ID mapping file.
            CobocConfig cobocConfig = bcContext.getConfig().getCobocConfig();
            CobocObjectIdMap objectIdMap = cobocConfig.getObjectIdMap();
            
            if (objectIdMap != null) {
                File contentFile = cobocConfig.getObjectIdMappingFile();
                
                // First try to load the old ones.
                if (contentFile.exists()) {
                    bcContext.getLogger().info("Reading object ID mappings from file: " + contentFile);
                    
                    try {
                        objectIdMap.loadFromFile(contentFile);
                    }
                    catch (Exception e) {
                        ictTask.log("Unable to load object ID mappings.", Project.MSG_ERR);
                    }
                }
            }
            
            int xContentRootNode = 0;
            
            try
            {
                // Replaces the file pointers with the XML content read from
                // the corresponding files.
                processExternalFileContents(ictTask, cContent, xInput, "",
                                            "<><content>");
                
                // Don't add any CoBOC nodes if we don't have any content.
                // Otherwise we will get the CoBOC loader screen for every ISV package.
                if (Node.getFirstElement(xInput) == 0) { 
                    return new int[0];
                }

                // Load the content from the XML.
                StringBuilder sbBuilder = new StringBuilder(10000);
                int[] xaContentNodes = Find.match(xInput, "<><><>");
                
                sbBuilder.append("<root>");
                for (int xNode : xaContentNodes)
                {
                    sbBuilder.append(Node.writeToString(xNode, false));
                }
                sbBuilder.append("</root>");
                
                String sFileSystemXml = sbBuilder.toString(); 
                OMElement oeFileSystemRoot = AxiomUtils.parseString(sFileSystemXml);
                IXmlSource xsXmlSource = IXmlSource.Factory.newInstance(oeFileSystemRoot);   
                
                ccmCobocManager.readContentFromXml(csFileSystem, xsXmlSource);

                // Create the result ISV XML structure. 
                StringWriter swStringWriter = new StringWriter(10000);
                XMLStreamWriter xswWriter = AxiomUtils.xofXmlOuputFactory.createXMLStreamWriter(swStringWriter);
                IXmlDestination xdIsvDestination = IXmlDestination.Factory.newInstance(xswWriter);
                String sIsvXml;
                
                // Write a dummy root node.
                xswWriter.writeStartDocument();
                xswWriter.writeStartElement("dummy_root");
                
                csIsv.setIsvDestination(xdIsvDestination);
                ccmCobocManager.writeContentToSource(csIsv);
                
                xswWriter.writeEndElement(); // End dummy root node.
                xswWriter.writeEndDocument();
                
                xswWriter.flush();
                xswWriter.close();
                sIsvXml = swStringWriter.toString();
                xContentRootNode = Node.getDocument(xInput).parseString(sIsvXml);
                
                int[] xaRes = Find.match(xContentRootNode, "<><>");
                
                for (int i = 0; i < xaRes.length; i++)
                {
                    xaRes[i] = Node.unlink(xaRes[i]);
                }
                
                Node.delete(xContentRootNode);
                xContentRootNode = 0;
                
                return xaRes;
            }
            catch (Exception xe)
            {
                
                if (xContentRootNode != 0) {
                    Node.delete(xContentRootNode);
                    xContentRootNode = 0;
                }
                
                GeneralUtils.handleException(XML_ERROR + xe.getMessage(), xe,
                        cContent);

            }

            cContent.log("Final Content Node:" +
                            Node.writeToString(xContentRootNode, true),
                            Project.MSG_DEBUG);            
            
        }
        catch (Exception e)
        {
            GeneralUtils.handleException(XML_ERROR + e.getMessage(), e, ictTask);
            return new int[0];
        }
        
        return new int[0];
    }
    
    /**
     * Initializes this handler.
     * @param cContent Ant content element.
     * @throws BFException 
     * @throws BindingException 
     */
    private void initialize(ISVCreatorTask ictTask, Content cContent) throws BFException, BindingException {
        BFConfig bcConfig;
        BcpVersionInfo bcpVersion = ictTask.getVersionInfo();
        
        bcContext = new BFContext();
        bcContext.setLiLogger(new AntTaskLogger(ictTask, "coboc"));
        bcConfig = bcContext.getConfig();
        bcConfig.setUserDn(ictTask.getUserdn());
        bcConfig.setOrganizationDn(ictTask.getOrganization());

        // Set the platform version information.
        bcConfig.setVersionInfo(bcpVersion);

        // Check for C3 message template fix flag.
        if ("false".equals(ictTask.getProject().getProperty("coboc.c3messagetemplatefix"))) {
            bcConfig.getCobocConfig().setFixC3MessageTemplateIds(false);
        } else if (! bcpVersion.isLaterThan(EBcpVersion.BCP42_C3)) {
            // Disable the fix for C2
            bcConfig.getCobocConfig().setFixC3MessageTemplateIds(false);
        }
        
        // Check for object ID mapping file support.
        if ("true".equals(ictTask.getProject().getProperty(CobocConstants.PROPERTY_USE_OBJECT_ID_MAP))) {
            CobocConfig cobocConfig = bcConfig.getCobocConfig();
            
            cobocConfig.setUseObjectIdMappingFile(true);
            
            String rootDir = ictTask.getProject().getProperty(BuildFrameworkProperty.SRC_CONTENT_COBOC.getName());
            
            if (rootDir == null) {
                throw new BFException("CoBOC content folder property not defined: " + BuildFrameworkProperty.SRC_CONTENT_COBOC.getName());
            }
            
            bcConfig.getCobocConfig().setObjectIdMappingFile(new File(rootDir, CobocConstants.COBOC_OBJECT_ID_MAP_FILE));
        }
        
        csIsv = new ContentSourceIsv(bcContext);
        ContentSourceConfigurator.loadFromClasspath(csIsv, bcContext);
        
        csFileSystem = new ContentSourceFileSystem(bcContext);
        ContentSourceConfigurator.loadFromClasspath(csFileSystem, bcContext);
        
        ccmCobocManager = new CobocContentManager();
        ccmCobocManager.initialize(bcContext);
        
        boolean autoDeploy = "true".equals(GeneralUtils.getTrimmedProperty(ictTask.getProject(), BuildFrameworkProperty.COBOC_SCHEDULES_AUTODEPLOY.getName()));
        
        bcConfig.getCobocConfig().setAutoDeployMessageModels(autoDeploy);
    }
}
