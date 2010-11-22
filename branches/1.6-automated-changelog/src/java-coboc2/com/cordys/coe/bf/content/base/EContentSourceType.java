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
 * An enumeration for content source types.
 *
 * @author mpoyhone
 */
public enum EContentSourceType {
	BCP,
	FILESYSTEM("file system"),
    ISV;
    
    /**
     * Name of this type used for logging.
     */
    private String sLogName;
    
    /**
     * Default Constructor for EContentSourceType. Uses the enumeration name for log name.
     */
    private EContentSourceType() {
        this.sLogName = toString();
    }    
    
    /**
     * Constructor for EContentSourceType with a custom log name.
     * @param sLogName Custom log name.
     */    
    private EContentSourceType(String sLogName) {
        this.sLogName = sLogName;
    }
 
    /**
     * Returns the log name for this content type.
     * @return Log name.
     */
    public String getLogName() {
        return sLogName;
    }	
}
