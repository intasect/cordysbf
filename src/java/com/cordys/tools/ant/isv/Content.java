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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;

import com.cordys.coe.ant.coboc.task.CoBOCISVHandler;
import com.cordys.coe.ant.studio.task.StudioISVHandler;
import com.cordys.tools.ant.cm.IContent;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.XMLException;

/**
 * This class represents the nested tag of the ISV task. This class extends
 * from the Class<code>Task</code> in ant.
 *
 * @author msreejit
 */
public class Content extends Task
    implements IContent
{
    /**
     * type dbschema
     */
    public static final String TYPE_DBSCHEMA = "dbschema";
    /**
     * type filesystem
     */
    public static final String TYPE_FILESYSTEM = "filesystem";
    /**
     * type systemenvironment
     */
    public static final String TYPE_SYSTEMENVIRONMENT = "systemenvironment";
    /**
     * type virtualdirectories
     */
    public static final String TYPE_VIRTUALDIRECTORIES = "virtualdirectories";
    /**
     * type prompts
     */
    public static final String TYPE_PROMPTS = "prompts";
    /**
     * type static
     */
    public static final String TYPE_STATIC = "static";
    /**
     * type xforms
     */
    public static final String TYPE_XFORMS = "xforms";
    /**
     * type studio
     */
    public static final String TYPE_STUDIO = "studio";
    /**
     * type coboc (old)
     */
    public static final String TYPE_COBOC = "coboc";
    /**
     * type coboc (new)
     */
    public static final String TYPE_COBOC2 = "coboc2";
    /**
     * type coboc (new)
     */
    public static final String TYPE_LOCALIZATIONS = "localizations";
    /**
     * type dependencies
     */
    public static final String TYPE_DEPENDENCIES = "dependencies";
    /**
     * The content file which specifies the ISV configuration.
     */
    private File contentFile;
    /**
     * The handler of the custom isv content type.
     */
    private String handler;
    /**
     * The type attribute
     */
    private String type;
    /**
     * The file sets that are configured for this handler. Probably usefull only for
     * FileSystem handler.
     */
    private List<FileSet> lFileSets = new ArrayList<FileSet>(2);
    /**
     * The pattern sets that are configured for this handler. Probably usefull only for
     * FileSystem handler.
     */
    private List<PatternSet> lPatternSets = new ArrayList<PatternSet>(2);
    /**
     * <code>true</code> if the content file is optional.
     */
    private boolean optional;
    
    /**
     * Sets the content which specifies the ISV configuration.
     *
     * @param file The content file which specifies the ISV configuration.
     */
    public void setContentFile(File file)
    {
        contentFile = file;
    }

    /**
     * Returns the content file in which the ISV configuration is done.
     *
     * @return The file where the contents are placed.
     */
    public File getContentFile()
    {
        return contentFile;
    }

    /**
     * Sets the handler for the content.
     *
     * @param handler The handler for the content.
     */
    public void setHandler(String handler)
    {
        this.handler = handler;
    }

    /**
     * Returns the handler for the content.
     *
     * @return The handler for the content.
     */
    public String getHandler()
    {
        return handler;
    }

    /**
     * The ISV content XML passed on by each handler which should be appended
     * to the main ISV xml which will be sent in the request for creating the
     * ISV.
     *
     * @param isvTask The instance of the ISVCreatorTask class for passing on
     *        the content handlers.
     * @param iCurrentIsvContentNode Contains already generated ISV content.
     * @param iCurrentIsvPromptsetNode Contains already generated ISV prompts
     *        content.
     *
     * @return The ISV Content XML handled by this ISV content type which
     *         should be appended to the ISV xml.
     *
     * @throws XMLException If the the content xml specified in the contentfile
     *         attribute fails to load.
     */
    public int[] getISVContentXML(ISVCreatorTask isvTask,
                                  int iCurrentIsvContentNode,
                                  int iCurrentIsvPromptsetNode)
                           throws XMLException
    {
        Document doc = isvTask.getDocument();
        int contentRootNode = doc.load(contentFile.getAbsolutePath());
        isvTask.registerNodeForCleanup(contentRootNode);

        int[] iaRes;
        ISVContentHandler ichHandler = getHandlerObject(isvTask);

        iaRes = ichHandler.getISVContentXML(isvTask, this, contentRootNode,
                                            iCurrentIsvContentNode,
                                            iCurrentIsvPromptsetNode);
        return iaRes;
    }

    /**
     * Sets the type of content.
     *
     * @param type The type of the content.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Returns the type of content.
     *
     * @return The type of the content.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Checks the handler and sets it.
     *
     * @throws BuildException If handler is not specified in the case of custom
     *         content types.
     */
    public void checkAndSetHandler()
                            throws BuildException
    {
        if (type.toString().equals(TYPE_FILESYSTEM))
        {
            handler = FileSystem.class.getName();
        }
        else if (type.toString().equals(TYPE_MENUS))
        {
            handler = Menus.class.getName();
        }
        else if (type.toString().equals(TYPE_TOOLBARS))
        {
            handler = Toolbars.class.getName();
        }
        else if (type.toString().equals(TYPE_METHODSETS))
        {
            handler = Methodsets.class.getName();
        }
        else if (type.toString().equals(TYPE_PROMPTS))
        {
            handler = Prompts.class.getName();
        }
        else if (type.toString().equals(TYPE_ROLES))
        {
            handler = OrganizationalRoles.class.getName();
        }
        else if (type.toString().equals(TYPE_SYSTEMENVIRONMENT))
        {
            handler = SystemEnvironment.class.getName();
        }
        else if (type.toString().equals(TYPE_XMLSTORE))
        {
            handler = XMLStore.class.getName();
        }
        else if (type.toString().equals(TYPE_APPLICATIONCONNECTOR))
        {
            handler = ApplicationConnector.class.getName();
        }
        else if (type.toString().equals(TYPE_STYLES))
        {
            handler = Styles.class.getName();
        }
        else if (type.toString().equals(TYPE_DBSCHEMA))
        {
            handler = DBSchema.class.getName();
        }
        else if (type.toString().equals(TYPE_VIRTUALDIRECTORIES))
        {
            handler = VirtualDirectories.class.getName();
        }
        else if (type.toString().equals(TYPE_COBOC))
        {
            handler = CoBOCISVHandler.class.getName();
        }
        else if (type.toString().equals(TYPE_COBOC2))
        {
            handler = "com.cordys.coe.bf.ant.CobocAntIsvContentHandler";
        }
        else if (type.toString().equals(TYPE_STUDIO))
        {
            handler = StudioISVHandler.class.getName();
        }
        else if (type.toString().equals(TYPE_XFORMS))
        {
            handler = XForms.class.getName();
        }
        else if (type.toString().equals(TYPE_WSAPPSERVER))
        {
            handler = WsAppServer_c2.class.getName();
        }
        else if (type.toString().equals(TYPE_STATIC))
        {
            handler = StaticContent.class.getName();
        }
        else if (type.toString().equals(TYPE_LOCALIZATIONS))
        {
            handler = Localizations.class.getName();
        }
        else if (type.toString().equals(TYPE_DEPENDENCIES))
        {
            handler = Dependencies.class.getName();
        }
        else if (type.toString().equals(TYPE_XREPORTS))
        {
            handler = XReports.class.getName();
        }
        else if ((handler == null) || "".equals(handler))
        {
            throw new BuildException("\nType: " + type +
                                     "\nDetails: Custom content needs the handler attribute to be specified.");
        }
        
    }

    /**
     * Gets the handler's instance.The handler is mentioned in the handler
     * attribute.
     * 
     * @param isvTask The ISV task.
     *
     * @return Instance of the handler.
     *
     * @throws BuildException If handler does not implement the
     *         <code>ISVContentHandler</code> interface
     */
    private ISVContentHandler getHandlerObject(ISVCreatorTask isvTask)
                                        throws BuildException
    {
        Object obj;

        try
        {
            isvTask.log("Handler task: " + handler, Project.MSG_VERBOSE);
            Class<?> clz = Class.forName(handler);
            obj = clz.newInstance();
        }
        catch (ClassNotFoundException e)
        {
            throw new BuildException(e);
        }
        catch (InstantiationException e)
        {
            throw new BuildException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new BuildException(e);
        }

        if (!(obj instanceof ISVContentHandler))
        {
            throw new BuildException("\nType: " + type +
                                     "\nDetails: Handler does not implement ISVContentHandler interface");
        }

        return (ISVContentHandler) obj;
    }
    
    /**
     * Adds a set of files to be scanned for content.
     *
     * @param set The fileset used by the content.
     */
    public void addFileset(FileSet set)
    {
        lFileSets.add(set);
    }
    
    /**
     * Returns a configured file set. This returns the first one because the file set is
     * actually an exclusion file set and there should be only one specified.
     * @return Configured file set or <code>null</code> if none is set.
     */
    public FileSet getFileset() {
        if (lFileSets.isEmpty()) {
            return null;
        }
        
        return lFileSets.get(0);
    }
    
    /**
     * Adds a file patterns to be included/excluded for content. Used only by file system handler.
     *
     * @param set The pattern set used by the content.
     */
    public void addPatternset(PatternSet set)
    {
        lPatternSets.add(set);
    }
    
    /**
     * Returns a configured pattern set. This returns there should be only one specified.
     * @return Configured pattern set or <code>null</code> if none is set.
     */
    public PatternSet getPatternSet() {
        if (lPatternSets.isEmpty()) {
            return null;
        }
        
        return lPatternSets.get(0);
    }

    /**
     * Returns the optional.
     *
     * @return Returns the optional.
     */
    public boolean isOptional()
    {
        return optional;
    }

    /**
     * Sets the optional.
     *
     * @param optional The optional to be set.
     */
    public void setOptional(boolean optional)
    {
        this.optional = optional;
    }
}
