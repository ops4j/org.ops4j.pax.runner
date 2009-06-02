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
     * Filter part of the provisioning spec.
     */
    private final String m_filter;
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
     * Filter as pattern.
     */
    private Pattern m_filterPattern;

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
            for( int i = 1; i < optionsSegments.length; i++ )
            {
                parseSegment( optionsSegments[ i ].trim() );
            }
        }
        final String[] pathSegments;
        if( optionsSegments[ 0 ].startsWith( "jar:" ) )
        {
            pathSegments = new String[]{ optionsSegments[ 0 ] };
        }
        else
        {
            pathSegments = optionsSegments[ 0 ].split( ServiceConstants.SEPARATOR_FILTER );
        }
        String path = pathSegments[ 0 ];
        if( path.endsWith( "!" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }
        m_path = path;
        if( pathSegments.length > 1 )
        {
            m_filter = pathSegments[ 1 ];
            m_filterPattern = parseFilter( m_filter );
        }
        else
        {
            m_filter = null;
            m_filterPattern = parseFilter( DEFAULT_FILTER );
        }
    }

    public ProvisionSpec( final String scheme,
                          final String path,
                          final String filter,
                          final Integer startLevel,
                          final Boolean shouldStart,
                          final Boolean shouldUpdate )
        throws MalformedSpecificationException
    {

        m_scheme = scheme;
        m_path = path;
        m_filter = filter;
        m_startLevel = startLevel;
        m_shouldStart = shouldStart;
        m_shouldUpdate = shouldUpdate;
        if( m_filter == null )
        {
            m_filterPattern = parseFilter( DEFAULT_FILTER );
        }
        else
        {
            m_filterPattern = parseFilter( m_filter );
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
     * @return path as URL
     *
     * @throws java.net.MalformedURLException - If occured while creating the URL
     */
    public URL getPathAsCachedUrl()
        throws MalformedURLException
    {
        // first check if "cache:" is supported
        try
        {
            new URL( "cache:file:foo.bar" );
            return new URL( "cache:" + getPath() );
        }
        catch( MalformedURLException e )
        {
            return getPathAsUrl();
        }
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
        if( segment.equalsIgnoreCase( OPTION_START ) )
        {
            if( m_shouldStart == null )
            {
                m_shouldStart = true;
            }
        }
        else if( segment.equalsIgnoreCase( OPTION_NO_START ) )
        {
            if( m_shouldStart == null )
            {
                m_shouldStart = false;
            }
        }
        else if( segment.equalsIgnoreCase( OPTION_UPDATE ) )
        {
            if( m_shouldUpdate == null )
            {
                m_shouldUpdate = true;
            }
        }
        else if( segment.equalsIgnoreCase( OPTION_NO_UPDATE ) )
        {
            if( m_shouldUpdate == null )
            {
                m_shouldUpdate = false;
            }
        }
        else if( m_startLevel == null )
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
    public String getFilter()
    {
        return m_filter;
    }

    /**
     * Getter.
     *
     * @return filter
     */
    public Pattern getFilterPattern()
    {
        return m_filterPattern;
    }

    /**
     * Constructs a string representation of this provision spec.
     *
     * @return a string representation of the object
     */
    public String toExternalForm()
    {
        final StringBuilder form = new StringBuilder()
            .append( getScheme() )
            .append( SEPARATOR_SCHEME )
            .append( getPath() );

        if( m_filter != null )
        {
            form.append( SEPARATOR_FILTER ).append( m_filter );
        }
        if( m_startLevel != null )
        {
            form.append( SEPARATOR_OPTION ).append( m_startLevel );
        }
        if( m_shouldStart != null )
        {
            if( m_shouldStart )
            {
                form.append( SEPARATOR_OPTION ).append( OPTION_START );
            }
            else
            {
                form.append( SEPARATOR_OPTION ).append( OPTION_NO_START );
            }
        }
        if( m_shouldUpdate != null )
        {
            if( m_shouldUpdate )
            {
                form.append( SEPARATOR_OPTION ).append( OPTION_UPDATE );
            }
            else
            {
                form.append( SEPARATOR_OPTION ).append( OPTION_NO_UPDATE );
            }
        }

        return form.toString();
    }

}
