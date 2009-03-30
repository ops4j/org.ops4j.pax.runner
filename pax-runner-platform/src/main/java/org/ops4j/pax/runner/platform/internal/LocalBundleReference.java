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
import java.net.MalformedURLException;
import java.net.URL;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.platform.BundleReference;

/**
 * A {@link BundleReference} pointing to a local downloaded file.
 *
 * @author Alin Dreghiciu
 * @since August 20, 2007
 */
public class LocalBundleReference
    implements BundleReference
{

    /**
     * The bundle reference this local bundle refers to. Cannot be null.
     */
    private final BundleReference m_bundleReference;
    /**
     * The file corresponding to above bundle refrence. Cannot be null.
     */
    private final File m_file;

    /**
     * Creates a new local bundle.
     *
     * @param bundleReference a bundle reference; mandatory
     * @param file            corresponding file; mandatory
     */
    public LocalBundleReference( final BundleReference bundleReference, final File file )
    {
        NullArgumentException.validateNotNull( bundleReference, "Bundle reference" );
        NullArgumentException.validateNotNull( file, "File" );

        m_bundleReference = bundleReference;
        m_file = file;
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return m_bundleReference.getName();
    }

    /**
     * {@inheritDoc}
     */
    public URL getURL()
    {
        try
        {
            return m_file.toURL();
        }
        catch( MalformedURLException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public Integer getStartLevel()
    {
        return m_bundleReference.getStartLevel();
    }

    /**
     * {@inheritDoc}
     */
    public Boolean shouldStart()
    {
        return m_bundleReference.shouldStart();
    }

    /**
     * {@inheritDoc}
     */
    public Boolean shouldUpdate()
    {
        return m_bundleReference.shouldUpdate();
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        LocalBundleReference that = (LocalBundleReference) o;

        return m_file.equals( that.m_file );

    }

    @Override
    public int hashCode()
    {
        return m_file.hashCode();
    }

    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return new StringBuilder()
            .append( "{" )
            .append( "file=" )
            .append( m_file )
            .append( ",reference=" )
            .append( m_bundleReference )
            .append( "}" )
            .toString();
    }

}
