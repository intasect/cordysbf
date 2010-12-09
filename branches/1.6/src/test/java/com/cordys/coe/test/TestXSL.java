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

import java.io.InputStream;
import java.util.regex.Pattern;

import com.cordys.tools.ant.cm.SoapNodesHandler;
import com.cordys.tools.ant.util.FileUtil;
import com.cordys.tools.ant.util.GeneralUtils;
import com.cordys.tools.ant.util.XMLUtils;
import com.cordys.tools.ant.util.XSLUtil;
import com.eibus.version.Version;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * This class can be used to test XSL transforms of the content.
 *
 * @author $author$
 */
public class TestXSL
{
    /**
     * Static XML document.
     */
    private static Document s_dDoc = new Document();

    /**
     * Main method.
     *
     * @param saArguments The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            System.out.println(new Version().getBuild());
            int iSourceXML = s_dDoc.load("./src/test/java/com/cordys/coe/test/content.xml");
            
            int iTemp = s_dDoc.createElement("soapnodes");
            Node.appendToChildren(iSourceXML, iTemp);
            iSourceXML = iTemp;
            
            int[] aiMethodSets = Find.match(iSourceXML,
            "<soapnodes><soapnode><methodset>");
            
            Pattern pPattern = Pattern.compile("cn=cordys,o=[^,]+$");
            String sLDAPRoot = GeneralUtils.getLdapRootFromOrganization("o=Jaap,cn=cordys,o=123456789.com");
            
            for (int iCount = 0; iCount < aiMethodSets.length; iCount++)
            {
                int iMethodSetNode = aiMethodSets[iCount];
                String sMethodsetDN = Node.getDataWithDefault(iMethodSetNode,
                                                              "");

                if (sMethodsetDN.length() > 0)
                {
                    System.out.println("] Fixing DN: " + sMethodsetDN);
                    sMethodsetDN = pPattern.matcher(sMethodsetDN).replaceAll(sLDAPRoot);
                    System.out.println("]    New DN: " + sMethodsetDN);
                    
                    //Delete the current value
                    while (Node.getFirstChild(iMethodSetNode) != 0)
                    {
                        Node.delete(Node.getFirstChild(iMethodSetNode));
                    }
                    
                    Node.getDocument(iMethodSetNode).createText(sMethodsetDN, iMethodSetNode);
                }
            }

            InputStream xslStream = FileUtil.getResourceAsStream(SoapNodesHandler.class,
                                                                 "xsl/DEV_TO_SOAPNODES.xsl");
//            InputStream xslStream = FileUtil.getResourceAsStream(TestXSL.class,
//            "test.xsl");

            XMLUtils.convertNodeToString(iSourceXML,
                                         "<soapnodes><soapnode><soapprocessor><soapprocessorconfiguration>");
//            System.out.println(Node.writeToString(iSourceXML, true));

            int updateXmlNode = XSLUtil.getXSLTransformAsNode(Node.writeToString(iSourceXML,
                                                                                 true),
                                                              xslStream,
                                                              new String[]
                                                              {
                                                                  "orgDN",
                                                                  "ldapRootDN"
                                                              },
                                                              new String[]
                                                              {
                                                                  "o=Test,cn=cordys,o=iets.com",
                                                                  "cn=cordys,o=iets.com"
                                                              });

            System.out.println(Node.writeToString(updateXmlNode, true));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
