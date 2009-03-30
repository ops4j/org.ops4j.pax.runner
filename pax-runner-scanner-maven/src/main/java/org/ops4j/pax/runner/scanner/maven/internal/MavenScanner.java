/*
 * Copyright 2009 Alin Dreghiciu.
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
package org.ops4j.pax.runner.scanner.maven.internal;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.QualityRange;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.ArtifactBasicResults;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.repository.virtual.VirtualRepositoryReader;
import org.apache.maven.mercury.transport.api.Server;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ProvisionSpec;
import org.ops4j.pax.runner.provision.ScannedBundle;
import org.ops4j.pax.runner.provision.Scanner;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.ScannedFileBundle;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.pax.runner.provision.scanner.ScannerConfigurationImpl;
import org.ops4j.pax.runner.scanner.maven.ServiceConstants;
import org.ops4j.pax.url.maven.commons.MavenConfiguration;
import org.ops4j.pax.url.maven.commons.MavenConfigurationImpl;
import org.ops4j.pax.url.maven.commons.MavenRepositoryURL;
import org.ops4j.pax.url.maven.commons.MavenSettingsImpl;
import org.ops4j.util.property.PropertyResolver;

/**
 * A scanner that scans maven artifacts for main artifact (if a jar/bundle), direct dependencies and transitive
 * dependencies.
 *
 * @author Alin Dreghiciu
 * @since 0.18.0, March 19, 2007
 */
public class MavenScanner
    implements Scanner
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( MavenScanner.class );
    /**
     * PropertyResolver used to resolve properties.
     */
    private PropertyResolver m_propertyResolver;
    /**
     * Scanner configuration.
     */
    private ScannerConfiguration m_scannerConfiguration;
    /**
     * Mercury virtual repository reader.
     */
    private VirtualRepositoryReader m_vrr;
    /**
     * Exception in case that thrown during initialization.
     */
    private Exception m_exception;

    /**
     * Creates a new file scanner.
     *
     * @param propertyResolver a propertyResolver; mandatory
     */
    public MavenScanner( final PropertyResolver propertyResolver )
    {
        NullArgumentException.validateNotNull( propertyResolver, "PropertyResolver" );
        m_propertyResolver = propertyResolver;
        try
        {
            initialize();
            m_exception = null;
        }
        catch( Exception e )
        {
            m_exception = e;
        }
    }

    /**
     * Reads the bundles from the file specified by the urlSpec.
     * {@inheritDoc}
     */
    public List<ScannedBundle> scan( final ProvisionSpec provisionSpec )
        throws MalformedSpecificationException, ScannerException
    {
        NullArgumentException.validateNotNull( provisionSpec, "Provision spec" );

        LOGGER.debug( "Scanning [" + provisionSpec.getPath() + "]" );

        if( m_exception != null )
        {
            throw new ScannerException( "Exception while configuration", m_exception );
        }

        List<ScannedBundle> scannedBundles = new ArrayList<ScannedBundle>();

        final Integer defaultStartLevel = getDefaultStartLevel( provisionSpec, m_scannerConfiguration );
        final Boolean defaultStart = getDefaultStart( provisionSpec, m_scannerConfiguration );
        final Boolean defaultUpdate = getDefaultUpdate( provisionSpec, m_scannerConfiguration );

        try
        {
            List<ArtifactBasicMetadata> query = new ArrayList<ArtifactBasicMetadata>();
            final ArtifactMetadata queryMeta = new ArtifactMetadata( provisionSpec.getPath() );
            query.add( queryMeta );

            final ArtifactBasicResults results = m_vrr.readVersions( query );
            if( results.hasExceptions() )
            {
                //noinspection ThrowableResultOfMethodCallIgnored
                throw new ScannerException( results.getError( queryMeta ).getMessage() );
            }
            if( results.hasResults( queryMeta ) )
            {
                final List<ArtifactBasicMetadata> foundArtifacts = results.getResult( queryMeta );
                for( ArtifactBasicMetadata foundArtifact : foundArtifacts )
                {
                    final ArtifactResults artifactResults = m_vrr.readArtifacts( toList( foundArtifact ) );
                    if( artifactResults.hasExceptions() )
                    {
                        //noinspection ThrowableResultOfMethodCallIgnored
                        throw new ScannerException( artifactResults.getError( foundArtifact ).getMessage() );
                    }
                    if( artifactResults.hasResults( foundArtifact ) )
                    {
                        final List<Artifact> artifacts = artifactResults.getResults( foundArtifact );
                        for( Artifact artifact : artifacts )
                        {
                            scannedBundles.add(
                                new ScannedFileBundle(
                                    artifact.getFile().toURL().toExternalForm(),
                                    defaultStartLevel, defaultStart, defaultUpdate
                                )
                            );
                        }
                    }
                }
            }
        }
        catch( MalformedURLException e )
        {
            throw new ScannerException( e.getMessage() );
        }
        catch( RepositoryException e )
        {
            throw new ScannerException( e.getMessage() );
        }

        return scannedBundles;
    }

    /**
     * Initialize configurations and virtual repository reader.
     *
     * @throws Exception - Re-thrown
     */
    private void initialize()
        throws Exception
    {
        m_scannerConfiguration = createScannerConfiguration();
        final MavenConfiguration mavenConfiguration = createMavenConfiguration();

        final List<Repository> repositories = new ArrayList<Repository>();
        final MavenRepositoryURL localRepository = mavenConfiguration.getLocalRepository();
        if( localRepository != null )
        {
            LOGGER.debug( "Using local repository " + localRepository );
            repositories.add( toRepository( localRepository ) );
        }
        for( MavenRepositoryURL repositoryURL : mavenConfiguration.getRepositories() )
        {
            LOGGER.debug( "Using remote repository " + repositoryURL );
            repositories.add( toRepository( repositoryURL ) );
        }
        m_vrr = new VirtualRepositoryReader( repositories );
    }

    /**
     * Adapt a {@link MavenRepositoryURL} to a Mercury {@link Repository}.
     *
     * @param repositoryURL to adapt
     *
     * @return adapted
     */
    private Repository toRepository( final MavenRepositoryURL repositoryURL )
    {
        final Repository repository;
        if( repositoryURL.isFileRepository() )
        {
            repository = new LocalRepositoryM2(
                repositoryURL.getId(), repositoryURL.getFile(), DependencyProcessor.NULL_PROCESSOR
            );
        }
        else
        {
            final Server server = new Server( repositoryURL.getId(), repositoryURL.getURL() );
            repository = new RemoteRepositoryM2( server.getId(), server, DependencyProcessor.NULL_PROCESSOR );
        }
        repository.setRepositoryQualityRange(
            createQualityRange( repositoryURL.isReleasesEnabled(), repositoryURL.isSnapshotsEnabled() )
        );
        return repository;
    }

    // TODO remove once a version > 1.0-alpha-5 of Mercury and use QualityRange.create
    private static QualityRange createQualityRange( boolean releases, boolean snapshots )
    {
        if( releases && snapshots )
        {
            return QualityRange.ALL;
        }
        else if( releases )
        {
            return new QualityRange( Quality.ALPHA_QUALITY, true, Quality.RELEASE_QUALITY, true );
        }
        else if( snapshots )
        {
            return new QualityRange( Quality.SNAPSHOT_QUALITY, true, Quality.SNAPSHOT_TS_QUALITY, true );
        }

        throw new IllegalArgumentException( "Unsuported combination for releases/snapshots" );
    }

    /**
     * Utility to add var args elements into a list.
     *
     * @param elements to add
     * @param <T>      any type
     *
     * @return list
     */
    private <T> List<T> toList( final T... elements )
    {
        final ArrayList<T> list = new ArrayList<T>();
        list.addAll( Arrays.asList( elements ) );
        return list;
    }

    /**
     * Returns the default start level by first looking at the parser and if not set fallback to configuration.
     *
     * @param provisionSpec a parser
     * @param config        a configuration
     *
     * @return default start level or null if nos set.
     */
    private Integer getDefaultStartLevel( ProvisionSpec provisionSpec, ScannerConfiguration config )
    {
        Integer startLevel = provisionSpec.getStartLevel();
        if( startLevel == null )
        {
            startLevel = config.getStartLevel();
        }
        return startLevel;
    }

    /**
     * Returns the default start by first looking at the parser and if not set fallback to configuration.
     *
     * @param provisionSpec a parser
     * @param config        a configuration
     *
     * @return default start level or null if nos set.
     */
    private Boolean getDefaultStart( final ProvisionSpec provisionSpec, final ScannerConfiguration config )
    {
        Boolean start = provisionSpec.shouldStart();
        if( start == null )
        {
            start = config.shouldStart();
        }
        return start;
    }

    /**
     * Returns the default update by first looking at the parser and if not set fallback to configuration.
     *
     * @param provisionSpec a parser
     * @param config        a configuration
     *
     * @return default update or null if nos set.
     */
    private Boolean getDefaultUpdate( final ProvisionSpec provisionSpec, final ScannerConfiguration config )
    {
        Boolean update = provisionSpec.shouldUpdate();
        if( update == null )
        {
            update = config.shouldUpdate();
        }
        return update;
    }

    /**
     * Sets the propertyResolver to use.
     *
     * @param propertyResolver a propertyResolver
     */
    public void setResolver( final PropertyResolver propertyResolver )
    {
        NullArgumentException.validateNotNull( propertyResolver, "PropertyResolver" );
        m_propertyResolver = propertyResolver;
        try
        {
            initialize();
            m_exception = null;
        }
        catch( Exception e )
        {
            m_exception = e;
        }
    }

    /**
     * Creates a new scanner configuration.
     *
     * @return a configuration
     */
    ScannerConfiguration createScannerConfiguration()
    {
        return new ScannerConfigurationImpl( m_propertyResolver, ServiceConstants.PID );
    }

    /**
     * Creates a new scanner configuration.
     *
     * @return a configuration
     */
    MavenConfiguration createMavenConfiguration()
    {
        final MavenConfigurationImpl configuration =
            new MavenConfigurationImpl( m_propertyResolver, ServiceConstants.PID );
        configuration.setSettings( new MavenSettingsImpl( configuration.getSettingsFileUrl() ) );
        return configuration;
    }

}