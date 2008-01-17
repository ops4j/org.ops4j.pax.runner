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
package org.ops4j.pax.runner.platform.internal;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.resolver.BundleContextPropertyResolver;
import org.ops4j.pax.runner.platform.Platform;
import org.ops4j.pax.runner.platform.PlatformBuilder;
import org.ops4j.pax.runner.platform.ServiceConstants;
import org.ops4j.util.property.DictionaryPropertyResolver;

/**
 * Bundle activator for platform extender.
 *
 * @author Alin Dreghiciu
 * @since August 20, 2007
 */
public class Activator
    implements BundleActivator
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( Activator.class );
    /**
     * The bundle context.
     */
    private BundleContext m_bundleContext;
    /**
     * The platform builder service service tracker.
     */
    private ServiceTracker m_serviceTracker;
    /**
     * Map between service reference of platform builder and service registration of registered platform.
     */
    private Map<ServiceReference, ServiceRegistration> m_registrations;
    /**
     * Map between service reference of platform builder and registered platform.
     */
    private Map<ServiceReference, PlatformImpl> m_platforms;
    /**
     * Managed service registration. Used for cleanup.
     */
    private ServiceRegistration m_managedServiceReg;
    /**
     * Configuration properties in use.
     */
    private Dictionary m_config;
    /**
     * Lock for synchronized set of configuration properties and platform registration.
     */
    private Lock m_lock;

    /**
     * Creates a new activator.
     */
    public Activator()
    {
        m_lock = new ReentrantLock();
    }

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( final BundleContext bundleContext )
        throws Exception
    {
        Assert.notNull( "Bundle context", bundleContext );
        m_bundleContext = bundleContext;
        m_registrations = new HashMap<ServiceReference, ServiceRegistration>();
        m_platforms = Collections.synchronizedMap( new HashMap<ServiceReference, PlatformImpl>() );
        trackPlatformBuilders();
        registerManagedService();
        LOGGER.debug( "Platform extender started" );
    }

    /**
     * Performs cleanup:<br/>
     * * Stop the service tracker;<br/>
     * * Unregister all platforms;<br/>
     * * Unregister managed service;<br/>
     * * Release platforms;<br/>
     * * Release bundle context.
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop( final BundleContext bundleContext )
        throws Exception
    {
        Assert.notNull( "Bundle context", bundleContext );
        if( m_serviceTracker != null )
        {
            m_serviceTracker.close();
            m_serviceTracker = null;
        }
        if( m_registrations != null )
        {
            for( ServiceRegistration registration : m_registrations.values() )
            {
                if( registration != null )
                {
                    registration.unregister();
                }
            }
        }
        if( m_managedServiceReg != null )
        {
            m_managedServiceReg.unregister();
        }
        m_platforms = null;
        m_bundleContext = null;
        LOGGER.debug( "Platform extender stopped" );
    }

    /**
     * Tracks platform builder services via a Service tracker.
     */
    private void trackPlatformBuilders()
    {
        m_serviceTracker = new ServiceTracker( m_bundleContext, PlatformBuilder.class.getName(), null )
        {
            /**
             * Create and register a new platform.
             *
             * @see ServiceTracker#addingService(org.osgi.framework.ServiceReference)
             */
            @Override
            public Object addingService( final ServiceReference serviceReference )
            {
                Assert.notNull( "Service reference", serviceReference );
                LOGGER.debug( "Platform builder available [" + serviceReference + "]" );
                final PlatformBuilder platformBuilder = (PlatformBuilder) super.addingService( serviceReference );
                if( platformBuilder != null )
                {
                    LOGGER.debug( "Registering a platform for [" + platformBuilder + "]" );
                    // copy all properties
                    final Dictionary<String, Object> props = new Hashtable<String, Object>();
                    final String[] keys = serviceReference.getPropertyKeys();
                    if( keys != null )
                    {
                        for( String key : keys )
                        {
                            props.put( key, serviceReference.getProperty( key ) );
                        }
                    }
                    try
                    {
                        // create and register the platform
                        final Platform platform = createPlatform( platformBuilder );
                        final ServiceRegistration registration = m_bundleContext.registerService(
                            Platform.class.getName(),
                            platform,
                            props
                        );
                        // and store it for clean up
                        m_registrations.put( serviceReference, registration );
                        if( platform instanceof PlatformImpl )
                        {
                            m_platforms.put( serviceReference, (PlatformImpl) platform );
                        }
                        LOGGER.debug( "Registred platform [" + platform + "]" );
                    }
                    catch( Exception e )
                    {
                        LOGGER.error( "Could not register a platform for [" + serviceReference + "]", e );
                    }
                }
                return platformBuilder;
            }

            /**
             * Unregister the platform corespronding to th ebuilder.
             *
             * @see ServiceTracker#removedService(org.osgi.framework.ServiceReference,Object)
             */
            @Override
            public void removedService( final ServiceReference serviceReference, final Object object )
            {
                LOGGER.debug( "Platform builder removed [" + serviceReference + "]" );
                if( !( object instanceof PlatformBuilder ) )
                {
                    throw new IllegalArgumentException(
                        "Invalid tracked object [" + object.getClass() + "]. Expected an "
                        + PlatformBuilder.class.getName()
                    );
                }
                super.removedService( serviceReference, object );
                final ServiceRegistration registration = m_registrations.get( serviceReference );
                if( registration != null )
                {
                    registration.unregister();
                    m_registrations.remove( serviceReference );
                    LOGGER.debug( "Unregistred platform for [" + object + "]" );
                }
                m_platforms.remove( serviceReference );
            }
        };
        m_serviceTracker.open();
    }

    /**
     * Registers a managed service to listen on configuration updates.
     */
    private void registerManagedService()
    {
        final Dictionary<String, String> props = new Hashtable<String, String>();
        props.put( Constants.SERVICE_PID, ServiceConstants.PID );
        m_managedServiceReg = m_bundleContext.registerService(
            ManagedService.class.getName(),
            new ManagedService()
            {
                /**
                 * Sets the resolver for each registred platform.
                 *
                 * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
                 */
                public void updated( final Dictionary config )
                    throws ConfigurationException
                {
                    m_lock.lock();
                    try
                    {
                        m_config = config;
                        for( PlatformImpl platform : m_platforms.values() )
                        {
                            if( m_config == null )
                            {
                                platform.setResolver( new BundleContextPropertyResolver( m_bundleContext ) );
                            }
                            else
                            {
                                platform.setResolver(
                                    new DictionaryPropertyResolver(
                                        config,
                                        new BundleContextPropertyResolver( m_bundleContext )
                                    )
                                );
                            }
                        }
                    }
                    finally
                    {
                        m_lock.unlock();
                    }
                }
            },
            props
        );
    }

    /**
     * Platform factory method.
     *
     * @param platformBuilder a platform builder
     *
     * @return a platform implementation
     */
    Platform createPlatform( final PlatformBuilder platformBuilder )
    {
        final PlatformImpl platform = new PlatformImpl( platformBuilder, m_bundleContext );
        m_lock.lock();
        try
        {
            if( m_config == null )
            {
                platform.setResolver( new BundleContextPropertyResolver( m_bundleContext ) );
            }
            else
            {
                platform.setResolver(
                    new DictionaryPropertyResolver(
                        m_config,
                        new BundleContextPropertyResolver( m_bundleContext )
                    )
                );
            }
        }
        finally
        {
            m_lock.unlock();
        }
        return platform;
    }

}
