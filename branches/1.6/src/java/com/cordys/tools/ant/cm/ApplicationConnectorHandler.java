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

/**
 * The class handles the content of type application connectors. This extends
 * most of the features used commonly in XML Store kind of contents from the
 * Class <code>XMLStoreHandler</code>.
 *
 * @author msreejit
 */
public class ApplicationConnectorHandler extends XMLStoreHandler
    implements ContentHandler
{
    /**
     * DOCUMENTME
     */
    private static final String HANDLER_XMLSTORE_KEY = "/Cordys/WCP/Application Connector";
    /**
     * Message logged when no content is found to import from ECX
     */
    private static final String NO_IMPORT_CONTENT_FOUND = "No applicationconnectors found to import from ECX.";
    /**
     * Message logged when no content is found to export from ECX
     */
    private static final String NO_EXPORT_CONTENT_FOUND = "No applicationconnectors found to export from ECX.";
    /**
     * Message logged when no content is found to delete from ECX
     */
    private static final String NO_DELETE_CONTENT_FOUND = "No applicationconnectors found in ECX to delete.";
    /**
     * Message logged when import from ECX is successful
     */
    private static final String ECX_TO_FILE_SUCCESS = "Successfully exported applicationconnectors from ECX.";
    /**
     * Message logged when export from ECX is successful
     */
    private static final String FILE_TO_ECX_SUCCESS = "Successfully imported applicationconnectors to ECX.";
    /**
     * Message logged when delete from ECX is successful
     */
    private static final String DELETE_SUCCESS = "Successfully deleted applicationconnectors from ECX.";

    /**
     * Default Constructor
     */
    public ApplicationConnectorHandler()
    {
        super();
        setXmlStoreKey(HANDLER_XMLSTORE_KEY);
        setMsgExportContentNotFound(NO_EXPORT_CONTENT_FOUND);
        setMsgImportContentNotFound(NO_IMPORT_CONTENT_FOUND);
        setMsgDeleteContentNotFound(NO_DELETE_CONTENT_FOUND);
        setMsgEcxToFileSucess(ECX_TO_FILE_SUCCESS);
        setMsgFileToEcxSucess(FILE_TO_ECX_SUCCESS);
        setMsgDeleteSucess(DELETE_SUCCESS);
        setContentTag("applicationconnector");
        setContentRootTag("applicationconnectors");
        setIsvContentPattern("?<applicationconnectors><><><><tuple>");
        setUseFullPath(false);
    }
}
