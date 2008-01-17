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
package org.ops4j.pax.runner.platform.builder;

import java.util.Dictionary;
import static org.easymock.EasyMock.*;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.runner.platform.PlatformBuilder;

public class AbstractPlatformBuilderActivatorTest
{

    private PlatformBuilder m_platformBuilder1;
    private PlatformBuilder m_platformBuilder2;

    @Before
    public void setUp()
    {
        m_platformBuilder1 = createMock( PlatformBuilder.class );
        m_platformBuilder2 = createMock( PlatformBuilder.class );
    }

    @Test( expected = IllegalArgumentException.class )
    public void startWithNullBundleContext()
        throws Exception
    {
        new TestAPBActivator().start( null );
    }

    @Test
    public void start()
        throws Exception
    {
        BundleContext context = createMock( BundleContext.class );
        expect( context.registerService(
            eq( PlatformBuilder.class.getName() ),
            eq( m_platformBuilder1 ),
            (Dictionary) notNull()
        )
        ).andReturn( null );
        expect( context.registerService(
            eq( PlatformBuilder.class.getName() ),
            eq( m_platformBuilder2 ),
            (Dictionary) notNull()
        )
        ).andReturn( null );
        expect( m_platformBuilder1.getProviderName() ).andReturn( "provider" );
        expect( m_platformBuilder1.getProviderVersion() ).andReturn( "version1" );
        expect( m_platformBuilder2.getProviderName() ).andReturn( "provider" );
        expect( m_platformBuilder2.getProviderVersion() ).andReturn( "version2" );

        replay( context, m_platformBuilder1, m_platformBuilder2 );
        new TestAPBActivator().start( context );
        verify( context, m_platformBuilder1, m_platformBuilder2 );
    }

    @Test( expected = IllegalArgumentException.class )
    public void stopWithNullBundleContext()
        throws Exception
    {
        new TestAPBActivator().stop( null );
    }

    @Test
    public void stop()
        throws Exception
    {
        BundleContext context = createMock( BundleContext.class );
        replay( context );
        new TestAPBActivator().stop( context );
        verify( context );
    }

    private class TestAPBActivator extends AbstractPlatformBuilderActivator
    {

        @Override
        protected PlatformBuilder[] createPlatformBuilders( final BundleContext bundleContext )
        {
            return new PlatformBuilder[]{ m_platformBuilder1, m_platformBuilder2 };
        }

    }

}
