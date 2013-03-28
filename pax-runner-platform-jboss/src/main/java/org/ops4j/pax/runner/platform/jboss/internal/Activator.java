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
package org.ops4j.pax.runner.platform.jboss.internal;

import org.osgi.framework.BundleContext;
import org.ops4j.pax.runner.platform.PlatformBuilder;
import org.ops4j.pax.runner.platform.builder.AbstractPlatformBuilderActivator;

/**
 * Bundle activator for jboss platform.
 *
 * @author dpishchukhin
 * @see org.ops4j.pax.runner.platform.builder.AbstractPlatformBuilderActivator
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
                new JBossPlatformBuilderF100( bundleContext, "1.0.0" ),
        };
    }

}

