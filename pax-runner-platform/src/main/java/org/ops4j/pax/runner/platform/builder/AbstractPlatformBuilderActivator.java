package org.ops4j.pax.runner.platform.builder;

import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.platform.PlatformBuilder;
import org.ops4j.pax.runner.platform.ServiceConstants;

/**
 * Abstract bundle activator for platform builders.<br/>
 * Registers a platform builder that the extender will automatically create and register a platform based on
 * the registered builder.
 *
 * @author Alin Dreghiciu
 * @since September 01, 2007
 */
public abstract class AbstractPlatformBuilderActivator
    implements BundleActivator
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( AbstractPlatformBuilderActivator.class );
    /**
     * The bundle context.
     */
    private BundleContext m_bundleContext;
    /**
     * Platform builder service registrations. Used for cleanup.
     */
    private ServiceRegistration[] m_platformBuilderServiceReg;

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( final BundleContext bundleContext )
        throws Exception
    {
        Assert.notNull( "Bundle context", bundleContext );
        m_bundleContext = bundleContext;
        registerPlatformBuilders( createPlatformBuilders( m_bundleContext ) );
        LOGGER.debug( "Platform builder [" + this + "] started" );
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
        if( m_platformBuilderServiceReg != null )
        {
            for( ServiceRegistration registration : m_platformBuilderServiceReg )
            {
                if( registration != null )
                {
                    registration.unregister();
                }
            }
            m_platformBuilderServiceReg = null;
        }
        m_bundleContext = null;
        LOGGER.debug( "Platform builder [" + this + "] stopped" );
    }

    /**
     * Registers the platform builders.
     *
     * @param platformBuilders an array of platform builders to register
     */
    private void registerPlatformBuilders( final PlatformBuilder[] platformBuilders )
    {
        m_platformBuilderServiceReg = new ServiceRegistration[platformBuilders.length];
        int i = 0;
        for( PlatformBuilder platformBuilder : platformBuilders )
        {
            Dictionary<String, Object> props = new Hashtable<String, Object>();
            props.put( ServiceConstants.PROPERTY_PROVIDER, platformBuilder.getProviderName() );
            props.put( ServiceConstants.PROPERTY_PROVIDER_VERSION, platformBuilder.getProviderVersion() );
            m_platformBuilderServiceReg[ i++ ] =
                m_bundleContext.registerService( PlatformBuilder.class.getName(), platformBuilder, props );
        }
    }

    /**
     * Platform builders factory method. It should return an array of platform builders supported.
     *
     * @param bundleContext current bundle context
     *
     * @return corresponding platform builder for the passed version
     */
    abstract protected PlatformBuilder[] createPlatformBuilders( BundleContext bundleContext );

}
