/**
 * Copyright 2006 Cordys R&D B.V. 
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

import org.apache.tools.ant.Project;

import com.cordys.tools.ant.util.GeneralUtils;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * This class makes sure that the reports are packaged in the ISV package.
 *
 * @author  pgussow
 */
public class XReports extends ISVContentHelper
    implements ISVContentHandler
{
    /**
     * The root tag of the content type.
     */
    private static final String CONTENT_ROOT_TAG = "xreports";
    /**
     * Message logged when the root tag of the content type is incorrect.
     */
    private static final String ROOT_TAG_ERROR = "Root tag of content XML should be 'xreports'";
    

    /**
     * The method should be implemented by the ISV content handlers and will be called accordingly.
     * The task instance is passed on for performing certains tasks in the ant workspace. The ISV
     * content should be returned as an Node reference which will be appended to the ISV xml and
     * send by the <code>ISVCreatorTask</code> task. The input xml is the consolidated xml prepared
     * the <code>ISVCreatorTask</code> task. The xml is usually specified in the content file
     * attribute which points to a configuration file.
     *
     * @param   isvTask                   The ISV creator task that requests the ISV XML.
     * @param   contentTask               The corresponding content-task.
     * @param   inputXML                  The input XML structure.
     * @param   iCurrentIsvContentNode    Contains already generated ISV content.
     * @param   iCurrentIsvPromptsetNode  Contains already generated ISV prompts content.
     *
     * @return  The content XML created by this ISV content creator.
     *
     * @see     com.cordys.tools.ant.isv.ISVContentHandler#getISVContentXML(com.cordys.tools.ant.isv.ISVCreatorTask,
     *          com.cordys.tools.ant.isv.Content, int, int, int)
     */
    public int[] getISVContentXML(ISVCreatorTask isvTask, Content contentTask, int inputXML,
                                  int iCurrentIsvContentNode, int iCurrentIsvPromptsetNode)
    {
        if (!CONTENT_ROOT_TAG.equals(Node.getName(inputXML)))
        {
            GeneralUtils.handleException(ROOT_TAG_ERROR);
        }

        // Create the loader XML.
        Document dDoc = Node.getDocument(inputXML);
        int iReturn = dDoc.createElement("xreports");
        Node.setAttribute(iReturn, "loader", "com.cordys.xreport.isv.XReportsISVLoader");
        Node.setAttribute(iReturn, "description", "XReports");

        // Now we need to read the publish.xml files, since those are the ones that need to be put
        // in the the ISV.
        int[] aiReports = Find.match(inputXML, "fChild<xreport>");

        for (int iReportRef : aiReports)
        {
            // Get the reference to the file.
            String sFilePath = Node.getAttribute(iReportRef, "file");

            if ((sFilePath == null) || (sFilePath.length() == 0))
            {
                GeneralUtils.handleException("Missing file name: " +
                                             Node.writeToString(iReportRef, false));
            }
            
            //Resolve the property.
            sFilePath = isvTask.getProject().replaceProperties(sFilePath);

            File fReportMetadata = new File(sFilePath);
            File fPublishXML = new File(fReportMetadata.getParentFile(), "publish.xml");

            if (!fPublishXML.exists())
            {
                GeneralUtils.handleException("Missing publish.xml: " +
                                             fPublishXML.getAbsolutePath());
            }

            // Load the publish.xml and append it to the return tag.
            try
            {
                int iReport = dDoc.load(fPublishXML.getAbsolutePath());
                Node.appendToChildren(iReport, iReturn);

                isvTask.log("[" + contentTask.getType() + "] Added report " +
                            fPublishXML.getParentFile().getName() + " to the ISV.",
                            Project.MSG_VERBOSE);
            }
            catch (XMLException e)
            {
                GeneralUtils.handleException("Error loading publish.xml: " +
                                             fPublishXML.getAbsolutePath(), e, isvTask);
            }
        }

        return new int[] { iReturn };
    }
}
