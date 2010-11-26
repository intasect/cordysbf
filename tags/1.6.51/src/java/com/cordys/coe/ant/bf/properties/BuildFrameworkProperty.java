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
 package com.cordys.coe.ant.bf.properties;

import java.util.LinkedHashMap;

/**
 * This enum describes all properties that can be set within the buildframework.
 * 
 * @author pgussow
 */
public enum BuildFrameworkProperty
{
    //Compiler options.
    JAVAC_DEBUG("javac.debug", "Whether or not to compile the classes in debug mode."),
    JAVAC_DEPRECATION("javac.deprecation", "Whether or not to compile the classes and show deprecation warnings."),
    JAVAC_OPTIMIZE("javac.optimize", "Whether or not to optimize the classes."),
    JAVAC_SOURCE_VERSION("javac.source.version", "The Java source version."),
    JAVAC_TARGET_VERSION("javac.target.version", "The Java target version."),
    //Javadoc options.
    JAVADOC_AUTHOR("javadoc.author", "Whether or not to include the author tag in the generated documentation."),
    JAVADOC_PRIVATE("javadoc.private", "Whether or not to include the private methods in the generated documentation."),
    JAVADOC_USE("javadoc.use", "Whether or not to create class and package usage pages."),
    JAVADOC_VERSION("javadoc.version", "Whether or not to include the version tag in the generated documentation."),
    JAVADOC_WINDOWTITLE("javadoc.windowtitle", "The window title in the generated documentation."),
    JAVADOC_PACKAGENAME("javadoc.packagename", "The base package of which to generate JavaDocs"),
    //ISV packaging properties
    ISV_OWNER("isv.owner", "The owner of the ISV package that is created."),
    ISV_PRODUCT_NAME("isv.productname", "The product name of the ISV package that is created."),
    ISV_VERSION("isv.version", "The version of the ISV package that is created."),
    ISV_NAME("isv.name", "The name of the ISV package that is created (The final DN)."),
    ISV_FILENAME("isv.filename", "The filename of the ISV package that is created."),
    ISV_CONTENT("isv.content", "Specifies the types of content that should be packaged within the ISV package."),
    CONTENT_FILTER("content.filter", "Defines the filter of the different types of content that the ContentManager task should handle."),
    //Cordys installation related userdn/orgdn
    ISV_USERDN("isv.userdn", "The DN of the user to use when sending ISV related requests to Cordys."),
    CONTENT_USERDN("content.userdn", "The DN of the user to use when sending content related requests to Cordys."),
    ISV_ORG("isv.org", "The organization used for creating an ISV package."),
    CONTENT_ORG("content.org", "The organization used for transferring content to and from Cordys."),
    PLATFORM_VERSION_STRING("platform.version.string", "BCP version string", "Used to override the BCP version. If not set it is read from the server. Format is: BCP 4.2 C2 FP4"),
    //Source folders
    SRC_DIR("src.dir", "The location of the src folder"),
    SRC_JAVA("src.java", "The location of the java source folder (./src/java)"),
    SRC_WEB("src.web", "The location of the web source folder (./src/web)"),
    SRC_DBSCHEMA("src.dbschema", "The location of the dbschema source folder (./src/dbschema)"),
    SRC_CONTENT("src.content", "The location of the Cordys content source folder (./src/content)"),
    SRC_CONTENT_APPCON("src.content.appcon", "The location of the application connectors source folder (./src/content/applicationconnectors)"),
    SRC_CONTENT_COBOC("src.content.coboc", "The location of the CoBOC source folder (./src/content/coboc)"),
    SRC_CONTENT_COBOC_FOLDERS("src.content.coboc.folders", "The location of the CoBOC folders source folder (./src/content/coboc/folders)"),
    SRC_CONTENT_DBSCHEMA("src.content.dbschema", "The location of the dbschema source folder (./src/content/dbschema)"),
    SRC_CONTENT_ISV("src.content.isv", "The location of the ISV packaging control files folder (./src/content/isv)"),
    SRC_CONTENT_MENUS("src.content.menus", "The location of the menus source folder (./src/content/menus)"),
    SRC_CONTENT_METHODSETS("src.content.methodsets", "The location of the method sets source folder (./src/content/methodsets)"),
    SRC_CONTENT_ROLES("src.content.roles", "The location of the roles source folder (./src/content/roles)"),
    SRC_CONTENT_SOAP_NODES("src.content.soapnodes", "The location of the SOAP nodes source folder (./src/content/soapnodes)"),
    SRC_CONTENT_STUDIO("src.content.studio", "The location of the studio source folder (./src/content/studio)"),
    SRC_CONTENT_STUDIO_FLOWS("src.content.studio.flows", "The location of the BPM flows source folder (./src/content/studio/flows)"),
    SRC_CONTENT_STUDIO_XFORMS("src.content.studio.xforms", "The location of the XForms source folder (./src/content/studio/xforms)"),
    SRC_CONTENT_STYLES("src.content.styles", "The location of the styles source folder (./src/content/styles)"),
    SRC_CONTENT_TOOLBARS("src.content.toolbars", "The location of the toolbars source folder (./src/content/toolbars)"),
    SRC_CONTENT_XMLSTORE("src.content.xmlstore", "The location of the XML store source folder (./src/content/xmlstore)"),
    SRC_CONTENT_XAS("src.content.xas", "The location of the WsApp-Server source folder (./src/content/xas)"),
    SRC_CONTENT_LOCALIZATIONS("src.content.localizations", "The location of the localizations source folder (./src/content/localization)"),
    SRC_CONTENT_XREPORTS("src.content.xreports", "The location of the XReports source folder (./src/content/xreports)"),
    SRC_SITE("src.site", "The location of the custom project site files (./src/site)"),
    //Test related folders
    TEST_DIR("test.dir", "The location of the base folder for all test-related sources."),
    TEST_JAVA("test.java", "The location of the base folder for all test-related Java sources."),
    //Documentation related folders
    DOCS_DIR("docs.dir", "The location base for all documentation related content."),
    DOCS_INTERNAL("docs.internal", "The location for all internal projetc documentation.", "Note: Content in this folder will not be packaged in the ISV."),
    DOCS_EXTERNAL("docs.external", "The location for all external projetc documentation.", "Note: Content in this folder WILL be packaged in the ISV."),
    //Other path-related properties
    USER_CLASSDIR("user.classdir", "The location where the .class files will be published when tocordys classes is executed."),
    LIB_DIR("lib.dir", "The location of the project specific java libraries folder (./lib)"),
    LIB_CONFIG("lib.config", "Holds the folder in which configuration files with regard to the build framework.", "The files in this folder contain information about the state of the project. i.e. which version it supports. Also the cehcksumfile is stored here."),
    LIB_CHECKSUM_FILE("lib.checksum.file", "The file that holds the checksums of each file."),
    LIB_BF_VERSION_FILE("lib.bf.version.file", "The file that contains the version of the buildframework that the current project was last updated to."),
    SDK_DIR("sdk.dir", "The location of the base folder of the build framework related libraries (./sdk)"),
    SDK_LIB_DIR("sdk.lib.dir", "The location of the base folder of the build framework related Java libraries (./sdk/lib)"),
    SDK_BUILD_DIR("sdk.build.dir", "The location of the folder of the build framework with the buildfiles (./sdk/build)"),
    SDK_BUILD_SITE("sdk.build.site", "The location of the folder of the project site files (./sdk/build/site)"),
    PLATFORM_DIR("platform.dir", "The location of the folder containing all the platform binaries."),
    //Build folders
    BUILD_DIR("build.dir", "The location of the build folder (./build)"),
    BUILD_CLASSES("build.classes", "The location of the build classes folder (./build/classes)"),
    BUILD_JAR("build.jar", "The location of the build jar folder (./build/jar)"),
    BUILD_WEB("build.web", "The location of the build web folder (./build/web)"),
    BUILD_TEST("build.test", "The location of the build test folder (./build/test)"),
    BUILD_DOCS("build.docs", "The location of the build docs folder (./build/documentation)"),
    BUILD_APIDOCS("build.apidocs", "The location of the build apidocs folder (./build/apidocs)"),
    BUILD_ISV("build.isv", "The location of the build isv folder (./build/isv)"),
    BUILD_CONTENT("build.content", "The location of the build content folder (./build/content)"),
    BUILD_JUNITREPORTS("build.junitreports", "The location of the build junitreports folder (./build/junit-reports)"),
    BUILD_TEST_CLASSES("build.test.classes", "The location of the build test classes folder (./build/testclasses)"),
    BUILD_BIN("build.bin", "The location of the build bin folder (./build/bin)"),
    BUILD_LOCALIZATIONS("build.localizations", "The location of the build localizations folder (./build/localization)"),
    BUILD_XREPORTS("build.xreports", "The location of the build XReports folder (./build/xreports)"),
    BUILD_SITE("build.site", "The location of the build site folder (./build/site)"),
    //Cobertura related build properties.
    BUILD_COBERTURA("build.cobertura", "The location of the build folder for Cobertura reports."),
    BUILD_COBERTURA_INSTRUMENTED("build.cobertura.instrumented", "The location of the build folder for Cobertura 'instrumented' reports."),
    BUILD_COBERTURA_REPORT("build.cobertura.report", "The location of the build folder for the actual Cobertura generated reports."),
    //Prefix and Postfix for Snapshot and release building.
    SNAPSHOT_VERSION_PREFIX("snapshot.version.prefix", "The prefix for snapshot versions."),
    SNAPSHOT_VERSION_POSTFIX("snapshot.version.postfix", "The postfix for snapshot versions."),
    RELEASE_VERSION_PREFIX("release.version.prefix", "The prefix for release versions."),
    RELEASE_VERSION_POSTFIX("release.version.postfix", "The prefix for release versions."),
    //Version file generation properties.
    VERSIONFILE_GENERATE("versionfile.generate", "Indicates whether or not the version file should be generated"),
    VERSIONFILE_CLASS_PACKAGE("versionfile.class.package", "The package in which the version class should be put."),
    VERSIONFILE_CLASS_NAME("versionfile.class.name", "The name of the version class."),
    VERSIONFILE_PRODUCTNAME("versionfile.productname", "The product name that should be displayed in the version file."),
    VERSIONFILE_TEMPLATE("versionfile.template", "The location of the template to use for the version class."),
    //Dist folders
    DIST_DIR("dist.dir", "The location of the dist folder (./dist)"),
    DIST_DOCS("dist.docs", "The location of the dist docs folder (./dist/docs)"),
    DIST_APIDOCS("dist.apidocs", "The location of the dist apidocs folder (./dist/apidocs)"),
    DIST_SETUP("dist.setup", "The location of the dist setup folder (./dist/setup)"),
    DIST_INCLUDE_SOURCES("dist.include.sources", "Whether or not to include a zipped version of the sources in the distribution."),
    DIST_SITE("dist.site", "The location of the dist project site folder (./dist/site)"),
    DIST_REPORTS_JUNIT("dist.reports.junit", "The location of the dist JUnit reports folder (./dist/reports/junit)"),
    DIST_REPORTS_COBERTURA("dist.reports.cobertura", "The location of the dist Cobertura reports folder (./dist/reports/cobertura)"),
    //SubVersion related properties.
    SVN_PROJECT("svn.project", "The SubVersion base URL of the project.", "This URL points to the base of the project. Underneath this URL will be the main and branches folder."),
    SVN_MAIN("svn.main", "The SubVersion URL of the main (trunk)."),
    SVN_BRANCHES("svn.branches", "The SubVersion URL of the branches."),
    SVN_LABEL("svn.label", "The text to prefex to the version number when creating a new branch."),
    SVN_USERNAME("svn.username", "The username to use for connecting to SubVersion.", "It defaults to the current user."),
    SVN_PASSWORD("svn.password", "The password to use for connecting to SubVersion."),
    //Experimental: for future use: Maven properties.
    SKIP_PUBLISH("skip.publish", ""),
    //Miscellaneous properties.
    SDK_BUILD_FRAMEWORK_DIR("sdk.build.framework.dir", "The location of the different level for the buildfiles."),
    XMLSTORE_FORMAT_CONTENT("xmlstore.format.content", "This controls wheter all XMLStore contents are pretty printed when exported from BCP."),
    JUNIT_CUSTOM_CSS("junit.custom.css", "Custom stylesheet for the JUnit reports."),
    BUILD_STUDIO_ISV_FILENAME("build.studio.isv.filename", "The resulting Studio vcmdata file that has all the vcmdata files merged into it."),
    CORDYS_HOME("cordys.home", "The location of the Cordys installation folde rof the server you are developing on.", "This is needed since some files (like localizations) are copied to the filesystem directly."),
    //CoBOC and Studio specific properties.
    COBOC_ROOT_PATH("coboc.root.path", "CoBOC folder content path filter. Used to limit the operations to only a specific subtree."),
    COBOC_CONTENT("coboc.content", "Specify the different types of CoBOC content that your project has. Removing content from here will disable the reading of that type from BCP and file system."),
    COBOC_SCHEDULES_AUTODEPLOY("coboc.schedules.autodeploy", "Specifies if CoBOC schedule templates put in to an ISV package with autodeploy option."),
    COBOC_USE_FILETYPES("coboc.use.filetypes", "Indicates whether or not the CoBOC handler should use filte types."),
    STUDIO_BPM_ROOT_PATH("studio.bpm.root.path", "Studio BPM content root path. Used to limit the operations to only a specific subtree."),
    STUDIO_BPM_EXPORT_SUBPROCESSES("studio.bmp.export.subprocesses", "Determines if BPM vcmdata files has linked submodels included."),
    STUDIO_XFORMS_ROOT_PATH("studio.xforms.root.path", "Studio XForms content root path. Used to limit the operations to only a specific subtree."),
    STUDIO_EXPORT_VERSIONS("studio.export.versions", "Specifies versions for exporting Studio XForms and BPM's. Multiple version can be separated with a semi-colon. If not set, all versions will be exported."), 
    STUDIO_XFORMS_EXPORT_FORMATXML("studio.xforms.export.formatxml", "Specifies if Studio XForms are pretty printed when exported from the server. Setting this to 'true' can help comparing two version more easily."),
    XAS_PACKAGE("xas.package", "Set this property to the actual XAS package that is part of this project. Multiple packages can be separated with a semi-colon.", "Note: This is not needed for C2 and up."),
    RELEASE_REPOSITORY_PATH("release.repository.path", "Path of the project release repository where all release build contents will be copied. By default this is a local path but for shared projects this should be a location on a file server, e.g. \\\\my-server\\shared-location\release-repository. If this property is not set, release builds are not copied."),
    USING_SVN("using.svn", "This property indicates whether or not the project is under subversion. When this property is set all svn tasks will be executed. To indicate a project that is not under subversion remove this property."),
    HANDLER_XAS("handler.xas", "Specifies the WsAppServer handler to be used. Valid values are: xas - Ws-Appserver files in <CORDYS_HOME>\bsf wsappserver - Ws-Appserver files in XMLStore (Cordys C2 and later). This is default."),
    //For the new Website generation module
    BUILD_FORCE_CHANGELOG("build.force.changelog", "If 'true', the release build checks that the changelog has been updated for this release."),
    PROJECT_SITE_ENABLED("project.site.enabled", "Indicates whether or not the website should be generated."),
    PROJECT_SITE_URL("project.site.url", "The URL for the project website."),
    PROJECT_SITE_BUILDS_DIRNAME("project.site.builds.dirname", "Name of the site folder where all builds are stores (builds)"),
    PROJECT_SITE_BUILDS_RELPATH_PREFIX("project.site.builds.relpath.prefix", "Name of the build folder prefix under the builds folder (build-)"),
    PROJECT_SITE_BUILDS_ROOT_URL("project.site.builds.root.url", "Full URL of the 'builds' folder"),
    PROJECT_SITE_DEPLOY_FTP_HOSTNAME("project.site.deploy.ftp.hostname", "Hostname of the FTP server used for deploying the site."),
    PROJECT_SITE_DEPLOY_FTP_PORT("project.site.deploy.ftp.port", "Port of the FTP server used for deploying the site (21)"),
    PROJECT_SITE_DEPLOY_FTP_USERNAME("project.site.deploy.ftp.username", "FTP user name"),
    PROJECT_SITE_DEPLOY_FTP_PASSWORD("project.site.deploy.ftp.password", "FTP user password"),
    //Connection related properties.
    DEV_SERVER("dev.server", "The name of the server on which the project is bing developed."),
    CONNECTION_MODE("connection.mode", "The type of connection used to communicate with Cordys (bus / webgateway"),
    WEBGATEWAY_USER("webgateway.user", "webgateway - The username to use for authenticating to the webgateway"),
    WEBGATEWAY_PASSWORD("webgateway.password", "webgateway - The password for authenticating to the webgateway"),
    WEBGATEWAY_URL("webgateway.url", "webgateway - The URL where the webgateway is running."),
    LDAP_SERVER("ldap.server", "bus - The name of the server to connect to."),
    LDAP_PORT("ldap.port", "bus - The portnumber on which the LDAP server is running."),
    LDAP_USER("ldap.user", "bus - The username for the LDAP server."),
    LDAP_PASSWORD("ldap.password", "bus - The password for the ldap server."),
    BUS_LDAP_PROCESSOR_SSL("bus.ldap.processor.ssl", "bus - Indicates whether or not the LDAP uses SSL."),
    BUS_SSL_TRUSTSTOREPASSWORD("bus.ssl.truststorepassword", "bus - The password of the trust store for connecting to an SSL enabled LDAP directly."),
    BUS_SSL_TRUSTSTORE("bus.ssl.truststore", "bus - The location of the trust store for connecting to an SSL enabled LDAP directly."),
    //Misc properties
    INCLUDE_SOURCE("include.source", "Indicates whether or not the whole project should be packaged for deployment"),
    //General project properties.
    PRODUCT_VENDOR("product.vendor", "The vendor of this product."),
    PROJECT_VERSION("project.version", "The version of the product.", "The version number consists of a major and minor version. The build-targets will take care of the patch- and milestone number"),
    PROJECT_WEB_NAME("project.web.name", "The deploy-location of the web files within <cordys_install_dir>/Web"),
    PROJECT_JAR_NAME("project.jar.name", "The name of the project's jar file."),
    PROJECT_DEPLOY("project.deploy", "The name of the folder in which the project should be deployed within the Cordys installation folder."),
    PROJECT_NAME("project.name", "The name of the project.",
                 "This property is used to identify the name of the project. The project name and version will be combined to name the jar file."),
    // Determine how content which content types are allowed for fromcordys, tocordys and deletecordys.
    CONTENT_ALLOW_FROMCORDYS("content.allow.fromcordys", "Lists content types allowed for fromcordys command.", "Lists content types allowed for fromcordys command. Format is: content.allow.fromcordys = type[allow|deny|prompt]"),
    CONTENT_ALLOW_TOCORDYS("content.allow.tocordys", "Lists content types allowed for tocordys command.", "Lists content types allowed for fromcordys command. Format is: content.allow.tocordys = type[allow|deny|prompt]"),
    CONTENT_ALLOW_DELETECORDYS("content.allow.deletecordys", "Lists content types allowed for deletecordys command.", "Lists content types allowed for fromcordys command. Format is: content.allow.deletecordys = type[allow|deny|prompt]");
    /**
     * Holds the name of the property in the properties file.
     */
    private String m_sName;
    /**
     * Holds teh short description of the property.
     */
    private String m_sShort;
    /**
     * Holds the long description of the property.
     */
    private String m_sLong;

    /**
     * Constructor. Creates the enum with the given properties.
     * 
     * @param sName The name of the property.
     * @param sShort The short description.
     * @param sLong The long description.
     */
    private BuildFrameworkProperty(String sName, String sShort, String sLong)
    {
        m_sName = sName;
        m_sShort = sShort;
        m_sLong = sLong;
    }
    
    /**
     * Constructor. Creates the enum with the given properties.
     * 
     * @param sName The name of the property.
     * @param sShort The short description.
     * @param sLong The long description.
     */
    private BuildFrameworkProperty(String sName, String sShort)
    {
        this(sName, sShort, sShort);
    }

    /**
     * This method gets the name of the property.
     *
     * @return The name of the property.
     */
    public String getName()
    {
        return m_sName;
    }

    /**
     * This method gets the short description.
     *
     * @return The short description.
     */
    public String getShortDescription()
    {
        return m_sShort;
    }

    /**
     * This method gets the long description.
     *
     * @return The long description.
     */
    public String getLongDescription()
    {
        return m_sLong;
    }
    
    /**
     * Holds the mutex.
     */
    private static Object s_oMutex = new Object();
    /**
     * Holds the list of all properties.
     */
    private static LinkedHashMap<String, BuildFrameworkProperty> s_lhmAllProps = null;
    
    /**
     * This method returns a list with all property names.
     * 
     * @return The list with all proeprty names.
     */
    private static LinkedHashMap<String, BuildFrameworkProperty> getAllProperties()
    {
        if (s_lhmAllProps == null)
        {
            synchronized (s_oMutex)
            {
                if (s_lhmAllProps == null)
                {
                    s_lhmAllProps = new LinkedHashMap<String, BuildFrameworkProperty>();
                    BuildFrameworkProperty[] abfpProps = values();
                    for (int iCount = 0; iCount < abfpProps.length; iCount++)
                    {
                        BuildFrameworkProperty bfpProp = abfpProps[iCount];
                        s_lhmAllProps.put(bfpProp.getName(), bfpProp);
                    }
                }
            }
        }
        return s_lhmAllProps;
    }
    
    /**
     * This method returns if the given property is defined in this enum.
     * 
     * @param sPropertyName The name of the property.
     * 
     * @return Whether or not this property is defined.
     */
    public static boolean isProperty(String sPropertyName)
    {
        boolean bReturn = false;
        
        LinkedHashMap<String, BuildFrameworkProperty> lhmAllprops = getAllProperties();
        
        bReturn = lhmAllprops.containsKey(sPropertyName);
        
        return bReturn;
    }
}
