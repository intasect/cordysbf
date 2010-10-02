/**
 * Copyright 2007 Cordys R&D B.V. 
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
package com.cordys.tools.ant.taskdefs;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.condition.Condition;

import com.cordys.tools.ant.soap.GatewaySoapRequestManager;
import com.cordys.tools.ant.soap.HttpConnectionConfig;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.NomCollector;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Checks that all required SOAP processor are running in the
 * system organization.
 *
 * @author mpoyhone
 */
public class BcpStatusCondition extends Task implements Condition
{
    private URL uServerUrl;
    private String sUsername;
    private String sPassword;
    private static String[] saProcessorNameList = {
        "Orchestrator Admin Processor",
        "XForms Processor",
        "XForms Translation Processor",
        "Notification Processor",
        "COBOC Processor",
        "Studio Processor",
        "LDAP Processor",
    };
    
    private void sendRequest() {
        ISoapRequestManager srmSoap;
        
        log("Getting Cordys BCP status information from " + uServerUrl, Project.MSG_VERBOSE);
        
        try
        {
            HttpConnectionConfig config = new HttpConnectionConfig();
            
            config.setGatewayUrl(uServerUrl);
            config.setUserName(sUsername);
            config.setPassword(sPassword);
            
            srmSoap = new GatewaySoapRequestManager(config);
        }
        catch (SoapRequestException e)
        {
            throw new BuildException("Unable to create SOAP request manager.", e);
        }
        
        NomCollector ncGarbage = new NomCollector();
        int xResponse = 0;
        
        srmSoap.addNomCollector(ncGarbage);
        try
        {
            xResponse = srmSoap.makeSoapRequest("http://schemas.cordys.com/1.0/monitor", "List", 0);
            
            int[] xaProcessorInfos = Find.match(xResponse, "?<tuple><old><workerprocess>");
            Map<String, Integer> mEntryMap = new HashMap<String, Integer>();
            
            for (int xNode : xaProcessorInfos)
            {
                String sDn = Node.getDataElement(xNode, "name", "");
                String sName = sDn.replaceFirst("cn=([^,]+),.*", "$1");
                
                mEntryMap.put(sName, xNode);
            }
            
            for (String sName : saProcessorNameList)
            {
                if (! mEntryMap.containsKey(sName)) {
                    throw new BuildException("Processor '" + sName + "' not found.");
                }
                
                int xNode = mEntryMap.get(sName);
                String sProcessId = Node.getDataElement(xNode, "process-id", "");
                String sStatus = Node.getDataElement(xNode, "status", "");
                
                log(sName + ": Status='" + sStatus + "', Process ID='" + sProcessId + "'", Project.MSG_DEBUG);
                
                if (sProcessId.length() == 0 || ! "started".equalsIgnoreCase(sStatus)) {
                    throw new BuildException("SOAP processor '" + sName + "' is not running.");
                }
            }
        }
        catch (SoapRequestException e)
        {
            log("SOAP request failed: " + e.getMessage(), Project.MSG_VERBOSE);
            
            if (xResponse != 0) {
                log("SOAP response:\n" + Node.writeToString(xResponse, true), Project.MSG_DEBUG);
            }
            
            throw new BuildException("Unable to send SOAP request.", e);
        }
        finally {
            ncGarbage.deleteNodes();
        }
    }

    /**
     * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
     */
    public boolean eval() throws BuildException
    {
        try {
            sendRequest();
            return true;
        }
        catch (Exception e) {
            log("Not all Cordys BCP SOAP processors are running: " + e.getMessage(), Project.MSG_VERBOSE);
            return false;
        }
    }
    
    /**
     * Returns the password.
     *
     * @return Returns the password.
     */
    public String getPassword()
    {
        return sPassword;
    }
    
    /**
     * The password to set.
     *
     * @param aPassword The password to set.
     */
    public void setPassword(String aPassword)
    {
        sPassword = aPassword;
    }
    
    /**
     * Returns the serverUrl.
     *
     * @return Returns the serverUrl.
     */
    public URL getUrl()
    {
        return uServerUrl;
    }
    
    /**
     * The serverUrl to set.
     *
     * @param aServerUrl The serverUrl to set.
     */
    public void setUrl(URL aServerUrl)
    {
        uServerUrl = aServerUrl;
    }
    
    /**
     * Returns the username.
     *
     * @return Returns the username.
     */
    public String getUser()
    {
        return sUsername;
    }
    /**
     * The username to set.
     *
     * @param aUsername The username to set.
     */
    public void setUser(String aUsername)
    {
        sUsername = aUsername;
    }    
}
