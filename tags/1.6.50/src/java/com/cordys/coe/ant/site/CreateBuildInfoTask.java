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

import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.w3c.dom.Node;

/**
 * Creates build-info.xml file for a project site.
 *
 * @author mpoyhone
 */
public class CreateBuildInfoTask extends Task
{
    /**
     * Contains dist folders to be searched.
     */
    private List<DistFolder> lDistSubfolders = new ArrayList<DistFolder>(10);
    /**
     * Contains file type mappers.
     */
    private List<FileMapper> lMappers = new ArrayList<FileMapper>(10);
    /**
     * Build date.
     */
    private String sBuildDate;
    /**
     * Build version.
     */
    private String sBuildVersion;
    /**
     * Builds folder URL in the target website.
     */
    private String sBuildurl;
    /**
     * Dist directory.
     */
    private String sDistDir;
    /**
     * Project name.
     */
    private String sProjectName;
    /**
     * Project version.
     */
    private String sProjectVersion;
    /**
     * Target website URL.
     */
    private String sSiteurl;

    /**
     * Adds a new dist folder to be scanned.
     *
     * @return Dist folder.
     */
    public DistFolder createDistfolder()
    {
        DistFolder dfFolder = new DistFolder();

        lDistSubfolders.add(dfFolder);
        return dfFolder;
    }

    /**
     * Adds a new type mapper.
     *
     * @return Type mapper.
     */
    public FileMapper createMapper()
    {
        FileMapper fm = new FileMapper();

        lMappers.add(fm);
        return fm;
    }

    /**
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute()
                 throws BuildException
    {
        Map<String, File> mFileMap = new TreeMap<String, File>();

        for (DistFolder dfFolder : lDistSubfolders)
        {
            File fDir = new File(sDistDir, dfFolder.sFolder);

            if (fDir.exists())
            {
                File[] faFiles = fDir.listFiles();

                for (File fFile : faFiles)
                {
                    log("Found file: " + fFile, Project.MSG_DEBUG);
                    
                    mFileMap.put(fFile.getName(), fFile);
                }
            }
        }

        Node nRoot = XMLHelper.createElement("buildinfo", null);

        XMLHelper.createAttribute("version", sBuildVersion, nRoot);
        XMLHelper.createAttribute("date", sBuildDate, nRoot);

        XMLHelper.createTextElement("projectversion", sProjectVersion, nRoot);
        XMLHelper.createTextElement("projectname", sProjectName, nRoot);
        XMLHelper.createTextElement("projecturl", sSiteurl, nRoot);

        Node nFiles = XMLHelper.createElement("files", nRoot);

        for (File fFile : mFileMap.values())
        {
            Node nFile = XMLHelper.createElement("file", nFiles);
            String sPath = fFile.toString().replace('\\', '/')
                                .replaceFirst("^(?:\\./)?[^/]+/(.*)", "$1");
            String sSize = fFile.isFile() ? Long.toString(fFile.length()) : "";
            String sType = getFileType(fFile, sPath);
            boolean bNeedLinkTarget = false;
            
            if (fFile.isDirectory()) {
                sPath = sPath + "/index.html";
                bNeedLinkTarget = true;
            }
            
            log("File: " + fFile + ", type: " + sType, Project.MSG_DEBUG);

            XMLHelper.createAttribute("name", fFile.getName(), nFile);
            XMLHelper.createAttribute("type", sType, nFile);
            XMLHelper.createAttribute("url", sBuildurl + "/" + sPath, nFile);
            XMLHelper.createAttribute("size", sSize, nFile);
            XMLHelper.createAttribute("targetframe", bNeedLinkTarget ? "_blank" : "", nFile);
            XMLHelper.createAttribute("isfolder",
                                      fFile.isDirectory() ? "true" : "false",
                                      nFile);
        }

        File fBuildInfo = new File(sDistDir, "build-info.xml");
        OutputStream osOutput = null;

        try
        {
            osOutput = new FileOutputStream(fBuildInfo);
            new CoENiceDOMWriter(nRoot, osOutput, 0, 4).flush();
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to write file: " + fBuildInfo, e);
        }
        finally
        {
            FileUtils.closeStream(osOutput);
        }
    }

    /**
     * The buildDate to set.
     *
     * @param aBuildDate The buildDate to set.
     */
    public void setBuildDate(String aBuildDate)
    {
        sBuildDate = aBuildDate;
    }

    /**
     * The buildVersion to set.
     *
     * @param aBuildVersion The buildVersion to set.
     */
    public void setBuildVersion(String aBuildVersion)
    {
        sBuildVersion = aBuildVersion;
    }

    /**
     * The buildurl to set.
     *
     * @param aBuildurl The buildurl to set.
     */
    public void setBuildurl(String aBuildurl)
    {
        sBuildurl = aBuildurl;
    }

    /**
     * The distDir to set.
     *
     * @param aDistDir The distDir to set.
     */
    public void setDistdir(String aDistDir)
    {
        sDistDir = aDistDir;
    }

    /**
     * The projectName to set.
     *
     * @param aProjectName The projectName to set.
     */
    public void setProjectName(String aProjectName)
    {
        sProjectName = aProjectName;
    }

    /**
     * The projectVersion to set.
     *
     * @param aProjectVersion The projectVersion to set.
     */
    public void setProjectVersion(String aProjectVersion)
    {
        sProjectVersion = aProjectVersion;
    }

    /**
     * The siteurl to set.
     *
     * @param aSiteurl The siteurl to set.
     */
    public void setSiteurl(String aSiteurl)
    {
        sSiteurl = aSiteurl;
    }

    /**
     * DOCUMENTME
     *
     * @param fFile DOCUMENTME
     * @param sFilePath DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private String getFileType(File fFile, String sFilePath)
    {
        for (FileMapper fmMapper : lMappers)
        {
            if (fmMapper.bDirectory && !fFile.isDirectory())
            {
                continue;
            }

            if (!fmMapper.bDirectory && fFile.isDirectory())
            {
                continue;
            }
            
            log(fmMapper.pRegex.pattern() + ": " + sFilePath, Project.MSG_VERBOSE);

            if (fmMapper.pRegex.matcher(sFilePath).matches())
            {
                return fmMapper.sType;
            }
        }

        return "unknown";
    }

    /**
     * DOCUMENTME
     *
     * @author $author$
      */
    public class DistFolder
    {
        /**
         * DOCUMENTME
         */
        String sFolder;

        /**
         * The folder to set.
         *
         * @param aFolder The folder to set.
         */
        public void setFolder(String aFolder)
        {
            sFolder = aFolder;
        }
    }

    /**
     * DOCUMENTME
     *
     * @author $author$
      */
    public class FileMapper
    {
        /**
         * DOCUMENTME
         */
        Pattern pRegex;
        /**
         * DOCUMENTME
         */
        String sType;
        /**
         * DOCUMENTME
         */
        boolean bDirectory;

        /**
         * The directory to set.
         *
         * @param aDirectory The directory to set.
         */
        public void setDirectory(boolean aDirectory)
        {
            bDirectory = aDirectory;
        }

        /**
         * The regex to set.
         *
         * @param aRegex The regex to set.
         */
        public void setRegex(String aRegex)
        {
            pRegex = Pattern.compile(aRegex);
        }

        /**
         * The type to set.
         *
         * @param aType The type to set.
         */
        public void setType(String aType)
        {
            sType = aType;
        }
    }
}
