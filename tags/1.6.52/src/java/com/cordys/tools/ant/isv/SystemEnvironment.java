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
 * SystemEnvironment content - A ISV Package Content Type which defines what
 * environment variables should be handled by the ISV Package creator. Typical
 * format in ISV XML:
 * <pre><xmp>
 * <systemenvironment loader="com.eibus.contentmanagement.ISVSystemEnvironmentManager" description="Environment Settings">
 * 		<variables name="" variables="" />
 * 		:
 * </systemenvironment>
 * </xmp></pre>
 *
 * @author msreejit
 */
public class SystemEnvironment
    implements ISVContentHandler
{
    /**
     * The xml document that is used to handle xml functions.
     */
    private static Document document;
    /**
     * The class name of the content loader.
     */
    private static final String CONTENT_LOADER = "com.eibus.contentmanagement.ISVSystemEnvironmentManager";
    /**
     * The description of the content.
     */
    private static final String CONTENT_DESCRIPTION = "Environment Settings";
    /**
     * The root tag of the content type.
     */
    private static final String CONTENT_ROOT_TAG = "systemenvironment";
    /**
     * Message logged when nested tags are not specified.
     */
    private static final String NO_NESTED_TAG = "No 'variable' tag specified in system environment content xml";
    /**
     * Message logged when the root tag of the content type is incorrect
     */
    private static final String NO_ROOT_TAG = "No content root tag 'systemenvironment' in system environment content xml";
    /**
     * The root node of the content
     */
    private int contentRootNode;

    /**
     * Default constructor
     */
    public SystemEnvironment()
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
        int[] variableNodes = getVariableNodes(inputXML);

        if (variableNodes.length == 0)
        {
            contentTask.log(NO_NESTED_TAG, Project.MSG_INFO);
            return null;
        }

        document = isvTask.getDocument();
        contentRootNode = document.createElement(CONTENT_ROOT_TAG);
        Node.setAttribute(contentRootNode, "loader", CONTENT_LOADER);
        Node.setAttribute(contentRootNode, "description", CONTENT_DESCRIPTION);

        String name;
        String value;

        for (int i = 0; i < variableNodes.length; i++)
        {
            name = Node.getAttribute(variableNodes[i], "name");
            name = contentTask.getProject().replaceProperties(name);

            value = Node.getAttribute(variableNodes[i], "value");
            value = contentTask.getProject().replaceProperties(value);

            String ifProperty = Node.getAttribute(variableNodes[i], "if", null);
            String unlessProperty = Node.getAttribute(variableNodes[i],
                                                      "unless", null);

            //If unless property is specified & the property is set, 
            //we should not add that filenode to isv xml
            if ((unlessProperty != null) &&
                    (contentTask.getProject().getProperty(unlessProperty) != null))
            {
                contentTask.log("Variable '" + name +
                                "' skipped because property unless=\"" +
                                unlessProperty + "\" was set.", Project.MSG_INFO);
                continue;
            }

            //If property is set, then we should have the property defined
            //to add filenode to isv xml
            if ((ifProperty != null) &&
                    (contentTask.getProject().getProperty(ifProperty) == null))
            {
                contentTask.log("Variable '" + name +
                                "' skipped because property if=\"" +
                                ifProperty + "\" was set.", Project.MSG_INFO);
                continue;
            }

            int varNode = document.createElement("variable", contentRootNode);

            isvTask.log("[" + contentTask.getType() + "] Adding variable " +
                        name + " to the ISV package.", Project.MSG_INFO);

            Node.setAttribute(varNode, "name", name);
            Node.setAttribute(varNode, "value", value);
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
    private int[] getVariableNodes(int rootNode)
    {
        //check for root tag.
        if (!CONTENT_ROOT_TAG.equals(Node.getName(Node.getRoot(rootNode))))
        {
            GeneralUtils.handleException(NO_ROOT_TAG);
        }

        return Find.match(rootNode, "?<variable>");
    }
}
