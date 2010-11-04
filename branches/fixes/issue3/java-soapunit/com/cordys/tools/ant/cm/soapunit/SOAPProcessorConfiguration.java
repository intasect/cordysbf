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
import com.eibus.util.Base64;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

public class SOAPProcessorConfiguration extends BaseClass
{
    private ConnectorWrapper connector;
	private String soapProcessorName;
	private String soapProcessorDN;
	private String soapNodeDN;
	private String soapNodeCN;
	private int soapprocessorConfiguration;
	private int ldapInsert;
	private int ldapInsertEnvelope;
	private int ldapDeleteEnvelope;
	private boolean deleteEvenIfExists = false;
    private ConnectionpointConfiguration cpc;
    private String status;
    public static String compNode;
    private String st;
    private ContentManagerTask cmtTask;    
    
    public SOAPProcessorConfiguration(int soapprocessorConfiguration, ContentManagerTask cmtTask, String soapNodeDN, String soapNodeCN)
	{
        this.cmtTask = cmtTask;
        this.connector = new ConnectorWrapper(cmtTask);
		this.soapprocessorConfiguration = soapprocessorConfiguration;
		this.soapNodeDN = soapNodeDN;
		this.soapNodeCN = soapNodeCN;
	}
	
	public void setUp()
		throws Exception
	{
		this.soapProcessorName = Node.getAttribute(soapprocessorConfiguration, "name", null);
		//this.assertTrue("SystemConfiguration: soap processor has no name", soapProcessorName!=null);
		/*
		 * No need throw exception. We can follow default naming convention for soap processor name.
		 */
		if(this.soapProcessorName == null)
			this.soapProcessorName = "soapProcessor@"+this.soapNodeCN;
		
		this.soapProcessorDN = "cn="+soapProcessorName+","+soapNodeDN;
		
		String deleteAfterwards = Node.getAttribute(soapprocessorConfiguration, "deleteEvenIfExists", "false");
		this.deleteEvenIfExists = deleteAfterwards.equalsIgnoreCase("true");

		ldapInsert = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "Update");
		ldapInsertEnvelope = Node.getRoot(ldapInsert);

		int soapProcessorTuple = Node.createElement("tuple", ldapInsert);
		int soapProcessorNewNode = Node.createElement("new", soapProcessorTuple);
		int entryNode = Node.createElement("entry", soapProcessorNewNode);
		Node.setAttribute(entryNode, "dn", soapProcessorDN);
		int objectclass = Node.createElement("objectclass", entryNode);
		Node.createTextElement("string", "top", objectclass);
		Node.createTextElement("string", "bussoapprocessor", objectclass);
		int cn = Node.createElement("cn", entryNode);
		Node.createTextElement("string", soapProcessorName, cn);
		int computer = Node.createElement("computer",entryNode);
		compNode = Node.getDataWithDefault(Find.firstMatch(soapprocessorConfiguration,"?<soapprocessor><computer>"),EIBProperties.getProperty("bus.ldap.processor.host"));
		Node.createTextElement("string",compNode,computer);
		int autostart = Node.createElement("automaticstart",entryNode);
		Node.createTextElement("string","false",autostart);//make this to read from file
		int configuration = Find.firstMatch(soapprocessorConfiguration,"?<soapprocessor><configurations>");
		
		int dsoNode = Find.firstMatch(configuration,"?<configurations><configuration><dso>");
		if(dsoNode!=0){
			String passwordStr = Node.getAttribute(dsoNode,"password","");
			if(!passwordStr.equals("")){
				String enc_pwd = Base64.encode(passwordStr);
				Node.setAttribute(Find.firstMatch(configuration,"?<configurations><configuration><dso>"), "password", enc_pwd);
			}	
		}
		int bussoapprocessconfig = Node.createElement("bussoapprocessorconfiguration",entryNode);
		Node.createTextElement("string",Node.writeToString(configuration, true),bussoapprocessconfig);
		int description = Node.createElement("description", entryNode);
		Node.createTextElement("string","TestSoapProcessor",description);
				
		int ldapDelete = connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "DeleteRecursive");
		int duplicateTuple = Node.duplicateAndAppendToChildren(soapProcessorTuple, soapProcessorTuple, ldapDelete);
		Node.setName(Find.firstMatch(duplicateTuple, "<tuple><new>"), "old");
		ldapDeleteEnvelope = Node.getRoot(ldapDelete);
		
		int ldapInsertResult = connector.sendAndWait(ldapInsertEnvelope);
		// Check the result of this method.
        int soapFaultNode = Find.firstMatch(ldapInsertResult, "?<SOAP:Fault>");
        if (soapFaultNode != 0)
        {
            assertEquals("System-Configuration: Setup SOAPProcessor", "Server.Application", Node.getDataElement(soapFaultNode, "SOAP:faultcode", ""));
            assertTrue("System-Configuration: Setup SOAPProcessor", Node.getDataElement(soapFaultNode, "SOAP:faultstring", "").endsWith("already exists on the LDAP server."));

			// If we get to this point, the organization already existed,
			//  so we should make sure that we do not delete this content after the test
			if (!deleteEvenIfExists)
			{
				Node.delete(ldapDeleteEnvelope);
				ldapDeleteEnvelope = 0;
			}
        }
       
        /*connectionpoint configuration*/
        
        int connectionpointConfiguration = Find.firstMatch(soapprocessorConfiguration,"<soapprocessor><connectionpoint>");
	    cpc = new ConnectionpointConfiguration(connectionpointConfiguration, cmtTask, soapProcessorDN, this.soapProcessorName);
        cpc.setUp();
        int tryCount=0;
        System.out.println("Starting SOAPProcessor \""+soapProcessorName+"\"");
        while(tryCount<3){
        	st = startProcessor();
        	if(!st.equals("Started")){
        		System.out.println("SOAPProcessor \""+soapProcessorName+"\"  Not Started on try "+ tryCount);
        		cpc.tearDown();
	        	cpc.setUp();
	        	tryCount++;	        	
        	}
        	else break;	
        	
        }
        	System.out.println("SOAPProcessor\" "+soapProcessorDN+"\"  status ::: "+st);
     }

	private String startProcessor()
    {
        int method = 0;
        int response = 0;
		try
        {
            method = connector.createSOAPMethod("http://schemas.cordys.com/1.0/monitor", "Start");
            Node.createTextElement("dn", soapProcessorDN, method);
            response = connector.sendAndWait(Node.getParent(method));
            status = getSOAPProcessorStatus(connector, soapProcessorDN);
            long startingTime = System.currentTimeMillis();
            if (!status.equals("Started"))
            {
            	while (System.currentTimeMillis() - startingTime < 45000)
                {
            		Thread.sleep(3000);
            		status = getSOAPProcessorStatus(connector, soapProcessorDN);
            		if(status.equals("Started") || status.equalsIgnoreCase("Connection Error"))
            			return status;
            	}
            }
		}
		catch(Exception e)
        {
			e.printStackTrace();
		}
        finally
        {
            Node.delete(Node.getRoot(method));
            Node.delete(Node.getRoot(response));
        }
		return status;
	}
		
	public String getSOAPProcessorStatus(ConnectorWrapper connector,String soapProcessorDN) throws Exception {
		int method = connector.createSOAPMethod("http://schemas.cordys.com/1.0/monitor", "List");
		int response = connector.sendAndWait(Node.getParent(method));
		int responseSOAPBody = Find.firstMatch(response, "?<SOAP:Body>");
		String status = "";
		int[] soapProcessors = Find.match(responseSOAPBody,"?<tuple><old><workerprocess><name>");
		for (int i = 0; i < soapProcessors.length; i++) {
			if (Node.getData(soapProcessors[i]).equals(soapProcessorDN)) {
				status = Node.getData(Find.firstMatch(Node
						.getParent(soapProcessors[i]), "?<status>"));
			}
		}
		return status;
	}
	
	public void stopSoapProcessor()
    {
        int method = 0;
        int response = 0;
		try
        {
            method = connector.createSOAPMethod("http://schemas.cordys.com/1.0/monitor", "Stop");
            Node.createTextElement("dn", soapProcessorDN, method);
            response = connector.sendAndWait(Node.getParent(method));
		}
        catch(Exception ex)
        {
			System.out.println("Error in stopping the processor  "+ex.getMessage());
		}
        finally
        {
            Node.delete(Node.getRoot(method));
            Node.delete(Node.getRoot(response));
        }
	}

	public void tearDown() throws Exception
	{
		stopSoapProcessor();
		// It may throw NullPointer Exception
		if(cpc != null)
			cpc.tearDown();
		if (ldapDeleteEnvelope!=0)
		{
			int ldapDeleteResult = connector.sendAndWait(ldapDeleteEnvelope);
			Node.delete(ldapDeleteResult);
		}
        
        cleanup();
    }
   
    public void cleanup() 
    {    
        if(cpc != null)
            cpc.cleanup();
        if (ldapDeleteEnvelope!=0)
        {
            Node.delete(ldapDeleteEnvelope);
            ldapDeleteEnvelope = 0;
        }        
	}
}
