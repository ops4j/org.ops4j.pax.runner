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
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.ServiceConstants;
import org.ops4j.util.property.PropertyResolver;
import org.ops4j.util.property.PropertyStore;

/**
 * Service Configuration implementation.
 *
 * @author Alin Dreghiciu
 * @see Configuration
 * @since August 25, 2007
 */
public class ConfigurationImpl
    extends PropertyStore
    implements Configuration
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( ConfigurationImpl.class );
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
    private final PropertyResolver m_propertyResolver;

    /**
     * Creates a new service configuration.
     *
     * @param propertyResolver propertyResolver used to resolve properties; mandatory
     */
    public ConfigurationImpl( final PropertyResolver propertyResolver )
    {
        NullArgumentException.validateNotNull( propertyResolver, "Property resolver" );
        m_propertyResolver = propertyResolver;
    }

    /**
     * @see Configuration#getDefinitionURL()
     */
    public URL getDefinitionURL()
        throws MalformedURLException
    {
        if( !contains( ServiceConstants.CONFIG_DEFINITION_URL ) )
        {
            final String urlSpec = m_propertyResolver.get( ServiceConstants.CONFIG_DEFINITION_URL );
            URL url = null;
            if( urlSpec != null )
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
        if( !contains( ServiceConstants.CONFIG_WORKING_DIRECTORY ) )
        {
            String workDir = m_propertyResolver.get( ServiceConstants.CONFIG_WORKING_DIRECTORY );
            if( workDir == null )
            {
                workDir = DEFAULT_WORKING_DIRECTORY;
            }
            return set( ServiceConstants.CONFIG_WORKING_DIRECTORY, workDir );
        }
        return get( ServiceConstants.CONFIG_WORKING_DIRECTORY );
    }

    /**
     * @see Configuration#getVMOptions()
     */
    public String[] getVMOptions()
    {
        // TODO unit test
        if( !contains( ServiceConstants.CONFIG_VMOPTIONS ) )
        {
            final String vmOptions = m_propertyResolver.get( ServiceConstants.CONFIG_VMOPTIONS );
            if( vmOptions != null )
            {
                if( vmOptions.contains( "-D" + Constants.FRAMEWORK_BOOTDELEGATION + "=" ) )
                {
                    LOGGER.warn(
                        "WARNING!: Setting boot delegation packages should be done via --bootDelegation/bd option"
                        + " not by using " + Constants.FRAMEWORK_BOOTDELEGATION + " system variable"
                    );
                }
                if( vmOptions.contains( "-cp" ) )
                {
                    LOGGER.warn(
                        "WARNING!: Setting class path should be done via --classpath/cp option"
                        + " not by using -cp as vm option"
                    );
                }
                final Collection<String> options = new ArrayList<String>();
                for( String option : vmOptions.split( " " ) )
                {
                    if( option.trim().length() > 0 )
                    {
                        options.add( option );
                    }
                }
                if( options.size() > 0 )
                {
                    return set( ServiceConstants.CONFIG_VMOPTIONS, options.toArray( new String[options.size()] ) );
                }
                else
                {
                    return set( ServiceConstants.CONFIG_VMOPTIONS, null );
                }
            }
            else
            {
                return set( ServiceConstants.CONFIG_VMOPTIONS, null );
            }

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
        if( !contains( ServiceConstants.CONFIG_CLASSPATH ) )
        {
            String classpath = m_propertyResolver.get( ServiceConstants.CONFIG_CLASSPATH );
            if( classpath == null )
            {
                classpath = "";
            }
            else if( !classpath.startsWith( File.pathSeparator ) )
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
        if( !contains( ServiceConstants.CONFIG_SYSTEM_PACKAGES ) )
        {
            return set(
                ServiceConstants.CONFIG_SYSTEM_PACKAGES,
                m_propertyResolver.get( ServiceConstants.CONFIG_SYSTEM_PACKAGES )
            );
        }
        return get( ServiceConstants.CONFIG_SYSTEM_PACKAGES );
    }

    /**
     * @see Configuration#getExecutionEnvironment()
     */
    public String getExecutionEnvironment()
    {
        if( !contains( ServiceConstants.CONFIG_EXECUTION_ENV ) )
        {
            String javaVersion = m_propertyResolver.get( ServiceConstants.CONFIG_EXECUTION_ENV );
            if( javaVersion == null )
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
        if( !contains( ServiceConstants.CONFIG_JAVA_HOME ) )
        {
            String javaHome = m_propertyResolver.get( ServiceConstants.CONFIG_JAVA_HOME );
            if( javaHome == null )
            {
                javaHome = System.getProperty( "JAVA_HOME" );
                if( javaHome == null )
                {
                    try
                    {
                        javaHome = System.getenv( "JAVA_HOME" );
                    }
                    catch( Error e )
                    {
                        // should only happen under Java 1.4.x as this method does not exist
                    }
                    if( javaHome == null )
                    {
                        javaHome = System.getProperty( "java.home" );
                    }
                }
            }
            return set( ServiceConstants.CONFIG_JAVA_HOME, javaHome );
        }
        return get( ServiceConstants.CONFIG_JAVA_HOME );
    }

    /**
     * @see Configuration#usePersistedState()
     */
    public Boolean usePersistedState()
    {
        if( !contains( ServiceConstants.CONFIG_USE_PERSISTED_STATE ) )
        {
            String usePersistedState = m_propertyResolver.get( ServiceConstants.CONFIG_USE_PERSISTED_STATE );
            if( usePersistedState == null )
            {
                return set( ServiceConstants.CONFIG_USE_PERSISTED_STATE, Boolean.FALSE );
            }
            return set( ServiceConstants.CONFIG_USE_PERSISTED_STATE, Boolean.valueOf( usePersistedState )
            );
        }
        return get( ServiceConstants.CONFIG_USE_PERSISTED_STATE );
    }

    /**
     * @see Configuration#startConsole()
     */
    public Boolean startConsole()
    {
        if( !contains( ServiceConstants.CONFIG_CONSOLE ) )
        {
            String console = m_propertyResolver.get( ServiceConstants.CONFIG_CONSOLE );
            if( console == null )
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
        if( !contains( ServiceConstants.CONFIG_OVERWRITE ) )
        {
            return set( ServiceConstants.CONFIG_OVERWRITE,
                        Boolean.valueOf( m_propertyResolver.get( ServiceConstants.CONFIG_OVERWRITE ) )
            );
        }
        return get( ServiceConstants.CONFIG_OVERWRITE );
    }

    /**
     * @see Configuration#getProfiles()
     */
    public String getProfiles()
    {
        if( !contains( ServiceConstants.CONFIG_PROFILES ) )
        {
            return set( ServiceConstants.CONFIG_PROFILES, m_propertyResolver.get( ServiceConstants.CONFIG_PROFILES ) );
        }
        return get( ServiceConstants.CONFIG_PROFILES );
    }

    /**
     * @see Configuration#getFrameworkProfile()
     */
    public String getFrameworkProfile()
    {
        if( !contains( ServiceConstants.CONFIG_FRAMEWORK_PROFILE ) )
        {
            String profile = m_propertyResolver.get( ServiceConstants.CONFIG_FRAMEWORK_PROFILE );
            if( profile == null )
            {
                profile = DEFAULT_FRAMEWORK_PROFILE;
            }
            return set( ServiceConstants.CONFIG_FRAMEWORK_PROFILE, profile );
        }
        return get( ServiceConstants.CONFIG_FRAMEWORK_PROFILE );
    }

    /**
     * @see Configuration#isCleanStart()
     */
    public Boolean isCleanStart()
    {
        if( !contains( ServiceConstants.CONFIG_CLEAN ) )
        {
            return set( ServiceConstants.CONFIG_CLEAN,
                        Boolean.valueOf( m_propertyResolver.get( ServiceConstants.CONFIG_CLEAN ) )
            );
        }
        return get( ServiceConstants.CONFIG_CLEAN );
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
        if( !contains( optionName ) )
        {
            String startLevel = m_propertyResolver.get( optionName );
            Integer startLevelAsInt = defaultValue;
            if( startLevel != null )
            {
                try
                {
                    startLevelAsInt = Integer.valueOf( startLevel );
                }
                catch( NumberFormatException ignore )
                {
                    // ignore and use default value
                }
            }
            return set( optionName, startLevelAsInt );
        }
        return get( optionName );
    }

    /**
     * @see Configuration#isOverwriteUserBundles()
     */
    public Boolean isOverwriteUserBundles()
    {
        if( !contains( ServiceConstants.CONFIG_OVERWRITE_USER_BUNDLES ) )
        {
            return set( ServiceConstants.CONFIG_OVERWRITE_USER_BUNDLES,
                        Boolean.valueOf( m_propertyResolver.get( ServiceConstants.CONFIG_OVERWRITE_USER_BUNDLES ) )
            );
        }
        return get( ServiceConstants.CONFIG_OVERWRITE_USER_BUNDLES );
    }

    /**
     * @see Configuration#isOverwriteSystemBundles()
     */
    public Boolean isOverwriteSystemBundles()
    {
        if( !contains( ServiceConstants.CONFIG_OVERWRITE_SYSTEM_BUNDLES ) )
        {
            return set( ServiceConstants.CONFIG_OVERWRITE_SYSTEM_BUNDLES,
                        Boolean.valueOf( m_propertyResolver.get( ServiceConstants.CONFIG_OVERWRITE_SYSTEM_BUNDLES ) )
            );
        }
        return get( ServiceConstants.CONFIG_OVERWRITE_SYSTEM_BUNDLES );
    }

    /**
     * @see Configuration#isDebugClassLoading()
     */
    public Boolean isDebugClassLoading()
    {
        if( !contains( ServiceConstants.CONFIG_DEBUG_CLASS_LOADING ) )
        {
            return set( ServiceConstants.CONFIG_DEBUG_CLASS_LOADING,
                        Boolean.valueOf( m_propertyResolver.get( ServiceConstants.CONFIG_DEBUG_CLASS_LOADING ) )
            );
        }
        return get( ServiceConstants.CONFIG_DEBUG_CLASS_LOADING );
    }

    /**
     * @see Configuration#isDownloadFeedback()
     */
    public Boolean isDownloadFeedback()
    {
        if( !contains( ServiceConstants.CONFIG_DOWNLOAD_FEEDBACK ) )
        {
            String downloadFeedback = m_propertyResolver.get( ServiceConstants.CONFIG_DOWNLOAD_FEEDBACK );
            if( downloadFeedback == null )
            {
                downloadFeedback = Boolean.TRUE.toString();
            }
            return set( ServiceConstants.CONFIG_DOWNLOAD_FEEDBACK,
                        Boolean.valueOf( downloadFeedback )
            );
        }
        return get( ServiceConstants.CONFIG_OVERWRITE_SYSTEM_BUNDLES );
    }

    /**
     * @see Configuration#getBootDelegation()
     */
    public String getBootDelegation()
    {
        if( !contains( ServiceConstants.CONFIG_BOOT_DELEGATION ) )
        {
            String bootDelegation = m_propertyResolver.get( ServiceConstants.CONFIG_BOOT_DELEGATION );
            if( bootDelegation != null )
            {
                bootDelegation = bootDelegation.trim();
                if( bootDelegation.endsWith( "," ) )
                {
                    bootDelegation = bootDelegation.substring( 0, bootDelegation.length() - 1 );
                }
                if( bootDelegation.length() == 0 )
                {
                    bootDelegation = null;
                }
            }
            return set( ServiceConstants.CONFIG_BOOT_DELEGATION, bootDelegation );
        }
        return get( ServiceConstants.CONFIG_BOOT_DELEGATION );
    }

    /**
     * @see Configuration#isAutoWrap()
     */
    public Boolean isAutoWrap()
    {
        if( !contains( ServiceConstants.CONFIG_AUTO_WRAP ) )
        {
            return set( ServiceConstants.CONFIG_AUTO_WRAP,
                        Boolean.valueOf( m_propertyResolver.get( ServiceConstants.CONFIG_AUTO_WRAP ) )
            );
        }
        return get( ServiceConstants.CONFIG_AUTO_WRAP );
    }

    /**
     * {@inheritDoc}
     */
    public Boolean keepOriginalUrls()
    {
        if( !contains( ServiceConstants.CONFIG_USE_ORIGINAL_URLS ) )
        {
            return set( ServiceConstants.CONFIG_USE_ORIGINAL_URLS,
                        Boolean.valueOf( m_propertyResolver.get( ServiceConstants.CONFIG_USE_ORIGINAL_URLS ) )
            );
        }
        return get( ServiceConstants.CONFIG_USE_ORIGINAL_URLS );
    }

    /**
     * {@inheritDoc}
     */
    public String getProperty( final String name )
    {
        return m_propertyResolver.get( name  );
    }

}
