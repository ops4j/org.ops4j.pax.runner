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
package org.ops4j.pax.runner.provision.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.util.tracker.ServiceTracker;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.provision.ProvisionService;
import org.ops4j.pax.runner.provision.Scanner;

/**
 * Activate the provisioning service implementation.
 *
 * @author Alin Dreghiciu
 * @since August 17, 2007
 */
public final class Activator
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
     * The provision service implementation.
     */
    private ProvisionServiceImpl m_provisionService;
    /**
     * The Scanner service service tracker.
     */
    private ServiceTracker m_serviceTracker;
    /**
     * Provision service registration. Used for cleanup.
     */
    private ServiceRegistration m_provisionServiceReg;

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( final BundleContext bundleContext )
    {
        Assert.notNull( "Bundle context", bundleContext );
        m_bundleContext = bundleContext;
        registerProvisionService();
        trackStartLevelService();
        trackScanners();
        LOGGER.debug( "Provisioning service started" );
    }

    /**
     * Performs cleanup:<br/>
     * * Stop the service tracker;<br/>
     * * Unregister provision service;<br/>
     * * Release bundle context.
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop( final BundleContext bundleContext )
    {
        Assert.notNull( "Bundle context", bundleContext );
        if( m_serviceTracker != null )
        {
            m_serviceTracker.close();
            m_serviceTracker = null;
        }
        if( m_provisionServiceReg != null )
        {
            m_provisionServiceReg.unregister();
            m_provisionServiceReg = null;
            m_provisionService = null;
        }
        m_bundleContext = null;
        LOGGER.debug( "Provisioning service stopped" );
    }

    /**
     * Tracks Scanner services via a Service tracker.
     */
    private void trackScanners()
    {
        m_serviceTracker = new ServiceTracker( m_bundleContext, Scanner.class.getName(), null )
        {
            /**
             * Adds the scanner to provision service.
             *
             * @see ServiceTracker#addingService(org.osgi.framework.ServiceReference)
             */
            @Override
            public Object addingService( final ServiceReference serviceReference )
            {
                Assert.notNull( "Service reference", serviceReference );
                LOGGER.debug( "Scanner available [" + serviceReference + "]" );
                Object schema = serviceReference.getProperty( Scanner.SCHEMA_PROPERTY );
                Scanner scanner = null;
                // only use the right registered scanners
                if( schema != null && schema instanceof String && ( (String) schema ).trim().length() > 0 )
                {
                    scanner = (Scanner) super.addingService( serviceReference );
                    if( scanner != null )
                    {
                        m_provisionService.addScanner( scanner, (String) schema );
                    }
                }
                return scanner;
            }

            /**
             * Removes the scanner from the provision service.
             *
             * @see ServiceTracker#removedService(org.osgi.framework.ServiceReference,Object)
             */
            @Override
            public void removedService( ServiceReference serviceReference, Object object )
            {
                LOGGER.debug( "Scanner removed [" + serviceReference + "]" );
                super.removedService( serviceReference, object );
                if( !( object instanceof Scanner ) )
                {
                    throw new IllegalArgumentException(
                        "Invalid tracked object [" + object.getClass() + "]. Expected an " + Scanner.class.getName()
                    );
                }
                m_provisionService.removeScanner( (Scanner) object );
            }
        };
        m_serviceTracker.open();
    }

    /**
     * Registers the provision service.
     */
    private void registerProvisionService()
    {
        m_provisionServiceReg = m_bundleContext.registerService(
            ProvisionService.class.getName(),
            m_provisionService = new ProvisionServiceImpl( m_bundleContext ),
            null
        );
    }

    /**
     * Tracks Start Level service via a Service tracker.
     * TODO add unit tests to verify what happens when service becomes available. Does the provision service uses it?
     * TODO check out if we should no apply a renewal of service on remove and release the service on adding another one
     */
    private void trackStartLevelService()
    {
        m_serviceTracker = new ServiceTracker( m_bundleContext, StartLevel.class.getName(), null )
        {
            /**
             * Sets the start level service to provision service.
             *
             * @see ServiceTracker#addingService(org.osgi.framework.ServiceReference)
             */
            @Override
            public Object addingService( final ServiceReference serviceReference )
            {
                Assert.notNull( "Service reference", serviceReference );
                LOGGER.debug( "Start Level service available [" + serviceReference + "]" );
                final StartLevel startLevel = (StartLevel) super.addingService( serviceReference );
                if( startLevel != null )
                {
                    m_provisionService.setStartLevelService( startLevel );
                }
                return startLevel;
            }
        };
        m_serviceTracker.open();
    }

}

