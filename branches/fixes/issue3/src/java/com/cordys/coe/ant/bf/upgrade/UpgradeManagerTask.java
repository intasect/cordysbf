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
package com.cordys.coe.ant.bf.upgrade;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.cordys.coe.ant.bf.Version;
import com.cordys.coe.ant.bf.properties.BuildFrameworkProperty;
import com.cordys.coe.ant.bf.upgrade.bf150.UpgradeTo1_5_0;
import com.cordys.coe.ant.bf.upgrade.bf150.UpgradeTo1_5_12;
import com.cordys.coe.ant.bf.upgrade.bf150.UpgradeTo1_5_14;
import com.cordys.coe.ant.bf.upgrade.bf150.UpgradeTo1_5_16;
import com.cordys.coe.ant.bf.upgrade.bf150.UpgradeTo1_5_18;
import com.cordys.coe.ant.bf.upgrade.bf160.UpgradeTo1_6_0;
import com.cordys.coe.ant.bf.upgrade.bf160.UpgradeTo1_6_13;
import com.cordys.coe.ant.bf.upgrade.bf160.UpgradeTo1_6_14;
import com.cordys.coe.ant.bf.upgrade.bf160.UpgradeTo1_6_25;
import com.cordys.coe.ant.bf.upgrade.bf160.UpgradeTo1_6_28;
import com.cordys.coe.ant.bf.upgrade.bf160.UpgradeTo1_6_6;
import com.cordys.coe.ant.bf.upgrade.bf160.UpgradeTo1_6_7;
import com.cordys.coe.ant.bf.upgrade.bf160.UpgradeTo1_6_9;
import com.cordys.coe.util.VersionNumberComparator;

/**
 * This class manages the upgrading of the project. It will scan the
 * current JarFile for scripts that implement the IUpgradeScript interface and
 * create the sequence of steps that should be executed to upgrade the project
 * to the current version. <br>
 * The upgrade scripts should all be under the package
 * com.cordys.coe.ant.bf.upgrade.
 *
 * @author pgussow
 */
public class UpgradeManagerTask extends Task
{
    /**
     * Holds the name of the property in the BF-version file.
     */
    public static final String PROP_BF_VERSION = "bf.version";
    /**
     * A table of hex digits
     */
    private static final char[] hexDigit = 
                                           {
                                               '0', '1', '2', '3', '4', '5', '6',
                                               '7', '8', '9', 'A', 'B', 'C', 'D',
                                               'E', 'F'
                                           };
    /**
     * This arraylist holds all the upgrade scripts.
     */
    private ArrayList<IUpgradeScript> m_alAllScripts = new ArrayList<IUpgradeScript>();
    /**
     * This arraylist holds the sequence of the scripts that should be
     * executed.
     */
    private ArrayList<IUpgradeScript> m_alSequence = new ArrayList<IUpgradeScript>();
    /**
     * Used for comparing two version numbers. Handles e.g. 1.0.10 vs.
     * 1.0.9 correctly (string comparator would put 1.0.10 before 1.0.9).
     */
    private VersionNumberComparator vncVersionComparator = new VersionNumberComparator();

    /**
     * Creates a new UpgradeManager object.
     */
    public void execute()
                 throws BuildException
    {
        log("Starting the upgrade manager. First register all scripts.",
            Project.MSG_DEBUG);
        registerScript();

        log("Going to determine the sequence of upgrades.");
        determineSequence();

        //Now we can actually execute the scripts.
        try
        {
            executeScripts();
        }
        catch (BFUpgradeException e)
        {
            throw new BuildException("Error upgrading project.", e);
        }

        //It could be that for the latest version there was no update script. 
        //So in that case we need to write our version number.
        log("Project has been updated to version " + Version.getFullVersion(),
            Project.MSG_INFO);
        writeVersion(Version.getFullVersion());
    }

    /**
     * This method determines the sequence of steps that should be
     * executed based on the current version.
     */
    private void determineSequence()
                            throws BuildException
    {
        try
        {
            String sCurrentVersion = getCurrentVersion();
            log("Current version of the project is " + sCurrentVersion);

            for (Iterator<?> iUpgradeScripts = m_alAllScripts.iterator();
                     iUpgradeScripts.hasNext();)
            {
                IUpgradeScript usScript = (IUpgradeScript) iUpgradeScripts.next();

                if (vncVersionComparator.compare(usScript.getVersion(),
                                                     sCurrentVersion) > 0)
                {
                    log("This script should be executed: " +
                        usScript.getClass().getSimpleName() + ": Version " +
                        usScript.getVersion(), Project.MSG_DEBUG);
                    m_alSequence.add(usScript);
                }
                else
                {
                    log("This script will be skipped: " +
                        usScript.getClass().getSimpleName() + ": Version " +
                        usScript.getVersion(), Project.MSG_DEBUG);
                }
            }
        }
        catch (Exception e)
        {
            throw new BuildException("Error determining the current version of the project.");
        }
    }

    /**
     * This method will execute the scripts.
     */
    private void executeScripts()
                         throws BFUpgradeException
    {
        for (Iterator<IUpgradeScript> iScripts = m_alSequence.iterator(); iScripts.hasNext();)
        {
            IUpgradeScript usScript = iScripts.next();
            log("Execute upgrade to version " + usScript.getVersion(), Project.MSG_INFO);

            usScript.execute(getProject(), this);
            writeVersion(usScript.getVersion());
        }
    }

    /**
     * This method gets the current build framework version of the
     * project.
     *
     * @return The current build framework version of the project.
     */
    private String getCurrentVersion()
                              throws FileNotFoundException, IOException
    {
        String sReturn = "0.0.0";

        String sFile = getProject()
                           .getProperty(BuildFrameworkProperty.LIB_BF_VERSION_FILE.getName());
        File fFile = new File(getProject().getBaseDir(), sFile);

        if (!fFile.exists())
        {
            //If the file does not exist we assume version 0.0.0
        }
        else
        {
            Properties pProp = new Properties();
            pProp.load(new FileInputStream(fFile));
            sReturn = pProp.getProperty(PROP_BF_VERSION);
        }
        return sReturn;
    }

    /**
     * This method adds all the available upgrade scripts with this
     * class. This means that if you make a new upgrade script you should
     * register it within this method. Important: Make sure that the scripts
     * are added in the proper sequence. The task does not do any sorting!
     */
    private void registerScript()
    {
        m_alAllScripts.add(new UpgradeTo1_5_0());
        m_alAllScripts.add(new UpgradeTo1_5_12());
        m_alAllScripts.add(new UpgradeTo1_5_14());
        m_alAllScripts.add(new UpgradeTo1_5_16());
        m_alAllScripts.add(new UpgradeTo1_5_18());
        m_alAllScripts.add(new UpgradeTo1_6_0());
        m_alAllScripts.add(new UpgradeTo1_6_6());
        m_alAllScripts.add(new UpgradeTo1_6_7());
        m_alAllScripts.add(new UpgradeTo1_6_9());
        m_alAllScripts.add(new UpgradeTo1_6_13());
        m_alAllScripts.add(new UpgradeTo1_6_14());
        m_alAllScripts.add(new UpgradeTo1_6_25());
        m_alAllScripts.add(new UpgradeTo1_6_28());
    }

    /**
     * Converts unicodes to encoded &#92;uxxxx and escapes special
     * characters with a preceding slash<br>
     * This was copied from the Properties class.
     *
     * @param theString DOCUMENTME
     * @param escapeSpace DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private String saveConvert(String theString, boolean escapeSpace)
    {
        int len = theString.length();
        int bufLen = len * 2;

        if (bufLen < 0)
        {
            bufLen = Integer.MAX_VALUE;
        }

        StringBuffer outBuffer = new StringBuffer(bufLen);

        for (int x = 0; x < len; x++)
        {
            char aChar = theString.charAt(x);

            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127))
            {
                if (aChar == '\\')
                {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }

            switch (aChar)
            {
                case ' ':

                    if ((x == 0) || escapeSpace)
                    {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(' ');
                    break;

                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;

                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;

                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;

                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;

                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\');
                    outBuffer.append(aChar);
                    break;

                default:

                    if ((aChar < 0x0020) || (aChar > 0x007e))
                    {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex(aChar & 0xF));
                    }
                    else
                    {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Convert a nibble to a hex character
     *
     * @param nibble the nibble to convert.
     *
     * @return DOCUMENTME
     */
    private static char toHex(int nibble)
    {
        return hexDigit[(nibble & 0xF)];
    }

    /**
     * This method writes the given version to the propertyfile.
     *
     * @param sVersion The new version.
     *
     * @throws BuildException In case the writing fails.
     */
    private void writeVersion(String sVersion)
                       throws BuildException
    {
        //Now write the propertyfile.
        Properties pProp = new Properties();
        pProp.setProperty(PROP_BF_VERSION, sVersion);

        try
        {
            File fVersionFile = new File(getProject().getBaseDir(),
                                         getProject()
                                             .getProperty(BuildFrameworkProperty.LIB_BF_VERSION_FILE.getName()));

            if (!fVersionFile.exists())
            {
                // Create the parent folders, if needed.
                File fParent = fVersionFile.getParentFile();

                if (!fParent.exists() && !fParent.mkdirs())
                {
                    throw new BuildException("Unable to create version file parent folder: " +
                                             fParent);
                }
            }

            //Because the standard Properties.store writes a comment in the file
            //we have to do the storing ourselves.
            FileOutputStream fos = null;

            try
            {
                fos = new FileOutputStream(fVersionFile, false);

                BufferedWriter bwWriter;
                bwWriter = new BufferedWriter(new OutputStreamWriter(fos,
                                                                    "8859_1"));

                for (Enumeration<?> e = pProp.keys(); e.hasMoreElements();)
                {
                    String sKey = (String) e.nextElement();
                    String sValue = (String) pProp.get(sKey);
                    sKey = saveConvert(sKey, true);

                    //No need to escape embedded and trailing spaces for value, hence
                    //pass false to flag.
                    sValue = saveConvert(sValue, false);
                    bwWriter.write(sKey + "=" + sValue);
                    bwWriter.newLine();
                }
                bwWriter.flush();
            }
            finally
            {
                fos.close();
            }
        }
        catch (Exception e)
        {
            throw new BuildException("Error updating the property file containing the current version of the build framework",
                                     e);
        }
    }
}
