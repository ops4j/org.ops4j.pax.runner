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
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.util.property.PropertyResolver;

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
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.definitionURL" ) ).andReturn(
            "file:definition.xml"
        );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Definition URL", new URL( "file:definition.xml" ), config.getDefinitionURL() );
        verify( propertyResolver );
    }

    // expect a MalformedURLException when the property value is not good.
    @Test( expected = MalformedURLException.class )
    public void getMalformedDefinitionURL()
        throws MalformedURLException
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.definitionURL" ) ).andReturn(
            "xxx:definition.xml"
        );

        replay( propertyResolver );
        new ConfigurationImpl( propertyResolver ).getDefinitionURL();
        verify( propertyResolver );
    }

    // test that it does not crash when definition url property is not set
    @Test
    public void getNotConfiguredDefinitionURL()
        throws MalformedURLException
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.definitionURL" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Definition URL", null, config.getDefinitionURL() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void getWorkingDirectory()
        throws MalformedURLException
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.workingDirectory" ) ).andReturn(
            "myWorkingDirectory"
        );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Working directory", "myWorkingDirectory", config.getWorkingDirectory() );
        verify( propertyResolver );
    }

    // test that it returns "runner" when property is not set
    @Test
    public void getDefualtWorkingDirectory()
        throws MalformedURLException
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.workingDirectory" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Default working directory", "runner", config.getWorkingDirectory() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void getSystemPackages()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.systemPackages" ) ).andReturn( "systemPackages" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "System packages", "systemPackages", config.getSystemPackages() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void getExecutionEnvironment()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.ee" ) ).andReturn( "some-ee" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Execution environment", "some-ee", config.getExecutionEnvironment() );
        verify( propertyResolver );
    }

    // return the ee based on jvm version if option not set
    @Test
    public void getDefaultJavaVersion()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.ee" ) ).andReturn( null );
        String javaVersion = System.getProperty( "java.version" ).substring( 0, 3 );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Execution environment", "J2SE-" + javaVersion, config.getExecutionEnvironment() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void getJavaHome()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.javaHome" ) ).andReturn( "javaHome" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Java home", "javaHome", config.getJavaHome() );
        verify( propertyResolver );
    }

    // returns the home of the current running version
    @Test
    public void getDefaultJavaHome()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.javaHome" ) ).andReturn( null );
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
        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Java home", javaHome, config.getJavaHome() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void getProfileStartLevel()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.profileStartLevel" ) ).andReturn( "10" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Platform start level", Integer.valueOf( 10 ), config.getProfileStartLevel() );
        verify( propertyResolver );
    }

    // default platform bundles start level is 1
    @Test
    public void getDefaultProfileStartLevel()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.profileStartLevel" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Platform start level", Integer.valueOf( 1 ), config.getProfileStartLevel() );
        verify( propertyResolver );
    }

    // does not crash with an invalid value and returns the default value 1
    @Test
    public void getDefaultProfileStartLevelIfOptionIsInvalid()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.profileStartLevel" ) ).andReturn( "invalid" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Platform start level", Integer.valueOf( 1 ), config.getProfileStartLevel() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void getStartLevel()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.startLevel" ) ).andReturn( "10" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Platform start level", Integer.valueOf( 10 ), config.getStartLevel() );
        verify( propertyResolver );
    }

    // default start level is 6
    @Test
    public void getDefaultStartLevel()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.startLevel" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Platform start level", Integer.valueOf( 6 ), config.getStartLevel() );
        verify( propertyResolver );
    }

    // does not crash with an invalid value and returns the default value 6
    @Test
    public void getDefaultStartLevelIfOptionIsInvalid()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.startLevel" ) ).andReturn( "invalid" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Platform start level", Integer.valueOf( 6 ), config.getStartLevel() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void getBundleStartLevel()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.bundleStartLevel" ) ).andReturn( "10" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Bundles start level", Integer.valueOf( 10 ), config.getBundleStartLevel() );
        verify( propertyResolver );
    }

    // default start level is 6
    @Test
    public void getDefaultBundleStartLevel()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.bundleStartLevel" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Bundles start level", Integer.valueOf( 5 ), config.getBundleStartLevel() );
        verify( propertyResolver );
    }

    // does not crash with an invalid value and returns the default value 6
    @Test
    public void getDefaultBundleStartLevelIfOptionIsInvalid()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.bundleStartLevel" ) ).andReturn( "invalid" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Bundles start level", Integer.valueOf( 5 ), config.getBundleStartLevel() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void usePersistedState()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.usePersistedState" ) ).andReturn( "true" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Use persisted state", true, config.usePersistedState() );
        verify( propertyResolver );
    }

    // test that an invalid value will not cause problems and will return false
    @Test
    public void usePersistedStateWithInvalidValue()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.usePersistedState" ) ).andReturn( "of course" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Use persisted state", false, config.usePersistedState() );
        verify( propertyResolver );
    }

    // test that if value is not set it will not cause problems and will return false
    @Test
    public void usePersistedStateDefaultValue()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.usePersistedState" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Use persisted state", false, config.usePersistedState() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void startConsole()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.console" ) ).andReturn( "true" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Start console", true, config.startConsole() );
        verify( propertyResolver );
    }

    // test that an invalid value will not cause problems and will return true.
    @Test
    public void startConsoleWithInvalidValue()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.console" ) ).andReturn( "of course" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Start console", false, config.startConsole() );
        verify( propertyResolver );
    }

    // test that an not set value (null) will not cause problems and will return true.
    @Test
    public void startConsoleWithNotSetValue()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.console" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Start console", true, config.startConsole() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void getProfiles()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.profiles" ) ).andReturn( "myProfile" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Profiles", "myProfile", config.getProfiles() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void getFrameworkProfile()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.frameworkProfile" ) ).andReturn( "myProfile" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Framework profile", "myProfile", config.getFrameworkProfile() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void getDefaultFrameworkProfile()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.frameworkProfile" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Framework profile", "runner", config.getFrameworkProfile() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void isOverwrite()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.overwrite" ) ).andReturn( "true" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Overwrite", true, config.isOverwrite() );
        verify( propertyResolver );
    }

    // test that an invalid value will not cause problems and will return false
    @Test
    public void isOverwriteWithInvalidValue()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.overwrite" ) ).andReturn( "of course" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Overwrite", false, config.isOverwrite() );
        verify( propertyResolver );
    }

    // default value should be false
    @Test
    public void isOverwriteDefault()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.overwrite" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Overwrite", false, config.isOverwrite() );
        verify( propertyResolver );
    }

    /**
     * Tests the happy path.
     */
    @Test
    public void clean()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.clean" ) ).andReturn( "true" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Clean", true, config.isCleanStart() );
        verify( propertyResolver );
    }

    /**
     * Tests that default value is false.
     */
    @Test
    public void cleanDefault()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.clean" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Clean", false, config.isCleanStart() );
        verify( propertyResolver );
    }

    /**
     * Test that an invalid value will not cause problems and will return false.
     */
    @Test
    public void cleanWithInvalidValue()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.clean" ) ).andReturn( "of course" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Clean", false, config.isCleanStart() );
        verify( propertyResolver );
    }

    /**
     * Tests that if vm options is set and contains only one option the correct array is returned.
     */
    @Test
    public void getVMOptionsWithOneOption()
        throws MalformedURLException
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.vmOptions" ) ).andReturn( "-Xmx512m" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertArrayEquals( "Working directory", new String[]{ "-Xmx512m" }, config.getVMOptions() );
        verify( propertyResolver );
    }

    /**
     * Tests that if vm options is set and contains more then one option the correct array is returned.
     */
    @Test
    public void getVMOptionsWithMoreOptions()
        throws MalformedURLException
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.vmOptions" ) ).andReturn( "-Xmx512m -Xms512m" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertArrayEquals( "Working directory", new String[]{ "-Xmx512m", "-Xms512m" }, config.getVMOptions() );
        verify( propertyResolver );
    }

    /**
     * Tests tthat if vm options is not set there is no exception (as NPE) and null is returned.
     */
    @Test
    public void getVMOptionsNotSet()
        throws MalformedURLException
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.vmOptions" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertArrayEquals( "Working directory", null, config.getVMOptions() );
        verify( propertyResolver );
    }

    /**
     * Test normal flow
     */
    @Test
    public void isOverwriteUserBundles()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.overwriteUserBundles" ) ).andReturn( "true" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Overwrite user bundles", true, config.isOverwriteUserBundles() );
        verify( propertyResolver );
    }

    /**
     * Test that an invalid value will not cause problems and will return false
     */
    @Test
    public void isOverwriteUserBundlesWithInvalidValue()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.overwriteUserBundles" ) ).andReturn( "of course" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Overwrite user bundles", false, config.isOverwriteUserBundles() );
        verify( propertyResolver );
    }

    /**
     * Test that default value is false.
     */
    @Test
    public void isOverwriteUserBundlesDefault()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.overwriteUserBundles" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Overwrite user bundles", false, config.isOverwriteUserBundles() );
        verify( propertyResolver );
    }

    /**
     * Test normal flow
     */
    @Test
    public void isOverwriteSystemBundles()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.overwriteSystemBundles" ) ).andReturn( "true" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Overwrite system bundles", true, config.isOverwriteSystemBundles() );
        verify( propertyResolver );
    }

    /**
     * Test that an invalid value will not cause problems and will return false
     */
    @Test
    public void isOverwriteSystemBundlesWithInvalidValue()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.overwriteSystemBundles" ) ).andReturn(
            "of course"
        );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Overwrite system bundles", false, config.isOverwriteSystemBundles() );
        verify( propertyResolver );
    }

    /**
     * Test that default value is false.
     */
    @Test
    public void isOverwriteSystemBundlesDefault()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.overwriteSystemBundles" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Overwrite system bundles", false, config.isOverwriteSystemBundles() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void isDebugClassLoading()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.debugClassLoading" ) ).andReturn( "true" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "DebugClassLoading", true, config.isDebugClassLoading() );
        verify( propertyResolver );
    }

    // test that an invalid value will not cause problems and will return false
    @Test
    public void isDebugClassLoadingWithInvalidValue()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.debugClassLoading" ) ).andReturn( "of course" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "DebugClassLoading", false, config.isDebugClassLoading() );
        verify( propertyResolver );
    }

    // default value should be false
    @Test
    public void isDebugClassLoadingDefault()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.debugClassLoading" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "DebugClassLoading", false, config.isDebugClassLoading() );
        verify( propertyResolver );
    }

    /**
     * Test that default value is true.
     */
    @Test
    public void isDownloadFeebackDefault()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.downloadFeedback" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Download feedback", true, config.isDownloadFeedback() );
        verify( propertyResolver );
    }

    /**
     * Test normal flow
     */
    @Test
    public void isDownloadFeedback()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.downloadFeedback" ) ).andReturn( "false" );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Download feedback", false, config.isDownloadFeedback() );
        verify( propertyResolver );
    }

    // normal flow
    @Test
    public void getBootDelegation()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.bootDelegation" ) ).andReturn( " javax.* " );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Boot Delegation", "javax.*", config.getBootDelegation() );
        verify( propertyResolver );
    }

    /**
     * Test that returned value is null when option is not set.
     */
    @Test
    public void getBootDelegationWhenOptionIsNotSet()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.bootDelegation" ) ).andReturn( null );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Boot Delegation", null, config.getBootDelegation() );
        verify( propertyResolver );
    }

    /**
     * Test that returned value is null when option is not empty.
     * Also tests that the spaces are trimmed.
     */
    @Test
    public void getBootDelegationWhenOptionIsEmpty()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.bootDelegation" ) ).andReturn( "  " );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Boot Delegation", null, config.getBootDelegation() );
        verify( propertyResolver );
    }

    /**
     * Test that returned value is null when option is set but contains only a comma.
     * Also tests that the spaces are trimmed.
     */
    @Test
    public void getBootDelegationWhenOptionIsOnlyComma()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.bootDelegation" ) ).andReturn( " , " );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Boot Delegation", null, config.getBootDelegation() );
        verify( propertyResolver );
    }

    /**
     * Test that returned value is null when option ends with a comma.
     * Also tests that the spaces are trimmed.
     */
    @Test
    public void getBootDelegationWhenOptionEndsWithComma()
    {
        PropertyResolver propertyResolver = createMock( PropertyResolver.class );
        expect( propertyResolver.get( "org.ops4j.pax.runner.platform.bootDelegation" ) ).andReturn( " javax.*, " );

        replay( propertyResolver );
        Configuration config = new ConfigurationImpl( propertyResolver );
        assertEquals( "Boot Delegation", "javax.*", config.getBootDelegation() );
        verify( propertyResolver );
    }

}
