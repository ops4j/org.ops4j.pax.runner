/*
 * Copyright 2007 Damian Golda.
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
package org.ops4j.pax.runner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

public class EquinoxConfiguratorTest extends TestCase
{
    public void testRegexp()
    {
        EquinoxConfigurator configurator = new EquinoxConfigurator();
        String line = "org.eclipse:osgi:3.2.1.R32x_v20060717";
        String[] segments = configurator.splitLine(line);
        Assert.assertNotNull(segments);
        Assert.assertEquals(3, segments.length);
        Assert.assertEquals("org.eclipse", segments[0]);
        Assert.assertEquals("osgi", segments[1]);
        Assert.assertEquals("3.2.1.R32x_v20060717", segments[2]);

    }

    public void testNoFileNoResource() throws IOException
    {
        EquinoxConfigurator configurator = new EquinoxConfigurator();
        String fileName = "non-existient-file.txt";
        String resourceName = "/non-existient-resource.txt";
        try
        {
            configurator.load(new File(fileName), resourceName);
            Assert.fail("Expected exception");
        } catch (IllegalStateException e)
        {
            Assert.assertTrue(true);
        }
    }


    public void testDefaultResource() throws IOException
    {
        GroupArtifactVersion expected = new GroupArtifactVersion("org.eclipse", "osgi", "3.2.1.R32x_v20060717");
        EquinoxConfigurator configurator = new EquinoxConfigurator();
        String fileName = "non-existient-file.txt";
        String resourceName = "/equinox.txt";
        configurator.load(new File(fileName), resourceName);
        Assert.assertEquals(expected, configurator.getSystemBundleArtifact());
        List<GroupArtifactVersion> bundles = configurator.getBundleArtifacts();
        Assert.assertNotNull(bundles);
        Assert.assertEquals(2, bundles.size());

        GroupArtifactVersion bundle0 = new GroupArtifactVersion("org.eclipse.osgi", "util", "3.1.100.v20060601");
        Assert.assertEquals(bundle0, bundles.get(0));
        GroupArtifactVersion bundle1 = new GroupArtifactVersion("org.eclipse.osgi", "services", "3.1.100.v20060601");
        Assert.assertEquals(bundle1, bundles.get(1));
    }

    public void testLoad() throws IOException
    {
        GroupArtifactVersion expected = new GroupArtifactVersion("org.eclipse", "osgi", "3.2.1.R32x_v20060717");
        EquinoxConfigurator configurator = new EquinoxConfigurator();
        configurator.load(new File("."));
        Assert.assertEquals(expected, configurator.getSystemBundleArtifact());
        List<GroupArtifactVersion> bundles = configurator.getBundleArtifacts();
        Assert.assertNotNull(bundles);
        Assert.assertEquals(2, bundles.size());

        GroupArtifactVersion bundle0 = new GroupArtifactVersion("org.eclipse.osgi", "util", "3.1.100.v20060601");
        Assert.assertEquals(bundle0, bundles.get(0));
        GroupArtifactVersion bundle1 = new GroupArtifactVersion("org.eclipse.osgi", "services", "3.1.100.v20060601");
        Assert.assertEquals(bundle1, bundles.get(1));
    }

    public void testResource() throws IOException
    {
        GroupArtifactVersion expected = new GroupArtifactVersion("org.eclipse", "osgi", "5.0");
        EquinoxConfigurator configurator = new EquinoxConfigurator();
        String fileName = "non-existient-file.txt";
        String resourceName = "/org/ops4j/pax/runner/EquinoxConfiguratorTest-resource.txt";
        configurator.load(new File(fileName), resourceName);
        Assert.assertEquals(expected, configurator.getSystemBundleArtifact());
        List<GroupArtifactVersion> bundles = configurator.getBundleArtifacts();
        Assert.assertNotNull(bundles);
        Assert.assertEquals(2, bundles.size());

        GroupArtifactVersion bundle0 = new GroupArtifactVersion("org.eclipse.osgi", "util", "5.0");
        Assert.assertEquals(bundle0, bundles.get(0));
        GroupArtifactVersion bundle1 = new GroupArtifactVersion("org.eclipse.osgi", "services", "5.0");
        Assert.assertEquals(bundle1, bundles.get(1));
    }

    public void testFile() throws IOException
    {
        GroupArtifactVersion expected = new GroupArtifactVersion("org.eclipse", "osgi", "6.0");
        EquinoxConfigurator configurator = new EquinoxConfigurator();
        String fileName = "src/test/resources/org/ops4j/pax/runner/EquinoxConfiguratorTest-file.txt";
        String resourceName = "/non-existient-resource.txt";
        configurator.load(new File(fileName), resourceName);
        Assert.assertEquals(expected, configurator.getSystemBundleArtifact());
        List<GroupArtifactVersion> bundles = configurator.getBundleArtifacts();
        Assert.assertNotNull(bundles);
        Assert.assertEquals(2, bundles.size());

        GroupArtifactVersion bundle0 = new GroupArtifactVersion("org.eclipse.osgi", "util", "6.0");
        Assert.assertEquals(bundle0, bundles.get(0));
        GroupArtifactVersion bundle1 = new GroupArtifactVersion("org.eclipse.osgi", "services", "6.0");
        Assert.assertEquals(bundle1, bundles.get(1));
    }

    public void testEmptyFile() throws IOException
    {
        GroupArtifactVersion expected = new GroupArtifactVersion("org.eclipse", "osgi", "6.0");
        EquinoxConfigurator configurator = new EquinoxConfigurator();
        String fileName = "src/test/resources/org/ops4j/pax/runner/EquinoxConfiguratorTest-emptyfile.txt";
        String resourceName = "/non-existient-resource.txt";
        try
        {
            configurator.load(new File(fileName), resourceName);
            Assert.fail("Expected exception");
        } catch (IllegalStateException e)
        {
            Assert.assertTrue(true);
        }
    }

}
