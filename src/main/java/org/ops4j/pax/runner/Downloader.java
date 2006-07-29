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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URL;

public class Downloader
{

    private String m_repository;

    public Downloader( String repository )
    {
        m_repository = repository;
    }

    void download( URL source, File destination, boolean force )
        throws IOException
    {
        if( destination.exists() && ! force )
        {
            return;
        }
        File localRepo = new File( System.getProperty( "user.home" ), ".m2/repository" );
        String path = source.toExternalForm().substring( m_repository.length() );
        File localCache = new File( localRepo, path );
        if( localCache.exists() )
        {
            copyFile( localCache, destination );
            return;
        }
        File parentDir = destination.getParentFile();
        parentDir.mkdirs();

        FileOutputStream fos = null;
        InputStream in = source.openStream();
        try
        {
            fos = new FileOutputStream( destination );
            BufferedOutputStream out = new BufferedOutputStream( fos );
            if( ! ( in instanceof BufferedInputStream ) )
            {
                in = new BufferedInputStream( in );
            }
            streamCopy( in, out, "Downloading " + composeFileName( source ) );
            out.flush();
        } finally
        {
            in.close();
            if( fos != null )
            {
                fos.close();
            }
        }
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

    static void copyStream( InputStream source, OutputStream dest, boolean buffer )
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

    private void streamCopy( InputStream in, BufferedOutputStream out, String title )
        throws IOException
    {
        long start = System.currentTimeMillis();
        int b = in.read();
        int counter = 0;
        int bytes = 0;
        while( b != -1 )
        {
            out.write( b );
            b = in.read();
            counter = ( counter + 1 ) % 1024;
            if( counter == 0 )
            {
                long time = System.currentTimeMillis() - start;
                if( time <= 0 )
                {
                    time = 1;
                }
                long kbps = bytes / time;
                System.out.print( title + " : " + bytes + " bytes @ [ " + kbps + "kBps ]\r" );
            }
            bytes++;
        }
        System.out.println();
    }

    public String getRepository()
    {
        return m_repository;
    }
}
