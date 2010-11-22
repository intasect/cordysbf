/**
 * Copyright 2005 Cordys R&D B.V. 
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
package com.cordys.tools.ant.cm.soapunit;

import java.util.Vector;

import com.cordys.tools.ant.cm.ContentManagerTask;
import com.eibus.xml.nom.Find;

public class SystemConfiguration extends BaseClass
{
	private int systemConfiguration;
    private ConnectorWrapper connector;
	private Vector<OrganizationConfiguration> organizations = new Vector<OrganizationConfiguration>();
    private ContentManagerTask cmtTask;
    
	public SystemConfiguration(int systemConfiguration, ContentManagerTask cmtTask)
	{
		this.systemConfiguration = systemConfiguration;
        this.cmtTask = cmtTask;
        this.connector = new ConnectorWrapper(cmtTask);
	}
    
    /**
     * This method gets the connection wrapper.
     * 
     * @return The connection wrapper.
     */
    public ConnectorWrapper getConnectorWrapper()
    {
        return connector;
    }

	public void setUp() throws Exception
	{
		int[] organizationConfigurations = Find.match(systemConfiguration, "<SystemConfiguration><organization>");
		for (int i=0; i<organizationConfigurations.length; i++)
		{
			OrganizationConfiguration oc = new OrganizationConfiguration(organizationConfigurations[i], cmtTask);
			organizations.addElement(oc);
		}

		for (int i=0; i<organizations.size(); i++)
		{
			OrganizationConfiguration oc = (OrganizationConfiguration) organizations.elementAt(i);
			oc.setUp();
		}
	}

	public void tearDown() throws Exception
	{
		for (int i=organizations.size()-1; i>=0; i--)
		{
			OrganizationConfiguration oc = (OrganizationConfiguration) organizations.elementAt(i);
			oc.tearDown();
		}
        
        cleanup();
	}
    
    public void cleanup()
    {
        for (int i=organizations.size()-1; i>=0; i--)
        {
            OrganizationConfiguration oc = (OrganizationConfiguration) organizations.elementAt(i);
            oc.cleanup();
        }
    }
    
    /**
     * If value is ending with $ldap.root$, then it will be replaced by the current ldap root. 
     */
    static String getLDAPRootReplacedValue(String value, ConnectorWrapper connector)
    {
        if (value != null && value.endsWith("$ldap.root$"))
        {
            String ldapRoot = connector.getSoapManager().getLdapRoot();
            value = value.substring(0, value.length() - "$ldap.root$".length()) + ldapRoot;
        }
        return value;
    }
}
