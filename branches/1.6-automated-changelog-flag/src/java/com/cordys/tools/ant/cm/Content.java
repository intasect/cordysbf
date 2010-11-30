/**
 * Copyright 2004 Cordys R&D B.V. 
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

import com.cordys.coe.ant.coboc.task.CoBOCTaskHandler;
import com.cordys.coe.ant.studio.task.StudioTaskHandler;

import java.io.File;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;

/**
 * This class represents the nested tag of the contentmanager task. This class extends from the
 * Class<code>Matching Task</code>
 *
 * @author  msreejit
 */
public class Content extends MatchingTask
    implements IContent
{
    /**
     * Contains the server version info.
     */
    private BcpVersionInfo bcpVersion;
    /**
     * The contentfile where the content should be placed. This is a contentfile in the case of flat
     * contentfile for content.
     */
    private File contentfile;
    /**
     * The dir where the content should be placed. This is a directory in the case of multiple
     * content files.
     */
    private File dir;
    /**
     * The filesets where are configured in the nested tag content.
     */
    private Vector<FileSet> filesets = new Vector<FileSet>();
    /**
     * The flag which sets whether the output contents should be formatted(default value true).
     */
    private boolean formatOutput = true;
    /**
     * Contains all configured filters.
     */
    private FilterSet fsFilters = new FilterSet();
    /**
     * The handler of the custom content type.
     */
    private String handler;
    /**
     * Contains content content names when only that content is supposed to be handled. The
     * isPathAccepted method will return false for all other kinds of content.
     */
    private Set<String> sSingleContentNames;
    /**
     * The type attribute.
     */
    private String type;

    /**
     * Adds a content filter to this content handler.
     *
     * @param  filter  Filter element to be added
     */
    public void addConfiguredFilter(Filter filter)
    {
        fsFilters.addFilter(filter);
    }

    /**
     * Adds a content pattern set to this content handler.
     *
     * @param  psPatternSet  PatternSet element to be added
     */
    public void addConfiguredPatternset(PatternSet psPatternSet)
    {
        String[] saIncludePatterns = psPatternSet.getIncludePatterns(getProject());
        String[] saExcludePatterns = psPatternSet.getExcludePatterns(getProject());

        if (saIncludePatterns != null)
        {
            for (int i = 0; i < saIncludePatterns.length; i++)
            {
                String sPattern = saIncludePatterns[i];

                fsFilters.addFilter(new Filter(sPattern, true));
            }
        }

        if (saExcludePatterns != null)
        {
            for (int i = 0; i < saExcludePatterns.length; i++)
            {
                String sPattern = saExcludePatterns[i];

                fsFilters.addFilter(new Filter(sPattern, false));
            }
        }
    }

    /**
     * Adds a set of files to be scanned for content.
     *
     * @param  set  The fileset used by the content.
     */
    public void addFileset(FileSet set)
    {
        filesets.addElement(set);
    }

    /**
     * Checks the handler and sets it.
     */
    public void checkAndSetHandler()
    {
        if (type.equals(TYPE_MENUS))
        {
            handler = MenusHandler.class.getName();
        }
        else if (type.equals(TYPE_TOOLBARS))
        {
            handler = ToolbarsHandler.class.getName();
        }
        else if (type.equals(TYPE_METHODSETS))
        {
            handler = MethodsetsHandler.class.getName();
        }
        else if (type.equals(TYPE_ROLES))
        {
            handler = RolesHandler.class.getName();
        }
        else if (type.equals(TYPE_XMLSTORE))
        {
            handler = XMLStoreHandler.class.getName();
        }
        else if (type.equals(TYPE_SOAPNODES))
        {
            handler = SoapNodesHandler.class.getName();
        }
        else if (type.equals(TYPE_APPLICATIONCONNECTOR))
        {
            handler = ApplicationConnectorHandler.class.getName();
        }
        else if (type.equals(TYPE_STYLES))
        {
            handler = StylesHandler.class.getName();
        }
        else if (type.equals(TYPE_STUDIO_XFORMS))
        {
            handler = StudioTaskHandler.class.getName();
        }
        else if (type.equals(TYPE_XFORMS))
        {
            handler = XFormsHandler.class.getName();
        }
        else if (type.equals(TYPE_STUDIO_BPMS))
        {
            handler = StudioTaskHandler.class.getName();
        }
        else if (type.equals(TYPE_COBOC_FOLDERS))
        {
            handler = CoBOCTaskHandler.class.getName();
        }
        else if (type.equals(TYPE_XAS))
        {
            handler = XASTaskHandler.class.getName();
        }
        else if (type.equals(TYPE_WSAPPSERVER))
        {
            handler = WsAppServerHandler_c2.class.getName();
        }
        else if (type.equals(TYPE_COBOC2))
        {
            handler = "com.cordys.coe.bf.ant.CoBOCAntContentHandler";
        }
        else if (type.equals(TYPE_LOCALIZATIONS))
        {
            handler = Localizations.class.getName();
        }
        else if (type.equals(TYPE_USERS))
        {
            handler = UsersHandler.class.getName();
        }
        else if (type.equals(TYPE_XREPORTS))
        {
            handler = XReportsHandler.class.getName();
        }
        else if ((handler == null) || "".equals(handler))
        {
            throw new BuildException("\nType: " + type +
                                     "\nDetails: Custom content needs the handler attribute to be specified.");
        }
    }

    /**
     * Returns the bcpVersion.
     *
     * @return  Returns the bcpVersion.
     */
    public BcpVersionInfo getBcpVersion()
    {
        return bcpVersion;
    }

    /**
     * Returns the flat contentfile in which the contents are placed. Either contentfile or dir
     * attribute should be specified. Will return null if not set.
     *
     * @return  The contentfile where the contents are placed.
     */
    public File getContentFile()
    {
        return contentfile;
    }

    /**
     * Returns the directory where the contents are managed. Either contentfile or dir attribute
     * should be specified. Will return null if not set.
     *
     * @return  The directory where the contents are managed
     */
    public File getDir()
    {
        return dir;
    }

    /**
     * The fileset which is used in the case of writing contents from files to ECX. Will return null
     * if not set.
     *
     * @return  The fileset used for specifying or filtering the files.
     */
    public Vector<FileSet> getFileSet()
    {
        return filesets;
    }

    /**
     * Returns configured filters.
     *
     * @return  A list of configured filters.
     */
    public FilterSet getFilterSet()
    {
        return fsFilters;
    }

    /**
     * Returns the handler for the content.
     *
     * @return  The handler for the content.
     */
    public String getHandler()
    {
        return handler;
    }

    /**
     * Gets the handler's instance.The handler is mentioned in the handler attribute.
     *
     * @return  Instance of the handler.
     *
     * @throws  BuildException  If handler does not implement the <code>ContentHandler</code>
     *                          interface
     */
    public ContentHandler getHandlerObject()
                                    throws BuildException
    {
        Object obj;

        try
        {
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
        catch (IllegalArgumentException e)
        {
            throw new BuildException(e);
        }
        catch (SecurityException e)
        {
            throw new BuildException(e);
        }

        if (!(obj instanceof ContentHandler))
        {
            throw new BuildException("\nType: " + type +
                                     "\nDetails: Handler does not implement ContentHandler interface");
        }

        return (ContentHandler) obj;
    }

    /**
     * The method provides the implicit fileset provided by the MatchingTask from ANT.
     *
     * @return  The implicit fileset provided by the MatchingTask from ANT
     */
    public FileSet getImplicitFileSetUsed()
    {
        return getImplicitFileSet();
    }

    /**
     * Returns the type of content.
     *
     * @return  The type of the content.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns the flag which sets whether the output contents should be formatted.
     *
     * @return  Returns the output format flag.
     */
    public boolean isFormatOutput()
    {
        return formatOutput;
    }

    /**
     * Returns true if the given path is accepted by configured content filters. This method also
     * check the single content name if that is set.
     *
     * @param   sPath  The path to be tested.
     *
     * @return  True if the given path is accepted by configured content filters.
     */
    public boolean isPathAccepted(String sPath)
    {
        return isPathAccepted(sPath, null);
    }

    /**
     * Returns true if the given path is accepted by configured content filters. This method also
     * check the single content name if that is set.
     *
     * @param   sPath                   The path to be tested.
     * @param   pPathExtractionPattern  Optional pattern to extract the actual path.
     *
     * @return  True if the given path is accepted by configured content filters.
     */
    public boolean isPathAccepted(String sPath, Pattern pPathExtractionPattern)
    {
        String sModifiedPath = sPath;

        // We might have to modify the path for the single content thing because otherwise we would
        // need to pass the exact CoBOC/Studio key in the command line.
        if (pPathExtractionPattern != null)
        {
            Matcher mMatcher = pPathExtractionPattern.matcher(sPath);

            if (mMatcher.matches())
            {
                // Concatenate all groups to form the new key.
                StringBuffer sbTmp = new StringBuffer(128);

                for (int i = 0; i < mMatcher.groupCount(); i++)
                {
                    String sValue = mMatcher.group(i + 1);

                    sbTmp.append((sValue != null) ? sValue : "");
                }

                String sNewPath = sbTmp.toString();

                if (sNewPath.length() > 0)
                {
                    sModifiedPath = sNewPath;
                }
            }
        }
        
        if (sSingleContentNames != null)
        {
            String sTmp = sModifiedPath;

            // Remove the leading slash if needed.
            if (sTmp.startsWith("/"))
            {
                sTmp = sTmp.substring(1);
            }

            if (!sSingleContentNames.contains(sTmp))
            {
                return false;
            }
        }

        return fsFilters.isPathAccepted(sPath);
    }

    /**
     * Returns <code>true</code> if the single content names are set.
     *
     * @return  <code>true</code> if the single content names are set.
     */
    public boolean isSingleContentSet()
    {
        return sSingleContentNames != null;
    }

    /**
     * Sets the bcpVersion.
     *
     * @param  bcpVersion  The bcpVersion to be set.
     */
    public void setBcpVersion(BcpVersionInfo bcpVersion)
    {
        this.bcpVersion = bcpVersion;
    }

    /**
     * Sets the flat contentfile in which the contents can be placed.
     *
     * @param  file  The contentfile where the contents can be placed.
     */
    public void setContentFile(File file)
    {
        this.contentfile = file;
    }

    /**
     * Sets the directory where contents are to be managed.
     *
     * @param  dir  The directory where contents are managed.
     */
    public void setDir(File dir)
    {
        this.dir = dir;
    }

    /**
     * Sets the flag which sets whether the output contents should be formatted.
     *
     * @param  formatOutput  The output format flag to set.
     */
    public void setFormatOutput(boolean formatOutput)
    {
        this.formatOutput = formatOutput;
    }

    /**
     * Sets the handler for the content.
     *
     * @param  handler  The handler for the content.
     */
    public void setHandler(String handler)
    {
        this.handler = handler;
    }

    /**
     * The single content name to set.
     *
     * @param  aSingleContentName  The single content name to set.
     */
    public void setSingleContentName(String aSingleContentName)
    {
        if (aSingleContentName == null)
        {
            sSingleContentNames = null;
            return;
        }

        String[] saNames = aSingleContentName.split(";");

        // Remove the leading slash if necessary.
        for (int i = 0; i < saNames.length; i++)
        {
            String sName = saNames[i];

            if (sName.startsWith("/"))
            {
                saNames[i] = sName.substring(1);
            }
        }

        sSingleContentNames = new HashSet<String>(Arrays.asList(saNames));
    }

    /**
     * Sets the type of content.
     *
     * @param  type  The type of the content.
     */
    public void setType(String type)
    {
        this.type = type;
    }
}
