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
package org.ops4j.pax.runner.platform.felix.internal;

import java.io.File;

import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.PlatformContext;
import org.ops4j.util.collections.PropertiesWriter;
import org.osgi.framework.BundleContext;

/**
 * Platform builder for felix platform versions 1.4.0 and 1.4.1.
 *
 * @author Alin Dreghiciu
 * @since 0.16.0, November 05, 2008
 */
public class FelixPlatformBuilderF140T141
    extends FelixPlatformBuilder
{

    /**
     * {@inheritDoc}
     */
    public FelixPlatformBuilderF140T141( final BundleContext bundleContext, final String version )
    {
        super(bundleContext, version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void appendFrameworkStorage( final PlatformContext context, final PropertiesWriter writer ) {
        // storage directory
        {
            final File workingDirectory = context.getWorkingDirectory();
            final File configDirectory = new File( workingDirectory, CONFIG_DIRECTORY );
            final File cacheDirectory = new File( configDirectory, CACHE_DIRECTORY );
            final File storageDirectory = new File(cacheDirectory, "runner");
            writer.append(
                "org.osgi.framework.storage",
                context.getFilePathStrategy().normalizeAsPath( storageDirectory ).replace( File.separatorChar, '/' )
            );
        }
        // use persisted state
        {
            Configuration configuration = context.getConfiguration();
            final Boolean usePersistedState = configuration.usePersistedState();
            if( usePersistedState != null && !usePersistedState )
            {
                writer.append( "org.osgi.framework.storage.clean", "onFirstInit" );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getFrameworkStartLevelPropertyName()
    {
        return "org.osgi.framework.startlevel";
    }

}
