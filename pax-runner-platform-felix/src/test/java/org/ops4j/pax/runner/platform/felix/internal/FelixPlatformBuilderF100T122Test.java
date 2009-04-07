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
package org.ops4j.pax.runner.platform.felix.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import static org.easymock.EasyMock.*;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.PlatformContext;
import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.pax.runner.platform.internal.PlatformContextImpl;

public class FelixPlatformBuilderF100T122Test
{

    private File m_workDir;
    private BundleContext m_bundleContext;
    private Configuration m_configuration;
    private PlatformContext m_platformContext;

    @Before
    public void setUp()
        throws IOException
    {
        m_bundleContext = createMock( BundleContext.class );
        m_configuration = createMock( Configuration.class );
        m_workDir = File.createTempFile( "runner", "" );
        m_workDir.delete();
        m_workDir = new File( m_workDir.getAbsolutePath() );
        m_workDir.mkdirs();
        m_workDir.deleteOnExit();
        m_platformContext = new PlatformContextImpl();
        m_platformContext.setConfiguration( m_configuration );
        m_platformContext.setWorkingDirectory( m_workDir );
    }

    @After
    public void tearDown()
    {
        FileUtils.delete( m_workDir );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullBundleContext()
    {
        new FelixPlatformBuilderF100T122( null, "version" );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullVersion()
    {
        new FelixPlatformBuilderF100T122( m_bundleContext, null );
    }

    @Test
    public void mainClassName()
    {
        replay( m_bundleContext );
        assertEquals(
            "Main class name",
            "org.apache.felix.main.Main",
            new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).getMainClassName()
        );
        verify( m_bundleContext );
    }

    @Test
    public void getRequiredProfilesWithoutConsole()
    {
        expect( m_configuration.startConsole() ).andReturn( null );

        replay( m_bundleContext, m_configuration );
        assertNull(
            "Required profiles is not null",
            new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).getRequiredProfile( m_platformContext )
        );
        verify( m_bundleContext, m_configuration );
    }

    @Test
    public void getRequiredProfilesWithConsole()
    {
        expect( m_configuration.startConsole() ).andReturn( true );

        replay( m_bundleContext, m_configuration );
        assertEquals(
            "Required profiles",
            "tui",
            new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).getRequiredProfile( m_platformContext )
        );
        verify( m_bundleContext, m_configuration );
    }

    @Test
    public void getArguments()
    {
        assertNull( "Arguments is not not null",
                    new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).getArguments( m_platformContext )
        );
    }

    @Test
    public void getVMOptions()
    {
        replay( m_bundleContext );
        assertArrayEquals(
            "System options",
            new String[]{
                "-Dfelix.config.properties="
                + m_platformContext.normalizeAsUrl( new File( m_workDir, "/felix/config.ini" ) ),
                "-Dfelix.cache.dir="
                + m_platformContext.normalizeAsPath( new File( m_workDir, "felix/cache" ) )
            },
            new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).getVMOptions( m_platformContext )
        );
        verify( m_bundleContext );
    }

    @Test( expected = IllegalArgumentException.class )
    public void getVMOptionsWithNullPlatformContext()
    {
        replay( m_bundleContext );
        new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).getVMOptions( null );
        verify( m_bundleContext );
    }

    @Test( expected = IllegalArgumentException.class )
    public void prepareWithNullPlatformContext()
        throws PlatformException
    {
        replay( m_bundleContext );
        new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).prepare( null );
        verify( m_bundleContext );
    }

    // tests that the platform configuration ini file is correct with no bundles.
    // also tests that the start level is not set if is not configured
    // also tests that the default start level is not set if is not configured
    @Test
    public void prepareWithoutBundles()
        throws PlatformException, IOException
    {

        m_platformContext.setBundles( null );
        m_platformContext.setExecutionEnvironment( "EE-1,EE-2" );
        m_platformContext.setSystemPackages( "sys.package.one,sys.package.two" );
        Properties properties = new Properties();
        properties.setProperty( "myProperty", "myValue" );
        m_platformContext.setProperties( properties );

        expect( m_configuration.getBootDelegation() ).andReturn( "javax.*" );
        expect( m_configuration.getStartLevel() ).andReturn( null );
        expect( m_configuration.getBundleStartLevel() ).andReturn( null );
        expect( m_configuration.getFrameworkProfile() ).andReturn( null );

        replay( m_bundleContext, m_configuration );
        new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).prepare( m_platformContext );
        verify( m_bundleContext, m_configuration );

        compareFiles(
            FileUtils.getFileFromClasspath( "felixplatformbuilder/configWithNoBundles.ini" ),
            new File( m_workDir + "/felix/config.ini" ),
            true,
            null
        );
    }

    // tests that the platform configuration ini file is correct with bundles to be installed.
    // also tests that the start level is set correctly if configured
    // also tests that the default start level is set correctly if configured
    @Test
    public void prepare()
        throws PlatformException, IOException
    {

        List<BundleReference> bundles = new ArrayList<BundleReference>();

        // a bunlde with start level that should start
        BundleReference bundle1 = createMock( BundleReference.class );
        bundles.add( bundle1 );
        expect( bundle1.getURL() ).andReturn( new File( m_workDir, "bundles/bundle1.jar" ).toURL() );
        expect( bundle1.getStartLevel() ).andReturn( 10 );
        expect( bundle1.shouldStart() ).andReturn( true );

        // a bundle with only start level that should not start
        BundleReference bundle2 = createMock( BundleReference.class );
        bundles.add( bundle2 );
        expect( bundle2.getURL() ).andReturn( new File( m_workDir, "bundles/bundle2.jar" ).toURL() );
        expect( bundle2.getStartLevel() ).andReturn( 10 );
        expect( bundle2.shouldStart() ).andReturn( null );

        // a bunlde without start level that should start
        BundleReference bundle3 = createMock( BundleReference.class );
        bundles.add( bundle3 );
        expect( bundle3.getURL() ).andReturn( new File( m_workDir, "bundles/bundle3.jar" ).toURL() );
        expect( bundle3.getStartLevel() ).andReturn( null );
        expect( bundle3.shouldStart() ).andReturn( true );

        // a bundle without start level that should not start
        BundleReference bundle4 = createMock( BundleReference.class );
        bundles.add( bundle4 );
        expect( bundle4.getURL() ).andReturn( new File( m_workDir, "bundles/bundle4.jar" ).toURL() );
        expect( bundle4.getStartLevel() ).andReturn( null );
        expect( bundle4.shouldStart() ).andReturn( null );

        m_platformContext.setBundles( bundles );
        m_platformContext.setExecutionEnvironment( "EE-1,EE-2" );
        m_platformContext.setSystemPackages( "sys.package.one,sys.package.two" );
        Properties properties = new Properties();
        properties.setProperty( "myProperty", "myValue" );
        m_platformContext.setProperties( properties );

        expect( m_configuration.getBootDelegation() ).andReturn( null );
        expect( m_configuration.getStartLevel() ).andReturn( 10 );
        expect( m_configuration.getBundleStartLevel() ).andReturn( 20 ).times( 2 );
        expect( m_configuration.getFrameworkProfile() ).andReturn( "myProfile" );
        expect( m_configuration.usePersistedState() ).andReturn( false );

        replay( m_bundleContext, m_configuration,
                bundle1, bundle2, bundle3, bundle4
        );
        new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).prepare( m_platformContext );
        verify( m_bundleContext, m_configuration,
                bundle1, bundle2, bundle3, bundle4
        );

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put(
            "${bundle1.path}",
            m_platformContext.normalizeAsUrl( new File( m_workDir, "bundles/bundle1.jar" ) )
        );
        replacements.put(
            "${bundle2.path}",
            m_platformContext.normalizeAsUrl( new File( m_workDir, "bundles/bundle2.jar" ) )
        );
        replacements.put(
            "${bundle3.path}",
            m_platformContext.normalizeAsUrl( new File( m_workDir, "bundles/bundle3.jar" ) )
        );
        replacements.put(
            "${bundle4.path}",
            m_platformContext.normalizeAsUrl( new File( m_workDir, "bundles/bundle4.jar" ) )
        );

        compareFiles(
            FileUtils.getFileFromClasspath( "felixplatformbuilder/config.ini" ),
            new File( m_workDir + "/felix/config.ini" ),
            true,
            replacements
        );
    }

    private static void compareFiles( File expected, File actual, boolean reverse, Map<String, String> replacements )
        throws IOException
    {
        BufferedReader expectedReader = null;
        BufferedReader actualReader = null;
        try
        {
            expectedReader = new BufferedReader( new FileReader( expected ) );
            actualReader = new BufferedReader( new FileReader( actual ) );
            String actualLine, expectedLine;
            int lineNumber = 1;
            while( ( actualLine = actualReader.readLine() ) != null )
            {
                expectedLine = expectedReader.readLine();
                if( reverse )
                {
                    if( replacements != null )
                    {
                        for( Map.Entry<String, String> entry : replacements.entrySet() )
                        {
                            expectedLine = expectedLine.replace( entry.getKey(), entry.getValue() );
                        }
                    }
                    assertEquals( "Config ini line " + lineNumber++, expectedLine, actualLine );
                }
                else
                {
                    if( replacements != null )
                    {
                        for( Map.Entry<String, String> entry : replacements.entrySet() )
                        {
                            actualLine = actualLine.replace( entry.getKey(), entry.getValue() );
                        }
                    }
                    assertEquals( "Config ini line " + lineNumber++, actualLine, expectedLine );
                }
            }
        }
        finally
        {
            if( expectedReader != null )
            {
                expectedReader.close();
            }
            if( actualReader != null )
            {
                actualReader.close();
            }
        }
        if( reverse )
        {
            compareFiles( actual, expected, false, replacements );
        }
    }

    public void clean( boolean usePersistedState )
        throws PlatformException, IOException
    {
        expect( m_configuration.getBootDelegation() ).andReturn( null );
        expect( m_configuration.getStartLevel() ).andReturn( null );
        expect( m_configuration.getBundleStartLevel() ).andReturn( null );
        expect( m_configuration.getFrameworkProfile() ).andReturn( "runner" );
        expect( m_configuration.usePersistedState() ).andReturn( usePersistedState );

        replay( m_bundleContext, m_configuration );
        new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).prepare( m_platformContext );
        verify( m_bundleContext, m_configuration );
    }

    // cache folder should not exist after returning
    @Test
    public void usePersistedStateWithExistingFolder()
        throws IOException, PlatformException
    {
        File cacheDir = new File( m_workDir, "felix/cache/runner" );
        new File( cacheDir, "bundle1" ).mkdirs();
        assertTrue( "Cache folder could not be created before running the test", cacheDir.exists() );
        clean( false );
        assertFalse( "Cache folder was not removed", cacheDir.exists() );
    }

    // cache folder should not exist after returning and should not crash as the folder is not there
    @Test
    public void usePersistedStateWithNotExistingFolder()
        throws IOException, PlatformException
    {
        File cacheDir = new File( m_workDir, "felix/cache/runner" );
        FileUtils.delete( cacheDir );
        assertFalse( "Cache folder could not be deleted before running the test", cacheDir.exists() );
        clean( false );
        assertFalse( "Cache folder was not removed", cacheDir.exists() );
    }

    // cahce folder should not exist after returning
    @Test
    public void noUsePersistedStateWithExistingFolder()
        throws IOException, PlatformException
    {
        new File( m_workDir, "felix/cache/runner/bundle1" ).mkdirs();
        File cacheDir = new File( m_workDir, "felix/cache/runner" );
        assertTrue( "Cache folder could not be created before running the test", cacheDir.exists() );
        clean( true );
        assertTrue( "Cache folder was removed but it should not had been removed", cacheDir.exists() );
    }

}
