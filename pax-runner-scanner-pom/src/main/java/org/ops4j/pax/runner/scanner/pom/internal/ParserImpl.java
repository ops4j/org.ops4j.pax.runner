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
package org.ops4j.pax.runner.scanner.pom.internal;

import java.net.MalformedURLException;
import java.net.URL;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;

/**
 * Implementation of parser.
 *
 * @author Alin Dreghiciu
 * @since September 17, 2007
 */
public class ParserImpl
    implements Parser
{

    /**
     * Syntax for the url; to be shown on exception messages.
     */
    private static final String SYNTAX =
        "scan-pom:pom_xml_url[@start_level][@nostart][@nodep][@noartifact][@wrapdep][@wrapartifact]";
    /**
     * Separator for options.
     */
    private static final String SEPARATOR = "@";
    /**
     * Start option.
     */
    private static final String START = "nostart";

    /**
     * URL of pom file.
     */
    private URL m_pomURL;
    /**
     * The start level option.
     */
    private Integer m_startLevel;
    /**
     * The start option.
     */
    private Boolean m_shouldStart;

    /**
     * Creates a new protocol parser.
     *
     * @param path the path part of the url (without starting scan-pom:)
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
        if( path.startsWith( SEPARATOR ) || path.endsWith( SEPARATOR ) )
        {
            throw new MalformedSpecificationException(
                "Path cannot start or end with " + SEPARATOR + ". Syntax " + SYNTAX
            );
        }
        String[] segments = path.split( SEPARATOR );
        try
        {
            m_pomURL = new URL( segments[ 0 ] );
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
     * @see Parser#getPomURL()
     */
    public URL getPomURL()
    {
        return m_pomURL;
    }

    /**
     * @see Parser#getStartLevel()
     */
    public Integer getStartLevel()
    {
        return m_startLevel;
    }

    /**
     * @see Parser#shouldStart()
     */
    public Boolean shouldStart()
    {
        return m_shouldStart;
    }

}
