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
import javax.xml.parsers.ParserConfigurationException;
import org.ops4j.pax.runner.Repository;
import org.ops4j.pax.runner.Run;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MavenUtils
{
    static String getLatestVersion( String group, String artifact, Repository repository )
        throws IOException, ParserConfigurationException, SAXException
    {
        String metaLocation = group.replace( '.', '/') + "/" + artifact + "/maven-metadata.xml";
        File dest = new File( Run.WORK_DIR, "latest.pom" );
        try
        {
            repository.download( metaLocation, dest, true );
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
