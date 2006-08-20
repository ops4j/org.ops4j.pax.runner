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
package org.ops4j.pax.runner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class BundleManager
{
    private Downloader m_downloader;

    public BundleManager( Downloader downloader )
    {
        m_downloader = downloader;
    }

    public File getBundle( String group, String artifact, String version )
        throws IOException
    {
        URL url = composeURL( group, artifact, version );
        version = version.replace( "-SNAPSHOT", ".SNAPSHOT" );
        File dest = new File( Run.WORK_DIR, "lib/" + group.replace('.','/' ));
        dest = new File( dest, artifact + "_" + version + ".jar" );
        m_downloader.download( url, dest, false );
        return dest;
    }

    private URL composeURL( String group, String artifact, String version )
        throws MalformedURLException
    {
        String filename = artifact + "-" + version + ".jar";
        group = group.replace( '.', '/' );
        String url = m_downloader.getRepository() + group + "/" + artifact + "/" + version + "/" + filename;
        return new URL( url );
    }

}
