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

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.ops4j.pax.runner.repositories.RepositoryInfo;
import org.ops4j.pax.runner.repositories.BundleObserver;
import org.ops4j.pax.runner.repositories.BundleRef;
import org.xml.sax.SAXException;

public class ObrRepository extends DefaultMutableTreeNode
    implements MutableTreeNode, BundleObserver
{

    private static Logger m_logger = Logger.getLogger( ObrRepository.class );

    private RepositoryInfo m_info;
    private HashMap<String, DefaultMutableTreeNode> m_categories;

    public ObrRepository( RepositoryInfo info )
        throws IOException, ParserConfigurationException, SAXException
    {
        super();
        m_categories = new HashMap<String, DefaultMutableTreeNode>();
        m_info = info;
        setAllowsChildren( true );
    }

    public void bundleAdded( BundleRef bundle )
    {
        Properties props = bundle.getProperties();
        String category = props.getProperty( "bundle-category" );
        DefaultMutableTreeNode categoryNode = m_categories.get( category );
        if( categoryNode == null )
        {
            categoryNode = new DefaultMutableTreeNode( category, true );
            m_categories.put( category, categoryNode );
            add( categoryNode );
        }
        MutableTreeNode nameNode = new DefaultMutableTreeNode( bundle, false );
        categoryNode.add( nameNode );
    }

    public void bundleRemoved( BundleRef bundle )
    {
        Properties props = bundle.getProperties();
        String category = props.getProperty( "bundle-category" );
        DefaultMutableTreeNode categoryNode = m_categories.get( category );
        if( categoryNode == null )
        {
            return;
        }
        MutableTreeNode nameNode = new DefaultMutableTreeNode( bundle, false );
        categoryNode.remove( nameNode );
    }
}
