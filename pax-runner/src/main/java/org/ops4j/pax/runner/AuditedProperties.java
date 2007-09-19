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

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Porperties that audits changes of properties from the moment that was created.
 *
 * @author Alin Dreghiciu
 * @since 0.5.0
 */
public class AuditedProperties
    extends Properties
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( AuditedProperties.class );

    /**
     * Default properties to be used.
     */
    final Properties m_defaults;

    /**
     * Creates an empty property list with the specified defaults.
     *
     * @param defaults the defaults.
     */
    public AuditedProperties( Properties defaults )
    {
        super();
        m_defaults = defaults != null ? defaults : new Properties();
    }

    /**
     * Delegate to defaults if not found.
     *
     * @see java.util.Properties#getProperty(String)
     */
    @Override
    public String getProperty( String key )
    {
        return getProperty( key, null );
    }

    /**
     * Delegate to defaults if not found.
     *
     * @see java.util.Properties#getProperty(String,String)
     */
    @Override
    public String getProperty( String key, String defaultValue )
    {
        String value = super.getProperty( key );
        if( value == null )
        {
            value = m_defaults.getProperty( key, defaultValue );
        }
        return value;
    }

    @Override
    public synchronized Object setProperty( String key, String value )
    {
        LOGGER.info( "Using property [" + key + "=" + value + "]" );
        return super.setProperty( key, value );
    }

}
