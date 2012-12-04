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
package org.ops4j.pax.runner.platform.felix.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ops4j.io.FileUtils;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.PlatformContext;
import org.ops4j.util.collections.PropertiesWriter;
import org.osgi.framework.BundleContext;

/**
 * Platform builder for felix platform from version 1.0.0 till 1.2.2.
 *
 * @author Alin Dreghiciu
 * @since 0.16.0, November 05, 2008
 */
public class FelixPlatformBuilderF100T122
    extends FelixPlatformBuilder
{

    /**
     * {@inheritDoc}
     */
    public FelixPlatformBuilderF100T122( final BundleContext bundleContext, final String version )
    {
        super(bundleContext, version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void appendFrameworkStorage( final PlatformContext context, final PropertiesWriter writer ) {
        Configuration configuration = context.getConfiguration();
        final String profile = configuration.getFrameworkProfile();
        if( profile != null )
        {
            writer.append( "felix.cache.profile", profile );
            final Boolean usePersistedState = configuration.usePersistedState();
            if( usePersistedState != null && !usePersistedState )
            {
                final File workingDirectory = context.getWorkingDirectory();
                final File configDirectory = new File( workingDirectory, CONFIG_DIRECTORY );
                final File cacheDirectory = new File( configDirectory, CACHE_DIRECTORY );
                final File profileDirectory = new File( cacheDirectory, profile );
                LOGGER.trace( "Cleaning profile folder [" + profileDirectory + "]" );
                FileUtils.delete( profileDirectory );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getVMOptions( final PlatformContext context )
    {
        List<String> vmOptions = new ArrayList<String>( Arrays.asList( super.getVMOptions( context ) ) );
        final File workingDirectory = context.getWorkingDirectory();
        final File configDirectory = new File( workingDirectory, CONFIG_DIRECTORY );
        final File cacheDirectory = new File( configDirectory, CACHE_DIRECTORY );
        vmOptions.add(
            "-Dfelix.cache.dir="
            + context.getFilePathStrategy().normalizeAsPath( cacheDirectory )
        );
        return vmOptions.toArray( new String[vmOptions.size()] );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getFrameworkStartLevelPropertyName()
    {
        return "felix.startlevel.framework";
    }

}
