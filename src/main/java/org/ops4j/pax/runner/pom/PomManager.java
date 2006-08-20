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
package org.ops4j.pax.runner.pom;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.ops4j.pax.runner.Downloader;
import org.ops4j.pax.runner.CmdLine;
import org.ops4j.pax.runner.PropertyResolver;
import org.ops4j.pax.runner.BundleManager;
import org.ops4j.pax.runner.Run;

public class PomManager
{

    private Downloader m_downloader;

    public PomManager( Downloader downloader )
    {
        m_downloader = downloader;
    }

    public Document retrievePom( CmdLine cmdLine )
        throws IOException, ParserConfigurationException, SAXException
    {
        String artifact = cmdLine.getValue( "artifact" );
        String groupId = cmdLine.getValue( "group" );
        String version = cmdLine.getValue( "version" );
        if( "LATEST".equals( version) )
        {
            version = getLatestVersion( groupId, artifact );
        }

        URL url;
        if( artifact == null )
        {
            url = new URL( cmdLine.getValue( "url" ) );
            System.out.println( "   Starting: " + url.toString() );
        }
        else
        {
            System.out.println( "   Starting: " + groupId + ", " + artifact + ", " + version );
            String filename = artifact + "-" + version + ".pom";
            groupId = groupId.replace( '.', '/' );
            url = new URL(
                m_downloader.getRepository() + groupId + "/" + artifact + "/" + version + "/" + filename
            );
        }

        String filename = artifact + "_" + version + ".pom";
        filename = PropertyResolver.resolve( System.getProperties(), filename );
        File dest = new File( Run.WORK_DIR, "lib/" + filename );
        m_downloader.download( url, dest, false );
        return parseDoc( dest );
    }

    public void info( Document pom )
    {
        Element projectName = DomUtils.getElement( pom, "name" );
        Element description = DomUtils.getElement( pom, "description" );
        System.out.println( "       Name: " + projectName.getTextContent() );
        System.out.println( "Description: " + description.getTextContent() );
    }

    public List<File> getBundles( CmdLine cmdLine )
        throws IOException, ParserConfigurationException, SAXException
    {
        Document pom = retrievePom( cmdLine );
        info( pom );
        Element dependencies = DomUtils.getElement( pom, "dependencies" );
        return getBundles( dependencies );
    }

    public Properties getProperties( CmdLine cmdLine )
        throws IOException, ParserConfigurationException, SAXException
    {
        Document pom = retrievePom( cmdLine );
        return DomUtils.parseProperties( pom );
    }

    public List<File> getBundles( Element dependencies )
        throws IOException
    {
        List<File> bundles = new ArrayList<File>();
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
                if( ! "test".equals( scope ) && ! "compile".equals( scope ) )
                {
                    BundleManager bundleManager = new BundleManager( m_downloader );
                    File dest = bundleManager.getBundle( group, artifact, version );
                    if( ! "provided".equals( scope ) )
                    {
                        bundles.add( dest );
                    }
                }
            }
        }
        return bundles;
    }

    private String getLatestVersion( String group, String artifact )
        throws IOException, ParserConfigurationException, SAXException
    {

        String metaLocation =  m_downloader.getRepository() + group.replace( '.', '/') + "/" + artifact + "/maven-metadata.xml";
        URL metaUrl = new URL( metaLocation );
        File dest = new File( Run.WORK_DIR, "latest.pom" );
        try
        {
            m_downloader.download( metaUrl, dest, true );
        } catch( IOException e )
        {
            IOException ioException = new IOException( "Unable to retrieve LATEST version of [" + group + ":" + artifact + "]" );
            ioException.initCause( e );
            throw ioException;

        }
        Document doc = parseDoc( dest );
        Element root = doc.getDocumentElement();
        NodeList children = root.getElementsByTagName( "version" );
        Element latestVersion = (Element) children.item( 0 );
        return latestVersion.getTextContent();
    }

    private Document parseDoc( File docFile )
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

}
