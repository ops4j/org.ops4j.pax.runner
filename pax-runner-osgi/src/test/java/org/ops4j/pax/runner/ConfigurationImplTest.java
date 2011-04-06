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
package org.ops4j.pax.runner;

import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.pax.runner.ConfigurationImpl;

public class ConfigurationImplTest
{

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullConfigFile()
    {
        new ConfigurationImpl( null );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithEmptyConfigFile()
    {
        new ConfigurationImpl( "" );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithInexistentConfigURL()
    {
        new ConfigurationImpl( "aFile.properties" );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithInexistentConfigClasspathURL()
    {
        new ConfigurationImpl( "classpath:aFile.properties" );
    }

    @Test
    public void constructorWithValidClasspathConfiguration()
    {
        Configuration config =
            new ConfigurationImpl( "classpath:org/ops4j/pax/runner/configuration/runner.properties" );
        assertEquals( "platform.test", "org.ops4j.pax.runner.platform.Activator",
                      config.getProperty( "platform.test" )
        );
    }

}
