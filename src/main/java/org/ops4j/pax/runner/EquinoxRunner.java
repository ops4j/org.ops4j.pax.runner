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
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.ops4j.pax.runner.pom.BundleManager;
import org.xml.sax.SAXException;

public class EquinoxRunner
    implements Runnable
{

    private Properties m_props;
    private CmdLine m_cmdLine;
    private List m_bundles;
    private List m_defaultBundles;
    private File m_system;

    public EquinoxRunner( CmdLine cmdLine, Properties props, List bundles, BundleManager bundleManager )
        throws IOException, ParserConfigurationException, SAXException
    {
        m_cmdLine = cmdLine;
        m_bundles = bundles;
        m_props = props;
        EquinoxConfigurator configurator = new EquinoxConfigurator();
        configurator.load(Run.WORK_DIR);
        m_system = configurator.getSystemBundle(bundleManager);
        m_defaultBundles = configurator.getDefaultBundles(bundleManager);
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
        String bundlelevel = m_cmdLine.getValue( "bundlelevel" );
        String startlevel = m_cmdLine.getValue( "startlevel" );
        File confDir = new File( Run.WORK_DIR, "configuration" );
        confDir.mkdirs();
        File file = new File( confDir, "config.ini" );
        Writer out = FileUtils.openPropertyFile( file );
        try
        {
            boolean clean = m_cmdLine.isSet( "clean" );
            if( clean )
            {
                out.write( "\nosgi.clean=true\n" );
            }
            out.write( "\neclipse.ignoreApp=true\n" );
            out.write( "\nosgi.startLevel=" + startlevel + "\n" );
            out.write( "\nosgi.bundles=\\\n" );
            writeBundles( m_defaultBundles, out, "1", true );
            writeBundles( m_bundles, out, bundlelevel, false );
            out.write( '\n' );
            out.write( '\n' );
            for( Iterator i = m_props.entrySet().iterator(); i.hasNext(); )
            {
                Map.Entry entry = (Map.Entry)i.next();
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

    private void writeBundles( List bundles, Writer out, String bundlelevel, boolean first )
        throws IOException
    {
        for( Iterator i = bundles.iterator(); i.hasNext(); )
        {
            File bundle = (File)i.next();
            if( !first )
            {
                out.write( ",\\\n" );
            }
            first = false;
            String urlString = bundle.toURL().toString();
            out.write( "reference:" );
            out.write( urlString );
            out.write( "@" + bundlelevel + ":start" );
        }
    }

    private void runIt()
        throws IOException, InterruptedException
    {
        //framework specific args
        String[] commands =
            {
                "-jar",
                m_system.toString(),
                "-console",
                "-configuration",
                Run.WORK_DIR + "/configuration",
                "-install",
                Run.WORK_DIR.getAbsolutePath()
            };

        if( m_cmdLine.isSet( "no-console" ) )
        {
            commands[2] = "";
        }

        Run.execute( commands );
    }
}
