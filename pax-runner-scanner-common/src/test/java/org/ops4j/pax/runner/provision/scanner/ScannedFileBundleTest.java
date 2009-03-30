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

import java.net.MalformedURLException;
import static org.junit.Assert.*;
import org.junit.Test;

public class ScannedFileBundleTest
{

    @Test( expected = MalformedURLException.class )
    public void constructorWithNullReference()
        throws MalformedURLException
    {
        new ScannedFileBundle( null );
    }

    @Test( expected = MalformedURLException.class )
    public void constructorWithEmptyReference()
        throws MalformedURLException
    {
        new ScannedFileBundle( "" );
    }

    @Test( expected = MalformedURLException.class )
    public void constructorWithOnlySpacesReference()
        throws MalformedURLException
    {
        new ScannedFileBundle( "  " );
    }

    @Test
    public void constructorWithURL()
        throws MalformedURLException
    {
        ScannedFileBundle br = new ScannedFileBundle( "file://afile" );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", null, br.getStartLevel() );
        assertEquals( "start", Boolean.TRUE, br.shouldStart() );
        assertEquals( "update", Boolean.FALSE, br.shouldUpdate() );
    }

    @Test( expected = MalformedURLException.class )
    public void constructorWithoutURL()
        throws MalformedURLException
    {
        new ScannedFileBundle( "@5:nostart" );
    }

    @Test( expected = MalformedURLException.class )
    public void constructorEndingWithSeparator()
        throws MalformedURLException
    {
        new ScannedFileBundle( "file://afile@5@nostart@" );
    }

    @Test( expected = MalformedURLException.class )
    public void constructorWithInvalidOption()
        throws MalformedURLException
    {
        new ScannedFileBundle( "file://afile@5@nostart@invalid" );
    }

    @Test
    public void constructorWithURLAndNoStart()
        throws MalformedURLException
    {
        ScannedFileBundle br = new ScannedFileBundle( "file://afile@nostart" );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", null, br.getStartLevel() );
        assertEquals( "nostart", Boolean.FALSE, br.shouldStart() );
        assertEquals( "update", Boolean.FALSE, br.shouldUpdate() );
    }

    @Test
    public void constructorWithURLAndStartLevel()
        throws MalformedURLException
    {
        ScannedFileBundle br = new ScannedFileBundle( "file://afile@5" );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", Integer.valueOf( 5 ), br.getStartLevel() );
        assertEquals( "start", Boolean.TRUE, br.shouldStart() );
        assertEquals( "update", Boolean.FALSE, br.shouldUpdate() );
    }

    @Test
    public void constructorWithURLAndUpdate()
        throws MalformedURLException
    {
        ScannedFileBundle br = new ScannedFileBundle( "file://afile@update" );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", null, br.getStartLevel() );
        assertEquals( "start", Boolean.TRUE, br.shouldStart() );
        assertEquals( "update", Boolean.TRUE, br.shouldUpdate() );
    }

    @Test
    public void constructorWithURLAndStartLevelAndNoStart()
        throws MalformedURLException
    {
        ScannedFileBundle br = new ScannedFileBundle( "file://afile@5@nostart" );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", Integer.valueOf( 5 ), br.getStartLevel() );
        assertEquals( "start", Boolean.FALSE, br.shouldStart() );
        assertEquals( "update", Boolean.FALSE, br.shouldUpdate() );
    }

    @Test
    public void constructorWithURLAndStartLevelAndNStartAndUpdate()
        throws MalformedURLException
    {
        ScannedFileBundle br = new ScannedFileBundle( "file://afile@5@nostart@update" );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", Integer.valueOf( 5 ), br.getStartLevel() );
        assertEquals( "start", Boolean.FALSE, br.shouldStart() );
        assertEquals( "update", Boolean.TRUE, br.shouldUpdate() );
    }

    @Test
    public void constructorWithDefaultOptions()
        throws MalformedURLException
    {
        ScannedFileBundle br = new ScannedFileBundle( "file://afile", 10, false, true );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", Integer.valueOf( 10 ), br.getStartLevel() );
        assertEquals( "start", Boolean.FALSE, br.shouldStart() );
        assertEquals( "update", Boolean.TRUE, br.shouldUpdate() );
    }

}
