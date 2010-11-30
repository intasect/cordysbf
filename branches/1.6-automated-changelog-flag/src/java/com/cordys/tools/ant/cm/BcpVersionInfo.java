/**
 * Copyright 2007 Cordys R&D B.V. 
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

import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.exceptions.XMLWrapperException;
import com.cordys.coe.util.xml.dom.XMLHelper;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPath;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Contains version information about the current BCP server.
 *
 * @author  mpoyhone
 */
public class BcpVersionInfo
{
    /**
     * Default version which will be used if no version is defined.
     */
    private static BcpVersionInfo defaultVersion;
    /**
     * Version matchers read from the configuration XML.
     */
    private static List<VersionMatcher> matchers;
    /**
     * Build number.
     */
    private String buildNumber;
    /**
     * Fix pack number.
     */
    private String fixPackNumber;
    /**
     * Major platform version, e.g. C3.
     */
    private EBcpVersion majorVersion;

    /**
     * Creates version from BCP buildinfo XML.
     *
     * @param   infoRoot  build info XML root node.
     *
     * @return  Created version object or <code>null</code> if version could not be determined.
     *
     * @throws  Exception  Thrown if the parsing failed.
     */
    public static BcpVersionInfo createFromInstallationInfo(int infoRoot)
                                                     throws Exception
    {
        if (matchers == null)
        {
            VersionMatcher.loadMatchers();
        }

        int buildInfoNode = Find.firstMatch(infoRoot, "<><tuple><old><buildinfo>");
        int patchInfoNode = Find.firstMatch(infoRoot, "<><tuple><old><patchinfo>");

        if (buildInfoNode == 0)
        {
            return null;
        }

        String buildVersion = Node.getDataElement(buildInfoNode, "version", "");
        String buildNumber = Node.getDataElement(buildInfoNode, "build", "");

        if ((buildVersion == null) || (buildVersion.length() == 0))
        {
            return null;
        }

        VersionMatcher matchedVersion = VersionMatcher.findVersion(buildVersion, buildNumber,
                                                                   patchInfoNode);

        if (matchedVersion == null)
        {
            return defaultVersion;
        }

        BcpVersionInfo res = new BcpVersionInfo();

        res.majorVersion = matchedVersion.majorVersion;
        res.buildNumber = buildNumber;
        res.fixPackNumber = matchedVersion.fixPack;

        return res;
    }

    /**
     * Creates version object from a property.
     *
     * @param   propValue  Property value.
     *
     * @return  Created version.
     *
     * @throws  Exception  Thrown if the parsing failed.
     */
    public static BcpVersionInfo createFromProperty(String propValue)
                                             throws Exception
    {
        if (matchers == null)
        {
            VersionMatcher.loadMatchers();
        }

        Pattern p = Pattern.compile("^\\s*BCP\\s+(\\d+)\\.(\\d+)(?:\\s*([\\w\\d]+))?(?:\\s+FP(\\d+))?\\s*");
        Matcher m = p.matcher(propValue.trim());

        if (!m.matches())
        {
            throw new IllegalArgumentException("Invalid BCP property string: " + propValue);
        }

        BcpVersionInfo info = new BcpVersionInfo();
        String enumString = String.format("BCP%s%s_%s", m.group(1), m.group(2), m.group(3));

        info.setMajorVersion(EBcpVersion.valueOf(enumString));

        if (m.group(4) != null)
        {
            info.setFixPackNumber(m.group(4));
        }
        info.setBuildNumber(null);

        return info;
    }

    /**
     * Creates a version from the BPC version and a build number.
     *
     * @param   buildVersion  BCP version.
     * @param   buildNumber   build number.
     *
     * @return  Version object.
     *
     * @throws  Exception  Thrown if the parsing failed.
     */
    public static BcpVersionInfo createFromVersionAndBuildNumber(String buildVersion,
                                                                 String buildNumber)
                                                          throws Exception
    {
        if (matchers == null)
        {
            VersionMatcher.loadMatchers();
        }

        VersionMatcher matchedVersion = VersionMatcher.findVersion(buildVersion, buildNumber, 0);

        if (matchedVersion == null)
        {
            return defaultVersion;
        }

        BcpVersionInfo res = new BcpVersionInfo();

        res.majorVersion = matchedVersion.majorVersion;
        res.buildNumber = buildNumber;
        res.fixPackNumber = matchedVersion.fixPack;

        return res;
    }

    /**
     * Returns the default version.
     *
     * @return  Default version object.
     *
     * @throws  Exception  Thrown if the version configuration XML parsing failed.
     */
    public static BcpVersionInfo getDefault()
                                     throws Exception
    {
        if (matchers == null)
        {
            VersionMatcher.loadMatchers();
        }

        return defaultVersion;
    }

    /**
     * Returns the buildNumber.
     *
     * @return  Returns the buildNumber.
     */
    public String getBuildNumber()
    {
        return buildNumber;
    }

    /**
     * Returns the fixPackNumber.
     *
     * @return  Returns the fixPackNumber.
     */
    public String getFixPackNumber()
    {
        return fixPackNumber;
    }

    /**
     * Returns the majorVersion.
     *
     * @return  Returns the majorVersion.
     */
    public EBcpVersion getMajorVersion()
    {
        return majorVersion;
    }

    /**
     * Compares the given major version to this version and returns <code>true</code> if the given
     * version is later or equal than this version.
     *
     * @param   version  to be compared.
     *
     * @return  Comparison result.
     */
    public boolean isLaterThan(EBcpVersion version)
    {
        return majorVersion.ordinal() >= version.ordinal();
    }

    /**
     * Sets the buildNumber.
     *
     * @param  buildNumber  The buildNumber to be set.
     */
    public void setBuildNumber(String buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    /**
     * Sets the fixPackNumber.
     *
     * @param  fixPackNumber  The fixPackNumber to be set.
     */
    public void setFixPackNumber(String fixPackNumber)
    {
        this.fixPackNumber = fixPackNumber;
    }

    /**
     * Sets the majorVersion.
     *
     * @param  majorVersion  The majorVersion to be set.
     */
    public void setMajorVersion(EBcpVersion majorVersion)
    {
        this.majorVersion = majorVersion;
    }

    /**
     * @see  java.lang.Object#toString()
     */
    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder(100);

        sb.append(majorVersion.getDisplayString());

        if (fixPackNumber != null)
        {
            sb.append(" Fixpack ").append(fixPackNumber);
        }

        if (buildNumber != null)
        {
            sb.append(" [Build ").append(buildNumber).append("]");
        }

        return sb.toString();
    }

    /**
     * Contains version matching configuration.
     *
     * @author  mpoyhone
     */
    private static class VersionMatcher
    {
        /**
         * Build number end range.
         */
        int buildNumberEnd;
        /**
         * Build number start range.
         */
        int buildNumberStart;
        /**
         * Group index of build version fix pack number.
         */
        int buildVersionFixPackGroup;
        /**
         * Regex for parsing the build version.
         */
        Pattern buildVersionRegex;
        /**
         * Fix pack name.
         */
        String fixPack;
        /**
         * Hot fix number.
         */
        int hotfixNumber;
        /**
         * Major version.
         */
        EBcpVersion majorVersion;

        /**
         * Finds a version.
         *
         * @param   buildVersion    Build version.
         * @param   buildNumberStr  Number number.
         * @param   patchinfoNodes  Patch info nodes.
         *
         * @return  Thrown if the operation failed.
         */
        @SuppressWarnings("deprecation")
        static VersionMatcher findVersion(String buildVersion, String buildNumberStr,
                                          int patchinfoNodes)
        {
            int buildNumber = 0;
            int hotfixNumber = 0;

            try
            {
                buildNumber = Integer.parseInt(buildNumberStr);
            }
            catch (NumberFormatException nfe)
            {
                // If you installed a C3 hot fix which replaces the baseline your build number
                // will be something like 338.70583.
                Pattern pTemp = Pattern.compile("(\\d+)\\.(\\d+)");
                Matcher mMatcher = pTemp.matcher(buildNumberStr);

                if (mMatcher.find())
                {
                    buildNumber = Integer.parseInt(mMatcher.group(1));

                    String sTemp = mMatcher.group(2);

                    if (sTemp != null)
                    {
                        hotfixNumber = Integer.parseInt(sTemp);
                    }
                }
            }

            VersionMatcher latestMatching = null;

            // First search by the build number.
            for (VersionMatcher m : matchers)
            {
                Matcher regexMatcher = m.buildVersionRegex.matcher(buildVersion);

                if (!regexMatcher.matches())
                {
                    continue;
                }

                if (m.buildVersionFixPackGroup >= 0)
                {
                    // We are getting the fix pack number from the build version string.
                    String str = regexMatcher.group(m.buildVersionFixPackGroup);

                    if ((str != null) && (str.length() > 0))
                    {
                        m.fixPack = str.replaceFirst("^0+(.*)$", "$1");
                        m.hotfixNumber = hotfixNumber;
                        return m;
                    }
                }

                if ((buildNumber >= m.buildNumberStart) && (buildNumber <= m.buildNumberEnd))
                {
                    m.hotfixNumber = hotfixNumber;
                    return m;
                }

                if ((latestMatching == null) || (latestMatching.buildNumberEnd < m.buildNumberEnd))
                {
                    // Keep track of the latest version that matches the build version string,
                    // so we use the latest in that series if we don't have an exact match by
                    // the build number.
                    latestMatching = m;
                }
            }

            if (patchinfoNodes != 0)
            {
                // Next try to figure out the version by the fix pack node.
                XPath fixpackXPath = new XPath("./patch[starts-with(name/text(), 'Cordys BCP')]/version/text()");

                for (VersionMatcher m : matchers)
                {
                    if (!m.buildVersionRegex.matcher(buildVersion).matches())
                    {
                        continue;
                    }

                    String fixPack = fixpackXPath.evaluateStringResult(patchinfoNodes);

                    if (fixPack != null)
                    {
                        VersionMatcher res = new VersionMatcher();

                        res.majorVersion = m.majorVersion;
                        res.fixPack = fixPack.replaceFirst("^.*[0]*(\\d+)$", "$1");
                        res.hotfixNumber = hotfixNumber;
                        return res;
                    }
                }
            }

            // Return the latest best match.
            if (latestMatching != null)
            {
                latestMatching.hotfixNumber = hotfixNumber;
            }

            return latestMatching;
        }

        /**
         * Loads the matchers from the configuration XML.
         *
         * @throws  IOException          Thrown if the loading failed.
         * @throws  XMLWrapperException  Thrown if the loading failed.
         */
        static void loadMatchers()
                          throws IOException, XMLWrapperException
        {
            matchers = new ArrayList<VersionMatcher>(40);

            String tmp = FileUtils.readTextResourceContents("BcpVersions.xml",
                                                            BcpVersionInfo.class);
            Document doc = XMLHelper.createDocumentFromXML(tmp);
            NodeList matcherList = doc.getElementsByTagName("match");
            NodeList defaultNode = doc.getElementsByTagName("default");

            defaultVersion = new BcpVersionInfo();

            if (defaultNode.getLength() > 0)
            {
                String major = XMLHelper.getData(defaultNode.item(0), "@version");
                String fixpack = XMLHelper.getData(defaultNode.item(0), "@fixpack");
                String buildNumber = XMLHelper.getData(defaultNode.item(0), "@buildnumber");

                defaultVersion.majorVersion = ((major != null) ? EBcpVersion.valueOf(major)
                                                               : EBcpVersion.BCP42_C3);
                defaultVersion.fixPackNumber = (((fixpack != null) && (fixpack.length() > 0))
                                                ? fixpack : null);
                defaultVersion.buildNumber = buildNumber;
            }
            else
            {
                defaultVersion.majorVersion = EBcpVersion.BCP42_C3;
                defaultVersion.fixPackNumber = null;
                defaultVersion.buildNumber = "200";
            }

            for (int i = 0; i < matcherList.getLength(); i++)
            {
                org.w3c.dom.Node n = matcherList.item(i);
                String destVersion = XMLHelper.getData(n, "@version");
                String destFixpack = XMLHelper.getData(n, "@fixpack");
                String buildVersionRegex = XMLHelper.getData(n, "./buildVersionRegex/text()");
                String fixPackGroup = XMLHelper.getData(n, "./buildVersionRegex/@fixpack-group");
                String buildNumberStart = XMLHelper.getData(n, "./buildNumber/@start");
                String buildNumberEnd = XMLHelper.getData(n, "./buildNumber/@end");

                if ((destVersion == null) || (destVersion.length() == 0))
                {
                    throw new IllegalArgumentException("destversion is not set in BcpVersions.xml");
                }

                if ((buildVersionRegex == null) || (buildVersionRegex.length() == 0))
                {
                    throw new IllegalArgumentException("buildVersionRegex is not set in BcpVersions.xml");
                }

                VersionMatcher m = new VersionMatcher();

                m.majorVersion = EBcpVersion.valueOf(destVersion);
                m.fixPack = (((destFixpack != null) && (destFixpack.length() > 0)) ? destFixpack
                                                                                   : null);
                m.buildVersionRegex = Pattern.compile(buildVersionRegex);
                m.buildVersionFixPackGroup = ((fixPackGroup != null)
                                              ? Integer.parseInt(fixPackGroup) : -1);
                m.buildNumberStart = (((buildNumberStart != null) &&
                                       (buildNumberStart.length() > 0))
                                      ? Integer.parseInt(buildNumberStart) : Integer.MIN_VALUE);
                m.buildNumberEnd = (((buildNumberEnd != null) && (buildNumberEnd.length() > 0))
                                    ? Integer.parseInt(buildNumberEnd) : Integer.MAX_VALUE);

                matchers.add(m);
            }
        }
    }
}
