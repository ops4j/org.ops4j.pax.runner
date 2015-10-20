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

import org.osgi.framework.BundleContext;
import org.ops4j.pax.runner.platform.PlatformBuilder;
import org.ops4j.pax.runner.platform.builder.AbstractPlatformBuilderActivator;

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
     * {@inheritDoc}
     */
    @Override
    protected PlatformBuilder[] createPlatformBuilders( final BundleContext bundleContext )
    {
        return new PlatformBuilder[]{
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.0.0" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.0.1" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.0.2" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.0.3" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.0.4" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.0.5" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.1.0" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.1.1" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.2.0" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.3.0" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.3.1" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.3.2" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.3.3" ),
            new KnopflerfishPlatformBuilderF200T233( bundleContext, "2.4.0" ),
            new KnopflerfishPlatformBuilderF300( bundleContext, "3.0.0" ),
            new KnopflerfishPlatformBuilderF300( bundleContext, "3.1.0" ),
            new KnopflerfishPlatformBuilderF300( bundleContext, "3.2.0" ),
            new KnopflerfishPlatformBuilderF300( bundleContext, "3.3.0" ),
            new KnopflerfishPlatformBuilderF300( bundleContext, "3.4.0" ),
            new KnopflerfishPlatformBuilderF300( bundleContext, "3.5.0" ),
            new KnopflerfishPlatformBuilderF300( bundleContext, "5.2.0" ),
            new KnopflerfishPlatformBuilderSnapshot( bundleContext )
        };
    }

}

