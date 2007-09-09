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
package org.ops4j.pax.runner.platform.knopflerfish.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.runner.commons.file.FileUtils;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.LocalBundle;
import org.ops4j.pax.runner.platform.PlatformContext;
import org.ops4j.pax.runner.platform.PlatformException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class KnopflerfishPlatformBuilderTest
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
        m_workDir = new File( File.createTempFile( "runner", null ).getParentFile(), "runner" );
        m_workDir.mkdirs();
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullBundleContext()
    {
        new KnopflerfishPlatformBuilder( null );
    }

    @Test
    public void mainClassName()
    {
        replay( m_bundleContext );
        assertEquals(
            "Main class name",
            "org.knopflerfish.framework.Main",
            new KnopflerfishPlatformBuilder( m_bundleContext ).getMainClassName()
        );
        verify( m_bundleContext );
    }

    @Test
    public void getDefinition()
        throws IOException
    {
        Bundle bundle = createMock( Bundle.class );

        expect( m_bundleContext.getBundle() ).andReturn( bundle );
        expect( bundle.getResource( "META-INF/platform-knopflerfish/definition-2.0.0.xml" ) ).andReturn(
            FileUtils.getFileFromClasspath( "META-INF/platform-knopflerfish/definition-2.0.0.xml" ).toURL()
        );

        replay( m_bundleContext, bundle );
        assertNotNull(
            "Definition input stream",
            new KnopflerfishPlatformBuilder( m_bundleContext ).getDefinition()
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
            new KnopflerfishPlatformBuilder( m_bundleContext ).getRequiredProfile( platformContext )
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
            new KnopflerfishPlatformBuilder( m_bundleContext ).getRequiredProfile( platformContext )
        );
        verify( m_bundleContext, m_configuration, platformContext );
    }

    @Test
    public void getArguments()
        throws MalformedURLException
    {
        PlatformContext platformContext = createMock( PlatformContext.class );

        expect( platformContext.getWorkingDirectory() ).andReturn( m_workDir );

        replay( m_bundleContext, platformContext );
        assertArrayEquals(
            "Arguments",
            new String[]{
                "-xargs",
                new File( m_workDir, "knopflerfish/config.ini" ).getAbsoluteFile().toURL().toExternalForm(),
            },
            new KnopflerfishPlatformBuilder( m_bundleContext ).getArguments( platformContext )
        );
        verify( m_bundleContext, platformContext );
    }

    @Test
    public void getVMOptions()
    {
        PlatformContext platformContext = createMock( PlatformContext.class );

        expect( platformContext.getWorkingDirectory() ).andReturn( m_workDir );

        replay( m_bundleContext, platformContext );
        assertArrayEquals(
            "System properties",
            new String[]{
                "-Dorg.knopflerfish.framework.usingwrapperscript=false",
                "-Dorg.knopflerfish.framework.exitonshutdown=true",
                "-Dorg.osgi.framework.dir="
                + m_workDir.getAbsolutePath() + File.separator + "knopflerfish" + File.separator + "fwdir"
            },
            new KnopflerfishPlatformBuilder( m_bundleContext ).getVMOptions( platformContext )
        );
        verify( m_bundleContext, platformContext );
    }

    @Test( expected = IllegalArgumentException.class )
    public void getSystemPropertiesWithNullPlatformContext()
    {
        replay( m_bundleContext );
        new KnopflerfishPlatformBuilder( m_bundleContext ).getVMOptions( null );
        verify( m_bundleContext );
    }

    @Test( expected = IllegalArgumentException.class )
    public void prepareWithNullPlatformContext()
        throws PlatformException
    {
        replay( m_bundleContext );
        new KnopflerfishPlatformBuilder( m_bundleContext ).prepare( null );
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
        expect( platformContext.getConfiguration() ).andReturn( m_configuration );
        expect( m_configuration.shouldClean() ).andReturn( false );
        expect( platformContext.getSystemPackages() ).andReturn( "sys.package.one,sys.package.two" );
        expect( m_configuration.getStartLevel() ).andReturn( null );
        expect( m_configuration.getBundleStartLevel() ).andReturn( null );

        Properties properties = new Properties();
        properties.setProperty( "myProperty", "myValue" );
        expect( platformContext.getProperties() ).andReturn( properties );

        replay( m_bundleContext, m_configuration, platformContext );
        new KnopflerfishPlatformBuilder( m_bundleContext ).prepare( platformContext );
        verify( m_bundleContext, m_configuration, platformContext );

        compareFiles(
            FileUtils.getFileFromClasspath( "knopflerfishplatformbuilder/configWithNoBundles.ini" ),
            new File( m_workDir + "/knopflerfish/config.ini" ),
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
        //expect( reference1.getStartLevel() ).andReturn( 10 );
        expect( reference1.shouldStart() ).andReturn( true );

        // a bundle with only start level that should not start
        LocalBundle bundle2 = createMock( LocalBundle.class );
        bundles.add( bundle2 );
        BundleReference reference2 = createMock( BundleReference.class );
        expect( bundle2.getFile() ).andReturn( new File( "bundle2.jar" ) );
        expect( bundle2.getBundleReference() ).andReturn( reference2 );
        //expect( reference2.getStartLevel() ).andReturn( 10 );
        expect( reference2.shouldStart() ).andReturn( null );

        // a bunlde without start level that should start
        LocalBundle bundle3 = createMock( LocalBundle.class );
        bundles.add( bundle3 );
        BundleReference reference3 = createMock( BundleReference.class );
        expect( bundle3.getFile() ).andReturn( new File( "bundle3.jar" ) );
        expect( bundle3.getBundleReference() ).andReturn( reference3 );
        //expect( reference3.getStartLevel() ).andReturn( null );
        expect( reference3.shouldStart() ).andReturn( true );

        // a bundle without start level that should not start
        LocalBundle bundle4 = createMock( LocalBundle.class );
        bundles.add( bundle4 );
        BundleReference reference4 = createMock( BundleReference.class );
        expect( bundle4.getFile() ).andReturn( new File( "bundle4.jar" ) );
        expect( bundle4.getBundleReference() ).andReturn( reference4 );
        //expect( reference4.getStartLevel() ).andReturn( null );
        expect( reference4.shouldStart() ).andReturn( null );

        expect( platformContext.getBundles() ).andReturn( bundles );
        expect( platformContext.getWorkingDirectory() ).andReturn( m_workDir );
        expect( platformContext.getConfiguration() ).andReturn( m_configuration );
        expect( m_configuration.shouldClean() ).andReturn( false );
        expect( platformContext.getSystemPackages() ).andReturn( "sys.package.one,sys.package.two" );
        expect( m_configuration.getStartLevel() ).andReturn( 10 );
        expect( m_configuration.getBundleStartLevel() ).andReturn( 20 );

        Properties properties = new Properties();
        properties.setProperty( "myProperty", "myValue" );
        expect( platformContext.getProperties() ).andReturn( properties );

        replay( m_bundleContext, m_configuration, platformContext, bundle1, bundle2, bundle3, reference1, reference2,
                reference3, bundle4, reference4
        );
        new KnopflerfishPlatformBuilder( m_bundleContext ).prepare( platformContext );
        verify( m_bundleContext, m_configuration, platformContext, bundle1, bundle2, bundle3, reference1, reference2,
                reference3, bundle4, reference4
        );

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put( "${bundle1.path}", new File( "bundle1.jar" ).toURL().toExternalForm() );
        replacements.put( "${bundle2.path}", new File( "bundle2.jar" ).toURL().toExternalForm() );
        replacements.put( "${bundle3.path}", new File( "bundle3.jar" ).toURL().toExternalForm() );
        replacements.put( "${bundle4.path}", new File( "bundle4.jar" ).toURL().toExternalForm() );

        compareFiles(
            FileUtils.getFileFromClasspath( "knopflerfishplatformbuilder/config.ini" ),
            new File( m_workDir + "/knopflerfish/config.ini" ),
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
            while ( ( actualLine = actualReader.readLine() ) != null )
            {
                expectedLine = expectedReader.readLine();
                if ( reverse )
                {
                    if ( replacements != null )
                    {
                        for ( Map.Entry<String, String> entry : replacements.entrySet() )
                        {
                            expectedLine = expectedLine.replace( entry.getKey(), entry.getValue() );
                        }
                    }
                    assertEquals( "Config ini line " + lineNumber++, expectedLine, actualLine );
                }
                else
                {
                    if ( replacements != null )
                    {
                        for ( Map.Entry<String, String> entry : replacements.entrySet() )
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
            if ( expectedReader != null )
            {
                expectedReader.close();
            }
            if ( actualReader != null )
            {
                actualReader.close();
            }
        }
        if ( reverse )
        {
            compareFiles( actual, expected, false, replacements );
        }
    }

    public void clean( boolean shoudlClean )
        throws PlatformException, IOException
    {
        PlatformContext platformContext = createMock( PlatformContext.class );

        expect( platformContext.getBundles() ).andReturn( null );
        expect( platformContext.getWorkingDirectory() ).andReturn( m_workDir );
        expect( platformContext.getConfiguration() ).andReturn( m_configuration );
        expect( platformContext.getSystemPackages() ).andReturn( null );
        expect( m_configuration.getStartLevel() ).andReturn( null );
        expect( m_configuration.getBundleStartLevel() ).andReturn( null );
        expect( m_configuration.shouldClean() ).andReturn( shoudlClean );
        expect( platformContext.getProperties() ).andReturn( null );

        replay( m_bundleContext, m_configuration, platformContext );
        new KnopflerfishPlatformBuilder( m_bundleContext ).prepare( platformContext );
        verify( m_bundleContext, m_configuration, platformContext );
    }

    // cahce folder should not exist after returning
    @Test
    public void cleanWithExistingFolder()
        throws IOException, PlatformException
    {
        File cacheDir = new File( m_workDir, "knopflerfish/fwdir" );
        new File( cacheDir, "bundle1" ).mkdirs();
        assertTrue( "Cache folder could not be created before running the test", cacheDir.exists() );
        clean( true );
        assertFalse( "Cache folder was not removed", cacheDir.exists() );
    }

    // cahce folder should not exist after returning and should not crash as the folder is not there
    @Test
    public void cleanWithNotExistingFolder()
        throws IOException, PlatformException
    {
        File cacheDir = new File( m_workDir, "knopflerfish/fwdir" );
        FileUtils.delete( cacheDir );
        assertFalse( "Cache folder could not be deleted before running the test", cacheDir.exists() );
        clean( true );
        assertFalse( "Cache folder was not removed", cacheDir.exists() );
    }

    // cahce folder should not exist after returning
    @Test
    public void noCleanWithExistingFolder()
        throws IOException, PlatformException
    {
        File cacheDir = new File( m_workDir, "knopflerfish/fwdir" );
        new File( cacheDir, "bundle1" ).mkdirs();
        assertTrue( "Cache folder could not be created before running the test", cacheDir.exists() );
        clean( false );
        assertTrue( "Cache folder was removed but it should not had been removed", cacheDir.exists() );
    }

}
