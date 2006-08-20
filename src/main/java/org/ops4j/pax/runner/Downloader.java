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
import java.io.FileNotFoundException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Downloader
{

    private String m_repository;
    private boolean m_noCheckMD5;

    public Downloader( String repository, boolean noCheckMD5 )
    {
        m_repository = repository;
        m_noCheckMD5 = noCheckMD5;
    }

    public void download( URL source, File destination, boolean force )
        throws IOException
    {
        if( destination.exists() && ! force )
        {
            return;
        }
        File localRepo = new File( System.getProperty( "user.home" ), ".m2/repository" );
        String sourceString = source.toExternalForm();
        String path = sourceString.substring( m_repository.length() );
        File localCache = new File( localRepo, path );
        File md5File = getMD5File( localCache );
        int count = 3;
        while( count > 0 && (! localCache.exists() || ! verifyMD5( localCache, md5File ) ) )
        {
            downloadFile( localCache, source );
            URL md5source = new URL( getMd5Filename( sourceString ) );
            try
            {
                downloadFile( md5File, md5source );
            } catch( FileNotFoundException e )
            {
                System.out.println( "MD5 not present on server. Creating locally: " + md5File.getName() );
                createMD5Locally( localCache, md5File );
            }
            count--;
        }
        copyFile( localCache, destination );
    }

    private void createMD5Locally( File localCache, File md5File )
        throws IOException
    {
        String md5 = computeMD5( localCache );
        FileUtils.writeTextContent( md5File, md5 );
    }

    private boolean verifyMD5( File fileToCheck, File md5File )
        throws IOException
    {
        if( m_noCheckMD5 )
        {
            return true;
        }
        if( ! md5File.exists() )
        {
            return false;
        }
        String md5Master = FileUtils.getTextContent( md5File ).trim();
        String md5Value = computeMD5( fileToCheck );
        return md5Value.equalsIgnoreCase( md5Master );
    }

    private String computeMD5( File fileToCheck )
        throws IOException
    {
        BufferedInputStream in = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance( "MD5" );
            FileInputStream fis = new FileInputStream( fileToCheck );
            in = new BufferedInputStream( fis );
            int b = in.read();
                while( b != -1 )
                {
                    digest.update( (byte) b );
                    b = in.read();
                }
                byte[] result = digest.digest();
                String md5Value = convertToHexString( result );
                return md5Value;
        } catch( NoSuchAlgorithmException e )
        {
            // MD5 is always present
            e.printStackTrace();
            return null;
        } finally
        {
            if( in != null )
            {
                in.close();
            }
        }
    }

    private String convertToHexString( byte[] data )
    {
        StringBuffer buf = new StringBuffer();
        for( byte b : data )
        {
            int d = b & 0xFF;
            String s = Integer.toHexString( d );
            if( d < 16 )
            {
                buf.append( "0" );
            }
            buf.append( s.toLowerCase() );
        }
        return buf.toString();
    }

    private File getMD5File( File fileToCheck )
    {
        String path = fileToCheck.getAbsolutePath();
        String md5filename = getMd5Filename( path );
        File md5File = new File( md5filename );
        return md5File;
    }

    private String getMd5Filename( String path )
    {
        String md5filename = path + ".md5";
        return md5filename;
    }

    private void downloadFile( File localCache, URL source )
        throws IOException
    {
        InputStream in = null;
        BufferedOutputStream out = null;
        try
        {
            in = source.openStream();
            File parentDir = localCache.getParentFile();
            parentDir.mkdirs();
            FileOutputStream fos = new FileOutputStream( localCache );
            out = new BufferedOutputStream( fos );
            if( ! ( in instanceof BufferedInputStream ) )
            {
                in = new BufferedInputStream( in );
            }
            streamCopy( in, out, "Downloading " + composeFileName( source ) );
            out.flush();
        } finally
        {
            if( in != null )
            {
                in.close();
            }
            if( out != null )
            {
                out.close();
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
        boolean printed = false;
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
                printed = true;
            }
            bytes++;
        }
        if( ! printed )
        {
            System.out.print( title + " : " + bytes + " bytes\r" );
        }
        System.out.println();
    }

    public String getRepository()
    {
        return m_repository;
    }
}
