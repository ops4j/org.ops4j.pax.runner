/*
 * Copyright 2009 Alin Dreghiciu.
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
package org.ops4j.pax.runner.platform.felix.internal;

import org.osgi.framework.BundleContext;

/**
 * Platform builder for felix platform after 1.6.0.
 *
 * @author Alin Dreghiciu
 * @since 0.18.0, April 06, 2009
 */
public class FelixPlatformBuilderF160
    extends FelixPlatformBuilderF140T141
{

    /**
     * {@inheritDoc}
     */
    public FelixPlatformBuilderF160( final BundleContext bundleContext,
                                     final String version )
    {
        super( bundleContext, version );
    }

    /**
     * Starting with Felix 1.6.0 the new property name is "org.osgi.framework.startlevel.beginning"
     * {@inheritDoc}
     */
    @Override
    protected String getFrameworkStartLevelPropertyName()
    {
        return "org.osgi.framework.startlevel.beginning";
    }
}