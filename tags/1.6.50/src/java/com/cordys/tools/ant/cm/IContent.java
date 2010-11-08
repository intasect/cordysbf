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

import java.io.File;

/**
 * Interface for Content. The commonality between the contentmanager and isv
 * tasks  are managed through this interface.
 *
 * @author msreejit
 */
public interface IContent
{
    /**
     * Property name for selecting a single content item.
     */
    public static final String SINGLE_CONTENT_PROPERTY = "single.content";
    /**
     * Property name for selecting a single content files.
     */
    public static final String SINGLE_CONTENT_FILE_PROPERTY = "single.content.file";    
    /**
     * type roles
     */
    public static final String TYPE_ROLES = "roles";
    /**
     * type methodsets
     */
    public static final String TYPE_METHODSETS = "methodsets";
    /**
     * type xmlstore
     */
    public static final String TYPE_XMLSTORE = "xmlstore";
    /**
     * type soapnodes
     */
    public static final String TYPE_SOAPNODES = "soapnodes";
    /**
     * type menus
     */
    public static final String TYPE_MENUS = "menus";
    /**
     * type toolbars
     */
    public static final String TYPE_TOOLBARS = "toolbars";
    /**
     * type applicationconnectors
     */
    public static final String TYPE_APPLICATIONCONNECTOR = "applicationconnectors";
    /**
     * type styles
     */
    public static final String TYPE_STYLES = "styles";
    /**
     * Type XForms (from XMLStore).
     */
    public static final String TYPE_XFORMS = "xforms";
    /**
     * Type Studio business process models.
     */
    public static final String TYPE_STUDIO_BPMS = "studio-bpms";
    /**
     * Type Studio XForms.
     */
    public static final String TYPE_STUDIO_XFORMS = "studio-xforms";
    /**
     * Type localizations.
     */
    public static final String TYPE_LOCALIZATIONS = "localizations";
    /**
     * Type CoBOC folder content (templates, mappings, etc). This is
     * the old CoBOC implementation.
     */
    public static final String TYPE_COBOC_FOLDERS = "coboc-folders";
    /**
     * Type for most of CoBOC (e.g. scripts are missing). This is the
     * new CoBOC implementation.
     */
    public static final String TYPE_COBOC2 = "coboc2";
    /**
     * Type for X-AS realted content (both repository and runtime).
     */
    public static final String TYPE_XAS = "xas";
    /**
     * Type for WsAppServer content (both repository and runtime). This
     * uses the XMLStore.
     */
    public static final String TYPE_WSAPPSERVER = "wsappserver";
    /**
     * Type for organization and authenticated users.
     */
    public static final String TYPE_USERS = "users";
    /**
     * Type for the XReports content.
     */
    public static final String TYPE_XREPORTS = "xreports";

    /**
     * Returns the flat contentfile in which the contents are placed.
     * Either contentfile or dir attribute should be specified. Will return
     * null if not set.
     *
     * @return The contentfile where the contents are placed.
     */
    public abstract File getContentFile();

    /**
     * Returns the handler for the content.
     *
     * @return The handler for the content.
     */
    public abstract String getHandler();

    /**
     * Returns the type of content.
     *
     * @return The type of the content.
     */
    public abstract String getType();

    /**
     * Sets the flat contentfile in which the contents can be placed.
     *
     * @param file The contentfile where the contents can be placed.
     */
    public abstract void setContentFile(File file);

    /**
     * Sets the handler for the content.
     *
     * @param handler The handler for the content.
     */
    public abstract void setHandler(String handler);

    /**
     * Sets the type of content.
     *
     * @param type The type of the content.
     */
    public abstract void setType(String type);
}
