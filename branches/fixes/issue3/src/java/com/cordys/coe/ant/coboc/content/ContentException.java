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
package com.cordys.coe.ant.coboc.content;

/**
 * Exception class for CoBOC content exceptions.
 *
 * @author mpoyhone
 */
public class ContentException extends Exception
{
    /**
     * Creates a new ContextException object.
     *
     * @param sMsg The expection message.
     */
    public ContentException(String sMsg)
    {
        super(sMsg);
    }

    /**
     * Creates a new ContextException object.
     */
    public ContentException()
    {
        super();
    }

    /**
     * Creates a new ContextException object.
     *
     * @param message The exception message. 
     * @param cause The causing exception.
     */
    public ContentException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Creates a new ContextException object.
     *
     * @param cause The causing exception.
     */
    public ContentException(Throwable cause)
    {
        super(cause);
    }
}
