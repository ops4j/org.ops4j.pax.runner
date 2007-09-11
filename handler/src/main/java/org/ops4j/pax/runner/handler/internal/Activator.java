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
package org.ops4j.pax.runner.handler.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.commons.Assert;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Bundle activator for url stream handler extender.
 *
 * @author Alin Dreghiciu
 * @since August 28, 2007
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
     * URl stream handler extender.
     */
    private URLStreamHandlerExtender m_extender;

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( final BundleContext bundleContext )
        throws Exception
    {
        Assert.notNull( "Bundle context", bundleContext );
        m_bundleContext = bundleContext;
        m_extender = createExtender();
        m_extender.start();
        trackURLStreamHandlerService();
        LOGGER.debug( "URL stream handler service extender started" );
    }

    /**
     * Performs cleanup:<br/>
     * * Stop the service tracker;<br/>
     * * Unregister all url stream handles;<br/>
     * * Release extender;<br/>
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
        m_extender = null;
        m_bundleContext = null;
        LOGGER.debug( "URL stream handler service extender stopped" );
    }

    /**
     * Tracks platform builder services via a Service tracker.
     */
    private void trackURLStreamHandlerService()
    {
        m_serviceTracker = new ServiceTracker( m_bundleContext, URLStreamHandlerService.class.getName(), null )
        {
            /**
             * Registers the url stream handler with the URL stream handler factory
             *
             * @see ServiceTracker#addingService(org.osgi.framework.ServiceReference)
             */
            @Override
            public Object addingService( final ServiceReference serviceReference )
            {
                Assert.notNull( "Service reference", serviceReference );
                LOGGER.debug( "URL stream handler service available [" + serviceReference + "]" );
                // TODO check for class cast exception (very defensive)
                final URLStreamHandlerService streamHandler =
                    (URLStreamHandlerService) super.addingService( serviceReference );
                if( streamHandler != null )
                {
                    // TODO ensure that the protocol property is set and is a string array
                    m_extender.register(
                        (String[]) serviceReference.getProperty( URLConstants.URL_HANDLER_PROTOCOL ),
                        streamHandler
                    );
                }
                return streamHandler;
            }

            /**
             * Unregisters the url stream handler with the URL stream handler factory
             *
             * @see ServiceTracker#removedService(org.osgi.framework.ServiceReference,Object)
             */
            @Override
            public void removedService( final ServiceReference serviceReference, final Object object )
            {
                LOGGER.debug( "URL stream handler service removed [" + serviceReference + "]" );
                if( !( object instanceof URLStreamHandlerService ) )
                {
                    throw new IllegalArgumentException(
                        "Invalid tracked object [" + object.getClass() + "]. Expected an "
                        + URLStreamHandlerService.class.getName()
                    );
                }
                // TODO ensure that the protocol property is set and is a string array
                m_extender.unregister( (String[]) serviceReference.getProperty( URLConstants.URL_HANDLER_PROTOCOL ) );
                super.removedService( serviceReference, object );
            }
        };
        m_serviceTracker.open();
    }

    /**
     * URLStreamHandlerExtender factory method.
     *
     * @return a URLStreamHandlerExtender
     */
    private URLStreamHandlerExtender createExtender()
    {
        return new URLStreamHandlerExtender();
    }

}
