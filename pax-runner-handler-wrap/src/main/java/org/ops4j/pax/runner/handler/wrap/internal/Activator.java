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
package org.ops4j.pax.runner.handler.wrap.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.resolver.BundleContextResolver;
import org.ops4j.pax.runner.commons.resolver.CompositeResolver;
import org.ops4j.pax.runner.commons.resolver.DictionaryResolver;
import org.ops4j.pax.runner.handler.wrap.ServiceConstants;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * Bundle activator for wrap: protocol handler.
 * TODO add unit tests
 *
 * @author Alin Dreghiciu
 * @since September 09, 2007
 */
public final class Activator
    implements BundleActivator
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( Activator.class );
    /**
     * Bundle context in use.
     */
    private BundleContext m_bundleContext;
    /**
     * Registered handler.
     */
    private Handler m_handler;
    /**
     * Handler service registration. Usef for cleanup.
     */
    private ServiceRegistration m_handlerReg;
    /**
     * Managed service registration. Used for cleanup.
     */
    private ServiceRegistration m_managedServiceReg;

    /**
     * Registers Handler as a wrap: protocol stream handler service and as a configuration managed service if
     * possible.
     *
     * @param bundleContext the bundle context.
     *
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( final BundleContext bundleContext )
    {
        Assert.notNull( "Bundle context", bundleContext );
        m_bundleContext = bundleContext;
        registerHandler();
        registerManagedService();
        LOGGER.info( "Protocol [" + ServiceConstants.PROTOCOL + "] handler started" );
    }

    /**
     * Performs cleanup:<br/>
     * * Unregister handler;<br/>
     * * Unregister managed service;<br/>
     * * Release bundle context.
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop( final BundleContext bundleContext )
    {
        Assert.notNull( "Bundle context", bundleContext );
        if ( m_handlerReg != null )
        {
            m_handlerReg.unregister();
        }
        if ( m_managedServiceReg != null )
        {
            m_managedServiceReg.unregister();
        }
        m_bundleContext = null;
        LOGGER.info( "Protocol [" + ServiceConstants.PROTOCOL + "] handler stopped" );
    }

    /**
     * Register the handler service.
     */
    private void registerHandler()
    {
        final Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put( URLConstants.URL_HANDLER_PROTOCOL, new String[]{ ServiceConstants.PROTOCOL } );
        m_handler = createHandler();
        m_handler.setResolver( new BundleContextResolver( m_bundleContext ) );
        m_handlerReg = m_bundleContext.registerService(
            URLStreamHandlerService.class.getName(),
            m_handler,
            props
        );

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
                 * Sets the resolver on handler.
                 *
                 * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
                 */
                public void updated( final Dictionary config )
                    throws ConfigurationException
                {
                    if ( config == null )
                    {
                        m_handler.setResolver( new BundleContextResolver( m_bundleContext ) );
                    }
                    else
                    {
                        m_handler.setResolver(
                            new CompositeResolver(
                                new DictionaryResolver( config ),
                                new BundleContextResolver( m_bundleContext )
                            )
                        );
                    }
                }
            },
            props
        );
    }

    /**
     * Handler factory method.
     *
     * @return a new handler
     */
    private Handler createHandler()
    {
        return new Handler();
    }

}
