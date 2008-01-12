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
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.Scanner;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.FileBundleReference;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.pax.runner.provision.scanner.ScannerConfigurationImpl;
import org.ops4j.pax.runner.scanner.dir.ServiceConstants;

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
     * Resolver used to resolve properties.
     */
    private Resolver m_resolver;

    /**
     * Creates a new file scanner.
     *
     * @param resolver a resolver; mandatory
     */
    public DirScanner( final Resolver resolver )
    {
        Assert.notNull( "Resolver", resolver );
        m_resolver = resolver;
    }

    /**
     * Reads the bundles from the file specified by the urlSpec.
     *
     * @param urlSpec url spec to the text file containing the bundle.
     */
    public List<BundleReference> scan( final String urlSpec )
        throws MalformedSpecificationException, ScannerException
    {
        LOGGER.debug( "Scanning [" + urlSpec + "]" );
        final Parser parser = createParser( urlSpec );
        final ScannerConfiguration config = createConfiguration();
        final Pattern filter = parser.getFilter();
        final String spec = parser.getURL();
        final Integer defaultStartLevel = getDefaultStartLevel( parser, config );
        final Boolean defaultStart = getDefaultStart( parser, config );
        final Boolean defaultUpdate = getDefaultUpdate( parser, config );
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
        if( url!= null && !url.toExternalForm().startsWith( "jar" ) )
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
        throw new MalformedSpecificationException( "Specification [" + urlSpec + "] could not be used" );
    }

    /**
     * Create bundle references based on the provided lister.
     *
     * @param lister      source of bundles
     * @param startLevel  default start level to use (see FileBundleReference)
     * @param shouldStart if by default should start (see FileBundleReference)
     * @param update      if by default should be updated (see FileBundleReference)
     *
     * @return a list of bundles references from the provided source
     *
     * @throws java.net.MalformedURLException re-thrown from FileBundleReference
     * @see FileBundleReference#FileBundleReference(String,Integer,Boolean,Boolean)
     */
    private List<BundleReference> list( final Lister lister, final Integer startLevel, final Boolean shouldStart,
                                        final Boolean update )
        throws MalformedURLException
    {
        final List<BundleReference> references = new ArrayList<BundleReference>();
        final List<URL> urls = lister.list();
        if( urls != null )
        {
            for( URL url : urls )
            {
                references.add( new FileBundleReference( url.toExternalForm(), startLevel, shouldStart, update ) );
            }
        }
        return references;
    }

    /**
     * Returns the default start level by first looking at the parser and if not set fallback to configuration.
     *
     * @param parser a parser
     * @param config a configuration
     *
     * @return default start level or null if nos set.
     */
    private Integer getDefaultStartLevel( Parser parser, ScannerConfiguration config )
    {
        Integer startLevel = parser.getStartLevel();
        if( startLevel == null )
        {
            startLevel = config.getStartLevel();
        }
        return startLevel;
    }

    /**
     * Returns the default start by first looking at the parser and if not set fallback to configuration.
     *
     * @param parser a parser
     * @param config a configuration
     *
     * @return default start level or null if not set.
     */
    private Boolean getDefaultStart( final Parser parser, final ScannerConfiguration config )
    {
        Boolean start = parser.shouldStart();
        if( start == null )
        {
            start = config.shouldStart();
        }
        return start;
    }

    /**
     * Returns the default update by first looking at the parser and if not set fallback to configuration.
     *
     * @param parser a parser
     * @param config a configuration
     *
     * @return default update or null if not set.
     */
    private Boolean getDefaultUpdate( final Parser parser, final ScannerConfiguration config )
    {
        Boolean update = parser.shouldUpdate();
        if( update == null )
        {
            update = config.shouldUpdate();
        }
        return update;
    }

    /**
     * Sets the resolver to use.
     *
     * @param resolver a resolver
     */
    public void setResolver( final Resolver resolver )
    {
        Assert.notNull( "Resolver", resolver );
        m_resolver = resolver;
    }

    /**
     * Creates a parser.
     *
     * @param urlSpec url spec to the text file containing the bundles.
     *
     * @return a parser
     *
     * @throws org.ops4j.pax.runner.provision.MalformedSpecificationException
     *          rethrown from parser
     */
    Parser createParser( final String urlSpec )
        throws MalformedSpecificationException
    {
        return new ParserImpl( urlSpec );
    }

    /**
     * Configuration factory method.
     *
     * @return a configuration
     */
    ScannerConfiguration createConfiguration()
    {
        return new ScannerConfigurationImpl( m_resolver, ServiceConstants.PID );
    }

}
