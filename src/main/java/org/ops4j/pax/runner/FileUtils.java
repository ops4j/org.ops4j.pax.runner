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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FileUtils
{
    static Writer openPropertyFile( File file )
        throws IOException
    {
        FileOutputStream fos = new FileOutputStream( file );
        OutputStreamWriter oos = new OutputStreamWriter( fos, "ISO-8859-1" );
        BufferedWriter out = new BufferedWriter( oos );
        return out;
    }

    static void writeProperty( Writer out, String key, String value )
        throws IOException
    {
        out.write( key );
        out.write( '=' );
        out.write( value );
        out.write( "\n\n" );
    }
}
