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
package org.ops4j.pax.runner.platform.equinox.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.platform.PlatformBuilder;
import org.ops4j.pax.runner.platform.ServiceConstants;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Bundle activator for equinox platform.<br/>
 * Registers a platform builder for equinox. The extender will automatically create and register a platform based on
 * the registered builder.
 *
 * @author Alin Dreghiciu
 * @since August 20, 2007
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
     * Platform builder service registration. Used for cleanup.
     */
    private ServiceRegistration m_platformBuilderServiceReg;
    /**
     * Provider name to be used in registration.
     */
    private static final String PROVIDER_EQUINOX = "equinox";
    /**
     * Provider version to be used in registration.
     */
    private static final String PROVIDER_VERSION_EQUINOX = "3.2.1";

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( final BundleContext bundleContext )
        throws Exception
    {
        Assert.notNull( "Bundle context", bundleContext );
        m_bundleContext = bundleContext;
        registerPlatformBuilder( createPlatformBuilder() );
        LOGGER.debug( "Equinox platform builder started" );
    }

    /**
     * Performs cleanup:<br/>
     * * Unregister the platform builder;<br/>
     * * Release bundle context.
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop( final BundleContext bundleContext )
        throws Exception
    {
        Assert.notNull( "Bundle context", bundleContext );
        if ( m_platformBuilderServiceReg != null )
        {
            m_platformBuilderServiceReg.unregister();
            m_platformBuilderServiceReg = null;
        }
        m_bundleContext = null;
        LOGGER.debug( "Equinox platform builder stopped" );
    }

    /**
     * Equinox platform builder factory method.
     *
     * @return equinox platform builder
     */
    private PlatformBuilder createPlatformBuilder()
    {
        return new EquinoxPlatformBuilder( m_bundleContext );
    }

    /**
     * Registers the platformBuilder.
     *
     * @param platformBuilder the platformBuilder to register
     */
    private void registerPlatformBuilder( final PlatformBuilder platformBuilder )
    {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put( ServiceConstants.PROPERTY_PROVIDER, PROVIDER_EQUINOX );
        props.put( ServiceConstants.PROPERTY_PROVIDER_VERSION, PROVIDER_VERSION_EQUINOX );
        m_platformBuilderServiceReg =
            m_bundleContext.registerService( PlatformBuilder.class.getName(), platformBuilder, props );
    }

}

