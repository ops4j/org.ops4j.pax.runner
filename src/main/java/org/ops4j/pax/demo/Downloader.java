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
package org.ops4j.pax.demo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedOutputStream;
import java.net.URL;

public class Downloader
{

    void download( URL source, File destination, boolean force )
        throws IOException
    {
        if( destination.exists() && ! force )
        {
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

    private String composeFileName( URL url )
    {
        String path = url.getPath();
        int slashPos = path.lastIndexOf( '/' );
        path = path.substring( slashPos +  1 );
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
            counter = (counter + 1) % 1024;
            if( counter == 0 )
            {
                long time = System.currentTimeMillis() - start;
                long kbps = bytes / time;
                System.out.print( title + " : " + bytes + " bytes @ [ " + kbps + "kBps ]\r" );
            }
            bytes++;
        }
        System.out.println();
    }
}
