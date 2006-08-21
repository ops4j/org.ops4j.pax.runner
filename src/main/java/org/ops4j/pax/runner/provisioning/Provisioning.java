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
package org.ops4j.pax.runner.provisioning;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.ops4j.pax.runner.CmdLine;
import org.ops4j.pax.runner.Downloader;
import org.ops4j.pax.runner.Run;
import org.ops4j.pax.runner.StreamUtils;

/* WARNING!!! This is not correct code, and should be deleted. Kept as reference for future
              Initial Provisioning bundle to commence later. */
public class Provisioning
{
    private Downloader m_downloader;

    public Provisioning( Downloader downloader )
    {
        m_downloader = downloader;
    }

    public List<File> getBundles( CmdLine m_cmdLine )
        throws IOException
    {
        URL zipURL = new URL( m_cmdLine.getValue( "url" ) );
        File destination = new File( Run.WORK_DIR, "initial-provisioning.zip" );
        m_downloader.download( zipURL, destination, false );
        ZipFile zipFile = new ZipFile( destination );
        Dictionary provisioningDictionary = new Hashtable();
        extractEntries( zipFile, provisioningDictionary );
        return null;
    }

    private void extractEntries( ZipFile zipFile, Dictionary provisioningDictionary )
        throws IOException
    {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while( entries.hasMoreElements() )
        {
            ZipEntry entry = entries.nextElement();
            byte[] extraField = entry.getExtra();
            if( extraField == null )
            {
                throw new IOException( "Not a valid Initial Provisioning ZIP file. Missing EXTRA field." );
            }
            String type = new String( extraField );
            if( type.startsWith( "text/plain" ) ) // text
            {
                String text = readText( zipFile, entry );
                provisioningDictionary.put( entry.getName(), text );
            }
            else if( "application/octet-stream".equals( type ) ) // binary
            {
                byte[] bytes = readBinary( zipFile, entry );
                provisioningDictionary.put( entry.getName(), bytes );
            }
            else if( "application/x-osgi-bundle".equals( type ) ) // bundle
            {
                byte[] bytes = readBinary( zipFile, entry );
                provisioningDictionary.put( entry.getName(), bytes );

            }
            else if( type.startsWith( "text/x-osgi-bundle-url" ) ) // bundle-url
            {
                String text = readText( zipFile, entry );
                provisioningDictionary.put( entry.getName(), text );
            }
            else // invalid
            {
                throw new IOException( "Not a valid Initial Provisioning ZIP file. Invalid Type: " + type );
            }
        }
    }

    private byte[] readBinary( ZipFile zipFile, ZipEntry entry )
        throws IOException
    {
        InputStream in = zipFile.getInputStream( entry );
        ByteArrayOutputStream mem = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream( mem );
        StreamUtils.streamCopy( in, out, null );
        out.flush();
        out.close();
        in.close();
        return mem.toByteArray();
    }

    private String readText( ZipFile zipFile, ZipEntry entry )
        throws IOException
    {
        BufferedReader br = null;
        try
        {
            InputStream in = zipFile.getInputStream( entry );
            InputStreamReader isr = new InputStreamReader( in, "UTF-8" );
            br = new BufferedReader( isr );
        } finally
        {
            if( br != null )
            {
                br.close();
            }
        }
        String result = br.readLine();
        br.close();
        return result;
    }

    public Properties getProperties( CmdLine m_cmdLine )
    {
        return new Properties();
    }
}
