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

import java.net.URL;
import java.util.Properties;

public class BundleRef
{
    private String m_name;
    private RepositoryInfo m_repository;
    private URL m_location;
    private Properties m_properties;

    public BundleRef( String name, RepositoryInfo repository, URL location, Properties properties )
    {
        m_properties = properties;
        m_name = name;
        m_repository = repository;
        m_location = location;
    }

    public URL getLocation()
    {
        return m_location;
    }

    public RepositoryInfo getRepository()
    {
        return m_repository;
    }

    public String getName()
    {
        return m_name;
    }

    public Properties getProperties()
    {
        return m_properties;
    }

    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        BundleRef bundleRef = (BundleRef) o;

        if( !m_location.equals( bundleRef.m_location ) )
        {
            return false;
        }
        if( !m_name.equals( bundleRef.m_name ) )
        {
            return false;
        }
        if( !m_repository.equals( bundleRef.m_repository ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = m_name.hashCode();
        result = 31 * result + m_repository.hashCode();
        result = 31 * result + m_location.hashCode();
        return result;
    }
}
