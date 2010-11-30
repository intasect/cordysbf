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
package com.cordys.coe.ant.coboc.methods;

import com.cordys.coe.ant.coboc.CoBOCMethods;
import com.cordys.coe.util.log.LogInterface;
import com.cordys.tools.ant.soap.ISoapRequest;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.eibus.xml.nom.Node;

/**
 * A wrapper for CoBOC DB methods (Create, Modify, Delete and Select)
 *
 * @author mpoyhone
 */
public class Modify extends MethodBase
{
    /**
     * Returns the method name.
     *
     * @return DOCUMENTME
     */
    public String getMethodName()
    {
        return CoBOCMethods.MTD_MODIFY;
    }

    /**
     * Returns the method namespace.
     *
     * @return DOCUMENTME
     */
    public String getMethodNameSpace()
    {
        return CoBOCMethods.NSP_COBOC;
    }

    /**
     * This method modifies an object in the CoBOC DB (works at least with
     * template attributes).
     *
     * @param swSoap The SOAP wrapper to use.
     * @param sTableName The DB table to be used.
     * @param iOldContentNode The old object contents XML structure.
     * @param iNewContentNode The new object contents XML structure.
     * @param lTimeout Request timeout in milliseconds.
     * @param lLogger Logger object for debug messages.
     *
     * @return The received response.
     */
    public int execute(ISoapRequestManager srmSoap, String sTableName,
                       int iOldContentNode, int iNewContentNode, long lTimeout,
                       LogInterface lLogger)
                throws SoapRequestException
    {
    	int iMethodNode;
        ISoapRequest srRequest;

        srRequest = srmSoap.createSoapRequest();
        iMethodNode = srRequest.addMethod(CoBOCMethods.NSP_COBOC, CoBOCMethods.MTD_MODIFY);
        Node.setAttribute(iMethodNode, "table", sTableName);
        
        // Add the content under the old and new nodes.
        Node.appendToChildren(Node.clone(iOldContentNode, true), iMethodNode);
        Node.appendToChildren(Node.clone(iNewContentNode, true), iMethodNode);        
        
        return srRequest.execute();
    }
}
