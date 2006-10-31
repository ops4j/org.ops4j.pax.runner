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
package org.ops4j.osgidea.module.packages;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.refactoring.listeners.RefactoringListenerManager;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class PackageManagerImpl
    implements PackageManager, ModuleComponent, JDOMExternalizable
{

    private static final Logger m_logger = Logger.getLogger( PackageManagerImpl.class );

    private HashMap<String, PackageInfo> m_packages;
    private PackageTracker m_tracker;
    private Project m_project;
    private Module m_module;

    public PackageManagerImpl( Project project, Module module )
    {
        m_project = project;
        m_module = module;
        m_packages = new HashMap<String, PackageInfo>();
    }

    public PackageInfo findPackageByFile( VirtualFile fileOrDirectory )
    {
        m_logger.debug( "getPackage( " + fileOrDirectory + " )" );
        if( ! fileOrDirectory.isDirectory() )
        {
            fileOrDirectory = fileOrDirectory.getParent();
        }
        ModuleRootManager rootMan = ModuleRootManager.getInstance( m_module );
        VirtualFile[] sourceRoots = rootMan.getSourceRoots();
        for( VirtualFile file : sourceRoots )
        {
            String sourceFolder = file.getUrl();
            String fullName = fileOrDirectory.getUrl();
            if( fullName.startsWith( sourceFolder ) )
            {
                int pos = sourceFolder.length();
                if( fullName.charAt( pos ) != '/' )
                {
                    break;
                }
                String packagename = fullName.substring( pos + 1 ).replace( '/', '.' );
                return findPackageInfoByName( packagename );
            }
        }
        return null;
    }

    public PackageInfo findPackageInfoByName( String packagename )
    {
        if( packagename == null || "".equals( packagename ) )
        {
            return null;
        }
        PackageInfo packageInfo = m_packages.get( packagename );
        if( packageInfo == null )
        {
            PackageInfo parent = findPackageInfoByName( chopLeaf( packagename ) );
            packageInfo = new PackageInfo( packagename, parent );
        }
        return packageInfo;
    }

    private String chopLeaf( String packagename )
    {
        int pos = packagename.lastIndexOf( '.' );
        if( pos <= 0 )
        {
            return "";
        }
        return packagename.substring( 0, pos );
    }

    public void addPackageInfo( PackageInfo packageInfo )
    {
        m_packages.put( packageInfo.getPackageName(), packageInfo );
    }

    public void removePackageInfo( PackageInfo packageInfo )
    {
        m_packages.remove( packageInfo.getPackageName() );
    }

    /**
     * Unique name of this component. If there is another component with the same name or
     * name is null internal assertion will occur.
     *
     * @return the name of this component
     */
    @NonNls
    @NotNull
    public String getComponentName()
    {
        return "org.ops4j.osgidea.module.packages.PackageManagerImpl";
    }

    /**
     * Component should do initialization and communication with another components in this method.
     */
    public void initComponent()
    {
        m_tracker = new PackageTracker( this );
        RefactoringListenerManager.getInstance( m_project ).addListenerProvider( m_tracker );
        PsiManager.getInstance( m_project ).addPsiTreeChangeListener( m_tracker );
    }

    /**
     * Component should dispose system resources or perform another cleanup in this method.
     */
    public void disposeComponent()
    {
        RefactoringListenerManager.getInstance( m_project ).removeListenerProvider( m_tracker );
        PsiManager.getInstance( m_project ).removePsiTreeChangeListener( m_tracker );
    }

    /**
     * Invoked when the project corresponding to this component instance is opened.<p>
     * Note that components may be created for even unopened projects and this method can be never
     * invoked for a particular component instance (for example for default project).
     */
    public void projectOpened()
    {
    }

    /**
     * Invoked when the project corresponding to this component instance is closed.<p>
     * Note that components may be created for even unopened projects and this method can be never
     * invoked for a particular component instance (for example for default project).
     */
    public void projectClosed()
    {
    }

    /**
     * Invoked when the module corresponding to this component instance has been completely
     * loaded and added to the project.
     */
    public void moduleAdded()
    {
        //TODO: Auto-generated, need attention.

    }

    public void readExternal( Element element )
        throws InvalidDataException
    {
        m_logger.debug( "readExternal()" );
    }

    public void writeExternal( Element element )
        throws WriteExternalException
    {
        m_logger.debug( "readExternal()" );
    }
}
