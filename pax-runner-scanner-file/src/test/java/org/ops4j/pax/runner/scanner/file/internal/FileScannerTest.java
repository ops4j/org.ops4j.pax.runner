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
import java.util.List;
import java.util.Properties;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ProvisionSpec;
import org.ops4j.pax.runner.provision.ScannedBundle;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.util.property.PropertyResolver;

public class FileScannerTest
{

    @Test( expected = NullArgumentException.class )
    public void scanWithNullURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new FileScanner( createMock( PropertyResolver.class ) ).scan( null );
    }

    @Test
    public void scanWithValidFileAndNoOptions()
        throws Exception
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "scanner/bundles.txt" );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );
        expect( config.getCertificateCheck() ).andReturn( false );

        replay( config );
        List<ScannedBundle> scannedBundles = createFileScanner( config ).scan(
            new ProvisionSpec( "scan-file:" + file.toURL().toExternalForm() )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Number of bundles", 4, scannedBundles.size() );
        verify( config );
    }

    @Test
    public void scanWithValidFileAndNoStart()
        throws Exception
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "scanner/bundles.txt" );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );
        expect( config.getCertificateCheck() ).andReturn( false );

        replay( config );
        List<ScannedBundle> scannedBundles = createFileScanner( config ).scan(
            new ProvisionSpec( "scan-file:" + file.toURL().toExternalForm() + "@nostart" )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Nuber of bundles", 4, scannedBundles.size() );
        assertEquals( "Should start", false, scannedBundles.get( 0 ).shouldStart() );
        assertEquals( "Should start", false, scannedBundles.get( 1 ).shouldStart() );
        verify( config );
    }

    @Test
    public void scanWithValidFileAndStartLevel()
        throws Exception
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "scanner/bundles.txt" );

        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );
        expect( config.getCertificateCheck() ).andReturn( false );

        replay( config );
        List<ScannedBundle> scannedBundles = createFileScanner( config ).scan(
            new ProvisionSpec( "scan-file:" + file.toURL().toExternalForm() + "@10" )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Nuber of bundles", 4, scannedBundles.size() );
        assertEquals( "Start level", Integer.valueOf( 10 ), scannedBundles.get( 0 ).getStartLevel() );
        assertEquals( "Start level", Integer.valueOf( 10 ), scannedBundles.get( 3 ).getStartLevel() );
        verify( config );
    }

    @Test( expected = ScannerException.class )
    public void scanWithInvalidFile()
        throws ScannerException, MalformedURLException
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );

        expect( config.getCertificateCheck() ).andReturn( false );

        replay( config );
        createFileScanner( config ).scan(
            new ProvisionSpec( "scan-file:file:inexistent" )
        );
        verify( config );
    }

    @Test
    public void scanWithEmptyFile()
        throws Exception
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "scanner/empty.txt" );

        expect( config.shouldStart() ).andReturn( true );
        expect( config.getCertificateCheck() ).andReturn( false );

        replay( config );
        List<ScannedBundle> scannedBundles = createFileScanner( config ).scan(
            new ProvisionSpec( "scan-file:" + file.toURL().toExternalForm() + "@10@update" )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Nuber of bundles", 0, scannedBundles.size() );
        verify( config );
    }

    @Test
    public void scanValidFileWithProperties()
        throws Exception
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        final Recorder recorder = createMock( Recorder.class );
        File file = FileUtils.getFileFromClasspath( "scanner/properties.txt" );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );
        expect( config.getCertificateCheck() ).andReturn( false );

        recorder.record( "prop.1=value.1" );
        recorder.record( "prop.2=value.2" );

        replay( config, recorder );
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
            List<ScannedBundle> scannedBundles = createFileScanner( config ).scan(
                new ProvisionSpec( "scan-file:" + file.toURL().toExternalForm() )
            );
            assertNotNull( "Returned list is null", scannedBundles );
            verify( config, recorder );
        }
        finally
        {
            System.setProperties( sysPropsBackup );
        }


    }

    private FileScanner createFileScanner( final ScannerConfiguration config )
    {
        return new FileScanner( createMock( PropertyResolver.class ) )
        {
            @Override
            ScannerConfiguration createConfiguration()
            {
                return config;
            }
        };
    }

}
