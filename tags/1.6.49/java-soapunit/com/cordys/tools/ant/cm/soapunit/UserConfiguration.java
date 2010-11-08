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
import com.eibus.connector.nom.SOAPMessage;
import com.eibus.directory.soap.DN;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

public class UserConfiguration extends BaseClass
{
    private ConnectorWrapper connector;
	private String organizationDN;
	private int userConfiguration;
	private String userName;
	private String organizationalUserDN;
	private String authenticatedUserDN;
	private int ldapDeleteAuthUserEnvelope;
	private int ldapDeleteOrgUserEnvelope;
	private boolean deleteEvenIfExists = false;
    private ContentManagerTask cmtTask;
    
	public UserConfiguration(int userConfiguration, ContentManagerTask cmtTask, String organizationDN) throws DirectoryException
	{
        this.cmtTask = cmtTask;
        this.connector = new ConnectorWrapper(cmtTask);
		this.userConfiguration = userConfiguration;
		this.organizationDN = organizationDN;
	}

	private void addACL(int aclNode, int ldapInsert)
	{
		int aclTuple = Node.createElement("tuple", ldapInsert);
		int aclNewNode = Node.createElement("new", aclTuple);
		int entryNode = Node.createElement("entry", aclNewNode);

		String aclName = Node.getAttribute(aclNode, "name", "");
		assertTrue("System-Configuration: Missing acl name", aclName.trim().length()>0);

		String aclDN = "cn="+aclName+","+organizationalUserDN;

		Node.setAttribute(entryNode, "dn", aclDN);
		int objectclass = Node.createElement("objectclass", entryNode);
		Node.createTextElement("string", "top", objectclass);
		Node.createTextElement("string", "busaccesscontrolset", objectclass);
		int cn = Node.createElement("cn", entryNode);
		Node.createTextElement("string", aclName, cn);
		int acobjecttree = Node.getFirstElement(Node.getElement(aclNode, "acobjecttree"));
		int aclObjectTreeNode = Node.createElement("acobjecttree", entryNode);
				
		Node.createTextElement("string", Node.writeToString(acobjecttree,false), aclObjectTreeNode);
		int acdomaintree = Node.getFirstElement(Node.getElement(aclNode, "acdomaintree"));
		int aclDomainTreeNode = Node.createElement("acdomaintree", entryNode);
		Node.createTextElement("string", Node.writeToString(acdomaintree,false), aclDomainTreeNode);
		String service = SystemConfiguration.getLDAPRootReplacedValue(Node.getDataElement(aclNode, "service", ""),connector);
		int serviceNode = Node.createElement("service", entryNode);
		Node.createTextElement("string", service, serviceNode);
	}

    public void setUp() throws Exception
	{
		userName = Node.getAttribute(userConfiguration, "name", null);
		assertNotNull("System-Configuration: User name", this.userName);

		String deleteAfterwards = Node.getAttribute(userConfiguration, "deleteEvenIfExists", "false");
		this.deleteEvenIfExists = deleteAfterwards.equalsIgnoreCase("true");
		
		organizationalUserDN = "cn="+userName+",cn=organizational users,"+organizationDN;
		authenticatedUserDN = "cn="+userName+",cn=authenticated users,"+cmtTask.getSoapRequestMgr().getLdapRoot();
		
		int insertAuthUserNode = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "Update");
		int insertAuthUserEnv = SOAPMessage.getEnvelopeNode(insertAuthUserNode);
		int userNewNode, entryNode, objectclass, cn;
		int authenticatedUserTuple = 0;
		/*
		 * authenticated user SYSTEM is created by installer itself.
		 * No need to recreate it and delete it.
		 */

		if(!userName.equals("SYSTEM")){	
			authenticatedUserTuple = Node.createElement("tuple", insertAuthUserNode);
			userNewNode = Node.createElement("new", authenticatedUserTuple);
			entryNode = Node.createElement("entry", userNewNode);
			Node.setAttribute(entryNode, "dn", authenticatedUserDN);
			objectclass = Node.createElement("objectclass", entryNode);
			Node.createTextElement("string", "top", objectclass);
			Node.createTextElement("string", "busauthenticationuser", objectclass);
			cn = Node.createElement("cn", entryNode);
			Node.createTextElement("string", userName, cn);
            int description = Node.createElement("description", entryNode);
            Node.createTextElement("string", userName, description);
			int defaultOrganization = Node.createElement("defaultcontext", entryNode);
			Node.createTextElement("string", organizationDN, defaultOrganization);
			int osidentity = Node.createElement("osidentity", entryNode);
			Node.createTextElement("string", userName, osidentity);

			int ldapDelete = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "DeleteRecursive");
			int duplicateTuple = Node.duplicateAndAppendToChildren(authenticatedUserTuple, authenticatedUserTuple, ldapDelete);
			Node.setName(Find.firstMatch(duplicateTuple, "<tuple><new>"), "old");
			ldapDeleteAuthUserEnvelope = SOAPMessage.getEnvelopeNode(ldapDelete);

			int ldapInsertAuthUserResult = connector.sendAndWait(insertAuthUserEnv);
	
			// Check the result of this method.
	        int soapFaultNode = Find.firstMatch(ldapInsertAuthUserResult, "?<SOAP:Fault>");
	        if (soapFaultNode != 0)
	        {
	            assertEquals("System-Configuration: Setup auth User", "Server.Application", Node.getDataElement(soapFaultNode, "SOAP:faultcode", ""));
	            assertTrue("System-Configuration: Setup auth user", Node.getDataElement(soapFaultNode, "SOAP:faultstring", "").endsWith("already exists on the LDAP server."));
	
				// If we get to this point, the user already existed,
				//  so we should make sure that we do not delete this content after the test
	            if (!deleteEvenIfExists){
					Node.delete(ldapDeleteAuthUserEnvelope);
					ldapDeleteAuthUserEnvelope = 0;
	            }
	        }
		}
		
		

		int ldapInsert = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "Update");
		int ldapInsertEnvelope = Node.getRoot(ldapInsert);

		int organizationalUserTuple = Node.createElement("tuple", ldapInsert);
		userNewNode = Node.createElement("new", organizationalUserTuple);
		entryNode = Node.createElement("entry", userNewNode);
		Node.setAttribute(entryNode, "dn", organizationalUserDN);
		objectclass = Node.createElement("objectclass", entryNode);
		Node.createTextElement("string", "top", objectclass);
		Node.createTextElement("string", "busorganizationaluser", objectclass);
		Node.createTextElement("string", "busorganizationalobject", objectclass);
		cn = Node.createElement("cn", entryNode);
		Node.createTextElement("string", userName, cn);
		int authenticatedUser = Node.createElement("authenticationuser", entryNode);
		Node.createTextElement("string", authenticatedUserDN, authenticatedUser);
		
		int[] roles = Find.match(userConfiguration, "<user><role>");
		if (roles.length>0)
		{			
			int role = Node.createElement("role", entryNode);
			for (int i=0; i<roles.length; i++)
			{
				String subRoleDN = Node.getDataElement(roles[i], null, "");
				assertTrue("System-Configuration: Subrole of user "+userName+" has no CN or DN", subRoleDN.trim().length()>0);

				//Try to see if ldap root needs to be filled dynamically.
                subRoleDN = SystemConfiguration.getLDAPRootReplacedValue(subRoleDN, connector);
                // We check to see if it is either a CN or a DN. If it is not a DN, it is a CN, and we will
				//  use the context to create a DN out of that.
				if (!DN.isDN(subRoleDN))
				{
					subRoleDN = "cn="+subRoleDN+",cn=organizational roles,"+organizationDN;
				}
				Node.createTextElement("string", subRoleDN, role);
			}
		}

		int ldapDelete = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "DeleteRecursive");
		int duplicateTuple = Node.duplicateAndAppendToChildren(organizationalUserTuple, organizationalUserTuple, ldapDelete);
		Node.setName(Find.firstMatch(duplicateTuple, "<tuple><new>"), "old");
		ldapDeleteOrgUserEnvelope = SOAPMessage.getEnvelopeNode(ldapDelete);
		
				
		int[] aclList = Find.match(userConfiguration, "<user><acl>");
		
		for (int i=0; i<aclList.length; i++)
		{
			addACL(aclList[i], ldapInsert);
		}
		int ldapInsertResult = connector.sendAndWait(ldapInsertEnvelope);

		// Check the result of this method.
        int soapFaultNode = Find.firstMatch(ldapInsertResult, "?<SOAP:Fault>");
        if (soapFaultNode != 0)
        {
            assertEquals("System-Configuration: Setup Organization", "Server.Application", Node.getDataElement(soapFaultNode, "SOAP:faultcode", ""));
            assertTrue("System-Configuration: Setup Organization", Node.getDataElement(soapFaultNode, "SOAP:faultstring", "").endsWith("already exists on the LDAP server."));

			// If we get to this point, the user already existed,
			//  so we should make sure that we do not delete this content after the test
            if (!deleteEvenIfExists){
				Node.delete(ldapDeleteAuthUserEnvelope);
				ldapDeleteAuthUserEnvelope = 0;
            }
        }
		Node.delete(ldapInsertResult);
		Node.delete(ldapInsertEnvelope);
	}

	public void tearDown() throws ExceptionGroup, TimeoutException
	{
		// Now clean up the user itself, but only if it is required
        // delete orgnizational user first after that delete auhtenicated user 
        if (ldapDeleteOrgUserEnvelope!=0)
        {
            int ldapDeleteResult = connector.sendAndWait(ldapDeleteOrgUserEnvelope);            
            Node.delete(ldapDeleteResult);
        }
        if (ldapDeleteAuthUserEnvelope!=0)
        {       
            int ldapDeleteResult = connector.sendAndWait(ldapDeleteAuthUserEnvelope);           
            Node.delete(ldapDeleteResult);
        }        
		
        cleanup();
    }
    
    public void cleanup() 
    {
		// delete orgnizational user first after that delete auhtenicated user 
		if (ldapDeleteOrgUserEnvelope!=0)
		{
			Node.delete(ldapDeleteOrgUserEnvelope);
            ldapDeleteOrgUserEnvelope = 0;
		}
		if (ldapDeleteAuthUserEnvelope!=0)
		{		
			Node.delete(ldapDeleteAuthUserEnvelope);
            ldapDeleteAuthUserEnvelope = 0;
		}
	}
}
