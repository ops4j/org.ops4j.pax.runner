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
package org.ops4j.pax.runner.handler.classpath.internal;

import java.net.MalformedURLException;

/**
 * Parser for classpath: protocol.
 *
 * @author Alin Dreghiciu
 * @see org.ops4j.pax.runner.handler.classpath.internal.Connection
 * @since August 15, 2007
 */
public class Parser
{

    /**
     * Syntax for the url; to be shown on exception messages.
     */
    private static final String SYNTAX = "classpath:[//bundle_symbolic_name/]path_to_resource";
    /**
     * Parsed resource name.
     */
    private String m_resourceName;

    /**
     * Creates a new protocol parser.
     *
     * @param path the path part of the url (without starting classpath:)
     *
     * @throws java.net.MalformedURLException if provided path does not comply to expected syntax
     */
    public Parser( final String path )
        throws MalformedURLException
    {
        if ( path == null )
        {
            throw new MalformedURLException( "Path cannot be null. Syntax " + SYNTAX );
        }
        if ( "".equals( path.trim() ) || "/".equals( path.trim() ) )
        {
            throw new MalformedURLException( "Path cannot be empty. Syntax " + SYNTAX );
        }
        m_resourceName = path;
        // remove leading slash 
        if ( m_resourceName.startsWith( "/" ) )
        {
            m_resourceName = m_resourceName.substring( 1 );
        }
    }

    /**
     * Return the parsed resource name.
     *
     * @return parsed resource name
     */
    public String getResourceName()
    {
        return m_resourceName;
    }

}
