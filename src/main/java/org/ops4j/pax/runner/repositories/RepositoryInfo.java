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

public class RepositoryInfo
{
    private String m_name;
    private String m_url;
    private RepositoryType m_type;

    public RepositoryInfo( String name, String url, RepositoryType type )
    {
        m_name = name;
        m_url = url;
        m_type = type;
    }

    public String getName()
    {
        return m_name;
    }

    public String getUrl()
    {
        return m_url;
    }

    public RepositoryType getType()
    {
        return m_type;
    }
}
