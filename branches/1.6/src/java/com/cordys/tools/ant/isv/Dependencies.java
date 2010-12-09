/**
 * Copyright 2007 Cordys R&D B.V. 
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

import org.apache.tools.ant.Project;

import com.cordys.tools.ant.util.GeneralUtils;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * The class handles the content of type dependencies (dependencies between ISVP.
 *
 * @author jpluimer
 */
public class Dependencies implements ISVContentHandler
{
	/**
	 * The root tag of the content type.
	 */
	private static final String CONTENT_ROOT_TAG = "dependencies";

	private static final String DEPENDENCY_TAG = "dependency";

	private static final Object ISVPACKAGE_TAG = "isvpackage";

	private static final Object CN_TAG = "cn";

	/**
	 * Message logged when the root tag of the content type is incorrect
	 */
	private static final String NO_ROOT_TAG = "No content root tag 'dependency' in dependency content xml.";

	/**
	 * Message logged when nested tags are not specified.
	 */
	private static final String NO_NESTED_TAG = "No 'isvpackage' tag specified in dependency content xml.";

	/**
	 * Default Constructor
	 */
	public Dependencies()
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
	public int[] getISVContentXML(ISVCreatorTask isvTask,
																Content contentTask,
																int inputXML,
																int iCurrentIsvContentNode,
																int iCurrentIsvPromptsetNode)
	{
		validateDependencyXML(isvTask, contentTask, inputXML);

		int schemaNode = getFirstDependenciesNode(inputXML);
		if (schemaNode == 0)
		{
			contentTask.log(NO_NESTED_TAG, Project.MSG_INFO);
			return null;
		}

		int dependencyCopy = Node.duplicate(inputXML);

		return new int[] { dependencyCopy };
	}

	/**
	 * Checks the root tag and matches the tags with the content tags.
	 *
	 * @param rootNode The XML loaded from the content file.
	 *
	 * @return The matched content tag.
	 */
	private int getFirstDependenciesNode(int rootNode)
	{
		if (!CONTENT_ROOT_TAG.equals(Node.getName(rootNode)))
		{
			GeneralUtils.handleException(NO_ROOT_TAG);
		}

		return Find.firstMatch(rootNode, "?<" + DEPENDENCY_TAG + ">");
	}

	private void validateDependencyXML(ISVCreatorTask isvTask,
																		 Content contentTask,
																		 int inputXML)
	{
		for (int dependencyNode = Node.getFirstElement(inputXML); dependencyNode != 0; dependencyNode = Node.getNextSibling(dependencyNode))
		{
			if (!Node.getLocalName(dependencyNode).equals(DEPENDENCY_TAG))
			{
				GeneralUtils.handleException("Invalid dependencies xml. Looking for tag ["
																		 + CONTENT_ROOT_TAG
																		 + "/"
																		 + DEPENDENCY_TAG
																		 + "].");
			}
			int isvPackageNode = Node.getFirstElement(dependencyNode);
			if (isvPackageNode == 0
					|| (!Node.getLocalName(isvPackageNode).equals(ISVPACKAGE_TAG)))
			{
				GeneralUtils.handleException("Invalid dependencies xml. Looking for tag ["
																		 + CONTENT_ROOT_TAG
																		 + "/"
																		 + DEPENDENCY_TAG
																		 + "/"
																		 + ISVPACKAGE_TAG
																		 + "].");
			}
			int cnNode = Node.getFirstElement(isvPackageNode);
			if (cnNode == 0 || (!Node.getLocalName(cnNode).equals(CN_TAG)))
			{
				GeneralUtils.handleException("Invalid dependencies xml. Looking for tag ["
																		 + CONTENT_ROOT_TAG
																		 + "/"
																		 + DEPENDENCY_TAG
																		 + "/"
																		 + ISVPACKAGE_TAG
																		 + "/"
																		 + CN_TAG
																		 + "].");
			}

			isvTask.log("["
									+ contentTask.getType()
									+ "] Adding dependency "
									+ Node.getData(cnNode)
									+ " to the ISV package.", Project.MSG_INFO);
		}
	}
}