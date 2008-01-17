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
import org.ops4j.util.property.PropertyResolver;

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
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( PID + ".startLevel" ) ).andReturn( "5" );
        replay( propertyResolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( propertyResolver, PID );
        Integer startlevel = config.getStartLevel();
        verify( propertyResolver );
        assertNotNull( "Start level is null", startlevel );
        assertEquals( "Start level", Integer.valueOf( 5 ), startlevel );
    }

    @Test
    public void getNotConfiguredStartLevel()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( PID + ".startLevel" ) ).andReturn( null );
        replay( propertyResolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( propertyResolver, PID );
        Integer startlevel = config.getStartLevel();
        verify( propertyResolver );
        assertEquals( "Start level", null, startlevel );
    }

    @Test
    public void getWrongConfiguredStartLevel()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( PID + ".startLevel" ) ).andReturn( "wrong" );
        replay( propertyResolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( propertyResolver, PID );
        Integer startlevel = config.getStartLevel();
        verify( propertyResolver );
        assertEquals( "Start level", null, startlevel );
    }

    @Test
    public void getStart()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( PID + ".start" ) ).andReturn( "false" );
        replay( propertyResolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( propertyResolver, PID );
        Boolean shouldStart = config.shouldStart();
        verify( propertyResolver );
        assertNotNull( "Start is null", shouldStart );
        assertEquals( "Start", false, shouldStart );
    }

    @Test
    public void getNotConfiguredStart()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( PID + ".start" ) ).andReturn( null );
        replay( propertyResolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( propertyResolver, PID );
        Boolean shouldStart = config.shouldStart();
        verify( propertyResolver );
        assertEquals( "Start", true, shouldStart );
    }

    @Test
    public void getWrongConfiguredStart()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( PID + ".start" ) ).andReturn( "wrong" );
        replay( propertyResolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( propertyResolver, PID );
        Boolean shouldStart = config.shouldStart();
        verify( propertyResolver );
        assertEquals( "Start", false, shouldStart );
    }

    @Test
    public void getUpdate()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( PID + ".update" ) ).andReturn( "true" );
        replay( propertyResolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( propertyResolver, PID );
        Boolean update = config.shouldUpdate();
        verify( propertyResolver );
        assertNotNull( "Update is null", update );
        assertEquals( "Update", true, update );
    }

    @Test
    public void getNotConfiguredUpdate()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( PID + ".update" ) ).andReturn( null );
        replay( propertyResolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( propertyResolver, PID );
        Boolean update = config.shouldUpdate();
        verify( propertyResolver );
        assertEquals( "Update", false, update );
    }

    @Test
    public void getWrongConfiguredUpdate()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( PID + ".update" ) ).andReturn( "wrong" );
        replay( propertyResolver );
        ScannerConfiguration config = new ScannerConfigurationImpl( propertyResolver, PID );
        Boolean update = config.shouldUpdate();
        verify( propertyResolver );
        assertEquals( "Update", false, update );
    }

}
