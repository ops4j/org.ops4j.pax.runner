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
package org.ops4j.pax.runner.provision.internal;

import java.util.Iterator;
import java.util.List;
import org.osgi.framework.BundleException;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.provision.InstallableBundle;
import org.ops4j.pax.runner.provision.InstallableBundles;

public class InstallableBundlesImpl
    implements InstallableBundles
{

    /**
     * List of installables from this set.
     */
    private final List<InstallableBundle> m_installables;

    /**
     * Creates a new installable bundles set.
     *
     * @param installables installable bundles that makes up the set
     */
    public InstallableBundlesImpl( final List<InstallableBundle> installables )
    {
        Assert.notNull( "List of installable bundles", installables );
        m_installables = installables;
    }

    /**
     * Returns an iterator over a set of InstallableBundle.
     *
     * @return an Iterator.
     */
    public Iterator<InstallableBundle> iterator()
    {
        return m_installables.iterator();
    }

    /**
     * @see org.ops4j.pax.runner.provision.InstallableBundles#install()
     */
    public InstallableBundles install()
        throws BundleException
    {
        for( InstallableBundle installable : m_installables )
        {
            installable.install();
        }
        for( InstallableBundle installable : m_installables )
        {
            installable.startIfNecessary();
        }
        return this;
    }

}
