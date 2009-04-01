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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.startlevel.StartLevel;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.provision.InstallableBundle;
import org.ops4j.pax.runner.provision.InstallableBundles;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ProvisionService;
import org.ops4j.pax.runner.provision.ProvisionSpec;
import org.ops4j.pax.runner.provision.ScannedBundle;
import org.ops4j.pax.runner.provision.Scanner;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.UnsupportedSchemaException;

/**
 * Implementation of Provision Service.
 *
 * @author Alin Dreghiciu
 * @see org.ops4j.pax.runner.provision.ProvisionService
 * @since August 17, 2007
 */
public class ProvisionServiceImpl
    implements ProvisionService
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( ProvisionServiceImpl.class );
    /**
     * Scheme -> scanner relation.
     */
    private final Map<String, Scanner> m_scanners;
    /**
     * Bundle context where the bundle is installed.
     */
    private final BundleContext m_bundleContext;
    /**
     * Start Level service.
     */
    private StartLevel m_startLevelService;

    /**
     * Creates a new provision service implementation.
     *
     * @param bundleContext a bundle context
     */
    public ProvisionServiceImpl( final BundleContext bundleContext )
    {
        NullArgumentException.validateNotNull( bundleContext, "Bundle context" );
        m_bundleContext = bundleContext;
        m_scanners = new HashMap<String, Scanner>();
    }

    /**
     * @see org.ops4j.pax.runner.provision.ProvisionService#scan(String)
     */
    public List<ScannedBundle> scan( final String spec )
        throws MalformedSpecificationException, ScannerException
    {
        LOGGER.info( "Scan bundles from [" + spec + "]" );
        final ProvisionSpec provisionSpec = new ProvisionSpec( spec );
        Scanner scanner = m_scanners.get( provisionSpec.getScheme() );
        if( scanner == null )
        {
            throw new UnsupportedSchemaException( "Unknown provisioning scheme [" + provisionSpec.getScheme() + "]" );
        }
        return scan( scanner, provisionSpec );
    }

    /**
     * Wraps a list of scanned bundles as installables. The methods could be overrided by subclasses.
     *
     * @param scannedBundles scanned bundles to be wrapped
     *
     * @return a set of installables
     */
    public InstallableBundles wrap( final List<ScannedBundle> scannedBundles )
    {
        List<InstallableBundle> installables = new ArrayList<InstallableBundle>();
        if( scannedBundles != null )
        {
            for( ScannedBundle scannedBundle : scannedBundles )
            {
                installables.add( wrap( scannedBundle ) );
            }
        }
        return createSet( installables );
    }

    /**
     * Creates a new installable set. The methods could be overrided by subclasses.
     *
     * @param installables a list of installables that makes up the set.
     *
     * @return The installable bundles as an Iterable.
     */
    InstallableBundles createSet( final List<InstallableBundle> installables )
    {
        return new InstallableBundlesImpl( installables );
    }

    /**
     * Wrap a scanned bundle as installable.
     *
     * @param scannedBundle the bscanned bundle to be wrapped
     *
     * @return an installable
     */
    InstallableBundle wrap( final ScannedBundle scannedBundle )
    {
        return new InstallableBundleImpl( m_bundleContext, scannedBundle, m_startLevelService );
    }

    /**
     * Uses the scanner to scan the bundles.
     *
     * @param scanner the scanner to use
     * @param spec    the path part of the specification
     *
     * @return a list of bundles found by the scanner.
     *
     * @throws ScannerException TODO
     * @throws MalformedSpecificationException
     *                          TODO
     */
    private List<ScannedBundle> scan( final Scanner scanner, final ProvisionSpec spec )
        throws ScannerException, MalformedSpecificationException
    {
        List<ScannedBundle> scannedBundles = scanner.scan( spec );
        if( LOGGER.isWarnEnabled() && scannedBundles == null )
        {
            LOGGER.warn( "Scanner did not return any bundle to install for [" + spec + "]" );
        }
        return scannedBundles;
    }

    /**
     * Adds a new scanner.
     *
     * @param scheme  the scheme the scanner handles
     * @param scanner the scanner
     */
    public void addScanner( final Scanner scanner, final String scheme )
    {
        NullArgumentException.validateNotNull( scheme, "Scheme" );
        NullArgumentException.validateNotNull( scanner, "Scanner" );
        synchronized( m_scanners )
        {
            m_scanners.put( scheme, scanner );
        }
        LOGGER.debug( "Added scheme [" + scheme + "] from scanner [" + scanner + "]" );
    }

    /**
     * Removes a scanner.
     *
     * @param scanner the scanner
     */
    public void removeScanner( final Scanner scanner )
    {
        NullArgumentException.validateNotNull( scanner, "Scanner" );
        synchronized( m_scanners )
        {
            for( Map.Entry<String, Scanner> entry : m_scanners.entrySet() )
            {
                if( scanner == entry.getValue() )
                {
                    m_scanners.remove( entry.getKey() );
                    LOGGER.debug( "Removed scheme [" + entry.getKey() + "] scanner [" + scanner + "]" );
                }
            }
        }
    }

    /**
     * Sets the start level service.
     *
     * @param startLevelService a start level service
     */
    public void setStartLevelService( final StartLevel startLevelService )
    {
        m_startLevelService = startLevelService;
    }

}
