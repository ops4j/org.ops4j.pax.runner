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
package org.ops4j.pax.runner.scanner.composite.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ProvisionService;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.util.property.PropertyResolver;

/**
 * {@link CompositeScanner} unit tests.
 *
 * @author Alin Dreghiciu
 * @since 0.18.0, March 07, 2007
 */
public class CompositeScannerTest
{

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithNullURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new CompositeScanner(
            createMock( PropertyResolver.class ),
            createMock( ProvisionService.class )
        ).scan( null );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithEmptyURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new CompositeScanner(
            createMock( PropertyResolver.class ),
            createMock( ProvisionService.class )
        ).scan( " " );
    }

    @Test
    public void scan()
        throws Exception
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        ProvisionService provisionService = createMock( ProvisionService.class );
        File file = FileUtils.getFileFromClasspath( "scanner/bundles.txt" );

        expect( parser.getFileURL() ).andReturn( file.toURL() ).anyTimes();
        expect( parser.getStartLevel() ).andReturn( null );
        expect( config.getStartLevel() ).andReturn( null );
        expect( parser.shouldStart() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );
        expect( config.getCertificateCheck() ).andReturn( false );
        List<BundleReference> refs = new ArrayList<BundleReference>();
        refs.add( createMock( BundleReference.class ) );
        expect( provisionService.scan( "scan-bundle:file:bundle1.txt" ) ).andReturn( refs );
        expect( provisionService.scan( "scan-file:file:foo.bundles@5" ) ).andReturn( refs );
        expect( provisionService.scan( "scan-dir:file:foo@5@nostart" ) ).andReturn( refs );
        expect( provisionService.scan( "scan-pom:mvn:group/artifact@nostart" ) ).andReturn( refs );

        replay( parser, config, provisionService );
        List<BundleReference> references = createScanner( config, parser, provisionService )
            .scan( file.toURL().toExternalForm() );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Nuber of bundles", 4, references.size() );
        verify( parser, config, provisionService );
    }

    @Test( expected = ScannerException.class )
    public void scanWithInvalidFile()
        throws ScannerException, MalformedURLException
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        ProvisionService provisionService = createMock( ProvisionService.class );

        expect( parser.getFileURL() ).andReturn( new URL( "file:inexistent" ) );
        expect( config.getCertificateCheck() ).andReturn( false );

        replay( parser, config, provisionService );
        createScanner( config, parser, provisionService ).scan( "file:inexistent" );
        verify( parser, config, provisionService );
    }

    @Test
    public void scanWithEmptyFile()
        throws Exception
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        ProvisionService provisionService = createMock( ProvisionService.class );
        File file = FileUtils.getFileFromClasspath( "scanner/empty.txt" );

        expect( parser.getFileURL() ).andReturn( file.toURL() ).anyTimes();
        expect( parser.getStartLevel() ).andReturn( 10 );
        expect( parser.shouldStart() ).andReturn( true );
        expect( parser.shouldUpdate() ).andReturn( true );
        expect( config.getCertificateCheck() ).andReturn( false );

        replay( parser, config, provisionService );
        List<BundleReference> references = createScanner( config, parser, provisionService )
            .scan( file.toURL().toExternalForm() );
        assertNotNull( "Returned bundle references list is null", references );
        assertEquals( "Nuber of bundles", 0, references.size() );
        verify( parser, config, provisionService );
    }

    @Test
    public void scanValidFileWithProperties()
        throws Exception
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        ProvisionService provisionService = createMock( ProvisionService.class );
        final Recorder recorder = createMock( Recorder.class );
        File file = FileUtils.getFileFromClasspath( "scanner/properties.txt" );

        expect( parser.getFileURL() ).andReturn( file.toURL() ).anyTimes();
        expect( parser.getStartLevel() ).andReturn( null );
        expect( config.getStartLevel() ).andReturn( null );
        expect( parser.shouldStart() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( parser.shouldUpdate() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );
        expect( config.getCertificateCheck() ).andReturn( false );

        recorder.record( "prop.1=value.1" );
        recorder.record( "prop.2=value.2" );

        replay( parser, config, recorder, provisionService );
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
            List<BundleReference> references = createScanner( config, parser, provisionService )
                .scan( file.toURL().toExternalForm() );
            assertNotNull( "Returned bundle references list is null", references );
            verify( parser, config, recorder, provisionService );
        }
        finally
        {
            System.setProperties( sysPropsBackup );
        }


    }

    private CompositeScanner createScanner( final ScannerConfiguration config,
                                            final Parser parser,
                                            final ProvisionService provisionService )
    {
        return new CompositeScanner(
            createMock( PropertyResolver.class ),
            provisionService
        )
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