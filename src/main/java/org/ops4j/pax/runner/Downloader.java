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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.ops4j.pax.runner.util.NullArgumentException;

public class Downloader
{

    private static final Logger LOGGER = Logger.getLogger( Downloader.class.getName() );

    private final List<String> m_repositories;
    private final boolean m_noCheckMD5;
    private boolean m_isNoCertCheckAcceptable;

    public Downloader( List<String> repositories, boolean noCheckMD5, boolean noCertCheckAcceptable )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( repositories, "repositories" );
        if( repositories.isEmpty() )
        {
            throw new IllegalArgumentException( "[repositories] argument must not be empty." );
        }
        m_isNoCertCheckAcceptable = noCertCheckAcceptable;
        m_repositories = repositories;
        m_noCheckMD5 = noCheckMD5;
    }

    public void download( String path, File destination, boolean force )
        throws IOException
    {
        NullArgumentException.validateNotEmpty( path, "path" );
        NullArgumentException.validateNotNull( destination, "destination" );

        if( destination.exists() && !force )
        {
            return;
        }

        File localRepo = new File( System.getProperty( "user.home" ), ".m2/repository" );

        File localCache = new File( localRepo, path );
        for( String repository : m_repositories )
        {
            File md5File = getMD5File( localCache );

            String fullPath = repository + path;
            URL url;
            try
            {
                url = new URL( fullPath );
            }
            catch( MalformedURLException e )
            {
                LOGGER.finer( "Fail to construct URL [" + fullPath + "]. Skip." );
                continue;
            }

            String sourceString = url.toExternalForm();
            int count = 3;
            while( count > 0 && (!localCache.exists() || !verifyMD5( localCache, md5File )) )
            {
                count--;

                try
                {
                    downloadFile( localCache, url );
                }
                catch( FileNotFoundException e )
                {
                    System.out.println( "Artifact from url [" + url + "] is not found." );
                    continue;
                }

                URL md5source = new URL( getMd5Filename( sourceString ) );
                try
                {
                    downloadFile( md5File, md5source );
                }
                catch( FileNotFoundException e )
                {
                    System.out.println( "MD5 not present on server. Creating locally: " + md5File.getName() );
                    createMD5Locally( localCache, md5File );
                }
            }
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
        if( !md5File.exists() )
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
        }
        catch( NoSuchAlgorithmException e )
        {
            // MD5 is always present
            e.printStackTrace();
            return null;
        }
        finally
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
            in = openUrlStream( source );

            // Means file not found.
            // TODO: Should we handle this with boolean return instead?
            if( in == null )
            {
                throw new FileNotFoundException( "File specified from [" + source + "] is not found." );
            }

            File parentDir = localCache.getParentFile();
            parentDir.mkdirs();
            FileOutputStream fos = new FileOutputStream( localCache );
            out = new BufferedOutputStream( fos );
            if( !(in instanceof BufferedInputStream) )
            {
                in = new BufferedInputStream( in );
            }
            StreamUtils.streamCopy( in, out, "Downloading " + composeFileName( source ) );
            out.flush();
        }
        catch( NoSuchAlgorithmException e )
        {
            LOGGER.logp( Level.SEVERE, Downloader.class.getName(), "downloadFile", e.getMessage(), e );
        }
        catch( KeyManagementException e )
        {
            LOGGER.logp( Level.SEVERE, Downloader.class.getName(), "downloadFile", e.getMessage(), e );
        }
        finally
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

    private InputStream openUrlStream( URL remote )
        throws IOException,
        NoSuchAlgorithmException,
        KeyManagementException
    {
        URLConnection conn = remote.openConnection();
        if( conn instanceof HttpsURLConnection )
        {
            LOGGER.fine( this + " - HTTPS connection opened." );
            if( m_isNoCertCheckAcceptable )
            {
                LOGGER.fine( this + " - Using NullTrustManager." );
                HttpsURLConnection ssl = (HttpsURLConnection) conn;
                TrustManager nullTrustManager = new NullTrustManager();
                SSLContext ctx = SSLContext.getInstance( "SSLv3" );
                TrustManager[] trustManagers = new TrustManager[]
                {
                    nullTrustManager
                };
                ctx.init( null, trustManagers, null );
                LOGGER.fine( this + " - Setting SSLv3 socket factory." );
                SSLSocketFactory factory = ctx.getSocketFactory();
                ssl.setSSLSocketFactory( factory );
                LOGGER.fine( this + " - SSL socket factory is set." );
            }
        }
        conn.connect();
        if( conn instanceof HttpURLConnection )
        {
            int code = ((HttpURLConnection) conn).getResponseCode();
            LOGGER.fine( this + " - ResponseCode: " + code );
            if( code == HttpURLConnection.HTTP_UNAUTHORIZED )
            {
                throw new IOException( "Unauthorized request." );
            }
            else if( code == HttpURLConnection.HTTP_NOT_FOUND )
            {
                return null;
            }
            else if( code != HttpURLConnection.HTTP_OK )
            {
                throw new IOException( "Unexpected Result: " + code );
            }
        }
        return conn.getInputStream();
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
        }
        finally
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
}
