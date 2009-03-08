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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.FileBundleReference;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.util.property.PropertyResolver;

public class PomScannerTest
{

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithNullURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new PomScanner( createMock( PropertyResolver.class ) ).scan( null );
    }

    @Test( expected = MalformedSpecificationException.class )
    public void scanWithEmptyURLSpec()
        throws ScannerException, MalformedSpecificationException
    {
        new PomScanner( createMock( PropertyResolver.class ) ).scan( " " );
    }

    @Test( expected = ScannerException.class )
    public void scanWithInvalidFile()
        throws ScannerException, MalformedURLException
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );

        expect( parser.getPomURL() ).andReturn( new URL( "file:inexistent" ) );
        expect( config.getCertificateCheck() ).andReturn( false );

        replay( parser, config );
        createPomScanner( config, parser ).scan( "file:inexistent" );
        verify( parser, config );
    }

    public void scan( BundleReference[] expected, Integer startLevel, Boolean shouldStart, Boolean update,
                      String pomFile )
        throws Exception
    {
        Parser parser = createMock( Parser.class );
        ScannerConfiguration config = createMock( ScannerConfiguration.class );
        File file = FileUtils.getFileFromClasspath( pomFile );

        expect( parser.getPomURL() ).andReturn( file.toURL() );
        expect( parser.getStartLevel() ).andReturn( startLevel );
        if( startLevel == null )
        {
            expect( config.getStartLevel() ).andReturn( null );
        }
        expect( parser.shouldStart() ).andReturn( shouldStart );
        if( shouldStart == null )
        {
            expect( config.shouldStart() ).andReturn( null );
        }
        expect( parser.shouldUpdate() ).andReturn( update );
        if( update == null )
        {
            expect( config.shouldUpdate() ).andReturn( null );
        }
        expect( config.getCertificateCheck() ).andReturn( false );

        replay( parser, config );
        List<BundleReference> references = createPomScanner( config, parser ).scan( file.toURL().toExternalForm() );
        assertNotNull( "Returned bundle references list is null", references );
        assertArrayEquals( "Bundles", expected, references.toArray() );
        verify( parser, config );
    }

    @Test
    public void scanWithValidPomAndNoOptions()
        throws Exception
    {
        BundleReference[] expected = new BundleReference[]
            {
                new FileBundleReference( "mvn:org.ops4j.pax.runner/main-artifact/0.1.0-SNAPSHOT", null, null, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/first-dependency/0.1.0/jar", null, null, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/second-dependency", null, null, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/third-dependency/0.3", null, null, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/forth-dependency/0.3", null, null, null )
            };
        scan( expected, null, null, null, "scanner/pom.xml" );
    }

    @Test
    public void scanWithValidPomAndStartLevel()
        throws Exception
    {
        BundleReference[] expected = new BundleReference[]
            {
                new FileBundleReference( "mvn:org.ops4j.pax.runner/main-artifact/0.1.0-SNAPSHOT", 5, null, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/first-dependency/0.1.0/jar", 5, null, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/second-dependency", 5, null, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/third-dependency/0.3", 5, null, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/forth-dependency/0.3", 5, null, null )
            };
        scan( expected, 5, null, null, "scanner/pom.xml" );
    }

    @Test
    public void scanWithValidPomAndNoStart()
        throws Exception
    {
        BundleReference[] expected = new BundleReference[]
            {
                new FileBundleReference( "mvn:org.ops4j.pax.runner/main-artifact/0.1.0-SNAPSHOT", null, false, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/first-dependency/0.1.0/jar", null, false, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/second-dependency", null, false, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/third-dependency/0.3", null, false, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/forth-dependency/0.3", null, false, null )
            };
        scan( expected, null, false, null, "scanner/pom.xml" );
    }

    @Test
    public void scanWithValidPomAndUpdate()
        throws Exception
    {
        BundleReference[] expected = new BundleReference[]
            {
                new FileBundleReference( "mvn:org.ops4j.pax.runner/main-artifact/0.1.0-SNAPSHOT", null, null, true ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/first-dependency/0.1.0/jar", null, null, true ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/second-dependency", null, null, true ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/third-dependency/0.3", null, null, true ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/forth-dependency/0.3", null, null, true )
            };
        scan( expected, null, null, true, "scanner/pom.xml" );
    }

    @Test
    public void scanWithValidPomAndStartLevelAndNoStart()
        throws Exception
    {
        BundleReference[] expected = new BundleReference[]
            {
                new FileBundleReference( "mvn:org.ops4j.pax.runner/main-artifact/0.1.0-SNAPSHOT", 5, false, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/first-dependency/0.1.0/jar", 5, false, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/second-dependency", 5, false, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/third-dependency/0.3", 5, false, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/forth-dependency/0.3", 5, false, null )
            };
        scan( expected, 5, false, null, "scanner/pom.xml" );
    }

    @Test
    public void scanWithValidPomWithoutDependencies()
        throws Exception
    {
        BundleReference[] expected = new BundleReference[]
            {
                new FileBundleReference( "mvn:org.ops4j.pax.runner/main-artifact/0.1.0-SNAPSHOT", null, null, null ),
            };
        scan( expected, null, null, null, "scanner/pomWithoutDependencies.xml" );
    }

    @Test
    public void scanWithValidPomAndProperties()
        throws Exception
    {
        final Recorder recorder = createMock( Recorder.class );
        recorder.record( "prop.1=value.1" );
        recorder.record( "prop.2=value.2" );

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
            BundleReference[] expected = new BundleReference[]
                {
                    new FileBundleReference( "mvn:org.ops4j.pax.runner/main-artifact/0.1.0-SNAPSHOT", null, null, null
                    ),
                };

            replay( recorder );
            scan( expected, null, null, null, "scanner/pomWithProperties.xml" );
            verify( recorder );
        }
        finally
        {
            System.setProperties( sysPropsBackup );
        }
    }

    @Test
    public void scanWithValidPomWithPropertiesInDependency()
        throws Exception
    {
        BundleReference[] expected = new BundleReference[]
            {
                new FileBundleReference( "mvn:org.ops4j.pax.runner/main-artifact/0.1.0-SNAPSHOT", null, null, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/first-dependency/0.1.0/jar", null, null, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/second-dependency/0.1.0.5", null, null, null ),
                new FileBundleReference( "mvn:org.ops4j.pax.runner/third-dependency/0.3", null, null, null )
            };
        scan( expected, null, null, null, "scanner/pomWithPropertiesInDependency.xml" );
    }

    private PomScanner createPomScanner( final ScannerConfiguration config, final Parser parser )
    {
        return new PomScanner( createMock( PropertyResolver.class ) )
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
