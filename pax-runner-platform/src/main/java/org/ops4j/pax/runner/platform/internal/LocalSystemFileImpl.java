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
package org.ops4j.pax.runner.platform.internal;

import java.io.File;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.platform.LocalSystemFile;
import org.ops4j.pax.runner.platform.SystemFileReference;

/**
 * A Java bean like implementation of local system file.
 *
 * @author Alin Dreghiciu
 * @since 0.15.0, October 28, 2007
 */
public class LocalSystemFileImpl
    implements LocalSystemFile
{

    /**
     * The system file reference this local system file refers to. Cannot be null.
     */
    private final SystemFileReference m_systemFileReference;
    /**
     * The file corresponding to above bundle refrence. Cannot be null.
     */
    private final File m_file;

    /**
     * Creates a new local bundle.
     *
     * @param systemFileReference a bundle reference; mandatory
     * @param file                corresponding file; mandatory
     */
    public LocalSystemFileImpl( final SystemFileReference systemFileReference, final File file )
    {
        NullArgumentException.validateNotNull( systemFileReference, "System file reference" );
        NullArgumentException.validateNotNull( file, "File" );
        m_systemFileReference = systemFileReference;
        m_file = file;
    }

    /**
     * {@inheritDoc}
     */
    public SystemFileReference getSystemFileReference()
    {
        return m_systemFileReference;
    }

    /**
     * {@inheritDoc}
     */
    public File getFile()
    {
        return m_file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( "{" )
            .append( "file=" )
            .append( getFile() )
            .append( ",reference=" )
            .append( getSystemFileReference() )
            .append( "}" )
            .toString();
    }

}