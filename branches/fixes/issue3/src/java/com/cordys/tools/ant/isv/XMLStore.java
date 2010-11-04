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
import com.eibus.xml.nom.XMLException;

import java.io.UnsupportedEncodingException;

import org.apache.tools.ant.Project;

/**
 * Typical format of content:
 * <pre>
 *  &lt;xmlstore loader=&quot;com.eibus.contentmanagement.ISVSOAPManager&quot; description=&quot;XMLStore&quot;&gt;
 *  		&lt;SOAP:Envelope xmlns:SOAP=&quot;http://schemas.xmlsoap.org/soap/envelope/&quot;&gt;
 *  			&lt;SOAP:Body&gt;
 *  				&lt;UpdateXMLObject xmlns=&quot;http://schemas.cordys.com/1.0/xmlstore&quot;&gt;
 *  					&lt;tuple version=&quot;isv&quot; unconditional=&quot;true&quot;&gt;
 *  						&lt;new&gt;
 *  							:
 *  							:
 *  							:
 *  						&lt;/new&gt;
 *  					&lt;/tuple&gt;
 *  				&lt;/UpdateXMLObject&gt;
 *  			&lt;/SOAP:Body&gt;
 *  		&lt;/SOAP:Envelope&gt;
 *  		&lt;SOAP:Envelope&gt;
 *  		:
 *  		&lt;/SOAP:Envelope&gt;
 *  &lt;/xmlstore&gt;
 * </pre>
 *
 * @author msreejit
 */
public class XMLStore extends ISVContentHelper
    implements ISVContentHandler
{
    /**
     * The xml document that is used to handle xml functions.
     */
    private static Document document;
    /**
     * Message logged when the root tag of the content type is incorrect
     */
    private static final String ROOT_TAG_ERROR = "Root tag of content XML should be ";
    /**
     * Message logged when xml handling related exception occurs
     */
    private static final String XML_ERROR = "Error occured while performing xml operation.";
    /**
     * The template used for xmlstore ISV content.
     */
    public static final String XMLSTORE_ISV_TEMPLATE =
        "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        "<SOAP:Body>" +
        "<UpdateXMLObject xmlns=\"http://schemas.cordys.com/1.0/xmlstore\">" +
        "<tuple version=\"isv\" unconditional=\"true\">" + "<new>" + "</new>" +
        "</tuple>" + "</UpdateXMLObject>" + "</SOAP:Body>" +
        "</SOAP:Envelope>";
    /**
     * The root tag of the content type.
     */
    private String contentRootTag = "xmlcontents";
    /**
     * The xpath pattern to be matched in the content XML passed.
     */
    private String contentTagMatchPattern = "<xmlcontents><xmlcontent>";
    /**
     * The Actual root of the content.
     */
    private String internalRootTag = "";
    /**
     * The Actual root of the content.
     */
    private String isvContentDescription = "XMLStore";
    /**
     * The xpath pattern to be matched in the content XML passed.
     */
    private String isvContentLoader = "com.eibus.contentmanagement.ISVSOAPManager";
    /**
     * The xpath pattern to be matched in the content XML passed.
     */
    private String isvContentRootTag = "xmlstore";
    /**
     * The root node of the content
     */
    private int contentRootNode;
    /**
     * Current ISV task object.
     */
    protected ISVCreatorTask currentIsvTask; 

    /**
     * Default Constructor
     */
    public XMLStore()
    {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @param contentRootTag The contentRootTag to set.
     */
    public void setContentRootTag(String contentRootTag)
    {
        this.contentRootTag = contentRootTag;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the contentRootTag.
     */
    public String getContentRootTag()
    {
        return contentRootTag;
    }

    /**
     * DOCUMENT ME!
     *
     * @param contentTagMatchPattern The contentTagMatchPattern to set.
     */
    public void setContentTagMatchPattern(String contentTagMatchPattern)
    {
        this.contentTagMatchPattern = contentTagMatchPattern;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the contentTagMatchPattern.
     */
    public String getContentTagMatchPattern()
    {
        return contentTagMatchPattern;
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
        currentIsvTask = isvTask;

        if (!contentRootTag.equals(Node.getName(inputXML)))
        {
            GeneralUtils.handleException(ROOT_TAG_ERROR + "'" + contentRootTag +
                                         "'.");
        }

        try
        {
            //Replaces content for file pointers
            processExternalXMLStoreContents(isvTask, contentTask, inputXML,
                                            internalRootTag,
                                            contentTagMatchPattern);

            isvTask.log("[" + contentTask.getType() + "] Looking for tag " +
                        contentRootTag, Project.MSG_DEBUG);

            int[] aiContent = Find.match(inputXML, contentTagMatchPattern);

            if (aiContent.length == 0)
            {
                isvTask.log("[" + contentTask.getType() +
                            "] No content found.", Project.MSG_INFO);
            }

            for (int iCount = 0; iCount < aiContent.length; iCount++)
            {
                isvTask.log("[" + contentTask.getType() + "] Adding " +
                            Node.getAttribute(aiContent[iCount], "name") +
                            " to the ISV package.", Project.MSG_INFO);
            }
            
            modifyIsvXml(inputXML, iCurrentIsvContentNode);

            contentRootNode = document.createElement(isvContentRootTag);
            Node.setAttribute(contentRootNode, "loader", isvContentLoader);
            Node.setAttribute(contentRootNode, "description",
                              isvContentDescription);
            isvTask.registerNodeForCleanup(contentRootNode);

            int[] xmlContentNodes = Find.match(inputXML, contentTagMatchPattern);
            int templateNode = document.parseString(XMLSTORE_ISV_TEMPLATE);
            isvTask.registerNodeForCleanup(templateNode);

            int tupleNode = 0;
            int newNode = 0;
            int xmlNode = 0;
            String key;

            for (int i = 0; i < xmlContentNodes.length; i++)
            {
                int tempNode = Node.duplicate(templateNode);
                tupleNode = Find.firstMatch(tempNode, "?<tuple>");
                key = Node.getAttribute(xmlContentNodes[i], "key");
                newNode = Find.firstMatch(tupleNode, "?<new>");
                Node.setAttribute(tupleNode, "key", key);
                xmlNode = Node.getFirstElement(xmlContentNodes[i]);

                //The node 'xmlNode' may be needed again for reference.
                //so clone and append that to the parent node.
                Node.appendToChildren(Node.duplicate(xmlNode), newNode);
                Node.appendToChildren(tempNode, contentRootNode);
            }
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

        contentTask.log("Final Content Node:" +
                        Node.writeToString(contentRootNode, true),
                        Project.MSG_DEBUG);
        
        currentIsvTask = null;

        return new int[] { contentRootNode };
    }

    /**
     * Call back for child classes. Can be used to modify the ISV XMl before
     * it is being written to the ISV package.
     * @param xInputXML Generated XML store ISV contentXML. 
     * @param xCurrentIsvContentNode XML of the whole ISV package that is generated so far.
     */
    protected void modifyIsvXml(int xInputXML, int xCurrentIsvContentNode)
    {
        // Modify BAM content.
        String bamRootKey = "/Cordys/BAM/";
        int[] xmlContentNodes = Find.match(xInputXML, contentTagMatchPattern);
        
        for (int i = 0; i < xmlContentNodes.length; i++)
        {
            String key = Node.getAttribute(xmlContentNodes[i], "key");
            int tmpNode;
            
            if (key == null || ! key.startsWith(bamRootKey)) {
                continue;
            }
         
            tmpNode = Find.firstMatch(xmlContentNodes[i], "<><><metainfo><status>");
            if (tmpNode != 0) {
                Node.setDataElement(tmpNode, "", "Unpublished");
            }
            
            tmpNode = Find.firstMatch(xmlContentNodes[i], "<><><metainfo><modificationkey>");
            if (tmpNode != 0) {
                Node.setDataElement(tmpNode, "", "");
            }
            
            tmpNode = Find.firstMatch(xmlContentNodes[i], "<><><metainfo><source>");
            if (tmpNode != 0) {
                Node.setDataElement(tmpNode, "", "isv");
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param internalRootTag The internalRootTag to set.
     */
    public void setInternalRootTag(String internalRootTag)
    {
        this.internalRootTag = internalRootTag;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the internalRootTag.
     */
    public String getInternalRootTag()
    {
        return internalRootTag;
    }

    /**
     * DOCUMENT ME!
     *
     * @param isvContentDescription The isvContentDescription to set.
     */
    public void setIsvContentDescription(String isvContentDescription)
    {
        this.isvContentDescription = isvContentDescription;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the isvContentDescription.
     */
    public String getIsvContentDescription()
    {
        return isvContentDescription;
    }

    /**
     * DOCUMENT ME!
     *
     * @param isvContentLoader The isvContentLoader to set.
     */
    public void setIsvContentLoader(String isvContentLoader)
    {
        this.isvContentLoader = isvContentLoader;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the isvContentLoader.
     */
    public String getIsvContentLoader()
    {
        return isvContentLoader;
    }

    /**
     * DOCUMENT ME!
     *
     * @param isvContentRootTag The isvContentRootTag to set.
     */
    public void setIsvContentRootTag(String isvContentRootTag)
    {
        this.isvContentRootTag = isvContentRootTag;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the isvContentRootTag.
     */
    public String getIsvContentRootTag()
    {
        return isvContentRootTag;
    }
}
