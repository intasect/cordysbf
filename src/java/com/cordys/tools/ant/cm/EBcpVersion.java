/**
 * Copyright 2007 Cordys R&D B.V. 
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
package com.cordys.tools.ant.cm;

/**
 * Enumeration for currently recognized BCP platform versions. These
 * values define major releases which can cause incompatibilities.
 *
 * @author mpoyhone
 */
public enum EBcpVersion
{
    BCP42_C1("Cordys BCP 4.2 C1", "bcp42_c1"),
    BCP42_C2("Cordys BCP 4.2 C2", "bcp42_c2"),
    BCP42_C3("Cordys BCP 4.2 C3", "bcp42_c3");
    
    private String displayName;
    private String cobocName;
    
    /**
     * Constructor for EBcpVersion
     * @param displayName
     * @param cobocName
     */
    private EBcpVersion(String displayName, String cobocName)
    {
        this.displayName = displayName;
        this.cobocName = cobocName;
    }

    public String getDisplayString() {
        return displayName;
    }

    public String getCobocString() {
        return cobocName;
    }
    
    public static EBcpVersion getLatest() {
        EBcpVersion[] a = values();
        
        return a.length > 0 ? a[a.length - 1] : null; 
    }
}
