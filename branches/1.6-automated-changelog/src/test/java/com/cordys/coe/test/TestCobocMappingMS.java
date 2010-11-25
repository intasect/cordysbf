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

import java.util.regex.Matcher;

import com.cordys.tools.ant.cm.MethodsetsHandler;
import com.eibus.util.Base64;

public class TestCobocMappingMS
{
    /**
     * Main method.
     *
     * @param saArguments The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            String sTempOther = "<implementation"+
"\ttype=\"CGMAP\">"+
"\t<mapid>11931489397725</mapid>"+
"\t<mapname>/iBus/iPlan/mapping/iPlanConsignmentLegMapM2B</mapname>"+
"</implementation>";
            
            String sTemp = Base64.decode("CjxpbXBsZW1lbnRhdGlvbgoJdHlwZT0iQ0dNQVAiPgoJPG1hcGlkPjExOTY2OTMxMTU0OTUxPC9t\n" + 
"YXBpZD4KCTxtYXBuYW1lPi9pQnVzL2lQbGFuL21hcHBpbmcvaVBsYW5Db25zaWdubWVudExlZ01h\n" +
"cEIyTTwvbWFwbmFtZT4KPC9pbXBsZW1lbnRhdGlvbj4K");
            
            sTemp = sTempOther;
            
            System.out.println(sTemp);
            
            Matcher m = MethodsetsHandler.pCobocMapIdPattern.matcher(sTemp);
            
            if (m.matches())
            {
                System.out.println("true");
                for (int i = 0; i <= m.groupCount(); i++)
                {
                    System.out.println("Group " + i + ": " + m.group(i));
                }
            }
            else
            {
                System.out.println("False");
            }
            System.out.println("Pattern: " + MethodsetsHandler.pCobocMapIdPattern.pattern());
            System.out.println("Flags: " + MethodsetsHandler.pCobocMapIdPattern.flags());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
