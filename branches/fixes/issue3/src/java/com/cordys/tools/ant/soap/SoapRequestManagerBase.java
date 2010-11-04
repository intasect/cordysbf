/**
 * Copyright 2006 Cordys R&D B.V. 
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

import java.util.LinkedList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.cordys.coe.util.soap.SoapFaultInfo;
import com.cordys.coe.util.soap.SoapHelpers;
import com.cordys.coe.util.xml.Message;
import com.cordys.coe.util.xml.SharedXMLTree;
import com.cordys.tools.ant.cm.BcpVersionInfo;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * A base class for all SOAP request managers.
 */
public abstract class SoapRequestManagerBase
    implements ISoapRequestManager
{
    /**
     * The instance of the ANT Task which can be used optionally to log
     * messages. This has to be static for the log methods (not nice but there
     * should be only one request manager present at one time anyway).
     */
    protected static Task antTask;
    /**
     * A stack of NOM node collectors.
     */
    protected LinkedList<NomCollector> lNomCollectorStack = new LinkedList<NomCollector>();
    /**
     * The fully qualified class name which is implements the interface
     * <code>ISoapRequest</code>
     */
    protected String clazz = BusSoapRequest.class.getName();
    /**
     * Current orgazation DN.
     */
    protected String sOrganizationDN;
    /**
     * Current user DN.
     */
    protected String sUserDN;
    /**
     * Current sReceiver.
     */
    protected String sReceiver="";    
    
    /**
     * Flag indicating if NOM collection is enabled.
     */
    protected boolean bNomCollectionEnabled = true;
    /**
     * Timeout in milliseconds. Zero means no timeout. Note that this means the
     * gateway timeout not the socket timeout.
     */
    private long lTimeout = 0;
    /**
     * Contains the BCP server version information.
     */
    protected BcpVersionInfo bcpVersion;

    /**
     * DOCUMENT ME!
     *
     * @param aAntTask The Ant task to set.
     */
    public void setAntTask(Task aAntTask)
    {
        antTask = aAntTask;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the Ant task.
     */
    public Task getAntTask()
    {
        return antTask;
    }

    /**
     * Returns the top NOM collector from the stack.
     *
     * @return Topmost NOM collector or <code>null</code> if the stack was
     *         empty.
     */
    public NomCollector getCurrentNomCollector()
    {
        if (lNomCollectorStack.isEmpty())
        {
            return null;
        }

        return (NomCollector) lNomCollectorStack.getLast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param aNomCollectionEnabled The nomCollectionEnabled to set.
     */
    public void setNomCollectionEnabled(boolean aNomCollectionEnabled)
    {
        bNomCollectionEnabled = aNomCollectionEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the nomCollectionEnabled.
     */
    public boolean isNomCollectionEnabled()
    {
        return (!lNomCollectorStack.isEmpty()) ? bNomCollectionEnabled : false;
    }

    /**
     * @see com.cordys.tools.ant.soap.ISoapRequestManager#setOrganizationDN(java.lang.String)
     */
    public void setOrganizationDN(String sDN)
    {
        sOrganizationDN = sDN;
    }

    /**
     * @see com.cordys.tools.ant.soap.ISoapRequestManager#getOrganizationDN()
     */
    public String getOrganizationDN()
    {
        return sOrganizationDN;
    }

    /**
     * Set the class which implements the interface <code>ISoapRequest</code>
     * This method can be used to set the class which implements interface
     * methods that the SoapRequestManager class uses.
     *
     * @param clazz The class which implements the interface
     *        <code>ISoapRequest</code>
     */
    public void setSoapRequestClass(String clazz)
    {
        this.clazz = clazz;
    }

    /**
     * Sets the timeout for the soap requests that are sent.
     *
     * @param timeout The timeout value to be set
     */
    public void setTimeout(long timeout)
    {
        lTimeout = timeout;
    }

    /**
     * Returns the timeout for the soap requests that are sent.
     *
     * @return The timeout value.
     */
    public long getTimeout()
    {
        return lTimeout;
    }

    /**
     * @see com.cordys.tools.ant.soap.ISoapRequestManager#setUserDN(java.lang.String)
     */
    public void setUserDN(String sDN)
    {
        sUserDN = sDN;
    }

    /**
     * @see com.cordys.tools.ant.soap.ISoapRequestManager#getUserDN()
     */
    public String getUserDN()
    {
        return sUserDN;
    }

    /**
     * Adds a new collector to the stack.
     *
     * @param nmCollector NOM collector to be added.
     */
    public void addNomCollector(NomCollector nmCollector)
    {
        lNomCollectorStack.add(nmCollector);
    }

    /**
     * @see com.cordys.tools.ant.soap.ISoapRequestManager#addNomGarbage(int)
     */
    public void addNomGarbage(int iNode)
                       throws IllegalStateException
    {
        NomCollector ncColl = getCurrentNomCollector();

        if (ncColl != null)
        {
            ncColl.addNode(iNode);
        }
    }

    /**
     * Resolves the receiver to send the Soap Message, creates the Soap
     * Message, sends the Soap Message and returns the Response XML Node.
     *
     * @param mRequest The namespace of the method to be invoked.
     *
     * @return The Soap Message Response XML Node.
     *
     * @throws SoapRequestException
     */
    public Message makeSoapRequest(Message mRequest)
                            throws SoapRequestException
    {
        NomCollector nmCollector = getCurrentNomCollector();
        boolean bOldEnabled = bNomCollectionEnabled;

        try
        {
            bNomCollectionEnabled = false;

            int iResponseNode;
            int iOrigRequestMethodNode = Node.clone(mRequest.getXmlNode(), true);
            int iRequestMethodNode;

            ISoapRequest soapRequest = createSoapRequest();
            soapRequest.setReceiver(getReceiver());
            iRequestMethodNode = soapRequest.addMethod(getOrganizationDN(),
                                                       iOrigRequestMethodNode);

            // Add all attributes from the method except the namespace.
            int iAttribCount = Node.getNumAttributes(iOrigRequestMethodNode);

            for (int i = 0; i < iAttribCount; i++)
            {
                String sLocalName = Node.getAttributeLocalName(iOrigRequestMethodNode,
                                                               i + 1);

                if ((sLocalName != null) && !sLocalName.equals("xmlns"))
                {
                    String sName = Node.getAttributeName(iOrigRequestMethodNode,
                                                         i + 1);
                    String sValue = Node.getAttribute(iOrigRequestMethodNode,
                                                      sName);

                    if ((sName != null) && (sValue != null))
                    {
                        Node.setAttribute(iRequestMethodNode, sName, sValue);
                    }
                }
            }

            iResponseNode = soapRequest.execute();

            // Find the response content.
            String soapPrefix = SoapHelpers.getSoapNamespacePrefix(iResponseNode);
            int iResponseData = Find.firstMatch(iResponseNode, "<><" + soapPrefix + "Body><>");

            if (iResponseData == 0)
            {
                if (iResponseNode != 0)
                {
                    Node.delete(iResponseNode);
                }

                throw new SoapRequestException("No data in response SOAP body.");
            }

            //check the response for Soap Fault.
            checkSoapFault(iResponseNode);

            // Construct a new Message object from the response.
            Message mResponse = new Message(new SharedXMLTree(iResponseNode),
                                            iResponseData);

            if ((nmCollector != null) && bOldEnabled)
            {
                nmCollector.addMessage(mResponse);
            }

            return mResponse;
        }
        finally
        {
            bNomCollectionEnabled = bOldEnabled;
        }
    }

    /**
     * Removes the top NOM collector from the stack.
     *
     * @return Removed topmost NOM collector or <code>null</code> if the stack
     *         was empty.
     */
    public NomCollector removeNomCollector()
    {
        if (lNomCollectorStack.isEmpty())
        {
            return null;
        }

        return (NomCollector) lNomCollectorStack.removeLast();
    }

    /**
     * Uses ants logging mechanism to log messages. Note:Works only if the ant
     * task instance has been passed in the constructor.
     *
     * @param message
     */
    protected static void log(String message)
    {
        log(message, Project.MSG_INFO);
    }

    /**
     * Uses ants logging mechanism to log messages. Note:Works only if the ant
     * task instance has been passed in the constructor.
     *
     * @param message The message to be logged.
     * @param messageLevel The message level for logging messages.
     */
    protected static void log(String message, int messageLevel)
    {
        if (antTask != null)
        {
            antTask.log(message, messageLevel);
        }
    }

    /**
     * This helper method is used for throwing SoapRequestException with a
     * message which is extracted from the response of the soap faults.
     *
     * @param responseNode The response node received after execution of the
     *        soap request.
     *
     * @throws BuildException
     */
    private static void checkSoapFault(int responseNode)
                                throws SoapRequestException
    {
        String errorMess = "";

        /*
         * Check the response for Soap Fault.
         * If Soap Fault occurs, display appropriate message and halt the build.
         */
        SoapFaultInfo info = SoapFaultInfo.findSoapFault(responseNode);
        
        if (info != null) {
            String faultString = info.getFaultstring();
            
            if (faultString != null) {
                errorMess = "\nMessage: " + faultString;
            }

            int detailNode = info.getDetail();
            
            if (detailNode != 0) {
                int descNode = Find.firstMatch(detailNode, "<><errordescription>");
                
                errorMess = errorMess + "\nDetails: " + Node.getData(descNode);
            }
        }

        if (!"".equals(errorMess))
        {
            throw new SoapRequestException(errorMess);
        }
    }

    /**
     * Returns the bcpVersion.
     *
     * @return Returns the bcpVersion.
     */
    public BcpVersionInfo getBcpVersion()
    {
        return bcpVersion;
    }

    /**
     * Sets the bcpVersion.
     *
     * @param bcpVersion The bcpVersion to be set.
     */
    public void setBcpVersion(BcpVersionInfo bcpVersion)
    {
        this.bcpVersion = bcpVersion;
    }
    
    /*
     * (non-Javadoc)
     * @see com.cordys.tools.ant.soap.ISoapRequestManager#getReceiver()
     */
	public String getReceiver() {
		return sReceiver;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cordys.tools.ant.soap.ISoapRequestManager#setReceiver(java.lang.String)
	 */
	public void setReceiver(String receiver) {
		this.sReceiver = receiver;
	}
}
