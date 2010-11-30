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
 package com.cordys.coe.ant.bf.upgrade;

/**
 * Class that identifies Upgrade Exceptions.
 *
 * @author pgussow
 */
public class BFUpgradeException extends Exception
{
/**
     * Creates a new BFUpgradeException object.
     */
    public BFUpgradeException()
    {
    }

/**
     * Creates a new BFUpgradeException object.
     *
     * @param sMessage The exception message.
     */
    public BFUpgradeException(String sMessage)
    {
        super(sMessage);
    }

/**
     * Creates a new BFUpgradeException object.
     *
     * @param cCause The cause of this exception.
     */
    public BFUpgradeException(Throwable cCause)
    {
        super(cCause);
    }

/**
     * Creates a new BFUpgradeException object.
     *
     * @param sMessage The exception message.
     * @param cCause The cause of this exception.
     */
    public BFUpgradeException(String sMessage, Throwable cCause)
    {
        super(sMessage, cCause);
    }
}
