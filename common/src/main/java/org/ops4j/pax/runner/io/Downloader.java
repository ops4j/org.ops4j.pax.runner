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
package org.ops4j.pax.runner.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.ops4j.pax.runner.FileManager;
import org.ops4j.pax.runner.internal.RunnerOptions;

public class Downloader
{

    private static final Logger m_logger = Logger.getLogger( Downloader.class.getName() );
    
    private String m_repository;
    private URL m_context;
    private RunnerOptions m_options;

    public Downloader( String repository, boolean noCheckMD5 )
        throws MalformedURLException
    {
        m_context = new URL( repository );
        m_repository = repository;
    }

    public void download( String sourceURL, File destination, boolean force )
        throws IOException
    {
        if( destination.exists() && !force )
        {
            return;
        }
        File localRepo = new File( System.getProperty( "user.home" ), ".m2/repository" );
        URL source = new URL( m_context, sourceURL );
        String sourceString = source.toExternalForm();
        String path = sourceString.substring( m_repository.length() );
        File localCache = new File( localRepo, path );
        FileManager fileManager = new FileManagerImpl( m_options );
        int count = 3;
        File localMd5 = new File( localCache.getAbsolutePath() + ".md5" );
        while( count > 0 && ( !localCache.exists() || !fileManager.verifyMD5( localCache ) ) )
        {
            downloadFile( localCache, source );
            URL md5source = new URL( sourceString + ".md5" );
            try
            {
                downloadFile( localMd5, md5source );
            } catch( FileNotFoundException e )
            {
                m_logger.info( "MD5 not present on server. Creating locally: " + localMd5.getName() );
                fileManager.createMD5( localCache );
            }
            count--;
        }
        copyFile( localCache, destination );
    }

    private void downloadFile( File localCache, URL source )
        throws IOException
    {
    }

    private static void copyFile( File localCache, File destination )
        throws IOException
    {
        File destDir = destination.getParentFile();
        destDir.mkdirs();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try
        {
            fis = new FileInputStream( localCache );
            fos = new FileOutputStream( destination );
            copyStream( fis, fos, true );
        } finally
        {
            if( fis != null )
            {
                fis.close();
            }
            if( fos != null )
            {
                fos.close();
            }
        }
    }

    public static void copyStream( InputStream source, OutputStream dest, boolean buffer )
        throws IOException
    {
        if( source == null )
        {
            throw new IllegalArgumentException( "source == null" );
        }
        if( dest == null )
        {
            throw new IllegalArgumentException( "dest == null" );
        }
        if( buffer )
        {
            source = new BufferedInputStream( source );
            dest = new BufferedOutputStream( dest );
        }
        int ch = source.read();
        while( ch != -1 )
        {
            dest.write( ch );
            ch = source.read();
        }
        dest.flush();
    }

    private String composeFileName( URL url )
    {
        String path = url.getPath();
        int slashPos = path.lastIndexOf( '/' );
        path = path.substring( slashPos + 1 );
        return path;
    }

    public String getRepository()
    {
        return m_repository;
    }
}
