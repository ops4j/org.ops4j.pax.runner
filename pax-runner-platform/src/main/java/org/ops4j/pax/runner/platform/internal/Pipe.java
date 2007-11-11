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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * TODO add unit tests
 * TODO add Javadoc
 */
public class Pipe
    implements Runnable
{

    private final BufferedReader m_in;
    private final BufferedWriter m_out;
    private Object m_processStream;

    private volatile Thread m_thread;

    public Pipe( InputStream processStream, OutputStream systemStream )
    {
        m_in = new BufferedReader( new InputStreamReader( processStream ) );
        m_out = new BufferedWriter( new OutputStreamWriter( systemStream ) );
        m_processStream = m_in;
    }

    public Pipe( OutputStream processStream, InputStream systemStream )
    {
        m_in = new BufferedReader( new InputStreamReader( systemStream ) );
        m_out = new BufferedWriter( new OutputStreamWriter( processStream ) );
        m_processStream = m_out;
    }

    public synchronized Pipe start( final String name )
    {
        if( null == m_processStream || null != m_thread )
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
        if( null == m_processStream || null == m_thread )
        {
            return;
        }

        Thread t = m_thread;
        m_thread = null;

        t.interrupt();
    }

    public void run()
    {
        while( Thread.currentThread() == m_thread )
        {
            try
            {
                String line = m_in.readLine();
                if( line == null )
                {
                    break;
                }
                m_out.write( line );
                m_out.newLine();
                m_out.flush();
            }
            catch( IOException e )
            {
                if( Thread.currentThread() == m_thread )
                {
                    e.printStackTrace();
                }
            }
        }

        try
        {
            if( m_in == m_processStream )
            {
                m_in.close();
            }
            else
            {
                m_out.close();
            }
        }
        catch( IOException e )
        {
            // ignore
        }
        finally
        {
            m_processStream = null;
        }
    }
}
