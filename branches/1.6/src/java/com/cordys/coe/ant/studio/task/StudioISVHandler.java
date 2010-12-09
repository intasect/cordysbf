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
package com.cordys.coe.ant.studio.task;

import com.cordys.coe.ant.studio.util.FlowMerge;
import com.cordys.coe.exception.GeneralException;

import com.cordys.tools.ant.isv.Content;
import com.cordys.tools.ant.isv.FileSystem;
import com.cordys.tools.ant.isv.ISVCreatorTask;
import com.cordys.tools.ant.util.GeneralUtils;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import java.io.File;

import org.apache.tools.ant.Project;

/**
 * Ant content handler for putting Studio contents to an ISV file. The vcmdata
 * files are merged into one file and that is loaded by the ISV loaded to a
 * file system. This vcmdata file must be imported manually after the ISV
 * package is loaded.
 *
 * @author mpoyhone
 */
public class StudioISVHandler extends FileSystem
{
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
        // Get the vcmdata files from the input file.
        int[] iaFileNodes = getStudioFileNodes(inputXML);

        // If we don't have any content, do not even create the ISV element.
        if ((iaFileNodes == null) || (iaFileNodes.length == 0))
        {
            return null;
        }

        // Get the resulting vcmdata file name.
        String sDestFileName = Node.getAttribute(inputXML, "buildfile");
        String sISVDestFileName = Node.getAttribute(inputXML, "destdir");
        String sISVECXDir = Node.getAttribute(inputXML, "ecxdir");

        if (sDestFileName == null)
        {
            GeneralUtils.handleException("Studio ISV build field is not set.");
        }
        sDestFileName = contentTask.getProject().replaceProperties(sDestFileName);
        sDestFileName = contentTask.getProject().resolveFile(sDestFileName)
                                   .getAbsolutePath();

        if (sISVDestFileName == null)
        {
            GeneralUtils.handleException("Studio ISV destdir name is not set.");
        }

        if (sISVECXDir == null)
        {
            GeneralUtils.handleException("Studio ISV ecxdir name is not set.");
        }

        Document dDoc = isvTask.getDocument();
        isvTask.log("[" + contentTask.getType() + "] Merging flows into " +
                    sDestFileName, Project.MSG_INFO);

        FlowMerge fwMerger = new FlowMerge(dDoc);

        // Load all vcmdata files and merge them to one file.
        for (int i = 0; i < iaFileNodes.length; i++)
        {
            int iNode = iaFileNodes[i];
            String sFileName;

            // Resolve the absolute file name
            sFileName = Node.getAttribute(iNode, "file");
            sFileName = contentTask.getProject().replaceProperties(sFileName);
            sFileName = contentTask.getProject().resolveFile(sFileName)
                                   .getAbsolutePath();

            File fFile = new File(sFileName);
            isvTask.log("[" + contentTask.getType() + "] Adding flow " +
                        fFile.getName() + " to the ISV", Project.MSG_INFO);

            // Check if it is a valid file.
            if (!fFile.exists())
            {
                GeneralUtils.handleException("Studio content file " + fFile +
                                             " does not exist.");
            }

            try
            {
                // Load the contents.
                fwMerger.addFile(fFile);
            }
            catch (GeneralException e)
            {
                GeneralUtils.handleException("Unable to add vcmdata file " +
                                             fFile, e, isvTask);
            }
        }

        try
        {
            // Create the resulting vcmdata file.
            fwMerger.createFinalFile(new File(sDestFileName));
        }
        catch (GeneralException e)
        {
            GeneralUtils.handleException("Unable to merge the vcmdata files",
                                         e, isvTask);
        }

        int iFileSystemNode = 0;

        // Create the ISV structure understood by the super class.
        try
        {
            int iFileNode;

            iFileSystemNode = dDoc.createElement("filesystem");
            iFileNode = Node.createElement("file", iFileSystemNode);
            Node.setAttribute(iFileNode, "source", sDestFileName);
            Node.setAttribute(iFileNode, "destdir", sISVDestFileName);
            Node.setAttribute(iFileNode, "ecxdir", sISVECXDir);

            // Create the ISV element by calling super class.
            return super.getISVContentXML(isvTask, contentTask,
                                          iFileSystemNode,
                                          iCurrentIsvContentNode,
                                          iCurrentIsvPromptsetNode);
        }
        finally
        {
            if (iFileSystemNode != 0)
            {
                Node.delete(iFileSystemNode);
            }
        }
    }

    /**
     * Matches the tags with the content tags.
     *
     * @param rootNode The XML loaded from the content file.
     *
     * @return The matched content tags.
     */
    private int[] getStudioFileNodes(int rootNode)
    {
        return Find.match(rootNode, "?<content>");
    }
}
