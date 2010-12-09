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
 *         File                :        CopyWithOperation.java
 *         Author                :        manesh@cordys.com
 *         Created on         :        Oct 4, 2004
 *
 */
package com.cordys.tools.ant.taskdefs;

import com.cordys.tools.ant.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterSet;
import org.apache.tools.ant.types.FilterSetCollection;

/**
 * Actual copy operation is handled in doFileOperation (with fileUtils Object)
 * Since we want to implement our custom handler, we need to override
 * fileUtils This can work only if we write our own extended class for
 * FileUtils org.apache.tools.ant.util.FileUtils -->
 * com.cordys.tools.ant.util.FileUtils All functions that refer fileUtils
 * instance in Copy Class has to be overridden This is with respect to
 * apache-ant-1.5.3-1
 */
public class CopyWithOperation extends Copy
{
    /**
     * Handles Compression Task written by V Ramesh
     */
    public static final String OPERATION_COMPRESS = "compress";
    /**
     * Operation type
     */
    public static final String OPERATION_ELIMINATE_INDENT = "eliminateindent";
    /**
     * Operation type
     */
    public static final String OPERATION_DEFAULT = "default";
    /**
     * Operation type (Used for comment removal for eibus htcs
     */
    public static final String OPERATION_ELIMINATE_COMMENT = "eliminatecomment";
    /**
     * Operation type (Used for comment removal for eibus htcs
     */
    public static final String OPERATION_REPLACE_PROPERTIES = "replaceproperties";
    /**
     * DOCUMENTME
     */
    private FileUtils fileUtils = null;
    /**
     * DOCUMENTME
     */
    private OperationAttribute operation = null;
    /**
     * DOCUMENTME
     */
    private String copyRight = null;
    /**
     * DOCUMENTME
     */
    private boolean failonerror = true;

    /**
     * CopyWithOperation task constructor.   Here super() is not called.  Hence
     * fileUtils instance of Copy will be invalid
     */
    public CopyWithOperation()
    {
        fileUtils = FileUtils.newInstance();
    }

    /**
     * Load the copyright file that should be appended to all files
     *
     * @param copyRight File which contains the copyright notice
     */
    public void setCopyRight(File copyRight)
    {
        this.copyRight = "";

        char[] ch = new char[1];
        BufferedReader br;

        try
        {
            br = new BufferedReader(new FileReader(copyRight));

            String temp = null;

            while ((br.read(ch)) >= 0)
            {
                temp = new String(ch);
                this.copyRight += temp;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * If false, note errors to the output but keep going.
     *
     * @param failonerror true or false
     */
    public void setFailOnError(boolean failonerror)
    {
        this.failonerror = failonerror;
    }

    /**
     * This method gets the failonerror flag.
     *
     * @return The failonerror flag.
     */
    public boolean getFailonerror()
    {
        return failonerror;
    }

    /**
     * Sets the operation to be used.
     *
     * @param opr
     */
    public void setOperation(OperationAttribute opr)
    {
        this.operation = opr;
    }

    /**
     * Returns the operation mode in which the CopyWithOperation should be
     * executed. Can be any of the three types - cl, cpc or three
     *
     * @return The operation mode.
     */
    public OperationAttribute getOperation()
    {
        return operation;
    }

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public static String[] getValidOperationAttributes()
    {
        return new String[]
               {
                   OPERATION_COMPRESS, OPERATION_ELIMINATE_INDENT,
                   OPERATION_ELIMINATE_COMMENT, OPERATION_REPLACE_PROPERTIES,
                   OPERATION_DEFAULT
               };
    }

    /**
     * If you are using CopyWithOperation to get the fileUtils instance,  you
     * should use this function instead of getFileUtils
     *
     * @return fileUtils com.cordys.tools.ant.util.FileUtils
     */
    protected FileUtils getFileUtilsInstance()
    {
        return fileUtils;
    }

    /**
     * Actually does the file (and possibly empty directory) copies. This is a
     * good method for subclasses to override.
     */
    protected void doFileOperations()
    {
        //Get all private members of Copy
        Vector<?> filterSets = getFilterSets();
        Vector<?> filterChains = getFilterChains();
        String encoding = getEncoding();

        if (fileCopyMap.size() > 0)
        {
            log("Copying " + fileCopyMap.size() + " file" +
                ((fileCopyMap.size() == 1) ? "" : "s") + " to " +
                destDir.getAbsolutePath());

            Enumeration<?> e = fileCopyMap.keys();

            while (e.hasMoreElements())
            {
                String fromFile = (String) e.nextElement();
                String[] toFiles = (String[]) fileCopyMap.get(fromFile);

                for (int i = 0; i < toFiles.length; i++)
                {
                    String toFile = toFiles[i];

                    //if (fromFile.equals(toFile))
                    if (fromFile.equals(toFile) &&
                            !(OPERATION_ELIMINATE_INDENT.equals(operation.getValue()) &&
                            OPERATION_ELIMINATE_COMMENT.equals(operation.getValue())))
                    {
                        log("Skipping self-copy of " + fromFile, verbosity);
                        continue;
                    }

                    try
                    {
                        log("Copying " + fromFile + " to " + toFile, verbosity);

                        FilterSetCollection executionFilters = new FilterSetCollection();

                        if (filtering)
                        {
                            executionFilters.addFilterSet(getProject()
                                                              .getGlobalFilterSet());
                        }

                        for (Enumeration<?> filterEnum = filterSets.elements();
                                 filterEnum.hasMoreElements();)
                        {
                            executionFilters.addFilterSet((FilterSet) filterEnum.nextElement());
                        }

                        //fileUtils.copyFile(fromFile, toFile, executionFilters,
                        //                   filterChains, forceOverwrite,
                        //                   preserveLastModified, inputEncoding,
                        //                   outputEncoding, getProject());
                        fileUtils.copyFile(fromFile, toFile, executionFilters,
                                           filterChains, forceOverwrite,
                                           preserveLastModified, encoding,
                                           getProject(), operation.getValue(),
                                           copyRight);
                    }
                    catch (IOException ioe)
                    {
                        String msg = "Failed to copy " + fromFile + " to " +
                                     toFile + " due to " + ioe.getMessage();
                        File targetFile = new File(toFile);

                        if (targetFile.exists() && !targetFile.delete())
                        {
                            msg += (" and I couldn't delete the corrupt " +
                            toFile);
                        }
                        throw new BuildException(msg, ioe, getLocation());
                    }
                }
            }
        }

        if (includeEmpty)
        {
            Enumeration<?> e = dirCopyMap.elements();
            int createCount = 0;

            while (e.hasMoreElements())
            {
                String[] dirs = (String[]) e.nextElement();

                for (int i = 0; i < dirs.length; i++)
                {
                    File d = new File(dirs[i]);

                    if (!d.exists())
                    {
                        if (!d.mkdirs())
                        {
                            log("Unable to create directory " +
                                d.getAbsolutePath(), Project.MSG_ERR);
                        }
                        else
                        {
                            createCount++;
                        }
                    }
                }
            }

            if (createCount > 0)
            {
                log("Copied " + dirCopyMap.size() + " empty director" +
                    ((dirCopyMap.size() == 1) ? "y" : "ies") + " to " +
                    createCount + " empty director" +
                    ((createCount == 1) ? "y" : "ies") + " under " +
                    destDir.getAbsolutePath());
            }
        }
    }

    //************************************************************************
    //  protected and private methods
    //************************************************************************
    /**
     * Ensure we have a consistent and legal set of attributes, and set any
     * internal flags necessary based on different combinations of attributes.
     * This method is overridden because, it refer fileUtils instance Copied
     * as such without any change
     */
    protected void validateAttributes()
                               throws BuildException
    {
        if (operation == null)
        {
            throw new BuildException("Specify at least one operation attribute");
        }

        if ((file == null) && (filesets.size() == 0))
        {
            throw new BuildException("Specify at least one source " +
                                     "- a file or a fileset.");
        }

        if ((destFile != null) && (destDir != null))
        {
            throw new BuildException("Only one of tofile and todir " +
                                     "may be set.");
        }

        if ((destFile == null) && (destDir == null))
        {
            throw new BuildException("One of tofile or todir must be set.");
        }

        if ((file != null) && file.exists() && file.isDirectory())
        {
            throw new BuildException("Use a fileset to copy directories.");
        }

        if ((destFile != null) && (filesets.size() > 0))
        {
            if (filesets.size() > 1)
            {
                throw new BuildException("Cannot concatenate multiple files into a single file.");
            }
            else
            {
                FileSet fs = (FileSet) filesets.elementAt(0);
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                String[] srcFiles = ds.getIncludedFiles();

                if (srcFiles.length == 0)
                {
                    throw new BuildException("Cannot perform operation from directory to file.");
                }
                else if (srcFiles.length == 1)
                {
                    if (file == null)
                    {
                        file = new File(ds.getBasedir(), srcFiles[0]);
                        filesets.removeElementAt(0);
                    }
                    else
                    {
                        throw new BuildException("Cannot concatenate multiple " +
                                                 "files into a single file.");
                    }
                }
                else
                {
                    throw new BuildException("Cannot concatenate multiple " +
                                             "files into a single file.");
                }
            }
        }

        if (destFile != null)
        {
            destDir = fileUtils.getParentFile(destFile);
        }
    }

    /**
     * Inner Class for handling OperationAttribute as a enumerated attribute.
     */
    public static class OperationAttribute extends EnumeratedAttribute
    {
        /**
         * Implementing the getValues of EnumeratedAttributes class.
         *
         * @return DOCUMENTME
         */
        public String[] getValues()
        {
            return CopyWithOperation.getValidOperationAttributes();
        }
    }
}
