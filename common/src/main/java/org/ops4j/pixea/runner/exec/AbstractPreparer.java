/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.runner.exec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.ops4j.pax.runner.DownloadManager;
import org.ops4j.pax.runner.PomInfo;
import org.ops4j.pax.runner.RunnerOptions;
import org.ops4j.pax.runner.ServiceException;
import org.ops4j.pax.runner.ServiceManager;
import org.ops4j.pax.runner.RunPreparer;
import org.ops4j.pax.runner.repositories.BundleInfo;
import org.ops4j.pax.runner.repositories.BundleRef;
import org.ops4j.pax.runner.state.Bundle;
import org.ops4j.pax.runner.state.BundleState;

public abstract class AbstractPreparer
    implements RunPreparer
{

    public void prepareForRun( RunnerOptions options )
        throws IOException
    {
        DownloadManager downloadManager = null;
        try
        {
            downloadManager = ServiceManager.getInstance().getService( DownloadManager.class );
        } catch( ServiceException e )
        {
            e.printStackTrace();  //TODO: Auto-generated, need attention.
        }
        List<BundleRef> systemBundles = new ArrayList<BundleRef>();
        for( PomInfo pom : getSystemBundles() )
        {
            File file = downloadManager.download( pom );
            BundleRef ref = new BundleRef( pom.getArtifact(), options.getRepositories().get( 0 ), file.toURL(), null );
            systemBundles.add( ref );
        }
        options.setSystemBundles( systemBundles );
        List<Bundle> bundles = new ArrayList<Bundle>();
        for( PomInfo bundleInfo : getDefaultBundles() )
        {
            Bundle bundle = createBundle( downloadManager, bundleInfo, options );
            bundles.add( bundle );
        }
        if( options.isStartGui() )
        {
            for( PomInfo bundleInfo : getGuiBundles() )
            {
                Bundle bundle = createBundle( downloadManager, bundleInfo, options );
                bundles.add( bundle );
            }
        }
        for( BundleRef ref : options.getBundleRefs() )
        {
            createBundle( ref );
        }
        createConfigFile( options, bundles );
    }

    protected abstract void createConfigFile( RunnerOptions options, List<Bundle> bundles )
        throws IOException;

    protected abstract PomInfo[] getSystemBundles();

    protected abstract  PomInfo[] getDefaultBundles();

    protected abstract PomInfo[] getGuiBundles();

    protected Bundle createBundle( DownloadManager downloadManager, PomInfo pom, RunnerOptions options )
        throws IOException
    {
        File file = downloadManager.download( pom );
        BundleRef ref = new BundleRef( pom.getArtifact(), options.getRepositories().get( 0 ), file.toURL(), null );
        return createBundle( ref );
    }

    private Bundle createBundle( BundleRef ref )
    {
        BundleInfo info = new BundleInfo( ref );
        return new Bundle( info, 1, BundleState.START );
    }
}
