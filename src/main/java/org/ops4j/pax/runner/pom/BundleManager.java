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
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import org.ops4j.pax.runner.Downloader;
import org.ops4j.pax.runner.Run;
import org.ops4j.pax.runner.util.NullArgumentException;
import org.xml.sax.SAXException;

public final class BundleManager
{
    private Downloader m_downloader;

    public BundleManager( Downloader downloader )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( downloader, "downloader" );

        m_downloader = downloader;
    }

    public final File getBundle( String group, String artifact, String version )
        throws IllegalArgumentException,
        IOException,
        ParserConfigurationException,
        SAXException
    {
        NullArgumentException.validateNotEmpty( group, "group" );
        NullArgumentException.validateNotEmpty( artifact, "artifact" );
        NullArgumentException.validateNotEmpty( version, "version" );

        String url = composePath( group, artifact, version );
        if ( "LATEST".equalsIgnoreCase( version ) )
        {
            version = MavenUtils.getLatestVersion( group, artifact, m_downloader );
        }
        version = version.replace( "-SNAPSHOT", ".SNAPSHOT" );
        if ( version == null || "LATEST".equals( version ) )
        {
            version = MavenUtils.getLatestVersion( group, artifact, m_downloader );
        }

        File dest = new File( Run.WORK_DIR, "lib/" + group.replace( '.', '/' ) );
        dest = new File( dest, artifact + "_" + version + ".jar" );

        m_downloader.download( url, dest, false );

        return dest;
    }

    private String composePath( String group, String artifact, String version )
        throws MalformedURLException
    {
        NullArgumentException.validateNotEmpty( group, "group" );
        NullArgumentException.validateNotEmpty( artifact, "artifact" );
        NullArgumentException.validateNotEmpty( version, "version" );

        String filename = artifact + "-" + version + ".jar";
        group = group.replace( '.', '/' );
        String path = group + "/" + artifact + "/" + version + "/" + filename;
        
        return path;
    }
}
