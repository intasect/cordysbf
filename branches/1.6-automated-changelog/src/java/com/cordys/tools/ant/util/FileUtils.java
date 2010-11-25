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
 *         File                :        FileUtils.java
 *         Author                :        manesh@cordys.com
 *         Created on         :        Oct 4, 2004
 *
 */
package com.cordys.tools.ant.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Vector;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.types.FilterSetCollection;

import com.cordys.tools.ant.taskdefs.CopyWithOperation;
import com.cordys.tools.ant.taskdefs.copy.compress.AntiComment;

/**
 * Copy operation of Copy task is handled in
 * org.apache.tools.ant.util.FileUtils     Since we want to have our own
 * custom handling of the content, we need to     override the behaviour here.
 * This is with respect to apache-ant-1.5.3-1
 */
public class FileUtils extends org.apache.tools.ant.util.FileUtils
{
    /**
     * Empty constructor.
     */
    protected FileUtils()
    {
    }

    /**
     * DOCUMENTME
     *
     * @param fileName DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public static boolean isAnHTMLFile(String fileName)
    {
        if (fileName.length() < 5) //-.xml will make a length of 5 
        {
            return false;
        }

        String fileSuffix1 = fileName.substring(fileName.length() - 4);
        String fileSuffix2 = fileName.substring(fileName.length() - 5);

        //System.out.println(fileName+"   "+fileSuffix2+"   "+fileSuffix1+"   "+file.isFile());
        if (fileSuffix1.equalsIgnoreCase(".htm"))
        {
            return true;
        }
        else if (fileSuffix2.equalsIgnoreCase(".html"))
        {
            return true;
        }

        return false;
    }

    /**
     * Factory method.  Cannot override the factory method, newFileUtils(),  of
     * org.apache.tools.ant.util.FileUtils  Hence signature of factory method
     * is changed
     *
     * @return DOCUMENTME
     */
    public static FileUtils newInstance()
    {
        return new FileUtils();
    }

    /**
     * Convienence method to copy a file from a source to a destination
     * specifying if token filtering must be used, if source files may
     * overwrite newer destination files and the last modified time of
     * <code>destFile</code> file should be made equal to the last modified
     * time of <code>sourceFile</code>.
     *
     * @param sourceFile DOCUMENTME
     * @param destFile DOCUMENTME
     * @param filters DOCUMENTME
     * @param filterChains DOCUMENTME
     * @param overwrite DOCUMENTME
     * @param preserveLastModified DOCUMENTME
     * @param encoding DOCUMENTME
     * @param project DOCUMENTME
     * @param operation DOCUMENTME
     * @param copyRight DOCUMENTME
     *
     * @throws IOException
     *
     * @since 1.14, Ant 1.5
     */
    public void copyFile(String sourceFile, String destFile,
                         FilterSetCollection filters, Vector<?> filterChains,
                         boolean overwrite, boolean preserveLastModified,
                         String encoding, Project project, String operation,
                         String copyRight)
                  throws IOException
    {
        copyFile(new File(sourceFile), new File(destFile), filters,
                 filterChains, overwrite, preserveLastModified, encoding,
                 project, operation, copyRight);
    }

    /**
     * Convienence method to copy a file from a source to a destination
     * specifying if token filtering must be used, if filter chains must be
     * used, if source files may overwrite newer destination files and the
     * last modified time of <code>destFile</code> file should be made equal
     * to the last modified time of <code>sourceFile</code>.
     *
     * @param sourceFile DOCUMENTME
     * @param destFile DOCUMENTME
     * @param filters DOCUMENTME
     * @param filterChains DOCUMENTME
     * @param overwrite DOCUMENTME
     * @param preserveLastModified DOCUMENTME
     * @param encoding DOCUMENTME
     * @param project DOCUMENTME
     * @param operation DOCUMENTME
     * @param copyRight DOCUMENTME
     *
     * @throws IOException
     *
     * @since 1.15, Ant 1.5
     */
    public void copyFile(File sourceFile, File destFile,
                         FilterSetCollection filters, Vector<?> filterChains,
                         boolean overwrite, boolean preserveLastModified,
                         String encoding, Project project, String operation,
                         String copyRight)
                  throws IOException
    {
        if (overwrite || !destFile.exists() ||
                (destFile.lastModified() < sourceFile.lastModified()))
        {
            //sourceFile.equals(destFile) can be true for 
            //operation == eliminateindent 
            if (destFile.exists() && destFile.isFile() &&
                    !sourceFile.equals(destFile))
            {
                destFile.delete();
            }

            // ensure that parent dir of dest file exists!
            // not using getParentFile method to stay 1.1 compat
            File parent = getParentFile(destFile);

            if (!parent.exists())
            {
                parent.mkdirs();
            }

            final boolean filterSetsAvailable = ((filters != null) &&
                                                filters.hasFilters());
            final boolean filterChainsAvailable = ((filterChains != null) &&
                                                  (filterChains.size() > 0));

            if (operation.equals(CopyWithOperation.OPERATION_COMPRESS))
            {
                BufferedReader in = null;
                BufferedWriter out = null;

                try
                {
                    in = new BufferedReader(new FileReader(sourceFile));
                    out = new BufferedWriter(new FileWriter(destFile));
                    AntiComment.AntiCommentFunction(in, out, copyRight);
                }
                finally
                {
                    if (out != null)
                    {
                        out.close();
                    }

                    if (in != null)
                    {
                        in.close();
                    }
                }
            }
            else if (operation.equals(CopyWithOperation.OPERATION_ELIMINATE_INDENT))
            {
                eliminateIndent(sourceFile, destFile);
            }
            else if (operation.equals(CopyWithOperation.OPERATION_ELIMINATE_COMMENT))
            {
                removeComments(sourceFile, destFile);
            }
            else if (operation.equals(CopyWithOperation.OPERATION_REPLACE_PROPERTIES))
            {
                replaceProperties(sourceFile, destFile, project);
            }
            else if (filterSetsAvailable || filterChainsAvailable)
            {
                BufferedReader in = null;
                BufferedWriter out = null;

                try
                {
                    if (encoding == null)
                    {
                        in = new BufferedReader(new FileReader(sourceFile));
                        out = new BufferedWriter(new FileWriter(destFile));
                    }
                    else
                    {
                        in = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile),
                                                                      encoding));
                        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile),
                                                                        encoding));
                    }

                    if (filterChainsAvailable)
                    {
                        ChainReaderHelper crh = new ChainReaderHelper();
                        crh.setBufferSize(8192);
                        crh.setPrimaryReader(in);
                        crh.setFilterChains(filterChains);
                        crh.setProject(project);

                        Reader rdr = crh.getAssembledReader();
                        in = new BufferedReader(rdr);
                    }

                    String newline = null;
                    String line = in.readLine();

                    while (line != null)
                    {
                        if (line.length() == 0)
                        {
                            out.newLine();
                        }
                        else
                        {
                            if (filterSetsAvailable)
                            {
                                newline = filters.replaceTokens(line);
                            }
                            else
                            {
                                newline = line;
                            }
                            out.write(newline);
                            out.newLine();
                        }
                        line = in.readLine();
                    }
                }
                finally
                {
                    if (out != null)
                    {
                        out.close();
                    }

                    if (in != null)
                    {
                        in.close();
                    }
                }
            }
            else
            {
                FileInputStream in = null;
                FileOutputStream out = null;

                try
                {
                    in = new FileInputStream(sourceFile);
                    out = new FileOutputStream(destFile);

                    byte[] buffer = new byte[8 * 1024];
                    int count = 0;

                    do
                    {
                        out.write(buffer, 0, count);
                        count = in.read(buffer, 0, buffer.length);
                    }
                    while (count != -1);
                }
                finally
                {
                    if (out != null)
                    {
                        out.close();
                    }

                    if (in != null)
                    {
                        in.close();
                    }
                }
            }

            if (preserveLastModified)
            {
                setFileLastModified(destFile, sourceFile.lastModified());
            }
        }
    }

    //Code used in integrator
    public void removeComments(File in, File out)
                        throws IOException
    {
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try
        {
            reader = new BufferedReader(new FileReader(in));

            if (in.equals(out))
            {
                writer = new BufferedWriter(new FileWriter(in.getAbsolutePath() +
                                                           "_bk"));
            }
            else
            {
                writer = new BufferedWriter(new FileWriter(out));
            }

            String str;

            while ((str = reader.readLine()) != null)
            {
                analyzeLineForComment(str, writer);
            }
        }
        finally
        {
            if (reader != null)
            {
                writer.close();
            }

            if (reader != null)
            {
                writer.close();
            }
        }

        if (in.equals(out))
        {
            File tempFile = new File(in.getAbsolutePath() + "_bk");
            in.delete();
            tempFile.renameTo(in);
        }
    }

    //	Dynamically change all the properties
    public void replaceProperties(File in, File out, Project project)
                           throws IOException
    {
        BufferedReader oBReader = null;
        FileOutputStream oFos = null;

        try
        {
            oBReader = new BufferedReader(new FileReader(in));

            char[] cArr = new char[(int) in.length()];
            oBReader.read(cArr, 0, (int) in.length());

            String sTemp = new String(cArr);
            String sNew = project.replaceProperties(sTemp);
            oFos = new FileOutputStream(out);
            oFos.write(sNew.getBytes());
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (oBReader != null)
            {
                oBReader.close();
            }

            if (oFos != null)
            {
                oFos.close();
            }
        }
    }

    //----------------------------------------------------------------
    //	Actual content handling for opertations
    //	1. eliminateindent (Orchestrator)
    //	2. eliminatecomment (Integrator)
    //	Code is ported to fit to work with Ant task
    //----------------------------------------------------------------
    // Code used in Orchestrator
    private static void eliminateIndent(File in, File out)
                                 throws IOException
    {
        BufferedReader oBReader = null;
        FileOutputStream oFos = null;

        try
        {
            oBReader = new BufferedReader(new FileReader(in));

            char[] cArr = new char[(int) in.length()];
            oBReader.read(cArr, 0, (int) in.length());

            String sTemp = new String(cArr);
            String sNew = sTemp.replaceAll("<!--INDENT BEGIN-->[\\w\\W]*<!--INDENT END-->",
                                           "");
            oFos = new FileOutputStream(out);
            oFos.write(sNew.getBytes());
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (oBReader != null)
            {
                oBReader.close();
            }

            if (oFos != null)
            {
                oFos.close();
            }
        }
    }

    /**
     * DOCUMENTME
     *
     * @param str DOCUMENTME
     * @param writer DOCUMENTME
     *
     * @throws IOException DOCUMENTME
     */
    private void analyzeLineForComment(String str, BufferedWriter writer)
                                throws IOException
    {
        if ((str.indexOf("//") >= 0) & (str.indexOf("**Start Encode**") == -1))
        {
            if (str.indexOf("//") != 0)
            {
                String str1 = str;
                str = str.substring(0, str.indexOf("//"));

                if (str.trim().length() > 0)
                {
                    int doubQuotes = str.indexOf("\"");

                    if (doubQuotes < str1.indexOf("//"))
                    {
                        if (doubQuotes != -1)
                        {
                            writer.write(str1 + "\n");
                        }
                        else
                        {
                            writer.write(str + "\n");
                        }
                    }
                    else if (str.trim().length() > 0)
                    {
                        writer.write(str.trim() + "\n");
                    }
                }
            }
        }
        else
        {
            writer.write(str + "\n");
        }
    }
}
