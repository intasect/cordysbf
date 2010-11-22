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
import com.eibus.directory.soap.DN;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

public class SOAPNodeConfiguration extends BaseClass
{
    private ConnectorWrapper connector;

	private String organizationDN;
	private String soapNodeName;
	private String soapNodeDN;
	private Vector<SOAPProcessorConfiguration> soapprocess = new Vector<SOAPProcessorConfiguration>();
	private int soapnodeConfiguration;
	private int ldapInsert;
	private int ldapInsertEnvelope;
	private int ldapDeleteEnvelope;
	private boolean deleteEvenIfExists = false;
	private SOAPProcessorConfiguration spc=null;
    private ContentManagerTask cmtTask;
    
	public SOAPNodeConfiguration(int soapnodeConfiguration, ContentManagerTask cmtTask, String organizationDN)
	{
        this.cmtTask = cmtTask;
        this.connector = new ConnectorWrapper(cmtTask);
		this.soapnodeConfiguration = soapnodeConfiguration;
		this.organizationDN = organizationDN;
	}

	public void setUp()
		throws Exception
	{
		this.soapNodeName = Node.getAttribute(soapnodeConfiguration, "name", null);
		assertTrue("SystemConfiguration: soap node has no name", this.soapNodeName!=null);

		String deleteAfterwards = Node.getAttribute(soapnodeConfiguration, "deleteEvenIfExists", "false");
		this.deleteEvenIfExists = deleteAfterwards.equalsIgnoreCase("true");

		this.soapNodeDN = "cn="+soapNodeName+",cn=soap nodes,"+organizationDN;

		ldapInsert = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "Update");
		ldapInsertEnvelope = Node.getRoot(ldapInsert);

		int soapNodeTuple = Node.createElement("tuple", ldapInsert);
		int soapNodeNewNode = Node.createElement("new", soapNodeTuple);
		int entryNode = Node.createElement("entry", soapNodeNewNode);
		Node.setAttribute(entryNode, "dn", soapNodeDN);
		int objectclass = Node.createElement("objectclass", entryNode);
		Node.createTextElement("string", "top", objectclass);
		Node.createTextElement("string", "bussoapnode", objectclass);
		int cn = Node.createElement("cn", entryNode);
		Node.createTextElement("string", soapNodeName, cn);

		int methodSetsNode = Node.createElement("busmethodsets", entryNode);
		String[] methodSets = Find.data(soapnodeConfiguration, "<soapnode><methodset>");
		for (int i=0; i<methodSets.length; i++)
		{
			String methodSetName = methodSets[i];
			
			//See if ldap root needs to be filled dynamically.
			methodSetName = SystemConfiguration.getLDAPRootReplacedValue(methodSetName, connector);
            
			if (!DN.isDN(methodSetName))
			{
				// Assume it is CN, and take it from the organization
				methodSetName = "cn="+methodSetName+",cn=method sets,"+organizationDN;
			}
			Node.createTextElement("string", methodSetName, methodSetsNode);
		}
		int labeledURIsNode = Node.createElement("labeleduri", entryNode);
		String[] labeledURIs = Find.data(soapnodeConfiguration, "<soapnode><labeleduri>");
		for (int i=0; i<labeledURIs.length; i++)
		{
			Node.createTextElement("string", labeledURIs[i], labeledURIsNode);
		}

		int ldapDelete = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "DeleteRecursive");
		int duplicateTuple = Node.duplicateAndAppendToChildren(soapNodeTuple, soapNodeTuple, ldapDelete);
		Node.setName(Find.firstMatch(duplicateTuple, "<tuple><new>"), "old");
		ldapDeleteEnvelope = Node.getRoot(ldapDelete);
		
		
		int[] soapProcessorConfigurations = Find.match(soapnodeConfiguration,"<soapnode><soapprocessor>");
		
		int ldapInsertResult = connector.sendAndWait(ldapInsertEnvelope);
		
		for (int i=0; i<soapProcessorConfigurations.length; i++)
		{
			spc = new SOAPProcessorConfiguration(soapProcessorConfigurations[i], cmtTask, soapNodeDN, this.soapNodeName);
			soapprocess.addElement(spc);
			spc.setUp();
			
		}
		// Check the result of this method.
        int soapFaultNode = Find.firstMatch(ldapInsertResult, "?<SOAP:Fault>");
        if (soapFaultNode != 0)
        {
            assertEquals("System-Configuration: Setup Organization", "Server.Application", Node.getDataElement(soapFaultNode, "SOAP:faultcode", ""));
            assertTrue("System-Configuration: Setup Organization", Node.getDataElement(soapFaultNode, "SOAP:faultstring", "").endsWith("already exists on the LDAP server."));

			// If we get to this point, the organization already existed,
			//  so we should make sure that we do not delete this content after the test
			if (!deleteEvenIfExists)
			{
				Node.delete(ldapDeleteEnvelope);
				ldapDeleteEnvelope = 0;
			}
        }
        
        
	}	
	public void tearDown() throws Exception
	{
		// We have to delete all the soap processors created by us.
		for (int i=soapprocess.size()-1; i>=0; i--)
		{
			SOAPProcessorConfiguration spc = (SOAPProcessorConfiguration) soapprocess.elementAt(i);
			spc.tearDown();
		}		
		if (ldapDeleteEnvelope!=0)
		{
			int ldapDeleteResult = connector.sendAndWait(ldapDeleteEnvelope);
			Node.delete(ldapDeleteResult);
		}
        
        cleanup();
    }
   
    public void cleanup() 
    {     
        for (int i=soapprocess.size()-1; i>=0; i--)
        {
            SOAPProcessorConfiguration spc = (SOAPProcessorConfiguration) soapprocess.elementAt(i);
            spc.cleanup();
        }   
        soapprocess.clear();
        
        if (ldapDeleteEnvelope!=0)
        {
            Node.delete(ldapDeleteEnvelope);
            ldapDeleteEnvelope = 0;
        }        
	}
}
