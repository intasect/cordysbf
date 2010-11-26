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

import java.util.Random;

import com.cordys.tools.ant.cm.ContentManagerTask;
import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;
import com.eibus.util.PortScanner;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

public class ConnectionpointConfiguration extends BaseClass
{
	private ConnectorWrapper connector;
	private String connectionPointName;
	private String soapProcessorDN;
	private String soapProcessorCN;
	private String connectionPointDN;
	private int connectionpointConfiguration;
	private int ldapInsert;
	private int ldapInsertEnvelope;
	private int ldapDeleteEnvelope;
	private boolean deleteEvenIfExists = false;
	private String point;	
	private Random rn=null;
    private ContentManagerTask cmtTask;

	public ConnectionpointConfiguration(int connectionpointConfiguration, ContentManagerTask cmtTask, String soapProcessorDN, String soapProcessorCN)
	{
        this.cmtTask = cmtTask;
		this.connector = new ConnectorWrapper(cmtTask);
		this.connectionpointConfiguration = connectionpointConfiguration;
		this.soapProcessorDN = soapProcessorDN;
		this.soapProcessorCN = soapProcessorCN;
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
	
	private int getPrtNumber() {
		rn = new Random();
		int p_Number = rn.nextInt(65535);
		if (p_Number < 5000)
			getPrtNumber();
		//System.out.println("Port Number:::  " + p_Number);
		return p_Number;
	}
	public void setUp() throws Exception{
		
		
		this.connectionPointName = Node.getAttribute(connectionpointConfiguration, "name", null);
		//this.assertTrue("SystemConfiguration: ConnectionPoint has no name", connectionPointName!=null);
		/*
		 * No need throw exception. We can follow default naming convention for soap processor name.
		 */
		if(this.connectionPointName == null)
			this.connectionPointName = "connectionpoint-"+this.soapProcessorCN;
		
		

		String deleteAfterwards = Node.getAttribute(connectionpointConfiguration, "deleteEvenIfExists", "false");
		this.deleteEvenIfExists = deleteAfterwards.equalsIgnoreCase("true");

		this.connectionPointDN = "cn="+connectionPointName+","+soapProcessorDN;

		ldapInsert = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "Update");
		ldapInsertEnvelope = Node.getRoot(ldapInsert);

		int connectionPointTuple = Node.createElement("tuple", ldapInsert);
		int connectionPointNewNode = Node.createElement("new", connectionPointTuple);
		int entryNode = Node.createElement("entry", connectionPointNewNode);
		
		Node.setAttribute(entryNode, "dn", connectionPointDN);
		int objectclass = Node.createElement("objectclass", entryNode);
		Node.createTextElement("string", "top", objectclass);
		Node.createTextElement("string", "busconnectionpoint", objectclass);
		int cn = Node.createElement("cn", entryNode);
		Node.createTextElement("string", connectionPointName, cn);
		int labeleduri = Node.createElement("labeleduri", entryNode);
		try{
			boolean portinuse = true;
			long cTime = System.currentTimeMillis(); 
			while(portinuse){
				int p = getPrtNumber();
				point = "socket://"+SOAPProcessorConfiguration.compNode+":"+p;
				portinuse = PortScanner.isPortInUse(point);
				if(System.currentTimeMillis()-cTime > 30000)
					break;
		   }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		Node.createTextElement("string",point,labeleduri);
			
		int busprivatepoint = Node.createElement("busprivatepoint",entryNode);
		Node.createTextElement("string","false",busprivatepoint);
		
		int ldapDelete = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "DeleteRecursive");
		int duplicateTuple = Node.duplicateAndAppendToChildren(connectionPointTuple, connectionPointTuple, ldapDelete);
		Node.setName(Find.firstMatch(duplicateTuple, "<tuple><new>"), "old");
		ldapDeleteEnvelope = Node.getRoot(ldapDelete);
		
		int ldapInsertResult = connector.sendAndWait(ldapInsertEnvelope);

		// Check the result of this method.
        int soapFaultNode = Find.firstMatch(ldapInsertResult, "?<SOAP:Fault>");
        if (soapFaultNode != 0)
        {
            assertEquals("System-Configuration: Setup Organization", "Server.Application", Node.getDataElement(soapFaultNode, "SOAP:faultcode", ""));
            assertTrue("System-Configuration: Setup ConnectionPoint", Node.getDataElement(soapFaultNode, "SOAP:faultstring", "").endsWith("already exists on the LDAP server."));

			// If we get to this point, the organization already existed,
			//  so we should make sure that we do not delete this content after the test
			if (!deleteEvenIfExists)
			{
				Node.delete(ldapDeleteEnvelope);
				ldapDeleteEnvelope = 0;
			}
        }		
	}
	public void tearDown() throws ExceptionGroup, TimeoutException
	{
		// Now clean up the connectionpoint itself, but only if it is required
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