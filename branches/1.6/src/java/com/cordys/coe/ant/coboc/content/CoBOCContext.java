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
package com.cordys.coe.ant.coboc.content;

import java.util.HashMap;
import java.util.Map;

import com.cordys.coe.ant.coboc.CoBOCConstants;
import com.cordys.coe.ant.coboc.content.folders.Folder;
import com.cordys.coe.ant.coboc.methods.Create;
import com.cordys.coe.ant.coboc.methods.Delete;
import com.cordys.coe.ant.coboc.methods.GetCollection;
import com.cordys.coe.ant.coboc.methods.GetXMLObject;
import com.cordys.coe.ant.coboc.methods.Modify;
import com.cordys.coe.ant.coboc.methods.Select;
import com.cordys.coe.ant.coboc.methods.UpdateXMLObject;
import com.cordys.coe.util.log.LogInterface;
import com.cordys.coe.util.log.StdoutLogger;
import com.cordys.tools.ant.soap.ISoapRequestManager;
import com.eibus.xml.nom.Document;

/**
 * A class that contains all the information needed for an export or import
 * operation.
 *
 * @author mpoyhone
 */
public class CoBOCContext
{
    /**
     * The wrapper object for CoBOC Create method.
     */
    public final Create cmCreate = new Create();
    /**
     * The wrapper object for CoBOC Delete method.
     */
    public final Delete cmDelete = new Delete();
    /**
     * The wrapper object for CoBOC GetCollection method.
     */
    public final GetCollection cmGetCollection = new GetCollection();
    /**
     * The wrapper object for CoBOC GetXMLObject method.
     */
    public final GetXMLObject cmGetXMLObject = new GetXMLObject();
    /**
     * The wrapper object for CoBOC Modify method.
     */
    public final Modify cmModify = new Modify();
    /**
     * The wrapper object for CoBOC Select method.
     */
    public final Select cmSelect = new Select();
    /**
     * The wrapper object for CoBOC UpdateXMLObject method.
     */
    public final UpdateXMLObject cmUpdateXMLObject = new UpdateXMLObject();
    /**
     * The XML Document object used when creating XML nodes.
     */
    protected Document dDoc;
    /**
     * The CoBOC root folder object.
     */
    protected Folder fRootFolder;
    /**
     * A logger object that can be replace by the application. On default logs
     * to standard output.
     */
    protected LogInterface lLogger = new StdoutLogger();
    /**
     * A map from CoBOC entity ID's to CoBOC objects.
     */
    protected Map<String, CoBOCObject> mObjectIdMap = new HashMap<String, CoBOCObject>();
    /**
     * A map from CoBOC keys to CoBOC objects.
     */
    protected Map<String, CoBOCObject> mObjectPathMap = new HashMap<String, CoBOCObject>();
    /**
     * The SOAPWrapper object that is used to make connections to CoBOC.
     */
    protected ISoapRequestManager srmSoap;
    /**
     * The destination organization for CoBOC content. When transfering content
     * to CoBOC, the objects are put under this organization.
     */
    private String sDestOrganization = null;
    /**
     * The user DN used for for CoBOC content. When transfering content to
     * CoBOC, the objects are put under this user.
     */
    private String sDestUser = null;

    {
        // Create the root folder object.
        fRootFolder = new Folder(this);
        fRootFolder.sEntityId = CoBOCConstants.COBOC_ROOT_FOLDER_ID;
        fRootFolder.sPathKey = "/";
        fRootFolder.setIsRootFolder(true);
        updateObject(fRootFolder);
        mObjectIdMap.put("0", fRootFolder); // Sometimes the root is 0. Go figure..
    }

    /**
     * Creates a new CoBOCContext object.
     */
    public CoBOCContext()
    {
    	srmSoap = null;
        dDoc = new Document();
    }

    /**
     * Creates a new CoBOCContext object.
     *
     * @param swSoap The SOAP wrapper to be used when connecting to CoBOC.
     */
    public CoBOCContext(ISoapRequestManager srmSoap)
    {
        this.srmSoap = srmSoap;
        dDoc = srmSoap.getDocument();
    }

    /**
     * Sets the destination organization for CoBOC content. When transfering
     * content to CoBOC, the objects are put under this organization.
     *
     * @param sOrganization The organization to be used for CoBOC content.
     */
    public void setContentDestOrganization(String sOrganization)
    {
        sDestOrganization = sOrganization;
    }

    /**
     * Returns the destination organization for CoBOC content. When transfering
     * content to CoBOC, the objects are put under this organization.
     *
     * @return The destination organization for CoBOC content.
     */
    public String getContentDestOrganization()
    {
        return sDestOrganization;
    }

    /**
     * Sets the destination user DN for CoBOC content. When transfering content
     * to CoBOC, the objects are put under this user.
     *
     * @param sUserDN The user DN to be used for CoBOC content.
     */
    public void setContentDestUser(String sUserDN)
    {
        sDestUser = sUserDN;
    }

    /**
     * Returns the destination user DN for CoBOC content. When transfering
     * content to CoBOC, the objects are put under this user.
     *
     * @return The destination user DN for CoBOC content.
     */
    public String getContentDestUser()
    {
        return sDestUser;
    }

    /**
     * Returns the document object.
     *
     * @return The document object.
     */
    public Document getDocument()
    {
        return dDoc;
    }

    /**
     * Sets the logger object for log messages.
     *
     * @param lLogger The new logger object.
     */
    public void setLogger(LogInterface lLogger)
    {
        this.lLogger = lLogger;
    }

    /**
     * Returns the logger object.
     *
     * @return The logger object.
     */
    public LogInterface getLogger()
    {
        return lLogger;
    }

    /**
     * Tries to find a CoBOC object by entity ID.
     *
     * @param sId Object's entity ID.
     *
     * @return The CoBOC object or null, if the object could not be found.
     */
    public CoBOCObject getObjectById(String sId)
    {
        return mObjectIdMap.get(sId);
    }

    /**
     * Tries to find a CoBOC object by entity key.
     *
     * @param sPathKey Object's entity key.
     *
     * @return The CoBOC object or null, if the object could not be found.
     */
    public CoBOCObject getObjectByPathKey(String sPathKey)
    {
        return mObjectPathMap.get(sPathKey);
    }

    /**
     * Sets the root folder object.
     *
     * @param fFolder The new root folder object.
     */
    public void setRootFolder(Folder fFolder)
    {
        fRootFolder = fFolder;
    }

    /**
     * Returns the root folder object.
     *
     * @return The root folder object or null, if none is set.
     */
    public Folder getRootFolder()
    {
        return fRootFolder;
    }

    /**
     * Returns the timeout value for SOAP requests.
     *
     * @return The timeout value for SOAP requests.
     */
    public long getSoapTimeout()
    {
        return 30000L;
    }

    /**
     * Returns the SOAP wrapper that is used to make SOAP requests to CoBOC.
     *
     * @return The SOAP wrapper object.
     */
    public ISoapRequestManager getSoapManager()
    {
        return srmSoap;
    }

    /**
     * Removes the object from context.
     *
     * @param coObject The object to be removed.
     */
    public void removeObject(CoBOCObject coObject)
    {
        if (coObject.getEntityID() != null)
        {
            mObjectIdMap.remove(coObject.getEntityID());
        }

        if (coObject.getEntityPathKey() != null)
        {
            mObjectPathMap.remove(coObject.getEntityPathKey());
        }
    }

    /**
     * Updates object's state in the context, i.e. maps the object  according
     * to its entity key and ID values.
     *
     * @param coObject The object to be updated.
     */
    public void updateObject(CoBOCObject coObject)
    {
        removeObject(coObject);

        if (coObject.getEntityID() != null)
        {
            mObjectIdMap.put(coObject.getEntityID(), coObject);
        }

        if (coObject.getEntityPathKey() != null)
        {
            mObjectPathMap.put(coObject.getEntityPathKey(), coObject);
        }
    }
}
