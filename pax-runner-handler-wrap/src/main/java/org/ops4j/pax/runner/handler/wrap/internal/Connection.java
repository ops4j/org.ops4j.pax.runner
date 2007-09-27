/*
 * Copyright 2007 Alin Dreghiciu.
 * Copyright 2007 Peter Kriens.  
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
package org.ops4j.pax.runner.handler.wrap.internal;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Jar;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.jar.Manifest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.url.URLUtils;

/**
 * An URLConnection that supports wrap: protocol.<br/>
 * TODO add unit tests
 *
 * @author Alin Dreghiciu
 * @see org.ops4j.pax.runner.handler.wrap.internal.Handler
 * @since September 09, 2007
 */
public class Connection
    extends URLConnection
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( Connection.class );
    /**
     * Parsed url.
     */
    private Parser m_parser;
    /**
     * Service configuration.
     */
    private final Configuration m_configuration;

    /**
     * Creates a new connection.
     *
     * @param url           the url; cannot be null.
     * @param configuration service configuration; cannot be null
     *
     * @throws MalformedURLException in case of a malformed url
     */
    public Connection( final URL url, final Configuration configuration )
        throws MalformedURLException
    {
        super( url );
        Assert.notNull( "URL cannot be null", url );
        Assert.notNull( "Service configuration", configuration );
        m_configuration = configuration;
        m_parser = new Parser( url.getPath() );
    }

    /**
     * Does nothing.
     *
     * @see java.net.URLConnection#connect()
     */
    @Override
    public void connect()
    {
        // do nothing
    }

    /**
     * Returns the input stream denoted by the url.
     *
     * @return the input stream for the resource denoted by url
     *
     * @throws IOException in case of an exception during accessing the resource
     * @see java.net.URLConnection#getInputStream()
     */
    @Override
    public InputStream getInputStream()
        throws IOException
    {
        connect();
        LOGGER.debug( "Start wrapping [" + m_parser.getWrappedJarURL() + "]" );

        final Properties properties = m_parser.getWrappingProperties();
        final InputStream target =
            URLUtils.prepareInputStream( m_parser.getWrappedJarURL(), !m_configuration.getCertificateCheck() );

        final Jar jar = new Jar( "dot", target );
        final Manifest manifest = jar.getManifest();

        // Verify it is not a bundle
        if ( manifest == null
             || ( manifest.getMainAttributes().getValue( Analyzer.EXPORT_PACKAGE ) == null
                  && manifest.getMainAttributes().getValue( Analyzer.IMPORT_PACKAGE ) == null )
            )
        {
            properties.put( "Pax-Runner-Generated-From", getURL().toExternalForm() );
            final Analyzer analyzer = new Analyzer();
            analyzer.setJar( jar );
            analyzer.setProperties( properties );
            checkMandatoryProperties( analyzer, jar );
            analyzer.mergeManifest( manifest );
            analyzer.calcManifest();
        }

        return createInputStream( jar );
    }

    /**
     * Creates an piped input stream for the wrapped jar.
     * This is done in a thread so we can retrun quickly.
     *
     * @param jar the wrapped jar
     *
     * @return an input stream for the wrapped jar
     *
     * @throws IOException re-thrown
     */
    private PipedInputStream createInputStream( final Jar jar )
        throws IOException
    {
        final PipedInputStream pin = new PipedInputStream();
        final PipedOutputStream pout = new PipedOutputStream( pin );

        new Thread()
        {
            public void run()
            {
                try
                {
                    jar.write( pout );
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( "Could not generate the wrapped jar, e" );
                }
                finally
                {
                    try
                    {
                        jar.close();
                        pout.close();
                    }
                    catch ( IOException ignore )
                    {
                        throw new RuntimeException( "Could not generate the wrapped jar, e" );
                    }
                }
            }
        }.start();

        return pin;
    }

    /**
     * Check if manadatory properties are present, otherwise generate default.
     *
     * @param analyzer a bnd analyzer
     * @param jar      a bnd jar
     */
    private void checkMandatoryProperties( final Analyzer analyzer, final Jar jar )
    {
        final String importPackage = analyzer.getProperty( Analyzer.IMPORT_PACKAGE );
        if ( importPackage == null || importPackage.trim().length() == 0 )
        {
            analyzer.setProperty( Analyzer.IMPORT_PACKAGE, "*;resolution:=optional" );
        }
        final String exportPackage = analyzer.getProperty( Analyzer.EXPORT_PACKAGE );
        if ( exportPackage == null || exportPackage.trim().length() == 0 )
        {
            analyzer.setProperty( Analyzer.EXPORT_PACKAGE, analyzer.calculateExportsFromContents( jar ) );
        }
        final String symbolicName = analyzer.getProperty( Analyzer.BUNDLE_SYMBOLICNAME );
        if ( symbolicName == null || symbolicName.trim().length() == 0 )
        {
            analyzer.setProperty( Analyzer.BUNDLE_SYMBOLICNAME,
                                  generateSymbolicName( m_parser.getWrappedJarURL().toExternalForm() )
            );
        }
    }

    /**
     * Generates a symbolic name form an url spec by replacing invalid characters.
     *
     * @param urlSpec the source for the name
     *
     * @return a valid symbolic name
     */
    private String generateSymbolicName( final String urlSpec )
    {
        return urlSpec.replaceAll( "[^a-zA-Z_0-9-]", "_" );
    }

}
