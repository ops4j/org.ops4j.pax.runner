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
package org.ops4j.pax.runner.idea.repositories;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.ops4j.pax.runner.repositories.BundleInfo;
import org.ops4j.pax.runner.repositories.Repository;
import org.ops4j.pax.runner.repositories.RepositoryInfo;
import org.apache.log4j.Logger;

public class ObrRepository extends DefaultMutableTreeNode
    implements MutableTreeNode, Repository
{
    private static Logger m_logger = Logger.getLogger( ObrRepository.class );

    private RepositoryInfo m_info;

    public ObrRepository( RepositoryInfo info )
        throws IOException, ParserConfigurationException, SAXException
    {
        super();
        m_info = info;
        setAllowsChildren( true );
        URL url = new URL( m_info.getUrl() );
        InputStream in = null;
        try
        {
            in = url.openStream();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse( in );
            Element root = doc.getDocumentElement();
            Element repository = (Element) root.getElementsByTagName( "repository" ).item( 0 );
            Element repoName = (Element) repository.getElementsByTagName( "name" ).item( 0 );
            setUserObject( repoName.getTextContent() );
            NodeList bundleNodes = root.getElementsByTagName( "bundle" );
            HashMap<String, DefaultMutableTreeNode> categories = new HashMap<String, DefaultMutableTreeNode>();
            for( int i = 0; i < bundleNodes.getLength(); i++ )
            {
                Element bundleElement = (Element) bundleNodes.item( i );
                Element nameElement = (Element) bundleElement.getElementsByTagName( "bundle-name" ).item( 0 );
                Element categoryElement = (Element) bundleElement.getElementsByTagName( "bundle-category" ).item( 0 );
                String category = categoryElement.getTextContent();
                DefaultMutableTreeNode categoryNode = categories.get( category );
                if( categoryNode == null )
                {
                    categoryNode = new DefaultMutableTreeNode( category, true );
                    categories.put( category, categoryNode );
                    add( categoryNode );
                }
                String name = nameElement.getTextContent();

                File dest = null; // TODO
                BundleInfo bundle = new BundleInfo( name, this.getInfo(), dest );
                MutableTreeNode nameNode = new DefaultMutableTreeNode( bundle, false );
                categoryNode.add( nameNode );
            }
        } finally
        {
            if( in != null )
            {
                try
                {
                    in.close();
                } catch( IOException e )
                {
                    e.printStackTrace();  //TODO: Auto-generated, need attention.
                }
            }
        }
    }

    public RepositoryInfo getInfo()
    {
        return m_info;
    }

    public void download( BundleInfo bundle )
    {
    }
}
