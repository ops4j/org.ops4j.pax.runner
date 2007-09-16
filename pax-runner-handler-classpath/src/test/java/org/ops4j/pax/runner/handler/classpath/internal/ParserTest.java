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
package org.ops4j.pax.runner.handler.classpath.internal;

import java.net.MalformedURLException;
import static org.junit.Assert.*;
import org.junit.Test;

public class ParserTest
{

    @Test( expected = MalformedURLException.class )
    public void constructorWithNullPath()
        throws MalformedURLException
    {
        new Parser( null );
    }

    @Test( expected = MalformedURLException.class )
    public void urlEmpty()
        throws MalformedURLException
    {
        new Parser( "" );
    }

    @Test
    public void getResourceNameWithoutLeadingSlash()
        throws MalformedURLException
    {
        assertEquals( "Resource name", "resource", new Parser( "resource" ).getResourceName() );
    }

    @Test
    public void getResourceNameWithLeadingSlash()
        throws MalformedURLException
    {
        assertEquals( "Resource name", "resource", new Parser( "/resource" ).getResourceName() );
    }

    @Test
    public void getResourceNameWithContainingSlash()
        throws MalformedURLException
    {
        assertEquals( "Resource name", "somewhere/resource", new Parser( "somewhere/resource" ).getResourceName() );
    }

}
