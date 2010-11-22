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
package com.cordys.tools.ant.taskdefs;

import org.apache.tools.ant.Task;

/**
 * This class wraps around the soap processor that can be started/stopped.
 *
 * @author pgussow
 */
public class Processor extends Task
{
    /**
     * Holds the DN of the processor.
     */
    private String dn;
    /**
     * Holds the organization in which the processor is running
     */
    private String organization;

    /**
     * Constructor.
     */
    public Processor()
    {
        super();
    }

    /**
     * This method sets the value of dn.
     *
     * @param dn The new value for dn.
     */
    public void setDn(String dn)
    {
        this.dn = dn;
    }

    /**
     * This method gets the dn
     *
     * @return The dn.
     */
    public String getDn()
    {
        return dn;
    }

    /**
     * This method sets the value of organization.
     *
     * @param organization The new value for organization.
     */
    public void setOrganization(String organization)
    {
        this.organization = organization;
    }

    /**
     * This method gets the organization
     *
     * @return The organization.
     */
    public String getOrganization()
    {
        return organization;
    }
}
