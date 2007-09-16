/*
 * Copyright 2007 Alin Dreghiciu.
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
package org.ops4j.pax.runner.provision;

/**
 * A Java bean like implementation of Bundle reference.
 *
 * @author Alin Dreghiciu
 * @since August 17, 2007
 */
public class BundleReferenceBean
    implements BundleReference
{

    private String m_location;
    private Integer m_startLevel;
    private Boolean m_shouldStart;

    public String getLocation()
    {
        return m_location;
    }

    public Integer getStartLevel()
    {
        return m_startLevel;
    }

    public Boolean shouldStart()
    {
        return m_shouldStart;
    }

    public void setLocation( final String location )
    {
        m_location = location;
    }

    public void setStartLevel( final Integer startLevel )
    {
        m_startLevel = startLevel;
    }

    public void setShouldStart( final Boolean shouldStart )
    {
        m_shouldStart = shouldStart;
    }

    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return new StringBuilder()
            .append( "{" )
            .append( "location=" )
            .append( getLocation() )
            .append( ",startlevel=" )
            .append( getStartLevel() )
            .append( ",shouldStart=" )
            .append( shouldStart() )
            .append( "}" )
            .toString();
    }

}
