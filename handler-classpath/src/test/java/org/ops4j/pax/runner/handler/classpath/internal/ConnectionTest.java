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
package org.ops4j.pax.runner.handler.classpath.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.pax.runner.commons.file.FileUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class ConnectionTest
{

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullURL()
        throws MalformedURLException
    {
        new Connection( null, createMock( BundleContext.class ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullBundleContext()
        throws MalformedURLException
    {
        // the classpath protocol name is not important here so we use http for easier testing
        new Connection( new URL( "http:resource" ), null );
    }

    @Test
    public void searchFirstTheThreadClasspath()
        throws IOException
    {
        BundleContext context = createMock( BundleContext.class );
        replay( context );
        InputStream is = new Connection( new URL( "http:connection/resource" ), context ).getInputStream();
        assertNotNull( "Returned input stream is null", is );
        verify( context );
    }

    @Test
    public void searchSpecificBundle()
        throws IOException
    {
        BundleContext context = createMock( BundleContext.class );
        Bundle bundle1 = createMock( Bundle.class );
        Bundle bundle2 = createMock( Bundle.class );
        expect( context.getBundles() ).andReturn( new Bundle[]{ bundle1, bundle2 } );
        expect( bundle1.getSymbolicName() ).andReturn( "bundle1" );
        expect( bundle2.getSymbolicName() ).andReturn( "bundle2" );
        expect( bundle2.getResource( "fake" ) ).andReturn(
            FileUtils.getFileFromClasspath( "connection/resource" ).toURL()
        );
        replay( context, bundle1, bundle2 );
        InputStream is = new Connection( new URL( "http://bundle2/fake" ), context ).getInputStream();
        assertNotNull( "Returned input stream is null", is );
        verify( context, bundle1, bundle2 );
    }

    @Test
    public void searchInstalledBundles()
        throws IOException
    {
        BundleContext context = createMock( BundleContext.class );
        Bundle bundle1 = createMock( Bundle.class );
        Bundle bundle2 = createMock( Bundle.class );
        expect( context.getBundles() ).andReturn( new Bundle[]{ bundle1, bundle2 } );
        expect( bundle1.getResource( "fake" ) ).andReturn( null );
        expect( bundle2.getResource( "fake" ) ).andReturn(
            FileUtils.getFileFromClasspath( "connection/resource" ).toURL()
        );
        replay( context, bundle1, bundle2 );
        InputStream is = new Connection( new URL( "http:fake" ), context ).getInputStream();
        assertNotNull( "Returned input stream is null", is );
        verify( context, bundle1, bundle2 );
    }

    @Test( expected = IOException.class )
    public void searchInstalledBundlesWhenThereAreNoBundles()
        throws IOException
    {
        BundleContext context = createMock( BundleContext.class );
        expect( context.getBundles() ).andReturn( null );
        replay( context );
        InputStream is = new Connection( new URL( "http:nonExisting" ), context ).getInputStream();
        verify( context );
    }

}
