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
package org.ops4j.pax.runner.maven2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ops4j.pax.runner.Bundle;
import org.ops4j.pax.runner.BundleState;
import org.ops4j.pax.runner.PropertyResolver;
import org.ops4j.pax.runner.Repository;
import org.ops4j.pax.runner.RunnerOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PomManager
{

    private static final int STARTLEVEL_DEFAULT = 5;
	private Repository m_repository;
    private RunnerOptions m_options;

    private PomManager( Repository repository, RunnerOptions options )
    {
        m_repository = repository;
        m_options = options;
    }

    public Document retrievePom( PomInfo pomInfo )
        throws IOException, ParserConfigurationException, SAXException
    {
        String artifact = pomInfo.getArtifact();
        String groupId = pomInfo.getGroup();
        String version = pomInfo.getVersion();
        if( "LATEST".equals( version) )
        {
            version = MavenUtils.getLatestVersion( groupId, artifact, m_repository, m_options );
        }

        String path;
        if( artifact == null )
        {
            path = m_options.getURL();
            System.out.println( "   Starting: " + path );
        }
        else
        {
            System.out.println( "   Starting: " + groupId + ", " + artifact + ", " + version );
            String filename = artifact + "-" + version + ".pomInfo";
            groupId = groupId.replace( '.', '/' );
            path = groupId + "/" + artifact + "/" + version + "/" + filename;
        }

        String filename = artifact + "_" + version + ".pomInfo";
        filename = PropertyResolver.resolve( System.getProperties(), filename );
        File dest = new File( m_options.getWorkDir(), "lib/" + filename );
        m_repository.download( path, dest, false );
        return parseDoc( dest );
    }

    public void info( Document pom )
    {
        Element projectName = DomUtils.getElement( pom, "name" );
        Element description = DomUtils.getElement( pom, "description" );
        if ( projectName != null )
        {
            System.out.println( "       Name: " + projectName.getTextContent() );
        }
        if ( description != null )
        {
            System.out.println( "Description: " + description.getTextContent() );
        }
    }

    public List<Bundle> getBundles( PomInfo pomInfo )
        throws IOException, ParserConfigurationException, SAXException
    {
        Document pomDoc = retrievePom( pomInfo );
        info( pomDoc );
        Element dependencies = DomUtils.getElement( pomDoc, "dependencies" );
        return getBundles( dependencies );
    }

    public Properties getProperties( PomInfo pomInfo )
        throws IOException, ParserConfigurationException, SAXException
    {
        Document pomDoc = retrievePom( pomInfo );
        return DomUtils.parseProperties( pomDoc );
    }

    /**
     *
     * @param dependencies of type
     * &lt;dependency startlevel="3" targetstate="resolved"&gt;
     * &lt;groupId&gt;org.ops4j.pax.wicket.samples.departmentstore.view
     * &lt;/groupId&gt;
     * &lt;artifactId&gt;floor&lt;/artifactId&gt;
     * &lt;version&gt;0.2.1&lt;/version\&gt;
     * &lt;/dependency&gt;
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public List<Bundle> getBundles( Element dependencies )
        throws IOException, ParserConfigurationException, SAXException
    {
        List<Bundle> bundles = new ArrayList<Bundle>();
        NodeList nl = dependencies.getElementsByTagName( "dependency" );
        for( int i = 0; i < nl.getLength(); i++ )
        {
            Node node = nl.item( i );
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                String artifact = DomUtils.getElement( node, "artifactId" );
                String group = DomUtils.getElement( node, "groupId" );
                String version = DomUtils.getElement( node, "version" );
                String scope = DomUtils.getElement( node, "scope" );

                NamedNodeMap attributes = node.getAttributes();
                Node startLevelNode = attributes.getNamedItem( "startlevel" );
                int startLevel = STARTLEVEL_DEFAULT;
                if ( startLevelNode != null )
                {
                	startLevel = Integer.parseInt(startLevelNode.getNodeValue());
                }
                Node targetStateNode = attributes.getNamedItem( "targetstate" );
                BundleState targetState = BundleState.START;
                if ( targetStateNode != null )
                {
                	targetState = BundleState.valueOf( BundleState.class, targetStateNode.getNodeValue().toUpperCase());
                }
                if( ! "test".equals( scope ) && ! "compile".equals( scope ) )
                {
                    BundleManager bundleManager = new BundleManager( m_repository, m_options );
                    File bundleFile = bundleManager.getBundleFile( group, artifact, version );
                    Bundle dest = new Bundle(bundleFile, startLevel, targetState );
                    if( ! "provided".equals( scope ) )
                    {
                        bundles.add( dest );
                    }
                }
            }
        }
        return bundles;
    }

    static Document parseDoc( File docFile )
        throws ParserConfigurationException, SAXException, IOException
    {
        FileInputStream fis = new FileInputStream( docFile );
        try
        {
            BufferedInputStream in = new BufferedInputStream( fis );
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource source = new InputSource( in );
            Document document = builder.parse( source );
            return document;
        } finally
        {
            fis.close();
        }
    }

    public static PomManager getInstance( Repository repository, RunnerOptions options )
    {
        return new PomManager( repository, options );
    }

}
