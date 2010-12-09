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
package com.cordys.tools.ant.util;

import com.eibus.version.Version;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * This class wraps around the different Cordys version.
 *
 * @author pgussow
 */
public class CordysVersion
{
    /**
     * Identifies an unknown Cordys build.
     */
    public static int BCP_UNKNOWN = 1;
    /**
     * Identifies a Cordys C1 build.
     */
    public static int BCP_4_2_C1 = 1;
    /**
     * Identifies a Cordys C2 build.
     */
    public static int BCP_4_2_C2 = 2;
    /**
     * Identifies a Cordys C3 build.
     */
    public static int BCP_4_2_C3 = 3;
    /**
     * Holds all the combinations.
     */
    private static HashMap<String, Integer> s_hmVersions = new LinkedHashMap<String, Integer>();

    static
    {
        s_hmVersions.put("4.2_" + 170, BCP_4_2_C1);
        s_hmVersions.put("4.2_" + 407, BCP_4_2_C2);
        s_hmVersions.put("4.2_" + 425, BCP_4_2_C2);
        s_hmVersions.put("4.2_" + 508, BCP_4_2_C2);
        s_hmVersions.put("4.2_" + 538, BCP_4_2_C2);
        s_hmVersions.put("4.2_" + 572, BCP_4_2_C2);
    }

    /**
     * This method gets the Cordys version that runs within this JVM.
     *
     * @return The Cordys version of the wcp.jar which is in the classpath of
     *         this JVM.
     */
    public static int getCordysVersion()
    {
        int iReturn = BCP_UNKNOWN;

        iReturn = getCordysVersion(new Version().getVersion(),
                                   new Version().getBuild());

        return iReturn;
    }

    /**
     * This method gets the Cordys version of the version and build
     * number that is specified.
     *
     * @param sVersion The version number.
     * @param sBuildNr The build number.
     *
     * @return The Cordys version corresponding the passed on version and build
     *         number.
     */
    public static int getCordysVersion(String sVersion, String sBuildNr)
    {
        int iReturn = BCP_UNKNOWN;

        if (s_hmVersions.containsKey(sVersion + "_" + sBuildNr))
        {
            iReturn = s_hmVersions.get(sVersion + "_" + sBuildNr);
        }

        return iReturn;
    }
}
