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
package org.ops4j.pax.runner.platform;

import java.net.URL;
import org.ops4j.pax.runner.commons.Assert;

/**
 * A Java bean like implementation of Bundle reference.
 *
 * @author Alin Dreghiciu
 * @since August 20, 2007
 */
public class BundleReferenceBean
    implements BundleReference
{

    /**
     * Name of the bundle.
     */
    private String m_name;
    /**
     * Bundle location as url.
     */
    private URL m_url;
    /**
     * Start level of the bundle. Can be null, case when the bundle start level is not set.
     */
    private Integer m_startLevel;
    /**
     * Whether or not the bundle should be started. Can be null, case when the bundle is tarted.
     */
    private Boolean m_shouldStart;

    /**
     * Create a new bundle reference based on url and with null start level and start.
     *
     * @param url bundle location
     */
    public BundleReferenceBean( final URL url )
    {
        this( null, url, null, null );
    }

    /**
     * Create a new bundle reference based on url with the m_name form the corresponding parameter and with null start
     * level and start.
     *
     * @param name a nice ready to print bundle m_name; optional
     * @param url  bundle location
     */
    public BundleReferenceBean( final String name, final URL url )
    {
        this( name, url, null, null );
    }

    /**
     * Creates a new bundle reference.
     *
     * @param name        a nice ready to print bundle m_name; optional
     * @param url         bundle location
     * @param startLevel  start level of the bundle; optional
     * @param shouldStart wether or not the bundle should be started; optional
     */
    public BundleReferenceBean( final String name, final URL url, final Integer startLevel, final Boolean shouldStart )
    {
        setURL( url );
        setName( name );
        m_startLevel = startLevel;
        m_shouldStart = shouldStart;
    }

    public String getName()
    {
        return m_name;
    }

    public URL getURL()
    {
        return m_url;
    }

    public Integer getStartLevel()
    {
        return m_startLevel;
    }

    public Boolean shouldStart()
    {
        return m_shouldStart;
    }

    public void setName( String name )
    {
        m_name = name;
        if ( m_name == null )
        {
            m_name = m_url.toString();
        }
    }

    public void setURL( final URL url )
    {
        Assert.notNull( "URL", url );
        m_url = url;
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
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals( final Object object )
    {
        if ( object == null || !( object instanceof BundleReference ) )
        {
            return false;
        }
        final BundleReference another = (BundleReference) object;
        return m_name.equals( another.getName() ) && m_url.equals( another.getURL() );
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( "{" )
            .append( "name=" )
            .append( getName() )
            .append( ",url=" )
            .append( getURL() )
            .append( ",startlevel=" )
            .append( getStartLevel() )
            .append( ",shouldStart=" )
            .append( shouldStart() )
            .append( "}" )
            .toString();
    }

}
