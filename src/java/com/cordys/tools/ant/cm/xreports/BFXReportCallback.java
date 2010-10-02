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

import com.cordys.tools.ant.cm.ContentManagerTask;

import com.cordys.xreport.publish.IWrapperCallback;
import com.cordys.xreport.publish.ReportWrapper;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.input.InputRequest;

/**
 * This class handles callback messages from the report wrapper.
 *
 * @author  pgussow
 */
public class BFXReportCallback
    implements IWrapperCallback
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
     * Creates a new BFXReportCallback object.
     *
     * @param  cmtTask       The Ant task to use.
     * @param  sContentType  The current content type.
     */
    public BFXReportCallback(ContentManagerTask cmtTask, String sContentType)
    {
        m_cmtTask = cmtTask;
        m_sContentType = sContentType;
    }

    /**
     * This method should return the file path to the passed on report XML.
     *
     * @param   sReportName  The name of the report.
     * @param   rwReport     The instance of the main report wrapper.
     *
     * @return  The file path for the sub report jrxml file.
     *
     * @see     com.cordys.xreport.publish.IWrapperCallback#getSubreportFilepath(java.lang.String, com.cordys.xreport.publish.ReportWrapper)
     */
    public String getSubreportFilepath(String sReportName, ReportWrapper rwReport)
    {
        m_cmtTask.log("[" + m_sContentType + "] Getting the location for subreport " + sReportName,
                      Project.MSG_VERBOSE);

        InputRequest ir = new InputRequest("Please enter the location for the jrxml file for report " +
                                           sReportName);

        m_cmtTask.getProject().getInputHandler().handleInput(ir);

        return ir.getInput();
    }
}
