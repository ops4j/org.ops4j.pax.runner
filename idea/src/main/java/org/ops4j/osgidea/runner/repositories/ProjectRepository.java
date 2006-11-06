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
package org.ops4j.osgidea.runner.repositories;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import java.net.URL;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import org.ops4j.osgidea.runner.config.ConfigBean;
import org.ops4j.pax.model.bundles.BundleRef;
import org.ops4j.pax.model.repositories.RepositoryInfo;

public class ProjectRepository extends DefaultMutableTreeNode
    implements MutableTreeNode
{

    public ProjectRepository( RepositoryInfo info, ConfigBean config )
    {
        super();
        setAllowsChildren( true );
        Project project = config.getProject();
        String projectName = "Project - " + project.getName();
        DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode( projectName, false );
        setUserObject( projectNode );
        Module[] modules = ModuleManager.getInstance( project ).getModules();
        for( Module module : modules )
        {
            String moduleName = module.getName();
            URL dest = null; // TODO
            BundleRef bundle = new BundleRef( moduleName, info, dest, null );
            DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode( bundle, false );
            add( moduleNode );
        }
    }
}
