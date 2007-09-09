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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.osgi.service.url.URLStreamHandlerService;

public class URLStreamHandlerExtenderTest
{

    // it should just run without any exception
    @Test
    public void constructor()
    {
        new URLStreamHandlerExtender();
    }

    // be defensive
    @Test( expected = IllegalArgumentException.class )
    public void registerWithNullProtocol()
    {
        new URLStreamHandlerExtender().register( null, createMock( URLStreamHandlerService.class ) );
    }

    // be defensive
    @Test( expected = IllegalArgumentException.class )
    public void registerWithEmptyProtocol()
    {
        new URLStreamHandlerExtender().register( new String[]{ " " }, createMock( URLStreamHandlerService.class ) );
    }

    // be defensive
    @Test( expected = IllegalArgumentException.class )
    public void registerWithNullService()
    {
        new URLStreamHandlerExtender().register( new String[]{ "protocol" }, null );
    }

    @Test
    public void register()
    {
        new URLStreamHandlerExtender().register( new String[]{ "protocol" },
                                                 createMock( URLStreamHandlerService.class )
        );
    }

    // be defensive
    @Test( expected = IllegalArgumentException.class )
    public void unregisterWithNullProtocol()
    {
        new URLStreamHandlerExtender().unregister( null );
    }

    // be defensive
    @Test( expected = IllegalArgumentException.class )
    public void unregisterWithEmptyProtocol()
    {
        new URLStreamHandlerExtender().unregister( new String[]{ " " } );
    }

    //
    @Test
    public void unregister()
    {
        new URLStreamHandlerExtender().unregister( new String[]{ "protocol" } );
    }

    // actually this should never happen but just be defensive
    @Test( expected = IllegalArgumentException.class )
    public void createURLStreamHandlerWithNullProtocol()
    {
        new URLStreamHandlerExtender().createURLStreamHandler( null );
    }

    // actually this should never happen but just be defensive
    @Test( expected = IllegalArgumentException.class )
    public void createURLStreamHandlerWithEmptyProtocol()
    {
        new URLStreamHandlerExtender().createURLStreamHandler( " " );
    }

    // it should return null if there is no registred URLStreamHandlerService for the protocol
    @Test
    public void createURLStreamHandlerWithUnknownProtocol()
    {
        assertNull( "URL stream handler was supposed to be null",
                    new URLStreamHandlerExtender().createURLStreamHandler( "protocol" )
        );
    }

    // should return the registred URLStreamHandlerService
    @Test
    public void createURLStreamHandlerWithKnownProtocol()
    {
        URLStreamHandlerService handler = createMock( URLStreamHandlerService.class );
        final URLStreamHandlerProxy proxy = new URLStreamHandlerProxy( handler );
        URLStreamHandlerExtender extender = new URLStreamHandlerExtender()
        {
            URLStreamHandlerProxy createProxy( final URLStreamHandlerService urlStreamHandlerService )
            {
                return proxy;
            }
        };
        extender.register( new String[]{ "protocol" }, handler );
        assertEquals( "URL stream handler ", proxy, extender.createURLStreamHandler( "protocol" ) );
    }

}
