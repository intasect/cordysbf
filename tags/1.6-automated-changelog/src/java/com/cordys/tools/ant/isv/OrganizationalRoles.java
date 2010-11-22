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

import com.cordys.tools.ant.util.FileUtil;
import com.cordys.tools.ant.util.GeneralUtils;
import com.cordys.tools.ant.util.XMLUtils;
import com.cordys.tools.ant.util.XSLUtil;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.Project;

/**
 * Typical format of content:
 * <pre><xmp>
 * <busorganizationalroles loader="com.eibus.contentmanagement.ISVRoleManager" description="Roles">
 *   <busorganizationalrole>
 * 		<entry>
 * 		:(Role)
 * 		</entry>
 * 		<entry>
 * 		:(ACL)
 * 		</entry>
 * 		<SOAP:Envelope xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/">
 * 	  		<SOAP:Body>
 * 				<UpdateXMLObject xmlns="http://schemas.cordys.com/1.0/xmlstore">
 * 		  			<tuple key="/Cordys/WCP/Menu/Radar/Radar" version="isv" unconditional="true">
 * 						<new>
 * 			  				<menu>
 * 			  				:
 * 			  				</menu>
 * 						</new>
 * 		  			</tuple>
 * 				</UpdateXMLObject>
 * 	  		</SOAP:Body>
 * 		</SOAP:Envelope>
 * 	</busorganizationalrole>
 * </busorganizationalroles>
 * </xmp></pre>
 *
 * @author msreejit
 */
public class OrganizationalRoles extends ISVContentHelper
    implements ISVContentHandler
{
    /**
     * The root tag of the content type.
     */
    private static final String CONTENT_ROOT_TAG = "roles";
    /**
     * Message logged when the root tag of the content type is incorrect
     */
    private static final String ROOT_TAG_ERROR = "Root tag of content XML should be 'roles'";
    /**
     * Message logged when file handling related exception occurs
     */
    private static final String FILE_ERROR = "Error occured while performing file operations.";
    /**
     * Message logged when xml handling related exception occurs
     */
    private static final String XML_ERROR = "Error occured while performing xml operation.";
    /**
     * Message logged when xml transformation handling related exception occurs
     */
    private static final String XML_TRANSFORMATION_ERROR = "Error occured while applying XSL transformations.";
    /**
     * The root node of the content
     */
    private int roleContentRootNode;

    /**
     * Default constructor
     */
    public OrganizationalRoles()
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
        if (!CONTENT_ROOT_TAG.equals(Node.getName(inputXML)))
        {
            GeneralUtils.handleException(ROOT_TAG_ERROR);
        }

        try
        {
            //Replaces content for file pointers
            processExternalFileContents(isvTask, contentTask, inputXML, "role",
                                        "<roles><role>");

            int[] aiContent = Find.match(inputXML, "<roles><role>");

            if (aiContent.length == 0)
            {
                isvTask.log("[" + contentTask.getType() +
                            "] No content found.", Project.MSG_INFO);
            }

            for (int iCount = 0; iCount < aiContent.length; iCount++)
            {
                isvTask.log("[" + contentTask.getType() + "] Adding role " +
                            Node.getAttribute(aiContent[iCount], "name") +
                            " to the ISV package.", Project.MSG_INFO);
            }

            //convert the acl tree to string format.
            XMLUtils.convertNodeToString(inputXML,
                                         "<roles><role><acl><acobjecttree>");

            contentTask.log("Before Transformation:" +
                            Node.writeToString(inputXML, true),
                            Project.MSG_DEBUG);

            InputStream xslFile = FileUtil.getResourceAsStream(OrganizationalRoles.class,
                                                               "xsl/DEV_TO_ISVROLES.xsl");

            //transform the xml using xsl.
            roleContentRootNode = XSLUtil.getXSLTransformAsNode(Node.writeToString(inputXML,
                                                                                   true),
                                                                xslFile,
                                                                new String[]
                                                                {
                                                                    "orgDN",
                                                                    "isvDN"
                                                                },
                                                                new String[]
                                                                {
                                                                    isvTask.getOrganization(),
                                                                    isvTask.getIsvDN()
                                                                });

            // If we have more than one everyoneIn<ISV package> role then this role
            // was specified in the ISV package and we can just delete the generated one.
            int[] xaRoles = Find.match(roleContentRootNode,
                                       "<><busorganizationalrole>");
            int iNumEveryOneInRoles = 0;
            String sIsvPackageName = isvTask.getIsvDN().substring(3,
                                                                  isvTask.getIsvDN()
                                                                         .indexOf(',',
                                                                                  3));

            for (int i = 0; i < xaRoles.length; i++)
            {
                int xRole = xaRoles[i];
                int xDn = Find.firstMatch(xRole, "<><entry><cn><string>");
                String sDn;

                if ((xDn != 0) && ((sDn = Node.getData(xDn)) != null))
                {
                    if (sDn.equals("everyoneIn" + sIsvPackageName))
                    {
                        iNumEveryOneInRoles++;
                    }
                }
            }

            if (iNumEveryOneInRoles > 1)
            {
                int[] xaTmp = new int[xaRoles.length - 1];

                // The created role is always the first.
                Node.delete(xaRoles[0]);

                System.arraycopy(xaRoles, 1, xaTmp, 0, xaTmp.length);
                xaRoles = xaTmp;
            }

            int[] matches = Find.match(roleContentRootNode,
                                       "?<tobereplacedmenu>");

            if (matches.length > 0)
            {
                processMenuReferences(isvTask, contentTask,
                                      iCurrentIsvContentNode,
                                      iCurrentIsvPromptsetNode);
            }

            matches = Find.match(roleContentRootNode, "?<tobereplacedtoolbar>");

            if (matches.length > 0)
            {
                processToolbarReferences(isvTask, contentTask,
                                         iCurrentIsvContentNode,
                                         iCurrentIsvPromptsetNode);
            }
        }
        catch (TransformerException te)
        {
            GeneralUtils.handleException(XML_TRANSFORMATION_ERROR +
                                         te.getMessageAndLocation(), te,
                                         contentTask);
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
        catch (IOException ioe)
        {
            GeneralUtils.handleException(FILE_ERROR + ioe.getMessage(), ioe,
                                         contentTask);
        }

        contentTask.log("Final Content Node" +
                        Node.writeToString(roleContentRootNode, true),
                        Project.MSG_DEBUG);

        return new int[] { roleContentRootNode };
    }

    /**
     * Returns true if the given toolbar belong to standard cordys
     * installation. Note that this method might not check correcly all cases.
     *
     * @param sKey Toolbar key to be checked.
     *
     * @return True, if this toolbar is part of standard installation.
     */
    protected boolean isSystemMenu(String sKey)
    {
        sKey = sKey.toLowerCase();

        if (sKey.startsWith("/cordys/wcp/menu/cordys/bac") ||
                sKey.startsWith("/cordys/wcp/menu/cordys/cas") ||
                sKey.startsWith("/cordys/wcp/menu/cordys/cc") ||
                sKey.startsWith("/cordys/wcp/menu/cordys/cl") ||
                sKey.startsWith("/cordys/wcp/menu/cordys/orchestrator") ||
                sKey.startsWith("/cordys/wcp/menu/cordys/studio") ||
                sKey.startsWith("/cordys/wcp/menu/cordys/wcp"))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns true if the given toolbar belong to standard cordys
     * installation. Note that this method might not check correcly all cases.
     *
     * @param sKey Toolbar key to be checked.
     *
     * @return True, if this toolbar is part of standard installation.
     */
    protected boolean isSystemToolbar(String sKey)
    {
        sKey = sKey.toLowerCase();

        if (sKey.startsWith("/cordys/wcp/toolbar/cordys/wcp/"))
        {
            return true;
        }

        return false;
    }

    /**
     * Method processes all the menu references and replaces the tag
     * 'tobereplaced' which is a placeholder put by the XSL Stylesheet with
     * the menu contents as required by the roles ISV XML.
     *
     * @param isvTask The instance of ISVCreatorTask class representing the tag
     *        'isv'.
     * @param contentTask The instance of ContentTask class representing the
     *        tag 'content'.
     * @param iCurrentIsvContentNode DOCUMENTME
     * @param iCurrentIsvPromptsetNode DOCUMENTME
     *
     * @throws XMLException If the menu processing could not load the content
     *         xml.
     */
    private void processMenuReferences(ISVCreatorTask isvTask,
                                       Content contentTask,
                                       int iCurrentIsvContentNode,
                                       int iCurrentIsvPromptsetNode)
                                throws XMLException
    {
        int[] replaceNodes = Find.match(roleContentRootNode,
                                        "?<tobereplacedmenu>");

        if (replaceNodes.length == 0)
        {
            //if menu is not configured do not proceed.
            return;
        }

        Map<String, Content> contents = isvTask.getContents();
        Content menucontent = (Content) contents.get("menus");

        if (menucontent != null)
        {
            //get the menu contents
            int[] iaMenuContentNodes = menucontent.getISVContentXML(isvTask,
                                                                    iCurrentIsvContentNode,
                                                                    iCurrentIsvPromptsetNode);
            int menuContentNode = ((iaMenuContentNodes != null) &&
                                  (iaMenuContentNodes.length > 0))
                                  ? iaMenuContentNodes[0] : 0;

            for (int k = 0; k < replaceNodes.length; k++)
            {
                String replacementKey = Node.getAttribute(replaceNodes[k], "key");

                //get the parent of the node to be replaced.
                int parentNode = Node.getParent(replaceNodes[k]);

                //find the same menu in menu contents.
                int matchedNode = Find.firstMatch(menuContentNode,
                                                  "?<tuple key=\"" +
                                                  replacementKey +
                                                  "\"><new><menu>");

                if (matchedNode != 0)
                {
                    //Delete the replacement reference node.
                    Node.delete(replaceNodes[k]);
                    //The node 'matchedNode' may be needed again for menu references in other roles.
                    //so clone and append that to the parent node.
                    Node.appendToChildren(Node.clone(matchedNode, true),
                                          parentNode);
                }
                else
                {
                    if (!isSystemMenu(replacementKey))
                    {
                        GeneralUtils.handleException("Invalid menu reference in roles! Could not find the menu with key:" +
                                                     replacementKey);
                    }

                    int iEnvelopeNode;

                    // This is a standard menu, so just delete the extra XMLStore message.
                    iEnvelopeNode = Find.firstMatch(replaceNodes[k],
                                                    "parent.parent.parent.parent.parent");

                    if (iEnvelopeNode == 0)
                    {
                        GeneralUtils.handleException("Menu SOAP:Envelope node found for standard Cordys menu: " +
                                                     replacementKey);
                        return;
                    }

                    Node.unlink(iEnvelopeNode);
                    Node.delete(iEnvelopeNode);
                }
            }
        }
        else
        {
            GeneralUtils.handleException("Invalid menu reference in roles! Configuration of menus should be done.");
        }
    }

    /**
     * Method processes all the toolbar references and replaces the tag
     * 'tobereplacedtoolbar' which is a placeholder put by the XSL Stylesheet
     * with the menu contents as required by the roles ISV XML.
     *
     * @param isvTask The instance of ISVCreatorTask class representing the tag
     *        'isv'.
     * @param contentTask The instance of ContentTask class representing the
     *        tag 'content'.
     * @param iCurrentIsvContentNode DOCUMENTME
     * @param iCurrentIsvPromptsetNode DOCUMENTME
     *
     * @throws XMLException If the toolbar processing could not load the
     *         content xml.
     */
    private void processToolbarReferences(ISVCreatorTask isvTask,
                                          Content contentTask,
                                          int iCurrentIsvContentNode,
                                          int iCurrentIsvPromptsetNode)
                                   throws XMLException
    {
        int[] replaceNodes = Find.match(roleContentRootNode,
                                        "?<tobereplacedtoolbar>");

        if (replaceNodes.length == 0)
        {
            //if toolbar is not configured do not proceed.
            return;
        }

        Map<String, Content> contents = isvTask.getContents();
        Content toolbarcontent = (Content) contents.get("toolbars");

        if (toolbarcontent != null)
        {
            //get the menu contents
            int[] iaToolbarContentNodes = toolbarcontent.getISVContentXML(isvTask,
                                                                          iCurrentIsvContentNode,
                                                                          iCurrentIsvPromptsetNode);
            int toolbalContentNode = ((iaToolbarContentNodes != null) &&
                                     (iaToolbarContentNodes.length > 0))
                                     ? iaToolbarContentNodes[0] : 0;

            for (int k = 0; k < replaceNodes.length; k++)
            {
                String replacementKey = Node.getAttribute(replaceNodes[k], "key");

                //get the parent of the node to be replaced.
                int parentNode = Node.getParent(replaceNodes[k]);

                //find the same toolbar in toolbar contents.
                int matchedNode = Find.firstMatch(toolbalContentNode,
                                                  "?<tuple key=\"" +
                                                  replacementKey +
                                                  "\"><new><menu>");

                if (matchedNode != 0)
                {
                    //Delete the replacement reference node.
                    Node.delete(replaceNodes[k]);
                    //The node 'matchedNode' may be needed again for toolbar references in other roles.
                    //so clone and append that to the parent node.
                    Node.appendToChildren(Node.clone(matchedNode, true),
                                          parentNode);
                }
                else
                {
                    if (!isSystemToolbar(replacementKey))
                    {
                        GeneralUtils.handleException("Invalid toolbar reference in roles! Could not find the toolbar with key: " +
                                                     replacementKey);
                        return;
                    }

                    int iEnvelopeNode;

                    // This is a standard toolbar, so just delete the extra XMLStore message.
                    iEnvelopeNode = Find.firstMatch(replaceNodes[k],
                                                    "parent.parent.parent.parent.parent");

                    if (iEnvelopeNode == 0)
                    {
                        GeneralUtils.handleException("Toolbar SOAP:Envelope node found for standard Cordys toolbar: " +
                                                     replacementKey);
                        return;
                    }

                    Node.unlink(iEnvelopeNode);
                    Node.delete(iEnvelopeNode);
                }
            }
        }
        else
        {
            GeneralUtils.handleException("Invalid toolbar reference in roles! Configuration of toolbars should be done.");
        }
    }
}
