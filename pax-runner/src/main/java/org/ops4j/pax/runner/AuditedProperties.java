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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Java Properties that audits changes of properties from the moment that was created.
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
     * Pattern used for replacing placeholders.
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile( "(.*?\\$\\{)([.[^\\$]]+?)(\\}.*)" );

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
        final String replaced = replacePlaceholders( value );
        LOGGER.trace( "Setting system property [" + key + "=" + replaced + "]" );
        return super.setProperty( key, replaced );
    }


    @Override
    public Enumeration<?> propertyNames()
    {
        Set<String> combinedNames = new HashSet<String>();

        Enumeration<?> selfNames = super.propertyNames();
        while ( selfNames.hasMoreElements() )
        {
            combinedNames.add((String) selfNames.nextElement());
        }

        Enumeration<?> defaultNames = m_defaults.propertyNames();
        while ( defaultNames.hasMoreElements())
        {
            combinedNames.add((String) defaultNames.nextElement());
        }
        return Collections.enumeration(combinedNames);
    }

    @Override
    public boolean containsKey(Object key)
    {
        return super.containsKey(key) || m_defaults.containsKey(key);
    }

    /**
     * Replaces placeholders = ${*}.
     *
     * @param value the string where the place holders should be replaced
     *
     * @return replaced place holders or the original if there are no place holders or a value for place holder could
     *         not be found
     */
    private String replacePlaceholders( final String value )
    {
        String replaced = value;
        String rest = value;
        while( rest != null && rest.length() != 0 )
        {
            final Matcher matcher = PLACEHOLDER_PATTERN.matcher( rest );
            if( matcher.matches() && matcher.groupCount() == 3 )
            {
                // groups 2 contains the placeholder name
                final String placeholderName = matcher.group( 2 );
                final String placeholderValue = getProperty( placeholderName );
                if( placeholderValue != null )
                {
                    replaced = replaced.replace( "${" + placeholderName + "}", placeholderValue );
                }
                rest = matcher.group( 3 );
            }
            else
            {
                rest = null;
            }
        }
        if( replaced != null && !replaced.equals( value ) )
        {
            replaced = replacePlaceholders( replaced );
        }
        return replaced;
    }

}
