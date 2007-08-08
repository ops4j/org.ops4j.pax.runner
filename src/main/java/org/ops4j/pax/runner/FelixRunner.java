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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.ops4j.pax.runner.pom.BundleManager;
import org.xml.sax.SAXException;

public class FelixRunner
    implements Runnable
{

    private static final String GROUPID = "org.apache.felix";

    private static final String VERSION = "1.0.0";

    // have to escape \ for pattern compiler and again for javac
    private static final String ONE_BACKSLASH_REGEXP = "\\\\";
    private static final String TWO_BACKSLASH_REGEXP = "\\\\\\\\";

    private Properties m_props;
    private CmdLine m_cmdLine;
    private List m_sysBundles;
    private List m_appBundles;
    private String m_classpath;

    private File m_main;

    private static final String FRAMEWORK_PACKAGES = "org.osgi.framework; version=1.3.0, " +
                                                     "org.osgi.service.condpermadmin; version=1.0.0, " +
                                                     "org.osgi.service.packageadmin; version=1.2.0, " +
                                                     "org.osgi.service.permissionadmin; version=1.2.0, " +
                                                     "org.osgi.service.startlevel; version=1.0.0, " +
                                                     "org.osgi.service.url; version=1.0.0, " +
                                                     "org.osgi.util.tracker; version=1.3.1, ";

    public FelixRunner( CmdLine cmdLine, Properties props, List bundles, BundleManager bundleManager, String classpath )
        throws IOException, ParserConfigurationException, SAXException
    {
        m_cmdLine = cmdLine;
        m_props = props;

        m_appBundles = bundles;
        m_classpath = classpath;
        m_sysBundles = new ArrayList();
        File system1 = bundleManager.getBundle( GROUPID, "org.apache.felix.shell", VERSION );
        m_sysBundles.add( system1 );
        File system2 = bundleManager.getBundle( GROUPID, "org.apache.felix.shell.tui", VERSION );
        m_sysBundles.add( system2 );
        File system3 = bundleManager.getBundle( GROUPID, "org.apache.felix.bundlerepository", VERSION );
        m_sysBundles.add( system3 );
        if( m_cmdLine.isSet( "gui" ) )
        {
            File system4 = bundleManager.getBundle( GROUPID, "org.apache.felix.shell.gui", VERSION );
            m_sysBundles.add( system4 );
            File system5 = bundleManager.getBundle( GROUPID, "org.apache.felix.shell.gui.plugin", VERSION );
            m_sysBundles.add( system5 );
        }
        File system6 = bundleManager.getBundle( GROUPID, "org.osgi.compendium", VERSION );
        m_sysBundles.add( system6 );
        File system7 = bundleManager.getBundle( GROUPID, "javax.servlet", VERSION );
        m_sysBundles.add( system7 );
        m_main = bundleManager.getBundle( GROUPID, "org.apache.felix.main", VERSION );
    }

    public void run()
    {
        try
        {
            createConfigFile();
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

    private void createConfigFile()
        throws IOException
    {
        String startlevel = m_cmdLine.getValue( "startlevel" );
        String bundlelevel = m_cmdLine.getValue( "bundlelevel" );
        File confDir = new File( Run.WORK_DIR, "conf" );
        confDir.mkdirs();
        File file = new File( confDir, "config.properties" );
        Writer out = FileUtils.openPropertyFile( file );
        try
        {
            String packages = FileUtils.getSystemPackages( FRAMEWORK_PACKAGES, m_cmdLine );
            FileUtils.writeProperty( out, "org.osgi.framework.system.packages", packages );
            String profile = m_cmdLine.getValue( "profile" );
            if( profile != null )
            {
                FileUtils.writeProperty( out, "felix.cache.profile", profile );
            }

            // Need to quote windows separators in propertyfile otherwise they're interpreted as line continuations
            String quotedWorkDir = Run.WORK_DIR.getPath().replaceAll( ONE_BACKSLASH_REGEXP, TWO_BACKSLASH_REGEXP );

            FileUtils.writeProperty( out, "felix.cache.dir", quotedWorkDir + "/cache" );
            FileUtils.writeProperty( out, "felix.startlevel.framework", startlevel );
            FileUtils.writeProperty( out, "felix.startlevel.bundle", bundlelevel );
            FileUtils.writeProperty( out, "obr.repository.url", "http://www2.osgi.org/repository/repository.xml" );

            writeBundleList( out, "felix.auto.start.1", m_sysBundles );
            writeBundleList( out, "felix.auto.start." + bundlelevel, m_appBundles );

            for( Iterator i = m_props.entrySet().iterator(); i.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) i.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                FileUtils.writeProperty( out, key, value );
            }
            out.flush();
        } finally
        {
            out.close();
        }
    }

    private static void writeBundleList( Writer out, String startLevel, List bundles )
        throws IOException
    {
        boolean first = true;
        StringBuffer buf = new StringBuffer();
        for( Iterator i = bundles.iterator(); i.hasNext(); )
        {
            File bundle = (File) i.next();
            if( !first )
            {
                buf.append( " \\\n    " );
            }
            first = false;
            buf.append( bundle.toURI() );
        }
        FileUtils.writeProperty( out, startLevel, buf.toString() );
    }

    private void runIt()
        throws IOException, InterruptedException
    {
        String[] commands =
            {
                "-Dfelix.config.properties=" + Run.WORK_DIR.toURI() + "/conf/config.properties",
                "-cp",
                m_main.getAbsolutePath() + File.pathSeparator + m_classpath,
                "org.apache.felix.framework.Main"
            };
        //copy these two together
        Run.execute( commands );
    }

}
