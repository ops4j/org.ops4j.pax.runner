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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;

public class EquinoxRunner
    implements Runnable
{
    private Properties m_props;
    private Downloader m_downloader;
    private Properties m_appProperties;
    private boolean m_clean;
    private int m_startLevel;
    private Set<File> m_bundles;

    public EquinoxRunner( Properties props )
    {
        m_props = props;
        m_downloader = new Downloader();
        m_clean = true;
        m_startLevel = 7;
    }

    public void run()
    {
        try
        {
            System.out.println( "    ______  ________  __  __" );
            System.out.println( "   / __  / /  __   / / / / /" );
            System.out.println( "  /  ___/ /  __   / _\\ \\ _/" );
            System.out.println( " /  /    /  / /  / / _\\ \\" );
            System.out.println( "/__/    /__/ /__/ /_/ /_/" );
            System.out.println();
            System.out.println( "Pax Demo Runner from OPS4J - http://www.ops4j.org" );
            System.out.println( "-------------------------------------------------" );
            System.out.println();
            System.out.println( "   Starting: " + m_props.get( "name" ));
            System.out.println( "Description: " + m_props.get( "description" ) );

            m_appProperties = getProperties();
            m_bundles = getBundles();
            createConfigIniFile();
            runTheDemo();
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

    private Set getBundles()
        throws IOException
    {
        Set bundles = new HashSet();
        for( Map.Entry entry : m_props.entrySet() )
        {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if( key.startsWith( "bundle" ) )
            {
                URL url = composeURL( value );
                File dest = new File( "lib/", composeFileName( url ) );
                m_downloader.download( url, dest, false );
                bundles.add( dest );
            }
        }
        return bundles;
    }

    private String composeFileName( URL url )
    {
        String path = url.getPath();
        int slashPos = path.lastIndexOf( '/' );
        path = path.substring( slashPos +  1 );
        return path;
    }

    private URL composeURL( String value )
        throws MalformedURLException
    {
        StringTokenizer st = new StringTokenizer( value, ",", false );
        String group = st.nextToken().replace( '.', '/' ).trim();
        String name = st.nextToken().trim();
        String version = st.nextToken().trim();
        String filename = name + "-" + version + ".jar";
        String url = "http://repository.ops4j.org/maven2/" + group + "/" + name + "/" + version + "/" + filename;
        return new URL( url );
    }

    private Properties getProperties()
    {
        Properties props = new Properties();
        for( Map.Entry entry : m_props.entrySet() )
        {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if( key.startsWith( "property" ) )
            {
                key = key.substring( 9 ); // skip "property."
                props.put( key, value );
            }
        }
        return props;
    }

    private void createConfigIniFile()
        throws IOException
    {
        System.out.println( "Creating configuration file." );
        File confDir = new File( "configuration" );
        confDir.mkdirs();
        File file = new File( confDir, "config.ini" );
        FileOutputStream fos = new FileOutputStream( file );
        try
        {
            Properties props = new Properties();
            props.put( "osgi.clean", Boolean.toString( m_clean ) );
            props.put( "osgi.console", "5000" );
            props.put( "osgi.noShutdown", "true" );
            props.put( "osgi.startLevel", Integer.toString( m_startLevel ) );
            props.put( "eclipse.ignoreApp", "true" );
            props.putAll( m_appProperties );
            StringBuffer buf = new StringBuffer();
            boolean first = true;
            for( File bundle : m_bundles )
            {
                if( ! first )
                {
                    buf.append( ',' );
                }
                buf.append( constructBundleName( bundle ) );
                buf.append( "@5:start" );
            }
            props.put( "osgi.bundles", buf.toString() );
            props.store( fos, "Equinox Configuration File. DO NOT EDIT!" );
        } finally
        {
            if( fos != null )
            {
                fos.close();
            }
        }
    }

    private String constructBundleName( File bundle )
    {
        String name = bundle.getName();
        int lastScore = name.lastIndexOf( '_' );
        if( lastScore >= 0 )
        {
            return name.substring( 0, lastScore );
        }
        int lastDash = name.lastIndexOf( '-' );
        if( lastDash >= 0 )
        {
            if( "-SNAPSHOT".equals( name.substring( lastDash ) ) )
            {
                lastDash = name.lastIndexOf( '-', lastDash - 1 );
            }
            return name.substring( 0, lastDash );
        }
        return null;
    }

    private void runTheDemo()
        throws IOException, InterruptedException
    {
        Runtime runtime = Runtime.getRuntime();

        File equinoxJar = locateEquinox( new File( "lib/" ) );
        if( equinoxJar == null )
        {
            System.out.println( "Equinox bundle was not part of the Demo. This is a requirement." );
            System.exit( 1 );
        }
        File cwd = new File( System.getProperty( "user.dir" ) );
        String[] cmd = { "java", "-jar", equinoxJar.toString() };
        Process process = runtime.exec( cmd, null, cwd );
        process.waitFor();
    }

    private File locateEquinox( File dir )
    {
        File[] list = dir.listFiles();
        for( File file : list )
        {
            if( file.getName().startsWith( "org.eclipse.osgi_" ) )
            {
                return file;
            }
        }
        return null;
    }
}
