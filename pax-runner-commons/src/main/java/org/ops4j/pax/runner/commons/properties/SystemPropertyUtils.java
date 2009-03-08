/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.runner.commons.properties;

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for resolving placeholders in texts. Usually applied to file paths.
 *
 * <p>A text may contain <code>${...}</code> placeholders, to be resolved as
 * system properties: e.g. <code>${user.dir}</code>.
 *
 * @author Juergen Hoeller
 * @see #PLACEHOLDER_PREFIX
 * @see #PLACEHOLDER_SUFFIX
 * @see System#getProperty(String)
 * @since 1.2.5
 */
public abstract class SystemPropertyUtils
{

    private static final Log LOGGER = LogFactory.getLog( SystemPropertyUtils.class );

    /**
     * Prefix for system property placeholders: "${"
     */
    public static final String PLACEHOLDER_PREFIX = "${";

    /**
     * Suffix for system property placeholders: "}"
     */
    public static final String PLACEHOLDER_SUFFIX = "}";

    /**
     * Resolve ${...} placeholders in the given text, replacing them with corresponding system property values.
     *
     * @param text the String to resolve
     *
     * @return the resolved String
     *
     * @see #PLACEHOLDER_PREFIX
     * @see #PLACEHOLDER_SUFFIX
     */
    public static String resolvePlaceholders( final String text )
    {
        return resolvePlaceholders( text, new Properties() );
    }

    /**
     * Resolve ${...} placeholders in the given text, replacing them with corresponding property values or system
     * property values.
     *
     * @param text       the String to resolve
     * @param properties properties to be searched beside system properties
     *
     * @return the resolved String
     *
     * @see #PLACEHOLDER_PREFIX
     * @see #PLACEHOLDER_SUFFIX
     */
    public static String resolvePlaceholders( final String text,
                                              final Properties properties )
    {
        StringBuffer buf = new StringBuffer( text );

        int startIndex = buf.indexOf( PLACEHOLDER_PREFIX );
        while( startIndex != -1 )
        {
            int endIndex = buf.indexOf( PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length() );
            if( endIndex != -1 )
            {
                String placeholder = buf.substring( startIndex + PLACEHOLDER_PREFIX.length(), endIndex );
                int nextIndex = endIndex + PLACEHOLDER_SUFFIX.length();
                try
                {
                    String propVal = properties.getProperty( placeholder );
                    if( propVal == null )
                    {
                        propVal = System.getProperty( placeholder );
                        if( propVal == null )
                        {
                            // Fall back to searching the system environment.
                            propVal = System.getenv( placeholder );
                        }
                    }
                    if( propVal != null )
                    {
                        buf.replace( startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal );
                        nextIndex = startIndex + propVal.length();
                    }
                    else
                    {
                        if( LOGGER.isWarnEnabled() )
                        {
                            LOGGER.warn( "Could not resolve placeholder '" + placeholder + "' in [" + text +
                                         "] as system property: neither system property nor environment variable found"
                            );
                        }
                    }
                }
                catch( Throwable ex )
                {
                    if( LOGGER.isWarnEnabled() )
                    {
                        LOGGER.warn( "Could not resolve placeholder '" + placeholder + "' in [" + text +
                                     "] as system property: " + ex
                        );
                    }
                }
                startIndex = buf.indexOf( PLACEHOLDER_PREFIX, nextIndex );
            }
            else
            {
                startIndex = -1;
            }
        }

        return buf.toString();
    }

}
