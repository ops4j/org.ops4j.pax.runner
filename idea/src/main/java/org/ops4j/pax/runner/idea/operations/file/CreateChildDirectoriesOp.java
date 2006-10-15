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

public class CreateChildDirectoriesOp
    implements Runnable
{

    private VirtualFile m_dir;
    private String m_child;
    private VirtualFile m_result;
    private IOException m_exception;
    private boolean m_successful;

    public CreateChildDirectoriesOp( VirtualFile dir, String child )
    {
        m_dir = dir;
        m_child = child;
    }

    public void run()
    {
        try
        {
            m_result = createDir( m_dir, m_child );
            m_successful = true;
        } catch( IOException e )
        {
            m_exception = e;
            m_successful = false;
        }
    }

    private VirtualFile createDir( VirtualFile parent, String childName )
        throws IOException
    {
        String nextChild = null;
        int pos = childName.indexOf( '/' );
        String thisChild;
        if( pos > 0 )
        {
            thisChild = childName.substring( 0, pos );
            nextChild = childName.substring( pos + 1 );
        }
        else
        {
            thisChild = childName;
        }
        VirtualFile dir = parent.createChildDirectory( this, thisChild );
        if( nextChild != null )
        {
            return createDir( dir, nextChild );
        }
        return dir;
    }

    public VirtualFile getResult()
    {
        return m_result;
    }

    public boolean isSuccessful()
    {
        return m_successful;
    }

    public IOException getException()
    {
        return m_exception;
    }
}
