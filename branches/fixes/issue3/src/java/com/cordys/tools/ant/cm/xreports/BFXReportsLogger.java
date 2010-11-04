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
package com.cordys.tools.ant.cm.xreports;

import com.cordys.coe.util.general.Util;

import com.cordys.tools.ant.cm.ContentManagerTask;

import com.cordys.xreport.publish.ILogger;

import org.apache.tools.ant.Project;

/**
 * Class to capture the logging.
 *
 * @author  pgussow
 */
public class BFXReportsLogger
    implements ILogger
{
    /**
     * The ant task to use..
     */
    private ContentManagerTask m_cmtTask;
    /**
     * Holds the current content type.
     */
    private String m_sContentType;

    /**
     * Creates a new BFXReportsLogger object.
     *
     * @param  cmtTask       The Ant task to use.
     * @param  sContentType  The current content type.
     */
    public BFXReportsLogger(ContentManagerTask cmtTask, String sContentType)
    {
        m_cmtTask = cmtTask;
        m_sContentType = sContentType;
    }

    /**
     * This method logs a debug message.
     *
     * @param  sMessage  The message.
     *
     * @see    com.cordys.xreport.publish.ILogger#debug(java.lang.String)
     */
    public void debug(String sMessage)
    {
        m_cmtTask.log("[" + m_sContentType + "] " + sMessage, Project.MSG_VERBOSE);
    }

    /**
     * This method logs a debug message.
     *
     * @param  sMessage    The message.
     * @param  tException  The exception.
     *
     * @see    com.cordys.xreport.publish.ILogger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug(String sMessage, Throwable tException)
    {
        m_cmtTask.log("[" + m_sContentType + "] " + sMessage + "\n" +
                      Util.getStackTrace(tException), Project.MSG_VERBOSE);
    }

    /**
     * This method returns whether or not the debug logging is enabled.
     *
     * @return  true if the debug logging is enabled. Otherwise false.
     *
     * @see     com.cordys.xreport.publish.ILogger#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        return true;
    }

    /**
     * This method logs a warning message.
     *
     * @param  sMessage  The message.
     *
     * @see    com.cordys.xreport.publish.ILogger#warn(java.lang.String)
     */
    public void warn(String sMessage)
    {
        m_cmtTask.log("[" + m_sContentType + "] " + sMessage, Project.MSG_WARN);
    }
}
