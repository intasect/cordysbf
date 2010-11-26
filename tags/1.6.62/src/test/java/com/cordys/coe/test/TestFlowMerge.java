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

import java.io.File;

import com.cordys.coe.ant.studio.util.FlowMerge;
import com.eibus.xml.nom.Document;

/**
 * @author pgussow
 *
 */
public class TestFlowMerge
{
	private static Document s_doc = new Document();

	/**
	 * Main method.
	 *
	 * @param saArguments Commandline arguments.
	 */
	public static void main(String[] saArguments)
	{
		try
		{
			FlowMerge fm = new FlowMerge(s_doc);
			
			fm.addFile(new File(".\\src\\test\\java\\com\\cordys\\coe\\test\\emailnotification_fta1.6.vcmdata"));
			fm.addFile(new File(".\\src\\test\\java\\com\\cordys\\coe\\test\\startprocinorg_fta1.6.vcmdata"));
			
			fm.createFinalFile(new File("d:\\temp\\merged.vcmdata"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
