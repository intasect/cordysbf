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

import com.cordys.coe.exception.GeneralException;

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
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.Project;

/**
 * Typical format of content:<pre><xmp>
 * <busmethodsets loader="com.eibus.contentmanagement.ISVMethodSetManager" description="Method Sets">
 *  <busmethodset>                <entry>                        :(MethodSet)
 *                 </entry>                <entry>
 *                         :(Method)                </entry> <busmethodset>
 * </busmethodsets></xmp></pre>
 *
 * @author msreejit
 */
public class Methodsets extends ISVContentHelper
    implements ISVContentHandler
{
    /**
     * The root tag of the content type.
     */
    private static final String CONTENT_ROOT_TAG = "methodsets";
    /**
     * Message logged when the root tag of the content type is
     * incorrect
     */
    private static final String ROOT_TAG_ERROR = "Root tag of content XML should be 'methodsets'";
    /**
     * Message logged when file handling related exception occurs
     */
    private static final String FILE_ERROR = "Error occured while performing file operations.";
    /**
     * Message logged when xml handling related exception occurs
     */
    private static final String XML_ERROR = "Error occured while performing xml operation.";
    /**
     * Message logged when xml transformation handling related
     * exception occurs
     */
    private static final String XML_TRANSFORMATION_ERROR = "Error occured while applying XSL transformations.";
    /**
     * The root node of the content
     */
    private int contentRootNode;

/**
     * Default Constructor
     */
    public Methodsets()
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
            processExternalFileContents(isvTask, contentTask, inputXML,
                                        "methodset", "<methodsets><methodset>");

            //PGUSSOW: Now we're going to write the different methodsets to the log.
            int[] aiMethodSets = Find.match(inputXML, "<methodsets><methodset>");

            for (int iCount = 0; iCount < aiMethodSets.length; iCount++)
            {
                isvTask.log("[" + contentTask.getType() +
                            "] Adding methodset " +
                            Node.getAttribute(aiMethodSets[iCount], "name") +
                            " to the ISV package.", Project.MSG_INFO);
            }

            fixCobocMethodsForISV(inputXML, iCurrentIsvContentNode);

            //convert the specific xml nodes to xml string.
            XMLUtils.convertNodeToString(inputXML,
                                         "<methodsets><methodset><method><methodimplementation>");
            XMLUtils.convertNodeToString(inputXML,
                                         "<methodsets><methodset><method><methodwsdl>");
            XMLUtils.convertNodeToString(inputXML,
                                         "<methodsets><methodset><method><methodinterface>");
            XMLUtils.convertNodeToString(inputXML,
                                         "<methodsets><methodset><method><busmethodsignature>");
            XMLUtils.convertNodeToString(inputXML,
                                         "<methodsets><methodset><xsd><methodxsd>");

            contentTask.log("Before Transformation:" +
                            Node.writeToString(inputXML, true),
                            Project.MSG_DEBUG);

            InputStream xslStream = FileUtil.getResourceAsStream(Methodsets.class,
                                                                 "xsl/DEV_TO_ISVMETHODSETS.xsl");

            //transform the xml using xsl.
            contentRootNode = XSLUtil.getXSLTransformAsNode(Node.writeToString(inputXML,
                                                                               true),
                                                            xslStream,
                                                            new String[] { "isvDN" },
                                                            new String[]
                                                            {
                                                                isvTask.getIsvDN()
                                                            });
        }
        catch (GeneralException te)
        {
            GeneralUtils.handleException(XML_TRANSFORMATION_ERROR +
                                         te.getMessage(), te, contentTask);
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
                        Node.writeToString(contentRootNode, true),
                        Project.MSG_DEBUG);

        return new int[] { contentRootNode };
    }

    /**
     * Fixes methods that have a reference to a CoBOC content, e.g.
     * method based on a mapping. This class looks into the already generated
     * XML and tried to find the CoBOC object based on the given key.
     *
     * @param xContentNode Method set root node.
     * @param iCurrentIsvContent Current ISV content.
     *
     * @throws GeneralException Thrown if the content handling failed.
     */
    private void fixCobocMethodsForISV(int xContentNode, int iCurrentIsvContent)
                                throws GeneralException
    {
        int[] xaMethodImplNodes = Find.match(xContentNode,
                                             "<methodsets><methodset><method><methodimplementation><implementation type=\"CGMAP\">");
        int[] xaCobocMapTuples = Find.match(iCurrentIsvContent, "?<CPCImporter><MappingContent><map><tuple>");
        Map<String, String> mTupleMap = new HashMap<String, String>();
        
        // Initialize the CoBOC mapping key -> object ID map.
        for (int i = 0; i < xaCobocMapTuples.length; i++)
        {
            int xMapTuple = xaCobocMapTuples[i];
            String sKey = Node.getAttribute(xMapTuple, "key", "");
            String sObjectId = Node.getAttribute(xMapTuple, "entity_id", "");
            
            if (sKey == null || sKey.length() == 0)
            {
                throw new GeneralException("Key attribute not found from CoBOC mapping tuple.");
            }
            
            if (sObjectId == null || sObjectId.length() == 0)
            {
                throw new GeneralException("Object ID attribute not found from CoBOC mapping tuple.");
            }
            
            mTupleMap.put(sKey, sObjectId);
        }

        for (int i = 0; i < xaMethodImplNodes.length; i++)
        {
            int xImplNode = xaMethodImplNodes[i];
            int xMapKeyNode = Find.firstMatch(xImplNode, "<><mapid>");

            if (xMapKeyNode == 0)
            {
                continue;
            }

            String sMapKey = Node.getDataWithDefault(xMapKeyNode, "");

            if (sMapKey.length() == 0)
            {
                continue;
            }

            String sMapId = mTupleMap.get(sMapKey);

            if (sMapId == null || sMapId.length() == 0)
            {
                throw new GeneralException("CoBOC mapping not found in the ISV content. Key=" +
                                           sMapKey);
            }

            Node.setDataElement(xMapKeyNode, "", sMapId);
        }
    }
}
