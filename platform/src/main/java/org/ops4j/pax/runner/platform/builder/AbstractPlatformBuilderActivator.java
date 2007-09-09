package org.ops4j.pax.runner.platform.builder;

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
     * Platform builder service registration. Used for cleanup.
     */
    private ServiceRegistration m_platformBuilderServiceReg;

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( final BundleContext bundleContext )
        throws Exception
    {
        Assert.notNull( "Bundle context", bundleContext );
        m_bundleContext = bundleContext;
        registerPlatformBuilder( createPlatformBuilder( m_bundleContext ) );
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
        if ( m_platformBuilderServiceReg != null )
        {
            m_platformBuilderServiceReg.unregister();
            m_platformBuilderServiceReg = null;
        }
        m_bundleContext = null;
        LOGGER.debug( "Platform builder [" + this + "] stopped" );
    }

    /**
     * Registers the platformBuilder.
     *
     * @param platformBuilder the platformBuilder to register
     */
    private void registerPlatformBuilder( final PlatformBuilder platformBuilder )
    {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put( ServiceConstants.PROPERTY_PROVIDER, getProviderName() );
        props.put( ServiceConstants.PROPERTY_PROVIDER_VERSION, getProviderVersion() );
        m_platformBuilderServiceReg =
            m_bundleContext.registerService( PlatformBuilder.class.getName(), platformBuilder, props );
    }

    /**
     * Returns the provider version.
     *
     * @return provider version
     */
    abstract protected String getProviderVersion();

    /**
     * Returns the provider name.
     *
     * @return provider name
     */
    abstract protected String getProviderName();

    /**
     * Platform builder factory method.
     *
     * @param bundleContext current bundle context
     *
     * @return equinox platform builder
     */
    abstract protected PlatformBuilder createPlatformBuilder( BundleContext bundleContext );

}
