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
package com.cordys.coe.ant.bf;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

/**
 * This class holds the current version of an agent.
 *
 * @author pgussow
 */
public class Version
{
    /**
     * So that this class can determine whether or not it was
     * generated.
     */
    private static final String GENERATED = "@generated@";
    /**
     * Holds the current major/minor version string.
     */
    private static final String VERSION_MAJOR_MINOR = new String("@majorminor@");
    /**
     * Holds the milestone number.
     */
    private static final String TMP_VERSION_MILESTONE = new String("@milestonenumber@");
    /**
     * Holds the patch number.
     */
    private static final String TMP_REVISION_NUMBER = new String("@revisionnumber@");
    /**
     * Holds the real versionnumber for the milestone.
     */
    private static int VERSION_MILESTONE = 0;
    /**
     * Holds the real versionnumber for the patch.
     */
    private static int REVISION_NUMBER = 0;

    static
    {
        if (!GENERATED.equals("@" + "generated" + "@"))
        {
            try
            {
                VERSION_MILESTONE = Integer.parseInt(TMP_VERSION_MILESTONE);
            }
            catch (Exception e)
            {
                //Ignore it.
            }
            try
            {
                REVISION_NUMBER = Integer.parseInt(TMP_REVISION_NUMBER);
            }
            catch (Exception e)
            {
                //Ignore it.
            }
        }
    }

    /**
     * Holds the date on which it was built.
     */
    private static final String BUILD_DATE = "@builddate@";
    /**
     * Holds the name of the product.
     */
    private static final String PRODUCT_NAME = "@productname@";

    /**
     * This method gets the date on which the build was made.
     *
     * @return The date on which the build was made.
     */
    public static String getBuildDate()
    {
        return BUILD_DATE;
    }

    /**
     * This method returns the full version number of the agent.
     *
     * @return The full version number.
     */
    public static String getFullVersion()
    {
        return VERSION_MAJOR_MINOR + "." + VERSION_MILESTONE;
    }

    /**
     * This method returns the host name on which this code is running.
     *
     * @return The host name.
     */
    public static String getHostName()
                              throws UnknownHostException
    {
        return InetAddress.getLocalHost().getHostName();
    }

    /**
     * This method returns the status of the system. It returns a
     * string that contains all the settings for the current virtual machine.
     *
     * @return A string containing the full system information.
     */
    public static String getSystemInformation()
    {
        StringWriter swWriter = new StringWriter(1024);
        PrintWriter pwOut = new PrintWriter(swWriter);

        Properties pSystemProperties = System.getProperties();

        //Output the OS information
        pwOut.println("General system information");
        pwOut.println("==========================");
        pwOut.println();
        writeProperty("os.name", pSystemProperties, "OS Name", pwOut);
        writeProperty("os.version", pSystemProperties, "OS Version", pwOut);
        writeProperty("os.arch", pSystemProperties, "OS Architecture", pwOut);
        writeProperty("sun.cpu.isalist", pSystemProperties, "CPU", pwOut);
        pwOut.println();

        pwOut.println("User information");
        pwOut.println("================");
        pwOut.println();
        writeProperty("user.name", pSystemProperties, "Username", pwOut);
        writeProperty("user.dir", pSystemProperties, "Working folder", pwOut);
        writeProperty("user.home", pSystemProperties, "Home folder", pwOut);
        writeProperty("user.language", pSystemProperties, "Language", pwOut);
        writeProperty("user.country", pSystemProperties, "Country", pwOut);
        pwOut.println();

        //Output the information about the virtual machine
        pwOut.println("Virtual machine information");
        pwOut.println("===========================");
        pwOut.println();
        writeProperty("java.version", pSystemProperties, "Version", pwOut);
        writeProperty("java.vm.vendor", pSystemProperties, "Vendor", pwOut);
        writeProperty("java.vm.info", pSystemProperties, "Run mode", pwOut);
        writeProperty("java.vm.version", pSystemProperties, "JVM Version", pwOut);

        //Write the different paths.
        writePath("sun.boot.library.path", pSystemProperties, "Boot path", pwOut);
        writePath("java.library.path", pSystemProperties, "JVM specific path",
                  pwOut);
        writePath("sun.boot.class.path", pSystemProperties, "Boot classpath",
                  pwOut);
        writePath("java.class.path", pSystemProperties,
                  "JVM specific classpath", pwOut);
        pwOut.println();

        //Output all properties.
        pwOut.println("All properties (sorted by name)");
        pwOut.println("===============================");
        pwOut.println();

        TreeSet<Object> tsTemp = new TreeSet<Object>(pSystemProperties.keySet());

        for (Iterator<Object> iKeys = tsTemp.iterator(); iKeys.hasNext();)
        {
            String sKey = (String) iKeys.next();
            String sValue = pSystemProperties.getProperty(sKey);
            pwOut.print(sKey);
            pwOut.print(" = ");
            pwOut.println(sValue);
        }

        return swWriter.getBuffer().toString();
    }

    /**
     * This method gets more details about the version.
     *
     * @return The details for this verison.
     */
    public static String getVersionInfo()
    {
        return getVersionInfo(false);
    }

    /**
     * This method gets more details about the version.
     *
     * @param bIncludeSystemInfo DOCUMENTME
     *
     * @return The details for this verison.
     */
    public static String getVersionInfo(boolean bIncludeSystemInfo)
    {
        StringBuffer sbReturn = new StringBuffer(1024);

        //Product name
        sbReturn.append(PRODUCT_NAME + " version " + getFullVersion());
        sbReturn.append(System.getProperty("line.separator"));

        //Build date
        sbReturn.append("Build date: " + BUILD_DATE);
        sbReturn.append(System.getProperty("line.separator"));

        if (REVISION_NUMBER != 0)
        {
            sbReturn.append("Revision number: " + REVISION_NUMBER);
            sbReturn.append(System.getProperty("line.separator"));
        }

        if (bIncludeSystemInfo == true)
        {
            sbReturn.append(System.getProperty("line.separator"));
            sbReturn.append(getSystemInformation());
        }

        return sbReturn.toString();
    }

    /**
     * Main method.
     *
     * @param saArgs The commandline arguments.
     */
    public static void main(String[] saArgs)
    {
        if (saArgs.length > 0)
        {
            System.out.println(getVersionInfo(true));
        }
        else
        {
            System.out.println(getVersionInfo(false));
        }
    }

    /**
     * This method returns a path nicely formatted.
     *
     * @param sPropName The name of the system property.
     * @param pProperties The properties collection.
     * @param sCaption The caption for this path.
     * @param pwOut The printwriter to output it.
     */
    private static void writePath(String sPropName, Properties pProperties,
                                  String sCaption, PrintWriter pwOut)
    {
        if (pProperties.containsKey(sPropName))
        {
            String sValue = pProperties.getProperty(sPropName);

            if ((sValue != null) && (sValue.length() > 0))
            {
                pwOut.println(sCaption);

                //Seperate the entries.
                String[] saEntries = sValue.split(pProperties.getProperty("path.separator"));

                for (int iCount = 0; iCount < saEntries.length; iCount++)
                {
                    StringBuffer sbTemp = new StringBuffer(100);
                    sbTemp.append("\t");

                    if (iCount < 10)
                    {
                        sbTemp.append("0");
                    }
                    sbTemp.append(String.valueOf(iCount));
                    sbTemp.append(": ");
                    sbTemp.append(saEntries[iCount]);
                    pwOut.println(sbTemp);
                }
            }
        }
    }

    /**
     * This method writes the property to the given output writer.
     *
     * @param sPropName The name of the property.
     * @param pProperties The propertiescollection.
     * @param sCaption The caption.
     * @param pwOut The output writer.
     */
    private static void writeProperty(String sPropName, Properties pProperties,
                                      String sCaption, PrintWriter pwOut)
    {
        if (pProperties.containsKey(sPropName))
        {
            String sValue = pProperties.getProperty(sPropName);

            if ((sValue != null) && (sValue.length() > 0))
            {
                pwOut.println(sCaption + " = " + sValue);
            }
        }
    }
}
