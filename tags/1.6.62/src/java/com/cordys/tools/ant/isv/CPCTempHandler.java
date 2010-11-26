/**
 * Copyright 2005 Cordys R&D B.V. 
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

package com.cordys.tools.ant.isv;

import com.cordys.tools.ant.util.GeneralUtils;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

import java.io.UnsupportedEncodingException;

import org.apache.tools.ant.Project;

/**
 * This is a workaround class for putting CPC contents in the ISVP. The class
 * expects the CPC root xml of the following format. Typical format of
 * content:
 * <pre>
 * <CPCImporter xmlns="http://schemas.cordys.com/1.0/coboc" type="ISV" loader="com.cordys.cpc.deployment.loaders.CPCDeploymentLoader" description="The Orchestrator Deployment Loader" url="/cordys/cpc/deployment/deploymentwizard.htm">
 *   <FolderContent/>
 *   <MappingContent/>
 *   <ProcessFlowContent/>
 * </CPCImporter>
 * </pre>
 *
 * @author msreejit
 */
public class CPCTempHandler extends ISVContentHelper
    implements ISVContentHandler
{
    /**
     * The xml document that is used to handle xml functions.
     */
    private static Document document;
    /**
     * Message logged when xml handling related exception occurs
     */
    private static final String XML_ERROR = "Error occured while performing xml operation.";
    /**
     * The root node of the content
     */
    private int contentRootNode;

    /**
     * Default Constructor
     */
    public CPCTempHandler()
    {
        super();
    }

    /**
     * Implementation of the abstract method getContentXML
     *
     * @param isvTask The instance of ISVCreatorTask class representing the tag
     *        'isv'.
     * @param contentTask The instance of ContentTask class representing the
     *        tag 'content'.
     * @param inputXML The content XML which has to be processed.
     * @param iCurrentIsvContentNode DOCUMENTME
     * @param iCurrentIsvPromptsetNode DOCUMENTME
     *
     * @return The content XML created by this content creator
     */
    public int[] getISVContentXML(ISVCreatorTask isvTask, Content contentTask,
                                  int inputXML, int iCurrentIsvContentNode,
                                  int iCurrentIsvPromptsetNode)
    {
        document = isvTask.getDocument();

        try
        {
            contentRootNode = document.parseString(Node.writeToString(inputXML,
                                                                      true));
        }
        catch (UnsupportedEncodingException ue)
        {
            GeneralUtils.handleException(XML_ERROR + ue.getMessage(), ue,
                                         contentTask);
        }
        catch (XMLException xe)
        {
            GeneralUtils.handleException(XML_ERROR + xe.getMessage(), xe,
                                         contentTask);
        }

        contentTask.log("Final Content Node" +
                        Node.writeToString(contentRootNode, true),
                        Project.MSG_DEBUG);

        return new int[] { contentRootNode };
    }
}
