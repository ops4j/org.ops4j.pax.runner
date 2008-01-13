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
package org.ops4j.pax.runner.handler.wrap.internal;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.pax.runner.commons.resolver.Resolver;

public class ConfigurationImplTest
{

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullResolver()
    {
        new ConfigurationImpl( null );
    }

    @Test
    public void getCertificateCheck()
    {
        Resolver resolver = createMock( Resolver.class );

        expect( resolver.get( "org.ops4j.pax.runner.handler.wrap.certificateCheck" ) ).andReturn( "true" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Certificate check", true, config.getCertificateCheck() );
        verify( resolver );
    }

    @Test
    public void getDefaultCertificateCheck()
    {
        Resolver resolver = createMock( Resolver.class );

        expect( resolver.get( "org.ops4j.pax.runner.handler.wrap.certificateCheck" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Certificate check", false, config.getCertificateCheck() );
        verify( resolver );
    }

}
