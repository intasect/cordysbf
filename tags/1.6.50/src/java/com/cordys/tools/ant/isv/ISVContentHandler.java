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

package com.cordys.tools.ant.isv;

/**
 * Interface for implementing the different ISV content handlers. The ISV
 * content should be returned as an Node reference which will be appended to
 * the ISV xml and send by the <code>ISVCreatorTask</code>
 *
 * @author msreejit
 */
public interface ISVContentHandler
{
    /**
     * The method should be implemented by the ISV content handlers  and will
     * be called accordingly. The task instance is passed on for performing
     * certains tasks in the ant workspace. The ISV content should be returned
     * as  an Node reference which will be appended to the ISV xml and send by
     * the <code>ISVCreatorTask</code> task. The input xml is the consolidated
     * xml prepared the <code>ISVCreatorTask</code> task. The xml is usually
     * specified in the content file attribute which points to a configuration
     * file.
     *
     * @param isvTask The ISV creator task that requests the ISV XML.
     * @param contentTask The corresponding content-task.
     * @param inputXML The input XML structure.
     * @param iCurrentIsvContentNode Contains already generated ISV content.
     * @param iCurrentIsvPromptsetNode Contains already generated ISV prompts
     *        content.
     *
     * @return The content XML created by this ISV content creator.
     */
    public abstract int[] getISVContentXML(ISVCreatorTask isvTask,
                                           Content contentTask, int inputXML,
                                           int iCurrentIsvContentNode,
                                           int iCurrentIsvPromptsetNode);
}
