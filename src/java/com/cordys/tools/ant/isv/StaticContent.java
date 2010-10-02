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
 *         File                :        StaticContent.java
 *         Author                :        manesh@cordys.com
 *         Created on         :        Sep 27, 2004
 *
 */
package com.cordys.tools.ant.isv;

import com.cordys.tools.ant.soap.ISoapRequestManager;

import com.eibus.xml.nom.Node;

import java.io.File;
import java.io.IOException;

/**
 * Each component will have custom loader that need to be specified with isv.
 * These custom xml will be inserted as child of
 * <pre><ISVPackage><content></pre>
 * <pre>
 * 	A sample custom xml definition is 
 * 	<OrchestratorDBLoader 
 * 		description="DataBase Configuration"
 * 		loader="com.cordys.cpc.installer.ConfigureDBPanel"
 * 		url="${build.web}/developer/content/databasedetails.htm"
 * 	/>
 *  
 * 	Here loader is the class which will be called by ISVLoader.
 * 	url is optional. (url="")
 * 	If url is present, its value must be a valid entry in the isvp file,
 * 	prefixed with 'isv_temp_dir\'
 * 	ie. A valid zip entry must be present.  
 * 
 * 	Since ISVPackage creator makes zip entries with absolute file name,
 * 	that exists on drive, we need a custom handling, which will find out
 * 	the absolute file name that is specified in the url value, and will
 * 	put the required prefix also. ('isv_temp_dir\')
 * 
 * 	Sample convertion of url value
 * 
 * 	Url specified in xml file
 * 		"${build.web}/developer/content/databasedetails.htm"
 * 	
 * 	After changing the properties
 * 		./build/web/developer/content/databasedetails.htm
 * 
 * 	Absolute path of the file
 *  D:\config\bcp\products\Orchestrator\.\build\web\developer\content\databasedetails.htm
 * 
 * 	Cannonical path of the file
 *  D:\config\bcp\products\Orchestrator\build\web\developer\content\databasedetails.htm
 * 
 * 	CannonicalPath-Without drive
 *  config\bcp\products\Orchestrator\build\web\developer\content\databasedetails.htm
 * 
 * 	Url after inserting the prefix
 *  isv_temp_dir\config\bcp\products\Orchestrator\build\web\developer\content\databasedetails.htm
 * </pre>
 */
public class StaticContent
    implements ISVContentHandler
{
    /**
     * Default constructor
     */
    public StaticContent()
    {
        super();
    }

    /**
     * Implementation of the abstract method getContentXML
     *
     * @param isvTask The instance of ISVCreatorTask class representing the tag
     *        'isv'.
     * @param contentTask The instance of ContentTask class representing the
     *        tag 'content'.
     * @param inputXML The content XML which has to be processed.
     * @param iCurrentIsvContentNode DOCUMENTME
     * @param iCurrentIsvPromptsetNode DOCUMENTME
     *
     * @return The content XML created by this content creator
     */
    public int[] getISVContentXML(ISVCreatorTask isvTask, Content contentTask,
                                  int inputXML, int iCurrentIsvContentNode,
                                  int iCurrentIsvPromptsetNode)
    {
        int iReturn = Node.clone(inputXML, true);
        String sURL = Node.getAttribute(iReturn, "url");

        if ((sURL != null) && !"".equals(sURL))
        {
            //PGUSSOW: The replacement should only be done if the URL is referencing to an HTML file
            //which is located within the project. If a default page is shown the url property should be untouched.
            //The replacement will only be done if the current URL contains properties.
            if (sURL.indexOf("${") >= 0)
            {
                sURL = contentTask.getProject().replaceProperties(sURL);

                try
                {
                    sURL = new File(sURL).getCanonicalPath();
                    
                    if (File.separator.equals("\\")){
                        // For Windows srip drive letter from path
                        sURL = sURL.substring(2); //Remove drive 'D:'
                    }
                    
                    sURL = "isv_temp_dir" + sURL;
                }
                catch (IOException ioe)
                {
                    //Throw Exception from here
                }
                
                isvTask.log("Setting url value for " +
                            Node.getName(inputXML) + " :" + sURL,
                            ISoapRequestManager.MSG_DEBUG);
                
                Node.setAttribute(iReturn, "url", sURL);
            }
        }

        return new int[] { iReturn };
    }
}
