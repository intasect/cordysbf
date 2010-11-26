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

import java.io.File;

import com.eibus.xml.nom.Find;

/**
 * Typical format of content:
 * <pre>
 *  	&lt;menus loader=&quot;com.eibus.contentmanagement.ISVSOAPManager&quot; description=&quot;Menus&quot;&gt;
 *  		&lt;SOAP:Envelope xmlns:SOAP=&quot;http://schemas.xmlsoap.org/soap/envelope/&quot;&gt;
 *  			&lt;SOAP:Body&gt;
 *  				&lt;UpdateXMLObject xmlns=&quot;http://schemas.cordys.com/1.0/xmlstore&quot;&gt;
 *  					&lt;tuple version=&quot;isv&quot; unconditional=&quot;true&quot;&gt;
 *  						&lt;new&gt;
 *  							&lt;menu&gt;
 *  							:
 *  							&lt;/menu&gt;
 *  						&lt;/new&gt;
 *  					&lt;/tuple&gt;
 *  				&lt;/UpdateXMLObject&gt;
 *  			&lt;/SOAP:Body&gt;
 *  		&lt;/SOAP:Envelope&gt;
 *  		&lt;SOAP:Envelope&gt;
 *  		:
 *  		&lt;/SOAP:Envelope&gt;
 *  	&lt;/menus&gt;
 * </pre>
 *
 * @author msreejit
 */

//Copying the Menus Handler
public class XForms extends XMLStore
    implements ISVContentHandler
{
    /**
     * The xpath pattern to be matched in the content XML passed.
     */
    private static final String CONTENT_TAG_MATCH_PATTERN = "<xforms><xform>";
    /**
     * The Actual root of the content. This is empty because XForm file have
     * root tag 'xformhtml' and translation file has 'mlm'. So the root
     * tag is not checked for XForms or translation files.
     */
    private static final String CONTENT_INTERNAL_ROOT_TAG = "";
    /**
     * The root tag of the ISV content to be created.
     */
    private static final String ISV_CONTENT_ROOT_TAG = "xforms";
    /**
     * The root tag of the developer content.
     */
    private static final String CONTENT_ROOT_TAG = "xforms";
    /**
     * The description of the isv content loader.
     */
    private static final String ISV_CONTENT_DESCRIPTION = "XForms";

    /**
     * Default Constructor
     */
    public XForms()
    {
        super();
        setContentRootTag(CONTENT_ROOT_TAG);
        setContentTagMatchPattern(CONTENT_TAG_MATCH_PATTERN);
        setInternalRootTag(CONTENT_INTERNAL_ROOT_TAG);
        setIsvContentDescription(ISV_CONTENT_DESCRIPTION);
        setIsvContentRootTag(ISV_CONTENT_ROOT_TAG);
    }
    
    /**
     * Returns content root node inside the content loaded from the given file.
     * Returns the file root node for an XForm or the 'dictionary' node for an XForm
     * translation.
     * 
     * @param fFile File in question.
     * @param iFileRootNode XML content loaded from the file.
     * @return Content root node for this content type or zero if no content was found.
     */    
    protected int getFileContentNode(File fFile, int iFileRootNode) 
    {
        if (fFile.getName().endsWith(".mlm")) 
        {
            int iDictionaryNode = Find.firstMatch(iFileRootNode, "?<content><dictionary>");
            
            return iDictionaryNode;
        } 
        else if (fFile.getName().endsWith(".caf")) 
        {
        	int iContentNode = Find.firstMatch(iFileRootNode, "<caf><content><xformhtml>");
        	if (iContentNode > 0)
        	{            
        		return iContentNode;
        	}
        } 
        return iFileRootNode;
    }
}
