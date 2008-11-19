/*
 * Copyright 2007 Alin Dreghiciu
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.platform.PlatformException;
import org.osgi.framework.Constants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Abstraction of an execution environment.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.16.1, November 18, 2008
 */
class ExecutionEnvironment
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( ExecutionEnvironment.class );
    /**
     * relative location of ee packages root.
     */
    private static final String EE_FILES_ROOT = "META-INF/platform/ee/";
    /**
     * Comma separated list of system packages.
     */
    private final String m_systemPackages;
    /**
     * Comma separated list of execution environments.
     */
    private final String m_executionEnvironment;

    /**
     * Constructor.
     *
     * @param ee comma separated list of execution environments names.
     *
     * @throws PlatformException - If encountered during reading of profiles
     *                           - If profile cannot be found or determined
     */
    ExecutionEnvironment( final String ee )
        throws PlatformException
    {
        // we make an union of the packages form each ee so let's have a unique set for it
        final Set<String> uniquePackages = new TreeSet<String>( );
        final Set<String> uniqueEE = new TreeSet<String>();

        for( String segment : ee.split( "," ) )
        {
            try
            {
                final Properties profile = new Properties();
                profile.load( discoverExecutionEnvironmentURL( segment ).openStream() );

                final String systemPackagesProp = profile.getProperty( Constants.FRAMEWORK_SYSTEMPACKAGES, "" );
                if( systemPackagesProp != null && systemPackagesProp.trim().length() > 0 )
                {
                    uniquePackages.addAll( Arrays.asList( systemPackagesProp.split( "," ) ) );
                }

                final String eeProp = profile.getProperty( Constants.FRAMEWORK_EXECUTIONENVIRONMENT, "" );
                if( eeProp != null && eeProp.trim().length() > 0 )
                {
                    uniqueEE.addAll( Arrays.asList( eeProp.split( "," ) ) );
                }
            }
            catch( IOException e )
            {
                throw new PlatformException( "Could not read execution environment profile", e );
            }
        }
        m_systemPackages = join( uniquePackages, "," );
        m_executionEnvironment = join( uniqueEE, "," );
    }

    /**
     * Returns teh system packages.
     *
     * @return a comma separated list of system packages combined from all execution environments.
     */
    public String getSystemPackages()
    {
        return m_systemPackages;
    }

    /**
     * Returns the execution environments.
     *
     * @return a comma separated list of execution environments combined from all execution environments.
     */
    public String getExecutionEnvironment()
    {
        return m_executionEnvironment;
    }

    /**
     * Returns the url of a file containing the execution environment packages (profile).
     *
     * @param ee execution environment name
     *
     * @return url of the file
     *
     * @throws PlatformException if execution environment could not be determined or found
     */
    private URL discoverExecutionEnvironmentURL( final String ee )
        throws PlatformException
    {
        URL url;
        final String relativeFileName = getEEMAppings().get( ee.toUpperCase() );
        if( relativeFileName != null )
        {
            url = this.getClass().getClassLoader().getResource( EE_FILES_ROOT + relativeFileName );
            if( url == null )
            {
                throw new PlatformException( "Execution environment [" + ee + "] not supported" );
            }
            LOGGER.info( "Using execution environment [" + ee + "]" );
        }
        else
        {
            try
            {
                url = new URL( ee );
                LOGGER.info( "Execution environment [" + url.toExternalForm() + "]" );
            }
            catch( MalformedURLException e )
            {
                throw new PlatformException( "Execution environment [" + ee + "] could not be found", e );
            }
        }
        return url;
    }

    /**
     * Returns a map between capitalized name of built in execution environments and profile name.
     *
     * @return a map between capitalized name of built in execution environments and profile name.
     */
    private static Map<String, String> getEEMAppings()
    {
        final Map<String, String> mappings = new HashMap<String, String>();

        mappings.put( "CDC-1.0/Foundation-1.0".toUpperCase(), "CDC-1.0/Foundation-1.0.profile" );
        mappings.put( "CDC-1.1/Foundation-1.1".toUpperCase(), "CDC-1.1/Foundation-1.1.profile" );
        mappings.put( "OSGi/Minimum-1.0".toUpperCase(), "OSGi/Minimum-1.0.profile" );
        mappings.put( "OSGi/Minimum-1.1".toUpperCase(), "OSGi/Minimum-1.1.profile" );
        mappings.put( "OSGi/Minimum-1.2".toUpperCase(), "OSGi/Minimum-1.2.profile" );
        mappings.put( "JRE-1.1".toUpperCase(), "JRE-1.1.profile" );
        mappings.put( "J2SE-1.2".toUpperCase(), "J2SE-1.2.profile" );
        mappings.put( "J2SE-1.3".toUpperCase(), "J2SE-1.3.profile" );
        mappings.put( "J2SE-1.4".toUpperCase(), "J2SE-1.4.profile" );
        mappings.put( "J2SE-1.5".toUpperCase(), "J2SE-1.5.profile" );
        mappings.put( "J2SE-1.6".toUpperCase(), "JavaSE-1.6.profile" );
        mappings.put( "JavaSE-1.6".toUpperCase(), "JavaSE-1.6.profile" );
        mappings.put( "PersonalJava-1.1".toUpperCase(), "PersonalJava-1.1.profile" );
        mappings.put( "PersonalJava-1.2".toUpperCase(), "PersonalJava-1.2.profile" );
        mappings.put( "CDC-1.0/PersonalBasis-1.0".toUpperCase(), "CDC-1.0/PersonalBasis-1.0.profile" );
        mappings.put( "CDC-1.0/PersonalJava-1.0".toUpperCase(), "CDC-1.0/PersonalJava-1.0.profile" );
        mappings.put( "NONE".toUpperCase(), "None.profile" );

        return mappings;
    }

    /**
     * Joins a collection of strings into one string using the delimiter
     *
     * @param toJoin    collection to be joined
     * @param delimiter join delimiter
     *
     * @return joined collection
     */
    public static String join( final Collection<String> toJoin,
                               final String delimiter )
    {
        final StringBuilder builder = new StringBuilder();
        for( String entry : toJoin )
        {
            if( builder.length() > 0 )
            {
                builder.append( delimiter );
            }
            builder.append( entry );
        }
        return builder.toString();
    }

}