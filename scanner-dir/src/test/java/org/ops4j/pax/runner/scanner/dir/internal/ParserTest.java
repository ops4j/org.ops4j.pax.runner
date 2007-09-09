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
package org.ops4j.pax.runner.scanner.dir.internal;

import java.net.MalformedURLException;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;

public class ParserTest
{

    @Test( expected = MalformedSpecificationException.class )
    public void constructorWithNullPath()
        throws MalformedSpecificationException
    {
        new ParserImpl( null );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void emptyUrl()
        throws MalformedSpecificationException
    {
        new ParserImpl( "" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void urlStartingWithOptionSeparator()
        throws MalformedSpecificationException
    {
        new ParserImpl( "@option" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void urlEndingWithOptionSeparator()
        throws MalformedSpecificationException
    {
        new ParserImpl( "file@" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void urlWithOnlyUnknownOption()
        throws MalformedSpecificationException
    {
        new ParserImpl( "file:/dir@unknown" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void urlWithStartLevelAndUnknownOption()
        throws MalformedSpecificationException
    {
        new ParserImpl( "file:/dir@5@unknown" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void urlWithStartLevelAndStartAndUnknownOption()
        throws MalformedSpecificationException
    {
        new ParserImpl( "file:/dir@5@start@unknown" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void urlWithDuplicateStartLevel()
        throws MalformedSpecificationException
    {
        new ParserImpl( "file:/dir@5@start@4" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void urlWithDuplicateStart()
        throws MalformedSpecificationException
    {
        new ParserImpl( "file:/dir@start@5@start" );
    }

    @Test
    public void validUrl()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
    }

    @Test
    public void validUrlAndStartLevel()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir@5" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
        assertEquals( "Start level", 5, parser.getStartLevel() );
    }

    @Test
    public void validUrlAndStart()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir@NostART" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
        assertEquals( "Start", false, parser.shouldStart() );
    }

    @Test
    public void validUrlAndStartLevelAndStart()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir@5@nostart" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
        assertEquals( "Start level", 5, parser.getStartLevel() );
        assertEquals( "Start", false, parser.shouldStart() );
    }

    @Test
    public void filter01()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir!" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
        assertEquals( "Filter", "[^/]*", parser.getFilter().pattern() );
    }

    @Test
    public void filter02()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir!/" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
        assertEquals( "Filter", "[^/]*", parser.getFilter().pattern() );
    }

    @Test
    public void filter03()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir!/aFilter" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
        assertNotNull( "Filter was not supposed to be null", parser.getFilter() );
        assertEquals( "Filter", "aFilter", parser.getFilter().pattern() );
    }

    @Test
    public void filter04()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir!/**" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
        assertNotNull( "Filter was not supposed to be null", parser.getFilter() );
        assertEquals( "Filter", ".*", parser.getFilter().pattern() );
    }

    @Test
    public void filter05()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir!/**/" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
        assertNotNull( "Filter was not supposed to be null", parser.getFilter() );
        assertEquals( "Filter", ".*/", parser.getFilter().pattern() );
    }

    @Test
    public void filter06()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir!/**/aFilter" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
        assertNotNull( "Filter was not supposed to be null", parser.getFilter() );
        assertEquals( "Filter", ".*/aFilter", parser.getFilter().pattern() );
    }

    @Test
    public void filter07()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir!/*" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
        assertEquals( "Filter", "[^/]*", parser.getFilter().pattern() );
    }

    @Test
    public void filter08()
        throws MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/dir" );
        assertEquals( "URL", "file:/dir", parser.getURL() );
        assertEquals( "Filter", "[^/]*", parser.getFilter().pattern() );
    }

}
