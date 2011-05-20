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

import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.runner.platform.BundleReference;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PlatformDefinitionImplTest
{

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullInputStream()
        throws IOException, ParserConfigurationException, SAXException
    {
        new PlatformDefinitionImpl( null, 10 );
    }

    @Test( expected = SAXException.class )
    public void constructorWithANonXMLInputStream()
        throws IOException, ParserConfigurationException, SAXException
    {
        new PlatformDefinitionImpl( new ByteArrayInputStream( "this is not xml".getBytes() ), 10 );
    }

    @Test
    public void getPlatformBundlesForDefaultProfileWithNull()
        throws IOException, ParserConfigurationException, SAXException
    {
        PlatformDefinition definition = new PlatformDefinitionImpl(
            FileUtils.getFileFromClasspath( "platformdefinition/definition.xml" ).toURL().openStream(),
            10
        );
        assertEquals( "System package name", "System package name", definition.getSystemPackageName() );
        assertEquals( "System package", new URL( "file:system.jar" ), definition.getSystemPackage() );
        assertEquals( "System packages", "package.1,package.2", definition.getPackages() );
        // verify default profile
        List<BundleReference> references = definition.getPlatformBundles( null );
        assertNotNull( "Bundle references from default profile cannot be null", references );
        assertEquals( "Number of bundle references from default profile", 2, references.size() );
        assertEquals( "Bundle 1 name from default profile", "Bundle 1", references.get( 0 ).getName() );
        assertEquals( "Bundle 1 url from default profile", new URL( "file:bundle1.jar" ), references.get( 0 ).getURL()
        );
        assertEquals( "Bundle 2 name from default profile", "file:bundle2.jar", references.get( 1 ).getName() );
        assertEquals( "Bundle 2 url from default profile", new URL( "file:bundle2.jar" ), references.get( 1 ).getURL()
        );
    }

    @Test
    public void getPlatformBundlesForDefaultProfileWithEmpty()
        throws IOException, ParserConfigurationException, SAXException
    {
        PlatformDefinition definition = new PlatformDefinitionImpl(
            FileUtils.getFileFromClasspath( "platformdefinition/definition.xml" ).toURL().openStream(),
            10
        );
        assertEquals( "System package name", "System package name", definition.getSystemPackageName() );
        assertEquals( "System package", new URL( "file:system.jar" ), definition.getSystemPackage() );
        assertEquals( "System packages", "package.1,package.2", definition.getPackages() );
        // verify default profile
        List<BundleReference> references = definition.getPlatformBundles( " " );
        assertNotNull( "Bundle references from default profile cannot be null", references );
        assertEquals( "Number of bundle references from default profile", 2, references.size() );
        assertEquals( "Bundle 1 name from default profile", "Bundle 1", references.get( 0 ).getName() );
        assertEquals( "Bundle 1 url from default profile", new URL( "file:bundle1.jar" ), references.get( 0 ).getURL()
        );
        assertEquals( "Bundle 2 name from default profile", "file:bundle2.jar", references.get( 1 ).getName() );
        assertEquals( "Bundle 2 url from default profile", new URL( "file:bundle2.jar" ), references.get( 1 ).getURL()
        );
    }

    @Test
    public void getPlatformBundlesForOneProfile()
        throws IOException, ParserConfigurationException, SAXException
    {
        PlatformDefinition definition = new PlatformDefinitionImpl(
            FileUtils.getFileFromClasspath( "platformdefinition/definition.xml" ).toURL().openStream(),
            10
        );
        assertEquals( "System package name", "System package name", definition.getSystemPackageName() );
        assertEquals( "System package", new URL( "file:system.jar" ), definition.getSystemPackage() );
        assertEquals( "System packages", "package.1,package.2", definition.getPackages() );
        List<BundleReference> references = definition.getPlatformBundles( "extended1" );
        assertNotNull( "Bundle references cannot be null", references );
        assertEquals( "Number of bundle references", 3, references.size() );
        assertEquals( "Bundle 1 name", "Bundle 1", references.get( 0 ).getName() );
        assertEquals( "Bundle 1 url", new URL( "file:bundle1.jar" ), references.get( 0 ).getURL() );
        assertEquals( "Bundle 2 name", "file:bundle2.jar", references.get( 1 ).getName() );
        assertEquals( "Bundle 2 urle", new URL( "file:bundle2.jar" ), references.get( 1 ).getURL() );
        assertEquals( "Bundle 3 name", "Bundle 3", references.get( 2 ).getName() );
        assertEquals( "Bundle 3 url", new URL( "file:bundle3.jar" ), references.get( 2 ).getURL() );
    }

    @Test
    public void getPlatformBundlesForMoreProfiles()
        throws IOException, ParserConfigurationException, SAXException
    {
        PlatformDefinition definition = new PlatformDefinitionImpl(
            FileUtils.getFileFromClasspath( "platformdefinition/definition.xml" ).toURL().openStream(),
            10
        );
        assertEquals( "System package name", "System package name", definition.getSystemPackageName() );
        assertEquals( "System package", new URL( "file:system.jar" ), definition.getSystemPackage() );
        assertEquals( "System packages", "package.1,package.2", definition.getPackages() );
        List<BundleReference> references = definition.getPlatformBundles( "extended1,profile2" );
        assertNotNull( "Bundle references cannot be null", references );
        assertEquals( "Number of bundle references", 4, references.size() );
        assertEquals( "Bundle 1 name", "Bundle 1", references.get( 0 ).getName() );
        assertEquals( "Bundle 1 url", new URL( "file:bundle1.jar" ), references.get( 0 ).getURL() );
        assertEquals( "Bundle 2 name", "file:bundle2.jar", references.get( 1 ).getName() );
        assertEquals( "Bundle 2 urle", new URL( "file:bundle2.jar" ), references.get( 1 ).getURL() );
        assertEquals( "Bundle 3 name", "Bundle 3", references.get( 2 ).getName() );
        assertEquals( "Bundle 3 url", new URL( "file:bundle3.jar" ), references.get( 2 ).getURL() );
        assertEquals( "Bundle 4 name", "Bundle 4", references.get( 3 ).getName() );
        assertEquals( "Bundle 4 url", new URL( "file:bundle4.jar" ), references.get( 3 ).getURL() );
    }

    @Test
    public void getPlatformBundlesForOverlapingProfiles()
        throws IOException, ParserConfigurationException, SAXException
    {
        PlatformDefinition definition = new PlatformDefinitionImpl(
            FileUtils.getFileFromClasspath( "platformdefinition/definition.xml" ).toURL().openStream(),
            10
        );
        assertEquals( "System package name", "System package name", definition.getSystemPackageName() );
        assertEquals( "System package", new URL( "file:system.jar" ), definition.getSystemPackage() );
        List<BundleReference> references = definition.getPlatformBundles( "extended1,overlaping2" );
        assertNotNull( "Bundle references cannot be null", references );
        assertEquals( "Number of bundle references", 4, references.size() );
        assertEquals( "Bundle 1 name", "Bundle 1", references.get( 0 ).getName() );
        assertEquals( "Bundle 1 url", new URL( "file:bundle1.jar" ), references.get( 0 ).getURL() );
        assertEquals( "Bundle 2 name", "file:bundle2.jar", references.get( 1 ).getName() );
        assertEquals( "Bundle 2 urle", new URL( "file:bundle2.jar" ), references.get( 1 ).getURL() );
        assertEquals( "Bundle 3 name", "Bundle 3", references.get( 2 ).getName() );
        assertEquals( "Bundle 3 url", new URL( "file:bundle3.jar" ), references.get( 2 ).getURL() );
        assertEquals( "Bundle 5 name", "Bundle 5", references.get( 3 ).getName() );
        assertEquals( "Bundle 5 url", new URL( "file:bundle5.jar" ), references.get( 3 ).getURL() );
    }

    // test that just the valid profile is returned and the invalid ones are ignored
    @Test
    public void getPlatformBundlesForOneValidAndsomeInvalidProfiles()
        throws IOException, ParserConfigurationException, SAXException
    {
        PlatformDefinition definition = new PlatformDefinitionImpl(
            FileUtils.getFileFromClasspath( "platformdefinition/definition.xml" ).toURL().openStream(),
            10
        );
        assertEquals( "System package name", "System package name", definition.getSystemPackageName() );
        assertEquals( "System package", new URL( "file:system.jar" ), definition.getSystemPackage() );
        assertEquals( "System packages", "package.1,package.2", definition.getPackages() );
        // verify default profile
        List<BundleReference> references = definition.getPlatformBundles( "ivalid1,extended1,invalid2" );
        assertNotNull( "Bundle references cannot be null", references );
        assertEquals( "Number of bundle references", 3, references.size() );
        assertEquals( "Bundle 1 name", "Bundle 1", references.get( 0 ).getName() );
        assertEquals( "Bundle 1 url", new URL( "file:bundle1.jar" ), references.get( 0 ).getURL() );
        assertEquals( "Bundle 2 name", "file:bundle2.jar", references.get( 1 ).getName() );
        assertEquals( "Bundle 2 urle", new URL( "file:bundle2.jar" ), references.get( 1 ).getURL() );
        assertEquals( "Bundle 3 name", "Bundle 3", references.get( 2 ).getName() );
        assertEquals( "Bundle 3 url", new URL( "file:bundle3.jar" ), references.get( 2 ).getURL() );
    }

    // test that default profile is returned if there is no valid profile in the list
    @Test
    public void getPlatformBundlesForOnlyInvalidProfiles()
        throws IOException, ParserConfigurationException, SAXException
    {
        PlatformDefinition definition = new PlatformDefinitionImpl(
            FileUtils.getFileFromClasspath( "platformdefinition/definition.xml" ).toURL().openStream(),
            10
        );
        assertEquals( "System package name", "System package name", definition.getSystemPackageName() );
        assertEquals( "System package", new URL( "file:system.jar" ), definition.getSystemPackage() );
        assertEquals( "System packages", "package.1,package.2", definition.getPackages() );
        // verify default profile
        List<BundleReference> references = definition.getPlatformBundles( "invalid1,,invalid2" );
        assertNotNull( "Bundle references from default profile cannot be null", references );
        assertEquals( "Number of bundle references from default profile", 2, references.size() );
        assertEquals( "Bundle 1 name from default profile", "Bundle 1", references.get( 0 ).getName() );
        assertEquals( "Bundle 1 url from default profile", new URL( "file:bundle1.jar" ), references.get( 0 ).getURL()
        );
        assertEquals( "Bundle 2 name from default profile", "file:bundle2.jar", references.get( 1 ).getName() );
        assertEquals( "Bundle 2 url from default profile", new URL( "file:bundle2.jar" ), references.get( 1 ).getURL()
        );
    }

    @Test
    public void getPlatformBundlesWithMultipleInheritance()
        throws IOException, ParserConfigurationException, SAXException
    {
        PlatformDefinition definition = new PlatformDefinitionImpl(
            FileUtils.getFileFromClasspath( "platformdefinition/definition.xml" ).toURL().openStream(),
            10
        );
        assertEquals( "System package name", "System package name", definition.getSystemPackageName() );
        assertEquals( "System package", new URL( "file:system.jar" ), definition.getSystemPackage() );
        assertEquals( "System packages", "package.1,package.2", definition.getPackages() );
        List<BundleReference> references = definition.getPlatformBundles( "multiple" );
        assertNotNull( "Bundle references cannot be null", references );
        assertEquals( "Number of bundle references", 5, references.size() );
        assertEquals( "Bundle 1 name", "Bundle 1", references.get( 0 ).getName() );
        assertEquals( "Bundle 1 url", new URL( "file:bundle1.jar" ), references.get( 0 ).getURL() );
        assertEquals( "Bundle 2 name", "file:bundle2.jar", references.get( 1 ).getName() );
        assertEquals( "Bundle 2 urle", new URL( "file:bundle2.jar" ), references.get( 1 ).getURL() );
        assertEquals( "Bundle 3 name", "Bundle 3", references.get( 2 ).getName() );
        assertEquals( "Bundle 3 url", new URL( "file:bundle3.jar" ), references.get( 2 ).getURL() );
        assertEquals( "Bundle 4 name", "Bundle 4", references.get( 3 ).getName() );
        assertEquals( "Bundle 4 url", new URL( "file:bundle4.jar" ), references.get( 3 ).getURL() );
        assertEquals( "Bundle 6 name", "Bundle 6", references.get( 4 ).getName() );
        assertEquals( "Bundle 6 url", new URL( "file:bundle6.jar" ), references.get( 4 ).getURL() );
    }

    @Test
    public void getPlatformBundlesForDefaultProfileWithOptions()
        throws IOException, ParserConfigurationException, SAXException
    {
        PlatformDefinition definition = new PlatformDefinitionImpl(
            FileUtils.getFileFromClasspath( "platformdefinition/definition_ex.xml" ).toURL().openStream(),
            10
        );
        assertEquals( "System package name", "System package name", definition.getSystemPackageName() );
        assertEquals( "System package", new URL( "file:system.jar" ), definition.getSystemPackage() );
        assertEquals( "System packages", "package.1,package.2", definition.getPackages() );
        // verify default profile
        List<BundleReference> references = definition.getPlatformBundles( null );
        assertNotNull( "Bundle references from default profile cannot be null", references );
        assertEquals( "Number of bundle references from default profile", 4, references.size() );
        assertEquals( "Bundle 1 name from default profile", "Bundle 1", references.get( 0 ).getName() );
        assertEquals( "Bundle 1 url from default profile", new URL( "file:bundle1.jar" ), references.get( 0 ).getURL()
        );
        assertEquals( "Bundle 1 @nostart from default profile", false, references.get( 0 ).shouldStart()
        );
        assertEquals( "Bundle 2 name from default profile", "file:bundle2.jar", references.get( 1 ).getName() );
        assertEquals( "Bundle 2 url from default profile", new URL( "file:bundle2.jar" ), references.get( 1 ).getURL()
        );
        assertEquals( "Bundle 2 @3 level from default profile", new Integer(3), references.get( 1 ).getStartLevel());
        assertEquals( "Bundle 3 name from default profile", "file:bundle3.jar", references.get( 2 ).getName() );
        assertEquals( "Bundle 3 url from default profile", new URL( "file:bundle3.jar" ), references.get( 2 ).getURL()
        );
        assertEquals( "Bundle 3 @3 level from default profile", new Integer(3), references.get( 2 ).getStartLevel());
        assertEquals( "Bundle 3 @update from default profile", true, references.get( 2 ).shouldUpdate());
        assertEquals( "Bundle 4 name from default profile", "file:bundle4.jar", references.get( 3 ).getName() );
        assertEquals( "Bundle 4 url from default profile", new URL( "file:bundle4.jar" ), references.get( 3 ).getURL()
        );
        assertEquals( "Bundle 4 default level from default profile", new Integer(10), references.get( 3 ).getStartLevel());
        assertEquals( "Bundle 4 @start TRUE from default profile", true, references.get( 3 ).shouldStart());
        assertEquals( "Bundle 4 @update FALSE from default profile", false, references.get( 3 ).shouldUpdate());
    }
}
