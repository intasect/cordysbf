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
 /**
 *         Project         :        BuildFramework
 *         File                :        XForms.java
 *         Author                :        manesh@cordys.com
 *         Created on         :        Oct 1, 2004
 *
 */
package com.cordys.tools.ant.isv;

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;

import com.cordys.tools.ant.cm.EBcpVersion;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * DOCUMENTME
 *
 * @author $author$
  */
public class WsAppServer_c2 extends XMLStore
    implements ISVContentHandler
{
    /**
     * The xpath pattern to be matched in the content XML passed.
     */
    private static final String CONTENT_TAG_MATCH_PATTERN = "<wsappserver-content><content>";
    /**
     * The Actual root of the content.
     */
    private static final String CONTENT_INTERNAL_ROOT_TAG = "";
    /**
     * The root tag of the ISV content to be created.
     */
    private static final String ISV_CONTENT_ROOT_TAG = "wsappserver-content";
    /**
     * The root tag of the developer content.
     */
    private static final String CONTENT_ROOT_TAG = "wsappserver-content";
    /**
     * The description of the isv content loader.
     */
    private static final String ISV_CONTENT_DESCRIPTION = "Ws-AppServer";

/**
    * Default Constructor
    */
    public WsAppServer_c2()
    {
        super();
        setContentRootTag(CONTENT_ROOT_TAG);
        setContentTagMatchPattern(CONTENT_TAG_MATCH_PATTERN);
        setInternalRootTag(CONTENT_INTERNAL_ROOT_TAG);
        setIsvContentDescription(ISV_CONTENT_DESCRIPTION);
        setIsvContentRootTag(ISV_CONTENT_ROOT_TAG);
    }

    /**
     * Call back for child classes. Can be used to modify the ISV XMl
     * before it is being written to the ISV package.
     *
     * @param xInputXML Generated XML store ISV contentXML.
     * @param xCurrentIsvContentNode XML of the whole ISV package that is
     *        generated so far.
     */
    protected void modifyIsvXml(int xInputXML, int xCurrentIsvContentNode)
    {
        if (currentIsvTask.getVersionInfo().isLaterThan(EBcpVersion.BCP42_C3)) {
            // In C3 WS-AppServer templates are no longer stored in CoBOC. 
            return;
        }
        
        int[] xmlNodes;
        int[] xmlNodes1 = Find.match(xInputXML, "<wsappserver-content><content><class><templateid>");
        int[] xmlNodes2 = Find.match(xInputXML, "<wsappserver-content><content><classregistry><class><templateid>");

        xmlNodes = new int[xmlNodes1.length + xmlNodes2.length];
        System.arraycopy(xmlNodes1, 0, xmlNodes, 0, xmlNodes1.length);
        System.arraycopy(xmlNodes2, 0, xmlNodes, xmlNodes1.length, xmlNodes2.length);
        
        int[] xaCobocMapTuples = Find.match(xCurrentIsvContentNode,
                                            "?<CPCImporter><TemplateContent><template><tuple>");
        Map<String, String> mTupleMap = new HashMap<String, String>();

        // Initialize the CoBOC template key -> object ID map.
        for (int i = 0; i < xaCobocMapTuples.length; i++)
        {
            int xMapTuple = xaCobocMapTuples[i];
            String sKey = Node.getAttribute(xMapTuple, "key", "");
            String sObjectId = Node.getAttribute(xMapTuple, "entity_id", "");

            if ((sKey == null) || (sKey.length() == 0))
            {
                throw new BuildException("Key attribute not found from CoBOC template tuple.");
            }

            if ((sObjectId == null) || (sObjectId.length() == 0))
            {
                throw new BuildException("Object ID attribute not found from CoBOC template tuple.");
            }

            mTupleMap.put(sKey, sObjectId);
        }
        
        for (int i = 0; i < xmlNodes.length; i++)
        {
            int node = xmlNodes[i];
            String sKey = Node.getDataWithDefault(node, "");
            
            if (sKey.length() == 0 || sKey.equals("1020")) {
                // No ID set or it is the default template ID.
                continue;
            }
            
            String sId = mTupleMap.get(sKey);
            
            if (sId == null || sId.length() == 0)
            {
                throw new BuildException("Unable to find CoBOC template with key: " + sKey);
            }
            
            Node.setDataElement(node, "", sId);
        }          
    }
}
