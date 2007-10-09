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
package org.ops4j.pax.runner.platform.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import static org.easymock.EasyMock.*;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.runner.commons.file.FileUtils;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.BundleReferenceBean;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.LocalBundle;
import org.ops4j.pax.runner.platform.PlatformBuilder;
import org.ops4j.pax.runner.platform.PlatformContext;
import org.ops4j.pax.runner.platform.PlatformException;

public class PlatformImplTest
{

    private PlatformDefinition m_definition;
    private Configuration m_config;
    private String m_workDir;
    private PlatformBuilder m_builder;
    private PlatformContext m_context;
    private BundleContext m_bundleContext;
    private Bundle m_bundle;

    @Before
    public void setUp()
        throws IOException
    {
        m_builder = createMock( PlatformBuilder.class );
        m_definition = createMock( PlatformDefinition.class );
        m_config = createMock( Configuration.class );
        m_context = createMock( PlatformContext.class );
        m_bundleContext = createMock( BundleContext.class );
        m_bundle = createMock( Bundle.class );
        File workDir = File.createTempFile( "runner", "" );
        m_workDir = workDir.getAbsolutePath();
        workDir.delete();
        workDir = new File( m_workDir );
        workDir.mkdirs();
        workDir.deleteOnExit();
    }

    @After
    public void tearDown()
    {
        FileUtils.delete( new File( m_workDir ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullPlatformBuilder()
        throws IOException, PlatformException
    {
        new PlatformImpl( null, m_bundleContext );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullBundleContext()
        throws IOException, PlatformException
    {
        new PlatformImpl( m_builder, null );
    }

    public String createPackageList( final String ee, final String packages )
        throws MalformedURLException, PlatformException
    {
        expect( m_config.getExecutionEnvironment() ).andReturn( ee );
        expect( m_config.getSystemPackages() ).andReturn( packages );
        expect( m_definition.getPackages() ).andReturn( null );

        replay( m_builder, m_bundleContext, m_bundle, m_config, m_definition );
        PlatformImpl platform = new PlatformImpl( m_builder, m_bundleContext );
        String systemPackages = platform.createPackageList( m_config, m_definition );
        verify( m_builder, m_bundleContext, m_bundle, m_config, m_definition );
        return systemPackages;
    }

    // test returned packages when there is no user defined packages option
    @Test
    public void createPackageListWithNoUserDefinedPackages()
        throws MalformedURLException, PlatformException
    {
        expect( m_bundleContext.getBundle() ).andReturn( m_bundle );
        expect( m_bundle.getResource( "META-INF/platform/ee/J2SE-1.5.packages" ) ).andReturn(
            FileUtils.getFileFromClasspath( "platform/systemPackages.txt" ).toURL()
        );
        assertEquals(
            "System packages",
            "system.package.1, system.package.2",
            createPackageList( "J2SE-1.5", null )
        );
    }

    // test returned packages when there is a user defined packages option
    @Test
    public void createPackageListWithUserDefinedPackages()
        throws MalformedURLException, PlatformException
    {
        expect( m_bundleContext.getBundle() ).andReturn( m_bundle );
        expect( m_bundle.getResource( "META-INF/platform/ee/J2SE-1.5.packages" ) ).andReturn(
            FileUtils.getFileFromClasspath( "platform/systemPackages.txt" ).toURL()
        );
        assertEquals(
            "System packages",
            "system.package.1, system.package.2, u.p.1, u.p.2",
            createPackageList( "J2SE-1.5", "u.p.1, u.p.2" )
        );
    }

    // test returned packages when the ee is an valid url
    @Test
    public void createPackageListWithURLEE()
        throws MalformedURLException, PlatformException
    {
        assertEquals(
            "System packages",
            "system.package.1, system.package.2",
            createPackageList(
                FileUtils.getFileFromClasspath( "platform/systemPackages.txt" ).toURL().toExternalForm(),
                null
            )
        );
    }

    // test that no packages are in use when ee is NONE
    @Test
    public void createPackageListNONE()
        throws MalformedURLException, PlatformException
    {
        assertEquals(
            "System packages",
            "",
            createPackageList(
                "NONE",
                null
            )
        );
    }

    // test returned packages when the ee is a valid url but has incorect letter case
    @Test
    public void createPackageListLetterCase()
        throws MalformedURLException, PlatformException
    {
        expect( m_bundleContext.getBundle() ).andReturn( m_bundle );
        expect( m_bundle.getResource( "META-INF/platform/ee/J2SE-1.5.packages" ) ).andReturn(
            FileUtils.getFileFromClasspath( "platform/systemPackages.txt" ).toURL()
        );
        assertEquals(
            "System packages",
            "system.package.1, system.package.2",
            createPackageList(
                "j2se-1.5",
                null
            )
        );
    }

    // test returned packages when the ee is an invalid url
    @Test( expected = PlatformException.class )
    public void createPackageListWithInvalidEE()
        throws MalformedURLException, PlatformException
    {
        expect( m_bundleContext.getBundle() ).andReturn( m_bundle );
        expect( m_bundle.getResource( "META-INF/platform/ee/J2SE-1.5.packages" ) ).andReturn(
            FileUtils.getFileFromClasspath( "platform/systemPackages.txt" ).toURL()
        );
        assertEquals(
            "System packages",
            "system.package.1, system.package.2",
            createPackageList(
                "invalid",
                null
            )
        );
    }

    // test returned packages when there are more EEs
    @Test
    public void createPackageListWithMoreEEs()
        throws MalformedURLException, PlatformException
    {
        expect( m_bundleContext.getBundle() ).andReturn( m_bundle );
        expect( m_bundle.getResource( "META-INF/platform/ee/J2SE-1.4.packages" ) ).andReturn(
            FileUtils.getFileFromClasspath( "platform/systemPackages.txt" ).toURL()
        );
        expect( m_bundleContext.getBundle() ).andReturn( m_bundle );
        expect( m_bundle.getResource( "META-INF/platform/ee/J2SE-1.5.packages" ) ).andReturn(
            FileUtils.getFileFromClasspath( "platform/systemPackages2.txt" ).toURL()
        );
        assertEquals(
            "System packages",
            "system.package.1, system.package.2, system.package.3",
            createPackageList( "J2SE-1.4,J2SE-1.5", null )
        );
    }

    // normal flow
    @Test
    public void getJavaExecutable()
        throws MalformedURLException, PlatformException
    {
        expect( m_config.getJavaHome() ).andReturn( "javaHome" );

        replay( m_builder, m_bundleContext, m_config );
        PlatformImpl platform = new PlatformImpl( m_builder, m_bundleContext );
        assertEquals( "Java executable", "javaHome/bin/java", platform.getJavaExecutable( m_config ) );
        verify( m_builder, m_bundleContext, m_config );
    }

    // test that a platform exception is thrown when there is no java home
    @Test( expected = PlatformException.class )
    public void getJavaExecutableWithInvalidJavaHome()
        throws MalformedURLException, PlatformException
    {
        expect( m_config.getJavaHome() ).andReturn( null );

        replay( m_builder, m_bundleContext, m_config );
        PlatformImpl platform = new PlatformImpl( m_builder, m_bundleContext );
        platform.getJavaExecutable( m_config );
    }

    @Test
    public void startWithBundles()
        throws MalformedURLException, PlatformException
    {
        List<BundleReference> bundles = new ArrayList<BundleReference>();
        bundles.add(
            new BundleReferenceBean( FileUtils.getFileFromClasspath( "platform/bundle1.jar" ).toURL() )
        );
        bundles.add(
            new BundleReferenceBean( FileUtils.getFileFromClasspath( "platform/bundle2.jar" ).toURL() )
        );
        start( bundles );
    }

    // test that platform starts even without bundles to be installed
    @Test
    public void startWithoutBundles()
        throws MalformedURLException, PlatformException
    {
        start( null );
    }

    // expected to throw an exception the bundle is a plain file not a jar
    @Test( expected = PlatformException.class )
    public void startWithNotAJarBundle()
        throws MalformedURLException, PlatformException
    {
        List<BundleReference> bundles = new ArrayList<BundleReference>();
        bundles.add(
            new BundleReferenceBean( FileUtils.getFileFromClasspath( "platform/invalid.jar" ).toURL() )
        );
        start( bundles );
    }

    // expected to throw an exception since bundle is jar that does not have a manifest entry for symbolic name
    @Test( expected = PlatformException.class )
    public void startWithAJarWithNoManifestAttr()
        throws MalformedURLException, PlatformException
    {
        List<BundleReference> bundles = new ArrayList<BundleReference>();
        bundles.add(
            new BundleReferenceBean( FileUtils.getFileFromClasspath( "platform/noManifestAttr.jar" ).toURL() )
        );
        start( bundles );
    }

    // test that platform starts even if the system jar is not a bundle by itself
    @Test
    public void startWithoutBundlesAndAsystemJarThatIsNotBunlde()
        throws MalformedURLException, PlatformException
    {
        start( null, FileUtils.getFileFromClasspath( "platform/noManifestAttr.jar" ).toURL() );
    }

    public void start( final List<BundleReference> bundles )
        throws PlatformException, MalformedURLException
    {
        start( bundles, FileUtils.getFileFromClasspath( "platform/system.jar" ).toURL() );
    }

    public void start( final List<BundleReference> bundles, URL systemBundleURL )
        throws PlatformException, MalformedURLException
    {
        expect( m_builder.getMainClassName() ).andReturn( "Main" );
        m_context.setProperties( null );
        m_context.setConfiguration( m_config );
        expect( m_config.isCleanStart() ).andReturn( false );
        expect( m_config.getWorkingDirectory() ).andReturn( m_workDir );
        m_context.setWorkingDirectory( new File( m_workDir ) );
        expect( m_config.isOverwrite() ).andReturn( true );
        expect( m_config.isOverwriteUserBundles() ).andReturn( false );
        expect( m_config.isOverwriteSystemBundles() ).andReturn( false );
        expect( m_definition.getSystemPackage() ).andReturn( systemBundleURL );
        expect( m_definition.getSystemPackageName() ).andReturn( "system package" );
        List<BundleReference> platformBundles = new ArrayList<BundleReference>();
        platformBundles.add(
            new BundleReferenceBean( FileUtils.getFileFromClasspath( "platform/platform.jar" ).toURL() )
        );
        // from downloadPlatformBundles()
        expect( m_context.getConfiguration() ).andReturn( m_config );
        expect( m_builder.getRequiredProfile( m_context ) ).andReturn( null );
        expect( m_definition.getPlatformBundles( "" ) ).andReturn( platformBundles );
        // from start()
        m_context.setBundles( (List<LocalBundle>) notNull() );
        m_context.setSystemPackages( "systemPackages" );
        m_builder.prepare( m_context );
        expect( m_config.getProfiles() ).andReturn( null );
        expect( m_config.getVMOptions() ).andReturn( new String[]{ "-Xmx512m", "-Xms128m" } );
        expect( m_builder.getVMOptions( m_context ) ).andReturn( new String[]{ "-Dproperty=value" } );
        expect( m_config.getClasspath() ).andReturn( "" );
        expect( m_builder.getArguments( m_context ) ).andReturn( new String[]{ "arg1" } );
        expect( m_context.getWorkingDirectory() ).andReturn( new File( m_workDir ) );

        replay( m_builder, m_definition, m_config, m_context, m_bundleContext, m_bundle );
        new TestPlatform().start( bundles, null, null );
        verify( m_builder, m_definition, m_config, m_context, m_bundleContext, m_bundle );
    }

    private class TestPlatform
        extends PlatformImpl
    {

        TestPlatform()
            throws PlatformException
        {
            super( m_builder, m_bundleContext );
        }

        @Override
        PlatformDefinition createPlatformDefinition( final Configuration configuration )
            throws PlatformException
        {
            return m_definition;
        }

        @Override
        Configuration createConfiguration( final Dictionary config )
        {
            return m_config;
        }

        @Override
        PlatformContext createPlatformContext()
        {
            return m_context;
        }

        @Override
        void executeProcess( final String[] commandLine, final File workingDirectory )
            throws PlatformException
        {
            assertNotNull( "Command line cannot be null", commandLine );
            assertNotNull( "Working directory cannot be null", workingDirectory );
        }

        @Override
        String createPackageList( final Configuration configuration, final PlatformDefinition platformDefinition )
            throws PlatformException
        {
            assertNotNull( "Configuration cannot be null", configuration );
            return "systemPackages";
        }

        @Override
        String getJavaExecutable( final Configuration configuration )
            throws PlatformException
        {
            return "javaExecutable";
        }
    }

}
