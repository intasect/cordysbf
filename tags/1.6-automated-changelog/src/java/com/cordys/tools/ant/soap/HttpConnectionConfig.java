/**
 * Copyright 2008 Cordys R&D B.V. 
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
package com.cordys.tools.ant.soap;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.cordys.tools.ant.util.GeneralUtils;

/**
 * Configuration for an HTTP connection.
 *
 * @author mpoyhone
 */
public class HttpConnectionConfig
{
    /**
     * Property name for web gateway URL.
     */
    private static final String PROPERTY_GATEWAY_URL = "webgateway.url";
    /**
     * Property name for web gateway user name.
     */
    private static final String PROPERTY_GATEWAY_USERNAME = "webgateway.user";
    /**
     * Property name for web gateway user password.
     */
    private static final String PROPERTY_GATEWAY_PASSWORD = "webgateway.password";
    /**
     * Property name for web gateway sso enabling.
     */
    private static final String PROPERTY_GATEWAY_SSO = "webgateway.sso";
    /**
     * Property name for HTTP proxy host name.
     */
    private static final String PROPERTY_PROXY_HOST = "http.proxy.host";
    /**
     * Property name for HTTP proxy port.
     */
    private static final String PROPERTY_PROXY_PORT = "http.proxy.port";
    /**
     * Property name for HTTP user name.
     */
    private static final String PROPERTY_PROXY_USER_NAME = "http.proxy.user";    
    /**
     * Property name for HTTP user password.
     */
    private static final String PROPERTY_PROXY_USER_PASSWORD = "http.proxy.password";    
    /**
     * Name of HTTP authentication scheme to be used.
     */
    private static final String PROPERTY_HTTP_AUTH_SCHEME = "http.authentication.scheme";
    
    /**
     * Contains the web gateway URL if connection mode is 'webgateway'.
     */
    private URL gatewayUrl;
    /**
     * Contains the web gateway user name.
     */
    private String userName;
    /**
     * Contains the web gateway user password.
     */
    private String password;    
    /**
     * Contains the HTTP proxy host name.
     */
    private String proxyHost;
    /**
     * Contains the HTTP proxy port.
     */
    private int proxyPort = 8080;
    /**
     * Contains the HTTP proxy user name.
     */
    private String proxyUserName;
    /**
     * Contains the HTTP proxy user password.
     */
    private String proxyPassword;
    /**
     * Optional HTTP authentication scheme name. 
     */
    private String authScheme;
    /**
     * Flag indicating whether the java.net authenticator has been created with these settings.
     */
    private boolean authenticatorCreated;
    /**
     * Optional Ant task for logging.
     */
    private Task logTask;
    /**
     * Holds whether or not Cordys SSO is enabled.
     */
	private boolean ssoEnabled;
    
    /**
     * Reads the connection settings from the Ant task.
     * 
     * @param task Task.
     */
    public void configureFromTask(Task task, String sConnectionMode) throws SoapRequestException
    {
        logTask = task;
        
        Project project = task.getProject();
        String sGatewayUrl = GeneralUtils.getTrimmedProperty(project, PROPERTY_GATEWAY_URL);
        String sGatewayUser = GeneralUtils.getTrimmedProperty(project,
                                                              PROPERTY_GATEWAY_USERNAME);
        String sGatewayPassword = GeneralUtils.getTrimmedProperty(project,
                                                                  PROPERTY_GATEWAY_PASSWORD);
        boolean bGatewaySSO = Boolean.parseBoolean(GeneralUtils.getTrimmedProperty(project, PROPERTY_GATEWAY_SSO));
        
        URL uUrl;

        if (sGatewayUrl == null)
        {
            throw new SoapRequestException("Connection mode '" + sConnectionMode +
                                           "' requires the property '" + PROPERTY_GATEWAY_URL +
                                           "' to be set.");
        }
        
        try
        {
            uUrl = new URL(sGatewayUrl);
        }
        catch (MalformedURLException e)
        {
            throw new SoapRequestException("Invalid gateway URL " + sGatewayUrl);
        }

        gatewayUrl = uUrl;
        userName = sGatewayUser;
        password = sGatewayPassword;
        ssoEnabled = bGatewaySSO;
        authScheme = GeneralUtils.getTrimmedProperty(project, PROPERTY_HTTP_AUTH_SCHEME);
        
        proxyHost = GeneralUtils.getTrimmedProperty(project, PROPERTY_PROXY_HOST);
        
        if (proxyHost != null && proxyHost.length() > 0)
        {
            String portStr = GeneralUtils.getTrimmedProperty(project, PROPERTY_PROXY_PORT);
            
            if (portStr != null && portStr.length() > 0)
            {
                try {
                    proxyPort = Integer.parseInt(portStr);
                }
                catch (Exception e) {
                    throw new SoapRequestException("Invalid HTTP proxy port value: " + portStr);
                }
            }
            
            proxyUserName = GeneralUtils.getTrimmedProperty(project, PROPERTY_PROXY_USER_NAME);
            proxyPassword= GeneralUtils.getTrimmedProperty(project, PROPERTY_PROXY_USER_PASSWORD);
        } 
    }
    
    /**
	 * This method gets whether or not sso is enabled.
	 * 
	 * @return Whether or not sso is enabled.
	 */
	public boolean getSSO()
	{
		return ssoEnabled;
	}

    /**
     * Creates the Java Authenticator which provides the user name and password for HTTP connection.
     * The authenticator is created only with the first call other calls do nothing.
     * 
     * @param logTask Optional Ant task for logging.
     */
    public void createAuthenticator()
    {
        if (authenticatorCreated) {
            return;
        }
        
        PasswordAuthentication gatewayAuth = null;
        PasswordAuthentication proxyAuth = null;
        
        if (userName != null && userName.length() > 0)
        {
            gatewayAuth = new PasswordAuthentication(userName,
                                                     password != null ? password.toCharArray() : new char[0]);
        }
        
        if (proxyHost != null && proxyHost.length() > 0) {
            if (proxyUserName != null && proxyUserName.length() > 0)
            {
                proxyAuth = new PasswordAuthentication(proxyUserName,
                                                       proxyPassword != null ? proxyPassword.toCharArray() : new char[0]);
            }
            
            if (logTask != null) {
                logTask.log("Setting HTTP proxy: " + proxyHost + ":" + proxyPort, Project.MSG_DEBUG);
            }
            
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", Integer.toString(proxyPort));
            
            if (authScheme != null && authScheme.length() > 0)
            {
                if (logTask != null) {
                    logTask.log("Setting HTTP authentication scheme: " + authScheme, Project.MSG_DEBUG);
                }
                
                System.setProperty("http.auth.preference", authScheme);
            }
        }
        
        final PasswordAuthentication gatewayAuth1 = gatewayAuth;
        final PasswordAuthentication proxyAuth1 = proxyAuth;
        
        Authenticator aAuth = new Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                if (Authenticator.RequestorType.PROXY == getRequestorType()) {
                    if (logTask != null) {
                        logTask.log("Returing HTTP proxy user name: " + proxyAuth1.getUserName(), Project.MSG_DEBUG);
                    }
                    
                    return proxyAuth1;
                } else {
                    if (logTask != null) {
                        logTask.log("Returning HTTP server user name: " + gatewayAuth1.getUserName(), Project.MSG_DEBUG);
                    }
                    
                    return gatewayAuth1;
                }
            }
        };

        Authenticator.setDefault(aAuth);
        
        authenticatorCreated = true;
    }
    
    /**
     * Returns the gatewayUrl.
     *
     * @return Returns the gatewayUrl.
     */
    public URL getGatewayUrl()
    {
        return gatewayUrl;
    }

    /**
     * Sets the gatewayUrl.
     *
     * @param gatewayUrl The gatewayUrl to be set.
     */
    public void setGatewayUrl(URL gatewayUrl)
    {
        this.gatewayUrl = gatewayUrl;
    }

    /**
     * Returns the userName.
     *
     * @return Returns the userName.
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Sets the userName.
     *
     * @param userName The userName to be set.
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    /**
     * Returns the password.
     *
     * @return Returns the password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password The password to be set.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Returns the proxyHost.
     *
     * @return Returns the proxyHost.
     */
    public String getProxyHost()
    {
        return proxyHost;
    }

    /**
     * Sets the proxyHost.
     *
     * @param proxyHost The proxyHost to be set.
     */
    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
    }

    /**
     * Returns the proxyPort.
     *
     * @return Returns the proxyPort.
     */
    public int getProxyPort()
    {
        return proxyPort;
    }

    /**
     * Sets the proxyPort.
     *
     * @param proxyPort The proxyPort to be set.
     */
    public void setProxyPort(int proxyPort)
    {
        this.proxyPort = proxyPort;
    }

    /**
     * Returns the proxyUserName.
     *
     * @return Returns the proxyUserName.
     */
    public String getProxyUserName()
    {
        return proxyUserName;
    }

    /**
     * Sets the proxyUserName.
     *
     * @param proxyUserName The proxyUserName to be set.
     */
    public void setProxyUserName(String proxyUserName)
    {
        this.proxyUserName = proxyUserName;
    }

    /**
     * Returns the proxyPassword.
     *
     * @return Returns the proxyPassword.
     */
    public String getProxyPassword()
    {
        return proxyPassword;
    }

    /**
     * Sets the proxyPassword.
     *
     * @param proxyPassword The proxyPassword to be set.
     */
    public void setProxyPassword(String proxyPassword)
    {
        this.proxyPassword = proxyPassword;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder(2048);
        
        buf.append("  Web Gateway URL : ").append(gatewayUrl).append("\n");
        
        if (userName != null && userName.length() > 0)
        {
            buf.append("  User Name       : ").append(userName).append("\n");    
        }
        
        if (proxyHost != null && proxyHost.length() > 0)
        {
            buf.append("  HTTP Proxy      : ").append(proxyHost).append(":").append(proxyPort).append("\n");    
            
            if (proxyUserName != null && proxyUserName.length() > 0)
            {
                buf.append("  HTTP Proxy User : ").append(proxyUserName).append("\n");            
            }
        }
        
        return buf.toString();
    }
}