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
package org.ops4j.pax.runner.idea.bundles;

import aQute.lib.osgi.Builder;
import aQute.lib.osgi.Jar;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.util.Properties;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class BundleJar
    implements ModuleComponent
{

    private Module m_module;

    public BundleJar( Module module )
    {
        m_module = module;
    }

    /**
     * Invoked when the project corresponding to this component instance is opened.<p>
     * Note that components may be created for even unopened projects and this method can be never
     * invoked for a particular component instance (for example for default project).
     */
    public void projectOpened()
    {}

    /**
     * Invoked when the project corresponding to this component instance is closed.<p>
     * Note that components may be created for even unopened projects and this method can be never
     * invoked for a particular component instance (for example for default project).
     */
    public void projectClosed()
    {}

    /**
     * Invoked when the module corresponding to this component instance has been completely
     * loaded and added to the project.
     */
    public void moduleAdded()
    {}

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
        return "org.ops4j.pax.runner.idea.bundles.BundleJar";
    }

    /**
     * Component should do initialization and communication with another components in this method.
     */
    public void initComponent()
    {}

    /**
     * Component should dispose system resources or perform another cleanup in this method.
     */
    public void disposeComponent()
    {}

    private Jar createJar( Properties props, VirtualFile jarDest, VirtualFile[] sources )
        throws Exception
    {
        if( !props.containsKey( "Export-Package" ) )
        {
            props.put( "Export-Package", "*" );
        }
        File target = new File( jarDest.getPath() );
        Builder builder = new Builder();
        builder.setJar( target );
        builder.setProperties( props );
        File[] sourceFiles = new File[ sources.length ];
        int i = 0;
        for( VirtualFile source : sources )
        {
            String sourcepath = source.getPath();
            sourceFiles[ i ] = new File( sourcepath );
        }
        builder.setSourcepath( sourceFiles );
        return builder.build();
    }
}
