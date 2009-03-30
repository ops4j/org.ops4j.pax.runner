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

import java.util.List;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ProvisionSpec;
import org.ops4j.pax.runner.provision.ScannedBundle;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.util.property.PropertyResolver;

public class BundleScannerTest
{

    @Test( expected = NullArgumentException.class )
    public void scanWithNullURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new BundleScanner( createMock( PropertyResolver.class ) ).scan( null );
    }

    @Test
    public void scanWithValidURL()
        throws ScannerException, MalformedSpecificationException
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( config );
        List<ScannedBundle> scannedBundles = createBundleScanner( config ).scan(
            new ProvisionSpec( "scan-bundle:file:bundle.jar" )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Nuber of bundles", 1, scannedBundles.size() );
        verify( config );
    }

    private BundleScanner createBundleScanner( final ScannerConfiguration config )
    {
        return new BundleScanner( createMock( PropertyResolver.class ) )
        {
            @Override
            ScannerConfiguration createConfiguration()
            {
                return config;
            }
        };
    }

}
