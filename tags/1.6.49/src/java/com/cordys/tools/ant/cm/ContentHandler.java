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

package com.cordys.tools.ant.cm;

import java.io.File;
import java.io.IOException;

import com.cordys.tools.ant.soap.ISoapRequestManager;

/**
 * This interface should be implemented by all content handlers which are used
 * by the 'contentmanager' task.
 *
 * @author msreejit
 */
public interface ContentHandler
{
    /**
     * The method should be implemented by the content handlers  and
     * will be called according the operation specified. The method deletes
     * the content specified. The task instance is passed on for performing
     * certains tasks in the  ant workspace.
     *
     * @param cmtTask The current contentmanager task.
     * @param cContent The specific content that needs to be published.
     * @param srmSoap The object to use for sending soap messages.
     */
    public void executeDelete(ContentManagerTask cmtTask, Content cContent,
                              ISoapRequestManager srmSoap);

    /**
     * The method should be implemented by the content handlers  and
     * will be called according the operation specified. The method exports
     * contents from the ECX to a file or files in a directory. The task
     * instance is passed on for performing certains tasks in the  ant
     * workspace.
     *
     * @param cmtTask The current contentmanager task.
     * @param cContent The specific content that needs to be published.
     * @param srmSoap The object to use for sending soap messages.
     */
    public void executeEcxToFile(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap);

    /**
     * The method should be implemented by the content handlers  and
     * will be called according the operation specified. The method exports
     * contents from the file or files found inside a directory  to the ECX
     * machine. The task instance is passed on for performing certains tasks
     * in the ant workspace.
     *
     * @param cmtTask The current contentmanager task.
     * @param cContent The specific content that needs to be published.
     * @param srmSoap The object to use for sending soap messages.
     */
    public void executeFileToEcx(ContentManagerTask cmtTask, Content cContent,
                                 ISoapRequestManager srmSoap);

    /**
     * This method should take care of publishing the specific content
     * to runtime. For example studio flows need to be published to the
     * runtime environment before they can be run. This method should take
     * care of that.
     *
     * @param cmtTask The current contentmanager task.
     * @param cContent The specific content that needs to be published.
     * @param srmSoap The object to use for sending soap messages.
     */
    public void executePublishToRuntime(ContentManagerTask cmtTask,
                                        Content cContent,
                                        ISoapRequestManager srmSoap);

    /**
     * Returns the number of items that were processed during the last
     * execution method.
     *
     * @return Number of processed items or -1 if this task does not support
     *         this method.
     */
    public int getNumberOfProcessedItems();
    
    /**
     * Returns the single content name from the given content file. The file
     * must be recognized by this handler. 
     * @param contentFile Content file. 
     * @param cmtTask TODO
     * @param content Content object.
     * @param toEcx TODO
     * @return Single content name or <code>null</code> if the content name could not be determined.
     * @throws IOException 
     */
    public String getSingleContentName(File contentFile, ContentManagerTask cmtTask, Content content, boolean toEcx) throws IOException;
}
