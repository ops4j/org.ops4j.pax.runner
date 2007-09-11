/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Default implementation of Command Line.
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public class CommandLineImpl implements CommandLine
{

    /**
     * Option profix.
     */
    private static final String OPTION_PREFIX = "--";
    /**
     * Option name, option value separator.
     */
    private static final String EQ = "=";

    /**
     * Options as properties.
     */
    private final Properties m_options;
    /**
     * List of arguments.
     */
    private final List<String> m_arguments;

    /**
     * Creates a new Command line by parsing every argument into an option or argument.
     *
     * @param args an array of arguments to be parsed
     */
    public CommandLineImpl( final String... args )
    {
        m_options = new Properties();
        m_arguments = new ArrayList<String>();

        for( String arg : args )
        {
            if( arg.startsWith( OPTION_PREFIX ) )
            {
                parseOption( arg );
            }
            else
            {
                parseArgument( arg );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getOption( final String key )
    {
        return m_options.getProperty( key );
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getArguments()
    {
        return m_arguments;
    }

    /**
     * Parses an option of type --name=value
     *
     * @param arg a command line argument to be parsed
     */
    private void parseOption( final String arg )
    {
        final StringTokenizer tokenizer = new StringTokenizer( arg.substring( 2 ), EQ );
        final String key = tokenizer.nextToken();
        if( key != null && !"".equals( key.trim() ) )
        {
            if( tokenizer.hasMoreTokens() )
            {
                m_options.put( key, tokenizer.nextToken() );
            }
            else
            {
                if( key.startsWith( "no" ) && key.length() > 2 )
                {
                    String actualKey = key.substring( 2, 3 ).toLowerCase();
                    if( key.length() >= 3 )
                    {
                        actualKey = actualKey + key.substring( 3 );
                    }
                    m_options.put( actualKey, "false" );
                }
                else
                {
                    m_options.put( key, "true" );
                }
            }
        }
    }

    /**
     * Parses and argument (does not start with --).
     *
     * @param arg a command line argument to be parsed
     */
    private void parseArgument( final String arg )
    {
        m_arguments.add( arg );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Arguments: " );
        for( String entry : m_arguments )
        {
            builder
                .append( "[" )
                .append( entry )
                .append( "]" );
        }
        builder.append( "Options: " );
        for( Object key : m_options.keySet() )
        {
            builder
                .append( "[" )
                .append( key )
                .append( "=" )
                .append( m_options.getProperty( (String) key ) )
                .append( "]" );
        }
        return builder.toString();
    }

}
