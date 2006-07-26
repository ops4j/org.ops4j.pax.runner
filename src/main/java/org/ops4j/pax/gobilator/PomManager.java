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
package org.ops4j.pax.gobilator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PomManager
{

    private Downloader m_downloader;

    public PomManager( Downloader downloader )
    {
        m_downloader = downloader;
    }

    Document retrievePom( CmdLine cmdLine )
        throws IOException, ParserConfigurationException, SAXException
    {
        String artifact = cmdLine.getValue( "artifact" );
        String groupId = cmdLine.getValue( "group" );
        String version = cmdLine.getValue( "version" );
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
        File dest = new File( "lib/", filename );
        m_downloader.download( url, dest, false );
        FileInputStream fis = new FileInputStream( dest );
        try
        {
            BufferedInputStream in = new BufferedInputStream( fis );
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            if( ! ( in instanceof BufferedInputStream ) )
            {
                in = new BufferedInputStream( in );
            }
            InputSource source = new InputSource( in );
            Document document = builder.parse( source );
            return document;
        } finally
        {
            fis.close();
        }
    }

    public void info( Document pom )
    {
        Element projectName = DomUtils.getElement( pom, "name" );
        Element description = DomUtils.getElement( pom, "description" );
        System.out.println( "       Name: " + projectName.getTextContent() );
        System.out.println( "Description: " + description.getTextContent() );
    }
}
