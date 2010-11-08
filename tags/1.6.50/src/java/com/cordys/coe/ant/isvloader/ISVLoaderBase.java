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
package com.cordys.coe.ant.isvloader;

import java.io.FileWriter;
import java.io.IOException;

import com.eibus.contentmanagement.ISVContentManager;
import com.eibus.contentmanagement.ISVPackage;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * A base class for custom ISV loaders. Mainly helps with logging.
 *
 * @author mpoyhone
 */
public class ISVLoaderBase extends ISVContentManager
{
    /**
     * The XML document object.
     */
    protected Document dDoc = null;
    /**
     * The ISV package
     */
    protected ISVPackage ipIsvPackage = null;
    /**
     * Name of the log file.
     */
    protected String sLogFileName = null;
    /**
     * The log XML node.
     */
    protected int iLogNode = 0;

    /**
     * Called when the ISV package is being loaded.
     *
     * @param isvPackage The ISV package object.
     * @param contentItem The CoBOC content XML structure that is in the ISV
     *        package.
     * @param logcontentItem The log XML structure.
     *
     * @throws Exception Thrown if the operation failed.
     */
    public void load(ISVPackage isvPackage, int contentItem, int logcontentItem)
              throws Exception
    {
        ipIsvPackage = isvPackage;

        openLog(logcontentItem);

        log("Starting. Node: " + Node.writeToString(contentItem, true));
    }

    /**
     * Called when the ISV package is being unloaded.
     *
     * @param isvPackage The ISV package object.
     * @param contentItem The CoBOC content XML structure that is in the ISV
     *        package.
     * @param logcontentItem The log XML structure.
     *
     * @throws Exception Thrown if the operation failed.
     */
    public void unload(ISVPackage isvPackage, int contentItem,
                       int logcontentItem)
                throws Exception
    {
        openLog(logcontentItem);

        if (!isRolledBack)
        {
            super.unload(isvPackage, contentItem, logcontentItem);
        }
    }

    /**
     * A simple logging method to write log messages while the content is being
     * processed.
     *
     * @param sMsg The message to be written to the log.
     */
    protected void log(String sMsg)
    {
        if ((iLogNode == 0) || (dDoc == null))
        {
            return;
        }

        dDoc.createTextElement("entry", sMsg, iLogNode);

        try
        {
            FileWriter fwWriter = new FileWriter(sLogFileName, true);

            fwWriter.write(sMsg + "\n");
            fwWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Opens the log file. Some of the stuff is copied from a Studio loader
     * class.
     *
     * @param iLogNode The log XML node passed to this loader.
     */
    protected void openLog(int iLogNode)
    {
        dDoc = Node.getDocument(iLogNode);
        this.iLogNode = iLogNode;

        if (System.getProperty("os.name").indexOf("Linux") != -1)
        {
            sLogFileName = EIBProperties.getInstallDir() +
                           System.getProperty("file.separator") +
                           "test-loader.txt";
        }
        else
        {
            sLogFileName = EIBProperties.getInstallDir() +
                           System.getProperty("file.separator") +
                           "test-loader.txt";
        }
    }
}
