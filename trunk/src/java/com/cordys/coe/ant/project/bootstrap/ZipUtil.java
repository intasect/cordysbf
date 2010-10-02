
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
package com.cordys.coe.ant.project.bootstrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;


/**
 * @author i.coulter
 *
 */
public class ZipUtil {

    public static void unzip(File src, File dest, boolean overwrite) throws IOException {
        if (!dest.exists()) {
        	throw new IOException("impossible to copy: destination does not exist: "+dest);
        }
        
        if (!dest.isDirectory()) {
        	throw new IOException("impossible to copy: destination is not a directory: "+dest);
        }
        
        ZipFile zipFile = new ZipFile(src);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while(entries.hasMoreElements()) {
        	ZipEntry entry = entries.nextElement();

        	System.out.println("Unzipping Entry : "+entry.getName());
        	if(entry.isDirectory()) {
        		if (!(new File(dest,entry.getName())).exists()) {
        			if (!(new File(dest,entry.getName())).mkdirs()) {
        				throw new IOException("Failed to unzip: "+entry.getName());
        			}
        		}
	        	continue;
	        }
	
	        if (!(new File(new File(dest,entry.getName()).getParent())).exists()) {
	        	if (!(new File(dest,new File(entry.getName()).getParent())).mkdirs()) {
	            	throw new IOException("Failed to unzip: "+entry.getName());
	        	}	
	        }
	        copyInputStream(zipFile.getInputStream(entry),
	           new BufferedOutputStream(new FileOutputStream(new File(dest,entry.getName()))));
	    }
        zipFile.close();
	    return;
    }

    public static final void copyInputStream(InputStream in, OutputStream out)
    throws IOException
    {
      byte[] buffer = new byte[1024];
      int len;

      while((len = in.read(buffer)) >= 0)
        out.write(buffer, 0, len);

      in.close();
      out.close();
    }


}
