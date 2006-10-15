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
package org.ops4j.pax.runner.idea.operations.file;

import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CreateFile
    implements Runnable
{

    private VirtualFile m_dir;
    private String m_fileName;
    private InputStream m_in;
    private boolean m_successful;
    private IOException m_exception;
    private VirtualFile m_result;

    public CreateFile( VirtualFile dir, String fileName, InputStream data )
    {
        super();
        m_dir = dir;
        m_fileName = fileName;
        m_in = data;
    }

    public void run()
    {
        OutputStream out = null;
        try
        {
            VirtualFile file = m_dir.createChildData( this, m_fileName );
            long now = System.currentTimeMillis();
            out = file.getOutputStream( this, now, now );
            int data = m_in.read();
            while( data >= 0 )
            {
                out.write( data );
                data = m_in.read();
            }
            out.flush();
            m_result = file;
            m_successful = true;
        } catch( IOException e )
        {
            m_exception = e;
            m_successful = false;
        } finally
        {
            if( out != null )
            {
                try
                {
                    out.close();
                } catch( IOException e )
                {
                    m_exception = e;
                    m_successful = false;
                }
            }
        }
    }

    public boolean isSuccessful()
    {
        return m_successful;
    }

    public IOException getException()
    {
        return m_exception;
    }

    public VirtualFile getResult()
    {
        return m_result;
    }
}
