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
package org.ops4j.pax.runner.handler.wrap.internal;

import java.net.MalformedURLException;
import java.net.URL;

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
     * Wrapped jar URL.
     */
    private URL m_wrappedJarURL;
    /**
     * Wrapping instructions URL.
     */
    private URL m_instructionsURL;
    /**
     * Wrapping instructions .
     */
    private String m_instructions;

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
        if ( path == null || path.trim().length() == 0)
        {
            throw new MalformedURLException( "Path cannot be null or empty. Syntax " + SYNTAX );
        }
        if ( path.startsWith( INSTRUCTIONS_SEPARATOR ) || path.endsWith( INSTRUCTIONS_SEPARATOR ) )
        {
            throw new MalformedURLException(
                "Path cannot start or end with " + INSTRUCTIONS_SEPARATOR + ". Syntax " + SYNTAX
            );
        }
        if ( path.contains( INSTRUCTIONS_SEPARATOR ) )
        {
            int pos = path.lastIndexOf( INSTRUCTIONS_SEPARATOR );
            parseInstructions( path.substring( pos + 1 ) );
            m_wrappedJarURL = new URL( path.substring( 0, pos ) );
        }
        else
        {
            parseInstructions( path );
        }
    }

    /**
     * Parses the instructions of the url ( without the wrapped jar url).
     *
     * @param part url part without protocol and wrapped jar url.
     *
     * @throws MalformedURLException if provided path does not comply to syntax.
     */
    private void parseInstructions( final String part )
        throws MalformedURLException
    {
        // TODO implement instrcutions parsing 
    }

    /**
     * Returns the wrapped URL if present, null otherwise
     *
     * @return repository URL
     */
    public URL getWrappedJarURL()
    {
        return m_wrappedJarURL;
    }

    /**
     * Returns the wrapping instructions url if this form was used, null otherwise.
     *
     * @return wrapping instructions url
     */
    public URL getInstructionsURL()
    {
        return m_instructionsURL;
    }

    /**
     * Returns the wrapping instructions if this form was used, null otherwise.
     *
     * @return wrapping instructions
     */
    public String getInstructions()
    {
        return m_instructions;
    }

}
