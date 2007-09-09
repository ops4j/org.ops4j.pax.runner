/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.runner.platform.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO add unit tests
 * TODO add Javadoc
 */
public class Pipe
    implements Runnable
{

    private final InputStream m_in;
    private final OutputStream m_out;
    private Object m_processStream;

    private volatile Thread m_thread;

    public Pipe( InputStream processStream, OutputStream systemStream )
    {
        m_in = new BufferedInputStream( processStream );
        m_out = new BufferedOutputStream( systemStream );
        m_processStream = m_in;
    }

    public Pipe( OutputStream processStream, InputStream systemStream )
    {
        m_in = new BufferedInputStream( systemStream );
        m_out = new BufferedOutputStream( processStream );
        m_processStream = m_out;
    }

    public synchronized Pipe start( final String name )
    {
        if ( null == m_processStream || null != m_thread )
        {
            return this;
        }

        m_thread = new Thread( this, name );
        m_thread.setDaemon( true );
        m_thread.start();
        return this;
    }

    public synchronized void stop()
    {
        if ( null == m_processStream || null == m_thread )
        {
            return;
        }

        Thread t = m_thread;
        m_thread = null;

        t.interrupt();
    }

    public void run()
    {
        while ( Thread.currentThread() == m_thread )
        {
            try
            {
                int ch = m_in.read();
                if ( ch == -1 )
                {
                    break;
                }
                m_out.write( ch );
                m_out.flush();
            }
            catch ( IOException e )
            {
                if ( Thread.currentThread() == m_thread )
                {
                    e.printStackTrace();
                }
            }
        }

        try
        {
            if ( m_in == m_processStream )
            {
                m_in.close();
            }
            else
            {
                m_out.close();
            }
        }
        catch ( IOException e )
        {
            // ignore
        }
        finally
        {
            m_processStream = null;
        }
    }
}
