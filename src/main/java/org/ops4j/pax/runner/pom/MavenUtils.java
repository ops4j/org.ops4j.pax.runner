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

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.ops4j.pax.runner.Downloader;
import org.ops4j.pax.runner.Run;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MavenUtils
{

    static String getLatestVersion( String group, String artifact, Downloader downloader )
        throws IOException, ParserConfigurationException, SAXException
    {
        String metaLocation = group.replace( '.', '/' ) + "/" + artifact + "/maven-metadata.xml";
        File dest = new File( Run.WORK_DIR, "latest.pom" );
        try
        {
            downloader.download( metaLocation, dest, true );
        } catch( IOException e )
        {
            IOException ioException =
                new IOException( "Unable to retrieve LATEST version of [" + group + ":" + artifact + "]" );
            ioException.initCause( e );
            throw ioException;
        }
        Document doc = PomManager.parseDoc( dest );
        return getTextContentOfElement( doc, "versioning/versions/version[last]" );
    }

    static String getTextContentOfElement( Document doc, String path )
    {
        StringTokenizer st = new StringTokenizer( path, "/", false );
        Element currentElement = doc.getDocumentElement();
        while( st.hasMoreTokens() )
        {
            String childName = st.nextToken();
            NodeList children = currentElement.getElementsByTagName( childName );
            int numChildren = children.getLength();
            int index = 0;
            if( childName.endsWith( "]" ) )
            {
                int startPos = childName.indexOf( "[" );
                int endPos = childName.indexOf( "]" );
                String numbers = childName.substring( startPos + 1, endPos );
                if( "last".equals( numbers ) )
                {
                    index = numChildren - 1;
                }
                else
                {
                    index = Integer.parseInt( numbers );
                }
            }
            if( index > numChildren )
            {
                throw new IllegalArgumentException(
                    "index of " + index + " is larger than the number of child nodes (" + numChildren + ")"
                );
            }
            currentElement = (Element) children.item( index );
        }
        return currentElement.getTextContent();
    }
}
