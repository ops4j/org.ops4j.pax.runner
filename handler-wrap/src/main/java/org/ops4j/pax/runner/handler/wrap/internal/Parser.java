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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ops4j.pax.runner.commons.url.URLUtils;

/**
 * Parser for wrap: protocol.<br/>
 *
 * @author Alin Dreghiciu
 * @see org.ops4j.pax.runner.handler.wrap.internal.Connection
 * @since September 09, 2007
 */
public class Parser
{

    /**
     * Syntax for the url; to be shown on exception messages.
     */
    private static final String SYNTAX = "wrap:jar_url[!wrapping_instructions_url|wrapping_instructions]";
    /**
     * Separator between wrapped jar url and instructions.
     */
    private static final String INSTRUCTIONS_SEPARATOR = "!";
    /**
     * Regex pattern for matching instructions when specified in url.
     */
    private static final Pattern INSTRUCTIONS_PATTERN =
        Pattern.compile( "([a-zA-Z_0-9-]+)=([-!\"'()*+,.0-9A-Z_a-z%]+)" );
    /**
     * Wrapped jar URL.
     */
    private URL m_wrappedJarURL;
    /**
     * Wrapping instructions URL.
     */
    private Properties m_wrappingProperties;

    /**
     * Creates a new protocol parser.
     *
     * @param path the path part of the url (without starting wrap:)
     *
     * @throws MalformedURLException if provided path does not comply to expected syntax or has malformed urls
     */
    public Parser( final String path )
        throws MalformedURLException
    {
        if( path == null || path.trim().length() == 0 )
        {
            throw new MalformedURLException( "Path cannot be null or empty. Syntax " + SYNTAX );
        }
        if( path.startsWith( INSTRUCTIONS_SEPARATOR ) || path.endsWith( INSTRUCTIONS_SEPARATOR ) )
        {
            throw new MalformedURLException(
                "Path cannot start or end with " + INSTRUCTIONS_SEPARATOR + ". Syntax " + SYNTAX
            );
        }
        m_wrappingProperties = new Properties();
        Pattern splitPattern = Pattern.compile( "(.+?)!(.+?)" );
        if( splitPattern.matcher( path ).matches() )
        {
            Matcher matcher = splitPattern.matcher( path );
            matcher.matches();
            parseInstructions( matcher.group( 2 ) );
            m_wrappedJarURL = new URL( matcher.group( 1 ) );
        }
        else
        {
            m_wrappedJarURL = new URL( path );
        }
    }

    /**
     * Parses the instructions of the url ( without the wrapped jar url).
     *
     * @param spec url part without protocol and wrapped jar url.
     *
     * @throws MalformedURLException if provided path does not comply to syntax.
     */
    private void parseInstructions( final String spec )
        throws MalformedURLException
    {
        try
        {
            // first try to make an url out of the instructions.
            try
            {
                final URL url = new URL( spec );
                // TODO use the certificate check property from the handler instead of true bellow
                try
                {
                    m_wrappingProperties.load( URLUtils.prepareInputStream( url, true ) );
                }
                catch( IOException e )
                {
                    throw initMalformedURLException( "Could not retrieve the instructions from [" + spec + "]", e );
                }
            }
            catch( MalformedURLException ignore )
            {
                // just ignore for the moment and try out if we have valid properties separated by "&"
                final String segments[] = spec.split( "&" );
                for( String segment : segments )
                {
                    final Matcher matcher = INSTRUCTIONS_PATTERN.matcher( segment );
                    if( matcher.matches() )
                    {
                        m_wrappingProperties.put(
                            matcher.group( 1 ),
                            URLDecoder.decode( matcher.group( 2 ), "UTF-8" )
                        );
                    }
                    else
                    {
                        throw new MalformedURLException( "Invalid syntax for instruction [" + segment
                                                         + "]. Take a look at http://www.aqute.biz/Code/Bnd."
                        );
                    }
                }
            }
        }
        catch( UnsupportedEncodingException e )
        {
            throw initMalformedURLException( "Could not retrieve the instructions from [" + spec + "]", e );
        }
        if( m_wrappingProperties.size() == 0 )
        {
            throw new MalformedURLException( "Could not determine wrapping instructions from [" + spec + "]" );
        }
    }

    /**
     * Returns the wrapped URL if present, null otherwise
     *
     * @return wraped jar URL
     */
    public URL getWrappedJarURL()
    {
        return m_wrappedJarURL;
    }

    /**
     * Returns the wrapping instructions as Properties.
     *
     * @return wrapping instructions as Properties
     */
    public Properties getWrappingProperties()
    {
        return m_wrappingProperties;
    }

    /**
     * Creates an MalformedURLException with a message and a cause.
     *
     * @param message exception message
     * @param cause   exception cause
     *
     * @return the created MalformedURLException
     */
    private MalformedURLException initMalformedURLException( final String message, final Exception cause )
    {
        final MalformedURLException exception = new MalformedURLException( message );
        exception.initCause( cause );
        return exception;
    }

}
