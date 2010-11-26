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
 * DBSchema contents - A ISV Package Content type which defines what dbschema
 * should be handled by the ISV Package creator. Typical format in ISV XML:
 * <pre><xmp>
 * 		<dbschema
 * 				loader="com.eibus.contentmanagement.ISVDatabaseManager"
 * 				description="Database Manager">
 * 			<schema loader="" unloader="" />
 * 		</dbschema>
 * </xmp></pre>
 * Example of contents configured in content file:
 * <pre><xmp>
 * Root tag of the content xml is <i>dbschema</i>.
 * Every content is enclosed within the <i>schema<i> tag.
 * The required attributes for the <i>schema<i> tag.
 * 		loader="The path of the DB installation file."
 * 		unloader="The path of the DB un-installation file."
 * 
 * 		<dbschema>
 *   		<schema 
 * 				loader="${src.content}/DBSchema/HelloWorldDBInstall.sql"
 * 				unloader="${src.content}/DBSchema/HelloWorldDBUnInstall.sql" />
 * 		</dbschema>
 * </xmp></pre>
 *
 * @author knayak
 */
public class DBSchema
    implements ISVContentHandler
{
    /**
     * The xml document that is used to handle xml functions.
     */
    private static Document document;
    /**
     * The class name of the content loader.
     */
    private static final String CONTENT_LOADER = "com.eibus.contentmanagement.ISVDatabaseManager";
    /**
     * The description of the content.
     */
    private static final String CONTENT_DESCRIPTION = "Database Manager";
    /**
     * The root tag of the content type.
     */
    private static final String CONTENT_ROOT_TAG = "dbschema";
    /**
     * Message logged when nested tags are not specified.
     */
    private static final String NO_NESTED_TAG = "No 'schema' tag specified in dbschema content xml.";
    /**
     * Message logged when the root tag of the content type is incorrect
     */
    private static final String NO_ROOT_TAG = "No content root tag 'dbschema' in dbschema content xml.";
    /**
     * The root node of the content
     */
    private int contentRootNode;

    /**
     * Default Constructor
     */
    public DBSchema()
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

        contentRootNode = document.createElement(CONTENT_ROOT_TAG);
        Node.setAttribute(contentRootNode, "loader", CONTENT_LOADER);
        Node.setAttribute(contentRootNode, "description", CONTENT_DESCRIPTION);
        isvTask.registerNodeForCleanup(contentRootNode);

        int schemaNode = getSchemaNode(inputXML);

        if (schemaNode == 0)
        {
            contentTask.log(NO_NESTED_TAG, Project.MSG_INFO);

            return null;
        }

        String loader;
        String unloader;

        int isvSchemaNode = document.createElement("schema", contentRootNode);

        loader = Node.getAttribute(schemaNode, "loader");
        loader = contentTask.getProject().replaceProperties(loader);
        loader = contentTask.getProject().resolveFile(loader).getAbsolutePath();

        unloader = Node.getAttribute(schemaNode, "unloader");
        unloader = contentTask.getProject().replaceProperties(unloader);
        unloader = contentTask.getProject().resolveFile(unloader)
                              .getAbsolutePath();

        Node.setAttribute(isvSchemaNode, "loader", loader);
        Node.setAttribute(isvSchemaNode, "unloader", unloader);

        return new int[] { contentRootNode };
    }

    /**
     * Checks the root tag and matches the tags with the content tags.
     *
     * @param rootNode The XML loaded from the content file.
     *
     * @return The matched content tag.
     */
    private int getSchemaNode(int rootNode)
    {
        //check for root tag.
        if (!CONTENT_ROOT_TAG.equals(Node.getName(rootNode)))
        {
            GeneralUtils.handleException(NO_ROOT_TAG);
        }

        return Find.firstMatch(rootNode, "?<schema>");
    }
}
