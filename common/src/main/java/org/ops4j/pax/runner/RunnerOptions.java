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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ops4j.pax.runner.repositories.BundleInfo;
import org.ops4j.pax.runner.repositories.BundleObserver;
import org.ops4j.pax.runner.repositories.RepositoryInfo;
import org.ops4j.pax.runner.repositories.RepositoryObserver;

public class RunnerOptions
{
    private boolean m_runClean;
    private boolean m_startGui;
    private String m_proxyHost;
    private String m_proxyPort;
    private String m_proxyUser;
    private String m_proxyPass;
    protected String m_selectedPlatform;
    private String m_vmArguments;
    private File m_workDir;
    protected List<BundleInfo> m_bundles;
    protected ArrayList<String> m_platforms;
    protected List<RepositoryInfo> m_repositories;
    protected Map<String, String> m_systemProperties;
    protected ArrayList<BundleObserver> m_bundleObservers;
    protected ArrayList<RepositoryObserver> m_repositoryObservers;
    private String m_profile;
    private String m_URL;
    private boolean m_noMd5Checks;

    public RunnerOptions()
    {
        m_platforms = new ArrayList<String>();
        m_systemProperties = new HashMap<String, String>();
        m_bundles = new ArrayList<BundleInfo>();
        m_repositories = new ArrayList<RepositoryInfo>();
        m_bundleObservers = new ArrayList<BundleObserver>();
        m_repositoryObservers = new ArrayList<RepositoryObserver>();
        m_platforms.add( "Knopflerfish" );
        m_platforms.add( "Felix" );
        m_platforms.add( "Equinox" );
        m_selectedPlatform = "Knopflerfish";
    }

    public String getProxyHost()
    {
        return m_proxyHost;
    }

    public void setProxyHost( String proxyHost )
    {
        m_proxyHost = proxyHost;
    }

    public String getProxyPort()
    {
        return m_proxyPort;
    }

    public void setProxyPort( String proxyPort )
    {
        m_proxyPort = proxyPort;
    }

    public String getProxyUser()
    {
        return m_proxyUser;
    }

    public void setProxyUser( String proxyUser )
    {
        m_proxyUser = proxyUser;
    }

    public String getProxyPass()
    {
        return m_proxyPass;
    }

    public void setProxyPass( String proxyPass )
    {
        m_proxyPass = proxyPass;
    }

    public String getSelectedPlatform()
    {
        return m_selectedPlatform;
    }

    public void setSelectedPlatform( String selectedPlatform )
    {
        m_selectedPlatform = selectedPlatform;
    }

    public boolean isRunClean()
    {
        return m_runClean;
    }

    public void setRunClean( boolean runClean )
    {
        m_runClean = runClean;
    }

    public boolean isStartGui()
    {
        return m_startGui;
    }

    public void setStartGui( boolean startGui )
    {
        m_startGui = startGui;
    }

    public String getVmArguments()
    {
        return m_vmArguments;
    }

    public void setVmArguments( String vmArguments )
    {
        m_vmArguments = vmArguments;
    }

    public Map<String, String> getSystemProperties()
    {
        return m_systemProperties;
    }

    public void addSystemProperty( String key, String value )
    {
        m_systemProperties.put( key, value );
    }

    public void removeSystemProperty( String key )
    {
        m_systemProperties.remove( key );
    }

    public File getWorkDir()
    {
        if( m_workDir == null )
        {
            return new File( System.getProperty( "user.dir" ) );
        }
        return m_workDir;
    }

    public void setWorkDir( File workDir )
    {
        m_workDir = workDir;
    }

    public ArrayList<String> getPlatforms()
    {
        return m_platforms;
    }

    public void addPlatform( String platform )
    {
        m_platforms.add( platform );
    }

    public void removePlatform( String platform )
    {
        m_platforms.remove( platform );
    }

    public List<BundleInfo> getBundles()
    {
        return m_bundles;
    }

    public void addBundle( BundleInfo bundle )
    {
        m_bundles.add( bundle );
        for( BundleObserver observer : m_bundleObservers )
        {
            observer.bundleAdded( bundle );
        }
    }

    public void removeBundle( BundleInfo bundle )
    {
        m_bundles.remove( bundle );
        for( BundleObserver observer : m_bundleObservers )
        {
            observer.bundleRemoved( bundle );
        }
    }

    public List<RepositoryInfo> getRepositories()
    {
        return Collections.unmodifiableList( m_repositories );
    }

    public void setRepositories( List<RepositoryInfo> repositories )
    {
        m_repositories = repositories;
    }

    public void addRepository( RepositoryInfo repoInfo )
    {
        m_repositories.add( repoInfo );
        for( RepositoryObserver obs : m_repositoryObservers )
        {
            obs.repositoryAdded( repoInfo );
        }
    }

    public void removeRepository( RepositoryInfo repoInfo )
    {
        m_repositories.remove( repoInfo );
        for( RepositoryObserver obs : m_repositoryObservers )
        {
            obs.repositoryAdded( repoInfo );
        }
    }

    public void addRepositoryObserver( RepositoryObserver observer )
    {
        m_repositoryObservers.add( observer );
    }

    public void removeRepositoryObserver( RepositoryObserver observer )
    {
        m_repositoryObservers.remove( observer );
    }

    public void addBundleObserver( BundleObserver observer )
    {
        m_bundleObservers.add( observer );
    }

    public void removeBundleObserver( BundleObserver observer )
    {
        m_bundleObservers.remove( observer );
    }

    public void setSystemProperties( Map<String, String> systemProperties )
    {
        m_systemProperties = systemProperties;
    }

    public void setBundles( List<BundleInfo> bundles )
    {
        m_bundles = bundles;
    }

    public String getProfile()
    {
        return m_profile;
    }

    public void setProfile( String profile )
    {
        m_profile = profile;
    }

    public String getURL()
    {
        return m_URL;
    }

    public void setNoMd5Checks( boolean noMd5Checks )
    {
        m_noMd5Checks = noMd5Checks;
    }

    public boolean isNoMd5Checks()
    {
        return m_noMd5Checks;
    }

    public void setURL( String URL )
    {
        m_URL = URL;
    }
}
