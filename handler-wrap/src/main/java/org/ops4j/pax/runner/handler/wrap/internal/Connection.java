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
package org.ops4j.pax.runner.handler.wrap.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.url.URLUtils;

/**
 * An URLConnection that supports wrap: protocol.<br/>
 * TODO add java doc
 *
 * @author Alin Dreghiciu
 * @see org.ops4j.pax.runner.handler.wrap.internal.Handler
 * @since September 09, 2007
 */
public class Connection
    extends URLConnection
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( Connection.class );

    /**
     * Parsed url.
     */
    private Parser m_parser;
    /**
     * Service configuration.
     */
    private final Configuration m_configuration;

    /**
     * Creates a new connection.
     *
     * @param url           the url; cannot be null.
     * @param configuration service configuration; cannot be null
     *
     * @throws MalformedURLException in case of a malformed url
     */
    public Connection( final URL url, final Configuration configuration )
        throws MalformedURLException
    {
        super( url );
        Assert.notNull( "URL cannot be null", url );
        Assert.notNull( "Service configuration", configuration );
        m_configuration = configuration;
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
        // do nothing
    }

    /**
     * Returns the input stream denoted by the url.<br/>
     * If the url does not contain a repository the resource is searched in every repository if available, in the order
     * provided by the repository setting.
     *
     * @return the input stream for the resource denoted by url
     *
     * @throws IOException in case of an exception during accessing the resource
     * @see java.net.URLConnection#getInputStream()
     */
    @Override
    public InputStream getInputStream()
        throws IOException
    {
        connect();
        // TODO implement wrapping
        return null;
    }

    /**
     * Prepare url for authentication if necessary and returns the input stream from the url.
     *
     * @param url url to prepare
     *
     * @return input stream from url
     *
     * @throws IOException re-thrown
     */
    private InputStream prepareInputStream( final URL url )
        throws IOException
    {
        URLConnection conn = url.openConnection();
        URLUtils.prepareForAuthentication( conn );
        if ( !m_configuration.getCertificateCheck() )
        {
            URLUtils.prepareForSSL( conn );
        }
        return conn.getInputStream();
    }

}
