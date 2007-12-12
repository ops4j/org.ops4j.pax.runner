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
package org.ops4j.pax.runner.scanner.bundle.internal;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.Scanner;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.FileBundleReference;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.pax.runner.provision.scanner.ScannerConfigurationImpl;
import org.ops4j.pax.runner.scanner.bundle.ServiceConstants;

/**
 * A scanner that scans plain text file.
 *
 * @author Alin Dreghiciu
 * @since August 15, 2007
 */
public class BundleScanner
    implements Scanner
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( BundleScanner.class );
    /**
     * Resolver used to resolve properties.
     */
    private Resolver m_resolver;

    /**
     * Creates a new scanner.
     *
     * @param resolver a resolver; mandatory
     */
    public BundleScanner( final Resolver resolver )
    {
        Assert.notNull( "Resolver", resolver );
        m_resolver = resolver;
    }

    /**
     * Reads the bundles from the file specified by the urlSpec.
     *
     * @param urlSpec url spec to the text file containing the bundle.
     */
    public List<BundleReference> scan( final String urlSpec )
        throws MalformedSpecificationException, ScannerException
    {
        LOGGER.debug( "Scanning [" + urlSpec + "]" );
        final List<BundleReference> references = new ArrayList<BundleReference>();
        final ScannerConfiguration config = createConfiguration();
        try
        {
            references.add(
                new FileBundleReference(
                    urlSpec,
                    config.getStartLevel(),
                    config.shouldStart(),
                    config.shouldUpdate()
                )
            );
        }
        catch( MalformedURLException e )
        {
            throw new MalformedSpecificationException( "Invalid url", e );
        }
        return references;
    }

    /**
     * Sets the resolver to use.
     *
     * @param resolver a resolver
     */
    public void setResolver( final Resolver resolver )
    {
        Assert.notNull( "Resolver", resolver );
        m_resolver = resolver;
    }

    /**
     * Creates a new configuration.
     *
     * @return a configuration
     */
    ScannerConfiguration createConfiguration()
    {
        return new ScannerConfigurationImpl( m_resolver, ServiceConstants.PID );
    }

}
