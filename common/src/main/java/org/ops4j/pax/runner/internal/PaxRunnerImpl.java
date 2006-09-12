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
package org.ops4j.pax.runner.internal;

import java.io.File;
import java.util.List;
import java.util.Properties;
import org.ops4j.pax.runner.DownloadManager;
import org.ops4j.pax.runner.PaxRunner;
import org.ops4j.pax.runner.PomInfo;
import org.ops4j.pax.runner.PomManager;
import org.ops4j.pax.runner.Runner;
import org.ops4j.pax.runner.RunnerSelector;
import org.ops4j.pax.runner.ServiceManager;
import org.ops4j.pax.runner.pom.Dependency;
import org.ops4j.pax.runner.pom.Model;
import org.ops4j.pax.runner.state.Bundle;
import org.ops4j.pax.runner.state.BundleState;
import org.w3c.dom.Element;

public class PaxRunnerImpl
    implements PaxRunner
{

    public void fillOptionsWithPomData( PomInfo pomInfo, RunnerOptions options )
        throws Exception
    {
        PomManager pomManager = ServiceManager.getInstance().getService( PomManager.class );
        DownloadManager downloadManager = ServiceManager.getInstance().getService( DownloadManager.class );
        Model pom = pomManager.getPom( pomInfo );
        List<Bundle> bundles = options.getBundles();
        Properties props = options.getProperties();
        for( Dependency dep : pom.getDependencies().getDependency() )
        {
            PomInfo depInfo = new PomInfo( dep.getArtifactId(), dep.getGroupId(), dep.getVersion() );
            File bundleFile = downloadManager.download( depInfo );
            Bundle bundle = new Bundle( bundleFile, 4, BundleState.START );
            bundles.add( bundle );
        }
        for( Element elem : pom.getProperties().getAny() )
        {
            String value = elem.getTextContent();
            String key = elem.getTagName();
            props.put( key, value );
        }
    }

    public void run( RunnerOptions options )
        throws Exception
    {
        Properties props = options.getProperties();
        RunnerSelector selector = ServiceManager.getInstance().getService( RunnerSelector.class );
        String platform = options.getSelectedPlatform();
        Runner runner = selector.select( platform, props );
        List<Bundle> bundles = options.getBundles();
        runner.execute( options, bundles );
    }
}
