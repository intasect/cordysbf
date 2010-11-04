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

/**
 * A wrapper class for CoBOC UpdateXMLObject method.
 *  
 * @author mpoyhone
 */
public class UpdateXMLObject extends MethodBase {
	   /**
     * Updates the folder object at CoBOC
     *
     * @param swSoap The SOAP wrapper to use.
     * @param iTupleNode The tuple node that contains old or/and new nodes.
     * @param lTimeout Request timeout in milliseconds.
     *
     * @return The received response.
     */
    public int execute(ISoapRequestManager srmSoap, int iTupleNode,
                                      long lTimeout)
                               throws SoapRequestException
    {
        return execute(srmSoap, iTupleNode, lTimeout, null);
    }

    /**
     * Updates the folder object at CoBOC
     *
     * @param swSoap The SOAP wrapper to use.
     * @param iTupleNode The tuple node that contains old or/and new nodes.
     * @param lTimeout Request timeout in milliseconds.
     * @param lLogger The logger object for debug messages.
     *
     * @return The received response.
     */
    public int execute(ISoapRequestManager srmSoap, int iTupleNode,
                                      long lTimeout, LogInterface lLogger)
                               throws SoapRequestException
    {
        ISoapRequest srRequest;

        srRequest = srmSoap.createSoapRequest();
        srRequest.addMethod(CoBOCMethods.NSP_COBOC, CoBOCMethods.MTD_UPDATEXMLOBJECT, iTupleNode);

        return srRequest.execute();
    }


	/**
	 * Returns the method name.
	 * 
	 * @param The method name.
	 */
	public String getMethodName() {
		return CoBOCMethods.MTD_UPDATEXMLOBJECT;
	}

	/**
	 * Returns the method namespace.
	 * 
	 * @param The method namespace.
	 */
	public String getMethodNameSpace() {
		return CoBOCMethods.NSP_COBOC;
	}
}
