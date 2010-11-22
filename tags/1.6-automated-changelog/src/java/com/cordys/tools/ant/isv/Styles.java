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

package com.cordys.tools.ant.isv;

/**
 * Styles contents - A ISV Package Content type which defines what styles
 * should be handled by the ISV Package creator. Typical format of content:
 * <pre>
 *  &lt;styles description=&quot;Styles&quot; loader=&quot;com.eibus.contentmanagement.ISVSOAPManager&quot;&gt;
 *  	&lt;SOAP:Envelope xmlns:SOAP=&quot;http://schemas.xmlsoap.org/soap/envelope/&quot;&gt;
 *  		&lt;SOAP:Body&gt;
 *  			&lt;UpdateXMLObject xmlns=&quot;http://schemas.cordys.com/1.0/xmlstore&quot;&gt;
 *  				&lt;tuple key=&quot;&quot; unconditional=&quot;true&quot; version=&quot;isv&quot;&gt;
 *  					&lt;new&gt;
 *  						&lt;StyleSheet name=&quot;&quot;&gt;
 *  						:
 *  						&lt;/StyleSheet&gt;
 *  					&lt;/new&gt;
 *  				&lt;/tuple&gt;
 *  			&lt;/UpdateXMLObject&gt;
 *  		&lt;/SOAP:Body&gt;
 *  	&lt;/SOAP:Envelope&gt;
 *   &lt;SOAP:Envelope&gt;
 *   :
 *   &lt;/SOAP:Envelope&gt;
 *  &lt;/styles&gt;
 * </pre>
 *
 * @author knayak, msreejit
 */
public class Styles extends XMLStore
    implements ISVContentHandler
{
    /**
     * The xpath pattern to be matched in the content XML passed.
     */
    private static final String CONTENT_TAG_MATCH_PATTERN = "<styles><style>";
    /**
     * The Actual root of the content.
     */
    private static final String CONTENT_INTERNAL_ROOT_TAG = "StyleSheet";
    /**
     * The root tag of the ISV content to be created.
     */
    private static final String ISV_CONTENT_ROOT_TAG = "styles";
    /**
     * The root tag of the developer content.
     */
    private static final String CONTENT_ROOT_TAG = "styles";
    /**
     * The description of the isv content loader.
     */
    private static final String ISV_CONTENT_DESCRIPTION = "XMLStore";

    /**
     * Default Constructor
     */
    public Styles()
    {
        super();
        setContentRootTag(CONTENT_ROOT_TAG);
        setContentTagMatchPattern(CONTENT_TAG_MATCH_PATTERN);
        setInternalRootTag(CONTENT_INTERNAL_ROOT_TAG);
        setIsvContentDescription(ISV_CONTENT_DESCRIPTION);
        setIsvContentRootTag(ISV_CONTENT_ROOT_TAG);
    }
}
