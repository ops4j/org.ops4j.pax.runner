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
package org.ops4j.pax.runner.obr.script.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.obr.RepositoryAdmin;
import org.osgi.service.obr.Requirement;
import org.osgi.service.obr.Resolver;
import org.osgi.service.obr.Resource;
import org.ops4j.pax.swissbox.property.BundleContextPropertyResolver;
import org.ops4j.pax.swissbox.tracker.ReplaceableService;
import org.ops4j.pax.swissbox.tracker.ReplaceableServiceListener;

/**
 * @author Alin Dreghiciu
 * @since 0.7.0, February 04, 2008
 */
public class Activator
    implements BundleActivator
{

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog( Activator.class );

    public void start( final BundleContext bundleContext )
        throws Exception
    {
        final List<String> filters =
            readScript(
                new BundleContextPropertyResolver( bundleContext ).get( "org.ops4j.pax.runner.scanner.obr.script" )
            );
        final ReplaceableService<RepositoryAdmin> replaceableService =
            new ReplaceableService<RepositoryAdmin>(
                bundleContext,
                RepositoryAdmin.class,
                new ReplaceableServiceListener<RepositoryAdmin>()
                {

                    public void serviceChanged( final RepositoryAdmin ignore,
                                                final RepositoryAdmin repositoryAdmin )
                    {
                        if( repositoryAdmin != null )
                        {
                            LOG.debug( "Using OBR to resolve " + filters );
                            final Resolver resolver = repositoryAdmin.resolver();
                            boolean shouldResolve = false;
                            for( String filter : filters )
                            {
                                final Resource[] resources = repositoryAdmin.discoverResources( filter );
                                if( resources == null || resources.length == 0 )
                                {
                                    LOG.warn( "Cannot find a bundle matching [" + filter + "]. Skipping." );
                                }
                                else
                                {
                                    // TODO select highest resolved version
                                    resolver.add( resources[ 0 ] );
                                    shouldResolve = true;
                                }
                            }
                            if( shouldResolve )
                            {
                                if( resolver.resolve() )
                                {
                                    // install and start filters
                                    resolver.deploy( true );
                                }
                                else
                                {
                                    LOG.warn( "Could not resolve bundles due to unsatisfied requirements." );
                                    if( LOG.isTraceEnabled() )
                                    {
                                        final Requirement[] requirements = resolver.getUnsatisfiedRequirements();
                                        if( requirements != null && requirements.length > 0 )
                                        {
                                            for( Requirement req : requirements )
                                            {
                                                final Resource[] unresolvedResources = resolver.getResources( req );
                                                if( unresolvedResources != null )
                                                {
                                                    // seems like felix is returning the same resource multiple times
                                                    // so put the id's in a set
                                                    final Set<String> uniques = new HashSet<String>();
                                                    for( Resource unresolvedResource : unresolvedResources )
                                                    {
                                                        uniques.add( unresolvedResource.getId() );
                                                    }
                                                    for( String unique : uniques )
                                                    {
                                                        LOG.trace( "Unsatisified : "
                                                                   + req.getFilter()
                                                                   + " of "
                                                                   + unique
                                                        );
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            );
        replaceableService.start();
    }

    /**
     * Reads the script files containing obr filters.
     *
     * @param scriptURL url of script file
     *
     * @return list of obr filters for the bundles to be installed
     */
    private List<String> readScript( final String scriptURL )
    {
        final List<String> filters = new ArrayList<String>();
        if( scriptURL != null )
        {
            BufferedReader reader = null;
            try
            {
                try
                {
                    reader = new BufferedReader( new InputStreamReader( new URL( scriptURL ).openStream() ) );
                    String line;
                    while( ( line = reader.readLine() ) != null )
                    {
                        filters.add( line.trim() );
                    }
                }
                finally
                {
                    if( reader != null )
                    {
                        reader.close();
                    }
                }
            }
            catch( IOException ignore )
            {
                LOG.warn( "Could not parse the script file, reason: " + ignore.getMessage() );
            }
        }
        return filters;
    }

    public void stop( final BundleContext bundleContext )
        throws Exception
    {
        // nothing to do
    }
}
