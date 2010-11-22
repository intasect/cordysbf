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

package com.cordys.tools.ant.cm;

import com.cordys.tools.ant.soap.ISoapRequest;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;

/**
 * The class handles the WsAppServer content. This handler handles the
 * content located in the XMLStore as it is from BCP 4.2 C2 onwards.  This
 * extends most of the features used commonly in XML Store kind of contents
 * from the  Class <code>XMLStoreHandler</code>.
 *
 * @author msreejit
 */
public class WsAppServerHandler_c2 extends XMLStoreHandler
    implements ContentHandler
{
    /**
     * The location in the XML Store where the WsAppServer files are
     * stored.
     */
    private static final String HANDLER_XMLSTORE_KEY = "/Cordys/WS-AppServer";
    /**
     * Message logged when no content is found to import from ECX
     */
    private static final String NO_IMPORT_CONTENT_FOUND = "No WsAppServer content found to import from ECX.";
    /**
     * Message logged when no content is found to export from ECX
     */
    private static final String NO_EXPORT_CONTENT_FOUND = "No WsAppServer content found to export from ECX.";
    /**
     * Message logged when no content is found to delete from ECX
     */
    private static final String NO_DELETE_CONTENT_FOUND = "No WsAppServer content found in ECX to delete.";
    /**
     * Message logged when import from ECX is successful
     */
    private static final String ECX_TO_FILE_SUCCESS = "Successfully exported WsAppServer content from ECX.";
    /**
     * Message logged when export from ECX is successful
     */
    private static final String FILE_TO_ECX_SUCCESS = "Successfully imported WsAppServer content to ECX.";
    /**
     * Message logged when delete from ECX is successful
     */
    private static final String DELETE_SUCCESS = "Successfully deleted WsAppServer content from ECX.";
    /**
     * Contains mappings from CoBOC template ID to key. This is needed
     * because there is no method to get the template object by ID.
     */
    private static Map<String, String> mTemplateIdToKeyMap;

/**
     * Default Constructor
     */
    public WsAppServerHandler_c2()
    {
        super();
        setXmlStoreKey(HANDLER_XMLSTORE_KEY);
        setMsgExportContentNotFound(NO_EXPORT_CONTENT_FOUND);
        setMsgImportContentNotFound(NO_IMPORT_CONTENT_FOUND);
        setMsgDeleteContentNotFound(NO_DELETE_CONTENT_FOUND);
        setMsgEcxToFileSucess(ECX_TO_FILE_SUCCESS);
        setMsgFileToEcxSucess(FILE_TO_ECX_SUCCESS);
        setMsgDeleteSucess(DELETE_SUCCESS);
        setContentTag("wsappserver");
        setContentRootTag("content");
        setIsvContentPattern("?<wsappserver><><><><content>");
        setUseFullPath(false);
    }

    /**
     * 
     * @see com.cordys.tools.ant.cm.ContentHandler#getSingleContentName(java.io.File,
     *      ContentManagerTask, Content, boolean)
     */
    @Override
    public String getSingleContentName(File contentFile,
                                       ContentManagerTask cmtTask,
                                       Content content, boolean toEcx)
                                throws IOException
    {
        String name = super.getSingleContentName(contentFile, cmtTask, content,
                                                 toEcx);

        if ((name != null) && !toEcx)
        {
            String fixedName;

            fixedName = name.replaceFirst("^repository/(.*)",
                                          "repository/packages/$1");

            if (fixedName.length() == name.length())
            {
                fixedName = name.replaceFirst("^runtime/(.*)",
                                              "runtime/classregistry/$1");
            }

            name = fixedName;
        }
        return name;
    }

    /**
     * Implementation of modifyContentBeforeUpdateXmlStoreContent.
     *
     * @param updateXMLNode The XML content.
     * @param srmSoapRequestManager DOCUMENTME
     */
    protected void modifyContentBeforeUpdateXmlStoreContent(int updateXMLNode,
                                                            ISoapRequestManager srmSoapRequestManager)
    {
        // remove packages folder from key attribute
        int[] xmlNodes = Find.match(updateXMLNode, "<content><wsappserver>");

        for (int i = 0; i < xmlNodes.length; i++)
        {
            int node = xmlNodes[i];
            String key = Node.getAttribute(node, "key");
            String newKey = key.replaceFirst("/Cordys/WS-AppServer/repository",
                                             "/Cordys/WS-AppServer/repository/packages");

            newKey = newKey.replaceFirst("/Cordys/WS-AppServer/runtime",
                                         "/Cordys/WS-AppServer/runtime/classregistry");

            Node.setAttribute(node, "key", newKey);
        }

        if (! cCurrentContent.getBcpVersion().isLaterThan(EBcpVersion.BCP42_C3)) {
            // Replace CoBOC template key with template ID.
            int[] xmlNodes1 = Find.match(updateXMLNode,
                                         "<content><wsappserver><class><templateid>");
            int[] xmlNodes2 = Find.match(updateXMLNode,
                                         "<content><wsappserver><classregistry><class><templateid>");
    
            xmlNodes = new int[xmlNodes1.length + xmlNodes2.length];
            System.arraycopy(xmlNodes1, 0, xmlNodes, 0, xmlNodes1.length);
            System.arraycopy(xmlNodes2, 0, xmlNodes, xmlNodes1.length,
                             xmlNodes2.length);
    
            for (int i = 0; i < xmlNodes.length; i++)
            {
                int node = xmlNodes[i];
                String sKey = Node.getDataWithDefault(node, "");
    
                if ((sKey.length() == 0) || sKey.equals("1020"))
                {
                    // No ID set or it is the default template ID.
                    continue;
                }
    
                String sId;
    
                try
                {
                    sId = getCobocTemplateID(srmSoapRequestManager, sKey);
                }
                catch (SoapRequestException e)
                {
                    throw new BuildException("Unable to read CoBOC templates.");
                }
    
                Node.setDataElement(node, "", sId);
            }
        }
    }

    /**
     * Implementation of modifyContentBeforeWriteToFolder.
     *
     * @param contentNode Content node
     * @param srmSoapRequestManager DOCUMENTME
     */
    protected void modifyContentBeforeWriteToFolder(int contentNode,
                                                    ISoapRequestManager srmSoapRequestManager)
    {
        // remove packages folder from key attribute
        int[] xmlNodes = Find.match(contentNode, "<content><wsappserver>");

        for (int i = 0; i < xmlNodes.length; i++)
        {
            int node = xmlNodes[i];
            String key = Node.getAttribute(node, "key");
            String newKey = key.replaceFirst("/Cordys/WS-AppServer/repository/packages",
                                             "/Cordys/WS-AppServer/repository");

            newKey = newKey.replaceFirst("/Cordys/WS-AppServer/runtime/classregistry",
                                         "/Cordys/WS-AppServer/runtime");

            Node.setAttribute(node, "key", newKey);
        }

        if (! cCurrentContent.getBcpVersion().isLaterThan(EBcpVersion.BCP42_C3)) {
            // Replace CoBOC template ID with template key.
            int[] xmlNodes1 = Find.match(contentNode,
                                         "<content><wsappserver><class><templateid>");
            int[] xmlNodes2 = Find.match(contentNode,
                                         "<content><wsappserver><classregistry><class><templateid>");
    
            xmlNodes = new int[xmlNodes1.length + xmlNodes2.length];
            System.arraycopy(xmlNodes1, 0, xmlNodes, 0, xmlNodes1.length);
            System.arraycopy(xmlNodes2, 0, xmlNodes, xmlNodes1.length,
                             xmlNodes2.length);
    
            for (int i = 0; i < xmlNodes.length; i++)
            {
                int node = xmlNodes[i];
                String sId = Node.getDataWithDefault(node, "");
    
                if ((sId.length() == 0) || sId.equals("1020"))
                {
                    // No ID set or it is the default template ID.
                    continue;
                }
    
                String sKey;
    
                try
                {
                    sKey = getCobocTemplateKey(srmSoapRequestManager, sId);
                }
                catch (SoapRequestException e)
                {
                    throw new BuildException("Unable to read CoBOC templates.");
                }
    
                Node.setDataElement(node, "", sKey);
            }
        }
    }

    /**
     * This method gets the CoBOC template id for a certain key.
     *
     * @param srmSoapRequestManager The soap request manager.
     * @param sKey The key of the template.
     *
     * @return The template ID.
     */
    private String getCobocTemplateID(ISoapRequestManager srmSoapRequestManager,
                                      String sKey)
                               throws SoapRequestException
    {
        String sReturn = null;

        ISoapRequest srRequest = srmSoapRequestManager.createSoapRequest();
        int xRequestNode;
        int xRequestKeyNode;

        xRequestNode = srRequest.addMethod("http://schemas.cordys.com/4.2/coboc",
                                           "GetXMLObject");
        xRequestKeyNode = Node.createTextElement("key", sKey, xRequestNode);
        Node.setAttribute(xRequestKeyNode, "filter", "template");
        Node.setAttribute(xRequestKeyNode, "type", "entity");
        Node.setAttribute(xRequestKeyNode, "version", "organization");

        int xResponseNode = srRequest.execute();

        int xMapIdNode = Find.firstMatch(xResponseNode,
                                         "?<tuple><old><ENTITY><ENTITY_ID>");
        sReturn = Node.getDataWithDefault(xMapIdNode, "");

        if (sReturn.length() == 0)
        {
            throw new SoapRequestException("Unable to find template ID for key: " +
                                           sKey);
        }

        return sReturn;
    }

    /**
     * This method gets the CoBOC template key for a certain ID.
     *
     * @param srmSoapRequestManager The soap request manager.
     * @param sId The ID of the template.
     *
     * @return The template key.
     */
    private String getCobocTemplateKey(ISoapRequestManager srmSoapRequestManager,
                                       String sId)
                                throws SoapRequestException
    {
        if (mTemplateIdToKeyMap == null)
        {
            Map<String, String> mMap = new HashMap<String, String>();
            ISoapRequest srRequest = srmSoapRequestManager.createSoapRequest();
            int xRequestNode;

            xRequestNode = srRequest.addMethod("http://schemas.cordys.com/4.2/coboc",
                                               "GetTemplatesByOrganization");
            Node.createTextElement("organization",
                                   srmSoapRequestManager.getOrganizationDN(),
                                   xRequestNode);

            int xResponseNode = srRequest.execute();

            int[] xaTemplateNodes = Find.match(xResponseNode, "?<tuple>");

            for (int xNode : xaTemplateNodes)
            {
                String sTplKey = Node.getAttribute(xNode, "key", "");
                String sTplId = Node.getDataWithDefault(Find.firstMatch(xNode,
                                                                        "<><><ENTITY><ENTITY_ID>"),
                                                        "");

                if ((sTplKey == null) || (sTplKey.length() == 0))
                {
                    throw new SoapRequestException("CoBOC template key not set in the response.");
                }

                if ((sTplId == null) || (sTplId.length() == 0))
                {
                    throw new SoapRequestException("CoBOC template ID not set in the response.");
                }

                mMap.put(sTplId, sTplKey);
            }

            mTemplateIdToKeyMap = mMap;
        }

        return mTemplateIdToKeyMap.get(sId);
    }
}
