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
package org.ops4j.pax.runner.scanner.feature.internal;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.kernel.gshell.features.Feature;
import org.apache.servicemix.kernel.gshell.features.internal.RepositoryImpl;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.provision.ProvisionSpec;
import org.ops4j.pax.runner.provision.ScannedBundle;
import org.ops4j.pax.runner.provision.ScannedBundleBean;
import org.ops4j.pax.runner.provision.Scanner;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.pax.runner.provision.scanner.ScannerConfigurationImpl;
import org.ops4j.pax.runner.scanner.feature.ServiceConstants;
import org.ops4j.util.property.PropertyResolver;

/**
 * A scanner that scans Apache ServiceMix Kernel features files (xml).
 *
 * @author Alin Dreghiciu
 * @since 0.18.0, April 01, 2007
 */
public class FeatureScanner
    implements Scanner
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( FeatureScanner.class );
    /**
     * PropertyResolver used to resolve properties.
     */
    private PropertyResolver m_propertyResolver;

    /**
     * Creates a new file scanner.
     *
     * @param propertyResolver a propertyResolver; mandatory
     */
    public FeatureScanner( final PropertyResolver propertyResolver )
    {
        NullArgumentException.validateNotNull( propertyResolver, "PropertyResolver" );
        m_propertyResolver = propertyResolver;
    }

    /**
     * Reads the bundles from the file specified by the urlSpec.
     * {@inheritDoc}
     */
    public List<ScannedBundle> scan( final ProvisionSpec provisionSpec )
        throws ScannerException
    {
        NullArgumentException.validateNotNull( provisionSpec, "Provision spec" );

        LOGGER.debug( "Scanning [" + provisionSpec.getPath() + "]" );

        final ScannerConfiguration config = createConfiguration();

        final Integer defaultStartLevel = getDefaultStartLevel( provisionSpec, config );
        final Boolean defaultStart = getDefaultStart( provisionSpec, config );
        final Boolean defaultUpdate = getDefaultUpdate( provisionSpec, config );

        try
        {
            final RepositoryImpl repository = new RepositoryImpl( provisionSpec.getPathAsUrl().toURI() );
            return features( repository.getFeatures(),
                             provisionSpec.getFilterPattern(),
                             defaultStartLevel, defaultStart, defaultUpdate
            );
        }
        catch( URISyntaxException e )
        {
            throw new ScannerException( "URL cannot be used", e );
        }
        catch( Exception e )
        {
            throw new ScannerException( "Repository cannot be used", e );
        }
    }

    private List<ScannedBundle> features( final Feature[] features,
                                          final Pattern filter,
                                          final Integer startLevel,
                                          final Boolean shouldStart,
                                          final Boolean shouldUpdate )
    {
        final List<ScannedBundle> scannedBundles = new ArrayList<ScannedBundle>();
        if( features != null && features.length > 0 )
        {
            for( Feature feature : features )
            {
                if( filter != null && !filter.matcher( feature.getId() ).matches() )
                {
                    continue;
                }
                final List<Feature> dependencies = feature.getDependencies();
                if( dependencies != null && dependencies.size() > 0 )
                {
                    for( Feature dependency : dependencies )
                    {
                        scannedBundles.addAll(
                            features(
                                features,
                                Pattern.compile( dependency.getId() ),
                                startLevel, shouldStart, shouldUpdate
                            )
                        );
                    }
                }
                for( String bundleUrl : feature.getBundles() )
                {
                    scannedBundles.add( new ScannedBundleBean( bundleUrl, startLevel, shouldStart, shouldUpdate ) );
                }
            }
        }
        return scannedBundles;
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
    }

    /**
     * Creates a new scanner configuration.
     *
     * @return a configuration
     */
    ScannerConfiguration createConfiguration()
    {
        return new ScannerConfigurationImpl( m_propertyResolver, ServiceConstants.PID );
    }

}