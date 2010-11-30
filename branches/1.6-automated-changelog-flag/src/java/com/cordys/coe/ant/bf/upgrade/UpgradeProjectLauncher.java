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

import java.io.File;
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
 * this class launches Ant for upgrading the project to the proper version
 * of the buildframework.
 *
 * @author pgussow
 */
public class UpgradeProjectLauncher
{
    /**
     * Holds the char for the option project location.
     */
    private static final String OPTION_PROJECT_LOCATION = "l";
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
     * Creates a new UpgradeProjectLauncher object.
     *
     * @param saArguments The commandline arguments.
     */
    public UpgradeProjectLauncher(String[] saArguments)
                           throws ParseException
    {
        CommandLineParser clp = new GnuParser();

        try
        {
            m_clLine = clp.parse(getOptions(), saArguments, true);

            if (m_clLine.hasOption(OPTION_USAGE))
            {
                displayUsage(null);
            }
        }
        catch (MissingOptionException moe)
        {
            String sAdditional = "Missing options: " + moe.getMessage();
            displayUsage(sAdditional);
        }
    }

    /**
     * This method displays the usage and terminates the JVM.
     *
     * @param sAdditional Additional message to display.
     */
    public void displayUsage(String sAdditional)
    {
        StringWriter swTemp = new StringWriter(2048);
        PrintWriter pwTemp = new PrintWriter(swTemp);

        if ((sAdditional != null) && (sAdditional.length() > 0))
        {
            pwTemp.println(sAdditional);
            pwTemp.println();
        }

        HelpFormatter hfFormatter = new HelpFormatter();
        hfFormatter.printUsage(pwTemp, 80, "upgrade.bat", getOptions());

        pwTemp.println();
        System.err.println(swTemp.toString());

        hfFormatter.printHelp("upgrade.bat", getOptions());

        System.exit(1);
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
            UpgradeProjectLauncher upl = new UpgradeProjectLauncher(saArguments);
            upl.upgradeProject();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method upgrades the current project to the new version.
     */
    public void upgradeProject()
                        throws FileNotFoundException, IOException
    {
        //Build up the additional properties file
        Properties pProperties = new Properties();

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
        }

        //Now store the file in a temp folder.
        String sBaseFolder = System.getProperty("user.home");
        File fTemp = new File(sBaseFolder);
        File fAdditionalProperties = new File(fTemp, "tmp.additional.properties");
        pProperties.store(new FileOutputStream(fAdditionalProperties, false),
                          "Temp file for creating a new project.");
        
        String envJavaHome = System.getenv("JAVA_HOME");
        String envAntClasspath = System.getenv("ANT_CP");
        String envLibs = System.getenv("LIBS");
        String envBaseFolder = System.getenv("BASE_FOLDER");

        try
        {
            //Build up the command
            ArrayList<String> alCommand = new ArrayList<String>();
            alCommand.add(envJavaHome + "\\bin\\java.exe");
            alCommand.add("-cp");
            alCommand.add(envAntClasspath);
            alCommand.add("org.apache.tools.ant.launch.Launcher");
            alCommand.add("-lib");
            alCommand.add(envLibs);
            alCommand.add("-file");
            alCommand.add(envBaseFolder + "\\sdk\\build\\upgrade\\build-upgrade.xml");
            alCommand.add("-Dadditional.properties=" +
                          fAdditionalProperties.getCanonicalPath());

            String[] asOthers = m_clLine.getArgs();

            for (int iCount = 0; iCount < asOthers.length; iCount++)
            {
                alCommand.add(asOthers[iCount]);
            }

            String[] saCmdArgs = alCommand.toArray(new String[alCommand.size()]);
            
            for (String arg : saCmdArgs)
            {
                System.out.println(arg);
            }

            ProcessBuilder pb = new ProcessBuilder(saCmdArgs);
            String sProjectRoot = pProperties.getProperty("project.root");
            pb.directory(new File(sProjectRoot));

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

            //Project location
            Option oProjectLocation = OptionBuilder.withArgName("projectlocation")
                                                   .hasArg()
                                                   .withDescription("Sets the location of the new project that should be upgraded.")
                                                   .isRequired()
                                                   .create(OPTION_PROJECT_LOCATION);
            s_oOptions.addOption(oProjectLocation);

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
