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
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.LocalBundle;

/**
 * A Java bean like implementation of local bundle.
 *
 * @author Alin Dreghiciu
 * @since August 20, 2007
 */
public class LocalBundleImpl
    implements LocalBundle
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
    public LocalBundleImpl( final BundleReference bundleReference, final File file )
    {
        Assert.notNull( "Bundle reference", bundleReference );
        Assert.notNull( "File", file );
        m_bundleReference = bundleReference;
        m_file = file;
    }

    /**
     * @see LocalBundle#getBundleReference()
     */
    public BundleReference getBundleReference()
    {
        return m_bundleReference;
    }

    /**
     * @see LocalBundle#getFile()
     */
    public File getFile()
    {
        return m_file;
    }

    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return new StringBuilder()
            .append( "{" )
            .append( "file=" )
            .append( getFile() )
            .append( ",reference=" )
            .append( getBundleReference() )
            .append( "}" )
            .toString();
    }

}
