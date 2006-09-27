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
package org.ops4j.pax.runner.idea.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.ops4j.pax.runner.idea.OsgiIcons;
import org.ops4j.pax.runner.idea.OsgiResourceBundle;
import org.ops4j.pax.runner.idea.UserKeys;
import org.ops4j.pax.runner.idea.packages.PackageInfo;

public class OsgiModuleExportEditor
    implements ModuleConfigurationEditor
{

    private static final Logger m_logger = Logger.getLogger( OsgiModuleExportEditor.class );

    private Project m_project;
    private Module m_module;
    private ModulesProvider m_provider;
    private ModifiableRootModel m_rootModel;
    private OsgiModuleExportForm m_form;
    private DataBeanExport m_exportBean;

    public OsgiModuleExportEditor( Project project, Module module, ModulesProvider provider,
                                   ModifiableRootModel rootModel )
    {
        m_project = project;
        m_module = module;
        m_provider = provider;
        m_rootModel = rootModel;
    }

    private Set<PackageInfo> preparePackages( Module module, ModifiableRootModel rootModel )
    {
        Set<PackageInfo> packages = getPackages( module );

        Module[] depModules = rootModel.getModuleDependencies();
        for( Module depModule : depModules )
        {
            Set<PackageInfo> depPackages = getPackages( depModule );
            packages.addAll( depPackages );
        }
        Library[] libraries = rootModel.getModuleLibraryTable().getLibraries();
        for( Library library : libraries )
        {
            Set<PackageInfo> depPackages = getPackages( library );
            packages.addAll( depPackages );
        }
        return packages;
    }

    private Set<PackageInfo> getPackages( Library library )
    {
        return new TreeSet<PackageInfo>();
    }

    private Set<PackageInfo> getPackages( Module module )
    {
        Set<PackageInfo> packages = new TreeSet<PackageInfo>();
        GlobalSearchScope scope = module.getModuleRuntimeScope( false );
        PsiManager psiMan = PsiManager.getInstance( m_project );
        PsiPackage psiPackage = psiMan.findPackage( "" );
        establishSubPackages( psiPackage, scope, packages, null );
        return packages;
    }

    private void establishSubPackages( PsiPackage psiPackage, GlobalSearchScope scope, Set<PackageInfo> result,
                                       PackageInfo parent )
    {
        for( PsiPackage child : psiPackage.getSubPackages( scope ) )
        {
            String name = child.getQualifiedName();
            PackageInfo packageInfo = child.getUserData( UserKeys.KEY_EXPORTED );
            if( packageInfo == null )
            {
                packageInfo = new PackageInfo( name, parent );
            }
            result.add( packageInfo );
            establishSubPackages( child, scope, result, packageInfo );
        }
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
        return OsgiResourceBundle.message( "osgi.module.export.editor" );
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
        m_form = new OsgiModuleExportForm();
        reset();
        return m_form.$$$getRootComponent$$$();
    }

    /**
     * Checks if the settings in the user interface component were modified by the user and
     * need to be saved.
     *
     * @return true if the settings were modified, false otherwise.
     */
    public boolean isModified()
    {
        return m_form.isModified( m_exportBean );
    }

    /**
     * Store the settings from configurable to other components.
     */
    public void apply()
        throws ConfigurationException
    {
        m_form.getData( m_exportBean );
//        for( PackageInfo info : m_exportBean.getPackages() )
//        {
//            info.makeReadOnly();
//            PsiPackage psi = info.getSource();
//            psi.putUserData( UserKeys.KEY_EXPORTED, info );
//        }
        reset();
    }

    /**
     * Load settings from other components to configurable.
     */
    public void reset()
    {
        Set<PackageInfo> packages = preparePackages( m_module, m_rootModel );
        m_exportBean = new DataBeanExport( packages );
        m_form.setData( m_exportBean );
    }

    /**
     * Disposes the Swing components used for displaying the configuration.
     */
    public void disposeUIResources()
    {
        m_logger.debug( "disposeUIResources()" );
    }
}
