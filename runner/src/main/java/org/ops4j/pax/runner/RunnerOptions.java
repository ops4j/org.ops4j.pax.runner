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
package org.ops4j.pax.runner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.ops4j.pax.model.repositories.RepositoryInfo;
import org.ops4j.pax.model.repositories.RepositoryObserver;
import org.ops4j.pax.model.bundles.BundleRef;
import org.ops4j.pax.model.bundles.BundleObserver;
import org.ops4j.pax.model.ApplicationOptions;

public interface RunnerOptions extends ApplicationOptions
{
    String getSelectedPlatform();

    void setSelectedPlatform( String selectedPlatform );

    boolean isRunClean();

    void setRunClean( boolean runClean );

    boolean isStartGui();

    void setStartGui( boolean startGui );

    String getVmArguments();

    void setVmArguments( String vmArguments );

    File getWorkDir();

    void setWorkDir( File workDir );

    ArrayList<String> getPlatforms();

    void addPlatform( String platform );

    void removePlatform( String platform );

    List<BundleRef> getBundleRefs();

    void addBundleRef( BundleRef ref );

    void removeBundleRef( BundleRef ref );

    List<RepositoryInfo> getRepositories();

    void setRepositories( List<RepositoryInfo> repositories );

    void addRepository( RepositoryInfo repoInfo );

    void removeRepository( RepositoryInfo repoInfo );

    void addRepositoryObserver( RepositoryObserver observer );

    void removeRepositoryObserver( RepositoryObserver observer );

    void addBundleObserver( BundleObserver observer );

    void removeBundleObserver( BundleObserver observer );

    void setBundleRefs( List<BundleRef> refs );

    String getProfile();

    void setProfile( String profile );

    String getURL();

    void setURL( String URL );

    Properties getProperties();

    BundleRef getSystemBundle();

    void setSystemBundles( List<BundleRef> systemBundles );
}
