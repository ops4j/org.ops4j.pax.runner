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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.regex.Pattern;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.util.property.PropertyResolver;

public class DirScannerTest
{

    private Pattern m_filter;

    @Before
    public void setUp()
        throws MalformedSpecificationException
    {
        m_filter = ParserImpl.parseFilter( "*.jar" );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithNullURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new DirScanner( createMock( PropertyResolver.class ) ).scan( null );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithEmptyURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new DirScanner( createMock( PropertyResolver.class ) ).scan( " " );
    }

    @Test
    public void scanDir()
        throws ScannerException, MalformedURLException, FileNotFoundException
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "dirscanner" );

        expect( parser.getURL() ).andReturn( file.getAbsolutePath() );
        expect( parser.getFilter() ).andReturn( m_filter );
        expect( parser.getStartLevel() ).andReturn( null );
        expect( config.getStartLevel() ).andReturn( null );
        expect( parser.shouldStart() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( parser, config );
        List<BundleReference> references = createScanner( config, parser ).scan( file.getAbsolutePath() );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Number of bundles", 2, references.size() );
        verify( parser, config );
    }

    @Test
    public void scanDirFromFileURL()
        throws ScannerException, MalformedURLException, FileNotFoundException
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "dirscanner" );

        expect( parser.getURL() ).andReturn( file.toURL().toExternalForm() );
        expect( parser.getFilter() ).andReturn( m_filter );
        expect( parser.getStartLevel() ).andReturn( null );
        expect( config.getStartLevel() ).andReturn( null );
        expect( parser.shouldStart() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( parser, config );
        List<BundleReference> references = createScanner( config, parser ).scan( file.toURL().toExternalForm() );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Number of bundles", 2, references.size() );
        verify( parser, config );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void scanDirFromHttpURL()
        throws ScannerException, MalformedURLException
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );

        expect( parser.getURL() ).andReturn( "http:myserver/mydir" );
        expect( parser.getFilter() ).andReturn( m_filter );
        expect( parser.getStartLevel() ).andReturn( null );
        expect( config.getStartLevel() ).andReturn( null );
        expect( parser.shouldStart() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( parser, config );
        createScanner( config, parser ).scan( "http:myserver/myfile" );
        verify( parser, config );
    }

    @Test
    public void scanZip()
        throws ScannerException, MalformedURLException, FileNotFoundException
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "dirscanner.zip" );

        expect( parser.getURL() ).andReturn( file.getAbsolutePath() );
        expect( parser.getFilter() ).andReturn( m_filter );
        expect( parser.getStartLevel() ).andReturn( null );
        expect( config.getStartLevel() ).andReturn( null );
        expect( parser.shouldStart() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( parser, config );
        List<BundleReference> references = createScanner( config, parser ).scan( file.getAbsolutePath() );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Number of bundles", 2, references.size() );
        verify( parser, config );
    }

    @Test
    public void scanZipFromFileURL()
        throws ScannerException, MalformedURLException, FileNotFoundException
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "dirscanner.zip" );

        expect( parser.getURL() ).andReturn( file.toURL().toExternalForm() );
        expect( parser.getFilter() ).andReturn( m_filter );
        expect( parser.getStartLevel() ).andReturn( null );
        expect( config.getStartLevel() ).andReturn( null );
        expect( parser.shouldStart() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( parser, config );
        List<BundleReference> references = createScanner( config, parser ).scan( file.toURL().toExternalForm() );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Number of bundles", 2, references.size() );
        verify( parser, config );
    }

    // TODO resolve test
    //@Test
    public void scanZipFromHttpURL()
        throws ScannerException, MalformedURLException
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );

        expect( parser.getURL() ).andReturn( "http:myserver/my.zip" );
        expect( parser.getFilter() ).andReturn( m_filter );
        expect( parser.getStartLevel() ).andReturn( null );
        expect( config.getStartLevel() ).andReturn( null );
        expect( parser.shouldStart() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( parser, config );
        List<BundleReference> references = createScanner( config, parser ).scan( "http:myserver/my.zip" );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Number of bundles", 2, references.size() );
        verify( parser, config );
    }

    private DirScanner createScanner( final ScannerConfiguration config, final Parser parser )
    {
        return new DirScanner( createMock( PropertyResolver.class ) )
        {
            @Override
            ScannerConfiguration createConfiguration()
            {
                return config;
            }

            @Override
            Parser createParser( final String urlSpec )
                throws MalformedSpecificationException
            {
                return parser;
            }
        };
    }

}
