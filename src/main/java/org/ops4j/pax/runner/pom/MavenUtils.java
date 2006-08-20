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

import org.ops4j.pax.runner.Downloader;
import org.ops4j.pax.runner.Run;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;

public class MavenUtils
{
    static String getLatestVersion( String group, String artifact, Downloader downloader )
        throws IOException, ParserConfigurationException, SAXException
    {
        String metaLocation = downloader.getRepository() + group.replace( '.', '/') + "/" + artifact + "/maven-metadata.xml";
        URL metaUrl = new URL( metaLocation );
        File dest = new File( Run.WORK_DIR, "latest.pom" );
        try
        {
            downloader.download( metaUrl, dest, true );
        } catch( IOException e )
        {
            IOException ioException = new IOException( "Unable to retrieve LATEST version of [" + group + ":" + artifact + "]" );
            ioException.initCause( e );
            throw ioException;
        }
        Document doc = PomManager.parseDoc( dest );
        Element root = doc.getDocumentElement();
        NodeList children = root.getElementsByTagName( "version" );
        Element latestVersion = (Element) children.item( 0 );
        return latestVersion.getTextContent();
    }
}
