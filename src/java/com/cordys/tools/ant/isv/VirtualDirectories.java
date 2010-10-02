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
 * VirtualDirectory contents - A ISV Package Content type which defines what
 * virtual directories should be handled by the ISV Package creator. Typical
 * format in ISV XML:
 * <pre><xmp>
 * 		<webservermanager 
 * 				loader="com.eibus.contentmanagement.ISVWebServerManager">
 * 				description="WebServer Manager" 
 * 			<virtualroot alias="" physicalpath="">
 * 				<authtype>Basic</authtype>
 * 			</virtualroot>
 * 		</webservermanager>
 * </xmp></pre>
 * Example of contents configured in content file:
 * <pre><xmp>
 * Root tag of the content xml is <i>virtualdirectories</i>.
 * Every content is enclosed within the <i>virtualdirectory<i> tag.
 * The required attributes for the <i>virtualdirectory<i> tag.
 * 		alias="Alias name for the virtual directory"
 * 		physicalpath="physical path of the the virtual directory on the machine"
 * 		authtype="Basic|Inherit|Anonymous"
 * 		<virtualdirectories>
 * 			<virtualdirectory 
 * 				alias="helloworld" 
 * 				physicalpath="cordys_web_dir\helloworld" 
 * 				authtype="Basic" />
 * 		</virtualdirectories>
 * </xmp></pre>
 *
 * @author knayak
 */
public class VirtualDirectories
    implements ISVContentHandler
{
    /**
     * The xml document that is used to handle xml functions.
     */
    private static Document document;
    /**
     * The class name of the content loader.
     */
    private static final String CONTENT_LOADER = "com.eibus.contentmanagement.ISVWebServerManager";
    /**
     * The description of the content.
     */
    private static final String CONTENT_DESCRIPTION = "WebServer Manager";
    /**
     * The root tag of the content type.
     */
    private static final String CONTENT_ROOT_TAG = "virtualdirectories";
    /**
     * Message logged when nested tags are not specified.
     */
    private static final String NO_NESTED_TAG = "No 'virtualdirectory' tag specified in virtualdirectories content xml.";
    /**
     * Message logged when the root tag of the content type is incorrect
     */
    private static final String NO_ROOT_TAG = "No content root tag 'virtualdirectories' in virtualdirectories content xml.";
    /**
     * The root node of the content
     */
    private int contentRootNode;

    /**
     * Default Constructor
     */
    public VirtualDirectories()
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
        int[] virtualDirectoryNodes = getVirtualDirectory(inputXML);

        if (virtualDirectoryNodes.length == 0)
        {
            contentTask.log(NO_NESTED_TAG, Project.MSG_INFO);
            return null;
        }

        document = isvTask.getDocument();
        contentRootNode = document.createElement(CONTENT_ROOT_TAG);
        Node.setAttribute(contentRootNode, "loader", CONTENT_LOADER);
        Node.setAttribute(contentRootNode, "description", CONTENT_DESCRIPTION);

        String alias;
        String physicalpath;
        String authtype;

        for (int i = 0; i < virtualDirectoryNodes.length; i++)
        {
            int isvSchemaNode = document.createElement("virtualroot",
                                                       contentRootNode);

            alias = Node.getAttribute(virtualDirectoryNodes[i], "alias");
            physicalpath = Node.getAttribute(virtualDirectoryNodes[i],
                                             "physicalpath");

            isvTask.log("[" + contentTask.getType() +
                        "] Adding virtual directory " + alias +
                        " to the ISV package.", Project.MSG_INFO);
            //Modified by Manesh
            //We need to specify property based value in physical path
            physicalpath = contentTask.getProject().replaceProperties(physicalpath);

            Node.setAttribute(isvSchemaNode, "alias", alias);
            Node.setAttribute(isvSchemaNode, "physicalpath", physicalpath);

            authtype = Node.getAttribute(virtualDirectoryNodes[i], "authtype");
            Node.setDataElement(isvSchemaNode, "authtype", authtype);
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
    private int[] getVirtualDirectory(int rootNode)
    {
        //check for root tag.
        if (!CONTENT_ROOT_TAG.equals(Node.getName(Node.getRoot(rootNode))))
        {
            GeneralUtils.handleException(NO_ROOT_TAG);
        }

        return Find.match(rootNode, "?<virtualdirectory>");
    }
}
