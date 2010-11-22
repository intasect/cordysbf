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
package com.cordys.tools.junit.framework;

import junit.framework.Assert;

import com.eibus.xml.nom.Find;

/**
 * DOCUMENT ME!
 *
 * @author <a href="mailto:manesh@cordys.com">Manesh R</a>
 */
public class NomAssert
{
    //These are the status flags returned by NomXPathValidator
    /**
     * DOCUMENTME
     */
    private static final int ERROR = -1;
    /**
     * DOCUMENTME
     */
    private static final int FALSE = 0;
    /**
     * DOCUMENTME
     */
    private static final int TRUE = 1;

    /**
     * private constructor since it is a static only class
     */
    private NomAssert()
    {
    } //NomAssert

    /**
     * Extended xpath assertion on node.
     * <pre>
     * Here you can specify an extened xpath.
     * Fail if
     * 		xpath is not proper
     * 		node is invalid
     * 		node satisfy that condition
     * </pre>
     *
     * @param message Message to display on failure
     * @param node nom node on which condition has to be applied
     * @param condition Extended xPath Query that should be satisfied.
     */
    public static void assertFalse(String message, int node, String condition)
    {
        assertCondition(message, node, condition, FALSE);
    } //assertTrue

    /**
     * Calls assertFalse(null, node, condition)
     *
     * @param node DOCUMENTME
     * @param condition DOCUMENTME
     */
    public static void assertFalse(int node, String condition)
    {
        assertFalse(null, node, condition);
    } //assertTrue

    /**
     * Asserts that node is invalid
     *
     * @param message DOCUMENTME
     * @param node DOCUMENTME
     */
    public static void assertInvalidNode(String message, int node)
    {
        if (Find.node(node) == true)
        {
            fail(message);
        }
    } //assertInvalidNode

    /**
     * Calls assertValidNode(null, node)
     *
     * @param node DOCUMENTME
     */
    public static void assertInvalidNode(int node)
    {
        assertInvalidNode(null, node);
    } //assertInvalidNode

    /**
     * Asserts that two nodes are equal.
     * <pre>
     * 	The check is for complete comparison node equal, attributes equal 
     * 	and values equal. If it isn't it throws an AssertionFailedError 
     * 	with the given message.
     * 
     * 	Here if any of the nodes is not valid, then it will throw error.
     * 	</pre>
     *
     * @param message DOCUMENTME
     * @param expected DOCUMENTME
     * @param actual DOCUMENTME
     */
    public static void assertNodeEquals(String message, int expected, int actual)
    {
        if ((Find.node(expected) == false) || (Find.node(actual) == false))
        {
            fail("Invalid Node: " + message);
            return;
        }

        //In this case its obvious
        if (expected == actual)
        {
            return;
        }

        //Compare using the function provided by nom package
        if (Find.listCompare(expected, expected, actual, actual, 0, 0) == 0)
        {
            return;
        }

        fail(message);
    } //assertNodeEquals

    /**
     * Calls assertNodeEquals(null, expected, actual)
     *
     * @param expected DOCUMENTME
     * @param actual DOCUMENTME
     */
    public static void assertNodeEquals(int expected, int actual)
    {
        assertNodeEquals(null, expected, actual);
    } //assertNodeEquals

    /**
     * Asserts that two nodes are not equal.
     * <pre>
     * 	Checking is same as that for assertNodeEquals.
     * 	If its found to be equal, then it will fail.
     * 
     * 	Here if any of the nodes is not valid, then it will throw error.
     * 	</pre>
     *
     * @param message DOCUMENTME
     * @param expected DOCUMENTME
     * @param actual DOCUMENTME
     */
    public static void assertNodeNotEquals(String message, int expected,
                                           int actual)
    {
        if ((Find.node(expected) == false) || (Find.node(actual) == false))
        {
            fail("Invalid Node: " + message);
            return;
        }

        //In this case its obvious
        if (expected == actual)
        {
            fail(message);
            return;
        }

        if (Find.listCompare(expected, expected, actual, actual, 0, 0) == 0)
        {
            fail(message);
        }
    } //assertNodeNotEquals

    /**
     * Calls assertNodeNotEquals(null, expected, actual)
     *
     * @param expected DOCUMENTME
     * @param actual DOCUMENTME
     */
    public static void assertNodeNotEquals(int expected, int actual)
    {
        assertNodeNotEquals(null, expected, actual);
    } //assertNodeNotEquals

    /**
     * This assertion is for simple integers.     Fails if they are equal
     *
     * @param message DOCUMENTME
     * @param expected DOCUMENTME
     * @param actual DOCUMENTME
     */
    public static void assertNotEquals(String message, int expected, int actual)
    {
        if (expected == actual)
        {
            fail(message);
        }
    } //assertNotEquals

    /**
     * Extended xpath assertion on node.
     * <pre>
     * Here you can specify an extened xpath.
     * Fail if
     * 		xpath is not proper
     * 		node is invalid
     * 		node doesnt satisfy that condition
     * </pre>
     *
     * @param message Message to display on failure
     * @param node nom node on which condition has to be applied
     * @param condition Extended xPath Query that should be satisfied.
     */
    public static void assertTrue(String message, int node, String condition)
    {
        assertCondition(message, node, condition, TRUE);
    } //assertTrue

    /**
     * Calls assertTrue(null, node, condition)
     *
     * @param node DOCUMENTME
     * @param condition DOCUMENTME
     */
    public static void assertTrue(int node, String condition)
    {
        assertTrue(null, node, condition);
    } //assertTrue

    /**
     * Asserts that node is valid
     *
     * @param message DOCUMENTME
     * @param node DOCUMENTME
     */
    public static void assertValidNode(String message, int node)
    {
        if (Find.node(node) == false)
        {
            fail(message);
        }
    } //assertValidNode

    /**
     * Calls assertValidNode(null, node)
     *
     * @param node DOCUMENTME
     */
    public static void assertValidNode(int node)
    {
        assertValidNode(null, node);
    } //assertValidNode

    /**
     * Fails a test with the given message.  Internally calls fail method of
     * Assert
     *
     * @param message DOCUMENTME
     */
    public static void fail(String message)
    {
        Assert.fail(message);
    } //fail

    /*
     * For internal use only.
     */
    private static void assertCondition(String message, int node,
                                        String condition, int flag)
    {
        if (Find.node(node) == false)
        {
            fail("Invalid Node: " + message);
            return;
        }

        NomXPathValidator _obj = new NomXPathValidator();
        int iRet = _obj.executeXPathQuery(node, condition);

        if (iRet == ERROR)
        {
            fail("Invalid XPath: " + message);
            return;
        }

        if (iRet != flag)
        {
            fail(message);
        }
    } //assertCondition
}
