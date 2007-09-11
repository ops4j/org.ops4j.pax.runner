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
package org.ops4j.pax.runner.scanner.dir.internal;

import java.net.MalformedURLException;
import java.util.regex.Pattern;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;

/**
 * Implementation of parser.
 *
 * @author Alin Dreghiciu
 * @since September 04, 2007
 */
public class ParserImpl
    implements Parser
{

    /**
     * Syntax for the url; to be shown on exception messages.
     */
    private static final String SYNTAX = "directory_url[!filter][@start_level][@nostart]";
    /**
     * Separator for options.
     */
    private static final String SEPARATOR_OPTIONS = "@";
    /**
     * Separator for filter.
     */
    private static final String SEPARATTOR_FILTER = "!/";
    /**
     * Default filter to be used if none specified (match all entries form the root directory, not recursive.
     */
    private static final String DEFAULT_FILTER = "*";
    /**
     * Start option.
     */
    private static final String START = "nostart";

    /**
     * URL to file containing the bundles to be installed.
     */
    private String m_URL;
    /**
     * The start level option.
     */
    private Integer m_startLevel;
    /**
     * The start option.
     */
    private Boolean m_shouldStart;
    /**
     * Filter.
     */
    private Pattern m_filter;

    /**
     * Creates a new protocol parser.
     *
     * @param path the path part of the url (without starting scan-file:)
     *
     * @throws MalformedSpecificationException
     *          if provided path does not comply to expected syntax or an malformed file URL
     */
    public ParserImpl( final String path )
        throws MalformedSpecificationException
    {
        if( path == null || path.trim().length() == 0 )
        {
            throw new MalformedSpecificationException( "Path cannot be null or empty. Syntax " + SYNTAX );
        }
        if( path.startsWith( SEPARATOR_OPTIONS ) || path.endsWith( SEPARATOR_OPTIONS ) )
        {
            throw new MalformedSpecificationException(
                "Path cannot start or end with " + SEPARATOR_OPTIONS + ". Syntax " + SYNTAX
            );
        }
        final String[] segments = path.split( SEPARATOR_OPTIONS );
        try
        {
            final String[] urlParts = segments[ 0 ].split( SEPARATTOR_FILTER );
            String url = urlParts[ 0 ];
            if( url.endsWith( "!" ) )
            {
                url = url.substring( 0, url.length() - 1 );
            }
            m_URL = url;
            if( urlParts.length > 1 )
            {
                m_filter = parseFilter( urlParts[ 1 ] );
            }
            else
            {
                m_filter = parseFilter( DEFAULT_FILTER );
            }
        }
        catch( MalformedURLException e )
        {
            throw new MalformedSpecificationException( "Invalid url", e );
        }
        if( segments.length > 1 )
        {
            for( int i = 1; i < segments.length; i++ )
            {
                parseSegment( segments[ i ].trim() );
            }
        }
    }

    /**
     * Parses the options. If the value is not one of the syntax options will throw an exception.
     *
     * @param segment an option from the provided path part of the url
     *
     * @throws MalformedSpecificationException
     *          if provided path does not comply to syntax.
     */
    private void parseSegment( final String segment )
        throws MalformedSpecificationException
    {
        if( m_shouldStart == null && segment.equalsIgnoreCase( START ) )
        {
            m_shouldStart = false;
            return;
        }
        if( m_startLevel == null )
        {
            try
            {
                m_startLevel = Integer.parseInt( segment );
                return;
            }
            catch( NumberFormatException e )
            {
                throw new MalformedSpecificationException( "Invalid option [" + segment + "]. Syntax " + SYNTAX );
            }
        }
        throw new MalformedSpecificationException( "Duplicate option [" + segment + "]. Syntax " + SYNTAX );
    }

    /**
     * Parses a usual filer into a regex pattern.
     *
     * @param spec the filter to be parsed
     *
     * @return a regexp pattern corresponding to the filter
     *
     * @throws org.ops4j.pax.runner.provision.MalformedSpecificationException
     *          if the filter could not be compiled to a pattern
     */
    static Pattern parseFilter( final String spec )
        throws MalformedSpecificationException
    {
        StringBuffer sb = new StringBuffer();
        for( int j = 0; j < spec.length(); j++ )
        {
            char c = spec.charAt( j );
            switch( c )
            {
                case'.':
                    sb.append( "\\." );
                    break;

                case'*':
                    // test for ** (all directories)
                    if( j < spec.length() - 1 && spec.charAt( j + 1 ) == '*' )
                    {
                        sb.append( ".*" );
                        j++;
                    }
                    else
                    {
                        sb.append( "[^/]*" );
                    }
                    break;
                default:
                    sb.append( c );
                    break;
            }
        }
        String s = sb.toString();
        try
        {
            return Pattern.compile( s );
        }
        catch( Exception e )
        {
            throw new MalformedSpecificationException( "Invalid characted used in the filter name", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        return m_URL;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getStartLevel()
    {
        return m_startLevel;
    }

    /**
     * {@inheritDoc}
     */
    public Boolean shouldStart()
    {
        return m_shouldStart;
    }

    /**
     * {@inheritDoc}
     */
    public Pattern getFilter()
    {
        return m_filter;
    }

}
