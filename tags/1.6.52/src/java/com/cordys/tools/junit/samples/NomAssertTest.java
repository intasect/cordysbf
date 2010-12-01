/*
 * Created on Mar 19, 2004
 *
 */
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
package com.cordys.tools.junit.samples;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.cordys.tools.junit.framework.NomAssert;
import com.eibus.xml.nom.Document;

/**
 * <pre>
 * 	This class is to test the functionalities of NomAssert
 * 	This can also be used as a help file to look at the usage.
 * 	
 * 	Tests are written for parseString function of Document class.
 * 	
 * 	public int parseString(java.lang.String xml)
 *  throws XMLException,
 *  java.io.UnsupportedEncodingException
 *  Parses the string. Calls method parseString(xml, "UTF8");
 * 	Parameters:
 *  xml - A string containing XML.
 *  Throws:
 * 	XMLException - if the parser could not parse string
 * 	java.io.UnsupportedEncodingException - If this system does not 
 * 	have a	UTF8 coder
 * 	
 * 	You can see the documentation given for the function in the sdk. (I edited some part)
 * 	So here we need to simulate many conditions, all that we can think of.
 * 	1. Correct loading of string.
 * 	2. Exception throwing for improper xml parsing(if its improper, can you cal it as xml?)
 * 	3. ...
 *  </pre>
 */
public class NomAssertTest extends TestCase
{
    /**
     * DOCUMENTME
     */
    private Document document;

    /**
     * DOCUMENTME
     *
     * @param args DOCUMENTME
     */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(NomAssertTest.class);
    } //main

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public static Test suite()
    {
        return new TestSuite(NomAssertTest.class);
    } //suite	

    /*
     * I found that node tags are case sensitive.
     * So here am trying to load two non similar nodes.
     */
    public void testNonSimilarXMLNodes()
    {
        String sXMLOne = "<new>" + "<black_list>" + "</black_list>" + "</new>";

        String sXMLTwo = "<new>" + "<BLACK_LIST>" + "</BLACK_LIST>" + "</new>";

        int iNodeOne = 0;
        int iNodeTwo = 0;

        try
        {
            iNodeOne = document.parseString(sXMLOne);
            iNodeTwo = document.parseString(sXMLTwo);
        }
        catch (com.eibus.xml.nom.XMLException e)
        {
            fail(e.toString());
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            fail(e.toString());
        }
        catch (Exception e)
        {
            fail(e.toString());
            //Will it come here ever?
        }

        NomAssert.assertValidNode(iNodeOne);
        NomAssert.assertValidNode(iNodeTwo);
        NomAssert.assertNodeNotEquals(iNodeOne, iNodeTwo);
    } //testNonSimilarXMLNodes	

    /*
     * First we need to make sure that the set up function is
     * successfully completed.
     */
    public void testSetUp()
    {
        assertNotNull(document);
    } //testSetUp

    /*
     * Nom formats the string input given. Check different combinations
     * You can try for different combinations of attributes also
     */
    public void testSimilarXMLNodes()
    {
        //This should give two equivalent nodes
        //Check for the difference in string xmls.
        //Both ultimately give similar int xml nodes
        String sXMLOne = "<new>" + "<BLACK_LIST>" + "<URL />" + //<---A space after L and before /
                         "<CATEGORY null=\"true\" />" + //Here close of tag with />
                         "<STATUS null=\"true\"/>" + // no space before />
                         "</BLACK_LIST>" + "</new>";

        String sXMLTwo = "<new>" + "<BLACK_LIST>" + "<URL/>" + //<--No space
                         "<CATEGORY null=\"true\"></CATEGORY>" +
                         "<STATUS null=\"true\" ></STATUS>" + "</BLACK_LIST>" +
                         "</new>";

        int iNodeOne = 0;
        int iNodeTwo = 0;

        try
        {
            iNodeOne = document.parseString(sXMLOne);
            iNodeTwo = document.parseString(sXMLTwo);
        }
        catch (com.eibus.xml.nom.XMLException e)
        {
            fail(e.toString());
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            fail(e.toString());
        }
        catch (Exception e)
        {
            fail(e.toString());
            //Will it come here ever?
        }

        NomAssert.assertValidNode(iNodeOne);
        NomAssert.assertValidNode(iNodeTwo);
        NomAssert.assertNodeEquals(iNodeOne, iNodeTwo);
    } //testSimilarXMLNodes

    /*
     * If am giving a proper formatted xml, then it should get loaded properly
     *
     * In this function you can see how to give an extended xpath query
     */
    public void testSuccessfulParse()
    {
        //This is a proper xml (Dont edit this and try this test)
        //This will also give idea, as to 
        //	1. How to specify attribute values
        //	2. In what all way you can specify a node, if its value is null
        //		"<STATUS/>" 
        //		"<CATEGORY></CATEGORY>" +
        //	3. ... 
        String sDSO = "<dso " + "dataSource=\"srv-ind-vm7a\" " +
                      "driver=\"JDBC\" " +
                      "jdbcDriver=\"com.microsoft.jdbc.sqlserver.SQLServerDriver\" " +
                      "connectionString=\"jdbc:microsoft:sqlserver://srv-ind-vm7a:1024;SelectMethod=cursor\" " +
                      "provider=\"SQLOLEDB\" " + "update=\"true\" " +
                      "defaultDB=\"FFI\" >" + "<cursor-cache>" +
                      "<size id=\"10\">1000</size>" +
                      "<refresh-interval>3600</refresh-interval>" +
                      "</cursor-cache>" + "<query-cache>" +
                      "<size>1000</size>" +
                      "<refresh-interval>3600</refresh-interval>" +
                      "</query-cache>" + "<STATUS/>" +
                      "<CATEGORY><desc><url id=\"abc\">cd</url></desc></CATEGORY>" +
                      "</dso>";

        int iNode = 0;

        try
        {
            iNode = document.parseString(sDSO);
        }
        catch (com.eibus.xml.nom.XMLException e)
        {
            fail(e.toString());
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            fail(e.toString());
        }
        catch (Exception e)
        {
            fail(e.toString());
            //Will it come here ever?
        }

        //It should be a non zero value
        NomAssert.assertValidNode(iNode);

        //-----------------------------------------
        //	Various extended xpath tests are done
        //-----------------------------------------
        //Note how you need to give query value for contions
        String sQuery_01 = "?<query-cache><size>\"1000\"";
        NomAssert.assertTrue(iNode, sQuery_01);

        //Note: A space between ( and ?
        String sQuery_02 = "( ?<query-cache><size>\"1000\" )";
        NomAssert.assertTrue(iNode, sQuery_02);

        //Note: A space between size and id is ok.. ;)
        //Thats a part of a single token. :)
        String sQuery_03 = "( ?<query-cache><size>\"1000\" " +
                           "AND ?<cursor-cache><size id=\"10\">\"1000\" )";
        NomAssert.assertTrue(iNode, sQuery_03);

        //Here first cdn of OR will fail. But second will succeed
        String sQuery_04 = "( ?<query-cache><size>\"1000\" " +
                           "	AND ?<cursor-cache><size id=\"1\">\"1000\" " +
                           ") OR <dso jdbcDriver=\"com.microsoft.jdbc.sqlserver.SQLServerDriver\">";
        NomAssert.assertTrue(iNode, sQuery_04);

        //Make sure that space is there between different tokens
        String sQuery_05 = "?<query-cache><size>\"1000\" " +
                           "AND ?<cursor-cache><size id=\"10\">\"1000\" " +
                           "AND <dso jdbcDriver=\"com.microsoft.jdbc.sqlserver.SQLServerDriver\"> " +
                           "AND <dso>?<desc><url id=\"abc\">\"cd\"";
        NomAssert.assertTrue(iNode, sQuery_05);
    } //testSuccessfulParse

    /*
     * If we give an improper xml is should thorow XMLException
     */
    public void testUnSuccessfulParse()
    {
        //This is an improper xml (Dont edit this and try this test)
        //<refresh-interval> not property closed.
        //So we expect a XMLException on parseString
        String sDSOImproper = "<dso " + "dataSource=\"srv-ind-vm7a\" " +
                              "driver=\"JDBC\" " + "defaultDB=\"FFI\" >" +
                              "<cursor-cache>" + "<size id=\"10\">1000</size>" +
                              "<refresh-interval>3600</interval-refresh>" + //<--------------
                              "</cursor-cache>" + "</dso>";

        int iNode = 0;

        try
        {
            iNode = document.parseString(sDSOImproper);
        }
        catch (com.eibus.xml.nom.XMLException e)
        {
            //If it comes here then test is success
            NomAssert.assertInvalidNode(iNode);
            return;
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            fail(e.toString());
        }
        catch (Exception e)
        {
            fail(e.toString());
            //Will it come here ever?
        }

        //If control comes here then its failure
        fail("testUnSuccessfulParse");
    } //testUnSuccessfulParse

    /*
     * int parseString(java.lang.String xml, java.lang.String encoding)
     * If we give an proper xml and ask for an unknown encoding.
     * Am confused, as what all inputs I can give in the 'encoding' field.
     * Default is "UTF8"
     */
    public void testUnSupportedEncoding()
    {
        //This is an proper xml (Dont edit this and try this test)
        String sDSOImproper = "<dso " + "dataSource=\"srv-ind-vm7a\" " +
                              "driver=\"JDBC\" " + "defaultDB=\"FFI\" >" +
                              "<cursor-cache>" + "<size id=\"10\">1000</size>" +
                              "<refresh-interval>3600</refresh-interval>" +
                              "</cursor-cache>" + "</dso>";

        int iNode = 0;

        try
        {
            iNode = document.parseString(sDSOImproper, "UTF545");
        }
        catch (com.eibus.xml.nom.XMLException e)
        {
            fail(e.toString());
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            //If it comes here then test is success	
            NomAssert.assertInvalidNode(iNode);
            return;
        }
        catch (Exception e)
        {
            fail(e.toString());
            //Will it come here ever?
        }

        //If control comes here then its failure
        fail("testUnSupportedEncoding");
    } //testUnSupportedEncoding

    /**
     * DOCUMENTME
     */
    protected void setUp()
    {
        document = new Document();
    } //setUp
} //NomAssertTest
