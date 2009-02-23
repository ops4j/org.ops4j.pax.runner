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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.LocalBundle;
import org.ops4j.pax.runner.platform.PlatformContext;
import org.ops4j.pax.runner.platform.PlatformException;

public class FelixPlatformBuilderF100T122Test
{

    private File m_workDir;
    private BundleContext m_bundleContext;
    private Configuration m_configuration;

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
    public void getDefinition_1_0_0()
        throws IOException
    {
        Bundle bundle = createMock( Bundle.class );

        expect( m_bundleContext.getBundle() ).andReturn( bundle );
        expect( bundle.getResource( "META-INF/platform-felix/definition-1.0.0.xml" ) ).andReturn(
            FileUtils.getFileFromClasspath( "META-INF/platform-felix/definition-1.0.0.xml" ).toURL()
        );

        replay( m_bundleContext, bundle );
        assertNotNull(
            "Definition input stream",
            new FelixPlatformBuilderF100T122( m_bundleContext, "1.0.0" ).getDefinition()
        );
        verify( m_bundleContext, bundle );
    }

    @Test
    public void getRequiredProfilesWithoutConsole()
    {
        PlatformContext platformContext = createMock( PlatformContext.class );

        expect( platformContext.getConfiguration() ).andReturn( m_configuration );
        expect( m_configuration.startConsole() ).andReturn( null );

        replay( m_bundleContext, m_configuration, platformContext );
        assertNull(
            "Required profiles is not null",
            new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).getRequiredProfile( platformContext )
        );
        verify( m_bundleContext, m_configuration, platformContext );
    }

    @Test
    public void getRequiredProfilesWithConsole()
    {
        PlatformContext platformContext = createMock( PlatformContext.class );

        expect( platformContext.getConfiguration() ).andReturn( m_configuration );
        expect( m_configuration.startConsole() ).andReturn( true );

        replay( m_bundleContext, m_configuration, platformContext );
        assertEquals(
            "Required profiles",
            "tui",
            new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).getRequiredProfile( platformContext )
        );
        verify( m_bundleContext, m_configuration, platformContext );
    }

    @Test
    public void getArguments()
    {
        PlatformContext platformContext = createMock( PlatformContext.class );

        replay( m_bundleContext, m_configuration, platformContext );
        assertNull( "Arguments is not not null",
                    new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).getArguments( platformContext )
        );
        verify( m_bundleContext, m_configuration, platformContext );
    }

    @Test
    public void getVMOptions()
    {
        PlatformContext platformContext = createMock( PlatformContext.class );

        expect( platformContext.getWorkingDirectory() ).andReturn( m_workDir );

        replay( m_bundleContext, platformContext );
        assertArrayEquals(
            "System options",
            new String[]{
                "-Dfelix.config.properties="
                + m_workDir.toURI() + "/felix/config.ini",
                "-Dfelix.cache.dir="
                + m_workDir.getAbsolutePath() + File.separator + "felix" + File.separator + "cache"
            },
            new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).getVMOptions( platformContext )
        );
        verify( m_bundleContext, platformContext );
    }

    @Test( expected = IllegalArgumentException.class )
    public void getSystemPropertiesWithNullPlatformContext()
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
        PlatformContext platformContext = createMock( PlatformContext.class );

        expect( platformContext.getBundles() ).andReturn( null );
        expect( platformContext.getWorkingDirectory() ).andReturn( m_workDir );
        expect( platformContext.getConfiguration() ).andReturn( m_configuration ).times( 2 );
        expect( platformContext.getExecutionEnvironment() ).andReturn( "EE-1,EE-2" );
        expect( m_configuration.getBootDelegation() ).andReturn( "javax.*" );
        expect( platformContext.getSystemPackages() ).andReturn( "sys.package.one,sys.package.two" );
        expect( m_configuration.getStartLevel() ).andReturn( null );
        expect( m_configuration.getBundleStartLevel() ).andReturn( null );
        expect( m_configuration.getFrameworkProfile() ).andReturn( null );

        Properties properties = new Properties();
        properties.setProperty( "myProperty", "myValue" );
        expect( platformContext.getProperties() ).andReturn( properties );

        replay( m_bundleContext, m_configuration, platformContext );
        new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).prepare( platformContext );
        verify( m_bundleContext, m_configuration, platformContext );

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
        PlatformContext platformContext = createMock( PlatformContext.class );

        List<LocalBundle> bundles = new ArrayList<LocalBundle>();

        // a bunlde with start level that should start
        LocalBundle bundle1 = createMock( LocalBundle.class );
        bundles.add( bundle1 );
        BundleReference reference1 = createMock( BundleReference.class );
        expect( bundle1.getFile() ).andReturn( new File( "bundle1.jar" ) );
        expect( bundle1.getBundleReference() ).andReturn( reference1 );
        expect( reference1.getStartLevel() ).andReturn( 10 );
        expect( reference1.shouldStart() ).andReturn( true );

        // a bundle with only start level that should not start
        LocalBundle bundle2 = createMock( LocalBundle.class );
        bundles.add( bundle2 );
        BundleReference reference2 = createMock( BundleReference.class );
        expect( bundle2.getFile() ).andReturn( new File( "bundle2.jar" ) );
        expect( bundle2.getBundleReference() ).andReturn( reference2 );
        expect( reference2.getStartLevel() ).andReturn( 10 );
        expect( reference2.shouldStart() ).andReturn( null );

        // a bunlde without start level that should start
        LocalBundle bundle3 = createMock( LocalBundle.class );
        bundles.add( bundle3 );
        BundleReference reference3 = createMock( BundleReference.class );
        expect( bundle3.getFile() ).andReturn( new File( "bundle3.jar" ) );
        expect( bundle3.getBundleReference() ).andReturn( reference3 );
        expect( reference3.getStartLevel() ).andReturn( null );
        expect( reference3.shouldStart() ).andReturn( true );

        // a bundle without start level that should not start
        LocalBundle bundle4 = createMock( LocalBundle.class );
        bundles.add( bundle4 );
        BundleReference reference4 = createMock( BundleReference.class );
        expect( bundle4.getFile() ).andReturn( new File( "bundle4.jar" ) );
        expect( bundle4.getBundleReference() ).andReturn( reference4 );
        expect( reference4.getStartLevel() ).andReturn( null );
        expect( reference4.shouldStart() ).andReturn( null );

        expect( platformContext.getBundles() ).andReturn( bundles );
        expect( platformContext.getWorkingDirectory() ).andReturn( m_workDir );
        expect( platformContext.getConfiguration() ).andReturn( m_configuration ).times( 2 );
        expect( platformContext.getExecutionEnvironment() ).andReturn( "EE-1,EE-2" );
        expect( m_configuration.getBootDelegation() ).andReturn( null );
        expect( platformContext.getSystemPackages() ).andReturn( "sys.package.one,sys.package.two" );
        expect( m_configuration.getStartLevel() ).andReturn( 10 );
        expect( m_configuration.getBundleStartLevel() ).andReturn( 20 ).times( 2 );
        expect( m_configuration.getFrameworkProfile() ).andReturn( "myProfile" );
        expect( m_configuration.usePersistedState() ).andReturn( false );

        Properties properties = new Properties();
        properties.setProperty( "myProperty", "myValue" );
        expect( platformContext.getProperties() ).andReturn( properties );

        replay( m_bundleContext, m_configuration, platformContext, bundle1, bundle2, bundle3, reference1, reference2,
                reference3, bundle4, reference4
        );
        new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).prepare( platformContext );
        verify( m_bundleContext, m_configuration, platformContext, bundle1, bundle2, bundle3, reference1, reference2,
                reference3, bundle4, reference4
        );

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put( "${bundle1.path}", new File( "bundle1.jar" ).toURL().toExternalForm() );
        replacements.put( "${bundle2.path}", new File( "bundle2.jar" ).toURL().toExternalForm() );
        replacements.put( "${bundle3.path}", new File( "bundle3.jar" ).toURL().toExternalForm() );
        replacements.put( "${bundle4.path}", new File( "bundle4.jar" ).toURL().toExternalForm() );

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
        PlatformContext platformContext = createMock( PlatformContext.class );

        expect( platformContext.getBundles() ).andReturn( null );
        expect( platformContext.getWorkingDirectory() ).andReturn( m_workDir );
        expect( platformContext.getConfiguration() ).andReturn( m_configuration ).times( 2 );
        expect( platformContext.getExecutionEnvironment() ).andReturn( "EE-1,EE-2" );
        expect( m_configuration.getBootDelegation() ).andReturn( null );
        expect( platformContext.getSystemPackages() ).andReturn( null );
        expect( m_configuration.getStartLevel() ).andReturn( null );
        expect( m_configuration.getBundleStartLevel() ).andReturn( null );
        expect( m_configuration.getFrameworkProfile() ).andReturn( "runner" );
        expect( m_configuration.usePersistedState() ).andReturn( usePersistedState );
        expect( platformContext.getProperties() ).andReturn( null );

        replay( m_bundleContext, m_configuration, platformContext );
        new FelixPlatformBuilderF100T122( m_bundleContext, "version" ).prepare( platformContext );
        verify( m_bundleContext, m_configuration, platformContext );
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
