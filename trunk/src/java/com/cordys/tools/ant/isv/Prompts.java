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
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import org.apache.tools.ant.Project;

/**
 * Prompts content - A ISV Package Content Type which defines the prompts to be
 * handled by the ISV Package creator. Typical format in ISV XML:
 * <pre>
 * <prompts>
 * 		&lt;prompt id="Prompt01" description="Prompt 01"/&gt;
 * 		&lt;prompt id="Prompt02" description="Prompt 02"/&gt;
 * &lt;/prompts>
 * </pre>
 *
 * @author msreejit
 */
public class Prompts
    implements ISVContentHandler
{
    /**
     * The xml document that is used to handle xml functions.
     */
    private static Document document;
    /**
     * The root tag of the content type.
     */
    private static final String CONTENT_ROOT_TAG = "prompts";
    /**
     * Message logged when nested tags are not specified.
     */
    private static final String NO_NESTED_TAG = "No 'prompt' tag specified in prompts content xml";
    /**
     * Message logged when the root tag of the content type is incorrect
     */
    private static final String NO_ROOT_TAG = "No content root tag 'prompts' in prompts content xml";
    /**
     * The root node of the content
     */
    private int contentRootNode;

    /**
     * Default constructor
     */
    public Prompts()
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
        int[] promptNodes = getPromptNodes(inputXML);

        if (promptNodes.length == 0)
        {
            contentTask.log(NO_NESTED_TAG, Project.MSG_DEBUG);
            return null;
        }

        document = isvTask.getDocument();
        //TODO Change here for ISVC: id attribute for ISVC
        contentRootNode = document.createElement(CONTENT_ROOT_TAG);

        String id;
        String description;

        for (int i = 0; i < promptNodes.length; i++)
        {
            int promptNode = document.createElement("prompt", contentRootNode);

            id = Node.getAttribute(promptNodes[i], "id");
            id = contentTask.getProject().replaceProperties(id);

            description = Node.getAttribute(promptNodes[i], "description");
            description = contentTask.getProject().replaceProperties(description);

            isvTask.log("[" + contentTask.getType() + "] Adding prompt " + id +
                        " to the ISV package.", Project.MSG_INFO);

            Node.setAttribute(promptNode, "id", id);
            Node.setAttribute(promptNode, "description", description);
        }

        return new int[] { contentRootNode };
    }

    /**
     * Checks the root tag and matches the tags with the content tags.
     *
     * @param rootNode The XML loaded from the content file.
     *
     * @return The matched content tags.
     */
    private int[] getPromptNodes(int rootNode)
    {
        //check for root tag.
        if (!CONTENT_ROOT_TAG.equals(Node.getName(rootNode)))
        {
            GeneralUtils.handleException(NO_ROOT_TAG);
        }

        return Find.match(rootNode, "?<prompt>");
    }
}
