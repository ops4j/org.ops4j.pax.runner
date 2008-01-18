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

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.url.URLStreamHandlerService;
import org.ops4j.lang.NullArgumentException;

/**
 * An extender that implements URLStreamHandlerFactory.
 * Registers/unregisters URLStreamHandlerService as URLStreamHandler's.
 * Note that this is not full proven URL Handler Service implementation. It does just enough to be used in runner.
 *
 * @author Alin Dreghiciu
 * @see java.net.URLStreamHandlerFactory
 * @since August 28, 2007
 */
public class URLStreamHandlerExtender
    implements URLStreamHandlerFactory
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( Activator.class );
    /**
     * Map between protocol and URLStreamHandlerService proxy.
     */
    private final Map<String, URLStreamHandlerProxy> m_proxies;

    /**
     * Creates a new extender.
     */
    public URLStreamHandlerExtender()
    {
        m_proxies = new HashMap<String, URLStreamHandlerProxy>();
    }

    /**
     * Registres itself to URL as URLStreamHandlerFactory.
     * This is not in the constructor as only once per JVM this could be set so we could not do unit tests.
     */
    public void start()
    {
        URLUtils.setURLStreamHandlerFactory( this );
    }

    /**
     * Registres the URLStreamHandlerService as URLStreamHandler.
     *
     * @param protocols               an array of protocols handled by URLStreamHandlerService
     * @param urlStreamHandlerService an  URLStreamHandlerService
     */
    public void register( final String[] protocols, final URLStreamHandlerService urlStreamHandlerService )
    {
        LOGGER.debug(
            "Registering protocols [" + Arrays.toString( protocols ) + "] to service [" + urlStreamHandlerService + "]"
        );
        NullArgumentException.validateNotEmpty( protocols, "Protocol" );
        NullArgumentException.validateNotNull( urlStreamHandlerService, "URL stream handler service" );
        for( String protocol : protocols )
        {
            m_proxies.put( protocol, createProxy( urlStreamHandlerService ) );
        }

    }

    /**
     * Unregistres the URLStreamHandlerService as URLStreamHandler.
     *
     * @param protocols an array of protocols handled by URLStreamHandlerService
     */
    public void unregister( final String[] protocols )
    {
        LOGGER.debug( "Unregistering protocols [" + Arrays.toString( protocols ) + "]" );
        NullArgumentException.validateNotEmpty( protocols, "Protocols" );
        for( String protocol : protocols )
        {
            m_proxies.remove( protocol );
        }
    }

    /**
     * If there is a registred URLStreamHandlerService for the protocol the URLStreamHandlerService is returned,
     * otherwise will return null, so the JVM will handle as defaut.
     *
     * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(String)
     */
    public URLStreamHandler createURLStreamHandler( final String protocol )
    {
        NullArgumentException.validateNotEmpty( protocol, "Protocol" );
        return m_proxies.get( protocol );
    }

    /**
     * URLStreamHandlerProxy factory method.
     *
     * @param urlStreamHandlerService an URLStreamHandlerService
     *
     * @return a proxy
     */
    URLStreamHandlerProxy createProxy( final URLStreamHandlerService urlStreamHandlerService )
    {
        return new URLStreamHandlerProxy( urlStreamHandlerService );
    }

}
