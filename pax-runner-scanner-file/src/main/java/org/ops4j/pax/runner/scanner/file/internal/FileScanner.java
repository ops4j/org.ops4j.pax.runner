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
package org.ops4j.pax.runner.scanner.file.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.properties.SystemPropertyUtils;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.Scanner;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.FileBundleReference;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.pax.runner.provision.scanner.ScannerConfigurationImpl;
import org.ops4j.pax.runner.scanner.file.ServiceConstants;

/**
 * A scanner that scans plain text file.
 *
 * @author Alin Dreghiciu
 * @since August 15, 2007
 */
public class FileScanner
    implements Scanner
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( FileScanner.class );
    /**
     * The starting character for a comment line.
     */
    private static final String COMMENT_SIGN = "#";
    /**
     * Prefix for properties.
     */
    private static final String PROPERTY_PREFIX = "-D";
    /**
     * Regex pattern used to spint property key/value.
     */
    private static final Pattern PROPERTY_PATTERN = Pattern.compile( "-D(.*)=(.*)" );

    /**
     * Resolver used to resolve properties.
     */
    private Resolver m_resolver;

    /**
     * Creates a new file scanner.
     *
     * @param resolver a resolver; mandatory
     */
    public FileScanner( final Resolver resolver )
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
        List<BundleReference> references = new ArrayList<BundleReference>();
        Parser parser = createParser( urlSpec );
        ScannerConfiguration config = createConfiguration();
        BufferedReader bufferedReader = null;
        try
        {
            try
            {
                bufferedReader = new BufferedReader( new InputStreamReader( parser.getFileURL().openStream() ) );
                Integer defaultStartLevel = getDefaultStartLevel( parser, config );
                Boolean defaultStart = getDefaultStart( parser, config );
                String line;
                while( ( line = bufferedReader.readLine() ) != null )
                {
                    if( !"".equals( line.trim() ) && !line.trim().startsWith( COMMENT_SIGN ) )
                    {
                        if( line.trim().startsWith( PROPERTY_PREFIX ) )
                        {
                            final Matcher matcher = PROPERTY_PATTERN.matcher( line.trim() );
                            if( !matcher.matches() || matcher.groupCount() != 2 )
                            {
                                throw new ScannerException( "Invalid property: " + line );
                            }
                            String value = matcher.group( 2 );
                            value = SystemPropertyUtils.resolvePlaceholders(value);
							System.setProperty( matcher.group( 1 ), value );
                        }
                        else
                        {
                        	line = SystemPropertyUtils.resolvePlaceholders(line);
                            references.add( new FileBundleReference( line, defaultStartLevel, defaultStart ) );
                        }
                    }
                }
            }
            finally
            {
                if( bufferedReader != null )
                {
                    bufferedReader.close();
                }
            }
        }
        catch( IOException e )
        {
            throw new ScannerException( "Could not parse the provision file", e );
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
     * @return default start level or null if nos set.
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
     * Creates a new configuration.
     *
     * @return a configuration
     */
    ScannerConfiguration createConfiguration()
    {
        return new ScannerConfigurationImpl( m_resolver, ServiceConstants.PID );
    }

}
