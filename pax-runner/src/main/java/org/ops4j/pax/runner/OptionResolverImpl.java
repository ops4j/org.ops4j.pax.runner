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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.lang.NullArgumentException;
import static org.ops4j.pax.runner.CommandLine.*;
import org.ops4j.pax.runner.commons.Info;
import org.ops4j.pax.url.mvn.ServiceConstants;

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
    private final Map<String, String> m_cacheOptions;
    /**
     * Multiple options cache.
     */
    private final Map<String, String[]> m_cacheMultipleOptions;

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
        m_cacheOptions = new HashMap<String, String>();
        m_cacheMultipleOptions = new HashMap<String, String[]>();
    }

    /**
     * {@inheritDoc}
     */
    public String get( final String name )
    {
        final String result = getInternal( name );
        // resolve some omplicit options

        // if using profiles pax runner repository is automatically added
        if( name.equalsIgnoreCase( ServiceConstants.PROPERTY_REPOSITORIES )
            && ( result == null || result.trim().startsWith( "+" ) ) )
        {
            final String profiles = get( OPTION_PROFILES );
            if( profiles == null || profiles.trim().length() == 0 )
            {
                return result;
            }
            final String profilesRepo = get( OPTION_PROFILES_REPO );
            if( profilesRepo == null || profilesRepo.trim().length() == 0 )
            {
                return result;
            }
            if( result == null || result.trim().length() == 0 )
            {
                return "+" + profilesRepo;
            }
            return result + "," + profilesRepo;
        }

        // using executor=inProcess needs --absoluteFilePaths
        if( name.equalsIgnoreCase( org.ops4j.pax.runner.platform.ServiceConstants.CONFIG_USE_ABSOLUTE_FILE_PATHS )
            && result == null )
        {
            final String executor = get( OPTION_EXECUTOR );
            if( "inProcess".equalsIgnoreCase( executor ) )
            {
                return "TRUE";
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    private String getInternal( final String name )
    {
        NullArgumentException.validateNotEmpty( name, "Option name" );
        LOGGER.trace( "Resolving option [" + name + "]" );
        // if is in the cache just return it
        if( m_cacheOptions.containsKey( name ) )
        {
            final String value = m_cacheOptions.get( name );
            LOGGER.trace( "Option [" + name + "] resolved to [" + value + "]" );
            return value;
        }
        // then look in the command line
        String value = getOption( name );
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
                    value = getOption( entry );
                    // let's also try out a case insensitive version
                    if( value == null )
                    {
                        value = getOption( entry.toLowerCase() );
                    }
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
        m_cacheOptions.put( name, value );
        LOGGER.trace( "Option [" + name + "] resolved to [" + value + "]" );
        return value;
    }

    /**
     * Gets an option value by first looking into {@link CommandLine} and if not found by loking into system properties.
     *
     * @param key option name
     *
     * @return found value, or null if not found
     */
    private String getOption( final String key )
    {
        String value = m_commandLine.getOption( key );
        if( value == null )
        {
            value = System.getProperty( key );
        }
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

    /**
     * {@inheritDoc}
     */
    public String[] getMultiple( final String name )
    {
        NullArgumentException.validateNotEmpty( name, "Option name" );
        LOGGER.trace( "Resolving option [" + name + "]" );
        // if is in the cache just return it
        if( m_cacheMultipleOptions.containsKey( name ) )
        {
            final String[] values = m_cacheMultipleOptions.get( name );
            LOGGER.trace( "Option [" + name + "] resolved to [" + Arrays.toString( values ) + "]" );
            return values;
        }
        // then look in the command line
        String[] values = m_commandLine.getMultipleOption( name );
        // maybe there is an alias for it
        if( values.length == 0 )
        {
            final String alias = m_configuration.getProperty( "alias." + name );
            if( alias != null && alias.trim().length() > 0 )
            {
                // alias could be a comma separated set of possibilities
                String[] aliases = alias.split( "," );
                for( String entry : aliases )
                {
                    values = m_commandLine.getMultipleOption( entry );
                    // let's also try out a case insensitive version
                    if( values.length == 0 )
                    {
                        values = m_commandLine.getMultipleOption( entry.toLowerCase() );
                    }
                    // we get out on first found
                    if( values.length == 0 )
                    {
                        break;
                    }
                }
            }
        }
        // try to replace the shortcuts
        if( values.length > 0 )
        {
            int index = 0;
            for( String value : values )
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
                values[ index ] = newValue.toString();
                index++;
            }
        }
        m_cacheMultipleOptions.put( name, values );
        LOGGER.trace( "Option [" + name + "] resolved to [" + Arrays.toString( values ) + "]" );
        return values;
    }

}
