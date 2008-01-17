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
package org.ops4j.pax.runner.scanner.file.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;

public class FileScannerTest
{

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithNullURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new FileScanner( createMock( Resolver.class ) ).scan( null );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithEmptyURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new FileScanner( createMock( Resolver.class ) ).scan( " " );
    }

    @Test
    public void scanWithValidFileAndNoOptions()
        throws Exception
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "scanner/bundles.txt" );

        expect( parser.getFileURL() ).andReturn( file.toURL() );
        expect( parser.getStartLevel() ).andReturn( null );
        expect( config.getStartLevel() ).andReturn( null );
        expect( parser.shouldStart() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( parser, config );
        List<BundleReference> references = createFileScanner( config, parser ).scan( file.toURL().toExternalForm() );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Nuber of bundles", 4, references.size() );
        verify( parser, config );
    }

    @Test
    public void scanWithValidFileAndNoStart()
        throws Exception
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "scanner/bundles.txt" );

        expect( parser.getFileURL() ).andReturn( file.toURL() );
        expect( parser.getStartLevel() ).andReturn( null );
        expect( config.getStartLevel() ).andReturn( null );
        expect( parser.shouldStart() ).andReturn( false );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( parser, config );
        List<BundleReference> references =
            createFileScanner( config, parser ).scan( file.toURL().toExternalForm() + "@nostart" );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Nuber of bundles", 4, references.size() );
        assertEquals( "Should start", false, references.get( 0 ).shouldStart() );
        assertEquals( "Should start", false, references.get( 1 ).shouldStart() );
        verify( parser, config );
    }

    @Test
    public void scanWithValidFileAndStartLevel()
        throws Exception
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "scanner/bundles.txt" );

        expect( parser.getFileURL() ).andReturn( file.toURL() );
        expect( parser.getStartLevel() ).andReturn( 10 );
        expect( parser.shouldStart() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( parser, config );
        List<BundleReference> references =
            createFileScanner( config, parser ).scan( file.toURL().toExternalForm() + "@10" );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Nuber of bundles", 4, references.size() );
        assertEquals( "Start level", Integer.valueOf( 10 ), references.get( 0 ).getStartLevel() );
        assertEquals( "Start level", Integer.valueOf( 10 ), references.get( 3 ).getStartLevel() );
        verify( parser, config );
    }

    @Test( expected = ScannerException.class )
    public void scanWithInvalidFile()
        throws ScannerException, MalformedURLException
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );

        expect( parser.getFileURL() ).andReturn( new URL( "file:inexistent" ) );

        replay( parser, config );
        createFileScanner( config, parser ).scan( "file:inexistent" );
        verify( parser, config );
    }

    @Test
    public void scanWithEmptyFile()
        throws Exception
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "scanner/empty.txt" );

        expect( parser.getFileURL() ).andReturn( file.toURL() );
        expect( parser.getStartLevel() ).andReturn( 10 );
        expect( parser.shouldStart() ).andReturn( true );
        expect( parser.shouldUpdate() ).andReturn( true );

        replay( parser, config );
        List<BundleReference> references = createFileScanner( config, parser ).scan( file.toURL().toExternalForm() );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Nuber of bundles", 0, references.size() );
        verify( parser, config );
    }

    @Test
    public void scanValidFileWithProperties()
        throws Exception
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        final Recorder recorder = createMock( Recorder.class );
        File file = FileUtils.getFileFromClasspath( "scanner/properties.txt" );

        expect( parser.getFileURL() ).andReturn( file.toURL() );
        expect( parser.getStartLevel() ).andReturn( null );
        expect( config.getStartLevel() ).andReturn( null );
        expect( parser.shouldStart() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        recorder.record( "prop.1=value.1" );
        recorder.record( "prop.2=value.2" );

        replay( parser, config, recorder );
        Properties sysPropsBackup = System.getProperties();
        try
        {
            System.setProperties(
                new Properties()
                {

                    @Override
                    public synchronized Object setProperty( String key, String value )
                    {
                        recorder.record( key + "=" + value );
                        return null;
                    }

                }
            );
            List<BundleReference> references =
                createFileScanner( config, parser ).scan( file.toURL().toExternalForm() );
            assertNotNull( "Returned bundle references list is null", references );
            verify( parser, config, recorder );
        }
        finally
        {
            System.setProperties( sysPropsBackup );
        }


    }

    private FileScanner createFileScanner( final ScannerConfiguration config, final Parser parser )
    {
        return new FileScanner( createMock( Resolver.class ) )
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
