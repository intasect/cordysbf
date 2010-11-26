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
package com.cordys.coe.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class takes care of logging. Logging can be done do both a file or
 * stdout. The class is a singleton and it's thread-safe.
 */
public class Logger
{
    /**
     * Identifies a pure debug message
     */
    public static final int LOG_DEBUG = 100;
    /**
     * Identifies a normal message
     */
    public static final int LOG_MESSAGE = 75;
    /**
     * Identifies a waring
     */
    public static final int LOG_WARNING = 50;
    /**
     * Identifies an exception
     */
    public static final int LOG_EXCEPTION = 25;
    /**
     * Singleton.
     */
    private static Logger lLogger;
    /**
     * Holds the writer to the file.
     */
    private BufferedWriter bwFile;
    /**
     * Holds the descriptions for the categories
     */
    private Hashtable<Integer, String> hDesc;
    /**
     * Holds the name of the file to log to.
     */
    private String sFilename;
    /**
     * Holds a list with all components to write the messages of type
     * LOG_MESSAGE to.
     */
    private Vector<LogWindow> vMsgWindows;
    /**
     * Holds wether or not to include the time in the entry.
     */
    private boolean bDateStamp;
    /**
     * Holds wether or not to log to a file
     */
    private boolean bFile;
    /**
     * Holds wether or not to log to stdout
     */
    private boolean bStdOut;
    /**
     * Holds the currently set log-level.
     */
    private int iLogLevel;

    /**
     * Constructor.
     */
    private Logger()
    {
        iLogLevel = 100;
        bFile = false;
        bStdOut = true;
        bDateStamp = true;
        hDesc = new Hashtable<Integer, String>();
        hDesc.put(new Integer(LOG_DEBUG), "DBG");
        hDesc.put(new Integer(LOG_WARNING), "WRN");
        hDesc.put(new Integer(LOG_EXCEPTION), "EXC");
        hDesc.put(new Integer(LOG_MESSAGE), "MSG");
        vMsgWindows = new Vector<LogWindow>();
    }

    /**
     * This method returns the Logger-class to use. If it hasn't been created
     * yet, it will be created.
     *
     * @return The Logger to use.
     */
    public static final Logger getLogger()
    {
        if (lLogger == null)
        {
            lLogger = new Logger();
        }
        return lLogger;
    }

    /**
     * Sets wether or not to use the timestamp
     *
     * @param bDateStamp Wether or not to use the timestamp
     */
    public void setDateStamp(boolean bDateStamp)
    {
        this.bDateStamp = bDateStamp;
    }

    /**
     * This method enables the logging to a file.
     *
     * @param bFile Wether or not to log to a file.
     * @param sFilename The name of the file to log to.
     */
    public void setFile(boolean bFile, String sFilename)
    {
        this.bFile = bFile;
        this.sFilename = sFilename;

        if (bwFile != null)
        {
            try
            {
                bwFile.close();
            }
            catch (IOException e)
            {
                //Ignore exception
            }
        }

        if (bFile == true)
        {
            if ((sFilename == null) || sFilename.equals(""))
            {
                sFilename = "default.log";
            }

            File fTemp = new File(sFilename);

            if (fTemp.exists())
            {
                fTemp.delete();
            }

            try
            {
                bwFile = new BufferedWriter(new FileWriter(fTemp));
            }
            catch (IOException e)
            {
                bFile = false;
                sFilename = null;
            }
        }
    }

    /**
     * Returns the name of the logfile.
     *
     * @return The name of the logfile.
     */
    public String getFilename()
    {
        return sFilename;
    }

    /**
     * This method sets the loglevel to display
     *
     * @param iLogLevel The loglevel to display.
     */
    public void setLogLevel(int iLogLevel)
    {
        this.iLogLevel = iLogLevel;
    }

    /**
     * Sets wether or not to log to stdout.
     *
     * @param bStdOut Wether or not to log to stdout.
     */
    public void setStdOut(boolean bStdOut)
    {
        this.bStdOut = bStdOut;
    }

    /**
     * Adds an object to the display-vector.
     *
     * @param lwLogWindow The component to log to.
     */
    public void addMsgDisplay(LogWindow lwLogWindow)
    {
        vMsgWindows.add(lwLogWindow);
    }

    /**
     * This method logs the passed on message. The message will be logged as a
     * DEBUG-message.
     *
     * @param sMessage The message to log
     */
    public static void log(String sMessage)
    {
        log(LOG_DEBUG, sMessage);
    }

    /**
     * This method logs the passed on message.
     *
     * @param iCategory The category to log under.
     * @param sMessage The message to log.
     */
    public static void log(int iCategory, String sMessage)
    {
        getLogger().logMessage(iCategory, sMessage);
    }

    /**
     * This method returns the datestrig to use. If the date shouldn't be
     * displayed an empty string is returned with spaces. Datetime would be
     * returned like YYYYMMDD_HHMMSS.
     *
     * @return The date-string.
     */
    private String getDateString()
    {
        String sReturn = null;

        if (bDateStamp == true)
        {
            Date dDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            sReturn = sdf.format(dDate);
        }
        else
        {
            sReturn = "               ";
        }
        return sReturn;
    }

    /**
     * This method returns the description for the passed on category.
     *
     * @param iCategory The category to get the description of.
     *
     * @return DOCUMENTME
     */
    private String getDesc(int iCategory)
    {
        return hDesc.get(new Integer(iCategory));
    }

    /**
     * This method logs the passed on message.
     *
     * @param iCategory The category to log under.
     * @param sMessage The message to log.
     */
    private void logMessage(int iCategory, String sMessage)
    {
        if (iCategory <= iLogLevel)
        {
            StringBuffer sbBuffer = new StringBuffer();
            sbBuffer.append(getDesc(iCategory));
            sbBuffer.append(" ");
            sbBuffer.append(getDateString());
            sbBuffer.append(" ");

            String sBase = sbBuffer.toString();
            String[] saStrings = sMessage.split("\r\n|\n");

            for (int iCount = 0; iCount < saStrings.length; iCount++)
            {
                String sActualMessage = sBase + " " + saStrings[iCount];

                //Standard out
                if (bStdOut)
                {
                    System.out.println(sActualMessage);
                }

                //File logging
                if (bFile)
                {
                    try
                    {
                        bwFile.write(sActualMessage);
                        bwFile.newLine();
                        bwFile.flush();
                    }
                    catch (IOException e)
                    {
                        //Ignore exception
                    }
                }

                //Dispatch the message to the subscribers.
                for (int iLWCount = 0; iLWCount < vMsgWindows.size();
                         iLWCount++)
                {
                    vMsgWindows.get(iLWCount).onMessage(iCategory,
                                                                      sActualMessage);
                }
            }
        }
    }
}
