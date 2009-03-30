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
 * A Java bean like implementation {@link ScannedBundle}.
 *
 * @author Alin Dreghiciu
 * @since August 17, 2007
 */
public class ScannedBundleBean
    implements ScannedBundle
{

    private String m_location;
    private Integer m_startLevel;
    private Boolean m_shouldStart;
    private Boolean m_shouldUpdate;

    public ScannedBundleBean()
    {
        // JavaBean constructor
    }

    public ScannedBundleBean( final String location,
                              final Integer startLevel,
                              final Boolean shouldStart,
                              final Boolean shouldUpdate )
    {
        m_location = location;
        m_startLevel = startLevel;
        m_shouldStart = shouldStart;
        m_shouldUpdate = shouldUpdate;
    }

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

    public Boolean shouldUpdate()
    {
        return m_shouldUpdate;
    }

    public void setShouldUpdate( final Boolean update )
    {
        this.m_shouldUpdate = update;
    }

    @Override
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

        ScannedBundleBean that = (ScannedBundleBean) o;

        return m_location.equals( that.m_location );

    }

    @Override
    public int hashCode()
    {
        return m_location.hashCode();
    }

    @Override
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
            .append( ",shouldUpdate=" )
            .append( shouldUpdate() )
            .append( "}" )
            .toString();
    }

}
