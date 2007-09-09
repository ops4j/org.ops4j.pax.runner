/*
 * Copyright 2007 Alin Dreghiciu.
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
package org.ops4j.pax.runner.platform.internal;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.Info;

/**
 * Stream related utilities.
 * TODO add units tests
 *
 * @author Alin Dreghiciu
 * @since August 19, 2007
 */
public class StreamUtils
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( StreamUtils.class );

    /**
     * Utility class. Ment to be used via static methods.
     */
    private StreamUtils()
    {
        // utility class
    }

    /**
     * Copy a stream to a destination. It does not close the streams.
     *
     * @param in          the stream to copy from
     * @param out         the stream to copy to
     * @param displayName to be shown during download
     *
     * @throws IOException re-thrown
     */
    public static void streamCopy( final InputStream in, final BufferedOutputStream out, final String displayName )
        throws IOException
    {
        Assert.notNull( "Input stream", in );
        Assert.notNull( "Output stream", out );
        long start = System.currentTimeMillis();
        int b = in.read();
        int counter = 0;
        int bytes = 0;
        boolean printed = false;
        boolean infoEnabled = LOGGER.isInfoEnabled();
        while ( b != -1 )
        {
            out.write( b );
            b = in.read();
            counter = ( counter + 1 ) % 1024;
            if ( counter == 0 )
            {
                long time = System.currentTimeMillis() - start;
                if ( time <= 0 )
                {
                    time = 1;
                }
                long kbps = bytes / time;
                if ( displayName != null && infoEnabled )
                {
                    Info.print( displayName + " : " + bytes + " bytes @ [ " + kbps + "kBps ]\r" );
                }
                printed = true;
            }
            bytes++;
        }
        if ( displayName != null && infoEnabled )
        {
            if ( !printed )
            {
                Info.print( displayName + " : " + bytes + " bytes\r" );
            }
            Info.println();
        }
    }

    /**
     * Copy a stream from an urlto a destination.
     *
     * @param url         the url to copy from
     * @param out         the stream to copy to
     * @param displayName to be shown during download
     *
     * @throws IOException re-thrown
     */
    public static void streamCopy( final URL url, final BufferedOutputStream out, final String displayName )
        throws IOException
    {
        Assert.notNull( "URL", url );
        InputStream is = null;
        try
        {
            if ( LOGGER.isInfoEnabled() )
            {
                Info.print( displayName + " : connecting...\r" );
            }
            is = url.openStream();
            streamCopy( is, out, displayName );
        }
        finally
        {
            if ( is != null )
            {
                is.close();
            }
        }

    }

}
