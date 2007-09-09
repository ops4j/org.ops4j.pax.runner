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
package org.ops4j.pax.runner.handler.mvn.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.resolver.ConfigurationMap;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.handler.mvn.ServiceConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Service Configuration implementation.
 *
 * @author Alin Dreghiciu
 * @see Configuration
 * @since August 11, 2007
 */
public class ConfigurationImpl
    extends ConfigurationMap
    implements Configuration
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( ConfigurationImpl.class );

    /**
     * The character that should be the first character in repositories property in order to be appended with the
     * repositories from settings.xml.
     */
    private final static String REPOSITORIES_APPEND_SIGN = "+";
    /**
     * Repositories separator.
     */
    private final static String REPOSITORIES_SEPARATOR = ",";

    /**
     * Maven settings abstraction. Can be null.
     */
    private Settings m_settings;
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
     * Sets maven settings abstraction.
     *
     * @param settings maven settings abstraction
     */
    public void setSettings( final Settings settings )
    {
        m_settings = settings;
    }

    /**
     * @see Configuration#getCertificateCheck()
     */
    public Boolean getCertificateCheck()
    {
        if ( !contains( ServiceConstants.PROPERTY_CERTIFICATE_CHECK ) )
        {
            return set( ServiceConstants.PROPERTY_CERTIFICATE_CHECK,
                        Boolean.valueOf( m_resolver.get( ServiceConstants.PROPERTY_CERTIFICATE_CHECK ) )
            );
        }
        return get( ServiceConstants.PROPERTY_CERTIFICATE_CHECK );
    }

    /**
     * Returns the URL of settings file. Will try first to use the url as is. If a malformed url encountered then will
     * try to use the url as a file path. If still not valid will throw the original Malformed URL exception.
     *
     * @see Configuration#getSettings()
     */
    public URL getSettings()
        throws MalformedURLException
    {
        if ( !contains( ServiceConstants.PROPERTY_SETTINGS_FILE ) )
        {
            String spec = m_resolver.get( ServiceConstants.PROPERTY_SETTINGS_FILE );
            if ( spec != null )
            {
                try
                {
                    return set( ServiceConstants.PROPERTY_SETTINGS_FILE, new URL( spec ) );
                }
                catch ( MalformedURLException e )
                {
                    File file = new File( spec );
                    if ( file.exists() )
                    {
                        return set( ServiceConstants.PROPERTY_SETTINGS_FILE, file.toURL() );
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
        }
        return get( ServiceConstants.PROPERTY_SETTINGS_FILE );
    }

    /**
     * Repository is a comma separated list of repositories to be used. If repository acces requests authentication
     * the user name and password must be specified in the repository url as for example
     * http://user:password@repository.ops4j.org/maven2.<br/>
     * If the repository from 1/2 bellow starts with a plus (+) the option 3 is also used and the repositories from
     * settings.xml will be cummulated.<br/>
     * Repository resolution:<br/>
     * 1. looks for a configuration property named repository;<br/>
     * 2. looks for a framework property/system setting repository;<br/>
     * 3. looks in settings.xml (see settings.xml resolution). in this case all configured repositories will be used
     * including configured user/password. In this case the central repository is also added.
     * Note that the local repository is added as the first repository if exists.
     *
     * @see Configuration#getRepositories()
     * @see Configuration#getLocalRepository()
     */
    public List<URL> getRepositories()
        throws MalformedURLException
    {
        if ( !contains( ServiceConstants.PROPERTY_REPOSITORIES ) )
        {
            // look for repositories property
            String repositoriesProp = m_resolver.get( ServiceConstants.PROPERTY_REPOSITORIES );
            // if not set or starting with a plus (+) get repositories from settings xml
            if ( ( repositoriesProp == null || repositoriesProp.startsWith( REPOSITORIES_APPEND_SIGN ) )
                 && m_settings != null )
            {
                String settingsRepos = m_settings.getRepositories();
                if ( settingsRepos != null )
                {
                    if ( repositoriesProp == null )
                    {
                        repositoriesProp = settingsRepos;
                    }
                    else
                    {
                        // apend repositories from settings xml and get rid of +
                        repositoriesProp = repositoriesProp.substring( 1 ) + REPOSITORIES_SEPARATOR + settingsRepos;
                    }
                }
            }
            // build repositories list
            final List<URL> repositoriesProperty = new ArrayList<URL>();
            URL localRepository = getLocalRepository();
            if ( localRepository != null )
            {
                repositoriesProperty.add( localRepository );
            }
            if ( repositoriesProp != null && repositoriesProp.trim().length() > 0 )
            {
                String[] repositories = repositoriesProp.split( REPOSITORIES_SEPARATOR );
                for ( String repositoryURL : repositories )
                {
                    repositoriesProperty.add( new URL( repositoryURL ) );
                }
            }
            LOGGER.trace( "Using repositories [" + repositoriesProperty + "]" );
            return set( ServiceConstants.PROPERTY_REPOSITORIES, repositoriesProperty );
        }
        return get( ServiceConstants.PROPERTY_REPOSITORIES );
    }

    /**
     * Resolves local repository directory by using the following resolution:<br/>
     * 1. looks for a configuration property named localRepository;
     * 2. looks for a framework property/system setting localRepository;<br/>
     * 3. looks in settings.xml (see settings.xml resolution);<br/>
     * 4. falls back to ${user.home}/.m2/repository.
     *
     * @see Configuration#getLocalRepository()
     */
    public URL getLocalRepository()
        throws MalformedURLException
    {
        if ( !contains( ServiceConstants.PROPERTY_LOCAL_REPOSITORY ) )
        {
            // look for a local repository property
            String spec = m_resolver.get( ServiceConstants.PROPERTY_LOCAL_REPOSITORY );
            // if not set get local repository from maven settings
            if ( spec == null && m_settings != null )
            {
                spec = m_settings.getLocalRepository();
            }
            if ( spec != null )
            {
                // check if we have a valid url
                try
                {
                    set( ServiceConstants.PROPERTY_LOCAL_REPOSITORY, new URL( spec ) );
                }
                catch ( MalformedURLException e )
                {
                    // maybe is just a file?
                    File file = new File( spec );
                    if ( file.exists() )
                    {
                        set( ServiceConstants.PROPERTY_LOCAL_REPOSITORY, file.toURL() );
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
        }
        return get( ServiceConstants.PROPERTY_LOCAL_REPOSITORY );
    }

}
