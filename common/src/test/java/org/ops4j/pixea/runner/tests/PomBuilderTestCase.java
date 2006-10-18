/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.runner.tests;

import java.io.InputStream;
import junit.framework.TestCase;
import org.ops4j.pax.runner.ServiceManager;
import org.ops4j.pax.runner.pom.Dependency;
import org.ops4j.pax.runner.pom.Model;

public class PomBuilderTestCase extends TestCase
{

    public void testPomBuilder()
        throws Exception
    {
        PomBuilder builder = ServiceManager.getInstance().getService( PomBuilder.class );
        InputStream in = getClass().getResourceAsStream( "pom.xml" );
        assertNotNull( "Can't find test resource: pom.xml", in );
        Model model = builder.parse( in );
        assertEquals( "Pax Runner Common", model.getName() );
        Dependency dep = model.getDependencies().getDependency().get(0);
        assertEquals( "jaxb-api", dep.getArtifactId() );
    }
}
