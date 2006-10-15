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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class BndFile
    implements ModuleComponent, JDOMExternalizable
{

    private Module m_module;
    private VirtualFile m_bndFile;
    private String m_name;

    public BndFile( Module module )
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
        return "org.ops4j.pax.runner.idea.bundles.BndFile";
    }

    /**
     * Component should do initialization and communication with another components in this method.
     */
    public void initComponent()
    {
    }

    /**
     * Component should dispose system resources or perform another cleanup in this method.
     */
    public void disposeComponent()
    {
    }

    public void readExternal( Element element )
    throws InvalidDataException
    {
        VirtualFile moduleDir = m_module.getModuleFile().getParent();
        Element bndFilename = element.getChild( "filename" );
        m_name = bndFilename.getText();
        m_bndFile = moduleDir.findChild( m_name );
        if( m_bndFile == null )
        {
            try
            {
                m_bndFile = moduleDir.createChildData( this, m_name );
            } catch( IOException e )
            {
                // TODO: ignore ??
            }
        }
        load();
    }

    public void writeExternal( Element element )
    throws WriteExternalException
    {
        Element filename = element.addContent( "filename");
        filename.setText( m_name );
        save();
    }

    private void save()
    {

    }

    private void load()
    {
    }
}
