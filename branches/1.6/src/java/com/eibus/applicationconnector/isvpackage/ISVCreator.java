/**
 * Copyright 2004 Cordys R&D B.V. 
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
 /**
 *         Project         :        BuildFramework
 *         File                :        ISVCreator.java
 *         Author                :        manesh@cordys.com
 *         Created on         :        Oct 20, 2004
 *
 */
package com.eibus.applicationconnector.isvpackage;

import org.apache.tools.ant.Task;

import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.eibus.soap.BodyBlockCreator;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * This class will invoke the CreateISVPackage message at api level.
 */
public class ISVCreator extends CreateISVPackage
{
    /**
     * DOCUMENTME
     */
    private String isvpDir = null;

    /**
     * Creates a new ISVCreator object.
     */
    public ISVCreator()
    {
        document = new Document();
    }

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public Document getDocument()
    {
        return document;
    }

    /**
     * DOCUMENTME
     *
     * @param isvpDir DOCUMENTME
     */
    public void setISVPDir(String isvpDir)
    {
        this.isvpDir = isvpDir;
    }

    /**
     * DOCUMENTME
     *
     * @param node DOCUMENTME
     * @param isvpDir DOCUMENTME
     * @param task DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public int createISVP(int node, String isvpDir, Task task)
    {
        setISVPDir(isvpDir);
        return createISVP(node, task);
    }

    /**
     * Caller is expected to clean the request node and response node after
     * returning from the function. No clean up is performed in this function.
     * <pre>
     * 	<SOAP:Body>
     *  <CreateISVPackage xmlns="http://schemas.cordys.com/1.0/isvpackage">
     * 	<filename>Cordys Orchestrator 4.3</filename>
     * 	<ISVPackage>
     *  .....
     * 	</ISVPackage>
     *  </CreateISVPackage>
     * 	</SOAP:Body>
     *  </pre>
     * Here you should have a &lt;SOAP:Body> node as partent to
     * CreateISVPackage node
     *
     * @param node Node reference to CreateISVPackage node
     * @param task DOCUMENTME
     *
     * @return
     *
     * @throws Exception
     */
    public int createISVP(int node, Task task)
    {
        printDebugLog("APICall Request" +
                      Node.writeToString(Node.getRoot(node), true), task);

        //Create a dummy driver to invoke CreateISVPackage methods
        BodyBlockCreator driver = new BodyBlockCreator(node);

        //Instance variable from CreateISVPackage
        request = driver.getRequestBodyBlock();
        response = driver.getResponseBodyBlock();
        requestInterface = node;

        //TODO:EIBProperties has to be set dynamically
        printVerboseLog("APICall: Going to call process", task);

        boolean flag = prepare();
        printVerboseLog("APICall: Call returned from process : Status=" + flag,
                        task);

        if (flag)
        {
            printVerboseLog("APICall: Going to call commit", task);
            commit();
            printVerboseLog("APICall: Call returned from commit", task);
        }
        else
        {
            //Ideally should be calling abort (Here not required)
            //abort ();
        }

        printDebugLog("APICall Response" +
                      Node.writeToString(Node.getRoot(response.getXMLNode()),
                                         true), task);

        return Node.getRoot(response.getXMLNode());
    }

    /**
     * Returns the directory where ISV Package is to be created
     *
     * @return DOCUMENTME
     */
    protected String getISVPDir()
    {
        if ((isvpDir == null) || "".equals(isvpDir))
        {
            return super.getISVPDir();
        }

        return isvpDir;
    }

    /**
     * DOCUMENTME
     *
     * @param msg DOCUMENTME
     * @param task DOCUMENTME
     */
    private static void printDebugLog(String msg, Task task)
    {
        printLog(msg, task, ISoapRequestManager.MSG_DEBUG);
    }

    /**
     * DOCUMENTME
     *
     * @param msg DOCUMENTME
     * @param task DOCUMENTME
     * @param level DOCUMENTME
     */
    private static void printLog(String msg, Task task, int level)
    {
        if (task != null)
        {
            task.log(msg, level);
        }
        else
        {
            System.out.println(msg);
        }
    }

    /**
     * DOCUMENTME
     *
     * @param msg DOCUMENTME
     * @param task DOCUMENTME
     */
    private static void printVerboseLog(String msg, Task task)
    {
        printLog(msg, task, ISoapRequestManager.MSG_VERBOSE);
    }
}
