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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.ParserConfigurationException;
import org.ops4j.pax.runner.internal.RunnerOptions;
import org.ops4j.pax.runner.repositories.Repository;
import org.ops4j.pax.runner.repositories.BundleInfo;
import org.ops4j.pax.runner.PomInfo;
import org.xml.sax.SAXException;

public class BundleManager
{

    private Repository m_repository;
    private RunnerOptions m_options;

    public BundleManager( Repository repository, RunnerOptions options )
    {
        m_repository = repository;
        m_options = options;
    }

    public BundleInfo getBundleFile( PomInfo pomInfo )
        throws IOException, ParserConfigurationException, SAXException
    {
        String group = pomInfo.getGroup();
        String artifact = pomInfo.getArtifact();
        String version = pomInfo.getVersion();
        String path = composeURL( group, artifact, version );
        // TODO: Add back support for LATEST.
//        if( "LATEST".equalsIgnoreCase( version ) )
//        {
//            version = MavenUtils.getLatestVersion( group, artifact, m_repository, m_options );
//        }
        version = version.replace( "-SNAPSHOT", ".SNAPSHOT" );
        File dest = new File( m_options.getWorkDir(), "lib/" + group.replace( '.', '/' ) );
        dest = new File( dest, artifact + "_" + version + ".jar" );
        BundleInfo bundle = new BundleInfo( group + "." + artifact, null, dest );
        m_repository.download( bundle );
        return bundle;
    }

    private String composeURL( String group, String artifact, String version )
        throws MalformedURLException
    {
        String filename = artifact + "-" + version + ".jar";
        group = group.replace( '.', '/' );
        return group + "/" + artifact + "/" + version + "/" + filename;
    }

}
