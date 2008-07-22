/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.osgidea.bundles.editor;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.ops4j.osgidea.OsgiIcons;
import org.ops4j.osgidea.OsgiResourceBundle;
import org.ops4j.osgidea.bundles.forms.OsgiModuleImportForm;
import org.ops4j.osgidea.bundles.editor.DataBeanImport;

public class OsgiModuleImportEditor
    implements ModuleConfigurationEditor
{
    private static final Logger m_logger = Logger.getLogger( OsgiModuleImportEditor.class );

    private OsgiModuleImportForm m_importForm;
    private Project m_project;
    private Module m_module;
    private ModulesProvider m_provider;
    private ModifiableRootModel m_rootModel;
    private DataBeanImport m_importBean;

    public OsgiModuleImportEditor( Project project, Module module, ModulesProvider modulesProvider,
                                   ModifiableRootModel rootModel )
    {
        m_project = project;
        m_module = module;
        m_provider = modulesProvider;
        m_rootModel = rootModel;
        reset();
    }

    public void saveData()
    {
        m_logger.debug( "saveData()" );
    }

    public void moduleStateChanged()
    {
        m_logger.debug( "moduleStateChanged()" );
    }

    /**
     * Returns the user-visible name of the settings component.
     *
     * @return the visible name of the component.
     */
    public String getDisplayName()
    {
        return OsgiResourceBundle.message( "osgi.module.import.editor" );
    }

    /**
     * Returns the icon representing the settings component. Components
     * shown in the IDEA settings dialog have 32x32 icons.
     *
     * @return the icon for the component.
     */
    public Icon getIcon()
    {
        return OsgiIcons.ICON_NORMAL;
    }

    /**
     * Returns the topic in the help file which is shown when help for the configurable
     * is requested.
     *
     * @return the help topic, or null if no help is available.
     */
    @Nullable
    @NonNls
    public String getHelpTopic()
    {
        return null;
    }

    /**
     * Returns the user interface component for editing the configuration.
     *
     * @return the component instance.
     */
    public JComponent createComponent()
    {
        m_importForm = new OsgiModuleImportForm();
        return m_importForm.$$$getRootComponent$$$();
    }

    /**
     * Checks if the settings in the user interface component were modified by the user and
     * need to be saved.
     *
     * @return true if the settings were modified, false otherwise.
     */
    public boolean isModified()
    {
        if( m_importForm == null )
        {
            return true;
        }
        return m_importForm.isModified( m_importBean );
    }

    /**
     * Store the settings from configurable to other components.
     */
    public void apply()
        throws ConfigurationException
    {
        m_importBean = m_importBean.clone();
    }

    /**
     * Load settings from other components to configurable.
     */
    public void reset()
    {
        m_importBean = new DataBeanImport();
    }

    /**
     * Disposes the Swing components used for displaying the configuration.
     */
    public void disposeUIResources()
    {
        m_logger.debug( "disposeUIResources()" );
    }
}
