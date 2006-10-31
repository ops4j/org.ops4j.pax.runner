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
package org.ops4j.pax.repoman;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.ops4j.io.StreamUtils;
import org.ops4j.pax.runner.RunnerOptions;
import org.ops4j.pax.common.FileUtils;

public class FileManagerImpl
    implements FileManager
{

    private RunnerOptions m_options;

    public FileManagerImpl( RunnerOptions options )
    {
        m_options = options;
    }

    public void copy( File src, File dest )
        throws IOException
    {
        FileInputStream fis = new FileInputStream( src );
        if( dest.isDirectory() )
        {
            String filename = src.getName();
            dest = new File( dest, filename );
        }
        FileOutputStream fos = new FileOutputStream( dest );
        StreamUtils.copyStream( fis, fos, true );
    }

    public void move( File src, File dest )
        throws IOException
    {
        FileInputStream fis = new FileInputStream( src );
        if( dest.isDirectory() )
        {
            String filename = src.getName();
            dest = new File( dest, filename );
        }
        FileOutputStream fos = new FileOutputStream( dest );
        StreamUtils.copyStream( fis, fos, true );
        src.delete();
    }

    public void createMD5( File file )
        throws IOException
    {
        File md5File = getMD5File( file );
        createMD5Locally( file, md5File );
    }

    public boolean verifyMD5( File file )
        throws IOException
    {
        File md5File = getMD5File( file );
        return verifyMD5( file, md5File );
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
        return new File( md5filename );
    }

    private String getMd5Filename( String path )
    {
        return path + ".md5";
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
        if( m_options.isNoMd5Checks() )
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

}
