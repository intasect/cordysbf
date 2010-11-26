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
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * @author Lejil
 * July 3, 2006
 * 
 *	 Creates the custom methodsets requird for the  junit test case and 
 * attach the methodsets to the  SOAP Node. Remove the methodsets on tearDown.
 * 
 */
public class MethodSetConfiguration extends BaseClass
{
	private ConnectorWrapper m_connector;
	private String m_organizationDN;
	private int m_methodsetConfiguration;
	private String m_methodsetNamespace;
	private String m_methodsetDN;
	private String m_methodsetName;
	private String m_implementationClass;
    private ContentManagerTask cmtTask;
    
	public MethodSetConfiguration(int methodsetConfiguration, ContentManagerTask cmtTask, String organizationDN)
	{
        this.cmtTask = cmtTask;
        this.m_connector = new ConnectorWrapper(cmtTask);
		this.m_methodsetConfiguration = methodsetConfiguration;
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
		int ldapUpdateResult =0,ldapInsertNode =0;
		try
		{
			m_methodsetName = Find.firstDataWithDefault(m_methodsetConfiguration,"<methodset><name>",null);
			m_methodsetDN = "cn=" + m_methodsetName + ",cn=Method Sets," + m_organizationDN;
			
			m_methodsetNamespace = Find.firstDataWithDefault(m_methodsetConfiguration,"<methodset><namespace>",null);
			m_implementationClass = Find.firstDataWithDefault(m_methodsetConfiguration,"<methodset><implementationClass>",null);
			
			ldapInsertNode = m_connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap", "Update");
			createMethodSet(ldapInsertNode,m_implementationClass);
			int[] methodNodes = Find.match(m_methodsetConfiguration,"?<methodset><method>");
			for (int i=0;i<methodNodes.length;i++)
			{
				String implementation = Find.firstDataWithDefault(methodNodes[i],"<method><impementation>",null);
				String wsdl = Find.firstDataWithDefault(methodNodes[i],"<method><wsdl>",null);
				String name = Find.firstDataWithDefault(methodNodes[i],"<method><name>",null);
				createMethod(ldapInsertNode,name,implementation,wsdl,i);
			}
			ldapUpdateResult = m_connector.sendAndWait(Node.getParent(ldapInsertNode), 2 * 60 * 1000);
			checkforSoapFault(ldapUpdateResult);
		}
		catch(Exception ex)
		{
			throw ex;
		}
		finally
		{
			if (ldapUpdateResult != 0)
				Node.delete(Node.getRoot(ldapUpdateResult));
			if (ldapInsertNode != 0)
				Node.delete(Node.getRoot(ldapInsertNode));
		}
    }
	
	/**
	 * Creates the method set. 
	 */
	private void createMethodSet(int ldapInsert,String implClass)
	{
		int tuple = Node.createElement("tuple", ldapInsert);
		int newNode = Node.createElement("new", tuple);
		int entryNode = Node.createElement("entry", newNode);
		Node.setAttribute(entryNode, "dn", m_methodsetDN);
		int objectclass = Node.createElement("objectclass", entryNode);
		Node.createTextElement("string", "top", objectclass);
		Node.createTextElement("string", "busmethodset", objectclass);
		int cn = Node.createElement("cn", entryNode);
		Node.createTextElement("string", m_methodsetName, cn);
		int labeleduri = Node.createElement("labeleduri", entryNode);
		Node.createTextElement("string", m_methodsetNamespace, labeleduri);
		int  implementationclass = Node.createElement("implementationclass", entryNode);
		Node.createTextElement("string", implClass, implementationclass);
	}
	
	/**
	 * Creates the methods
	 */
	private void createMethod(int ldapInsert,String methodName,String implementation,String wsdl,int sync_id) throws Exception
	{
		int tuple = Node.createElement("tuple", ldapInsert);
		Node.setAttribute(tuple,"sync_id",Integer.toString(sync_id));
		int newNode = Node.createElement("new", tuple);
		int entryNode = Node.createElement("entry", newNode);
		Node.setAttribute(entryNode, "dn", "cn=" + methodName+ ",cn="+m_methodsetName + ",cn=Method Sets,"+ m_organizationDN);
		int objectclass = Node.createElement("objectclass", entryNode);
		Node.createTextElement("string", "top", objectclass);
		Node.createTextElement("string", "busmethod", objectclass);
		int cn = Node.createElement("cn", entryNode);
		Node.createTextElement("string", methodName, cn);
		int busmethodimplementation = Node.createElement("busmethodimplementation", entryNode);
		Node.createTextElement("string", implementation, busmethodimplementation);
		int  busmethodwsdl = Node.createElement("busmethodwsdl", entryNode);
		Node.createTextElement("string", wsdl, busmethodwsdl);
	}
	
	
	public void tearDown() throws Exception
	{
	   int deleteNode = 0 ,deleteResponseNode =0;
	   try
	   {
		   deleteNode = m_connector.createSOAPMethod("http://schemas.cordys.com/1.0/ldap","DeleteRecursive");
		   int tupleNode = Node.createElement("tuple",deleteNode);
		   int oldNode = Node.createElement("old",tupleNode);
		   int entry = Node.createElement("entry",oldNode);
		   Node.setAttribute(entry,"dn",m_methodsetDN);
		   int cn = Node.createElement("cn",entry);
		   Node.createTextElement("string",m_methodsetName,cn);
		   int objectClass = Node.createElement("objectclass",entry);
		   Node.createTextElement("string","top",objectClass);
		   Node.createTextElement("string","busmethodset",objectClass);
		   int labeleduri = Node.createElement("labeleduri",entry);
		   Node.createTextElement("string",m_methodsetNamespace,labeleduri);
		   int implmentationClass = Node.createElement("implementationclass",entry);
		   Node.createTextElement("string",m_implementationClass,implmentationClass);
		   deleteResponseNode =m_connector.sendAndWait(Node.getParent(deleteNode), 2 * 60 * 1000);
		   checkforSoapFault(deleteResponseNode);
	   }
	   catch(Exception ex)
	   {
		  throw ex;
	   }
	   finally
	   {
		   if (deleteNode !=0)
			   Node.delete(Node.getRoot(deleteNode));
		   if (deleteResponseNode != 0)
			   Node.delete(Node.getRoot(deleteResponseNode));
	   }
    }
   
    public void cleanup() 
    {           
	}
		
	 private  void checkforSoapFault(int response) throws Exception
	 {
		try
		{
			int  faultNode =Find.firstMatch(response,"?<SOAP:Fault>");

			if ( faultNode != 0)
			{
	            assertTrue("System-Configuration: Setup Method Set:" + Node.writeToString(faultNode, true), Node.getDataElement(faultNode, "SOAP:faultstring", "").endsWith("already exists on the LDAP server."));
			}
		}
		catch(Exception ex)
		{
			throw ex;
		}
	}
}
