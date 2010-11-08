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
package com.cordys.coe.test;

import com.cordys.coe.ant.studio.content.StudioConstants;
import com.cordys.tools.ant.cm.ContentManagerTask;
import com.cordys.tools.ant.util.GeneralUtils;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestFlowName
{
    /**
     * Main method.
     *
     * @param  saArguments  Commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
        	String studioRoot = getFlowsRootFolder("");
        	String key = "/cordys/cas/vcm/studio/modelrepository/3.Business Process Models/SiemensFS/CoF/MainProcesses/TerminateRequest_SiemensFS10.bpm";
			String flowFilename = createFlowFileName(key, studioRoot);
        	System.out.println("studioRoot:\t'" + studioRoot + "'\nKey:\t\t" + key + "\nFilename:\t" + flowFilename + "\n\n");
        	
        	studioRoot = getFlowsRootFolder("/3.Business Process Models");
        	key = "/cordys/cas/vcm/studio/modelrepository/3.Business Process Models/SiemensFS/CoF/MainProcesses/TerminateRequest_SiemensFS10.bpm";
			flowFilename = createFlowFileName(key, studioRoot);
			System.out.println("studioRoot:\t'" + studioRoot + "'\nKey:\t\t" + key + "\nFilename:\t" + flowFilename + "\n\n");
        	
        	studioRoot = getFlowsRootFolder("3.Business Process Models");
        	key = "/cordys/cas/vcm/studio/modelrepository/3.Business Process Models/SiemensFS/CoF/MainProcesses/TerminateRequest_SiemensFS10.bpm";
			flowFilename = createFlowFileName(key, studioRoot);
			System.out.println("studioRoot:\t'" + studioRoot + "'\nKey:\t\t" + key + "\nFilename:\t" + flowFilename + "\n\n");
        	
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Creates flow vcmdata file name from the flow key.
     *
     * @param   sFlowKey  Flow key.
     * @param sStudioFlowRootDir 
     *
     * @return  File name.
     */
    protected static String createFlowFileName(String sFlowKey, String sStudioFlowRootDir)
    {
        String sDestFileName = sFlowKey.substring(sStudioFlowRootDir.length() + 1);

        if (sStudioFlowRootDir.endsWith("/3.Business Process Models") &&
                !sFlowKey.startsWith("3.Business Process Models"))
        {
            sDestFileName = "3.Business Process Models/" + sDestFileName;
        }

        return sDestFileName.replaceFirst("\\.bpm$", ".vcmdata");
    }
    
    /**
     * This method returns the base folder for the BPMs to export. This is needed to be able to
     * support extracting the full content.
     * 
     * @param   cmtTask  Current task.
     *
     * @return  The root folder for the flow.
     */
    public static String getFlowsRootFolder(String sStudioRootFolder)
    {
        if ((sStudioRootFolder == null) || (sStudioRootFolder.length() == 0))
        {
            sStudioRootFolder = "/3.Business Process Models";
        }

        if (!sStudioRootFolder.startsWith("/"))
        {
            sStudioRootFolder = "/" + sStudioRootFolder;
        }

        sStudioRootFolder = StudioConstants.DEFAULT_FLOW_ROOT_FOLDER + sStudioRootFolder;

        return sStudioRootFolder;
    }
}
