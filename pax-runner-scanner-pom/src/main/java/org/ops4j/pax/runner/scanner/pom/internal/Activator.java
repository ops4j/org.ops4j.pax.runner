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
package org.ops4j.pax.runner.scanner.pom.internal;

import org.ops4j.pax.runner.commons.resolver.BundleContextResolver;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.provision.scanner.AbstractScannerActivator;
import org.ops4j.pax.runner.scanner.pom.ServiceConstants;
import org.osgi.framework.BundleContext;

/**
 * Bundle activator for pom scanner.
 *
 * @author Alin Dreghiciu
 * @since September 17, 2007
 */
public final class Activator
    extends AbstractScannerActivator<PomScanner>
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected PomScanner createScanner( final BundleContext bundleContext )
    {
        return new PomScanner( new BundleContextResolver( bundleContext ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPID()
    {
        return ServiceConstants.PID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSchema()
    {
        return ServiceConstants.SCHEMA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setResolver( final Resolver resolver )
    {
        getScanner().setResolver( resolver );
    }

}

