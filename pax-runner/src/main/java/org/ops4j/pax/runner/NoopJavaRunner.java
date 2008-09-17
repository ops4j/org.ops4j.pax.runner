package org.ops4j.pax.runner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.platform.JavaRunner;
import org.ops4j.pax.runner.platform.PlatformException;

/**
 * A JavaRunner that does nothing.
 *
 * @author Alin Dreghiciu
 * @since 0.13.0, September 17, 2008
 */
public class NoopJavaRunner
    implements JavaRunner
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( NoopJavaRunner.class );

    /**
     * {@inheritDoc}
     */
    public void exec( String[] vmOptions, String[] classpath, String mainClass, String[] programOptions )
        throws PlatformException
    {
        LOGGER.info( "Skipping platform start and exit immediately" );
    }

}
