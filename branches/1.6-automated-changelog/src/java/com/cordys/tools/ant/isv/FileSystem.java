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

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

/**
 * Filesystem content - A ISV Package Content Type which defines what file
 * system objects should be handled by the ISV Package creator. Typical format
 * in ISV XML:<pre><xmp><filesystem 
 *                 loader="com.eibus.contentmanagement.ISVFileSystemManager" 
 *                 description="File System Objects">
 *                 <file source="" destination="" dir="" />                :
 * </filesystem></xmp></pre>Example of contents configured in content file:
 * <pre><xmp>Root tag of the content xml is <i>filesystem</i>.
 * Every content is enclosed within the <i>file<i> tag.
 * The required attributes for the <i>file<i> tag.
 *                 source="The directory/file which has to be copied."
 *                 destdir="The relative path of the directory specified in ecxdir"
 *                 ecxdir="The directory on the ECX machine."
 *                 <filesystem>                  <file 
 *                                 source="${docs.dir}" 
 *                                 destdir="/${project.name}/docs"
 *                                 ecxdir="cordys_install_dir" />
 *                 </filesystem></xmp></pre>
 *
 * @author msreejit
 */
public class FileSystem
    implements ISVContentHandler
{
    /**
     * The xml document that is used to handle xml functions.
     */
    private static Document document;
    /**
     * The class name of the content loader.
     */
    private static final String CONTENT_LOADER = "com.eibus.contentmanagement.ISVFileSystemManager";
    /**
     * The description of the content.
     */
    private static final String CONTENT_DESCRIPTION = "File System Objects";
    /**
     * The root tag of the content type.
     */
    private static final String ISV_CONTENT_ROOT_TAG = "filesystem";
    /**
     * Message logged when the root tag of the content type is
     * incorrect
     */
    private String NO_ROOT_TAG = "No content root tag 'filesystems' in the content file.";
    /**
     * Holds the tag used to find the files that should be packaged.
     */
    private String m_sFileTag;
    /**
     * Holds the local roottag.
     */
    private String m_sLocalRootTag;
    /**
     * The root node of the content
     */
    private int contentRootNode;

/**
     * Default Constructor
     */
    public FileSystem()
    {
        super();
        m_sLocalRootTag = ISV_CONTENT_ROOT_TAG;
        m_sFileTag = "file";
    }

/**
     * Creates a new FileSystem object.
     *
     * @param sLocalRootTag The roottag of the content XML.
     * @param sFileTag DOCUMENTME
     */
    public FileSystem(String sLocalRootTag, String sFileTag)
    {
        m_sLocalRootTag = sLocalRootTag;
        m_sFileTag = sFileTag;
        NO_ROOT_TAG = "No content root tag '" + sLocalRootTag +
                      "' in content file system.";
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
        int[] fileNodes = getFileNodes(inputXML);

        if (fileNodes.length == 0)
        {
            isvTask.log("[" + contentTask.getType() + "] No content found.",
                        Project.MSG_INFO);
            return null;
        }

        document = isvTask.getDocument();
        contentRootNode = document.createElement(ISV_CONTENT_ROOT_TAG);
        Node.setAttribute(contentRootNode, "loader", CONTENT_LOADER);
        Node.setAttribute(contentRootNode, "description", CONTENT_DESCRIPTION);

        String source;
        String destination;
        String dir;

        //ID-102: We should support if, unless properties with file node
        //Requirement from Studio team
        for (int i = 0; i < fileNodes.length; i++)
        {
            String ifProperty = Node.getAttribute(fileNodes[i], "if", null);
            String unlessProperty = Node.getAttribute(fileNodes[i], "unless",
                                                      null);

            source = Node.getAttribute(fileNodes[i], "source");

            if ((source == null) || (source.length() == 0))
            {
                throw new IllegalArgumentException("Source file/directory is missing or empty.");
            }

            source = contentTask.getProject().replaceProperties(source);
            source = contentTask.getProject().resolveFile(source)
                                .getAbsolutePath();

            //If unless property is specified & the property is set, 
            //we should not add that filenode to isv xml
            if ((unlessProperty != null) &&
                    (GeneralUtils.getTrimmedProperty(contentTask.getProject(), unlessProperty) != null))
            {
                contentTask.log("Source '" + source +
                                "' skipped because property unless=\"" +
                                unlessProperty + "\" was set.", Project.MSG_INFO);
                continue;
            }

            //If property is set, then we should have the property defined
            //to add filenode to isv xml
            if ((ifProperty != null) &&
                    (GeneralUtils.getTrimmedProperty(contentTask.getProject(), ifProperty) == null))
            {
                contentTask.log("Source '" + source +
                                "' skipped because property if=\"" +
                                ifProperty + "\" was set.", Project.MSG_INFO);
                continue;
            }

            // Check that source file/directory exists. This is needed because
            // BCP does not report missing file's name.
            File fSource = new File(source);

            if (!fSource.exists())
            {
                String sMsg = "Source file or directory '" + fSource +
                              "' not found.";

                contentTask.log(sMsg, Project.MSG_ERR);
                throw new IllegalArgumentException(sMsg);
            }

            if (!fSource.canRead())
            {
                String sMsg = "Source file or directory '" + fSource +
                              "' cannot be read.";
                contentTask.log(sMsg, Project.MSG_ERR);
                throw new IllegalArgumentException(sMsg);
            }

            if (fSource.isDirectory())
            {
                // Try to copy the files from the source directory to the build folder
                // in order to filter out any Subversion and other non-wanted files.
                if (contentTask.getFileset() != null)
                {
                    String sNewSource = copyFilesToBuildFolder(contentTask,
                                                               contentTask.getFileset(),
                                                               fSource);

                    if (sNewSource != null)
                    {
                        // Copy succeeded, so change the source folder to point to the build folder.
                        source = sNewSource;
                        fSource = new File(source);
                    }
                }
                
                if (! checkFolderForFiles(fSource))
                {
                    // Just warn the user and skip the directory instead of failing.
                    contentTask.log("Source directory '" + fSource +
                                    "' does not contain any files. Skipping it.", Project.MSG_WARN);
                    continue;
                }                
            }

            int fileNode = document.createElement("file", contentRootNode);

            destination = Node.getAttribute(fileNodes[i], "destdir");
            destination = contentTask.getProject().replaceProperties(destination);

            isvTask.log("[" + contentTask.getType() + "] Adding file " +
                        fSource.getName() + " to the ISV package.",
                        Project.MSG_INFO);

            if ((destination == null) || (destination.length() == 0))
            {
                throw new IllegalArgumentException("Destination file/directory is missing or empty.");
            }

            dir = Node.getAttribute(fileNodes[i], "ecxdir");

            Node.setAttribute(fileNode, "source", source);
            Node.setAttribute(fileNode, "destination", destination);
            Node.setAttribute(fileNode, "dir", dir);
        }

        return new int[] { contentRootNode };
    }
    
    /**
     * Checks if the folder or any of its subfolders contain files.
     * @param folder Folder to be checked.
     * @return <code>true</code> if files were found.
     */
    private boolean checkFolderForFiles(File folder)
    {
        File[] entries = folder.listFiles();
        
        for (File entry : entries)
        {
            if (entry.isFile()) {
                return true;
            } else {
                if (checkFolderForFiles(entry)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Tries to copy the files from the source folder to the build
     * folder. This is needed to be able to filter out e.g. subversion files.
     *
     * @param cmtTask Content task.
     * @param fsSrcFileSet Filter fileset set for the copy operation.
     * @param fSrcFolder Source folder where the files are copied from.
     *
     * @return Build folder where the files where copied to or
     *         <code>null</code> if the operation failed.
     */
    private String copyFilesToBuildFolder(Content cmtTask,
                                          FileSet fsSrcFileSet, File fSrcFolder)
    {
        String sBuildRoot = GeneralUtils.getTrimmedProperty(cmtTask.getProject(), "build.dir");

        if ((sBuildRoot == null) || (sBuildRoot.length() == 0))
        {
            cmtTask.log("Build directory property is not set. Using the source folder as-is.",
                        Project.MSG_WARN);
            return null;
        }

        if (sBuildRoot.startsWith("./") || sBuildRoot.startsWith(".\\"))
        {
            sBuildRoot = ((sBuildRoot.length() > 2) ? sBuildRoot.substring(2) : "");
        }

        File fBuildDir = new File(sBuildRoot);

        if (!fBuildDir.exists() || !fBuildDir.canWrite())
        {
            cmtTask.log("Build directory does not exist or is not writable. Using the source folder as-is.",
                        Project.MSG_WARN);
            return null;
        }

        // Check that the source folder is not already in the build folder.
        try
        {
            if (fSrcFolder.getCanonicalPath()
                              .startsWith(fBuildDir.getCanonicalPath()))
            {
                return null;
            }
        }
        catch (IOException e)
        {
            return null;
        }

        // Set build dir to absolute path (otherwise the path in the ISV package won't work).
        fBuildDir = fBuildDir.getAbsoluteFile();

        File fFileSystemBuildDir = new File(fBuildDir, "filesystem");

        fFileSystemBuildDir.mkdir();

        if (!fFileSystemBuildDir.exists())
        {
            cmtTask.log("Unable to create the filesystem build directory. Using the source folder as-is.",
                        Project.MSG_WARN);
            return null;
        }

        File fDestDir = null;

        for (int i = 0; i <= 100; i++)
        {
            File fTmp = new File(fFileSystemBuildDir,
                                 fSrcFolder.getName() +
                                 ((i == 0) ? "" : String.valueOf(i)));

            if (!fTmp.exists())
            {
                fTmp.mkdir();

                if (fTmp.exists())
                {
                    fDestDir = fTmp;
                    break;
                }
            }
        }

        if (!fDestDir.exists())
        {
            cmtTask.log("Unable to create the filesystem destination directory. Using the source folder as-is.",
                        Project.MSG_WARN);
            return null;
        }

        fsSrcFileSet.setDir(fSrcFolder);

        Copy cCopy = new Copy();

        cCopy.setProject(cmtTask.getProject());
        cCopy.setTodir(fDestDir);
        cCopy.setTaskName(cmtTask.getTaskName());
        cCopy.addFileset(fsSrcFileSet);
        cCopy.execute();

        return fDestDir.getPath();
    }

    /**
     * Checks the root tag and matches the tags with the content tags.
     *
     * @param rootNode The XML loaded from the content file.
     *
     * @return The matched content tags.
     */
    private int[] getFileNodes(int rootNode)
    {
        //check for root tag.
        if (!m_sLocalRootTag.equals(Node.getName(rootNode)))
        {
            GeneralUtils.handleException(NO_ROOT_TAG);
        }

        return Find.match(rootNode, "?<" + m_sFileTag + ">");
    }
}
