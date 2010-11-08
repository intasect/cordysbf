/**
 * Copyright 2004 Cordys R&D B.V. 
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
 /*
 *         Project         :        BuildFramework
 *         File                :        BodyBlockCreator.java
 *         Author                :        manesh@cordys.com
 *         Created on         :        Oct 20, 2004
 *
 */
package com.eibus.soap;


/**
 * Wrapper around com.eibus.soap.BodyBlock, so that it can be used     by
 * ISVCreator (which invokes the CreateISVPackage at api level)
 */
public class BodyBlockCreator
{
    /**
     * DOCUMENTME
     */
    BodyBlock bodyBlock = null;

    /**
     * Creates a new BodyBlockCreator object.
     *
     * @param node DOCUMENTME
     */
    public BodyBlockCreator(int node)
    {
        bodyBlock = new BodyBlock(node, null, null);
    }

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public BodyBlock getRequestBodyBlock()
    {
        return bodyBlock;
    }

    /**
     * DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public BodyBlock getResponseBodyBlock()
    {
        return bodyBlock.getResponseBodyBlock();
    }
}
