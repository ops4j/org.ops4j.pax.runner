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

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerSetter;
import org.ops4j.pax.runner.commons.Assert;

/**
 * A proxy that get's registred with the JVM as URLStreamhandler but actualy delegates to the URLStreamHandlerService
 * OSGi style.
 */
public class URLStreamHandlerProxy
    extends URLStreamHandler
    implements URLStreamHandlerSetter
{

    /**
     * The handler to delegate to.
     */
    private final URLStreamHandlerService m_handler;

    /**
     * Creates a new proxy for the protocol.
     *
     * @param handler the handler to delegate to
     */
    public URLStreamHandlerProxy( final URLStreamHandlerService handler )
    {
        Assert.notNull( "URL stream handler service", handler );
        m_handler = handler;
    }

    /**
     * Delegates to handler.
     *
     * @see URLStreamHandler#equals(java.net.URL,java.net.URL)
     */
    @Override
    protected synchronized boolean equals( final URL first, final URL second )
    {
        return m_handler.equals( first, second );
    }

    /**
     * Delegates to handler.
     *
     * @see URLStreamHandler#getDefaultPort()
     */
    @Override
    protected synchronized int getDefaultPort()
    {
        return m_handler.getDefaultPort();
    }

    /**
     * Delegates to handler.
     *
     * @see URLStreamHandler#getHostAddress(java.net.URL)
     */
    @Override
    protected synchronized InetAddress getHostAddress( final URL url )
    {
        return m_handler.getHostAddress( url );
    }

    /**
     * Delegates to handler.
     *
     * @see URLStreamHandler#hashCode(java.net.URL)
     */
    @Override
    protected synchronized int hashCode( final URL url )
    {
        return m_handler.hashCode( url );
    }

    /**
     * Delegates to handler.
     *
     * @see URLStreamHandler#hostsEqual(java.net.URL,java.net.URL)
     */
    @Override
    protected synchronized boolean hostsEqual( URL first, URL second )
    {
        return m_handler.hostsEqual( first, second );
    }

    /**
     * Delegates to handler.
     *
     * @see URLStreamHandler#openConnection(java.net.URL)
     */
    @Override
    protected synchronized URLConnection openConnection( final URL url )
        throws IOException
    {
        return m_handler.openConnection( url );
    }

    /**
     * Delegates to handler.
     *
     * @see URLStreamHandler#parseURL(java.net.URL,String,int,int)
     */
    @Override
    protected synchronized void parseURL( final URL url, final String spec, final int start, final int limit )
    {
        m_handler.parseURL( this, url, spec, start, limit );
    }

    /**
     * Delegates to handler.
     *
     * @see URLStreamHandler#sameFile(java.net.URL,java.net.URL)
     */
    @Override
    protected synchronized boolean sameFile( URL first, URL second )
    {
        return m_handler.sameFile( first, second );
    }

    /**
     * @see URLStreamHandlerSetter#setURL(java.net.URL,String,String,int,String,String,String,String,String)
     */
    @Override
    public void setURL( final URL url, final String protocol, final String host, final int port, final String authority,
                        final String userInfo, final String path, final String query, final String ref )
    {
        super.setURL( url, protocol, host, port, authority, userInfo, path, query, ref );
    }

    /**
     * @see URLStreamHandlerSetter#setURL(java.net.URL,String,String,int,String,String)
     */
    @Override
    public void setURL( final URL url, final String protocol, final String host, final int port, final String file,
                        final String ref )
    {
        super.setURL( url, protocol, host, port, null, null, file, null, ref );
    }

    /**
     * Delegates to handler.
     *
     * @see URLStreamHandler#toExternalForm(java.net.URL)
     */
    @Override
    protected synchronized String toExternalForm( final URL url )
    {
        return m_handler.toExternalForm( url );
    }

}