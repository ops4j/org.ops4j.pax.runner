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

import java.net.MalformedURLException;
import java.net.URL;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.platform.Configuration;

public class ConfigurationImplTest
{

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullResolver()
    {
        new ConfigurationImpl( null );
    }

    // test normal flow
    @Test
    public void getDefinitionURL()
        throws MalformedURLException
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.definitionURL" ) ).andReturn( "file:definition.xml" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Definition URL", new URL( "file:definition.xml" ), config.getDefinitionURL() );
        verify( resolver );
    }

    // expect a MalformedURLException when the property value is not good.
    @Test( expected = MalformedURLException.class )
    public void getMalformedDefinitionURL()
        throws MalformedURLException
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.definitionURL" ) ).andReturn( "xxx:definition.xml" );

        replay( resolver );
        new ConfigurationImpl( resolver ).getDefinitionURL();
        verify( resolver );
    }

    // test that it does not crash when definition url property is not set
    @Test
    public void getNotConfiguredDefinitionURL()
        throws MalformedURLException
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.definitionURL" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Definition URL", null, config.getDefinitionURL() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void getWorkingDirectory()
        throws MalformedURLException
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.workingDirectory" ) ).andReturn( "myWorkingDirectory" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Working directory", "myWorkingDirectory", config.getWorkingDirectory() );
        verify( resolver );
    }

    // test that it returns "runner" when property is not set
    @Test
    public void getDefualtWorkingDirectory()
        throws MalformedURLException
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.workingDirectory" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Default working directory", "runner", config.getWorkingDirectory() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void getSystemPackages()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.systemPackages" ) ).andReturn( "systemPackages" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "System packages", "systemPackages", config.getSystemPackages() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void getExecutionEnvironment()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.ee" ) ).andReturn( "some-ee" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Execution environment", "some-ee", config.getExecutionEnvironment() );
        verify( resolver );
    }

    // return the ee based on jvm version if option not set
    @Test
    public void getDefaultJavaVersion()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.ee" ) ).andReturn( null );
        String javaVersion = System.getProperty( "java.version" ).substring( 0, 3 );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Execution environment", "J2SE-" + javaVersion, config.getExecutionEnvironment() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void getJavaHome()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.javaHome" ) ).andReturn( "javaHome" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Java home", "javaHome", config.getJavaHome() );
        verify( resolver );
    }

    // returns the home of the current running version
    @Test
    public void getDefaultJavaHome()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.javaHome" ) ).andReturn( null );
        String javaHome = System.getProperty( "JAVA_HOME" );
        if( javaHome == null )
        {
            try
            {
                javaHome = System.getenv( "JAVA_HOME" );
            }
            catch( Error e )
            {
            }
            if( javaHome == null )
            {
                javaHome = System.getProperty( "java.home" );
            }
        }
        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Java home", javaHome, config.getJavaHome() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void getProfileStartLevel()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.profileStartLevel" ) ).andReturn( "10" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Platform start level", Integer.valueOf( 10 ), config.getProfileStartLevel() );
        verify( resolver );
    }

    // default platform bundles start level is 1
    @Test
    public void getDefaultProfileStartLevel()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.profileStartLevel" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Platform start level", Integer.valueOf( 1 ), config.getProfileStartLevel() );
        verify( resolver );
    }

    // does not crash with an invalid value and returns the default value 1
    @Test
    public void getDefaultProfileStartLevelIfOptionIsInvalid()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.profileStartLevel" ) ).andReturn( "invalid" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Platform start level", Integer.valueOf( 1 ), config.getProfileStartLevel() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void getStartLevel()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.startLevel" ) ).andReturn( "10" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Platform start level", Integer.valueOf( 10 ), config.getStartLevel() );
        verify( resolver );
    }

    // default start level is 6
    @Test
    public void getDefaultStartLevel()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.startLevel" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Platform start level", Integer.valueOf( 6 ), config.getStartLevel() );
        verify( resolver );
    }

    // does not crash with an invalid value and returns the default value 6
    @Test
    public void getDefaultStartLevelIfOptionIsInvalid()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.startLevel" ) ).andReturn( "invalid" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Platform start level", Integer.valueOf( 6 ), config.getStartLevel() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void getBundleStartLevel()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.bundleStartLevel" ) ).andReturn( "10" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Bundles start level", Integer.valueOf( 10 ), config.getBundleStartLevel() );
        verify( resolver );
    }

    // default start level is 6
    @Test
    public void getDefaultBundleStartLevel()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.bundleStartLevel" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Bundles start level", Integer.valueOf( 5 ), config.getBundleStartLevel() );
        verify( resolver );
    }

    // does not crash with an invalid value and returns the default value 6
    @Test
    public void getDefaultBundleStartLevelIfOptionIsInvalid()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.bundleStartLevel" ) ).andReturn( "invalid" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Bundles start level", Integer.valueOf( 5 ), config.getBundleStartLevel() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void usePersistedState()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.usePersistedState" ) ).andReturn( "true" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Use persisted state", true, config.usePersistedState() );
        verify( resolver );
    }

    // test that an invalid value will not cause problems and will return false
    @Test
    public void usePersistedStateWithInvalidValue()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.usePersistedState" ) ).andReturn( "of course" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Use persisted state", false, config.usePersistedState() );
        verify( resolver );
    }

    // test that if value is not set it will not cause problems and will return false
    @Test
    public void usePersistedStateDefaultValue()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.usePersistedState" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Use persisted state", false, config.usePersistedState() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void startConsole()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.console" ) ).andReturn( "true" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Start console", true, config.startConsole() );
        verify( resolver );
    }

    // test that an invalid value will not cause problems and will return true.
    @Test
    public void startConsoleWithInvalidValue()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.console" ) ).andReturn( "of course" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Start console", false, config.startConsole() );
        verify( resolver );
    }

    // test that an not set value (null) will not cause problems and will return true.
    @Test
    public void startConsoleWithNotSetValue()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.console" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Start console", true, config.startConsole() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void getProfiles()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.profiles" ) ).andReturn( "myProfile" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Profiles", "myProfile", config.getProfiles() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void getFrameworkProfile()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.frameworkProfile" ) ).andReturn( "myProfile" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Framework profile", "myProfile", config.getFrameworkProfile() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void getDefaultFrameworkProfile()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.frameworkProfile" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Framework profile", "runner", config.getFrameworkProfile() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void isOverwrite()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.overwrite" ) ).andReturn( "true" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Overwrite", true, config.isOverwrite() );
        verify( resolver );
    }

    // test that an invalid value will not cause problems and will return false
    @Test
    public void isOverwriteWithInvalidValue()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.overwrite" ) ).andReturn( "of course" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Overwrite", false, config.isOverwrite() );
        verify( resolver );
    }

    // default value should be false
    @Test
    public void isOverwriteDefault()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.overwrite" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Overwrite", false, config.isOverwrite() );
        verify( resolver );
    }

    /**
     * Tests the happy path.
     */
    @Test
    public void clean()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.clean" ) ).andReturn( "true" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Clean", true, config.isCleanStart() );
        verify( resolver );
    }

    /**
     * Tests that default value is false.
     */
    @Test
    public void cleanDefault()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.clean" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Clean", false, config.isCleanStart() );
        verify( resolver );
    }

    /**
     * Test that an invalid value will not cause problems and will return false.
     */
    @Test
    public void cleanWithInvalidValue()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.clean" ) ).andReturn( "of course" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Clean", false, config.isCleanStart() );
        verify( resolver );
    }

    /**
     * Tests that if vm options is set and contains only one option the correct array is returned.
     */
    @Test
    public void getVMOptionsWithOneOption()
        throws MalformedURLException
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.vmOptions" ) ).andReturn( "-Xmx512m" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertArrayEquals( "Working directory", new String[]{ "-Xmx512m" }, config.getVMOptions() );
        verify( resolver );
    }

    /**
     * Tests that if vm options is set and contains more then one option the correct array is returned.
     */
    @Test
    public void getVMOptionsWithMoreOptions()
        throws MalformedURLException
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.vmOptions" ) ).andReturn( "-Xmx512m -Xms512m" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertArrayEquals( "Working directory", new String[]{ "-Xmx512m", "-Xms512m" }, config.getVMOptions() );
        verify( resolver );
    }

    /**
     * Tests tthat if vm options is not set there is no exception (as NPE) and null is returned.
     */
    @Test
    public void getVMOptionsNotSet()
        throws MalformedURLException
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.vmOptions" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertArrayEquals( "Working directory", null, config.getVMOptions() );
        verify( resolver );
    }

    /**
     * Test normal flow
     */
    @Test
    public void isOverwriteUserBundles()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.overwriteUserBundles" ) ).andReturn( "true" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Overwrite user bundles", true, config.isOverwriteUserBundles() );
        verify( resolver );
    }

    /**
     * Test that an invalid value will not cause problems and will return false
     */
    @Test
    public void isOverwriteUserBundlesWithInvalidValue()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.overwriteUserBundles" ) ).andReturn( "of course" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Overwrite user bundles", false, config.isOverwriteUserBundles() );
        verify( resolver );
    }

    /**
     * Test that default value is false.
     */
    @Test
    public void isOverwriteUserBundlesDefault()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.overwriteUserBundles" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Overwrite user bundles", false, config.isOverwriteUserBundles() );
        verify( resolver );
    }

    /**
     * Test normal flow
     */
    @Test
    public void isOverwriteSystemBundles()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.overwriteSystemBundles" ) ).andReturn( "true" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Overwrite system bundles", true, config.isOverwriteSystemBundles() );
        verify( resolver );
    }

    /**
     * Test that an invalid value will not cause problems and will return false
     */
    @Test
    public void isOverwriteSystemBundlesWithInvalidValue()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.overwriteSystemBundles" ) ).andReturn( "of course" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Overwrite system bundles", false, config.isOverwriteSystemBundles() );
        verify( resolver );
    }

    /**
     * Test that default value is false.
     */
    @Test
    public void isOverwriteSystemBundlesDefault()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.overwriteSystemBundles" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Overwrite system bundles", false, config.isOverwriteSystemBundles() );
        verify( resolver );
    }

    // normal flow
    @Test
    public void isDebugClassLoading()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.debugClassLoading" ) ).andReturn( "true" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "DebugClassLoading", true, config.isDebugClassLoading() );
        verify( resolver );
    }

    // test that an invalid value will not cause problems and will return false
    @Test
    public void isDebugClassLoadingWithInvalidValue()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.debugClassLoading" ) ).andReturn( "of course" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "DebugClassLoading", false, config.isDebugClassLoading() );
        verify( resolver );
    }

    // default value should be false
    @Test
    public void isDebugClassLoadingDefault()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.debugClassLoading" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "DebugClassLoading", false, config.isDebugClassLoading() );
        verify( resolver );
    }

    /**
     * Test that default value is true.
     */
    @Test
    public void isDownloadFeebackDefault()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.downloadFeedback" ) ).andReturn( null );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Download feedback", true, config.isDownloadFeedback() );
        verify( resolver );
    }

    /**
     * Test normal flow
     */
    @Test
    public void isDownloadFeedback()
    {
        Resolver resolver = createMock( Resolver.class );
        expect( resolver.get( "org.ops4j.pax.runner.platform.downloadFeedback" ) ).andReturn( "false" );

        replay( resolver );
        Configuration config = new ConfigurationImpl( resolver );
        assertEquals( "Download feedback", false, config.isDownloadFeedback() );
        verify( resolver );
    }
}
