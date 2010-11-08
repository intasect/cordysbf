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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * A wrapper class for calling the build.bat from fromcordys, tocordys,
 * deletecordys and toruntime scripts. This content types and prefixes them
 * with the Ant target type prefix (e.g. fromcordys-methodsets).
 * <pre>
 * For single content export the format is :
 *    [script] single [content type] "Content name"
 * and this comes as
 *    build.bat -Dsingle.content="Content name" [target prexif]-[content-type]
 * </pre>
 *
 * @author mpoyhone
 */
public class AntLauncher
{
    /**
     * Contains names of all valid target prefixes.
     */
    private static final Set<String> sValidTargets = new HashSet<String>(Arrays.asList(new String[]
                                                                       {
                                                                           "fromcordys",
                                                                           "tocordys",
                                                                           "deletecordys",
                                                                           "toruntime"
                                                                       }));
    /**
     * Contains flags that take an argument as the next cmd line argument,.
     */
    private static final Set<String> sFlagsWithArgument = new HashSet<String>(Arrays.asList(new String[]
                                                                            {
                                                                                "-lib",
                                                                                "-logfile",
                                                                                "-l",
                                                                                "-logger",
                                                                                "-listener",
                                                                                "-buildfile",
                                                                                "-file",
                                                                                "-f",
                                                                                "-propertyfile",
                                                                                "-inputhandler",
                                                                                "-find",
                                                                                "-s",
                                                                                "-nice",
                                                                            }));

    private static void usage() {
        System.err.println("Usage: <build cmd> <target type> [parameters]");
    }
    
    /**
     * Main method.
     *
     * @param args Program line arguments.
     */
    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            usage();
            return;
        }
        
        int iArgPtr = 0;

        // Get the build command name (build.bat)
        String sBuildCmd = args[iArgPtr++];
        List<String> lBuildCmdArgs = new LinkedList<String>();
        
        if (sBuildCmd.startsWith("-p")) {
            // This defines the number of build command arguments (including the command itself).
            int iCmdArgCount = Integer.parseInt(sBuildCmd.substring(2));
            
            if (args.length < iCmdArgCount + 1) {
                usage();
                return;
            }
            
            sBuildCmd = args[iArgPtr++];
            
            for (int i = 0; i < iCmdArgCount - 1; i++) {
                lBuildCmdArgs.add(args[iArgPtr++]);
            }
        }

        // Get the target type fromcordys, etc. 
        String sTargetType = (String) args[iArgPtr++];

        if (!sValidTargets.contains(sTargetType))
        {
            System.err.println("Invalid Ant target type " + sTargetType);
            return;
        }

        // Separate the flags and ant tasks.
        List<String> lFlags = new LinkedList<String>();
        List<String> lTargets = new LinkedList<String>();

        for (int i = iArgPtr; i < args.length; i++)
        {
            String sArg = args[i];

            if (sArg.startsWith("-"))
            {
                lFlags.add(sArg);

                if (sFlagsWithArgument.contains(sArg) &&
                        (i < (args.length - 1)))
                {
                    // This flag uses the next argument also.
                    i++;
                    lFlags.add(args[i]);
                }
            }
            else
            {
                lTargets.add(sArg);
            }
        }

        if (lTargets.size() > 0)
        {
            // Check for single content operation.
            if (lTargets.get(0).equals("single") || lTargets.get(0).equals("\"single\""))
            {
                if (lTargets.size() < 3)
                {
                    System.err.println("Missing content name for single content operation.");
                    System.err.println("Usage: <cmd> single (<content name>) [<content name>]...");
                    return;
                }

                lTargets.remove(0);

                // Set the content type from the command line.
                String sContentType = lTargets.get(0);

                lTargets.remove(0);

                // Create the property from the given content names.
                StringBuffer sbSingleProperty = new StringBuffer(128);

                sbSingleProperty.append("-Dsingle.content=");

                for (ListIterator<String> liIter = lTargets.listIterator();
                         liIter.hasNext();)
                {
                    String sContentName = liIter.next();

                    if (liIter.previousIndex() > 0)
                    {
                        sbSingleProperty.append(';');
                    }
                    sbSingleProperty.append(sContentName);
                }

                lFlags.add(sbSingleProperty.toString());
                lTargets.clear();
                lTargets.add(sContentType);
            }
        }
        else
        {
            // Use the 'all' target.
            lTargets.add("all");
        }

        // Prefix the content types with target type to get the Ant task names.
        for (ListIterator<String> liIter = lTargets.listIterator(); liIter.hasNext();)
        {
            String sContentType = liIter.next();

            liIter.set(sTargetType + "-" + sContentType);
        }

        // Create the command line arguments and execute the build command.
        List<String> lArguments = new LinkedList<String>();

        lArguments.add(sBuildCmd);
        lArguments.addAll(lBuildCmdArgs);
        lArguments.addAll(lFlags);
        lArguments.addAll(lTargets);

        String[] saCmdArgs = lArguments.toArray(new String[lArguments.size()]);

        try
        {
            // Execute the build command.
            Process pBuildProcess = Runtime.getRuntime().exec(saCmdArgs);

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
