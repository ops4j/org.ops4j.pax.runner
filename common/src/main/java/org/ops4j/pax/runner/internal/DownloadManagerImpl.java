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
package org.ops4j.pax.runner.internal;

import org.ops4j.pax.runner.DownloadManager;
import org.ops4j.pax.runner.ServiceManager;
import org.ops4j.pax.runner.CacheHandler;
import org.ops4j.pax.runner.ServiceException;
import org.ops4j.monitors.stream.StreamMonitor;
import org.ops4j.io.PrintStreamMonitor;
import org.ops4j.io.StreamUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadManagerImpl
    implements DownloadManager
{

    public File download( String symbolicName )
        throws IOException
    {
        //TODO: Auto-generated, need attention.
        return null;
    }

    public File download( PomInfo pomInfo )
        throws IOException
    {
        //TODO: Auto-generated, need attention.
        return null;
    }

    public File download( URL url )
        throws IOException, ServiceException
    {
        CacheHandler cacheHandler = ServiceManager.getInstance().getService( CacheHandler.class );
        File localCache = cacheHandler.getLocalCache( url );
        if( localCache.exists() )
        {
            return localCache;
        }
        URLConnection urlConnection = url.openConnection();
        int expected = urlConnection.getContentLength();
        InputStream in = urlConnection.getInputStream();
        File parentDir = localCache.getParentFile();
        parentDir.mkdirs();
        FileOutputStream fos = new FileOutputStream( localCache );
        BufferedOutputStream out = new BufferedOutputStream( fos );

        StreamMonitor monitor = new PrintStreamMonitor( System.out );
        StreamUtils.copyStream( monitor, url, expected, in, out, true );
        return localCache;
    }
}
