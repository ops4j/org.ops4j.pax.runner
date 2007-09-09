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
import java.util.Iterator;
import java.util.List;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.InstallableBundles;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.Scanner;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.UnsupportedSchemaException;
import org.osgi.framework.BundleContext;

public class ProvisionServiceImplTest
{

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithNullSpec()
        throws MalformedSpecificationException, ScannerException
    {
        new ProvisionServiceImpl( createMock( BundleContext.class ) ).scan( null );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithEmptySpec()
        throws MalformedSpecificationException, ScannerException
    {
        new ProvisionServiceImpl( createMock( BundleContext.class ) ).scan( " " );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithNoScheme()
        throws MalformedSpecificationException, ScannerException
    {
        new ProvisionServiceImpl( createMock( BundleContext.class ) ).scan( "noscheme" );
    }

    @Test( expected = UnsupportedSchemaException.class )
    public void scanWithUnknownScheme()
        throws MalformedSpecificationException, ScannerException
    {
        new ProvisionServiceImpl( createMock( BundleContext.class ) ).scan( "noscheme:" );
    }

    @Test
    public void scanWithKnownScheme()
        throws MalformedSpecificationException, ScannerException
    {
        Scanner scanner = createMock( Scanner.class );
        BundleReference reference = createMock( BundleReference.class );
        List<BundleReference> references = new ArrayList<BundleReference>();
        references.add( reference );
        expect( scanner.scan( "anURL" ) ).andReturn( references );
        replay( scanner, reference );
        ProvisionServiceImpl service = new ProvisionServiceImpl( createMock( BundleContext.class ) );
        service.addScanner( scanner, "scheme" );
        InstallableBundles set = service.scan( "scheme:anURL" );
        assertNotNull( "Returned installable bundles set is null", set );
        Iterator it = set.iterator();
        assertNotNull( "Returned iterator is null", it );
        assertTrue( "Iterator should have at least one installable bunlde", it.hasNext() );
        assertNotNull( "There should be a valid installable bundle", it.next() );
        verify( scanner, reference );
    }

    @Test
    public void scannerReturningNull()
        throws MalformedSpecificationException, ScannerException
    {
        Scanner scanner = createMock( Scanner.class );
        expect( scanner.scan( "anURL" ) ).andReturn( null );
        replay( scanner );
        ProvisionServiceImpl service = new ProvisionServiceImpl( createMock( BundleContext.class ) );
        service.addScanner( scanner, "scheme" );
        InstallableBundles set = service.scan( "scheme:anURL" );
        assertNotNull( "Returned installable bundles set is null", set );
        Iterator it = set.iterator();
        assertNotNull( "Returned iterator is null", it );
        assertTrue( "Iterator should have no bundles", !it.hasNext() );
        verify( scanner );
    }

}
