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
package com.cordys.coe.ant.coboc.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.cordys.tools.ant.soap.SoapRequestException;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.directory.soap.LDAPDirectory;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * Utility functions for CoBOC ant task.
 *
 * @author mpoyhone
 */
public class CoBOCUtils
{
    /**
     * Compares two objects for equality. Also supports comparison of null
     * values.
     *
     * @param o1 Object to be compared to o2. Can be null.
     * @param o2 Object to be compared to o1. Can be null.
     *
     * @return True if both o1 and o2 are null or their equals-method returns
     *         true.
     */
    public static boolean equals(Object o1, Object o2)
    {
        if ((o1 == null) || (o2 == null))
        {
            return (o1 == null) && (o2 == null);
        }

        return o1.equals(o2);
    }

    /**
     * Reads file contents into an XML structure.
     *
     * @param fFile The file the be read.
     * @param dDoc The document that is used when parsing the XML.
     *
     * @return The XML structure root node, or zero if the file was not found
     *         or the parsing failed.
     */
    public static int readXmlFileContents(File fFile, Document dDoc)
    {
        StringBuffer sbBuffer = new StringBuffer(1024);
        InputStreamReader isrInput = null;

        try
        {
            isrInput = new InputStreamReader(new BufferedInputStream(new FileInputStream(fFile)), "UTF-8");

            int iCount;
            char[] caBuffer = new char[1024];

            while ((iCount = isrInput.read(caBuffer)) != -1)
            {
                sbBuffer.append(caBuffer, 0, iCount);
            }
        }
        catch (Exception e)
        {
            return 0;
        }
        finally
        {
            if (isrInput != null)
            {
                try
                {
                    isrInput.close();
                }
                catch (Exception ignored)
                {
                }
            }
        }

        int iResult;

        try
        {
            iResult = dDoc.parseString(sbBuffer.toString());
        }
        catch (Exception e)
        {
            return 0;
        }

        return iResult;
    }

    /**
     * Recursively traverses the file system directory tree starting at the
     * given root diretctory and calls the callback interface.  If the
     * callback method returns false, the iteration is stopped.  Iteration is
     * done in a breadth-first fashion, i.e. directory contents are returned
     * first and then subdirectory contents.
     *
     * @param fRootDir The recursion root directory.
     * @param fcCallback The callback object that will be called for every
     *        file.
     *
     * @throws Exception Thrown if the callback threw an exception.
     */
    public static void recurseDirectoryTreeBreadthFirst(File fRootDir,
                                                        FileCallback fcCallback)
                                                 throws Exception
    {
        // Call the callback for the root directory.
        if (!fcCallback.handleDirectory(fRootDir))
        {
            return;
        }

        // List all files and directories in the directory.
        File[] faFiles = fRootDir.listFiles();

        // Call the callback for all the files and subdirectories in this directory
        for (int i = 0; i < faFiles.length; i++)
        {
            File fFile = faFiles[i];

            // Call the right handler for this file or directory.
            if (fFile.isDirectory())
            {
                fcCallback.handleDirectory(fFile);
            }
            else
            {
                fcCallback.handleFile(fFile);
            }
        }

        // Recurse into subdirectories.
        for (int i = 0; i < faFiles.length; i++)
        {
            File fFile = faFiles[i];

            // Check that is a directory.
            if (fFile.isDirectory())
            {
                recurseDirectoryTreeBreadthFirst(fFile, fcCallback);
            }
        }
    }

 
    /**
     * Writes XML structure contents into a file.
     *
     * @param iNode The XML structure root node that will be written to the
     *        file.
     * @param fFile The file the be created.
     *
     * @return True if the writing succeeded.
     */
    public static boolean writeXmlContentsToFile(int iNode, File fFile)
    {
        String sXmlData = Node.writeToString(iNode, true);
        OutputStreamWriter oswOutput = null;

        if ((sXmlData == null) || sXmlData.equals(""))
        {
            return false;
        }

        try
        {
            oswOutput = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(fFile)), "UTF-8");

            oswOutput.write(sXmlData);
        }
        catch (Exception e)
        {
            return false;
        }
        finally
        {
            if (oswOutput != null)
            {
                try
                {
                    oswOutput.flush();
                    oswOutput.close();
                }
                catch (Exception ignored)
                {
                }
            }
        }

        return true;
    }
    
    
	/**
	 * Resolves the receiver when the method parameters are specified.
	 *
	 * @param sOrganization The organization in which the soap node has to be
	 * 		  resolved
	 * @param sNamespace The namespace of the method.
	 * @param sMethod The name of the method.
	 *
	 * @return The dn of the receiver soap node to which the request has to be
	 * 		   sent.
	 *
	 * @throws SoapRequestException
	 */
	public static String getReceiver(LDAPDirectory ldLdapDirectory, String sOrganization, String sNamespace,
							   String sMethod)
						throws DirectoryException
	{
		String sReceiver;

		if ((sOrganization == null) || (sOrganization.length() == 0))
		{
			//Resolve the Receiver to whom to send.
			sReceiver = ldLdapDirectory.findSOAPNode(sNamespace, sMethod);
		}
		else
		{
			ldLdapDirectory.setOrganization(sOrganization);
			
			//Resolve the Receiver to whom to send.
			sReceiver = ldLdapDirectory.findSOAPNode(sOrganization, sNamespace,
												  sMethod);
		}

		return sReceiver;
	}     

    /**
     * A callback class for directory tree recursion method.
     */
    public static class FileCallback
    {
        /**
         * Called when a directory is encountered.
         *
         * @param fDir The directory encountered during directory tree
         *        recursion.
         *
         * @return If the method returns false, the recursion is stopped.
         *
         * @throws Exception Thrown if the callback threw an exception.
         */
        public boolean handleDirectory(File fDir)
                                throws Exception
        {
            return true;
        }

        /**
         * Called when a file is encountered.
         *
         * @param fFile The file encountered during directory tree recursion.
         *
         * @return If the method returns false, the recursion is stopped.
         *
         * @throws Exception Thrown if the callback threw an exception.
         */
        public boolean handleFile(File fFile)
                           throws Exception
        {
            return true;
        }
    }
}
