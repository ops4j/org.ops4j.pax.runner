/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.runner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.ops4j.pax.runner.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CmdLine
{

    private Map m_values;
    private Map m_platforms;

    public CmdLine( String[] args )
    {
        Map shortcuts = new HashMap();
        shortcuts.put( "p", "platform" );
        shortcuts.put( "r", "repository" );
        shortcuts.put( "l", "localRepository" );
        shortcuts.put( "s", "startlevel" );
        shortcuts.put( "b", "bundlelevel" );
        m_platforms = new HashMap();
        m_platforms.put( "e", "equinox" );
        m_platforms.put( "eq", "equinox" );
        m_platforms.put( "f", "felix" );
        m_platforms.put( "k", "knopflerfish" );
        m_platforms.put( "kf", "knopflerfish" );
        String[] nonOpted = new String[3];
        int count = 0;
        m_values = new HashMap();
        populateDefaults();
        for( int i = 0; i < args.length; i++ )
        {
            String arg = args[ i ];

            if( arg.startsWith( "--" ) )
            {
                parseOption( arg );
            }
            else if( arg.startsWith( "-" ) )
            {
                String lookup = arg.substring( 1 );
                arg = "--" + shortcuts.get( lookup ) + "=" + args[ i + 1 ];
                i++;
                parseOption( arg );
            }
            else
            {
                nonOpted[ count ] = arg;
                count = count + 1;
                if( count > 3 )
                {
                    throw new IllegalArgumentException( "Maximum 3 commandline arguments." );
                }
            }
        }
        if( count == 0 )
        {
            m_values.put( "group", "org.ops4j.pax.wicket.samples.departmentstore" );
            m_values.put( "artifact", "demo" );
            m_values.put( "version", "LATEST" );
        }
        else if( count == 1 )
        {
            try
            {
                URL url = new URL( nonOpted[ 0 ] );
                m_values.put( "url", url.toExternalForm() );
            } catch( MalformedURLException e )
            {
                m_values.put( "artifact", nonOpted[ 0 ] );
                m_values.put( "version", "LATEST" );
            }

        }
        else if( count == 2 )
        {
            m_values.put( "artifact", nonOpted[ 0 ] );
            m_values.put( "version", nonOpted[ 1 ] );
        }
        else
        {
            m_values.put( "group", nonOpted[ 0 ] );
            m_values.put( "artifact", nonOpted[ 1 ] );
            m_values.put( "version", nonOpted[ 2 ] );
        }
    }

    private void populateDefaults()
    {
        m_values.put( "platform", "equinox" );
        m_values.put( "startlevel", "6" );
        m_values.put( "bundlelevel", "5" );
        m_values.put( "dir", System.getProperty( "user.dir" ) + "/runner" );
        m_values.put( "group", "org.ops4j.pax.apps" );
        m_values.put( "repository", "http://repository.ops4j.org/maven2/" );
        m_values.put( "localRepository", getLocalRepository() );
        m_values.put( "proxy-username", System.getProperty( "user.name" ) );
        m_values.put( "proxy-password", "" );
        m_values.put( "repository-username", System.getProperty( "user.name" ) );
        m_values.put( "repository-password", "" );
        m_values.put( "vmopts", "" );
    }

    private static String getLocalRepository()
    {
        String localRepository = getLocalRepositoryFromSettings( System.getProperty( "user.home" ) + "/.m2/settings.xml" );
        if ( localRepository == null )
        {
            localRepository = getLocalRepositoryFromSettings( System.getProperty( "maven.home" ) + "/conf/settings.xml" );
            if ( localRepository == null )
            {
                localRepository = getLocalRepositoryFromSettings( System.getenv( "M2_HOME" ) + "/conf/settings.xml" );
                if ( localRepository == null )
                {
                    localRepository = System.getProperty( "user.home" ) + "/.m2/repository";
                }
            }
        }
        return localRepository;
    }

    private static String getLocalRepositoryFromSettings( final String settingsFileName )
    {
        File settingsFile = new File( settingsFileName );
        if ( settingsFile.exists() && settingsFile.isFile() )
        {
            try
            {
                Document settingsDoc = XmlUtils.parseDoc( settingsFile );
                Element settingsElement = XmlUtils.getElement( settingsDoc, "localRepository" );
                if ( settingsElement != null )
                {
                    return settingsElement.getTextContent();
                }

            } catch( Exception e )
            {
                // just fallback
                return null;
            }
        }
        return null;
    }

    public String getValue( String key )
    {
        return (String)m_values.get( key );
    }

    public boolean isSet( String key )
    {
        return m_values.containsKey( key );
    }

    private void parseOption( String arg )
    {
        String key = arg.substring( 2 );
        int equalPos = key.indexOf( '=' );
        if( equalPos <= 0 )
        {
            m_values.put( key, key );
            return;
        }
        String value = key.substring( equalPos + 1 );
        key = key.substring( 0, equalPos );
        if( "platform".equals( key ) )
        {
            if ( "choose".equalsIgnoreCase( value.trim() ) )
            {
                value = choosePlatform();
            }
            String platform = (String)m_platforms.get( value );
            if( platform != null )
            {
                value = platform;
            }
        }
        m_values.put( key, value );
    }

    private static String choosePlatform()
    {
        System.out.print( "OSGi platform ( equinox|e|eq / knopflerfish|k|kf / felix|f ) ? " );
        BufferedReader stdin = new BufferedReader( new InputStreamReader( System.in ) );
        try
        {
            return stdin.readLine();
        } catch( IOException ignore )
        {
            return null;
        }
    }
}
