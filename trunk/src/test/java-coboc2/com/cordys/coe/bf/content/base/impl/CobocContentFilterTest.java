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
package com.cordys.coe.bf.content.base.impl;

import com.cordys.coe.bf.content.types.EContentType;

import junit.framework.TestCase;

/**
 * Test cases for coboc content filter.
 *
 * @author mpoyhone
 */
public class CobocContentFilterTest extends TestCase
{
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testBPM() throws Exception
    {
        CobocContentFilter filter = new CobocContentFilter();
        
        filter.addFilter("/cordys/cas/*", false);
        filter.addFilter("3.Business Process Models/MyFlows/Flow1", true, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
        
        assertTrue(filter.checkFolderAccess("/"));
        assertTrue(filter.checkFolderAccess("/Business Processes"));
        assertTrue(filter.checkFolderAccess("/Business Processes/3.Business Process Models"));
        assertTrue(filter.checkFolderAccess("/Business Processes/3.Business Process Models/MyFlows"));
        assertTrue(filter.checkItemAccess("/Business Processes/3.Business Process Models/MyFlows/Flow1_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/3.Business Process Models/MyFlows/Flow1_demo2.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        
        assertFalse(filter.checkFolderAccess("/Business Processes/3.Business Process Models/MyTests"));
        assertFalse(filter.checkFolderAccess("/Business Processes/3.Business Process Models/MyFlow"));
        assertFalse(filter.checkFolderAccess("/Business Processes/3.Business Process Models/MyFlow1"));
    }
    
    public void testBPML() throws Exception
    {
        CobocContentFilter filter = new CobocContentFilter();
        
        filter.addFilter("/cordys/cas/*", false);
        filter.addFilter("3.Business Process Models/MyFlows/Flow1", true, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
        
        assertTrue(filter.checkFolderAccess("/Business Processes/BPML/3.Business Process Models"));
        assertTrue(filter.checkFolderAccess("/Business Processes/BPML/3.Business Process Models/MyFlows"));
        assertTrue(filter.checkFolderAccess("/Business Processes/BPML/3.Business Process Models/MyFlows/Flow1_vcmdata.bpm"));
        assertTrue(filter.checkFolderAccess("/Business Processes/BPML/3.Business Process Models/MyFlows/Flow1_demo2.bpm"));
        assertTrue(filter.checkItemAccess("/Business Processes/BPML/3.Business Process Models/MyFlows/Flow1_vcmdata.bpm/2d70f04e-0a01-1e49-0130-4cdb465e4f2b", EContentType.COBOC_FOLDERS_PROCESSBPML));
        assertTrue(filter.checkItemAccess("/Business Processes/BPML/3.Business Process Models/MyFlows/Flow1_demo2.bpm/2d70f04e-0a01-1e49-0130-4cdb465e4f2b", EContentType.COBOC_FOLDERS_PROCESSBPML));

        assertFalse(filter.checkFolderAccess("/Business Processes/BPML/3.Business Process Models/MyTests"));
        assertFalse(filter.checkFolderAccess("/Business Processes/BPMN/3.Business Process Models/MyTests"));
    }    
    
    public void testBPMN() throws Exception
    {
        CobocContentFilter filter = new CobocContentFilter();
        
        filter.addFilter("/cordys/cas/*", false);
        filter.addFilter("3.Business Process Models/MyFlows/Flow1", true, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
        
        assertTrue(filter.checkFolderAccess("/Business Processes/BPMN/3.Business Process Models"));
        assertTrue(filter.checkFolderAccess("/Business Processes/BPMN/3.Business Process Models/MyFlows"));
        assertTrue(filter.checkFolderAccess("/Business Processes/BPMN/3.Business Process Models/MyFlows/Flow1_vcmdata.bpm"));
        assertTrue(filter.checkFolderAccess("/Business Processes/BPMN/3.Business Process Models/MyFlows/Flow1_demo2.bpm"));
        assertTrue(filter.checkItemAccess("/Business Processes/BPMN/3.Business Process Models/MyFlows/Flow1_vcmdata.bpm/2d70f04e-0a01-1e49-0130-4cdb465e4f2b", EContentType.COBOC_FOLDERS_PROCESSBPMN));
        assertTrue(filter.checkItemAccess("/Business Processes/BPMN/3.Business Process Models/MyFlows/Flow1_demo2.bpm/2d70f04e-0a01-1e49-0130-4cdb465e4f2b", EContentType.COBOC_FOLDERS_PROCESSBPMN));

        assertFalse(filter.checkFolderAccess("/Business Processes/BPMN/3.Business Process Models/MyTests"));
        assertFalse(filter.checkFolderAccess("/Business Processes/BPMN/3.Business Process Models/MyTests"));
    }       
    
    public void testRootBPM() throws Exception
    {
        CobocContentFilter filter = new CobocContentFilter();
        
        filter.addFilter("/cordys/cas/*", false);
        filter.addFilter("Flow1", true, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
        
        assertTrue(filter.checkFolderAccess("/"));
        assertTrue(filter.checkFolderAccess("/Business Processes"));
        assertTrue(filter.checkItemAccess("/Business Processes/Flow1_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/Flow1_demo2.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertFalse(filter.checkItemAccess("/Business Processes/Flow2_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/BPML/Flow1_vcmdata.bpm/2d70f04e-0a01-1e49-0130-4cdb465e4f2b", EContentType.COBOC_FOLDERS_PROCESSBPML));
        assertTrue(filter.checkItemAccess("/Business Processes/BPMN/Flow1_vcmdata.bpm/2d70f04e-0a01-1e49-0130-4cdb465e4f2b", EContentType.COBOC_FOLDERS_PROCESSBPMN));
    }       
    
    public void testExclusion() throws Exception
    {
        CobocContentFilter filter = new CobocContentFilter();
        
        filter.addFilter("/cordys/cas/**", false);
        
        assertTrue(filter.checkFolderAccess("/"));
        assertTrue(filter.checkFolderAccess("/cordys"));
        assertTrue(filter.checkFolderAccess("/cordys/cas"));
        assertFalse(filter.checkFolderAccess("/cordys/cas/blah"));
        assertFalse(filter.checkItemAccess("/cordys/cas/blah", EContentType.COBOC_FOLDERS_TEMPLATE));
        assertTrue(filter.checkItemAccess("/custom", EContentType.COBOC_FOLDERS_TEMPLATE));
        assertTrue(filter.checkItemAccess("/custom/test", EContentType.COBOC_FOLDERS_TEMPLATE));
    }        
    
    public void testWildcard() throws Exception
    {
        CobocContentFilter filter = new CobocContentFilter();
        
        filter.addFilter("/cordys/cas/**", false);
        filter.addFilter("3.Business Process Models/MyFlows/**", true, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
        filter.addFilter("3.Business Process Models/OtherFlows/A*/B*", true, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);
        filter.addFilter("**/WC_FLOW", true, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);

        assertTrue(filter.checkItemAccess("/Business Processes/3.Business Process Models/MyFlows/Flow1_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/3.Business Process Models/MyFlows/Flow2_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/3.Business Process Models/MyFlows/Flow3_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/3.Business Process Models/MyFlows/SubFlows/Flow1_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/3.Business Process Models/OtherFlows/Atest/Bflow_demo2.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertFalse(filter.checkItemAccess("/Business Processes/3.Business Process Models/OtherFlows/Atest/Cflow_demo2.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertFalse(filter.checkItemAccess("/Business Processes/3.Business Process Models/OtherFlows/Btest/Bflow_demo2.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));

        assertTrue(filter.checkItemAccess("/Business Processes/3.Business Process Models/OtherFlows/WC_FLOW_demo2.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/3.Business Process Models/OtherFlows/Atest/WC_FLOW_demo2.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/WC_FLOW_demo2.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/Atest/WC_FLOW_demo2.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertFalse(filter.checkItemAccess("/Atest/WC_FLOW_demo2.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
    }      
    
    public void testWildcard2() throws Exception
    {
        CobocContentFilter filter = new CobocContentFilter();
        
        filter.addFilter("/cordys/cas/**", false);
        filter.addFilter("**/File*", true, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);

        assertFalse(filter.checkItemAccess("/FileFlow_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertFalse(filter.checkItemAccess("/Flow1_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/FileFlow_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertFalse(filter.checkItemAccess("/Business Processes/Flow1_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/MyFlows/FileFlow_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertFalse(filter.checkItemAccess("/Business Processes/MyFlows/Flow1_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/3.Business Process Models/MyFlows/FileFlow_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertFalse(filter.checkItemAccess("/Business Processes/3.Business Process Models/MyFlows/Flow1_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
    }    
    
    public void testWildcard3() throws Exception
    {
        CobocContentFilter filter = new CobocContentFilter();
        
        filter.addFilter("/cordys/cas/**", false);
        filter.addFilter("Tests/Test1/**", true, EContentType.COBOC_FOLDERS_PROCESSTEMPLATE);

        assertFalse(filter.checkItemAccess("/TestFlow_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertFalse(filter.checkItemAccess("/Business Processes/TestFlow_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertFalse(filter.checkItemAccess("/Business Processes/Tests/TestFlow_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/Tests/Test1/TestFlow_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/BPML/Tests/Test1/TestFlow_vcmdata.bpm/123asdasd", EContentType.COBOC_FOLDERS_PROCESSBPML));
        assertTrue(filter.checkItemAccess("/Business Processes/BPMN/Tests/Test1/TestFlow_vcmdata.bpm/123asdasd", EContentType.COBOC_FOLDERS_PROCESSBPMN));
        assertTrue(filter.checkItemAccess("/Business Processes/Tests/Test1/Sub1/TestFlow_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertTrue(filter.checkItemAccess("/Business Processes/BPML/Tests/Test1/Sub1/TestFlow_vcmdata.bpm/123asdasd", EContentType.COBOC_FOLDERS_PROCESSBPML));
        assertTrue(filter.checkItemAccess("/Business Processes/BPMN/Tests/Test1/Sub1/TestFlow_vcmdata.bpm/123asdasd", EContentType.COBOC_FOLDERS_PROCESSBPMN));
        assertTrue(filter.checkItemAccess("/Business Processes/Tests/Test1/Sub2/TestFlow_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
        assertFalse(filter.checkItemAccess("/Business Processes/Tests/Test2/TestFlow_vcmdata.bpm", EContentType.COBOC_FOLDERS_PROCESSINSTANCE));
    }       
}
