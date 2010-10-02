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
package com.cordys.coe.ant.studio.content;

import java.util.regex.Pattern;

/**
 * Contains Studio specific constants.
 *
 * @author  mpoyhone
 */
public class StudioConstants
{
    /**
     * The default flow root folder.
     */
    public static final String DEFAULT_FLOW_ROOT_FOLDER = "/cordys/cas/vcm/studio/modelrepository";
    /**
     * The default XForms root folder.
     */
    public static final String DEFAULT_XFORMS_ROOT_FOLDER = "/cordys/cas/cam/studio/xformsrepository";
    /**
     * Property name of export versions string.
     */
    public static final String EXPORT_VERSION_PROPERTY = "studio.export.versions";
    /**
     * Property name of XForms root folder (relative to the DEFAULT_XFORMS_ROOT_FOLDER).
     */
    public static final String XFORMS_ROOTFOLDER_PROPERTY = "studio.xforms.root.path";
    /**
     * Property name of XForms root folder (relative to the DEFAULT_FLOW_ROOT_FOLDER).
     */
    public static final String STUDIO_ROOTFOLDER_PROPERTY = "studio.bpm.root.path";
    /**
     * Property name of export subprocesses flag.
     */
    public static final String EXPORT_SUBPROCESSES_PROPERTY = "studio.bmp.export.subprocesses";
    /**
     * Property name of check last modification timestamp flag.
     */
    public static final String CHECK_LASTMODIFIED_PROPERTY = "studio.bmp.export.check.lastmodified";
    /**
     * The default XForms translation root folder.
     */
    public static final String DEFAULT_XFORMS_TRANSLATION_ROOT_FOLDER = "/cordys/cas/cam/xforms/translation";
    /**
     * The default flow BPML root folder.
     */
    public static final String DEFAULT_FLOW_BPML_ROOT_FOLDER = "/cordys/cas/vcm/studio/bpml";
    /**
     * Pattern for extracting the filter path from ECX XForm key.
     */
    public static final Pattern ECX_XFORMS_FILTER_MATCH_PATTERN = Pattern.compile("^[/]?(.*)_[^_]+\\.(?:caf|mlm)$");
    /**
     * Pattern for extracting the filter path from file system XForm key.
     */
    public static final Pattern FILESYSTEM_XFORMS_FILTER_MATCH_PATTERN = Pattern.compile("^[/]?(.*)\\.caf$");
    /**
     * Pattern for extracting the filter path from file system XForm key.
     */
    public static final Pattern FILESYSTEM_XFORMS_MLM_FILTER_MATCH_PATTERN = Pattern.compile("^[/]?([^/].*[/])?mlm/([^/]+)\\.mlm$");
    /**
     * Pattern for extracting the filter path from ECX XForm key.
     */
    public static final Pattern ECX_FLOW_FILTER_MATCH_PATTERN = Pattern.compile("^[/]?(.*)_[^_]+\\.bpm$");
    /**
     * Pattern for extracting the filter path from file system XForm key.
     */
    public static final Pattern FILESYSTEM_FLOW_FILTER_MATCH_PATTERN = Pattern.compile("^[/]?(.*)_[^_]+\\.vcmdata$");
}
