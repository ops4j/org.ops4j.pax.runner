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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.ops4j.pax.runner.pom.BundleManager;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

public class EquinoxRunner
    implements Runnable
{
    private Properties m_props;
    private CmdLine m_cmdLine;
    private List<File> m_bundles;
    private File m_system;

    public EquinoxRunner( CmdLine cmdLine, Properties props, List<File> bundles, BundleManager bundleManager )
        throws IOException, ParserConfigurationException, SAXException
    {
        m_cmdLine = cmdLine;
        m_bundles = bundles;
        m_props = props;
        m_system = bundleManager.getBundle( "org.eclipse", "osgi", "3.2.1.R32x_v20060717" );
        File services = bundleManager.getBundle( "org.eclipse.osgi", "services", "3.1.100.v20060601" );
        bundles.add( services );
        File util = bundleManager.getBundle( "org.eclipse.osgi", "util", "3.1.100.v20060601" );
        bundles.add(  util );
    }

    public void run()
    {
        try
        {
            createConfigIniFile();
            runIt();
        } catch( MalformedURLException e )
        {
            e.printStackTrace();
        } catch( IOException e )
        {
            e.printStackTrace();
        } catch( InterruptedException e )
        {
            e.printStackTrace();
        }
    }

    private void createConfigIniFile()
        throws IOException
    {
        File confDir = new File( Run.WORK_DIR, "configuration" );
        confDir.mkdirs();
        File file = new File( confDir, "config.ini" );
        Writer out = FileUtils.openPropertyFile( file );
        try
        {
            boolean first = true;
            boolean clean = m_cmdLine.isSet( "clean" );
            if( clean )
            {
                out.write( "\nosgi.clean=true\n" );
            }
            out.write( "\neclipse.ignoreApp=true\n" );
            out.write( "\nosgi.bundles=\\\n" );
            for( File bundle : m_bundles )
            {
                if( ! first )
                {
                    out.write( ",\\\n" );
                }
                first = false;
                String urlString = bundle.toURL().toString();
                out.write( "reference:" );
                out.write( urlString );
                out.write( "@5:start" );
            }
            out.write( '\n' );
            out.write( '\n' );
            for( Map.Entry entry : m_props.entrySet() )
            {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                out.write( key );
                out.write( "=" );
                out.write( value );
                out.write( "\n\n" );
            }
            out.flush();
        } finally
        {
            if( out != null )
            {
                out.close();
            }
        }
    }

    private void runIt()
        throws IOException, InterruptedException
    {
        Runtime runtime = Runtime.getRuntime();

        File cwd = new File( System.getProperty( "user.dir" ) );
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
                    "-jar",
                    m_system.toString(),
                    "-console",
                    "-configuration",
                    Run.WORK_DIR + "/configuration",
                    "-install",
                    Run.WORK_DIR.getAbsolutePath()
                };
            Process process = runtime.exec( cmd, null, cwd );
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
