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
package com.cordys.coe.ant.project;

import com.cordys.coe.util.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class is used to parse the arguments passed on to the application.
 * It creates a temporary properties file which can be used to control the
 * parameters needed.
 *
 * @author pgussow
 */
public class NewProjectLauncher
{
    /**
     * Holds the char for the option project location.
     */
    private static final String OPTION_PROJECT_LOCATION = "l";
    /**
     * Holds the char for the option platform location.
     */
    private static final String OPTION_PLATFORM_LOCATION = "p";
    /**
     * Holds the char for the option source property file.
     */
    private static final String OPTION_SOURCE_PROPERTY_FILE = "s";
    /**
     * Holds the char for the option source property file.
     */
    private static final String OPTION_ADDITIONAL_PROPERTIES = "a";
    /**
     * Holds the char for the option svnexternals.
     */
    private static final String OPTION_SVN_EXTERNALS = "svnexternals";
    /**
     * Holds the char for the option svn.
     */
    private static final String OPTION_SVN = "svn";
    /**
     * Holds the string for the sdk location.
     */
    private static final String OPTION_SDK_LOCATION = "sdk";
    /**
     * Holds the string for the usage option.
     */
    private static final String OPTION_USAGE = "h";
    /**
     * Holds the option definition.
     */
    private static Options s_oOptions = null;
    /**
     * Holds the parsed commandline.
     */
    private CommandLine m_clLine;

/**
     * Creates a new NewProjectLauncher object.
     *
     * @param saArguments The arguments from the the command line.
     */
    public NewProjectLauncher(String[] saArguments)
                       throws ParseException
    {
        CommandLineParser clp = new GnuParser();

        try
        {
            m_clLine = clp.parse(getOptions(), saArguments, true);
        }
        catch (MissingOptionException moe)
        {
            StringWriter swTemp = new StringWriter(2048);
            PrintWriter pwTemp = new PrintWriter(swTemp);
            pwTemp.println("Missing options: " + moe.getMessage());
            pwTemp.println();

            HelpFormatter hfFormatter = new HelpFormatter();
            hfFormatter.printUsage(pwTemp, 80, "new-project.bat", getOptions());

            pwTemp.println();
            System.err.println(swTemp.toString());

            hfFormatter.printHelp("new-project.bat", getOptions());

            System.exit(1);
        }
    }

    /**
     * Main method.
     *
     * @param saArguments The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            NewProjectLauncher npl = new NewProjectLauncher(saArguments);
            npl.createPropertyFile();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method creates the actual property file with the needed
     * values.
     */
    public void createPropertyFile()
                            throws FileNotFoundException, IOException
    {
        //Get the SDK folder, because that's the base folder.
        String sSDKFolder = m_clLine.getOptionValue(OPTION_SDK_LOCATION);
        File fSDKFolder = new File(sSDKFolder);

        Properties pProperties = new Properties();

        //In order to keep control over the Commandline length we write all
        //properties to a file in the temp-folder.
        String sFile = m_clLine.getOptionValue(OPTION_ADDITIONAL_PROPERTIES);

        if ((sFile != null) && (sFile.length() > 0))
        {
            //Load it.
            pProperties.load(new FileInputStream(new File(sFile)));
        }

        for (Iterator<?> iOptions = m_clLine.iterator(); iOptions.hasNext();)
        {
            Option oTemp = (Option) iOptions.next();
            StringBuffer sbTemp = new StringBuffer("");
            String[] saValues = oTemp.getValues();

            if (saValues != null)
            {
                for (int iValCount = 0; iValCount < saValues.length;
                         iValCount++)
                {
                    sbTemp.append(saValues[iValCount]);

                    if (iValCount < (saValues.length - 1))
                    {
                        sbTemp.append(", ");
                    }
                }
            }

            if (oTemp.getOpt().equals(OPTION_PROJECT_LOCATION))
            {
                pProperties.setProperty("project.root", sbTemp.toString());
            }
            else if (oTemp.getOpt().equals(OPTION_PLATFORM_LOCATION))
            {
                //Check if it's a relative of absolute path.
                String sPath = sbTemp.toString();

                if (FilenameUtils.isRelative(sPath))
                {
                    //It's relative.
                    File fTemp = new File(fSDKFolder, sPath);
                    sPath = fTemp.getCanonicalPath();
                }
                pProperties.setProperty("src.platform.folder", sPath);
            }
            else if (oTemp.getOpt().equals(OPTION_SDK_LOCATION))
            {
                pProperties.setProperty("src.sdk.folder",
                                        fSDKFolder.getCanonicalPath());
            }
            else if (oTemp.getOpt().equals(OPTION_SVN))
            {
                pProperties.setProperty("create.svn", "true");
            }
            else if (oTemp.getOpt().equals(OPTION_SVN_EXTERNALS))
            {
                pProperties.setProperty("create.svn.externals", "true");
            }
            else if (oTemp.getOpt().equals(OPTION_SOURCE_PROPERTY_FILE))
            {
                //Check if it's a relative of absolute path.
                String sPath = sbTemp.toString();

                if (FilenameUtils.isRelative(sPath))
                {
                    //It's relative.
                    File fTemp = new File(fSDKFolder, sPath);
                    sPath = fTemp.getCanonicalPath();
                }
                pProperties.setProperty("source.property.file", sPath);
            }
        }

        //Now store the file in a temp folder.
        String sBaseFolder = System.getProperty("user.home");
        File fTemp = new File(sBaseFolder);
        File fAdditionalProperties = new File(fTemp, "tmp.additional.properties");
        pProperties.store(new FileOutputStream(fAdditionalProperties, false),
                          "Temp file for creating a new project.");

        try
        {
            //Build up the command
            ArrayList<String> alCommand = new ArrayList<String>();
            alCommand.add("cmd.exe");
            alCommand.add("/c");
            alCommand.add("%JAVA_HOME%\\bin\\java.exe");
            alCommand.add("-cp");
            alCommand.add("%ANT_CP%");
            alCommand.add("org.apache.tools.ant.launch.Launcher");
            alCommand.add("-lib");
            alCommand.add("%LIBS%");
            alCommand.add("-file");
            alCommand.add("%BASE_FOLDER%\\build\\new\\build-new-project.xml");

            alCommand.add("-Dadditional.properties=" +
                          fAdditionalProperties.getCanonicalPath());

            String[] asOthers = m_clLine.getArgs();

            for (int iCount = 0; iCount < asOthers.length; iCount++)
            {
                alCommand.add(asOthers[iCount]);
            }

            String[] saCmdArgs = alCommand.toArray(new String[alCommand.size()]);

            ProcessBuilder pb = new ProcessBuilder(saCmdArgs);

            try
            {
                // Execute the build command.
                Process pBuildProcess = pb.start();

                // Attach the streams.
                StreamPumper ispStdinPumper = new StreamPumper(System.in,
                                                               pBuildProcess.getOutputStream());
                StreamPumper ispStdoutPumper = new StreamPumper(pBuildProcess.getInputStream(),
                                                                System.out);
                StreamPumper ispStderrPumper = new StreamPumper(pBuildProcess.getErrorStream(),
                                                                System.err);

                new Thread(ispStdinPumper).start();
                new Thread(ispStdoutPumper).start();
                new Thread(ispStderrPumper).start();

                // Wait for the build process to end and return its status code.
                System.exit(pBuildProcess.waitFor());

                ispStdinPumper.terminate();
                ispStdoutPumper.terminate();
                ispStderrPumper.terminate();
            }
            catch (Exception e)
            {
                System.err.println("Exception while executing the build command:");
                e.printStackTrace(System.err);
            }
        }
        finally
        {
            if (fAdditionalProperties.exists())
            {
                fAdditionalProperties.delete();
            }
        }
    }

    /**
     * This method creates the options that can be passed on to this
     * program.
     *
     * @return The options that can be passed on to this program.
     */
    @SuppressWarnings("static-access")
    private static Options getOptions()
    {
        if (s_oOptions == null)
        {
            s_oOptions = new Options();

            //svn
            Option oCreateSVN = new Option(OPTION_SVN, false,
                                           "If this flag is set the project will be shared to SubVersion.");
            s_oOptions.addOption(oCreateSVN);

            //svn:externals
            Option oCreateSVNExternals = new Option(OPTION_SVN_EXTERNALS,
                                                    false,
                                                    "If this flag is set the project will be use the SVN externals.");
            s_oOptions.addOption(oCreateSVNExternals);

            //Project location
            Option oProjectLocation = OptionBuilder.withArgName("projectlocation")
                                                   .hasArg()
                                                   .withDescription("Sets the location where the new project should be created.")
                                                   .isRequired()
                                                   .create(OPTION_PROJECT_LOCATION);
            s_oOptions.addOption(oProjectLocation);

            //Platform location
            Option oPlatformLocation = OptionBuilder.withArgName("platformlocation")
                                                    .hasArg()
                                                    .withDescription("Sets the location where the platform files can be found.")
                                                    .isRequired()
                                                    .create(OPTION_PLATFORM_LOCATION);
            s_oOptions.addOption(oPlatformLocation);

            //SDK location
            Option oSDKLocation = OptionBuilder.withArgName("sdklocation")
                                               .hasArg()
                                               .withDescription("Sets the location where the sdk files can be found.")
                                               .isRequired()
                                               .create(OPTION_SDK_LOCATION);
            s_oOptions.addOption(oSDKLocation);

            //Source property file
            Option oSourcePropertyFile = OptionBuilder.withArgName("sourcepropertyfile")
                                                      .hasArg()
                                                      .withDescription("Sets the location where the property file can be found with the project properties that need to be set.")
                                                      .create(OPTION_SOURCE_PROPERTY_FILE);
            s_oOptions.addOption(oSourcePropertyFile);
            //Additional properties file
            Option oAdditionalPropertyFile = OptionBuilder.withArgName("additionalproperties")
                                                      .hasArg()
                                                      .withDescription("Sets the location where the property file can be found with the additional properties for creating the project.")
                                                      .create(OPTION_ADDITIONAL_PROPERTIES);
            s_oOptions.addOption(oAdditionalPropertyFile);

            //Usage
            Option oUsage = new Option(OPTION_USAGE, false,
                                       "Prints this information");
            s_oOptions.addOption(oUsage);
        }

        return s_oOptions;
    }

    /**
     * DOCUMENTME
     *
     * @author $author$
     */
    static class StreamPumper
        implements Runnable
    {
        /**
         * DOCUMENTME
         */
        InputStream isInput;
        /**
         * DOCUMENTME
         */
        OutputStream osOutput;
        /**
         * DOCUMENTME
         */
        Thread tPumperThread;
        /**
         * DOCUMENTME
         */
        boolean bTerminate;

/**
         * Creates a new StreamPumper object.
         *
         * @param isInput DOCUMENTME
         * @param osOutput DOCUMENTME
         */
        StreamPumper(InputStream isInput, OutputStream osOutput)
        {
            this.isInput = isInput;
            this.osOutput = osOutput;
        }

        /**
         * DOCUMENTME
         */
        public void run()
        {
            tPumperThread = Thread.currentThread();

            byte[] baBuffer = new byte[128];

            while (!isTerminateSet())
            {
                try
                {
                    int iAvailable = isInput.available();
                    int iMaxCount = (iAvailable > 0)
                                    ? Math.min(iAvailable, baBuffer.length)
                                    : baBuffer.length;
                    int iRead = isInput.read(baBuffer, 0, iMaxCount);

                    if (iRead <= 0)
                    {
                        break;
                    }

                    osOutput.write(baBuffer, 0, iRead);
                    osOutput.flush();
                }
                catch (IOException e)
                {
                    break;
                }
            }
        }

        /**
         * DOCUMENTME
         *
         * @return DOCUMENTME
         */
        synchronized boolean isTerminateSet()
        {
            return bTerminate;
        }

        /**
         * DOCUMENTME
         */
        void terminate()
        {
            while ((tPumperThread != null) && tPumperThread.isAlive())
            {
                synchronized (this)
                {
                    bTerminate = true;
                    tPumperThread.interrupt();
                }

                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException ignored)
                {
                }
            }
        }
    }
}
