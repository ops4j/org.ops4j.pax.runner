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
package org.ops4j.pax.runner.platform.knopflerfish.internal;

import org.ops4j.pax.runner.platform.PlatformBuilder;
import org.ops4j.pax.runner.platform.builder.AbstractPlatformBuilderActivator;
import org.osgi.framework.BundleContext;

/**
 * Bundle activator for knopflerfish platform.
 *
 * @author Alin Dreghiciu
 * @see org.ops4j.pax.runner.platform.builder.AbstractPlatformBuilderActivator
 * @since September 09, 2007
 */
public final class Activator
    extends AbstractPlatformBuilderActivator
{

    /**
     * Provider name to be used in registration.
     */
    private static final String PROVIDER_NAME = "knopflerfish";
    /**
     * Provider version to be used in registration.
     */
    private static final String PROVIDER_VERSION = "2.0.0";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProviderName()
    {
        return PROVIDER_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProviderVersion()
    {
        return PROVIDER_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PlatformBuilder createPlatformBuilder( final BundleContext bundleContext )
    {
        return new KnopflerfishPlatformBuilder( bundleContext );
    }

}

