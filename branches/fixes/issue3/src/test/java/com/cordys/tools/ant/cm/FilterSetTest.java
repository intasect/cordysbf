/**
 * Copyright 2005 Cordys R&D B.V. 
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
 

package com.cordys.tools.ant.cm;

import junit.framework.TestCase;

/**
 * TODO Enter description.
 *
 * @author mpoyhone
 */
public class FilterSetTest extends TestCase {

	public void testIsEmpty() {
	}

	/*
	 * Class under test for boolean isPathAccepted(String)
	 */
	public void testIsPathAcceptedString() {
		FilterSet fs;
		Filter f;
		
		// Simple inclusion
		fs = new FilterSet();
		f = new Filter("/root/dir/**", true);	
		fs.addFilter(f);
		assertTrue(fs.isPathAccepted("/root/dir/a"));
		assertTrue(fs.isPathAccepted("/root/dir", true));
		assertFalse(fs.isPathAccepted("/root/dir"));
		assertFalse(fs.isPathAccepted("/root2/dir/a"));
		assertFalse(fs.isPathAccepted("/root/dir2", true));
		assertFalse(fs.isPathAccepted("/root/di/a"));
		assertTrue(fs.isPathAccepted("/root", true));
		
		// Simple exclusion
		fs = new FilterSet();
		f = new Filter("/root/dir/**", false);	
		fs.addFilter(f);
		assertFalse(fs.isPathAccepted("/root/dir/a"));
		assertFalse(fs.isPathAccepted("/root/dir", true));
		assertTrue(fs.isPathAccepted("/root/dir"));
		assertTrue(fs.isPathAccepted("/root/dir2/a"));
		assertTrue(fs.isPathAccepted("/root/dir2", true));
		assertTrue(fs.isPathAccepted("/root/dir2"));
		assertTrue(fs.isPathAccepted("/root/di/a"));
		assertTrue(fs.isPathAccepted("/root", true));
	}
}
