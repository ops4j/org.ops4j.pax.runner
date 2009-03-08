/*
 * Copyright 2009 Alin Dreghiciu.
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
package org.ops4j.pax.runner.provision;

import java.net.MalformedURLException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * {@link ProvisionSpec} unit tests.
 *
 * @author Alin Dreghiciu
 * @since 0.18.0, March 07, 2007
 */
public class ProvisionSpecTest
{

    @Test( expected = MalformedSpecificationException.class )
    public void constructorWithNullSpec()
        throws MalformedSpecificationException
    {
        new ProvisionSpec( null );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void emptySpec()
        throws MalformedSpecificationException
    {
        new ProvisionSpec( "" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void pathStartingWithOptionSeparator()
        throws MalformedSpecificationException
    {
        new ProvisionSpec( "scanner:@option" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void pathEndingWithOptionSeparator()
        throws MalformedSpecificationException
    {
        new ProvisionSpec( "scanner:file:@" );
    }

    @Test
    public void validUrl()
        throws Exception
    {
        ProvisionSpec spec = new ProvisionSpec( "scan:file:/pom.xml" );
        assertEquals( "URL", "file:/pom.xml", spec.getPath() );
    }

    @Test
    public void validUrlAndStartLevel()
        throws Exception
    {
        ProvisionSpec spec = new ProvisionSpec( "scan:file:/pom.xml@5" );
        assertEquals( "URL", "file:/pom.xml", spec.getPath() );
        assertEquals( "Start level", Integer.valueOf( 5 ), spec.getStartLevel() );
    }

    @Test
    public void validUrlAndStart()
        throws Exception
    {
        ProvisionSpec spec = new ProvisionSpec( "scan:file:/pom.xml@NostART" );
        assertEquals( "URL", "file:/pom.xml", spec.getPath() );
        assertEquals( "Start", false, spec.shouldStart() );
    }

    @Test
    public void validUrlAndStartLevelAndStart()
        throws Exception
    {
        ProvisionSpec spec = new ProvisionSpec( "scan:file:/pom.xml@5@nostart" );
        assertEquals( "URL", "file:/pom.xml", spec.getPath() );
        assertEquals( "Start level", Integer.valueOf( 5 ), spec.getStartLevel() );
        assertEquals( "Start", false, spec.shouldStart() );
    }

    @Test
    public void filter01()
        throws MalformedURLException
    {
        ProvisionSpec parser = new ProvisionSpec( "scan:file:/dir!" );
        assertEquals( "URL", "file:/dir", parser.getPath() );
        assertEquals( "Filter", "[^/]*", parser.getFilter().pattern() );
    }

    @Test
    public void filter02()
        throws MalformedURLException
    {
        ProvisionSpec parser = new ProvisionSpec( "scan:file:/dir!/" );
        assertEquals( "URL", "file:/dir", parser.getPath() );
        assertEquals( "Filter", "[^/]*", parser.getFilter().pattern() );
    }

    @Test
    public void filter03()
        throws MalformedURLException
    {
        ProvisionSpec parser = new ProvisionSpec( "scan:file:/dir!/aFilter" );
        assertEquals( "URL", "file:/dir", parser.getPath() );
        assertNotNull( "Filter was not supposed to be null", parser.getFilter() );
        assertEquals( "Filter", "aFilter", parser.getFilter().pattern() );
    }

    @Test
    public void filter04()
        throws MalformedURLException
    {
        ProvisionSpec parser = new ProvisionSpec( "scan:file:/dir!/**" );
        assertEquals( "URL", "file:/dir", parser.getPath() );
        assertNotNull( "Filter was not supposed to be null", parser.getFilter() );
        assertEquals( "Filter", ".*", parser.getFilter().pattern() );
    }

    @Test
    public void filter05()
        throws MalformedURLException
    {
        ProvisionSpec parser = new ProvisionSpec( "scan:file:/dir!/**/" );
        assertEquals( "URL", "file:/dir", parser.getPath() );
        assertNotNull( "Filter was not supposed to be null", parser.getFilter() );
        assertEquals( "Filter", ".*/", parser.getFilter().pattern() );
    }

    @Test
    public void filter06()
        throws MalformedURLException
    {
        ProvisionSpec parser = new ProvisionSpec( "scan:file:/dir!/**/aFilter" );
        assertEquals( "URL", "file:/dir", parser.getPath() );
        assertNotNull( "Filter was not supposed to be null", parser.getFilter() );
        assertEquals( "Filter", ".*/aFilter", parser.getFilter().pattern() );
    }

    @Test
    public void filter07()
        throws MalformedURLException
    {
        ProvisionSpec parser = new ProvisionSpec( "scan:file:/dir!/*" );
        assertEquals( "URL", "file:/dir", parser.getPath() );
        assertEquals( "Filter", "[^/]*", parser.getFilter().pattern() );
    }

    @Test
    public void filter08()
        throws MalformedURLException
    {
        ProvisionSpec parser = new ProvisionSpec( "scan:file:/dir" );
        assertEquals( "URL", "file:/dir", parser.getPath() );
        assertEquals( "Filter", "[^/]*", parser.getFilter().pattern() );
    }

}