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
package com.cordys.coe.ant.isvloadtask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.tools.ant.Task;

import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.log.AntTaskLogger;
import com.cordys.coe.util.xml.Message;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.eibus.util.Base64;

/**
 * This class wraps around the isvfile-task which is nested withing the
 * isvload-task.
 *
 * @author pgussow
 */
public class IsvFile extends Task
{
    /**
     * Holds the wrapper around the ant logger.
     */
    private AntTaskLogger atlLog;
    /**
     * Source directory for load.
     */
    private File fLoadSrcDir = null;
    /**
     * Holds the current SOAP connection object.
     */
    private ISoapRequestManager srmSoap = null;
    /**
     * Holds the name parameter
     */
    private String sName = "";

    /**
     * Default constructor.
     */
    public IsvFile()
    {
        super();
        atlLog = new AntTaskLogger(this);
    }

    /**
     * This method sets the file name. It can contain wildcards.
     *
     * @param sName File name
     */
    public void setName(String sName)
    {
        sName = sName.replaceAll("\\*", ".*");
        sName = sName.replaceAll("\\?", ".");

        this.sName = sName;
    }

    /**
     * Sets the load source dir.
     *
     * @param sDir Source directory for ISV packages.
     */
    public void setSrcDir(String sDir)
    {
        fLoadSrcDir = new File(sDir);
    }

    /**
     * Unloads the ISV package.
     *
     * @param srmSoap The SOAP connection used.
     */
    public void executeLoad(ISoapRequestManager srmSoap)
                     throws Exception
    {
        this.srmSoap = srmSoap;

        if (fLoadSrcDir == null)
        {
            throw new Exception("Source directory not set.");
        }

        if (!fLoadSrcDir.exists())
        {
            throw new Exception("Source directory does not exist.");
        }

        File fFile = null;
        Message mFileDesc = null;
        File[] faSrcFiles = fLoadSrcDir.listFiles();

        for (int i = 0; i < faSrcFiles.length; i++)
        {
            File fSrcFile = faSrcFiles[i];
            Message mSrcFileDesc = getFileDesc(fSrcFile);

            if (mSrcFileDesc == null)
            {
                continue;
            }

            String sPackageName = mSrcFileDesc.getValue("./cn");

            if (sPackageName.matches(sName))
            {
                fFile = fSrcFile;
                mFileDesc = mSrcFileDesc;
                break;
            }
        }

        if (fFile == null)
        {
            atlLog.error("No packages found to be loaded.");
            return;
        }

        Message[] maPackages = getInstalledIsvPackages();

        // First try to unload the package.
        for (int i = 0; i < maPackages.length; i++)
        {
            Message mPkgEntry = maPackages[i];
            String sPkgName = mPkgEntry.getValue("./cn/string");
            String sFileName = mPkgEntry.getValue("./member/string")
                                        .replaceFirst("cn=(.*).isvp", "$1");

            if (sPkgName.matches(sName))
            {
                unloadPackage(false, sFileName);
            }
        }
        
        URL uFileUrl;

        // Upload the new ISV
        try
        {
            uFileUrl = uploadPackage(fFile);
        }
        catch (IOException e)
        {
            throw new Exception("Error while uploading the ISV file to the server.",
                                e);
        }        

        // Load the new ISV
        loadPackage(uFileUrl, mFileDesc);
    }

    /**
     * Unloads the ISV package.
     *
     * @param scSoap The SOAP connection used.
     * @param bDeleteReferences If true, references to roles and method sets are deleted.
     */
    public void executeUnload(ISoapRequestManager srmSoap, boolean bDeleteReferences)
                       throws Exception
    {
        this.srmSoap = srmSoap;

        Message[] maPackages = getInstalledIsvPackages();
        boolean bUnloaded = false;

        for (int i = 0; i < maPackages.length; i++)
        {
            Message mPkgEntry = maPackages[i];
            String sPkgName = mPkgEntry.getValue("./cn/string");
            String sFileName = mPkgEntry.getValue("./member/string")
                                        .replaceFirst("cn=(.*).isvp", "$1");

            if (sPkgName.matches(sName))
            {
                unloadPackage(bDeleteReferences, sFileName);
                bUnloaded = true;
            }
        }

        if (!bUnloaded)
        {
            atlLog.error("No ISV packages found to be unloaded.");
        }
    }

    /**
     * Returns ISV file description from the ISV file XML.
     *
     * @param fFile The ISV file to be read.
     *
     * @return Description XML structure.
     */
    protected Message getFileDesc(File fFile)
    {
        ZipFile zfFile = null;

        try
        {
            ZipEntry zeIsvEntry;

            zfFile = new ZipFile(fFile);
            zeIsvEntry = zfFile.getEntry("isv.xml");

            if (zeIsvEntry == null)
            {
                return null;
            }

            String sEntryData = FileUtils.readTextZipEntryContents(zfFile,
                                                                   zeIsvEntry);

            Message mFileContents = srmSoap.getCurrentNomCollector().getMessageContext().createMessage(sEntryData);

            return mFileContents.select("./description");
        }
        catch (Exception ignored)
        {
            return null;
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
    }

    /**
     * Returns a list of installed ISV packages.
     *
     * @return A list of package XML descriptions.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected Message[] getInstalledIsvPackages()
                                         throws Exception
    {
        String sDN = srmSoap.getOrganizationDN().replaceFirst(".*(cn=[^,]*,o=.*)$",
                                                             "$1");
        String sXml =
            "<GetSoftwarePackages xmlns='http://schemas.cordys.com/1.0/ldap'>" +
            "  <dn>" + sDN + "</dn>" + "  <sort>ascending</sort>" +
            "</GetSoftwarePackages>";
        Message mRequest = srmSoap.getCurrentNomCollector().getMessageContext().createMessage(sXml);
        Message mResponse;

        mResponse = srmSoap.makeSoapRequest(mRequest);

        List<Message> lPackages = new LinkedList<Message>();

        for (Iterator<?> iIter = mResponse.selectAll("./tuple/old/entry");
                 iIter.hasNext();)
        {
            Message mEntry = (Message) iIter.next();

            lPackages.add(mEntry);
        }

        return lPackages.toArray(new Message[lPackages.size()]);
    }

    /**
     * Loads the ISV file.
     *
     * @param uFileUrl ISV file URL at BCP web directory.
     * @param mFileDesc File's description XML strcuture.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected void loadPackage(URL uFileUrl, Message mFileDesc)
                        throws Exception
    {
        atlLog.info("Loading ISV package " + uFileUrl.getFile());

        String sXml =
            "<LoadISVPackage xmlns='http://schemas.cordys.com/1.0/isvpackage'>" +
                        "  <url>" + uFileUrl + "</url>" + "  <ISVPackage>" +
                        mFileDesc + "    <content/>" + "    <promptset/>" +
                        "  </ISVPackage>" + "</LoadISVPackage>";

        Message mRequest = srmSoap.getCurrentNomCollector().getMessageContext().createMessage(sXml);
        Message mResponse;

        mResponse = srmSoap.makeSoapRequest(mRequest);

        String sStatus = mResponse.getValue("./status");

        atlLog.info(sStatus);
    }

    /**
     * Unloads an ISV package.
     *
     * @param bDeleteReferences If true, references to roles and method sets are deleted.
     * @param sName ISV package name.
     *
     * @return True, if the operation succeeded.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected boolean unloadPackage(boolean bDeleteReferences, String sName)
                             throws Exception
    {
        atlLog.info("Unloading ISV package " + sName);

        String sXml =
                        "<UnloadISVPackage xmlns='http://schemas.cordys.com/1.0/isvpackage'>" +
                        "	<file deletereference='" + bDeleteReferences + "'>" +
                        sName + "</file>" + "</UnloadISVPackage>";

        Message mRequest = srmSoap.getCurrentNomCollector().getMessageContext().createMessage(sXml);
        Message mResponse;

        mResponse = srmSoap.makeSoapRequest(mRequest);

        String sStatus = mResponse.getValue("./status");

        if (sStatus == null)
        {
            throw new Exception("Unload method returned no status.");
        }

        atlLog.info(sStatus);

        return true;
    }

    /**
     * Uploads an ISV package to the BCP web directory.
     *
     * @param fIsvFile The ISV file to be uploaded.
     *
     * @return The URL where the file was uploaded.
     *
     * @throws Exception Thrown if the operation failed.
     */
    protected URL uploadPackage(File fIsvFile)
                         throws Exception
    {
        StringBuffer sbBase64Content = new StringBuffer((int) (2 * fIsvFile.length()));
        byte[] baBuffer = new byte[4096];
        InputStream isInput = null;

        atlLog.info("Uploading file " + fIsvFile.getName());

        try
        {
            int iRead;

            isInput = new FileInputStream(fIsvFile);

            while ((iRead = isInput.read(baBuffer)) > 0)
            {
                if (iRead == baBuffer.length)
                {
                    sbBase64Content.append(Base64.encode(baBuffer));
                }
                else
                {
                    byte[] baTmp = new byte[iRead];

                    System.arraycopy(baBuffer, 0, baTmp, 0, iRead);
                    sbBase64Content.append(Base64.encode(baTmp));
                }
            }
        }
        finally
        {
            FileUtils.closeStream(isInput);
        }

        String sXml =
                        "<UploadISVPackage xmlns='http://schemas.cordys.com/1.0/isvpackage'>" +
                        "  <name>" + fIsvFile.getName() + "</name>" +
                        "  <content></content>" + "</UploadISVPackage>";
        Message mRequest = srmSoap.getCurrentNomCollector().getMessageContext().createMessage(sXml);
        Message mResponse;

        mRequest.setValue("./content", sbBase64Content.toString());

        mResponse = srmSoap.makeSoapRequest(mRequest);

        String sUrl = mResponse.getValue("./url");

        atlLog.info("Upload successful. File URL is " + sUrl);

        return new URL(sUrl);
    }
}
