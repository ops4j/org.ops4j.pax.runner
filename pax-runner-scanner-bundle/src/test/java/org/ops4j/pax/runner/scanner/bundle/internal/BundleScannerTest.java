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
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;

public class BundleScannerTest
{

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithNullURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( config );
        List<BundleReference> references = createFileScanner( config ).scan( null );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithEmptyURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( config );
        List<BundleReference> references = createFileScanner( config ).scan( " " );
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
        List<BundleReference> references = createFileScanner( config ).scan( "file:bundle.jar" );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Nuber of bundles", 1, references.size() );
        verify( config );
    }

    private BundleScanner createFileScanner( final ScannerConfiguration config )
    {
        return new BundleScanner( createMock( Resolver.class ) )
        {
            @Override
            ScannerConfiguration createConfiguration()
            {
                return config;
            }
        };
    }

}
