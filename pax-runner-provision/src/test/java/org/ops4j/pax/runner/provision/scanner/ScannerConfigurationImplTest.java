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
package org.ops4j.pax.runner.provision.scanner;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.pax.runner.provision.scanner.ScannerConfigurationImpl;

public class ScannerConfigurationImplTest
{

    private static final String PID = "pid";

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullResolver()
    {
        new ScannerConfigurationImpl( null, PID );
    }

    @Test
    public void getStartLevel()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( PID + ".startLevel" ) ).andReturn( "5" );
        replay( resolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( resolver, PID );
        Integer startlevel = config.getStartLevel();
        verify( resolver );
        assertNotNull( "Start level is null", startlevel );
        assertEquals( "Start level", Integer.valueOf( 5), startlevel );
    }

    @Test
    public void getNotConfiguredStartLevel()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( PID + ".startLevel" ) ).andReturn( null );
        replay( resolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( resolver, PID );
        Integer startlevel = config.getStartLevel();
        verify( resolver );
        assertEquals( "Start level", null, startlevel );
    }

    @Test
    public void getWrongConfiguredStartLevel()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( PID + ".startLevel" ) ).andReturn( "wrong" );
        replay( resolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( resolver, PID );
        Integer startlevel = config.getStartLevel();
        verify( resolver );
        assertEquals( "Start level", null, startlevel );
    }

    @Test
    public void getStart()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( PID + ".start" ) ).andReturn( "false" );
        replay( resolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( resolver, PID );
        Boolean shouldStart = config.shouldStart();
        verify( resolver );
        assertNotNull( "Start is null", shouldStart );
        assertEquals( "Start", false, shouldStart );
    }

    @Test
    public void getNotConfiguredStart()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( PID + ".start" ) ).andReturn( null );
        replay( resolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( resolver, PID );
        Boolean shouldStart = config.shouldStart();
        verify( resolver );
        assertEquals( "Start", true, shouldStart );
    }

    @Test
    public void getWrongConfiguredStart()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( PID + ".start" ) ).andReturn( "wrong" );
        replay( resolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( resolver, PID );
        Boolean shouldStart = config.shouldStart();
        verify( resolver );
        assertEquals( "Start", false, shouldStart );
    }

    @Test
    public void getUpdate()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( PID + ".update" ) ).andReturn( "true" );
        replay( resolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( resolver, PID );
        Boolean update = config.shouldUpdate();
        verify( resolver );
        assertNotNull( "Update is null", update );
        assertEquals( "Update", true, update );
    }

    @Test
    public void getNotConfiguredUpdate()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( PID + ".update" ) ).andReturn( null );
        replay( resolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( resolver, PID );
        Boolean update = config.shouldUpdate();
        verify( resolver );
        assertEquals( "Update", false, update );
    }

    @Test
    public void getWrongConfiguredUpdate()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( PID + ".update" ) ).andReturn( "wrong" );
        replay( resolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( resolver, PID );
        Boolean update = config.shouldUpdate();
        verify( resolver );
        assertEquals( "Update", false, update );
    }

}
