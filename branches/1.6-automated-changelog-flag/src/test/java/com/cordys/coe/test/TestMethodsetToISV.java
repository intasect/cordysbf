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
package com.cordys.coe.test;

import com.cordys.tools.ant.util.XSLUtil;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * DOCUMENTME
 *
 * @author $author$
 */
public class TestMethodsetToISV
{
    /**
     * DOCUMENTME
     */
    private static Document dDoc = new Document();

    /**
     * Main method.
     *
     * @param saArguments The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            int iTMP = dDoc.load("D:\\development\\workspaces\\EclipsePlugins\\CommandConnector\\src\\content\\methodsets\\Method Set Demo Command Connector.xml");
            int inputXML = dDoc.createElement("methodsets");
            Node.appendToChildren(iTMP, inputXML);
            System.out.println(Node.writeToString(inputXML, true));

            InputStream xslStream = new FileInputStream("D:\\development\\workspaces\\EclipsePlugins\\SVN_BuildTasks_main\\src\\java\\com\\cordys\\tools\\ant\\isv\\xsl\\DEV_TO_ISVMETHODSETS.xsl");

            //transform the xml using xsl.
            int contentRootNode = XSLUtil.getXSLTransformAsNode(Node.writeToString(inputXML,
                                                                                   true),
                                                                xslStream,
                                                                new String[]
                                                                {
                                                                    "isvDN"
                                                                },
                                                                new String[]
                                                                {
                                                                    "bbblllaaaa"
                                                                });

            System.out.println(Node.writeToString(contentRootNode, true));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
