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
import com.eibus.directory.soap.DN;
import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

public class RoleConfiguration extends BaseClass
{
	private ConnectorWrapper connector;
	private String roleName;
	private String organizationDN;
	private String organizationalRoleDN;
	private int roleConfiguration;
	private int ldapInsert;  // Points to <Update> tag
	private int ldapInsertEnvelope;
	private int ldapDeleteEnvelope;
	//private boolean deleteEvenIfExists = false;
	private boolean defaultEveryOneRole;
    private ContentManagerTask cmtTask;
    
	public RoleConfiguration(int roleConfiguration, ContentManagerTask cmtTask, String organizationDN, boolean defaultEveryOneRole)
	{
        this.cmtTask = cmtTask;
        this.connector = new ConnectorWrapper(cmtTask);
		this.roleConfiguration = roleConfiguration;
		this.organizationDN = organizationDN;
		this.defaultEveryOneRole = defaultEveryOneRole;
	}

	public void setUp() throws Exception
	{
		this.roleName = Node.getAttribute(roleConfiguration, "name", null);
        //Check whether acl is attached with everyoneInOrganization role , if not then attaching the acl with everyoneIn.. role
		if(defaultEveryOneRole);
        {	
        	if(Find.firstMatch(roleConfiguration,"<role><acl>")==0)
        	{
        		String ldapRoot = cmtTask.getSoapRequestMgr().getLdapRoot();
                int aclnode = Node.getDocument(roleConfiguration).parseString(
        		"<acl name=\"ACL4LDAP\">" +
        		"	<service>cn=LDAP Service,cn=soap nodes,o=system," + ldapRoot  + "</service>" +
        		"	<acobjecttree>" +
        		"		<object>" +
        		"			<object id=\"dn\">" +
        		"				<object id=\"$root$\">" +
        		"					<object id=\"$org$\">" +
        		"						<method id=\"delete\" acl=\"open\"/>" +
        		"					</object>" +
        		"				</object>" +
        		"			</object>" +
        		"		</object>" +
        		"	</acobjecttree>" +
        		"</acl>");
        		Node.appendToChildren(aclnode, roleConfiguration);
        	}
        }
               
		//String deleteAfterwards = Node.getAttribute(roleConfiguration, "deleteEvenIfExists", "false");
		//this.deleteEvenIfExists = deleteAfterwards.equalsIgnoreCase("true");
		
		//Extra space can create problem?
		
		organizationalRoleDN = "cn="+roleName+",cn=organizational roles,"+organizationDN;
		
		ldapInsert = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "Update");
		ldapInsertEnvelope = Node.getRoot(ldapInsert);

		int organizationalRoleTuple = Node.createElement("tuple", ldapInsert);
		int roleNewNode = Node.createElement("new", organizationalRoleTuple);
		int entryNode = Node.createElement("entry", roleNewNode);
		Node.setAttribute(entryNode, "dn", organizationalRoleDN);
		int objectclass = Node.createElement("objectclass", entryNode);
		Node.createTextElement("string", "top", objectclass);
		Node.createTextElement("string", "busorganizationalrole", objectclass);
		Node.createTextElement("string", "busorganizationalobject", objectclass);
		int cn = Node.createElement("cn", entryNode);
		Node.createTextElement("string", roleName, cn);

		int[] roles = Find.match(roleConfiguration, "<role><role>");
		if (roles.length>0)
		{
			int role = Node.createElement("role", entryNode);
			for (int i=0; i<roles.length; i++)
			{
				String subRoleDN = Node.getDataElement(roles[i], null, "");
				assertTrue("System-Configuration: Subrole of role "+roleName+" has no CN or DN", subRoleDN.trim().length()>0);

                //Try to see if ldap root needs to be filled dynamically.
                subRoleDN = SystemConfiguration.getLDAPRootReplacedValue(subRoleDN, connector);                
				// 	We check to see if it is either a CN or a DN. If it is not a DN, it is a CN, and we will
				//  use the context to create a DN out of that.
				if (!DN.isDN(subRoleDN))
				{
					subRoleDN = "cn="+subRoleDN+",cn=organizational roles,"+organizationDN;
				}
				Node.createTextElement("string", subRoleDN, role);
			}
		}

		int[] aclList = Find.match(roleConfiguration, "<role><acl>");
		for (int i=0; i<aclList.length; i++)
		{
			addACL(aclList[i]);
		}
		int ldapDelete = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "DeleteRecursive");
		int duplicateTuple = Node.duplicateAndAppendToChildren(organizationalRoleTuple, organizationalRoleTuple, ldapDelete);
		Node.setName(Find.firstMatch(duplicateTuple, "<tuple><new>"), "old");
        ldapDeleteEnvelope = Node.getRoot(ldapDelete);
	
		int ldapInsertResult = connector.sendAndWait(ldapInsertEnvelope);

		// Check the result of this method.
        int soapFaultNode = Find.firstMatch(ldapInsertResult, "?<SOAP:Fault>");
        if (soapFaultNode != 0)
        {
            assertEquals("System-Configuration: Setup Organization", "Server.Application", Node.getDataElement(soapFaultNode, "SOAP:faultcode", ""));
            assertTrue("System-Configuration: Setup Organization*********", Node.getDataElement(soapFaultNode, "SOAP:faultstring", "").endsWith("already exists on the LDAP server."));
			// If we get to this point, the organization already existed,
			//  so we should make sure that we do not delete this content after the test
			Node.delete(ldapDeleteEnvelope);
			ldapDeleteEnvelope = 0;
        }


		Node.delete(ldapInsertResult);
		Node.delete(ldapInsertEnvelope);
	}

	private void addACL(int aclNode)
	{
		int aclTuple = Node.createElement("tuple", ldapInsert);
		int aclNewNode = Node.createElement("new", aclTuple);
		int entryNode = Node.createElement("entry", aclNewNode);

		String aclName = Node.getAttribute(aclNode, "name", "");
		assertTrue("System-Configuration: Missing acl name", aclName.trim().length()>0);
		
		String aclDN = "cn="+aclName+","+organizationalRoleDN;
		Node.setAttribute(entryNode, "dn", aclDN);
		int objectclass = Node.createElement("objectclass", entryNode);
		Node.createTextElement("string", "top", objectclass);
		Node.createTextElement("string", "busaccesscontrolset", objectclass);
		int cn = Node.createElement("cn", entryNode);
		Node.createTextElement("string", aclName, cn);

		int acobjecttree = Node.getFirstElement(Node.getElement(aclNode, "acobjecttree"));
		if("$org$".equals(Node.getAttribute(Find.firstMatch(acobjecttree,"<object><object><object><object>"),"id")))
				Node.setAttribute(Find.firstMatch(acobjecttree,"<object><object><object><object>"),"id",organizationDN.substring(0, organizationDN.indexOf(',')).toLowerCase());
		int aclObjectTreeNode = Node.createElement("acobjecttree", entryNode);
		Node.createTextElement("string", Node.writeToString(acobjecttree,false), aclObjectTreeNode);
		int acdomaintree = Node.getFirstElement(Node.getElement(aclNode, "acdomaintree"));
		int aclDomainTreeNode = Node.createElement("acdomaintree", entryNode);
		Node.createTextElement("string", Node.writeToString(acdomaintree,false), aclDomainTreeNode);
		String service = Node.getDataElement(aclNode, "service", "");
		int serviceNode = Node.createElement("service", entryNode);
		Node.createTextElement("string", service, serviceNode);

	}

	public void tearDown() throws ExceptionGroup, TimeoutException
	{
		// Now clean up the organization itself, but only if it is required
		if (ldapDeleteEnvelope!=0)
		{
			int ldapDeleteResult = connector.sendAndWait(ldapDeleteEnvelope);			
			Node.delete(ldapDeleteResult);
		}

       cleanup();
    }
   
    public void cleanup() 
    {    
        if (ldapDeleteEnvelope!=0)
        {
            Node.delete(ldapDeleteEnvelope);
            ldapDeleteEnvelope = 0;
        }
	}
}
