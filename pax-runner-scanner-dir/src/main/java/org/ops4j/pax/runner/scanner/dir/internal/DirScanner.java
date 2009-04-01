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
package org.ops4j.pax.runner.scanner.dir.internal;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ProvisionSpec;
import org.ops4j.pax.runner.provision.ScannedBundle;
import org.ops4j.pax.runner.provision.Scanner;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.ScannedFileBundle;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.pax.runner.provision.scanner.ScannerConfigurationImpl;
import org.ops4j.pax.runner.scanner.dir.ServiceConstants;
import org.ops4j.util.property.PropertyResolver;

/**
 * A scanner that scans directory content for bundles.
 *
 * @author Alin Dreghiciu
 * @since September 04, 2007
 */
public class DirScanner
    implements Scanner
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( DirScanner.class );
    /**
     * PropertyResolver used to resolve properties.
     */
    private PropertyResolver m_propertyResolver;

    /**
     * Creates a new file scanner.
     *
     * @param propertyResolver a propertyResolver; mandatory
     */
    public DirScanner( final PropertyResolver propertyResolver )
    {
        NullArgumentException.validateNotNull( propertyResolver, "PropertyResolver" );
        m_propertyResolver = propertyResolver;
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
        final ScannerConfiguration config = createConfiguration();
        final Pattern filter = provisionSpec.getFilterPattern();
        final String spec = provisionSpec.getPath();
        final Integer defaultStartLevel = getDefaultStartLevel( provisionSpec, config );
        final Boolean defaultStart = getDefaultStart( provisionSpec, config );
        final Boolean defaultUpdate = getDefaultUpdate( provisionSpec, config );
        // try out an url
        LOGGER.trace( "Searching for [" + spec + "]" );
        URL url = null;
        try
        {
            url = new URL( spec );
        }
        catch( MalformedURLException ignore )
        {
            // ignore this as the spec may be resolved other way
            LOGGER.trace( "Specification is not a valid url: " + ignore.getMessage() + ". Continue discovery..." );
        }
        File file = null;
        if( url != null && "file".equals( url.getProtocol() ) )
        // if we have an url and it's a file url
        {
            try
            {
                file = new File( url.toURI() );
            }
            catch( URISyntaxException ignore )
            {
                // ignore this as the spec may be resolved other way
                LOGGER.trace(
                    "Specification is not a valid file url: " + ignore.getMessage() + ". Continue discovery..."
                );
            }
        }
        else
        // if we don't have an url then let's try out a direct file
        {
            file = new File( spec );
        }
        if( file != null && file.exists() )
        // if we have a directory
        {
            if( file.isDirectory() )
            {
                try
                {
                    return list(
                        new DirectoryLister( file, filter ),
                        defaultStartLevel,
                        defaultStart,
                        defaultUpdate
                    );
                }
                catch( MalformedURLException e )
                {
                    throw new MalformedSpecificationException( e );
                }
            }
            else
            {
                LOGGER.trace( "Specification is not a directory. Continue discovery..." );
            }
        }
        else
        {
            LOGGER.trace( "Specification is not a valid file. Continue discovery..." );
        }
        // on this point we may have a zip
        try
        {
            ZipFile zip = null;
            URL baseUrl = null;
            if( file != null && file.exists() )
            // try out a zip from the file we have
            {
                zip = new ZipFile( file );
                baseUrl = file.toURL();
            }
            else if( url != null )
            {
                zip = new ZipFile( url.toExternalForm() );
                baseUrl = url;
            }
            if( zip != null && baseUrl != null )
            {
                try
                {
                    return list(
                        new ZipLister( baseUrl, zip.entries(), filter ),
                        defaultStartLevel, defaultStart, defaultUpdate
                    );
                }
                catch( MalformedURLException e )
                {
                    throw new MalformedSpecificationException( e );
                }
            }
        }
        catch( IOException ignore )
        {
            // ignore for the moment
            LOGGER.trace( "Specification is not a valid zip: " + ignore.getMessage() + "Continue discovery..." );
        }
        // finaly try with a zip protocol
        if( url != null && !url.toExternalForm().startsWith( "jar" ) )
        {
            try
            {
                final URL jarUrl = new URL( "jar:" + url.toURI().toASCIIString() + "!/" );
                final JarURLConnection jar = (JarURLConnection) jarUrl.openConnection();
                return list(
                    new ZipLister( url, jar.getJarFile().entries(), filter ),
                    defaultStartLevel, defaultStart, defaultUpdate
                );
            }
            catch( Exception ignore )
            {
                LOGGER.trace( "Specification is not a valid jar: " + ignore.getMessage() );
            }
        }
        // if we got to this point then we cannot go further
        LOGGER.trace( "Specification urlSpec cannot be used. Stopping." );
        throw new MalformedSpecificationException( "Specification [" + provisionSpec.getPath() + "] could not be used"
        );
    }

    /**
     * Create scanned bundles based on the provided lister.
     *
     * @param lister      source of bundles
     * @param startLevel  default start level to use
     * @param shouldStart if by default should start
     * @param update      if by default should be updated
     *
     * @return a list of scanned bundles from the provided source
     *
     * @throws java.net.MalformedURLException re-thrown
     */
    private List<ScannedBundle> list( final Lister lister, final Integer startLevel, final Boolean shouldStart,
                                      final Boolean update )
        throws MalformedURLException
    {
        final List<ScannedBundle> scannedBundles = new ArrayList<ScannedBundle>();
        final List<URL> urls = lister.list();
        if( urls != null )
        {
            for( URL url : urls )
            {
                final ScannedFileBundle scannedFileBundle = new ScannedFileBundle(
                    url.toExternalForm(), startLevel, shouldStart, update
                );
                scannedBundles.add( scannedFileBundle );
                LOGGER.debug( "Installing bundle [" + scannedFileBundle + "]" );
            }
        }
        return scannedBundles;
    }

    /**
     * Returns the default start level by first looking at the parser and if not set fallback to configuration.
     *
     * @param provisionSpec provision spec
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
     * @param provisionSpec provision spec
     * @param config        a configuration
     *
     * @return default start level or null if not set.
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
     * @param provisionSpec provision spec
     * @param config        a configuration
     *
     * @return default update or null if not set.
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
     * Configuration factory method.
     *
     * @return a configuration
     */
    ScannerConfiguration createConfiguration()
    {
        return new ScannerConfigurationImpl( m_propertyResolver, ServiceConstants.PID );
    }

}
