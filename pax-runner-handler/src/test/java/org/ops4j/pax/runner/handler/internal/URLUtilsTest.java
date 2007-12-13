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
package org.ops4j.pax.runner.handler.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

// the test is now very correct as in setup we use a method that is actually in the class under test

// and if that method crashes or does not work as expected....
public class URLUtilsTest
{

    @Before
    public void setUp()
    {
        URLUtils.resetURLStreamHandlerFactory();
    }

    /**
     * Tests that reset works fine by setting the factory twice.
     * If the reset would not work the secon set would fail.
     *
     * :) Actually the reset method is tested on evry run due to the call on setUp()
     */
    @Test
    public void resetWhenURLStreamHandlerIsNotSet()
    {
        URLStreamHandlerFactory factory = createMock( URLStreamHandlerFactory.class );
        URL.setURLStreamHandlerFactory( factory );
        URLUtils.resetURLStreamHandlerFactory();
        URL.setURLStreamHandlerFactory( factory );
    }

    /**
     * Tests that the get on factory will return the set factory.
     */
    @Test
    public void getURLStreamHandlerFactory()
    {
        URLStreamHandlerFactory factory = createMock( URLStreamHandlerFactory.class );
        URL.setURLStreamHandlerFactory( factory );
        assertEquals( "Factory", factory, URLUtils.getURLStreamHandlerFactory() );
    }

    /**
     * Tests that the get on factory will return the set factory.
     */
    @Test
    public void setURLStreamHandlerFactory()
    {
        URLStreamHandlerFactory factory = createMock( URLStreamHandlerFactory.class );
        URLUtils.setURLStreamHandlerFactory( factory );
        assertEquals( "Factory", factory, URLUtils.getURLStreamHandlerFactory() );
    }

    /**
     * Tests that setting on two factories via utils.
     */
    @Test
    public void setTwiceURLStreamHandlerFactory()
    {
        URLStreamHandlerFactory factory = createMock( URLStreamHandlerFactory.class );
        URLUtils.setURLStreamHandlerFactory( factory );
        URLUtils.setURLStreamHandlerFactory( factory );
        assertEquals( "Factory", CompositeURLStreamHandlerFactory.class,
                      URLUtils.getURLStreamHandlerFactory().getClass()
        );
    }

    /**
     * Tests that composite factory get's setup correctly on two set factory calls.
     */
    @Test
    public void compositeFactorySetup()
    {
        URLStreamHandlerFactory factory1 = createMock( URLStreamHandlerFactory.class );
        URLStreamHandlerFactory factory2 = createMock( URLStreamHandlerFactory.class );
        URLUtils.setURLStreamHandlerFactory( factory1 );
        URLUtils.setURLStreamHandlerFactory( factory2 );
        expect( factory1.createURLStreamHandler( "foo" ) ).andReturn( null );
        expect( factory2.createURLStreamHandler( "foo" ) ).andReturn( null );
        replay( factory1, factory2 );
        try
        {
            new URL( "foo:bar" );
        }
        catch( MalformedURLException ignore )
        {
            // just ignore as we are not interested in this matter
        }
        verify( factory1, factory2 );
    }

}