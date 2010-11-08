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

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.tools.ant.util.FileUtils;

import com.cordys.tools.ant.util.GeneralUtils;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * Helper class for common methods used for ISV Content Handling
 *
 * @author msreejit
 */
public abstract class ISVContentHelper
{
    /**
     * The instance of the <code>FileUtils</code> class.
     */
    public static FileUtils fileUtils = FileUtils.newFileUtils();

    /**
     * Default Constructor
     */
    public ISVContentHelper()
    {
        super();
    }
    
    /**
     * Returns content root node inside the content loaded from the given file.
     * This method is intended to be used for subclasses that need to fetch
     * a subnode from the file content.
     * 
     * This default implementation just returns the file root node. 
     * 
     * @param fFile File in question.
     * @param iFileRootNode XML content loaded from the file.
     * @return Content root node for this content type or zero if no content was found.
     */
    protected int getFileContentNode(File fFile, int iFileRootNode) 
    {
        return iFileRootNode;
    }

    /**
     * Replaces the file pointers with the contents.
     *
     * @param isvTask The instance of ISVCreatorTask class representing the tag
     *        'isv'.
     * @param contentTask The instance of ContentTask class representing the
     *        tag 'content'.
     * @param inputXML The content XML which has to be processed.
     * @param externalRootTag The root tag of the file referenced in the file
     *        attribute.
     * @param patternToMatch The xpath pattern to be matched in the content XML
     *        passed.
     */
    protected void processExternalFileContents(ISVCreatorTask isvTask,
                                               Content contentTask,
                                               int inputXML,
                                               String externalRootTag,
                                               String patternToMatch)
    {
        processExternalContents(isvTask, contentTask, inputXML,
                                externalRootTag, patternToMatch, false);
    }

    /**
     * Replaces the file pointers with the contents of type XMLStore.
     *
     * @param isvTask The instance of ISVCreatorTask class representing the tag
     *        'isv'.
     * @param contentTask The instance of ContentTask class representing the
     *        tag 'content'.
     * @param inputXML The content XML which has to be processed.
     * @param externalRootTag The root tag of the file referenced in the file
     *        attribute.
     * @param patternToMatch The xpath pattern to be matched in the content XML
     *        passed.
     */
    protected void processExternalXMLStoreContents(ISVCreatorTask isvTask,
                                                   Content contentTask,
                                                   int inputXML,
                                                   String externalRootTag,
                                                   String patternToMatch)
    {
        processExternalContents(isvTask, contentTask, inputXML,
                                externalRootTag, patternToMatch, true);
    }
    
    /**
     * Replaces the file pointers with the contents
     *
     * @param isvTask The instance of ISVCreatorTask class representing the tag
     *        'isv'.
     * @param contentTask The instance of ContentTask class representing the
     *        tag 'content'.
     * @param inputXML The content XML which has to be processed.
     * @param externalRootTag The root tag of the file referenced in the file
     *        attribute.
     * @param patternToMatch The xpath pattern to be matched in the content XML
     *        passed.
     * @param isXMLStoreContent Specifies a boolean switch whether the content
     *        type  is a XMLStore kind or ordinary type.
     */
    private void processExternalContents(ISVCreatorTask isvTask,
                                         Content contentTask, int inputXML,
                                         String externalRootTag,
                                         String patternToMatch,
                                         boolean isXMLStoreContent)
    {
        if ((contentTask == null) || (externalRootTag == null) ||
                (patternToMatch == null))
        {
            GeneralUtils.handleException("The parameters specified cannot be null");
        }

        Document document = isvTask.getDocument();
        File referenceFile = null;
        String filePath = "";
        String key = "";
        String name = "";
        String matchedRootTag = "";
        String referenceRootTag = "";

        int[] matchedNodes = Find.match(inputXML, patternToMatch);

        for (int i = 0; i < matchedNodes.length; i++)
        {
            matchedRootTag = Node.getName(matchedNodes[i]);
            filePath = Node.getAttribute(matchedNodes[i], "file",
                                         "File not found");

            //Checks if file attribute exists and is not empty	
            if (filePath.equals("File not found") ||
                    filePath.trim().equals(""))
            {
                //if content is xmlstore specific. check the key and name attribute.
                if (isXMLStoreContent)
                {
                    key = Node.getAttribute(matchedNodes[i], "key");
                    name = Node.getAttribute(matchedNodes[i], "name");

                    if ((key == null) || "".equals(key))
                    {
                        GeneralUtils.handleException("Mandatory attribute 'key' has not been specified!" +
                                                     "\nContent Type :" +
                                                     contentTask.getType());
                    }

                    if ((name == null) || "".equals(name))
                    {
                        name = referenceFile.getName();
                        Node.setAttribute(matchedNodes[i], "name", name);
                    }
                }

                //continue the loop.
                continue;
            }

            //resolve the file for properties if any.
            filePath = contentTask.getProject().replaceProperties(filePath);
            referenceFile = contentTask.getProject().resolveFile(filePath);

            if (!referenceFile.exists())
            {
                GeneralUtils.handleException("Could not find the file referenced in file attribute." +
                                             "\nContent Type :" +
                                             contentTask.getType() +
                                             "\nFile :" + filePath);
            }

            filePath = referenceFile.getAbsolutePath();

            //load the external reference file
            int fileContentNode = 0;

            try
            {
                fileContentNode = document.load(filePath);

                String fileContent = Node.writeToString(fileContentNode, false);
                fileContent = contentTask.getProject().replaceProperties(fileContent);
                Node.delete(fileContentNode);
                fileContentNode = document.parseString(fileContent);
                fileContent = null;
                isvTask.registerNodeForCleanup(fileContentNode);
            }
            catch (XMLException xe)
            {
                GeneralUtils.handleException("Could not parse the xml file referenced in file attribute." +
                                             "\nContent Type :" +
                                             contentTask.getType() +
                                             "\nFile :" + filePath +
                                             "\nDetails :" + xe.getMessage(),
                                             xe, contentTask);
            }
            catch (UnsupportedEncodingException xe)
            {
                GeneralUtils.handleException("Could not parse the xml file referenced in file attribute." +
                                             "\nContent Type :" +
                                             contentTask.getType() +
                                             "\nFile :" + filePath +
                                             "\nException:UnsupportedEncodingException" +
                                             "\nDetails :" + xe.getMessage(),
                                             xe, contentTask);
            }
            
            fileContentNode = getFileContentNode(referenceFile, fileContentNode);
            
            if (fileContentNode == 0)
            {
                GeneralUtils.handleException("Content not found from the file! " +
                                             "\nContent Type :" +
                                             contentTask.getType() +
                                             "\nReferenced file :" + filePath);
            }          

            referenceRootTag = Node.getName(fileContentNode);

            if (!externalRootTag.equals("") &&
                    !externalRootTag.equals(referenceRootTag))
            {
                GeneralUtils.handleException("Invalid root tag for content! " +
                                             "\nContent Type :" +
                                             contentTask.getType() +
                                             "\nReferenced file :" + filePath +
                                             "\nDetails :" +
                                             "The root tag of the content should be " +
                                             externalRootTag);
            }

            //if content is xmlstore specific. check the key and name attribute.
            if (isXMLStoreContent)
            {
                key = Node.getAttribute(matchedNodes[i], "key");
                name = Node.getAttribute(matchedNodes[i], "name");

                if ((key == null) || "".equals(key))
                {
                    GeneralUtils.handleException("Mandatory attribute 'key' has not been specified!" +
                                                 "\nContent Type :" +
                                                 contentTask.getType() +
                                                 "\nReferenced file :" +
                                                 filePath);
                }

                if ((name == null) || "".equals(name))
                {
                    name = referenceFile.getName();
                    Node.setAttribute(matchedNodes[i], "name", name);
                }
            }

            /*
             * check for duplicate root tags.
             * If duplicate tag is noticed then take the tag.
             */
            if (matchedRootTag.equals(referenceRootTag) && !isXMLStoreContent)
            {
                int parentNode = Node.getParent(matchedNodes[i]);

                //remove duplicate node.
                Node.delete(matchedNodes[i]);
                Node.appendToChildren(fileContentNode, parentNode);
            }
            else
            {
                //append the node with root of the referenced content.
                Node.appendToChildren(fileContentNode, matchedNodes[i]);
            }
        }
    }
}
