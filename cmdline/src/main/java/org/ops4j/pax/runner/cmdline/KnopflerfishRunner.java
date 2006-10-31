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
package org.ops4j.pax.runner.cmdline;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.ops4j.pax.runner.Runner;
import org.ops4j.pax.runner.RunnerOptions;
import org.ops4j.pax.runner.exec.KnopflerfishPreparer;
import org.ops4j.pax.common.Pipe;

public class KnopflerfishRunner
    implements Runner
{

    private KnopflerfishPreparer m_preparer;

    public KnopflerfishRunner( KnopflerfishPreparer preparer )
    {
        m_preparer = preparer;
    }

    public void execute( RunnerOptions options )
        throws Exception
    {
        m_preparer.prepareForRun( options );
        runIt( options );
    }

    protected void runIt( RunnerOptions options )
        throws IOException, InterruptedException
    {
        Runtime runtime = Runtime.getRuntime();

        String javaHome = System.getProperty( "java.home" );
        if( javaHome == null )
        {
            javaHome = System.getenv().get( "JAVA_HOME" );
        }
        if( javaHome == null )
        {
            System.err.println( "JAVA_HOME is not set." );
        }
        else
        {
            String[] cmd =
                {
                    javaHome + "/bin/java",
                    "-Dorg.knopflerfish.framework.usingwrapperscript=false",
                    "-Dorg.knopflerfish.framework.exitonshutdown=true",
                    "-jar",
                    options.getSystemBundle().toString()
                };
            Process process = runtime.exec( cmd, null, options.getWorkDir() );
            InputStream err = process.getErrorStream();
            InputStream out = process.getInputStream();
            OutputStream in = process.getOutputStream();
            Pipe errPipe = new Pipe( err, System.err );
            errPipe.start();
            Pipe outPipe = new Pipe( out, System.out );
            outPipe.start();
            Pipe inPipe = new Pipe( System.in, in );
            inPipe.start();
            process.waitFor();
            inPipe.stop();
            outPipe.stop();
            errPipe.stop();
        }
    }

}
