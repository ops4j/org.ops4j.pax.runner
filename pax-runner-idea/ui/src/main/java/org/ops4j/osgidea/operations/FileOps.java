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
package org.ops4j.osgidea.operations;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import org.ops4j.osgidea.operations.file.CreateChildDirectoriesOp;
import org.ops4j.osgidea.operations.file.CreateFile;
import java.io.IOException;
import java.io.InputStream;

public class FileOps
{
    public static VirtualFile createNestedChildDir( VirtualFile parent, String childDir )
        throws IOException
    {
        Application app = ApplicationManager.getApplication();
        CreateChildDirectoriesOp operation = new CreateChildDirectoriesOp( parent, childDir );
        app.runWriteAction( operation );
        if( operation.isSuccessful() )
        {
            return operation.getResult();
        }
        throw operation.getException();
    }

    public static VirtualFile createFile( VirtualFile dir, String fileName, InputStream data )
        throws IOException
    {
        Application app = ApplicationManager.getApplication();
        CreateFile operation = new CreateFile( dir, fileName, data );
        app.runWriteAction( operation );
        if( operation.isSuccessful() )
        {
            return operation.getResult();
        }
        throw operation.getException();
    }
}
