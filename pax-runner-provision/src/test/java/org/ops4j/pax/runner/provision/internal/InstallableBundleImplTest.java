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

import java.net.MalformedURLException;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.startlevel.StartLevel;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.InstallableBundle;

public class InstallableBundleImplTest
{

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullBundleContext()
    {
        new InstallableBundleImpl( null, createMock( BundleReference.class ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullReference()
    {
        new InstallableBundleImpl( createMock( BundleContext.class ), null );
    }

    @Test
    public void constructorWithNullStartLevel()
    {
        new InstallableBundleImpl( createMock( BundleContext.class ), createMock( BundleReference.class ), null );
    }

    @Test
    public void getBundleBeforeInstallation()
    {
        assertNull( "Bundle should be null before installing",
                    new InstallableBundleImpl( createMock( BundleContext.class ), createMock( BundleReference.class )
                    ).getBundle()
        );
    }

    @Test( expected = BundleException.class )
    public void installWithInvalidBundleReference()
        throws BundleException
    {
        BundleContext context = createMock( BundleContext.class );
        BundleReference reference = createMock( BundleReference.class );
        expect( reference.getLocation() ).andReturn( null );
        replay( context, reference );
        new InstallableBundleImpl( context, reference ).install();
        verify( context, reference );
    }

    @Test
    public void installWhenBundleShouldBeStarted()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        BundleReference reference = createMock( BundleReference.class );
        Bundle bundle = createMock( Bundle.class );
        expect( reference.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( reference.shouldUpdate() ).andReturn( false );
        expect( reference.shouldStart() ).andReturn( true );
        bundle.start();
        replay( context, reference, bundle );
        new InstallableBundleImpl( context, reference ).install();
        verify( context, reference, bundle );
    }

    @Test
    public void installWhenBundleShouldNotBeStarted()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        BundleReference reference = createMock( BundleReference.class );
        Bundle bundle = createMock( Bundle.class );
        expect( reference.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( reference.shouldUpdate() ).andReturn( false );
        expect( reference.shouldStart() ).andReturn( false );
        replay( context, reference, bundle );
        new InstallableBundleImpl( context, reference ).install();
        verify( context, reference, bundle );
    }

    @Test
    public void installWithStartLevel()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        BundleReference reference = createMock( BundleReference.class );
        Bundle bundle = createMock( Bundle.class );
        StartLevel startLevel = createMock( StartLevel.class );
        expect( reference.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( reference.shouldUpdate() ).andReturn( false );
        expect( reference.getStartLevel() ).andReturn( 5 );
        startLevel.setBundleStartLevel( bundle, 5 );
        expect( reference.shouldStart() ).andReturn( false );
        replay( context, reference, bundle, startLevel );
        new InstallableBundleImpl( context, reference, startLevel ).install();
        verify( context, reference, bundle, startLevel );
    }

    @Test
    public void installWithStartLevelButNoBundleStartLevel()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        BundleReference reference = createMock( BundleReference.class );
        Bundle bundle = createMock( Bundle.class );
        StartLevel startLevel = createMock( StartLevel.class );
        expect( reference.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( reference.shouldUpdate() ).andReturn( false );
        expect( reference.getStartLevel() ).andReturn( null );
        expect( reference.shouldStart() ).andReturn( false );
        replay( context, reference, bundle, startLevel );
        new InstallableBundleImpl( context, reference, startLevel ).install();
        verify( context, reference, bundle, startLevel );
    }

    @Test
    public void startAfterInstallation()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        BundleReference reference = createMock( BundleReference.class );
        Bundle bundle = createMock( Bundle.class );
        expect( reference.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( reference.shouldUpdate() ).andReturn( false );
        expect( reference.shouldStart() ).andReturn( false );
        replay( context, reference, bundle );
        InstallableBundle installable = new InstallableBundleImpl( context, reference ).install();
        verify( context, reference, bundle );
        reset( context, reference, bundle );
        bundle.start();
        replay( context, reference, bundle );
        installable.start();
        verify( context, reference, bundle );
    }

    @Test
    public void startBeforeInstallation()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        BundleReference reference = createMock( BundleReference.class );
        Bundle bundle = createMock( Bundle.class );
        expect( reference.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( reference.shouldUpdate() ).andReturn( false );
        expect( reference.shouldStart() ).andReturn( false );
        bundle.start();
        replay( context, reference, bundle );
        new InstallableBundleImpl( context, reference ).start();
        verify( context, reference, bundle );
    }

    @Test
    public void installAfterInstall()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        BundleReference reference = createMock( BundleReference.class );
        Bundle bundle = createMock( Bundle.class );
        expect( reference.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( reference.shouldUpdate() ).andReturn( false );
        expect( reference.shouldStart() ).andReturn( false );
        replay( context, reference, bundle );
        new InstallableBundleImpl( context, reference ).install().install();
        verify( context, reference, bundle );
    }

    @Test
    public void startAfterStart()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        BundleReference reference = createMock( BundleReference.class );
        Bundle bundle = createMock( Bundle.class );
        expect( reference.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( reference.shouldUpdate() ).andReturn( false );
        expect( reference.shouldStart() ).andReturn( false );
        bundle.start();
        replay( context, reference, bundle );
        new InstallableBundleImpl( context, reference ).start().start();
        verify( context, reference, bundle );
    }

}
