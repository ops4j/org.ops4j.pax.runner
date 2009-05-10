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
package org.ops4j.pax.runner.platform.equinox.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
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
import org.ops4j.pax.runner.platform.internal.RelativeFilePathStrategy;

public class EquinoxPlatformBuilderTest
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
        m_platformContext.setFilePathStrategy( new RelativeFilePathStrategy( m_workDir ) );
    }

    @After
    public void tearDown()
    {
        FileUtils.delete( m_workDir );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullBundleContext()
    {
        new EquinoxPlatformBuilder( null, "version" );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullVersion()
    {
        new EquinoxPlatformBuilder( m_bundleContext, null );
    }

    @Test
    public void mainClassName()
    {
        replay( m_bundleContext );
        assertEquals(
            "Main class name",
            "org.eclipse.core.runtime.adaptor.EclipseStarter",
            new EquinoxPlatformBuilder( m_bundleContext, "version" ).getMainClassName()
        );
        verify( m_bundleContext );
    }

    @Test
    public void getRequiredProfiles()
    {
        replay( m_bundleContext );
        assertNull(
            "Required profiles is not null",
            new EquinoxPlatformBuilder( m_bundleContext, "version" ).getRequiredProfile( m_platformContext )
        );
        verify( m_bundleContext );
    }

    @Test
    public void getArgumentsWithConsole()
    {
        expect( m_configuration.startConsole() ).andReturn( true );
        expect( m_configuration.isDebugClassLoading() ).andReturn( false );

        replay( m_bundleContext, m_configuration );
        assertArrayEquals(
            "Arguments",
            new String[]{
                "-console",
                "-configuration",
                m_platformContext.getFilePathStrategy().normalizeAsPath( new File( m_workDir, "equinox" ) )
            },
            new EquinoxPlatformBuilder( m_bundleContext, "version" ).getArguments( m_platformContext )
        );
        verify( m_bundleContext, m_configuration );
    }

    @Test
    public void getArgumentsWithoutConsole()
    {
        expect( m_configuration.startConsole() ).andReturn( false );
        expect( m_configuration.isDebugClassLoading() ).andReturn( false );

        replay( m_bundleContext, m_configuration );
        assertArrayEquals(
            "Arguments",
            new String[]{
                "-configuration",
                m_platformContext.getFilePathStrategy().normalizeAsPath( new File( m_workDir, "equinox" ) )
            },
            new EquinoxPlatformBuilder( m_bundleContext, "version" ).getArguments( m_platformContext )
        );
        verify( m_bundleContext, m_configuration );
    }

    @Test
    public void getArgumentsWithNullConsoleValue()
    {
        expect( m_configuration.startConsole() ).andReturn( null );
        expect( m_configuration.isDebugClassLoading() ).andReturn( false );

        replay( m_bundleContext, m_configuration );
        assertArrayEquals(
            "Arguments",
            new String[]{
                "-configuration",
                m_platformContext.getFilePathStrategy().normalizeAsPath( new File( m_workDir, "equinox" ) )
            },
            new EquinoxPlatformBuilder( m_bundleContext, "version" ).getArguments( m_platformContext )
        );
        verify( m_bundleContext, m_configuration );
    }

    @Test( expected = IllegalArgumentException.class )
    public void getArgumentsWithNullPlatformContext()
    {
        replay( m_bundleContext );
        new EquinoxPlatformBuilder( m_bundleContext, "version" ).getArguments( null );
        verify( m_bundleContext );
    }

    @Test
    public void getVMOptions()
    {
        replay( m_bundleContext );
        assertArrayEquals(
            "System options",
            new String[]{
                "-Dosgi.install.area="
                + m_platformContext.getFilePathStrategy().normalizeAsPath( new File( m_workDir, "equinox" ) )
            },
            new EquinoxPlatformBuilder( m_bundleContext, "version" ).getVMOptions( m_platformContext )
        );
        verify( m_bundleContext );
    }

    @Test( expected = IllegalArgumentException.class )
    public void getVMOptionsWithNullPlatformContext()
    {
        replay( m_bundleContext );
        new EquinoxPlatformBuilder( m_bundleContext, "version" ).getVMOptions( null );
        verify( m_bundleContext );
    }

    @Test( expected = IllegalArgumentException.class )
    public void prepareWithNullPlatformContext()
        throws PlatformException
    {
        replay( m_bundleContext );
        new EquinoxPlatformBuilder( m_bundleContext, "version" ).prepare( null );
        verify( m_bundleContext );
    }

    // tests that the platform configuration ini file is correct with no bundles.
    // also tests that the start level is not set if is not configured
    // also tests that the default start level is not set if is not configured
    @Test
    public void prepareWithNullLocalBundles()
        throws PlatformException, IOException
    {

        m_platformContext.setExecutionEnvironment( "EE-1,EE-2" );
        m_platformContext.setSystemPackages( "sys.package.one,sys.package.two" );
        Properties properties = new Properties();
        properties.setProperty( "myProperty", "myValue" );
        m_platformContext.setProperties( properties );

        expect( m_configuration.startConsole() ).andReturn( true );
        expect( m_configuration.usePersistedState() ).andReturn( false );
        expect( m_configuration.getStartLevel() ).andReturn( null );
        expect( m_configuration.getBundleStartLevel() ).andReturn( null );
        expect( m_configuration.getBootDelegation() ).andReturn( "javax.*" );
        expect( m_configuration.isDebugClassLoading() ).andReturn( false );

        replay( m_bundleContext, m_configuration );
        new EquinoxPlatformBuilder( m_bundleContext, "version" ).prepare( m_platformContext );
        verify( m_bundleContext, m_configuration );

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put( "${sys.path}", m_platformContext.getFilePathStrategy().normalizeAsPath( m_workDir ) );
        compareFiles(
            FileUtils.getFileFromClasspath( "equinoxplatformbuilder/configWithNoBundles.ini" ),
            new File( m_workDir + "/equinox/config.ini" ),
            true,
            replacements
        );
    }

    // tests that the platform configuration ini file is correct with bundles to be installed.
    // also tests that the start level is set correctly if configured
    // also tests that the default start level is set correctly if configured
    @Test
    public void prepare()
        throws PlatformException, IOException, URISyntaxException
    {

        List<BundleReference> bundles = new ArrayList<BundleReference>();
        m_platformContext.setBundles( bundles );
        m_platformContext.setExecutionEnvironment( "EE-1,EE-2" );
        m_platformContext.setSystemPackages( "sys.package.one,sys.package.two" );
        Properties properties = new Properties();
        properties.setProperty( "myProperty", "myValue" );
        m_platformContext.setProperties( properties );

        expect( m_configuration.startConsole() ).andReturn( false );
        expect( m_configuration.usePersistedState() ).andReturn( false );
        expect( m_configuration.getStartLevel() ).andReturn( 10 );
        expect( m_configuration.getBundleStartLevel() ).andReturn( 20 );
        expect( m_configuration.getBootDelegation() ).andReturn( null );

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

        expect( m_configuration.isDebugClassLoading() ).andReturn( false );

        replay( m_bundleContext, m_configuration,
                bundle1, bundle2, bundle3
        );
        new EquinoxPlatformBuilder( m_bundleContext, "version" ).prepare( m_platformContext );
        verify( m_bundleContext, m_configuration,
                bundle1, bundle2, bundle3
        );

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put( "${sys.path}", m_platformContext.getFilePathStrategy().normalizeAsPath( m_workDir ) );
        replacements.put(
            "${bundle1.path}",
            m_platformContext.getFilePathStrategy().normalizeAsUrl( new File( m_workDir, "bundles/bundle1.jar" ) )
        );
        replacements.put(
            "${bundle2.path}",
            m_platformContext.getFilePathStrategy().normalizeAsUrl( new File( m_workDir, "bundles/bundle2.jar" ) )
        );
        replacements.put(
            "${bundle3.path}",
            m_platformContext.getFilePathStrategy().normalizeAsUrl( new File( m_workDir, "bundles/bundle3.jar" ) )
        );

        compareFiles(
            FileUtils.getFileFromClasspath( "equinoxplatformbuilder/config.ini" ),
            new File( m_workDir + "/equinox/config.ini" ),
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

}
