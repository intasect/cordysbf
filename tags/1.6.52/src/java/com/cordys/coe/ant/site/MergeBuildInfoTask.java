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
package com.cordys.coe.ant.site;

import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.VersionNumberComparator;
import com.cordys.coe.util.exceptions.XMLWrapperException;
import com.cordys.coe.util.xml.dom.CoENiceDOMWriter;
import com.cordys.coe.util.xml.dom.XMLHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Creates build-info.xml file for a project site.
 *
 * @author mpoyhone
 */
public class MergeBuildInfoTask extends Task
{
    /**
     * Contains the destination build-info-list.xml file.
     */
    private File fDestfile;
    /**
     * Contains file sets for source build-info.xml files.
     */
    private List<FileSet> lSourceFilesSets = new ArrayList<FileSet>(10);

    /**
     * Adds a new source file set.
     *
     * @param fsSet File set.
     */
    public void addFileSet(FileSet fsSet)
    {
        lSourceFilesSets.add(fsSet);
    }

    /**
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute()
                 throws BuildException
    {
        Map<String, Node> mFileMap = new TreeMap<String, Node>(new VersionNumberComparator(false));
        long lLatestTimestamp = 0;
        Node nLatestReleaseNode = null;

        for (FileSet fsSourceFiles : lSourceFilesSets)
        {
            File fDir = fsSourceFiles.getDir(getProject());

            if ((fDir == null) || !fDir.exists() || !fDir.isDirectory())
            {
                continue;
            }

            DirectoryScanner dsScanner = fsSourceFiles.getDirectoryScanner(getProject());
            String[] saFiles;

            dsScanner.scan();
            saFiles = dsScanner.getIncludedFiles();

            for (int i = 0; i < saFiles.length; i++)
            {
                String sSource = saFiles[i];
                File fSourcePath = new File(dsScanner.getBasedir(), sSource);
                Node nCurrentNode;

                try
                {
                    Document dDoc = XMLHelper.loadXMLFile(fSourcePath.getAbsolutePath());
                    nCurrentNode = dDoc.getDocumentElement();
                }
                catch (XMLWrapperException e)
                {
                    throw new BuildException("Unable to load build info file: " +
                                             fSourcePath, e);
                }

                mFileMap.put(getBuildVersion(nCurrentNode, fSourcePath),
                             nCurrentNode);

                String sTimestamp = getAttribute(nCurrentNode, "timestamp");

                if (sTimestamp != null)
                {
                    long lTimestamp = Long.parseLong(sTimestamp);

                    if ((nLatestReleaseNode == null) ||
                            (lLatestTimestamp < lTimestamp))
                    {
                        nLatestReleaseNode = nCurrentNode;
                        lLatestTimestamp = lTimestamp;
                    }
                }
            }
        }

        OutputStream osOutput = null;

        try
        {
            osOutput = new FileOutputStream(fDestfile);

            osOutput.write("<buildinfolist".getBytes());

            if (nLatestReleaseNode != null)
            {
                String sValue;

                if ((sValue = getAttribute(nLatestReleaseNode, "version")) != null)
                {
                    osOutput.write(" latestversion=\"".getBytes());
                    osOutput.write(sValue.getBytes());
                    osOutput.write("\"".getBytes());
                }

                if ((sValue = getAttribute(nLatestReleaseNode, "timestamp")) != null)
                {
                    osOutput.write(" latesttimestamp=\"".getBytes());
                    osOutput.write(sValue.getBytes());
                    osOutput.write("\"".getBytes());
                }

                if ((sValue = getAttribute(nLatestReleaseNode, "date")) != null)
                {
                    osOutput.write(" latestdate=\"".getBytes());
                    osOutput.write(sValue.getBytes());
                    osOutput.write("\"".getBytes());
                }
            }

            osOutput.write(">\n".getBytes());

            for (Node nNode : mFileMap.values())
            {
                new CoENiceDOMWriter(nNode, osOutput, 1, 4).flush();
            }
            osOutput.write("\n</buildinfolist>\n".getBytes());
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to write file: " + fDestfile, e);
        }
        finally
        {
            FileUtils.closeStream(osOutput);
        }
    }

    /**
     * @param nNode
     * @param nName
     *
     * @return
     */
    public static String getAttribute(Node nNode, String nName)
    {
        return getAttribute(nNode, nName, null);
    }

    /**
     * @param nNode
     * @param nName
     * @param nDefault
     *
     * @return
     */
    public static String getAttribute(Node nNode, String nName, String nDefault)
    {
        NamedNodeMap nnmAttribs = nNode.getAttributes();

        if (nnmAttribs == null)
        {
            return null;
        }

        Node nAttrib = nnmAttribs.getNamedItem(nName);

        return (nAttrib != null) ? nAttrib.getNodeValue() : nDefault;
    }

    /**
     * The destfile to set.
     *
     * @param aDestfile The destfile to set.
     */
    public void setDestfile(File aDestfile)
    {
        fDestfile = aDestfile;
    }

    /**
     * Returns the build version from the buildinfo XML.
     *
     * @param nInfoNode Buildinfo XML root node.
     * @param fFile File where the XML was loaded from.
     *
     * @return Build version.
     */
    private String getBuildVersion(Node nInfoNode, File fFile)
    {
        NamedNodeMap nnmAttribs = nInfoNode.getAttributes();
        Node nAttrib = (nnmAttribs != null)
                       ? nnmAttribs.getNamedItem("version") : null;
        String sVersion = (nAttrib != null) ? nAttrib.getTextContent() : null;

        if ((sVersion == null) || (sVersion.length() == 0))
        {
            throw new BuildException("Build version not found from file: " +
                                     fFile);
        }

        return sVersion;
    }
}
