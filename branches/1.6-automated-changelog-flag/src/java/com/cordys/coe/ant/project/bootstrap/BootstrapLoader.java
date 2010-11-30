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
package com.cordys.coe.ant.project.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * Is used to load a bootstrap Ant script for downloading all
 * the file needed for creating or updating a Build Framework project.
 * This way the update script will always come from the version
 * that will be put in the sdk folder.
 *
 * @author mpoyhone
 */
public class BootstrapLoader
{
    /**
     * TODO Describe the field.
     */
    private static final String PROPERTY_PLATFORM_VERSION = "bf.platform.version";
    /**
     * TODO Describe the field.
     */
    private static final String PROPERTY_SDK_VERSION = "bf.sdk.version";
    /**
     * TODO Describe the field.
     */
    private static final String DESTDIR_SDK = "sdk";
    /**
     * TODO Describe the field.
     */
    private static final String DESTDIR_PLATFORM = "platform";
    /**
     * TODO Describe the field.
     */
    private static final String VERSION_FILE_NAME = "current-version.properties";
    /**
     * TODO Describe the field.
     */
    private static final String VERSION_PROPERTY_NAME = "version";

    public static void main(String[] args)
    {
        /*
        File fScript = null;
        InputStream isScriptSource = null;
        OutputStream osScriptDest = null;
        
        try {
            fScript = File.createTempFile("project-bootstrap", ".xml", new File("."));
            fScript.deleteOnExit();
            
            isScriptSource = BootstrapLoader.class.getResourceAsStream("project-bootstrap.xml");
            osScriptDest = new FileOutputStream(fScript);
            streamCopy(isScriptSource, osScriptDest, -1);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to create the bootstrap script file");
        }
        finally {
            if (isScriptSource != null) {
                try
                {
                    isScriptSource.close();
                }
                catch (IOException ignored)
                {
                }
            }
            
            if (osScriptDest != null) {
                try
                {
                    osScriptDest.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }
        
        String[] antArgs = {
           "-f", 
           fScript.getName()
        };
        
        org.apache.tools.ant.Main.main(antArgs);*/
        
        String sSdkVersion = System.getProperty(PROPERTY_SDK_VERSION);
        String sPlatformVersion = System.getProperty(PROPERTY_PLATFORM_VERSION);
        
        String className = "/build/build-framework.xml";
        URL classUrl = BootstrapLoader.class.getResource(className);
        
        if (classUrl == null) {
            System.out.println("\nClass '" + className + 
                "' not found.");
        } else {
            System.out.println("\nClass '" + className + 
                "' found in \n'" + classUrl.getFile() + "'");
        }
        
        String sZipPath = classUrl.getFile();
        int iSep = sZipPath.lastIndexOf('!');
        
        if (iSep > 0) {
            sZipPath = sZipPath.substring(0, iSep);
        }
        
        System.out.println(sZipPath);
        
        File fSdkZip = getZipFromClasspath("/build/build-framework.xml");
        File fSdkDestDir = new File(DESTDIR_SDK);
        
        try
        {
            createAndCleanFolder(fSdkDestDir);
        }
        catch (IOException e)
        {
            System.out.println(e);
            System.exit(1);
        }   
        
        try
        {
            extractZipWithVersion(fSdkZip, fSdkDestDir, sSdkVersion);
        }
        catch (IOException e)
        {
            System.out.println("Unzip operation failed: " + e);
            System.exit(1);
        }
        
        File fPlatformZip = getZipFromClasspath("/int/wcp.jar");
        File fPlatformDestDir = new File(DESTDIR_PLATFORM);
        
        try
        {
            createAndCleanFolder(fPlatformDestDir);
        }
        catch (IOException e)
        {
            System.out.println(e);
            System.exit(1);
        }      
        
        try
        {
            extractZipWithVersion(fPlatformZip, fPlatformDestDir, sPlatformVersion);
        }
        catch (IOException e)
        {
            System.out.println("Unzip operation failed: " + e);
            System.exit(1);
        }
    }
    
    private static void createAndCleanFolder(File fFolder) throws IOException {
        if (! fFolder.exists()) {
            fFolder.mkdir();
            return;
        }      
        
        File[] faFiles = fFolder.listFiles();
        
        for (File fFile : faFiles)
        {
            if (fFile.isDirectory()) {
                createAndCleanFolder(fFile);
            }
            
            if (! fFile.delete()) {
                throw new IOException("Unable to delete file/folder: " + fFile);
            }
        }
    }
    
    
    private static File getZipFromClasspath(String sContainsFile) {
        URL uFileUrl = BootstrapLoader.class.getResource(sContainsFile);
        
        if (uFileUrl == null) {
            return null;
        }
        
        String sFilePath = uFileUrl.getFile();
        int iSep = sFilePath.lastIndexOf('!');
        
        if (iSep > 0) {
            sFilePath = sFilePath.substring(0, iSep);
        }
        
        System.out.println(sFilePath);
        
        File fSourceFile = null;
        
        try
        {
            fSourceFile = new File(new URI(sFilePath));
        }
        catch (URISyntaxException e)
        {
            System.err.println("Invalid file URL: " + sFilePath);
            System.exit(1);
        }
        
        if (! fSourceFile.exists()) {
            System.err.println("File '" + fSourceFile +
                    "' does not exist.");
            return null;
        }        
        
        return fSourceFile;
    }
    
    /**
     * Extracts the source zip file in the destination directory. This
     * tries to first reads a version property file from the destination
     * diretory and skips the unzipping if the version matches to the
     * passed version.
     * @param fSourceZip Source ZIP file to be exctracted.
     * @param fDestDir Destination directory.
     * @param sVersion Version string to be compared.
     */
    private static void extractZipWithVersion(File fSourceZip, File fDestDir, String sVersion) throws IOException {
        Properties pVersionProps = new Properties();
        
        if (sVersion != null) {
            // Try to load the current version from the property file.
            File fVersionFile = new File(fDestDir, VERSION_FILE_NAME);
            
            if (fVersionFile.exists()) {
                InputStream isInput;
                String sCurrentVersion;
                
                isInput = new FileInputStream(fVersionFile);
                
                try {
                    pVersionProps.load(isInput);
                }
                finally {
                    try {
                        isInput.close();
                    }
                    catch (Exception ignored) {
                    }
                }
                
                sCurrentVersion = pVersionProps.getProperty(VERSION_PROPERTY_NAME);
                
                if (sVersion.equals(sCurrentVersion)) {
                    // This is the same version, so skip extraction.
                    return;
                }
            }
        }
        
        ZipUtil.unzip(fSourceZip, fDestDir, true);
        
        if (sVersion != null) {
            // Save the version to the property file.
            pVersionProps.put(VERSION_PROPERTY_NAME, sVersion);
            
            OutputStream osOutput;
            File fVersionFile = new File(fDestDir, VERSION_FILE_NAME);
            
            osOutput = new FileOutputStream(fVersionFile);
            
            try {
                pVersionProps.store(osOutput, "Current extracted version.");
            }
            finally {
                try {
                    osOutput.close();
                }
                catch (Exception ignored) {
                }
            }            
        }
    }
}
