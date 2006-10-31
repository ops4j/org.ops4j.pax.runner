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
package org.ops4j.osgidea.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.DefaultModuleConfigurationEditorFactory;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.ops4j.osgidea.bundles.editor.OsgiModuleExportEditor;
import org.ops4j.osgidea.bundles.editor.OsgiModuleImportEditor;

public class OsgiModuleEditorsProvider
    implements ModuleComponent, ModuleConfigurationEditorProvider
{

    private Module m_module;

    public OsgiModuleEditorsProvider( Module module )
    {
        m_module = module;
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
        return "org.ops4j.osgidea.runner.idea.OsgiModuleEditorsProvider";
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

    public ModuleConfigurationEditor[] createEditors( ModuleConfigurationState state )
    {
        Project project = state.getProject();
        ModifiableRootModel rootModel = state.getRootModel();
        Module module = rootModel.getModule();
        ModulesProvider provider = state.getModulesProvider();
        OsgiModuleExportEditor exportEditor = new OsgiModuleExportEditor( project, module, provider, rootModel );
        OsgiModuleImportEditor importEditor = new OsgiModuleImportEditor( project, module, provider, rootModel );
        OsgiModuleTypeEditor typeEditor = new OsgiModuleTypeEditor( project, module, provider, rootModel );
        DefaultModuleConfigurationEditorFactory editorFactory = DefaultModuleConfigurationEditorFactory.getInstance();
        return new ModuleConfigurationEditor[]
            {
                editorFactory.createModuleContentRootsEditor( state ),
                editorFactory.createOutputEditor( state ),
                editorFactory.createClasspathEditor( state ),
                exportEditor, importEditor, typeEditor
            };
    }
}
