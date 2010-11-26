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
package com.cordys.coe.ant.studio.content;

import com.cordys.coe.exception.GeneralException;

/**
 * An exception class for Studio operations
 *
 * @author mpoyhone 
 */
public class StudioException extends GeneralException
{
    /**
     * Constructs a new exception object.
     */
    public StudioException()
    {
        super();
    }

    /**
     * Constructs a new exception object.
     *
     * @param sMsg Exception message.
     */
    public StudioException(String sMsg)
    {
        super(sMsg);
    }

    /**
     * Constructs a new exception object.
     *
     * @param tCause Causing exception.
     */
    public StudioException(Throwable tCause)
    {
        super(tCause);
    }

    /**
     * Constructs a new exception object.
     *
     * @param sMsg Exception message.
     * @param tCause Causing exception.
     */
    public StudioException(String sMsg, Throwable tCause)
    {
        super(tCause, sMsg);
    }
}
