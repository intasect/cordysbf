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

package com.cordys.tools.ant.util;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.XMLException;

/**
 * Helper class for XSL related functions.
 *
 * @author msreejit
 */
public class XSLUtil
{
    /**
     * document used to parse the string to Node reference.
     */
    private static Document document = new Document();

    /**
     * Helper method to apply XSL transformation on a XML String using XSL
     * Stylesheet.
     *
     * @param xmlString The string which represents the XML text.
     * @param xslInputStream The string which represents the XSL text.
     * @param paramNames The array of names of the parameters in the XSL file
     *        which need to be replaced.     Can be null if parameter is not
     *        required.
     * @param paramValues The array of parameter values that need to be
     *        replaced.     Can be null if parameter is not required.
     *
     * @return The transformed result as an Node reference(int).
     *
     * @throws UnsupportedEncodingException If any encoding error occurs while
     *         doing XML operations.
     * @throws XMLException If any error occurs while doing XML operations.
     * @throws TransformerException If any error occurs while doing XML
     *         transformations.
     */
    public static int getXSLTransformAsNode(String xmlString,
                                            InputStream xslInputStream,
                                            String[] paramNames,
                                            String[] paramValues)
                                     throws UnsupportedEncodingException, 
                                            XMLException, TransformerException
    {
        return document.parseString(getXSLTransformAsString(xmlString,
                                                            xslInputStream,
                                                            paramNames,
                                                            paramValues));
    }

    /**
     * Helper method to apply XSL transformation on a XML String using XSL
     * Stylesheet.
     *
     * @param xmlFile File which points to the xml file.
     * @param xslInputStream The string which represents the XSL text.
     * @param paramNames The array of names of the parameters in the XSL file
     *        which need to be replaced.     Can be null if parameter is not
     *        required.
     * @param paramValues The array of parameter values that need to be
     *        replaced.     Can be null if parameter is not required.
     *
     * @return The transformed result as an Node reference(int).
     *
     * @throws TransformerException If any error occurs while doing XML
     *         transformations.
     * @throws UnsupportedEncodingException If any encoding error occurs while
     *         doing XML operations.
     * @throws XMLException If any error occurs while doing XML operations.
     */
    public static int getXSLTransformAsNode(File xmlFile,
                                            InputStream xslInputStream,
                                            String[] paramNames,
                                            String[] paramValues)
                                     throws TransformerException, 
                                            UnsupportedEncodingException, 
                                            XMLException
    {
        return document.parseString(getXSLTransformAsString(xmlFile,
                                                            xslInputStream,
                                                            paramNames,
                                                            paramValues));
    }

    /**
     * Helper method to apply XSL transformation on a XML String using XSL
     * Stylesheet
     *
     * @param xmlString The string which represents the XML text.
     * @param xslInputStream The string which represents the XSL text.
     * @param paramNames The array of names of the parameters in the XSL file
     *        which need to be replaced.     Can be null if parameter is not
     *        required.
     * @param paramValues The array of parameter values that need to be
     *        replaced.     Can be null if parameter is not required.
     *
     * @return The transformation result.
     *
     * @throws TransformerException If any error occurs while doing XML
     *         transformations.
     */
    public static String getXSLTransformAsString(String xmlString,
                                                 InputStream xslInputStream,
                                                 String[] paramNames,
                                                 String[] paramValues)
                                          throws TransformerException
    {
        return getTransformedString(xmlString, xslInputStream, paramNames,
                                    paramValues);
    }

    /**
     * Helper method to apply XSL transformation on a XML String using XSL
     * Stylesheet
     *
     * @param xmlFile File which points to the xml file.
     * @param xslInputStream File which points to the the xsl file.
     * @param paramNames The array of names of the parameters in the XSL file
     *        which need to be replaced.     Can be null if parameter is not
     *        required.
     * @param paramValues The array of parameter values that need to be
     *        replaced.     Can be null if parameter is not required.
     *
     * @return The transformation result.
     *
     * @throws TransformerException If any error occurs while doing XML
     *         transformations.
     */
    public static String getXSLTransformAsString(File xmlFile,
                                                 InputStream xslInputStream,
                                                 String[] paramNames,
                                                 String[] paramValues)
                                          throws TransformerException
    {
        return getTransformedString(xmlFile, xslInputStream, paramNames,
                                    paramValues);
    }

    /**
     * For internal use - Perform an xsl transform.
     *
     * @param xmlObj object containing the xml to be transformed
     * @param xslInputStream object containing the xsl stylesheet
     * @param paramNames The array of names of the parameters in the XSL file
     *        which need to be replaced.     Can be null if parameter is not
     *        required.
     * @param paramValues The array of parameter values that need to be
     *        replaced.     Can be null if parameter is not required.
     *
     * @return results of the transform
     *
     * @throws TransformerException If any error occurs while doing XML
     *         transformations.
     */
    private static String getTransformedString(Object xmlObj,
                                               InputStream xslInputStream,
                                               String[] paramNames,
                                               String[] paramValues)
                                        throws TransformerException
    {
        String output = null;
        StringWriter xmlOutputWriter = new StringWriter();

        if (xmlObj instanceof String)
        {
            String xmlString = (String) xmlObj;
            StringReader xmlSourceReader = new StringReader(xmlString);
            transform(new StreamSource(xmlSourceReader),
                      new StreamSource(xslInputStream),
                      new StreamResult(xmlOutputWriter), paramNames, paramValues);
        }

        if (xmlObj instanceof File)
        {
            File xmlSourceFile = (File) xmlObj;
            transform(new StreamSource(xmlSourceFile),
                      new StreamSource(xslInputStream),
                      new StreamResult(xmlOutputWriter), paramNames, paramValues);
        }

        if (xmlObj instanceof Reader)
        {
            Reader xmlSourceReader = (Reader) xmlObj;
            transform(new StreamSource(xmlSourceReader),
                      new StreamSource(xslInputStream),
                      new StreamResult(xmlOutputWriter), paramNames, paramValues);
        }

        //write the contents to a String
        output = xmlOutputWriter.toString();

        //flush the xmlOutputWriter contents.
        xmlOutputWriter.flush();

        //return the transformed output string
        return output;
    }

    /**
     * For internal use - Perform an xsl transform.
     *
     * @param xmlFile stream containing the xml file to be transformed
     * @param xslFile stream containing the xsl stylesheet
     * @param output stream containing writer to output the results of the
     *        transform
     * @param paramNames The array of names of the parameters in the XSL file
     *        which need to be replaced.     Can be null if parameter is not
     *        required.
     * @param paramValues The array of parameter values that need to be
     *        replaced.     Can be null if parameter is not required.
     *
     * @throws TransformerException If any error occurs while doing XML
     *         transformations.
     */
    private static void transform(StreamSource xmlFile, StreamSource xslFile,
                                  StreamResult output, String[] paramNames,
                                  String[] paramValues)
                           throws TransformerException
    {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(xslFile);

        if (paramNames != null)
        {
            for (int k = 0; k < paramNames.length; k++)
            {
                //check if null value is being passed.
                if (paramValues[k] == null)
                {
                    paramValues[k] = "";
                }

                if (paramNames[k] != null)
                {
                    transformer.setParameter(paramNames[k], paramValues[k]);
                }
            }
        }

        transformer.transform(xmlFile, output);
    }
}
