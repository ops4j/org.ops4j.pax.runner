/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.ops4j.pax.runner.scanner.obr.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.obr.RepositoryAdmin;
import org.ops4j.pax.runner.provision.scanner.AbstractScannerActivator;
import org.ops4j.pax.runner.scanner.obr.ServiceConstants;
import org.ops4j.pax.swissbox.property.BundleContextPropertyResolver;
import org.ops4j.pax.swissbox.tracker.ReplaceableService;
import org.ops4j.util.property.PropertyResolver;

/**
 * Bundle activator for obr scanner.
 *
 * @author Alin Dreghiciu
 * @since 0.7.0, February 04, 2008
 */
public final class Activator
    extends AbstractScannerActivator<ObrScanner>
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected ObrScanner createScanner( final BundleContext bundleContext )
    {
        return new ObrScanner(
            new BundleContextPropertyResolver( bundleContext ),
            new FilterValidator()
            {
                /**
                 * Validates filter syntax by creating an OSGi filter. If an
                 * @see FilterValidator#validate(String)
                 */
                public boolean validate( final String filter )
                {
                    try
                    {
                        bundleContext.createFilter( filter );
                        return true;
                    }
                    catch( InvalidSyntaxException e )
                    {
                        return false;
                    }
                }
            }
        );
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

