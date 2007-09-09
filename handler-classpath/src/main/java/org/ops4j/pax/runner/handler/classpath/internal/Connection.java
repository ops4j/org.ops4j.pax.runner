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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.ops4j.pax.runner.commons.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * An URLConnection that supports classpath: protocol.<br/>
 * Syntax:<br/>
 * classpath:[//bundle_symbolic_name/]path_to_resource<br/>
 * where:<br/>
 * ...
 */
public class Connection
    extends URLConnection
{

    /**
     * The protocol name.
     */
    public static final String PROTOCOL = "classpath";
    /**
     * The curent bundle context.
     */
    private final BundleContext m_bundleContext;
    /**
     * URL path parser.
     */
    private Parser m_parser;

    /**
     * Creates a new connection.
     *
     * @param url           the url; cannot be null
     * @param bundleContext the bundle context; cannot be null
     *
     * @throws java.net.MalformedURLException in case of a malformed url
     */
    public Connection( final URL url, final BundleContext bundleContext )
        throws MalformedURLException
    {
        super( url );
        Assert.notNull( "URL", url );
        Assert.notNull( "Bundle context", bundleContext );
        m_bundleContext = bundleContext;
        m_parser = new Parser( url.getPath() );
    }

    /**
     * Does nothing.
     *
     * @see java.net.URLConnection#connect()
     */
    @Override
    public void connect()
    {
        //do nothing
    }

    /**
     * Returns the input stream denoted by the url.
     * Resource resolution:
     * 1. if a bundle symbolic name is present then search the specific bundle. If not found then stop.
     * 2. search the current thread classpath
     * 3. search all bundles if allowed (has permittion)
     *
     * @return the input stream for the resource denoted by url
     *
     * @throws IOException in case of an exception during accessing the resource
     * @see java.net.URLConnection#getInputStream()
     */
    public InputStream getInputStream()
        throws IOException
    {
        connect();
        InputStream is;
        if ( url.getAuthority() != null )
        {
            is = getFromSpecificBundle();
        }
        else
        {
            is = getFromClasspath();
            if ( is == null )
            {
                is = getFromInstalledBundles();
            }
        }
        if ( is == null )
        {
            throw new IOException( "URL [" + m_parser.getResourceName() + "] could not be resolved from classpath" );
        }
        return is;
    }

    /**
     * Searches the resource in a bundle with symbolic name from the url.
     *
     * @return input stream if resource is found in the specified bundle, null othwerwise
     *
     * @throws java.io.IOException re-thrown if thrown by founded resource
     */
    private InputStream getFromSpecificBundle()
        throws IOException
    {
        Bundle[] bundles = getBundles( url.getAuthority() );
        if ( bundles != null && bundles.length > 0 )
        {
            final URL resource = bundles[ 0 ].getResource( m_parser.getResourceName() );
            if ( resource != null )
            {
                return resource.openStream();
            }
        }
        return null;
    }

    /**
     * Searches the resource in the classpath.
     *
     * @return input stream if resource is found in the specified bundle, null othwerwise
     */
    private InputStream getFromClasspath()
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if ( cl != null )
        {
            return cl.getResourceAsStream( m_parser.getResourceName() );
        }
        return null;
    }

    /**
     * Searches the resource in all available bundles.
     *
     * @return input stream if resource is found in the any of the installed bundles, null othwerwise
     *
     * @throws java.io.IOException re-thrown if thrown by founded resource
     */
    private InputStream getFromInstalledBundles()
        throws IOException
    {
        Bundle[] bundles = getBundles( null );
        if ( bundles != null && bundles.length > 0 )
        {
            for ( Bundle bundle : bundles )
            {
                URL resource = bundle.getResource( m_parser.getResourceName() );
                if ( resource != null )
                {
                    return resource.openStream();
                }
            }
        }
        return null;
    }

    /**
     * Returns the list of bundles from bundles context.
     * If symbolic name is not null will return the bundle that has that symbolic name otherwise null.
     *
     * @param symbolicName a specific bundle symbolic name
     *
     * @return an array of bundles
     */
    private Bundle[] getBundles( final String symbolicName )
    {
        final Bundle[] bundles = m_bundleContext.getBundles();
        if ( bundles != null )
        {
            if ( symbolicName != null )
            {
                for ( Bundle bundle : bundles )
                {
                    if ( bundle.getSymbolicName().equals( symbolicName ) )
                    {
                        return new Bundle[]{ bundle };
                    }
                }
            }
        }
        return bundles;
    }

}
