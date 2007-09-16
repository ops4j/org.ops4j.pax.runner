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
package org.ops4j.pax.runner.commons.resolver;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.osgi.framework.BundleContext;

public class BundleContextResolverTest
{

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullBundleContext()
    {
        new BundleContextResolver( null );
    }

    @Test
    public void getPropertyFromContext()
    {
        BundleContext bc = createMock( BundleContext.class );
        expect( bc.getProperty( "ns.property" ) ).andReturn( "value" );
        replay( bc );
        Resolver resolver = new BundleContextResolver( bc );
        assertEquals( "Property", "value", resolver.get( "ns.property" ) );
        verify( bc );
    }

    @Test
    public void getEmptyProperty()
    {
        BundleContext bc = createMock( BundleContext.class );
        expect( bc.getProperty( "ns.property" ) ).andReturn( "" );
        replay( bc );
        Resolver resolver = new BundleContextResolver( bc );
        assertEquals( "Property", null, resolver.get( "ns.property" ) );
        verify( bc );
    }

}
