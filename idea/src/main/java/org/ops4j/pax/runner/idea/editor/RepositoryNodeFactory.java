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
package org.ops4j.pax.runner.idea.editor;

import com.intellij.openapi.components.ApplicationComponent;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;
import org.jetbrains.annotations.NonNls;
import org.ops4j.pax.runner.idea.config.ConfigBean;
import org.ops4j.pax.runner.idea.repositories.ObrRepository;
import org.ops4j.pax.runner.idea.repositories.ProjectRepository;
import org.ops4j.pax.runner.repositories.RepositoryInfo;
import org.ops4j.pax.runner.repositories.RepositoryType;
import org.xml.sax.SAXException;

public class RepositoryNodeFactory
    implements ApplicationComponent
{

    private HashMap<RepositoryInfo, DefaultMutableTreeNode> m_nodes;

    public RepositoryNodeFactory()
    {
        m_nodes = new HashMap<RepositoryInfo, DefaultMutableTreeNode>();
    }

    /**
     * Unique name of this component. If there is another component with the same name or
     * name is null internal assertion will occur.
     *
     * @return the name of this component
     */
    @NonNls
    public String getComponentName()
    {
        return "org.ops4j.pax.runner.idea.editor.RepositoryNodeFactory";
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

    public DefaultMutableTreeNode createNode( RepositoryInfo repository, ConfigBean config )
    {
        RepositoryType type = null;
        try
        {
            type = repository.getType();
            if( RepositoryType.obr == type )
            {
                return createOscarBundleRepositoryNode( repository );
            }
//            if( RepositoryType.EclipseUpdateCenter == type )
//            {
//                return createEclipseUpdateCenterNode( repository );
//            }
//            if( RepositoryType.OSGiBundleRepository == type )
//            {
//                return createOsgiBundleRepositoryNode( repository );
//            }
            if( RepositoryType.project == type )
            {
                return createProjectRepositoryNode( repository, config );
            }
            throw new UnknownRepositoryTypeException( type );
        } catch( IOException e )
        {
            e.printStackTrace();
            throw new RuntimeException( "Unable to access Repository", e );
        } catch( ParserConfigurationException e )
        {
            e.printStackTrace();
            throw new RuntimeException( "Invalid XML configuration in JVM", e );
        } catch( SAXException e )
        {
            e.printStackTrace();
            throw new RuntimeException( "Invalid Repository format", e );
        }
    }

//    private DefaultMutableTreeNode createEclipseUpdateCenterNode( RepositoryInfo repository )
//    {
//        DefaultMutableTreeNode node = m_nodes.get( repository );
//        if( node == null )
//        {
//            node = new EclipseUpdateRepository( repository );
//            m_nodes.put( repository, node );
//        }
//        return node;
//    }

//    private DefaultMutableTreeNode createOsgiBundleRepositoryNode( RepositoryInfo repository )
//    {
//        DefaultMutableTreeNode node = m_nodes.get( repository );
//        if( node == null )
//        {
//            node = new OsgiBundleRepository( repository );
//            m_nodes.put( repository, node );
//        }
//        return node;
//    }

    private DefaultMutableTreeNode createProjectRepositoryNode( RepositoryInfo repository, ConfigBean config )
    {
        DefaultMutableTreeNode node = m_nodes.get( repository );
        if( node == null )
        {
            node = new ProjectRepository( repository, config );
            m_nodes.put( repository, node );
        }
        return node;
    }

    private DefaultMutableTreeNode createOscarBundleRepositoryNode( RepositoryInfo repository )
        throws IOException, ParserConfigurationException, SAXException
    {
        DefaultMutableTreeNode node = m_nodes.get( repository );
        if( node == null )
        {
            node = new ObrRepository( repository );
            m_nodes.put( repository, node );
        }
        return node;
    }
}
