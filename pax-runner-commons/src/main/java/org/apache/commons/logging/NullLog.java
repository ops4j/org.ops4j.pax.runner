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
package org.apache.commons.logging;

import static org.apache.commons.logging.LogLevel.*;
import org.ops4j.pax.runner.commons.Info;

public class NullLog implements Log
{

    private final LogLevel m_logLevel;
    private static Log m_me;

    private NullLog( LogLevel logLevel )
    {
        m_logLevel = logLevel;
    }

    public boolean isDebugEnabled()
    {
        return ge( m_logLevel, DEBUG );
    }

    public boolean isErrorEnabled()
    {
        return ge( m_logLevel, ERROR );
    }

    public boolean isFatalEnabled()
    {
        return ge( m_logLevel, FATAL );
    }

    public boolean isInfoEnabled()
    {
        return ge( m_logLevel, INFO );
    }

    public boolean isTraceEnabled()
    {
        return ge( m_logLevel, TRACE );
    }

    public boolean isWarnEnabled()
    {
        return ge( m_logLevel, WARNING );
    }

    public void trace( Object message )
    {
        if( message != null && isTraceEnabled() )
        {
            Info.println( message.toString() );
        }
    }

    public void trace( Object message, Throwable t )
    {
        if( isTraceEnabled() )
        {
            if( message != null )
            {
                Info.println( message.toString() );
            }
            if( t != null )
            {
                t.printStackTrace();
            }
        }
    }

    public void debug( Object message )
    {
        if( message != null && isDebugEnabled() )
        {
            Info.println( message.toString() );
        }
    }

    public void debug( Object message, Throwable t )
    {
        if( isDebugEnabled() )
        {
            if( message != null )
            {
                Info.println( message.toString() );
            }
            if( t != null )
            {
                t.printStackTrace();
            }
        }
    }

    public void info( Object message )
    {
        if( message != null && isInfoEnabled() )
        {
            Info.println( message.toString() );
        }
    }

    public void info( Object message, Throwable t )
    {
        if( isInfoEnabled() )
        {
            if( message != null )
            {
                Info.println( message.toString() );
            }
            if( t != null )
            {
                t.printStackTrace();
            }
        }
    }

    public void warn( Object message )
    {
        if( message != null && isWarnEnabled() )
        {
            Info.println( message.toString() );
        }
    }

    public void warn( Object message, Throwable t )
    {
        if( isWarnEnabled() )
        {
            if( message != null )
            {
                Info.println( message.toString() );
            }
            if( t != null )
            {
                t.printStackTrace();
            }
        }
    }

    public void error( Object message )
    {
        if( message != null && isErrorEnabled() )
        {
            Info.println( message.toString() );
        }
    }

    public void error( Object message, Throwable t )
    {
        if( isErrorEnabled() )
        {
            if( message != null )
            {
                Info.println( message.toString() );
            }
            if( t != null )
            {
                t.printStackTrace();
            }
        }
    }

    public void fatal( Object message )
    {
        if( message != null && isFatalEnabled() )
        {
            Info.println( message.toString() );
        }
    }

    public void fatal( Object message, Throwable t )
    {
        if( isFatalEnabled() )
        {
            if( message != null )
            {
                Info.println( message.toString() );
            }
            if( t != null )
            {
                t.printStackTrace();
            }
        }
    }

    public int getLogLevel()
    {
        return m_logLevel.ordinal();
    }

    public static Log newInstance()
    {
        return newInstance( NONE );
    }

    public static Log newInstance( final LogLevel logLevel )
    {
        if( m_me == null )
        {
            m_me = new NullLog( logLevel );
        }
        return m_me;
    }

}
