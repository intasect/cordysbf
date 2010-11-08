/**
 * Copyright 2007 Cordys R&D B.V. 
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
import com.cordys.tools.ant.soap.ISoapRequest;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.eibus.exception.ExceptionGroup;


/**
 * A wrapper class for the ISoapRequestManager for using it through a NOM connector
 * like interface.
 *
 * @author mpoyhone
 */
public class ConnectorWrapper
{
    private ContentManagerTask cmtTask;
    private ISoapRequestManager srmSoapManager;
    
    /**
     * Constructor for ConnectorWrapper
     * @param cmtTask
     */
    public ConnectorWrapper(ContentManagerTask cmtTask)
    {
        super();
        this.cmtTask = cmtTask;
        this.srmSoapManager = cmtTask.getSoapRequestMgr();
    }

    /**
     * Returns the cmtTask.
     *
     * @return Returns the cmtTask.
     */
    public ContentManagerTask getContentManagerTask()
    {
        return cmtTask;
    }

    /**
     * Returns the srmSoapManager.
     *
     * @return Returns the srmSoapManager.
     */
    public ISoapRequestManager getSoapManager()
    {
        return srmSoapManager;
    }

    /**
     * @param receiver
     * @param uri
     * @return
     * @throws SoapRequestException 
     */
    public int createSOAPMessage(String receiver, String uri) throws SoapRequestException
    {
        ISoapRequest req = srmSoapManager.createSoapRequest();
        
        return req.addMethod(null, null);
    }

    /**
     * TODO Describe the method.
     * @param root
     * @return
     * @throws SoapRequestException 
     */
    public int sendAndWait(int envelope) throws ExceptionGroup
    {
        try
        {
            ISoapRequest req = srmSoapManager.createSoapRequest();
            
            req.setSoapRequest(envelope);
            
            return req.execute();
        }
        catch (SoapRequestException e)
        {
            throw new ExceptionGroup(new Throwable[] { e });
        }            
    }
    
    /**
     * TODO Describe the method.
     * @param envelope
     * @param timeout
     * @return
     * @throws ExceptionGroup 
     */
    public int sendAndWait(int envelope, long timeout) throws ExceptionGroup
    {
        try
        {
            ISoapRequest req = srmSoapManager.createSoapRequest();
            
            req.setSoapRequest(envelope);
            
            return req.execute(timeout);
        }
        catch (SoapRequestException e)
        {
            throw new ExceptionGroup(new Throwable[] { e });
        }  
    }

    /**
     * TODO Describe the method.
     * @param string
     * @param string2
     * @return
     * @throws ExceptionGroup 
     */
    public int createSOAPMethod(String namespace, String method) throws ExceptionGroup
    {
        try
        {
            ISoapRequest req = srmSoapManager.createSoapRequest();
            
            return req.addMethod(namespace, method);
        }
        catch (SoapRequestException e)
        {
            throw new ExceptionGroup(new Throwable[] { e });
        }
    }


    
}
