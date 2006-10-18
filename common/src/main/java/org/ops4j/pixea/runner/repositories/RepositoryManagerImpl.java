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
package org.ops4j.pax.runner.repositories;

import org.ops4j.pax.runner.RepositoryManager;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URL;
import java.net.MalformedURLException;

public class RepositoryManagerImpl
    implements RepositoryManager, BundleObserver
{
    private HashMap<RepositoryInfo, Repository> m_repositories;
    private List<RepositoryObserver> m_repositoryObservers;
    private List<BundleObserver> m_bundleObservers;

    public RepositoryManagerImpl()
    {
        m_repositories = new HashMap<RepositoryInfo, Repository>();
        m_repositoryObservers = new ArrayList<RepositoryObserver>();
        m_bundleObservers = new ArrayList<BundleObserver>();
    }

    public BundleInfo download( BundleRef bundleReference )
    {
        RepositoryInfo repoInfo = bundleReference.getRepository();
        Repository repo = m_repositories.get( repoInfo );
        return repo.download( bundleReference );
    }

    public List<RepositoryInfo> getRepositories()
    {
        List<RepositoryInfo> repos = new ArrayList<RepositoryInfo>();
        repos.addAll( m_repositories.keySet() );
        return repos;
    }

    public Repository getRepository( RepositoryInfo info )
    {
        return m_repositories.get( info );
    }

    public void addRepository( RepositoryInfo repository )
        throws MalformedURLException
    {
        if( m_repositories.containsKey( repository) )
        {
            return;
        }
        RepositoryType type = repository.getType();
        URL url = new URL( repository.getUrl() );
        switch( type )
        {
            case oscar:
                Repository repoInstance = new ObrRepository( url );
                repoInstance.addBundleObserver( this );
                m_repositories.put( repository, repoInstance );
                break;
            default:
                return;
        }
        for( RepositoryObserver observer : m_repositoryObservers )
        {
            observer.repositoryAdded( repository );
        }
    }

    public void removeRepository( RepositoryInfo repository )
    {
        Repository repoInstance = m_repositories.remove(  repository );
        if( repoInstance == null )
        {
            return;
        }
        repoInstance.removeBundleObserver( this );
        for( RepositoryObserver observer : m_repositoryObservers )
        {
            observer.repositoryRemoved( repository );
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

    public void bundleAdded( BundleRef bundle )
    {
        for(BundleObserver observer : m_bundleObservers )
        {
            observer.bundleAdded( bundle );
        }
    }

    public void bundleRemoved( BundleRef bundle )
    {
        for(BundleObserver observer : m_bundleObservers )
        {
            observer.bundleRemoved( bundle );
        }
    }
}
