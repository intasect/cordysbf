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

package com.cordys.tools.ant.soap;


/**
 * This Exception is thrown by the <code>SoapRequestManager</code> Class.
 *
 * @author msreejit
 */
public class SoapRequestException extends Exception {
    /**
     * Holds a nested exception.
     */
    private Throwable m_nestedException;

    /**
     * Default Constructor. Creates a new SoapRequestException object.
     */
    public SoapRequestException() {
        this(null, null);
    }

    /**
     * Creates a new SoapRequestException object with a message.
     *
     * @param message The message to be passed in the exception.
     */
    public SoapRequestException(String message) {
        this(message, null);
    }

    /**
     * Creates a new SoapRequestException object with a nested exception.
     *
     * @param nestedException The nested exception.
     */
    public SoapRequestException(Throwable nestedException) {
        this(null, nestedException);
    }

    /**
     * Creates a new SoapRequestException object with a message and a nested
     * exception.
     *
     * @param message The message to be passed in the exception.
     * @param nestedException The nested exception.
     */
    public SoapRequestException(String message, Throwable nestedException) {
        super(message);
        m_nestedException = nestedException;
    }

    /**
     * Returns the nested exception in the Soap Request Exception
     *
     * @return The nested exception.
     */
    public Throwable getNestedException() {
        return m_nestedException;
    }

    /**
     * Returns the messages from the exception and the its nested exception.
     *
     * @return The messages from this excepion and the nested exception.
     */
    public String toString() {
        String ret = super.toString();
        Throwable nested = getNestedException();

        if (nested != null) {
            ret += ("\nNested: " + nested.toString());
        }

        return ret;
    }

    /**
     * @see java.lang.Throwable#getCause()
     */
    public Throwable getCause() {
        return m_nestedException;
    }
}
