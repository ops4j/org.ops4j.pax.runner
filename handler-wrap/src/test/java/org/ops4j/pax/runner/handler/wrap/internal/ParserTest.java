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
package org.ops4j.pax.runner.handler.wrap.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.pax.runner.commons.file.FileUtils;

public class ParserTest
{

    @Test( expected = MalformedURLException.class )
    public void nullUrl()
        throws MalformedURLException
    {
        new Parser( null );
    }

    @Test( expected = MalformedURLException.class )
    public void emptyUrl()
        throws MalformedURLException
    {
        new Parser( " " );
    }

    @Test( expected = MalformedURLException.class )
    public void urlStartingWithInstructionsSeparator()
        throws MalformedURLException
    {
        new Parser( "!instructions" );
    }

    @Test( expected = MalformedURLException.class )
    public void urlEndingWithInstructionsSeparator()
        throws MalformedURLException
    {
        new Parser( "file:toWrap.jar!" );
    }

    @Test
    public void validWrappedJarURL()
        throws MalformedURLException
    {
        Parser parser = new Parser( "file:toWrap.jar" );
        assertEquals( "Wrapped Jar URL", new URL( "file:toWrap.jar" ), parser.getWrappedJarURL());
        assertNotNull( "Properties was not expected to be null", parser.getWrappedJarURL() );
    }

    @Test( expected = MalformedURLException.class )
    public void validWrappedJarURLAndInvalidInstructionsURL()
        throws MalformedURLException
    {
        new Parser( "file:toWrap.jar!wrongprotocol:toInstructions" );
    }

    @Test( expected = MalformedURLException.class )
    public void validWrappedJarURLAndInvalidInstructions01()
        throws MalformedURLException
    {
        new Parser( "file:toWrap.jar!instructions" );
    }

    @Test( expected = MalformedURLException.class )
    public void validWrappedJarURLAndInvalidInstructions02()
        throws MalformedURLException
    {
        new Parser( "file:toWrap.jar!Bundle-SymbolicName&Bundle-Name" );
    }

    @Test( expected = MalformedURLException.class )
    public void validWrappedJarURLAndInvalidInstructions03()
        throws MalformedURLException
    {
        new Parser( "file:toWrap.jar!Bundle-SymbolicName&" );
    }

    @Test( expected = MalformedURLException.class )
    public void validWrappedJarURLAndInvalidInstructions04()
        throws MalformedURLException
    {
        new Parser( "file:toWrap.jar!&Bundle-Name" );
    }

    @Test( expected = MalformedURLException.class )
    public void validWrappedJarURLAndInvalidInstructions05()
        throws MalformedURLException
    {
        new Parser( "file:toWrap.jar!Bundle-SymbolicName&Bundle-Name=v2" );
    }

    @Test( expected = MalformedURLException.class )
    public void validWrappedJarURLAndInvalidInstructions06()
        throws MalformedURLException
    {
        new Parser( "file:toWrap.jar!Bundle-SymbolicName=v1&Bundle-Name" );
    }

    @Test
    public void validWrappedJarURLAndValidInstructions()
        throws MalformedURLException
    {
        Parser parser = new Parser( "file:toWrap.jar!Bundle-SymbolicName=v1&Bundle-Name=v2" );
        assertEquals( "Wrapped Jar URL", new URL( "file:toWrap.jar" ), parser.getWrappedJarURL() );
        Properties props = parser.getWrappingProperties();
        assertNotNull( "Properties was not expected to be null", props );
        assertEquals( "Property 1", "v1", props.getProperty( "Bundle-SymbolicName" ) );
        assertEquals( "Property 2", "v2", props.getProperty( "Bundle-Name" ) );
    }

    @Test
    public void validWrappedJarURLAndValidOneInstruction()
        throws MalformedURLException
    {
        Parser parser = new Parser( "file:toWrap.jar!Bundle-SymbolicName=v1" );
        assertEquals( "Wrapped Jar URL", new URL( "file:toWrap.jar" ), parser.getWrappedJarURL() );
        Properties props = parser.getWrappingProperties();
        assertNotNull( "Properties was not expected to be null", props );
        assertEquals( "Property 1", "v1", props.getProperty( "Bundle-SymbolicName" ) );
    }

    @Test
    public void validWrappedJarURLAndValidInstructionsURL()
        throws MalformedURLException
    {
        Parser parser = new Parser(
            "file:toWrap.jar!"
            + FileUtils.getFileFromClasspath( "parser/instructions.properties" ).toURL().toExternalForm()
        );
        assertEquals( "Wrapped Jar URL", new URL( "file:toWrap.jar" ), parser.getWrappedJarURL() );
        Properties props = parser.getWrappingProperties();
        assertNotNull( "Properties was not expected to be null", props );
        assertEquals( "Property 1", "v1", props.getProperty( "Bundle-SymbolicName" ) );
        assertEquals( "Property 2", "v2", props.getProperty( "Bundle-Name" ) );
    }

}
