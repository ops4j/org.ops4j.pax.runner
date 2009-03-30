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
import org.ops4j.pax.runner.provision.InstallableBundle;
import org.ops4j.pax.runner.provision.ScannedBundle;

public class InstallableBundleImplTest
{

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullBundleContext()
    {
        new InstallableBundleImpl( null, createMock( ScannedBundle.class ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullScannedBundle()
    {
        new InstallableBundleImpl( createMock( BundleContext.class ), null );
    }

    @Test
    public void constructorWithNullStartLevel()
    {
        new InstallableBundleImpl( createMock( BundleContext.class ), createMock( ScannedBundle.class ), null );
    }

    @Test
    public void getBundleBeforeInstallation()
    {
        assertNull( "Bundle should be null before installing",
                    new InstallableBundleImpl( createMock( BundleContext.class ), createMock( ScannedBundle.class )
                    ).getBundle()
        );
    }

    @Test( expected = BundleException.class )
    public void installWithInvalidScannedBundle()
        throws BundleException
    {
        BundleContext context = createMock( BundleContext.class );
        ScannedBundle scannedBundle = createMock( ScannedBundle.class );
        expect( scannedBundle.getLocation() ).andReturn( null );
        replay( context, scannedBundle );
        new InstallableBundleImpl( context, scannedBundle ).install();
        verify( context, scannedBundle );
    }

    @Test
    public void installWhenBundleShouldBeStarted()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        ScannedBundle scannedBundle = createMock( ScannedBundle.class );
        Bundle bundle = createMock( Bundle.class );
        expect( scannedBundle.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( scannedBundle.shouldUpdate() ).andReturn( false );
        expect( scannedBundle.shouldStart() ).andReturn( true );
        bundle.start();
        replay( context, scannedBundle, bundle );
        new InstallableBundleImpl( context, scannedBundle ).install();
        verify( context, scannedBundle, bundle );
    }

    @Test
    public void installWhenBundleShouldNotBeStarted()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        ScannedBundle scannedBundle = createMock( ScannedBundle.class );
        Bundle bundle = createMock( Bundle.class );
        expect( scannedBundle.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( scannedBundle.shouldUpdate() ).andReturn( false );
        expect( scannedBundle.shouldStart() ).andReturn( false );
        replay( context, scannedBundle, bundle );
        new InstallableBundleImpl( context, scannedBundle ).install();
        verify( context, scannedBundle, bundle );
    }

    @Test
    public void installWithStartLevel()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        ScannedBundle scannedBundle = createMock( ScannedBundle.class );
        Bundle bundle = createMock( Bundle.class );
        StartLevel startLevel = createMock( StartLevel.class );
        expect( scannedBundle.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( scannedBundle.shouldUpdate() ).andReturn( false );
        expect( scannedBundle.getStartLevel() ).andReturn( 5 );
        startLevel.setBundleStartLevel( bundle, 5 );
        expect( scannedBundle.shouldStart() ).andReturn( false );
        replay( context, scannedBundle, bundle, startLevel );
        new InstallableBundleImpl( context, scannedBundle, startLevel ).install();
        verify( context, scannedBundle, bundle, startLevel );
    }

    @Test
    public void installWithStartLevelButNoBundleStartLevel()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        ScannedBundle scannedBundle = createMock( ScannedBundle.class );
        Bundle bundle = createMock( Bundle.class );
        StartLevel startLevel = createMock( StartLevel.class );
        expect( scannedBundle.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( scannedBundle.shouldUpdate() ).andReturn( false );
        expect( scannedBundle.getStartLevel() ).andReturn( null );
        expect( scannedBundle.shouldStart() ).andReturn( false );
        replay( context, scannedBundle, bundle, startLevel );
        new InstallableBundleImpl( context, scannedBundle, startLevel ).install();
        verify( context, scannedBundle, bundle, startLevel );
    }

    @Test
    public void startAfterInstallation()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        ScannedBundle scannedBundle = createMock( ScannedBundle.class );
        Bundle bundle = createMock( Bundle.class );
        expect( scannedBundle.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( scannedBundle.shouldUpdate() ).andReturn( false );
        expect( scannedBundle.shouldStart() ).andReturn( false );
        replay( context, scannedBundle, bundle );
        InstallableBundle installable = new InstallableBundleImpl( context, scannedBundle ).install();
        verify( context, scannedBundle, bundle );
        reset( context, scannedBundle, bundle );
        bundle.start();
        replay( context, scannedBundle, bundle );
        installable.start();
        verify( context, scannedBundle, bundle );
    }

    @Test
    public void startBeforeInstallation()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        ScannedBundle scannedBundle = createMock( ScannedBundle.class );
        Bundle bundle = createMock( Bundle.class );
        expect( scannedBundle.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( scannedBundle.shouldUpdate() ).andReturn( false );
        expect( scannedBundle.shouldStart() ).andReturn( false );
        bundle.start();
        replay( context, scannedBundle, bundle );
        new InstallableBundleImpl( context, scannedBundle ).start();
        verify( context, scannedBundle, bundle );
    }

    @Test
    public void installAfterInstall()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        ScannedBundle scannedBundle = createMock( ScannedBundle.class );
        Bundle bundle = createMock( Bundle.class );
        expect( scannedBundle.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( scannedBundle.shouldUpdate() ).andReturn( false );
        expect( scannedBundle.shouldStart() ).andReturn( false );
        replay( context, scannedBundle, bundle );
        new InstallableBundleImpl( context, scannedBundle ).install().install();
        verify( context, scannedBundle, bundle );
    }

    @Test
    public void startAfterStart()
        throws BundleException, MalformedURLException
    {
        BundleContext context = createMock( BundleContext.class );
        ScannedBundle scannedBundle = createMock( ScannedBundle.class );
        Bundle bundle = createMock( Bundle.class );
        expect( scannedBundle.getLocation() ).andReturn( "file:bundle.jar" );
        expect( context.installBundle( "file:bundle.jar" ) ).andReturn( bundle );
        expect( scannedBundle.shouldUpdate() ).andReturn( false );
        expect( scannedBundle.shouldStart() ).andReturn( false );
        bundle.start();
        replay( context, scannedBundle, bundle );
        new InstallableBundleImpl( context, scannedBundle ).start().start();
        verify( context, scannedBundle, bundle );
    }

}
