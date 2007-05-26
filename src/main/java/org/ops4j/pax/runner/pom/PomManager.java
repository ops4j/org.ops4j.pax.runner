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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.ops4j.pax.runner.CmdLine;
import org.ops4j.pax.runner.Downloader;
import org.ops4j.pax.runner.PropertyResolver;
import org.ops4j.pax.runner.Run;
import org.ops4j.pax.runner.util.NullArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PomManager
{

    private static final String SCOPE_TAGS = "scope";
    private static final String SCOPE_TEST = "test";
    private static final String SCOPE_COMPILE = "compile";

    private Downloader m_downloader;

    public PomManager( Downloader downloader )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( downloader, "downloader" );
        m_downloader = downloader;
    }

    public Document retrievePom( CmdLine cmdLine )
        throws IOException,
               ParserConfigurationException,
               SAXException
    {
        NullArgumentException.validateNotNull( cmdLine, "cmdLine" );

        String artifact = cmdLine.getValue( "artifact" );
        if( artifact == null )
        {
            System.out.println( "   Artifact declaration is required." );

            return null;
        }

        String groupId = cmdLine.getValue( "group" );
        String version = cmdLine.getValue( "version" );

        if( version == null || version.length() == 0 || "LATEST".equalsIgnoreCase( version ) )
        {
            version = MavenUtils.getLatestVersion( groupId, artifact, m_downloader );
        }

        String snapshot = version;
        if( version.endsWith( "-SNAPSHOT" ) )
        {
            snapshot = MavenUtils.getSnapshotVersion( groupId, artifact, version, m_downloader );
        }

        System.out.println( "   Starting: " + groupId + ", " + artifact + ", " + version );
        String pomFilename = artifact + "-" + snapshot + ".pom";
        groupId = groupId.replace( '.', '/' );
        String path = groupId + "/" + artifact + "/" + version + "/" + pomFilename;

        String filename = artifact + "_" + version + ".pom";
        filename = PropertyResolver.resolve( System.getProperties(), filename );
        File dest = new File( Run.WORK_DIR, "lib/" + filename );
        m_downloader.download( path, dest, version.indexOf( "SNAPSHOT") >= 0 );
        return parseDoc( dest );
    }

    public void info( Document pom )
    {
        Element projectName = DomUtils.getElement( pom, "name" );
        Element description = DomUtils.getElement( pom, "description" );
        if ( projectName != null )
        {
            String textContent = projectName.getTextContent();
            System.out.println( "       Name: " + textContent );
        }
        
        if ( description != null )
        {
            String textContent = description.getTextContent();
            System.out.println( "Description: " + textContent );
        }
    }

    public List<File> getBundles( CmdLine cmdLine )
        throws IOException,
               ParserConfigurationException,
               SAXException
    {
        Document pom = retrievePom( cmdLine );
        if( pom == null )
        {
            return new ArrayList<File>();
        }
        info( pom );
        Element dependencies = DomUtils.getElement( pom, "dependencies" );
        return getBundles( dependencies );
    }

    public Properties getProperties( CmdLine cmdLine )
        throws IOException,
               ParserConfigurationException,
               SAXException
    {
        Document pom = retrievePom( cmdLine );
        if( pom == null )
        {
            return new Properties();
        }

        return DomUtils.parseProperties( pom );
    }

    private final List<File> getBundles( Element dependencies )
        throws IOException,
               ParserConfigurationException,
               SAXException
    {
        if( dependencies == null )
        {
            System.err.println( "WARNING: [dependencies] argument is null => no bundles will be deployed." );
            return new ArrayList<File>();
        }

        List<File> bundles = new ArrayList<File>();
        NodeList nl = dependencies.getElementsByTagName( "dependency" );
        for( int i = 0; i < nl.getLength(); i++ )
        {
            Node node = nl.item( i );

            short nodeType = node.getNodeType();
            if( nodeType == Node.ELEMENT_NODE )
            {
                String scope = DomUtils.getElement( node, SCOPE_TAGS );
                if( !SCOPE_TEST.equals( scope ) && !SCOPE_COMPILE.equals( scope ) )
                {
                    String group = DomUtils.getElement( node, "groupId" );
                    String artifact = DomUtils.getElement( node, "artifactId" );
                    String version = DomUtils.getElement( node, "version" );
                    BundleManager bundleManager = new BundleManager( m_downloader );
                    File dest = bundleManager.getBundle( group, artifact, version );
                    if( !"provided".equals( scope ) )
                    {
                        bundles.add( dest );
                    }
                }
            }
        }

        return bundles;
    }

    static Document parseDoc( File docFile )
        throws ParserConfigurationException,
               SAXException,
               IOException
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
        }
        finally
        {
            fis.close();
        }
    }

}
