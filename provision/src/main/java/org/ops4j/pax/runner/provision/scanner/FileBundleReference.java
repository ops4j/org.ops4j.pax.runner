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
package org.ops4j.pax.runner.provision.scanner;

import java.net.MalformedURLException;
import org.ops4j.pax.runner.provision.BundleReferenceBean;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;

/**
 * Represents an entry in the scanned file..
 *
 * @author Alin Dreghiciu
 * @since August 18, 2007
 */
public class FileBundleReference
    extends BundleReferenceBean
{

    /**
     * Syntax for the url; to be shown on exception messages.
     */
    private static final String SYNTAX = "bundle_url[@start_level][@nostart]";
    /**
     * Separator for options.
     */
    private static final String SEPARATOR = "@";
    /**
     * Start option.
     */
    private static final String NO_START = "nostart";

    /**
     * Creates a new bundle reference based on a bundle reference.
     *
     * @param reference the bundle reference specification
     *
     * @throws MalformedSpecificationException
     *          if the reference is malformed
     */
    public FileBundleReference( final String reference )
        throws MalformedSpecificationException
    {
        if ( reference == null || "".equals( reference.trim() ) )
        {
            throw new MalformedSpecificationException( "Reference cannot be null or empty" );
        }
        if ( reference.trim().length() == 0 )
        {
            throw new MalformedSpecificationException( "Path cannot be empty. Syntax " + SYNTAX );
        }
        if ( reference.startsWith( SEPARATOR ) || reference.endsWith( SEPARATOR ) )
        {
            throw new MalformedSpecificationException(
                "Path cannot start or end with " + SEPARATOR + ". Syntax " + SYNTAX
            );
        }
        String[] segments = reference.split( SEPARATOR );
        setLocation( segments[ 0 ] );
        if ( segments.length > 1 )
        {
            for ( int i = 1; i < segments.length; i++ )
            {
                try
                {
                    parseSegment( segments[ i ].trim() );
                }
                catch ( MalformedURLException e )
                {
                    throw new MalformedSpecificationException( e );
                }
            }
        }
        if ( shouldStart() == null )
        {
            setShouldStart( true );
        }
    }

    /**
     * Creates a new bundle reference based on a bundle reference with default start level and start options.
     *
     * @param reference          the bundle reference specification
     * @param defaultStartLevel  default start level if not set on the bundle reference
     * @param defaultShouldStart default start if not set on the bundle reference
     *
     * @throws MalformedURLException if the reference is malformed
     */
    public FileBundleReference( final String reference, final Integer defaultStartLevel,
                                final Boolean defaultShouldStart )
        throws MalformedURLException
    {
        this( reference );
        if ( defaultStartLevel != null && getStartLevel() == null )
        {
            setStartLevel( defaultStartLevel );
        }
        // by default should start is true so if we have a default passed then use this one
        if ( defaultShouldStart != null && ( shouldStart() == null || shouldStart() ) )
        {
            setShouldStart( defaultShouldStart );
        }
    }

    /**
     * Parses the options. If the value is not one of the syntax options will throw an exception.
     *
     * @param segment an option from the provided path part of the url
     *
     * @throws MalformedURLException if provided path does not comply to syntax.
     */
    private void parseSegment( final String segment )
        throws MalformedURLException
    {
        if ( shouldStart() == null && segment.equalsIgnoreCase( NO_START ) )
        {
            setShouldStart( false );
            return;
        }
        if ( getStartLevel() == null )
        {
            try
            {
                setStartLevel( Integer.parseInt( segment ) );
                return;
            }
            catch ( NumberFormatException e )
            {
                throw new MalformedURLException( "Invalid option [" + segment + "]. Syntax " + SYNTAX );
            }
        }
        throw new MalformedURLException( "Duplicate option [" + segment + "]. Syntax " + SYNTAX );
    }

}
