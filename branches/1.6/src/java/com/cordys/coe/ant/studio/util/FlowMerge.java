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
package com.cordys.coe.ant.studio.util;

import com.cordys.coe.exception.GeneralException;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.xml.Message;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * A class that takes a list of vcmdata files and merges them to one file. This currently works only
 * with exported business processed.
 *
 * @author  mpoyhone
 */
public class FlowMerge
{
    /**
     * Name of the export summary file in the vcmdata zip file.
     */
    public static final String FILE_EXPORTCONFIGURATION = "exportconfiguration.xml";
    /**
     * Name of the bpmview_casType in the vcmdata zip file.
     */
    public static final String FILE_BPMVIEW_CASTYPE = "casrepositorycontent\\bpmview\\bpmview_casType.xml";
    /**
     * Name of the bpmview directory in the vcmdata zip file.
     */
    public static final String FILE_BPMVIEW_DIR = "casrepositorycontent\\bpmview\\";
    /**
     * Name of the file type file in the vcmdata zip file.
     */
    public static final String FILE_FILETYPE = "filetypes\\filetypes.xml";
    /**
     * Name of the versions file in the vcmdata zip file.
     */
    public static final String FILE_VERSIONS = "versions\\versions.xml";
    /**
     * The document used to create XML elements.
     */
    protected Document dDoc;
    /**
     * All export summary elements are colledted here as ExportConfiguration objects.
     */
    List<ExportConfiguration> lExportConfigurationList = new LinkedList<ExportConfiguration>();
    /**
     * List of all processed vcmdata files. This is needed to extract bpm file later.
     */
    List<File> lZipFileList = new LinkedList<File>();
    /**
     * A map of file in bpmview dir.
     */
    Map<String, Message> mBpmviewMap = new HashMap<String, Message>();
    /**
     * A map of tuples in bpmview_casType.xml file.
     */
    Map<String, Message> mCasTypeMap = new HashMap<String, Message>();
    /**
     * A map of tuples in filetypes.xml file.
     */
    Map<String, Message> mFileTypeMap = new HashMap<String, Message>();
    /**
     * A map of tuples in versions.xml file.
     */
    Map<String, Message> mVersionMap = new HashMap<String, Message>();

    /**
     * Creates a new FlowMerge object.
     *
     * @param  dDoc  XML document to be used.
     */
    public FlowMerge(Document dDoc)
    {
        this.dDoc = dDoc;
    }

    /**
     * Adds a vcmdata file to be merged.
     *
     * @param   fFile  The file to be merged.
     *
     * @throws  GeneralException  Thrown if the operation failed.
     */
    public void addFile(File fFile)
                 throws GeneralException
    {
        ZipFile zfFile = null;

        // Read all the elements from the file.
        try
        {
            zfFile = new ZipFile(fFile);

            // This is for old format where the models were in a single file.
            loadObjects(zfFile, FILE_BPMVIEW_CASTYPE, mCasTypeMap, false);

            // This is for the new format with multiple files.
            loadFilesFromDir(zfFile, FILE_BPMVIEW_DIR, mBpmviewMap, false,
                             new String[] { FILE_BPMVIEW_CASTYPE });

            loadFileTypes(zfFile, mFileTypeMap);
            loadVersions(zfFile, mVersionMap);

            ExportConfiguration ecConfig = new ExportConfiguration();

            ecConfig.loadFromFile(dDoc, zfFile);
            lExportConfigurationList.add(ecConfig);
        }
        catch (ZipException e)
        {
            throw new GeneralException(e, "Unable to open the vcmdata file " + fFile);
        }
        catch (IOException e)
        {
            throw new GeneralException(e, "Unable to open the vcmdata file " + fFile);
        }
        finally
        {
            if (zfFile != null)
            {
                try
                {
                    zfFile.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }

        lZipFileList.add(fFile);
    }

    /**
     * Creates the merged output file.
     *
     * @param   fDestFile  The output file.
     *
     * @throws  GeneralException  Thrown if the operation failed.
     */
    public void createFinalFile(File fDestFile)
                         throws GeneralException
    {
        ZipOutputStream zosOutput = null;

        if ((fDestFile != null) && !fDestFile.getParentFile().exists())
        {
            fDestFile.mkdirs();
        }

        try
        {
            zosOutput = new ZipOutputStream(new FileOutputStream(fDestFile));

            // Write old version bpmview file if we there is content.
            if (mCasTypeMap.size() > 0)
            {
                writeObjects(zosOutput, FILE_BPMVIEW_CASTYPE, mCasTypeMap);
            }

            // Write new version bpmview files if we there is content.
            if (mBpmviewMap.size() > 0)
            {
                writeFiles(zosOutput, mBpmviewMap);
            }

            writeObjects(zosOutput, FILE_FILETYPE, mFileTypeMap);
            writeObjects(zosOutput, FILE_VERSIONS, mVersionMap);
            writeExportConfiguration(zosOutput, FILE_EXPORTCONFIGURATION);

            copyBpmFiles(zosOutput);
        }
        catch (FileNotFoundException e)
        {
            throw new GeneralException(e, "Unable to create the destination file " + fDestFile);
        }
        catch (IOException e)
        {
            throw new GeneralException(e, "Unable to write the destination file " + fDestFile);
        }
        finally
        {
            try
            {
                if (zosOutput != null)
                {
                    zosOutput.close();
                }
            }
            catch (IOException ignored)
            {
            }
        }
    }

    /**
     * Copies bpm files from the source vcmdata files to the destination vcmdata file.
     *
     * @param   zosOutput  The destination ZIP output stream
     *
     * @throws  GeneralException  Thrown if the operation failed.
     */
    protected void copyBpmFiles(ZipOutputStream zosOutput)
                         throws GeneralException
    {
        Map<String, String> mSeenFiles = new HashMap<String, String>();

        for (Iterator<File> iIter = lZipFileList.iterator(); iIter.hasNext();)
        {
            File fFile = iIter.next();
            ZipFile zfFile = null;

            try
            {
                zfFile = new ZipFile(fFile);

                byte[] baBuffer = new byte[4096];

                for (Enumeration<? extends ZipEntry> e = zfFile.entries(); e.hasMoreElements();)
                {
                    ZipEntry zeEntry = e.nextElement();
                    String sEntryName = zeEntry.getName().toLowerCase();

                    if (!sEntryName.endsWith(".bpm") || mSeenFiles.containsKey(sEntryName))
                    {
                        continue;
                    }

                    mSeenFiles.put(sEntryName, sEntryName);

                    ZipEntry zeOutEntry = new ZipEntry(zeEntry.getName());

                    zosOutput.putNextEntry(zeOutEntry);

                    InputStream isInput = null;
                    int iRead;

                    try
                    {
                        isInput = zfFile.getInputStream(zeEntry);

                        while ((iRead = isInput.read(baBuffer)) != -1)
                        {
                            zosOutput.write(baBuffer, 0, iRead);
                        }
                    }
                    finally
                    {
                        if (isInput != null)
                        {
                            isInput.close();
                        }
                    }

                    zosOutput.closeEntry();
                }
            }
            catch (Exception e)
            {
                throw new GeneralException(e, "Unable to copy the bpm file.");
            }
            finally
            {
                try
                {
                    zfFile.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }
    }

    /**
     * Loads files from a directory inside the vcmdata file.
     *
     * @param   zfFile          The source ZIP file.
     * @param   sDirName        Source directory name in the ZIP file.
     * @param   mDestMap        The map where the files are stored.
     * @param   bMustExists     If true an exception if thrown if the file did not exist.
     * @param   saIgnoredFiles  An optional list of files to be ignored
     *
     * @throws  GeneralException  Thrown if the operation failed.
     */
    protected void loadFilesFromDir(ZipFile zfFile, String sDirName, Map<String, Message> mDestMap,
                                    boolean bMustExists, String[] saIgnoredFiles)
                             throws GeneralException
    {
        boolean bFound = false;

        sDirName = sDirName.replace('\\', '/');

        toploop:
        for (Enumeration<? extends ZipEntry> eEnum = zfFile.entries(); eEnum.hasMoreElements();)
        {
            ZipEntry zeEntry = eEnum.nextElement();
            String sFileName = zeEntry.getName();

            sFileName = sFileName.replace('\\', '/');

            if (!sFileName.startsWith(sDirName))
            {
                continue;
            }

            if (saIgnoredFiles != null)
            {
                for (int i = 0; i < saIgnoredFiles.length; i++)
                {
                    String sIgnoreFileName = saIgnoredFiles[i];

                    sIgnoreFileName = sIgnoreFileName.replace('\\', '/');

                    if (sFileName.equals(sIgnoreFileName))
                    {
                        continue toploop;
                    }
                }
            }

            if (!zeEntry.isDirectory())
            {
                Message mFileXml = null;

                try
                {
                    String sFileContents = FileUtils.readTextZipEntryContents(zfFile, zeEntry);

                    mFileXml = new Message(dDoc, sFileContents);
                }
                catch (Exception e)
                {
                    throw new GeneralException(e, "Unable to extract file " + zeEntry.getName());
                }

                if (mDestMap.containsKey(sFileName))
                {
                    Message mOldFile = mDestMap.get(sFileName);
                    long lOldTime = Long.parseLong(mOldFile.getValue("./@lastModified", "0"));
                    long lNewTime = Long.parseLong(mFileXml.getValue("./@lastModified", "0"));

                    if (lOldTime >= lNewTime)
                    {
                        continue;
                    }
                }

                mDestMap.put(sFileName, mFileXml);

                bFound = true;
            }
        }

        if (bMustExists && !bFound)
        {
            throw new GeneralException("No files in directory " + sDirName +
                                       " found in vcmdata file.");
        }
    }

    /**
     * Loads the file contents.
     *
     * @param   zfFile    The input vcm data file.
     * @param   mDestMap  The map where these objects are stored.
     *
     * @throws  GeneralException  Thrown if the operation failed.
     */
    protected void loadFileTypes(ZipFile zfFile, Map<String, Message> mDestMap)
                          throws GeneralException
    {
        loadObjects(zfFile, FILE_FILETYPE, mDestMap, false);
    }

    /**
     * Loads tuples from a file inside the vcmdata file.
     *
     * @param   zfFile       The source ZIP file.
     * @param   sEntryName   Name of the source file
     * @param   mDestMap     The map where the objects are stored.
     * @param   bMustExists  If true an exception if thrown if the file did not exist.
     *
     * @throws  GeneralException  Thrown if the operation failed.
     */
    protected void loadObjects(ZipFile zfFile, String sEntryName, Map<String, Message> mDestMap,
                               boolean bMustExists)
                        throws GeneralException
    {
        sEntryName = sEntryName.replace('\\', '/');

        ZipEntry zeEntry = zfFile.getEntry(sEntryName);

        if (zeEntry == null)
        {
            if (!bMustExists)
            {
                return;
            }
            throw new GeneralException("File " + sEntryName + " not found in vcmdata file.");
        }

        loadObjects(zfFile, zeEntry, mDestMap, bMustExists);
    }

    /**
     * Loads tuples from a file inside the vcmdata file.
     *
     * @param   zfFile       The source ZIP file.
     * @param   zeEntry      Source file ZIP entry
     * @param   mDestMap     The map where the objects are stored.
     * @param   bMustExists  If true an exception if thrown if the file did not exist.
     *
     * @throws  GeneralException  Thrown if the operation failed.
     */
    protected void loadObjects(ZipFile zfFile, ZipEntry zeEntry, Map<String, Message> mDestMap,
                               boolean bMustExists)
                        throws GeneralException
    {
        Message mFileXml = null;

        try
        {
            String sFileContents = FileUtils.readTextZipEntryContents(zfFile, zeEntry);

            mFileXml = new Message(dDoc, sFileContents);
        }
        catch (Exception e)
        {
            throw new GeneralException(e, "Unable to extract file " + zeEntry.getName());
        }

        for (Iterator<?> iIter = mFileXml.selectAll("./tuple"); iIter.hasNext();)
        {
            Message mTuple = (Message) iIter.next();
            String sKey = mTuple.getValue("./@key", "");

            if (sKey.length() == 0)
            {
                continue;
            }

            // System.out.println(mTuple.getValue("./@description"));
            if (mDestMap.containsKey(sKey))
            {
                Message mOldTuple = mDestMap.get(sKey);
                long lOldTime = Long.parseLong(mOldTuple.getValue("./@lastModified", "0"));
                long lNewTime = Long.parseLong(mTuple.getValue("./@lastModified", "0"));

                if (lOldTime >= lNewTime)
                {
                    // System.out.println("SKIP");
                    continue;
                }
            }

            mDestMap.put(sKey, mTuple);
        }
    }

    /**
     * Loads the version tuples from the vcmdata file.
     *
     * @param   zfFile    The vcmdata ZIP file.
     * @param   mDestMap  The map where the tuples are stored.
     *
     * @throws  GeneralException  Thrown if the operation failed.
     */
    protected void loadVersions(ZipFile zfFile, Map<String, Message> mDestMap)
                         throws GeneralException
    {
        loadObjects(zfFile, FILE_VERSIONS, mDestMap, false);
    }

    /**
     * Writes the merged export summary file.
     *
     * @param   zosOutput  The ZIP output stream that the file is written to.
     * @param   sFileName  The ZIP entry name of this file.
     *
     * @throws  GeneralException  Thrown if the operation failed.
     */
    protected void writeExportConfiguration(ZipOutputStream zosOutput, String sFileName)
                                     throws GeneralException
    {
        Message mContents = new Message(dDoc,
                                        "<Export xmlns='http://schemas.cordys.com/1.0/cas-vcm'>" +
                                        "	<information>" +
                                        "		<exportfilename>exporttest</exportfilename>" +
                                        "		<filetypes key=''/>" +
                                        "		<versions key=''/>" +
                                        "		<modelrepositorycontents exportSubprocesses='true' exportUsedContent='true' exportSubDocuments='true'>" +
                                        "			<models>" + "			</models>" +
                                        "			<projects/>" + "			<workspaces/>" +
                                        "		</modelrepositorycontents>" +
                                        "		<casrepositorycontents>" +
                                        "			<vcmview/>" + "			<bcmview/>" +
                                        "			<bpmview>" + "				<Cordys/>" +
                                        "			</bpmview>" +
                                        "		</casrepositorycontents>" +
                                        "	</information>" + "	<metadata>" +
                                        "		<createdBy></createdBy>" +
                                        "		<creationDate></creationDate>" +
                                        "		<system></system>" +
                                        "		<organization></organization>" +
                                        "		<softwareVersion></softwareVersion>" +
                                        "		<description></description>" +
                                        "	</metadata>" + "</Export>");

        ExportConfiguration ecMergedConfig;

        ecMergedConfig = ExportConfiguration.createMergedObject(lExportConfigurationList);

        for (Iterator<Message> iIter = ecMergedConfig.lCasrepositorycontents.iterator();
                 iIter.hasNext();)
        {
            Message mModel = iIter.next();

            mContents.append("//casrepositorycontents/bpmview/Cordys", mModel);
        }

        for (Iterator<Message> iIter = ecMergedConfig.lModelrepositorycontents.iterator();
                 iIter.hasNext();)
        {
            Message mModel = iIter.next();

            mContents.append("//modelrepositorycontents/models", mModel);
        }

        for (Iterator<Message> iIter = ecMergedConfig.lFiletypes.iterator(); iIter.hasNext();)
        {
            Message mModel = iIter.next();

            mContents.append("//filetypes", mModel);
        }

        for (Iterator<Message> iIter = ecMergedConfig.lVersions.iterator(); iIter.hasNext();)
        {
            Message mModel = iIter.next();

            mContents.append("//versions", mModel);
        }

        try
        {
            ZipEntry zeOutEntry = new ZipEntry(sFileName);

            zosOutput.putNextEntry(zeOutEntry);

            zosOutput.write(Node.writeToString(mContents.getXmlNode(), true).getBytes());

            zosOutput.closeEntry();
        }
        catch (Exception e)
        {
            throw new GeneralException(e, "Unable to write zip entry " + sFileName);
        }
    }

    /**
     * Writes files to the output vcmdata file.
     *
     * @param   zosOutput  The ZIP output stream that the file is written to.
     * @param   mSrcMap    Source map that contains all the merged tuples.
     *
     * @throws  IOException       Thrown if the operation failed.
     * @throws  GeneralException  Thrown if the operation failed.
     */
    protected void writeFiles(ZipOutputStream zosOutput, Map<String, Message> mSrcMap)
                       throws IOException, GeneralException
    {
        for (Iterator<String> iIter = mSrcMap.keySet().iterator(); iIter.hasNext();)
        {
            String sFileName = iIter.next();
            Message mFileXml = mSrcMap.get(sFileName);
            ZipEntry zeEntry;

            zeEntry = new ZipEntry(sFileName);
            zosOutput.putNextEntry(zeEntry);

            zosOutput.write(Node.write(mFileXml.getXmlNode(), true));
            zosOutput.closeEntry();
        }
    }

    /**
     * Writes merged tuple objects to the output vcmdata file.
     *
     * @param   zosOutput   The ZIP output stream that the file is written to.
     * @param   sEntryName  The ZIP entry name of this file.
     * @param   mSrcMap     Source map that contains all the merged tuples.
     *
     * @throws  IOException       Thrown if the operation failed.
     * @throws  GeneralException  Thrown if the operation failed.
     */
    protected void writeObjects(ZipOutputStream zosOutput, String sEntryName,
                                Map<String, Message> mSrcMap)
                         throws IOException, GeneralException
    {
        ZipEntry zeEntry;

        zeEntry = new ZipEntry(sEntryName);
        zosOutput.putNextEntry(zeEntry);

        Message mDest = new Message(dDoc, "<GetCollection/>");

        for (Iterator<Message> iIter = mSrcMap.values().iterator(); iIter.hasNext();)
        {
            Message mTuple = iIter.next();

            mDest.append(".", mTuple);
        }

        zosOutput.write(Node.write(mDest.getXmlNode(), true));
        zosOutput.closeEntry();

        mDest.clear();
    }

    /**
     * DOCUMENTME.
     *
     * @author  $author$
     */
    protected static class ExportConfiguration
    {
        /**
         * Contains XML data from 'casrepositorycontents' bpmview section.
         */
        public List<Message> lCasrepositorycontents = new LinkedList<Message>();
        /**
         * Contains XML data from 'filetypes' bpmview section.
         */
        public List<Message> lFiletypes = new LinkedList<Message>();
        /**
         * Contains XML data from 'modelrepositorycontents' models section.
         */
        public List<Message> lModelrepositorycontents = new LinkedList<Message>();
        /**
         * Contains XML data from 'versions' bpmview section.
         */
        public List<Message> lVersions = new LinkedList<Message>();

        /**
         * Creates a new configuration object that contain all information contained in the objects
         * in the input list.
         *
         * @param   lConfigList  List containing all ExportConfiguration objects to be merged
         *
         * @return  The merged object.
         */
        @SuppressWarnings("unchecked")
        public static ExportConfiguration createMergedObject(List<ExportConfiguration> lConfigList)
        {
            List<Message>[] laCasrepositorycontentsLists = new List[lConfigList.size()];
            List<Message>[] laModelrepositorycontentsLists = new List[lConfigList.size()];
            List<Message>[] laFiletypesLists = new List[lConfigList.size()];
            List<Message>[] laVersionsLists = new List[lConfigList.size()];

            int i = 0;

            for (Iterator<ExportConfiguration> iter = lConfigList.iterator(); iter.hasNext(); i++)
            {
                ExportConfiguration ecConfig = iter.next();

                laCasrepositorycontentsLists[i] = ecConfig.lCasrepositorycontents;
                laModelrepositorycontentsLists[i] = ecConfig.lModelrepositorycontents;
                laFiletypesLists[i] = ecConfig.lFiletypes;
                laVersionsLists[i] = ecConfig.lVersions;
            }

            ExportConfiguration ecResConfig = new ExportConfiguration();

            mergeEntries(laCasrepositorycontentsLists, ecResConfig.lCasrepositorycontents);
            mergeEntries(laModelrepositorycontentsLists, ecResConfig.lModelrepositorycontents);
            mergeEntries(laFiletypesLists, ecResConfig.lFiletypes);
            mergeEntries(laVersionsLists, ecResConfig.lVersions);

            return ecResConfig;
        }

        /**
         * Loads contents from a file.
         *
         * @param   dDoc    Document used for XML nodes.
         * @param   zfFile  Source file
         *
         * @throws  GeneralException  Thrown if the operation failed.
         */
        public void loadFromFile(Document dDoc, ZipFile zfFile)
                          throws GeneralException
        {
            ZipEntry zeEntry = zfFile.getEntry(FILE_EXPORTCONFIGURATION);

            if (zeEntry == null)
            {
                throw new GeneralException("File " + FILE_EXPORTCONFIGURATION +
                                           " not found in vcmdata file.");
            }

            Message mFileXml = null;

            try
            {
                String sFileContents = FileUtils.readTextZipEntryContents(zfFile, zeEntry);

                mFileXml = new Message(dDoc, sFileContents);
            }
            catch (Exception e)
            {
                throw new GeneralException(e, "Unable to extract file " + FILE_EXPORTCONFIGURATION);
            }

            for (Iterator<?> iIter = mFileXml.selectAll("//modelrepositorycontents/models/model");
                     iIter.hasNext();)
            {
                Message mModel = (Message) iIter.next();
                String sKey = mModel.getValue("./@key", "");

                if (sKey.length() == 0)
                {
                    continue;
                }

                lModelrepositorycontents.add(mModel);
            }

            for (Iterator<?> iIter = mFileXml.selectAll("//casrepositorycontents/bpmview/Cordys/cas");
                     iIter.hasNext();)
            {
                Message mModel = (Message) iIter.next();
                String sKey = mModel.getValue("./@key", "");

                if (sKey.length() == 0)
                {
                    continue;
                }

                lCasrepositorycontents.add(mModel);
            }

            for (Iterator<?> iIter = mFileXml.selectAll("//filetypes/filetype"); iIter.hasNext();)
            {
                Message mModel = (Message) iIter.next();
                String sKey = mModel.getValue("./@key", "");

                if (sKey.length() == 0)
                {
                    continue;
                }

                lFiletypes.add(mModel);
            }

            for (Iterator<?> iIter = mFileXml.selectAll("//versions/version"); iIter.hasNext();)
            {
                Message mModel = (Message) iIter.next();
                String sKey = mModel.getValue("./@key", "");

                if (sKey.length() == 0)
                {
                    continue;
                }

                lVersions.add(mModel);
            }
        }

        /**
         * Merges all entries in the input lists to output list. Objects are <code>Message</code>
         * objects that have a <code>key</code> attribute.
         *
         * @param  laInputLists  Input list containing<code>Message</code> objects.
         * @param  lOutputList   Unique collection of input elements.
         */
        protected static void mergeEntries(List<Message>[] laInputLists, List<Message> lOutputList)
        {
            HashMap<String, String> hmSeenEntries = new HashMap<String, String>();

            for (int i = 0; i < laInputLists.length; i++)
            {
                List<Message> lInput = laInputLists[i];

                for (Iterator<Message> iIter = lInput.iterator(); iIter.hasNext();)
                {
                    Message mEntry = iIter.next();
                    String sKey = mEntry.getValue("./@key", "");

                    if ("".equals(sKey))
                    {
                        continue;
                    }

                    if (hmSeenEntries.containsKey(sKey))
                    {
                        continue;
                    }

                    lOutputList.add(mEntry);
                    hmSeenEntries.put(sKey, sKey);
                }
            }
        }
    }
}
