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
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ProvisionSpec;
import org.ops4j.pax.runner.provision.ScannedBundle;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.util.property.PropertyResolver;

public class DirScannerTest
{

    @Test( expected = NullArgumentException.class )
    public void scanWithNullURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new DirScanner( createMock( PropertyResolver.class ) ).scan( null );
    }

    @Test
    public void scanDir()
        throws ScannerException, MalformedURLException, FileNotFoundException
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "dirscanner" );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( config );
        List<ScannedBundle> scannedBundles = createScanner( config ).scan(
            new ProvisionSpec( "scan-dir:" + file.getAbsolutePath() + "!/*.jar" )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Number of bundles", 2, scannedBundles.size() );
        verify( config );
    }

    @Test
    public void scanDirFromFileURL()
        throws ScannerException, MalformedURLException, FileNotFoundException
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "dirscanner" );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( config );
        List<ScannedBundle> scannedBundles = createScanner( config ).scan(
            new ProvisionSpec( "scan-dir:" + file.toURL().toExternalForm() + "!/*.jar" )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Number of bundles", 2, scannedBundles.size() );
        verify( config );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void scanDirFromHttpURL()
        throws ScannerException, MalformedURLException
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( config );
        createScanner( config ).scan(
            new ProvisionSpec( "scan-dir:http:myserver/myfile!/*.jar" )
        );
        verify( config );
    }

    @Test
    public void scanZip()
        throws ScannerException, MalformedURLException, FileNotFoundException
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "dirscanner.zip" );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( config );
        List<ScannedBundle> scannedBundles = createScanner( config ).scan(
            new ProvisionSpec( "scan-dir:" + file.getAbsolutePath() + "!/*.jar" )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Number of bundles", 2, scannedBundles.size() );
        verify( config );
    }

    @Test
    public void scanZipFromFileURL()
        throws ScannerException, MalformedURLException, FileNotFoundException
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( "dirscanner.zip" );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( config );
        List<ScannedBundle> scannedBundles = createScanner( config ).scan(
            new ProvisionSpec( "scan-dir:" + file.toURL().toExternalForm() + "!/*.jar" )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Number of bundles", 2, scannedBundles.size() );
        verify( config );
    }

    @Test
    @Ignore
    public void scanZipFromHttpURL()
        throws ScannerException, MalformedURLException
    {
        ScannerConfiguration config = createMock( ScannerConfiguration.class );

        expect( config.getStartLevel() ).andReturn( null );
        expect( config.shouldStart() ).andReturn( null );
        expect( config.shouldUpdate() ).andReturn( null );

        replay( config );
        List<ScannedBundle> scannedBundles = createScanner( config ).scan(
            new ProvisionSpec( "scan-dir:http:myserver/my.zip!/*.jar" )
        );
        assertNotNull( "Returned list is null", scannedBundles );
        assertEquals( "Number of bundles", 2, scannedBundles.size() );
        verify( config );
    }

    private DirScanner createScanner( final ScannerConfiguration config )
    {
        return new DirScanner( createMock( PropertyResolver.class ) )
        {
            @Override
            ScannerConfiguration createConfiguration()
            {
                return config;
            }
        };
    }

}
