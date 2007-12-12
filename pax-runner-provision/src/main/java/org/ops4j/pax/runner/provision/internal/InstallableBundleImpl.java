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
package org.ops4j.pax.runner.provision.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.startlevel.StartLevel;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.InstallableBundle;

public class InstallableBundleImpl
    implements InstallableBundle
{

    /**
     * Holds the bundle reference for the bundle to be installed.
     */
    private final BundleReference m_reference;
    /**
     * The installed bundle. Null before installation.
     */
    private Bundle m_bundle;
    /**
     * Bundle context where the bundle is installed.
     */
    private final BundleContext m_bundleContext;
    /**
     * The start level service or null if not available.
     */
    private final StartLevel m_startLevelService;
    /**
     * The internal state.
     */
    private State m_state;

    /**
     * Creates a new Installable Bundle with no start level service.
     *
     * @param bundleContext a bundle context; mandatory
     * @param reference     a bundle reference; mandatory
     */
    public InstallableBundleImpl( final BundleContext bundleContext, final BundleReference reference )
    {
        this( bundleContext, reference, null );
    }

    /**
     * Creates a new Installable Bundle with a start level service that can be null.
     *
     * @param bundleContext     a bundle context; mandatory
     * @param reference         a bundle reference; mandatory
     * @param startLevelService a start level service; optional
     */
    public InstallableBundleImpl( final BundleContext bundleContext, final BundleReference reference,
                                  final StartLevel startLevelService )
    {
        Assert.notNull( "Bundle context", bundleContext );
        Assert.notNull( "Bundle reference", reference );
        m_bundleContext = bundleContext;
        m_reference = reference;
        m_startLevelService = startLevelService;
        m_state = new NotInstalledState();
    }

    /**
     * @see org.ops4j.pax.runner.provision.InstallableBundle#getBundle()
     */
    public Bundle getBundle()
    {
        return m_bundle;
    }

    /**
     * @see org.ops4j.pax.runner.provision.InstallableBundle#install()
     */
    public InstallableBundle install()
        throws BundleException
    {
        m_state.install();
        return this;
    }

    /**
     * @see org.ops4j.pax.runner.provision.InstallableBundle#startIfNecessary()
     */
    public InstallableBundle startIfNecessary()
        throws BundleException
    {
        if( m_reference.shouldStart() )
        {
            start();
        }
        return this;
    }

    /**
     * @see org.ops4j.pax.runner.provision.InstallableBundle#start()
     */
    public InstallableBundle start()
        throws BundleException
    {
        m_state.start();
        return this;
    }

    /**
     * Performs the actual installation.
     *
     * @throws org.osgi.framework.BundleException
     *          see install()
     */
    private void doInstall()
        throws BundleException
    {
        final String location = m_reference.getLocation();
        if( location == null )
        {
            throw new BundleException( "The bundle reference has no location" );
        }
        // get current time to be ubale to verify if the bundle was already installed before the install below
        long currentTime = System.currentTimeMillis();
        m_bundle = m_bundleContext.installBundle( location );
        // if the bundle was modified (installed/updated) before then force an update
        Boolean shouldUpdate = m_reference.shouldUpdate();
        if( shouldUpdate != null && shouldUpdate && m_bundle.getLastModified() < currentTime )
        {
            m_bundle.update();
        }
        if( m_bundle == null )
        {
            throw new BundleException( "The bundle could not be installed due to unknown reason" );
        }
        m_state = new InstalledState();
        if( m_startLevelService != null )
        {
            Integer startLevel = m_reference.getStartLevel();
            if( startLevel != null )
            {
                m_startLevelService.setBundleStartLevel( m_bundle, startLevel );
            }
        }
        startIfNecessary();
    }

    /**
     * Performs the actual start.
     *
     * @throws BundleException see start()
     */
    private void doStart()
        throws BundleException
    {
        if( m_bundle != null )
        {
            m_bundle.start();
        }
        m_state = new StartedState();
    }

    /**
     * Internal state of the installable bundle.
     */
    private class State
    {

        /**
         * Does nothing.
         *
         * @throws org.osgi.framework.BundleException
         *          Can not happen.
         */
        void install()
            throws BundleException
        {
            // installation in the actual state
        }

        /**
         * Does nothing.
         *
         * @throws org.osgi.framework.BundleException
         *          Can not happen.
         */
        void start()
            throws BundleException
        {
            // start in the actual state
        }

    }

    /**
     * When the bundle was not installed = initial state.
     */
    private class NotInstalledState
        extends State
    {

        /**
         * Installs the bundle.
         */
        void install()
            throws BundleException
        {
            doInstall();
        }

        /**
         * Starts the bundle.
         */
        void start()
            throws BundleException
        {
            install();
            doStart();
        }

    }

    /**
     * When the bundle was installed. Only start should do something.
     */
    private class InstalledState
        extends State
    {

        /**
         * Starts the bundle.
         */
        void start()
            throws BundleException
        {
            doStart();
        }

    }

    /**
     * When the bundle was installed and started. There is nothing more to do.
     */
    private class StartedState
        extends State
    {

    }

}
