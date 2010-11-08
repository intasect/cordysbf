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

import com.cordys.tools.ant.cm.ContentManagerTask;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * @author T.Ramesh
 * August 2, 2006
 * 
 *	 Creates the custom consortia requird for the  junit test case and 
 *   Remove the consortia on tearDown.
 * 
 */
public class ConsortiaConfiguration extends BaseClass
{
	private ConnectorWrapper m_connector;
	private String m_organizationDN;
	private int m_consortiaConfiguration;
	private String m_consortiaName;
	private String m_consortiaUser;
	private int m_ldapInsertWorkgroupUserNode = 0;
	private int m_ldapInsertWorkGroupNode = 0;
	private int m_ldapInsertConsortiaUserNode = 0;
	private int m_ldapInsertConsortiaNode= 0;
	private int m_ldapDeleteWorkgroupUserNode = 0;
	private int m_ldapDeleteWorkGroupNode = 0;
	private int m_ldapDeleteConsortiaUserNode = 0;
	private int m_ldapDeleteConsortiaNode = 0;
	private static final String LDAP_ROOT = EIBProperties.getProperty("ldap.root","cn=cordys,o=vanenburg.com");
	private static final String LDAPSOAPNODEDN = "cn=LDAP Service,cn=soap nodes,o=system," + LDAP_ROOT;
	private String m_consortiaNamespace = "http://schemas.cordys.com/1.0/virtualconsortium";
    private ContentManagerTask cmtTask;
    
	public ConsortiaConfiguration(int consortiaConfiguration, ContentManagerTask cmtTask, String organizationDN)
	{
        this.cmtTask = cmtTask;
        this.m_connector = new ConnectorWrapper(cmtTask);
		this.m_consortiaConfiguration = consortiaConfiguration;
		this.m_organizationDN = organizationDN;
	}
    
    /**
     * This method gets the content manager task.
     * 
     * @return The content manager task.
     */
    public ContentManagerTask getContentManagerTask()
    {
        return cmtTask;
    }
	
	public void setUp() throws Exception
	{	
		int ldapUpdateResult =0;
		try
		{
			m_consortiaName = Find.firstDataWithDefault(m_consortiaConfiguration,"<consortia><name>",null);
			m_ldapInsertConsortiaNode = m_connector.createSOAPMessage( LDAPSOAPNODEDN, null);
			m_ldapDeleteConsortiaNode = m_connector.createSOAPMessage(LDAPSOAPNODEDN, null);
			m_ldapInsertConsortiaUserNode = m_connector.createSOAPMessage(LDAPSOAPNODEDN, null);
			m_ldapDeleteConsortiaUserNode = m_connector.createSOAPMessage(LDAPSOAPNODEDN, null);
			m_ldapInsertWorkGroupNode = m_connector.createSOAPMessage(LDAPSOAPNODEDN, null);
			m_ldapDeleteWorkGroupNode = m_connector.createSOAPMessage(LDAPSOAPNODEDN, null);
			m_ldapInsertWorkgroupUserNode = m_connector.createSOAPMessage(LDAPSOAPNODEDN, null);
			m_ldapDeleteWorkgroupUserNode= m_connector.createSOAPMessage(LDAPSOAPNODEDN, null);
			createConsortia();
			m_consortiaUser = Find.firstDataWithDefault(m_consortiaConfiguration,"<consortia><consortiauser><name>",null);
			createConsortiaUser();
			int[] workgroupNodes = Find.match(m_consortiaConfiguration,"?<consortia><workgroup>");
			for (int i=0;i<workgroupNodes.length;i++)
			{
				String name = Find.firstDataWithDefault(workgroupNodes[i],"<workgroup><name>",null);
				createWorkgroup(name);
				createWorkgroupUser(name);	
			}
			ldapUpdateResult = m_connector.sendAndWait(Node.getRoot(m_ldapInsertConsortiaNode));
			checkforSoapFault(ldapUpdateResult);
			ldapUpdateResult = m_connector.sendAndWait(Node.getRoot(m_ldapInsertConsortiaUserNode));
			checkforSoapFault(ldapUpdateResult);
			ldapUpdateResult = m_connector.sendAndWait(Node.getRoot(m_ldapInsertWorkGroupNode));
			checkforSoapFault(ldapUpdateResult);
			ldapUpdateResult = m_connector.sendAndWait(Node.getRoot(m_ldapInsertWorkgroupUserNode));
			checkforSoapFault(ldapUpdateResult);
		}
		catch(Exception ex)
		{
			throw ex;
		}
		finally
		{
			Node.delete(Node.getRoot(m_ldapInsertConsortiaNode));
			Node.delete(Node.getRoot(m_ldapInsertConsortiaUserNode));
			Node.delete(Node.getRoot(m_ldapInsertWorkgroupUserNode));
			Node.delete(Node.getRoot(m_ldapInsertWorkGroupNode));
			Node.delete(ldapUpdateResult);
		}
    }
	
	/**
	 * Creates the consortia.
	 * <UpdateConsortium xmlns="http://schemas.cordys.com/1.0/virtualconsortium">
     * 	<tuple>
     *  <new>
     *     <consortium>
     *      <name>MyConsortium</name>
     *       <description>This is my sample consortium</description>
     *       <organization>o=system,cn=cordys,o=vanenburg.com</organization>
     *     </consortium>
     *  </new>
     *	</tuple>
	 *</UpdateConsortium> 
	 */
	private void createConsortia()
	{
		int ldapMethod =Node.createElement("UpdateConsortium", m_ldapInsertConsortiaNode);
		Node.setAttribute(ldapMethod, "xmlns", m_consortiaNamespace);
		int tuple = Node.createElement("tuple", ldapMethod);
		int newNode = Node.createElement("new", tuple);
		int entryNode = Node.createElement("consortium", newNode);
		Node.createTextElement("name", m_consortiaName,entryNode);
		Node.createTextElement("description", "Created by System Configuration", entryNode);
		Node.createTextElement("organization", m_organizationDN, entryNode);
		ldapMethod =Node.createElement("UpdateConsortium", m_ldapDeleteConsortiaNode);
		Node.setAttribute(ldapMethod, "xmlns", m_consortiaNamespace);
		tuple = Node.createElement("tuple", ldapMethod);
		newNode = Node.createElement("old", tuple);
		entryNode = Node.createElement("consortium", newNode);
		Node.createTextElement("name", m_consortiaName,entryNode);
		Node.createTextElement("description", "Created by System Configuration", entryNode);
		Node.createTextElement("organization", m_organizationDN, entryNode);
	}
	
	/**
	 * Creates the consortia User
	 * 
	 * <UpdateConsortiumUsers xmlns="http://schemas.cordys.com/1.0/virtualconsortium">
	      <tuple>
	        <new>
	          <consortiumusers>
	            <name>system</name>
	            <userdn>cn=system,cn=organizational users,o=system,cn=cordys,o=vanenburg.com</userdn>
	            <consortium>MyConsortium</consortium>
	            <isAdministrator>true</isAdministrator>
	          </consortiumusers>
	        </new>
	      </tuple>
		</UpdateConsortiumUsers>
	 */
	private void createConsortiaUser() throws Exception
	{
		String userDN = "cn=" + m_consortiaUser + ",cn=organizational users," + m_organizationDN;
		int ldapMethod =Node.createElement("UpdateConsortiumUsers", m_ldapInsertConsortiaUserNode);
		Node.setAttribute(ldapMethod, "xmlns", m_consortiaNamespace);
		int tuple = Node.createElement("tuple", ldapMethod);
		int newNode = Node.createElement("new", tuple);
		int entryNode = Node.createElement("consortiumusers", newNode);
		Node.createTextElement("name", m_consortiaUser,entryNode);
		Node.createTextElement("userdn", userDN, entryNode);
		Node.createTextElement("consortium", m_consortiaName, entryNode);
		Node.createTextElement("isAdministrator", "true", entryNode);
		ldapMethod =Node.createElement("UpdateConsortiumUsers", m_ldapDeleteConsortiaUserNode);
		Node.setAttribute(ldapMethod, "xmlns", m_consortiaNamespace);
		tuple = Node.createElement("tuple", ldapMethod);
		newNode = Node.createElement("old", tuple);
		entryNode = Node.createElement("consortiumusers", newNode);
		Node.createTextElement("name", m_consortiaUser,entryNode);
		Node.createTextElement("userdn", userDN, entryNode);
		Node.createTextElement("consortium", m_consortiaName, entryNode);
		Node.createTextElement("isAdministrator", "true", entryNode);
	}
	
	
	/**
	 * Creates the consortia User
	 * 
	 * <UpdateWorkgroupUsers xmlns="http://schemas.cordys.com/1.0/virtualconsortium">
	      <tuple>
	        <new>
	          <workgroupusers>
	            <name>system</name>
	            <workgroup>Workgroup</workgroup>
	            <consortium>MyConsortium</consortium>
	            <userdn>cn=system,cn=consortium users,cn=MyConsortium,cn=consortia,cn=cordys,o=vanenburg.com</userdn>
	            <isModerator>true</isModerator>
	          </workgroupusers>
	        </new>
	      </tuple>
		</UpdateWorkgroupUsers>
	 */
	private void createWorkgroupUser( String workgroupName) throws Exception
	{
		String userDN = "cn=" + m_consortiaUser + ",cn=consortium users," + "cn=" + m_consortiaName + ",cn=consortia," + LDAP_ROOT;
		int ldapMethod =Node.createElement("UpdateWorkgroupUsers", m_ldapInsertWorkgroupUserNode);
		Node.setAttribute(ldapMethod, "xmlns", m_consortiaNamespace);
		int tuple = Node.createElement("tuple", ldapMethod);
		int newNode = Node.createElement("new", tuple);
		int entryNode = Node.createElement("workgroupusers", newNode);
		Node.createTextElement("name", m_consortiaUser,entryNode);
		Node.createTextElement("workgroup", workgroupName, entryNode);
		Node.createTextElement("consortium", m_consortiaName, entryNode);
		Node.createTextElement("userdn", userDN,entryNode);
		Node.createTextElement("isAdministrator", "true", entryNode);
		ldapMethod =Node.createElement("UpdateWorkgroupUsers", m_ldapDeleteWorkgroupUserNode);
		Node.setAttribute(ldapMethod, "xmlns", m_consortiaNamespace);
		tuple = Node.createElement("tuple", ldapMethod);
		newNode = Node.createElement("old", tuple);
		entryNode = Node.createElement("workgroupusers", newNode);
		Node.createTextElement("name", m_consortiaUser,entryNode);
		Node.createTextElement("workgroup", workgroupName, entryNode);
		Node.createTextElement("consortium", m_consortiaName, entryNode);
		Node.createTextElement("userdn", userDN,entryNode);
		Node.createTextElement("isAdministrator", "true", entryNode);
	}
	/**
	 * Creates the workgroup
	 * 
	 * <UpdateWorkgroup xmlns="http://schemas.cordys.com/1.0/virtualconsortium">
      	<tuple>
        <new>
          <workgroup>
            <name>Workgroup</name>
            <description>This is sample work group</description>
            <consortium>MyConsortium</consortium>
          </workgroup>
        </new>
      	</tuple>
		</UpdateWorkgroup>
	 */
	private void createWorkgroup(String workgroupName) throws Exception
	{
		int ldapMethod =Node.createElement("UpdateWorkgroup", m_ldapInsertWorkGroupNode);
		Node.setAttribute(ldapMethod, "xmlns", m_consortiaNamespace);
		int tuple = Node.createElement("tuple", ldapMethod);
		int newNode = Node.createElement("new", tuple);
		int entryNode = Node.createElement("workgroup", newNode);
		Node.createTextElement("name", workgroupName,entryNode);
		Node.createTextElement("description", "Created by System Configuration", entryNode);
		Node.createTextElement("consortium", m_consortiaName, entryNode);
		ldapMethod =Node.createElement("UpdateWorkgroup", m_ldapDeleteWorkGroupNode);
		Node.setAttribute(ldapMethod, "xmlns", m_consortiaNamespace);
		tuple = Node.createElement("tuple", ldapMethod);
		newNode = Node.createElement("old", tuple);
		entryNode = Node.createElement("workgroup", newNode);
		Node.createTextElement("name", workgroupName,entryNode);
		Node.createTextElement("description", "Created by System Configuration", entryNode);
		Node.createTextElement("consortium", m_consortiaName, entryNode);
	}
	
	
	public void tearDown() throws Exception
	{
	   int ldapDeleteResponse = 0; 
	   try
	   {
		   ldapDeleteResponse = m_connector.sendAndWait(Node.getRoot(m_ldapDeleteWorkgroupUserNode));
		   checkforSoapFault(ldapDeleteResponse);
		   ldapDeleteResponse = m_connector.sendAndWait(Node.getRoot(m_ldapDeleteWorkGroupNode));
		   checkforSoapFault(ldapDeleteResponse);
		   ldapDeleteResponse = m_connector.sendAndWait(Node.getRoot(m_ldapDeleteConsortiaUserNode));
		   checkforSoapFault(ldapDeleteResponse);		 
		   ldapDeleteResponse = m_connector.sendAndWait(Node.getRoot(m_ldapDeleteConsortiaNode));
		   checkforSoapFault(ldapDeleteResponse);
	   }
	   catch(Exception ex)
	   {
		  throw ex;
	   }
	   finally
	   {
		   Node.delete(ldapDeleteResponse);
	   }

       cleanup();
	}
   
	public void cleanup() 
	{    
        if (m_ldapDeleteConsortiaNode != 0) {
            Node.delete(Node.getRoot(m_ldapDeleteConsortiaNode));
            m_ldapDeleteConsortiaNode = 0;
        }
       
        if (m_ldapDeleteConsortiaUserNode != 0) {
            Node.delete(Node.getRoot(m_ldapDeleteConsortiaUserNode));
            m_ldapDeleteConsortiaUserNode = 0;
        }
        
        if (m_ldapDeleteWorkgroupUserNode != 0) {
            Node.delete(Node.getRoot(m_ldapDeleteWorkgroupUserNode));
            m_ldapDeleteWorkgroupUserNode = 0;
        }
        
        if (m_ldapDeleteWorkGroupNode != 0) {
            Node.delete(Node.getRoot(m_ldapDeleteWorkGroupNode));
            m_ldapDeleteWorkGroupNode = 0;
        }
	}
		
	private  void checkforSoapFault(int response) throws Exception
	{
		try
		{
			int  faultNode =Find.firstMatch(response,"?<SOAP:Fault>");
			if (faultNode != 0)
	        {
	            assertTrue("System-Configuration: Setup Consortia:" + Node.writeToString(faultNode, true) , Node.getDataElement(faultNode, "SOAP:faultstring", "").endsWith("already exists on the LDAP server."));
	        }	
		}
		catch(Exception ex)
		{
			throw ex;
		}
	}
}
