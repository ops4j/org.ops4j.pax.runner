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
import org.ops4j.lang.NullArgumentException;

/**
 * A Java bean like implementation of system file reference.
 *
 * @author Alin Dreghiciu
 * @since 0.15.0, October 28, 2007
 */
public class SystemFileReferenceBean
    implements SystemFileReference
{

    /**
     * Name of the system file.
     */
    private String m_name;
    /**
     * System file url.
     */
    private URL m_url;
    /**
     * True if the bundle should be updated. Can be null, case when the bundle is not updated.
     */
    private Boolean m_prepend;

    /**
     * Create a new bundle reference based on url, appended.
     *
     * @param url bundle location
     */
    public SystemFileReferenceBean( final URL url )
    {
        this( null, url, false );
    }

    /**
     * Create a new bundle reference based on url with the name from the corresponding parameter, appended
     *
     * @param name a nice ready to print bundle m_name; optional
     * @param url  bundle location
     */
    public SystemFileReferenceBean( final String name, final URL url )
    {
        this( name, url, false );
    }

    /**
     * Creates a new system file reference.
     *
     * @param name        a nice ready to print sytem file name; optional
     * @param url         bundle location
     * @param prepend      if the system file should be prepended to the classpath
     */
    public SystemFileReferenceBean( final String name, final URL url, final Boolean prepend )
    {
        setURL( url );
        setName( name );
        m_prepend = prepend;
    }

    public String getName()
    {
        return m_name;
    }

    public URL getURL()
    {
        return m_url;
    }

    public Boolean shouldPrepend()
    {
        return m_prepend;
    }

    public void setName( String name )
    {
        m_name = name;
        if( m_name == null )
        {
            m_name = m_url.toString();
        }
    }

    public void setURL( final URL url )
    {
        NullArgumentException.validateNotNull( url, "URL" );
        m_url = url;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals( final Object object )
    {
        if( object == null || !( object instanceof SystemFileReference ) )
        {
            return false;
        }
        final SystemFileReference another = (SystemFileReference) object;
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
            .append( ",shouldPrepend=" )
            .append( shouldPrepend() )
            .append( "}" )
            .toString();
    }

}