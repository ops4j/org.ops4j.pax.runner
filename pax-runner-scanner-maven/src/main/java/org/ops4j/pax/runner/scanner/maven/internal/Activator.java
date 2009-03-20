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
package org.ops4j.pax.runner.scanner.maven.internal;

import org.osgi.framework.BundleContext;
import org.ops4j.pax.runner.provision.scanner.AbstractScannerActivator;
import org.ops4j.pax.runner.scanner.maven.ServiceConstants;
import org.ops4j.pax.swissbox.property.BundleContextPropertyResolver;
import org.ops4j.util.property.PropertyResolver;

/**
 * Bundle activator for maven scanner.
 *
 * @author Alin Dreghiciu
 * @since 0.18.0, March 19, 2009
 */
public final class Activator
    extends AbstractScannerActivator<MavenScanner>
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected MavenScanner createScanner( final BundleContext bundleContext )
    {
        return new MavenScanner( new BundleContextPropertyResolver( bundleContext ) );
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
    protected void setResolver( final PropertyResolver propertyResolver )
    {
        getScanner().setResolver( propertyResolver );
    }

}