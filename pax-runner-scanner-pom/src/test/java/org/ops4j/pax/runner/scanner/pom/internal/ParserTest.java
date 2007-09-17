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
package org.ops4j.pax.runner.scanner.pom.internal;

import java.net.MalformedURLException;
import java.net.URL;
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
        new ParserImpl( "file:/pom.xml@unknown" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void urlWithStartLevelAndUnknownOption()
        throws MalformedSpecificationException
    {
        new ParserImpl( "file:/pom.xml@5@unknown" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void urlWithStartLevelAndStartAndUnknownOption()
        throws MalformedSpecificationException
    {
        new ParserImpl( "file:/pom.xml@5@start@unknown" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void urlWithDuplicateStartLevel()
        throws MalformedSpecificationException
    {
        new ParserImpl( "file:/pom.xml@5@start@4" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void urlWithDuplicateStart()
        throws MalformedSpecificationException
    {
        new ParserImpl( "file:/pom.xml@start@5@start" );
    }

    @Test
    public void validUrl01()
        throws MalformedSpecificationException, MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/pom.xml" );
        assertEquals( "URL", new URL( "file:/pom.xml" ), parser.getPomURL() );
    }

    @Test
    public void validUrl02()
        throws MalformedSpecificationException, MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/pom.xml@5" );
        assertEquals( "URL", new URL( "file:/pom.xml" ), parser.getPomURL() );
        assertEquals( "Start level", 5, parser.getStartLevel() );
    }

    @Test
    public void validUrl03()
        throws MalformedSpecificationException, MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/pom.xml@NostART" );
        assertEquals( "URL", new URL( "file:/pom.xml" ), parser.getPomURL() );
        assertEquals( "Start", false, parser.shouldStart() );
    }

    @Test
    public void validUrl04()
        throws MalformedSpecificationException, MalformedURLException
    {
        Parser parser = new ParserImpl( "file:/pom.xml@5@nostart" );
        assertEquals( "URL", new URL( "file:/pom.xml" ), parser.getPomURL() );
        assertEquals( "Start level", 5, parser.getStartLevel() );
        assertEquals( "Start", false, parser.shouldStart() );
    }

}
