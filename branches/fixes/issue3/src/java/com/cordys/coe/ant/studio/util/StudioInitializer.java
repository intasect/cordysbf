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
package com.cordys.coe.ant.studio.util;

import com.cordys.coe.util.xml.Message;
import com.cordys.coe.util.xml.MessageContext;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.cordys.tools.ant.soap.SoapRequestException;
import com.eibus.xml.nom.Document;

/**
 * Helper class for initializing Studio and filling the user preferences.
 *
 * @author mpoyhone
 */
public class StudioInitializer
{
    /**
     * The static document used in message templates.
     */
    public static final Document dRequestDoc = new Document();
    /**
     * The Studio GetObject method SOAP message template for determining if Studio is already initialized.
     */
    protected static Message mMethodGetInitializationStatus;    
    /**
     * The Studio Initialize method SOAP message template.
     */
    protected static Message mMethodInitialize;
    /**
     * The Studio GetObject method SOAP message template for determining if user preferences are set.
     */
    protected static Message mMethodGetUserPreferences;      
    /**
     * The Studio UpdateObject method SOAP message template for inserting default user preferences.
     */
    protected static Message mMethodInsertDefaultUserPreferences;     

    static
    {
        // Builds the needed SOAP messages on class load.
        buildMessages();
    }

    
    /**
     * Builds the needed SOAP messages when this class is loaded.
     *
     * @throws IllegalStateException Thrown if the message parsing failed.
     */
    protected static void buildMessages()
                                 throws IllegalStateException
    {
        String sXml = "";

        try
        {
            sXml = "<GetObject xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">" + 
                   "   <key version=\"organization\" vcmApplication=\"vcmDefault\" vcmVersion=\"vcmDefault\">/cordys/cas/vcm/studio/admin/parameters/parameters</key>" + 
                   "</GetObject>";
            mMethodGetInitializationStatus = new Message(dRequestDoc, sXml);
            mMethodGetInitializationStatus.getSharedXmlTree().setReadOnly(true);
            
            sXml = "<Initialize xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\" reInitialize=\"false\"/>";
            mMethodInitialize = new Message(dRequestDoc, sXml);
            mMethodInitialize.getSharedXmlTree().setReadOnly(true);    
            
            sXml = "<GetObject xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">" + 
                    "  <key version=\"user\" vcmApplication=\"vcmDefault\" vcmVersion=\"vcmDefault\">/cordys/cas/vcm/studio/preferences/preferences</key>" + 
                    "</GetObject>";
            mMethodGetUserPreferences = new Message(dRequestDoc, sXml);
            mMethodGetUserPreferences.getSharedXmlTree().setReadOnly(true);  
            
            sXml = "<UpdateObject xmlns=\"http://schemas.cordys.com/1.0/cas-vcm\">" + 
                    "  <tuple key=\"/cordys/cas/vcm/studio/preferences/preferences\" name=\"preferences\" objectID=\"0\" lastModified=\"0\" vcmApplication=\"vcmDefault\" vcmVersion=\"vcmDefault\" version=\"user\" templatePath=\"/cordys/cas/vcm/templates/studio/preferences/preferences\">" + 
                    "   <new>" + 
                    "     <preferences>" + 
                    "       <documentProperties>" + 
                    "         <name>preferences</name>" + 
                    "         <description>Studio User Preferences</description>" + 
                    "         <caption/>" + 
                    "         <mimeType/>" + 
                    "         <notes/>" + 
                    "         <version/>" + 
                    "         <revision/>" + 
                    "         <createdBy/>" + 
                    "         <creationDate/>" + 
                    "         <lastModifiedBy/>" + 
                    "         <lastModificationDate/>" + 
                    "       </documentProperties>" + 
                    "       <content>" + 
                    "         <currentVersion>vcmdemo10</currentVersion>" + 
                    "         <studioTreeFilter>allObjects</studioTreeFilter>" + 
                    "         <showPublishedModels>false</showPublishedModels>" + 
                    "         <showStatusbar>true</showStatusbar>" + 
                    "         <showToolbar>true</showToolbar>" + 
                    "         <statusbarInformation>userVersion</statusbarInformation>" + 
                    "         <studioTreeDescriptionMask>objectName-objectDescription</studioTreeDescriptionMask>" + 
                    "         <versionDescriptionMask>objectDescription</versionDescriptionMask>" + 
                    "         <casRepositoryTreeDescriptionMask>objectDescription</casRepositoryTreeDescriptionMask>" + 
                    "       </content>" + 
                    "     </preferences>" + 
                    "   </new>" + 
                    "  </tuple>" + 
                    "</UpdateObject>";
            mMethodInsertDefaultUserPreferences = new Message(dRequestDoc, sXml);
            mMethodInsertDefaultUserPreferences.getSharedXmlTree().setReadOnly(true);              
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to parse the SOAP message " +
                                            sXml + " : " + e);
        }
    }
    
    /**
     * Determines if Studio is initialized for the organization that is set in the SOAP manager object.
     *
     * @param srmSoap SOAP manager object.
     * @param ctx XML message context object.
     * @return <code>true</code> if Studio was initialized. 
     */
    public static boolean isStudioInitialized(ISoapRequestManager srmSoap, MessageContext ctx)
    {
        Message mRequest = ctx.createMessage(mMethodGetInitializationStatus);
        Message mResponse;
        try
        {
            mResponse = srmSoap.makeSoapRequest(mRequest);
        }
        catch (SoapRequestException ignored)
        {
            return false;
        }
        
        return mResponse.getValue("//tuple/@objectID") != null;
    }    
    
    /**
     * Initializes Studio for the organization that is set in the SOAP manager object.
     *
     * @param srmSoap SOAP manager object.
     * @param ctx XML message context object.
     * @return <code>true</code> if Studio was initialized. <code>false</code> if the initialization failed or Studio was already initialized. 
     */
    public static boolean initializeStudio(ISoapRequestManager srmSoap, MessageContext ctx)
    {
        if (isStudioInitialized(srmSoap, ctx)) {
            return false;
        }
        
        Message mRequest = ctx.createMessage(mMethodInitialize);

        try
        {
            srmSoap.makeSoapRequest(mRequest);
        }
        catch (SoapRequestException ignored)
        {
            return false;
        }
        
        return true;
    }
    
    /**
     * Determines if Studio user preferences are set for the current user.
     *
     * @param srmSoap SOAP manager object.
     * @param ctx XML message context object.
     * @return <code>true</code> if the preferences were set.. 
     */
    public static boolean areUserPreferencesSet(ISoapRequestManager srmSoap, MessageContext ctx)
    {
        Message mRequest = ctx.createMessage(mMethodGetUserPreferences);
        Message mResponse;
        try
        {
            mResponse = srmSoap.makeSoapRequest(mRequest);
        }
        catch (SoapRequestException ignored)
        {
            return false;
        }
        
        return mResponse.getValue("//tuple/@objectID") != null;
    }    
    
    /**
     * Inserts default user preferences in Studio for the current user.
     *
     * @param srmSoap SOAP manager object.
     * @param ctx XML message context object.
     * @return <code>true</code> if preferences were inserted. <code>false</code> if the insertion failed or the preferences were already set. 
     */
    public static boolean insertDefaultUserPreferences(ISoapRequestManager srmSoap, MessageContext ctx)
    {
        if (areUserPreferencesSet(srmSoap, ctx)) {
            return false;
        }
        
        Message mRequest = ctx.createMessage(mMethodInsertDefaultUserPreferences);

        try
        {
            srmSoap.makeSoapRequest(mRequest);
        }
        catch (SoapRequestException ignored)
        {
            return false;
        }
        
        return true;
    }
}
