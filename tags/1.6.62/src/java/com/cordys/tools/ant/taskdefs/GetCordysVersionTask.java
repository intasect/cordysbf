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
 * Returns Cordys BCP version. This class also implements the <code>Condition</code> interface
 * so this can be used to check if the BCP installation is running (inside a waitfor element).
 *
 * @author mpoyhone
 */
public class GetCordysVersionTask extends Task implements Condition
{
    private URL uServerUrl;
    private String sUsername;
    private String sPassword;
    private String sVersionProperty; 
    private String sBuildNumberProperty;
    private String sRevisionNumberProperty;
    
    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        super.execute();
        
        sendRequest();
    }
    
    private void sendRequest() {
        ISoapRequestManager srmSoap;
        
        log("Getting Cordys BCP version information from " + uServerUrl, Project.MSG_VERBOSE);
        
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
            xResponse = srmSoap.makeSoapRequest("http://schemas.cordys.com/1.0/monitor", "GetInstallationInfo", 0);
            
            int xBuildInfo = Find.firstMatch(xResponse, "?<tuple><old><buildinfo>");
            
            if (xBuildInfo == 0) {
                throw new BuildException("No 'buildinfo' element in the response.");
            }
            
            Project pProject = getProject();
            String sVersion = Node.getDataElement(xBuildInfo, "version", ""); 
            String sBuildNumber = Node.getDataElement(xBuildInfo, "build", "");
            String sRevisionNumber = Node.getDataElement(xBuildInfo, "revision", "");
            
            if (sVersionProperty != null && sVersionProperty.length() > 0)
            {
                pProject.setProperty(sVersionProperty, sVersion);
            }
            
            if (sBuildNumberProperty != null && sBuildNumberProperty.length() > 0)
            {
                pProject.setProperty(sBuildNumberProperty, sBuildNumber);
            }
            
            if (sRevisionNumberProperty != null && sRevisionNumberProperty.length() > 0)
            {
                pProject.setProperty(sRevisionNumberProperty, sRevisionNumber);
            }
            
            log("BCP version:        " + sVersion, Project.MSG_VERBOSE);
            log("BCP build number:   " + sBuildNumber, Project.MSG_VERBOSE);
            log("BCP build revision: " + sRevisionNumber, Project.MSG_VERBOSE);
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
    
    /**
     * Returns the buildNumberProperty.
     *
     * @return Returns the buildNumberProperty.
     */
    public String getBuildproperty()
    {
        return sBuildNumberProperty;
    }
    
    /**
     * The buildNumberProperty to set.
     *
     * @param aBuildNumberProperty The buildNumberProperty to set.
     */
    public void setBuildproperty(String aBuildNumberProperty)
    {
        sBuildNumberProperty = aBuildNumberProperty;
    }
    
    /**
     * Returns the revisionNumberProperty.
     *
     * @return Returns the revisionNumberProperty.
     */
    public String getRevisionproperty()
    {
        return sRevisionNumberProperty;
    }
    
    /**
     * The revisionNumberProperty to set.
     *
     * @param aRevisionNumberProperty The revisionNumberProperty to set.
     */
    public void setRevisionproperty(String aRevisionNumberProperty)
    {
        sRevisionNumberProperty = aRevisionNumberProperty;
    }
    
    /**
     * Returns the versionProperty.
     *
     * @return Returns the versionProperty.
     */
    public String getVersionproperty()
    {
        return sVersionProperty;
    }
    
    /**
     * The versionProperty to set.
     *
     * @param aVersionProperty The versionProperty to set.
     */
    public void setVersionproperty(String aVersionProperty)
    {
        sVersionProperty = aVersionProperty;
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
        catch (BuildException ignored) {
            return false;
        }
    }
}
