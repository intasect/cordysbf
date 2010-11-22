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
package com.cordys.tools.ant.taskdefs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author srkrishnan
 */
public class ChangeLog {

    /**
     * @param args the command line arguments
     */
    public static void createAutoLog(String f) {
        // TODO code application logic here
        try {

		    if(f==null)
            {
                System.exit(0);
            }

            BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
            
            String xmlFile =f;
            File file = new File(xmlFile);
            String outfile="changelog.txt";
            if(file.getParent()!=null)
                outfile=file.getParent()+File.separator+"changelog.txt";
            FileWriter fstream = new FileWriter(outfile, false);
            BufferedWriter out = new BufferedWriter(fstream);

            if (file.exists()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(file);
                TransformerFactory tranFactory = TransformerFactory.newInstance();
                Transformer aTransformer = tranFactory.newTransformer();
                // Get nodes list of all elements
                NodeList list = doc.getElementsByTagName("release");
                for (int i = 2; i < list.getLength(); i++) {

                    Element ele = (Element) list.item(i);
                    
                    System.out.println("version " + ele.getAttribute("version") + " (released " + ele.getAttribute("date") + ")");
 
                    out.write("version " + ele.getAttribute("version") + " (released " + ele.getAttribute("date") + ")");
                    out.newLine();

                    NodeList actlist = ele.getElementsByTagName("action");
                    for (int j = 0; j < actlist.getLength(); j++) {
                        Element actele = (Element) actlist.item(j);
                        System.out.println("-" + actele.getAttribute("type") + ": " + actele.getTextContent());

                        out.write("-" + actele.getAttribute("type") + ": " + actele.getTextContent());
                        out.newLine();
                    }
                    out.newLine();
                    
                }

            out.close();

            } else {
                System.out.println(xmlFile + " (file name) doesn't found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
