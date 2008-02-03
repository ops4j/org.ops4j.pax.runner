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
package org.ops4j.pax.runner.obr.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.ObrCommandImpl;
import org.apache.felix.bundlerepository.RepositoryAdminImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.obr.RepositoryAdmin;

/**
 * Lazy OBR Repository Admin Service. This is a copy of Felix bundle repository that publishes a lazy wrapper over the
 * original RepositoryAdminImpl. This in order to avoid the parsing of the repository files on instantiation, as this
 * parsing is quite time consuming and it may be alost of time for those that do not use obr provisioning.
 *
 * @author Alin Dreghiciu
 * @since 0.7.0, February 03, 2008
 */
public class Activator implements BundleActivator
{

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog( Activator.class );

    public void start( BundleContext context )
    {
        // Register bundle repository service.
        RepositoryAdminImpl repoAdmin = new RepositoryAdminImpl( context );
        context.registerService(
            RepositoryAdmin.class.getName(),
            repoAdmin,
            null // no properties
        );

        // We dynamically import the impl service API, so it
        // might not actually be available, so be ready to catch
        // the exception when we try to register the command service.
        try
        {
            // Register "obr" impl command service as a
            // wrapper for the bundle repository service.
            context.registerService(
                org.apache.felix.shell.Command.class.getName(),
                new ObrCommandImpl( context, repoAdmin ),
                null // no properties
            );
        }
        catch( Throwable ignore )
        {
            // Ignore.
        }

        LOG.debug( "Lazy OBR service started" );
    }

    public void stop( BundleContext context )
    {
        LOG.debug( "Lazy OBR service stopped" );
    }

}
