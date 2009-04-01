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
package org.ops4j.pax.runner.osgi;

import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class RunnerBundle
    extends UnsupportedBundle
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( RunnerBundle.class );
    /**
     * Bundle location URL. Used for bundles installed by a scanner.
     */
    private URL m_location;
    /**
     * Whether or not the bundle should be started.
     */
    private boolean m_shouldStart;
    /**
     * Bundles start level. If null there is no start level set for the bundle.
     */
    private Integer m_startLevel;
    /**
     * True if the bundle should be updated.
     */
    private boolean m_shouldUpdate;

    /**
     * Default constructor. Used for bundles that are part of runner.
     */
    public RunnerBundle()
    {
        m_location = null;
        m_shouldStart = false;
        m_shouldUpdate = false;
    }

    /**
     * Creates a bundle with a specific location (valid URL). Used for bundles installed by a scanner.
     *
     * @param location URL of the bundle to be installed later on
     */
    public RunnerBundle( final URL location )
    {
        LOGGER.debug( "Installed bundle [" + location + "]" );
        m_location = location;
    }

    @Override
    public int getState()
    {
        // alwasy return active as we don't care but felix do care
        return Bundle.ACTIVE;
    }

    @Override
    public void start()
        throws BundleException
    {
        LOGGER.debug( "Bundle [" + m_location + "] will be started" );
        m_shouldStart = true;
    }

    /**
     * Returns true if the bundle should be started.
     *
     * @return true if the bundle should be started
     */
    public boolean shouldStart()
    {
        return m_shouldStart;
    }

    /**
     * The start level of the bundle. Set by start level service.
     *
     * @param startLevel start level of the bundle.
     */
    public void setStartLevel( final Integer startLevel )
    {
        LOGGER.debug( "Bundle [" + m_location + "] will start at level [" + startLevel + "]" );
        m_startLevel = startLevel;
    }

    /**
     * Returns the start level of the bundle. If it returns null the start level was not set so a default start level
     * should apply.
     *
     * @return the start level of the bundle or null if not set
     */
    public Integer getStartLevel()
    {
        return m_startLevel;
    }

    /**
     * Called by provisioning system if the update option is set on the bundle to be installed.
     */
    @Override
    public void update()
    {
        LOGGER.debug( "Bundle [" + m_location + "] will be updated" );
        m_shouldUpdate = true;
    }

    /**
     * Always return zero = long time ago, so provisioning service will force an update.
     *
     * @return zero (0)
     */
    @Override
    public long getLastModified()
    {
        return 0;
    }

    /**
     * Returns true of the bundle whould be updated.
     *
     * @return true if the bundle shoud be updated
     */
    public boolean shouldUpdate()
    {
        return m_shouldUpdate;
    }

    /**
     * Returns the url for the bundle.
     *
     * @return an url
     */
    public URL getLocationAsURL()
    {
        return m_location;
    }

    /**
     * Delegates to class loader.
     * {@inheritDoc}
     */
    @Override
    public URL getResource( final String name )
    {
        return getClass().getClassLoader().getResource( name );
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( m_location );
        if( m_startLevel != null )
        {
            sb.append( ", at start level " ).append( m_startLevel );
        }
        else
        {
            sb.append( ", at default start level" );
        }
        sb.append( m_shouldStart ? ", bundle will be started" : ", bundle will not be started" );
        sb.append( m_shouldUpdate ? ", bundle will be re-downloaded" : ", bundle will be loaded from the cache" );
        return sb.toString();
    }
}
