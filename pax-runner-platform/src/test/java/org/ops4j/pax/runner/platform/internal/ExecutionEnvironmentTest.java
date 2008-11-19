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

import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.runner.platform.PlatformException;

public class ExecutionEnvironmentTest
{

    public void constructorWithNull()
        throws Exception
    {
        new ExecutionEnvironment( null );
    }

    // test returned packages when the ee is an valid url
    @Test
    public void createPackageListWithURLEE()
        throws Exception
    {
        assertEquals(
            "System packages",
            "system.package.1,system.package.2",
            new ExecutionEnvironment(
                FileUtils.getFileFromClasspath( "platform/systemPackages.profile" ).toURL().toExternalForm()
            ).getSystemPackages()
        );
    }

    // test that no packages are in use when ee is NONE
    @Test
    public void createPackageListNONE()
        throws Exception
    {
        assertEquals(
            "System packages",
            "",
            new ExecutionEnvironment(
                "NONE"
            ).getSystemPackages()
        );
    }

    // test returned packages when the ee is a valid url but has incorect letter case
    @Test
    public void createPackageListLetterCase()
        throws Exception
    {
        assertEquals(
            "System packages",
            "javax.microedition.io,javax.microedition.pki,javax.security.auth.x500",
            new ExecutionEnvironment(
                "cdc-1.1/foundation-1.1"
            ).getSystemPackages()
        );
    }

    // test returned packages when the ee is an invalid url
    @Test( expected = PlatformException.class )
    public void createPackageListWithInvalidEE()
        throws Exception
    {
        new ExecutionEnvironment( "invalid" );
    }

    // test returned packages when there are more EEs
    @Test
    public void createPackageListWithMoreEEs()
        throws Exception
    {

        assertEquals(
            "System packages",
            "system.package.1,system.package.2,system.package.3",
            new ExecutionEnvironment(
                FileUtils.getFileFromClasspath( "platform/systemPackages.profile" ).toURL().toExternalForm()
                + ","
                + FileUtils.getFileFromClasspath( "platform/systemPackages2.profile" ).toURL().toExternalForm()
            ).getSystemPackages()
        );
    }

}