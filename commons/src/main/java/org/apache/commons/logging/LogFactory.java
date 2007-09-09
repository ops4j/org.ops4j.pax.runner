/* Copyright 2007 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.logging;

public class LogFactory
{

    /**
     * Logger factory instance in use.
     */
    private static LogFactory m_instance = new LogFactory();
    /**
     * Logger to use.
     */
    private Log m_log;

    /**
     * Private constructor as this is ment to be used via static factory methods.
     */
    private LogFactory()
    {
    }

    public static Log getLog( Class clazz )
    {
        return getFactory().getInstance( clazz.getName() );
    }

    public static Log getLog( Class clazz, LogLevel logLevel )
    {
        return getFactory().getInstance( clazz.getName(), logLevel );
    }

    public static Log getLog( String name )
    {
        return getFactory().getInstance( name );
    }

    public Log getInstance( Class clazz )
    {
        return getInstance( clazz.getName() );
    }

    public Log getInstance( String name )
    {
        if ( m_log == null )
        {
            m_log = NullLog.newInstance();
        }
        return m_log;
    }

    public Log getInstance( String name, LogLevel logLevel )
    {
        if ( m_log == null )
        {
            m_log = NullLog.newInstance( logLevel );
        }
        return m_log;
    }

    public static LogFactory getFactory()
    {
        return m_instance;
    }


}
