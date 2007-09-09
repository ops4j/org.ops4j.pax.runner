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
import org.ops4j.pax.runner.provision.scanner.FileBundleReference;

public class FileBundleReferenceTest
{

    @Test( expected = MalformedURLException.class )
    public void constructorWithNullReference()
        throws MalformedURLException
    {
        new FileBundleReference( null );
    }

    @Test( expected = MalformedURLException.class )
    public void constructorWithEmptyReference()
        throws MalformedURLException
    {
        new FileBundleReference( "" );
    }

    @Test( expected = MalformedURLException.class )
    public void constructorWithOnlySpacesReference()
        throws MalformedURLException
    {
        new FileBundleReference( "  " );
    }

    @Test
    public void constructorWithURL()
        throws MalformedURLException
    {
        FileBundleReference br = new FileBundleReference( "file://afile" );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", null, br.getStartLevel() );
        assertEquals( "start", Boolean.TRUE, br.shouldStart() );
    }

    @Test( expected = MalformedURLException.class )
    public void constructorWithoutURL()
        throws MalformedURLException
    {
        new FileBundleReference( "@5:nostart" );
    }

    @Test( expected = MalformedURLException.class )
    public void constructorEndingWithSeparator()
        throws MalformedURLException
    {
        new FileBundleReference( "file://afile@5@nostart@" );
    }

    @Test( expected = MalformedURLException.class )
    public void constructorWithInvalidOption()
        throws MalformedURLException
    {
        new FileBundleReference( "file://afile@5@nostart@invalid" );
    }

    @Test
    public void constructorWithURLAndNoStart()
        throws MalformedURLException
    {
        FileBundleReference br = new FileBundleReference( "file://afile@nostart" );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", null, br.getStartLevel() );
        assertEquals( "nostart", Boolean.FALSE, br.shouldStart() );
    }

    @Test
    public void constructorWithURLAndStartLevel()
        throws MalformedURLException
    {
        FileBundleReference br = new FileBundleReference( "file://afile@5" );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", 5, br.getStartLevel() );
        assertEquals( "start", Boolean.TRUE, br.shouldStart() );
    }

    @Test
    public void constructorWithURLAndStartLevelAndNStart()
        throws MalformedURLException
    {
        FileBundleReference br = new FileBundleReference( "file://afile@5@nostart" );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", 5, br.getStartLevel() );
        assertEquals( "start", Boolean.FALSE, br.shouldStart() );
    }

    @Test
    public void constructorWithDefaultOptions()
        throws MalformedURLException
    {
        FileBundleReference br = new FileBundleReference( "file://afile", 10, false );
        assertEquals( "url", "file://afile", br.getLocation() );
        assertEquals( "start level", 10, br.getStartLevel() );
        assertEquals( "start", Boolean.FALSE, br.shouldStart() );
    }

}
