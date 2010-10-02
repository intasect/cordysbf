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
package com.cordys.coe.bf.content.base;

/**
 * A base interface for content read and write methods. Currently
 * this is only used as a marker interface.
 *
 * @author mpoyhone
 */
public interface IContentHandlingMethod
{
    /**
     * Returns the method version string or <code>null</code> if this supports all versions.
     * @return The method version string or <code>null</code> if this supports all versions.
     */
    public String getMethodVersion();
    /**
     * Enables or disbales content filtering for this method.
     * @param bOn If <code>true</code> content filters are used, otherwise not.
     */
    public void setContentFilterStatus(boolean bOn);
    /**
     * Returns the content filtering flag.
     * @return  If <code>true</code> content filters are used, otherwise not.
     */
    public boolean getContentFilterStatus();
}
