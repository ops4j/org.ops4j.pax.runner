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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils
{

    public static void streamCopy( InputStream in, BufferedOutputStream out, String title )
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
                if( title != null )
                {
                    System.out.print( title + " : " + bytes + " bytes @ [ " + kbps + "kBps ]\r" );
                }
                printed = true;
            }
            bytes++;
        }
        if( title != null )
        {
            if( !printed )
            {
                System.out.print( title + " : " + bytes + " bytes\r" );
            }
            System.out.println();
        }
    }
}
