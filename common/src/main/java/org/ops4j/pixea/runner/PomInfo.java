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

public class PomInfo
{

    private String m_artifact;
    private String m_group;
    private String m_version;
    private String m_type;

    public PomInfo( String group, String artifact, String version )
    {
        this( artifact, group, version, "jar" );
    }

    public PomInfo( String group, String artifact, String version, String type )
    {
        m_type = type;
        m_artifact = artifact;
        m_group = group;
        m_version = version;
    }

    public String getArtifact()
    {
        return m_artifact;
    }

    public String getGroup()
    {
        return m_group;
    }

    public String getVersion()
    {
        return m_version;
    }

    public String getType()
    {
        return m_type;
    }
}
