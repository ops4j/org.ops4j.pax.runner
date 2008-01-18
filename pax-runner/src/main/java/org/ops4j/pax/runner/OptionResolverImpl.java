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
package org.ops4j.pax.runner;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.commons.Info;

/**
 * Resolvs options by:<br/>
 * 1. look in the cache;<br/>
 * 2. look in command line;<br/>
 * 3. look for an alias and if found look in the command line for the alias<br/>
 * 3. if value is "choose" ask the user;<br/>
 * 4. look in configuration for a default value
 *
 * TODO add unit testing
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public class OptionResolverImpl
    implements OptionResolver
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( OptionResolverImpl.class );
    /**
     * Command line to use.
     */
    private final CommandLine m_commandLine;
    /**
     * Configuration to use.
     */
    private final Configuration m_configuration;
    /**
     * Options cache.
     */
    private final Map<String, String> m_cache;

    /**
     * Creates anew option resolver.
     *
     * @param commandLine   command line to use
     * @param configuration configuration to use
     */
    public OptionResolverImpl( final CommandLine commandLine, final Configuration configuration )
    {
        NullArgumentException.validateNotNull( commandLine, "Command line" );
        NullArgumentException.validateNotNull( configuration, "Configuration" );
        m_commandLine = commandLine;
        m_configuration = configuration;
        m_cache = new HashMap<String, String>();
    }

    /**
     * {@inheritDoc}
     */
    public String get( final String name )
    {
        NullArgumentException.validateNotEmpty( name, "Option name" );
        LOGGER.trace( "Resolving option [" + name + "]" );
        // if is in the cache just return it
        if( m_cache.containsKey( name ) )
        {
            final String value = m_cache.get( name );
            LOGGER.trace( "Option [" + name + "] resolved to [" + value + "]" );
            return value;
        }
        // then look in the command line
        String value = m_commandLine.getOption( name );
        // maybe there is an alias for it
        if( value == null || value.trim().length() == 0 )
        {
            final String alias = m_configuration.getProperty( "alias." + name );
            if( alias != null && alias.trim().length() > 0 )
            {
                // alias could be a comma separated set of possibilities
                String[] aliases = alias.split( "," );
                for( String entry : aliases )
                {
                    value = m_commandLine.getOption( entry );
                    // we get out on first found
                    if( value != null )
                    {
                        break;
                    }
                }
            }
        }
        // maybe is a "choose" option, so ask the user
        if( value != null && "choose".equalsIgnoreCase( value ) )
        {
            Info.print( "your " + name.toLowerCase() + "? " );
            value = User.ask();
        }
        // and finally look for a default
        if( value == null || value.trim().length() == 0 )
        {
            value = m_configuration.getProperty( "default." + name );
        }
        // try to replace the shortcuts
        if( value != null && value.trim().length() > 0 )
        {
            // the value must be a comma separated list so replace each value
            final String[] segments = value.split( "," );
            final StringBuilder newValue = new StringBuilder();
            for( String segment : segments )
            {
                if( newValue.length() > 0 )
                {
                    newValue.append( "," );
                }
                final String replacer = m_configuration.getProperty( "alias." + name + "." + segment );
                if( replacer != null )
                {
                    newValue.append( replacer );
                }
                else
                {
                    newValue.append( segment );
                }
            }
            value = newValue.toString();
        }
        m_cache.put( name, value );
        LOGGER.trace( "Option [" + name + "] resolved to [" + value + "]" );
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public String getMandatory( final String name )
    {
        final String value = get( name );
        if( value == null )
        {
            throw new MissingOptionException( name );
        }
        return value;
    }

}
