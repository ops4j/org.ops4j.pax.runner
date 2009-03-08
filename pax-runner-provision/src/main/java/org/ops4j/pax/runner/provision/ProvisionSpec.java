/*
 * Copyright 2009 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.runner.provision;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import static org.ops4j.pax.runner.provision.ServiceConstants.*;

/**
 * Provisioning specification.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.18.0, March 08, 2009
 */
public class ProvisionSpec
{

    /**
     * Default filter to be used if none specified (match all entries form the root directory, not recursive.
     */
    private static final String DEFAULT_FILTER = "*";

    /**
     * Scheme part of the provisioning spec.
     */
    private final String m_scheme;
    /**
     * Path part of the provisioning spec.
     */
    private final String m_path;
    /**
     * Provisioning spec options.
     */
    private final String[] m_options;
    /**
     * The start level option.
     */
    private Integer m_startLevel;
    /**
     * The start option.
     */
    private Boolean m_shouldStart;
    /**
     * The update option.
     */
    private Boolean m_shouldUpdate;
    /**
     * Filter.
     */
    private Pattern m_filter;

    /**
     * Constructor.
     *
     * @param spec provisioning spec
     *
     * @throws MalformedSpecificationException
     *          - If spec is null
     *          - If spec is empty
     *          - If provisioning scheme is not specified
     *          - Starts or ends with {@link ServiceConstants#SEPARATOR_OPTION}
     */
    public ProvisionSpec( final String spec )
        throws MalformedSpecificationException
    {
        if( spec == null || spec.trim().length() == 0 )
        {
            throw new MalformedSpecificationException( "Specification cannot be null or empty" );
        }
        if( !spec.contains( SEPARATOR_SCHEME ) )
        {
            throw new UnsupportedSchemaException( "Provisioning scheme is not specified" );
        }
        m_scheme = spec.substring( 0, spec.indexOf( SEPARATOR_SCHEME ) );
        final String fullPath = spec.substring( spec.indexOf( SEPARATOR_SCHEME ) + 1 );
        if( fullPath == null || fullPath.trim().length() == 0 )
        {
            throw new MalformedSpecificationException( "Path cannot be null or empty." );
        }
        if( fullPath.startsWith( SEPARATOR_OPTION ) || fullPath.endsWith( SEPARATOR_OPTION ) )
        {
            throw new MalformedSpecificationException( "Path cannot start or end with " + SEPARATOR_OPTION );
        }
        final String[] optionsSegments = fullPath.split( SEPARATOR_OPTION );
        if( optionsSegments.length > 1 )
        {
            m_options = new String[optionsSegments.length - 1];
            for( int i = 1; i < optionsSegments.length; i++ )
            {
                m_options[ i - 1 ] = optionsSegments[ i ].trim();
                parseSegment( m_options[ i - 1 ] );
            }
        }
        else
        {
            m_options = new String[0];
        }
        final String[] pathSegments = optionsSegments[ 0 ].split( ServiceConstants.SEPARATOR_FILTER );
        String path = pathSegments[ 0 ];
        if( path.endsWith( "!" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }
        m_path = path;
        if( pathSegments.length > 1 )
        {
            m_filter = parseFilter( pathSegments[ 1 ] );
        }
        else
        {
            m_filter = parseFilter( DEFAULT_FILTER );
        }
    }

    /**
     * Getter.
     *
     * @return scheme
     */
    public String getScheme()
    {
        return m_scheme;
    }

    /**
     * Getter.
     *
     * @return path
     */
    public String getPath()
    {
        return m_path;
    }

    /**
     * Getter.
     *
     * @return path as URL
     *
     * @throws java.net.MalformedURLException - If occured while creating the URL
     */
    public URL getPathAsUrl()
        throws MalformedURLException
    {
        return new URL( getPath() );
    }

    /**
     * Getter.
     *
     * @return options
     */
    public String[] getOptions()
    {
        return m_options;
    }

    /**
     * Verify if the path is an valid url.
     *
     * @return true if path is a valid url, false otherwise
     */
    public boolean isPathValidUrl()
    {
        try
        {
            new URL( m_path );
            return true;
        }
        catch( MalformedURLException e )
        {
            return false;
        }
    }

    /**
     * Parses some of the known options options.
     *
     * @param segment an option from the provided path part of the url
     */
    private void parseSegment( final String segment )
    {
        if( m_shouldStart == null && segment.equalsIgnoreCase( OPTION_NO_START ) )
        {
            m_shouldStart = false;
            return;
        }
        if( m_shouldUpdate == null && segment.equalsIgnoreCase( OPTION_UPDATE ) )
        {
            m_shouldUpdate = true;
            return;
        }
        if( m_startLevel == null )
        {
            try
            {
                m_startLevel = Integer.parseInt( segment );
            }
            catch( NumberFormatException ignore )
            {
            }
        }
    }

    /**
     * Parses a usual filter into a regex pattern.
     *
     * @param spec the filter to be parsed
     *
     * @return a regexp pattern corresponding to the filter
     *
     * @throws MalformedSpecificationException
     *          - If the filter could not be compiled to a pattern
     */
    public static Pattern parseFilter( final String spec )
        throws MalformedSpecificationException
    {
        StringBuffer sb = new StringBuffer();
        for( int j = 0; j < spec.length(); j++ )
        {
            char c = spec.charAt( j );
            switch( c )
            {
                case '.':
                    sb.append( "\\." );
                    break;

                case '*':
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
     * Getter.
     *
     * @return start level if option present, null otherwise
     */
    public Integer getStartLevel()
    {
        return m_startLevel;
    }

    /**
     * Getter.
     *
     * @return true scanned bundles should be started, false otherwise if option is present, null otherwise
     */
    public Boolean shouldStart()
    {
        return m_shouldStart;
    }

    /**
     * Getter.
     *
     * @return true scanned bundles should be updated, false otherwise if option is present, null otherwise
     */
    public Boolean shouldUpdate()
    {
        return m_shouldUpdate;
    }

    /**
     * Getter.
     *
     * @return filter
     */
    public Pattern getFilter()
    {
        return m_filter;
    }

}
