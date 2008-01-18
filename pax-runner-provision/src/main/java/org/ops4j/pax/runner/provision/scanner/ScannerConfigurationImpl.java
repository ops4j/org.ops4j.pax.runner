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

import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.commons.resolver.ConfigurationMap;
import org.ops4j.pax.runner.provision.ServiceConstants;
import org.ops4j.util.property.PropertyResolver;

/**
 * ScannerConfiguration implementation.
 *
 * @author Alin Dreghiciu
 * @see ScannerConfiguration
 * @since August 11, 2007
 */
public class ScannerConfigurationImpl
    extends ConfigurationMap
    implements ScannerConfiguration
{

    /**
     * Property resolver. Cannot be null.
     */
    private final PropertyResolver m_propertyResolver;
    /**
     * Persistent identifier for scanner.
     */
    private final String m_pid;

    /**
     * Creates a new service configuration.
     *
     * @param propertyResolver propertyResolver used to resolve properties; mandatory
     * @param pid              scanner pid
     */
    public ScannerConfigurationImpl( final PropertyResolver propertyResolver, final String pid )
    {
        NullArgumentException.validateNotNull( propertyResolver, "Property propertyResolver" );
        NullArgumentException.validateNotNull( pid, "PID" );
        m_propertyResolver = propertyResolver;
        m_pid = pid;
    }

    /**
     * @see ScannerConfiguration#getStartLevel()
     */
    public Integer getStartLevel()
    {
        if( !contains( m_pid + ServiceConstants.PROPERTY_START_LEVEL ) )
        {
            final String value = m_propertyResolver.get( m_pid + ServiceConstants.PROPERTY_START_LEVEL );
            if( value != null )
            {
                try
                {
                    return set( m_pid + ServiceConstants.PROPERTY_START_LEVEL, Integer.valueOf( value ) );
                }
                catch( NumberFormatException e )
                {
                    // do nothing
                }
            }
            set( m_pid + ServiceConstants.PROPERTY_START_LEVEL, null );
        }
        return get( m_pid + ServiceConstants.PROPERTY_START_LEVEL );
    }

    /**
     * @see ScannerConfiguration#shouldStart()
     */
    public Boolean shouldStart()
    {
        if( !contains( m_pid + ServiceConstants.PROPERTY_START ) )
        {
            final String value = m_propertyResolver.get( m_pid + ServiceConstants.PROPERTY_START );
            if( value == null )
            {
                return set( m_pid + ServiceConstants.PROPERTY_START, Boolean.TRUE );
            }
            else
            {
                return set( m_pid + ServiceConstants.PROPERTY_START, Boolean.valueOf( value ) );
            }
        }
        return get( m_pid + ServiceConstants.PROPERTY_START );
    }

    /**
     * @see ScannerConfiguration#shouldUpdate()
     */
    public Boolean shouldUpdate()
    {
        if( !contains( m_pid + ServiceConstants.PROPERTY_UPDATE ) )
        {
            final String value = m_propertyResolver.get( m_pid + ServiceConstants.PROPERTY_UPDATE );
            if( value == null )
            {
                return set( m_pid + ServiceConstants.PROPERTY_UPDATE, Boolean.FALSE );
            }
            else
            {
                return set( m_pid + ServiceConstants.PROPERTY_UPDATE, Boolean.valueOf( value ) );
            }
        }
        return get( m_pid + ServiceConstants.PROPERTY_UPDATE );
    }
}
