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
package org.ops4j.pax.runner.platform.internal;

import java.util.Dictionary;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.ops4j.pax.runner.platform.Platform;
import org.ops4j.pax.runner.platform.PlatformBuilder;
import static org.ops4j.pax.runner.platform.internal.Capture.*;

public class ActivatorTest
{

    @Test( expected = IllegalArgumentException.class )
    public void startWithNullBundleContext()
        throws Exception
    {
        new Activator().start( null );
    }

    @Test
    public void start()
        throws Exception
    {
        BundleContext context = createMock( BundleContext.class );
        expect( context.registerService( eq( ManagedService.class.getName() ), notNull(), (Dictionary) notNull() )
        ).andReturn( null );
        expect( context.createFilter( "(objectClass=" + PlatformBuilder.class.getName() + ")" )
        ).andReturn( createMock( Filter.class ) );
        context.addServiceListener(
            (ServiceListener) notNull(),
            eq( "(objectClass=" + PlatformBuilder.class.getName() + ")" )
        );
        expect( context.getServiceReferences(
            eq( PlatformBuilder.class.getName() ),
            (String) isNull()
        )
        ).andReturn( null );
        replay( context );
        new Activator().start( context );
        verify( context );
    }

    // test that a platform is registered onec a platform builder is registered
    @Test
    public void registerPlatformBuilder()
        throws Exception
    {
        final PlatformBuilder builder = createMock( PlatformBuilder.class );
        BundleContext context = createMock( BundleContext.class );
        Filter filter = createMock( Filter.class );
        ServiceReference reference = createMock( ServiceReference.class );
        final Platform platform = createMock( Platform.class );

        expect( context.registerService( eq( ManagedService.class.getName() ), notNull(), (Dictionary) notNull() )
        ).andReturn( null );
        expect( context.createFilter( "(objectClass=" + PlatformBuilder.class.getName() + ")" ) ).andReturn( filter );
        Capture<ServiceListener> listenerCapture = new Capture<ServiceListener>();
        context.addServiceListener( capture( listenerCapture ), (String) notNull() );
        expect( context.getServiceReferences( PlatformBuilder.class.getName(), null ) ).andReturn( null );
        expect( context.getService( reference ) ).andReturn( builder );
        expect( reference.getPropertyKeys() ).andReturn( null );
        expect( context.registerService(
            eq( Platform.class.getName() ), eq( platform ), (Dictionary) notNull()
        )
        ).andReturn( null );

        replay( builder, context, filter, reference, platform );
        new Activator()
        {
            @Override
            Platform createPlatform( final PlatformBuilder platformBuilder )
            {
                assertEquals( "Platform builder", builder, platformBuilder );
                return platform;
            }
        }.start( context );
        listenerCapture.getCaptured().serviceChanged( new ServiceEvent( ServiceEvent.REGISTERED, reference ) );
        verify( builder, context, filter, reference, platform );
    }

    // test that a platform is unregistered once a platform builder is unregistred
    // also checks that the stop will no longer unregister the already unregistred platform
    // check out that the test can fail if the registration fail, usualy when also registerPlatformBuilder fail
    @Test
    public void unregisterPlatformBuilder()
        throws Exception
    {
        final PlatformBuilder builder = createMock( PlatformBuilder.class );
        BundleContext context = createMock( BundleContext.class );
        Filter filter = createMock( Filter.class );
        ServiceReference reference = createMock( ServiceReference.class );
        final Platform platform = createMock( Platform.class );
        ServiceRegistration registration = createMock( ServiceRegistration.class );

        expect( context.registerService( eq( ManagedService.class.getName() ), notNull(), (Dictionary) notNull() )
        ).andReturn( null );
        expect( context.createFilter( "(objectClass=" + PlatformBuilder.class.getName() + ")" ) ).andReturn( filter );
        Capture<ServiceListener> listenerCapture = new Capture<ServiceListener>();
        context.addServiceListener( capture( listenerCapture ), (String) notNull() );
        expect( context.getServiceReferences( PlatformBuilder.class.getName(), null ) ).andReturn( null );
        expect( context.getService( reference ) ).andReturn( builder );
        expect( reference.getPropertyKeys() ).andReturn( null );
        expect( context.registerService(
            eq( Platform.class.getName() ), eq( platform ), (Dictionary) notNull()
        )
        ).andReturn( registration );
        expect( context.ungetService( reference ) ).andReturn( true );
        registration.unregister();
        context.removeServiceListener( (ServiceListener) notNull() );

        replay( builder, context, filter, reference, platform, registration );
        final Activator activator = new Activator()
        {
            @Override
            Platform createPlatform( final PlatformBuilder platformBuilder )
            {
                assertEquals( "Platform builder", builder, platformBuilder );
                return platform;
            }
        };
        activator.start( context );
        listenerCapture.getCaptured().serviceChanged( new ServiceEvent( ServiceEvent.REGISTERED, reference ) );
        listenerCapture.getCaptured().serviceChanged( new ServiceEvent( ServiceEvent.UNREGISTERING, reference ) );
        activator.stop( context );
        verify( builder, context, filter, reference, platform, registration );
    }

    @Test( expected = IllegalArgumentException.class )
    public void stopWithNullBundleContext()
        throws Exception
    {
        new Activator().stop( null );
    }

    @Test
    public void stop()
        throws Exception
    {
        BundleContext context = createMock( BundleContext.class );

        replay( context );
        new Activator().stop( context );
        verify( context );
    }

}
