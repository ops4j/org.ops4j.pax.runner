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

public class BundleInfo
{
    private String m_name;
    private String m_description;
    private String m_vendor;
    private String m_version;
    private String m_symbolicName;

    private RepositoryInfo m_repository;

    public BundleInfo( String name, RepositoryInfo repository )
    {
        m_name = name;
        m_repository = repository;
    }

    public String getName()
    {
        return m_name;
    }

    public RepositoryInfo getRepository()
    {
        return m_repository;
    }

    public String getDescription()
    {
        return m_description;
    }

    public void setDescription( String description )
    {
        m_description = description;
    }

    public String getVendor()
    {
        return m_vendor;
    }

    public void setVendor( String vendor )
    {
        m_vendor = vendor;
    }

    public String getVersion()
    {
        return m_version;
    }

    public void setVersion( String version )
    {
        m_version = version;
    }

    public String getSymbolicName()
    {
        return m_symbolicName;
    }

    public void setSymbolicName( String symbolicName )
    {
        m_symbolicName = symbolicName;
    }

    public String toString()
    {
        return m_name;
    }
}
