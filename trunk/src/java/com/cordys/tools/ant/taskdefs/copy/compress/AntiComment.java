/**
 * Copyright 2004 Cordys R&D B.V. 
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
 *         File                :        AntiComment.java
 *         Author                :        vramesh@cordys.com
 *         Created on         :        Oct 4, 2004
 *        Description        :         Remove the comments and the white spaces from java script & HTML
 *
 *        Written by Ramesh (Collaborative Learning)
 *        Integrating to Build Framework by Manesh
 */
package com.cordys.tools.ant.taskdefs.copy.compress;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AntiComment
{
    /**
     * DOCUMENTME
     */
    public static String fullReport = "";
    /**
     * DOCUMENTME
     */
    public static int TotalNoofLines = 0;
    /**
     * DOCUMENTME
     */
    public static int TotalNoofBlankLines = 0;
    /**
     * DOCUMENTME
     */
    public static int TotalNoofExecutableCode = 0;
    /**
     * DOCUMENTME
     */
    public static int TotalNoofJSCommentLines = 0;
    /**
     * DOCUMENTME
     */
    public static int TotalNoofHTMLCommentLines = 0;
    /**
     * DOCUMENTME
     */
    public static long TotalOriginalSize = 0;
    /**
     * DOCUMENTME
     */
    public static long TotalCompressedSize = 0;
    /**
     * DOCUMENTME
     */
    String destinationFile;
    /**
     * DOCUMENTME
     */
    String sourceFile;

    //public static  void AntiCommentFunction(String sourceFile, String destinationFile, String copyRight)throws IOException
    public static void AntiCommentFunction(BufferedReader source,
                                           BufferedWriter destination,
                                           String copyRight)
                                    throws IOException
    {
        /*if(args.length!=2)
           {
                   System.out.println("\n\nInvalid No of arguments \n\n");
                   System.out.println("Usage :\n       C:\\>java AntiComment <Source File> <Destination File> ");
                   System.out.println("\n      Make Sure that your script Tag is \"<script language=javascript>\" \n with out any text on either side of it");
                   return;
           }*/
        int NoofLines = 0;
        int NoofBlankLines = 0;
        int NoofExecutableCode = 0;
        int NoofJSCommentLines = 0;
        int NoofHTMLCommentLines = 0;
        long originalSize = 0;
        long compressedSize = 0;

        //BufferedReader source = new BufferedReader(new FileReader(sourceFile));
        //RandomAccessFile source1=new RandomAccessFile(sourceFile,"r");
        //RandomAccessFile destination=new RandomAccessFile(destinationFile,"rw");
        //BufferedWriter destination=new BufferedWriter(new FileWriter(destinationFile));
        // Adds the copy right string to the file
        if ((copyRight != null) && !"".equals(copyRight))
        {
            destination.write(copyRight + "\n");
        }

        //TODO: This has to be done on the fly
        //File file =new File(sourceFile);
        //originalSize=file.length();

        String s = "";
        boolean isTagFound = false;
        ArrayList<String> tempList = new ArrayList<String>();
        String t = "";
        String u = "";

        //System.out.println("File processed is  "+sourceFile);
        //System.out.println("\n\n Relax!!! While  The Files  is Being Processed....\n\n");
        //System.out.println(" Removing the Single Line comments in the Java Script ....\n" );
        //System.out.println(" Removing the White Space and new Line char actor from HTML....\n" );
        boolean onceCheck = false;
        boolean htmlCommentStart = false;

        while (s != null)
        {
            s = source.readLine();
            NoofLines++;

            if (s != null)
            {
                s = s.trim();
            }

            //System.out.println(s);
            if ((s != null) && (s.length() == 0))
            {
                NoofBlankLines++;
            }

            if ((isTagFound == false) && (s != null))
            {
                if (s.equalsIgnoreCase("<script language=javascript>") ||
                        s.equalsIgnoreCase("<script language=\"javascript\">") ||
                        s.trim().equalsIgnoreCase("<script language=jscript>") ||
                        s.trim().equalsIgnoreCase("<script language=\"jscript\">") ||
                        s.trim().equalsIgnoreCase("<script>"))
                {
                    isTagFound = true;
                    onceCheck = true;
                }
                else
                {
                    // to find out weather its a external Js file attached
                    String sInLower = s.toLowerCase();

                    if (sInLower.indexOf("script") != -1)
                    {
                        int x = sInLower.indexOf("script");

                        if (sInLower.indexOf("language", x) != -1)
                        {
                            int y = sInLower.indexOf("language", x);

                            if ((sInLower.indexOf("javascript", y) != -1) ||
                                    (sInLower.indexOf("jscript", y) != -1))
                            {
                                if (sInLower.indexOf("src") == -1)
                                {
                                    isTagFound = true;
                                    onceCheck = true;
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                onceCheck = false;
            }

            if ((isTagFound == true) && (s != null) &&
                    (s.equalsIgnoreCase("</script>") ||
                    s.equalsIgnoreCase("</ script >") ||
                    s.equalsIgnoreCase("</ script>") ||
                    s.trim().equalsIgnoreCase("</script >") ||
                    s.trim().equalsIgnoreCase("< / script >")))
            {
                isTagFound = false;
            }

            //if(isTagFound==true && s!=null && !(s.trim().equalsIgnoreCase("<script language=javascript>")||s.trim().equalsIgnoreCase("<script language=\"javascript\">")||s.trim().equalsIgnoreCase("<script language=jscript>")||s.trim().equalsIgnoreCase("<script language=\"jscript\">")||s.trim().equalsIgnoreCase("<script>")))
            if ((isTagFound == true) && (s != null) && !onceCheck)
            {
                /* to avoid the html comment inside the scritp  <!-- --> */
                if (s.indexOf("<!--") == 0)
                {
                    s = s.substring(4);
                }

                if (s.indexOf("-->") != -1)
                {
                    int b = s.indexOf("-->");

                    if ((b + 3) == s.length())
                    {
                        s = s.substring(0, b);
                    }
                }

                String literal = "";
                boolean inQuote;
                boolean escaped;
                char c = 0;
                char quoteChar = 0;

                t = "";

                int j = 0;
                StringBuffer tTemp;

                j = 0;
                inQuote = false;
                escaped = false;
                tTemp = new StringBuffer(32000);

                /* replacing the code inside quotations with the _-n_- format*/
                while (j < s.length())
                {
                    c = s.charAt(j);

                    if (!inQuote)
                    {
                        if ((c == '"') || (c == '\''))
                        {
                            inQuote = true;
                            escaped = false;
                            quoteChar = c;
                            literal += c;
                        }
                        else
                        {
                            tTemp.append(c);
                        }
                    }

                    // Already in a string, look for end and copy characters.
                    else
                    {
                        if ((c == quoteChar) && !escaped)
                        {
                            inQuote = false;
                            literal += quoteChar;
                            tTemp.append("-_" + tempList.toArray().length +
                                         "-_");
                            tempList.add(literal);
                            literal = "";
                        }
                        else if ((c == '\\') && !escaped)
                        {
                            escaped = true;
                        }
                        else
                        {
                            escaped = false;
                        }

                        if (inQuote != false)
                        {
                            literal += c;
                        }
                    }
                    j++;

                    if ((inQuote == true) && (j == s.length()))
                    {
                        int a = s.lastIndexOf("'");
                        int b = s.lastIndexOf("\"");

                        if (a > b)
                        {
                            s = s.substring(0, a) + s.substring(a + 1);
                        }
                        else
                        {
                            s = s.substring(0, b) + s.substring(b + 1);
                        }
                        j = 0;
                        inQuote = false;
                        escaped = false;
                        tTemp = new StringBuffer(32000);
                        //System.out.println(s + tTemp.toString());
                        continue;
                    }
                }

                t += tTemp.toString();
                t += "\n";

                //System.out.println(t);
                // removing single line comments
                int l = 0;

                for (l = 1; l < t.length(); l++)
                {
                    if ((t.charAt(l) == '/') && (t.charAt(l - 1) == '/'))
                    {
                        NoofJSCommentLines++;
                        break;
                    }
                }

                for (j = 0; j < (l - 1); j++)
                {
                    u = u + t.charAt(j);
                }

                /* removing \n if ; ,{,} are there*/
                if (u.length() != 0)
                {
                    if ((u.charAt(u.length() - 1) == ';') ||
                            (u.charAt(u.length() - 1) == '{') ||
                            (u.charAt(u.length() - 1) == '}'))
                    {
                    }
                    else
                    {
                        u += "\n";
                    }
                }
            }

            boolean setPrevious = false;

            if ((s != null) && (isTagFound == false) &&
                    !(s.trim().equalsIgnoreCase("</script>") ||
                    s.trim().equalsIgnoreCase("</ script >") ||
                    s.trim().equalsIgnoreCase("</ script>") ||
                    s.trim().equalsIgnoreCase("</script >") ||
                    s.trim().equalsIgnoreCase("< / script >")))
            {
                setPrevious = false;

                if ((s.trim().indexOf("<!--") == -1) && !htmlCommentStart)
                {
                    //System.out.println("in if:::" +s);
                    for (int i = 0; i < s.length(); i++)
                    {
                        if (setPrevious == false)
                        {
                            destination.write(s.charAt(i));
                            //sp=sp+s.charAt(i);
                        }

                        if (setPrevious == true)
                        {
                            if ((s.charAt(i) != ' ') || (s.charAt(i) != '\t'))
                            {
                                destination.write(s.charAt(i));
                                //sp=sp+s.charAt(i);
                            }
                        }

                        if ((s.charAt(i) == ' ') || (s.charAt(i) == '\t'))
                        {
                            setPrevious = true;
                        }
                        else
                        {
                            setPrevious = false;
                        }
                    }
                }
                else
                {
                    //System.out.println("in else:::"+s);
                    NoofHTMLCommentLines++;
                    htmlCommentStart = true;

                    if ((s.indexOf("<!--") != -1) && (s.indexOf("<!--") != 0))
                    {
                        destination.write(s.substring(0, s.indexOf("<!--")));
                    }

                    if (s.indexOf("-->") != -1)
                    {
                        htmlCommentStart = false;

                        if ((s.indexOf("-->") + 3) != s.length())
                        {
                            destination.write(s.substring(s.indexOf("-->") + 3));
                        }
                    }
                }

                /*setPrevious=false;
                   //System.out.println(" Removing the White Space and new Line char actor from HTML...." );
                                      for(int i=0;i<sp.length();i++)
                                              {
                                                           if(setPrevious==false)
                                                           //        if(sp.charAt(i)=='\n')
                                                                   destination.writeByte(sp.charAt(i));
                                                           if(setPrevious==true)
                                                           {
                                                                   if(sp.charAt(i)!='\t')
                                                                   //  if(sp.charAt(i)=='\n')
                                                                           destination.writeByte(sp.charAt(i));
                                                           }
                                                           if(sp.charAt(i)=='\t')
                                                           {
                                                                   setPrevious=true;
                                                           }
                                                           else
                                                           {
                                                                   setPrevious=false;
                                                           }
                                              }
                 */

                //destination.writeByte('\n');
            }
        }

        int l = 0;

        StringBuffer nextlevelBuffer = new StringBuffer(128000);
        String nextlevel = "";
        Vector<Integer> intArray = new Vector<Integer>();
        intArray.add(new Integer(0));

        boolean previousIsStart = false;

        //	System.out.println(" Removing the Multi Line comments....\n" );
        for (l = 1; l < u.length(); l++)
        {
            if ((u.charAt(l - 1) == '/') && (u.charAt(l) == '*'))
            {
                if (!previousIsStart)
                {
                    intArray.add(new Integer(l - 1));
                }
                previousIsStart = true;
            }

            if ((u.charAt(l - 1) == '*') && (u.charAt(l) == '/'))
            {
                intArray.add(new Integer(l + 1));
                previousIsStart = false;
            }
        }


        /*                for(index=0;index<intArray.size();index++)
           {
                   System.out.println((Integer)intArray.elementAt(index));
           } */
        int k = 0;
        int index1;
        int index2;

        /* Copy the Code except the comments*/
        for (index1 = 0, index2 = 1;
                 (index1 < intArray.size()) && (index2 < intArray.size());
                 index1 = index1 + 2, index2 = index2 + 2)
        {
            for (k = (intArray.elementAt(index1)).intValue();
                     k < (intArray.elementAt(index2)).intValue();
                     k++)
            {
                //nextlevel=nextlevel+ u.charAt(k);
                nextlevelBuffer.append(u.charAt(k));

                if (u.charAt(k) == '\n')
                {
                    NoofJSCommentLines++;
                }
            }
            nextlevelBuffer.append("\n");
            //nextlevel+="\n";
        }

        for (int i = (intArray.elementAt(intArray.size() - 1)).intValue();
                 i < u.length(); i++)
        {
            //nextlevel=nextlevel+ u.charAt(i);
            nextlevelBuffer.append(u.charAt(i));
        }

        /*                int n=tempList.toArray().length;
           String[] literalStrings=new String[n];
           for(index=0;index<n;index++)
                                {
                                        literalStrings[index]=tempList.toArray()[index].toString();
                                }*/
        int i;
        boolean write = true;
        nextlevel = nextlevelBuffer.toString();

        StringBuffer spaceRemovedBuffer = new StringBuffer(128000);
        String spaceRemoved = "";

        //System.out.println(" Removing excess spaces in the Script....\n");
        //loop run for removing excess spaces in the String .. String is whole string
        for (i = 0; i < nextlevel.length(); i++)
        {
            if ((nextlevel.charAt(i) == ' ') || (nextlevel.charAt(i) == '\t'))
            {
                if (i != 0)
                {
                    if ((nextlevel.charAt(i + 1) == 'i') ||
                            (nextlevel.charAt(i - 1) == 'e') ||
                            (nextlevel.charAt(i - 1) == 'n') ||
                            (nextlevel.charAt(i - 1) == 'r') ||
                            (nextlevel.charAt(i - 1) == 'w'))
                    {
                        write = true;
                    }
                    else
                    {
                        write = false;
                    }
                    /*System.out.print("$");
                       if(nextlevel.charAt(i)=='\n')
                               {
                                       //System.out.print("$");
                                       if(nextlevel.charAt(i-1)==';')
                                          {System.out.print(nextlevel.charAt(i-1));
                                          write=false;}
                               }*/
                }
            }
            else
            {
                write = true;
            }

            if (write == true)
            {
                // spaceRemoved+= nextlevel.charAt(i);
                spaceRemovedBuffer.append(nextlevel.charAt(i));
                //System.out.print("@="+nextlevel.charAt(i));
            }
        }
        spaceRemoved = spaceRemovedBuffer.toString();

        //System.out.println(spaceRemoved);
        //StringBuffer strOutput = new StringBuffer(64768);
        StringBuffer strOutput = new StringBuffer(128000);
        String initial = new String();
        String strInput = new String();
        int nPos = 0;

        for (i = 0; i <= tempList.toArray().length; i++)
        {
            initial = "-_" + i + "-_";

            int nIndex = spaceRemoved.indexOf(initial, nPos);

            if (nIndex < 0)
            {
                continue;
            }
            else if ((nIndex < 0) && (i == tempList.toArray().length))
            {
                strOutput.append(spaceRemoved.substring(nPos));
            }
            else
            {
                strOutput.append(spaceRemoved.substring(nPos, nIndex));
                strOutput.append(tempList.get(i));
                //	System.out.println(tempList.get(i));
                nPos = nIndex + initial.length();
            }
        }
        /*        for( i=0; i<=literalStrings.length;i++)
           {
                   initial="-_"+i+"-_";
                   int nIndex = spaceRemoved.indexOf(initial,nPos);
                   if(nIndex<0)
                   {
                           continue;
                   }
                   else if(nIndex<0 && i==literalStrings.length)
                   {
                           strOutput.append(spaceRemoved.substring(nPos));
                   }
                   else
                   {
                           strOutput.append(spaceRemoved.substring(nPos,nIndex));
                           strOutput.append(literalStrings[i]);
                           nPos = nIndex+initial.length();
                   }
           }*/
        strOutput.append(spaceRemoved.substring(nPos));
        strInput = strOutput.toString();

        destination.write("<script language=javascript>\n");
        destination.write(strInput);
        destination.write("</script>");

        //System.out.println("\n\n File Done :-)");
        //These will be closed by the caller
        //source.close();
        //destination.close();
        // Writing the individual file deatails to the Report file
        //TODO: This has to be done on the fly
        //file =new File(destinationFile);
        //compressedSize=file.length();
        NoofExecutableCode = NoofLines - NoofBlankLines - NoofJSCommentLines -
                             NoofHTMLCommentLines;
        //TODO: Should be handled propertly
        //AntiComment.fullReport+= sourceFile+"  :"+NoofLines+"  :"+NoofBlankLines+"  :"+NoofJSCommentLines+"  :"+NoofHTMLCommentLines+"  :"+NoofExecutableCode+"  :"+originalSize+"  :"+compressedSize+"\n";
        //System.out.println("File Name:" + sourceFile+"  Total Lines:"+NoofLines+"  Blank Lines:"+NoofBlankLines+"  JS Comment Lines:"+NoofJSCommentLines+"  HTML Comment Lines:"+NoofHTMLCommentLines+"  Executable Lines:"+NoofExecutableCode+"  Ori Size:"+originalSize+"  Compress Size:"+compressedSize);
        AntiComment.TotalNoofLines += NoofLines;
        AntiComment.TotalNoofBlankLines += NoofBlankLines;
        AntiComment.TotalNoofExecutableCode += NoofJSCommentLines;
        AntiComment.TotalNoofJSCommentLines += NoofHTMLCommentLines;
        AntiComment.TotalNoofHTMLCommentLines += NoofExecutableCode;
        AntiComment.TotalOriginalSize += originalSize;
        AntiComment.TotalCompressedSize += compressedSize;
    }
}
