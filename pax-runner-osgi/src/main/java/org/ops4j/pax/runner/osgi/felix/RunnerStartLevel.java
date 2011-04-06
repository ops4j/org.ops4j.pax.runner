package org.ops4j.pax.runner.osgi.felix;

import org.apache.felix.framework.ServiceRegistry;
import org.ops4j.pax.runner.osgi.UnsupportedBundle;
import org.ops4j.pax.runner.osgi.UnsupportedStartLevel;
import org.osgi.framework.Bundle;
import org.osgi.service.startlevel.StartLevel;

public class RunnerStartLevel
    extends UnsupportedStartLevel
{

    /**
     * Installs itself as a service in felix service registry.
     *
     * @param serviceRegistry a felix service registry
     */
    public static void install( final ServiceRegistry serviceRegistry )
    {
        serviceRegistry.registerService(
            new UnsupportedBundle(),
            new String[]{ StartLevel.class.getName() },
            new RunnerStartLevel(),
            null
        );
    }

    /**
     * Sets the bundle start level if the bundle is a runner bundle.
     *
     * @see org.osgi.service.startlevel.StartLevel#setBundleStartLevel(org.osgi.framework.Bundle,int)
     */
    public void setBundleStartLevel( final Bundle bundle, final int startLevel )
    {
        if( !( bundle instanceof RunnerBundle ) )
        {
            super.setBundleStartLevel( bundle, startLevel );
        }
        ( (RunnerBundle) bundle ).setStartLevel( startLevel );
    }

}
