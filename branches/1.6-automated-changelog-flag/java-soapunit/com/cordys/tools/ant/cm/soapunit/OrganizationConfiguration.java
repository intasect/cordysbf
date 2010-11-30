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
import com.eibus.connector.nom.SOAPMessage;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

public class OrganizationConfiguration extends BaseClass
{
	private ConnectorWrapper m_connector;
	private int m_organizationConfiguration;
	public String m_organizationName;
	private int m_ldapInsertEnvelope;
	private int m_ldapDeleteEnvelope;
	private Vector<SOAPNodeConfiguration> m_soapnodes = new Vector<SOAPNodeConfiguration>();
	private Vector<MethodSetConfiguration> m_methodsets = new Vector<MethodSetConfiguration>();
	private Vector<UserConfiguration> m_users = new Vector<UserConfiguration>();
	private Vector<RoleConfiguration> m_roles = new Vector<RoleConfiguration>();
	private Vector<ConsortiaConfiguration> m_consortia = new Vector<ConsortiaConfiguration>();
	private boolean m_deleteEvenIfExists = false;
	public  String m_nameOfEveryoneRole;
	private String m_organizationDN;
	private String m_ldapRoot;
    private ContentManagerTask cmtTask;
    
	public OrganizationConfiguration(int organizationConfiguration, ContentManagerTask cmtTask)
	{
        this.cmtTask = cmtTask;
        this.m_connector = new ConnectorWrapper(cmtTask);
		this.m_organizationConfiguration = organizationConfiguration;
        m_ldapRoot = cmtTask.getSoapRequestMgr().getLdapRoot();
	}

	private int createGroupOfNames(String name, String parentDN, int parent)
	{
		int tuple = Node.createElement("tuple", parent);
		int newNode = Node.createElement("new", tuple);
		int entryNode = Node.createElement("entry", newNode);
		Node.setAttribute(entryNode, "dn", "cn="+name+","+parentDN);
		int objectclass = Node.createElement("objectclass", entryNode);
		Node.createTextElement("string", "top", objectclass);
		Node.createTextElement("string", "groupOfNames", objectclass);
		int cn = Node.createElement("cn", entryNode);
		Node.createTextElement("string", name, cn);
		return tuple;
	}

	/**
	 * An new organization contains the following entries.
	 * - organization
	 * - organizational users
	 * - organizational roles
	 * - soap nodes
	 * - method sets
	 * <BR>
	 * - role everyOneIn<organization>
	 * - accesscontrolObject on everyOneIn<organization>
	 * - SYSTEM organization user
	 * 
	 * @param name
	 * @param parentDN
	 * @param parent
	 * @return
	 */
	public void setUp() throws Exception
	{
		int ldapInsert = m_connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "Update");
		m_ldapInsertEnvelope = SOAPMessage.getEnvelopeNode(ldapInsert);

		createOrganization(ldapInsert);		
		createGroupOfNames("soap nodes", m_organizationDN, ldapInsert);
		createGroupOfNames("organizational users", m_organizationDN, ldapInsert);
		createGroupOfNames("organizational roles", m_organizationDN, ldapInsert);
		createGroupOfNames("method sets", m_organizationDN, ldapInsert);
		createMethodSets();
		createSOAPNodes();
		createEveryOneInRole(); // including ACL creation.
		createConsortia();
		createSYSTEMUser();
		createUsers();
		insert();
		
		for (int i=0; i<m_roles.size(); i++)
		{
			RoleConfiguration rc = m_roles.elementAt(i);
			rc.setUp();
		}
		for (int i=0; i<m_users.size(); i++)
		{
			UserConfiguration uc =  m_users.elementAt(i);
			uc.setUp();
		}
		for (int i=0; i<m_methodsets.size(); i++)
		{
			MethodSetConfiguration mc =  m_methodsets.elementAt(i);
			mc.setUp();
		}
		for (int i=0; i<m_consortia.size(); i++)
		{
			ConsortiaConfiguration cc =  m_consortia.elementAt(i);
			cc.setUp();
		}
		for (int i=0; i<m_soapnodes.size(); i++)
		{
			SOAPNodeConfiguration snc =  m_soapnodes.elementAt(i);
			snc.setUp();
		}
		resetLDAPCache();
	}
	
	private void createOrganization(int ldapInsert) throws Exception
	{
		this.m_organizationName = Node.getAttribute(m_organizationConfiguration, "name", null);
		assertNotNull("System-Configuration: Organization name",this.m_organizationName);

		String deleteAfterwards = Node.getAttribute(m_organizationConfiguration, "deleteEvenIfExists", "false");
		this.m_deleteEvenIfExists = deleteAfterwards.equalsIgnoreCase("true");		
		m_organizationDN = "o=" + m_organizationName + "," +m_ldapRoot;
		int organizationTuple = Node.createElement("tuple", ldapInsert);
		int organizationNewNode = Node.createElement("new", organizationTuple);
		int entryNode = Node.createElement("entry", organizationNewNode);
		Node.setAttribute(entryNode, "dn", m_organizationDN);
		int objectclass = Node.createElement("objectclass", entryNode);
		Node.createTextElement("string", "top", objectclass);
		Node.createTextElement("string", "organization", objectclass);
		int o = Node.createElement("o", entryNode);
		Node.createTextElement("string", m_organizationName, o);
		int description = Node.createElement("description", entryNode);
		Node.createTextElement("string", m_organizationName, description);

		int ldapDelete = m_connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "DeleteRecursive");
		int duplicateTuple = Node.duplicateAndAppendToChildren(organizationTuple, organizationTuple, ldapDelete);
		Node.setName(Find.firstMatch(duplicateTuple, "<tuple><new>"), "old");
		m_ldapDeleteEnvelope = Node.getRoot(ldapDelete);
	}
	
	private void createSOAPNodes() throws Exception
	{
		int[] soapNodeConfigurations = Find.match(m_organizationConfiguration, "<organization><soapnode>");
		for (int i=0; i<soapNodeConfigurations.length; i++)
		{
			SOAPNodeConfiguration oc = new SOAPNodeConfiguration(soapNodeConfigurations[i], cmtTask, m_organizationDN);
			m_soapnodes.addElement(oc);
		}
	}
	
	private void createMethodSets() throws Exception
	{
        int[] methodsetConfigurations = Find.match(m_organizationConfiguration, "<organization><methodset>");
		for (int i=0; i<methodsetConfigurations.length; i++)
		{
			MethodSetConfiguration mc = new MethodSetConfiguration(methodsetConfigurations[i], cmtTask, m_organizationDN);
			m_methodsets.addElement(mc);
		}
	}
	
	private void createConsortia() throws Exception
	{
        int[]consortiaConfigurations = Find.match(m_organizationConfiguration, "<organization><consortia>");
		for (int i=0; i<consortiaConfigurations.length; i++)
		{
			ConsortiaConfiguration cc = new ConsortiaConfiguration(consortiaConfigurations[i], cmtTask, m_organizationDN);
			m_consortia.addElement(cc);
		}
	}
	
	
	private void createEveryOneInRole() throws Exception
	{
		m_nameOfEveryoneRole = "everyoneIn"+m_organizationName;
		boolean foundEveryoneRole = false;
		int[] roleConfigurations = Find.match(m_organizationConfiguration, "<organization><role>");
		for (int i=0; i<roleConfigurations.length; i++)
		{
			if (Node.getAttribute(roleConfigurations[i], "name", "").equalsIgnoreCase(m_nameOfEveryoneRole))
			{
				foundEveryoneRole = true;
			}
			RoleConfiguration rc = new RoleConfiguration(roleConfigurations[i], cmtTask, m_organizationDN, false);
			m_roles.addElement(rc);
		}
		if (! foundEveryoneRole)
		{
			int roleNode = Node.createElement("role", m_organizationConfiguration);
			Node.setAttribute(roleNode, "name", m_nameOfEveryoneRole);
			Node.createTextElement("role","cn=everyone,cn=Cordys WCP 1.2," + m_ldapRoot,roleNode);
			RoleConfiguration rc = new RoleConfiguration(roleNode, cmtTask, m_organizationDN, true);
			m_roles.addElement(rc);
		}
	}	
	
	private void createSYSTEMUser() throws Exception
	{
		/*
		 * SYSTEM user should be present in all organizations by default
		 */
		String name_systemUser = "SYSTEM";
		boolean foundSystemUser = false;		
		int userCofiguration_systemUser = Find.firstMatch(m_organizationConfiguration,"<organization><user name=\"SYSTEM\">");
		if(userCofiguration_systemUser != 0)
			foundSystemUser = true;		
		if(!foundSystemUser){
			int userNode = Node.createElement("user", m_organizationConfiguration);
			Node.setAttribute(userNode, "name", name_systemUser);
			/*SYSTEM user in an organization is by default assigned two roles,
			namely everyOneIn<org>, organizationalAdmin
			*/
			//No spaces in DN
			String dn_everyOneRole = "cn="+m_nameOfEveryoneRole+",cn=organizational roles,"+m_organizationDN;
			String dn_orgAdminRole = "cn=organizationalAdmin,cn=Software Package Cordys 1.0,"+m_ldapRoot;
			Node.createTextElement("role", dn_everyOneRole, userNode);
			Node.createTextElement("role", dn_orgAdminRole, userNode);
		}
	}
	
	private void createUsers() throws Exception
	{
		int[] userConfigurations = Find.match(m_organizationConfiguration, "<organization><user>");
		int length_userConfigurations = userConfigurations.length;
		for (int i=0; i<length_userConfigurations; i++)
		{
			String dn_everyOneRole = "cn="+m_nameOfEveryoneRole+",cn=organizational roles,"+m_organizationDN;
			Node.createTextElement("role", dn_everyOneRole, userConfigurations[i]);
			UserConfiguration oc = new UserConfiguration(userConfigurations[i], cmtTask, m_organizationDN);
			m_users.addElement(oc);
		}
	}
	
	private void insert() throws Exception
	{
		int ldapInsertResult = m_connector.sendAndWait(m_ldapInsertEnvelope);
		// Check the result of this method.
        int soapFaultNode = Find.firstMatch(ldapInsertResult, "?<SOAP:Fault>");
        if (soapFaultNode != 0)
        {
            assertEquals("System-Configuration: Setup Organization", "Server.Application", Node.getDataElement(soapFaultNode, "SOAP:faultcode", ""));
            assertTrue("System-Configuration: Setup Organization", Node.getDataElement(soapFaultNode, "SOAP:faultstring", "").endsWith("already exists on the LDAP server."));
			// If we get to this point, the organization already existed,
			//  so we should make sure that we do not delete this content after the test
			if (!m_deleteEvenIfExists)
			{
				Node.delete(m_ldapDeleteEnvelope);
				m_ldapDeleteEnvelope = 0;
			}
        }

		Node.delete(ldapInsertResult);
		Node.delete(m_ldapInsertEnvelope);
	}

	public void tearDown() throws Exception
	{		
		// First, delete all the 'mess' we created inside the organization, in the reverse order
		for (int i=m_soapnodes.size()-1; i>=0; i--)
		{
			SOAPNodeConfiguration snc =  m_soapnodes.elementAt(i);
			snc.tearDown();
		}
		
		for (int i=m_methodsets.size()-1; i>=0; i--)
		{
			MethodSetConfiguration mc =  m_methodsets.elementAt(i);
			mc.tearDown();
		}	
		
		for (int i=m_consortia.size()-1; i>=0; i--)
		{
			ConsortiaConfiguration cc =  m_consortia.elementAt(i);
			cc.tearDown();
		}
		for (int i=m_users.size()-1; i>=0; i--)
		{
			UserConfiguration uc =  m_users.elementAt(i);
			uc.tearDown();
		}
		for (int i=m_roles.size()-1; i>=0; i--)
		{
			RoleConfiguration rc =  m_roles.elementAt(i);
			rc.tearDown();
		}
		// Now clean up the organization itself, but only if it is required
		if (m_ldapDeleteEnvelope!=0)
		{
			int ldapDeleteResult = m_connector.sendAndWait(m_ldapDeleteEnvelope, 2 * 60 * 1000);
			Node.delete(ldapDeleteResult);
		}
		resetLDAPCache();
        
        cleanup();
    }
   
    public void cleanup() 
    {    
        // First, delete all the 'mess' we created inside the organization, in the reverse order
        for (int i=m_soapnodes.size()-1; i>=0; i--)
        {
            SOAPNodeConfiguration snc =  m_soapnodes.elementAt(i);
            snc.cleanup();
        }
        m_soapnodes.clear();
        
        for (int i=m_methodsets.size()-1; i>=0; i--)
        {
            MethodSetConfiguration mc =  m_methodsets.elementAt(i);
            mc.cleanup();
        }   
        m_methodsets.clear();
        
        for (int i=m_consortia.size()-1; i>=0; i--)
        {
            ConsortiaConfiguration cc =  m_consortia.elementAt(i);
            cc.cleanup();
        }
        m_consortia.clear();
        
        for (int i=m_users.size()-1; i>=0; i--)
        {
            UserConfiguration uc =  m_users.elementAt(i);
            uc.cleanup();
        }
        m_users.clear();
        
        for (int i=m_roles.size()-1; i>=0; i--)
        {
            RoleConfiguration rc =  m_roles.elementAt(i);
            rc.cleanup();
        }
        m_roles.clear();
        
        // Now clean up the organization itself, but only if it is required
        if (m_ldapDeleteEnvelope!=0)
        {
            Node.delete(m_ldapDeleteEnvelope);
            m_ldapDeleteEnvelope = 0;
        }
	}
	
	private void resetLDAPCache() throws Exception
	{
		int resetNode =0, resetResponseNode =0;
		try
		{
			String ldapProcessorDN= EIBProperties.getProperty("ldap.soap.processor.dn",null);
			if (ldapProcessorDN != null)
			{
				resetNode =  m_connector.createSOAPMethod("http://schemas.cordys.com/1.0/monitor", "Reset");
				Node.createTextElement("dn",ldapProcessorDN,resetNode);
				resetResponseNode = m_connector.sendAndWait(Node.getParent(resetNode), 2 * 60 * 1000);
			}
		}
		catch(Exception ex)
		{
			throw ex;
		}
		finally
		{
			if (resetNode!= 0)
				Node.delete(Node.getRoot(resetNode));
			if (resetResponseNode !=0)
				Node.delete(Node.getRoot(resetResponseNode));
		}
	 }	
}
