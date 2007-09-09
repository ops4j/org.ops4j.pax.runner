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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.resolver.ConfigurationMap;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.ServiceConstants;

/**
 * Service Configuration implementation.
 *
 * @author Alin Dreghiciu
 * @see org.ops4j.pax.runner.platform.Configuration
 * @since August 25, 2007
 */
public class ConfigurationImpl
    extends ConfigurationMap
    implements Configuration
{

    /**
     * Default working directory.
     */
    private static final String DEFAULT_WORKING_DIRECTORY = "runner";
    /**
     * Default framework profile.
     */
    private static final String DEFAULT_FRAMEWORK_PROFILE = "runner";
    /**
     * Default platform start level.
     */
    private static final int DEFAULT_START_LEVEL = 6;
    /**
     * Default platform bundles start level.
     */
    private static final int DEFAULT_PROFILE_START_LEVEL = 1;
    /**
     * Default installed bundles start level.
     */
    private static final int DEFAULT_BUNDLE_START_LEVEL = 5;

    /**
     * Property resolver. Cannot be null.
     */
    private final Resolver m_resolver;

    /**
     * Creates a new service configuration.
     *
     * @param resolver resolver used to resolve properties; mandatory
     */
    public ConfigurationImpl( final Resolver resolver )
    {
        Assert.notNull( "Property resolver", resolver );
        m_resolver = resolver;
    }

    /**
     * @see Configuration#getDefinitionURL()
     */
    public URL getDefinitionURL()
        throws MalformedURLException
    {
        if ( !contains( ServiceConstants.CONFIG_DEFINITION_URL ) )
        {
            final String urlSpec = m_resolver.get( ServiceConstants.CONFIG_DEFINITION_URL );
            URL url = null;
            if ( urlSpec != null )
            {
                url = new URL( urlSpec );
            }
            return set( ServiceConstants.CONFIG_DEFINITION_URL, url );
        }
        return get( ServiceConstants.CONFIG_DEFINITION_URL );
    }

    /**
     * @see Configuration#getWorkingDirectory()
     */
    public String getWorkingDirectory()
    {
        if ( !contains( ServiceConstants.CONFIG_WORKING_DIRECTORY ) )
        {
            String workDir = m_resolver.get( ServiceConstants.CONFIG_WORKING_DIRECTORY );
            if ( workDir == null )
            {
                workDir = DEFAULT_WORKING_DIRECTORY;
            }
            return set( ServiceConstants.CONFIG_WORKING_DIRECTORY, workDir );
        }
        return get( ServiceConstants.CONFIG_WORKING_DIRECTORY );
    }

    /**
     * TODO add unit tests
     *
     * @see Configuration#getVMOptions()
     */
    public String getVMOptions()
    {
        // TODO unit test
        if ( !contains( ServiceConstants.CONFIG_VMOPTIONS ) )
        {
            return set( ServiceConstants.CONFIG_VMOPTIONS, m_resolver.get( ServiceConstants.CONFIG_VMOPTIONS ) );
        }
        return get( ServiceConstants.CONFIG_VMOPTIONS );
    }

    /**
     * TODO add unit tests
     *
     * @see Configuration#getClasspath()
     */
    public String getClasspath()
    {
        // TODO unit test
        if ( !contains( ServiceConstants.CONFIG_CLASSPATH ) )
        {
            String classpath = m_resolver.get( ServiceConstants.CONFIG_CLASSPATH );
            if ( classpath == null )
            {
                classpath = "";
            }
            else if ( !classpath.startsWith( File.pathSeparator ) )
            {
                classpath = File.pathSeparator + classpath;
            }
            return set( ServiceConstants.CONFIG_CLASSPATH, classpath );
        }
        return get( ServiceConstants.CONFIG_CLASSPATH );
    }

    /**
     * @see Configuration#getSystemPackages()
     */
    public String getSystemPackages()
    {
        if ( !contains( ServiceConstants.CONFIG_SYSTEM_PACKAGES ) )
        {
            return set(
                ServiceConstants.CONFIG_SYSTEM_PACKAGES,
                m_resolver.get( ServiceConstants.CONFIG_SYSTEM_PACKAGES )
            );
        }
        return get( ServiceConstants.CONFIG_SYSTEM_PACKAGES );
    }

    /**
     * @see Configuration#getExecutionEnvironment()
     */
    public String getExecutionEnvironment()
    {
        if ( !contains( ServiceConstants.CONFIG_EXECUTION_ENV ) )
        {
            String javaVersion = m_resolver.get( ServiceConstants.CONFIG_EXECUTION_ENV );
            if ( javaVersion == null )
            {
                javaVersion = "J2SE-" + System.getProperty( "java.version" ).substring( 0, 3 );
            }
            return set( ServiceConstants.CONFIG_EXECUTION_ENV, javaVersion );
        }
        return get( ServiceConstants.CONFIG_EXECUTION_ENV );
    }

    /**
     * @see Configuration#getJavaHome()
     */
    public String getJavaHome()
    {
        if ( !contains( ServiceConstants.CONFIG_JAVA_HOME ) )
        {
            String javaHome = m_resolver.get( ServiceConstants.CONFIG_JAVA_HOME );
            if ( javaHome == null )
            {
                javaHome = System.getProperty( "JAVA_HOME" );
                if ( javaHome == null )
                {
                    try
                    {
                        javaHome = System.getenv( "JAVA_HOME" );
                    }
                    catch ( Error e )
                    {
                        // fallback when running under Java 1.4.x
                        javaHome = System.getProperty( "java.home" );
                    }
                }
            }
            return set( ServiceConstants.CONFIG_JAVA_HOME, javaHome );
        }
        return get( ServiceConstants.CONFIG_JAVA_HOME );
    }

    /**
     * @see Configuration#shouldClean()
     */
    public Boolean shouldClean()
    {
        if ( !contains( ServiceConstants.CONFIG_CLEAN ) )
        {
            return set( ServiceConstants.CONFIG_CLEAN,
                        Boolean.valueOf( m_resolver.get( ServiceConstants.CONFIG_CLEAN ) )
            );
        }
        return get( ServiceConstants.CONFIG_CLEAN );
    }

    /**
     * @see Configuration#startConsole()
     */
    public Boolean startConsole()
    {
        if ( !contains( ServiceConstants.CONFIG_CONSOLE ) )
        {
            String console = m_resolver.get( ServiceConstants.CONFIG_CONSOLE );
            if ( console == null )
            {
                return set( ServiceConstants.CONFIG_CONSOLE, Boolean.TRUE );
            }
            return set( ServiceConstants.CONFIG_CONSOLE, Boolean.valueOf( console ) );
        }
        return get( ServiceConstants.CONFIG_CONSOLE );
    }

    /**
     * @see Configuration#isOverwrite()
     */
    public Boolean isOverwrite()
    {
        if ( !contains( ServiceConstants.CONFIG_OVERWRITE ) )
        {
            return set( ServiceConstants.CONFIG_OVERWRITE,
                        Boolean.valueOf( m_resolver.get( ServiceConstants.CONFIG_OVERWRITE ) )
            );
        }
        return get( ServiceConstants.CONFIG_OVERWRITE );
    }

    /**
     * @see Configuration#getProfiles()
     */
    public String getProfiles()
    {
        if ( !contains( ServiceConstants.CONFIG_PROFILES ) )
        {
            return set( ServiceConstants.CONFIG_PROFILES, m_resolver.get( ServiceConstants.CONFIG_PROFILES ) );
        }
        return get( ServiceConstants.CONFIG_PROFILES );
    }

    /**
     * @see Configuration#getFrameworkProfile()
     */
    public String getFrameworkProfile()
    {
        if ( !contains( ServiceConstants.CONFIG_FRAMEWORK_PROFILE ) )
        {
            String profile = m_resolver.get( ServiceConstants.CONFIG_FRAMEWORK_PROFILE );
            if ( profile == null )
            {
                profile = DEFAULT_FRAMEWORK_PROFILE;
            }
            return set( ServiceConstants.CONFIG_FRAMEWORK_PROFILE, profile );
        }
        return get( ServiceConstants.CONFIG_FRAMEWORK_PROFILE );
    }

    /**
     * @see Configuration#getProfileStartLevel()
     */
    public Integer getProfileStartLevel()
    {
        return resolveStartLevel( ServiceConstants.CONFIG_PROFILE_START_LEVEL, DEFAULT_PROFILE_START_LEVEL );
    }

    /**
     * @see Configuration#getStartLevel()
     */
    public Integer getStartLevel()
    {
        return resolveStartLevel( ServiceConstants.CONFIG_START_LEVEL, DEFAULT_START_LEVEL );
    }

    /**
     * @see Configuration#getBundleStartLevel()
     */
    public Integer getBundleStartLevel()
    {
        return resolveStartLevel( ServiceConstants.CONFIG_BUNDLE_START_LEVEL, DEFAULT_BUNDLE_START_LEVEL );
    }

    /**
     * Common configuration resolver for start level related configurations.
     *
     * @param optionName   name of the option
     * @param defaultValue default value if the start level option is not set
     *
     * @return the configured value
     */
    private Integer resolveStartLevel( final String optionName, final Integer defaultValue )
    {
        if ( !contains( optionName ) )
        {
            String startLevel = m_resolver.get( optionName );
            Integer startLevelAsInt = defaultValue;
            if ( startLevel != null )
            {
                try
                {
                    startLevelAsInt = Integer.valueOf( startLevel );
                }
                catch ( NumberFormatException ignore )
                {
                    // ignore and use default value
                }
            }
            return set( optionName, startLevelAsInt );
        }
        return get( optionName );
    }

}
