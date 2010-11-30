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
 * A wrapper class for CoBOC GetXMLObject method.
 *  
 * @author mpoyhone
 */
public class GetXMLObject extends MethodBase {
    /**
     * Calls the CoBOC GetXMLObject mehtod. ## Copied from com.cordys.tools.at.cm.CoBOCHandler
     * (mpoyhone) ##
     *
     * @param swSoap The SOAP wrapper to use.
     * @param sKey The name of the folder.
     * @param sFilter The filter (i.e. 'folder' or 'template').
     * @param sType The type (i.e. 'entity').
     * @param sLevel The version ('isv', 'organization' or 'user').
     * @param lTimeout Request timeout in milliseconds.
     *
     * @return The received response.
     */
    public int execute(ISoapRequestManager srmSoap, String sKey,
                                   String sFilter, String sType, String sLevel,
                                   long lTimeout)
                            throws SoapRequestException
    {
        return execute(srmSoap, sKey, sFilter, sType, sLevel, lTimeout, null);
    }

    /**
     * Calls the CoBOC GetXMLObject mehtod.   ## Copied from com.cordys.tools.at.cm.CoBOCHandler
     * (mpoyhone) ##
     *
     * @param swSoap The SOAP wrapper to use.
     * @param sKey The name of the folder.
     * @param sFilter The filter (i.e. 'folder' or 'template').
     * @param sType The type (i.e. 'entity').
     * @param sLevel The version ('isv', 'organization' or 'user').
     * @param lTimeout Request timeout in milliseconds.
     * @param lLogger Logger object for debug messages.
     *
     * @return The received response.
     */
    public int execute(ISoapRequestManager srmSoap, String sKey,
                                   String sFilter, String sType, String sLevel,
                                   long lTimeout, LogInterface lLogger)
                            throws SoapRequestException
    {
        //This is a hack for CoBOC. If you send the root-folder as '/' it returns a NULL-pointer.
        //So if it's '/' we'll make it empty.
        String sRealFolder = sKey;

        if (sRealFolder.equals("/"))
        {
            sRealFolder = "";
        }
        
        int iMethodNode;
        ISoapRequest srRequest;

        srRequest = srmSoap.createSoapRequest();
        iMethodNode = srRequest.addMethod(CoBOCMethods.NSP_COBOC, CoBOCMethods.MTD_GETXMLOBJECT);

        int iParamNode = Node.createTextElement("key", sRealFolder, iMethodNode);
        Node.setAttribute(iParamNode, "filter", sFilter);
        Node.setAttribute(iParamNode, "type", sType);
        Node.setAttribute(iParamNode, "version", sLevel);

        return srRequest.execute();
    }

	/**
	 * Returns the method name.
	 * 
	 * @param The method name.
	 */
	public String getMethodName() {
		return CoBOCMethods.MTD_GETXMLOBJECT;
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
