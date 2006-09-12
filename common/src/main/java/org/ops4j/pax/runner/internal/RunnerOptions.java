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

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.File;
import org.ops4j.pax.runner.repositories.RepositoryInfo;
import org.ops4j.pax.runner.repositories.RepositoryObserver;
import org.ops4j.pax.runner.repositories.BundleObserver;
import org.ops4j.pax.runner.state.Bundle;

public interface RunnerOptions
{

    String getProxyHost();

    void setProxyHost( String proxyHost );

    String getProxyPort();

    void setProxyPort( String proxyPort );

    String getProxyUser();

    void setProxyUser( String proxyUser );

    String getProxyPass();

    void setProxyPass( String proxyPass );

    String getSelectedPlatform();

    void setSelectedPlatform( String selectedPlatform );

    boolean isRunClean();

    void setRunClean( boolean runClean );

    boolean isStartGui();

    void setStartGui( boolean startGui );

    String getVmArguments();

    void setVmArguments( String vmArguments );

    Map<String, String> getSystemProperties();

    void addSystemProperty( String key, String value );

    void removeSystemProperty( String key );

    File getWorkDir();

    void setWorkDir( File workDir );

    ArrayList<String> getPlatforms();

    void addPlatform( String platform );

    void removePlatform( String platform );

    List<Bundle> getBundles();

    void addBundle( Bundle bundle );

    void removeBundle( Bundle bundle );

    List<RepositoryInfo> getRepositories();

    void setRepositories( List<RepositoryInfo> repositories );

    void addRepository( RepositoryInfo repoInfo );

    void removeRepository( RepositoryInfo repoInfo );

    void addRepositoryObserver( RepositoryObserver observer );

    void removeRepositoryObserver( RepositoryObserver observer );

    void addBundleObserver( BundleObserver observer );

    void removeBundleObserver( BundleObserver observer );

    void setSystemProperties( Map<String, String> systemProperties );

    void setBundles( List<Bundle> bundles );

    String getProfile();

    void setProfile( String profile );

    String getURL();

    void setNoMd5Checks( boolean noMd5Checks );

    boolean isNoMd5Checks();

    void setURL( String URL );

    Properties getProperties();
}
