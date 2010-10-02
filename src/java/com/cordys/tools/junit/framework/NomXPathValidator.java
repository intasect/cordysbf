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

import java.util.StringTokenizer;

import com.eibus.xml.nom.Find;

/**
 * This interpreter for xml nom nodes is based on the following grammer
 * <pre>
 *  stmt	-->	NOT stmt
 * 	|	stmt OR stmt
 * 	|	stmt AND stmt
 * 	|	term
 * 
 *  term	-->	( stmt )
 * 	|	xPathSearchPattern
 * 
 * 
 * 	We need to avoid left recursion in the grammer. 
 * 	The rule for removing left recursion is as follows
 * 
 *  A	--> Ax | B
 * 
 *  Change it into 
 *  A	--> BR
 *  R	--> xR	| £
 * 
 * 	We apply the above rule to our grammer. Then it becomes
 * 
 *  stmt	--> NOT stmt rest
 * 	|	term rest
 * 
 *  rest	-->	OR term rest
 * 	|	AND term rest
 * 	|	£
 * 
 *  term	-->	( stmt )
 * 	|	xPathSearchPattern
 * 
 * 	Interpreter is written based on the above reduced grammar.
 * 	
 *  See the sample test files to know more about the usage.
 * 	junit.samples.NomAssertTest
 * 
 * 	</pre>
 */
public class NomXPathValidator
{
    /**
     * DOCUMENTME
     */
    private static final boolean bDEBUG = false;
    /**
     * DOCUMENTME
     */
    private static final String LP = "(";
    /**
     * DOCUMENTME
     */
    private static final String RP = ")";
    /**
     * DOCUMENTME
     */
    private static final String NOT = "NOT";
    /**
     * DOCUMENTME
     */
    private static final String AND = "AND";
    /**
     * DOCUMENTME
     */
    private static final String OR = "OR";
    /**
     * DOCUMENTME
     */
    private static final String DONE = "DONE";
    /**
     * DOCUMENTME
     */
    private static final String[] RESERVED_WORDS = { LP, RP, NOT, AND, OR, DONE };
    /**
     * DOCUMENTME
     */
    private String sQuery = null;
    /*
     * Private members of class instance
     */
    /**
     * DOCUMENTME
     */
    private String[] tokens = null; //Array of tokens generated
    /**
     * DOCUMENTME
     */
    private int iNode = 0;
    /**
     * DOCUMENTME
     */
    private int tokenIndex = -1; //Recursive fns can use it as global

    /**
     * Creates a new NomXPathValidator object.
     */
    public NomXPathValidator()
    {
    }

    /**
     * Run extended xpath search on node.
     * <pre>
     * Here you can specify an extended xPath query as supported by the grammar
     * shown by the grammar above. 
     * </pre>
     *
     * @param iNode xml nom node
     * @param sQuery extended xpath query
     *
     * @return -1    If it cannot resolve xPath or some other error occurs<br>
     *         0    If node doesnt satisfy the sQuery<br>
     *         1     If node satisfies the sQuery<br>
     */
    public int executeXPathQuery(int iNode, String sQuery)
    {
        clear(); //clear all previous data if any

        this.iNode = iNode;
        this.sQuery = sQuery;

        prepareTokens();

        int lRet = stmtExecuter();

        if (match(DONE)) //At the end of execute you need to reach last token
        {
            return lRet;
        }

        return -1;
    } //executeXPathQuery

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private String getCurrentToken()
    {
        return tokens[tokenIndex];
    } //getCurrentToken

    //-------------------------------------------------------------------
    //	General functions required for compiler 
    //	advance, match, getCurrentToken
    //-------------------------------------------------------------------
    private void advance()
    {
        tokenIndex++;
    } //advance

    /*
     * Clear the object instance data before running execute
     */
    private void clear()
    {
        tokens = null;
        tokenIndex = -1;
    } //clear

    /**
     * DOCUMENTME
     *
     * @param token DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private boolean match(String token)
    {
        if (tokenIndex == -1)
        {
            advance();
        }

        if (token.compareToIgnoreCase(tokens[tokenIndex]) == 0)
        {
            return true;
        }

        return false;
    } //match

    /*
     * Prepare the token array. Here SP (' ') is the delimitter for tokens
     * If space is there between two non reserverd words, then those are
     * together taken as a single token.
     *
     * In the first loop, counts the tokens.
     * In the second loop polpulates the tokens array.
     */
    private void prepareTokens()
    {
        int count = 0;
        boolean bPrevTokenWasReserved = true;
        StringTokenizer st = new StringTokenizer(sQuery);

        while (st.hasMoreTokens())
        {
            String token = st.nextToken();
            boolean bReserverdWord = false;

            for (int i = 0; i < RESERVED_WORDS.length; i++)
            { //dont want DONE

                if (token.compareToIgnoreCase(RESERVED_WORDS[i]) == 0)
                {
                    bReserverdWord = true;
                }
            }

            //Increment count only if either of perv or current token 
            //is reserved. Else its either a new token or extenstion of
            //prev token that we saw in this while loop.
            if (bPrevTokenWasReserved || bReserverdWord)
            {
                count++;
            }
            bPrevTokenWasReserved = bReserverdWord;
        } //end of while

        tokens = new String[count + 1]; //Want to put DONE at end
        tokens[count] = DONE;
        st = null;
        st = new StringTokenizer(sQuery);
        count = -1; //using tokens[++count]
        bPrevTokenWasReserved = true;

        while (st.hasMoreTokens())
        {
            String token = st.nextToken();
            boolean bReserverdWord = false;

            for (int i = 0; i < (RESERVED_WORDS.length - 1); i++)
            { //dont want DONE

                if (token.compareToIgnoreCase(RESERVED_WORDS[i]) == 0)
                {
                    bReserverdWord = true;
                }
            }

            if (bPrevTokenWasReserved || bReserverdWord)
            {
                tokens[++count] = token;
            }
            else
            {
                //This is obvious (You have only 2*2 = 4 combinations)
                //if(!bPrevTokenWasReserved && !bReserverdWord) {
                tokens[count] = tokens[count] + " " + token;
            }
            bPrevTokenWasReserved = bReserverdWord;
        } //end of while

        if (bDEBUG)
        {
            for (int i = 0; i < tokens.length; i++)
            {
                System.out.println(tokens[i]);
            }
        }
    } //prepareTokens

    /**
     * DOCUMENTME
     *
     * @param input DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private int restExecuter(int input)
    {
        if (match(OR))
        { //rest	-->	OR term rest
            advance();

            int lRet = termExecuter();

            if ((input == 1) || (lRet == 1))
            {
                return restExecuter(1);
            }
            else if ((input == 0) && (lRet == 0))
            {
                return restExecuter(0);
            }
            else
            {
                return -1; //ERROR
            }
        }
        else if (match(AND))
        { //rest	-->	AND term rest
            advance();

            int lRet = termExecuter();

            if ((input == 1) && (lRet == 1))
            {
                return restExecuter(1);
            }
            else if ((input == 0) || (lRet == 0))
            {
                return restExecuter(0);
            }
            else
            {
                return -1; //ERROR
            }
        }
        else
        { //rest	-->	£
            return input;
        }
    } //restExecuter

    /**
     * Functions required for compier according to the grammar Please check the
     * grammar also to understand the flow of these.  Here AND , OR are
     * handled in restExecuter. Since they are binary operators am using
     * workaround of passing an i/p
     *
     * @return DOCUMENTME
     */
    private int stmtExecuter()
    {
        if (match(NOT))
        { //stmt	--> NOT stmt rest
            advance();

            int lRet = stmtExecuter();

            //Apply Not on stmt
            if (lRet == 0)
            {
                return restExecuter(1);
            }
            else if (lRet == 1)
            {
                return restExecuter(0);
            }
            else
            { //will it ever come here (for some exception)
                return -1;
            }
        }
        else
        { //stmt	--> term rest

            int lRet = termExecuter();

            return restExecuter(lRet);
        }
    } //stmtExecuter

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private int termExecuter()
    {
        if (match(LP))
        { //term	-->	( stmt )	
            advance();

            int lRet = stmtExecuter();

            if (match(RP))
            {
                advance();
                return lRet;
            }
            return -1;
        }
        else
        { //term	-->	xPathSearchPattern
            return xPathExecuter();
        }
    } //termExecuter

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private int xPathExecuter()
    {
        String xPathQuery = getCurrentToken();
        advance(); //Advance only after takin current token

        //Try to get a node that satisfies the query
        int node = Find.firstMatch(iNode, xPathQuery);

        if (node == 0)
        {
            return 0;
        }
        else
        {
            return 1;
        }
    } //xPathExecuter
} //NomXPathValidator
